import java.nio.file.Path

// -----------------------------------------------------------------------------






// ARCHETYPE-FOLDER-FUNCTIONS.GROOVY -------------------------------------------
def deleteFolder(File pathToFolder) {
    if (pathToFolder.isDirectory()) {

        pathToFolder.listFiles()
                .each {
                    file -> if (file.isDirectory()) deleteFolder(file) else file.delete()
                }
    }
    pathToFolder.delete()
}


//def deleteElkLogSystemStructure(log_text) {
//
//    log_text += "\nFOLDER_ELK:\n $rootELKFolder\n"
//    deleteFolder(rootELKFolder.toFile())
//
//    Path dockerElkComposePath =
//            rootDockerFolder
//                    .resolve("elk-compose.yml")
//
//    log_text += "\nelk-compose.yml -> docker:\n $dockerElkComposePath\n"
//    deleteFile(dockerElkComposePath.toFile())
//}


def renameFolder(File pathToFolder, String newName) {

    if (pathToFolder.isDirectory()) {
        def newFolderPath = pathToFolder.parentFile.toPath().resolve(newName)
        if (newFolderPath.toFile().exists()) {
            println "Erro: Pasta com o nome $newName já existe."
            return
        }
        pathToFolder.renameTo(newFolderPath.toFile())
        println "Pasta renomeada com sucesso: $pathToFolder -> $newName"
    } else {
        println "Erro: $pathToFolder não é um diretório."
    }
}


/**
 * Deleta recursivamente todos os arquivos com uma determinada extensão
 * @param directory Path do diretório
 * @param extension Extensão dos arquivos (exemplo: ".yml", ".properties")
 */
def deleteFilesByExtensionRecursivelyFromFolder(Path pathToFiles, String extension) {

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
}


def deleteFilesFromListFromFolder(Path pathToFiles, List<String> files) {

    println "Entrou na funcao deleteFilesFromListFromFolder"

    files.removeIf { it == null || it.trim() == "" }

    // Valida se diretório existe
    if (!pathToFiles.toFile().isDirectory()) {
        println "Pasta não encontrada: $pathToFiles"
        return
    }

    // Processa cada arquivo
    files.each {
        fileName ->
            try {
                def fileToDelete = pathToFiles.resolve(fileName).toFile()

                if (fileToDelete.exists()) {

                    fileToDelete.delete()
                    println "File Found: $fileName"
                } else {

                    println "File Not Found: $fileName em ${pathToFiles.toAbsolutePath()}"
                }
            } catch (Exception e) {
                println "Erro ao deletar $fileName: ${e.message}"
            }
    }

    println "Saiu da funcao deleteFilesFromListFromFolder"
}