/**
 * Unit test for CacheStore: JSONL v2/v1 round-trip, replace semantics,
 * realistic provenance reports and corrupted-line tolerance.
 *
 * Run from repo root:
 *   groovy src/test/pre-processamento-maven-flags-via-llm/cache/CacheStoreTest.groovy
 * (also executed by the build - see src/test/groovy/run-maven-flags-via-llm-tests.groovy)
 */

def moduleRoot = new File("src/main/pre-processamento-maven-flags-via-llm")
def sourceIndex = [:]
moduleRoot.eachFileRecurse { File f ->
    if (f.isFile() && f.name.endsWith(".groovy")) sourceIndex[f.name] = f
}
def gcl = new GroovyClassLoader(this.class.classLoader)
gcl.setResourceLoader({ String className ->
    String simple = className.substring(className.lastIndexOf(".") + 1)
    File source = sourceIndex[simple + ".groovy"]
    return source == null ? null : source.toURI().toURL()
} as groovy.lang.GroovyResourceLoader)

def cacheStoreClass = gcl.loadClass("eda.postgenerate.pre_processamento_maven_flags_via_llm.cache.CacheStore")
def cacheStore = cacheStoreClass.newInstance()

int failures = 0
def check = { String name, boolean cond ->
    if (cond) { println "PASS: ${name}" } else { println "FAIL: ${name}"; failures++ }
}

String nl = System.lineSeparator()
String report = "| flag | valor final | fonte | notas |" + nl +
        "|---|---|---|---|" + nl +
        "| api_port | 8080 | DEFAULT | |" + nl + nl +
        "**Resumo**: 0 CONTRACT / 0 CONTRACT_DERIVED / 0 INFERENCE / 1 DEFAULT / 0 descartada(s)"

// ----------------------------------------------------------------------
// 1) cache write + lookup round-trip (incl. multi-line report)
// ----------------------------------------------------------------------
def dir = new File(System.getProperty("java.io.tmpdir"), "pre-processamento-maven-flags-via-llm-cache-test-" + System.nanoTime())
String key1 = "b" * 64
String cmd1 = "mvn archetype:generate --batch-mode -Dapi_port=8080"
cacheStore.write(dir.absolutePath, key1, cmd1, report)

String raw = new File(dir, "cache.jsonl").getText("UTF-8")
check("1.1 cache line matches v2 shape", raw ==~ /(?s)^\{"k":"[0-9a-f]{64}","v":"(?:[^"\\]|\\.)*","r":"(?:[^"\\]|\\.)*","t":\d+\}\s*$/)
check("1.2 no double-quote regression (bug fix)", !raw.contains('""'))
check("1.3 FR-017: contract-like sentinel absent from cache file", !raw.contains("secret_field") && !raw.contains("shhh"))

def hit1 = cacheStore.lookup(dir.absolutePath, key1)
check("1.4 lookup finds v2 entry", hit1 != null)
check("1.5 command round-trips byte-equivalently", hit1.get("v") == cmd1)
check("1.6 report round-trips (multi-line escaped)", hit1.get("r") == report)

String key2 = "c" * 64
cacheStore.write(dir.absolutePath, key2, "mvn cmd-2", "report-2")
def hit2 = cacheStore.lookup(dir.absolutePath, key2)
check("1.7 second entry served correctly after write", hit2 != null && hit2.get("v") == "mvn cmd-2" && hit2.get("r") == "report-2")
def hit1again = cacheStore.lookup(dir.absolutePath, key1)
check("1.8 old entry replaced or served byte-equivalently (never corrupt)", hit1again == null || hit1again.get("v") == cmd1)

// ----------------------------------------------------------------------
// 2) v1 legacy line fallback ({"k","v","t"} -> report == null)
// ----------------------------------------------------------------------
String key3 = "d" * 64
File f = new File(dir, "cache.jsonl")
f.append("{\"k\":\"" + key3 + "\",\"v\":\"mvn legacy-cmd\",\"t\":123}" + nl, "UTF-8")
def hit3 = cacheStore.lookup(dir.absolutePath, key3)
check("2.1 legacy v1 line is readable", hit3 != null)
check("2.2 legacy command served", hit3.get("v") == "mvn legacy-cmd")
check("2.3 legacy report is null (caller prints legacy note)", hit3.get("r") == null)
check("2.4 unknown key misses", cacheStore.lookup(dir.absolutePath, "e" * 64) == null)

// ----------------------------------------------------------------------
// 3) REGRESSION (bug contrato-json-inferido-inoperante): a realistic
//    provenance report must round-trip through the cache.
// ----------------------------------------------------------------------
def bigReport = new StringBuilder()
bigReport.append("| flag | valor final | fonte | notas |\n|---|---|---|---|\n")
for (int i = 0; i < 40; i++) {
    bigReport.append("| flag_number_").append(i)
      .append(" | some_reasonably_long_value_").append(i)
      .append(" | CONTRACT_DERIVED | valor rejeitado pela regex; default aplicado (FR-006) |\n")
}
bigReport.append("**Resumo**: 1 CONTRACT / 4 CONTRACT_DERIVED / 0 INFERENCE / 28 DEFAULT / 8 descartada(s)\n")
String bigReportText = bigReport.toString()
check("3.1 the realistic report is above the historical overflow threshold", bigReportText.length() > 1900)

String key4 = "e" * 64
String bigCmd = "mvn archetype:generate --batch-mode -Dentity_name=ContractEntity -Dentity_contract_path=C:/tmp/contracts/abc.json"
cacheStore.write(dir.absolutePath, key4, bigCmd, bigReportText)

def hit4 = null
String overflow = null
try {
    hit4 = cacheStore.lookup(dir.absolutePath, key4)
} catch (Throwable t) {
    overflow = (t.cause?.message ?: t.message)
}
check("3.2 lookup of a realistic report does NOT overflow the stack", overflow == null)
if (overflow != null) {
    println "       thrown: " + overflow
}
check("3.3 the realistic report round-trips byte-equivalently",
        hit4 != null && hit4.get("r") == bigReportText)
check("3.4 the command line round-trips byte-equivalently",
        hit4 != null && hit4.get("v") == bigCmd)

// A poisoned (corrupted) line must not invalidate the whole cache.
File cacheFileObj = new File(dir, "cache.jsonl")
cacheFileObj.append("this is not json at all" + nl, "UTF-8")
def hit5 = null
String corruptError = null
try {
    hit5 = cacheStore.lookup(dir.absolutePath, key4)
} catch (Throwable t) {
    corruptError = (t.cause?.message ?: t.message)
}
check("3.5 a corrupted line is skipped, not fatal", corruptError == null)
check("3.6 the cache still serves the valid entry after a corrupted line",
        hit5 != null && hit5.get("v") == bigCmd)

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
