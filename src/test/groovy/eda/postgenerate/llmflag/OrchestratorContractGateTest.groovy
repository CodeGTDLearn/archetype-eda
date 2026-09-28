/**
 * Unit test for the FR-001/FR-002/FR-003 input gates of the Orchestrator
 * (spec.md) - "OrchestratorContractGateTest" (T018/T052/T080 of
 * specs/003-llm-flag-inference/tasks.md, which had no file on disk).
 *
 * The gates live in Orchestrator.run(), which terminates the process with
 * System.exit - so the Orchestrator is invoked in a FORKED JVM and the exit
 * code, stdout and stderr are asserted. The inference stub is generated in a
 * temp directory (a one-line script that prints a fixed flag map) so the test
 * is hermetic: no dependency on `groovy`/`bash` being on the PATH.
 *
 * Run from repo root:
 *   groovy src/test/groovy/eda/postgenerate/llmflag/OrchestratorContractGateTest.groovy
 * (also executed by the build - see src/test/groovy/run-all-tests.groovy)
 */

def repoRoot = new File(".").canonicalPath
def orchestratorPath = new File(repoRoot, "src/main/groovy/eda/postgenerate/llmflag/Orchestrator.groovy").absolutePath

int failures = 0
def check = { String name, boolean cond ->
    if (cond) { println "PASS: ${name}" } else { println "FAIL: ${name}"; failures++ }
}

// ---------------------------------------------------------------------------
// Harness: classpath, java binary, temp inference stub, forked invocation
// ---------------------------------------------------------------------------
def buildClasspath = {
    def entries = new LinkedHashSet<String>()
    (System.getProperty("java.class.path") ?: "").split(File.pathSeparator).each { if (it) entries << it }
    def cl = this.class.classLoader
    while (cl != null) {
        if (cl instanceof URLClassLoader) {
            cl.URLs.each { u -> if (u.protocol == "file") entries << new File(u.toURI()).absolutePath }
        }
        cl = cl.parent
    }
    entries.join(File.pathSeparator)
}
def classpath = buildClasspath()
def isWindows = System.getProperty("os.name").toLowerCase().contains("win")
def javaBin = new File(new File(System.getProperty("java.home"), "bin"), isWindows ? "java.exe" : "java").absolutePath

def workDir = new File(System.getProperty("java.io.tmpdir"), "llmflag-gate-" + System.nanoTime())
workDir.mkdirs()

// A one-line inference stub: prints a single-line JSON flag map, exits 0.
def stub = new File(workDir, isWindows ? "inference-stub.cmd" : "inference-stub.sh")
if (isWindows) {
    stub.text = "@echo off\r\necho {\"api_port\":\"8080\"}\r\n"
} else {
    stub.text = "#!/bin/sh\necho '{\"api_port\":\"8080\"}'\n"
    stub.setExecutable(true)
}
def stubPath = stub.absolutePath

def runOrchestrator = { List<String> extraArgs, String stdin ->
    List<String> cmd = [javaBin, "-cp", classpath, "groovy.ui.GroovyMain", orchestratorPath] + extraArgs
    ProcessBuilder pb = new ProcessBuilder(cmd)
    pb.redirectErrorStream(false)
    Process p = pb.start()
    if (stdin != null) {
        p.outputStream.withWriter("UTF-8") { it.write(stdin) }
        p.outputStream.close()
    }
    def out = new StringBuffer()
    def err = new StringBuffer()
    p.consumeProcessOutput(out, err)
    int code = p.waitFor()
    return [code: code, out: out.toString(), err: err.toString()]
}

def VALID_CONTRACT = '{\n  "entityName": "GateEntity",\n  "attributes": [ { "name": "id", "value": 1 } ]\n}\n'

// ---------------------------------------------------------------------------
// 1) FR-001 - the description gate (checked BEFORE the contract, and BEFORE
//    any inference). stdin MUST NOT be consumed in these cases.
// ---------------------------------------------------------------------------
def r1 = runOrchestrator(["--use-case-contract"], VALID_CONTRACT)
check("1.1 missing description exits with code 2", r1.code == 2)
check("1.2 missing description names the FR-001 error",
        r1.err.contains("USE CASE DESCRIPTION is missing or blank"))
check("1.3 missing description produces NO command line on stdout", !r1.out.contains("mvn archetype:generate"))
check("1.4 the contract gate is NOT reached (description is checked first)",
        !r1.err.contains("USE CASE CONTRACT is missing"))

