package ${package}.${entity_package_name}.contract;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
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

    public static final String CONTRACT_RESOURCE = "/contracts/contract.json";
    public static final int MAX_DEPTH = 25;

    private static final Set<String> META_KEYS = new HashSet<String>(Arrays.asList(
            "entity_name", "entityName", "entity", "package", "entity_package_name",
            "description", "attributes", "fields"));

    private static final Set<String> JAVA_LANG_TYPES = new HashSet<String>(Arrays.asList(
            "String", "Integer", "Long", "Double", "Float", "Short", "Byte", "Boolean",
            "Character", "Number", "Object", "StringBuilder", "StringBuffer"));

    private static final Set<String> JDK_SIMPLE_NAMES = new HashSet<String>(Arrays.asList(
            "List", "Map", "Set", "Date", "BigDecimal", "BigInteger", "UUID", "Optional",
            "Collection", "Iterable", "Queue", "Deque", "LocalDate", "LocalDateTime",
            "LocalTime", "Instant", "Duration", "Period", "ZonedDateTime", "OffsetDateTime"));

    private static final Set<String> JAVA_KEYWORDS = new HashSet<String>(Arrays.asList(
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char",
            "class", "const", "continue", "default", "do", "double", "else", "enum",
            "extends", "final", "finally", "float", "for", "goto", "if", "implements",
            "import", "instanceof", "int", "interface", "long", "native", "new",
            "package", "private", "protected", "public", "return", "short", "static",
            "strictfp", "super", "switch", "synchronized", "this", "throw", "throws",
            "transient", "try", "void", "volatile", "while", "true", "false", "null"));

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
        String name = "${entity_name}";
        String base = name.substring(0, 1).toLowerCase(Locale.ROOT) + name.substring(1);
        if (name.equals(name.toUpperCase(Locale.ROOT))) {
            base = name.toLowerCase(Locale.ROOT);
        }
        return base + "Id";
    }

    public static ContractModel load() {
        try (InputStream in = ContractModel.class.getResourceAsStream(CONTRACT_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException(
                        "CONTRACT STRUCTURAL FAILURE: test resource " + CONTRACT_RESOURCE + " not found");
            }
            JsonNode root = JsonMapper.builder().build().readTree(in);
            if (root == null || !root.isObject()) {
                throw new IllegalStateException(
                        "CONTRACT STRUCTURAL FAILURE: the contract root must be a JSON object");
            }
            return new ContractModel(readEntityName(root), readAttributes(root));
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(
                    "CONTRACT STRUCTURAL FAILURE: cannot read " + CONTRACT_RESOURCE
                            + " - " + e.getMessage(), e);
        }
    }

    private static String readEntityName(JsonNode root) {
        for (String key : Arrays.asList("entity_name", "entityName", "entity")) {
            JsonNode node = root.get(key);
            if (node != null && node.isTextual()) {
                return node.asText();
            }
        }
        return "";
    }

    private static List<Attribute> readAttributes(JsonNode root) {
        JsonNode collection = root.has("attributes") ? root.get("attributes")
                : (root.has("fields") ? root.get("fields") : null);
        List<Raw> raws = new ArrayList<Raw>();

        if (collection != null) {
            if (collection.isObject()) {
                for (Map.Entry<String, JsonNode> entry : collection.properties()) {
                    raws.add(new Raw(entry.getKey(), entry.getValue(), null, null));
                }
            } else if (collection.isArray()) {
                for (JsonNode item : collection) {
                    if (item.isTextual()) {
                        raws.add(new Raw(item.asText(), null, null, null));
                    } else if (item.isObject()) {
                        String key = firstText(item, "name", "attribute", "field");
                        JsonNode value = item.has("value") ? item.get("value")
                                : (item.has("default") ? item.get("default") : null);
                        String declaredType = item.has("type") ? item.get("type").asText() : null;
                        List<String> constants = arrayOf(item.has("enumValues")
                                ? item.get("enumValues") : item.get("values"));
                        raws.add(new Raw(key, value, declaredType, constants));
                    }
                }
            }
        } else {
            for (Map.Entry<String, JsonNode> entry : root.properties()) {
                if (!META_KEYS.contains(entry.getKey())) {
                    raws.add(new Raw(entry.getKey(), entry.getValue(), null, null));
                }
            }
        }

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
            // regras-02 §5: a project type over an array that is not an enum
            // falls back to String, exactly like the inference that produced
            // the entity. regras-03 §1: only an object WITHOUT a declared type
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
            return new Attribute(path, javaName, Kind.ENUM, pascalCase(contractKey), constants, null);
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
                return new Attribute(path, javaName, Kind.ENUM, pascalCase(contractKey),
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
        String normalized = stripAccents(key);
        if (normalized.matches("^[a-z][a-zA-Z0-9_]*$")) {
            return normalized;
        }
        return null;
    }

    private static String nestedFieldName(String key) {
        String camel = camelCase(key);
        if (camel == null || camel.isEmpty()) {
            return null;
        }
        if (JAVA_KEYWORDS.contains(camel)) {
            return camel + "_";
        }
        return camel;
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
                } else if (upperNext) {
                    out.append(Character.toUpperCase(c));
                } else {
                    out.append(c);
                }
                upperNext = false;
            } else {
                upperNext = out.length() > 0;
            }
        }
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
        if (out.length() == 0) {
            return "Type";
        }
        String name = out.toString();
        if (JAVA_KEYWORDS.contains(lowerFirst(name))) {
            name = name + "Type";
        }
        if (Character.isDigit(name.charAt(0))) {
            name = "Type" + name;
        }
        return name;
    }

    private static String nestedTypeName(String key) {
        return pascalCase(key);
    }

    private static String stripAccents(String value) {
        String decomposed = Normalizer.normalize(value, Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{M}", "");
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
            String normalized = stripAccents(value)
                    .toUpperCase(Locale.ROOT)
                    .replaceAll("[^A-Z0-9]", "_");
            if (!normalized.isEmpty()) {
                constants.add(normalized);
            }
        }
        return new ArrayList<String>(constants);
    }

    private static List<String> arrayOf(JsonNode node) {
        if (node == null || !node.isArray()) {
            return null;
        }
        List<String> values = new ArrayList<String>();
        for (JsonNode item : node) {
            values.add(item.asText());
        }
        return values;
    }

    private static String firstText(JsonNode node, String... keys) {
        for (String key : keys) {
            JsonNode value = node.get(key);
            if (value != null && value.isTextual()) {
                return value.asText();
            }
        }
        return null;
    }

    static String lowerFirst(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return value.substring(0, 1).toLowerCase(Locale.ROOT) + value.substring(1);
    }
}
