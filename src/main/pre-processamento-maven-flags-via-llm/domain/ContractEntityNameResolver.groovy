package eda.postgenerate.pre_processamento_maven_flags_via_llm.domain

/**
 * Resolves the entity name from the operator's contract and, as a fallback,
 * from the LLM proposal (FR-009 allowlist).
 *
 * Responsibility: name resolution precedence only.
 */
class ContractEntityNameResolver {

    Map<String, Object> resolve(Map<String, Object> contract, Map<String, String> llmInference) {
        String entityName = null
        String entityNameSource = null
        if (contract != null) {
            for (String metaKey : ["entity_name", "entityName", "entity"]) {
                Object raw = contract.get(metaKey)
                if (raw instanceof String && !((String) raw).trim().isEmpty()) {
                    entityName = ((String) raw).trim()
                    entityNameSource = "CONTRACT"
                    break
                }
            }
        }
        if (entityName == null && llmInference != null) {
            String proposed = llmInference.get("entity_name")
            if (proposed != null && !proposed.trim().isEmpty()) {
                entityName = proposed.trim()
                entityNameSource = "INFERENCE"
            }
        }
        return [entityName: entityName, entityNameSource: entityNameSource]
    }
}
