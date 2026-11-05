/**
 * Unit-test runner for the Maven Flags via LLM PRE-PROCESSING mechanism.
 *
 * Runs the inference/pre-processing suites only. The archetype post-processor
 * has its own runner: run-archetype-post-generate-tests.groovy.
 *
 * Why this exists: the suites are plain Groovy scripts (no JUnit infra in this
 * repo), so surefire never picked them up. This runner evaluates each suite
 * in-process; a failing suite throws (instead of calling System.exit), which
 * the runner catches and turns into a build failure.
 *
 * Each suite remains directly runnable on its own:
 *   groovy src/test/pre-processamento-maven-flags-via-llm/domain/ContractDerivationTest.groovy
 *
 * Wired into the build by the `groovy-maven-plugin` execution
 * `maven-flags-via-llm-test-runner` (phase: test).
 */

List<String> suites = [
        // Entry point / CLI gates
        "src/test/pre-processamento-maven-flags-via-llm/MavenFlagsViaLlmStarterTest.groovy",
        // cache
        "src/test/pre-processamento-maven-flags-via-llm/cache/CacheKeyFactoryTest.groovy",
        "src/test/pre-processamento-maven-flags-via-llm/cache/CacheStoreTest.groovy",
        // catalog
        "src/test/pre-processamento-maven-flags-via-llm/catalog/FlagCatalogExtractorTest.groovy",
        "src/test/pre-processamento-maven-flags-via-llm/catalog/FlagCatalogExtractorDriftTest.groovy",
        // command
        "src/test/pre-processamento-maven-flags-via-llm/commandLine/CommandLineAssemblerTest.groovy",
        "src/test/pre-processamento-maven-flags-via-llm/commandLine/CommandLineArgumentsTest.groovy",
        // contract
        "src/test/pre-processamento-maven-flags-via-llm/contract/ContractReaderTest.groovy",
        "src/test/pre-processamento-maven-flags-via-llm/contract/ContractStoreTest.groovy",
        "src/test/pre-processamento-maven-flags-via-llm/contract/ContractSecretScannerTest.groovy",
        // domain
        "src/test/pre-processamento-maven-flags-via-llm/domain/ContractDerivationTest.groovy",
        "src/test/pre-processamento-maven-flags-via-llm/domain/ContractTypeResolverTest.groovy",
        "src/test/pre-processamento-maven-flags-via-llm/domain/ContractAttributeExtractorTest.groovy",
        "src/test/pre-processamento-maven-flags-via-llm/domain/ContractAttributeAssemblerTest.groovy",
        "src/test/pre-processamento-maven-flags-via-llm/domain/ContractEntityNameResolverTest.groovy",
        "src/test/pre-processamento-maven-flags-via-llm/domain/ContractValueReaderTest.groovy",
        "src/test/pre-processamento-maven-flags-via-llm/domain/DerivedFlagProjectorTest.groovy",
        "src/test/pre-processamento-maven-flags-via-llm/domain/FlagValidationPolicyTest.groovy",
        "src/test/pre-processamento-maven-flags-via-llm/domain/InferenceEligibilityPolicyTest.groovy",
        "src/test/pre-processamento-maven-flags-via-llm/domain/TypeNameListFormatterTest.groovy",
        "src/test/pre-processamento-maven-flags-via-llm/domain/CustomAttributesProfileConsistencyTest.groovy",
        "src/test/pre-processamento-maven-flags-via-llm/domain/ProvenanceReportTest.groovy",
        // inference
        "src/test/pre-processamento-maven-flags-via-llm/inference/InferenceConfigTest.groovy",
        "src/test/pre-processamento-maven-flags-via-llm/inference/InferenceClientTest.groovy",
        // presentation
        "src/test/pre-processamento-maven-flags-via-llm/presentation/CliOutputTest.groovy",
        "src/test/pre-processamento-maven-flags-via-llm/presentation/ProvenanceReportRendererTest.groovy"
]

int failed = 0
int executed = 0

suites.each { String suitePath ->
    File suiteFile = new File(suitePath)
    if (!suiteFile.isFile()) {
        failed++
        println "[maven-flags-via-llm] MISSING SUITE: ${suitePath}"
        return
    }
    println ""
    println "[maven-flags-via-llm] ===== ${suiteFile.name} ====="
    try {
        new GroovyShell(this.class.classLoader).evaluate(suiteFile)
        executed++
        println "[maven-flags-via-llm] PASSED: ${suiteFile.name}"
    } catch (Throwable t) {
        failed++
        println "[maven-flags-via-llm] FAILED: ${suiteFile.name}"
        println "[maven-flags-via-llm]   ${t.message}"
    }
}

println ""
println "[maven-flags-via-llm] ${executed}/${suites.size()} suite(s) passed, ${failed} failed"
if (failed > 0) {
    throw new RuntimeException("Maven flags via LLM unit test failure: ${failed} suite(s) did not pass")
}
