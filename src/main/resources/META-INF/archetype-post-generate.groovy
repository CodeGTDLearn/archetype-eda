import java.nio.file.Path
import java.nio.file.Paths

Properties properties = request.properties

// 1.0) capturando Variaveis/Properties vindas do "ARCHETYPE-METADATA".xml
def entity_folder = "${properties?.entity_name}"
def enable_logback = "${properties?.enable_logback}"
def keep_docker_folder = "${properties?.enable_docker}"
def show_log = "${properties?.show_archetype_posprocessor_log}"
def logback_console_type = "${properties?.logback_console_type}"
def keep_test_folder = "${properties?.enable_folder_src_test_java}"
def extra_POM_dependencies = "${properties?.extra_POM_dependencies}"
def keep_jwt_auth_folder = "${properties?.enable_jwt_authentication}"
def enable_logback_appender_smtp = "${properties?.enable_logback_appender_smtp}"
def enable_logback_appender_file = "${properties?.enable_logback_appender_file}"
def enable_logback_appender_logstash = "${properties?.enable_logback_appender_logstash}"


// 2.0) Variaveis Operacionais exclusivas deste Script
def config_folder = "config"
def authentication_folder = "authentication"
//def project_security_folder = "${properties?.entity_name}".concat(config_folder)

def log_text_header="\n\n\n"+
             "\n-------------------------------------------------------------------------------\n" +
             "\n---------- POS-PROCESSOR  -  ARCHETYPE-POST-GENERATE LOG:  STARTING -----------\n" +
             "\n-------------------------------------------------------------------------------\n\n\n"

def log_text_footer ="\n\n"+
        "\n-------------------------------------------------------------------------------\n" +
        "\n----------- POS-PROCESSOR  -  ARCHETYPE-POST-GENERATE LOG:  ENDING ------------\n" +
        "\n-------------------------------------------------------------------------------\n" +
        "\n\n\n\n\n"

def log_text=log_text_header

def project_package = "${properties?.package}".replace('.', '/')

if (keep_test_folder == 'false') {
    Path folderTestPath =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'test', 'java')
    log_text += "\nFOLDER_SRC_TEST_JAVA:\n $folderTestPath\n"
    deleteFolder(folderTestPath.toFile())

    Path folderTestResourcePath =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'test', 'resources')
    log_text += "\nFOLDER_SRC_TEST_RESOURCES:\n $folderTestResourcePath\n"

    deleteFolder(folderTestResourcePath.toFile())
}

if (enable_logback == 'false') {
    Path logbackPath =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'resources', 'logback-spring.xml')
    log_text += "\nlogback-spring.xml -> resources:\n $logbackPath\n"
    deleteFile(logbackPath.toFile())

    Path testLogbackPath =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'test', 'resources', 'logback-test.xml')
    log_text += "\nlogback-test.xml -> test-resources:\n $testLogbackPath\n"
    deleteFile(testLogbackPath.toFile())

    Path folderLogbackSpringConfigPath =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'resources', 'logback-spring-config')
    log_text += "\nFOLDER_LOGBACK-SPRING-CONFIG:\n $folderLogbackSpringConfigPath\n"
    deleteFolder(folderLogbackSpringConfigPath.toFile())
}

if (enable_logback == 'true' && logback_console_type == '1') {

    Path logbackPath =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'resources', 'logback-spring.xml')
    log_text += "\nlogback-spring.xml -> resources:\n $logbackPath\n"
    deleteFile(logbackPath.toFile())

    Path testLogbackPath =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'test', 'resources', 'logback-test.xml')
    log_text += "\nlogback-test.xml -> test-resources:\n $testLogbackPath\n"
    deleteFile(testLogbackPath.toFile())

    Path consoleGranular =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'resources', 'logback-spring-config','appenders-console-granular.xml')
    log_text += "\nAppenders-console-granular.xml -> resources -> logback-spring-config:\n $consoleGranular\n"
    deleteFile(consoleGranular.toFile())

    Path granularLayoutConsole =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'resources', 'logback-spring-config','appenders-console-layouts-granular.xml')
    log_text += "\nappenders-console-layouts-granular.xml -> resources -> logback-spring-config:\n $granularLayoutConsole\n"
    deleteFile(granularLayoutConsole.toFile())

    Path consoleTabular =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'resources', 'logback-spring-config','appenders-console-tabular.xml')
    log_text += "\nAppenders-console-tabular.xml -> resources -> logback-spring-config:\n $consoleTabular\n"
    deleteFile(consoleTabular.toFile())
}

if (enable_logback == 'true' && logback_console_type == '2') {

    Path consoleGranular =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'resources', 'logback-spring-config','appenders-console-granular.xml')
    log_text += "\nAppenders-console-granular.xml -> resources -> logback-spring-config:\n $consoleGranular\n"
    deleteFile(consoleGranular.toFile())

    Path granularLayoutConsole =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'resources', 'logback-spring-config','appenders-console-layouts-granular.xml')
    log_text += "\nappenders-console-layouts-granular.xml -> resources -> logback-spring-config:\n $granularLayoutConsole\n"
    deleteFile(granularLayoutConsole.toFile())
}

