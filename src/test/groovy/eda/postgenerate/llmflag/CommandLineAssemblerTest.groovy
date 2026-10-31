/**
 * Unit test for the command-line assembler (FR-010, data-model.md Entity 5).
 *
 * "CommandLineAssemblerTest" (T016/T050 of
 * specs/003-llm-flag-inference/tasks.md) had no file on disk: the escape
 * rules of the command line - the operator-facing surface of the whole
 * mechanism - were never pinned down by a test.
 *
 * Run from repo root:
 *   groovy src/test/groovy/eda/postgenerate/llmflag/CommandLineAssemblerTest.groovy
 * (also executed by the build - see src/test/groovy/run-all-tests.groovy)
 */

def gcl = new GroovyClassLoader(this.class.classLoader)
def assemblerClass = gcl.parseClass(new File("src/main/groovy/eda/postgenerate/llmflag/CommandLineAssembler.groovy"))
def assembler = assemblerClass.newInstance()

int failures = 0
def check = { String name, boolean cond ->
    if (cond) { println "PASS: ${name}" } else { println "FAIL: ${name}"; failures++ }
}

def assemble = { Map<String, String> proposal ->
    return (String) assemblerClass.getDeclaredMethod("assemble", Map).invoke(assembler, proposal)
}

// ---------------------------------------------------------------------------
// 1) Format
// ---------------------------------------------------------------------------
def line = assemble([api_port: "8080", entity_name: "MyEntity"])
check("1.1 the command line starts with the archetype:generate prefix",
        line.startsWith("mvn archetype:generate --batch-mode"))
check("1.2 every flag is emitted as -D<key>=<value>", line.contains("-Dapi_port=8080") && line.contains("-Dentity_name=MyEntity"))
check("1.3 the command line is a SINGLE line", !line.contains("\n"))
check("1.4 exactly one -D per catalog key",
        (line =~ /-Dapi_port=/).size() == 1 && (line =~ /-Dentity_name=/).size() == 1)
check("1.5 the key order follows the proposal order (deterministic)",
        line.indexOf("-Dapi_port=") < line.indexOf("-Dentity_name="))
check("1.6 assembling the same proposal twice is byte-identical",
        assemble([api_port: "8080", entity_name: "MyEntity"]) == line)

// ---------------------------------------------------------------------------
// 2) Escape rule (data-model.md Entity 5)
// ---------------------------------------------------------------------------
check("2.1 a plain value is emitted verbatim (no quotes)",
        assemble([api_port: "8080"]).contains("-Dapi_port=8080"))
check("2.2 a value with whitespace is wrapped in double quotes",
        assemble([entity_name: "My Entity"]).contains('-Dentity_name="My Entity"'))
check("2.3 an embedded double quote is escaped",
        assemble([project_description: 'He said "hi"']).contains('-Dproject_description="He said \\"hi\\""'))
check("2.4 a value with a dollar sign is wrapped",
        assemble([project_description: 'cost $5']).contains('-Dproject_description="cost $5"'))
check("2.5 a value with a backslash is wrapped and escaped",
        assemble([entity_contract_path: 'C:\\tmp\\a.json']).contains('-Dentity_contract_path="C:\\\\tmp\\\\a.json"'))
check("2.6 shell metacharacters force quoting",
        assemble([v: 'a|b']).contains('-Dv="a|b"'))
check("2.7 an empty value is emitted as -D<key>= (no quotes)",
        assemble([v: ""]).contains("-Dv= ") || assemble([v: ""]).endsWith("-Dv="))
check("2.8 a null value does not break the assembly", assemble([v: null]).endsWith("-Dv="))

// ---------------------------------------------------------------------------
// 2b) The PROJECT type channels: imports and enum definitions
// ---------------------------------------------------------------------------
check("2.9 a list of imports is emitted verbatim",
        assemble([custom_attribute_imports: "br.com.projeto.EnderecoCliente,br.com.projeto.StatusPedido"])
                .contains("-Dcustom_attribute_imports=br.com.projeto.EnderecoCliente,br.com.projeto.StatusPedido"))
check("2.10 an enum definition is quoted (it carries a pipe)",
        assemble([custom_enum_definitions: "br.com.projeto.StatusPedido:ATIVO|CANCELADO"])
                .contains('-Dcustom_enum_definitions="br.com.projeto.StatusPedido:ATIVO|CANCELADO"'))
check("2.11 several enum definitions are quoted (they carry a semicolon)",
        assemble([custom_enum_definitions: "br.com.projeto.StatusPedido:ATIVO;Situacao:ABERTA"])
                .contains('-Dcustom_enum_definitions="br.com.projeto.StatusPedido:ATIVO;Situacao:ABERTA"'))
check("2.12 the 'none' default is emitted verbatim",
        assemble([custom_enum_definitions: "none"]).contains("-Dcustom_enum_definitions=none"))

// ---------------------------------------------------------------------------
// 3) Guardrails
// ---------------------------------------------------------------------------
def threw = false
try { assemble(null) } catch (Throwable t) { threw = true }
check("3.1 a null proposal is rejected with an exception", threw)

def big = new LinkedHashMap()
(1..50).each { big.put("flag_${it}", "v${it}") }
def bigLine = assemble(big)
check("3.2 a 50-flag proposal emits 50 -D pairs", (bigLine =~ /-Dflag_\d+=/).size() == 50)
check("3.3 ...all in one line", !bigLine.contains("\n"))

println ""
if (failures == 0) { println "ALL CHECKS PASSED" } else { println "${failures} CHECK(S) FAILED" }
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
