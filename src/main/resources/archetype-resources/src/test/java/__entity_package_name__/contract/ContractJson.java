package ${package}.${entity_package_name}.contract;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Reads the persisted contract: shape precedence, meta-keys and the raw values.
 * It does not infer types.
 */
public final class ContractJson {

    public static final String CONTRACT_RESOURCE = "/contracts/contract.json";

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
            return new ContractJson(readEntityName(root), ContractAttributes.read(root));
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

    public record Attribute(String key, JsonNode value, String declaredType,
                            List<String> explicitConstants) {
    }
}
