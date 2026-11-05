package eda.postgenerate.pre_processamento_maven_flags_via_llm.domain

/**
 * Facade for the ENTITY semantics derived from the operator's JSON contract.
 *
 * The class is intentionally an orchestrator. Each policy lives in its own
 * collaborator:
 *   - ContractEntityNameResolver  - contract field / LLM precedence;
 *   - ContractAttributeExtractor  - explicit attributes, fields or flat fields;
 *   - ContractAttributeAssembler  - sanitization, uniqueness and type resolution;
 *   - TypeNameListFormatter       - the strict "Type:name,Type:name" wire format.
 *
 * The type resolver is INJECTED (constructor), never constructed here.
 */
class ContractEntityDeriver {

    private final ContractEntityNameResolver nameResolver
    private final ContractAttributeExtractor attributeExtractor
    private final ContractAttributeAssembler attributeAssembler
    private final TypeNameListFormatter listFormatter

    ContractEntityDeriver(ContractTypeResolver typeResolver) {
        if (typeResolver == null) {
            throw new IllegalArgumentException("ContractEntityDeriver: typeResolver MUST be non-null")
        }
        this.nameResolver = new ContractEntityNameResolver()
        this.attributeExtractor = new ContractAttributeExtractor()
        this.attributeAssembler = new ContractAttributeAssembler(typeResolver)
        this.listFormatter = new TypeNameListFormatter()
    }

    /**
     * Derives the entity semantics from the contract.
     *
     * The orchestration is intentionally thin: the name precedence, the
     * contract-shape adaptation, the attribute assembly and the wire-format
     * serialization live in dedicated collaborators.
     *
     * @param contract the operator's JSON contract (may be null)
     * @param llmInference flags proposed by the external inference script
     * @param catalog the flag catalog - used only to tell apart catalog flags
     *        from the business fields of the contract
     * @return a Map with keys:
     *   "entityName"        String  - the resolved entity name (may be null)
     *   "entityNameSource"  String  - "CONTRACT" or "INFERENCE" (may be null)
     *   "attributes"        List    - [name:.., type:..] per attribute
     *   "attributeDetails"  List    - [name:.., type:.., qualifiedType:..,
     *                                 projectType:.., enumType:..,
     *                                 enumConstants:..] per attribute
     *   "attributeList"     String  - the strict "Type:name,Type:name" list
     *   "warnings"          List    - loud derivation warnings
     */
    Map<String, Object> deriveFromContract(Map<String, Object> contract,
                                           Map<String, String> llmInference,
                                           Map<String, ?> catalog) {
        Map<String, Object> derived = new LinkedHashMap<>()
        List<String> warnings = new ArrayList<>()
        derived.put("warnings", warnings)

        Map<String, Object> name = nameResolver.resolve(contract, llmInference)
        derived.put("entityName", name.get("entityName"))
        derived.put("entityNameSource", name.get("entityNameSource"))

        Set<String> catalogKeys = catalog == null ? Collections.emptySet() : new LinkedHashSet<>(catalog.keySet())
        List<Map<String, Object>> rawAttributes = attributeExtractor.extract(contract, catalogKeys)
        Map<String, Object> assembly = attributeAssembler.assemble(rawAttributes)

        List<Map<String, Object>> attributes = (List<Map<String, Object>>) assembly.get("attributes")
        warnings.addAll((List<String>) assembly.get("warnings"))

        derived.put("attributes", attributes)
        derived.put("attributeDetails", assembly.get("attributeDetails"))
        derived.put("attributeList", listFormatter.format(attributes))
        return derived
    }
}
