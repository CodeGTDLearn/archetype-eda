import java.nio.file.Path
import java.nio.file.Paths







// ARCHETYPE-HEADER.GROOVY -----------------------------------------------------

Properties properties = request.properties







// -----------------------------------------------------------------------------

// ARCHETYPE-GLOBAL-VARIABLES.GROOVY -------------------------------------------

def entity_folder = "${properties?.entity_name}"
def entity_name = "${properties?.entity_name}"

def config_folder = "config"

def project_package = "${properties?.package}".replace('.', '/')

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







// VARIABLE-AUTHENTICATION.GROOVY ----------------------------------------------

def keep_jwt_authentication = "${properties?.enable_jwt_authentication}"

def authentication_folder = "authentication"
//def project_security_folder = "${properties?.entity_name}".concat(config_folder)







// VARIABLE-BINDING-PROPERTIES-TESTS.GROOVY ------------------------------------

def keep_binding_properties_tests = "${properties?.enable_binding_properties_tests}"







// VARIABLE-BINDING-PROPERTIES.GROOVY ------------------------------------------

def keep_binding_properties = "${properties?.enable_binding_properties}"
def keep_map_binding_properties = "${properties?.map_binding_properties}"
def keep_list_binding_properties = "${properties?.list_binding_properties}"
def keep_default_binding_properties = "${properties?.default_binding_properties}"
def mutable_binding_properties_choice = "${properties?.mutable_binding_properties}"
def keep_validated_binding_properties = "${properties?.validated_binding_properties}"







// VARIABLE-CPANEL.GROOVY ------------------------------------------------------

def keep_cpanel_elk_log_system = "${properties?.enable_api_cpanel_elk_log_system}"







// VARIABLE-DOCKER.GROOVY ------------------------------------------------------

def keep_docker = "${properties?.enable_docker}"







// VARIABLE-EXTRA-POM-DEPENDENCIES.GROOVY --------------------------------------

def extra_POM_dependencies = "${properties?.extra_POM_dependencies}"







// VARIABLE-LOGBACK.GROOVY -----------------------------------------------------

def keep_logback = "${properties?.enable_logback}"
def console_type = "${properties?.logback_console_type}"
def keep_appender_smtp = "${properties?.enable_logback_appender_smtp}"
def keep_appender_file = "${properties?.enable_logback_appender_file}"







// VARIABLE-PROPERTIES-FILETYPE.GROOVY -----------------------------------------

def properties_filetype_choice = "${properties?.properties_filetype}"







// VARIABLE-SRC-TEST-JAVA.GROOVY -----------------------------------------------

def keep_src_test_java = "${properties?.enable_folder_src_test_java}"







// SNIPPET-AUTHENTICATION.GROOVY -----------------------------------------------

if (keep_jwt_authentication == 'false') {

    Path folderJwtPath =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'java',
                    project_package,
                    entity_folder,
                    config_folder,
                    authentication_folder)
    log_text += "\nJwt_authentication_folder_path:\n $folderJwtPath\n"
    deleteFolder(folderJwtPath.toFile())


    Path filePathAppKey =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'resources',
                    'appJwt.key')
    log_text.concat("\nsrc_main_resources_app_key:\n $filePathAppKey\n")
    deleteFile(filePathAppKey.toFile())


    Path filePathAppPub =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'resources',
                    'appJwt.pub')
    log_text.concat("\nsrc_main_resources_app_pub:\n $filePathAppPub\n")
    deleteFile(filePathAppPub.toFile())
}










// SNIPPET-BINDING-PROPERTIES-TEST.GROOVY --------------------------------------
if (keep_binding_properties_tests == 'false') {

    Path srcTestJava_bindingPropertiesTestFolder_path =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'test', 'java',
                    entity_folder,
                    config_folder,
                    'properties')
    log_text += "\nFOLDER_BINDING_PROPERTIES_TEST:\n $srcTestJava_bindingPropertiesTestFolder_path\n"
    deleteFolder(srcTestJava_bindingPropertiesTestFolder_path.toFile())

    Path binding_properties_resources_path =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'test', 'resources',
                    "properties"
            )
    deleteFolder(binding_properties_resources_path.toFile())
    log_text += "\nBINDING_PROPERTY_RESOURCES_TEST - " + "$binding_properties_resources_path\n"
}