def r2 = runOrchestrator(["--use-case-description"], VALID_CONTRACT)
check("1.5 description as the last argument (no value) exits with code 2", r2.code == 2)
check("1.6 ...with the FR-001 error", r2.err.contains("USE CASE DESCRIPTION is missing or blank"))

def r3 = runOrchestrator(["--use-case-description", "   "], VALID_CONTRACT)
check("1.7 blank/whitespace description exits with code 2", r3.code == 2)
check("1.8 ...with the FR-001 error", r3.err.contains("USE CASE DESCRIPTION is missing or blank"))

def r4 = runOrchestrator(["--use-case-description", "--use-case-contract"], VALID_CONTRACT)
check("1.9 a following -- token is not swallowed as the description value", r4.code == 2)
check("1.10 ...with the FR-001 error", r4.err.contains("USE CASE DESCRIPTION is missing or blank"))

// ---------------------------------------------------------------------------
// 2) FR-002 / FR-003 - the contract gate
// ---------------------------------------------------------------------------
def r5 = runOrchestrator(["--use-case-description", "uma descricao"], null)
check("2.1 missing contract exits with code 2", r5.code == 2)
check("2.2 missing contract names the FR-003 error", r5.err.contains("USE CASE CONTRACT is missing or empty"))
check("2.3 missing contract produces NO command line", !r5.out.contains("mvn archetype:generate"))

def r6 = runOrchestrator(["--use-case-description", "uma descricao", "--use-case-contract"], "")
check("2.4 empty stdin contract exits with code 2", r6.code == 2)
check("2.5 ...with the FR-003 error", r6.err.contains("USE CASE CONTRACT is missing or empty"))

// ---------------------------------------------------------------------------
// 3) FR-002 - a multi-line JSON contract IS read from stdin (and the whole
//    flow reaches the command line)
// ---------------------------------------------------------------------------
def r7 = runOrchestrator(["--use-case-description", "uma descricao", "--use-case-contract",
                          "--inference-bin", stubPath, "--cache-dir", new File(workDir, "cache").absolutePath], VALID_CONTRACT)
check("3.1 a valid stdin contract produces exit code 0", r7.code == 0)
check("3.2 ...exactly ONE command line on stdout",
        r7.out.readLines().findAll { it.trim().startsWith("mvn archetype:generate") }.size() == 1)
check("3.3 ...the contract drove the entity (not the catalog default)",
        r7.out.contains("-Dentity_name=GateEntity"))
check("3.4 ...the provenance report went to stderr, not stdout",
        r7.err.contains("Relatorio de Proveniencia") && !r7.out.contains("Relatorio de Proveniencia"))

// ---------------------------------------------------------------------------
// 4) Malformed contract root (FR-002: the root MUST be a JSON object)
// ---------------------------------------------------------------------------
def r8 = runOrchestrator(["--use-case-description", "uma descricao", "--use-case-contract",
                          "--inference-bin", stubPath, "--cache-dir", new File(workDir, "cache2").absolutePath], "[1,2,3]")
check("4.1 a non-object contract root exits with code 5", r8.code == 5)
check("4.2 ...with a clear message", r8.err.contains("Contract root MUST be a JSON object"))

// ---------------------------------------------------------------------------
// 5) Unknown argument is rejected (non-zero exit, no command line)
// ---------------------------------------------------------------------------
def r9 = runOrchestrator(["--use-case-description", "d", "--unknown-arg"], VALID_CONTRACT)
check("5.1 an unknown argument is rejected with a non-zero exit", r9.code != 0)
check("5.2 ...and produces NO command line", !r9.out.contains("mvn archetype:generate"))

// ---------------------------------------------------------------------------
// 6) The gates fire BEFORE the inference step (no stub is even needed)
// ---------------------------------------------------------------------------
def r10 = runOrchestrator(["--use-case-description", "d", "--inference-bin",
                           new File(workDir, "does-not-exist").absolutePath], null)
check("6.1 the contract gate fires before the inference shell-out", r10.code == 2)
check("6.2 ...the inference script was never invoked (no 'inference script failed')",
        !r10.err.contains("External inference script failed"))

workDir.deleteDir()

println ""
if (failures == 0) { println "ALL CHECKS PASSED" } else { println "${failures} CHECK(S) FAILED" }
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
