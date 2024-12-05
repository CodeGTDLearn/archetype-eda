import java.nio.file.Path
import java.nio.file.Paths

def keep_logback_structure = "${properties?.enable_logback}"
def logback_console_type = "${properties?.logback_console_type}"
def keep_appender_smtp = "${properties?.enable_logback_appender_smtp}"
def keep_appender_file = "${properties?.enable_logback_appender_file}"
def keep_elk_log_system_structure = "${properties?.enable_api_cpanel_elk_log_system}"

if (keep_logback_structure == 'false') {
    Path logbackSpring_Path =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'resources', 'logback-spring.xml')
    log_text += "\nlogback-spring.xml -> resources:\n $logbackSpring_Path\n"
    deleteFile(logbackSpring_Path.toFile())

    Path logbackTest_Path =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'test', 'resources', 'logback-test.xml')
    log_text += "\nlogback-test.xml -> test-resources:\n $logbackTest_Path\n"
    deleteFile(logbackTest_Path.toFile())

    Path logbackSpringConfig_Path =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'resources', 'logback-spring-config')
    log_text += "\nFOLDER_LOGBACK-SPRING-CONFIG:\n $logbackSpringConfig_Path\n"
    deleteFolder(logbackSpringConfig_Path.toFile())

    deleteElkLogSystemStructure(log_text)
}

if (keep_logback_structure == 'true' && logback_console_type == '1') {

    Path consoleGranular =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'resources', 'logback-spring-config', 'appenders-console-granular.xml')
    log_text += "\nAppenders-console-granular.xml -> resources -> logback-spring-config:\n $consoleGranular\n"
    deleteFile(consoleGranular.toFile())

    Path granularLayoutConsole =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'resources', 'logback-spring-config', 'appenders-console-layouts-granular.xml')
    log_text += "\nappenders-console-layouts-granular.xml -> resources -> logback-spring-config:\n $granularLayoutConsole\n"
    deleteFile(granularLayoutConsole.toFile())

    Path consoleTabular =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'resources', 'logback-spring-config', 'appenders-console-tabular.xml')
    log_text += "\nAppenders-console-tabular.xml -> resources -> logback-spring-config:\n $consoleTabular\n"
    deleteFile(consoleTabular.toFile())
}

if (keep_logback_structure == 'true' && logback_console_type == '2') {

    Path consoleGranular =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'resources', 'logback-spring-config', 'appenders-console-granular.xml')
    log_text += "\nAppenders-console-granular.xml -> resources -> logback-spring-config:\n $consoleGranular\n"
    deleteFile(consoleGranular.toFile())

    Path granularLayoutConsole =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'resources', 'logback-spring-config', 'appenders-console-layouts-granular.xml')
    log_text += "\nappenders-console-layouts-granular.xml -> resources -> logback-spring-config:\n $granularLayoutConsole\n"
    deleteFile(granularLayoutConsole.toFile())
}

if (keep_logback_structure == 'true' && logback_console_type == '3') {

    Path consoleTabular =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'resources', 'logback-spring-config', 'appenders-console-tabular.xml')
    log_text += "\nAppenders-console-tabular.xml -> resources -> logback-spring-config:\n $consoleTabular\n"
    deleteFile(consoleTabular.toFile())
}

if (keep_logback_structure == 'true' && keep_appender_smtp == 'false') {

    Path stmpPath =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'resources', 'logback-spring-config', 'appenders-smtp.xml')
    log_text += "\nAppenders-smtp.xml -> resources -> logback-spring-config:\n $stmpPath\n"
    deleteFile(stmpPath.toFile())
}

if (keep_logback_structure == 'true' && keep_appender_file == 'false') {

    Path filePath =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'resources', 'logback-spring-config', 'appenders-file.xml')
    log_text += "\nAppenders-file.xml -> resources -> logback-spring-config:\n $filePath\n"
    deleteFile(filePath.toFile())
}

if (keep_logback_structure == 'true' && keep_elk_log_system_structure == 'false') {

    Path logstachPath =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'resources', 'logback-spring-config', 'appenders-tcp-logstash.xml')
    log_text += "\nAppenders-tcp-logstash.xml -> resources -> logback-spring-config:\n $logstachPath\n"
    deleteFile(logstachPath.toFile())

    deleteElkLogSystemStructure(log_text)
}

private void deleteElkLogSystemStructure(log_text) {
    Path folderELKPath =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'ELK')
    log_text += "\nFOLDER_ELK:\n $folderELKPath\n"
    deleteFolder(folderELKPath.toFile())

    Path dockerElkComposePath =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'docker', 'elk-compose.yml')
    log_text += "\nelk-compose.xml -> docker:\n $dockerElkComposePath\n"
    deleteFile(dockerElkComposePath.toFile())
}

def deleteFolder(File dir) {
    if (dir.isDirectory()) {
        dir.listFiles().each {
            file -> if (file.isDirectory()) deleteFolder(file) else file.delete()
        }
    }
    dir.delete()
}

def deleteFile(File file) {
    if (file.isFile()) file.delete()
}