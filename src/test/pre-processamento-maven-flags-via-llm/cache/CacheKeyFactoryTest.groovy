/**
 * Unit test for CacheKeyFactory: deterministic cache key and catalog
 * fingerprint.
 *
 * Run from repo root:
 *   groovy src/test/pre-processamento-maven-flags-via-llm/cache/CacheKeyFactoryTest.groovy
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

def catalogEntryClass = gcl.loadClass("eda.postgenerate.pre_processamento_maven_flags_via_llm.catalog.CatalogEntry")
def cacheKeyFactoryClass = gcl.loadClass("eda.postgenerate.pre_processamento_maven_flags_via_llm.cache.CacheKeyFactory")
def canonicalizerClass = gcl.loadClass("eda.postgenerate.pre_processamento_maven_flags_via_llm.cache.JsonCanonicalizer")

int failures = 0
def check = { String name, boolean cond ->
    if (cond) { println "PASS: ${name}" } else { println "FAIL: ${name}"; failures++ }
}

def mkEntry = { String k, String d, String r ->
    catalogEntryClass.newInstance(k, d, r, "")
}

def cacheKeyFactory = cacheKeyFactoryClass.newInstance(canonicalizerClass.newInstance())

// ----------------------------------------------------------------------
// 1) cache key embeds the catalog fingerprint -> a catalog change
//    invalidates every previously cached command line
// ----------------------------------------------------------------------
String fpA = "a" * 64
String fpB = "b" * 64
String keyStable1 = (String) cacheKeyFactory.makeKey("desc", "{\"k\":1}", fpA)
String keyStable2 = (String) cacheKeyFactory.makeKey("desc", "{\"k\":1}", fpA)
String keyOtherCatalog = (String) cacheKeyFactory.makeKey("desc", "{\"k\":1}", fpB)

check("1.1 cache key is deterministic for the same inputs", keyStable1 == keyStable2)
check("1.2 cache key changes when the catalog fingerprint changes",
    keyStable1 != keyOtherCatalog)
check("1.3 legacy key (no fingerprint) is NOT reachable",
    keyStable1 != (String) cacheKeyFactory.makeKey("desc", "{\"k\":1}", ""))

// fingerprint: same catalog -> same digest; different catalog -> different digest
def catA = new LinkedHashMap()
catA.put("api_port", mkEntry("api_port", "8080", "\\d{1,5}"))
def catB = new LinkedHashMap()
catB.put("api_port", mkEntry("api_port", "9090", "\\d{1,5}"))
def catC = new LinkedHashMap()
catC.put("api_port", mkEntry("api_port", "8080", "\\d{1,5}"))
check("1.4 fingerprint is stable for the same catalog",
    cacheKeyFactory.fingerprint(catA) == cacheKeyFactory.fingerprint(catC))
check("1.5 fingerprint changes when a defaultValue changes",
    cacheKeyFactory.fingerprint(catA) != cacheKeyFactory.fingerprint(catB))
check("1.6 fingerprint is a sha256 hex digest",
    ((String) cacheKeyFactory.fingerprint(catA)) ==~ /^[0-9a-f]{64}$/)

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
