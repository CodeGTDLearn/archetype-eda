import java.nio.file.Path

// -----------------------------------------------------------------------------








// SNIPPET-13-ENABLE_API_CPANEL_ELK_LOG_SYSTEM.GROOVY --------------------------
// OWNER: the ELK artifacts when LOGBACK IS ON. When logback is off the whole
// structure is already gone (see snippet-17-enable_logback_support).
if (enable_logback_support == 'true' && enable_api_cpanel_elk_log_system == 'false') {


    log_text += "\nAppenders-tcp-logstash.xml -> resources -> logback-spring-config:\n $logbackAppenderLogstashPathFile\n"
    deleteFile(logbackAppenderLogstashPathFile.toFile())


    log_text += deleteElkArtifacts(rootELKFolder, rootDockerFolder)
}