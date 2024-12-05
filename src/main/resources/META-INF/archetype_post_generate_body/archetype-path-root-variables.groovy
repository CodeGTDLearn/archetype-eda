import java.nio.file.Path
import java.nio.file.Paths

// -----------------------------------------------------------------------------






// ARCHETYPE-PATH-ROOT-VARIABLES.GROOVY ----------------------------------------

Path projectRootFolder = Paths.get(request.outputDirectory, request.artifactId)

Path rootELKFolder = projectRootFolder.resolve("ELK")
Path rootGitFolder = projectRootFolder.resolve("git_temp")
Path rootDockerFolder = projectRootFolder.resolve("docker")

Path rootGitIgnoreFile = projectRootFolder.resolve(".gitignore")