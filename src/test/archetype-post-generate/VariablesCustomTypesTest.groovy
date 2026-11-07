/**
 * Unit test for the POST-PROCESSOR variables layer:
 * variables-custom-types.groovy.
 *
 * Responsibility: verify that the three variables consumed by the enum
 * snippet are bound from the archetype properties, and that missing
 * properties become the "null" string (the convention used by the snippets).
 *
 * The enum generation itself is covered by SnippetCustomEnumsTest.
 *
 * Run from repo root:
 *   groovy src/test/archetype-post-generate/VariablesCustomTypesTest.groovy
 * (also executed by the build - see src/test/groovy/run-archetype-post-generate-tests.groovy)
 */

def variablesFile = new File("src/main/resources/META-INF/archetype_post_generate_variables/variables-custom-types.groovy")

int failures = 0
def check = { String name, boolean cond ->
    if (cond) { println "PASS: ${name}" } else { println "FAIL: ${name}"; failures++ }
}

check("1.1 the variables source exists", variablesFile.isFile())

// The variables file uses `def`, so the values are local to the evaluated
// script. The capture snippet below runs in the same GroovyShell and exposes
// them through the binding.
def evaluateVariables = { Properties properties ->
    def binding = new Binding()
    binding.setVariable("properties", properties)
    String capture = """
captured = [
    custom_attribute_imports_value: custom_attribute_imports_value,
    custom_enum_definitions_value: custom_enum_definitions_value,
    custom_nested_type_definitions_value: custom_nested_type_definitions_value,
    entity_name_package_value: entity_name_package_value
]
"""
    new GroovyShell(binding).evaluate(variablesFile.getText("UTF-8") + "\n" + capture)
    return (Map) binding.getVariable("captured")
}

// ---------------------------------------------------------------------------
// 2) Properties are mapped to the expected variables
// ---------------------------------------------------------------------------
def props = new Properties()
props.put("custom_attribute_imports", "a.B,c.D")
props.put("custom_enum_definitions", "Status:ATIVO|CANCELADO;Situacao:ABERTA")
props.put("custom_nested_type_definitions", "Endereco|logradouro:String,numero:Integer")
props.put("entity_name_package", "contractentity")

def captured = evaluateVariables(props)
check("2.1 custom_attribute_imports is mapped",
        captured.get("custom_attribute_imports_value") == "a.B,c.D")
check("2.2 custom_enum_definitions is mapped",
        captured.get("custom_enum_definitions_value") == "Status:ATIVO|CANCELADO;Situacao:ABERTA")
check("2.3 custom_nested_type_definitions is mapped",
        captured.get("custom_nested_type_definitions_value") ==
                "Endereco|logradouro:String,numero:Integer")
check("2.4 entity_name_package is mapped",
        captured.get("entity_name_package_value") == "contractentity")

// ---------------------------------------------------------------------------
// 3) Missing properties follow the "null" string convention
// ---------------------------------------------------------------------------
def missing = evaluateVariables(new Properties())
check("3.1 missing properties become the string 'null'",
        missing.values().every { it == "null" })

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
