/**
 * Unit-test runner - executes every Groovy unit suite of the LLM flag
 * inference mechanism and FAILS THE BUILD if any of them fails.
 *
 * Why this exists: the suites are plain Groovy scripts (no JUnit infra in this
 * repo), so surefire never picked them up - `mvn test` executed ZERO tests and
 * a green build did NOT mean the 110 unit checks had passed. This runner
 * evaluates each suite in-process; a failing suite throws (instead of calling
 * System.exit), which the runner catches and turns into a build failure.
 *
 * Each suite remains directly runnable on its own:
 *   groovy src/test/groovy/eda/postgenerate/llmflag/ContractDerivationTest.groovy
 * (a failure now surfaces as an uncaught exception, i.e. a non-zero exit code).
 *
 * Wired into the build by the `groovy-maven-plugin` execution
 * `unit-test-runner-execute` (phase: test).
 */

List<String> suites = [
        "src/test/groovy/eda/postgenerate/llmflag/ContractDerivationTest.groovy",
        "src/test/groovy/eda/postgenerate/llmflag/OrchestratorCacheTest.groovy",
        "src/test/groovy/eda/postgenerate/llmflag/ProvenanceReportTest.groovy",
        "src/test/groovy/eda/postgenerate/llmflag/FlagCatalogExtractorTest.groovy",
        "src/test/groovy/eda/postgenerate/llmflag/OrchestratorContractGateTest.groovy",
        "src/test/groovy/eda/postgenerate/llmflag/CommandLineAssemblerTest.groovy",
        "src/test/groovy/eda/postgenerate/llmflag/FlagCatalogExtractorDriftTest.groovy"
]

int failed = 0
int executed = 0

suites.each { String suitePath ->
    File suiteFile = new File(suitePath)
    if (!suiteFile.isFile()) {
        failed++
        println "[unit-tests] MISSING SUITE: ${suitePath}"
        return
    }
    println ""
    println "[unit-tests] ===== ${suiteFile.name} ====="
    try {
        new GroovyShell(this.class.classLoader).evaluate(suiteFile)
        executed++
        println "[unit-tests] PASSED: ${suiteFile.name}"
    } catch (Throwable t) {
        failed++
        println "[unit-tests] FAILED: ${suiteFile.name}"
        println "[unit-tests]   ${t.message}"
    }
}

println ""
println "[unit-tests] ${executed}/${suites.size()} suite(s) passed, ${failed} failed"
if (failed > 0) {
    throw new RuntimeException("Unit test failure: ${failed} suite(s) did not pass")
}
