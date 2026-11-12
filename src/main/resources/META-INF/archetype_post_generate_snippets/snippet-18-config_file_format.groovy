// -----------------------------------------------------------------------------








// SNIPPET-18-CONFIG_FILE_FORMAT.GROOVY ----------------------------------------
// OWNER: the config file extension pruning - the only extension-based owner.
// config_file_format:        1 - properties    2 - yml
// A value that is neither 1 nor 2 (an absent flag becomes the string 'null')
// falls back to the catalog default (1): the .properties are kept.
def deleteFileType = (config_file_format == '2') ? '.properties' : '.yml'
def fileTypeMessage = (config_file_format == '2') ? 'Delete All PROPERTIES-FILES' : 'Delete All YML-FILES'
log_text += "\nBINDING_PROPERTIES:\n=> Message: " +  fileTypeMessage


log_text += "\n" + fileTypeMessage + " -> main-resources:\n $srcMainResources\n"
deleteFilesByExtensionRecursivelyFromFolder(srcMainResources, deleteFileType)


log_text += "\n" + fileTypeMessage + " -> test-resources:\n $srcTestResources\n"
deleteFilesByExtensionRecursivelyFromFolder(srcTestResources, deleteFileType)