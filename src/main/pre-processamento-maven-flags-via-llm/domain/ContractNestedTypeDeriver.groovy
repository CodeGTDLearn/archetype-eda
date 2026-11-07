package eda.postgenerate.pre_processamento_maven_flags_via_llm.domain

/**
 * Derives the POJOs of the NESTED objects of the contract.
 *
 * The root entity is generation 1; a top-level nested object is generation 2.
 * The last POJO is generation 25: generation 26 and beyond fall back to
 * String with a loud warning. The limit is per succession chain, never a
 * global count of the contract.
 *
 * Every generated POJO carries the contract keys as its field names (never a
 * name derived from the type) and is emitted in the contract order, depth
 * first, parent before child.
 *
 * The names returned here are SIMPLE names; the entity template renders them
 * fully qualified (package.entity_package.nested.Type), so no import is
 * emitted and a nested type can never collide with a template import.
 */
class ContractNestedTypeDeriver {

    static final int MAX_GENERATIONS = 25

    private final ContractTypeResolver typeResolver
    private final String entityName
    private final ContractTypeNameRegistry registry
    private final ContractEnumDeriver enumDeriver
    private final List<Map<String, Object>> nestedTypes = new ArrayList<Map<String, Object>>()
    private final List<String> warnings = new ArrayList<String>()

    ContractNestedTypeDeriver(ContractTypeResolver typeResolver, String entityName) {
        this(typeResolver, entityName, new ContractTypeNameRegistry(typeResolver, entityName))
    }

    ContractNestedTypeDeriver(ContractTypeResolver typeResolver, String entityName,
                              ContractTypeNameRegistry registry) {
        this(typeResolver, entityName, registry, new ContractEnumDeriver(typeResolver, registry))
    }

    ContractNestedTypeDeriver(ContractTypeResolver typeResolver, String entityName,
                              ContractTypeNameRegistry registry, ContractEnumDeriver enumDeriver) {
        if (typeResolver == null) {
            throw new IllegalArgumentException("ContractNestedTypeDeriver: typeResolver MUST be non-null")
        }
        if (registry == null) {
            throw new IllegalArgumentException("ContractNestedTypeDeriver: registry MUST be non-null")
        }
        if (enumDeriver == null) {
            throw new IllegalArgumentException("ContractNestedTypeDeriver: enumDeriver MUST be non-null")
        }
        this.typeResolver = typeResolver
        this.entityName = entityName == null ? null : entityName.trim()
        this.registry = registry
        this.enumDeriver = enumDeriver
    }

    /**
     * Reserves the simple name of a type that is ALREADY used by the entity
     * (a declared project type kept by the contract), so a derived nested type
     * never shadows it. A reserved name resolves to "<Base>Type".
     */
    void reserveType(String raw) {
        registry.reserveType(raw)
    }

    /**
     * Derives the type of ONE top-level nested object. Returns the name of the
     * generated POJO, or "String" when the object cannot become a POJO.
     */
    String derive(String key, Object value) {
        if (!(value instanceof Map)) return "String"
        return deriveObject(key, (Map<?, ?>) value, 2, entityName)
    }

    private String deriveObject(String key, Map<?, ?> value, int generation, String parentType) {
        if (value == null || value.isEmpty()) {
            warnings.add("objeto aninhado '" + key + "': objeto vazio - tipo String")
            return "String"
        }
        if (generation > MAX_GENERATIONS) {
            warnings.add("objeto aninhado '" + key + "': limite de " + MAX_GENERATIONS +
                    " geracoes excedido - tipo String")
            return "String"
        }
        String base = typeResolver.typeNameFor(key)
        if (base == null) {
            warnings.add("objeto aninhado '" + key + "': nome de tipo invalido - tipo String")
            return "String"
        }
        String typeName = registry.uniqueTypeName(base, parentType)
        Map<String, Object> node = new LinkedHashMap<String, Object>()
        node.put("name", typeName)
        List<Map<String, Object>> fields = new ArrayList<Map<String, Object>>()
        node.put("fields", fields)
        nestedTypes.add(node)

        Set<String> usedFieldNames = new LinkedHashSet<String>()
        for (Map.Entry<?, ?> entry : value.entrySet()) {
            String rawField = entry.key == null ? null : entry.key.toString()
            String fieldName = typeResolver.sanitizeAttributeName(rawField)
            if (fieldName == null) {
                warnings.add("campo descartado no objeto '" + typeName +
                        "': nome invalido '" + rawField + "'")
                continue
            }
            fieldName = uniqueFieldName(fieldName, usedFieldNames)
            usedFieldNames.add(fieldName)

            Object fieldValue = entry.value
            String fieldType
            if (fieldValue instanceof Map) {
                Map<?, ?> nested = (Map<?, ?>) fieldValue
                if (nested.isEmpty()) {
                    warnings.add("campo '" + fieldName + "' do objeto '" + typeName +
                            "': objeto vazio - tipo String")
                    fieldType = "String"
                } else if (generation + 1 > MAX_GENERATIONS) {
                    warnings.add("campo '" + fieldName + "' do objeto '" + typeName +
                            "': limite de " + MAX_GENERATIONS + " geracoes excedido - tipo String")
                    fieldType = "String"
                } else {
                    fieldType = deriveObject(rawField, nested, generation + 1, typeName)
                }
            } else if (typeResolver.isUppercaseEnumArray(fieldValue)) {
                String enumName = enumDeriver.deriveNestedEnum(rawField, fieldValue, typeName)
                if (enumName != null) {
                    fieldType = enumName
                } else {
                    warnings.add("campo '" + fieldName + "' do objeto '" + typeName +
                            "': array/lista nao suportado - tipo String")
                    fieldType = "String"
                }
            } else if (typeResolver.isArrayValue(fieldValue)) {
                warnings.add("campo '" + fieldName + "' do objeto '" + typeName +
                        "': array/lista nao suportado - tipo String")
                fieldType = "String"
            } else {
                fieldType = typeResolver.estimateType(fieldValue)
            }

            Map<String, Object> field = new LinkedHashMap<String, Object>()
            field.put("name", fieldName)
            field.put("type", fieldType)
            fields.add(field)
        }

        if (fields.isEmpty()) {
            nestedTypes.remove(node)
            registry.releaseType(typeName)
            warnings.add("objeto aninhado '" + key + "': nenhum campo valido - tipo String")
            return "String"
        }
        return typeName
    }

    private String uniqueFieldName(String base, Set<String> used) {
        if (!used.contains(base)) return base
        int suffix = 2
        while (used.contains(base + "_" + suffix)) suffix++
        return base + "_" + suffix
    }

    boolean isNestedType(String name) {
        if (name == null) return false
        for (Map<String, Object> node : nestedTypes) {
            if (name == node.get("name")) return true
        }
        return false
    }

    List<Map<String, Object>> getNestedTypes() {
        return nestedTypes
    }

    List<String> getWarnings() {
        return warnings
    }
}
