package eda.postgenerate.llmflag

/**
 * Merges LLM-inferred flags, the operator's JSON contract, and the catalog defaults
 * into a final proposal. See spec.md FR-006/007/008/009/021 and data-model.md Entity 4.
 *
 * Precedence (highest wins):
 *   1. JSON contract value (FR-007)
 *   2. Value derived from the contract by the mechanism itself (FR-021: the
 *      path of the materialized contract file)
 *   3. Entity semantics derived from the contract (FR-009 amended: the entity
 *      name, its package, the attribute list and the mandatory
 *      enable_custom_attributes)
 *   4. LLM-inferred value, but only if the flag key is present in the contract
 *      or the flag is on the FR-009 allowlist
 *   5. Catalog defaultValue (FR-008 fills gaps; FR-006 replaces values failing
 *      validationRegex)
 *
 * IMPORTANT: this module is the unit-testable twin of the inlined logic in
 * Orchestrator.groovy (the Orchestrator must stay self-contained: it is
 * executed as a single file by `groovy Orchestrator.groovy` and by gmaven).
 * Every behavioural change MUST be applied to BOTH, otherwise the mechanism
 * and this module diverge. ContractDerivationTest.groovy asserts the parity.
 */
class FlagProposalMerger {

    /**
     * FR-009 (amended): flags whose semantic can be derived from the contract
     * even when the contract does not carry the literal key.
     */
    private static final List<String> FR009_ALLOWLIST = ["entity_name"]

    /**
     * Contract keys that carry meta-information about the module; they are
     * never entity attributes.
     */
    private static final List<String> CONTRACT_META_KEYS = [
            "entityName", "entity", "package", "description", "attributes", "fields"
    ]

    /**
     * Types the entity template can render without emitting an extra import.
     */
    private static final List<String> JAVA_LANG_TYPES = [
            "String", "Integer", "Long", "Double", "Float", "Short", "Byte",
            "Boolean", "Character", "Number", "Object", "StringBuilder", "StringBuffer"
    ]

    /**
     * @param llmInference flags proposed by the external inference script (any flag
     *        mentioned, regardless of whether the contract mentions it)
     * @param contract the operator's JSON contract as a Map (may have nested objects
     *        and arrays; this merger treats it as a flat key set for the top level)
     * @param catalog the canonical flag catalog (keys are valid flag names)
     * @return merged Map<flag, value> ready for the assembler
     */
    Map<String, String> merge(Map<String, String> llmInference,
                               Map<String, Object> contract,
                               Map<String, CatalogEntry> catalog) {
        // Public API preserved: returns only the merged values (FR-020 provenance
        // is available via mergeWithProvenance).
        return (Map<String, String>) mergeWithProvenance(llmInference, contract, catalog).get("values")
    }

    /**
     * FR-020: same precedence as merge(), additionally returning per-flag provenance.
     * @return Map with keys:
     *   "values"    Map<flag, value>            - final merged values
     *   "sources"   Map<flag, source>           - which step won
     *   "notes"     Map<flag, note>             - FR-006 regex rejections
     *   "discarded" Map<flag, value>            - LLM proposals absent from the contract (FR-009)
     *   "warnings"  List<String>                - loud derivation warnings
     */
    Map<String, Object> mergeWithProvenance(Map<String, String> llmInference,
                                            Map<String, Object> contract,
                                            Map<String, CatalogEntry> catalog) {
        return mergeWithProvenance(llmInference, contract, catalog, new LinkedHashMap<String, String>())
    }

