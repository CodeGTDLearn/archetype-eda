// -----------------------------------------------------------------------------






// ARCHETYPE-FILE-FUNCTIONS.GROOVY ---------------------------------------------

def deleteFile(File file) {
    if (file.isFile()) file.delete()
}


def renameFile(File file, String newName) {

    println "File: $file"

    def newFile = new File(file.getParent(), newName)

    println "newFile: $newFile"

    if (!file.exists() || !file.isFile()) {
        println "Error: File $file invalid or inexistent."
        return
    }

    if (newFile.exists()) {
        println "Error: File $newName already exists in the path"
        return
    }

    if (file.renameTo(newFile)) {
        println "File renamed: $file -> $newName"
    } else {
        println "Error: Unable to rename $file to $newFile"
    }
}