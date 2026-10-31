/**
 * Consistency test between the CONTRACT and the archetype integration
 * profile (FR-021/FR-024).
 *
 * The mechanism (Orchestrator) turns the operator's contract into the flags
 * the archetype consumes. The archetype integration profile
 * `src/test/resources/projects/custom-attributes` declares those flags BY
 * HAND (archetype.properties) and carries the contract that produced them
 * (sample-contract-for-archetype-test.json).
 *
 * This suite closes the loop between the two halves: the flags derived from
 * the profile's contract MUST be exactly the flags the profile declares.
 * A drift on either side (a new rule in the derivation, a flag renamed, a
 * different attribute order) breaks the test instead of silently producing a
 * generated project that no longer matches the contract.
 *
 * No JUnit infra in this repo; runnable directly:
 *   groovy src/test/groovy/eda/postgenerate/llmflag/CustomAttributesProfileConsistencyTest.groovy
 * (also executed by the build - see src/test/groovy/run-all-tests.groovy)
 */

import groovy.json.JsonSlurper
import groovy.xml.XmlSlurper

def profileDir = new File("src/test/resources/projects/custom-attributes")
def contractFile = new File(profileDir, "sample-contract-for-archetype-test.json")
def propertiesFile = new File(profileDir, "archetype.properties")

def gcl = new GroovyClassLoader(this.class.classLoader)
def orchestratorClass = gcl.parseClass(new File("src/main/groovy/eda/postgenerate/llmflag/Orchestrator.groovy"))

int failures = 0
def check = { String name, boolean cond ->
    if (cond) { println "PASS: ${name}" } else { println "FAIL: ${name}"; failures++ }
}
def skip = { String name, String reason -> println "SKIP: ${name} (${reason})" }

// ---------------------------------------------------------------------------
// 1) The profile files
// ---------------------------------------------------------------------------
check("1.1 the contract of the profile exists", contractFile.isFile())
check("1.2 the properties of the profile exist", propertiesFile.isFile())
if (!contractFile.isFile() || !propertiesFile.isFile()) {
    println ""
    println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
    if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
    return
}

def declared = new LinkedHashMap<String, String>()
propertiesFile.eachLine { String line ->
    def trimmed = line.trim()
    if (trimmed.isEmpty() || trimmed.startsWith("#")) return
    int separator = trimmed.indexOf("=")
    if (separator <= 0) return
    declared.put(trimmed.substring(0, separator).trim(), trimmed.substring(separator + 1).trim())
}

def contract = (Map<String, Object>) new JsonSlurper().parse(contractFile)

// ---------------------------------------------------------------------------
// 2) The REAL catalog (built metadata when available, fragments otherwise)
// ---------------------------------------------------------------------------
def catalog = new LinkedHashMap<String, Map<String, String>>()
def builtMetadata = new File("target/classes/META-INF/maven/archetype-metadata.xml")
if (builtMetadata.exists()) {
    def parsed = new XmlSlurper().parse(builtMetadata)
    parsed.requiredProperties.requiredProperty.each { node ->
        catalog.put(node.@key.text().toString(), [
                defaultValue    : node.defaultValue.text().toString(),
                validationRegex : node.validationRegex.text().toString()
        ])
    }
} else {
    def propsDir = new File("src/main/resources/META-INF/maven/archetype_metadata_properties")
    def slurper = new XmlSlurper()
    propsDir.listFiles().sort { it.name }.each { File f ->
        if (!f.name.endsWith(".xml")) return
        def fragment = f.getText("UTF-8").replaceAll(/(?s)^.*?\?>/, "").trim()
        def parsed = slurper.parseText("<catalog>" + fragment + "</catalog>")
        parsed.requiredProperty.each { node ->
            catalog.put(node.@key.text().toString(), [
                    defaultValue    : node.defaultValue.text().toString(),
                    validationRegex : node.validationRegex.text().toString()
            ])
        }
    }
}
check("2.1 the catalog was loaded", catalog != null && catalog.size() >= 35)

// ---------------------------------------------------------------------------
// 3) The derivation of the profile's contract
// ---------------------------------------------------------------------------
def orchestrator = orchestratorClass.newInstance()
def merge = orchestratorClass.getDeclaredMethod("merge", Map, Map, Map)
merge.accessible = true
def merged = (Map) merge.invoke(orchestrator, [:], contract, catalog)
def values = (Map<String, String>) merged.get("values")
def sources = (Map<String, String>) merged.get("sources")

// ---------------------------------------------------------------------------
// 4) The derived flags MUST be the flags the profile declares
// ---------------------------------------------------------------------------
["entity_name", "custom_attributes_list", "custom_attribute_imports", "custom_enum_definitions"].each { String flag ->
    check("4.1 the profile declares '${flag}'", declared.containsKey(flag))
    if (!declared.containsKey(flag)) return
    check("4.2 the derived '${flag}' is the one declared by the profile",
            values.get(flag) == declared.get(flag))
}

// The entity name comes from the contract LITERALLY (the contract carries the
// "entityName" key); the attribute list and the PROJECT type channels are
// DERIVED from the contract.
check("4.3 the entity name comes from the contract",
        "CONTRACT".equals(sources.get("entity_name")))
["custom_attributes_list", "custom_attribute_imports", "custom_enum_definitions"].each { String flag ->
    check("4.4 the derived '${flag}' comes from the contract",
            "CONTRACT_DERIVED".equals(sources.get(flag)))
}

// ---------------------------------------------------------------------------
// 5) The shape of the derived values
// ---------------------------------------------------------------------------
check("5.1 the attribute list is not empty", (values.get("custom_attributes_list") ?: "").length() > 0)
check("5.2 the attribute list satisfies the catalog rule",
        values.get("custom_attributes_list") ==~ catalog.get("custom_attributes_list").validationRegex)
check("5.3 the imports satisfy the catalog rule",
        values.get("custom_attribute_imports") ==~ catalog.get("custom_attribute_imports").validationRegex)
check("5.4 the enum definitions satisfy the catalog rule",
        values.get("custom_enum_definitions") ==~ catalog.get("custom_enum_definitions").validationRegex)
check("5.5 the PROJECT type names its attribute after the TYPE",
        values.get("custom_attributes_list").contains("StatusPedido:statusPedido"))
check("5.6 ...and after the type, for every project type",
        values.get("custom_attributes_list").contains("Situacao:situacao") &&
                values.get("custom_attributes_list").contains("AdminCredentials:adminCredentials"))
check("5.7 the referenced types travel as imports",
        values.get("custom_attribute_imports").contains("it.pkg.domain.StatusPedido") &&
                values.get("custom_attribute_imports").contains("it.pkg.contractentity.config.properties.credentials.AdminCredentials"))
check("5.8 the enums travel as definitions",
        values.get("custom_enum_definitions").contains("it.pkg.domain.StatusPedido:ATIVO|CANCELADO") &&
                values.get("custom_enum_definitions").contains("it.pkg.contractentity.Situacao:ABERTA"))
check("5.9 the enum of the profile carries NO explicit constants for the referenced class",
        !values.get("custom_enum_definitions").contains("AdminCredentials"))
check("5.10 the JDK type outside java.lang still falls back to the value estimate",
        values.get("custom_attributes_list").contains("String:createdAt"))

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
