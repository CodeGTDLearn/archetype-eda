/**
 * Contract test for the DESTINATION OF GENERATION (feature 005).
 *
 * The destination is the 5th entry of the operator spec. It travels in the
 * NATIVE property `outputDirectory` of the archetype generation goal — it is
 * NOT a catalog flag — and the procedure must never presume it.
 *
 * This suite proves, by reading the PACKAGED procedure (target/classes/llm-maven-flags-inference,
 * which is what goes into the JAR — not the development tree):
 *
 *   A. the procedure tree keeps the 19 files (count is locked);
 *   B. the marker PASTA DE DESTINO is declared where the rule lives;
 *   C. the native property is emitted in the command line;
 *   D. the destination is kept OUT of the contract meta-keys and out of the
 *      provenance table;
 *   E. the procedure carries no absolute user path and no post-generation move;
 *   F. the catalog counts are unchanged and the property is not a flag;
 *   G. (opt-in) the INSTALLED artifact carries the rule. Run with
 *      -Deda.verify.installed.jar=true AFTER `mvn install`.
 */
def procedure = new File("src/main/resources/llm-maven-flags-inference")
def packaged = new File("target/classes/llm-maven-flags-inference")
def catalogFile = new File("target/classes/META-INF/maven/archetype-metadata.xml")

int failures = 0
def check = { String name, boolean cond ->
    if (cond) { println "PASS: ${name}" } else { println "FAIL: ${name}"; failures++ }
}

// Content assertions read the PACKAGED copy — the one that goes into the JAR —
// so the verification never passes because of the development tree alone.
def read = { String relative ->
    new File(packaged, relative).getText("UTF-8")
}

def p00 = read("01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md")
def p03 = read("02-operacao-procedimento-do-agente/prompt-03-regras-obrigatorias-de-geracao.md")
def p04 = read("02-operacao-procedimento-do-agente/prompt-04-fluxo-de-execucao-e-verificacao.md")
def p05 = read("02-operacao-procedimento-do-agente/prompt-05-entregaveis-e-relatorios.md")
def r01 = read("03-derivacao-regras-de-formacao-do-contrato/01-como-derivar-a-entidade-do-contrato.md")
def r06 = read("03-derivacao-regras-de-formacao-do-contrato/06-como-relatar-proveniencia.md")

// ---------------------------------------------------------------------------
// A. the procedure tree keeps the locked count
// ---------------------------------------------------------------------------
def sourceFiles = []
procedure.eachFileRecurse { if (it.isFile()) sourceFiles << it }
check("1.1 the procedure carries exactly 19 procedure files: ${sourceFiles.size()}", sourceFiles.size() == 19)

def packagedFiles = []
if (packaged.isDirectory()) packaged.eachFileRecurse { if (it.isFile()) packagedFiles << it }
check("1.2 target/classes/llm-maven-flags-inference carries the same 19 files: ${packagedFiles.size()}", packagedFiles.size() == 19)

// ---------------------------------------------------------------------------
// B. the marker is declared where the rule lives
// ---------------------------------------------------------------------------
check("2.1 prompt-00 declares the PASTA DE DESTINO marker", p00.contains("PASTA DE DESTINO"))
check("2.2 01 declares the PASTA DE DESTINO marker", r01.contains("PASTA DE DESTINO"))
check("2.3 prompt-00 lists the destination as the 5th spec entry",
        p00.contains("pasta de destino da geração"))
check("2.4 prompt-00 declares the destination gate", p00.contains("Gate D"))
check("2.5 prompt-00 forbids a presumed destination", p00.contains("Nunca presuma destino"))
check("2.6 prompt-00 forbids a destination inside the archetype tree",
        p00.contains("proibido de forma **absoluta**"))

// ---------------------------------------------------------------------------
// C. the native property is emitted in the command line
// ---------------------------------------------------------------------------
check("3.1 prompt-04 command line carries -DoutputDirectory", p04.contains("-DoutputDirectory"))
check("3.2 prompt-03 declares the outputDirectory property", p03.contains("outputDirectory"))
check("3.3 prompt-05 final report carries the destination path",
        p05.contains("destino de geração: caminho absoluto"))
