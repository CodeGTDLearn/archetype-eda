package eda.postgenerate.pre_processamento_maven_flags_via_llm.domain

/**
 * Allocates unique simple names for the types DERIVED from the contract
 * (nested POJOs and enums).
 *
 * The registry is shared by every deriver of one contract, so a nested POJO
 * and an enum can never receive the same simple name. The policy is the one
 * planned for the nested types:
 *
 *   Base (free) -> BaseType (reserved: java.lang, entity, project) ->
 *   parent+Base (collision) -> BaseType -> BaseType2...
 *
 * Names are limited to MAX_TYPE_NAME_LENGTH characters; the suffix is applied
 * after shortening the stem, so a 120-char name can never make every candidate
 * equal to an already used name (the previous infinite loop).
 */
class ContractTypeNameRegistry {

    static final int MAX_TYPE_NAME_LENGTH = 120

    private final ContractTypeResolver typeResolver
    private final Set<String> reservedTypeNames = new LinkedHashSet<String>()
    private final Set<String> usedTypeNames = new LinkedHashSet<String>()

    ContractTypeNameRegistry(ContractTypeResolver typeResolver, String entityName) {
        if (typeResolver == null) {
            throw new IllegalArgumentException("ContractTypeNameRegistry: typeResolver MUST be non-null")
        }
        this.typeResolver = typeResolver
        String name = entityName == null ? null : entityName.trim()
        if (name != null && !name.isEmpty()) {
            this.reservedTypeNames.add(name)
        }
    }

    /**
     * Reserves the simple name of a type that is ALREADY used by the entity
     * (a declared project type kept by the contract), so a derived type never
     * shadows it. A reserved name resolves to "&lt;Base&gt;Type".
     */
    void reserveType(String raw) {
        if (raw == null) return
        String simple = raw.contains(".") ? raw.substring(raw.lastIndexOf(".") + 1) : raw
        simple = simple == null ? null : simple.trim()
        if (simple != null && !simple.isEmpty()) reservedTypeNames.add(simple)
    }

    /**
     * Releases a name that was allocated for a type that was later discarded
     * (an object with no valid field).
     */
    void releaseType(String name) {
        if (name != null) usedTypeNames.remove(name)
    }

    /**
     * The unique name for a derived type. {@code base} is the desired simple
     * name, {@code parentType} the immediate parent (the entity for a top-level
     * type, the nested POJO for a child).
     */
    String uniqueTypeName(String base, String parentType) {
        String accepted = accept(base)
        if (accepted != null) {
            usedTypeNames.add(accepted)
            return accepted
        }
        // A reserved name (java.lang, the entity, a declared project type)
        // resolves to "<Base>Type" before any parent prefix.
        if (isReserved(base)) {
            accepted = accept(withSuffix(base, "Type"))
            if (accepted != null) {
                usedTypeNames.add(accepted)
                return accepted
            }
        }
        String parent = parentType == null ? "" : parentType
        accepted = accept(withPrefix(parent, base))
        if (accepted != null) {
            usedTypeNames.add(accepted)
            return accepted
        }
        accepted = accept(withSuffix(base, "Type"))
        if (accepted != null) {
            usedTypeNames.add(accepted)
            return accepted
        }
        int suffix = 2
        while (true) {
            accepted = accept(withSuffix(base, "Type" + suffix))
            if (accepted != null) {
                usedTypeNames.add(accepted)
                return accepted
            }
            suffix++
        }
    }

    /**
     * True when the name is reserved by java.lang, the entity or a project
     * type declared in the contract.
     */
    boolean isReserved(String name) {
        if (name == null) return true
        return reservedTypeNames.contains(name) || typeResolver.isJavaLangType(name)
    }

    private String accept(String candidate) {
        if (candidate == null || candidate.isEmpty()) return null
        String limited = candidate.length() > MAX_TYPE_NAME_LENGTH
                ? candidate.substring(0, MAX_TYPE_NAME_LENGTH) : candidate
        if (isReserved(limited) || usedTypeNames.contains(limited)) return null
        return limited
    }

    /**
     * Appends a suffix keeping the result within MAX_TYPE_NAME_LENGTH. The
     * stem is shortened when needed, so the suffix always survives the
     * truncation that would otherwise make every candidate equal to a used
     * 120-char name (an infinite loop).
     */
    private String withSuffix(String base, String suffix) {
        int room = MAX_TYPE_NAME_LENGTH - suffix.length()
        if (room < 1) return suffix.substring(0, MAX_TYPE_NAME_LENGTH)
        String stem = base.length() > room ? base.substring(0, room) : base
        return stem + suffix
    }

    /**
     * Concatenates parent + base within MAX_TYPE_NAME_LENGTH.
     */
    private String withPrefix(String parent, String base) {
        String candidate = parent + base
        return candidate.length() > MAX_TYPE_NAME_LENGTH
                ? candidate.substring(0, MAX_TYPE_NAME_LENGTH) : candidate
    }
}
