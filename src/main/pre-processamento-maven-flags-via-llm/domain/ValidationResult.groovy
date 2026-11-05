package eda.postgenerate.pre_processamento_maven_flags_via_llm.domain

/**
 * The result of applying FR-006 to one flag value.
 */
class ValidationResult {
    final String value
    final boolean rejected
    final String note
    final String error

    ValidationResult(String value, boolean rejected, String note, String error) {
        this.value = value
        this.rejected = rejected
        this.note = note
        this.error = error
    }
}
