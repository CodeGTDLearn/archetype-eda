package ${package}.${entity_package_name}.contract;

import static ${package}.${entity_package_name}.contract.EntityFields.fieldNamed;
import static org.assertj.core.api.Assertions.assertThat;

import ${package}.${entity_package_name}.${entity_name};
import org.junit.jupiter.api.Test;

/**
 * Rule: the contract "id" attribute is represented by the technical identity
 * field. The entity must never expose a plain "id" field.
 */
class TechnicalIdMappingTest {

    private static final String TECHNICAL_ID_FIELD = ContractKeys.idFieldName();

    @Test
    void contractIdUsesTheTechnicalIdentityField() {
        assertThat(fieldNamed(${entity_name}.class, TECHNICAL_ID_FIELD))
                .as("technical identity field")
                .isNotNull();
    }

    @Test
    void technicalIdentityFieldIsLong() {
        assertThat(fieldNamed(${entity_name}.class, TECHNICAL_ID_FIELD).getType())
                .as("type of the technical identity field")
                .isEqualTo(Long.class);
    }

    @Test
    void entityHasNoPlainIdField() {
        assertThat(fieldNamed(${entity_name}.class, "id"))
                .as("plain 'id' field must not exist")
                .isNull();
    }
}
