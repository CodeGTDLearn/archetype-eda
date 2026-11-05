import java.nio.file.Path


// -----------------------------------------------------------------------------






// ARCHETYPE-PATH-SECURITY-VARIABLES.GROOVY ------------------------------------
def authentication_folder = "authentication"


Path filePathAppKey = srcMainResources.resolve("security/appJwt.key")
Path filePathAppPub = srcMainResources.resolve("security/appJwt.pub")
Path folderJwtPath = srcMainJavaEntityConfigFolder.resolve(authentication_folder)