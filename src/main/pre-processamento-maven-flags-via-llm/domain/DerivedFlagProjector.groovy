package eda.postgenerate.pre_processamento_maven_flags_via_llm.domain

import eda.postgenerate.pre_processamento_maven_flags_via_llm.catalog.CatalogEntry

/**
 * Projects the PROJECT-type metadata derived from the contract into the two
 * catalog flags the archetype consumes:
 *   - custom_attribute_imports: the FQNs of the referenced types;
 *   - custom_enum_definitions: "Type:CONSTANT|CONSTANT", separated by ';'.
 *
 * Responsibility: derived-flag projection and serialization only. It does
 * not print; FR-006 diagnostics are collected for the presentation layer.
 */
class DerivedFlagProjector {

    private final FlagValidationPolicy validationPolicy

    DerivedFlagProjector(FlagValidationPolicy validationPolicy) {
        if (validationPolicy == null) {
            throw new IllegalArgumentException("DerivedFlagProjector: validationPolicy MUST be non-null")
        }
        this.validationPolicy = validationPolicy
    }

    void apply(Map<String, String> values, Map<String, String> sources,
               Map<String, String> notes, Map<String, CatalogEntry> catalog,
               Map<String, Object> derived, List<String> diagnostics) {
        List<Map<String, Object>> details = (List<Map<String, Object>>) derived.get("attributeDetails")
        if (details == null || details.isEmpty()) return

        List<String> imports = new ArrayList<String>()
        Map<String, List<String>> enums = new LinkedHashMap<String, List<String>>()
        Map<String, String> simpleNames = new LinkedHashMap<String, String>()
        List<String> warnings = (List<String>) derived.get("warnings")
        for (Map<String, Object> detail : details) {
            if (!(Boolean) detail.get("projectType")) continue
            String qualified = (String) detail.get("qualifiedType")
            String type = (String) detail.get("type")
            if (qualified != null && qualified.contains(".") && !imports.contains(qualified)) {
                imports.add(qualified)
                // Two distinct types with the same simple name cannot both be
                // imported: the entity would not compile. The clash is loud.
                String simple = qualified.substring(qualified.lastIndexOf(".") + 1)
                String known = simpleNames.get(simple)
                if (known == null) {
                    simpleNames.put(simple, qualified)
                } else if (known != qualified && warnings != null) {
                    warnings.add("tipos com o mesmo nome simples e pacotes diferentes: '" +
                            known + "' e '" + qualified + "' - a entidade pode nao compilar")
                }
            }
            if ((Boolean) detail.get("enumType")) {
                String key = (qualified != null && qualified.contains(".")) ? qualified : type
                if (key == null) continue
                List<String> constants = enums.get(key)
                if (constants == null) {
                    constants = new ArrayList<String>()
                    enums.put(key, constants)
                }
                for (Object constant : (List<Object>) detail.get("enumConstants")) {
                    if (!constants.contains(constant)) constants.add((String) constant)
                }
            }
        }

        List<String> enumDefinitions = new ArrayList<String>()
        for (Map.Entry<String, List<String>> entry : enums.entrySet()) {
            enumDefinitions.add(entry.getKey() + ":" + entry.getValue().join("|"))
        }
        applyDerivedFlag(values, sources, notes, catalog, "custom_attribute_imports",
                imports.isEmpty() ? "none" : imports.join(","), diagnostics)
        applyDerivedFlag(values, sources, notes, catalog, "custom_enum_definitions",
                enumDefinitions.isEmpty() ? "none" : enumDefinitions.join(";"), diagnostics)
    }

    /**
     * Applies a derived flag value: the operator's explicit value (CONTRACT)
     * always wins; a value that violates the catalog rule falls back to the
     * catalog default (FR-006); otherwise the derived value is emitted with
     * the CONTRACT_DERIVED provenance.
     */
    private void applyDerivedFlag(Map<String, String> values, Map<String, String> sources,
                                  Map<String, String> notes, Map<String, CatalogEntry> catalog,
                                  String key, String derivedValue, List<String> diagnostics) {
        if (values == null || !values.containsKey(key)) return
        if ("CONTRACT".equals(sources.get(key))) return
        CatalogEntry entry = catalog.get(key)
        if (entry == null) return
        ValidationResult result = validationPolicy.validateDerived(entry, derivedValue)
        if (result.rejected) {
            notes.put(key, result.note)
            if (result.error != null && diagnostics != null) diagnostics.add(result.error)
            values.put(key, result.value)
            sources.put(key, "DEFAULT")
            return
        }
        values.put(key, derivedValue)
        sources.put(key, "CONTRACT_DERIVED")
    }
}
