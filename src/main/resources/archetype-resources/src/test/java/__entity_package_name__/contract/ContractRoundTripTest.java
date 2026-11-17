package ${package}.${entity_package_name}.contract;

import ${package}.${entity_package_name}.${entity_name};
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Strict round-trip: the normalized contract is deserialized into the entity
 * with FAIL_ON_UNKNOWN_PROPERTIES=true and re-serialized; the key sets and the
 * hierarchy must match. This is the structural proof that no contract key is
 * missing from the entity and no entity key is missing from the contract.
 */
public class ContractRoundTripTest {

    @Test
    void contractRoundTripsThroughTheEntity() {
        ContractDiagnostics diagnostics = new ContractDiagnostics();
        collect(diagnostics);
        diagnostics.failIfAny(
                "ContractRoundTripTest.contractRoundTripsThroughTheEntity",
                "src/test/resources/contracts/contract.json");
    }

    public static void collect(ContractDiagnostics diagnostics) {
        ContractModel model = ContractModel.load();
        JsonMapper mapper = JsonMapper.builder()
                .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();

        ObjectNode payload = mapper.createObjectNode();
        payload.put(ContractModel.idFieldName(), 1L);
        for (ContractModel.Attribute attribute : model.attributes()) {
            putValue(payload, attribute, mapper);
        }

        try {
            ${entity_name} entity = mapper.readValue(payload.toString(), ${entity_name}.class);
            JsonNode actual = mapper.readTree(mapper.writeValueAsString(entity));
            compare(actual, model.attributes(), !model.hasAttributes(), diagnostics);
        } catch (Exception e) {
            diagnostics.add(ContractDiagnostics.Direction.CONTRACT_TO_ENTITY,
                    "<round-trip>", "deserializable into ${entity_name}",
                    e.getClass().getSimpleName() + ": " + e.getMessage(),
                    ContractDiagnostics.Kind.STRUCTURAL);
        }
    }

    static void putValue(ObjectNode target, ContractModel.Attribute attribute, JsonMapper mapper) {
        switch (attribute.kind) {
            case PROJECT_TYPE:
                return;
            case ENUM:
                target.put(attribute.javaName, attribute.enumConstants.get(0));
                return;
            case NESTED:
                ObjectNode child = mapper.createObjectNode();
                for (ContractModel.Attribute nested : attribute.children) {
                    putValue(child, nested, mapper);
                }
                target.set(attribute.javaName, child);
                return;
            default:
                if ("Boolean".equals(attribute.expectedType)) {
                    target.put(attribute.javaName, true);
                } else if ("Integer".equals(attribute.expectedType)) {
                    target.put(attribute.javaName, 1);
                } else if ("Long".equals(attribute.expectedType)) {
                    target.put(attribute.javaName, 1L);
                } else if ("Double".equals(attribute.expectedType)) {
                    target.put(attribute.javaName, 1.0d);
                } else {
                    target.put(attribute.javaName, "x");
                }
        }
    }

    static void compare(JsonNode actual, List<ContractModel.Attribute> attributes,
                        boolean allowScaffold, ContractDiagnostics diagnostics) {
        Set<String> ignored = new HashSet<String>();
        Set<String> expected = new HashSet<String>();
        expected.add(ContractModel.idFieldName());
        for (ContractModel.Attribute attribute : attributes) {
            if (attribute.kind == ContractModel.Kind.PROJECT_TYPE) {
                ignored.add(attribute.javaName);
            } else {
                expected.add(attribute.javaName);
            }
        }

        for (ContractModel.Attribute attribute : attributes) {
            if (attribute.kind == ContractModel.Kind.PROJECT_TYPE) {
                continue;
            }
            if (!actual.has(attribute.javaName)) {
                diagnostics.add(ContractDiagnostics.Direction.CONTRACT_TO_ENTITY,
                        attribute.path, attribute.expectedType, "absent",
                        ContractDiagnostics.Kind.MISSING_FIELD);
                continue;
            }
            if (attribute.kind == ContractModel.Kind.NESTED) {
                compare(actual.get(attribute.javaName), attribute.children, false, diagnostics);
            }
        }

        List<String> extras = new ArrayList<String>();
        for (Map.Entry<String, JsonNode> entry : actual.properties()) {
            String name = entry.getKey();
            if (expected.contains(name) || ignored.contains(name)) {
                continue;
            }
            if (allowScaffold && ("username".equals(name) || "password".equals(name))) {
                continue;
            }
            extras.add(name);
        }
        Collections.sort(extras);
        for (String name : extras) {
            diagnostics.add(ContractDiagnostics.Direction.ENTITY_TO_CONTRACT,
                    name, "absent", actual.get(name).getNodeType().toString(),
                    ContractDiagnostics.Kind.EXTRA_FIELD);
        }
    }
}
