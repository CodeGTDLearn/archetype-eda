// -----------------------------------------------------------------------------


// SNIPPET-10-ENABLE_BINDING_EXTERNAL_PROPERTIES_TESTS-MAP.GROOVY --------------
// OWNER: the MAP sub-module test cases and test resources.
if (enable_binding_external_properties_tests == 'true' && enable_map_binding_external_properties == 'false') {

    deleteFolder(srcTestJavaEntityConfigPropertiesMapFolder.toFile())
    log_text += "\nFOLDER_BINDING_PROPERTIES_MAP_TEST:\n $srcTestJavaEntityConfigPropertiesMapFolder\n"

    List<String> filesToDeleteResources = binding_test_resource_artifacts.map.properties

    deleteFilesFromListFromFolder(srcTestResourcesPropertiesFolder, filesToDeleteResources)
    log_text += "\nLIST_BINDING_PROPERTY_RESOURCES:\n " +
            filesToDeleteResources.toString() + "\n $srcTestResourcesPropertiesFolder\n"
}