package eda.postgenerate.pre_processamento_maven_flags_via_llm.domain

/**
 * Serializes the resolved attributes into the catalog's wire format
 * "Type:name,Type:name".
 *
 * Responsibility: formatting only.
 */
class TypeNameListFormatter {

    String format(List<Map<String, Object>> attributes) {
        return attributes.collect { it.get("type") + ":" + it.get("name") }.join(",")
    }
}
