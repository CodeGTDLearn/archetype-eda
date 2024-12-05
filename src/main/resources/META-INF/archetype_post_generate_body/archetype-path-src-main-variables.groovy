import java.nio.file.Path
import java.nio.file.Paths

java.io.File


// -----------------------------------------------------------------------------

// ARCHETYPE-PATH-VARIABLES.GROOVY ---------------------------------------------

Path srcMainJava =
        Paths.get(
                request.outputDirectory,
                request.artifactId,
                'src', 'main', 'java')

Path srcMainResources =
        Paths.get(
                request.outputDirectory,
                request.artifactId,
                'src', 'main', 'resources')

Path srcMainResourcesProperties =
        srcMainResources
                .resolve("properties")

Path srcMainJavaEntityFolder =
        srcMainJava
                .resolve(project_package)
                .resolve(entity_folder)

Path srcMainJavaEntityConfigFolder =
        srcMainJavaEntityFolder
                .resolve(config_folder)

Path srcMainJavaEntityConfigPropertiesFolder =
        srcMainJavaEntityConfigFolder
                .resolve("properties")