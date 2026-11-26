/**
 * Contract test for the CONTRACT RULE RENAME (feature 011).
 *
 * Phase-aware:
 *   - Phase 0: old folder, current names.
 *   - Phase 1: new folder, current names.
 *   - Phase 2: new folder, 01-03 new, 04-06 current.
 *   - Phase 3: new folder, all six names new.
 *
 * Proves folder/name invariants, counts 19/13/6/4, index order/roles and
 * single source, README anchors/title/table, fixture comments, the legacy
 * redirect and the absence of old references in the operational corpus.
 */
def procedure = new File("src/main/resources/llm-maven-flags-inference")
def packaged = new File("target/classes/llm-maven-flags-inference")
def readme = new File("README.md")
def legacyRoot = new File("src/main/resources/utils")
def legacy = new File(legacyRoot, "01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md")
def suitesDir = new File("src/test/archetype-post-generate")
def self = new File(suitesDir, "ContractRuleRenameContractTest.groovy")

def oldFolderName = "03-derivacao-regras-de-formacao-do-contrato"
def newFolderName = "03-regras-de-formacao-do-contrato"
def p1Current = [
        "01-como-derivar-a-entidade-do-contrato.md",
        "02-como-resolver-tipos-e-enums.md",
        "03-como-derivar-flags-de-tipos-aninhados.md",
        "04-como-aplicar-precedencia-e-validacao.md",
        "05-como-materializar-e-emitir-o-contrato.md",
        "06-como-relatar-proveniencia.md"
]
def p1New = [
        "01-derivar-a-entidade-do-contrato.md",
        "02-resolver-tipos-e-enums.md",
        "03-derivar-flags-de-tipos-aninhados.md",
        "04-aplicar-precedencia-e-validacao.md",
        "05-materializar-e-emitir-o-contrato.md",
        "06-relatar-proveniencia.md"
]
def fixtureFiles = [
        "src/test/resources/projects/custom-nested-edge-cases/archetype.properties",
        "src/test/resources/projects/custom-nested-objects/archetype.properties",
        "src/test/resources/projects/entity-contract/archetype.properties",
        "src/test/resources/projects/custom-enums-from-payload/archetype.properties"
]

int failures = 0
def check = { String name, boolean cond ->
    if (cond) { println "PASS: ${name}" } else { println "FAIL: ${name}"; failures++ }
}
def filesOf = { File root ->
    def out = []
    if (root.isDirectory()) root.eachFileRecurse { File f -> if (f.isFile()) out << f }
    out
}

def oldFolder = new File(procedure, oldFolderName)
def newFolder = new File(procedure, newFolderName)
def rulePhase = !newFolder.isDirectory() ? 0 :
        (p1New.every { new File(newFolder, it).isFile() } ? 3 :
                (p1New[0..2].every { new File(newFolder, it).isFile() } ? 2 : 1))
def folder = rulePhase == 0 ? oldFolder : newFolder
def folderName = rulePhase == 0 ? oldFolderName : newFolderName
def expected03 = rulePhase <= 1 ? p1Current : (rulePhase == 2 ? p1New[0..2] + p1Current[3..5] : p1New)
def rejected03 = (rulePhase >= 2 ? p1Current[0..2] : []) + (rulePhase == 3 ? p1Current[3..5] : [])

println "INFO: ContractRuleRenameContractTest phase=${rulePhase}"

def p01 = ["00-entry-point.md", "01-mapa-dos-arquivos-e-ordem-de-leitura.md"]
def p02 = [
        "01-papel-e-restricoes-do-agente.md",
        "02-localizar-e-ler-o-archetype.md",
        "03-regras-obrigatorias-de-geracao.md",
        "04-fluxo-de-execucao-e-verificacao.md",
        "05-entregaveis-e-relatorios.md"
]
def p04 = [
        "01-schema-do-contrato.json",
        "02-contrato-plano.json",
        "03-contrato-com-objetos-aninhados.json",
        "04-contrato-com-enums.json",
        "05-contrato-com-tipos-do-projeto.json",
        "06-contrato-com-attributes-explicito.json"
]
def expectedPaths =
        p01.collect { "llm-maven-flags-inference/01-inicializacao/${it}" } +
                p02.collect { "llm-maven-flags-inference/02-procedimento-do-agente/${it}" } +
                expected03.collect { "llm-maven-flags-inference/${folderName}/${it}" } +
                p04.collect { "llm-maven-flags-inference/04-templates-de-contrato/${it}" }
def roles = [
        "Porta de entrada",
        "Este índice",
        "Papel e restrições",
        "Localização e leitura do JAR",
        "Regras obrigatórias de geração",
        "Execução e verificação",
        "Entregáveis e relatórios",
        "Entidade, nome, pacote e atributos",
        "Tipos Java e enums",
        "Imports, enums e tipos aninhados",
        "Precedência e validação",
        "Contrato materializado",
        "Proveniência",
        "Schema permissivo do contrato",
        "Exemplo de contrato plano",
        "Exemplo com objetos aninhados",
        "Exemplo com enums",
        "Exemplo com tipos do projeto",
        "Exemplo com `attributes` explícito"
]
def index = new File(procedure, "01-inicializacao/01-mapa-dos-arquivos-e-ordem-de-leitura.md")
def indexText = index.isFile() ? index.getText("UTF-8") : ""
def readmeText = readme.isFile() ? readme.getText("UTF-8") : ""
def legacyText = legacy.isFile() ? legacy.getText("UTF-8") : ""

