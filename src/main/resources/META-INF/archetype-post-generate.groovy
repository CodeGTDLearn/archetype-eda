import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.Files

// -----------------------------------------------------------------------------






// ARCHETYPE-HEADER.GROOVY -----------------------------------------------------

Properties properties = request.properties
// -----------------------------------------------------------------------------







// ARCHETYPE-GLOBAL-VARIABLES.GROOVY -------------------------------------------

def project_package = "${properties?.package}".replace('.', '/')

def entity_name = "${properties?.entity_name}"

def entity_folder = "${properties?.entity_name}"
def config_folder = "config"

def properties_folder = "properties"

def show_pos_processor_log = "${properties?.show_archetype_pos_processor_log}"

def log_text_header =
        "\n\n\n-------------------------------------------------------------------------------\n" +
                "\n---------- POS-PROCESSOR  -  ARCHETYPE-POST-GENERATE LOG:  STARTING -----------\n" +
                "\n-------------------------------------------------------------------------------\n" +
                "\n\n"

def log_text_footer =
        "\n\n\n-------------------------------------------------------------------------------\n" +
                "\n----------- POS-PROCESSOR  -  ARCHETYPE-POST-GENERATE LOG:  ENDING ------------\n" +
                "\n-------------------------------------------------------------------------------\n" +
                "\n\n\n\n\n"

def log_text = log_text_header

// -----------------------------------------------------------------------------






// ARCHETYPE-PATH-ROOT-VARIABLES.GROOVY ----------------------------------------

Path projectRootFolder = Paths.get(request.outputDirectory, request.artifactId)

Path rootELKFolder = projectRootFolder.resolve("ELK")
Path rootGitFolder = projectRootFolder.resolve("git_temp")
Path rootDockerFolder = projectRootFolder.resolve("docker")

Path rootGitIgnoreFile = projectRootFolder.resolve(".gitignore")

// -----------------------------------------------------------------------------







// ARCHETYPE-PATH-SRC-MAIN-VARIABLES.GROOVY -----------------------------------

Path srcMainJava = projectRootFolder.resolve("src").resolve("main").resolve("java")
Path srcMainResources = projectRootFolder.resolve("src").resolve("main").resolve("resources")

Path srcMainResourcesProperties = srcMainResources.resolve(properties_folder)

Path srcMainJavaEntityFolder = srcMainJava.resolve(project_package).resolve(entity_folder)
Path srcMainJavaEntityConfigFolder = srcMainJavaEntityFolder.resolve(config_folder)


Path srcMainJavaEntityConfigPropertiesFolder = srcMainJavaEntityConfigFolder.resolve(properties_folder)

// -----------------------------------------------------------------------------






// ARCHETYPE-PATH-SRC-TEST-VARIABLES.GROOVY -----------------------------------

Path srcTestJava = projectRootFolder.resolve("src").resolve("test").resolve("java")
Path srcTestResources = projectRootFolder.resolve("src").resolve("test").resolve("resources")

Path srcTestJavaEntityConfigPropertiesFolder = srcTestJava
        .resolve(entity_folder)
        .resolve(config_folder)
        .resolve(properties_folder)

Path srcTestResourcesPropertiesFolder = srcTestResources.resolve(properties_folder)

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


// -----------------------------------------------------------------------------






// ARCHETYPE-PATH-SECURITY-VARIABLES.GROOVY ------------------------------------
def authentication_folder = "authentication"


Path filePathAppKey = srcMainResources.resolve("appJwt.key")
Path filePathAppPub = srcMainResources.resolve("appJwt.pub")
Path folderJwtPath = srcMainJavaEntityConfigFolder.resolve(authentication_folder)
// -----------------------------------------------------------------------------







// VARIABLES-AUTHENTICATION.GROOVY ---------------------------------------------
def keep_jwt_authentication = "${properties?.enable_jwt_authentication}"

//def project_security_folder = "${properties?.entity_name}".concat(config_folder)
// -----------------------------------------------------------------------------







// VARIABLES-BINDING-PROPERTIES-TESTS.GROOVY -----------------------------------
def keep_binding_properties_tests = "${properties?.enable_binding_properties_tests}"
// -----------------------------------------------------------------------------







