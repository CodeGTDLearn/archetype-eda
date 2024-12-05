import java.nio.file.Path
import java.nio.file.Paths



// SNIPPET ---------------------------------------------------------------------


if (keep_docker == 'false') {
    Path folderPath =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'docker')
    log_text += "\nFOLDER_DOCKER:\n $folderPath\n"
    deleteFolder(folderPath.toFile())
}


// SNIPPET ---------------------------------------------------------------------