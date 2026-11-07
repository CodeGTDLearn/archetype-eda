/**
 * Unit test for the POST-PROCESSOR enum-generation snippet:
 * snippet-custom-enums.groovy.
 *
 * Responsibility: generate Java enum sources from custom_enum_definitions.
 * The variables layer is covered separately by VariablesCustomTypesTest;
 * this suite injects the three variables directly into the binding.
 *
 * Run from repo root:
 *   groovy src/test/archetype-post-generate/SnippetCustomEnumsTest.groovy
 * (also executed by the build - see src/test/groovy/run-archetype-post-generate-tests.groovy)
 */

import java.nio.file.Path
import java.nio.file.Paths

def snippetFile = new File("src/main/resources/META-INF/archetype_post_generate_snippets/snippet-custom-enums.groovy")

int failures = 0
def check = { String name, boolean cond ->
    if (cond) { println "PASS: ${name}" } else { println "FAIL: ${name}"; failures++ }
}

check("1.1 the snippet source exists", snippetFile.isFile())

// ---------------------------------------------------------------------------
// Harness: runs the snippet against a throw-away src/main/java tree.
// The optional `seed` map pre-creates files before the run.
// ---------------------------------------------------------------------------
def runSnippet = { String enumDefinitions, String entityPackage, String basePackage, Map seed ->
    File root = File.createTempDir("snippet-custom-enums-", "")
    root.deleteOnExit()
    Path srcMainJava = root.toPath().resolve("src").resolve("main").resolve("java")
    srcMainJava.toFile().mkdirs()

    if (seed != null) {
        seed.each { String relativePath, String content ->
            File file = new File(srcMainJava.toFile(), relativePath)
            file.getParentFile().mkdirs()
            file.setText(content, "UTF-8")
        }
    }

    def runtimeProps = new Properties()
    if (basePackage != null) runtimeProps.put("package", basePackage)

    def binding = new Binding()
    binding.setVariable("properties", runtimeProps)
    binding.setVariable("custom_enum_definitions_value", enumDefinitions)
    binding.setVariable("custom_attribute_imports_value", "none")
    binding.setVariable("entity_name_package_value", entityPackage)
    binding.setVariable("srcMainJava", srcMainJava)
    binding.setVariable("log_text", "")

    // The composed post-processor gets this import from the body/header; the
    // isolated snippet evaluation must provide it explicitly.
    new GroovyShell(binding).evaluate("import java.nio.file.Path\n" + snippetFile.getText("UTF-8"))

    return [root: root, srcMainJava: srcMainJava, log: binding.getVariable("log_text")]
}

def javaFiles = { Path srcMainJava ->
    def files = new ArrayList<File>()
    srcMainJava.toFile().eachFileRecurse { f ->
        if (f.isFile() && f.name.endsWith(".java")) files.add(f)
    }
    return files
}

// ---------------------------------------------------------------------------
// 2) The default/none/blank/null values generate nothing
// ---------------------------------------------------------------------------
def r2 = runSnippet("none", "contractentity", "it.pkg", null)
check("2.1 the default 'none' generates NO enum file", javaFiles(r2.srcMainJava).isEmpty())

def r2b = runSnippet("  ", "contractentity", "it.pkg", null)
check("2.2 a blank value generates NO enum file", javaFiles(r2b.srcMainJava).isEmpty())

def r2c = runSnippet("null", "contractentity", "it.pkg", null)
check("2.3 an ABSENT value generates NO enum file", javaFiles(r2c.srcMainJava).isEmpty())

// ---------------------------------------------------------------------------
// 3) A bare type name is generated in the package of the ENTITY
// ---------------------------------------------------------------------------
def r3 = runSnippet("Situacao:ABERTA", "contractentity", "it.pkg", null)
def f3 = new File(r3.srcMainJava.toFile(), "it/pkg/contractentity/Situacao.java")
check("3.1 a bare type name is generated in the package of the entity", f3.isFile())
if (f3.isFile()) {
    def src = f3.getText("UTF-8")
    check("3.2 ...the generated source declares the package",
            src.contains("package it.pkg.contractentity;"))
    check("3.3 ...the generated source declares the enum", src.contains("public enum Situacao"))
    check("3.4 ...the generated source carries the constant", src.contains("ABERTA"))
    check("3.5 ...the generated source carries the javadoc", src.contains("/**"))
}

