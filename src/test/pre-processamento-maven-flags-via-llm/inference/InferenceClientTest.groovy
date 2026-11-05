/**
 * Unit test for InferenceClient: JSON output parsing, non-zero exit, invalid
 * stdout and missing binary.
 *
 * Run from repo root:
 *   groovy src/test/pre-processamento-maven-flags-via-llm/inference/InferenceClientTest.groovy
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

def clientClass = gcl.loadClass("eda.postgenerate.pre_processamento_maven_flags_via_llm.inference.InferenceClient")
def client = clientClass.newInstance()

int failures = 0
def check = { String name, boolean cond ->
    if (cond) { println "PASS: ${name}" } else { println "FAIL: ${name}"; failures++ }
}

boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win")
File workDir = new File(System.getProperty("java.io.tmpdir"),
        "inference-client-test-" + System.nanoTime())
workDir.mkdirs()

def writeStub = { String name, String body ->
    File stub = new File(workDir, name + (isWindows ? ".cmd" : ".sh"))
    if (isWindows) {
        stub.text = "@echo off\r\n" + body + "\r\n"
    } else {
        stub.text = "#!/bin/sh\n" + body + "\n"
        stub.setExecutable(true)
    }
    return stub
}

try {
    // ------------------------------------------------------------------
    // 1) Valid JSON output -> Map<String, String>
    // ------------------------------------------------------------------
    def ok = writeStub("ok", isWindows
            ? 'echo {"api_port":"8080"}'
            : "echo '{\"api_port\":\"8080\"}'")
    def result = client.run(ok.absolutePath, "desc", '{"a":1}')
    check("1.1 valid stdout returns a flag map",
            result instanceof Map && result.get("api_port") == "8080")

    // ------------------------------------------------------------------
    // 2) Non-zero exit
    // ------------------------------------------------------------------
    def fail = writeStub("fail", isWindows ? "exit /b 7" : "exit 7")
    Throwable exitError = null
    try {
        client.run(fail.absolutePath, "desc", '{"a":1}')
    } catch (Throwable t) {
        exitError = t
    }
    check("2.1 non-zero exit throws", exitError != null)
    check("2.2 ...and names the exit code",
            exitError != null && exitError.message.contains("code 7"))

    // ------------------------------------------------------------------
    // 3) Invalid JSON output
    // ------------------------------------------------------------------
    def badJson = writeStub("bad-json", isWindows ? "echo not-json" : "echo 'not-json'")
    Throwable jsonError = null
    try {
        client.run(badJson.absolutePath, "desc", '{"a":1}')
    } catch (Throwable t) {
        jsonError = t
    }
    check("3.1 invalid stdout throws", jsonError != null)

    // ------------------------------------------------------------------
    // 4) Valid JSON but not an object
    // ------------------------------------------------------------------
    def arrayJson = writeStub("array-json", isWindows ? "echo [1,2]" : "echo '[1,2]'")
    Throwable arrayError = null
    try {
        client.run(arrayJson.absolutePath, "desc", '{"a":1}')
    } catch (Throwable t) {
        arrayError = t
    }
    check("4.1 non-object stdout throws",
            arrayError != null && arrayError.message.contains("not a JSON object"))

    // ------------------------------------------------------------------
    // 5) Missing binary
    // ------------------------------------------------------------------
    File missing = new File(workDir, "missing-binary" + (isWindows ? ".cmd" : ""))
    Throwable missingError = null
    try {
        client.run(missing.absolutePath, "desc", '{"a":1}')
    } catch (Throwable t) {
        missingError = t
    }
    check("5.1 missing binary throws", missingError != null)
} finally {
    workDir.deleteDir()
}

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
