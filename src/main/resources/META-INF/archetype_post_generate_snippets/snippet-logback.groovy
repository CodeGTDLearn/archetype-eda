// -----------------------------------------------------------------------------

// SNIPPET-LOGBACK.GROOVY
if (keep_logback == 'false' ||
        (keep_logback == 'true' &&
                console_type == '1' &&
                keep_appender_smtp == 'false' &&
                keep_appender_file == 'false' &&
                keep_cpanel_elk_log_system == 'false'
        )
) {
    log_text += "\nlogback-spring.xml -> resources:\n $srcMainResourcesLogbackSpringXmlFile\n"
    deleteFile(srcMainResourcesLogbackSpringXmlFile.toFile())

    log_text += "\nlogback-test.xml -> test-resources:\n $srcTestResourceslogbackTestFile\n"
    deleteFile(srcTestResourceslogbackTestFile.toFile())

    log_text += "\nFOLDER_LOGBACK-SPRING-CONFIG:\n $srcMainResourcesLogbackSpringConfigFolder\n"
    deleteFolder(srcMainResourcesLogbackSpringConfigFolder.toFile())

    deleteElkLogSystemStructure(log_text)
}

if (keep_logback == 'true' && console_type == '1') {
    log_text += "\nAppenders-console-granular.xml -> resources -> logback-spring-config:\n $logbackConsoleGranularFile\n"
    deleteFile(logbackConsoleGranularFile.toFile())

    log_text += "\nappenders-console-layouts-granular.xml -> resources -> logback-spring-config:\n $logbackGranularLayoutConsoleFile\n"
    deleteFile(logbackGranularLayoutConsoleFile.toFile())

    log_text += "\nAppenders-console-tabular.xml -> resources -> logback-spring-config:\n $logbackConsoleTabularFile\n"
    deleteFile(logbackConsoleTabularFile.toFile())

//        O Arquivo "LOGBACK-SPRING.XML" continua existindo E sendo usado
//        MAS SEM NENHUMA ESTRUTURA de CUSTOM-CONSOLE(tabular ou granular),
//        pois sem estrutura de  CUSTOM-CONSOLE apesar da existencia do "LOGBACK-SPRING.XML"
//        este arquivo nao possui qquer tipo de 'CUSTOM-CONSOLE', assumindo-se assim para fins de CONSOLE,
//        o CONSOLE-DEFAULT-INTERNO do Spring Boot AUTOMATICAMENTE
//        NO CASO DE TESTE("LOGBACK-TEST.XML") o arquivo e removido
//        E O console do Spring Boot sera acionado
    log_text += "\nlogback-test.xml -> test-resources:\n $srcTestResourcesLogbackTestFile\n"
    deleteFile(srcTestResourcesLogbackTestFile.toFile())
}

if (keep_logback == 'true' && console_type == '2') {
    log_text += "\nAppenders-console-granular.xml -> resources -> logback-spring-config:\n $logbackAppenderConsoleGranularFile\n"
    deleteFile(logbackAppenderConsoleGranularFile.toFile())

    log_text += "\nappender-console-layouts-granular.xml -> resources -> logback-spring-config:\n $appenderGranularLayoutConsoleFile\n"
    deleteFile(appenderGranularLayoutConsoleFile.toFile())
}

if (keep_logback == 'true' && console_type == '3') {
    log_text += "\nAppenders-console-tabular.xml -> resources -> logback-spring-config:\n $logbackConsoleTabularFile\n"
    deleteFile(logbackConsoleTabularFile.toFile())
}

if (keep_logback == 'true' && keep_appender_smtp == 'false') {
    log_text += "\nAppenders-smtp.xml -> resources -> logback-spring-config:\n $logbackStmpPathFile\n"
    deleteFile(logbackStmpPathFile.toFile())
}

if (keep_logback == 'true' && keep_appender_file == 'false') {
    log_text += "\nAppenders-file.xml -> resources -> logback-spring-config:\n $logbackRolFilePath\n"
    deleteFile(logbackRolFilePath.toFile())
}