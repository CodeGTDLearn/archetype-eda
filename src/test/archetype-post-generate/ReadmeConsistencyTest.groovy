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

// ---------------------------------------------------------------------------
// 3.5 The operator spec is portable: preamble, canonical entries and no
//     module identity. The preamble points at the packaged procedure; the
//     module identity stays in the procedure.
// ---------------------------------------------------------------------------
def pom = new File("pom.xml")
def pomText = pom.isFile() ? pom.getText("UTF-8") : ""
def versionMatcher = pomText =~ /(?s)<artifactId>eda-archetype<\/artifactId>\s*<version>([^<]+)<\/version>/
def archetypeVersion = versionMatcher.find() ? versionMatcher.group(1).trim() : ""

def scenarioAStart = text.indexOf('<a id="testabilidade-prompts-para-homologacao-manual-geracao-via-opencode-02-cenarios-cenario-a-contrato-plano-3-atributos" name=')
def scenarioBStart = text.indexOf('<a id="testabilidade-prompts-para-homologacao-manual-geracao-via-opencode-02-cenarios-cenario-b-contrato-aninhado-filho-neto" name=')
def flagsStart = text.indexOf('<a id="flags-do-mvn-archetype-generate" name=')
def scenarioA = (scenarioAStart >= 0 && scenarioBStart > scenarioAStart)
        ? text.substring(scenarioAStart, scenarioBStart) : ""
def scenarioB = (scenarioBStart >= 0 && flagsStart > scenarioBStart)
        ? text.substring(scenarioBStart, flagsStart) : ""

def specSections = [canonical: specSection, scenarioA: scenarioA, scenarioB: scenarioB]

def missingPreamble = []
specSections.each { String name, String body ->
    if (!body.contains("0. PROCEDIMENTO:")) missingPreamble << name
}
check("3.5.1 every spec template carries the 0. PROCEDIMENTO preamble: ${missingPreamble}",
        missingPreamble.isEmpty())

def prompt00Path = "utils/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md"
def missingPromptPath = []
specSections.each { String name, String body ->
    if (!body.contains(prompt00Path)) missingPromptPath << name
}
check("3.5.2 every spec template points at the packaged prompt-00: ${missingPromptPath}",
        missingPromptPath.isEmpty())

def expectedArchetype = "com.eda-archetype:eda-archetype:${archetypeVersion}"
def missingVersion = []
specSections.each { String name, String body ->
    if (!body.contains(expectedArchetype)) missingVersion << name
}
check("3.5.3 every spec template pins the pom version ${archetypeVersion}: ${missingVersion}",
        archetypeVersion.length() > 0 && missingVersion.isEmpty())

def requiredEntries = [
        "1. TIPO DE MÓDULO",
        "2. PACKAGE BASE (sem o nome da entidade)",
        "3. USE-CASE",
        "4. CONTRATO JSON",
        "5. PASTA DE DESTINO"
]
def missingEntries = []
specSections.each { String name, String body ->
    requiredEntries.each { String entry ->
        if (!body.contains(entry)) missingEntries << "${name}:${entry}"
    }
}
check("3.5.4 every spec template carries the five canonical entries: ${missingEntries}",
        missingEntries.isEmpty())

check("3.5.5 the canonical template carries the package-base examples",
        specSection.contains("`-Dpackage=org.clinica`") &&
                specSection.contains("`-Dpackage=org.clinica.cliente`"))
check("3.5.6 scenario A and B use distinct destinations",
        scenarioA.contains("C:\\Temp\\eda-005-destino-a") &&
                scenarioB.contains("C:\\Temp\\eda-005-destino-b"))

def forbiddenIdentity = ["ARTIFACT ID", "MODULE VERSION", "-DartifactId", "-Dversion"]
def identityLeaks = []
specSections.each { String name, String body ->
    forbiddenIdentity.each { String token ->
        if (body.contains(token)) identityLeaks << "${name}:${token}"
    }
}
check("3.5.7 the spec templates carry no module identity: ${identityLeaks}",
        identityLeaks.isEmpty())

def whatEntersStart = text.indexOf('<a id="inferencia-de-contrato-e-use-case-do-prompt-ao-maven-cli-o-que-entra-na-spec-ou-prompt" name=')
def whatEntersEnd = text.indexOf('<a id="inferencia-de-contrato-e-use-case-do-prompt-ao-maven-cli-onde-cada-informacao-vai-parar" name=', whatEntersStart)
def whatEnters = (whatEntersStart >= 0 && whatEntersEnd > whatEntersStart)
        ? text.substring(whatEntersStart, whatEntersEnd) : ""
def whatEntersLower = whatEnters.toLowerCase()
def entryMarkers = ["tipo de módulo", "package base", "use case", "contrato json", "pasta de destino"]
def missingMarkers = entryMarkers.findAll { String marker -> !whatEntersLower.contains(marker) }
check("3.5.8 the spec entry table lists the five entries: ${missingMarkers}",
        missingMarkers.isEmpty())

// ---------------------------------------------------------------------------
// 3.6 Reproducibility: the generation command pins the archetype plugin
//     version, and the README and the packaged procedure agree with the
//     pluginManagement of the pom. The pin keeps requiresProject=false true
//     for every generation, regardless of what the ~/.m2 resolves.
// ---------------------------------------------------------------------------
def pluginVersionMatcher = pomText =~ /(?s)<artifactId>maven-archetype-plugin<\/artifactId>\s*<version>([^<]+)<\/version>/
def pluginVersion = pluginVersionMatcher.find() ? pluginVersionMatcher.group(1).trim() : ""
def pluginCoordinate = "maven-archetype-plugin:${pluginVersion}"
def prompt04 = new File("src/main/resources/utils/02-operacao-procedimento-do-agente/prompt-04-fluxo-de-execucao-e-verificacao.md")
def prompt04Text = prompt04.isFile() ? prompt04.getText("UTF-8") : ""
check("3.6.1 the pom pins the archetype plugin: ${pluginVersion}",
        pluginVersion.length() > 0)
check("3.6.2 the README generation example pins the archetype plugin: ${pluginCoordinate}",
        text.contains(pluginCoordinate))
check("3.6.3 the packaged procedure pins the archetype plugin: ${pluginCoordinate}",
        prompt04Text.contains(pluginCoordinate))
check("3.6.4 the packaged procedure keeps the requiresProject=false note",
        prompt04Text.contains("requiresProject=false") && prompt04Text.contains("standalone-pom"))

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
