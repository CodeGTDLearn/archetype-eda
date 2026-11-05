package eda.postgenerate.pre_processamento_maven_flags_via_llm.domain

/**
 * Extracts the raw attribute descriptions from the contract.
 *
 * Responsibility: contract shape adaptation (explicit attributes, fields, or
 * flat business fields) only. It does not resolve types, sanitize names or
 * serialize flags.
 */
class ContractAttributeExtractor {

    private static final List<String> CONTRACT_META_KEYS = [
            "entityName", "entity", "package", "description", "attributes", "fields"
    ]

    List<Map<String, Object>> extract(Map<String, Object> contract, Set<String> catalogKeys) {
        List<Map<String, Object>> rawAttributes = new ArrayList<>()
        Object attrs = contract == null ? null : contract.get("attributes")
        if (attrs == null && contract != null) attrs = contract.get("fields")

        if (attrs instanceof Map) {
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) attrs).entrySet()) {
                rawAttributes.add([name: entry.key?.toString(), value: entry.value, type: null])
            }
        } else if (attrs instanceof List) {
            for (Object element : (List<Object>) attrs) {
                if (element instanceof Map) {
                    Map<?, ?> em = (Map<?, ?>) element
                    Object name = em.get("name")
                    if (name == null) name = em.get("attribute")
                    if (name == null) name = em.get("field")
                    Object value = null
                    if (em.containsKey("value")) value = em.get("value")
                    else if (em.containsKey("default")) value = em.get("default")
                    // The explicit enum constants of the attribute (when declared).
                    Object enumValues = em.get("enumValues")
                    if (enumValues == null) enumValues = em.get("values")
                    rawAttributes.add([name: name?.toString(), value: value, type: em.get("type"),
                                       enumValues: enumValues])
                } else if (element instanceof String) {
                    rawAttributes.add([name: (String) element, value: null, type: null])
                }
            }
        } else if (attrs == null && contract != null) {
            // No explicit attribute collection: the business fields of the
            // contract (top-level keys that are neither catalog flags nor
            // meta keys) ARE the entity attributes.
            for (Map.Entry<?, ?> entry : contract.entrySet()) {
                String key = entry.key?.toString()
                if (key == null) continue
                if (catalogKeys != null && catalogKeys.contains(key)) continue
                if (CONTRACT_META_KEYS.contains(key)) continue
                rawAttributes.add([name: key, value: entry.value, type: null])
            }
        }
        return rawAttributes
    }
}
