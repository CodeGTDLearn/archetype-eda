package ${package}.${entity_package_name}.contract;

import ${package}.${entity_package_name}.${entity_name};
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Direction ENTITY -> CONTRACT: no entity field may exist without a matching
 * contract key, except the technical identity field and (when the contract has
 * no effective attributes) the known username/password scaffold.
 */
public class ${entity_name}ContractCoverageTest {

    @Test
    void noEntityFieldEscapesTheContract() {
        ContractDiagnostics diagnostics = new ContractDiagnostics();
        collect(diagnostics);
        diagnostics.failIfAny(
                "${entity_name}ContractCoverageTest.noEntityFieldEscapesTheContract",
                "src/test/resources/contracts/contract.json");
    }

    public static void collect(ContractDiagnostics diagnostics) {
        ContractModel model = ContractModel.load();
        Set<String> allowed = names(model.attributes());
        allowed.add(ContractModel.idFieldName());
        if (!model.hasAttributes()) {
            allowed.add("username");
            allowed.add("password");
        }
        checkClass(${entity_name}.class, allowed, "", diagnostics);
        for (ContractModel.Attribute attribute : model.attributes()) {
            if (attribute.kind != ContractModel.Kind.NESTED) {
                continue;
            }
            Field field = ${entity_name}ContractConformanceTest.findField(${entity_name}.class, attribute.javaName);
            if (field != null) {
                checkNested(field.getType(), attribute.children, attribute.path, diagnostics);
            }
        }
    }

    static void checkNested(Class<?> owner, List<ContractModel.Attribute> children,
                            String path, ContractDiagnostics diagnostics) {
        Set<String> allowed = names(children);
        checkClass(owner, allowed, path, diagnostics);
        for (ContractModel.Attribute child : children) {
            if (child.kind != ContractModel.Kind.NESTED) {
                continue;
            }
            Field field = ${entity_name}ContractConformanceTest.findField(owner, child.javaName);
            if (field != null) {
                checkNested(field.getType(), child.children, child.path, diagnostics);
            }
        }
    }

    static void checkClass(Class<?> owner, Set<String> allowed, String prefix,
                           ContractDiagnostics diagnostics) {
        List<Field> extras = new ArrayList<Field>();
        for (Field field : owner.getDeclaredFields()) {
            if (field.isSynthetic() || Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if (allowed.contains(field.getName())) {
                continue;
            }
            extras.add(field);
        }
        Collections.sort(extras, new Comparator<Field>() {
            @Override
            public int compare(Field left, Field right) {
                return left.getName().compareTo(right.getName());
            }
        });
        for (Field field : extras) {
            String path = prefix.isEmpty() ? field.getName() : prefix + "." + field.getName();
            diagnostics.add(ContractDiagnostics.Direction.ENTITY_TO_CONTRACT, path,
                    "absent", field.getType().getSimpleName(),
                    ContractDiagnostics.Kind.EXTRA_FIELD);
        }
    }

    static Set<String> names(List<ContractModel.Attribute> attributes) {
        Set<String> names = new LinkedHashSet<String>();
        for (ContractModel.Attribute attribute : attributes) {
            names.add(attribute.javaName);
        }
        return names;
    }
}
