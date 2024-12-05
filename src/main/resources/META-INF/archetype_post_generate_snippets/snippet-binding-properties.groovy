// -----------------------------------------------------------------------------



// SNIPPET-BINDING-PROPERTIES.GROOVY

if (keep_binding_properties == 'false') {

    deleteFolder(propertiesProjectFolder.toFile())
    log_text += "\nFOLDER_BINDING_PROPERTIES:\n $propertiesProjectFolder\n"

    deleteFolder(propertiesResourcesFolder.toFile())
    log_text += "\nBINDING_PROPERTY_RESOURCES:\n $propertiesResourcesFolder\n"
}

if (keep_binding_properties == 'true' && keep_default_binding_properties == 'false') {

    List<String> filesToDelete =
            [
                    "ApplicationProperties.java",
                    "ApplicationPropertiesRecord.java"
            ]
    deleteFilesFromList(propertiesProjectFolder, filesToDelete)

    log_text += "\nDEFAULT_BINDING_PROPERTY:\n " +
            filesToDelete.toString() + "\n $propertiesProjectFolder\n"
}

if (keep_binding_properties == 'true' && keep_list_binding_properties == 'false') {

    List<String> filesToDelete =
            [
                    "ListObjects.java",
                    "ListProperties.java",
                    "ListPropertiesImmutable.java",
                    "ListPropertiesRecord.java"
            ]
    deleteFilesFromList(propertiesProjectFolder, filesToDelete)

    log_text += "\nLIST_BINDING_PROPERTY:\n " +
            filesToDelete.toString() + "\n $propertiesProjectFolder\n"


    List<String> filesToDeleteResources =
            [
                    "application_list.properties",
                    "application_list.yml"
            ]
    deleteFilesFromList(propertiesResourcesFolder, filesToDeleteResources)

    log_text += "\nLIST_BINDING_PROPERTY_RESOURCES:\n " +
            filesToDeleteResources.toString() + "\n $propertiesResourcesFolder\n"
}

if (keep_binding_properties == 'true' && keep_map_binding_properties == 'false') {

    List<String> filesToDelete =
            [
                    "MapItem.java",
                    "MapProperties.java",
                    "MapPropertiesImmutable.java",
                    "MapPropertiesRecord.java"
            ]
    deleteFilesFromList(propertiesProjectFolder, filesToDelete)

    log_text += "\nMAP_BINDING_PROPERTY:\n " +
            filesToDelete.toString() + "\n $propertiesProjectFolder\n"

    List<String> filesToDeleteResources =
            [
                    "application_map.properties",
                    "application_map.yml"
            ]
    deleteFilesFromList(propertiesResourcesFolder, filesToDeleteResources)

    log_text += "\nMAP_BINDING_PROPERTY_RESOURCES:\n " +
            filesToDeleteResources.toString() + "\n $propertiesResourcesFolder\n"
}

if (keep_binding_properties == 'true' && mutable_binding_properties_choice == '1') {

    List<String> filesToDelete =
            [
                    "ApplicationProperties.java",
                    "ListProperties.java",
                    "MapProperties.java"
            ]
    deleteFilesFromList(propertiesProjectFolder, filesToDelete)

    log_text += "\nIMMUTABLE_BINDING_PROPERTY:\n " +
            filesToDelete.toString() + "\n $propertiesProjectFolder\n"

}

if (keep_binding_properties == 'true' && mutable_binding_properties_choice == '2') {

    List<String> filesToDelete =
            [
                    "ApplicationPropertiesRecord.java",
                    "ListPropertiesImmutable.java",
                    "ListPropertiesRecord.java",
                    "MapPropertiesImmutable.java",
                    "MapPropertiesRecord.java"
            ]
    deleteFilesFromList(propertiesProjectFolder, filesToDelete)

    log_text += "\nMUTABLE_BINDING_PROPERTY:\n " +
            filesToDelete.toString() + "\n $propertiesProjectFolder\n"

}