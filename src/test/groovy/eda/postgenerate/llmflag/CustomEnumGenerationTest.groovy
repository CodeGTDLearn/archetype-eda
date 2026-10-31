/**
 * Unit test for the POST-PROCESSOR that materializes the ENUMS derived from
 * the contract (FR-021/FR-024).
 *
 * The post-processor is a Groovy script concatenated by the SnippetComposer
 * (archetype-post-generate.groovy), not a class - so it cannot be loaded as a
 * unit. This suite evaluates the very same source files the composer
 * concatenates (variables-custom-types.groovy + snippet-custom-enums.groovy),
 * in a throw-away source tree, and asserts the generated Java sources.
 *
 * Covered:
 *   - the default 'none' and the empty value generate NOTHING;
 *   - a bare type name is generated in the package of the entity;
 *   - a qualified type name is generated in its own package;
 *   - several enums in one flag are all generated;
 *   - an enum file that already exists is NEVER overwritten;
 *   - an invalid entry (no constant / no ':') is skipped;
 *   - an unknown project package generates NOTHING;
 *   - the generated source carries the package, the javadoc and the constants.
 *
 * Run from repo root:
 *   groovy src/test/groovy/eda/postgenerate/llmflag/CustomEnumGenerationTest.groovy
 * (also executed by the build - see src/test/groovy/run-all-tests.groovy)
 */

import java.nio.file.Path
import java.nio.file.Paths

def variablesFile = new File("src/main/resources/META-INF/archetype_post_generate_variables/variables-custom-types.groovy")
def snippetFile = new File("src/main/resources/META-INF/archetype_post_generate_snippets/snippet-custom-enums.groovy")

int failures = 0
def check = { String name, boolean cond ->
    if (cond) { println "PASS: ${name}" } else { println "FAIL: ${name}"; failures++ }
}

check("1.1 the variables source of the post-processor exists", variablesFile.isFile())
check("1.2 the snippet source of the post-processor exists", snippetFile.isFile())

// ---------------------------------------------------------------------------
// Harness: runs the two source files (as the composer concatenates them)
// against a throw-away src/main/java tree. The optional `seed` map pre-creates
// files (relative path from src/main/java -> content) BEFORE the run.
// ---------------------------------------------------------------------------
def runPostProcessor = { Map<String, String> props, Map<String, String> seed ->
    File root = File.createTempDir("custom-enums-", "")
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
    props.each { k, v -> if (v != null) runtimeProps.put(k, v) }

    def binding = new Binding()
    binding.setVariable("properties", runtimeProps)
    binding.setVariable("srcMainJava", srcMainJava)
    binding.setVariable("log_text", "")

    def script = "import java.nio.file.Path\n" + variablesFile.getText("UTF-8") + "\n" + snippetFile.getText("UTF-8")
    new GroovyShell(binding).evaluate(script)

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
// 2) The default value generates nothing
// ---------------------------------------------------------------------------
def r2 = runPostProcessor(["package": "it.pkg", "entity_name_package": "contractentity",
                           "custom_enum_definitions": "none"], null)
check("2.1 the default 'none' generates NO enum file", javaFiles(r2.srcMainJava).isEmpty())

def r2b = runPostProcessor(["package": "it.pkg", "entity_name_package": "contractentity",
                            "custom_enum_definitions": "  "], null)
check("2.2 a blank value generates NO enum file", javaFiles(r2b.srcMainJava).isEmpty())

def r2c = runPostProcessor(["package": "it.pkg", "entity_name_package": "contractentity"], null)
check("2.3 an ABSENT value generates NO enum file", javaFiles(r2c.srcMainJava).isEmpty())

// ---------------------------------------------------------------------------
// 3) A bare type name is generated in the package of the ENTITY
// ---------------------------------------------------------------------------
def r3 = runPostProcessor(["package": "it.pkg", "entity_name_package": "contractentity",
                           "custom_enum_definitions": "Situacao:ABERTA"], null)
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
// 4) A qualified type name is generated in ITS OWN package
// ---------------------------------------------------------------------------
def r4 = runPostProcessor(["package": "it.pkg", "entity_name_package": "contractentity",
                           "custom_enum_definitions": "br.com.projeto.StatusPedido:ATIVO|CANCELADO"], null)
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
def r5 = runPostProcessor(["package": "it.pkg", "entity_name_package": "contractentity",
                           "custom_enum_definitions": "br.com.projeto.StatusPedido:ATIVO;Situacao:ABERTA"], null)
def files5 = javaFiles(r5.srcMainJava)
check("5.1 two definitions generate TWO enum files", files5.size() == 2)
check("5.2 ...one per declared type",
        files5.any { it.name == "StatusPedido.java" } && files5.any { it.name == "Situacao.java" })

// ---------------------------------------------------------------------------
// 6) An enum file that ALREADY EXISTS is never overwritten
// ---------------------------------------------------------------------------
def seed6 = ["it/pkg/contractentity/Situacao.java":
             "package it.pkg.contractentity;\n\npublic enum Situacao { EXISTENTE }\n"]
def r6 = runPostProcessor(["package": "it.pkg", "entity_name_package": "contractentity",
                           "custom_enum_definitions": "Situacao:ABERTA"], seed6)
def target6 = new File(r6.srcMainJava.toFile(), "it/pkg/contractentity/Situacao.java")
check("6.1 an existing enum file is NOT overwritten",
        target6.isFile() && target6.getText("UTF-8").contains("EXISTENTE") &&
                !target6.getText("UTF-8").contains("ABERTA"))
check("6.2 ...the post-processor reports the skip",
        r6.log != null && r6.log.toString().contains("NOT overwritten"))

// ---------------------------------------------------------------------------
// 7) An invalid entry is skipped
// ---------------------------------------------------------------------------
def r7 = runPostProcessor(["package": "it.pkg", "entity_name_package": "contractentity",
                           "custom_enum_definitions": "SituacaoSemConstante"], null)
check("7.1 an entry without ':' generates NO enum file", javaFiles(r7.srcMainJava).isEmpty())

def r7b = runPostProcessor(["package": "it.pkg", "entity_name_package": "contractentity",
                            "custom_enum_definitions": "Situacao:"], null)
check("7.2 an entry without constants generates NO enum file", javaFiles(r7b.srcMainJava).isEmpty())

def r7c = runPostProcessor(["package": "it.pkg", "entity_name_package": "contractentity",
                            "custom_enum_definitions": "Situacao:ABERTA;Invalido"], null)
def files7c = javaFiles(r7c.srcMainJava)
check("7.3 an invalid entry does not stop the valid ones", files7c.size() == 1 &&
        files7c.get(0).name == "Situacao.java")

// ---------------------------------------------------------------------------
// 8) An unknown project package generates NOTHING
// ---------------------------------------------------------------------------
def r8 = runPostProcessor(["entity_name_package": "contractentity",
                           "custom_enum_definitions": "Situacao:ABERTA"], null)
check("8.1 an unknown package generates NO enum file", javaFiles(r8.srcMainJava).isEmpty())

// ---------------------------------------------------------------------------
// 9) The log of the post-processor mentions the generated enum
// ---------------------------------------------------------------------------
check("9.1 the log mentions the generated enum",
        r4.log != null && r4.log.toString().contains("StatusPedido"))

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
