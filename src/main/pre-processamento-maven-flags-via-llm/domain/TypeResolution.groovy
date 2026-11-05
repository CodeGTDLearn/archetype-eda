package eda.postgenerate.pre_processamento_maven_flags_via_llm.domain

/**
 * The outcome of resolving the type of one attribute: the Java type that will
 * be rendered in the entity, plus an optional warning.
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
