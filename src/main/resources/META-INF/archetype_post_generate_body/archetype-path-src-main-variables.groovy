import java.nio.file.Path

// -----------------------------------------------------------------------------

// ARCHETYPE-PATH-SRC-MAIN-VARIABLES.GROOVY -----------------------------------

Path srcMainJava = projectRootFolder.resolve("src").resolve("main").resolve("java")
Path srcMainResources = projectRootFolder.resolve("src").resolve("main").resolve("resources")

def resourcePropertiesFolder = "properties"

Path srcMainResourcesProperties = srcMainResources.resolve(resourcePropertiesFolder)

Path srcMainJavaEntityFolder = srcMainJava.resolve(project_package).resolve(entity_folder)
Path srcMainJavaEntityConfigFolder = srcMainJavaEntityFolder.resolve(config_folder)


Path srcMainJavaEntityConfigPropertiesFolder = srcMainJavaEntityConfigFolder.resolve(resourcePropertiesFolder)

// -----------------------------------------------------------------------------