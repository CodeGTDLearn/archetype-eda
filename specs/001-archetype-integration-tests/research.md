# Phase 0 Research: Integration Test Matrix for eda-archetype

**Slug**: archetype-integration-tests
**Created**: 2026-09-25
**Source spec**: [spec.md](./spec.md)

This is a confirmation pass. The substantive research (prior art, evidence against, dependency graph extraction) was already done in the assessment phase at `.specify/assessments/archetype-integration-tests/research.md`. Phase 0 here resolves any remaining `NEEDS CLARIFICATION` items from the plan's Technical Context and consolidates technical decisions.

## NEEDS CLARIFICATION Resolution

Technical Context had **zero `NEEDS CLARIFICATION` markers** after the assessment's clarifications were carried into the spec. Each item resolved as follows:

| Technical Context field | Decision | Rationale | Alternatives considered |
|---|---|---|---|
| Language/Version | Java 17 | Constitution Technology Stack mandates Java 17; archetype-packaging POM already uses it. | None — fixed by Constitution. |
| Primary Dependencies | `maven-archetype-plugin:integration-test`; Spring Boot 4.0.0 (generated project) | Same mechanism Apache uses for its own archetype (research: `apache/maven-archetype#1032`, 33 ITs passing). Spring Boot 4.0.0 is fixed by Constitution. | (a) Custom JUnit harness — rejected by concept.md Option B (reinvents the wheel, high cost, low value); (b) External archetype-testing tool — concept.md Option C, no such tool exists in this ecosystem. |
| Storage | N/A | Fixtures are file-system resources; no persistent data. | N/A |
| Testing | `maven-archetype-plugin:integration-test` for the matrix; Groovy `verify.groovy` for `custom-attributes` structural assertion | Inherited from the assessment's chosen mechanism (Option A) and the spec's FR-002/FR-003. | (a) JUnit + programmatic invocation — same as Option B above. (b) AssertJ + filesystem diffs — adds a dependency for a single use case. |
| Target Platform | Developer local machine (Win/Linux/macOS) | Spec FR-010 (locally runnable) + intake `Out of Scope` (no CI). | CI runner — explicitly excluded. |
| Project Type | Library/framework tooling (archetype) | `pom.xml` already declares `packaging=maven-archetype`. | N/A |
| Performance Goals | None | Spec Assumptions: "no orçamento fixo de tempo; priorizar cobertura e simplicidade sobre velocidade". | Fixed time budget — rejected. |
| Constraints | 5 constraints from spec FR-004/FR-007/FR-009/FR-011/FR-012 | Inherited verbatim from spec. | None — each is a NON-NEGOTIABLE from intake/constitution. |
| Scale/Scope | ≥ 8 fixtures (4 scenarios + 5 binding sub-modules, with `custom-attributes` counting as one of the 4 scenarios) | Spec SC-001. | Larger matrix — rejected by concept ("matriz curada, não exaustiva"). |

## Phase 0 Decisions Consolidated

### Decision 1 — Mechanism: `maven-archetype-plugin:integration-test`
- **What**: Use the standard `archetype:integration-test` Maven goal, with each fixture as a subdirectory under `src/test/resources/projects/<scenario>/`.
- **Why**: Apache itself uses this same mechanism for its own archetype (research: `apache/maven-archetype#1032` reports 33 passing ITs). Mature, stable, no new dependencies.
- **Alternatives considered**: Custom JUnit harness (rejected — concept Option B); external tool (rejected — concept Option C, none exists).

### Decision 2 — Fixture Layout: per-scenario subdirectory
- **What**: One subdirectory per scenario: `src/test/resources/projects/{minimal, full, custom-attributes, binding-default-only, binding-list-only, binding-map-only, binding-mutable-only, binding-validated-only}/`. Each contains `archetype.properties` (flags) and `goal.txt` (`clean verify`). The `custom-attributes` fixture additionally has `verify.groovy`.
- **Why**: The directory IS the matrix; no central config to maintain. Adding a scenario = adding a directory.
- **Alternatives considered**: Centralized matrix file (e.g., `matrix.json` enumerating scenarios) — adds a layer of indirection and a parser; the per-directory form is what the `integration-test` goal natively consumes.

### Decision 3 — `verify.groovy` only for `custom-attributes`
- **What**: `mvn verify` is the acceptance signal for every fixture except `custom-attributes`, which additionally has a `verify.groovy` that asserts structural properties of the generated entity.
- **Why**: Spec FR-002 (uniform build criterion) and FR-003 (structural assertion only where it adds signal). The declared `custom_attributes_list` must shape the generated entity — `mvn verify` is not sufficient for that signal.
- **Alternatives considered**: `verify.groovy` for all scenarios — rejected because most scenarios only need build-passing; adds complexity without signal.

### Decision 4 — Dependency rule encoded per fixture, not globally
- **What**: Each fixture's `archetype.properties` is hand-curated to respect the `DEPENDS_ON` rule (parent=false ⇒ children not set). No automated enforcement beyond the standard archetype behavior (unknown properties → generation failure).
- **Why**: Manual curation keeps fixtures readable; the archetype itself will reject semantically invalid combinations (unknown properties), providing a natural safety net.
- **Alternatives considered**: Programmatic matrix generation from `property-*.xml` `DEPENDS_ON` comments — possible future work, but out of scope per concept (intake `Out of Scope`: "manual curation is the explicit choice").

### Decision 5 — `verify.groovy` reuse Apache Maven convention
- **What**: The `verify.groovy` for `custom-attributes` will use the same conventions documented in `apache/maven-archetype#1032`: receive `basedir`, `scriptdir`, `context` as binding variables; return `null` or `true` to pass.
- **Why**: Aligns with the ecosystem standard (research); no reinvention.
- **Alternatives considered**: Custom JUnit-style assertions — would require a different runner.

## Open Items Carried Forward (none blocking)

* All blocking clarifications from the assessment (`decision.md`) are resolved in the spec.
* Network/Central availability is documented in Spec Assumptions (not blocking).
* Origin & Context is documented in Spec Assumptions (not blocking).

No new open items introduced by Phase 0.