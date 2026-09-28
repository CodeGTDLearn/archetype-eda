/**
 * Test for the Orchestrator's inlined FR-020 paths (merge provenance, report
 * rendering, cache v2 write/read with v1 legacy fallback, FR-017 guard).
 * Loads the REAL Orchestrator.groovy via GroovyClassLoader and invokes its
 * private methods reflectively — tests the actual production code, not a copy.
 *
 * Run from repo root:
 *   groovy src/test/groovy/eda/postgenerate/llmflag/OrchestratorCacheTest.groovy
 * Exits 0 when every check passes, 1 otherwise.
 */

def gcl = new GroovyClassLoader(this.class.classLoader)
def cls = gcl.parseClass(new File("src/main/groovy/eda/postgenerate/llmflag/Orchestrator.groovy"))
def inst = cls.newInstance()

int failures = 0
def check = { String name, boolean cond ->
    if (cond) { println "PASS: ${name}" } else { println "FAIL: ${name}"; failures++ }
}

def priv = { String name, Class... types ->
    def m = cls.getDeclaredMethod(name, types)
    m.accessible = true
    return m
}

// Inline-style catalog (Map<key, Map<defaultValue, validationRegex>>)
def catalog = new LinkedHashMap()
catalog.put("api_port", [defaultValue: "8080", validationRegex: "\\d{1,5}"])
catalog.put("log_level", [defaultValue: "INFO", validationRegex: null])
catalog.put("pipe_flag", [defaultValue: "none", validationRegex: null])

// ----------------------------------------------------------------------
// 1) merge() provenance + renderProvenanceReport()
// ----------------------------------------------------------------------
def mMerge = priv("merge", Map, Map, Map)
def mRender = priv("renderProvenanceReport", Map)

def contract = new LinkedHashMap()
contract.put("api_port", "999999")        // fails regex -> FR-006
contract.put("log_level", "DEBUG")        // CONTRACT
contract.put("secret_field", "shhh")      // not in catalog -> must never leak (FR-017)

def inference = new LinkedHashMap()
inference.put("api_port", "7070")         // in contract -> eligible, but contract wins
inference.put("pipe_flag", "a|b")         // in contract? no -> discarded; pipe tests mdCell
inference.put("unknown_llm", "zz")        // discarded

def merged = mMerge.invoke(inst, inference, contract, catalog)
String report = (String) mRender.invoke(inst, merged)

check("1.1 report has main table header", report.contains("| flag | valor final | fonte | notas |"))
check("1.2 FR-006 fallback row (api_port=8080 DEFAULT)", report.contains("| api_port | 8080 | DEFAULT |"))
check("1.3 FR-006 note mentions regex rejection", report.contains("rejeitado pela regex"))
check("1.4 CONTRACT row (log_level=DEBUG)", report.contains("| log_level | DEBUG | CONTRACT |"))
check("1.5 DEFAULT row (pipe_flag=none)", report.contains("| pipe_flag | none | DEFAULT |"))
check("1.6 discarded section present", report.contains("Propostas do LLM descartadas"))
check("1.7 discarded row (unknown_llm)", report.contains("| unknown_llm | zz |"))
check("1.8 mdCell escapes pipe in discarded value", report.contains("a\\|b"))
check("1.9 summary line counts", report.contains("**Resumo**: 1 CONTRACT / 0 CONTRACT_DERIVED / 0 INFERENCE / 2 DEFAULT / 2 descartada(s)"))
check("1.10 FR-017: secret_field absent from report", !report.contains("secret_field") && !report.contains("shhh"))

// ----------------------------------------------------------------------
// 2) cacheWrite v2 + cacheLookup round-trip (incl. multi-line report)
// ----------------------------------------------------------------------
def mWrite = priv("cacheWrite", String, String, String, String)
def mLookup = priv("cacheLookup", String, String)

def dir = new File(System.getProperty("java.io.tmpdir"), "llmflag-orch-test-" + System.nanoTime())
String key1 = "b" * 64
String cmd1 = "mvn archetype:generate --batch-mode -Dapi_port=8080"
mWrite.invoke(inst, dir.absolutePath, key1, cmd1, report)

String raw = new File(dir, "cache.jsonl").getText("UTF-8")
check("2.1 cache line matches v2 shape", raw ==~ /(?s)^\{"k":"[0-9a-f]{64}","v":"(?:[^"\\]|\\.)*","r":"(?:[^"\\]|\\.)*","t":\d+\}\s*$/)
check("2.2 no double-quote regression (bug fix)", !raw.contains('""'))
check("2.3 FR-017: secret absent from cache file", !raw.contains("secret_field") && !raw.contains("shhh"))

def hit1 = mLookup.invoke(inst, dir.absolutePath, key1)
check("2.4 lookup finds v2 entry", hit1 != null)
check("2.5 command round-trips byte-equivalently", hit1.get("v") == cmd1)
check("2.6 report round-trips (multi-line escaped)", hit1.get("r") == report)

