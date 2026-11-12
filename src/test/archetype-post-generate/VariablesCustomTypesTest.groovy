/**
 * Unit test for the POST-PROCESSOR variables layer of the CONTRACT TYPES:
 * variables-17-custom_entity_attribute_imports.groovy,
 * variables-18-contract_enum_definitions.groovy,
 * variables-19-contract_nested_type_definitions.groovy and
 * variables-14-entity_package_name.groovy.
 *
 * Responsibility: verify that the channels consumed by the contract generators
 * are bound from the archetype properties, and that missing properties become
 * the "null" string (the convention used by the generators).
 *
 * The enum generation itself is covered by SnippetContractEnumsTest; the POJOs
 * by SnippetContractNestedTypesTest.
 *
 * Run from repo root:
 *   groovy src/test/archetype-post-generate/VariablesCustomTypesTest.groovy
 * (also executed by the build - see src/test/groovy/run-archetype-post-generate-tests.groovy)
 */

def dir = new File("src/main/resources/META-INF/archetype_post_generate_variables")

int failures = 0
def check = { String name, boolean cond ->
    if (cond) { println "PASS: ${name}" } else { println "FAIL: ${name}"; failures++ }
}

def FILES = [
        custom_entity_attribute_imports   : "variables-17-custom_entity_attribute_imports.groovy",
        contract_enum_definitions         : "variables-18-contract_enum_definitions.groovy",
        contract_nested_type_definitions  : "variables-19-contract_nested_type_definitions.groovy",
        entity_package_name               : "variables-14-entity_package_name.groovy"
]

check("1.1 every contract-types variables fragment exists",
        FILES.values().every { new File(dir, it).isFile() })

// The variables file uses `def`, so the values are local to the evaluated
// script. The capture snippet below runs in the same GroovyShell and exposes
// them through the binding.
def evaluateVariables = { File variablesFile, Properties properties ->
    def binding = new Binding()
    binding.setVariable("properties", properties)
    String capture = "captured = [value: ${variablesFile.name.replaceAll(/^variables-\d{2}-/, "").replace(".groovy", "")}]"
    new GroovyShell(binding).evaluate(variablesFile.getText("UTF-8") + "\n" + capture)
    return (Map) binding.getVariable("captured")
}

// ---------------------------------------------------------------------------
// 2) Properties are mapped to the expected variables
// ---------------------------------------------------------------------------
def props = new Properties()
props.put("custom_entity_attribute_imports", "a.B,c.D")
props.put("contract_enum_definitions", "Status:ATIVO|CANCELADO;Situacao:ABERTA")
props.put("contract_nested_type_definitions", "Endereco|logradouro:String,numero:Integer")
props.put("entity_package_name", "contractentity")

FILES.each { String key, String fileName ->
    def captured = evaluateVariables(new File(dir, fileName), props)
    check("2.${FILES.keySet().toList().indexOf(key) + 1} ${key} is mapped", captured.value == props.getProperty(key))
}

// ---------------------------------------------------------------------------
// 3) Missing properties follow the "null" string convention
// ---------------------------------------------------------------------------
FILES.each { String key, String fileName ->
    def captured = evaluateVariables(new File(dir, fileName), new Properties())
    check("3.${FILES.keySet().toList().indexOf(key) + 1} missing ${key} becomes 'null'", captured.value == "null")
}

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