// ---------------------------------------------------------------------------
// 1. Structure and counts
// ---------------------------------------------------------------------------
def source = filesOf(procedure)
def target = filesOf(packaged)
check("1.1 source procedure carries 19 files: ${source.size()}", source.size() == 19)
check("1.2 source procedure carries 13 Markdown and 6 JSON",
        source.count { it.name.endsWith(".md") } == 13 && source.count { it.name.endsWith(".json") } == 6)
check("1.3 source procedure has 4 top-level subfolders",
        procedure.listFiles().findAll { it.isDirectory() }.size() == 4)
check("1.4 subfolder 03 carries 6 files: ${filesOf(folder).size()}", filesOf(folder).size() == 6)
check("1.5 target/classes carries 19 files: ${target.size()}", target.size() == 19)
check("1.6 target/classes carries 13 Markdown and 6 JSON",
        target.count { it.name.endsWith(".md") } == 13 && target.count { it.name.endsWith(".json") } == 6)

// ---------------------------------------------------------------------------
// 2. Folder
// ---------------------------------------------------------------------------
check("2.1 folder state for phase ${rulePhase}",
        rulePhase == 0 ? (oldFolder.isDirectory() && !newFolder.exists())
                : (newFolder.isDirectory() && !oldFolder.exists()))

// ---------------------------------------------------------------------------
// 3. Files
// ---------------------------------------------------------------------------
check("3.1 03 carries exactly the expected names",
        filesOf(folder)*.name.sort() == expected03.sort())
check("3.2 no rejected old 03 names remain",
        rejected03.every { !new File(folder, it).exists() })
check("3.3 no legacy regras-* names remain",
        filesOf(folder).every { !it.name.startsWith("regras-") })
if (rulePhase == 3) {
    check("3.4 no como- file remains", filesOf(folder).every { !it.name.contains("-como-") })
}

// ---------------------------------------------------------------------------
// 4. Index: 19 rows, order, roles and single source
// ---------------------------------------------------------------------------
def tableRows = indexText.readLines().findAll { it ==~ /^\| \d+ \|.*/ }
check("4.1 index carries 19 rows: ${tableRows.size()}", tableRows.size() == 19)
check("4.2 index paths are in the same order",
        tableRows.size() == 19 && (0..18).every { int i ->
            tableRows[i].startsWith("| ${i + 1} |") && tableRows[i].contains(expectedPaths[i])
        })
check("4.3 index roles are preserved",
        tableRows.size() == 19 && (0..18).every { int i -> tableRows[i].contains(roles[i]) })
check("4.4 index is the single source of the global order",
        indexText.contains("O índice é a única fonte da ordem global"))
check("4.5 index declares the 19 files count", indexText.contains("19 arquivos"))

// ---------------------------------------------------------------------------
// 5. README: folder, anchors, title and table
// ---------------------------------------------------------------------------
check("5.1 README has the folder heading", readmeText.contains("### ${folderName}"))
check("5.2 README has the folder anchor",
        readmeText.contains("id=\"llm-maven-flags-inference-${folderName}\"") &&
                readmeText.contains("#llm-maven-flags-inference-${folderName}"))
check("5.3 README has the folder path",
        readmeText.contains("llm-maven-flags-inference/${folderName}/"))
check("5.4 README table carries the expected names",
        expected03.every { readmeText.contains("| `${it}` |") })
if (rulePhase >= 1) {
    check("5.5 README has no old folder name", !readmeText.contains(oldFolderName))
}

// ---------------------------------------------------------------------------
// 6. Fixtures
// ---------------------------------------------------------------------------
check("6.1 fixtures carry the expected folder",
        fixtureFiles.every { new File(it).getText("UTF-8").contains("llm-maven-flags-inference/${folderName}/") })
if (rulePhase >= 1) {
    check("6.2 fixtures have no old folder name",
            fixtureFiles.every { !new File(it).getText("UTF-8").contains(oldFolderName) })
}

// ---------------------------------------------------------------------------
// 7. Legacy redirect
// ---------------------------------------------------------------------------
check("7.1 the legacy redirect exists", legacy.isFile())
check("7.2 the legacy redirect points at 00-entry-point.md",
        legacyText.contains("llm-maven-flags-inference/01-inicializacao/00-entry-point.md"))
check("7.3 the legacy redirect keeps no old entry path",
        !legacyText.contains("prompt-00-leia-este-arquivo-primeiro.md"))

// ---------------------------------------------------------------------------
// 8. Anti-drift in the operational corpus
// ---------------------------------------------------------------------------
def corpus = []
procedure.eachFileRecurse { File f -> if (f.isFile()) corpus << f }
if (readme.isFile()) corpus << readme
fixtureFiles.each { String path -> corpus << new File(path) }
suitesDir.eachFileRecurse { File f ->
    if (f.isFile() && f.name.endsWith(".groovy") &&
            f.canonicalPath != self.canonicalPath &&
            f.name != "FileRenameContractTest.groovy") {
        corpus << f
    }
}
def hits = []
corpus.each { File f ->
    String body = f.getText("UTF-8")
    if (rulePhase >= 1 && body.contains(oldFolderName)) hits << "${f.name}: old folder"
    rejected03.each { String oldName -> if (body.contains(oldName)) hits << "${f.name}: ${oldName}" }
    if (body =~ /regras-0[1-6]/) hits << "${f.name}: regras-*"
}
check("8.1 no old references in the operational corpus: ${hits}", hits.isEmpty())

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
