// -----------------------------------------------------------------------------


// SNIPPET-08-ENABLE_BINDING_EXTERNAL_PROPERTIES_TESTS-DEFAULT.GROOVY ----------
// OWNER: the DEFAULT sub-module test cases.
if (enable_binding_external_properties_tests == 'true' && enable_default_binding_external_properties == 'false') {

    List<String> filesToDelete = binding_test_source_artifacts.default.root

    deleteFilesFromListFromFolder(srcTestJavaEntityConfigPropertiesFolder, filesToDelete)
    log_text += "\nDEFAULT_BINDING_PROPERTY_TEST:\n " +
            filesToDelete.toString() + "\n $srcTestJavaEntityConfigPropertiesFolder\n"

}
