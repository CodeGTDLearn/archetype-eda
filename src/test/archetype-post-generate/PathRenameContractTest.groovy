/**
 * Contract test for the RENAMED PACKAGED PROCEDURE (feature 006).
 *
 * The procedure used to live in utils/. It now lives in
 * llm-maven-flags-inference/, with 19 files in the same 4 subfolders. The old
 * path keeps a single redirect at the legacy prompt-00 path so old specs keep
 * working.
 *
 * This suite proves:
 *
 *   A. the new root keeps the 4 subfolders and the 19 locked files, in the
 *      source tree and in target/classes;
 *   B. the operator-facing path is the new one;
 *   C. the legacy root keeps a single redirect and nothing else;
 *   D. no operational document cites the legacy path outside the redirect;
 *   E. the README publishes the topic and one subsection per subfolder;
 *   F. (opt-in) the installed JAR carries the new root and the redirect.
 */
def procedure = new File("src/main/resources/llm-maven-flags-inference")
def packaged = new File("target/classes/llm-maven-flags-inference")
def legacy = new File("src/main/resources/utils")
def legacyPackaged = new File("target/classes/utils")
def readme = new File("README.md")
def templatesDir = new File(".specify/templates")
def fixturesDir = new File("src/test/resources/projects")

int failures = 0
def check = { String name, boolean cond ->
    if (cond) { println "PASS: ${name}" } else { println "FAIL: ${name}"; failures++ }
}
def rel = { File root, File f ->
    f.absolutePath.substring(root.absolutePath.length() + 1).replace('\\', '/')
}
def filesOf = { File root ->
    def out = []
    if (root.isDirectory()) root.eachFileRecurse { File f -> if (f.isFile()) out << rel(root, f) }
    out.sort()
}

def categories = [
        "01-entrada-inicializacao",
        "02-operacao-procedimento-do-agente",
        "03-derivacao-regras-de-formacao-do-contrato",
        "04-referencia-templates-de-contrato"
]

def procedureFiles = [
        "01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md",
        "01-entrada-inicializacao/indice-01-mapa-dos-arquivos-e-ordem-de-leitura.md",
        "02-operacao-procedimento-do-agente/prompt-01-papel-e-restricoes-do-agente.md",
        "02-operacao-procedimento-do-agente/prompt-02-como-localizar-e-ler-o-archetype.md",
        "02-operacao-procedimento-do-agente/prompt-03-regras-obrigatorias-de-geracao.md",
        "02-operacao-procedimento-do-agente/prompt-04-fluxo-de-execucao-e-verificacao.md",
        "02-operacao-procedimento-do-agente/prompt-05-entregaveis-e-relatorios.md",
        "03-derivacao-regras-de-formacao-do-contrato/01-como-derivar-a-entidade-do-contrato.md",
        "03-derivacao-regras-de-formacao-do-contrato/02-como-resolver-tipos-e-enums.md",
        "03-derivacao-regras-de-formacao-do-contrato/03-como-derivar-flags-de-tipos-aninhados.md",
        "03-derivacao-regras-de-formacao-do-contrato/04-como-aplicar-precedencia-e-validacao.md",
        "03-derivacao-regras-de-formacao-do-contrato/05-como-materializar-e-emitir-o-contrato.md",
        "03-derivacao-regras-de-formacao-do-contrato/06-como-relatar-proveniencia.md",
        "04-referencia-templates-de-contrato/template-01-schema-do-contrato.json",
        "04-referencia-templates-de-contrato/template-02-exemplo-contrato-plano.json",
        "04-referencia-templates-de-contrato/template-03-exemplo-contrato-com-objetos-aninhados.json",
        "04-referencia-templates-de-contrato/template-04-exemplo-contrato-com-enums.json",
        "04-referencia-templates-de-contrato/template-05-exemplo-contrato-com-tipos-do-projeto.json",
        "04-referencia-templates-de-contrato/template-06-exemplo-contrato-com-attributes-explicito.json"
]

