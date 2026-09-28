/**
 * Test for the derivation of the entity from the operator's JSON contract.
 *
 * Covers the bug assessed in
 * .specify/bugs/contrato-json-inferido-inoperante/assessment.md:
 * the FR-009 gate (contract.containsKey(flag)) discarded EVERY proposal made
 * from a domain contract (entityName / attributes[]), so the contract - the
 * operator's declared source of truth - had zero effect on the generated
 * entity ("0 CONTRACT / 0 INFERENCE / 32 DEFAULT").
 *
 * The test drives BOTH implementations:
 *   - the inlined logic of Orchestrator.groovy (the production mechanism);
 *   - the unit-testable twin FlagProposalMerger.groovy.
 * The parity checks at the end assert that the two never diverge.
 *
 * No JUnit infra in this repo; runnable directly:
 *   groovy src/test/groovy/eda/postgenerate/llmflag/ContractDerivationTest.groovy
 * Exits 0 when every check passes, 1 otherwise.
 */

def gcl = new GroovyClassLoader(this.class.classLoader)
def catalogEntryClass = gcl.parseClass(new File("src/main/groovy/eda/postgenerate/llmflag/CatalogEntry.groovy"))
def mergerClass = gcl.parseClass(new File("src/main/groovy/eda/postgenerate/llmflag/FlagProposalMerger.groovy"))
def orchestratorClass = gcl.parseClass(new File("src/main/groovy/eda/postgenerate/llmflag/Orchestrator.groovy"))

int failures = 0
def check = { String name, boolean cond ->
    if (cond) { println "PASS: ${name}" } else { println "FAIL: ${name}"; failures++ }
}

// ---------------------------------------------------------------------------
// Catalog (mirrors the real archetype catalog for the entity flags)
// ---------------------------------------------------------------------------
def mkEntry = { String k, String d, String r -> catalogEntryClass.newInstance(k, d, r, "") }

def catalogEntries = new LinkedHashMap()
catalogEntries.put("entity_name", mkEntry("entity_name", "MyEntity", '^[A-Z][a-zA-Z0-9_]+$'))
catalogEntries.put("entity_name_package", mkEntry("entity_name_package", "myentity", '^[a-z][a-z0-9_.]+$'))
catalogEntries.put("enable_custom_attributes", mkEntry("enable_custom_attributes", "false", '^(true|false)$'))
catalogEntries.put("custom_attributes_list", mkEntry("custom_attributes_list", "Long:id,MyId:id,My_Id:my_id",
        '^(false|[A-Z][a-zA-Z0-9_]*:[a-z][a-zA-Z0-9_]*(,[A-Z][a-zA-Z0-9_]*:[a-z][a-zA-Z0-9_]*)*)$'))
catalogEntries.put("api_port", mkEntry("api_port", "8080", '\\d{1,5}'))
catalogEntries.put("entity_contract_path", mkEntry("entity_contract_path", "none", '^(none|[^\r\n]*\\.json)$'))

def catalogPlain = new LinkedHashMap()
catalogEntries.each { k, v ->
    catalogPlain.put(k, [defaultValue: v.defaultValue, validationRegex: v.validationRegex])
}

def merger = mergerClass.newInstance()
def orchestrator = orchestratorClass.newInstance()

def mMerge = {
    Map contract, Map inference ->
        def m = orchestratorClass.getDeclaredMethod("merge", Map, Map, Map)
        m.accessible = true
        return (Map) m.invoke(orchestrator, inference, contract, catalogPlain)
}
def mergeMerger = { Map contract, Map inference ->
    return (Map) merger.mergeWithProvenance(inference, contract, catalogEntries)
}

// ---------------------------------------------------------------------------
// 1) attributes as a LIST of {name, value} - type estimated from the VALUE
// ---------------------------------------------------------------------------
def contractList = new LinkedHashMap()
contractList.put("entityName", "ContractEntity")
contractList.put("attributes", [
        [name: "id", value: 1024],
        [name: "bigNumber", value: 5000000000L],
        [name: "name", value: "Acme Corp"],
        [name: "active", value: true],
        [name: "score", value: 9.5d]
])

def mergedList = mergeMerger(contractList, [:])
def valuesList = mergedList.get("values")
def sourcesList = mergedList.get("sources")

