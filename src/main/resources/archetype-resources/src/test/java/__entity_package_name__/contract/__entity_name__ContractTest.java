package ${package}.${entity_package_name}.contract;

import org.junit.jupiter.api.Test;

/**
 * Entry point of the entity<->contract conformance gate (Frente 01).
 *
 * Runs every responsibility and aggregates all divergences into a SINGLE,
 * deterministic failure, so the developer fixes the whole drift in one pass.
 */
public class ${entity_name}ContractTest {

    @Test
    void entityAndContractAreFullyAligned() {
        ContractDiagnostics diagnostics = new ContractDiagnostics();
        ${entity_name}ContractConformanceTest.collect(diagnostics);
        ${entity_name}ContractCoverageTest.collect(diagnostics);
        ContractNestedTypesTest.collect(diagnostics);
        ContractEnumsTest.collect(diagnostics);
        diagnostics.failIfAny(
                "${entity_name}ContractTest.entityAndContractAreFullyAligned",
                "src/test/resources/contracts/contract.json");
    }
}
