// -----------------------------------------------------------------------------


// SNIPPET-BINDING-PROPERTIES.GROOVY -------------------------------------------
// BINDING PROPERTIES ----------------------------------------------------------
if (keep_binding_properties == 'false' ||
        (keep_default_binding_properties == 'false' &&
                keep_list_binding_properties == 'false' &&
                keep_map_binding_properties == 'false')
) {

    deleteFolder(srcMainJavaEntityConfigPropertiesListFolder.toFile())
    log_text += "\nFOLDER_BINDING_PROPERTIES_LIST:\n $srcMainJavaEntityConfigPropertiesListFolder\n"

    deleteFolder(srcMainJavaEntityConfigPropertiesMapFolder.toFile())
    log_text += "\nFOLDER_BINDING_PROPERTIES_MAP:\n $srcMainJavaEntityConfigPropertiesMapFolder\n"

    deleteFolder(srcMainResourcesProperties.toFile())
    log_text += "\nBINDING_PROPERTY_RESOURCES:\n $srcMainResourcesProperties\n"

    List<String> filesToDelete =
            [
                    "ApplicationProperties.java",
                    "ApplicationPropertiesRecord.java"
            ]
    deleteFilesFromListFromFolder(srcMainJavaEntityConfigPropertiesFolder, filesToDelete)
    log_text += "\nDEFAULT_BINDING_PROPERTY:\n " +
            filesToDelete.toString() + "\n $srcMainJavaEntityConfigPropertiesFolder\n"
}

if (keep_binding_properties == 'true' && keep_default_binding_properties == 'false') {

    List<String> filesToDelete =
            [
                    "ApplicationProperties.java",
                    "ApplicationPropertiesRecord.java"
            ]
    deleteFilesFromListFromFolder(srcMainJavaEntityConfigPropertiesFolder, filesToDelete)
    log_text += "\nDEFAULT_BINDING_PROPERTY:\n " +
            filesToDelete.toString() + "\n $srcMainJavaEntityConfigPropertiesFolder\n"
}

if (keep_binding_properties == 'true' && keep_list_binding_properties == 'false') {

    List<String> filesToDelete =
            [
                    "ListObjects.java",
                    "ListProperties.java",
                    "ListPropertiesImmutable.java",
                    "ListPropertiesRecord.java"
            ]
    deleteFilesFromListFromFolder(srcMainJavaEntityConfigPropertiesListFolder, filesToDelete)
    deleteFolder(srcMainJavaEntityConfigPropertiesListFolder.toFile())
    log_text += "\nLIST_BINDING_PROPERTY:\n " +
            filesToDelete.toString() + "\n $srcMainJavaEntityConfigPropertiesListFolder\n"


    List<String> filesToDeleteResources =
            [
                    "application_list.properties",
                    "application_list.yml"
            ]
    deleteFilesFromListFromFolder(srcMainResourcesProperties, filesToDeleteResources)
    log_text += "\nLIST_BINDING_PROPERTY_RESOURCES:\n " +
            filesToDeleteResources.toString() + "\n $srcMainResourcesProperties\n"
}

if (keep_binding_properties == 'true' && keep_map_binding_properties == 'false') {

    List<String> filesToDelete =
            [
                    "MapItem.java",
                    "MapProperties.java",
                    "MapPropertiesImmutable.java",
                    "MapPropertiesRecord.java"
            ]
    deleteFilesFromListFromFolder(srcMainJavaEntityConfigPropertiesMapFolder, filesToDelete)
    deleteFolder(srcMainJavaEntityConfigPropertiesMapFolder.toFile())
    log_text += "\nMAP_BINDING_PROPERTY:\n " +
            filesToDelete.toString() + "\n $srcMainJavaEntityConfigPropertiesMapFolder\n"


    List<String> filesToDeleteResources =
            [
                    "application_map.properties",
                    "application_map.yml"
            ]
    deleteFilesFromListFromFolder(srcMainResourcesProperties, filesToDeleteResources)
    log_text += "\nMAP_BINDING_PROPERTY_RESOURCES:\n " +
            filesToDeleteResources.toString() + "\n $srcMainResourcesProperties\n"
}

// MUTABILITY ------------------------------------------------------------------
if (keep_binding_properties == 'true' && mutable_binding_properties_choice == '1') {

    List<String> filesToDelete = ["ApplicationProperties.java"]
    deleteFilesFromListFromFolder(srcMainJavaEntityConfigPropertiesFolder, filesToDelete)
    log_text += "\nIMMUTABLE_BINDING_PROPERTY:\n " +
            filesToDelete.toString() + "\n $srcMainJavaEntityConfigPropertiesFolder\n"


    List<String> listFilesToDelete = ["ListProperties.java"]
    deleteFilesFromListFromFolder(srcMainJavaEntityConfigPropertiesListFolder, listFilesToDelete)
    log_text += "\nIMMUTABLE_BINDING_PROPERTY_LIST:\n " +
            listFilesToDelete.toString() + "\n $srcMainJavaEntityConfigPropertiesListFolder\n"


    List<String> mapFilesToDelete = ["MapProperties.java"]
    deleteFilesFromListFromFolder(srcMainJavaEntityConfigPropertiesMapFolder, mapFilesToDelete)
    log_text += "\nIMMUTABLE_BINDING_PROPERTY_MAP:\n " +
            mapFilesToDelete.toString() + "\n $srcMainJavaEntityConfigPropertiesMapFolder\n"
}

if (keep_binding_properties == 'true' && mutable_binding_properties_choice == '2') {

    List<String> filesToDelete = ["ApplicationPropertiesRecord.java"]
    deleteFilesFromListFromFolder(srcMainJavaEntityConfigPropertiesFolder, filesToDelete)
    log_text += "\nMUTABLE_BINDING_PROPERTY:\n " +
            filesToDelete.toString() + "\n $srcMainJavaEntityConfigPropertiesFolder\n"

    List<String> listFilesToDelete =
            [
                    "ListPropertiesImmutable.java",
                    "ListPropertiesRecord.java"
            ]
    deleteFilesFromListFromFolder(srcMainJavaEntityConfigPropertiesListFolder, listFilesToDelete)
    log_text += "\nMUTABLE_BINDING_PROPERTY_LIST:\n " +
            listFilesToDelete.toString() + "\n $srcMainJavaEntityConfigPropertiesListFolder\n"

    List<String> mapFilesToDelete =
            [
                    "MapPropertiesImmutable.java",
                    "MapPropertiesRecord.java"
            ]
    deleteFilesFromListFromFolder(srcMainJavaEntityConfigPropertiesMapFolder, mapFilesToDelete)
    log_text += "\nMUTABLE_BINDING_PROPERTY_MAP:\n " +
            mapFilesToDelete.toString() + "\n $srcMainJavaEntityConfigPropertiesMapFolder\n"
}