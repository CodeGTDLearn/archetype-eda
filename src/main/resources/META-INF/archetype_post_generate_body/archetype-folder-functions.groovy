import java.nio.file.Path

// -----------------------------------------------------------------------------






// ARCHETYPE-FOLDER-FUNCTIONS.GROOVY -------------------------------------------
def deleteFolder(File pathToFolder) {

    println "\nEntrou na funcao deleteFolder: $pathToFolder"

    if (pathToFolder.isDirectory()) {

        pathToFolder.listFiles()
                .each {
                    file -> if (file.isDirectory()) deleteFolder(file) else file.delete()
                }
    }
    pathToFolder.delete()

    println "\nSaiu na funcao deleteFolder: $pathToFolder"

}


def renameFolder(File pathToFolder, String newName) {

    println "\nEntrou na funcao renameFolder: $pathToFolder"

    if (pathToFolder.isDirectory()) {
        def newFolderPath = pathToFolder.parentFile.toPath().resolve(newName)

        if (newFolderPath.toFile().exists()) {
            println "\nErro: Pasta com o nome $newName já existe."
            return
        }
        pathToFolder.renameTo(newFolderPath.toFile())
        println "\nPasta renomeada com sucesso: $pathToFolder -> $newName"
    } else {
        println "\nErro: $pathToFolder não é um diretório."
    }

    println "\nSaiu da funcao renameFolder: $pathToFolder"

}


/**
 * Deleta recursivamente todos os arquivos com uma determinada extensão
 * @param directory Path do diretório
 * @param extension Extensão dos arquivos (exemplo: ".yml", ".properties")
 */
def deleteFilesByExtensionRecursivelyFromFolder(Path pathToFiles, String extension) {

    println "\nEntrou na funcao deleteFilesByExtensionRecursivelyFromFolder: $pathToFiles"

    def currentFolder = pathToFiles.toFile()

    if (!currentFolder.exists()) return

    // Remove from 'Current Directory'
    currentFolder.eachFile {
        file -> if (file.isFile() && file.name.endsWith(extension)) file.delete()
    }

    // Remove from 'SubDirectories'
    currentFolder.eachDir {
        subDir -> deleteFilesByExtensionRecursivelyFromFolder(subDir.toPath(), extension)
    }

    println "\nSaiu na funcao deleteFilesByExtensionRecursivelyFromFolder: $pathToFiles"
}


def deleteFilesFromListFromFolder(Path pathToFiles, List<String> files) {

    println "\nEntrou na funcao deleteFilesFromListFromFolder: $pathToFiles"

    files.removeIf { it == null || it.trim() == "" }

    // Valida se diretório existe
    if (!pathToFiles.toFile().isDirectory()) {
        println "\nPasta não encontrada: $pathToFiles"
        return
    }

    // Processa cada arquivo
    files.each {
        fileName ->
            try {
                def fileToDelete = pathToFiles.resolve(fileName).toFile()

                if (fileToDelete.exists()) {

                    fileToDelete.delete()
                    println "\nFile Found: $fileName"
                } else {

                    println "\nFile Not Found: $fileName em ${pathToFiles.toAbsolutePath()}"
                }
            } catch (Exception e) {
                println "\nErro ao deletar $fileName: ${e.message}"
            }
    }

    println "\nSaiu da funcao deleteFilesFromListFromFolder: $pathToFiles"
}