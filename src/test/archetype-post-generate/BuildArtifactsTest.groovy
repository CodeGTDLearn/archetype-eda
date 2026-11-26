/**
 * Unit test for the MAVEN side of the archetype build.
 *
 * Asserts the artifacts produced by the build lifecycle (generate-resources /
 * process-resources) and the pom wiring that produces them. Runs in the
 * `test` phase, after process-resources.
 */
def metadata = new File("target/classes/META-INF/maven/archetype-metadata.xml")
def composed = new File("target/classes/META-INF/archetype-post-generate.groovy")
def pom = new File("pom.xml")

int failures = 0
def check = { String name, boolean cond ->
    if (cond) { println "PASS: ${name}" } else { println "FAIL: ${name}"; failures++ }
}

check("1.1 archetype-metadata.xml exists", metadata.isFile())
check("1.2 archetype-post-generate.groovy exists", composed.isFile())
check("1.3 pom.xml exists", pom.isFile())
// The post-processor source lives in archetype_post_generate_{body,variables,
// generators,snippets}/ and is COMPOSED into target/classes. A legacy monolith
// in src/main/resources/META-INF would be copied by the resources plugin to the
// same target path and could shadow the composed script (order within
// process-resources is not contractual), so its absence is part of the contract.
check("1.4 the legacy monolithic post-generate script is absent from the source tree",
        !new File("src/main/resources/META-INF/archetype-post-generate.groovy").exists())

if (!metadata.isFile() || !composed.isFile() || !pom.isFile()) {
    println ""
    println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
    if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
    return
}

def metadataText = metadata.getText("UTF-8")
def pomText = pom.getText("UTF-8")

// ---------------------------------------------------------------------------
// 2) Catalog artifact
// ---------------------------------------------------------------------------
check("2.1 the descriptor root is archetype-descriptor",
        metadataText.contains("<archetype-descriptor"))
check("2.2 the identity transform removed the DOCTYPE", !metadataText.contains("<!DOCTYPE"))
check("2.3 the catalog has 35 required properties",
        (metadataText =~ /<requiredProperty\s+key=/).count == 35)
check("2.4 the catalog carries 35 default values",
        (metadataText =~ /<defaultValue>/).count == 35)

// ---------------------------------------------------------------------------
// 3) Composed post-processor artifact
// ---------------------------------------------------------------------------
check("3.1 the composed script is non-trivial", composed.length() > 40000)
def composes = true
try { new GroovyShell().parse(composed.getText("UTF-8")) } catch (Throwable t) { composes = false }
check("3.2 the composed script is valid Groovy", composes)
check("3.3 the composed script is Groovy (no Velocity directive)",
        !(composed.getText("UTF-8") =~ /(?m)^\s*#(?:if|set|end)\b/))

// ---------------------------------------------------------------------------
// 4) pom wiring (the Maven pipeline that produces the artifacts above)
// ---------------------------------------------------------------------------
check("4.1 the archetype packaging extension is declared",
        pomText.contains("archetype-packaging"))
check("4.2 the xml-maven-plugin transform is wired", pomText.contains("xml-maven-plugin") &&
        pomText.contains("<goal>transform</goal>"))
check("4.3 the composer execution is wired (process-resources)",
        pomText.contains("archetype-post-generate-composer-execute"))
check("4.4 the post-generate test runner is wired (test)",
        pomText.contains("archetype-post-generate-test-runner"))
check("4.5 surefire is restricted to **/*Test.java", pomText.contains("**/*Test.java"))

// ---------------------------------------------------------------------------
// 5) Packaged procedure (llm-maven-flags-inference/) - the Option A artifacts
//    travel in the JAR
// ---------------------------------------------------------------------------
def procedureSource = new File("src/main/resources/llm-maven-flags-inference")
def procedureClasses = new File("target/classes/llm-maven-flags-inference")
def procedureIndex = new File(procedureSource,
        "01-inicializacao/01-mapa-dos-arquivos-e-ordem-de-leitura.md")

def procedureCategories = [
        "01-inicializacao",
        "02-procedimento-do-agente",
        "03-regras-de-formacao-do-contrato",
        "04-templates-de-contrato"
]

def procedureFiles = [
        "01-inicializacao/00-entry-point.md",
        "01-inicializacao/01-mapa-dos-arquivos-e-ordem-de-leitura.md",
        "02-procedimento-do-agente/01-papel-e-restricoes-do-agente.md",
        "02-procedimento-do-agente/02-localizar-e-ler-o-archetype.md",
        "02-procedimento-do-agente/03-regras-obrigatorias-de-geracao.md",
        "02-procedimento-do-agente/04-fluxo-de-execucao-e-verificacao.md",
        "02-procedimento-do-agente/05-entregaveis-e-relatorios.md",
        "03-regras-de-formacao-do-contrato/01-derivar-a-entidade-do-contrato.md",
        "03-regras-de-formacao-do-contrato/02-resolver-tipos-e-enums.md",
        "03-regras-de-formacao-do-contrato/03-derivar-flags-de-tipos-aninhados.md",
        "03-regras-de-formacao-do-contrato/04-aplicar-precedencia-e-validacao.md",
        "03-regras-de-formacao-do-contrato/05-materializar-e-emitir-o-contrato.md",
        "03-regras-de-formacao-do-contrato/06-relatar-proveniencia.md",
        "04-templates-de-contrato/01-schema-do-contrato.json",
        "04-templates-de-contrato/02-contrato-plano.json",
        "04-templates-de-contrato/03-contrato-com-objetos-aninhados.json",
        "04-templates-de-contrato/04-contrato-com-enums.json",
        "04-templates-de-contrato/05-contrato-com-tipos-do-projeto.json",
        "04-templates-de-contrato/06-contrato-com-attributes-explicito.json"
]

def procedureIndexText = procedureIndex.isFile() ? procedureIndex.getText("UTF-8") : ""

check("5.1 the four procedure categories exist in the source tree",
        procedureCategories.every { new File(procedureSource, it).isDirectory() })
check("5.2 the procedure tree carries the 19 procedure files",
        procedureFiles.size() == 19 && procedureFiles.every { new File(procedureSource, it).isFile() })
check("5.3 the index lists every procedure file",
        procedureIndexText.length() > 0 && procedureFiles.every { procedureIndexText.contains(it) })
check("5.4 target/classes carries the 19 procedure files",
        procedureFiles.every { new File(procedureClasses, it).isFile() })
check("5.5 the archetype-resources tree carries no procedure folder",
        !new File("src/main/resources/archetype-resources/utils").exists() &&
                !new File("target/classes/archetype-resources/utils").exists() &&
                !new File("src/main/resources/archetype-resources/llm-maven-flags-inference").exists() &&
                !new File("target/classes/archetype-resources/llm-maven-flags-inference").exists())

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
