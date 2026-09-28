// -----------------------------------------------------------------------------








// SNIPPET-ENTITY-CONTRACT.GROOVY ---------------------------------------------
// FR-021 (contract persistence): the contract that was extracted from the
// prompt - the base of the module - is copied into the generated project:
//
//     src/test/resources/contracts/contract.json
//
// The generated module therefore carries the contract it was built from, so
// that a future schema test can assert that the entity class still matches
// that contract.
//
// Guarded by enable_folder_src_test_java: the contracts folder exists only
// when the test tree is kept (snippet-src-test-java deletes
// src/test/resources entirely when the flag is false). The file name ends in
// .json on purpose: snippet-properties-filetype deletes .yml/.properties
// recursively under src/test/resources.
//
// The source path comes from the LLM inference mechanism (flag
// entity_contract_path). The value "none" means the module was generated
// without the mechanism - there is no contract to persist.

def resolveEntityContractSource = { String rawPath ->
    if (rawPath == null || rawPath.trim().isEmpty()) return null
    String cleanPath = rawPath.trim()
    File candidate = new File(cleanPath)
    if (candidate.isAbsolute()) {
        return candidate.isFile() ? candidate : null
    }
    // Relative path: try the working directory first, then walk up from the
    // project root (the archetype integration test runs the generation from
    // the archetype project, not from the generated project). The bound is
    // generous enough to reach the archetype project root from the deepest
    // generated-project layout (target/test-classes/projects/<p>/project/<a>).
    if (candidate.isFile()) return candidate
    Path current = projectRootFolder
    for (int level = 0; level < 12; level++) {
        Path resolved = current.resolve(cleanPath)
        if (resolved.toFile().isFile()) return resolved.toFile()
        if (current.parent == null) break
        current = current.parent
    }
    return null
}

def entity_contract_path_value = "${properties?.entity_contract_path}"

if (keep_src_test_java == 'true' && entity_contract_path_value != null &&
        !entity_contract_path_value.trim().isEmpty() && entity_contract_path_value.trim() != 'none') {

    def entityContractSourceFile = resolveEntityContractSource(entity_contract_path_value)

    if (entityContractSourceFile == null) {
        log_text += "\nENTITY_CONTRACT: source not found for '" + entity_contract_path_value +
                "' - the contract was NOT persisted\n"
        println "WARNING: entity_contract_path '" + entity_contract_path_value +
                "' does not exist - the contract was NOT persisted."
    } else {
        Path entityContractsFolder = srcTestResources.resolve("contracts")
        entityContractsFolder.toFile().mkdirs()
        Path entityContractTarget = entityContractsFolder.resolve("contract.json")
        entityContractTarget.toFile().bytes = entityContractSourceFile.bytes
        log_text += "\nENTITY_CONTRACT: " + entityContractSourceFile +
                "\n            -> " + entityContractTarget + "\n"
        println "Entity contract persisted: " + entityContractSourceFile + " -> " + entityContractTarget
    }
}
