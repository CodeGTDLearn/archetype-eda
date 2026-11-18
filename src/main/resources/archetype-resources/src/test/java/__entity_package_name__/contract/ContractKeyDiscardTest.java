package ${package}.${entity_package_name}.contract;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Rule: a key that remains invalid after normalization is discarded from the
 * effective attribute set.
 */
class ContractKeyDiscardTest {

    @Test
    void keyThatRemainsInvalidIsDiscarded() {
        assertThat(ContractKeys.discarded("nome-completo"))
                .as("key with a hyphen is not a Java identifier")
                .isTrue();
    }
}
