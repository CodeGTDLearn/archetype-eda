import java.nio.file.Path
import java.nio.file.Paths


// -----------------------------------------------------------------------------



// SNIPPET-AUTHENTICATION.GROOVY

if (keep_jwt_authentication == 'false') {

    Path folderJwtPath =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'java',
                    project_package,
                    entity_folder,
                    config_folder,
                    authentication_folder)
    log_text += "\nJwt_authentication_folder_path:\n $folderJwtPath\n"
    deleteFolder(folderJwtPath.toFile())


    Path filePathAppKey =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'resources',
                    'appJwt.key')
    log_text.concat("\nsrc_main_resources_app_key:\n $filePathAppKey\n")
    deleteFile(filePathAppKey.toFile())


    Path filePathAppPub =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'resources',
                    'appJwt.pub')
    log_text.concat("\nsrc_main_resources_app_pub:\n $filePathAppPub\n")
    deleteFile(filePathAppPub.toFile())
}