check("1.1 entity name comes from the contract", valuesList.get("entity_name") == "ContractEntity")
check("1.2 entity name source is CONTRACT", sourcesList.get("entity_name") == "CONTRACT")
check("1.3 package follows the entity name",
        valuesList.get("entity_name_package") == "contractentity")
check("1.4 package source is CONTRACT_DERIVED",
        sourcesList.get("entity_name_package") == "CONTRACT_DERIVED")
check("1.5 enable_custom_attributes is forced to true",
        valuesList.get("enable_custom_attributes") == "true")
check("1.6 enable_custom_attributes source is CONTRACT_DERIVED",
        sourcesList.get("enable_custom_attributes") == "CONTRACT_DERIVED")
check("1.7 attribute types are estimated from the VALUE",
        valuesList.get("custom_attributes_list") ==
                "Integer:id,Long:bigNumber,String:name,Boolean:active,Double:score")
check("1.8 attribute list source is CONTRACT_DERIVED",
        sourcesList.get("custom_attributes_list") == "CONTRACT_DERIVED")
check("1.9 the derived list satisfies the catalog regex",
        valuesList.get("custom_attributes_list") ==~ catalogEntries.get("custom_attributes_list").validationRegex)

// ---------------------------------------------------------------------------
// 2) attributes as a MAP
// ---------------------------------------------------------------------------
def contractMap = new LinkedHashMap()
contractMap.put("entityName", "ContractEntity")
contractMap.put("attributes", [id: 1024, nome_cachorro: "Caramelo", tosado: true])

def mergedMap = mergeMerger(contractMap, [:])
check("2.1 attributes as a map are derived too",
        mergedMap.get("values").get("custom_attributes_list") ==
                "Integer:id,String:nome_cachorro,Boolean:tosado")

// ---------------------------------------------------------------------------
// 3) no explicit attribute collection -> the business fields ARE the attributes
// ---------------------------------------------------------------------------
def contractFlat = new LinkedHashMap()
contractFlat.put("id", 1024)
contractFlat.put("nome_cachorro", "Caramelo")
contractFlat.put("idade", 18)
contractFlat.put("tosado", true)
contractFlat.put("api_port", "9090")      // a catalog flag: NOT an attribute

def mergedFlat = mergeMerger(contractFlat, [:])
check("3.1 business fields become the entity attributes",
        mergedFlat.get("values").get("custom_attributes_list") ==
                "Integer:id,String:nome_cachorro,Integer:idade,Boolean:tosado")
check("3.2 a catalog flag in the contract stays a flag, not an attribute",
        mergedFlat.get("values").get("api_port") == "9090")
check("3.3 the flag keeps the CONTRACT source",
        mergedFlat.get("sources").get("api_port") == "CONTRACT")

// ---------------------------------------------------------------------------
// 4) a declared type wins, but only when it is a java.lang type
// ---------------------------------------------------------------------------
def contractTypes = new LinkedHashMap()
contractTypes.put("attributes", [
        [name: "id", type: "Long", value: 1],
        [name: "createdAt", type: "java.time.Instant", value: 1],
        [name: "label", type: "java.lang.String", value: "x"]
])

def mergedTypes = mergeMerger(contractTypes, [:])
check("4.1 a declared java.lang type wins over the value estimate",
        mergedTypes.get("values").get("custom_attributes_list").contains("Long:id"))
check("4.2 a declared FQN java.lang type is normalized to the simple name",
        mergedTypes.get("values").get("custom_attributes_list").contains("String:label"))
check("4.3 a type outside java.lang falls back to the value estimate",
        mergedTypes.get("values").get("custom_attributes_list").contains("Integer:createdAt"))
check("4.4 the rejected type is reported as a loud warning",
        (mergedTypes.get("warnings") as List).any { it.contains("java.time.Instant") })

// ---------------------------------------------------------------------------
// 5) FR-009 allowlist: the LLM may name the entity even without a literal key
// ---------------------------------------------------------------------------
def contractNoName = new LinkedHashMap()
contractNoName.put("attributes", [[name: "id", value: 1]])

def inferenceName = new LinkedHashMap()
inferenceName.put("entity_name", "PedidoEntity")
inferenceName.put("api_port", "7070")

def mergedInference = mergeMerger(contractNoName, inferenceName)
check("5.1 the LLM-proposed entity name is accepted (FR-009 allowlist)",
        mergedInference.get("values").get("entity_name") == "PedidoEntity")
check("5.2 its source is INFERENCE",
        mergedInference.get("sources").get("entity_name") == "INFERENCE")
