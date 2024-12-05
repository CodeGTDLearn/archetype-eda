import java.nio.file.FileVisitResult
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.concurrent.atomic.AtomicInteger






// ARCHETYPE-FOOTER.GROOVY -----------------------------------------------------


// SNIPPET-POST-GENERATE-LOG.GROOVY --------------------------------------------
if (show_pos_processor_log == 'true') printf "\n$log_text $log_text_footer"


// UTIL FUNCTIONS --------------------------------------------------------------
def deleteFolder(File dir) {
    if (dir.isDirectory()) {
        dir.listFiles().each { file -> if (file.isDirectory()) deleteFolder(file) else file.delete()
        }
    }
    dir.delete()
}


def deleteFile(File file) {
    if (file.isFile()) file.delete()
//    FileOperations.deleteFile(file)
}


private void deleteElkLogSystemStructure(log_text) {
    Path folderELKPath =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'ELK')
    log_text += "\nFOLDER_ELK:\n $folderELKPath\n"
    deleteFolder(folderELKPath.toFile())

    Path dockerElkComposePath =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'docker', 'elk-compose.yml')
    log_text += "\nelk-compose.xml -> docker:\n $dockerElkComposePath\n"
    deleteFile(dockerElkComposePath.toFile())
}


/**
 * Deletes all files with a specified extension in a directory and its subdirectories.
 *
 * @param directoryPath Path of the directory where files will be deleted
 * @param extension Extension of files to be deleted (e.g.: ".yml", ".properties")
 * @param recursive If true, deletes files in subdirectories as well
 * @return Number of files deleted
 * @throws IOException If an error occurs while accessing or deleting files
 * @example: deleteFilesByExtension(Paths.get(directoryPath), ".yml", true)   */
private int deleteFilesByExtension(Path directoryPath, String extension, boolean recursive = true) {

    if (!Files.exists(directoryPath)) return 0

    final def deletedFiles = new AtomicInteger(0)

    Files.walkFileTree(directoryPath, new SimpleFileVisitor<Path>() {
        @Override
        FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            if (file.toString().endsWith(extension)) {
                Files.delete(file)
                deletedFiles.incrementAndGet()
            }
            return FileVisitResult.CONTINUE
        }

        @Override
        FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            return recursive || dir == directoryPath ?
                    FileVisitResult.CONTINUE :
                    FileVisitResult.SKIP_SUBTREE
        }
    })

    return deletedFiles.get()
}



// FOOTER ----------------------------------------------------------------------