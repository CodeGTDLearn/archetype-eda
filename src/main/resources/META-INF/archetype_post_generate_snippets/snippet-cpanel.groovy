// -----------------------------------------------------------------------------

// SNIPPET-CPANEL.GROOVY
if (keep_logback == 'true' && keep_cpanel_elk_log_system == 'false') {


    log_text += "\nAppenders-tcp-logstash.xml -> resources -> logback-spring-config:\n $logstachPath\n"
    deleteFile(logstachPath.toFile())


    deleteElkLogSystemStructure(log_text)
}