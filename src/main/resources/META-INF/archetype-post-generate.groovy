import java.nio.file.Path
import java.nio.file.Paths

Properties properties = request.properties


// 1.0) capturando Variaveis oriundas do archetype-metadata.xml
def project_name = "${properties?.project_name}"
def show_log = "${properties?.show_archetype_posprocessor_log}"
def keep_test_folder = "${properties?.enable_folder_src_test_java}"
def keep_resources_test_folder = "${properties?.enable_folder_src_test_resources}"
def keep_logback_file = "${properties?.enable_logback}"
def keep_security_folder = "${properties?.enable_security_authentication_support}"
def extra_POM_dependencies = "${properties?.extra_POM_dependencies}"


// 2.0) Variaveis Operacionais exclusivas deste Script
def security_folder_suffix = "Security"
def project_security_folder = "${properties?.project_name}".concat(security_folder_suffix)

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
    Path folderPath =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'test', 'java')
    log_text += "\n1) FOLDER_SRC_TEST_JAVA location:\n $folderPath\n"

    deleteFolder(folderPath.toFile())
}

if (keep_resources_test_folder == 'false') {
    Path folderPath =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'test', 'resources')
    log_text += "\n2) FOLDER_SRC_TEST_RESOURCES location:\n $folderPath\n"

    deleteFolder(folderPath.toFile())
}

if (keep_logback_file == 'false') {
    Path filePath =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'resources', 'logback-spring.xml')
    log_text += "\n3.1) logback-spring.xml -> resources -> location:\n $filePath\n"
    deleteFile(filePath.toFile())

    Path filePathConfig =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'resources','properties', 'logback_config.properties')
    log_text += "\n3.2) logback_config.properties -> resources-properties -> location:\n $filePathConfig\n"
    deleteFile(filePathConfig.toFile())

    Path filePathTest =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'test', 'resources', 'logback-test.xml')
    log_text += "\n3.3) logback-test.xml -> test-resources -> location:\n $filePathTest\n"
    deleteFile(filePathTest.toFile())
}

if (keep_security_folder == 'false') {
    Path folderPath =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'java', project_package, project_security_folder)
    log_text += "\n4) Security_folder_path location:\n $folderPath\n"

    deleteFolder(folderPath.toFile())

    Path filePathAppKey =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'resources', 'app.key')
    log_text.concat("\nsrc_main_resources_app_key:\n $filePathAppKey\n")

    deleteFile(filePathAppKey.toFile())

    Path filePathAppPub =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'resources', 'app.pub')
    log_text.concat("\nsrc_main_resources_app_pub:\n $filePathAppPub\n")

    deleteFile(filePathAppPub.toFile())
}

if (extra_POM_dependencies.length() > 0 && extra_POM_dependencies.toString() != 'false') {

    String[] dependencies = extra_POM_dependencies.split(',')

    Path pomFile = Paths.get(request.outputDirectory, request.artifactId, 'pom.xml')
    log_text += "\n5) POM.XML"
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