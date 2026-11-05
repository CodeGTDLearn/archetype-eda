package eda.postgenerate.pre_processamento_maven_flags_via_llm.domain

import eda.postgenerate.pre_processamento_maven_flags_via_llm.catalog.CatalogEntry

/**
 * Applies FR-006: a value that violates the catalog's validationRegex is
 * replaced by the catalog default and reported with a note.
 *
 * Responsibility: validation policy only. It does not print; the caller
 * collects the diagnostics and the presentation layer emits them.
 */
class FlagValidationPolicy {

    ValidationResult validate(CatalogEntry entry, String value) {
        if (value != null && !entry.matchesRegex(value)) {
            String note = "valor '" + value + "' rejeitado pela regex; default aplicado (FR-006)"
            String error = errorMessage(entry, value)
            return new ValidationResult(entry.defaultValue, true, note, error)
        }
        return new ValidationResult(value, false, null, null)
    }

    ValidationResult validateDerived(CatalogEntry entry, String value) {
        if (value != null && !entry.matchesRegex(value)) {
            String note = "valor derivado '" + value + "' rejeitado pela regex; default aplicado (FR-006)"
            String error = errorMessage(entry, value)
            return new ValidationResult(entry.defaultValue, true, note, error)
        }
        return new ValidationResult(value, false, null, null)
    }

    private String errorMessage(CatalogEntry entry, String value) {
        return "ERROR: Flag '" + entry.key + "' value '" + value +
                "' fails validation regex '" + entry.validationRegex +
                "'. Default value '" + entry.defaultValue + "' will be used instead."
    }
}
