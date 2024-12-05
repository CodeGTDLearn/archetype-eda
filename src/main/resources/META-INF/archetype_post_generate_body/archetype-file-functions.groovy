// -----------------------------------------------------------------------------






// ARCHETYPE-FILE-FUNCTIONS.GROOVY ---------------------------------------------

def deleteFile(File pathToFile) {

    println "\nEntrou na funcao deleteFile - File: $pathToFile"

    if (pathToFile.isFile()) pathToFile.delete()

    println "\nSaiu da funcao deleteFile - File: $pathToFile"

}


def renameFile(File pathToFile, String newName) {

    println "\nEntrou na funcao renameFile - File: $pathToFile"

    def newFile = new File(pathToFile.getParent(), newName)

    println "\nnewFile: $newFile"

    if (!pathToFile.exists() || !pathToFile.isFile()) {
        println "\nError: File $pathToFile invalid or inexistent."
        return
    }

    if (newFile.exists()) {
        println "\nError: File $newName already exists in the path"
        return
    }

    if (pathToFile.renameTo(newFile)) {
        println "\nFile renamed: $pathToFile -> $newName"
    } else {
        println "\nError: Unable to rename $pathToFile to $newFile"
    }

    println "\nSaiu da funcao renameFile - File: $pathToFile"
}


def copyFile(File sourceFile, File destinationFolder) {
    println "\nEntrou na funcao copyFile - Source: $sourceFile, Destination Folder: $destinationFolder"

    if (!sourceFile.exists() || !sourceFile.isFile()) {
        println "\nError: Source file $sourceFile invalid or inexistent."
        return
    }

    if (!destinationFolder.exists()) {
        destinationFolder.mkdirs()
    }

    def destinationFile = new File(destinationFolder, sourceFile.getName())

    try {
        destinationFile << sourceFile.bytes
        println "\nFile copied successfully: $sourceFile -> $destinationFile"
    } catch (Exception e) {
        println "\nError copying file: ${e.message}"
    }

    println "\nSaiu da funcao copyFile - Source: $sourceFile, Destination: $destinationFile"
}