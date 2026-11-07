/**
 * End-to-end test of the PAYLOAD enum convention: a pure payload (flat JSON)
 * whose array elements are ALL UPPERCASE becomes an enum.
 *
 * Drives the same components the production mechanism uses:
 *   FlagProposalMerger -> ContractEntityDeriver -> ContractAttributeAssembler
 *   -> ContractEnumDeriver / ContractNestedTypeDeriver
 *   -> DerivedFlagProjector.
 *
 * No JUnit infra in this repo; runnable directly:
 *   groovy src/test/pre-processamento-maven-flags-via-llm/domain/PayloadEnumIntegrationTest.groovy
 * Exits 0 when every check passes, 1 otherwise.
 */

def moduleRoot = new File("src/main/pre-processamento-maven-flags-via-llm")
def sourceIndex = [:]
moduleRoot.eachFileRecurse { File f ->
    if (f.isFile() && f.name.endsWith(".groovy")) sourceIndex[f.name] = f
}
def gcl = new GroovyClassLoader(this.class.classLoader)
gcl.setResourceLoader({ String className ->
    String simple = className.substring(className.lastIndexOf(".") + 1)
    File source = sourceIndex[simple + ".groovy"]
    return source == null ? null : source.toURI().toURL()
} as groovy.lang.GroovyResourceLoader)

def catalogEntryClass = gcl.loadClass("eda.postgenerate.pre_processamento_maven_flags_via_llm.catalog.CatalogEntry")
def resolverClass = gcl.loadClass("eda.postgenerate.pre_processamento_maven_flags_via_llm.domain.ContractTypeResolver")
def deriverClass = gcl.loadClass("eda.postgenerate.pre_processamento_maven_flags_via_llm.domain.ContractEntityDeriver")
def mergerClass = gcl.loadClass("eda.postgenerate.pre_processamento_maven_flags_via_llm.domain.FlagProposalMerger")

int failures = 0
def check = { String name, boolean cond ->
    if (cond) { println "PASS: ${name}" } else { println "FAIL: ${name}"; failures++ }
}

def mkEntry = { String k, String d, String r -> catalogEntryClass.newInstance(k, d, r, "") }
def catalog = new LinkedHashMap()
catalog.put("entity_name", mkEntry("entity_name", "MyEntity", '^[A-Z][a-zA-Z0-9_]+$'))
catalog.put("entity_name_package", mkEntry("entity_name_package", "myentity", '^[a-z][a-z0-9_.]+$'))
catalog.put("enable_custom_attributes", mkEntry("enable_custom_attributes", "false", '^(true|false)$'))
catalog.put("custom_attributes_list", mkEntry("custom_attributes_list", "none", '^(none|false|[A-Z][A-Za-z0-9_]*:[a-z][a-zA-Z0-9_]*(,[A-Z][A-Za-z0-9_]*:[a-z][a-zA-Z0-9_]*)*)$'))
catalog.put("custom_attribute_imports", mkEntry("custom_attribute_imports", "none",
        '^(none|([A-Za-z_][A-Za-z0-9_]*\\.)+[A-Za-z_][A-Za-z0-9_]*(,([A-Za-z_][A-Za-z0-9_]*\\.)+[A-Za-z_][A-Za-z0-9_]*)*)$'))
catalog.put("custom_enum_definitions", mkEntry("custom_enum_definitions", "none",
        '^(none|([A-Za-z_][A-Za-z0-9_]*\\.)*[A-Za-z_][A-Za-z0-9_]*:[A-Z][A-Z0-9_]*(\\|[A-Z][A-Z0-9_]*)*(;([A-Za-z_][A-Za-z0-9_]*\\.)*[A-Za-z_][A-Za-z0-9_]*:[A-Z][A-Z0-9_]*(\\|[A-Z][A-Z0-9_]*)*)*)$'))
catalog.put("custom_nested_type_definitions", mkEntry("custom_nested_type_definitions", "none",
        '^(none|[A-Z][A-Za-z0-9_]*\\|[a-z][a-zA-Z0-9_]*:[A-Za-z_][A-Za-z0-9_.]*(,[a-z][a-zA-Z0-9_]*:[A-Za-z_][A-Za-z0-9_.]*)*)(;[A-Z][A-Za-z0-9_]*\\|[a-z][a-zA-Z0-9_]*:[A-Za-z_][A-Za-z0-9_.]*(,[a-z][a-zA-Z0-9_]*:[A-Za-z_][A-Za-z0-9_.]*)*)*$'))

