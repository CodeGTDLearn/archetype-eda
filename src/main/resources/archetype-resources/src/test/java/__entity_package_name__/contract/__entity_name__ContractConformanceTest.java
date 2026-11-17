package ${package}.${entity_package_name}.contract;

import ${package}.${entity_package_name}.${entity_name};
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Direction CONTRACT -> ENTITY: every effective attribute of the contract must
 * exist as a declared field of the entity (recursively in nested POJOs), with
 * the expected type.
 */
public class ${entity_name}ContractConformanceTest {

    @Test
    void everyContractAttributeExistsInTheEntity() {
        ContractDiagnostics diagnostics = new ContractDiagnostics();
        collect(diagnostics);
        diagnostics.failIfAny(
                "${entity_name}ContractConformanceTest.everyContractAttributeExistsInTheEntity",
                "src/test/resources/contracts/contract.json");
    }

    public static void collect(ContractDiagnostics diagnostics) {
        ContractModel model = ContractModel.load();
        compareClass(${entity_name}.class, model.attributes(), diagnostics);
    }

    static void compareClass(Class<?> owner, List<ContractModel.Attribute> attributes,
                             ContractDiagnostics diagnostics) {
        for (ContractModel.Attribute attribute : attributes) {
            Field field = findField(owner, attribute.javaName);
            if (field == null) {
                diagnostics.add(ContractDiagnostics.Direction.CONTRACT_TO_ENTITY, attribute.path,
                        attribute.expectedType, "absent", ContractDiagnostics.Kind.MISSING_FIELD);
                continue;
            }
            if (!typeMatches(attribute, field)) {
                diagnostics.add(ContractDiagnostics.Direction.CONTRACT_TO_ENTITY, attribute.path,
                        attribute.expectedType, field.getType().getSimpleName(),
                        ContractDiagnostics.Kind.TYPE_MISMATCH);
                continue;
            }
            if (attribute.kind == ContractModel.Kind.NESTED) {
                compareClass(field.getType(), attribute.children, diagnostics);
            }
        }
    }

    static Field findField(Class<?> owner, String name) {
        for (Field field : owner.getDeclaredFields()) {
            if (field.isSynthetic() || Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if (field.getName().equals(name)) {
                return field;
            }
        }
        return null;
    }

    static boolean typeMatches(ContractModel.Attribute attribute, Field field) {
        Class<?> type = field.getType();
        switch (attribute.kind) {
            case ENUM:
                return type.isEnum() && type.getSimpleName().equals(attribute.expectedType);
            case NESTED:
                return !type.isPrimitive() && !type.isEnum()
                        && type.getSimpleName().equals(attribute.expectedType);
            default:
                return type.getSimpleName().equals(attribute.expectedType);
        }
    }
}
