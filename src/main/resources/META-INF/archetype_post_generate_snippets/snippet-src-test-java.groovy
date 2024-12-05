import java.nio.file.Path
import java.nio.file.Paths












// SNIPPET-SRC-TEST-JAVA.GROOVY ------------------------------------------------

if (keep_src_test_java == 'false') {
    Path folderTestPath =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'test', 'java')
    log_text += "\nFOLDER_SRC_TEST_JAVA:\n $folderTestPath\n"
    deleteFolder(folderTestPath.toFile())


    Path folderTestResourcePath =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'test', 'resources')
    log_text += "\nFOLDER_SRC_TEST_RESOURCES:\n $folderTestResourcePath\n"


    deleteFolder(folderTestResourcePath.toFile())
}