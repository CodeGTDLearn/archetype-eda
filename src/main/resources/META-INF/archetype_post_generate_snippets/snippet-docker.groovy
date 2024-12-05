// -----------------------------------------------------------------------------

// SNIPPET-DOCKER.GROOVY
if (keep_docker == 'false') {
    log_text += "\nFOLDER_DOCKER:\n $rootDockerFolder\n"
    deleteFolder(rootDockerFolder.toFile())
}