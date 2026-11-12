// -----------------------------------------------------------------------------


// SNIPPET-12-ENABLE_BINDING_EXTERNAL_PROPERTIES_TESTS-VALIDATED.GROOVY -------
// OWNER: the validation test cases.
// VALIDATED TESTS -------------------------------------------------------------
if (enable_binding_external_properties_tests == 'true' && enable_binding_external_properties_validation == 'false') {

    List<String> filesToDelete = binding_test_source_artifacts.validated.root
    deleteFilesFromListFromFolder(srcTestJavaEntityConfigPropertiesFolder, filesToDelete)

    log_text += "\nVALIDATED_BINDING_PROPERTY_TEST:\n " +
            filesToDelete.toString() + "\n $srcTestJavaEntityConfigPropertiesFolder\n"
}