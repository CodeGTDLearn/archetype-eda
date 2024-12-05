import java.nio.file.Path


// FOOTER ----------------------------------------------------------------------
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