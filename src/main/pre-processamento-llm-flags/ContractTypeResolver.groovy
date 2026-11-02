package eda.postgenerate.llmflag

/**
 * Resolves the Java type of a single entity attribute and sanitizes attribute
 * names. SINGLE RESPONSIBILITY: type inference - this component knows nothing
 * about contracts, flags, catalog precedence or the merge.
 *
 * Rules:
 *   1. A DECLARED java.lang type wins (FQN -> simple name, generics dropped).
 *   2. A declared PROJECT type (a type outside java.lang - a domain class or
 *      an enum) is KEPT: the entity references it by its simple name, the
 *      archetype imports it (flag custom_attribute_imports) and/or generates
 *      the enum source (flag custom_enum_definitions). The attribute NAME
 *      then comes from the type itself (EnderecoCliente -> enderecoCliente).
 *   3. Any OTHER declared type (a JDK type outside java.lang, a generic
 *      type) produces a LOUD WARNING and falls back to the type estimated
 *      from the VALUE.
 *   4. The estimate maps the runtime class of the value to a java.lang type;
 *      nested objects, arrays and null all fall back to String.
 *
 * Canonical module. The runtime MIRROR lives inlined in Orchestrator.groovy
 * (single-file constraint: gmaven 2.1.1 and groovy.ui.GroovyMain do not
 * resolve sibling sources). ContractDerivationTest.groovy asserts the parity
 * between the mirror and the canonical modules (through FlagProposalMerger).
 */
class ContractTypeResolver {

    /**
     * Types the entity template can render without emitting an extra import
     * (the template writes "private ${attrib.type} ${attrib.name};" verbatim).
     */
    private static final List<String> JAVA_LANG_TYPES = [
            "String", "Integer", "Long", "Double", "Float", "Short", "Byte",
            "Boolean", "Character", "Number", "Object", "StringBuilder", "StringBuffer"
    ]

    /**
     * Well-known JDK type names declared WITHOUT a package (e.g. "List").
     * A package-less name cannot be told apart from a project type by its
     * spelling, so the usual JDK names keep the legacy behaviour (rejected
     * + estimate from the value) instead of being treated as project types.
     */
    private static final List<String> STANDARD_JDK_TYPE_NAMES = [
            "List", "ArrayList", "LinkedList", "Map", "HashMap", "LinkedHashMap",
            "TreeMap", "Set", "HashSet", "LinkedHashSet", "TreeSet", "Collection",
            "Iterable", "Date", "Calendar", "Timestamp", "Instant", "LocalDate",
            "LocalDateTime", "LocalTime", "OffsetDateTime", "ZonedDateTime",
            "Duration", "Period", "UUID", "BigDecimal", "BigInteger", "Optional",
            "Stream", "Thread"
    ]

    /**
     * Resolves the Java type of ONE attribute.
     *
     * @param name the (already sanitized) attribute name - used in the warning
     * @param declared the type declared in the contract (may be null)
     * @param value the value carried by the contract (may be null)
     * @return a TypeResolution with the resolved type and an optional warning
     */
    TypeResolution resolve(String name, Object declared, Object value) {
        return resolve(name, declared, value, null)
    }

    /**
     * Resolves the Java type of ONE attribute.
     *
     * @param name the (already sanitized) attribute name - used in the warning
     * @param declared the type declared in the contract (may be null)
     * @param value the value carried by the contract (may be null)
     * @param enumValues the explicit enum constants declared in the contract
     *        (may be null): a List of constants or a single constant
     * @return a TypeResolution with the resolved type and an optional warning
     */
    TypeResolution resolve(String name, Object declared, Object value, Object enumValues) {
        String type = null
        String warning = null
        String qualifiedType = null
        boolean projectType = false
        boolean enumType = false
        List<String> enumConstants = new ArrayList<String>()
        if (declared != null) {
            String raw = declared.toString().trim()
            if (!raw.isEmpty()) qualifiedType = raw
            String normalized = normalizeType(raw)
            if (normalized != null) {
                type = normalized
            } else if (isProjectType(raw)) {
                // A PROJECT type (outside java.lang) is a valid domain type:
                // it is kept, referenced by its simple name and carried over
                // to the archetype (import + enum generation).
                type = simpleTypeName(raw)
                projectType = true
                List<String> constants = enumConstantsFor(value, enumValues)
                if (!constants.isEmpty()) {
                    enumType = true
                    enumConstants = constants
                }
            } else {
                warning = "atributo '" + name + "': tipo declarado '" + declared +
                        "' nao e um tipo de java.lang - tipo estimado pelo valor"
            }
        }
        if (type == null) type = estimateType(value)
        TypeResolution out = new TypeResolution()
        out.type = type
        out.warning = warning
        out.qualifiedType = qualifiedType
        out.projectType = projectType
        out.enumType = enumType
        out.enumConstants = enumConstants
        return out
    }

