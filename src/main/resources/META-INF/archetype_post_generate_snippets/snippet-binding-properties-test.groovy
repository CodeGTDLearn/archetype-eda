import java.nio.file.Path
import java.nio.file.Paths















// SNIPPET-BINDING-PROPERTIES-TEST.GROOVY --------------------------------------
if (keep_binding_properties_tests == 'false') {

    Path srcTestJava_bindingPropertiesTestFolder_path =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'test', 'java',
                    project_package,
                    entity_folder,
                    config_folder,
                    'properties')
    log_text += "\nFOLDER_BINDING_PROPERTIES_TEST:\n $srcTestJava_bindingPropertiesTestFolder_path\n"
    deleteFolder(srcTestJava_bindingPropertiesTestFolder_path.toFile())

    Path binding_properties_resources_path =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'test', 'resources',
                    "properties_test"
            )
    deleteFolder(binding_properties_resources_path.toFile())
    log_text += "\nBINDING_PROPERTY_RESOURCES - " + "$binding_properties_resources_path\n"
}



// DEFAULTs --------------------------------------------------------------------
if (keep_binding_properties_tests == 'true' && keep_default_binding_properties == 'false') {

    Path binding_properties_path =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'test', 'java',
                    project_package,
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
                    project_package,
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
                    "properties_test"
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
                    project_package,
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
                    "properties_test"
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



// MUTABLES --------------------------------------------------------------------
if (keep_binding_properties_tests == 'true' && keep_mutable_binding_properties == 'false') {

    Path binding_properties_path =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'test', 'java',
                    project_package,
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
                    project_package,
                    entity_folder,
                    config_folder,
                    'properties_test')
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