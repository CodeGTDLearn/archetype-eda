package ${package}.${entity_package_name}.contract;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Reads the persisted contract: shape precedence (attributes > fields > top
 * level), meta-keys and the raw values. It does not infer types.
 */
public final class ContractJson {

    public static final String CONTRACT_RESOURCE = "/contracts/contract.json";
    private static final Set<String> META_KEYS = new HashSet<String>(Arrays.asList(
            "entity_name", "entityName", "entity", "package", "entity_package_name",
            "description", "attributes", "fields"));

    private final String entityName;
    private final List<Attribute> attributes;

    private ContractJson(String entityName, List<Attribute> attributes) {
        this.entityName = entityName;
        this.attributes = List.copyOf(attributes);
    }

    public static ContractJson load() {
        try (InputStream in = ContractJson.class.getResourceAsStream(CONTRACT_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("CONTRACT STRUCTURAL FAILURE: test resource "
                        + CONTRACT_RESOURCE + " not found");
            }
            return parse(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("CONTRACT STRUCTURAL FAILURE: cannot read "
                    + CONTRACT_RESOURCE + " - " + e.getMessage(), e);
        }
    }

    public static ContractJson parse(String json) {
        try {
            JsonNode root = JsonMapper.builder().build().readTree(json);
            if (root == null || !root.isObject()) {
                throw new IllegalStateException(
                        "CONTRACT STRUCTURAL FAILURE: the contract root must be a JSON object");
            }
            return new ContractJson(readEntityName(root), readAttributes(root));
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("CONTRACT STRUCTURAL FAILURE: cannot read "
                    + CONTRACT_RESOURCE + " - " + e.getMessage(), e);
        }
    }

    public String entityName() {
        return entityName;
    }

    public List<Attribute> attributes() {
        return attributes;
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
                : (root.has("fields") ? root.get("fields") : root);
        List<Attribute> result = new ArrayList<Attribute>();
        if (collection.isObject()) {
            for (Map.Entry<String, JsonNode> entry : collection.properties()) {
                add(result, entry.getKey(), entry.getValue(), null, null);
            }
        } else if (collection.isArray()) {
            for (JsonNode item : collection) {
                if (item.isTextual()) {
                    add(result, item.asText(), null, null, null);
                } else if (item.isObject()) {
                    add(result, firstText(item, "name", "attribute", "field"),
                            item.has("value") ? item.get("value")
                                    : (item.has("default") ? item.get("default") : null),
                            item.has("type") ? item.get("type").asText() : null,
                            arrayOf(item.has("enumValues") ? item.get("enumValues") : item.get("values")));
                }
            }
        }
        return result;
    }

    private static void add(List<Attribute> result, String key, JsonNode value,
                            String declaredType, List<String> constants) {
        if (key == null || key.trim().isEmpty() || META_KEYS.contains(key.trim())) {
            return;
        }
        result.add(new Attribute(key.trim(), value, declaredType, constants));
    }

    private static String firstText(JsonNode node, String... keys) {
        for (String key : keys) {
            if (node.has(key) && node.get(key).isTextual()) {
                return node.get(key).asText();
            }
        }
        return null;
    }

    private static List<String> arrayOf(JsonNode node) {
        if (node == null || !node.isArray()) {
            return null;
        }
        List<String> values = new ArrayList<String>();
        for (JsonNode element : node) {
            if (element.isTextual()) {
                values.add(element.asText());
            }
        }
        return values;
    }

    public record Attribute(String key, JsonNode value, String declaredType,
                            List<String> explicitConstants) {
    }
}
