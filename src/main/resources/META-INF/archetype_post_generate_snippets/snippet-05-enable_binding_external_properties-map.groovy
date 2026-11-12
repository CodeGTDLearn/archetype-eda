// -----------------------------------------------------------------------------


// SNIPPET-05-ENABLE_BINDING_EXTERNAL_PROPERTIES-MAP.GROOVY --------------------
// OWNER: the MAP sub-module sources and resources.
if (enable_binding_external_properties == 'true' && enable_map_binding_external_properties == 'false') {

    List<String> filesToDelete = binding_source_artifacts.map.root
    deleteFilesFromListFromFolder(srcMainJavaEntityConfigPropertiesMapFolder, filesToDelete)
    deleteFolder(srcMainJavaEntityConfigPropertiesMapFolder.toFile())
    log_text += "\nMAP_BINDING_PROPERTY:\n " +
            filesToDelete.toString() + "\n $srcMainJavaEntityConfigPropertiesMapFolder\n"


    List<String> filesToDeleteResources = binding_resource_artifacts.map.properties
    deleteFilesFromListFromFolder(srcMainResourcesProperties, filesToDeleteResources)
    log_text += "\nMAP_BINDING_PROPERTY_RESOURCES:\n " +
            filesToDeleteResources.toString() + "\n $srcMainResourcesProperties\n"
}
