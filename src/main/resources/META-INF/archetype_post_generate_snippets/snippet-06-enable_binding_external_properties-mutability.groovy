// -----------------------------------------------------------------------------


// SNIPPET-06-ENABLE_BINDING_EXTERNAL_PROPERTIES-MUTABILITY.GROOVY -------------
// OWNER: the mutability axis of the binding sources (record vs bean).
// MUTABILITY ------------------------------------------------------------------
if (enable_binding_external_properties == 'true' && binding_external_properties_mutability == 'immutable') {

    List<String> filesToDelete = binding_source_artifacts.immutable.root
    deleteFilesFromListFromFolder(srcMainJavaEntityConfigPropertiesFolder, filesToDelete)
    log_text += "\nIMMUTABLE_BINDING_PROPERTY:\n " +
            filesToDelete.toString() + "\n $srcMainJavaEntityConfigPropertiesFolder\n"


    List<String> listFilesToDelete = binding_source_artifacts.immutable.list
    deleteFilesFromListFromFolder(srcMainJavaEntityConfigPropertiesListFolder, listFilesToDelete)
    log_text += "\nIMMUTABLE_BINDING_PROPERTY_LIST:\n " +
            listFilesToDelete.toString() + "\n $srcMainJavaEntityConfigPropertiesListFolder\n"


    List<String> mapFilesToDelete = binding_source_artifacts.immutable.map
    deleteFilesFromListFromFolder(srcMainJavaEntityConfigPropertiesMapFolder, mapFilesToDelete)
    log_text += "\nIMMUTABLE_BINDING_PROPERTY_MAP:\n " +
            mapFilesToDelete.toString() + "\n $srcMainJavaEntityConfigPropertiesMapFolder\n"
}

if (enable_binding_external_properties == 'true' && binding_external_properties_mutability == 'mutable') {

    List<String> filesToDelete = binding_source_artifacts.mutable.root
    deleteFilesFromListFromFolder(srcMainJavaEntityConfigPropertiesFolder, filesToDelete)
    log_text += "\nMUTABLE_BINDING_PROPERTY:\n " +
            filesToDelete.toString() + "\n $srcMainJavaEntityConfigPropertiesFolder\n"

    List<String> listFilesToDelete = binding_source_artifacts.mutable.list
    deleteFilesFromListFromFolder(srcMainJavaEntityConfigPropertiesListFolder, listFilesToDelete)
    log_text += "\nMUTABLE_BINDING_PROPERTY_LIST:\n " +
            listFilesToDelete.toString() + "\n $srcMainJavaEntityConfigPropertiesListFolder\n"

    List<String> mapFilesToDelete = binding_source_artifacts.mutable.map
    deleteFilesFromListFromFolder(srcMainJavaEntityConfigPropertiesMapFolder, mapFilesToDelete)
    log_text += "\nMUTABLE_BINDING_PROPERTY_MAP:\n " +
            mapFilesToDelete.toString() + "\n $srcMainJavaEntityConfigPropertiesMapFolder\n"
}