// DEFAULTs --------------------------------------------------------------------
if (keep_binding_properties_tests == 'true' && keep_default_binding_properties == 'false') {

    Path binding_properties_path =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'test', 'java',
                    entity_folder,
                    config_folder,
                    'properties')

        List<String> filesToDelete =
            [
                    "ApplicationPropertiesRecordTest.java",
                    "ApplicationPropertiesRecordValidationTest.java",
                    "ApplicationPropertiesTest.java",
                    "ApplicationPropertiesValidationTest.java"
            ]
    deleteFilesFromList(binding_properties_path, filesToDelete)
    log_text += "\nDEFAULT_BINDING_PROPERTY_TEST - " + filesToDelete.toString() + "\\n " + "$binding_properties_path\n"

}



// LISTs -----------------------------------------------------------------------
if (keep_binding_properties_tests == 'true' && keep_list_binding_properties == 'false') {

    Path binding_properties_path =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'test', 'java',
                    entity_folder,
                    config_folder,
                    'properties')
    List<String> filesToDelete =
            [
                    "ListPropertiesTest.java",
                    "ListPropertiesImmutableTest.java",
                    "ListPropertiesRecordTest.java"
            ]
    deleteFilesFromList(binding_properties_path, filesToDelete)
    log_text += "\nLIST_BINDING_PROPERTY_TEST - " +
            filesToDelete.toString() + "\\n " +
            "$binding_properties_path\n"

    Path binding_properties_resources_path =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'test', 'resources',
                    "properties"
            )
    List<String> filesToDeleteResources =
            [
                    "application_list_test.properties",
                    "application_list_test.yml"
            ]
    deleteFilesFromList(binding_properties_resources_path, filesToDeleteResources)
    log_text += "\nLIST_BINDING_PROPERTY_RESOURCES - " +
            filesToDeleteResources.toString() + "\\n " +
            "$binding_properties_resources_path\n"
}



// MAPs ------------------------------------------------------------------------
if (keep_binding_properties_tests == 'true' && keep_map_binding_properties == 'false') {

    Path binding_properties_path =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'test', 'java',
                    entity_folder,
                    config_folder,
                    'properties')
    List<String> filesToDelete =
            [
                    "MapPropertiesTest.java",
                    "MapPropertiesImmutableTest.java",
                    "MapPropertiesRecordTest.java"
            ]
    deleteFilesFromList(binding_properties_path, filesToDelete)
    log_text += "\nLIST_BINDING_PROPERTY_TEST - " +
            filesToDelete.toString() + "\\n " +
            "$binding_properties_path\n"

    Path binding_properties_resources_path =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'test', 'resources',
                    "properties"
            )
    List<String> filesToDeleteResources =
            [
                    "application_map_test.properties",
                    "application_map_test.yml"
            ]
    deleteFilesFromList(binding_properties_resources_path, filesToDeleteResources)
    log_text += "\nLIST_BINDING_PROPERTY_RESOURCES - " +
            filesToDeleteResources.toString() + "\\n " +
            "$binding_properties_resources_path\n"
}



// MUTABLEs --------------------------------------------------------------------
// mutable_binding_properties_choice:   1 - Immutable;   2 - Mutable;   3 - Both
if (keep_binding_properties == 'true' && mutable_binding_properties_choice == '1') {

    Path binding_properties_path =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'test', 'java',
                    entity_folder,
                    config_folder,
                    'properties')
    List<String> filesToDelete =
            [
                    "ApplicationPropertiesTest.java",
                    "ListPropertiesTest.java",
                    "MapPropertiesTest.java"
            ]
    deleteFilesFromList(binding_properties_path, filesToDelete)

    log_text += "\nIMMUTABLE_BINDING_PROPERTY_TESTE - " +
            filesToDelete.toString() + "\\n " +
            "$binding_properties_path\n"

}

