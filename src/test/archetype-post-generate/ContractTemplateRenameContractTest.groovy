/**
 * Contract test for the 04 TEMPLATES folder and its six files (feature 012).
 *
 * The suite is phase-aware:
 *   - Phase 0: 04-referencia-templates-de-contrato + 0X-exemplo-... (feature 009 final state)
 *   - Phase 1: 04-templates-de-contrato + 0X-exemplo-...
 *   - Phase 2: 04-templates-de-contrato + 0X-contrato-...
 *
 * It proves names, counts, the index rows 14-19, the README anchor/title/table,
 * the six contract suites and the legacy redirect.
 */
def procedure = new File("src/main/resources/llm-maven-flags-inference")
def packaged = new File("target/classes/llm-maven-flags-inference")
def readme = new File("README.md")
def index = new File(procedure, "01-inicializacao/01-mapa-dos-arquivos-e-ordem-de-leitura.md")
def legacyRoot = new File("src/main/resources/utils")
def legacy = new File(legacyRoot, "01-entrada-inicializacao/" + ["prompt", "00-leia-este-arquivo-primeiro.md"].join("-"))
def suitesDir = new File("src/test/archetype-post-generate")
def runner = new File("src/test/groovy/run-archetype-post-generate-tests.groovy")

int failures = 0
def check = { String name, boolean cond ->
    if (cond) { println "PASS: ${name}" } else { println "FAIL: ${name}"; failures++ }
}
def filesOf = { File root ->
    def out = []
    if (root.isDirectory()) root.eachFileRecurse { File f -> if (f.isFile()) out << f }
    out
}

def folderOld = "04-referencia-templates-de-contrato"
def folderNew = "04-templates-de-contrato"
def p2Current = [
        "01-schema-do-contrato.json",
        "02-exemplo-contrato-plano.json",
        "03-exemplo-contrato-com-objetos-aninhados.json",
        "04-exemplo-contrato-com-enums.json",
        "05-exemplo-contrato-com-tipos-do-projeto.json",
        "06-exemplo-contrato-com-attributes-explicito.json"
]
def p2New = [
        "01-schema-do-contrato.json",
        "02-contrato-plano.json",
        "03-contrato-com-objetos-aninhados.json",
        "04-contrato-com-enums.json",
        "05-contrato-com-tipos-do-projeto.json",
        "06-contrato-com-attributes-explicito.json"
]
def renamedCurrent = p2Current[1..5]
def renamedNew = p2New[1..5]

def folderPhase = new File(procedure, folderNew).isDirectory() ? 1 : 0
def templatePhase = folderPhase == 0 ? 0 :
        (new File(new File(procedure, folderNew), p2New[1]).isFile() ? 2 : 1)
def folderName = templatePhase == 0 ? folderOld : folderNew
def folder = new File(procedure, folderName)
def expected = templatePhase == 2 ? p2New : p2Current
println "INFO: ContractTemplateRenameContractTest templatePhase=${templatePhase}"

def allSource = filesOf(procedure)
def allPackaged = filesOf(packaged)
def indexText = index.isFile() ? index.getText("UTF-8") : ""
def readmeText = readme.isFile() ? readme.getText("UTF-8") : ""
def legacyText = legacy.isFile() ? legacy.getText("UTF-8") : ""
def runnerText = runner.isFile() ? runner.getText("UTF-8") : ""

// ---------------------------------------------------------------------------
// 1. Structure and counts
// ---------------------------------------------------------------------------
check("1.1 source procedure carries 19 files: ${allSource.size()}", allSource.size() == 19)
check("1.2 source carries 13 Markdown and 6 JSON",
        allSource.count { it.name.endsWith(".md") } == 13 && allSource.count { it.name.endsWith(".json") } == 6)
check("1.3 the fourth subfolder is ${folderName}", folder.isDirectory())
check("1.4 the other 04 folder name is absent",
        templatePhase == 0 ? !new File(procedure, folderNew).exists() : !new File(procedure, folderOld).exists())
check("1.5 folder 04 carries 6 files: ${filesOf(folder).size()}", filesOf(folder).size() == 6)
check("1.6 target/classes mirrors 19 files: ${allPackaged.size()}", allPackaged.size() == 19)
check("1.7 target/classes mirrors folder 04",
        filesOf(new File(packaged, folderName)).collect { it.name }.sort() == expected.sort())

// ---------------------------------------------------------------------------
// 2. Names, numeral and extension
// ---------------------------------------------------------------------------
check("2.1 folder 04 carries the expected names",
        filesOf(folder).collect { it.name }.sort() == expected.sort())
