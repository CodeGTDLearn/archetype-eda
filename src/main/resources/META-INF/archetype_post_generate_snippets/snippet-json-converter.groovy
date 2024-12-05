import java.nio.file.Path
import java.nio.file.Paths




// -----------------------------------------------------------------------------
// SNIPPET-JSON-CONVERTER.GROOVY


//FILE MANIPULATION
if (enable_denormalization == 'true') {

    Path logstachPath =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'resources', 'logback-spring-config', 'appender-tcp-elk-logstash.xml')

    log_text += "\nAppenders-tcp-logstash.xml -> resources -> logback-spring-config:\n $logstachPath\n"

    deleteFile(logstachPath.toFile())

    deleteElkLogSystemStructure(log_text)
}

//FOLDER MANIPULATION
if (enable_denormalization == 'false') {

    Path bindingPropertiesPath =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'java', entity_name, 'config', 'properties')

    log_text += "\\nFOLDER_BINDING_PROPERTIES:\\n $bindingPropertiesPath\n"

    deleteFolder(folderPath.toFile())
}