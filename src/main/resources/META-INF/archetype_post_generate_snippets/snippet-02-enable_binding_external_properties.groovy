// -----------------------------------------------------------------------------



// SNIPPET-02-ENABLE_BINDING_EXTERNAL_PROPERTIES.GROOVY ------------------------
// OWNER: the binding SYSTEM gate. When the system is off, or when no
// sub-module is on, every binding artifact is removed at once.
// BINDING PROPERTIES ----------------------------------------------------------
if (enable_binding_external_properties == 'false' ||
        (enable_default_binding_external_properties == 'false' &&
                enable_list_binding_external_properties == 'false' &&
                enable_map_binding_external_properties == 'false')
) {

    deleteFolder(srcMainJavaEntityConfigPropertiesListFolder.toFile())
    log_text += "\nFOLDER_BINDING_PROPERTIES_LIST:\n $srcMainJavaEntityConfigPropertiesListFolder\n"

    deleteFolder(srcMainJavaEntityConfigPropertiesMapFolder.toFile())
    log_text += "\nFOLDER_BINDING_PROPERTIES_MAP:\n $srcMainJavaEntityConfigPropertiesMapFolder\n"

    deleteFolder(srcMainResourcesProperties.toFile())
    log_text += "\nBINDING_PROPERTY_RESOURCES:\n $srcMainResourcesProperties\n"

    List<String> filesToDelete = binding_source_artifacts.default.root
    deleteFilesFromListFromFolder(srcMainJavaEntityConfigPropertiesFolder, filesToDelete)
    log_text += "\nDEFAULT_BINDING_PROPERTY:\n " +
            filesToDelete.toString() + "\n $srcMainJavaEntityConfigPropertiesFolder\n"
}
