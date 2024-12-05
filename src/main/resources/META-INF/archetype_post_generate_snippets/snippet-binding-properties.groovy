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
}


if (keep_default_binding_properties == 'false') {


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
                    "KeyProperties.java",
                    "ApplicationProperties.java"
            ]


    deleteFilesFromList(binding_properties_path, filesToDelete)


    log_text += "\nDEFAULT_BINDING_PROPERTY - " + filesToDelete.toString() + "\\n " + "$binding_properties_path\n"
}


if (keep_list_binding_properties == 'false') {

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
                    "ListPropertiesClass.java",
                    "ListPropertiesRecord.java"
            ]


    log_text += "\nLIST_BINDING_PROPERTY - " + filesToDelete.toString() + "\\n " + "$binding_properties_path\n"
    deleteFilesFromList(filesToDelete, binding_properties_path)
}


if (keep_map_binding_properties == 'false') {

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
                    "MapPropertiesClass.java",
                    "MapPropertiesRecord.java"
            ]


    log_text += "\nMAP_BINDING_PROPERTY - " + filesToDelete.toString() + "\\n " + "$binding_properties_path\n"
    deleteFilesFromList(filesToDelete, binding_properties_path)
}