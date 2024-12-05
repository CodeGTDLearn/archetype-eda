import java.nio.file.Path

// -----------------------------------------------------------------------------

// ARCHETYPE-PATH-SRC-TEST-VARIABLES.GROOVY -----------------------------------

Path srcTestJava = projectRootFolder.resolve("src").resolve("test").resolve("java")
Path srcTestResources = projectRootFolder.resolve("src").resolve("test").resolve("resources")

def testResourcePropertiesFolder = "properties_test"

Path srcTestJavaEntityConfigPropertiesFolder = srcTestJava
        .resolve(entity_folder)
        .resolve(config_folder)
        .resolve(testResourcePropertiesFolder)

Path srcTestResourcesPropertiesFolder = srcTestResources.resolve(testResourcePropertiesFolder)

// -----------------------------------------------------------------------------