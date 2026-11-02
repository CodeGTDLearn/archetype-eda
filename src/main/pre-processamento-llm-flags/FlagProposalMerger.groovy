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
 *
 * RESPONSIBILITY: this module merges the flags (precedence, FR-006 validation
 * and FR-020 provenance). The DERIVATION of the entity from the contract is
 * delegated to ContractEntityDeriver, which in turn delegates the type
 * inference to ContractTypeResolver. Both are INJECTED through the
 * constructor because a sibling source file is not resolvable at compile time
 * in this build (see Orchestrator.groovy for the rationale).
 */
class FlagProposalMerger {

    /**
     * The component that derives the entity semantics (name, attributes and
     * the attribute list) from the contract. Injected, never constructed here.
     */
    private final ContractEntityDeriver deriver

    FlagProposalMerger(ContractEntityDeriver deriver) {
        if (deriver == null) {
            throw new IllegalArgumentException("FlagProposalMerger: deriver MUST be non-null")
        }
        this.deriver = deriver
    }

    /**
     * FR-009 (amended): flags whose semantic can be derived from the contract
     * even when the contract does not carry the literal key.
     */
    private static final List<String> FR009_ALLOWLIST = ["entity_name"]

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
        Map<String, Object> derived = deriver.deriveFromContract(contract, llmInference, catalog)

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

        // The entity semantics may carry PROJECT types (types outside
        // java.lang). They are referenced by their simple name in the entity,
        // so the metadata the archetype needs travels in two derived flags:
        // the imports of the referenced types and the definitions of the
        // enums it must generate.
        applyDerivedTypeFlags(values, sources, notes, catalog, derived)

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
     * Derives the flags that carry the metadata of the PROJECT types:
     *   - custom_attribute_imports: the FQNs of the referenced types, so the
     *     entity template can import them;
     *   - custom_enum_definitions: the enums the archetype must generate
     *     ("Type:CONSTANT|CONSTANT", separated by ';').
     */
    private void applyDerivedTypeFlags(Map<String, String> values, Map<String, String> sources,
                                       Map<String, String> notes, Map<String, CatalogEntry> catalog,
                                       Map<String, Object> derived) {
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
                imports.isEmpty() ? "none" : imports.join(","))
        applyDerivedFlag(values, sources, notes, catalog, "custom_enum_definitions",
                enumDefinitions.isEmpty() ? "none" : enumDefinitions.join(";"))
    }

    /**
     * Applies a derived flag value: the operator's explicit value (CONTRACT)
     * always wins; a value that violates the catalog rule falls back to the
     * catalog default (FR-006); otherwise the derived value is emitted with
     * the CONTRACT_DERIVED provenance.
     */
    private void applyDerivedFlag(Map<String, String> values, Map<String, String> sources,
                                  Map<String, String> notes, Map<String, CatalogEntry> catalog,
                                  String key, String derivedValue) {
        if (values == null || !values.containsKey(key)) return
        if ("CONTRACT".equals(sources.get(key))) return
        CatalogEntry entry = catalog.get(key)
        if (entry == null) return
        if (entry.validationRegex != null && !entry.validationRegex.isEmpty()
                && !(derivedValue ==~ entry.validationRegex)) {
            System.err.println(
                    "ERROR: Flag '" + key + "' value '" + derivedValue +
                            "' fails validation regex '" + entry.validationRegex +
                            "'. Default value '" + entry.defaultValue + "' will be used instead."
            )
            notes.put(key, "valor derivado '" + derivedValue + "' rejeitado pela regex; default aplicado (FR-006)")
            values.put(key, entry.defaultValue)
            sources.put(key, "DEFAULT")
            return
        }
        values.put(key, derivedValue)
        sources.put(key, "CONTRACT_DERIVED")
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
