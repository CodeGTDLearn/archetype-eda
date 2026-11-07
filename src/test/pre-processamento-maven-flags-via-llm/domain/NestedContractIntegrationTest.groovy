/**
 * End-to-end test of the NESTED-object derivation through the same components
 * the production mechanism uses:
 *   FlagProposalMerger -> ContractEntityDeriver -> ContractAttributeAssembler
 *   -> ContractNestedTypeDeriver -> NestedTypeFlagProjector.
 *
 * Covers the contract flag custom_nested_type_definitions and its interaction
 * with custom_attributes_list (entity references the nested POJO by simple
 * name; the fields keep the contract keys).
 *
 * No JUnit infra in this repo; runnable directly:
 *   groovy src/test/pre-processamento-maven-flags-via-llm/domain/NestedContractIntegrationTest.groovy
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
// 1) A top-level nested object becomes a POJO and the entity keeps the key
// ---------------------------------------------------------------------------
def contract = new LinkedHashMap()
contract.put("entityName", "Cliente")
contract.put("attributes", [
        [name: "nome", value: "Acme"],
        [name: "endereco", value: [logradouro: "Rua A", numero: 10]]
])
def merged = merge(contract)
def values = merged.get("values")
def sources = merged.get("sources")

check("1.1 the entity references the nested POJO by its simple name",
        values.get("custom_attributes_list").contains("Endereco:endereco"))
check("1.2 ...the other fields are unaffected",
        values.get("custom_attributes_list").contains("String:nome"))
check("1.3 the nested flag carries the POJO and its fields",
        values.get("custom_nested_type_definitions") ==
                "Endereco|logradouro:String,numero:Integer")
check("1.4 ...with CONTRACT_DERIVED provenance",
        sources.get("custom_nested_type_definitions") == "CONTRACT_DERIVED")
check("1.5 the nested flag satisfies the catalog regex",
        values.get("custom_nested_type_definitions") ==~
                catalog.get("custom_nested_type_definitions").validationRegex)

// ---------------------------------------------------------------------------
// 2) Nested objects inside nested objects are all generated
// ---------------------------------------------------------------------------
def deep = new LinkedHashMap()
deep.put("entityName", "Cliente")
deep.put("attributes", [
        [name: "endereco", value: [logradouro: "Rua A", complemento: [numero: 1, bairro: "Centro"]]]
])
def mergedDeep = merge(deep)
check("2.1 the child nested object is generated too",
        mergedDeep.get("values").get("custom_nested_type_definitions") ==
                "Endereco|logradouro:String,complemento:Complemento;Complemento|numero:Integer,bairro:String")
check("2.2 ...and the parent field references the child type",
        mergedDeep.get("values").get("custom_nested_type_definitions").contains("complemento:Complemento"))

// ---------------------------------------------------------------------------
// 3) An array field becomes String and is reported
// ---------------------------------------------------------------------------
def arrayContract = new LinkedHashMap()
arrayContract.put("entityName", "Cliente")
arrayContract.put("attributes", [
        [name: "endereco", value: [tags: ["a", "b"]]]
])
def mergedArray = merge(arrayContract)
check("3.1 an array field becomes String in the nested POJO",
        mergedArray.get("values").get("custom_nested_type_definitions") == "Endereco|tags:String")
check("3.2 ...and a warning is raised",
        ((List) mergedArray.get("warnings")).any { it.contains("tags") })

// ---------------------------------------------------------------------------
// 4) An explicit list without a nested type suppresses the flag
// ---------------------------------------------------------------------------
def suppressed = new LinkedHashMap()
suppressed.put("entityName", "Cliente")
suppressed.put("custom_attributes_list", "String:nome")
suppressed.put("attributes", [[name: "endereco", value: [logradouro: "Rua A"]]])
def mergedSuppressed = merge(suppressed)
check("4.1 an explicit list without the nested type suppresses the flag",
        mergedSuppressed.get("values").get("custom_nested_type_definitions") == "none")

// ---------------------------------------------------------------------------
// 5) An explicit nested flag always wins
// ---------------------------------------------------------------------------
def explicit = new LinkedHashMap()
explicit.put("entityName", "Cliente")
explicit.put("custom_nested_type_definitions", "Explicito|a:String")
explicit.put("attributes", [[name: "endereco", value: [logradouro: "Rua A"]]])
def mergedExplicit = merge(explicit)
check("5.1 an explicit nested flag wins over the derivation",
        mergedExplicit.get("values").get("custom_nested_type_definitions") == "Explicito|a:String")
check("5.2 ...and keeps the CONTRACT source",
        mergedExplicit.get("sources").get("custom_nested_type_definitions") == "CONTRACT")

// ---------------------------------------------------------------------------
// 6) A contract without nested objects keeps the flag at 'none'
// ---------------------------------------------------------------------------
def flat = new LinkedHashMap()
flat.put("entityName", "Cliente")
flat.put("attributes", [[name: "nome", value: "Acme"]])
def mergedFlat = merge(flat)
check("6.1 a contract without nested objects keeps the flag at 'none'",
        mergedFlat.get("values").get("custom_nested_type_definitions") == "none")

// ---------------------------------------------------------------------------
// 7) A java.lang declared type wins over an object value (no POJO)
// ---------------------------------------------------------------------------
def declared = new LinkedHashMap()
declared.put("entityName", "Cliente")
declared.put("attributes", [[name: "payload", type: "String", value: [a: 1]]])
def mergedDeclared = merge(declared)
check("7.1 a declared java.lang type wins over the object value",
        mergedDeclared.get("values").get("custom_attributes_list") == "String:payload")
check("7.2 ...and no POJO is derived for it",
        mergedDeclared.get("values").get("custom_nested_type_definitions") == "none")

// ---------------------------------------------------------------------------
// 8) A reserved project type name never collides with a derived POJO
// ---------------------------------------------------------------------------
def reserved = new LinkedHashMap()
reserved.put("entityName", "Cliente")
reserved.put("attributes", [
        [name: "enderecoCliente", type: "br.com.projeto.EnderecoCliente"],
        [name: "enderecoAntigo", value: [logradouro: "Rua A"]]
])
def mergedReserved = merge(reserved)
check("8.1 the declared project type is imported",
        mergedReserved.get("values").get("custom_attribute_imports") == "br.com.projeto.EnderecoCliente")
check("8.2 ...and the nested object becomes its own POJO",
        mergedReserved.get("values").get("custom_nested_type_definitions").startsWith("EnderecoAntigo|"))

// ---------------------------------------------------------------------------
// 9) A project type + array: UPPERCASE elements are the enum constants
// ---------------------------------------------------------------------------
def enumArray = new LinkedHashMap()
enumArray.put("entityName", "Cliente")
enumArray.put("attributes", [
        [name: "status", type: "br.com.projeto.StatusPedido", value: ["ATIVO", "CANCELADO"]]
])
def mergedEnumArray = merge(enumArray)
check("9.1 a project type + UPPERCASE array keeps the project type",
        mergedEnumArray.get("values").get("custom_attributes_list").contains("StatusPedido:statusPedido"))
check("9.2 ...and the elements become the enum constants",
        mergedEnumArray.get("values").get("custom_enum_definitions") ==
                "br.com.projeto.StatusPedido:ATIVO|CANCELADO")
check("9.3 ...and the project type is imported",
        mergedEnumArray.get("values").get("custom_attribute_imports") == "br.com.projeto.StatusPedido")

def enumLower = new LinkedHashMap()
enumLower.put("entityName", "Cliente")
enumLower.put("attributes", [
        [name: "status", type: "br.com.projeto.StatusPedido", value: ["ativo", "cancelado"]]
])
def mergedEnumLower = merge(enumLower)
check("9.4 a project type + lowercase array becomes String",
        mergedEnumLower.get("values").get("custom_attributes_list") == "String:status")
check("9.5 ...without import nor enum",
        mergedEnumLower.get("values").get("custom_attribute_imports") == "none" &&
                mergedEnumLower.get("values").get("custom_enum_definitions") == "none")
check("9.6 ...and the fallback is reported",
        ((List) mergedEnumLower.get("warnings")).any { it.contains("array") })

def enumOverride = new LinkedHashMap()
enumOverride.put("entityName", "Cliente")
enumOverride.put("attributes", [
        [name: "status", type: "br.com.projeto.StatusPedido",
         value: ["ativo"], enumValues: ["ATIVO", "CANCELADO"]]
])
def mergedEnumOverride = merge(enumOverride)
check("9.7 the explicit enumValues win over the lowercase array",
        mergedEnumOverride.get("values").get("custom_enum_definitions") ==
                "br.com.projeto.StatusPedido:ATIVO|CANCELADO")

def langArray = new LinkedHashMap()
langArray.put("entityName", "Cliente")
langArray.put("attributes", [[name: "codigos", type: "Integer", value: [1, 2]]])
def mergedLangArray = merge(langArray)
check("9.8 java.lang + array keeps the declared type",
        mergedLangArray.get("values").get("custom_attributes_list") == "Integer:codigos")
check("9.9 ...and the fallback is reported",
        ((List) mergedLangArray.get("warnings")).any { it.contains("array") })

// ---------------------------------------------------------------------------
// 10) Naming: accents + keywords + entity-name reservation through the merger
// ---------------------------------------------------------------------------
def naming = new LinkedHashMap()
naming.put("entityName", "Pedido")
naming.put("attributes", [
        [name: "pedido", value: [uf: "SP"]],
        [name: "endereço", value: [class: "A", opção: "B"]]
])
def mergedNaming = merge(naming)
check("10.1 the entity name is reserved (PedidoType)",
        mergedNaming.get("values").get("custom_nested_type_definitions").startsWith("PedidoType|"))
check("10.2 the accented attribute is camelCase",
        mergedNaming.get("values").get("custom_attributes_list").contains("Endereco:endereco"))
check("10.3 ...and the accented field is ASCII-normalized",
        mergedNaming.get("values").get("custom_nested_type_definitions").contains("opcao:String"))
check("10.4 ...and the keyword field keeps the '_' suffix",
        mergedNaming.get("values").get("custom_nested_type_definitions").contains("class_:String"))

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
