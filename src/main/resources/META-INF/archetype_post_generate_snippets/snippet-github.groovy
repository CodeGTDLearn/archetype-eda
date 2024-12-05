// -----------------------------------------------------------------------------
// SNIPPET-GITHUB.GROOVY
if (keep_git_support == 'false') {

    log_text += "\\nRemoving FOLDER_.GIT:\\n $rootGitFolder\n"
    deleteFolder(rootGitFolder.toFile())

    log_text += "\nRemoving 'GIT_IGNORE FILE' -> Root Folder:\n $rootGitIgnoreFile\n"
    deleteFile(rootGitIgnoreFile.toFile())
}


if (keep_git_support == 'true') {

    log_text += "\\nRenaming FOLDER_.GIT:\\n $rootGitFolder\n"
    renameFolder(rootGitFolder.toFile(), ".git")

    log_text += "\nRenaming 'GIT_IGNORE FILE' to: .gitignore\n Root Folder: $rootGitIgnoreFile\n"
    renameFile(rootGitIgnoreFile.toFile(), ".gitignore")
}