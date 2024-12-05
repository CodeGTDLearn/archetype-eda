import java.nio.file.Path












// -----------------------------------------------------------------------------

// ARCHETYPE-PATH-VARIABLES.GROOVY ---------------------------------------------

Path projectRootFolder =
        Paths.get(
                request.outputDirectory,
                request.artifactId)

Path rootGitFolder =
        projectRootFolder
                .resolve(".git_temp")

Path rootDockerFolder =
        projectRootFolder
                .resolve("docker")

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

Path propertiesProjectFolder =
        srcMainJavaEntityConfigFolder
                .resolve("properties")