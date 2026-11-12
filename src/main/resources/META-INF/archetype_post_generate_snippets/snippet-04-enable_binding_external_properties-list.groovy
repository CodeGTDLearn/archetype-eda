// -----------------------------------------------------------------------------


// SNIPPET-04-ENABLE_BINDING_EXTERNAL_PROPERTIES-LIST.GROOVY -------------------
// OWNER: the LIST sub-module sources and resources.
if (enable_binding_external_properties == 'true' && enable_list_binding_external_properties == 'false') {

    List<String> filesToDelete = binding_source_artifacts.list.root
    deleteFilesFromListFromFolder(srcMainJavaEntityConfigPropertiesListFolder, filesToDelete)
    deleteFolder(srcMainJavaEntityConfigPropertiesListFolder.toFile())
    log_text += "\nLIST_BINDING_PROPERTY:\n " +
            filesToDelete.toString() + "\n $srcMainJavaEntityConfigPropertiesListFolder\n"


    List<String> filesToDeleteResources = binding_resource_artifacts.list.properties
    deleteFilesFromListFromFolder(srcMainResourcesProperties, filesToDeleteResources)
    log_text += "\nLIST_BINDING_PROPERTY_RESOURCES:\n " +
            filesToDeleteResources.toString() + "\n $srcMainResourcesProperties\n"
}
