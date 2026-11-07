/**
 * Unit-test runner for the ARCHETYPE POST-PROCESSOR.
 *
 * Runs the post-processor suites only. The LLM flag inference/pre-processing
 * mechanism has its own runner: run-maven-flags-via-llm-tests.groovy.
 *
 * Why this exists: the suites are plain Groovy scripts (no JUnit infra in this
 * repo), so surefire never picked them up. This runner evaluates each suite
 * in-process; a failing suite throws (instead of calling System.exit), which
 * the runner catches and turns into a build failure.
 *
 * Wired into the build by the `groovy-maven-plugin` execution
 * `archetype-post-generate-test-runner` (phase: test).
 */

List<String> suites = [
        // composer / build / metadata
        "src/test/archetype-post-generate/ComposerTest.groovy",
        "src/test/archetype-post-generate/BuildArtifactsTest.groovy",
        "src/test/archetype-post-generate/MetadataContractTest.groovy",
        "src/test/archetype-post-generate/FilesetConsistencyTest.groovy",
        "src/test/archetype-post-generate/TemplateFlagConsistencyTest.groovy",
        // variables
        "src/test/archetype-post-generate/VariablesTest.groovy",
        "src/test/archetype-post-generate/VariablesCustomTypesTest.groovy",
        // body
        "src/test/archetype-post-generate/BodyFunctionsTest.groovy",
        "src/test/archetype-post-generate/BodyVariablesTest.groovy",
        // snippets
        "src/test/archetype-post-generate/SnippetAuthenticationTest.groovy",
        "src/test/archetype-post-generate/SnippetBindingPropertiesTest.groovy",
        "src/test/archetype-post-generate/SnippetBindingPropertiesTestTest.groovy",
        "src/test/archetype-post-generate/SnippetCpanelTest.groovy",
        "src/test/archetype-post-generate/SnippetCustomEnumsTest.groovy",
        "src/test/archetype-post-generate/SnippetCustomNestedTypesTest.groovy",
        "src/test/archetype-post-generate/SnippetDockerTest.groovy",
        "src/test/archetype-post-generate/SnippetEntityContractTest.groovy",
        "src/test/archetype-post-generate/SnippetExtraPomDependenciesTest.groovy",
        "src/test/archetype-post-generate/SnippetGithubTest.groovy",
        "src/test/archetype-post-generate/SnippetLogbackTest.groovy",
        "src/test/archetype-post-generate/SnippetPropertiesFiletypeTest.groovy",
        "src/test/archetype-post-generate/SnippetSrcTestJavaTest.groovy",
        // coverage
        "src/test/archetype-post-generate/CoverageRegistryTest.groovy"
]

int failed = 0
int executed = 0

suites.each { String suitePath ->
    File suiteFile = new File(suitePath)
    if (!suiteFile.isFile()) {
        failed++
        println "[archetype-post-generate] MISSING SUITE: ${suitePath}"
        return
    }
    println ""
    println "[archetype-post-generate] ===== ${suiteFile.name} ====="
    try {
        new GroovyShell(this.class.classLoader).evaluate(suiteFile)
        executed++
        println "[archetype-post-generate] PASSED: ${suiteFile.name}"
    } catch (Throwable t) {
        failed++
        println "[archetype-post-generate] FAILED: ${suiteFile.name}"
        println "[archetype-post-generate]   ${t.message}"
    }
}

println ""
println "[archetype-post-generate] ${executed}/${suites.size()} suite(s) passed, ${failed} failed"
if (failed > 0) {
    throw new RuntimeException("Archetype post-generate unit test failure: ${failed} suite(s) did not pass")
}
