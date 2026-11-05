package eda.postgenerate.pre_processamento_maven_flags_via_llm.domain

/**
 * Turns raw contract attributes into resolved entity attributes.
 *
 * Responsibility: name sanitization, uniqueness and type-resolution
 * orchestration. The actual type inference is delegated to
 * ContractTypeResolver.
 */
class ContractAttributeAssembler {

    private final ContractTypeResolver typeResolver

    ContractAttributeAssembler(ContractTypeResolver typeResolver) {
        if (typeResolver == null) {
            throw new IllegalArgumentException("ContractAttributeAssembler: typeResolver MUST be non-null")
        }
        this.typeResolver = typeResolver
    }

    Map<String, Object> assemble(List<Map<String, Object>> rawAttributes) {
        List<Map<String, Object>> attributes = new ArrayList<>()
        List<Map<String, Object>> attributeDetails = new ArrayList<>()
        List<String> warnings = new ArrayList<>()
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
            TypeResolution resolution = typeResolver.resolve(contractName, raw.get("type"), raw.get("value"), enumValues)
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
            attributeDetails.add([name: name, type: type,
                                  qualifiedType: resolution.qualifiedType,
                                  projectType: resolution.projectType,
                                  enumType: resolution.enumType,
                                  enumConstants: resolution.enumConstants])
        }

        return [attributes: attributes, attributeDetails: attributeDetails, warnings: warnings]
    }
}
