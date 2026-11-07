package eda.postgenerate.pre_processamento_maven_flags_via_llm.domain

import eda.postgenerate.pre_processamento_maven_flags_via_llm.catalog.CatalogEntry

/**
 * Projects the nested POJOs derived from the contract into the catalog flag
 * the archetype consumes:
 *
 *   custom_nested_type_definitions:
 *       "Tipo|Campo:Tipo,Campo:Tipo;Tipo|Campo:Tipo"
 *
 * Suppression rules:
 *   - enable_custom_attributes=false                          -> none
 *   - custom_attributes_list=false/empty                      -> none
 *   - an explicit list that references no nested type         -> none
 *   - an explicit custom_nested_type_definitions always wins (CONTRACT)
 *
 * The flag is suppressed (never emitted) when the entity does not reference
 * a nested type. The entity template imports/marks only the intersection.
 */
class NestedTypeFlagProjector {

    static final String FLAG = "custom_nested_type_definitions"

    private final FlagValidationPolicy validationPolicy

    NestedTypeFlagProjector(FlagValidationPolicy validationPolicy) {
        if (validationPolicy == null) {
            throw new IllegalArgumentException("NestedTypeFlagProjector: validationPolicy MUST be non-null")
        }
        this.validationPolicy = validationPolicy
    }

    void apply(Map<String, String> values, Map<String, String> sources,
               Map<String, String> notes, Map<String, CatalogEntry> catalog,
               Map<String, Object> derived, List<String> diagnostics) {
        if (values == null || catalog == null || !catalog.containsKey(FLAG)) return
        if (!values.containsKey(FLAG)) return
        // The operator's explicit value always wins.
        if ("CONTRACT".equals(sources.get(FLAG))) return

        List<Map<String, Object>> nestedTypes = derived == null
                ? null : (List<Map<String, Object>>) derived.get("nestedTypes")
        if (nestedTypes == null || nestedTypes.isEmpty()) return

        String derivedValue = "none"
        String list = values.get("custom_attributes_list")
        boolean enabled = "true".equals(values.get("enable_custom_attributes"))
        if (enabled && list != null) {
            String trimmed = list.trim()
            if (!trimmed.isEmpty() && trimmed != "false" && trimmed != "none") {
                Set<String> referenced = referencedTypes(trimmed)
                Set<String> nestedNames = new LinkedHashSet<String>()
                for (Map<String, Object> node : nestedTypes) {
                    Object nestedName = node.get("name")
                    if (nestedName != null) nestedNames.add(nestedName.toString())
                }
                for (String type : referenced) {
                    if (nestedNames.contains(type)) {
                        derivedValue = format(nestedTypes)
                        break
                    }
                }
            }
        }

        CatalogEntry entry = catalog.get(FLAG)
        ValidationResult result = validationPolicy.validateDerived(entry, derivedValue)
        if (result.rejected) {
            notes.put(FLAG, result.note)
            if (result.error != null && diagnostics != null) diagnostics.add(result.error)
            values.put(FLAG, result.value)
            sources.put(FLAG, "DEFAULT")
            return
        }
        values.put(FLAG, derivedValue)
        sources.put(FLAG, "CONTRACT_DERIVED")
    }

    /**
     * Serializes the derived types in their deterministic order.
     */
    String format(List<Map<String, Object>> nestedTypes) {
        List<String> definitions = new ArrayList<String>()
        for (Map<String, Object> node : nestedTypes) {
            Object rawName = node.get("name")
            if (rawName == null) continue
            String name = rawName.toString()
            List<Map<String, Object>> fields = (List<Map<String, Object>>) node.get("fields")
            if (name.isEmpty() || fields == null || fields.isEmpty()) continue
            List<String> rendered = new ArrayList<String>()
            for (Map<String, Object> field : fields) {
                Object fieldName = field.get("name")
                Object fieldType = field.get("type")
                if (fieldName == null || fieldType == null) continue
                rendered.add(fieldName.toString() + ":" + fieldType.toString())
            }
            if (rendered.isEmpty()) continue
            definitions.add(name + "|" + rendered.join(","))
        }
        return definitions.isEmpty() ? "none" : definitions.join(";")
    }

    private Set<String> referencedTypes(String list) {
        Set<String> out = new LinkedHashSet<String>()
        for (String entry : list.split(",")) {
            String trimmed = entry.trim()
            if (trimmed.isEmpty()) continue
            int separator = trimmed.indexOf(":")
            if (separator <= 0) continue
            String type = trimmed.substring(0, separator).trim()
            if (!type.isEmpty()) out.add(type)
        }
        return out
    }
}
