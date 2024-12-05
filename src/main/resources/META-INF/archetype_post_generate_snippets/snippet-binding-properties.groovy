import java.nio.file.Path
import java.nio.file.Paths










// SNIPPET-BINDING-PROPERTIES.GROOVY -------------------------------------------


if (keep_binding_properties == 'false') {

    Path bindingPropertiesPath =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'java',
                    project_package,
                    entity_folder,
                    config_folder,
                    binding_properties_folder)
    log_text += "\\nFOLDER_BINDING_PROPERTIES:\\n $bindingPropertiesPath\n"
    deleteFolder(bindingPropertiesPath.toFile())


    Path propertiesYmlPath =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'resources', 'application.yml')
    log_text += "\napplication.yml -> resources:\n $propertiesYmlPath\n"
    deleteFile(propertiesYmlPath.toFile())
}


if (keep_binding_properties == 'true') {

    def fileType = (properties_filetype == '1') ? '.yml' : '.properties'
    def fileTypeMessage = (properties_filetype == '1') ? 'Delete All YML-FILES' : 'Delete All PROPERTIES-FILES'
    log_text += "\\nChosen FileType: " + fileType + " | " + fileTypeMessage

    Path mainJavaYmlPathDeletion =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'java',
                    project_package,
                    entity_folder,
                    config_folder,
                    binding_properties_folder)
    log_text += "\\nFOLDER_BINDING_PROPERTIES - " + fileTypeMessage + "\\n $mainJavaYmlPathDeletion\n"
//    deleteFolder(mainJavaYmlPathDeletion.toFile())
    deleteFilesByExtension(mainJavaYmlPathDeletion, fileType, true)


    Path mainResourcesYmlPathDeletion =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'resources',
                    fileType)
    log_text += "\n" + fileTypeMessage + " -> resources:\n $mainResourcesYmlPathDeletion\n"
//    deleteFile(mainResourcesYmlPathDeletion.toFile())
    deleteFilesByExtension(mainJavaYmlPathDeletion, fileType, true)
}