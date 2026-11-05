/**
 * Unit test for InferenceConfig: explicit arguments, defaults, catalog path
 * and the debug flag.
 *
 * Run from repo root:
 *   groovy src/test/pre-processamento-maven-flags-via-llm/inference/InferenceConfigTest.groovy
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

def configClass = gcl.loadClass("eda.postgenerate.pre_processamento_maven_flags_via_llm.inference.InferenceConfig")

int failures = 0
def check = { String name, boolean cond ->
    if (cond) { println "PASS: ${name}" } else { println "FAIL: ${name}"; failures++ }
}

String tempCache = new File(System.getProperty("java.io.tmpdir"),
        "inference-config-test-" + System.nanoTime()).absolutePath

// ----------------------------------------------------------------------
// 1) Explicit arguments win over defaults
// ----------------------------------------------------------------------
def explicit = configClass.from([
        "cache-dir"    : tempCache,
        "contract-dir" : "/tmp/contracts",
        "inference-bin": "/tmp/stub"
])
check("1.1 explicit cache-dir wins", explicit.cacheDir == tempCache)
check("1.2 explicit contract-dir wins", explicit.contractDir == "/tmp/contracts")
check("1.3 explicit inference-bin wins", explicit.inferenceBin == "/tmp/stub")

// ----------------------------------------------------------------------
// 2) Default contract directory follows the cache directory
// ----------------------------------------------------------------------
def withCache = configClass.from(["cache-dir": tempCache])
check("2.1 contractDir defaults to <cacheDir>/contracts",
        withCache.contractDir == new File(tempCache, "contracts").absolutePath)

// ----------------------------------------------------------------------
// 3) Catalog path
// ----------------------------------------------------------------------
def defaults = configClass.from([:])
check("3.1 catalogPath is the transformed metadata",
        defaults.catalogPath == "target/classes/META-INF/maven/archetype-metadata.xml")
check("3.2 catalogFile matches catalogPath",
        defaults.catalogFile() == new File(defaults.catalogPath))

// ----------------------------------------------------------------------
// 4) Inference binary default comes from the environment
// ----------------------------------------------------------------------
check("4.1 inferenceBin defaults to EDA_LLM_INFERENCE_BIN",
        defaults.inferenceBin == System.getenv("EDA_LLM_INFERENCE_BIN"))

// ----------------------------------------------------------------------
// 5) Debug flag comes from the JVM system property
// ----------------------------------------------------------------------
String originalDebug = System.getProperty("eda.llm.debug")
try {
    System.clearProperty("eda.llm.debug")
    check("5.1 debug defaults to false", configClass.from([:]).debug == false)
    System.setProperty("eda.llm.debug", "true")
    check("5.2 debug reads the system property", configClass.from([:]).debug == true)
} finally {
    if (originalDebug == null) System.clearProperty("eda.llm.debug")
    else System.setProperty("eda.llm.debug", originalDebug)
}

// ----------------------------------------------------------------------
// 6) Default cache directory is always non-empty
// ----------------------------------------------------------------------
check("6.1 default cacheDir is non-empty",
        defaults.cacheDir != null && !defaults.cacheDir.isEmpty())

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
