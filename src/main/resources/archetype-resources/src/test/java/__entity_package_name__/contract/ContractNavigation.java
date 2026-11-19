package ${package}.${entity_package_name}.contract;

import tools.jackson.databind.JsonNode;

/**
 * Navigates the persisted contract by normalized key path, so the
 * reconciliation tests can compare nested objects and enum values.
 */
final class ContractNavigation {

    private ContractNavigation() {
    }

    static JsonNode at(ContractJson contract, String path) {
        String[] parts = path.split("\\.");
        JsonNode current = topLevel(contract, parts[0]);
        for (int i = 1; i < parts.length && current != null; i++) {
            current = child(current, parts[i]);
        }
        return current;
    }

    private static JsonNode topLevel(ContractJson contract, String name) {
        for (ContractJson.Attribute attribute : contract.attributes()) {
            String candidate = attribute.key().equalsIgnoreCase("id")
                    ? ContractKeys.idFieldName()
                    : ContractKeys.topLevelName(attribute.key());
            if (name.equals(candidate)) {
                return attribute.value();
            }
        }
        return null;
    }

    private static JsonNode child(JsonNode object, String name) {
        if (object == null || !object.isObject()) {
            return null;
        }
        for (var entry : object.properties()) {
            if (name.equals(ContractKeys.nestedName(entry.getKey()))) {
                return entry.getValue();
            }
        }
        return null;
    }
}
