package ${package}.${entity_package_name}.contract;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Reads declared fields by reflection. The verdict never depends on generated
 * accessors, so it is identical with boilerplate on and off.
 */
public final class EntityFields {

    private EntityFields() {
    }

    public static Field fieldNamed(Class<?> owner, String name) {
        for (Field field : owner.getDeclaredFields()) {
            if (field.getName().equals(name)) {
                return field;
            }
        }
        return null;
    }

    public static void assertFieldType(Class<?> owner, String name, Class<?> expected) {
        Field field = fieldNamed(owner, name);
        if (field == null) {
            throw new AssertionError("[CONTRACT_TO_ENTITY] " + name + " | expected: "
                    + expected.getSimpleName() + " | found: absent | kind: MISSING_FIELD");
        }
        if (!field.getType().equals(expected)) {
            throw new AssertionError("[CONTRACT_TO_ENTITY] " + name + " | expected: "
                    + expected.getSimpleName() + " | found: " + field.getType().getSimpleName()
                    + " | kind: TYPE_MISMATCH");
        }
    }

    public static void assertFieldTypeName(Class<?> owner, String name, String expected) {
        Field field = fieldNamed(owner, name);
        if (field == null) {
            throw new AssertionError("[CONTRACT_TO_ENTITY] " + name + " | expected: "
                    + expected + " | found: absent | kind: MISSING_FIELD");
        }
        if (!field.getType().getSimpleName().equals(expected)) {
            throw new AssertionError("[CONTRACT_TO_ENTITY] " + name + " | expected: "
                    + expected + " | found: " + field.getType().getSimpleName()
                    + " | kind: TYPE_MISMATCH");
        }
    }

    public static Set<String> fieldNames(Class<?> owner) {
        Set<String> names = new LinkedHashSet<String>();
        for (Field field : owner.getDeclaredFields()) {
            names.add(field.getName());
        }
        return names;
    }
}
