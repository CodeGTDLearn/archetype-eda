/**
 * Contract test for the RENAMED PROCEDURE (feature 009).
 *
 * The suite is phase-aware:
 *   - rulePhase 0..3: the six rules of folder 03 (feature 011).
 *   - templatePhase 0..2: folder 04 and its six files (feature 012):
 *       Phase 0 = 04-referencia-templates-de-contrato + 0X-exemplo-...;
 *       Phase 1 = 04-templates-de-contrato + 0X-exemplo-...;
 *       Phase 2 = 04-templates-de-contrato + 0X-contrato-....
 *
 * It proves names, counts, the index, the absence of old references in the
 * operational corpus and the catalog invariants (35 properties / 18 filesets).
 */
def procedure = new File("src/main/resources/llm-maven-flags-inference")
def r03Old = new File(procedure, "03-derivacao-regras-de-formacao-do-contrato")
def r03New = new File(procedure, "03-regras-de-formacao-do-contrato")
def r04Old = new File(procedure, "04-referencia-templates-de-contrato")
def r04New = new File(procedure, "04-templates-de-contrato")
def packaged = new File("target/classes/llm-maven-flags-inference")
def readme = new File("README.md")
def index = new File(procedure, "01-inicializacao/01-mapa-dos-arquivos-e-ordem-de-leitura.md")
def catalog = new File("target/classes/META-INF/maven/archetype-metadata.xml")
def suitesDir = new File("src/test/archetype-post-generate")
def self = new File(suitesDir, "FileRenameContractTest.groovy")

int failures = 0
def check = { String name, boolean cond ->
    if (cond) { println "PASS: ${name}" } else { println "FAIL: ${name}"; failures++ }
}
def filesOf = { File root ->
    def out = []
    if (root.isDirectory()) root.eachFileRecurse { File f -> if (f.isFile()) out << f }
    out
}

def p1LegacyOld = [
        "regras-01-como-derivar-a-entidade-do-contrato.md",
        "regras-02-como-resolver-tipos-e-enums.md",
        "regras-03-como-derivar-flags-de-tipos-aninhados.md",
        "regras-04-como-aplicar-precedencia-e-validacao.md",
        "regras-05-como-materializar-e-emitir-o-contrato.md",
        "regras-06-como-relatar-proveniencia.md"
]
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
def rulePhase = !r03New.isDirectory() ? 0 :
        (p1New.every { new File(r03New, it).isFile() } ? 3 :
                (p1New[0..2].every { new File(r03New, it).isFile() } ? 2 : 1))
def r03 = rulePhase == 0 ? r03Old : r03New
def expected03 = rulePhase <= 1 ? p1Current : (rulePhase == 2 ? p1New[0..2] + p1Current[3..5] : p1New)
def rejected03 = p1LegacyOld + (rulePhase >= 2 ? p1Current[0..2] : []) + (rulePhase == 3 ? p1Current[3..5] : [])
def p2LegacyOld = [
        "template-01-schema-do-contrato.json",
        "template-02-exemplo-contrato-plano.json",
        "template-03-exemplo-contrato-com-objetos-aninhados.json",
        "template-04-exemplo-contrato-com-enums.json",
        "template-05-exemplo-contrato-com-tipos-do-projeto.json",
        "template-06-exemplo-contrato-com-attributes-explicito.json"
]
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
def p2RenamedCurrent = p2Current[1..5]

def templatePhase = !r04New.isDirectory() ? 0 :
        (new File(r04New, p2New[1]).isFile() ? 2 : 1)
def r04 = templatePhase == 0 ? r04Old : r04New
println "INFO: FileRenameContractTest templatePhase=${templatePhase}"

def allSource = filesOf(procedure)
def allPackaged = filesOf(packaged)

// ---------------------------------------------------------------------------
// 1. Counts and structure
// ---------------------------------------------------------------------------
check("1.1 source procedure carries 19 files: ${allSource.size()}", allSource.size() == 19)
check("1.2 source procedure carries 13 Markdown and 6 JSON",
        allSource.count { it.name.endsWith(".md") } == 13 && allSource.count { it.name.endsWith(".json") } == 6)
check("1.3 source procedure has 4 top-level subfolders",
        procedure.listFiles().findAll { it.isDirectory() }.size() == 4)
check("1.4 subfolder 03 carries 6 files: ${filesOf(r03).size()}", filesOf(r03).size() == 6)
check("1.5 subfolder 04 carries 6 files: ${filesOf(r04).size()}", filesOf(r04).size() == 6)
check("1.6 target/classes carries 19 files: ${allPackaged.size()}", allPackaged.size() == 19)
check("1.7 target/classes carries 13 Markdown and 6 JSON",
        allPackaged.count { it.name.endsWith(".md") } == 13 && allPackaged.count { it.name.endsWith(".json") } == 6)

// ---------------------------------------------------------------------------
// 2. Names
// ---------------------------------------------------------------------------
def expected04 = templatePhase == 2 ? p2New : p2Current
check("2.1 03 carries exactly the expected names",
        filesOf(r03).collect { it.name }.sort() == expected03.sort())
