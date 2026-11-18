package ${package}.${entity_package_name}.contract;

import tools.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Contract model of the generated module: reads /contracts/contract.json from
 * the test classpath and derives the effective attribute set, mirroring the
 * rules the inference mechanism used to build the entity (regras-01, 02, 03).
 *
 * This class is a test support class of the GENERATED application. It never
 * runs inside the archetype.
 */
public final class ContractModel {

    public static final int MAX_DEPTH = 25;

    private static final Set<String> JAVA_LANG_TYPES = new HashSet<String>(Arrays.asList(
            "String", "Integer", "Long", "Double", "Float", "Short", "Byte", "Boolean",
            "Character", "Number", "Object", "StringBuilder", "StringBuffer"));

    private static final Set<String> JDK_SIMPLE_NAMES = new HashSet<String>(Arrays.asList(
            "List", "Map", "Set", "Date", "BigDecimal", "BigInteger", "UUID", "Optional",
            "Collection", "Iterable", "Queue", "Deque", "LocalDate", "LocalDateTime",
            "LocalTime", "Instant", "Duration", "Period", "ZonedDateTime", "OffsetDateTime"));

    /** Non-fatal warnings already emitted (the model is loaded once per test class). */
    private static final Set<String> EMITTED_WARNINGS = new HashSet<String>();

    private static void warn(String message) {
        if (EMITTED_WARNINGS.add(message)) {
            System.out.println("CONTRACT MODEL WARNING: " + message);
        }
    }

    public enum Kind { VALUE, NESTED, ENUM, PROJECT_TYPE }

    public static final class Attribute {
        public final String path;
        public final String javaName;
        public final Kind kind;
        public final String expectedType;
        public final List<String> enumConstants;
        public final List<Attribute> children;

        Attribute(String path, String javaName, Kind kind, String expectedType,
                  List<String> enumConstants, List<Attribute> children) {
            this.path = path;
            this.javaName = javaName;
            this.kind = kind;
            this.expectedType = expectedType;
            this.enumConstants = enumConstants;
            this.children = children;
        }

        @Override
        public String toString() {
            return path + ":" + javaName + ":" + expectedType;
        }
    }

    private static final class Raw {
        final String key;
        final JsonNode value;
        final String declaredType;
        final List<String> explicitConstants;

        Raw(String key, JsonNode value, String declaredType, List<String> explicitConstants) {
            this.key = key;
            this.value = value;
            this.declaredType = declaredType;
            this.explicitConstants = explicitConstants;
        }
    }

    private final String entityName;
    private final List<Attribute> attributes;

    private ContractModel(String entityName, List<Attribute> attributes) {
        this.entityName = entityName;
        this.attributes = attributes;
    }

    public String entityName() {
        return entityName;
    }

    public List<Attribute> attributes() {
        return attributes;
    }

    public boolean hasAttributes() {
        return !attributes.isEmpty();
    }

    /** The technical identity field name, derived exactly like the entity template. */
    public static String idFieldName() {
        return ContractKeys.idFieldName();
    }

    public static ContractModel load() {
        ContractJson json = ContractJson.load();
        List<Raw> raws = new ArrayList<Raw>();
        for (ContractJson.Attribute attribute : json.attributes()) {
            raws.add(new Raw(attribute.key(), attribute.value(),
                    attribute.declaredType(), attribute.explicitConstants()));
        }
        return new ContractModel(json.entityName(), buildAll(raws));
    }

    private static List<Attribute> buildAll(List<Raw> raws) {
        List<Attribute> result = new ArrayList<Attribute>();
        Set<String> used = new LinkedHashSet<String>();
        for (Raw raw : raws) {
            if (raw.key == null || raw.key.trim().isEmpty()) {
                warn("attribute without a name ignored");
                continue;
            }
            if (raw.key.trim().equalsIgnoreCase("id")) {
                continue;
            }
            Attribute attribute = build(raw, raw.key.trim(), raw.key.trim(), 1, used, false);
            if (attribute != null) {
                if (used.contains(attribute.javaName)) {
                    warn("duplicate attribute '" + raw.key.trim() + "' ignored");
                    continue;
                }
                used.add(attribute.javaName);
                result.add(attribute);
            }
        }
        return result;
    }

