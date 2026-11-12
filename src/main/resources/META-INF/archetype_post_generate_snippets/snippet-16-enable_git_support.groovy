// -----------------------------------------------------------------------------








// SNIPPET-16-ENABLE_GIT_SUPPORT.GROOVY ----------------------------------------
// OWNER: git_temp/ (renamed to .git) and gitignore (renamed to .gitignore).
if (enable_git_support == 'false') {

    log_text += "\nRemoving FOLDER_.GIT:\n $rootGitFolder\n"
    deleteFolder(rootGitFolder.toFile())

    log_text += "\nRemoving 'GIT_IGNORE FILE' -> Root Folder:\n $rootGitIgnoreFile\n"
    deleteFile(rootGitIgnoreFile.toFile())
}


if (enable_git_support == 'true') {

    if (renameFolder(rootGitFolder.toFile(), ".git")) {
        log_text += "\nRenaming FOLDER_.GIT:\n $rootGitFolder\n"
    } else {
        log_text += "\nFolder '.git' NOT renamed (already exists or the source is missing):\n $rootGitFolder\n"
    }

    if (renameFile(rootGitIgnoreFile.toFile(), ".gitignore")) {
        log_text += "\nRenaming 'GIT_IGNORE FILE' to: .gitignore\n Root Folder: $rootGitIgnoreFile\n"
    } else {
        log_text += "\n'GIT_IGNORE FILE' NOT renamed (already exists or the source is missing):\n $rootGitIgnoreFile\n"
    }
}