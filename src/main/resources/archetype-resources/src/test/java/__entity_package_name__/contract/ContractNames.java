package ${package}.${entity_package_name}.contract;

import java.text.Normalizer;

/**
 * Pure name transformations shared by the key normalizers. Kept separate so
 * each support class stays small and has one reason to change.
 */
final class ContractNames {

    private ContractNames() {
    }

    static String camelCase(String key) {
        String normalized = stripAccents(key);
        StringBuilder out = new StringBuilder();
        boolean upperNext = false;
        for (int i = 0; i < normalized.length(); i++) {
            char c = normalized.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                if (out.length() == 0) {
                    out.append(Character.toLowerCase(c));
                } else {
                    out.append(upperNext ? Character.toUpperCase(c) : c);
                }
                upperNext = false;
            } else {
                upperNext = out.length() > 0;
            }
        }
        return valid(out);
    }

    static String pascalCase(String key) {
        String normalized = stripAccents(key);
        StringBuilder out = new StringBuilder();
        boolean upperNext = true;
        for (int i = 0; i < normalized.length(); i++) {
            char c = normalized.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                out.append(upperNext ? Character.toUpperCase(c) : c);
                upperNext = false;
            } else {
                upperNext = out.length() > 0;
            }
        }
        return out.length() == 0 ? "Type" : out.toString();
    }

    static String stripAccents(String value) {
        String decomposed = Normalizer.normalize(value, Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{M}", "");
    }

    static String lowerFirst(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return Character.toLowerCase(value.charAt(0)) + value.substring(1);
    }

    private static String valid(StringBuilder out) {
        if (out.length() == 0 || !Character.isJavaIdentifierStart(out.charAt(0))) {
            return null;
        }
        for (int i = 1; i < out.length(); i++) {
            if (!Character.isJavaIdentifierPart(out.charAt(i))) {
                return null;
            }
        }
        return out.toString();
    }
}