// second write: contract Section 3 documents replace semantics (tmp + atomic
// rename; "the second write wins"). The only hard guarantees: the new entry is
// served correctly and no corruption occurs (old entry may be replaced or,
// when the rename falls back to append, still be served byte-equivalently).
String key2 = "c" * 64
mWrite.invoke(inst, dir.absolutePath, key2, "mvn cmd-2", "report-2")
def hit2 = mLookup.invoke(inst, dir.absolutePath, key2)
check("2.7 second entry served correctly after write", hit2 != null && hit2.get("v") == "mvn cmd-2" && hit2.get("r") == "report-2")
def hit1again = mLookup.invoke(inst, dir.absolutePath, key1)
check("2.8 old entry replaced or served byte-equivalently (never corrupt)", hit1again == null || hit1again.get("v") == cmd1)

// ----------------------------------------------------------------------
// 3) v1 legacy line fallback ({"k","v","t"} -> report == null)
// ----------------------------------------------------------------------
String key3 = "d" * 64
File f = new File(dir, "cache.jsonl")
f.append("{\"k\":\"" + key3 + "\",\"v\":\"mvn legacy-cmd\",\"t\":123}" + System.lineSeparator(), "UTF-8")
def hit3 = mLookup.invoke(inst, dir.absolutePath, key3)
check("3.1 legacy v1 line is readable", hit3 != null)
check("3.2 legacy command served", hit3.get("v") == "mvn legacy-cmd")
check("3.3 legacy report is null (caller prints legacy note)", hit3.get("r") == null)

// unknown key misses
check("3.4 unknown key misses", mLookup.invoke(inst, dir.absolutePath, "e" * 64) == null)

// ----------------------------------------------------------------------
// 4) cache key embeds the catalog fingerprint -> a catalog change
//    invalidates every previously cached command line
// ----------------------------------------------------------------------
def mKey = priv("makeCacheKey", String, String, String)
def mFingerprint = priv("fingerprintCatalog", Map)

String fpA = "a" * 64
String fpB = "b" * 64
String keyStable1 = (String) mKey.invoke(inst, "desc", "{\"k\":1}", fpA)
String keyStable2 = (String) mKey.invoke(inst, "desc", "{\"k\":1}", fpA)
String keyOtherCatalog = (String) mKey.invoke(inst, "desc", "{\"k\":1}", fpB)

check("4.1 cache key is deterministic for the same inputs", keyStable1 == keyStable2)
check("4.2 cache key changes when the catalog fingerprint changes",
    keyStable1 != keyOtherCatalog)
check("4.3 legacy key (no fingerprint) is NOT reachable",
    keyStable1 != (String) mKey.invoke(inst, "desc", "{\"k\":1}", ""))

// fingerprint: same catalog -> same digest; different catalog -> different digest
def catA = new LinkedHashMap()
catA.put("api_port", [defaultValue: "8080", validationRegex: "\\d{1,5}"])
def catB = new LinkedHashMap()
catB.put("api_port", [defaultValue: "9090", validationRegex: "\\d{1,5}"])
def catC = new LinkedHashMap()
catC.put("api_port", [defaultValue: "8080", validationRegex: "\\d{1,5}"])
check("4.4 fingerprint is stable for the same catalog",
    mFingerprint.invoke(inst, catA) == mFingerprint.invoke(inst, catC))
check("4.5 fingerprint changes when a defaultValue changes",
    mFingerprint.invoke(inst, catA) != mFingerprint.invoke(inst, catB))
check("4.6 fingerprint is a sha256 hex digest",
    ((String) mFingerprint.invoke(inst, catA)) ==~ /^[0-9a-f]{64}$/)

// ----------------------------------------------------------------------
// 5) REGRESSION (bug contrato-json-inferido-inoperante): a realistic
//    provenance report must round-trip through the cache.
//    The previous regex-based parser compiled the alternation
//    ((?:[^"\\]|\\.)*) into a RECURSIVE Pattern$Loop - one stack frame per
//    character - and overflowed the stack on a report of ~1965 characters,
//    turning EVERY cache HIT into exit code 1.
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
check("5.1 the realistic report is above the historical overflow threshold", bigReportText.length() > 1900)

String key4 = "e" * 64
String bigCmd = "mvn archetype:generate --batch-mode -Dentity_name=ContractEntity -Dentity_contract_path=C:/tmp/contracts/abc.json"
mWrite.invoke(inst, dir.absolutePath, key4, bigCmd, bigReportText)

def hit4 = null
String overflow = null
try {
    hit4 = mLookup.invoke(inst, dir.absolutePath, key4)
} catch (Throwable t) {
    overflow = (t.cause?.message ?: t.message)
}
check("5.2 lookup of a realistic report does NOT overflow the stack", overflow == null)
if (overflow != null) {
    println "       thrown: " + overflow
}
check("5.3 the realistic report round-trips byte-equivalently",
        hit4 != null && hit4.get("r") == bigReportText)
check("5.4 the command line round-trips byte-equivalently",
        hit4 != null && hit4.get("v") == bigCmd)

// A poisoned (corrupted) line must not invalidate the whole cache.
File cacheFileObj = new File(dir, "cache.jsonl")
cacheFileObj.append("this is not json at all" + System.lineSeparator(), "UTF-8")
def hit5 = null
String corruptError = null
try {
    hit5 = mLookup.invoke(inst, dir.absolutePath, key4)
} catch (Throwable t) {
    corruptError = (t.cause?.message ?: t.message)
}
check("5.5 a corrupted line is skipped, not fatal", corruptError == null)
check("5.6 the cache still serves the valid entry after a corrupted line",
        hit5 != null && hit5.get("v") == bigCmd)

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
