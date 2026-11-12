// -----------------------------------------------------------------------------







// BODY-15-ELK-FUNCTIONS.GROOVY ----------------------------------------------
// Single owner of the ELK artifacts. The snippets decide WHEN to call it (see
// snippet-13 for logback ON and snippet-17 for logback OFF); this helper only
// knows HOW they are removed.

/**
 * Removes the ELK folder and the ELK docker compose.
 * Returns the report lines for the post-processor log.
 */
def String deleteElkArtifacts(Path elkFolder, Path dockerFolder) {
    StringBuilder elkLog = new StringBuilder()

    elkLog.append("\nFOLDER_ELK:\n ").append(elkFolder).append("\n")
    deleteFolder(elkFolder.toFile())

    Path dockerElkComposePath = dockerFolder.resolve("elk-compose.yml")
    elkLog.append("\nelk-compose.yml -> docker:\n ").append(dockerElkComposePath).append("\n")
    deleteFile(dockerElkComposePath.toFile())

    return elkLog.toString()
}
