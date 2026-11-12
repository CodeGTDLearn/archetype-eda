// -----------------------------------------------------------------------------


// SNIPPET-03-ENABLE_BINDING_EXTERNAL_PROPERTIES-DEFAULT.GROOVY ----------------
// OWNER: the DEFAULT sub-module sources.
if (enable_binding_external_properties == 'true' && enable_default_binding_external_properties == 'false') {

    List<String> filesToDelete = binding_source_artifacts.default.root
    deleteFilesFromListFromFolder(srcMainJavaEntityConfigPropertiesFolder, filesToDelete)
    log_text += "\nDEFAULT_BINDING_PROPERTY:\n " +
            filesToDelete.toString() + "\n $srcMainJavaEntityConfigPropertiesFolder\n"
}