// VARIABLES-BINDING-PROPERTIES.GROOVY -----------------------------------------
def keep_binding_properties = "${properties?.enable_binding_properties}"
def keep_map_binding_properties = "${properties?.map_binding_properties}"
def keep_list_binding_properties = "${properties?.list_binding_properties}"
def keep_default_binding_properties = "${properties?.default_binding_properties}"
def mutable_binding_properties_choice = "${properties?.mutable_binding_properties}"
def keep_validated_binding_properties = "${properties?.validated_binding_properties}"
// -----------------------------------------------------------------------------







// VARIABLES-CPANEL.GROOVY -----------------------------------------------------
def keep_cpanel_elk_log_system = "${properties?.enable_api_cpanel_elk_log_system}"
// -----------------------------------------------------------------------------







// VARIABLES-DOCKER.GROOVY -----------------------------------------------------
def keep_docker = "${properties?.enable_docker}"
// -----------------------------------------------------------------------------







// VARIABLES-EXTRA-POM-DEPENDENCIES.GROOVY -------------------------------------
def extra_POM_dependencies = "${properties?.extra_POM_dependencies}"
// -----------------------------------------------------------------------------







// VARIABLES-GITHUB.GROOVY -----------------------------------------------------
// GIT SUPPORT -----------------------------------------------------------------
def keep_git_support= "${properties?.enable_git_support}"
// -----------------------------------------------------------------------------







// VARIABLES-LOGBACK.GROOVY ----------------------------------------------------
def keep_logback = "${properties?.enable_logback}"
def console_type = "${properties?.logback_console_type}"
def keep_appender_smtp = "${properties?.enable_logback_appender_smtp}"
def keep_appender_file = "${properties?.enable_logback_appender_file}"
// -----------------------------------------------------------------------------







// VARIABLES-PROPERTIES-FILETYPE.GROOVY ----------------------------------------
def properties_filetype_choice = "${properties?.properties_filetype}"
// -----------------------------------------------------------------------------







// VARIABLES-SRC-TEST-JAVA.GROOVY ----------------------------------------------
def keep_src_test_java = "${properties?.enable_folder_src_test_java}"
// -----------------------------------------------------------------------------






// SNIPPET-AUTHENTICATION.GROOVY
if (keep_jwt_authentication == 'false') {

    log_text += "\nJwt_authentication_folder_path:\n $folderJwtPath\n"
    deleteFolder(folderJwtPath.toFile())

    log_text.concat("\nsrc_main_resources_app_key:\n $filePathAppKey\n")
    deleteFile(filePathAppKey.toFile())

    log_text.concat("\nsrc_main_resources_app_pub:\n $filePathAppPub\n")
    deleteFile(filePathAppPub.toFile())
}
// -----------------------------------------------------------------------------







// SNIPPET-BINDING-PROPERTIES-TEST.GROOVY --------------------------------------
if (keep_binding_properties_tests == 'false') {

    log_text += "\nFOLDER_BINDING_PROPERTIES_TEST:\n $srcTestJavaEntityConfigPropertiesFolder\n"
    deleteFolder(srcTestJavaEntityConfigPropertiesFolder.toFile())

    deleteFolder(srcTestResourcesPropertiesFolder.toFile())
    log_text += "\nBINDING_PROPERTY_RESOURCES_TEST:\n $srcTestResourcesPropertiesFolder\n"
}

if (keep_binding_properties_tests == 'true' && keep_default_binding_properties == 'false') {

    List<String> filesToDelete =
            [
                    "ApplicationPropertiesRecordTest.java",
                    "ApplicationPropertiesRecordValidationTest.java",
                    "ApplicationPropertiesTest.java",
                    "ApplicationPropertiesValidationTest.java"
            ]

    deleteFilesFromListFromFolder(srcTestJavaEntityConfigPropertiesFolder, filesToDelete)
    log_text += "\nDEFAULT_BINDING_PROPERTY_TEST:\n " +
            filesToDelete.toString() + "\n $srcTestJavaEntityConfigPropertiesFolder\n"

}

if (keep_binding_properties_tests == 'true' && keep_list_binding_properties == 'false') {

    List<String> filesToDelete =
            [
                    "ListPropertiesTest.java",
                    "ListPropertiesImmutableTest.java",
                    "ListPropertiesRecordTest.java"
            ]

    deleteFilesFromListFromFolder(srcTestJavaEntityConfigPropertiesFolder, filesToDelete)
    log_text += "\nLIST_BINDING_PROPERTY_TEST:\n " +
            filesToDelete.toString() + "\n $srcTestJavaEntityConfigPropertiesFolder\n"

    List<String> filesToDeleteResources =
            [
                    "application_list_test.properties",
                    "application_list_test.yml"
            ]

    deleteFilesFromListFromFolder(srcTestResourcesPropertiesFolder, filesToDeleteResources)
    log_text += "\nLIST_BINDING_PROPERTY_RESOURCES:\n " +
            filesToDeleteResources.toString() + "\n $srcTestResourcesPropertiesFolder\n"
}

