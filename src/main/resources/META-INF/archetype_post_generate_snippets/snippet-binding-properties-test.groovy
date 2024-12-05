import java.nio.file.Path
import java.nio.file.Paths










// SNIPPET-BINDING-PROPERTIES-TEST.GROOVY --------------------------------------



if (keep_binding_properties == 'false') {

    Path bindingPropertiesTestPath =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'test', 'java',
                    'binding_properties_test')
    log_text += "\\nFOLDER_BINDING_PROPERTIES_TEST:\\n $bindingPropertiesTestPath\n"
    deleteFolder(bindingPropertiesTestPath.toFile())


    Path propertiesYmlTestPath =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'test', 'resources',
                    'application.yml')
    log_text += "\napplication.yml -> test-resources:\n $propertiesYmlTestPath\n"
    deleteFile(propertiesYmlTestPath.toFile())
}