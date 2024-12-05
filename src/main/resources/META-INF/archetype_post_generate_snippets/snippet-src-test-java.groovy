// -----------------------------------------------------------------------------

// SNIPPET-SRC-TEST-JAVA.GROOVY
if (keep_src_test_java == 'false') {

    log_text += "\nFOLDER_SRC_TEST_JAVA:\n $srcTestJava\n"
    deleteFolder(srcTestJava.toFile())


    log_text += "\nFOLDER_SRC_TEST_RESOURCES:\n $srcTestResources\n"
    deleteFolder(srcTestResources.toFile())
}