if (keep_binding_properties == 'true' && mutable_binding_properties_choice == '2') {

    Path binding_properties_path =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'test', 'java',
                    entity_folder,
                    config_folder,
                    'properties')
    List<String> filesToDelete =
            [
                    "ApplicationPropertiesRecordTest.java",
                    "ListPropertiesImmutableTest.java",
                    "ListPropertiesRecordTest.java",
                    "MapPropertiesImmutableTest.java",
                    "MapPropertiesRecordTest.java"
            ]
    deleteFilesFromList(binding_properties_path, filesToDelete)

    log_text += "\nMUTABLE_BINDING_PROPERTY_TEST - " +
            filesToDelete.toString() + "\\n " +
            "$binding_properties_path\n"

}


// VALIDATION ------------------------------------------------------------------
if (keep_binding_properties_tests == 'true' && keep_validated_binding_properties == 'false') {

    Path binding_properties_path =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'test', 'java',
                    entity_folder,
                    config_folder,
                    'properties')
    List<String> filesToDelete =
            [
                    "ApplicationPropertiesRecordValidationTest.java",
                    "ApplicationPropertiesValidationTest.java"
            ]
    deleteFilesFromList(binding_properties_path, filesToDelete)

    log_text += "\nVALIDATED_BINDING_PROPERTY_TEST - " +
            filesToDelete.toString() + "\\n " +
            "$binding_properties_path\n"

}







// SNIPPET-BINDING-PROPERTIES.GROOVY -------------------------------------------
if (keep_binding_properties == 'false') {

    Path srcMainJava_bindingPropertiesFolder_path =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'java',
                    project_package,
                    entity_folder,
                    config_folder,
                    "properties")
    log_text += "\nFOLDER_BINDING_PROPERTIES:\n $srcMainJava_bindingPropertiesFolder_path\n"
    deleteFolder(srcMainJava_bindingPropertiesFolder_path.toFile())


    Path binding_properties_resources_path =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'resources',
                    "properties"
            )
    deleteFolder(binding_properties_resources_path.toFile())
    log_text += "\nBINDING_PROPERTY_RESOURCES - " + "$binding_properties_resources_path\n"
}



// DEFAULTs --------------------------------------------------------------------
if (keep_binding_properties == 'true' && keep_default_binding_properties == 'false') {

    Path binding_properties_path =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'java',
                    project_package,
                    entity_folder,
                    config_folder,
                    "properties"
            )

    List<String> filesToDelete =
            [
                    "ApplicationProperties.java",
                    "ApplicationPropertiesRecord.java"
            ]
    deleteFilesFromList(binding_properties_path, filesToDelete)

    log_text += "\nDEFAULT_BINDING_PROPERTY - " +
            filesToDelete.toString() + "\\n " +
            "$binding_properties_path\n"

}



// LISTs -----------------------------------------------------------------------
if (keep_binding_properties == 'true' && keep_list_binding_properties == 'false') {

    Path binding_properties_path =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'java',
                    project_package,
                    entity_folder,
                    config_folder,
                    "properties"
            )
    List<String> filesToDelete =
            [
                    "ListObjects.java",
                    "ListProperties.java",
                    "ListPropertiesImmutable.java",
                    "ListPropertiesRecord.java"
            ]
    deleteFilesFromList(binding_properties_path, filesToDelete)

    log_text += "\nLIST_BINDING_PROPERTY - " +
            filesToDelete.toString() + "\\n " +
            "$binding_properties_path\n"




    Path binding_properties_resources_path =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'resources',
                    "properties"
            )
    List<String> filesToDeleteResources =
            [
                    "application_list.properties",
                    "application_list.yml"
            ]
    deleteFilesFromList(binding_properties_resources_path, filesToDeleteResources)

    log_text += "\nLIST_BINDING_PROPERTY_RESOURCES - " +
            filesToDeleteResources.toString() + "\\n " +
            "$binding_properties_resources_path\n"
}



