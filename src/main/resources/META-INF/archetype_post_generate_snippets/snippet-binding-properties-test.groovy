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
    log_text += "\nLIST_BINDING_PROPERTY_TEST - " + filesToDelete.toString() + "\\n " + "$binding_properties_path\n"
}


// MAPs ------------------------------------------------------------------------
if (keep_binding_properties_tests == 'true' && keep_map_binding_properties == 'false') {

    Path binding_properties_path =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'test', 'java',
                    'binding_properties_test')
    List<String> filesToDelete =
            [
                    "MapPropertiesTest.java",
                    "MapPropertiesImmutableTest.java",
                    "MapPropertiesRecordTest.java"
            ]
    deleteFilesFromList(binding_properties_path, filesToDelete)
    log_text += "\nMAP_BINDING_PROPERTY_TEST - " + filesToDelete.toString() + "\\n " + "$binding_properties_path\n"
}


// IMMUTABLES ------------------------------------------------------------------
if (keep_binding_properties_tests == 'true' && chosen_immutable_binding_properties == '3') {

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
                    "ListPropertiesRecordTest.java",
                    "ListPropertiesImmutableTest.java",
                    "MapPropertiesRecordTest.java",
                    "MapPropertiesImmutableTest.java"
            ]
    deleteFilesFromList(binding_properties_path, filesToDelete)
    log_text += "\nDEFAULT_BINDING_PROPERTY_TEST - " + filesToDelete.toString() + "\\n " + "$binding_properties_path\n"

}

// se quiser manter Imutaveis - deleta mutaveis
// se quiser manter mutaveis - deleta Imutaveis
// se quiser manter Imutaveis+mutaveis - nao deleta ninguem