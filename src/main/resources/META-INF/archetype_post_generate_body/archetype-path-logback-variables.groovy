import java.nio.file.Path

// -----------------------------------------------------------------------------






// ARCHETYPE-PATH-LOGBACK-VARIABLES.GROOVY -------------------------------------
Path srcMainResourcesLogbackSpringConfigFolder = srcMainResources.resolve("logback-spring-config")

Path logbackAppenderLogstashPathFile = srcMainResourcesLogbackSpringConfigFolder.resolve("appender-tcp-elk-logstash.xml")

Path logbackAppenderSmtpPathFile = srcMainResourcesLogbackSpringConfigFolder.resolve("appender-smtp-email.xml")
Path logbackAppenderRolFilePathFile = srcMainResourcesLogbackSpringConfigFolder.resolve("appender-rolfile-file.xml")

Path logbackAppenderConsoleTabularPathFile = srcMainResourcesLogbackSpringConfigFolder.resolve("appender-console-tabular.xml")
Path logbackAppenderConsoleGranularPathFile = srcMainResourcesLogbackSpringConfigFolder.resolve("appender-console-granular.xml")
Path logbackAppenderConsoleLayoutGranularPathFile = srcMainResourcesLogbackSpringConfigFolder.resolve("appender-console-layouts-granular.xml")

Path srcTestResourcesLogbackTestFile = srcTestResources.resolve("logback-test.xml")
Path srcMainResourcesLogbackSpringXmlFile = srcMainResources.resolve("logback-spring.xml")