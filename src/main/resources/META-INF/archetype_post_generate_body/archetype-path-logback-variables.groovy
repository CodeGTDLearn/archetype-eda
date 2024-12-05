import java.nio.file.Path

// -----------------------------------------------------------------------------

// ARCHETYPE-PATH-LOGBACK-VARIABLES.GROOVY -------------------------------------

Path srcMainResourcesLogbackSpringConfigFolder = srcMainResources.resolve("logback-spring-config")

Path logstashPath = srcMainResourcesLogbackSpringConfigFolder.resolve("appender-tcp-elk-logstash.xml")
Path logbackSmtpPathFile = srcMainResourcesLogbackSpringConfigFolder.resolve("appender-smtp-email.xml")
Path logbackRolFilePath = srcMainResourcesLogbackSpringConfigFolder.resolve("appender-rolfile-file.xml")
Path logbackConsoleTabularFile = srcMainResourcesLogbackSpringConfigFolder.resolve("appender-console-tabular.xml")
Path logbackAppenderConsoleGranularFile = srcMainResourcesLogbackSpringConfigFolder.resolve("appender-console-granular.xml")
Path appenderGranularLayoutConsoleFile = srcMainResourcesLogbackSpringConfigFolder.resolve("appender-console-layouts-granular.xml")

Path srcTestResourcesLogbackTestFile = srcTestResources.resolve("logback-test.xml")
Path srcMainResourcesLogbackSpringXmlFile = srcMainResources.resolve("logback-spring.xml")

// -----------------------------------------------------------------------------