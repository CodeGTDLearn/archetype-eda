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