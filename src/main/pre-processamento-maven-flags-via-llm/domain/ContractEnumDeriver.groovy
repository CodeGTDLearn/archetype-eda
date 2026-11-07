package eda.postgenerate.pre_processamento_maven_flags_via_llm.domain

/**
 * Derives enums from the payload convention: an array whose elements are ALL
 * UPPERCASE strings is an enum. The enum type name is the PascalCase of the
 * attribute/field key ("status" -> Status, "uf" -> Uf), allocated by the same
 * ContractTypeNameRegistry used by the nested POJOs.
 *
 * Scope:
 *   - an entity attribute enum lives in the entity package (scope "entity");
 *   - a nested-object field enum lives in the nested package (scope "nested").
 * The projection layer turns the scope into the flag value: "Type:CONST" or
 * "nested.Type:CONST".
 *
 * An array that is empty, mixed, non-string or not fully uppercase is NOT an
 * enum and is left to the String + warning fallback.
 */
class ContractEnumDeriver {

    static final String SCOPE_ENTITY = "entity"
    static final String SCOPE_NESTED = "nested"

    private final ContractTypeResolver typeResolver
    private final ContractTypeNameRegistry registry
    private final List<Map<String, Object>> enumTypes = new ArrayList<Map<String, Object>>()

    ContractEnumDeriver(ContractTypeResolver typeResolver, ContractTypeNameRegistry registry) {
        if (typeResolver == null) {
            throw new IllegalArgumentException("ContractEnumDeriver: typeResolver MUST be non-null")
        }
        if (registry == null) {
            throw new IllegalArgumentException("ContractEnumDeriver: registry MUST be non-null")
        }
        this.typeResolver = typeResolver
        this.registry = registry
    }

    /**
     * Derives the enum of a TOP-LEVEL entity attribute. Returns the simple enum
     * type name, or null when the value is not an UPPERCASE array.
     */
    String deriveEntityEnum(String attributeName, Object value) {
        return derive(attributeName, value, null, SCOPE_ENTITY)
    }

    /**
     * Derives the enum of a FIELD inside a nested object. Returns the simple
     * enum type name, or null when the value is not an UPPERCASE array.
     */
    String deriveNestedEnum(String fieldName, Object value, String parentType) {
        return derive(fieldName, value, parentType, SCOPE_NESTED)
    }

    /**
     * The derived enums in derivation order. Each entry is
     * [name:.., constants:[..], scope:'entity'|'nested'].
     */
    List<Map<String, Object>> getEnumTypes() {
        return enumTypes
    }

    private String derive(String key, Object value, String parentType, String scope) {
        if (!typeResolver.isUppercaseEnumArray(value)) return null
        String base = typeResolver.typeNameFor(key)
        if (base == null) return null
        List<String> constants = typeResolver.enumConstantsFor(value, null)
        if (constants.isEmpty()) return null
        String typeName = registry.uniqueTypeName(base, parentType)
        enumTypes.add([name: typeName, constants: constants, scope: scope])
        return typeName
    }
}
