// -----------------------------------------------------------------------------








// BODY-12-FILE-FUNCTIONS.GROOVY ---------------------------------------------
// FILE HELPERS ----------------------------------------------------------------
// The helpers act and RETURN the outcome; the report is written by the
// snippets. The trace lines are gated by show_archetype_post_generate_log.

def deleteFile(File pathToFile) {

    if (postGenerateLogEnabled()) println "\nEntrou na funcao deleteFile - File: $pathToFile"

    boolean deleted = pathToFile.isFile() && pathToFile.delete()

    if (postGenerateLogEnabled()) println "\nSaiu da funcao deleteFile - File: $pathToFile"

    return deleted
}


def renameFile(File pathToFile, String newName) {

    if (postGenerateLogEnabled()) println "\nEntrou na funcao renameFile - File: $pathToFile"

    def newFile = new File(pathToFile.getParent(), newName)

    if (postGenerateLogEnabled()) println "\nnewFile: $newFile"

    if (newFile.absoluteFile == pathToFile.absoluteFile) {
        if (postGenerateLogEnabled()) println "\nNo-op: source and target are the same file: $pathToFile"
        return true
    }

    if (!pathToFile.exists() || !pathToFile.isFile()) {
        if (postGenerateLogEnabled()) println "\nError: File $pathToFile invalid or inexistent."
        return false
    }

    if (newFile.exists()) {
        if (postGenerateLogEnabled()) println "\nError: File $newName already exists in the path"
        return false
    }

    boolean renamed = pathToFile.renameTo(newFile)
    if (postGenerateLogEnabled()) {
        println renamed ? "\nFile renamed: $pathToFile -> $newName" : "\nError: Unable to rename $pathToFile to $newFile"
        println "\nSaiu da funcao renameFile - File: $pathToFile"
    }

    return renamed
}


def copyFile(File sourceFile, File destinationFolder) {
    if (postGenerateLogEnabled()) println "\nEntrou na funcao copyFile - Source: $sourceFile, Destination Folder: $destinationFolder"

    if (!sourceFile.exists() || !sourceFile.isFile()) {
        if (postGenerateLogEnabled()) println "\nError: Source file $sourceFile invalid or inexistent."
        return null
    }

    if (!destinationFolder.exists()) {
        destinationFolder.mkdirs()
    }

    def destinationFile = new File(destinationFolder, sourceFile.getName())

    try {
        destinationFile << sourceFile.bytes
        if (postGenerateLogEnabled()) println "\nFile copied successfully: $sourceFile -> $destinationFile"
    } catch (Exception e) {
        if (postGenerateLogEnabled()) println "\nError copying file: ${e.message}"
        return null
    }

    if (postGenerateLogEnabled()) println "\nSaiu da funcao copyFile - Source: $sourceFile, Destination: $destinationFile"

    return destinationFile
}
