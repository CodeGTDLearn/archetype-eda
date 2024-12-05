// -----------------------------------------------------------------------------


// SNIPPET-API-GENERAL.GROOVY --------------------------------------------------

// GIT SUPPORT -----------------------------------------------------------------
if (keep_git_support == 'false') {

  log_text += "\\nFOLDER_.GIT:\\n $rootGitFolder\n"
  
  deleteFolder(rootGitFolder.toFile())
}