check("2.2 04 carries exactly the 6 ${templatePhase == 2 ? 'new' : 'current'} names",
        filesOf(r04).collect { it.name }.sort() == expected04.sort())
check("2.3 no old 03 names remain", rejected03.every { !new File(r03, it).exists() })
if (templatePhase == 2) {
    check("2.4 no old 04 names remain", p2RenamedCurrent.every { !new File(r04, it).exists() })
} else {
    check("2.4 04 still carries the current names before Phase 2", p2Current.every { new File(r04, it).isFile() })
}
check("2.5 target/classes mirrors the 03 names",
        filesOf(new File(packaged, r03.name)).collect { it.name }.sort() == expected03.sort())
check("2.6 target/classes mirrors the 04 names",
        filesOf(new File(packaged, r04.name)).collect { it.name }.sort() == expected04.sort())

// ---------------------------------------------------------------------------
// 3. Index
// ---------------------------------------------------------------------------
def indexText = index.isFile() ? index.getText("UTF-8") : ""
def idx03 = expected03.collect { "llm-maven-flags-inference/${r03.name}/${it}" }
def idx04 = expected04.collect { "llm-maven-flags-inference/${r04.name}/${it}" }
check("3.1 index lists the 6 new 03 paths", idx03.every { indexText.contains(it) })
check("3.2 index lists the 6 ${templatePhase == 2 ? 'new' : 'current'} 04 paths", idx04.every { indexText.contains(it) })
check("3.3 index does not list old 03 paths",
        rejected03.every { !indexText.contains("llm-maven-flags-inference/${r03.name}/${it}") })
if (templatePhase == 2) {
    check("3.4 index does not list old 04 paths",
            p2RenamedCurrent.every { !indexText.contains("llm-maven-flags-inference/${r04.name}/${it}") })
} else {
    check("3.4 index still lists the current 04 paths before Phase 2",
            p2Current.every { indexText.contains("llm-maven-flags-inference/${r04.name}/${it}") })
}

// ---------------------------------------------------------------------------
// 4. Anti-drift in the operational corpus
// ---------------------------------------------------------------------------
def corpus = []
["01-inicializacao", "02-procedimento-do-agente", r03.name].each { String dir ->
    corpus.addAll(filesOf(new File(procedure, dir)))
}
if (templatePhase >= 1) corpus.addAll(filesOf(r04))
if (readme.isFile()) corpus << readme
suitesDir.eachFileRecurse { File f ->
    if (f.isFile() && f.name.endsWith(".groovy") && f.canonicalPath != self.canonicalPath &&
            f.name != "ContractRuleRenameContractTest.groovy") corpus << f
}
def hits = []
corpus.each { File f ->
    String body = f.getText("UTF-8")
    if ((body =~ /regras-0[1-6]/).find()) hits << "${f.name}: regras"
    if (templatePhase == 2 && (body =~ /template-0[1-6]/).find()) hits << "${f.name}: template"
    rejected03.each { String oldName -> if (body.contains(oldName)) hits << "${f.name}: ${oldName}" }
}
check("4.1 no old references in the ${templatePhase == 2 ? 'operational corpus' : 'Phase 1 corpus'}: ${hits}", hits.isEmpty())

def docCorpus = []
["01-inicializacao", "02-procedimento-do-agente", r03.name].each { String dir ->
    docCorpus.addAll(filesOf(new File(procedure, dir)))
}
if (readme.isFile()) docCorpus << readme
def docHits = []
docCorpus.each { File f ->
    String body = f.getText("UTF-8")
    if (templatePhase >= 1 && body.contains("04-referencia-templates-de-contrato")) docHits << "${f.name}: folder"
    if (templatePhase == 2) p2RenamedCurrent.each { if (body.contains(it)) docHits << "${f.name}: ${it}" }
}
check("4.2 no old 04 references in the operational documents: ${docHits}", docHits.isEmpty())

// ---------------------------------------------------------------------------
// 5. Catalog invariants
// ---------------------------------------------------------------------------
def catalogText = catalog.isFile() ? catalog.getText("UTF-8") : ""
check("5.1 catalog carries 35 required properties: ${(catalogText =~ /<requiredProperty\s+key=/).count}",
        (catalogText =~ /<requiredProperty\s+key=/).count == 35)
check("5.2 catalog carries 18 filesets: ${(catalogText =~ /<fileSet\s/).count}",
        (catalogText =~ /<fileSet\s/).count == 18)

// ---------------------------------------------------------------------------
// 6. Phase boundary: README must stay old in Phase 1 and be clean in Phase 2
// ---------------------------------------------------------------------------
def readmeText = readme.isFile() ? readme.getText("UTF-8") : ""
if (templatePhase == 2) {
    check("6.1 README has no old 04 names",
            p2RenamedCurrent.every { !readmeText.contains(it) } && !readmeText.contains("04-referencia-templates-de-contrato"))
} else if (templatePhase == 1) {
    check("6.1 README still carries the current 04 names in Phase 1",
            p2Current.every { readmeText.contains(it) })
} else {
    check("6.1 README still carries the legacy 04 names in Phase 0",
            p2LegacyOld.every { readmeText.contains(it) })
}

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
