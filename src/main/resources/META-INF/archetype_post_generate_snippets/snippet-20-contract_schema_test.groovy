// -----------------------------------------------------------------------------

// SNIPPET-20-CONTRACT_SCHEMA_TEST.GROOVY -------------------------------------
// OWNER: the contract schema test package of the GENERATED application.
//
// Removes <srcTestJava>/<entity_package>/contract when no contract was
// persisted into the generated project: entity_contract_file_path absent,
// "none", "null", empty, source file missing, or the test tree already dropped
// by snippet-19 (enable_folder_src_test_java=false).
//
// No user-facing flag: the presence of the persisted contract IS the trigger
// (generator-03 runs before this snippet because GENERATORS precede SNIPPETS).

def contractSchemaTestPersisted = srcTestResources.resolve("contracts").resolve("contract.json").toFile().isFile()

def contractSchemaTestFolder = srcTestJava
if (entity_package_name != null && !entity_package_name.trim().isEmpty() && entity_package_name.trim() != 'null') {
    contractSchemaTestFolder = contractSchemaTestFolder.resolve(entity_package_name.trim())
}
contractSchemaTestFolder = contractSchemaTestFolder.resolve("contract")

if (contractSchemaTestPersisted) {
    log_text += "\nCONTRACT_SCHEMA_TEST: kept " + contractSchemaTestFolder + "\n"
    println "Contract schema test kept: " + contractSchemaTestFolder
} else {
    deleteFolder(contractSchemaTestFolder.toFile())
    log_text += "\nCONTRACT_SCHEMA_TEST: removed " + contractSchemaTestFolder + " (no persisted contract)\n"
    println "Contract schema test removed (no persisted contract): " + contractSchemaTestFolder
}
