package ${package}.${entity_package_name}.contract;

import ${package}.${entity_package_name}.${entity_name};
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Enums derived from the contract are compared by BOTH: the derived type name
 * and the constant set. Missing or renamed constants on either side fail.
 */
public class ContractEnumsTest {

    @Test
    void enumsMatchTheContractByNameAndConstants() {
        ContractDiagnostics diagnostics = new ContractDiagnostics();
        collect(diagnostics);
        diagnostics.failIfAny(
                "ContractEnumsTest.enumsMatchTheContractByNameAndConstants",
                "src/test/resources/contracts/contract.json");
    }

    public static void collect(ContractDiagnostics diagnostics) {
        ContractModel model = ContractModel.load();
        Map<String, Set<String>> topLevel = new LinkedHashMap<String, Set<String>>();
        Map<String, Set<String>> nested = new LinkedHashMap<String, Set<String>>();
        aggregate(model.attributes(), false, topLevel, nested);
        check(${entity_name}.class, model.attributes(), diagnostics, false, topLevel, nested);
    }

    /**
     * contract_enum_definitions declares ONE definition per enum TYPE, so the
     * effective constant set is the union of every occurrence of that type
     * (regras-02 §4.3-§4.4). Two attributes of the same enum type do not create
     * two enums.
     */
    static void aggregate(List<ContractModel.Attribute> attributes, boolean nestedScope,
                          Map<String, Set<String>> topLevel, Map<String, Set<String>> nested) {
        Map<String, Set<String>> scope = nestedScope ? nested : topLevel;
        for (ContractModel.Attribute attribute : attributes) {
            if (attribute.kind == ContractModel.Kind.ENUM) {
                Set<String> constants = scope.get(attribute.expectedType);
                if (constants == null) {
                    constants = new LinkedHashSet<String>();
                    scope.put(attribute.expectedType, constants);
                }
                constants.addAll(attribute.enumConstants);
            } else if (attribute.kind == ContractModel.Kind.NESTED) {
                aggregate(attribute.children, true, topLevel, nested);
            }
        }
    }

    static void check(Class<?> owner, List<ContractModel.Attribute> attributes,
                      ContractDiagnostics diagnostics, boolean nestedScope,
                      Map<String, Set<String>> topLevel, Map<String, Set<String>> nested) {
        Map<String, Set<String>> scope = nestedScope ? nested : topLevel;
        for (ContractModel.Attribute attribute : attributes) {
            if (attribute.kind == ContractModel.Kind.NESTED) {
                Field nestedField = ${entity_name}ContractConformanceTest.findField(owner, attribute.javaName);
                if (nestedField != null) {
                    check(nestedField.getType(), attribute.children, diagnostics, true, topLevel, nested);
                }
                continue;
            }
            if (attribute.kind != ContractModel.Kind.ENUM) {
                continue;
            }
            Field field = ${entity_name}ContractConformanceTest.findField(owner, attribute.javaName);
            if (field == null) {
                diagnostics.add(ContractDiagnostics.Direction.CONTRACT_TO_ENTITY, attribute.path,
                        attribute.expectedType, "absent", ContractDiagnostics.Kind.MISSING_FIELD);
                continue;
            }
            if (!field.getType().isEnum()
                    || !field.getType().getSimpleName().equals(attribute.expectedType)) {
                diagnostics.add(ContractDiagnostics.Direction.CONTRACT_TO_ENTITY, attribute.path,
                        attribute.expectedType, field.getType().getSimpleName(),
                        ContractDiagnostics.Kind.ENUM_NAME_MISMATCH);
                continue;
            }
            List<String> actual = new ArrayList<String>();
            for (Object constant : field.getType().getEnumConstants()) {
                actual.add(String.valueOf(constant));
            }
            Set<String> declared = scope.get(attribute.expectedType);
            List<String> expected = declared == null
                    ? attribute.enumConstants
                    : new ArrayList<String>(declared);
            if (!sameConstants(expected, actual)) {
                diagnostics.add(ContractDiagnostics.Direction.CONTRACT_TO_ENTITY, attribute.path,
                        expected.toString(), actual.toString(),
                        ContractDiagnostics.Kind.ENUM_CONSTANT_MISMATCH);
            }
        }
    }

    static boolean sameConstants(List<String> expected, List<String> actual) {
        List<String> left = new ArrayList<String>(expected);
        List<String> right = new ArrayList<String>(actual);
        Collections.sort(left);
        Collections.sort(right);
        return left.equals(right);
    }
}
