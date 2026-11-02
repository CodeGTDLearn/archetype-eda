package eda.postgenerate.llmflag

/**
 * A single flag entry from the canonical archetype metadata.
 * See data-model.md Entity 3.
 *
 * Construction invariants (enforced by spec FR-018):
 *   - key MUST be non-empty
 *   - defaultValue MUST be non-empty
 *   - if validationRegex is present, it MUST compile as a valid java.util.regex.Pattern
 */
class CatalogEntry {
    final String key
    final String defaultValue
    final String validationRegex
    final String description

    CatalogEntry(String key, String defaultValue, String validationRegex, String description) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("CatalogEntry: key MUST be non-empty")
        }
        if (defaultValue == null || defaultValue.isEmpty()) {
            throw new IllegalArgumentException("CatalogEntry '${key}': defaultValue MUST be non-empty per FR-018")
        }
        if (validationRegex != null && !validationRegex.isEmpty()) {
            try {
                java.util.regex.Pattern.compile(validationRegex)
            } catch (Exception e) {
                throw new IllegalArgumentException("CatalogEntry '${key}': validationRegex '${validationRegex}' does not compile: ${e.message}")
            }
        }
        this.key = key
        this.defaultValue = defaultValue
        this.validationRegex = (validationRegex == null || validationRegex.isEmpty()) ? null : validationRegex
        this.description = description == null ? "" : description
    }

    boolean matchesRegex(String value) {
        if (validationRegex == null) return true
        return value ==~ validationRegex
    }
}