// MAPs ------------------------------------------------------------------------
if (keep_binding_properties == 'true' && keep_map_binding_properties == 'false') {

    Path binding_properties_path =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'java',
                    project_package,
                    entity_folder,
                    config_folder,
                    "properties"
            )
    List<String> filesToDelete =
            [
                    "MapItem.java",
                    "MapProperties.java",
                    "MapPropertiesImmutable.java",
                    "MapPropertiesRecord.java"
            ]
    deleteFilesFromList(binding_properties_path, filesToDelete)

    log_text += "\nMAP_BINDING_PROPERTY - " +
            filesToDelete.toString() + "\\n " +
            "$binding_properties_path\n"





    Path binding_properties_resources_path =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'resources',
                    "properties"
            )
    List<String> filesToDeleteResources =
            [
                    "application_map.properties",
                    "application_map.yml"
            ]
    deleteFilesFromList(binding_properties_resources_path, filesToDeleteResources)

    log_text += "\nMAP_BINDING_PROPERTY_RESOURCES - " +
            filesToDeleteResources.toString() + "\\n " +
            "$binding_properties_resources_path\n"
}



// MUTABLEs --------------------------------------------------------------------
// mutable_binding_properties_choice:   1 - Immutable;   2 - Mutable;   3 - Both
if (keep_binding_properties == 'true' && mutable_binding_properties_choice == '1') {

    Path binding_properties_path =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'java',
                    project_package,
                    entity_folder,
                    config_folder,
                    'properties')
    List<String> filesToDelete =
            [
                    "ApplicationProperties.java",
                    "ListProperties.java",
                    "MapProperties.java"
            ]
    deleteFilesFromList(binding_properties_path, filesToDelete)

    log_text += "\nIMMUTABLE_BINDING_PROPERTY - " +
            filesToDelete.toString() + "\\n " +
            "$binding_properties_path\n"

}

if (keep_binding_properties == 'true' && mutable_binding_properties_choice == '2') {

    Path binding_properties_path =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'java',
                    project_package,
                    entity_folder,
                    config_folder,
                    'properties')
    List<String> filesToDelete =
            [
                    "ApplicationPropertiesRecord.java",
                    "ListPropertiesImmutable.java",
                    "ListPropertiesRecord.java",
                    "MapPropertiesImmutable.java",
                    "MapPropertiesRecord.java"
            ]
    deleteFilesFromList(binding_properties_path, filesToDelete)

    log_text += "\nMUTABLE_BINDING_PROPERTY - " +
            filesToDelete.toString() + "\\n " +
            "$binding_properties_path\n"

}







// SNIPPET-CPANEL.GROOVY -------------------------------------------------------

if (keep_logback == 'true' && keep_cpanel_elk_log_system == 'false') {

    Path logstachPath =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'resources', 'logback-spring-config', 'appender-tcp-elk-logstash.xml')
    log_text += "\nAppenders-tcp-logstash.xml -> resources -> logback-spring-config:\n $logstachPath\n"
    deleteFile(logstachPath.toFile())


    deleteElkLogSystemStructure(log_text)
}







// SNIPPET-DOCKER.GROOVY -------------------------------------------------------

if (keep_docker == 'false') {
    Path folderDockerPath =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'docker')
    log_text += "\nFOLDER_DOCKER:\n $folderDockerPath\n"
    deleteFolder(folderDockerPath.toFile())
}







// SNIPPET-EXTRA-POM-DEPENDENCIES.GROOVY ---------------------------------------

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







// SNIPPET-LOGBACK.GROOVY ------------------------------------------------------

if (keep_logback == 'false' ||
        (keep_logback == 'true' &&
                console_type == '1' &&
                keep_appender_smtp == 'false' &&
                keep_appender_file == 'false' &&
                keep_cpanel_elk_log_system == 'false'
        )
) {
    Path logbackSpring_Path =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'resources',
                    'logback-spring.xml')
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
                    'src', 'main', 'resources',
                    'logback-spring-config')
    log_text += "\nFOLDER_LOGBACK-SPRING-CONFIG:\n $logbackSpringConfig_Path\n"
    deleteFolder(logbackSpringConfig_Path.toFile())

    deleteElkLogSystemStructure(log_text)
}




