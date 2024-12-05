import java.nio.file.Path

java.io.File


// -----------------------------------------------------------------------------

// ARCHETYPE-PATH-SECURITY-VARIABLES.GROOVY ------------------------------------
Path folderJwtPath =
        srcMainJavaEntityConfigFolder
                .resolve(authentication_folder)

Path filePathAppKey =
        srcMainResources
                .resolve("appJwt.key")

Path filePathAppPub =
        srcMainResources
                .resolve("appJwt.pub")