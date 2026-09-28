/**
 * Unit test for the catalog drift detection (FR-013): the flag catalog is the
 * canonical archetype metadata, read at runtime - there is NO hardcoded flag
 * list in the mechanism, so adding or removing a flag in the metadata must be
 * reflected WITHOUT any code change.
 *
 * "FlagCatalogExtractorDriftTest" (T024/T058/T081 of
 * specs/003-llm-flag-inference/tasks.md) had no file on disk.
 *
 * Run from repo root:
 *   groovy src/test/groovy/eda/postgenerate/llmflag/FlagCatalogExtractorDriftTest.groovy
 * (also executed by the build - see src/test/groovy/run-all-tests.groovy)
 */

def gcl = new GroovyClassLoader(this.class.classLoader)
def catalogEntryClass = gcl.parseClass(new File("src/main/groovy/eda/postgenerate/llmflag/CatalogEntry.groovy"))
def extractorClass = gcl.parseClass(new File("src/main/groovy/eda/postgenerate/llmflag/FlagCatalogExtractor.groovy"))

int failures = 0
def check = { String name, boolean cond ->
    if (cond) { println "PASS: ${name}" } else { println "FAIL: ${name}"; failures++ }
}

def extractor = extractorClass.newInstance()

def metadata = { String body ->
    return '<?xml version="1.0" encoding="UTF-8"?>' + "\n" +
            '<archetype-descriptor>' + "\n" +
            '  <requiredProperties>' + "\n" + body + "\n" +
            '  </requiredProperties>' + "\n" +
            '</archetype-descriptor>'
}
def prop = { String key, String dflt, String regex ->
    return "    <requiredProperty key=\"${key}\">" +
            "      <defaultValue>${dflt}</defaultValue>" +
            "      <validationRegex>${regex}</validationRegex>" +
            "    </requiredProperty>"
}

def tmpDir = new File(System.getProperty("java.io.tmpdir"), "llmflag-drift-" + System.nanoTime())
tmpDir.mkdirs()
def writeMeta = { String name, String body ->
    File f = new File(tmpDir, name)
    f.text = metadata(body)
    return f
}

def base = prop("api_port", "8080", '\\d{1,5}') + "\n" + prop("entity_name", "MyEntity", '^[A-Z][a-zA-Z0-9_]+$')
def added = base + "\n" + prop("enable_test_xyz", "false", '^(true|false)$')
def removed = prop("api_port", "8080", '\\d{1,5}')

def baseFile = writeMeta("base.xml", base)
def addedFile = writeMeta("added.xml", added)
def removedFile = writeMeta("removed.xml", removed)

def mExtract = extractorClass.getDeclaredMethod("extract", File)
def extract = { File f -> return (Map) mExtract.invoke(extractor, f) }

def catBase = extract(baseFile)
def catAdded = extract(addedFile)
def catRemoved = extract(removedFile)

// ---------------------------------------------------------------------------
// 1) A flag ADDED to the metadata appears in the catalog - with no code change
// ---------------------------------------------------------------------------
check("1.1 the base catalog has 2 flags", catBase.size() == 2)
check("1.2 an ADDED flag shows up in the catalog", catAdded.size() == 3)
check("1.3 ...with its key", catAdded.containsKey("enable_test_xyz"))
check("1.4 ...with its default value", catAdded.get("enable_test_xyz")?.defaultValue == "false")
check("1.5 ...with its validation rule", catAdded.get("enable_test_xyz")?.validationRegex == '^(true|false)$')
check("1.6 the base flags are untouched by the addition", catAdded.get("api_port")?.defaultValue == "8080")

// ---------------------------------------------------------------------------
// 2) A flag REMOVED from the metadata disappears from the catalog
// ---------------------------------------------------------------------------
check("2.1 a REMOVED flag disappears from the catalog", catRemoved.size() == 1)
check("2.2 ...and is not rejected by the loader (no stale entry)", !catRemoved.containsKey("entity_name"))

// ---------------------------------------------------------------------------
// 3) The catalogs are genuinely different - the mechanism carries no hardcoded list
// ---------------------------------------------------------------------------
check("3.1 the three catalogs are pairwise different",
        catBase != catAdded && catBase != catRemoved && catAdded != catRemoved)
check("3.2 the extractor needs no code change to follow the metadata",
        catAdded.keySet().containsAll(catBase.keySet()))

// ---------------------------------------------------------------------------
// 4) FR-018 still holds on a drifted catalog (an empty default aborts)
// ---------------------------------------------------------------------------
def badFile = writeMeta("bad.xml", prop("enable_test_xyz", "", '^(true|false)$'))
def badError = null
try { extract(badFile) } catch (Throwable t) { badError = (t.cause?.message ?: t.message) }
check("4.1 an empty defaultValue in the drifted catalog is rejected", badError != null)
check("4.2 ...naming the offending flag", (badError ?: "").contains("enable_test_xyz"))

tmpDir.deleteDir()

println ""
if (failures == 0) { println "ALL CHECKS PASSED" } else { println "${failures} CHECK(S) FAILED" }
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