if (keep_binding_properties_tests == 'true' && keep_map_binding_properties == 'false') {

    List<String> filesToDelete =
            [
                    "MapPropertiesTest.java",
                    "MapPropertiesImmutableTest.java",
                    "MapPropertiesRecordTest.java"
            ]
    deleteFilesFromListFromFolder(srcTestJavaEntityConfigPropertiesFolder, filesToDelete)
    log_text += "\nLIST_BINDING_PROPERTY_TEST:\n " +
            filesToDelete.toString() + "\n $srcTestJavaEntityConfigPropertiesFolder\n"

    List<String> filesToDeleteResources =
            [
                    "application_map_test.properties",
                    "application_map_test.yml"
            ]

    deleteFilesFromListFromFolder(srcTestResourcesPropertiesFolder, filesToDeleteResources)
    log_text += "\nLIST_BINDING_PROPERTY_RESOURCES:\n " +
            filesToDeleteResources.toString() + "\n $srcTestResourcesPropertiesFolder\n"
}

if (keep_binding_properties == 'true' && mutable_binding_properties_choice == '1') {

    List<String> filesToDelete =
            [
                    "ApplicationPropertiesTest.java",
                    "ApplicationPropertiesValidationTest.java",
                    "ListPropertiesTest.java",
                    "MapPropertiesTest.java"
            ]
    deleteFilesFromListFromFolder(srcTestJavaEntityConfigPropertiesFolder, filesToDelete)

    log_text += "\nIMMUTABLE_BINDING_PROPERTY_TESTE:\n " +
            filesToDelete.toString() + "\n $srcTestJavaEntityConfigPropertiesFolder\n"
}

if (keep_binding_properties == 'true' && mutable_binding_properties_choice == '2') {

    List<String> filesToDelete =
            [
                    "ApplicationPropertiesRecordValidationTest.java",
                    "ApplicationPropertiesRecordTest.java",
                    "ListPropertiesImmutableTest.java",
                    "ListPropertiesRecordTest.java",
                    "MapPropertiesImmutableTest.java",
                    "MapPropertiesRecordTest.java"
            ]
    deleteFilesFromListFromFolder(srcTestJavaEntityConfigPropertiesFolder, filesToDelete)

    log_text += "\nMUTABLE_BINDING_PROPERTY_TEST:\n " +
            filesToDelete.toString() + "\n $srcTestJavaEntityConfigPropertiesFolder\n"
}

if (keep_binding_properties_tests == 'true' && keep_validated_binding_properties == 'false') {

    List<String> filesToDelete =
            [
                    "ApplicationPropertiesRecordValidationTest.java",
                    "ApplicationPropertiesValidationTest.java"
            ]
    deleteFilesFromListFromFolder(srcTestJavaEntityConfigPropertiesFolder, filesToDelete)

    log_text += "\nVALIDATED_BINDING_PROPERTY_TEST:\n " +
            filesToDelete.toString() + "\n $srcTestJavaEntityConfigPropertiesFolder\n"
}
// -----------------------------------------------------------------------------







// SNIPPET-BINDING-PROPERTIES.GROOVY
if (keep_binding_properties == 'false') {

    deleteFolder(srcMainJavaEntityConfigPropertiesFolder.toFile())
    log_text += "\nFOLDER_BINDING_PROPERTIES:\n $srcMainJavaEntityConfigPropertiesFolder\n"

    deleteFolder(srcMainResourcesProperties.toFile())
    log_text += "\nBINDING_PROPERTY_RESOURCES:\n $srcMainResourcesProperties\n"
}

if (keep_binding_properties == 'true' && keep_default_binding_properties == 'false') {

    List<String> filesToDelete =
            [
                    "ApplicationProperties.java",
                    "ApplicationPropertiesRecord.java"
            ]
    deleteFilesFromListFromFolder(srcMainJavaEntityConfigPropertiesFolder, filesToDelete)

    log_text += "\nDEFAULT_BINDING_PROPERTY:\n " +
            filesToDelete.toString() + "\n $srcMainJavaEntityConfigPropertiesFolder\n"
}

