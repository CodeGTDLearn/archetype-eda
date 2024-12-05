import java.nio.file.Path
import java.nio.file.Paths







// SNIPPET-BINDING-PROPERTIES.GROOVY -------------------------------------------



if (keep_binding_properties == 'false') {

    Path bindingPropertiesPath =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'java', entity_name, 'config', 'properties')
    log_text += "\\nFOLDER_BINDING_PROPERTIES:\\n $bindingPropertiesPath\n"
    deleteFolder(bindingPropertiesPath.toFile())
}