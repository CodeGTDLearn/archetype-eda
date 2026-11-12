// -----------------------------------------------------------------------------








// SNIPPET-19-ENABLE_FOLDER_SRC_TEST_JAVA.GROOVY -------------------------------
// OWNER: the whole test tree (src/test/java + src/test/resources).
if (enable_folder_src_test_java == 'false') {

    log_text += "\nFOLDER_SRC_TEST_JAVA:\n $srcTestJava\n"
    deleteFolder(srcTestJava.toFile())


    log_text += "\nFOLDER_SRC_TEST_RESOURCES:\n $srcTestResources\n"
    deleteFolder(srcTestResources.toFile())
}