def merger = mergerClass.newInstance(deriverClass.newInstance(resolverClass.newInstance()))
def merge = { Map contract -> (Map) merger.mergeWithProvenance([:], contract, catalog) }

// ---------------------------------------------------------------------------
// 1) A flat payload: UPPERCASE arrays at the top and inside an object
// ---------------------------------------------------------------------------
def contract = new LinkedHashMap()
contract.put("entityName", "Pedido")
contract.put("id", 1)
contract.put("name", "Pedido 1")
contract.put("status", ["ATIVO", "CANCELADO"])
contract.put("endereco", [uf: ["SP", "RJ"]])
def merged = merge(contract)
def values = merged.get("values")
def sources = merged.get("sources")

check("1.1 the top-level array became an enum",
        values.get("custom_attributes_list").contains("Status:status"))
check("1.2 ...the nested array became a nested enum field",
        values.get("custom_nested_type_definitions") == "Endereco|uf:Uf")
check("1.3 the enum flag carries both enums",
        values.get("custom_enum_definitions") == "Status:ATIVO|CANCELADO;nested.Uf:SP|RJ")
check("1.4 no import is needed for the payload enums",
        values.get("custom_attribute_imports") == "none")
check("1.5 the enum flag is CONTRACT_DERIVED",
        sources.get("custom_enum_definitions") == "CONTRACT_DERIVED")
check("1.6 the nested flag is CONTRACT_DERIVED",
        sources.get("custom_nested_type_definitions") == "CONTRACT_DERIVED")
check("1.7 the flags satisfy the catalog regex",
        values.get("custom_attributes_list") ==~ catalog.get("custom_attributes_list").validationRegex &&
                values.get("custom_enum_definitions") ==~ catalog.get("custom_enum_definitions").validationRegex &&
                values.get("custom_nested_type_definitions") ==~ catalog.get("custom_nested_type_definitions").validationRegex)

// ---------------------------------------------------------------------------
// 2) Lowercase arrays stay String with a warning
// ---------------------------------------------------------------------------
def lower = new LinkedHashMap()
lower.put("entityName", "Pedido")
lower.put("status", ["ativo"])
def mergedLower = merge(lower)
check("2.1 a lowercase payload array becomes String",
        mergedLower.get("values").get("custom_attributes_list") == "String:status")
check("2.2 ...without an enum", mergedLower.get("values").get("custom_enum_definitions") == "none")
check("2.3 ...and the fallback is reported",
        ((List) mergedLower.get("warnings")).any { it.contains("array") })

// ---------------------------------------------------------------------------
// 3) A scalar uppercase string is NOT an enum (the rule is for arrays)
// ---------------------------------------------------------------------------
def scalar = new LinkedHashMap()
scalar.put("entityName", "Pedido")
scalar.put("status", "ATIVO")
def mergedScalar = merge(scalar)
check("3.1 a scalar uppercase string stays String",
        mergedScalar.get("values").get("custom_attributes_list") == "String:status")
check("3.2 ...without an enum", mergedScalar.get("values").get("custom_enum_definitions") == "none")

// ---------------------------------------------------------------------------
// 4) A non-UPPERCASE array becomes String with a warning
// ---------------------------------------------------------------------------
def langArray = new LinkedHashMap()
langArray.put("entityName", "Pedido")
langArray.put("codigos", [1, 2])
def mergedLangArray = merge(langArray)
check("4.1 a numeric payload array becomes String",
        mergedLangArray.get("values").get("custom_attributes_list") == "String:codigos")
check("4.2 ...without an enum", mergedLangArray.get("values").get("custom_enum_definitions") == "none")
check("4.3 ...and the fallback is reported",
        ((List) mergedLangArray.get("warnings")).any { it.contains("array") })

// ---------------------------------------------------------------------------
// 5) The entity name is reserved for the enum type
// ---------------------------------------------------------------------------
def collision = new LinkedHashMap()
collision.put("entityName", "Status")
collision.put("status", ["ATIVO"])
def mergedCollision = merge(collision)
check("5.1 the entity name is reserved for the enum",
        mergedCollision.get("values").get("custom_attributes_list") == "StatusType:status")
check("5.2 ...and the enum is renamed accordingly",
        mergedCollision.get("values").get("custom_enum_definitions") == "StatusType:ATIVO")

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