    private static Attribute build(Raw raw, String contractKey, String path, int depth,
                                   Set<String> used, boolean nested) {
        String javaName = nested ? nestedFieldName(contractKey) : topLevelFieldName(contractKey);
        if (javaName == null) {
            warn("attribute '" + contractKey + "' at '" + path
                    + "' ignored: it does not form a valid Java identifier");
            return null;
        }
        if (nested && used.contains(javaName)) {
            int suffix = 2;
            while (used.contains(javaName + "_" + suffix)) {
                suffix++;
            }
            javaName = javaName + "_" + suffix;
        }

        String declared = simpleName(raw.declaredType);
        if (declared != null && JAVA_LANG_TYPES.contains(declared)) {
            if (raw.value != null && raw.value.isArray()) {
                warn("attribute '" + path + "' declares '" + declared
                        + "' over an array; the declared type is kept");
            }
            return new Attribute(path, javaName, Kind.VALUE, declared, null, null);
        }
        if (declared != null && isProjectType(raw.declaredType, declared)) {
            String derived = nested ? javaName : lowerFirst(declared);
            if (!used.contains(derived)) {
                javaName = derived;
            }
            if (isEnumValue(raw.value, raw.explicitConstants)) {
                List<String> constants = enumConstants(raw.value, raw.explicitConstants);
                if (!constants.isEmpty()) {
                    return new Attribute(path, javaName, Kind.ENUM, declared, constants, null);
                }
            }
            // regras-02 Â§5: a project type over an array that is not an enum
            // falls back to String, exactly like the inference that produced
            // the entity. regras-03 Â§1: only an object WITHOUT a declared type
            // becomes a POJO, so a declared project type over an object is a
            // reference to an existing class and is validated by name only.
            if (raw.value != null && raw.value.isArray()) {
                warn("attribute '" + path + "' declares project type '" + declared
                        + "' over a non-enum array; falling back to String");
                return new Attribute(path, javaName, Kind.VALUE, "String", null, null);
            }
            return new Attribute(path, javaName, Kind.PROJECT_TYPE, declared, null, null);
        }

        if (raw.explicitConstants != null && !raw.explicitConstants.isEmpty()) {
            List<String> constants = normalizeConstants(raw.explicitConstants);
            return new Attribute(path, javaName, Kind.ENUM, nestedTypeName(contractKey), constants, null);
        }

        JsonNode value = raw.value;
        if (value == null || value.isNull()) {
            return new Attribute(path, javaName, Kind.VALUE, "String", null, null);
        }
        if (value.isBoolean()) {
            return new Attribute(path, javaName, Kind.VALUE, "Boolean", null, null);
        }
        if (value.isIntegralNumber()) {
            String type = value.canConvertToInt() ? "Integer" : "Long";
            return new Attribute(path, javaName, Kind.VALUE, type, null, null);
        }
        if (value.isFloatingPointNumber()) {
            return new Attribute(path, javaName, Kind.VALUE, "Double", null, null);
        }
        if (value.isTextual()) {
            return new Attribute(path, javaName, Kind.VALUE, "String", null, null);
        }
        if (value.isArray()) {
            if (isEnumArray(value)) {
                List<String> values = new ArrayList<String>();
                for (JsonNode element : value) {
                    values.add(element.asText());
                }
                return new Attribute(path, javaName, Kind.ENUM, nestedTypeName(contractKey),
                        normalizeConstants(values), null);
            }
            warn("attribute '" + path + "' is an array that is not an enum; falling back to String");
            return new Attribute(path, javaName, Kind.VALUE, "String", null, null);
        }
        if (value.isObject()) {
            if (depth >= MAX_DEPTH) {
                warn("attribute '" + path + "' exceeds the maximum contract depth of "
                        + MAX_DEPTH + "; falling back to String");
                return new Attribute(path, javaName, Kind.VALUE, "String", null, null);
            }
            if (value.size() == 0) {
                warn("attribute '" + path + "' is an empty object; falling back to String");
                return new Attribute(path, javaName, Kind.VALUE, "String", null, null);
            }
            String typeName = nestedTypeName(contractKey);
            List<Attribute> children = new ArrayList<Attribute>();
            Set<String> childUsed = new LinkedHashSet<String>();
            for (Map.Entry<String, JsonNode> entry : value.properties()) {
                String childKey = entry.getKey();
                String childPath = path + "." + childKey;
                Raw childRaw = new Raw(childKey, entry.getValue(), null, null);
                Attribute child = build(childRaw, childKey, childPath, depth + 1, childUsed, true);
                if (child != null) {
                    childUsed.add(child.javaName);
                    children.add(child);
                }
            }
            if (children.isEmpty()) {
                warn("attribute '" + path + "' has no valid field; falling back to String");
                return new Attribute(path, javaName, Kind.VALUE, "String", null, null);
            }
            return new Attribute(path, javaName, Kind.NESTED, typeName, null, children);
        }
        return new Attribute(path, javaName, Kind.VALUE, "String", null, null);
    }

