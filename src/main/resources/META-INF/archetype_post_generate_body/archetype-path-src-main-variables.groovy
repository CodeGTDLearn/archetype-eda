import java.nio.file.Path

// -----------------------------------------------------------------------------







// ARCHETYPE-PATH-SRC-MAIN-VARIABLES.GROOVY -----------------------------------

Path srcMainJava = projectRootFolder.resolve("src").resolve("main").resolve("java")
Path srcMainResources = projectRootFolder.resolve("src").resolve("main").resolve("resources")

Path srcMainResourcesProperties = srcMainResources.resolve(properties_folder)

Path srcMainJavaEntityFolder = srcMainJava.resolve(project_package).resolve(entity_folder)
Path srcMainJavaEntityConfigFolder = srcMainJavaEntityFolder.resolve(config_folder)


Path srcMainJavaEntityConfigPropertiesFolder = srcMainJavaEntityConfigFolder.resolve(properties_folder)