    /**
     * FR-021: derivedOverrides carries values the mechanism derives from the
     * contract itself (currently the path of the materialized contract file).
     */
    Map<String, Object> mergeWithProvenance(Map<String, String> llmInference,
                                            Map<String, Object> contract,
                                            Map<String, CatalogEntry> catalog,
                                            Map<String, String> derivedOverrides) {
        if (catalog == null || catalog.isEmpty()) {
            throw new IllegalStateException("FlagProposalMerger: catalog is empty or null")
        }
        Map<String, String> values = new LinkedHashMap<>()
        Map<String, String> sources = new LinkedHashMap<>()
        Map<String, String> notes = new LinkedHashMap<>()

        // The entity semantics are derived BEFORE the merge loop, because the
        // package depends on the resolved entity name.
        Map<String, Object> derived = deriveFromContract(contract, llmInference, catalog)

        // Iterate the catalog in its declared order so the assembler output is deterministic.
        for (Map.Entry<String, CatalogEntry> entry : catalog.entrySet()) {
            String key = entry.key
            CatalogEntry catEntry = entry.value
            String source = null

            // Step 1: contract value (highest precedence, FR-007)
            String value = pickFromContract(key, contract)
            if (value != null) source = "CONTRACT"

            // Step 2: value derived from the contract by the mechanism (FR-021)
            if (value == null && derivedOverrides != null && derivedOverrides.containsKey(key)) {
                value = derivedOverrides.get(key)
                if (value != null) source = "CONTRACT_DERIVED"
            }

            // Step 3: entity semantics derived from the contract
            if (value == null) {
                switch (key) {
                    case "entity_name":
                        if (derived.get("entityName") != null) {
                            value = (String) derived.get("entityName")
                            source = (String) derived.get("entityNameSource")
                        }
                        break
                    case "entity_name_package":
                        if (derived.get("entityName") != null) {
                            String pkg = ((String) derived.get("entityName")).toLowerCase(Locale.ROOT)
                            if (pkg ==~ /^[a-z][a-z0-9_.]+$/) {
                                value = pkg
                                source = "CONTRACT_DERIVED"
                            }
                        }
                        break
                    case "enable_custom_attributes":
                        if (!(derived.get("attributes") as List).isEmpty()) {
                            value = "true"
                            source = "CONTRACT_DERIVED"
                        }
                        break
                    case "custom_attributes_list":
                        if (!(derived.get("attributes") as List).isEmpty()) {
                            value = (String) derived.get("attributeList")
                            source = "CONTRACT_DERIVED"
                        }
                        break
                }
            }

            // Step 4: LLM-inferred (FR-009): eligible when the contract carries
            // the literal key OR the flag is on the allowlist.
            if (value == null && isInferenceEligible(key, contract)) {
                value = llmInference == null ? null : llmInference.get(key)
                if (value != null) source = "INFERENCE"
            }

            // Step 5: catalog default (FR-008)
            if (value == null) {
                value = catEntry.defaultValue
                source = "DEFAULT"
            }

            // FR-006: replace with default if value fails validationRegex
            if (value != null && !catEntry.matchesRegex(value)) {
                System.err.println(
                    "ERROR: Flag '" + key + "' value '" + value +
                    "' fails validation regex '" + catEntry.validationRegex +
                    "'. Default value '" + catEntry.defaultValue + "' will be used instead."
                )
                notes.put(key, "valor '" + value + "' rejeitado pela regex; default aplicado (FR-006)")
                value = catEntry.defaultValue
                source = "DEFAULT"
            }

            values.put(key, value)
            sources.put(key, source)
        }

        // FR-009/FR-020: LLM proposals whose key is absent from the contract
        // are discarded - unless the key is on the allowlist AND the proposal
        // was accepted.
        Map<String, String> discarded = new LinkedHashMap<>()
        if (llmInference != null) {
            for (Map.Entry<String, String> e : llmInference.entrySet()) {
                if (contract == null || !contract.containsKey(e.key)) {
                    if (FR009_ALLOWLIST.contains(e.key) && "INFERENCE".equals(sources.get(e.key))) {
                        continue
                    }
                    discarded.put(e.key, e.value)
                }
            }
        }

        List<String> warnings = (List<String>) derived.get("warnings")
        if (warnings != null) {
            for (String w : warnings) {
                System.err.println("WARNING: " + w)
            }
        }

        Map<String, Object> out = new LinkedHashMap<>()
        out.put("values", values)
        out.put("sources", sources)
        out.put("notes", notes)
        out.put("discarded", discarded)
        out.put("warnings", warnings == null ? new ArrayList<String>() : warnings)
        return out
    }

