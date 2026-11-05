/**
 * COVERAGE GATE for the archetype project (no JaCoCo).
 *
 * Tier 1 (files): every production .groovy file is referenced by at least one
 * test suite (or explicitly allowlisted as composition-only).
 * Tier 2 (runner): every test suite is wired into one of the two runners.
 * Tier 3 (assets): every snippet/variable/body file is referenced by a suite.
 */
def main = new File("src/main")
def tests = new File("src/test")
def runners = [new File("src/test/groovy/run-maven-flags-via-llm-tests.groovy"),
               new File("src/test/groovy/run-archetype-post-generate-tests.groovy")]
def runnerText = runners.collect { it.getText("UTF-8") }.join("\n")

int failures = 0
def check = { String n, boolean c -> if (c) { println "PASS: ${n}" } else { println "FAIL: ${n}"; failures++ } }

def prodFiles = []
main.eachFileRecurse { File f ->
    if (f.isFile() && f.name.endsWith(".groovy") && !f.absolutePath.contains("archetype-resources")) {
        prodFiles << f
    }
}
check("1.1 the production tree has 68 Groovy files (InvocationCache removed)", prodFiles.size() == 68)
check("1.2 the legacy InvocationCache is gone",
        !new File("src/main/pre-processamento-maven-flags-via-llm/cache/InvocationCache.groovy").exists())

def suiteFiles = []
[new File("src/test/archetype-post-generate"),
 new File("src/test/pre-processamento-maven-flags-via-llm")].each { root ->
    root.eachFileRecurse { File f ->
        if (f.isFile() && f.name.endsWith(".groovy") &&
                !f.name.startsWith("run-") && f.name != "PostProcessorHarness.groovy") {
            suiteFiles << f
        }
    }
}
def suiteText = suiteFiles.collect { it.getText("UTF-8").toLowerCase() }.join("\n")

// Composition-only body files: asserted structurally by ComposerTest, not by
// their own name. Explicit allowlist with a reason.
def allowlist = ["archetype-imports.groovy", "archetype-header.groovy",
                 // data holders exercised through their owning component
                 "MaterializationResult.groovy", "TypeResolution.groovy", "ValidationResult.groovy"]

def unreferenced = prodFiles.findAll { File f ->
    !allowlist.contains(f.name) && !suiteText.contains(f.name.replace(".groovy", "").toLowerCase())
}
check("1.3 every production file is covered by a suite: ${unreferenced*.name}", unreferenced.isEmpty())

// ---------------------------------------------------------------------------
// 2) Every suite is wired into a runner
// ---------------------------------------------------------------------------
def runnerEntries = []
runners.each { File r ->
    (r.getText("UTF-8") =~ /"([^"]+\.groovy)"/).each { m -> runnerEntries << m[1] }
}
check("2.1 the runners wire ${runnerEntries.size()} suites (${suiteFiles.size()} on disk)",
        runnerEntries.size() == suiteFiles.size() &&
                runnerEntries.toSet().size() == runnerEntries.size())
def notWired = suiteFiles.findAll { File f -> !runnerText.contains(f.name) }
check("2.2 every suite file is listed in a runner: ${notWired*.name}", notWired.isEmpty())

// ---------------------------------------------------------------------------
// 2b) The test tree mirrors the production package tree
// ---------------------------------------------------------------------------
def productionRoot = new File("src/main/pre-processamento-maven-flags-via-llm")
def testRoot = new File("src/test/pre-processamento-maven-flags-via-llm")
def productionPackages = productionRoot.listFiles().findAll { it.isDirectory() }*.name.sort()
def testPackages = testRoot.listFiles().findAll { it.isDirectory() }*.name.sort()
def emptyTestPackages = testPackages.findAll { String name ->
    !new File(testRoot, name).listFiles().any { it.isFile() && it.name.endsWith(".groovy") }
}
check("2.3 the test tree mirrors the production packages: ${productionPackages} / ${emptyTestPackages}",
        productionPackages == testPackages && emptyTestPackages.isEmpty())

// ---------------------------------------------------------------------------
// 3) Runner entries point to real files
// ---------------------------------------------------------------------------
def missing = []
(runnerText =~ /"([^"]+\.groovy)"/).each { m ->
    if (!new File(m[1]).isFile()) missing << m[1]
}
check("3.1 every runner entry exists on disk: ${missing}", missing.isEmpty())

// ---------------------------------------------------------------------------
// 4) Post-processor assets are all composed and referenced
// ---------------------------------------------------------------------------
def variableDir = new File("src/main/resources/META-INF/archetype_post_generate_variables")
def snippetDir = new File("src/main/resources/META-INF/archetype_post_generate_snippets")
check("4.1 there are 11 variable files", variableDir.listFiles().findAll { it.name.endsWith(".groovy") }.size() == 11)
check("4.2 there are 12 snippet files", snippetDir.listFiles().findAll { it.name.endsWith(".groovy") }.size() == 12)
def unreferencedAssets = (variableDir.listFiles() + snippetDir.listFiles()).findAll { File f ->
    f.name.endsWith(".groovy") && !suiteText.contains(f.name.toLowerCase())
}
check("4.3 every snippet/variable is referenced by a suite: ${unreferencedAssets*.name}", unreferencedAssets.isEmpty())

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
