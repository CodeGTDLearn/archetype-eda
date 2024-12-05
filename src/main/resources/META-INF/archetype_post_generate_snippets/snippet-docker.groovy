import java.nio.file.Path
import java.nio.file.Paths












// SNIPPET-DOCKER.GROOVY -------------------------------------------------------

if (keep_docker == 'false') {
    Path folderDockerPath =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'docker')
    log_text += "\nFOLDER_DOCKER:\n $folderDockerPath\n"
    deleteFolder(folderDockerPath.toFile())
}
