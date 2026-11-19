package ${package}.${entity_package_name}.contract;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Rule: contract keys are normalized exactly like the inference mechanism that
 * produced the entity.
 */
class ContractKeyNormalizationTest {

    @Test
    void accentsAreRemoved() {
        assertThat(ContractKeys.topLevelName("endereço"))
                .as("accented top-level key")
                .isEqualTo("endereco");
    }

    @Test
    void nestedKeysBecomeCamelCase() {
        assertThat(ContractKeys.nestedName("ponto_referencia"))
                .as("nested key")
                .isEqualTo("pontoReferencia");
    }

    @Test
    void caseDifferenceMapsToTheSameName() {
        assertThat(ContractKeys.topLevelName("Name"))
                .as("top-level key with a capital letter")
                .isEqualTo("name");
    }

    @Test
    void everyIdSpellingMapsToTheTechnicalField() {
        assertThat(ContractKeys.topLevelName("ID"))
                .as("id spelling")
                .isEqualTo(ContractKeys.idFieldName());
    }
}
