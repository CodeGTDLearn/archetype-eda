import java.nio.file.Path


// -----------------------------------------------------------------------------






// BODY-09-PATH-SECURITY-VARIABLES.GROOVY ------------------------------------
// authentication_folder is a domain constant: see archetype-global-variables.

Path filePathAppKey = srcMainResources.resolve("security/appJwt.key")
Path filePathAppPub = srcMainResources.resolve("security/appJwt.pub")
Path folderJwtPath = srcMainJavaEntityConfigFolder.resolve(authentication_folder)