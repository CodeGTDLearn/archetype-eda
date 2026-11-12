// -----------------------------------------------------------------------------


// SNIPPET-07-ENABLE_BINDING_EXTERNAL_PROPERTIES_TESTS.GROOVY ------------------
// OWNER: the binding TESTS gate.
// TESTS -----------------------------------------------------------------------
if (enable_binding_external_properties_tests == 'false' ||
        (enable_default_binding_external_properties == 'false' &&
                enable_list_binding_external_properties == 'false' &&
                enable_map_binding_external_properties == 'false')
) {
    log_text += "\nFOLDER_BINDING_PROPERTIES_TEST:\n $srcTestJavaEntityConfigPropertiesFolder\n"
    deleteFolder(srcTestJavaEntityConfigPropertiesFolder.toFile())

    deleteFolder(srcTestResourcesPropertiesFolder.toFile())
    log_text += "\nBINDING_PROPERTY_RESOURCES_TEST:\n $srcTestResourcesPropertiesFolder\n"
}