    private static String topLevelFieldName(String key) {
        return ContractKeys.topLevelName(key);
    }

    private static String nestedFieldName(String key) {
        return ContractKeys.nestedName(key);
    }

    private static String nestedTypeName(String key) {
        return ContractKeys.typeName(key);
    }

    private static String simpleName(String declared) {
        if (declared == null || declared.trim().isEmpty()) {
            return null;
        }
        String value = declared.trim();
        if (value.contains("<") || value.contains("[") || value.contains("]")) {
            return null;
        }
        int dot = value.lastIndexOf('.');
        return dot >= 0 ? value.substring(dot + 1) : value;
    }

    private static boolean isProjectType(String declaredType, String declared) {
        if (declared == null || !declared.matches("^[A-Z][A-Za-z0-9_]*$")) {
            return false;
        }
        if (JAVA_LANG_TYPES.contains(declared) || JDK_SIMPLE_NAMES.contains(declared)) {
            return false;
        }
        if (declaredType != null) {
            String value = declaredType.trim();
            if (value.startsWith("java.")
                    || value.startsWith("javax.")
                    || value.startsWith("jakarta.")) {
                return false;
            }
        }
        return true;
    }

    private static boolean isEnumValue(JsonNode value, List<String> explicitConstants) {
        if (explicitConstants != null && !explicitConstants.isEmpty()) {
            return true;
        }
        if (value == null || value.isNull()) {
            return false;
        }
        if (value.isTextual()) {
            return true;
        }
        return value.isArray() && isEnumArray(value);
    }

    private static List<String> enumConstants(JsonNode value, List<String> explicitConstants) {
        if (explicitConstants != null && !explicitConstants.isEmpty()) {
            return normalizeConstants(explicitConstants);
        }
        if (value == null || value.isNull()) {
            return new ArrayList<String>();
        }
        if (value.isTextual()) {
            return normalizeConstants(Arrays.asList(value.asText()));
        }
        if (value.isArray()) {
            List<String> values = new ArrayList<String>();
            for (JsonNode element : value) {
                if (element.isTextual()) {
                    values.add(element.asText());
                }
            }
            return normalizeConstants(values);
        }
        return new ArrayList<String>();
    }

    private static boolean isEnumArray(JsonNode array) {
        if (array.size() == 0) {
            return false;
        }
        for (JsonNode element : array) {
            if (!element.isTextual()) {
                return false;
            }
            String text = element.asText();
            if (!text.matches("^[A-Z][A-Z0-9_]*$")) {
                return false;
            }
        }
        return true;
    }

    private static List<String> normalizeConstants(List<String> values) {
        Set<String> constants = new LinkedHashSet<String>();
        for (String value : values) {
            String normalized = ContractKeys.stripAccents(value)
                    .toUpperCase(java.util.Locale.ROOT)
                    .replaceAll("[^A-Z0-9]", "_");
            if (!normalized.isEmpty()) {
                constants.add(normalized);
            }
        }
        return new ArrayList<String>(constants);
    }

    static String lowerFirst(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return Character.toLowerCase(value.charAt(0)) + value.substring(1);
    }
}
