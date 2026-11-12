// -----------------------------------------------------------------------------


// SNIPPET-09-ENABLE_BINDING_EXTERNAL_PROPERTIES_TESTS-LIST.GROOVY -------------
// OWNER: the LIST sub-module test cases and test resources.
if (enable_binding_external_properties_tests == 'true' && enable_list_binding_external_properties == 'false') {

    deleteFolder(srcTestJavaEntityConfigPropertiesListFolder.toFile())
    log_text += "\nFOLDER_BINDING_PROPERTIES_LIST_TEST:\n $srcTestJavaEntityConfigPropertiesListFolder\n"

    List<String> filesToDeleteResources = binding_test_resource_artifacts.list.properties

    deleteFilesFromListFromFolder(srcTestResourcesPropertiesFolder, filesToDeleteResources)
    log_text += "\nLIST_BINDING_PROPERTY_RESOURCES_TEST:\n " +
            filesToDeleteResources.toString() + "\n $srcTestResourcesPropertiesFolder\n"
}