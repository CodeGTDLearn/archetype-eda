// -----------------------------------------------------------------------------



// GENERATOR-03-ENTITY_CONTRACT_FILE_PATH.GROOVY -------------------------------
// FR-023 (contract persistence): the contract that was extracted from the
// prompt - the base of the module - is copied into the generated project:
//
//     src/test/resources/contracts/contract.json
//
// The generated module therefore carries the contract it was built from, so
// that a future schema test can assert that the entity class still matches
// that contract.
//
// Guarded by enable_folder_src_test_java: the contracts folder exists only
// when the test tree is kept (snippet-19 deletes src/test/resources entirely
// when the flag is false). The file name ends in .json on purpose:
// snippet-18 deletes .yml/.properties recursively under src/test/resources.
//
// The source path comes from the LLM inference mechanism (flag
// entity_contract_file_path). The value "none" means the module was generated
// without the mechanism - there is no contract to persist.

if (enable_folder_src_test_java == 'true' && entity_contract_file_path != null &&
        !entity_contract_file_path.trim().isEmpty() && entity_contract_file_path.trim() != 'none') {

    def entityContractSourceFile = resolveExistingFileWalkingUp(projectRootFolder, entity_contract_file_path, 12)

    if (entityContractSourceFile == null) {
        log_text += "\nENTITY_CONTRACT: source not found for '" + entity_contract_file_path +
                "' - the contract was NOT persisted\n"
        println "WARNING: entity_contract_file_path '" + entity_contract_file_path +
                "' does not exist - the contract was NOT persisted."
    } else {
        Path entityContractsFolder = srcTestResources.resolve("contracts")
        entityContractsFolder.toFile().mkdirs()
        Path entityContractTarget = entityContractsFolder.resolve("contract.json")
        entityContractTarget.toFile().bytes = entityContractSourceFile.bytes
        log_text += "\nENTITY_CONTRACT: " + entityContractSourceFile +
                "\n            -> " + entityContractTarget + "\n"
        println "Entity contract persisted: " + entityContractSourceFile + " -> " + entityContractTarget
    }
}
