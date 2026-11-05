package eda.postgenerate.pre_processamento_maven_flags_via_llm.cache

import eda.postgenerate.pre_processamento_maven_flags_via_llm.catalog.CatalogEntry
import eda.postgenerate.pre_processamento_maven_flags_via_llm.Sha256

/**
 * Derives the canonical contract representation and the cache key.
 *
 * Responsibility: cache-key policy only (canonicalization delegation,
 * catalog fingerprint and deterministic digest).
 */
class CacheKeyFactory {

    private final JsonCanonicalizer canonicalizer

    CacheKeyFactory(JsonCanonicalizer canonicalizer) {
        if (canonicalizer == null) {
            throw new IllegalArgumentException("CacheKeyFactory: canonicalizer MUST be non-null")
        }
        this.canonicalizer = canonicalizer
    }

    String canonicalize(Object value) {
        return canonicalizer.canonicalize(value)
    }

    /**
     * Stable fingerprint of the flag catalog: SHA-256 over every flag's key,
     * default value and validation rule, in catalog order.
     */
    String fingerprint(Map<String, CatalogEntry> catalog) {
        if (catalog == null || catalog.isEmpty()) return ""
        StringBuilder sb = new StringBuilder()
        for (Map.Entry<String, CatalogEntry> e : catalog.entrySet()) {
            CatalogEntry entry = e.getValue()
            sb.append(e.getKey()).append('\u0001')
              .append(entry == null ? null : entry.defaultValue).append('\u0001')
              .append(entry == null ? null : entry.validationRegex).append('\u0002')
        }
        return Sha256.hex(sb.toString())
    }

    String makeKey(String description, String canonicalContract, String catalogFingerprint) {
        String payload = (catalogFingerprint == null ? "" : catalogFingerprint) + "\u0000" +
                         (description == null ? "" : description) + "\u0000" +
                         (canonicalContract == null ? "" : canonicalContract)
        return Sha256.hex(payload)
    }
}
