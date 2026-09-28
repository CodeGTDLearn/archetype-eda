import java.nio.file.Path
import java.nio.file.Paths

// -----------------------------------------------------------------------------







// ARCHETYPE-PATH-SRC-MAIN-VARIABLES.GROOVY ------------------------------------

// ARCHETYPE-PATH-SRC-MAIN-JAVA ------------------------------------------------
Path srcMainJava = projectRootFolder.resolve("src").resolve("main").resolve("java")

Path srcMainJavaEntityFolder = srcMainJava.resolve(project_package).resolve(entity_folder)
Path srcMainJavaEntityConfigFolder = srcMainJavaEntityFolder.resolve(config_folder)

Path srcMainJavaEntityConfigPropertiesFolder = srcMainJavaEntityConfigFolder.resolve(properties_folder)
Path srcMainJavaEntityConfigPropertiesListFolder = srcMainJavaEntityConfigPropertiesFolder.resolve(list_properties_folder)
Path srcMainJavaEntityConfigPropertiesMapFolder = srcMainJavaEntityConfigPropertiesFolder.resolve(map_properties_folder)

// ARCHETYPE-PATH-SRC-MAIN-RESOURCES -------------------------------------------
Path srcMainResources = projectRootFolder.resolve("src").resolve("main").resolve("resources")
Path srcMainResourcesProperties = srcMainResources.resolve(properties_folder)