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
check("4.4 the pre-processing test runner is wired (test)",
        pomText.contains("maven-flags-via-llm-test-runner"))
check("4.5 the post-generate test runner is wired (test)",
        pomText.contains("archetype-post-generate-test-runner"))
check("4.6 surefire is restricted to **/*Test.java", pomText.contains("**/*Test.java"))

// ---------------------------------------------------------------------------
// 5) Packaged procedure (utils/) - the Option A artifacts travel in the JAR
// ---------------------------------------------------------------------------
def utilsSource = new File("src/main/resources/utils")
def utilsClasses = new File("target/classes/utils")
def utilsIndex = new File(utilsSource,
        "inicializacao/indice-01-mapa-dos-arquivos-e-ordem-de-leitura.md")

def utilsCategories = [
        "inicializacao",
        "procedimento-do-agente",
        "regras-de-formacao-do-contrato",
        "templates-de-contrato"
]

def utilsFiles = [
        "inicializacao/prompt-00-leia-este-arquivo-primeiro.md",
        "inicializacao/indice-01-mapa-dos-arquivos-e-ordem-de-leitura.md",
        "procedimento-do-agente/prompt-01-papel-e-restricoes-do-agente.md",
        "procedimento-do-agente/prompt-02-como-localizar-e-ler-o-archetype.md",
        "procedimento-do-agente/prompt-03-regras-obrigatorias-de-geracao.md",
        "procedimento-do-agente/prompt-04-fluxo-de-execucao-e-verificacao.md",
        "procedimento-do-agente/prompt-05-entregaveis-e-relatorios.md",
        "regras-de-formacao-do-contrato/regras-01-como-derivar-a-entidade-do-contrato.md",
        "regras-de-formacao-do-contrato/regras-02-como-resolver-tipos-e-enums.md",
        "regras-de-formacao-do-contrato/regras-03-como-derivar-flags-de-tipos-aninhados.md",
        "regras-de-formacao-do-contrato/regras-04-como-aplicar-precedencia-e-validacao.md",
        "regras-de-formacao-do-contrato/regras-05-como-materializar-e-emitir-o-contrato.md",
        "regras-de-formacao-do-contrato/regras-06-como-relatar-proveniencia.md",
        "templates-de-contrato/template-01-schema-do-contrato.json",
        "templates-de-contrato/template-02-exemplo-contrato-plano.json",
        "templates-de-contrato/template-03-exemplo-contrato-com-objetos-aninhados.json",
        "templates-de-contrato/template-04-exemplo-contrato-com-enums.json",
        "templates-de-contrato/template-05-exemplo-contrato-com-tipos-do-projeto.json",
        "templates-de-contrato/template-06-exemplo-contrato-com-attributes-explicito.json"
]

def utilsIndexText = utilsIndex.isFile() ? utilsIndex.getText("UTF-8") : ""

check("5.1 the four utils categories exist in the source tree",
        utilsCategories.every { new File(utilsSource, it).isDirectory() })
check("5.2 the utils tree carries the 19 procedure files",
        utilsFiles.size() == 19 && utilsFiles.every { new File(utilsSource, it).isFile() })
check("5.3 the index lists every procedure file",
        utilsIndexText.length() > 0 && utilsFiles.every { utilsIndexText.contains(it) })
check("5.4 target/classes carries the 19 procedure files",
        utilsFiles.every { new File(utilsClasses, it).isFile() })
check("5.5 the archetype-resources tree carries no utils folder",
        !new File("src/main/resources/archetype-resources/utils").exists() &&
                !new File("target/classes/archetype-resources/utils").exists())

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
