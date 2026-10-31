package eda.postgenerate.llmflag

/**
 * Derives the ENTITY semantics from the operator's JSON contract:
 *   - the entity name (contract field first, LLM proposal second);
 *   - the entity attributes, whose Java type is resolved by the injected
 *     ContractTypeResolver (a declared java.lang type wins; a declared
 *     PROJECT type is kept and referenced by its simple name; any other
 *     declared type falls back to the type estimated from the VALUE);
 *   - the strict "Type:name,Type:name" list expected by the catalog.
 *
 * SINGLE RESPONSIBILITY: contract -> entity/attribute derivation. This
 * component knows nothing about flag precedence, catalog validation rules
 * (FR-006) or the merge itself; the catalog Map is used only to tell apart
 * catalog flags from business fields of the contract.
 *
 * The type resolver is INJECTED (constructor), never constructed here: the
 * canonical modules live in separate files and a sibling source is not
 * resolvable at compile time (see Orchestrator.groovy for the single-file
 * constraint rationale).
 *
 * Canonical module. The runtime MIRROR lives inlined in Orchestrator.groovy.
 * ContractDerivationTest.groovy asserts the parity between the mirror and
 * this module (through FlagProposalMerger).
 */
class ContractEntityDeriver {

    /**
     * Contract keys that carry meta-information about the module; they are
     * never entity attributes.
     */
    private static final List<String> CONTRACT_META_KEYS = [
            "entityName", "entity", "package", "description", "attributes", "fields"
    ]

    private final ContractTypeResolver typeResolver

    ContractEntityDeriver(ContractTypeResolver typeResolver) {
        if (typeResolver == null) {
            throw new IllegalArgumentException("ContractEntityDeriver: typeResolver MUST be non-null")
        }
        this.typeResolver = typeResolver
    }

    /**
     * Derives the entity semantics from the contract.
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
                    // The explicit enum constants of the attribute (when declared).
                    Object enumValues = em.get("enumValues")
                    if (enumValues == null) enumValues = em.get("values")
                    rawAttributes.add([name: name?.toString(), value: value, type: em.get("type"),
                                       enumValues: enumValues])
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
        List<Map<String, Object>> attributeDetails = new ArrayList<>()
        List<String> parts = new ArrayList<>()
        Set<String> usedNames = new LinkedHashSet<String>()
        for (Map<String, Object> raw : rawAttributes) {
            String contractName = typeResolver.sanitizeAttributeName((String) raw.get("name"))
            if (contractName == null) {
                warnings.add("atributo descartado: nome invalido '" + raw.get("name") + "'")
                continue
            }
            // The explicit enum constants of the attribute (when declared).
            Object enumValues = raw.get("enumValues")
            if (enumValues == null) enumValues = raw.get("values")
            // The type is resolved by the dedicated resolver component: a
            // declared java.lang type wins; a declared PROJECT type is kept;
            // any other declared type is estimated from the value and the
            // rejection is reported as a warning.
            def resolution = typeResolver.resolve(contractName, raw.get("type"), raw.get("value"), enumValues)
            if (resolution.warning != null) warnings.add(resolution.warning)
            String type = resolution.type
            String name = contractName
            // A PROJECT type takes its attribute name from the TYPE itself:
            // EnderecoCliente -> enderecoCliente, StatusPedido -> statusPedido.
            if (resolution.projectType) {
                String typeName = typeResolver.fieldNameForType(type)
                if (typeName == null) {
                    warnings.add("atributo descartado: nome invalido para o tipo '" + type + "'")
                    continue
                }
                name = typeName
                // The type already named another attribute: the name of the
                // contract is kept (the entity MUST NOT repeat a field).
                if (usedNames.contains(name) && !usedNames.contains(contractName)) {
                    name = contractName
                }
            }
            if (usedNames.contains(name)) {
                warnings.add("atributo descartado: nome duplicado '" + name + "'")
                continue
            }
            usedNames.add(name)
            attributes.add([name: name, type: type])
            parts.add(type + ":" + name)
            attributeDetails.add([name: name, type: type,
                                  qualifiedType: resolution.qualifiedType,
                                  projectType: resolution.projectType,
                                  enumType: resolution.enumType,
                                  enumConstants: resolution.enumConstants])
        }
        derived.put("attributes", attributes)
        derived.put("attributeDetails", attributeDetails)
        derived.put("attributeList", parts.join(","))
        return derived
    }
}