if (enable_logback == 'true' && logback_console_type == '3') {

    Path consoleTabular =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'resources', 'logback-spring-config','appenders-console-tabular.xml')
    log_text += "\nAppenders-console-tabular.xml -> resources -> logback-spring-config:\n $consoleTabular\n"
    deleteFile(consoleTabular.toFile())
}

if (enable_logback == 'true' && enable_logback_appender_logstash == 'false') {

    Path logstachPath =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'resources', 'logback-spring-config','appenders-tcp-logstash.xml')
    log_text += "\nAppenders-tcp-logstash.xml -> resources -> logback-spring-config:\n $logstachPath\n"
    deleteFile(logstachPath.toFile())

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

if (enable_logback == 'true' && enable_logback_appender_smtp == 'false') {

    Path stmpPath =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'resources', 'logback-spring-config','appenders-smtp.xml')
    log_text += "\nAppenders-smtp.xml -> resources -> logback-spring-config:\n $stmpPath\n"
    deleteFile(stmpPath.toFile())
}

if (enable_logback == 'true' && enable_logback_appender_file == 'false') {

    Path filePath =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'resources', 'logback-spring-config','appenders-file.xml')
    log_text += "\nAppenders-file.xml -> resources -> logback-spring-config:\n $filePath\n"
    deleteFile(filePath.toFile())
}

if (keep_jwt_auth_folder == 'false') {
    Path folderPath =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'java',
                    project_package, entity_folder, config_folder, authentication_folder)
    log_text += "\nJwt_authentication_folder_path:\n $folderPath\n"
    deleteFolder(folderPath.toFile())

    Path filePathAppKey =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'resources', 'appJwt.key')
    log_text.concat("\nsrc_main_resources_app_key:\n $filePathAppKey\n")
    deleteFile(filePathAppKey.toFile())

    Path filePathAppPub =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'resources', 'appJwt.pub')
    log_text.concat("\nsrc_main_resources_app_pub:\n $filePathAppPub\n")
    deleteFile(filePathAppPub.toFile())
}

if (extra_POM_dependencies.length() > 0 && extra_POM_dependencies.toString() != 'false') {

    String[] dependencies = extra_POM_dependencies.split(',')

    Path pomFile = Paths.get(request.outputDirectory, request.artifactId, 'pom.xml')
    log_text += "\nPOM.XML"
    log_text += "\n - Location: $pomFile"
    log_text += "\n - Added Dependencies: $dependencies\n"

    if (pomFile.toFile().isFile()) {

        def sb = new StringBuilder()

        Scanner sc

        try {
            sc = new Scanner(pomFile)
            String currentLine = ''
            String lastLine = ''

            while (sc.hasNext()) {
//                log_text += "\n - While: $lastLine\n"
                lastLine = currentLine
                currentLine = sc.nextLine()

                if (currentLine.trim().length() == 0) continue

                if (
                        currentLine.contains("</dependency>") ||
                                currentLine.contains("</modelVersion>") ||
                                currentLine.contains("</parent>") ||
                                currentLine.contains("</description>") ||
                                currentLine.contains("</properties>") ||
                                currentLine.contains("</dependencies>") ||
                                currentLine.contains("</build>") ||
                                currentLine.contains("</dependencyManagement>")
                ) sb.append(currentLine).append("\n\n")
                else sb.append(currentLine).append("\n")

//                log_text += sb.toString()

                // Add dependencies
//                if (lastLine.contains("</dependencyManagement>") && currentLine.contains("<dependencies>")) {
                if (currentLine.contains("<dependencies>")) {

                    for (def dep : dependencies) {

                        String[] depInfo = dep.split(':')
                        String scopeVal = 'compile'

                        if (depInfo.length > 3) scopeVal = depInfo[3]

                        sb.append('        <dependency>').append('\n')
                        sb.append("            <groupId>${depInfo[0]}</groupId>").append('\n')
                        sb.append("            <artifactId>${depInfo[1]}</artifactId>").append('\n')
                        sb.append("            <version>${depInfo[2]}</version>").append('\n')
                        sb.append("            <scope>${scopeVal}</scope>").append('\n')
                        sb.append('        </dependency>').append('\n\n')
                    }
                }
            }

        } finally {
            sc.close()
        }

        // Empty the parent pom file
        //  PrintWriter pw = new PrintWriter(pomFile.toFile())
        PrintWriter pw = new PrintWriter(System.err)
        pw.close()

        // Write the modified parent pom
        PrintWriter writer = new PrintWriter(pomFile.toFile())
        writer.write(sb.toString())
        writer.close()
    }
}

if (keep_docker_folder == 'false') {
    Path folderPath =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'docker')
    log_text += "\nFOLDER_DOCKER:\n $folderPath\n"
    deleteFolder(folderPath.toFile())
}

if (show_log == 'true') printf "\n$log_text $log_text_footer"

//  Supplementar Methods
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