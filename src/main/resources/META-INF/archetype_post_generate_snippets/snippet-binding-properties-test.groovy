// -----------------------------------------------------------------------------


// SNIPPET-BINDING-PROPERTIES-TEST.GROOVY --------------------------------------
// TESTS -----------------------------------------------------------------------
if (keep_binding_properties_tests == 'false' ||
        (keep_default_binding_properties == 'false' &&
                keep_list_binding_properties == 'false' &&
                keep_map_binding_properties == 'false')
) {
    log_text += "\nFOLDER_BINDING_PROPERTIES_TEST:\n $srcTestJavaEntityConfigPropertiesFolder\n"
    deleteFolder(srcTestJavaEntityConfigPropertiesFolder.toFile())

    deleteFolder(srcTestResourcesPropertiesFolder.toFile())
    log_text += "\nBINDING_PROPERTY_RESOURCES_TEST:\n $srcTestResourcesPropertiesFolder\n"
}

if (keep_binding_properties_tests == 'true' && keep_default_binding_properties == 'false') {

    List<String> filesToDelete =
            [
                    "ApplicationPropertiesRecordTest.java",
                    "ApplicationPropertiesRecordValidationTest.java",
                    "ApplicationPropertiesTest.java",
                    "ApplicationPropertiesValidationTest.java"
            ]

    deleteFilesFromListFromFolder(srcTestJavaEntityConfigPropertiesFolder, filesToDelete)
    log_text += "\nDEFAULT_BINDING_PROPERTY_TEST:\n " +
            filesToDelete.toString() + "\n $srcTestJavaEntityConfigPropertiesFolder\n"

}

if (keep_binding_properties_tests == 'true' && keep_list_binding_properties == 'false') {

    deleteFolder(srcTestJavaEntityConfigPropertiesListFolder.toFile())
    log_text += "\nFOLDER_BINDING_PROPERTIES_LIST_TEST:\n $srcTestJavaEntityConfigPropertiesListFolder\n"

    List<String> filesToDeleteResources =
            [
                    "application_list_test.properties",
                    "application_list_test.yml"
            ]

    deleteFilesFromListFromFolder(srcTestResourcesPropertiesFolder, filesToDeleteResources)
    log_text += "\nLIST_BINDING_PROPERTY_RESOURCES_TEST:\n " +
            filesToDeleteResources.toString() + "\n $srcTestResourcesPropertiesFolder\n"
}

if (keep_binding_properties_tests == 'true' && keep_map_binding_properties == 'false') {

    deleteFolder(srcTestJavaEntityConfigPropertiesMapFolder.toFile())
    log_text += "\nFOLDER_BINDING_PROPERTIES_MAP_TEST:\n $srcTestJavaEntityConfigPropertiesMapFolder\n"

    List<String> filesToDeleteResources =
            [
                    "application_map_test.properties",
                    "application_map_test.yml"
            ]

    deleteFilesFromListFromFolder(srcTestResourcesPropertiesFolder, filesToDeleteResources)
    log_text += "\nLIST_BINDING_PROPERTY_RESOURCES:\n " +
            filesToDeleteResources.toString() + "\n $srcTestResourcesPropertiesFolder\n"
}


// MUTABILITY TESTS ------------------------------------------------------------
if (keep_binding_properties == 'true' && mutable_binding_properties_choice == '1') {

    List<String> filesToDelete =
            [
                    "ApplicationPropertiesTest.java",
                    "ApplicationPropertiesValidationTest.java"
            ]
    deleteFilesFromListFromFolder(srcTestJavaEntityConfigPropertiesFolder, filesToDelete)
    log_text += "\nIMMUTABLE_BINDING_PROPERTY_TESTE:\n " +
            filesToDelete.toString() + "\n $srcTestJavaEntityConfigPropertiesFolder\n"


    List<String> listFilesToDelete = ["ListPropertiesTest.java"]
    deleteFilesFromListFromFolder(srcTestJavaEntityConfigPropertiesListFolder, listFilesToDelete)
    log_text += "\nIMMUTABLE_BINDING_PROPERTY_TESTE_LIST:\n " +
            listFilesToDelete.toString() + "\n $srcTestJavaEntityConfigPropertiesListFolder\n"


    List<String> mapFilesToDelete = ["MapPropertiesTest.java"]
    deleteFilesFromListFromFolder(srcTestJavaEntityConfigPropertiesMapFolder, mapFilesToDelete)
    log_text += "\nIMMUTABLE_BINDING_PROPERTY_TESTE_MAP:\n " +
            mapFilesToDelete.toString() + "\n $srcTestJavaEntityConfigPropertiesMapFolder\n"
}

if (keep_binding_properties == 'true' && mutable_binding_properties_choice == '2') {

    List<String> filesToDelete =
            [
                    "ApplicationPropertiesRecordValidationTest.java",
                    "ApplicationPropertiesRecordTest.java"
            ]
    deleteFilesFromListFromFolder(srcTestJavaEntityConfigPropertiesFolder, filesToDelete)
    log_text += "\nMUTABLE_BINDING_PROPERTY_TEST:\n " +
            filesToDelete.toString() + "\n $srcTestJavaEntityConfigPropertiesFolder\n"


    List<String> listFilesToDelete =
            [
                    "ListPropertiesImmutableTest.java",
                    "ListPropertiesRecordTest.java"
            ]
    deleteFilesFromListFromFolder(srcTestJavaEntityConfigPropertiesListFolder, listFilesToDelete)
    log_text += "\nMUTABLE_BINDING_PROPERTY_TEST:\n " +
            listFilesToDelete.toString() + "\n $srcTestJavaEntityConfigPropertiesListFolder\n"


    List<String> mapFilesToDelete =
            [
                    "MapPropertiesImmutableTest.java",
                    "MapPropertiesRecordTest.java"
            ]
    deleteFilesFromListFromFolder(srcTestJavaEntityConfigPropertiesMapFolder, mapFilesToDelete)
    log_text += "\nMUTABLE_BINDING_PROPERTY_TEST:\n " +
            mapFilesToDelete.toString() + "\n $srcTestJavaEntityConfigPropertiesMapFolder\n"
}


// VALIDATED TESTS -------------------------------------------------------------
if (keep_binding_properties_tests == 'true' && keep_validated_binding_properties == 'false') {

    List<String> filesToDelete =
            [
                    "ApplicationPropertiesRecordValidationTest.java",
                    "ApplicationPropertiesValidationTest.java"
            ]
    deleteFilesFromListFromFolder(srcTestJavaEntityConfigPropertiesFolder, filesToDelete)

    log_text += "\nVALIDATED_BINDING_PROPERTY_TEST:\n " +
            filesToDelete.toString() + "\n $srcTestJavaEntityConfigPropertiesFolder\n"
}