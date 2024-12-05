import java.nio.file.Path













// ARCHETYPE-FUNCTIONS.GROOVY --------------------------------------------------

// FILE MANAGEMENT FUNCTIONS ---------------------------------------------------
def deleteFile(File file) {
    if (file.isFile()) file.delete()
}


/**
 * Deleta recursivamente todos os arquivos com uma determinada extensão
 * @param directory Path do diretório
 * @param extension Extensão dos arquivos (exemplo: ".yml", ".properties")
 */
def deleteFilesByExtensionRecursively(Path path, String extension) {

    def currentFolder = path.toFile()

    if (!currentFolder.exists()) return

    // Remove from 'Current Directory'
    currentFolder.eachFile {
        file -> if (file.isFile() && file.name.endsWith(extension)) file.delete()
    }

    // Remove from 'SubDirectories'
    currentFolder.eachDir {
        subDir -> deleteFilesByExtensionRecursively(subDir.toPath(), extension)
    }
}


def deleteFilesFromList(Path directory, List<String> files) {

    println "Entrou na funcao deleteFilesFromList"

    files.removeIf { it == null || it.trim() == "" }

    // Valida se diretório existe
    if (!directory.toFile().isDirectory()) {
        println "Diretório não encontrado: $directory"
        return
    }

    // Processa cada arquivo
    files.each {
        fileName ->
            try {
                def fileToDelete = directory.resolve(fileName).toFile()

                if (fileToDelete.exists()) {

                    fileToDelete.delete()
                    println "File Found: $fileName"
                } else {

                    println "File Not Found: $fileName em ${directory.toAbsolutePath()}"
                }
            } catch (Exception e) {
                println "Erro ao deletar $fileName: ${e.message}"
            }
    }

    println "Saiu da funcao deleteFilesFromList"
}


// FOLDER MANAGEMENT FUNCTIONS -------------------------------------------------
def deleteFolder(File dir) {
    if (dir.isDirectory()) {

        dir.listFiles()
                .each {
                    file -> if (file.isDirectory()) deleteFolder(file) else file.delete()
                }
    }
    dir.delete()
}


def deleteElkLogSystemStructure(log_text) {
    Path folderELKPath =
            Paths.get(
                    request.outputDirectory,
                    request.artifactId,
                    'ELK')
    log_text += "\nFOLDER_ELK:\n $folderELKPath\n"
    deleteFolder(folderELKPath.toFile())


    Path dockerElkComposePath =
            Paths.get(
                    request.outputDirectory,
                    request.artifactId,
                    'docker',
                    'elk-compose.yml')
    log_text += "\nelk-compose.xml -> docker:\n $dockerElkComposePath\n"
    deleteFile(dockerElkComposePath.toFile())
}


// FUNCTIONS -------------------------------------------------------------------
