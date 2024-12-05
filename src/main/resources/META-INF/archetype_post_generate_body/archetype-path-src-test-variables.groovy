import java.nio.file.Path
import java.nio.file.Paths

java.io.File


// -----------------------------------------------------------------------------

// ARCHETYPE-PATH-SRC-TEST-VARIABLES.GROOVY ---------------------------------------------
Path srcTestJava =
        Paths.get(request.outputDirectory,
                request.artifactId,
                'src', 'test', 'java')

Path srcTestResources =
        Paths.get(request.outputDirectory,
                request.artifactId,
                'src', 'test', 'resources')

Path srcTestJavaEntityConfigPropertiesFolder =
        srcTestJava
                .resolve(entity_folder)
                .resolve(config_folder)
                .resolve("properties")

Path srcTestResourcesPropertiesFolder =
        srcTestResources
                .resolve("properties")