def canonical = "llm-maven-flags-inference/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md"
def legacyEntry = "01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md"

// ---------------------------------------------------------------------------
// A. the new root keeps the four subfolders and the 19 locked files
// ---------------------------------------------------------------------------
def sourceFiles = filesOf(procedure)
def sourceDirs = procedure.isDirectory()
        ? procedure.listFiles().findAll { it.isDirectory() }*.name.sort()
        : []

check("1.1 the procedure root exists", procedure.isDirectory())
check("1.2 the four subfolders keep their names: ${sourceDirs}", sourceDirs == categories.sort())
check("1.3 the procedure root carries exactly 19 files: ${sourceFiles.size()}", sourceFiles.size() == 19)
check("1.4 the 19 relative paths are the locked ones", sourceFiles == procedureFiles.sort())
check("1.5 the new entry exists in the source tree", new File(procedure, legacyEntry).isFile())

def packagedFiles = filesOf(packaged)
check("1.6 target/classes carries the same 19 files: ${packagedFiles.size()}",
        packagedFiles == procedureFiles.sort())
check("1.7 the new entry exists in target/classes", new File(packaged, legacyEntry).isFile())
check("1.8 the archetype-resources tree carries no procedure folder",
        !new File("src/main/resources/archetype-resources/llm-maven-flags-inference").exists() &&
                !new File("target/classes/archetype-resources/llm-maven-flags-inference").exists())

// ---------------------------------------------------------------------------
// B. the operator-facing path is the new one
// ---------------------------------------------------------------------------
def readmeText = readme.isFile() ? readme.getText("UTF-8") : ""
check("2.1 the README carries the new canonical entry path", readmeText.contains(canonical))
check("2.2 the README carries no legacy path", !readmeText.contains("utils/"))

// ---------------------------------------------------------------------------
// C. the legacy root keeps a single redirect
// ---------------------------------------------------------------------------
def legacyFiles = filesOf(legacy)
def legacyFile = new File(legacy, legacyEntry)
def legacyText = legacyFile.isFile() ? legacyFile.getText("UTF-8") : ""

check("3.1 the legacy root exists", legacy.isDirectory())
check("3.2 the legacy root carries exactly 1 file: ${legacyFiles.size()}", legacyFiles.size() == 1)
check("3.3 the legacy file is the old prompt-00 path: ${legacyFiles}", legacyFiles == [legacyEntry])
check("3.4 the redirect points at the new entry", legacyText.contains(canonical))
check("3.5 the redirect carries no copy of another procedure file",
        procedureFiles.findAll { it != legacyEntry }.every { !legacyText.contains(it) })
check("3.6 the legacy root keeps only the entry subfolder",
        legacy.isDirectory() && legacy.listFiles().findAll { it.isDirectory() }*.name == ["01-entrada-inicializacao"])

def legacyPackagedFiles = filesOf(legacyPackaged)
check("3.7 target/classes keeps the same single redirect: ${legacyPackagedFiles.size()}",
        legacyPackagedFiles == [legacyEntry])

// ---------------------------------------------------------------------------
// D. the README publishes the topic, the sequence and one subsection per folder
// ---------------------------------------------------------------------------
def topicStart = readmeText.indexOf("\n## llm-maven-flags-inference")
def topicEnd = topicStart < 0 ? -1 : readmeText.indexOf("\n## ", topicStart + 4)
def topic = (topicStart >= 0 && topicEnd > topicStart)
        ? readmeText.substring(topicStart, topicEnd)
        : ""

check("4.1 the README has the topic", !topic.isEmpty())
check("4.2 the topic names the agent prompt target", topic.toLowerCase().contains("alvo do prompt do agente"))
check("4.3 the topic names the preprocessing", topic.toLowerCase().contains("pré-processamento"))
check("4.4 the topic carries the five steps",
        topic.contains("Spec") && topic.contains("archetype") && topic.contains("JAR") &&
                topic.contains("entrada") && topic.contains("índice") &&
                topic.contains("materializa o contrato") && topic.contains("mvn archetype:generate"))
