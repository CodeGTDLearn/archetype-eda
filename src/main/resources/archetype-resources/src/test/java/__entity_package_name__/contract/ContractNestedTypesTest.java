package ${package}.${entity_package_name}.contract;

import ${package}.${entity_package_name}.${entity_name};
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Recursive validation of the nested POJOs derived from the contract objects.
 * Walks every NESTED level (the mechanism allows up to 25) and reports the full
 * hierarchical path of any divergence.
 */
public class ContractNestedTypesTest {

    @Test
    void nestedObjectsAreValidatedRecursively() {
        ContractDiagnostics diagnostics = new ContractDiagnostics();
        collect(diagnostics);
        diagnostics.failIfAny(
                "ContractNestedTypesTest.nestedObjectsAreValidatedRecursively",
                "src/test/resources/contracts/contract.json");
    }

    public static void collect(ContractDiagnostics diagnostics) {
        ContractModel model = ContractModel.load();
        for (ContractModel.Attribute attribute : model.attributes()) {
            if (attribute.kind == ContractModel.Kind.NESTED) {
                walk(${entity_name}.class, attribute, diagnostics);
            }
        }
    }

    static void walk(Class<?> owner, ContractModel.Attribute attribute,
                     ContractDiagnostics diagnostics) {
        Field field = ${entity_name}ContractConformanceTest.findField(owner, attribute.javaName);
        if (field == null) {
            diagnostics.add(ContractDiagnostics.Direction.CONTRACT_TO_ENTITY, attribute.path,
                    attribute.expectedType, "absent", ContractDiagnostics.Kind.MISSING_FIELD);
            return;
        }
        if (field.getType().isPrimitive() || field.getType().isEnum()
                || !field.getType().getSimpleName().equals(attribute.expectedType)) {
            diagnostics.add(ContractDiagnostics.Direction.CONTRACT_TO_ENTITY, attribute.path,
                    attribute.expectedType, field.getType().getSimpleName(),
                    ContractDiagnostics.Kind.TYPE_MISMATCH);
            return;
        }
        List<ContractModel.Attribute> children = attribute.children;
        for (ContractModel.Attribute child : children) {
            if (child.kind != ContractModel.Kind.NESTED) {
                continue;
            }
            walk(field.getType(), child, diagnostics);
        }
    }
}
