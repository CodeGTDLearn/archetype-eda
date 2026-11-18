package ${package}.${entity_package_name}.contract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import tools.jackson.databind.JsonNode;

/**
 * Extracts the effective attribute list from the contract root, applying the
 * documented precedence attributes > fields > top level and dropping meta-keys.
 */
final class ContractAttributes {

    private static final Set<String> META_KEYS = new HashSet<String>(Arrays.asList(
            "entity_name", "entityName", "entity", "package", "entity_package_name",
            "description", "attributes", "fields"));

    private ContractAttributes() {
    }

    static List<ContractJson.Attribute> read(JsonNode root) {
        JsonNode collection = root.has("attributes") ? root.get("attributes")
                : (root.has("fields") ? root.get("fields") : root);
        List<ContractJson.Attribute> result = new ArrayList<ContractJson.Attribute>();
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

    private static void add(List<ContractJson.Attribute> result, String key, JsonNode value,
                            String declaredType, List<String> constants) {
        if (key == null || key.trim().isEmpty() || META_KEYS.contains(key.trim())) {
            return;
        }
        result.add(new ContractJson.Attribute(key.trim(), value, declaredType, constants));
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
}
