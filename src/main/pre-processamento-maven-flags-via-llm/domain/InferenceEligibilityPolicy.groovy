package eda.postgenerate.pre_processamento_maven_flags_via_llm.domain

/**
 * Decides whether an LLM proposal is eligible under FR-009 (amended).
 *
 * Responsibility: eligibility policy only. The allowlist currently contains
 * `entity_name` because its semantic is derivable from the contract.
 */
class InferenceEligibilityPolicy {

    private static final List<String> FR009_ALLOWLIST = ["entity_name"]

    boolean isEligible(String key, Map<String, Object> contract) {
        if (isAllowlisted(key)) return true
        return contract != null && contract.containsKey(key)
    }

    boolean isAllowlisted(String key) {
        return FR009_ALLOWLIST.contains(key)
    }
}
