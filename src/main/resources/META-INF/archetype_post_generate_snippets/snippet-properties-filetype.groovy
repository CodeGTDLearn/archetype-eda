import java.nio.file.Path
import java.nio.file.Paths



// -----------------------------------------------------------------------------

// SNIPPET-PROPERTIES-FILETYPE.GROOVY ------------------------------------------
// properties_filetype_choice:        1 - yml          2 - properties

def deleteFileType = (properties_filetype_choice == '1') ? '.yml' : '.properties'
def fileTypeMessage = (properties_filetype_choice == '1') ? 'Delete All YML-FILES' : 'Delete All PROPERTIES-FILES'
log_text += "\nBINDING_PROPERTIES:\n=> Message: " +  fileTypeMessage


Path srcMainJava_resourcesFolder_path =
        Paths.get(request.outputDirectory,
                request.artifactId,
                'src', 'main', 'resources')
log_text += "\n" + fileTypeMessage + " -> main-resources:\n $srcMainJava_resourcesFolder_path\n"
deleteFilesByExtensionRecursively(srcMainJava_resourcesFolder_path, deleteFileType)


Path srcTestJava_resourcesFolder_path =
        Paths.get(request.outputDirectory,
                request.artifactId,
                'src', 'test', 'resources')
log_text += "\n" + fileTypeMessage + " -> test-resources:\n $srcTestJava_resourcesFolder_path\n"
deleteFilesByExtensionRecursively(srcTestJava_resourcesFolder_path, deleteFileType)