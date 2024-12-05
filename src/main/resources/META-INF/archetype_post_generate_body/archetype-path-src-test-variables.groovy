import java.nio.file.Path

java.io.File


// -----------------------------------------------------------------------------

//// ARCHETYPE-PATH-SRC-TEST-VARIABLES.GROOVY ---------------------------------------------
//Path srcTestJava =
//        Paths.get(request.outputDirectory,
//                request.artifactId,
//                'src', 'test', 'java')
//
//Path srcTestResources =
//        Paths.get(request.outputDirectory,
//                request.artifactId,
//                'src', 'test', 'resources')
//
//Path srcTestJavaEntityConfigPropertiesFolder =
//        srcTestJava
//                .resolve(entity_folder)
//                .resolve(config_folder)
//                .resolve("properties")
//
//Path srcTestResourcesPropertiesFolder =
//        srcTestResources
//                .resolve("properties")


// ARCHETYPE-PATH-SRC-TEST-VARIABLES.GROOVY -----------------------------------

//Path srcTestJava = Paths.get(request.outputDirectory, request.artifactId, "src", "test", "java")
//Path srcTestResources = Paths.get(request.outputDirectory, request.artifactId, "src", "test", "resources")

Path srcTestJava = projectRootFolder.resolve("src").resolve("test").resolve("java")
Path srcTestResources = projectRootFolder.resolve("src").resolve("test").resolve("resources")

Path srcTestJavaEntityConfigPropertiesFolder = srcTestJava
        .resolve(entity_folder)
        .resolve(config_folder)
        .resolve("properties")

Path srcTestResourcesPropertiesFolder = srcTestResources.resolve("properties")