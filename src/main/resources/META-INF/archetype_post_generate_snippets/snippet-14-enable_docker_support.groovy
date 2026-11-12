// -----------------------------------------------------------------------------








// SNIPPET-14-ENABLE_DOCKER_SUPPORT.GROOVY -------------------------------------
// OWNER: the whole docker/ folder (precedence over elk-compose.yml).
if (enable_docker_support == 'false') {
    log_text += "\nFOLDER_DOCKER:\n $rootDockerFolder\n"
    deleteFolder(rootDockerFolder.toFile())
}