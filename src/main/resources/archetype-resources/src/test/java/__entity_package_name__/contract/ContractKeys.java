package ${package}.${entity_package_name}.contract;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Normalizes contract keys exactly like the inference mechanism that produced
 * the entity: accents, camelCase, Java keywords and every spelling of id.
 */
public final class ContractKeys {

    private static final Set<String> JAVA_KEYWORDS = new HashSet<String>(Arrays.asList(
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char",
            "class", "const", "continue", "default", "do", "double", "else", "enum",
            "extends", "final", "finally", "float", "for", "goto", "if", "implements",
            "import", "instanceof", "int", "interface", "long", "native", "new",
            "package", "private", "protected", "public", "return", "short", "static",
            "strictfp", "super", "switch", "synchronized", "this", "throw", "throws",
            "transient", "try", "void", "volatile", "while", "true", "false", "null"));

    private ContractKeys() {
    }

    public static String topLevelName(String key) {
        if (key == null) {
            return null;
        }
        String trimmed = key.trim();
        if (trimmed.equalsIgnoreCase("id")) {
            return idFieldName();
        }
        String normalized = ContractNames.stripAccents(trimmed);
        if (normalized.isEmpty()) {
            return null;
        }
        String candidate = ContractNames.lowerFirst(normalized);
        return candidate.matches("^[a-z][a-zA-Z0-9_]*$") ? candidate : null;
    }

    public static String nestedName(String key) {
        String camel = ContractNames.camelCase(key);
        if (camel == null || camel.isEmpty()) {
            return null;
        }
        return JAVA_KEYWORDS.contains(camel) ? camel + "_" : camel;
    }

    public static String typeName(String key) {
        String name = ContractNames.pascalCase(key);
        if (JAVA_KEYWORDS.contains(ContractNames.lowerFirst(name))) {
            name = name + "Type";
        }
        return Character.isDigit(name.charAt(0)) ? "Type" + name : name;
    }

    public static boolean discarded(String key) {
        return topLevelName(key) == null;
    }

    public static String idFieldName() {
        String name = "${entity_name}";
        String base = name.substring(0, 1).toLowerCase(Locale.ROOT) + name.substring(1);
        if (name.equals(name.toUpperCase(Locale.ROOT))) {
            base = name.toLowerCase(Locale.ROOT);
        }
        return base + "Id";
    }
}
