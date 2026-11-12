/**
 * COVERAGE GATE for the archetype project (no JaCoCo).
 *
 * Tier 1 (files): every production .groovy file is referenced by at least one
 * test suite (or explicitly allowlisted as composition-only).
 * Tier 2 (runner): every test suite is wired into the runner.
 * Tier 3 (assets): every snippet/variable/body file is referenced by a suite.
 */
def main = new File("src/main")
def tests = new File("src/test")
def runners = [new File("src/test/groovy/run-archetype-post-generate-tests.groovy")]
def runnerText = runners.collect { it.getText("UTF-8") }.join("\n")

int failures = 0
def check = { String n, boolean c -> if (c) { println "PASS: ${n}" } else { println "FAIL: ${n}"; failures++ } }

def prodFiles = []
main.eachFileRecurse { File f ->
    if (f.isFile() && f.name.endsWith(".groovy") && !f.absolutePath.contains("archetype-resources")) {
        prodFiles << f
    }
}
check("1.1 the production tree exposes the four post-processor asset folders", [
        new File("src/main/resources/META-INF/archetype_post_generate_body"),
        new File("src/main/resources/META-INF/archetype_post_generate_variables"),
        new File("src/main/resources/META-INF/archetype_post_generate_generators"),
        new File("src/main/resources/META-INF/archetype_post_generate_snippets")
].every { dir -> dir.isDirectory() && dir.listFiles().any { it.name.endsWith(".groovy") } } && !prodFiles.isEmpty())

// Test helpers: shared infrastructure, not suites (they carry no check).
def helpers = ["PostProcessorHarness.groovy", "TemplateRenderer.groovy"]

def suiteFiles = []
new File("src/test/archetype-post-generate").eachFileRecurse { File f ->
    if (f.isFile() && f.name.endsWith(".groovy") &&
            !f.name.startsWith("run-") && !helpers.contains(f.name)) {
        suiteFiles << f
    }
}
def suiteText = suiteFiles.collect { it.getText("UTF-8").toLowerCase() }.join("\n")

// Composition-only body files: asserted structurally by ComposerTest, not by
// their own name. Explicit allowlist with a reason.
def allowlist = ["body-01-imports.groovy", "body-02-header.groovy"]

def isPostProcessorAsset = { File f ->
    f.absolutePath.replace('\\', '/').contains('/archetype_post_generate_')
}
def unreferenced = prodFiles.findAll { File f ->
    !allowlist.contains(f.name) && !isPostProcessorAsset(f) &&
            !suiteText.contains(f.name.replace(".groovy", "").toLowerCase())
}
check("1.2 every production file is covered by a suite: ${unreferenced*.name}", unreferenced.isEmpty())

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
// 3) Runner entries point to real files
// ---------------------------------------------------------------------------
def missing = []
(runnerText =~ /"([^"]+\.groovy)"/).each { m ->
    if (!new File(m[1]).isFile()) missing << m[1]
}
check("3.1 every runner entry exists on disk: ${missing}", missing.isEmpty())

// ---------------------------------------------------------------------------
// 4) Post-processor assets: naming, catalog and binding coverage
//    (all counts/relations are DERIVED from the directories)
// ---------------------------------------------------------------------------
def bodyDir = new File("src/main/resources/META-INF/archetype_post_generate_body")
def variableDir = new File("src/main/resources/META-INF/archetype_post_generate_variables")
def snippetDir = new File("src/main/resources/META-INF/archetype_post_generate_snippets")
def generatorDir = new File("src/main/resources/META-INF/archetype_post_generate_generators")
def metadata = new File("target/classes/META-INF/maven/archetype-metadata.xml")

def catalog = new HashSet<String>()
if (metadata.isFile()) {
    (metadata.getText("UTF-8") =~ /<requiredProperty\s+key="([^"]+)"/).each { m -> catalog << m[1] }
}

def listOf = { File dir -> dir.listFiles().findAll { it.isFile() && it.name.endsWith(".groovy") } }
def variableAssets = listOf(variableDir)
def snippetAssets = listOf(snippetDir)
def generatorAssets = listOf(generatorDir)

check("4.1 every variable is named variables-NN-<flag>.groovy",
        variableAssets.every { it.name ==~ /^variables-\d{2}-[a-z][a-z0-9_]*\.groovy$/ })
check("4.2 every snippet is named snippet-NN-<flag>[-<sub>].groovy",
        snippetAssets.every { it.name ==~ /^snippet-\d{2}-[a-z][a-z0-9_]*(-[a-z0-9_]+)?\.groovy$/ })
