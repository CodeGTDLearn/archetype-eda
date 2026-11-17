package ${package}.${entity_package_name}.contract;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Aggregated diagnostics of the entity<->contract conformance gate.
 *
 * Every test class collects its divergences here; the entry test fails once
 * with the full, deterministic list (see contracts/schema-test-contract.md).
 */
public final class ContractDiagnostics {

    public enum Direction {
        CONTRACT_TO_ENTITY,
        ENTITY_TO_CONTRACT
    }

    public enum Kind {
        MISSING_FIELD,
        EXTRA_FIELD,
        TYPE_MISMATCH,
        ENUM_NAME_MISMATCH,
        ENUM_CONSTANT_MISMATCH,
        STRUCTURAL
    }

    public static final class Divergence {
        public final Direction direction;
        public final String path;
        public final String expected;
        public final String found;
        public final Kind kind;

        public Divergence(Direction direction, String path, String expected, String found, Kind kind) {
            this.direction = direction;
            this.path = path;
            this.expected = expected;
            this.found = found;
            this.kind = kind;
        }

        public String render() {
            return "[" + direction + "] " + path
                    + " | expected: " + expected
                    + " | found: " + found
                    + " | kind: " + kind;
        }
    }

    private final List<Divergence> items = new ArrayList<Divergence>();

    public void add(Direction direction, String path, String expected, String found, Kind kind) {
        items.add(new Divergence(direction, path, expected, found, kind));
    }

    public void addAll(ContractDiagnostics other) {
        items.addAll(other.items);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public int size() {
        return items.size();
    }

    public List<Divergence> items() {
        return Collections.unmodifiableList(items);
    }

    public void failIfAny(String testName, String contractFile) {
        if (items.isEmpty()) {
            return;
        }
        StringBuilder message = new StringBuilder();
        message.append("CONTRACT DRIFT DETECTED")
                .append("\n  test    : ").append(testName)
                .append("\n  contract: ").append(contractFile)
                .append("\n  total   : ").append(items.size()).append(" divergence(s)");
        for (Divergence item : items) {
            message.append("\n  - ").append(item.render());
        }
        throw new AssertionError(message.toString());
    }
}
