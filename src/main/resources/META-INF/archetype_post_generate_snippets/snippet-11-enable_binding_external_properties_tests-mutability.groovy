// -----------------------------------------------------------------------------


// SNIPPET-11-ENABLE_BINDING_EXTERNAL_PROPERTIES_TESTS-MUTABILITY.GROOVY -------
// OWNER: the mutability axis of the binding TEST cases.
// MUTABILITY TESTS ------------------------------------------------------------
if (enable_binding_external_properties == 'true' && binding_external_properties_mutability == 'immutable') {

    List<String> filesToDelete = binding_test_source_artifacts.immutable.root
    deleteFilesFromListFromFolder(srcTestJavaEntityConfigPropertiesFolder, filesToDelete)
    log_text += "\nIMMUTABLE_BINDING_PROPERTY_TESTE:\n " +
            filesToDelete.toString() + "\n $srcTestJavaEntityConfigPropertiesFolder\n"


    List<String> listFilesToDelete = binding_test_source_artifacts.immutable.list
    deleteFilesFromListFromFolder(srcTestJavaEntityConfigPropertiesListFolder, listFilesToDelete)
    log_text += "\nIMMUTABLE_BINDING_PROPERTY_TESTE_LIST:\n " +
            listFilesToDelete.toString() + "\n $srcTestJavaEntityConfigPropertiesListFolder\n"


    List<String> mapFilesToDelete = binding_test_source_artifacts.immutable.map
    deleteFilesFromListFromFolder(srcTestJavaEntityConfigPropertiesMapFolder, mapFilesToDelete)
    log_text += "\nIMMUTABLE_BINDING_PROPERTY_TESTE_MAP:\n " +
            mapFilesToDelete.toString() + "\n $srcTestJavaEntityConfigPropertiesMapFolder\n"
}

if (enable_binding_external_properties == 'true' && binding_external_properties_mutability == 'mutable') {

    List<String> filesToDelete = binding_test_source_artifacts.mutable.root
    deleteFilesFromListFromFolder(srcTestJavaEntityConfigPropertiesFolder, filesToDelete)
    log_text += "\nMUTABLE_BINDING_PROPERTY_TEST:\n " +
            filesToDelete.toString() + "\n $srcTestJavaEntityConfigPropertiesFolder\n"


    List<String> listFilesToDelete = binding_test_source_artifacts.mutable.list
    deleteFilesFromListFromFolder(srcTestJavaEntityConfigPropertiesListFolder, listFilesToDelete)
    log_text += "\nMUTABLE_BINDING_PROPERTY_TEST:\n " +
            listFilesToDelete.toString() + "\n $srcTestJavaEntityConfigPropertiesListFolder\n"


    List<String> mapFilesToDelete = binding_test_source_artifacts.mutable.map
    deleteFilesFromListFromFolder(srcTestJavaEntityConfigPropertiesMapFolder, mapFilesToDelete)
    log_text += "\nMUTABLE_BINDING_PROPERTY_TEST:\n " +
            mapFilesToDelete.toString() + "\n $srcTestJavaEntityConfigPropertiesMapFolder\n"
}