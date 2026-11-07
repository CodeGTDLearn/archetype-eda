package eda.postgenerate.pre_processamento_maven_flags_via_llm.domain

/**
 * Turns raw contract attributes into resolved entity attributes.
 *
 * Responsibility: name sanitization, uniqueness and type-resolution
 * orchestration. The actual type inference is delegated to
 * ContractTypeResolver; the nested-object POJOs are delegated to
 * ContractNestedTypeDeriver.
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
        return assemble(rawAttributes, null)
    }

    Map<String, Object> assemble(List<Map<String, Object>> rawAttributes, String entityName) {
        List<Map<String, Object>> attributes = new ArrayList<>()
        List<Map<String, Object>> attributeDetails = new ArrayList<>()
        List<String> warnings = new ArrayList<>()
        Set<String> usedNames = new LinkedHashSet<String>()
        ContractTypeNameRegistry typeRegistry = new ContractTypeNameRegistry(typeResolver, entityName)
        ContractEnumDeriver enumDeriver = new ContractEnumDeriver(typeResolver, typeRegistry)
        ContractNestedTypeDeriver nestedDeriver =
                new ContractNestedTypeDeriver(typeResolver, entityName, typeRegistry, enumDeriver)

        // The project types kept by the contract share the entity namespace:
        // reserve their simple names BEFORE deriving any nested POJO/enum.
        for (Map<String, Object> raw : rawAttributes) {
            Object declared = raw.get("type")
            if (declared == null || declared.toString().trim().isEmpty()) continue
            if (raw.get("value") instanceof Map) continue
            if (typeResolver.isProjectType(declared.toString())) {
                typeRegistry.reserveType(typeResolver.simpleTypeName(declared.toString()))
            }
        }

        for (Map<String, Object> raw : rawAttributes) {
            String contractName = typeResolver.sanitizeAttributeName((String) raw.get("name"))
            if (contractName == null) {
                warnings.add("atributo descartado: nome invalido '" + raw.get("name") + "'")
                continue
            }
            // The explicit enum constants of the attribute (when declared).
            Object enumValues = raw.get("enumValues")
            if (enumValues == null) enumValues = raw.get("values")

            Object declared = raw.get("type")
            Object value = raw.get("value")
            boolean declaredBlank = declared == null || declared.toString().trim().isEmpty()
            // A declared-blank attribute (nested object/enum/scalar) keeps the
            // contract name; if it is already used, drop it BEFORE deriving a
            // type, so no orphan POJO/enum is recorded.
            if (declaredBlank && usedNames.contains(contractName)) {
                warnings.add("atributo descartado: nome duplicado '" + contractName + "'")
                continue
            }
            String type = null
            String name = contractName
            boolean projectType = false
            boolean enumType = false
            List<String> enumConstants = new ArrayList<String>()
            TypeResolution resolution = null

            // A nested object WITHOUT a declared type becomes a POJO.
            if (value instanceof Map && declaredBlank) {
                type = nestedDeriver.derive(contractName, value)
            // An array of UPPERCASE strings is the enum signal (the payload
            // convention). The enum lives in the entity package.
            } else if (declaredBlank && typeResolver.isUppercaseEnumArray(value)) {
                String enumName = enumDeriver.deriveEntityEnum(contractName, value)
                if (enumName != null) {
                    type = enumName
                    enumType = true
                    enumConstants = typeResolver.enumConstantsFor(value, null)
                }
            }

            if (type == null) {
                resolution = typeResolver.resolve(contractName, declared, value, enumValues)
                if (resolution.warning != null) warnings.add(resolution.warning)
                type = resolution.type
                projectType = resolution.projectType
                enumType = resolution.enumType
                enumConstants = resolution.enumConstants
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
            }
            if (usedNames.contains(name)) {
                warnings.add("atributo descartado: nome duplicado '" + name + "'")
                continue
            }
            usedNames.add(name)
            attributes.add([name: name, type: type])
            attributeDetails.add([name: name, type: type,
                                  qualifiedType: resolution == null ? null : resolution.qualifiedType,
                                  projectType: projectType,
                                  enumType: enumType,
                                  enumConstants: enumConstants])
        }

        warnings.addAll(nestedDeriver.getWarnings())
        return [attributes: attributes, attributeDetails: attributeDetails, warnings: warnings,
                nestedTypes: nestedDeriver.getNestedTypes(), enumTypes: enumDeriver.getEnumTypes()]
    }
}
