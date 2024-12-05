import java.nio.file.Path

// -----------------------------------------------------------------------------






// ARCHETYPE-PATH-SRC-TEST-VARIABLES.GROOVY -----------------------------------

Path srcTestJava = projectRootFolder.resolve("src").resolve("test").resolve("java")
Path srcTestResources = projectRootFolder.resolve("src").resolve("test").resolve("resources")

Path srcTestJavaEntityConfigPropertiesFolder = srcTestJava
        .resolve(entity_folder)
        .resolve(config_folder)
        .resolve(properties_folder)

Path srcTestResourcesPropertiesFolder = srcTestResources.resolve(properties_folder)
Path srcTestJavaEntityConfigPropertiesListFolder = srcTestJavaEntityConfigPropertiesFolder.resolve(list_properties_folder)
Path srcTestJavaEntityConfigPropertiesMapFolder = srcTestJavaEntityConfigPropertiesFolder.resolve(map_properties_folder)