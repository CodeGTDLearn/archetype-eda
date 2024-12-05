// -----------------------------------------------------------------------------


// SNIPPET-API-GENERAL.GROOVY --------------------------------------------------

// GIT SUPPORT -----------------------------------------------------------------
if (keep_git_support == 'false') {

  log_text += "\\nRemovind FOLDER_.GIT:\\n $rootGitFolder\n"
  
  deleteFolder(rootGitFolder.toFile())
}


if (keep_git_support == 'true') {

  log_text += "\\nRenaming FOLDER_.GIT:\\n $rootGitFolder\n"

  listFolderContents(rootGitFolder.toFile())

  renameFolder(rootGitFolder.toFile(), ".git")
}