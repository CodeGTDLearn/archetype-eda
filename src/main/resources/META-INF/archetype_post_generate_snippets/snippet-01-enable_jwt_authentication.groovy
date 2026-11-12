// -----------------------------------------------------------------------------








// SNIPPET-01-ENABLE_JWT_AUTHENTICATION.GROOVY ---------------------------------
// OWNER: the JWT artifacts (authentication package + key pair).
if (enable_jwt_authentication == 'false') {

    log_text += "\nJwt_authentication_folder_path:\n $folderJwtPath\n"
    deleteFolder(folderJwtPath.toFile())

    log_text += "\nsrc_main_resources_app_key:\n $filePathAppKey\n"
    deleteFile(filePathAppKey.toFile())

    log_text += "\nsrc_main_resources_app_pub:\n $filePathAppPub\n"
    deleteFile(filePathAppPub.toFile())
}