check("4.5 the topic closes the preprocessing at the mvn line",
        topic.contains("termina") && topic.contains("geração Maven") && topic.contains("depois"))
check("4.6 the topic has one subsection per subfolder",
        categories.every { topic.contains("### " + it) })

def subsectionBody = { String name ->
    def marker = "### " + name
    def start = topic.indexOf(marker)
    if (start < 0) return ""
    def after = topic.indexOf("\n### ", start + marker.length())
    def end = after < 0 ? topic.length() : after
    topic.substring(start + marker.length(), end)
}
check("4.7 each subsection is a short paragraph",
        categories.every { String name ->
            def body = subsectionBody(name)
            body.trim().length() > 0 && body.length() < 800
        })
check("4.8 the README index links the topic",
        readmeText.contains('href="#llm-maven-flags-inference"'))

// ---------------------------------------------------------------------------
// E. anti-drift across the operational corpus
// ---------------------------------------------------------------------------
def corpusFiles = [readme, new File("pom.xml")]
if (templatesDir.isDirectory()) {
    templatesDir.listFiles().findAll { it.isFile() && it.name.endsWith(".md") }.each { corpusFiles << it }
}
if (procedure.isDirectory()) {
    procedure.eachFileRecurse { File f -> if (f.isFile() && f.name.endsWith(".md")) corpusFiles << f }
}
if (fixturesDir.isDirectory()) {
    fixturesDir.eachFileRecurse { File f -> if (f.isFile() && f.name == "archetype.properties") corpusFiles << f }
}

def drift = []
corpusFiles.each { File f ->
    String body = f.getText("UTF-8")
    if (body.contains("utils/")) drift << "${f.name}: legacy path"
    if (body.contains("eda-llm-flag-inference")) drift << "${f.name}: legacy concept"
}
check("5.1 no operational document cites the legacy path or concept: ${drift}", drift.isEmpty())

def templateText = corpusFiles.findAll { it.parentFile == templatesDir }.collect { it.getText("UTF-8") }.join("\n")
check("5.2 the kit templates cite the new concept name",
        templateText.contains("llm-maven-flags-inference") &&
                !templateText.contains("eda-llm-flag-inference"))

// ---------------------------------------------------------------------------
// F. opt-in: the installed artifact carries the new root
// ---------------------------------------------------------------------------
if (System.getProperty("eda.verify.installed.jar") == "true") {
    def jar = new File(System.getProperty("user.home"),
            ".m2/repository/com/eda-archetype/eda-archetype/0.0.1-SNAPSHOT/eda-archetype-0.0.1-SNAPSHOT.jar")
    check("6.1 the installed artifact exists", jar.isFile())
    if (jar.isFile()) {
        def zip = new java.util.zip.ZipFile(jar)
        def entries = zip.entries().collect { it.name }
        def newFiles = entries.findAll { it ==~ /^llm-maven-flags-inference\/.+\.(md|json)$/ }
        def entry = zip.getEntry(canonical)
        def installedEntry = entry == null ? "" : zip.getInputStream(entry).getText("UTF-8")
        def legacyInstalledFiles = entries.findAll { it ==~ /^utils\/.+\.(md|json)$/ }
        def legacyEntryZip = zip.getEntry("utils/" + legacyEntry)
        def installedLegacy = legacyEntryZip == null ? "" : zip.getInputStream(legacyEntryZip).getText("UTF-8")
        zip.close()
        check("6.2 the installed artifact carries the 19 procedure files: ${newFiles.size()}", newFiles.size() == 19)
        check("6.3 the installed entry is the new procedure", installedEntry.contains("llm-maven-flags-inference/"))
        check("6.4 the installed artifact carries the single redirect: ${legacyInstalledFiles}",
                legacyInstalledFiles == ["utils/" + legacyEntry])
        check("6.5 the installed redirect points at the new entry", installedLegacy.contains(canonical))
    }
}

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