check("4.3 every generator is named generator-NN-<flag>.groovy",
        generatorAssets.every { it.name ==~ /^generator-\d{2}-[a-z][a-z0-9_]*\.groovy$/ })

// The flag is the part of the name after the NN. A snippet may append a
// sub-axis (-default, -list, ...), so the flag is matched by prefix.
def flagOf = { File f, String prefix ->
    String remainder = f.name.replaceFirst(/^/ + prefix + /-\d{2}-/, "").replace(".groovy", "")
    return catalog.findAll { remainder == it || remainder.startsWith(it + "-") }
            .sort { -it.length() }
            .find { true }
}
def allAssets = [[variableAssets, "variables"], [snippetAssets, "snippet"], [generatorAssets, "generator"]]
def unresolved = []
allAssets.each { assets, prefix ->
    assets.each { f ->
        if (flagOf(f, prefix) == null) unresolved << f.name
    }
}
check("4.4 every asset resolves to a declared catalog flag: ${unresolved}", unresolved.isEmpty())

// Every flag READ by the body/snippets/generators must have a variables
// fragment - the binding layer is the single place that reads the flags.
def readers = []
[bodyDir, snippetDir, generatorDir].each { dir ->
    dir.eachFileRecurse { File f ->
        if (f.isFile() && f.name.endsWith(".groovy")) readers << f
    }
}
def readFlags = new TreeSet<String>()
readers.each { File f ->
    (f.getText("UTF-8") =~ /properties\?\.([A-Za-z_][A-Za-z0-9_]*)/).each { m -> readFlags << m[1] }
}
readFlags.remove("package")
def boundFlags = variableAssets.collect { flagOf(it, "variables") }.findAll { it != null } as Set
def unbound = readFlags.findAll { !boundFlags.contains(it) }
check("4.5 every flag read by the post-processor has a variables fragment: ${unbound}", unbound.isEmpty())

// No orphan binding: every variables fragment's flag is read somewhere.
// Two flags are bound for the post-processor but consumed by the VELOCITY
// templates of the generated project (entity_name, custom_entity_attribute_
// imports): they are explicitly allowlisted here.
def bindingOnlyAllowlist = ["entity_name", "custom_entity_attribute_imports"]
def readerText = readers.collect { it.getText("UTF-8") }.join("\n")
def isUsedByReader = { String flag -> (readerText =~ (/\b/ + flag + /\b/)).find() }
def orphanBindings = boundFlags.findAll { !isUsedByReader(it) && !bindingOnlyAllowlist.contains(it) }
check("4.6 every variables fragment is consumed by a reader: ${orphanBindings}", orphanBindings.isEmpty())

// ---------------------------------------------------------------------------
// 5) EXECUTION coverage: the asset is not only CITED, it is INVOKED by a
//    suite that the runner really wires.
//
//    Check 1.2 only proves that the name of a production file appears
//    somewhere in the text of the suites - a mention in a comment of a suite
//    that is not wired in the runner is enough to pass it. Here the name
//    must appear in a suite that the RUNNER executes.
// ---------------------------------------------------------------------------
def wiredSuites = suiteFiles.findAll { File f -> runnerText.contains(f.name) }
def wiredSuiteText = wiredSuites.collect { it.getText("UTF-8").toLowerCase() }.join("\n")

// The snippets and the generators are invoked BY NAME by the suites that
// exercise them. The variables fragments are derived from the directory by
// the VariablesTest (every fragment is evaluated); the body fragments are
// evaluated by the harness on every run.
def invokedAssets = (snippetAssets + generatorAssets)
        .collect { it.name.replace(".groovy", "").toLowerCase() }
def notInvoked = invokedAssets.findAll { !wiredSuiteText.contains(it) }
check("5.1 every snippet and generator is invoked by a suite the runner wires: ${notInvoked}",
        notInvoked.isEmpty())

// The body fragments the harness evaluates on every run are covered by
// definition; the composition-only ones are allowlisted with a reason.
def harnessText = new File("src/test/archetype-post-generate/PostProcessorHarness.groovy").getText("UTF-8").toLowerCase()
def bodyAssets = listOf(bodyDir).collect { it.name.replace(".groovy", "").toLowerCase() }
def bodyNotEvaluated = bodyAssets.findAll { !harnessText.contains(it) && !allowlist.contains(it + ".groovy") }
check("5.2 every body fragment is evaluated by the harness or is composition-only: ${bodyNotEvaluated}",
        bodyNotEvaluated.isEmpty())

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