if (keep_logback == 'true' && console_type == '1') {

    Path consoleGranularFile =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'resources',
                    'logback-spring-config',
                    'appender-console-granular.xml')
    log_text += "\nAppenders-console-granular.xml -> resources -> logback-spring-config:\n $consoleGranularFile\n"
    deleteFile(consoleGranularFile.toFile())


    Path granularLayoutConsoleFile =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'resources',
                    'logback-spring-config',
                    'appender-console-layouts-granular.xml')
    log_text += "\nappenders-console-layouts-granular.xml -> resources -> logback-spring-config:\n $granularLayoutConsoleFile\n"
    deleteFile(granularLayoutConsoleFile.toFile())


    Path consoleTabularFile =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'resources',
                    'logback-spring-config',
                    'appender-console-tabular.xml')
    log_text += "\nAppenders-console-tabular.xml -> resources -> logback-spring-config:\n $consoleTabularFile\n"
    deleteFile(consoleTabularFile.toFile())

//        O Arquivo "LOGBACK-SPRING.XML" continua existindo E sendo usado
//        MAS SEM NENHUMA ESTRUTURA de CUSTOM-CONSOLE(tabular ou granular),
//        pois sem estrutura de  CUSTOM-CONSOLE apesar da existencia do "LOGBACK-SPRING.XML"
//        este arquivo nao possui qquer tipo de 'CUSTOM-CONSOLE', assumindo-se assim para fins de CONSOLE,
//        o CONSOLE-DEFAULT-INTERNO do Spring Boot AUTOMATICAMENTE
//        NO CASO DE TESTE("LOGBACK-TEST.XML") o arquivo e removido
//        E O console do Spring Boot sera acionado
    Path logbackTestPath =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'test', 'resources',
                    'logback-test.xml')
    log_text += "\nlogback-test.xml -> test-resources:\n $logbackTestPath\n"
    deleteFile(logbackTestPath.toFile())
}




if (keep_logback == 'true' && console_type == '2') {

    Path consoleGranularFile =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'resources',
                    'logback-spring-config',
                    'appender-console-granular.xml')
    log_text += "\nAppenders-console-granular.xml -> resources -> logback-spring-config:\n $consoleGranularFile\n"
    deleteFile(consoleGranularFile.toFile())


    Path granularLayoutConsoleFile =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'resources',
                    'logback-spring-config',
                    'appender-console-layouts-granular.xml')
    log_text += "\nappender-console-layouts-granular.xml -> resources -> logback-spring-config:\n $granularLayoutConsoleFile\n"
    deleteFile(granularLayoutConsoleFile.toFile())
}




if (keep_logback == 'true' && console_type == '3') {

    Path consoleTabularFile =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'resources',
                    'logback-spring-config',
                    'appender-console-tabular.xml')
    log_text += "\nAppenders-console-tabular.xml -> resources -> logback-spring-config:\n $consoleTabularFile\n"
    deleteFile(consoleTabularFile.toFile())
}




if (keep_logback == 'true' && keep_appender_smtp == 'false') {

    Path stmpPathFile =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'resources',
                    'logback-spring-config',
                    'appender-smtp-email.xml')
    log_text += "\nAppenders-smtp.xml -> resources -> logback-spring-config:\n $stmpPathFile\n"
    deleteFile(stmpPathFile.toFile())
}




if (keep_logback == 'true' && keep_appender_file == 'false') {

    Path rolFilePath =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'resources',
                    'logback-spring-config',
                    'appender-rolfile-file.xml')
    log_text += "\nAppenders-file.xml -> resources -> logback-spring-config:\n $rolFilePath\n"
    deleteFile(rolFilePath.toFile())
}







// SNIPPET-PROPERTIES-FILETYPE.GROOVY ------------------------------------------
// properties_filetype_choice:
// 1 - yml
// 2 - properties
def deleteFileType = (properties_filetype_choice == '1') ? '.yml' : '.properties'
def fileTypeMessage = (properties_filetype_choice == '1') ? 'Delete All YML-FILES' : 'Delete All PROPERTIES-FILES'
log_text += "\nBINDING_PROPERTIES:\n=> Message: " +  fileTypeMessage