if (keep_binding_properties == 'true' && keep_list_binding_properties == 'false') {

    List<String> filesToDelete =
            [
                    "ListObjects.java",
                    "ListProperties.java",
                    "ListPropertiesImmutable.java",
                    "ListPropertiesRecord.java"
            ]
    deleteFilesFromListFromFolder(srcMainJavaEntityConfigPropertiesFolder, filesToDelete)

    log_text += "\nLIST_BINDING_PROPERTY:\n " +
            filesToDelete.toString() + "\n $srcMainJavaEntityConfigPropertiesFolder\n"


    List<String> filesToDeleteResources =
            [
                    "application_list.properties",
                    "application_list.yml"
            ]
    deleteFilesFromListFromFolder(srcMainResourcesProperties, filesToDeleteResources)

    log_text += "\nLIST_BINDING_PROPERTY_RESOURCES:\n " +
            filesToDeleteResources.toString() + "\n $srcMainResourcesProperties\n"
}

if (keep_binding_properties == 'true' && keep_map_binding_properties == 'false') {

    List<String> filesToDelete =
            [
                    "MapItem.java",
                    "MapProperties.java",
                    "MapPropertiesImmutable.java",
                    "MapPropertiesRecord.java"
            ]
    deleteFilesFromListFromFolder(srcMainJavaEntityConfigPropertiesFolder, filesToDelete)

    log_text += "\nMAP_BINDING_PROPERTY:\n " +
            filesToDelete.toString() + "\n $srcMainJavaEntityConfigPropertiesFolder\n"

    List<String> filesToDeleteResources =
            [
                    "application_map.properties",
                    "application_map.yml"
            ]
    deleteFilesFromListFromFolder(srcMainResourcesProperties, filesToDeleteResources)

    log_text += "\nMAP_BINDING_PROPERTY_RESOURCES:\n " +
            filesToDeleteResources.toString() + "\n $srcMainResourcesProperties\n"
}

if (keep_binding_properties == 'true' && mutable_binding_properties_choice == '1') {

    List<String> filesToDelete =
            [
                    "ApplicationProperties.java",
                    "ListProperties.java",
                    "MapProperties.java"
            ]
    deleteFilesFromListFromFolder(srcMainJavaEntityConfigPropertiesFolder, filesToDelete)

    log_text += "\nIMMUTABLE_BINDING_PROPERTY:\n " +
            filesToDelete.toString() + "\n $srcMainJavaEntityConfigPropertiesFolder\n"

}

if (keep_binding_properties == 'true' && mutable_binding_properties_choice == '2') {

    List<String> filesToDelete =
            [
                    "ApplicationPropertiesRecord.java",
                    "ListPropertiesImmutable.java",
                    "ListPropertiesRecord.java",
                    "MapPropertiesImmutable.java",
                    "MapPropertiesRecord.java"
            ]
    deleteFilesFromListFromFolder(srcMainJavaEntityConfigPropertiesFolder, filesToDelete)

    log_text += "\nMUTABLE_BINDING_PROPERTY:\n " +
            filesToDelete.toString() + "\n $srcMainJavaEntityConfigPropertiesFolder\n"

}

// -----------------------------------------------------------------------------






// SNIPPET-CPANEL.GROOVY
if (keep_logback == 'true' && keep_cpanel_elk_log_system == 'false') {


    log_text += "\nAppenders-tcp-logstash.xml -> resources -> logback-spring-config:\n $logbackAppenderLogstashPathFile\n"
    deleteFile(logbackAppenderLogstashPathFile.toFile())


//    deleteElkLogSystemStructure(log_text)
    log_text += "\nFOLDER_ELK:\n $rootELKFolder\n"
    deleteFolder(rootELKFolder.toFile())

    Path dockerElkComposePath =
            rootDockerFolder
                    .resolve("elk-compose.yml")

    log_text += "\nelk-compose.yml -> docker:\n $dockerElkComposePath\n"
    deleteFile(dockerElkComposePath.toFile())
}
// -----------------------------------------------------------------------------







// SNIPPET-DOCKER.GROOVY
if (keep_docker == 'false') {
    log_text += "\nFOLDER_DOCKER:\n $rootDockerFolder\n"
    deleteFolder(rootDockerFolder.toFile())
}


// -----------------------------------------------------------------------------






