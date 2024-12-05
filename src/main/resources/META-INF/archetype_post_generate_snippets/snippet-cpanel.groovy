import java.nio.file.Path

// -----------------------------------------------------------------------------






// SNIPPET-CPANEL.GROOVY
if (keep_logback == 'true' && keep_cpanel_elk_log_system == 'false') {


    log_text += "\nAppenders-tcp-logstash.xml -> resources -> logback-spring-config:\n $logbackAppenderLogstashPathFile\n"
    deleteFile(logbackAppenderLogstashPathFile.toFile())


//    deleteElkLogSystemStructure(log_text)
    log_text += "\nFOLDER_ELK:\n $rootELKFolder\n"
    deleteFolder(rootELKFolder.toFile())

    Path dockerElkComposePath =
            rootDockerFolder
                    .resolve("elk-compose.yml")

    log_text += "\nelk-compose.yml -> docker:\n $dockerElkComposePath\n"
    deleteFile(dockerElkComposePath.toFile())
}