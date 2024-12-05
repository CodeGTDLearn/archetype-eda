// -----------------------------------------------------------------------------







// SNIPPET-SRC-MAIN-RESOURCES.GROOVY
if (keep_json_entity_contracts == 'false') {

    log_text += "\nFOLDER_SRC_MAIN_RESOURCES_CONTRACTS:\n $srcMainResourcesContracts\n"
    deleteFolder(srcMainResourcesContracts.toFile())
}

if (keep_json_entity_contracts == 'true') {

    log_text += "\nCopy contract toFOLDER_SRC_MAIN_RESOURCES_CONTRACTS:\n" +
            " from: $absolute_path_jsonfile_contract\n"+
            " to: $srcMainResourcesContracts\n"
    copyFile(jsonfileEntityContractSourcePath.toFile(),srcMainResourcesContracts.toFile())
}