// ---------------------------------------------------------------------------
// 3b) The relative marker 'nested.Tipo' generates in the nested subpackage
// ---------------------------------------------------------------------------
def r3n = runSnippet("nested.Uf:SP|RJ", "contractentity", "it.pkg", null)
def f3n = new File(r3n.srcMainJava.toFile(), "it/pkg/contractentity/nested/Uf.java")
check("3.6 the relative marker generates in the nested subpackage", f3n.isFile())
if (f3n.isFile()) {
    def src = f3n.getText("UTF-8")
    check("3.7 ...the generated source declares the nested package",
            src.contains("package it.pkg.contractentity.nested;"))
    check("3.8 ...and the enum carries its constants",
            src.contains("public enum Uf") && src.contains("SP") && src.contains("RJ"))
}

// ---------------------------------------------------------------------------
// 4) A qualified type name is generated in ITS OWN package
// ---------------------------------------------------------------------------
def r4 = runSnippet("br.com.projeto.StatusPedido:ATIVO|CANCELADO", "contractentity", "it.pkg", null)
def f4 = new File(r4.srcMainJava.toFile(), "br/com/projeto/StatusPedido.java")
check("4.1 a qualified type name is generated in its own package", f4.isFile())
check("4.2 ...and NOT in the package of the entity",
        !new File(r4.srcMainJava.toFile(), "it/pkg/contractentity/StatusPedido.java").isFile())
if (f4.isFile()) {
    def src = f4.getText("UTF-8")
    check("4.3 ...the generated source declares the declared package",
            src.contains("package br.com.projeto;"))
    check("4.4 ...the generated source declares every constant",
            src.contains("ATIVO") && src.contains("CANCELADO"))
    check("4.5 ...the constants are separated by a comma",
            src.contains("ATIVO,") && src.contains("CANCELADO"))
}

// ---------------------------------------------------------------------------
// 5) Several enums in ONE flag are all generated
// ---------------------------------------------------------------------------
def r5 = runSnippet("br.com.projeto.StatusPedido:ATIVO;Situacao:ABERTA", "contractentity", "it.pkg", null)
def files5 = javaFiles(r5.srcMainJava)
check("5.1 two definitions generate TWO enum files", files5.size() == 2)
check("5.2 ...one per declared type",
        files5.any { it.name == "StatusPedido.java" } && files5.any { it.name == "Situacao.java" })

// ---------------------------------------------------------------------------
// 6) An enum file that ALREADY EXISTS is never overwritten
// ---------------------------------------------------------------------------
def seed6 = ["it/pkg/contractentity/Situacao.java":
             "package it.pkg.contractentity;\n\npublic enum Situacao { EXISTENTE }\n"]
def r6 = runSnippet("Situacao:ABERTA", "contractentity", "it.pkg", seed6)
def target6 = new File(r6.srcMainJava.toFile(), "it/pkg/contractentity/Situacao.java")
check("6.1 an existing enum file is NOT overwritten",
        target6.isFile() && target6.getText("UTF-8").contains("EXISTENTE") &&
                !target6.getText("UTF-8").contains("ABERTA"))
check("6.2 ...the post-processor reports the skip",
        r6.log != null && r6.log.toString().contains("NOT overwritten"))

// ---------------------------------------------------------------------------
// 7) An invalid entry is skipped
// ---------------------------------------------------------------------------
def r7 = runSnippet("SituacaoSemConstante", "contractentity", "it.pkg", null)
check("7.1 an entry without ':' generates NO enum file", javaFiles(r7.srcMainJava).isEmpty())

def r7b = runSnippet("Situacao:", "contractentity", "it.pkg", null)
check("7.2 an entry without constants generates NO enum file", javaFiles(r7b.srcMainJava).isEmpty())

def r7c = runSnippet("Situacao:ABERTA;Invalido", "contractentity", "it.pkg", null)
def files7c = javaFiles(r7c.srcMainJava)
check("7.3 an invalid entry does not stop the valid ones", files7c.size() == 1 &&
        files7c.get(0).name == "Situacao.java")

// ---------------------------------------------------------------------------
// 8) An unknown project package generates NOTHING
// ---------------------------------------------------------------------------
def r8 = runSnippet("Situacao:ABERTA", "contractentity", null, null)
check("8.1 an unknown package generates NO enum file", javaFiles(r8.srcMainJava).isEmpty())

// ---------------------------------------------------------------------------
// 9) The log of the post-processor mentions the generated enum
// ---------------------------------------------------------------------------
check("9.1 the log mentions the generated enum",
        r4.log != null && r4.log.toString().contains("StatusPedido"))

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
