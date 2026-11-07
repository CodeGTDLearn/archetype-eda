/**
 * Unit test for the POST-PROCESSOR nested-POJO snippet:
 * snippet-custom-nested-types.groovy.
 *
 * Responsibility: generate plain Java POJOs from
 * custom_nested_type_definitions in the 'nested' subpackage of the entity.
 * The variables layer is covered by VariablesCustomTypesTest; this suite
 * injects the variables directly into the binding.
 *
 * Run from repo root:
 *   groovy src/test/archetype-post-generate/SnippetCustomNestedTypesTest.groovy
 * (also executed by the build - see src/test/groovy/run-archetype-post-generate-tests.groovy)
 */

import java.nio.file.Path
import java.nio.file.Paths

def snippetFile = new File("src/main/resources/META-INF/archetype_post_generate_snippets/snippet-custom-nested-types.groovy")

int failures = 0
def check = { String name, boolean cond ->
    if (cond) { println "PASS: ${name}" } else { println "FAIL: ${name}"; failures++ }
}

check("1.1 the snippet source exists", snippetFile.isFile())

// ---------------------------------------------------------------------------
// Harness: runs the snippet against a throw-away src/main/java tree.
// ---------------------------------------------------------------------------
def runSnippet = { String nestedDefinitions, String entityPackage, String basePackage, Map seed ->
    File root = File.createTempDir("snippet-custom-nested-types-", "")
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
    binding.setVariable("custom_nested_type_definitions_value", nestedDefinitions)
    binding.setVariable("entity_name_package_value", entityPackage)
    binding.setVariable("srcMainJava", srcMainJava)
    binding.setVariable("log_text", "")

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

def nestedDir = { Path srcMainJava, String entityPackage ->
    new File(srcMainJava.toFile(), ("it/pkg/" + entityPackage + "/nested").toString())
}

// ---------------------------------------------------------------------------
// 2) The default/none/blank/null values generate nothing
// ---------------------------------------------------------------------------
def r2 = runSnippet("none", "contractentity", "it.pkg", null)
check("2.1 the default 'none' generates NO POJO", javaFiles(r2.srcMainJava).isEmpty())

def r2b = runSnippet("   ", "contractentity", "it.pkg", null)
check("2.2 a blank value generates NO POJO", javaFiles(r2b.srcMainJava).isEmpty())

def r2c = runSnippet("null", "contractentity", "it.pkg", null)
check("2.3 an ABSENT value generates NO POJO", javaFiles(r2c.srcMainJava).isEmpty())

// ---------------------------------------------------------------------------
// 3) A nested definition generates a plain POJO in the nested subpackage
// ---------------------------------------------------------------------------
def r3 = runSnippet("Endereco|logradouro:String,numero:Integer", "contractentity", "it.pkg", null)
def f3 = new File(nestedDir(r3.srcMainJava, "contractentity"), "Endereco.java")
check("3.1 the POJO is generated in the nested subpackage", f3.isFile())
if (f3.isFile()) {
    def src = f3.getText("UTF-8")
    check("3.2 ...the source declares the nested package",
            src.contains("package it.pkg.contractentity.nested;"))
    check("3.3 ...the source declares a plain class", src.contains("public class Endereco"))
    check("3.4 ...the fields keep the contract keys",
            src.contains("private String logradouro;") && src.contains("private Integer numero;"))
    check("3.5 ...getters are explicit", src.contains("public String getLogradouro()"))
    check("3.6 ...setters are explicit", src.contains("public void setNumero(Integer numero)"))
    check("3.7 ...no Lombok", !src.contains("lombok"))
    check("3.8 ...no JPA", !src.contains("jakarta.persistence"))
}

// ---------------------------------------------------------------------------
// 4) Several POJOs in ONE flag are all generated
// ---------------------------------------------------------------------------
def r4 = runSnippet("Endereco|logradouro:String;Contato|email:String", "contractentity", "it.pkg", null)
def files4 = javaFiles(r4.srcMainJava)
check("4.1 two definitions generate TWO POJOs", files4.size() == 2)
check("4.2 ...one per declared type",
        files4.any { it.name == "Endereco.java" } && files4.any { it.name == "Contato.java" })

// ---------------------------------------------------------------------------
// 5) A nested POJO can reference another nested type
// ---------------------------------------------------------------------------
def r5 = runSnippet("Endereco|logradouro:String;Cliente|endereco:Endereco,nome:String",
        "contractentity", "it.pkg", null)
def f5 = new File(nestedDir(r5.srcMainJava, "contractentity"), "Cliente.java")
check("5.1 the parent POJO is generated", f5.isFile())
if (f5.isFile()) {
    def src = f5.getText("UTF-8")
    check("5.2 ...the field references the nested type by its simple name",
            src.contains("private Endereco endereco;"))
    check("5.3 ...the accessor keeps the declared type",
            src.contains("public Endereco getEndereco()"))
}

// An UNDECLARED field type is emitted verbatim: the Java compilation of the
// generated project must fail loudly instead of silently dropping the field.
def r5b = runSnippet("Cliente|endereco:TipoDesconhecido", "contractentity", "it.pkg", null)
def f5b = new File(nestedDir(r5b.srcMainJava, "contractentity"), "Cliente.java")
check("5.4 an undeclared field type is emitted verbatim (compilation fails loudly)",
        f5b.isFile() && f5b.getText("UTF-8").contains("private TipoDesconhecido endereco;"))

// A field with an empty name/type is reported instead of silently skipped.
def r5c = runSnippet("Cliente|endereco:", "contractentity", "it.pkg", null)
check("5.5 a field with an empty type is reported and skipped",
        javaFiles(r5c.srcMainJava).isEmpty() && r5c.log.toString().contains("empty name or type"))

// ---------------------------------------------------------------------------
// 6) An existing POJO is never overwritten
// ---------------------------------------------------------------------------
def seed6 = ["it/pkg/contractentity/nested/Endereco.java":
             "package it.pkg.contractentity.nested;\n\npublic class Endereco { }\n"]
def r6 = runSnippet("Endereco|logradouro:String", "contractentity", "it.pkg", seed6)
def target6 = new File(nestedDir(r6.srcMainJava, "contractentity"), "Endereco.java")
check("6.1 an existing POJO is NOT overwritten",
        target6.isFile() && !target6.getText("UTF-8").contains("logradouro"))
check("6.2 ...the post-processor reports the skip",
        r6.log != null && r6.log.toString().contains("NOT overwritten"))

// ---------------------------------------------------------------------------
// 7) Invalid entries are skipped without stopping the valid ones
// ---------------------------------------------------------------------------
def r7 = runSnippet("Invalido", "contractentity", "it.pkg", null)
check("7.1 an entry without '|' generates NO POJO", javaFiles(r7.srcMainJava).isEmpty())

def r7b = runSnippet("Endereco|", "contractentity", "it.pkg", null)
check("7.2 an entry without fields generates NO POJO", javaFiles(r7b.srcMainJava).isEmpty())

def r7c = runSnippet("Endereco|logradouro:String;Invalido", "contractentity", "it.pkg", null)
def files7c = javaFiles(r7c.srcMainJava)
check("7.3 an invalid entry does not stop the valid ones",
        files7c.size() == 1 && files7c.get(0).name == "Endereco.java")

// ---------------------------------------------------------------------------
// 8) An unknown project package generates nothing
// ---------------------------------------------------------------------------
def r8 = runSnippet("Endereco|logradouro:String", "contractentity", null, null)
check("8.1 an unknown package generates NO POJO", javaFiles(r8.srcMainJava).isEmpty())

// ---------------------------------------------------------------------------
// 9) The log mentions the generated POJO
// ---------------------------------------------------------------------------
check("9.1 the log mentions the generated POJO",
        r4.log != null && r4.log.toString().contains("Endereco"))

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
