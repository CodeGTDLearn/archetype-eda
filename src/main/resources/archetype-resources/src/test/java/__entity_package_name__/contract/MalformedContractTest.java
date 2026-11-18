package ${package}.${entity_package_name}.contract;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/**
 * Rule: an unreadable contract fails explicitly. It is never skipped and never
 * passes silently.
 */
class MalformedContractTest {

    @Test
    void invalidJsonFailsWithStructuralMessage() {
        assertThatThrownBy(() -> ContractJson.parse("{ this is not valid json"))
                .as("malformed JSON")
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CONTRACT STRUCTURAL FAILURE");
    }

    @Test
    void nonObjectRootFailsWithStructuralMessage() {
        assertThatThrownBy(() -> ContractJson.parse("[]"))
                .as("contract root must be an object")
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be a JSON object");
    }
}