check("5.3 an accepted proposal is NOT listed as discarded",
        !mergedInference.get("discarded").containsKey("entity_name"))
check("5.4 a flag outside the allowlist is still discarded (FR-009)",
        mergedInference.get("discarded").containsKey("api_port"))
check("5.5 the package follows the LLM-proposed entity name",
        mergedInference.get("values").get("entity_name_package") == "pedidoentity")

// ---------------------------------------------------------------------------
// 6) an explicit contract value always wins over the derivation
// ---------------------------------------------------------------------------
def contractExplicit = new LinkedHashMap()
contractExplicit.put("entity_name", "ContractEntity")
contractExplicit.put("entity_name_package", "custom_pkg")
contractExplicit.put("custom_attributes_list", "String:fixed")
contractExplicit.put("attributes", [[name: "id", value: 1]])

def mergedExplicit = mergeMerger(contractExplicit, [:])
check("6.1 an explicit entity_name_package wins over the derivation",
        mergedExplicit.get("values").get("entity_name_package") == "custom_pkg")
check("6.2 an explicit custom_attributes_list wins over the derivation",
        mergedExplicit.get("values").get("custom_attributes_list") == "String:fixed")
check("6.3 the explicit value keeps the CONTRACT source",
        mergedExplicit.get("sources").get("custom_attributes_list") == "CONTRACT")

// ---------------------------------------------------------------------------
// 7) parity: the Orchestrator and the FlagProposalMerger must not diverge
// ---------------------------------------------------------------------------
def parityContracts = [
        contractList, contractMap, contractFlat, contractTypes, contractNoName, contractExplicit
]
int parityFailures = 0
parityContracts.eachWithIndex { Map c, int i ->
    def inference = (i == 4) ? inferenceName : [:]
    def a = mMerge(c, inference)
    def b = mergeMerger(c, inference)
    if (a.get("values") != b.get("values")) {
        parityFailures++
        println "       divergence on scenario ${i}: ${a.get('values')} != ${b.get('values')}"
    }
    if (a.get("sources") != b.get("sources")) {
        parityFailures++
        println "       divergence on sources, scenario ${i}"
    }
    if (a.get("discarded") != b.get("discarded")) {
        parityFailures++
        println "       divergence on discarded, scenario ${i}"
    }
}
check("7.1 Orchestrator and FlagProposalMerger produce identical results", parityFailures == 0)

// ---------------------------------------------------------------------------
// 8) FR-021: the materialized contract path travels as CONTRACT_DERIVED
// ---------------------------------------------------------------------------
def derivedOverrides = new LinkedHashMap()
derivedOverrides.put("entity_contract_path", "C:/tmp/contracts/abc.json")
def mMergeWithOverrides = orchestratorClass.getDeclaredMethod("merge", Map, Map, Map, Map)
mMergeWithOverrides.accessible = true
def mergedWithPath = (Map) mMergeWithOverrides.invoke(orchestrator, [:], contractList, catalogPlain, derivedOverrides)
check("8.1 the contract path is emitted as CONTRACT_DERIVED",
        mergedWithPath.get("values").get("entity_contract_path") == "C:/tmp/contracts/abc.json")
check("8.2 the contract path source is CONTRACT_DERIVED",
        mergedWithPath.get("sources").get("entity_contract_path") == "CONTRACT_DERIVED")

// ---------------------------------------------------------------------------
// 9) the contract file is materialized with a deterministic name
// ---------------------------------------------------------------------------
def mMaterialize = orchestratorClass.getDeclaredMethod("materializeContract", String, String, String)
mMaterialize.accessible = true
def tmpDir = new File(System.getProperty("java.io.tmpdir"), "llmflag-contract-" + System.nanoTime())
def pathA = (String) mMaterialize.invoke(orchestrator, tmpDir.absolutePath, "{\"a\":1}", "{\"a\": 1}")
def pathB = (String) mMaterialize.invoke(orchestrator, tmpDir.absolutePath, "{\"a\":1}", "{\"a\": 1}")
check("9.1 the materialized path is deterministic for the same contract", pathA == pathB)
check("9.2 the materialized file exists", new File(pathA).isFile())
check("9.3 the materialized file holds the contract as delivered",
        new File(pathA).getText("UTF-8") == "{\"a\": 1}")
check("9.4 the materialized path has no backslash (shell portability)",
        !pathA.contains("\\"))

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
