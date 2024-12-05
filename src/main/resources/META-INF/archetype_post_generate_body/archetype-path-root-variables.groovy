import java.nio.file.Path
import java.nio.file.Paths

java.io.File


// -----------------------------------------------------------------------------

// ARCHETYPE-PATH-ROOT-VARIABLES.GROOVY ----------------------------------------
Path projectRootFolder =
        Paths.get(
                request.outputDirectory,
                request.artifactId)

Path rootGitFolder =
        projectRootFolder
                .resolve("git_temp")

Path rootGitIgnoreFile =
        projectRootFolder
                .resolve('gitignore')

Path rootDockerFolder =
        projectRootFolder
                .resolve("docker")

Path rootELKFolder =
        Paths.get(
                request.outputDirectory,
                request.artifactId,
                'ELK')