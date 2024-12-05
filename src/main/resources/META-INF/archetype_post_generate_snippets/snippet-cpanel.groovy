import java.nio.file.Path
import java.nio.file.Paths


// -----------------------------------------------------------------------------

// SNIPPET-CPANEL.GROOVY
if (keep_logback == 'true' && keep_cpanel_elk_log_system == 'false') {

    Path logstachPath =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'resources',
                    'logback-spring-config',
                    'appender-tcp-elk-logstash.xml')
    log_text += "\nAppenders-tcp-logstash.xml -> resources -> logback-spring-config:\n $logstachPath\n"
    deleteFile(logstachPath.toFile())


    deleteElkLogSystemStructure(log_text)
}