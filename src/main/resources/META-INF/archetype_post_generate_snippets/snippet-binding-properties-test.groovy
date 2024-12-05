import java.nio.file.Path
import java.nio.file.Paths












// SNIPPET-BINDING-PROPERTIES-TEST.GROOVY --------------------------------------

if (keep_binding_properties == 'false') {

    Path srcTestJava_bindingPropertiesTestFolder_path =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'test', 'java',
                    'binding_properties_test')
    log_text += "\nFOLDER_BINDING_PROPERTIES_TEST:\n $srcTestJava_bindingPropertiesTestFolder_path\n"
    deleteFolder(srcTestJava_bindingPropertiesTestFolder_path.toFile())
}



if (keep_binding_properties == 'true' && keep_default_binding_properties == 'false') {

    Path binding_properties_path =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'test', 'java',
                    'binding_properties_test'
            )
    List<String> filesToDelete =
            [
                    "ApplicationPropertiesTest.java"
            ]
    deleteFilesFromList(binding_properties_path, filesToDelete)
    log_text += "\nDEFAULT_BINDING_PROPERTY_TEST - " + filesToDelete.toString() + "\\n " + "$binding_properties_path\n"

}



if (keep_binding_properties == 'true' && keep_list_binding_properties == 'false') {

    Path binding_properties_path =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'test', 'java',
                    'binding_properties_test')
    List<String> filesToDelete =
            [
                    "ListPropertiesClassTest.java",
                    "ListPropertiesImmutableClassTest.java",
                    "ListPropertiesRecordTest.java"
            ]
    deleteFilesFromList(binding_properties_path, filesToDelete)
    log_text += "\nLIST_BINDING_PROPERTY_TEST - " + filesToDelete.toString() + "\\n " + "$binding_properties_path\n"
}



if (keep_binding_properties == 'true' && keep_map_binding_properties == 'false') {

    Path binding_properties_path =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'test', 'java',
                    'binding_properties_test')
    List<String> filesToDelete =
            [
                    "MapPropertiesClassTest.java",
                    "MapPropertiesImmutableClassTest.java",
                    "MapPropertiesRecordTest.java"
            ]
    deleteFilesFromList(binding_properties_path, filesToDelete)
    log_text += "\nMAP_BINDING_PROPERTY_TEST - " + filesToDelete.toString() + "\\n " + "$binding_properties_path\n"
}