check("3.4 prompt-04 declares the property as native, not a catalog flag",
        p04.contains("nativa do plugin"))
check("3.5 prompt-04 distinguishes the output destination from the execution directory",
        p04.contains("não onde o Maven roda"))
check("3.6 prompt-04 instructs displaying the destination before executing",
        p04.contains("antes de executar"))

// ---------------------------------------------------------------------------
// D. the destination stays outside the contract and outside the provenance
// ---------------------------------------------------------------------------
check("4.1 01 keeps the marker out of the contract keys",
        r01.contains("não é chave de contrato"))
def metaKeyLine = r01.readLines().find { it.contains("meta-chaves (`entityName`") }
def originalMetaKeys = ["entityName", "entity", "package", "description", "attributes", "fields"]
check("4.2 01 keeps exactly the six contract meta-keys",
        metaKeyLine != null && originalMetaKeys.every { metaKeyLine.contains(it) })
check("4.3 the destination marker is not one of the contract meta-keys",
        metaKeyLine != null && !metaKeyLine.contains("PASTA DE DESTINO"))
check("4.4 06 keeps the destination out of the provenance table",
        r06.contains("não é flag"))

// ---------------------------------------------------------------------------
// E. the procedure carries no user path and no post-generation move
// ---------------------------------------------------------------------------
def corpus = packagedFiles.collect { it.getText("UTF-8") }.join("\n")
check("5.0 the packaged procedure is readable: ${packagedFiles.size()} files", packagedFiles.size() == 19)
check("5.1 the procedure carries no absolute user path",
        !corpus.contains("C:\\Users\\") && !corpus.contains("/Users/") && !corpus.contains("/home/"))
check("5.2 the procedure carries no robocopy", !corpus.toLowerCase().contains("robocopy"))
def hardcodedCd = corpus.readLines().any { it.trim() ==~ /cd [A-Za-z]:.*/ }
check("5.3 the procedure carries no hardcoded drive change", !hardcodedCd)
check("5.4 the procedure carries no post-generation move instruction",
        !corpus.toLowerCase().contains("/move"))

// ---------------------------------------------------------------------------
// F. the catalog is unchanged and the property is not a flag
// ---------------------------------------------------------------------------
int propertyCount = -1
int fileSetCount = -1
if (catalogFile.isFile()) {
    def catalog = new XmlSlurper(false, false).parse(catalogFile)
    propertyCount = catalog.requiredProperties.requiredProperty.size()
    fileSetCount = catalog.fileSets.fileSet.size()
}
check("6.1 the built catalog has 35 required properties: ${propertyCount}", propertyCount == 35)
check("6.2 the built catalog has 18 file sets: ${fileSetCount}", fileSetCount == 18)
def catalogText = catalogFile.isFile() ? catalogFile.getText("UTF-8") : ""
check("6.3 outputDirectory is NOT a catalog flag",
        !catalogText.contains('key="outputDirectory"'))
check("6.4 the marker PASTA DE DESTINO is NOT a catalog flag",
        !catalogText.contains('key="PASTA DE DESTINO"'))

// ---------------------------------------------------------------------------
// G. opt-in: the installed artifact carries the rule
// ---------------------------------------------------------------------------
if (System.getProperty("eda.verify.installed.jar") == "true") {
    def jar = new File(System.getProperty("user.home"),
            ".m2/repository/com/eda-archetype/eda-archetype/0.0.1-SNAPSHOT/eda-archetype-0.0.1-SNAPSHOT.jar")
    check("7.1 the installed artifact exists", jar.isFile())
    if (jar.isFile()) {
        def zip = new java.util.zip.ZipFile(jar)
        def entry = zip.getEntry("llm-maven-flags-inference/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md")
        def installed = entry == null ? "" : zip.getInputStream(entry).getText("UTF-8")
        zip.close()
        check("7.2 the installed artifact carries the destination rule",
                installed.contains("PASTA DE DESTINO"))
    }
}

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
