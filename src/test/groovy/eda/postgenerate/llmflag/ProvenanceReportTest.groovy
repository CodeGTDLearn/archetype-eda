/**
 * Unit test for FlagProposalMerger provenance (FR-020) — sibling module, public API.
 * No JUnit infra in this repo; runnable directly:
 *   groovy src/test/groovy/eda/postgenerate/llmflag/ProvenanceReportTest.groovy
 * Exits 0 when every check passes, 1 otherwise.
 */

def gcl = new GroovyClassLoader(this.class.classLoader)
def catalogEntryClass = gcl.parseClass(new File("src/main/groovy/eda/postgenerate/llmflag/CatalogEntry.groovy"))
// The canonical components: type inference and contract -> entity derivation.
// They are wired by hand (reflection) because a sibling source file is not
// resolvable at compile time in this build.
def resolverClass = gcl.parseClass(new File("src/main/groovy/eda/postgenerate/llmflag/ContractTypeResolver.groovy"))
def deriverClass = gcl.parseClass(new File("src/main/groovy/eda/postgenerate/llmflag/ContractEntityDeriver.groovy"))
def mergerClass = gcl.parseClass(new File("src/main/groovy/eda/postgenerate/llmflag/FlagProposalMerger.groovy"))

int failures = 0
def check = { String name, boolean cond ->
    if (cond) { println "PASS: ${name}" } else { println "FAIL: ${name}"; failures++ }
}

def mkEntry = { String k, String d, String r -> catalogEntryClass.newInstance(k, d, r, "") }

def catalog = new LinkedHashMap()
catalog.put("api_port", mkEntry("api_port", "8080", "\\d{1,5}"))
catalog.put("enable_jwt_authentication", mkEntry("enable_jwt_authentication", "false", "(true|false)"))
catalog.put("log_level", mkEntry("log_level", "INFO", null))
catalog.put("entity_name", mkEntry("entity_name", "MyEntity", '^[A-Z][a-zA-Z0-9_]+$'))

def merger = mergerClass.newInstance(deriverClass.newInstance(resolverClass.newInstance()))

// ----------------------------------------------------------------------
// Scenario A: contract wins over inference; unknown/absent LLM flags discarded
// ----------------------------------------------------------------------
def contract = new LinkedHashMap()
contract.put("api_port", "9090")
contract.put("enable_jwt_authentication", "true")

def inference = new LinkedHashMap()
inference.put("api_port", "7070")                       // loses to contract
inference.put("enable_jwt_authentication", "false")     // loses to contract
inference.put("log_level", "DEBUG")                     // absent from contract -> discarded (FR-009)
inference.put("unknown_flag", "x")                      // absent from contract -> discarded (FR-009)

def merged = merger.mergeWithProvenance(inference, contract, catalog)
def values = merged.get("values")
def sources = merged.get("sources")
def notes = merged.get("notes")
def discarded = merged.get("discarded")

check("A1 contract value wins (api_port=9090)", values.get("api_port") == "9090")
check("A2 source CONTRACT (api_port)", sources.get("api_port") == "CONTRACT")
check("A3 source CONTRACT (enable_jwt_authentication)", sources.get("enable_jwt_authentication") == "CONTRACT")
check("A4 log_level falls to catalog default", values.get("log_level") == "INFO")
check("A5 source DEFAULT (log_level)", sources.get("log_level") == "DEFAULT")
check("A6 LLM flag absent from contract is discarded (log_level=DEBUG)", discarded.get("log_level") == "DEBUG")
check("A7 unknown LLM flag is discarded", discarded.get("unknown_flag") == "x")
check("A8 discarded has exactly 2 entries", discarded.size() == 2)
check("A9 no FR-006 notes in scenario A", notes.isEmpty())
check("A10 values map has exactly the catalog keys", values.keySet() == catalog.keySet())

// merge() public API preserved: same values, same type semantics
def plain = merger.merge(inference, contract, catalog)
check("A11 merge() delegates and returns identical values", plain == values)

// ----------------------------------------------------------------------
// Scenario B: INFERENCE source (contract mentions the key with null value)
// ----------------------------------------------------------------------
def contractB = new LinkedHashMap()
contractB.put("api_port", null)   // key present -> LLM value allowed (FR-009)

def inferenceB = new LinkedHashMap()
inferenceB.put("api_port", "9090")

def mergedB = merger.mergeWithProvenance(inferenceB, contractB, catalog)
check("B1 LLM value used when contract key present with null value", mergedB.get("values").get("api_port") == "9090")
check("B2 source INFERENCE (api_port)", mergedB.get("sources").get("api_port") == "INFERENCE")
check("B3 api_port NOT discarded (key present in contract)", !mergedB.get("discarded").containsKey("api_port"))

// ----------------------------------------------------------------------
// Scenario C: FR-006 regex rejection -> default + note + source DEFAULT
// ----------------------------------------------------------------------
def contractC = new LinkedHashMap()
contractC.put("api_port", "999999")  // 6 digits, fails \d{1,5}

def mergedC = merger.mergeWithProvenance(null, contractC, catalog)
check("C1 invalid contract value replaced by default", mergedC.get("values").get("api_port") == "8080")
check("C2 source DEFAULT after FR-006 rejection", mergedC.get("sources").get("api_port") == "DEFAULT")
check("C3 FR-006 note recorded", (mergedC.get("notes").get("api_port") ?: "").contains("rejeitado pela regex"))
check("C4 null inference tolerated, nothing discarded", mergedC.get("discarded").isEmpty())

// ----------------------------------------------------------------------
// Scenario D: FR-017 guard — contract fields outside the catalog never leak
// ----------------------------------------------------------------------
def contractD = new LinkedHashMap()
contractD.put("api_port", "9090")
contractD.put("secret_field", "shhh")

def inferenceD = new LinkedHashMap()
inferenceD.put("api_port", "7070")

def mergedD = merger.mergeWithProvenance(inferenceD, contractD, catalog)
String blob = mergedD.toString()
check("D1 non-catalog contract field absent from merged output", !blob.contains("secret_field"))
check("D2 non-catalog contract value absent from merged output", !blob.contains("shhh"))

// ----------------------------------------------------------------------
// Scenario E: FR-009 (amended) - the allowlist.
// The entity name is derivable from the contract, so an LLM proposal for
// entity_name is accepted even when the contract carries no literal
// "entity_name" key. Every OTHER flag absent from the contract is still
// discarded (A6/A7 keep holding).
// ----------------------------------------------------------------------
def contractE = new LinkedHashMap()
contractE.put("attributes", [[name: "id", value: 1024]])

def inferenceE = new LinkedHashMap()
inferenceE.put("entity_name", "PedidoEntity")   // allowlisted -> accepted
inferenceE.put("log_level", "DEBUG")            // not allowlisted -> discarded

def mergedE = merger.mergeWithProvenance(inferenceE, contractE, catalog)
def valuesE = mergedE.get("values")
def sourcesE = mergedE.get("sources")
def discardedE = mergedE.get("discarded")

check("E1 allowlisted entity_name is accepted", valuesE.get("entity_name") == "PedidoEntity")
check("E2 allowlisted entity_name has source INFERENCE", sourcesE.get("entity_name") == "INFERENCE")
check("E3 accepted entity_name is NOT discarded", !discardedE.containsKey("entity_name"))
check("E4 a flag outside the allowlist is still discarded", discardedE.get("log_level") == "DEBUG")

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