// SNIPPET-EXTRA-POM-DEPENDENCIES.GROOVY
if (extra_POM_dependencies.length() > 0 && extra_POM_dependencies.toString() != 'false') {

    def additional_dependencies_splitter = ','
    def dependency_internal_coordinates_splitter = ':'

    // REGEX will avoid any 'SPACE' in the input storaged in 'extra_POM_dependencies variable'
    // JUST IN CASE, the code below will be executed
    extra_POM_dependencies = extra_POM_dependencies.trim()
    extra_POM_dependencies = extra_POM_dependencies.replaceAll("\\s+", "").trim()

    String[] dependencies = extra_POM_dependencies.split(additional_dependencies_splitter)

    Path pomFilePath =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'pom.xml')

    log_text += "\nPOM.XML"
    log_text += "\n - Location: $pomFilePath"
    log_text += "\n - Added Dependencies: $dependencies\n"

    if (pomFilePath.toFile().isFile()) {

        def sb = new StringBuilder()

        Scanner sc

        try {
            sc = new Scanner(pomFilePath)
            String currentLine = ''
            String lastLine = ''

            while (sc.hasNext()) {

                // log_text += "\n - While: $lastLine\n"
                lastLine = currentLine
                currentLine = sc.nextLine()

                if (currentLine.trim().length() == 0) continue

                if (currentLine.contains("</dependency>") ||
                        currentLine.contains("</modelVersion>") ||
                        currentLine.contains("</parent>") ||
                        currentLine.contains("</description>") ||
                        currentLine.contains("</properties>") ||
                        currentLine.contains("</dependencies>") ||
                        currentLine.contains("</build>") ||
                        currentLine.contains("</dependencyManagement>")) sb.append(currentLine).append("\n\n")
                else sb.append(currentLine).append("\n")

                // log_text += sb.toString()
                // Add dependencies
                // if (lastLine.contains("</dependencyManagement>") && currentLine.contains("<dependencies>")) {
                if (currentLine.contains("<dependencies>")) {

                    for (def dependency : dependencies) {

                        def splitter = dependency_internal_coordinates_splitter

                        String[] depInfo = dependency.split(splitter)
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
        // PrintWriter pw = new PrintWriter(pomFilePath.toFile())
        PrintWriter pw = new PrintWriter(System.err)
        pw.close()

        // Write the modified parent pom
        PrintWriter writer = new PrintWriter(pomFilePath.toFile())
        writer.write(sb.toString())
        writer.close()
    }
}
// -----------------------------------------------------------------------------







// SNIPPET-GITHUB.GROOVY
if (keep_git_support == 'false') {

    log_text += "\\nRemoving FOLDER_.GIT:\\n $rootGitFolder\n"
    deleteFolder(rootGitFolder.toFile())

    log_text += "\nRemoving 'GIT_IGNORE FILE' -> Root Folder:\n $rootGitIgnoreFile\n"
    deleteFile(rootGitIgnoreFile.toFile())
}


if (keep_git_support == 'true') {

    log_text += "\\nRenaming FOLDER_.GIT:\\n $rootGitFolder\n"
    renameFolder(rootGitFolder.toFile(), ".git")

    log_text += "\nRenaming 'GIT_IGNORE FILE' to: .gitignore\n Root Folder: $rootGitIgnoreFile\n"
    renameFile(rootGitIgnoreFile.toFile(), ".gitignore")
}

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
// -----------------------------------------------------------------------------







// SNIPPET-PROPERTIES-FILETYPE.GROOVY
// properties_filetype_choice:        1 - yml          2 - properties
def deleteFileType = (properties_filetype_choice == '1') ? '.yml' : '.properties'
def fileTypeMessage = (properties_filetype_choice == '1') ? 'Delete All YML-FILES' : 'Delete All PROPERTIES-FILES'
log_text += "\nBINDING_PROPERTIES:\n=> Message: " +  fileTypeMessage


log_text += "\n" + fileTypeMessage + " -> main-resources:\n $srcMainResources\n"
deleteFilesByExtensionRecursivelyFromFolder(srcMainResources, deleteFileType)


log_text += "\n" + fileTypeMessage + " -> test-resources:\n $srcTestResources\n"
deleteFilesByExtensionRecursivelyFromFolder(srcTestResources, deleteFileType)
// -----------------------------------------------------------------------------







// SNIPPET-SRC-TEST-JAVA.GROOVY
if (keep_src_test_java == 'false') {

    log_text += "\nFOLDER_SRC_TEST_JAVA:\n $srcTestJava\n"
    deleteFolder(srcTestJava.toFile())


    log_text += "\nFOLDER_SRC_TEST_RESOURCES:\n $srcTestResources\n"
    deleteFolder(srcTestResources.toFile())
}
// -----------------------------------------------------------------------------






// ARCHETYPE-FOOTER.GROOVY -----------------------------------------------------

// ARCHETYPE-POST-GENERATE-LOG -------------------------------------------------
if (show_pos_processor_log == 'true') printf "\n$log_text $log_text_footer"
// -----------------------------------------------------------------------------






// ARCHETYPE-FILE-FUNCTIONS.GROOVY ---------------------------------------------

def deleteFile(File file) {
    if (file.isFile()) file.delete()
}


def renameFile(File file, String newName) {

    println "File: $file"

    def newFile = new File(file.getParent(), newName)

    println "newFile: $newFile"

    if (!file.exists() || !file.isFile()) {
        println "Error: File $file invalid or inexistent."
        return
    }

    if (newFile.exists()) {
        println "Error: File $newName already exists in the path"
        return
    }

    if (file.renameTo(newFile)) {
        println "File renamed: $file -> $newName"
    } else {
        println "Error: Unable to rename $file to $newFile"
    }
}

// -----------------------------------------------------------------------------






// ARCHETYPE-FOLDER-FUNCTIONS.GROOVY -------------------------------------------
def deleteFolder(File pathToFolder) {
    if (pathToFolder.isDirectory()) {

        pathToFolder.listFiles()
                .each {
                    file -> if (file.isDirectory()) deleteFolder(file) else file.delete()
                }
    }
    pathToFolder.delete()
}


//def deleteElkLogSystemStructure(log_text) {
//
//    log_text += "\nFOLDER_ELK:\n $rootELKFolder\n"
//    deleteFolder(rootELKFolder.toFile())
//
//    Path dockerElkComposePath =
//            rootDockerFolder
//                    .resolve("elk-compose.yml")
//
//    log_text += "\nelk-compose.yml -> docker:\n $dockerElkComposePath\n"
//    deleteFile(dockerElkComposePath.toFile())
//}


def renameFolder(File pathToFolder, String newName) {

    if (pathToFolder.isDirectory()) {
        def newFolderPath = pathToFolder.parentFile.toPath().resolve(newName)
        if (newFolderPath.toFile().exists()) {
            println "Erro: Pasta com o nome $newName já existe."
            return
        }
        pathToFolder.renameTo(newFolderPath.toFile())
        println "Pasta renomeada com sucesso: $pathToFolder -> $newName"
    } else {
        println "Erro: $pathToFolder não é um diretório."
    }
}


/**
 * Deleta recursivamente todos os arquivos com uma determinada extensão
 * @param directory Path do diretório
 * @param extension Extensão dos arquivos (exemplo: ".yml", ".properties")
 */
def deleteFilesByExtensionRecursivelyFromFolder(Path pathToFiles, String extension) {

    def currentFolder = pathToFiles.toFile()

    if (!currentFolder.exists()) return

    // Remove from 'Current Directory'
    currentFolder.eachFile {
        file -> if (file.isFile() && file.name.endsWith(extension)) file.delete()
    }

    // Remove from 'SubDirectories'
    currentFolder.eachDir {
        subDir -> deleteFilesByExtensionRecursivelyFromFolder(subDir.toPath(), extension)
    }
}


def deleteFilesFromListFromFolder(Path pathToFiles, List<String> files) {

    println "Entrou na funcao deleteFilesFromListFromFolder"

    files.removeIf { it == null || it.trim() == "" }

    // Valida se diretório existe
    if (!pathToFiles.toFile().isDirectory()) {
        println "Pasta não encontrada: $pathToFiles"
        return
    }

    // Processa cada arquivo
    files.each {
        fileName ->
            try {
                def fileToDelete = pathToFiles.resolve(fileName).toFile()

                if (fileToDelete.exists()) {

                    fileToDelete.delete()
                    println "File Found: $fileName"
                } else {

                    println "File Not Found: $fileName em ${pathToFiles.toAbsolutePath()}"
                }
            } catch (Exception e) {
                println "Erro ao deletar $fileName: ${e.message}"
            }
    }

    println "Saiu da funcao deleteFilesFromListFromFolder"
}
