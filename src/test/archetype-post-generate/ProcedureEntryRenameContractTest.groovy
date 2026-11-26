/**
 * Contract test for the ENTRY/INDEX/OPERATION RENAME (feature 010).
 *
 * Phase-aware:
 *   - Phase 1: 01 carries the two new names; 02 still carries the five old names.
 *   - Phase 2: all seven names are new.
 *
 * Proves names, subfolder names, counts 19/13/6/4, index order/roles, the
 * legacy redirect, the absence of old references in the operational corpus
 * and the source/target mirror.
 */
def procedure = new File("src/main/resources/llm-maven-flags-inference")
def packaged = new File("target/classes/llm-maven-flags-inference")
def readme = new File("README.md")
def legacyRoot = new File("src/main/resources/utils")
def legacy = new File(legacyRoot, "01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md")
def suitesDir = new File("src/test/archetype-post-generate")
def self = new File(suitesDir, "ProcedureEntryRenameContractTest.groovy")

def entryOld = "prompt-00-leia-este-arquivo-primeiro.md"
def entryNew = "00-entry-point.md"
def indexOld = "indice-01-mapa-dos-arquivos-e-ordem-de-leitura.md"
def indexNew = "01-mapa-dos-arquivos-e-ordem-de-leitura.md"

def p02Old = [
        "prompt-01-papel-e-restricoes-do-agente.md",
        "prompt-02-como-localizar-e-ler-o-archetype.md",
        "prompt-03-regras-obrigatorias-de-geracao.md",
        "prompt-04-fluxo-de-execucao-e-verificacao.md",
        "prompt-05-entregaveis-e-relatorios.md"
]
def p02New = [
        "01-papel-e-restricoes-do-agente.md",
        "02-como-localizar-e-ler-o-archetype.md",
        "03-regras-obrigatorias-de-geracao.md",
        "04-fluxo-de-execucao-e-verificacao.md",
        "05-entregaveis-e-relatorios.md"
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
def phase2 = new File(procedure, "02-operacao-procedimento-do-agente/${p02New[0]}").isFile()
def expected02 = phase2 ? p02New : p02Old
def p01 = new File(procedure, "01-entrada-inicializacao")
def p02 = new File(procedure, "02-operacao-procedimento-do-agente")
def index = new File(p01, indexNew)
def indexText = index.isFile() ? index.getText("UTF-8") : ""
def readmeText = readme.isFile() ? readme.getText("UTF-8") : ""
def legacyText = legacy.isFile() ? legacy.getText("UTF-8") : ""

println "INFO: ProcedureEntryRenameContractTest phase=${phase2 ? 2 : 1}"

// ---------------------------------------------------------------------------
// 1. Structure and counts
// ---------------------------------------------------------------------------
def source = filesOf(procedure)
def target = filesOf(packaged)
check("1.1 source procedure carries 19 files: ${source.size()}", source.size() == 19)
check("1.2 source procedure carries 13 Markdown and 6 JSON",
        source.count { it.name.endsWith(".md") } == 13 && source.count { it.name.endsWith(".json") } == 6)
check("1.3 source procedure has exactly the four subfolders",
        procedure.listFiles().findAll { it.isDirectory() }*.name.sort() ==
                ["01-entrada-inicializacao", "02-operacao-procedimento-do-agente",
                 "03-regras-de-formacao-do-contrato", "04-referencia-templates-de-contrato"].sort())
check("1.4 subfolder 01 carries exactly 2 files: ${filesOf(p01).size()}", filesOf(p01).size() == 2)
check("1.5 subfolder 02 carries exactly 5 files: ${filesOf(p02).size()}", filesOf(p02).size() == 5)
check("1.6 target/classes carries 19 files: ${target.size()}", target.size() == 19)
check("1.7 target/classes carries 13 Markdown and 6 JSON",
        target.count { it.name.endsWith(".md") } == 13 && target.count { it.name.endsWith(".json") } == 6)

// ---------------------------------------------------------------------------
// 2. Names in 01 and 02
// ---------------------------------------------------------------------------
check("2.1 01 carries exactly the 2 new names",
        filesOf(p01)*.name.sort() == [entryNew, indexNew].sort())
check("2.2 no old entry/index file remains in 01",
        !new File(p01, entryOld).exists() && !new File(p01, indexOld).exists())
check("2.3 02 carries exactly the 5 ${phase2 ? 'new' : 'old'} names",
        filesOf(p02)*.name.sort() == expected02.sort())
if (phase2) {
    check("2.4 no old 02 name remains", p02Old.every { !new File(p02, it).exists() })
    check("2.5 02 carries no prompt-* file", filesOf(p02).every { !it.name.startsWith("prompt-") })
} else {
    check("2.4 02 still carries the 5 old names", p02Old.every { new File(p02, it).isFile() })
}
check("2.6 target/classes mirrors the 01 names",
        filesOf(new File(packaged, "01-entrada-inicializacao"))*.name.sort() == [entryNew, indexNew].sort())
check("2.7 target/classes mirrors the 02 names",
        filesOf(new File(packaged, "02-operacao-procedimento-do-agente"))*.name.sort() == expected02.sort())

// ---------------------------------------------------------------------------
// 3. Index: paths, order and roles
// ---------------------------------------------------------------------------
def expectedPaths = [
        "llm-maven-flags-inference/01-entrada-inicializacao/${entryNew}",
        "llm-maven-flags-inference/01-entrada-inicializacao/${indexNew}"
] + expected02.collect { "llm-maven-flags-inference/02-operacao-procedimento-do-agente/${it}" }
check("3.1 index exists at the new path", index.isFile())
check("3.2 index lists the 19 expected paths", expectedPaths.every { indexText.contains(it) })
check("3.3 index does not list old 01 paths",
        !indexText.contains("llm-maven-flags-inference/01-entrada-inicializacao/${entryOld}") &&
                !indexText.contains("llm-maven-flags-inference/01-entrada-inicializacao/${indexOld}"))
if (phase2) {
    check("3.4 index does not list old 02 paths",
            p02Old.every { !indexText.contains("llm-maven-flags-inference/02-operacao-procedimento-do-agente/${it}") })
} else {
    check("3.4 index still lists old 02 paths",
            p02Old.every { indexText.contains("llm-maven-flags-inference/02-operacao-procedimento-do-agente/${it}") })
}
def roles = ["Porta de entrada", "Este índice", "Papel e restrições", "Localização e leitura do JAR",
             "Regras obrigatórias de geração", "Execução e verificação", "Entregáveis e relatórios"]
def tableRows = indexText.readLines().findAll { it ==~ /^\| [1-7] \|.*/ }
check("3.5 index keeps rows 1 to 7", (1..7).every { indexText.contains("| ${it} |") })
check("3.6 index keeps the reading order and roles",
        tableRows.size() == 7 &&
                (0..6).every { int i -> tableRows[i].startsWith("| ${i + 1} |") && tableRows[i].contains(roles[i]) })
check("3.7 index declares the 19 files count", indexText.contains("19 arquivos"))

// ---------------------------------------------------------------------------
// 4. Legacy redirect
// ---------------------------------------------------------------------------
check("4.1 the legacy redirect exists", legacy.isFile())
check("4.2 the legacy redirect points at 00-entry-point.md",
        legacyText.contains("llm-maven-flags-inference/01-entrada-inicializacao/00-entry-point.md"))
check("4.3 the legacy redirect keeps no old entry path",
        !legacyText.contains("prompt-00-leia-este-arquivo-primeiro.md"))
check("4.4 the legacy root carries exactly 1 file: ${filesOf(legacyRoot).size()}", filesOf(legacyRoot).size() == 1)
check("4.5 the legacy root keeps only the entry subfolder",
        legacyRoot.isDirectory() && legacyRoot.listFiles().findAll { it.isDirectory() }*.name == ["01-entrada-inicializacao"])

// ---------------------------------------------------------------------------
// 5. Anti-drift in the operational corpus
// ---------------------------------------------------------------------------
def corpus = []
procedure.eachFileRecurse { File f ->
    if (f.isFile() && (f.name.endsWith(".md") || f.name.endsWith(".json"))) corpus << f
}
if (readme.isFile()) corpus << readme
suitesDir.eachFileRecurse { File f ->
    if (f.isFile() && f.name.endsWith(".groovy") &&
            f.canonicalPath != self.canonicalPath && f.name != "PathRenameContractTest.groovy" &&
            f.name != "ContractRuleRenameContractTest.groovy") {
        corpus << f
    }
}
def oldPatterns = phase2 ? [~/prompt-0[0-5]/, ~/indice-01/] : [~/prompt-00/, ~/indice-01/]
def hits = []
corpus.each { File f ->
    String body = f.getText("UTF-8")
    oldPatterns.each { p ->
        if (body =~ p) hits << "${f.path}: ${p}"
    }
}
check("5.1 no old references in the ${phase2 ? 'operational' : 'Phase 1'} corpus: ${hits}", hits.isEmpty())

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