    /**
     * FR-009 (amended): a flag proposed by the LLM is eligible when the
     * contract carries the literal key, or when the flag is on the allowlist.
     */
    private boolean isInferenceEligible(String key, Map<String, Object> contract) {
        if (FR009_ALLOWLIST.contains(key)) return true
        return contract != null && contract.containsKey(key)
    }

    /**
     * Derives the entity semantics from the contract: the entity name, the
     * entity attributes (type estimated from the VALUE) and the strict
     * "Type:name,Type:name" list expected by the catalog.
     */
    private Map<String, Object> deriveFromContract(Map<String, Object> contract,
                                                   Map<String, String> llmInference,
                                                   Map<String, CatalogEntry> catalog) {
        Map<String, Object> derived = new LinkedHashMap<>()
        List<String> warnings = new ArrayList<>()
        derived.put("warnings", warnings)

        // ---- entity name -------------------------------------------------
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
        derived.put("entityName", entityName)
        derived.put("entityNameSource", entityNameSource)

        // ---- attributes --------------------------------------------------
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
                    rawAttributes.add([name: name?.toString(), value: value, type: em.get("type")])
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
                if (catalog.containsKey(key)) continue
                if (CONTRACT_META_KEYS.contains(key)) continue
                rawAttributes.add([name: key, value: entry.value, type: null])
            }
        }

        List<Map<String, Object>> attributes = new ArrayList<>()
        List<String> parts = new ArrayList<>()
        for (Map<String, Object> raw : rawAttributes) {
            String name = sanitizeAttributeName((String) raw.get("name"))
            if (name == null) {
                warnings.add("atributo descartado: nome invalido '" + raw.get("name") + "'")
                continue
            }
            String type = null
            Object declared = raw.get("type")
            if (declared != null) {
                type = normalizeType(declared.toString())
                if (type == null) {
                    warnings.add("atributo '" + name + "': tipo declarado '" + declared +
                            "' nao e um tipo de java.lang - tipo estimado pelo valor")
                    type = null
                }
            }
            if (type == null) type = estimateType(raw.get("value"))
            attributes.add([name: name, type: type])
            parts.add(type + ":" + name)
        }
        derived.put("attributes", attributes)
        derived.put("attributeList", parts.join(","))
        return derived
    }

    /**
     * Normalizes a declared attribute type: FQN -> simple name, generics
     * dropped. Returns null when the type is not a java.lang type.
     */
    private String normalizeType(String raw) {
        if (raw == null) return null
        String t = raw.trim()
        if (t.isEmpty()) return null
        if (t.contains(".")) t = t.substring(t.lastIndexOf(".") + 1)
        if (t.contains("<")) t = t.substring(0, t.indexOf("<"))
        if (!JAVA_LANG_TYPES.contains(t)) return null
        return t
    }

    /**
     * Estimates the attribute type from the VALUE carried by the contract.
     */
    private String estimateType(Object value) {
        if (value == null) return "String"
        if (value instanceof Boolean) return "Boolean"
        if (value instanceof Integer || value instanceof Short || value instanceof Byte) return "Integer"
        if (value instanceof Long || value instanceof java.math.BigInteger) return "Long"
        if (value instanceof java.math.BigDecimal || value instanceof Double || value instanceof Float) return "Double"
        return "String"
    }

    /**
     * Normalizes an attribute name into a valid Java identifier that also
     * satisfies the catalog regex ^[a-z][a-zA-Z0-9_]*$.
     */
    private String sanitizeAttributeName(String raw) {
        if (raw == null) return null
        String s = raw.trim().replaceAll("[^A-Za-z0-9_]", "_")
        if (s.isEmpty()) return null
        if (Character.isDigit(s.charAt(0))) s = "field_" + s
        if (Character.isUpperCase(s.charAt(0))) s = Character.toLowerCase(s.charAt(0)) + s.substring(1)
        if (!(s ==~ /^[a-z][a-zA-Z0-9_]*$/)) return null
        return s
    }

    private String pickFromContract(String key, Map<String, Object> contract) {
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