Path srcMainJava_resourcesFolder_path =
        Paths.get(request.outputDirectory,
                request.artifactId,
                'src', 'main', 'resources')
log_text += "\n" + fileTypeMessage + " -> main-resources:\n $srcMainJava_resourcesFolder_path\n"
deleteFilesByExtensionRecursively(srcMainJava_resourcesFolder_path, deleteFileType)


Path srcTestJava_resourcesFolder_path =
        Paths.get(request.outputDirectory,
                request.artifactId,
                'src', 'test', 'resources')
log_text += "\n" + fileTypeMessage + " -> test-resources:\n $srcTestJava_resourcesFolder_path\n"
deleteFilesByExtensionRecursively(srcTestJava_resourcesFolder_path, deleteFileType)







// SNIPPET-SRC-TEST-JAVA.GROOVY ------------------------------------------------

if (keep_src_test_java == 'false') {
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







// ARCHETYPE-FOOTER.GROOVY -----------------------------------------------------

// ARCHETYPE-POST-GENERATE-LOG -------------------------------------------------
if (show_pos_processor_log == 'true') printf "\n$log_text $log_text_footer"

// FOOTER ----------------------------------------------------------------------







// ARCHETYPE-FUNCTIONS.GROOVY --------------------------------------------------

// FILE MANAGEMENT FUNCTIONS ---------------------------------------------------
def deleteFile(File file) {
    if (file.isFile()) file.delete()
}


/**
 * Deleta recursivamente todos os arquivos com uma determinada extensão
 * @param directory Path do diretório
 * @param extension Extensão dos arquivos (exemplo: ".yml", ".properties")
 */
def deleteFilesByExtensionRecursively(Path path, String extension) {

    def currentFolder = path.toFile()

    if (!currentFolder.exists()) return

    // Remove from 'Current Directory'
    currentFolder.eachFile {
        file -> if (file.isFile() && file.name.endsWith(extension)) file.delete()
    }

    // Remove from 'SubDirectories'
    currentFolder.eachDir {
        subDir -> deleteFilesByExtensionRecursively(subDir.toPath(), extension)
    }
}


def deleteFilesFromList(Path directory, List<String> files) {

    println "Entrou na funcao deleteFilesFromList"

    files.removeIf { it == null || it.trim() == "" }

    // Valida se diretório existe
    if (!directory.toFile().isDirectory()) {
        println "Diretório não encontrado: $directory"
        return
    }

    // Processa cada arquivo
    files.each {
        fileName ->
            try {
                def fileToDelete = directory.resolve(fileName).toFile()

                if (fileToDelete.exists()) {

                    fileToDelete.delete()
                    println "File Found: $fileName"
                } else {

                    println "File Not Found: $fileName em ${directory.toAbsolutePath()}"
                }
            } catch (Exception e) {
                println "Erro ao deletar $fileName: ${e.message}"
            }
    }

    println "Saiu da funcao deleteFilesFromList"
}


// FOLDER MANAGEMENT FUNCTIONS -------------------------------------------------
def deleteFolder(File dir) {
    if (dir.isDirectory()) {

        dir.listFiles()
                .each {
                    file -> if (file.isDirectory()) deleteFolder(file) else file.delete()
                }
    }
    dir.delete()
}


def deleteElkLogSystemStructure(log_text) {
    Path folderELKPath =
            Paths.get(
                    request.outputDirectory,
                    request.artifactId,
                    'ELK')
    log_text += "\nFOLDER_ELK:\n $folderELKPath\n"
    deleteFolder(folderELKPath.toFile())


    Path dockerElkComposePath =
            Paths.get(
                    request.outputDirectory,
                    request.artifactId,
                    'docker',
                    'elk-compose.yml')
    log_text += "\nelk-compose.xml -> docker:\n $dockerElkComposePath\n"
    deleteFile(dockerElkComposePath.toFile())
}


// FUNCTIONS -------------------------------------------------------------------