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