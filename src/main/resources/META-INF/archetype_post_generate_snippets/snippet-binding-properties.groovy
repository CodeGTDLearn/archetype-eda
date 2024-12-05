import java.nio.file.Path
import java.nio.file.Paths












// SNIPPET-BINDING-PROPERTIES.GROOVY -------------------------------------------
if (keep_binding_properties == 'false') {

    Path srcMainJava_bindingPropertiesFolder_path =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'java',
                    project_package,
                    entity_folder,
                    config_folder,
                    "properties")
    log_text += "\nFOLDER_BINDING_PROPERTIES:\n $srcMainJava_bindingPropertiesFolder_path\n"
    deleteFolder(srcMainJava_bindingPropertiesFolder_path.toFile())


    Path binding_properties_resources_path =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'resources',
                    "properties"
            )
    deleteFolder(binding_properties_resources_path.toFile())
    log_text += "\nBINDING_PROPERTY_RESOURCES - " + "$binding_properties_resources_path\n"
}



// DEFAULTs --------------------------------------------------------------------
if (keep_binding_properties == 'true' && keep_default_binding_properties == 'false') {

    Path binding_properties_path =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'java',
                    project_package,
                    entity_folder,
                    config_folder,
                    "properties"
            )

    List<String> filesToDelete =
            [
                    "ApplicationProperties.java",
                    "ApplicationPropertiesRecord.java"
            ]
    deleteFilesFromList(binding_properties_path, filesToDelete)

    log_text += "\nDEFAULT_BINDING_PROPERTY - " +
            filesToDelete.toString() + "\\n " +
            "$binding_properties_path\n"

}



// LISTs -----------------------------------------------------------------------
if (keep_binding_properties == 'true' && keep_list_binding_properties == 'false') {

    Path binding_properties_path =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'java',
                    project_package,
                    entity_folder,
                    config_folder,
                    "properties"
            )
    List<String> filesToDelete =
            [
                    "ListObjects.java",
                    "ListProperties.java",
                    "ListPropertiesImmutable.java",
                    "ListPropertiesRecord.java"
            ]
    deleteFilesFromList(binding_properties_path, filesToDelete)

    log_text += "\nLIST_BINDING_PROPERTY - " +
            filesToDelete.toString() + "\\n " +
            "$binding_properties_path\n"




    Path binding_properties_resources_path =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'resources',
                    "properties"
            )
    List<String> filesToDeleteResources =
            [
                    "application_list.properties",
                    "application_list.yml"
            ]
    deleteFilesFromList(binding_properties_resources_path, filesToDeleteResources)

    log_text += "\nLIST_BINDING_PROPERTY_RESOURCES - " +
            filesToDeleteResources.toString() + "\\n " +
            "$binding_properties_resources_path\n"
}



// MAPs ------------------------------------------------------------------------
if (keep_binding_properties == 'true' && keep_map_binding_properties == 'false') {

    Path binding_properties_path =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'java',
                    project_package,
                    entity_folder,
                    config_folder,
                    "properties"
            )
    List<String> filesToDelete =
            [
                    "MapItem.java",
                    "MapProperties.java",
                    "MapPropertiesImmutable.java",
                    "MapPropertiesRecord.java"
            ]
    deleteFilesFromList(binding_properties_path, filesToDelete)

    log_text += "\nMAP_BINDING_PROPERTY - " +
            filesToDelete.toString() + "\\n " +
            "$binding_properties_path\n"





    Path binding_properties_resources_path =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'resources',
                    "properties"
            )
    List<String> filesToDeleteResources =
            [
                    "application_map.properties",
                    "application_map.yml"
            ]
    deleteFilesFromList(binding_properties_resources_path, filesToDeleteResources)

    log_text += "\nMAP_BINDING_PROPERTY_RESOURCES - " +
            filesToDeleteResources.toString() + "\\n " +
            "$binding_properties_resources_path\n"
}



// MUTABLEs --------------------------------------------------------------------
// mutable_binding_properties_choice:   1 - Immutable;   2 - Mutable;   3 - Both
if (keep_binding_properties == 'true' && mutable_binding_properties_choice == '1') {

    Path binding_properties_path =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'java',
                    project_package,
                    entity_folder,
                    config_folder,
                    'properties')
    List<String> filesToDelete =
            [
                    "ApplicationProperties.java",
                    "ListProperties.java",
                    "MapProperties.java"
            ]
    deleteFilesFromList(binding_properties_path, filesToDelete)

    log_text += "\nIMMUTABLE_BINDING_PROPERTY - " +
            filesToDelete.toString() + "\\n " +
            "$binding_properties_path\n"

}

if (keep_binding_properties == 'true' && mutable_binding_properties_choice == '2') {

    Path binding_properties_path =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'java',
                    project_package,
                    entity_folder,
                    config_folder,
                    'properties')
    List<String> filesToDelete =
            [
                    "ApplicationPropertiesRecord.java",
                    "ListPropertiesImmutable.java",
                    "ListPropertiesRecord.java",
                    "MapPropertiesImmutable.java",
                    "MapPropertiesRecord.java"
            ]
    deleteFilesFromList(binding_properties_path, filesToDelete)

    log_text += "\nMUTABLE_BINDING_PROPERTY - " +
            filesToDelete.toString() + "\\n " +
            "$binding_properties_path\n"

}