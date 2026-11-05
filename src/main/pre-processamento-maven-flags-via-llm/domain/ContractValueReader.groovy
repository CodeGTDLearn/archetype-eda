package eda.postgenerate.pre_processamento_maven_flags_via_llm.domain

/**
 * Reads a flag value from the operator's contract.
 *
 * Responsibility: contract value extraction and serialization of nested
 * objects/arrays. The merge precedence does not live here.
 */
class ContractValueReader {

    String pick(String key, Map<String, Object> contract) {
        if (contract == null) return null
        Object raw = contract.get(key)
        if (raw == null) return null
        if (raw instanceof String) return (String) raw
        if (raw instanceof Number || raw instanceof Boolean) return raw.toString()
        // For nested objects or arrays, return the canonical JSON string.
        // The assembler treats the result verbatim.
        try {
            return new groovy.json.JsonOutput().toJson(raw)
        } catch (Exception e) {
            return raw.toString()
        }
    }
}
