package eda.postgenerate.pre_processamento_maven_flags_via_llm.contract

/**
 * The outcome of persisting the contract: the materialized path (or null) and
 * an optional warning for the presentation layer.
 */
class MaterializationResult {
    final String path
    final String warning

    MaterializationResult(String path, String warning) {
        this.path = path
        this.warning = warning
    }
}
