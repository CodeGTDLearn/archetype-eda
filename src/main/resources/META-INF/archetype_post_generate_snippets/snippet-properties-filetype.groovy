// -----------------------------------------------------------------------------







// SNIPPET-PROPERTIES-FILETYPE.GROOVY
// properties_filetype_choice:        1 - yml          2 - properties
def deleteFileType = (properties_filetype_choice == '1') ? '.yml' : '.properties'
def fileTypeMessage = (properties_filetype_choice == '1') ? 'Delete All YML-FILES' : 'Delete All PROPERTIES-FILES'
log_text += "\nBINDING_PROPERTIES:\n=> Message: " +  fileTypeMessage


log_text += "\n" + fileTypeMessage + " -> main-resources:\n $srcMainResources\n"
deleteFilesByExtensionRecursivelyFromFolder(srcMainResources, deleteFileType)


log_text += "\n" + fileTypeMessage + " -> test-resources:\n $srcTestResources\n"
deleteFilesByExtensionRecursivelyFromFolder(srcTestResources, deleteFileType)