def h = new GroovyClassLoader(this.class.classLoader).parseClass(new File("src/test/archetype-post-generate/PostProcessorHarness.groovy")).newInstance()
int failures = 0
def check = { String n, boolean c -> if (c) { println "PASS: ${n}" } else { println "FAIL: ${n}"; failures++ } }

def contractText = '{"entityName":"ContractEntity"}'

def root = File.createTempDir("contract-ok-", ""); root.deleteOnExit()
def source = h.seed(root, "contracts/sample.json", contractText)
h.run(root, [enable_folder_src_test_java: "true", entity_contract_path: "contracts/sample.json"], "snippet-entity-contract.groovy")
def persisted = new File(root, "src/test/resources/contracts/contract.json")
check("1.1 the contract is persisted", persisted.isFile())
check("1.2 the persisted contract is byte-identical", persisted.isFile() && persisted.getText("UTF-8") == contractText)

def root2 = File.createTempDir("contract-notest-", ""); root2.deleteOnExit()
h.seed(root2, "contracts/sample.json", contractText)
h.run(root2, [enable_folder_src_test_java: "false", entity_contract_path: "contracts/sample.json"], "snippet-entity-contract.groovy")
check("2.1 src_test=false does not persist the contract", !new File(root2, "src/test/resources/contracts/contract.json").exists())

def root3 = File.createTempDir("contract-none-", ""); root3.deleteOnExit()
h.run(root3, [enable_folder_src_test_java: "true", entity_contract_path: "none"], "snippet-entity-contract.groovy")
check("3.1 path=none does not persist the contract", !new File(root3, "src/test/resources/contracts/contract.json").exists())

def root4 = File.createTempDir("contract-missing-", ""); root4.deleteOnExit()
def log4 = h.run(root4, [enable_folder_src_test_java: "true", entity_contract_path: "missing.json"], "snippet-entity-contract.groovy")
check("4.1 a missing source does not persist the contract", !new File(root4, "src/test/resources/contracts/contract.json").exists())
check("4.2 a missing source is reported in the log", log4.contains("source not found"))

def absolute = File.createTempFile("absolute-contract-", ".json"); absolute.deleteOnExit()
absolute.setText(contractText, "UTF-8")
def root5 = File.createTempDir("contract-abs-", ""); root5.deleteOnExit()
h.run(root5, [enable_folder_src_test_java: "true", entity_contract_path: absolute.absolutePath], "snippet-entity-contract.groovy")
check("5.1 an absolute source path is persisted", new File(root5, "src/test/resources/contracts/contract.json").isFile())

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