check("2.2 every name keeps the numeral and the .json extension",
        filesOf(folder).every { it.name ==~ /^0[1-6]-[a-z0-9-]+\.json$/ })
check("2.3 the schema keeps its name", new File(folder, "01-schema-do-contrato.json").isFile())
if (templatePhase == 2) {
    check("2.4 no current (exemplo) name remains", renamedCurrent.every { !new File(folder, it).exists() })
} else {
    check("2.4 no target (contrato) name exists yet", renamedNew.every { !new File(folder, it).exists() })
}

// ---------------------------------------------------------------------------
// 3. Index: 19 rows, rows 14-19, roles and single source
// ---------------------------------------------------------------------------
def rows = indexText.readLines().findAll { it ==~ /^\| \d+ \|.*/ }
def expectedPaths = expected.collect { "llm-maven-flags-inference/${folderName}/${it}" }
check("3.1 index carries 19 rows: ${rows.size()}", rows.size() == 19)
check("3.2 rows 14-19 carry the expected paths",
        rows.size() == 19 && (0..5).every { int i ->
            rows[13 + i].startsWith("| ${14 + i} |") && rows[13 + i].contains(expectedPaths[i])
        })
def roles = [
        "Schema permissivo do contrato",
        "Exemplo de contrato plano",
        "Exemplo com objetos aninhados",
        "Exemplo com enums",
        "Exemplo com tipos do projeto",
        "Exemplo com `attributes` explícito"
]
check("3.3 rows 14-19 keep their roles",
        rows.size() == 19 && (0..5).every { int i -> rows[13 + i].contains(roles[i]) })
check("3.4 index is the single source of the global order",
        indexText.contains("O índice é a única fonte da ordem global"))

// ---------------------------------------------------------------------------
// 4. README: folder, anchor, title and table
// ---------------------------------------------------------------------------
check("4.1 README has the folder heading", readmeText.contains("### ${folderName}"))
check("4.2 README has the folder anchor",
        readmeText.contains("id=\"llm-maven-flags-inference-${folderName}\"") &&
                readmeText.contains("#llm-maven-flags-inference-${folderName}"))
check("4.3 README carries the folder path", readmeText.contains("llm-maven-flags-inference/${folderName}/"))
check("4.4 README table carries the expected example names",
        expected[1..5].every { readmeText.contains("| `${it}` |") })
if (templatePhase >= 1) {
    check("4.5 README has no old folder name", !readmeText.contains(folderOld))
}

// ---------------------------------------------------------------------------
// 5. The six contract suites
// ---------------------------------------------------------------------------
def suiteNames = [
        "BuildArtifactsTest.groovy",
        "PathRenameContractTest.groovy",
        "FileRenameContractTest.groovy",
        "ProcedureContractTest.groovy",
        "ContractRuleRenameContractTest.groovy",
        "ProcedureEntryRenameContractTest.groovy"
]
check("5.1 the six contract suites exist", suiteNames.every { new File(suitesDir, it).isFile() })
check("5.2 the runner registers the six contract suites", suiteNames.every { runnerText.contains(it) })
check("5.3 the runner registers this suite", runnerText.contains("ContractTemplateRenameContractTest.groovy"))

// ---------------------------------------------------------------------------
// 6. Legacy redirect
// ---------------------------------------------------------------------------
check("6.1 the legacy redirect exists", legacy.isFile())
check("6.2 the legacy redirect points at the entry point",
        legacyText.contains("llm-maven-flags-inference/01-inicializacao/00-entry-point.md"))
check("6.3 the legacy redirect carries no 04 folder name",
        !legacyText.contains(folderOld) && !legacyText.contains(folderNew))

// ---------------------------------------------------------------------------
// 7. Anti-drift in the operational documents
// ---------------------------------------------------------------------------
def docFiles = []
["01-inicializacao", "02-procedimento-do-agente", "03-regras-de-formacao-do-contrato"].each { String dir ->
    docFiles.addAll(filesOf(new File(procedure, dir)))
}
if (readme.isFile()) docFiles << readme
def docHits = []
docFiles.each { File f ->
    String body = f.getText("UTF-8")
    if (templatePhase >= 1 && body.contains(folderOld)) docHits << "${f.name}: folder"
    if (templatePhase == 2) renamedCurrent.each { if (body.contains(it)) docHits << "${f.name}: ${it}" }
}
check("7.1 no old 04 names in the operational documents: ${docHits}", docHits.isEmpty())

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
