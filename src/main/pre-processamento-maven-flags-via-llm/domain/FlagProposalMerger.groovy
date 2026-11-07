package eda.postgenerate.pre_processamento_maven_flags_via_llm.domain

import eda.postgenerate.pre_processamento_maven_flags_via_llm.catalog.CatalogEntry

/**
 * Merges LLM-inferred flags, the operator's JSON contract and the catalog
 * defaults into a final proposal, with per-flag provenance.
 *
 * Precedence (highest wins):
 *   1. JSON contract value (FR-007)
 *   2. Value derived from the contract by the mechanism itself (FR-021/FR-023)
 *   3. Entity semantics derived from the contract (FR-009 amended/FR-021)
 *   4. LLM-inferred value, only when the flag key is in the contract or on the
 *      FR-009 allowlist
 *   5. Catalog defaultValue (FR-008 fills gaps; FR-006 replaces values that
 *      fail the catalog's validationRegex)
 *
 * Responsibilities are intentionally delegated:
 *   - ContractEntityDeriver         - contract -> entity semantics;
 *   - ContractValueReader           - contract value extraction;
 *   - FlagValidationPolicy          - FR-006 validation;
 *   - DerivedFlagProjector          - PROJECT-type metadata -> derived flags;
 *   - InferenceEligibilityPolicy    - FR-009 eligibility.
 *
 * This facade owns only the precedence loop and the provenance maps. It does
 * not print: warnings and FR-006 diagnostics are returned to the caller.
 */
class FlagProposalMerger {

    private final ContractEntityDeriver deriver
    private final ContractValueReader valueReader
    private final FlagValidationPolicy validationPolicy
    private final DerivedFlagProjector derivedFlagProjector
    private final NestedTypeFlagProjector nestedTypeFlagProjector
    private final InferenceEligibilityPolicy inferenceEligibilityPolicy

    FlagProposalMerger(ContractEntityDeriver deriver) {
        if (deriver == null) {
            throw new IllegalArgumentException("FlagProposalMerger: deriver MUST be non-null")
        }
        this.deriver = deriver
        this.valueReader = new ContractValueReader()
        this.validationPolicy = new FlagValidationPolicy()
        this.derivedFlagProjector = new DerivedFlagProjector(validationPolicy)
        this.nestedTypeFlagProjector = new NestedTypeFlagProjector(validationPolicy)
        this.inferenceEligibilityPolicy = new InferenceEligibilityPolicy()
    }

    /**
     * Public API preserved: returns only the merged values (FR-020 provenance
     * is available via mergeWithProvenance).
     */
    Map<String, String> merge(Map<String, String> llmInference,
                               Map<String, Object> contract,
                               Map<String, CatalogEntry> catalog) {
        return (Map<String, String>) mergeWithProvenance(llmInference, contract, catalog).get("values")
    }

    /**
     * FR-020: same precedence as merge(), additionally returning per-flag provenance.
     * @return Map with keys:
     *   "values"      Map<flag, value> - final merged values
     *   "sources"     Map<flag, source> - which step won
     *   "notes"       Map<flag, note> - FR-006 regex rejections
     *   "discarded"   Map<flag, value> - LLM proposals absent from the contract (FR-009)
     *   "warnings"    List<String> - loud derivation warnings
     *   "diagnostics" List<String> - FR-006 error messages for the presentation layer
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
        List<String> diagnostics = new ArrayList<>()

        // The entity semantics are derived BEFORE the merge loop, because the
        // package depends on the resolved entity name.
        Map<String, Object> derived = deriver.deriveFromContract(contract, llmInference, catalog)

        // Iterate the catalog in its declared order so the assembler output is deterministic.
        for (Map.Entry<String, CatalogEntry> entry : catalog.entrySet()) {
            String key = entry.key
            CatalogEntry catEntry = entry.value
            String source = null

            // Step 1: contract value (highest precedence, FR-007)
            String value = valueReader.pick(key, contract)
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

            // Step 4: LLM-inferred (FR-009). A proposal is eligible when the
            // contract carries the literal key OR the key is on the allowlist.
            if (value == null && inferenceEligibilityPolicy.isEligible(key, contract)) {
                value = llmInference == null ? null : llmInference.get(key)
                if (value != null) source = "INFERENCE"
            }

            // Step 5: catalog default (FR-008)
            if (value == null) {
                value = catEntry.defaultValue
                source = "DEFAULT"
            }

            // FR-006: replace with default if value fails validationRegex
            if (value != null) {
                ValidationResult validation = validationPolicy.validate(catEntry, value)
                if (validation.rejected) {
                    notes.put(key, validation.note)
                    if (validation.error != null) diagnostics.add(validation.error)
                    value = validation.value
                    source = "DEFAULT"
                }
            }

            values.put(key, value)
            sources.put(key, source)
        }

        // The entity semantics may carry PROJECT types (types outside
        // java.lang). They are referenced by their simple name in the entity,
        // so the metadata the archetype needs travels in two derived flags.
        derivedFlagProjector.apply(values, sources, notes, catalog, derived, diagnostics)
        // The nested POJOs are projected AFTER the entity list is final: the
        // flag is suppressed when the entity does not reference a nested type.
        nestedTypeFlagProjector.apply(values, sources, notes, catalog, derived, diagnostics)

        // FR-009/FR-020: LLM proposals whose key is absent from the contract
        // are discarded - unless the key is on the allowlist AND the proposal
        // was accepted.
        Map<String, String> discarded = new LinkedHashMap<>()
        if (llmInference != null) {
            for (Map.Entry<String, String> e : llmInference.entrySet()) {
                if (contract == null || !contract.containsKey(e.key)) {
                    if (inferenceEligibilityPolicy.isAllowlisted(e.key) && "INFERENCE".equals(sources.get(e.key))) {
                        continue
                    }
                    discarded.put(e.key, e.value)
                }
            }
        }

        List<String> warnings = (List<String>) derived.get("warnings")

        Map<String, Object> out = new LinkedHashMap<>()
        out.put("values", values)
        out.put("sources", sources)
        out.put("notes", notes)
        out.put("discarded", discarded)
        out.put("warnings", warnings == null ? new ArrayList<String>() : warnings)
        out.put("diagnostics", diagnostics)
        return out
    }
}
