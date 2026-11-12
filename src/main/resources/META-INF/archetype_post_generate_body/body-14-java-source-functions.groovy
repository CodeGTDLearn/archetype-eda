// -----------------------------------------------------------------------------







// BODY-14-JAVA-SOURCE-FUNCTIONS.GROOVY --------------------------------------
// Helpers shared by the generators that WRITE Java sources or that resolve a
// package from the entity flags. Kept here (and not in the generators) so the
// package derivation and the no-overwrite rule have a single owner.

/**
 * The package of the entity: <package>.<entity_package_name>.
 * An unknown/blank suffix keeps the base package untouched.
 */
def String resolveEntityPackage(String basePackage, String entityPackageName) {
    String resolved = (basePackage == null) ? "" : basePackage.trim()
    String suffix = (entityPackageName == null) ? "" : entityPackageName.trim()

    if (suffix.isEmpty()) return resolved

    return resolved + "." + suffix
}

/** A base package is known when it is a real value (not blank, not 'null'). */
def boolean isPackageKnown(String basePackage) {
    return basePackage != null && !basePackage.trim().isEmpty() && basePackage.trim() != "null"
}

/**
 * Writes <folder>/<simpleName>.java only when the file does not exist.
 * Returns the written Path, or null when the file was preserved.
 */
def Path writeJavaSourceIfAbsent(Path folder, String simpleName, String source) {
    folder.toFile().mkdirs()

    Path target = folder.resolve(simpleName + '.java')
    if (target.toFile().exists()) return null

    target.toFile().text = source
    return target
}

/**
 * Resolves the source file of a path flag. Absolute paths are used as-is;
 * relative paths are tried against the working directory first and then by
 * walking up from `start` (the generated project root), which is where the
 * archetype integration test runs the generation from.
 */
def File resolveExistingFileWalkingUp(Path start, String rawPath, int maxLevels) {
    if (rawPath == null || rawPath.trim().isEmpty()) return null

    String cleanPath = rawPath.trim()
    File candidate = new File(cleanPath)
    if (candidate.isAbsolute()) {
        return candidate.isFile() ? candidate : null
    }
    if (candidate.isFile()) return candidate

    Path current = start
    for (int level = 0; level < maxLevels; level++) {
        Path resolved = current.resolve(cleanPath)
        if (resolved.toFile().isFile()) return resolved.toFile()
        if (current.parent == null) break
        current = current.parent
    }
    return null
}
