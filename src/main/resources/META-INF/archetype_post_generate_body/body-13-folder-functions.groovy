import java.nio.file.Path

// -----------------------------------------------------------------------------








// BODY-13-FOLDER-FUNCTIONS.GROOVY -------------------------------------------
// FOLDER HELPERS --------------------------------------------------------------
// The helpers act and RETURN the outcome; the report is written by the
// snippets. The trace lines are gated by show_archetype_post_generate_log.

def deleteFolder(File pathToFolder) {

    if (postGenerateLogEnabled()) println "\nEntrou na funcao deleteFolder: $pathToFolder"

    if (pathToFolder.isDirectory()) {

        pathToFolder.listFiles()
                .each {
                    file -> if (file.isDirectory()) deleteFolder(file) else file.delete()
                }
    }
    boolean deleted = pathToFolder.delete()

    if (postGenerateLogEnabled()) println "\nSaiu da funcao deleteFolder: $pathToFolder"

    return deleted
}


def renameFolder(File pathToFolder, String newName) {

    if (postGenerateLogEnabled()) println "\nEntrou na funcao renameFolder: $pathToFolder"

    if (pathToFolder.isDirectory()) {
        def newFolderPath = pathToFolder.parentFile.toPath().resolve(newName)

        if (newFolderPath.toFile().exists()) {
            if (postGenerateLogEnabled()) println "\nErro: Pasta com o nome $newName já existe."
            return false
        }
        boolean renamed = pathToFolder.renameTo(newFolderPath.toFile())
        if (postGenerateLogEnabled()) {
            println "\nPasta renomeada com sucesso: $pathToFolder -> $newName"
            println "\nSaiu da funcao renameFolder: $pathToFolder"
        }
        return renamed
    } else {
        if (postGenerateLogEnabled()) {
            println "\nErro: $pathToFolder não é um diretório."
            println "\nSaiu da funcao renameFolder: $pathToFolder"
        }
        return false
    }
}


/**
 * Deleta recursivamente todos os arquivos com uma determinada extensão
 * @param directory Path do diretório
 * @param extension Extensão dos arquivos (exemplo: ".yml", ".properties")
 * @return quantidade de arquivos removidos
 */
def deleteFilesByExtensionRecursivelyFromFolder(Path pathToFiles, String extension) {

    if (postGenerateLogEnabled()) println "\nEntrou na funcao deleteFilesByExtensionRecursivelyFromFolder: $pathToFiles"

    def currentFolder = pathToFiles.toFile()

    if (!currentFolder.exists()) return 0

    int removed = 0

    // Remove from 'Current Directory'
    currentFolder.eachFile {
        file -> if (file.isFile() && file.name.endsWith(extension)) {
            if (file.delete()) removed++
        }
    }

    // Remove from 'SubDirectories'
    currentFolder.eachDir {
        subDir -> removed += deleteFilesByExtensionRecursivelyFromFolder(subDir.toPath(), extension)
    }

    if (postGenerateLogEnabled()) println "\nSaiu da funcao deleteFilesByExtensionRecursivelyFromFolder: $pathToFiles"

    return removed
}


def deleteFilesFromListFromFolder(Path pathToFiles, List<String> files) {

    if (postGenerateLogEnabled()) println "\nEntrou na funcao deleteFilesFromListFromFolder: $pathToFiles"

    // The caller's list is never mutated: the registry is shared.
    List<String> effective = files.findAll { it != null && it.trim() != "" }

    // Valida se diretório existe
    if (!pathToFiles.toFile().isDirectory()) {
        if (postGenerateLogEnabled()) println "\nPasta não encontrada: $pathToFiles"
        return 0
    }

    int removed = 0

    // Processa cada arquivo
    effective.each {
        fileName ->
            try {
                def fileToDelete = pathToFiles.resolve(fileName).toFile()

                if (fileToDelete.exists()) {

                    if (fileToDelete.delete()) removed++
                    if (postGenerateLogEnabled()) println "\nFile Found: $fileName"
                } else {

                    if (postGenerateLogEnabled()) println "\nFile Not Found: $fileName em ${pathToFiles.toAbsolutePath()}"
                }
            } catch (Exception e) {
                if (postGenerateLogEnabled()) println "\nErro ao deletar $fileName: ${e.message}"
            }
    }

    if (postGenerateLogEnabled()) println "\nSaiu da funcao deleteFilesFromListFromFolder: $pathToFiles"

    return removed
}
