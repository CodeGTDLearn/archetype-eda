/**
 * Unit suite for snippet-20-contract_schema_test.groovy (the removal guard of
 * the contract schema test package).
 *
 * The guard is the only thing that prevents an orphan test package in a
 * generated module without a persisted contract. It is triggered by the
 * persisted contract itself (the same state generator-03 leaves behind), never
 * by a new user-facing flag.
 */
def h = new GroovyClassLoader(this.class.classLoader).parseClass(new File("src/test/archetype-post-generate/PostProcessorHarness.groovy")).newInstance()
int failures = 0
def check = { String n, boolean c -> if (c) { println "PASS: ${n}" } else { println "FAIL: ${n}"; failures++ } }

def contractDirRelative = "src/test/java/myentity/contract"

def prepare = { File root, boolean withContract, boolean withTestTree ->
    if (withTestTree) {
        h.seed(root, "${contractDirRelative}/MyEntityContractTest.java", "class Placeholder {}\n")
    }
    if (withContract) {
        h.seed(root, "src/test/resources/contracts/contract.json", '{"entityName":"MyEntity"}\n')
    }
}

def newRoot = { String prefix ->
    File root = File.createTempDir(prefix, "")
    root.deleteOnExit()
    return root
}

// ---------------------------------------------------------------------------
// 1) persisted contract keeps the test package
// ---------------------------------------------------------------------------
File root1 = newRoot("contract-test-keep-")
prepare(root1, true, true)
def log1 = h.run(root1, [enable_folder_src_test_java: "true", entity_contract_file_path: "some/contract.json"], "snippet-20-contract_schema_test.groovy")
check("1.1 a persisted contract keeps the test package", new File(root1, contractDirRelative).isDirectory())
check("1.2 the log records the kept package", log1.contains("CONTRACT_SCHEMA_TEST: kept"))

// ---------------------------------------------------------------------------
// 2) no persisted contract removes the test package
// ---------------------------------------------------------------------------
File root2 = newRoot("contract-test-remove-")
prepare(root2, false, true)
def log2 = h.run(root2, [enable_folder_src_test_java: "true", entity_contract_file_path: "some/contract.json"], "snippet-20-contract_schema_test.groovy")
check("2.1 a missing persisted contract removes the test package", !new File(root2, contractDirRelative).exists())
check("2.2 the log records the removal", log2.contains("CONTRACT_SCHEMA_TEST: removed"))

// ---------------------------------------------------------------------------
// 3) path "none" removes the test package
// ---------------------------------------------------------------------------
File root3 = newRoot("contract-test-none-")
prepare(root3, false, true)
h.run(root3, [enable_folder_src_test_java: "true", entity_contract_file_path: "none"], "snippet-20-contract_schema_test.groovy")
check("3.1 path none removes the test package", !new File(root3, contractDirRelative).exists())

// ---------------------------------------------------------------------------
// 4) null / empty path removes the test package
// ---------------------------------------------------------------------------
File root4 = newRoot("contract-test-null-")
prepare(root4, false, true)
h.run(root4, [enable_folder_src_test_java: "true", entity_contract_file_path: "null"], "snippet-20-contract_schema_test.groovy")
check("4.1 path null removes the test package", !new File(root4, contractDirRelative).exists())

File root5 = newRoot("contract-test-empty-")
prepare(root5, false, true)
h.run(root5, [enable_folder_src_test_java: "true", entity_contract_file_path: ""], "snippet-20-contract_schema_test.groovy")
check("5.1 empty path removes the test package", !new File(root5, contractDirRelative).exists())

// ---------------------------------------------------------------------------
// 6) disabled test tree: the guard is a safe no-op
// ---------------------------------------------------------------------------
File root6 = newRoot("contract-test-notree-")
def log6 = h.run(root6, [enable_folder_src_test_java: "false", entity_contract_file_path: "some/contract.json"], "snippet-20-contract_schema_test.groovy")
check("6.1 disabled test tree does not break the guard", !new File(root6, contractDirRelative).exists())
check("6.2 the log records the removal", log6.contains("CONTRACT_SCHEMA_TEST: removed"))

// ---------------------------------------------------------------------------
// 7) missing source file (flag points to a file that does not exist)
// ---------------------------------------------------------------------------
File root7 = newRoot("contract-test-missing-source-")
prepare(root7, false, true)
h.run(root7, [enable_folder_src_test_java: "true", entity_contract_file_path: "missing.json"], "snippet-20-contract_schema_test.groovy")
check("7.1 a missing source does not keep an orphan package", !new File(root7, contractDirRelative).exists())

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