    /**
     * Tells a PROJECT type (a domain class or an enum) from the types that
     * keep the legacy behaviour (java.lang types, JDK types outside
     * java.lang and generic types).
     */
    boolean isProjectType(String raw) {
        if (raw == null) return false
        String t = raw.trim()
        if (t.isEmpty()) return false
        if (t.contains("<") || t.contains(">") || t.contains("[")) return false
        String simple = simpleTypeName(t)
        if (simple == null) return false
        // A Java type is a class-like name.
        if (!(simple ==~ /^[A-Z][A-Za-z0-9_]*$/)) return false
        if (JAVA_LANG_TYPES.contains(simple)) return false
        if (t.contains(".")) {
            String pkg = t.substring(0, t.lastIndexOf(".")).trim()
            if (pkg.isEmpty()) return false
            String p = pkg.toLowerCase(Locale.ROOT)
            // A JDK type outside java.lang keeps the legacy behaviour.
            if (p.startsWith("java.") || p.startsWith("javax.") || p.startsWith("jakarta.")) return false
            return true
        }
        return !STANDARD_JDK_TYPE_NAMES.contains(simple)
    }

    /**
     * The simple name of a declared type: "br.com.projeto.StatusPedido" ->
     * "StatusPedido".
     */
    String simpleTypeName(String raw) {
        if (raw == null) return null
        String t = raw.trim()
        if (t.isEmpty()) return null
        if (t.contains(".")) t = t.substring(t.lastIndexOf(".") + 1)
        return t.trim()
    }

    /**
     * The enum constants of a PROJECT type: the explicit ones when the
     * contract declares them, otherwise the observed textual value.
     */
    List<String> enumConstantsFor(Object value, Object enumValues) {
        List<Object> raw = new ArrayList<Object>()
        if (enumValues instanceof Collection) {
            raw.addAll((Collection<Object>) enumValues)
        } else if (enumValues != null) {
            raw.add(enumValues)
        }
        if (raw.isEmpty() && value instanceof String) {
            raw.add(value)
        }
        List<String> out = new ArrayList<String>()
        for (Object o : raw) {
            String constant = normalizeEnumConstant(o == null ? null : o.toString())
            if (constant != null && !out.contains(constant)) out.add(constant)
        }
        return out
    }

    /**
     * Normalizes a value into a Java enum constant: "em processamento" ->
     * "EM_PROCESSAMENTO".
     */
    String normalizeEnumConstant(String raw) {
        if (raw == null) return null
        String s = raw.trim().toUpperCase(Locale.ROOT)
        if (s.isEmpty()) return null
        s = s.replaceAll("[^A-Z0-9]+", "_").replaceAll('^_+|_+$', "")
        if (s.isEmpty()) return null
        if (Character.isDigit(s.charAt(0))) s = "VALUE_" + s
        return s
    }

    /**
     * Normalizes a declared attribute type: FQN -> simple name, generics
     * dropped. Returns null when the type is not a java.lang type.
     */
    String normalizeType(String raw) {
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
    String estimateType(Object value) {
        if (value == null) return "String"
        if (value instanceof Boolean) return "Boolean"
        if (value instanceof Integer || value instanceof Short || value instanceof Byte) return "Integer"
        if (value instanceof Long || value instanceof java.math.BigInteger) return "Long"
        if (value instanceof java.math.BigDecimal || value instanceof Double || value instanceof Float) return "Double"
        // Arrays and objects are kept as their serialized String form: the
        // template emits no import, so java.util types would not compile.
        return "String"
    }

    /**
     * Normalizes an attribute name into a valid Java identifier that also
     * satisfies the catalog regex ^[a-z][a-zA-Z0-9_]*$.
     */
    String sanitizeAttributeName(String raw) {
        if (raw == null) return null
        String s = raw.trim().replaceAll("[^A-Za-z0-9_]", "_")
        if (s.isEmpty()) return null
        if (Character.isDigit(s.charAt(0))) s = "field_" + s
        // Character.toLowerCase(char) returns a char: it MUST be converted to
        // a String before the concatenation (a char + String is not a valid
        // Groovy operation and raises MissingMethodException).
        if (Character.isUpperCase(s.charAt(0))) s = Character.toLowerCase(s.charAt(0)).toString() + s.substring(1)
        if (!(s ==~ /^[a-z][a-zA-Z0-9_]*$/)) return null
        return s
    }

    /**
     * The attribute name of a PROJECT type: the simple name of the type with
     * its first letter lowercased ("StatusPedido" -> "statusPedido",
     * "EnderecoCliente" -> "enderecoCliente"). It satisfies the same catalog
     * regex as sanitizeAttributeName.
     */
    String fieldNameForType(String simpleTypeName) {
        if (simpleTypeName == null) return null
        String s = simpleTypeName.trim()
        if (s.isEmpty()) return null
        if (!(s ==~ /^[A-Za-z_][A-Za-z0-9_]*$/)) return null
        if (Character.isDigit(s.charAt(0))) s = "field_" + s
        if (Character.isUpperCase(s.charAt(0))) s = Character.toLowerCase(s.charAt(0)).toString() + s.substring(1)
        if (!(s ==~ /^[a-z][a-zA-Z0-9_]*$/)) return null
        return s
    }
}

/**
 * The outcome of resolving the type of one attribute: the Java type that will
 * be rendered in the entity, plus an optional warning (set when a declared
 * type had to be rejected).
 *
 * The extra fields carry the metadata the archetype needs for a PROJECT type:
 *   - qualifiedType: the type as declared (with its package, when declared)
 *   - projectType:   the type is a domain type (outside java.lang)
 *   - enumType:      the type is an enum that the archetype must generate
 *   - enumConstants: the constants of that enum
 */
class TypeResolution {
    String type
    String warning
    String qualifiedType
    boolean projectType
    boolean enumType
    List<String> enumConstants
}
