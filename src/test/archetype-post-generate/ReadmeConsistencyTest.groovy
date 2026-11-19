/**
 * Anti-drift test for the README.
 *
 * The README publishes the numbers of the tree it documents: the size of the
 * catalog, the number of filesets, the size of the packaged procedure, the
 * number of post-processor fragments. Those numbers were never verified - a
 * new flag, a new fileset or a new fragment could be added and the README
 * would keep promising the old count.
 *
 * This suite derives every published number from the tree and asserts it.
 */
def readme = new File("README.md")
def suitesDir = new File("src/test/archetype-post-generate")
def runner = new File("src/test/groovy/run-archetype-post-generate-tests.groovy")
def fixturesDir = new File("src/test/resources/projects")
def propertiesDir = new File("src/main/resources/META-INF/maven/archetype_metadata_required_properties")
def filesetsDir = new File("src/main/resources/META-INF/maven/archetype_metadata_filesets")
def utilsDir = new File("src/main/resources/utils")
def metaDir = new File("src/main/resources/META-INF")
def quickstart = new File("specs/001-archetype-integration-tests/quickstart.md")

int failures = 0
def check = { String n, boolean c -> if (c) { println "PASS: ${n}" } else { println "FAIL: ${n}"; failures++ } }

check("1.1 the README exists", readme.isFile())
if (!readme.isFile()) {
    println ""
    println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
    if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
    return
}
def text = readme.getText("UTF-8")

// ---------------------------------------------------------------------------
// The published sizes
// ---------------------------------------------------------------------------
int propertyCount = propertiesDir.listFiles().count { it.isFile() && it.name.endsWith(".xml") }
int filesetCount = filesetsDir.listFiles().count { it.isFile() && it.name.endsWith(".xml") }
int utilsCount = 0
utilsDir.eachFileRecurse { File f -> if (f.isFile()) utilsCount++ }

def assetCounts = [:]
["body", "variables", "generators", "snippets"].each { String dir ->
    File d = new File(metaDir, "archetype_post_generate_${dir}")
    int n = 0
    d.eachFileRecurse { File f -> if (f.isFile() && f.name.endsWith(".groovy")) n++ }
    assetCounts[dir] = n
}
int fragmentCount = assetCounts.values().sum()

check("2.1 the README publishes the size of the catalog: ${propertyCount} flags",
        text.contains("${propertyCount} flags") || text.contains("${propertyCount} property-"))
check("2.2 the README publishes the size of the filesets: ${filesetCount} filesets",
        text.contains("${filesetCount} arquivos") || text.contains("${filesetCount} fileset-"))
check("2.3 the README publishes the size of the packaged procedure: ${utilsCount} arquivos",
        text.contains("${utilsCount} arquivos"))
check("2.4 the README publishes the size of the post-processor: ${fragmentCount} fragmentos",
        text.contains("${fragmentCount} fragmentos"))
check("2.5 the README publishes the composition of the post-processor",
        text.contains("${assetCounts.body} `body/`") &&
                text.contains("${assetCounts.variables} `variables/`") &&
                text.contains("${assetCounts.generators} `generators/`") &&
                text.contains("${assetCounts.snippets} `snippets/`"))

// ---------------------------------------------------------------------------
// Every fixture of the tree is documented
// ---------------------------------------------------------------------------
def fixtures = fixturesDir.listFiles().findAll { it.isDirectory() }.sort { it.name }*.name
def quickstartText = quickstart.isFile() ? quickstart.getText("UTF-8") : ""
// The fixture detail lives in the matrix index since 2026-11-12
// (src/test/resources/projects/README.md); the README and the quickstart link to it.
def fixturesIndex = new File(fixturesDir, "README.md")
def fixturesIndexText = fixturesIndex.isFile() ? fixturesIndex.getText("UTF-8") : ""
def undocumentedFixtures = fixtures.findAll { String name ->
    !text.contains(name) && !quickstartText.contains(name) && !fixturesIndexText.contains(name)
}
check("3.1 every fixture of the tree is documented: ${undocumentedFixtures}", undocumentedFixtures.isEmpty())

// ---------------------------------------------------------------------------
// Every suite of the tree is wired in the runner
// ---------------------------------------------------------------------------
def helpers = ["PostProcessorHarness.groovy", "TemplateRenderer.groovy"]
def suites = suitesDir.listFiles().findAll { File f ->
    f.isFile() && f.name.endsWith(".groovy") && !f.name.startsWith("run-") && !helpers.contains(f.name)
}*.name
def runnerText = runner.getText("UTF-8")
def notWired = suites.findAll { String name -> !runnerText.contains(name) }
check("3.2 every suite of the tree is wired in the runner: ${notWired}", notWired.isEmpty())

// ---------------------------------------------------------------------------
// 3.3 The README MUST NOT hard-code the test counts
//
// The suite/check/fixture counts drift with every new suite (they went
// 24 -> 30 -> 34 suites and 314 -> 505 checks during a single working day).
// The README describes the MECHANISM; the counts live in the runner and in the
// fixture tree. A number in the prose is a promise nobody re-verifies.
// ---------------------------------------------------------------------------
def hardCoded = []
(text =~ /(?m)^.*\b\d+\s+(?:suites|suítes|checks|fixtures)\b.*$/).each { m ->
    hardCoded << m[0].trim()
}
check("3.3 the README does not hard-code the test counts: ${hardCoded}", hardCoded.isEmpty())

// ---------------------------------------------------------------------------
// 3.4 The operator spec template documents the destination entry
//
// The "Procedimento para Especificação/Spec" section is the canonical place
// where the operator learns the minimal spec. It MUST match the procedure:
// three entries (use-case, contract, destination) and Gates A-D.
// ---------------------------------------------------------------------------
def specSectionStart = text.indexOf('<a id="procedimento-para-especificacao-spec" name=')
def specSectionEnd = specSectionStart < 0 ? -1 : text.indexOf("### Onde estão as regras", specSectionStart)
def specSection = (specSectionStart >= 0 && specSectionEnd > specSectionStart)
        ? text.substring(specSectionStart, specSectionEnd) : ""
check("3.4 the operator spec section exists", !specSection.isEmpty())
check("3.4.1 the operator spec section documents PASTA DE DESTINO",
        specSection.contains("PASTA DE DESTINO"))
check("3.4.2 the operator spec section names Gate D", specSection.contains("Gate D"))
check("3.4.3 the operator spec example carries the destination section",
        specSection.contains("5. PASTA DE DESTINO:"))

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
