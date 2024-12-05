import java.nio.file.Path

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

    log_text += "\nlogback-test.xml -> test-resources:\n $srcTestResourcesLogbackTestFile\n"
    deleteFile(srcTestResourcesLogbackTestFile.toFile())

    log_text += "\nFOLDER_LOGBACK-SPRING-CONFIG:\n $srcMainResourcesLogbackSpringConfigFolder\n"
    deleteFolder(srcMainResourcesLogbackSpringConfigFolder.toFile())

//    deleteElkLogSystemStructure(log_text)
    log_text += "\nFOLDER_ELK:\n $rootELKFolder\n"
    deleteFolder(rootELKFolder.toFile())

    Path dockerElkComposePath =
            rootDockerFolder
                    .resolve("elk-compose.yml")

    log_text += "\nelk-compose.yml -> docker:\n $dockerElkComposePath\n"
    deleteFile(dockerElkComposePath.toFile())

}

if (keep_logback == 'true' && console_type == '1') {
    log_text += "\nAppenders-console-granular.xml -> resources -> logback-spring-config:\n $logbackAppenderConsoleGranularPathFile\n"
    deleteFile(logbackAppenderConsoleGranularPathFile.toFile())

    log_text += "\nappenders-console-layouts-granular.xml -> resources -> logback-spring-config:\n $logbackAppenderConsoleLayoutGranularPathFile\n"
    deleteFile(logbackAppenderConsoleLayoutGranularPathFile.toFile())

    log_text += "\nAppenders-console-tabular.xml -> resources -> logback-spring-config:\n $logbackAppenderConsoleTabularPathFile\n"
    deleteFile(logbackAppenderConsoleTabularPathFile.toFile())

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
    log_text += "\nAppenders-console-granular.xml -> resources -> logback-spring-config:\n $logbackAppenderConsoleGranularPathFile\n"
    deleteFile(logbackAppenderConsoleGranularPathFile.toFile())

    log_text += "\nappender-console-layouts-granular.xml -> resources -> logback-spring-config:\n $logbackAppenderConsoleLayoutGranularPathFile\n"
    deleteFile(logbackAppenderConsoleLayoutGranularPathFile.toFile())
}

if (keep_logback == 'true' && console_type == '3') {
    log_text += "\nAppenders-console-tabular.xml -> resources -> logback-spring-config:\n $logbackAppenderConsoleTabularPathFile\n"
    deleteFile(logbackAppenderConsoleTabularPathFile.toFile())
}

if (keep_logback == 'true' && keep_appender_smtp == 'false') {
    log_text += "\nAppenders-smtp.xml -> resources -> logback-spring-config:\n $logbackAppenderSmtpPathFile\n"
    deleteFile(logbackAppenderSmtpPathFile.toFile())
}

if (keep_logback == 'true' && keep_appender_file == 'false') {
    log_text += "\nAppenders-file.xml -> resources -> logback-spring-config:\n $logbackAppenderRolFilePathFile\n"
    deleteFile(logbackAppenderRolFilePathFile.toFile())
}