# Feature Specification: Integration Test Matrix for eda-archetype

**Feature Branch**: `[001-archetype-integration-tests]`
**Created**: 2026-09-25
**Status**: Draft
**Input**: User description: "Criar uma suíte de testes de integração abrangente para o eda-archetype, validando que todas as combinações relevantes das flags de geração (Git, entidades, Lombok, Docker, properties, logging, segurança, observabilidade) produzem projetos funcionais." (slug=archetype-integration-tests)

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Baseline Smoke Coverage (Priority: P1)

As the **archetype maintainer**, I want the most minimal flag combination (`minimal` — all optional flags off) to be exercised automatically, so that I have a regression-safe floor below which the archetype is broken.

**Why this priority**: Without a baseline run, every other comparison is meaningless. `minimal` is the cheapest possible end-to-end run and is the only one that was previously partially wired (the existing `basic` fixture). It is the MVP — if only this story ships, the project already has more coverage than today.

**Independent Test**: Run the local test suite for the `minimal` fixture only; assert it produces a buildable Spring Boot project and exits 0.

**Acceptance Scenarios**:

1. **Given** the maintainer has a clean checkout and the archetype is installed locally, **When** the local suite is executed for the `minimal` fixture, **Then** the generated project compiles, packages, and the suite reports one passing test.
2. **Given** a future change to `property-*.xml`, `archetype-post-generate.groovy`, or any file set, **When** the maintainer re-runs the local suite, **Then** the `minimal` fixture either still passes (no regression) or fails with a clear diagnostic pointing at the changed file.

---

### User Story 2 - Stress Coverage of Co-Enabled Flags (Priority: P1)

As the **archetype maintainer**, I want the "all flags on" combination (`full`) to be exercised automatically, so that conflicts between co-enabled features (e.g., Git + Docker + Logback + Lombok + binding properties + JSON contracts + custom attributes) are caught before they reach consumers.

**Why this priority**: Co-enabled flags are the second-most likely place for the post-processor to misbehave (conditional branches interact). `full` is the cheapest way to exercise the stress surface; without it, every individual flag looks fine but combinations blow up.

**Independent Test**: Run the local suite for the `full` fixture only; assert the project compiles, packages, and the post-processor's conditional paths for co-enabled flags all ran.

**Acceptance Scenarios**:

1. **Given** the maintainer runs the local suite for the `full` fixture, **When** the build completes, **Then** the suite reports one passing test and the build log shows no error.
2. **Given** a change to any flag's default in `property-*.xml`, **When** the maintainer re-runs the `full` fixture, **Then** the fixture either still passes or the failure pinpoints the changed default.

---

### User Story 3 - JSON Contract Entity Generation (Priority: P2)

As the **archetype maintainer**, I want the JSON-contract generation path (`json-contract` — `enable_json_entity_contracts=true` with a real JSON contract file as input) to be exercised automatically, so that I have confidence the generated entity matches the contract's shape — not just that the build passes.

**Why this priority**: This is the only scenario where `mvn verify` is **not sufficient** as an acceptance signal (the post-processor depends on an external file whose contents must shape the generated entity). It is the only scenario with structural assertions on the output.

**Independent Test**: Run the local suite for the `json-contract` fixture; assert the build passes AND the generated entity class matches the structural expectations of the contract.

**Acceptance Scenarios**:

1. **Given** the `json-contract` fixture is configured with a sample JSON contract file, **When** the local suite runs that fixture, **Then** the generated Spring Boot project passes `mvn verify` AND the generated entity class contains the attributes declared in the contract.
2. **Given** the JSON contract file is malformed, **When** the local suite runs the `json-contract` fixture, **Then** the suite reports a clear failure naming the contract file as the cause, and no generated artifact is silently accepted.

---

### User Story 4 - Binding Properties Coverage (Priority: P2)

As the **archetype maintainer**, I want at least one fixture per binding-properties sub-module (default, list, map, mutable, validated) to be exercised automatically, so that I have confidence in each binding variant independently — consistent with the project's "binding duplo de configuração externalizada" principle.

**Why this priority**: Binding properties is a large, multi-flag surface (one parent + 5 sub-flags). The Constitution requires immutable and mutable variants to be tested independently; this story is what makes that auditable.

**Independent Test**: Run the local suite for each binding-properties sub-fixture individually; assert each passes.

**Acceptance Scenarios**:

1. **Given** the maintainer has a binding-properties sub-fixture (e.g., `binding-default-only`), **When** the local suite runs that fixture, **Then** the project compiles and the relevant binding-properties classes are present in the generated source.
2. **Given** a change to `property-binding-properties.xml` (e.g., default value or validation regex), **When** the maintainer re-runs all binding-properties sub-fixtures, **Then** each fixture either still passes or the failure pinpoints the changed flag.

---

### Edge Cases

- **What happens when the post-processor Groovy throws an exception mid-run?** The build fails with the Groovy stack trace; the suite must not silently report success.
- **What happens when a fixture's `archetype.properties` declares a flag that does not exist in `property-*.xml`?** The archetype generation fails; the suite must surface the unknown flag name in the diagnostic.
- **What happens when the developer environment lacks network access to Maven Central?** The build fails during dependency resolution; the suite must not silently report partial success. (This is not mitigated by the feature — it is acknowledged in Assumptions.)
- **What happens when two fixtures share a name?** The build tool rejects the duplicate; this is a configuration error caught at suite-setup time.
- **What happens when an existing flag is renamed in `property-*.xml` without updating fixtures?** The renamed flag's fixture fails on archetype generation; the failure is unambiguous about the missing property.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001** — The system MUST execute each fixture as a separate, isolated generation-and-verify run, so that a failure in one fixture does not affect the others.
- **FR-002** — The system MUST validate, for each fixture, that the project generated from the archetype passes `mvn verify` (clean and verify goals), so that the "funcional" criterion is uniformly applied.
- **FR-003** — For the `json-contract` fixture, the system MUST additionally validate structural properties of the generated entity class against the input JSON contract, so that build-passing alone is not accepted as sufficient evidence.
- **FR-004** — The system MUST respect the dependency rule that when a parent flag is `false`, the related child flags are not exercised in that fixture, so that the matrix is not polluted with semantically invalid combinations.
- **FR-005** — The system MUST provide at least one fixture per scenario defined for this feature: `minimal`, `full`, `json-contract`, and one fixture per binding-properties sub-module (default, list, map, mutable, validated).
- **FR-006** — The system MUST source each fixture's flag values from an explicit, human-readable `archetype.properties` per fixture, so that the matrix is self-documenting in the filesystem.
- **FR-007** — The system MUST NOT make real network, Git, or Docker calls during a fixture run, so that tests remain deterministic and offline-runnable once the archetype and its dependencies are cached.
- **FR-008** — When a flag is added to or removed from `property-*.xml`, the system MUST allow the maintainer to extend coverage by editing or adding at most one fixture, so that incremental maintenance is bounded.
- **FR-009** — The system MUST assume a clean local install of the archetype before each suite run (per Constitution Principle VI: "validate before blaming the code"), so that an outdated local cache cannot mask a real regression.
- **FR-010** — The system MUST be runnable from the developer's local environment without requiring a CI pipeline, so that feedback loops are short and infrastructure-light.
- **FR-011** — The system MUST NOT modify, replace, or interact with the existing `bash_script/` tooling, so that the human-facing interactive flow remains untouched.
- **FR-012** — The system MUST NOT include combinations from the security + observability family (JWT + Logback + cPanel/ELK) in this scope, so that those combinations can be addressed in a separate, future assessment.

### Key Entities

- **Fixture**: A self-contained description of one scenario's flag values, plus a verification goal, located in a dedicated directory. Attributes: scenario name, set of flag values, expected verification outcome (build-only or build+structural).
- **Generated Project**: The temporary Spring Boot project produced by running the archetype generation with a given fixture's flags. Attributes: build status (pass/fail), generated file layout, generated entity shape (for the `json-contract` scenario).
- **Scenario**: A logical grouping of fixtures sharing a thematic purpose. In this feature: `minimal`, `full`, `json-contract`, and the `binding-properties` family.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001** — At least the 4 scenarios (`minimal`, `full`, `json-contract`, `binding-properties` family) and their valid sub-combinations per the dependency rule are exercised automatically. Baseline: 1 fixture (`basic`). Target: ≥ 8 fixtures covering the named scenarios plus one per binding-properties sub-module.
- **SC-002** — A maintainer running the suite locally receives a regression signal for a change to `archetype-post-generate.groovy` or `property-*.xml` within a single local run, rather than only when a downstream consumer reports the issue. Baseline: end-to-end (consumer-discovered). Target: local run after the change.
- **SC-003** — Adding a new flag in `property-*.xml` requires the maintainer to edit or add at most one fixture to extend coverage. Baseline: unbounded (no per-fixture tracking). Target: ≤ 1 fixture edit/add per new flag.
- **SC-004** — Every conditional branch of the post-processor's `if (flag == 'true'|'false')` transformations in `archetype-post-generate.groovy` is reachable by at least one fixture. Baseline: not measured. Target: 100% reachability of the documented flag-controlled branches.

## Assumptions

- The developer's local environment can resolve the archetype's runtime dependencies (Spring Boot, Lombok, Logback, etc.) from Maven Central, or has a populated local cache. Network unavailability is a known environmental constraint, not a feature failure.
- The existing `basic` fixture in `src/test/resources/projects/basic/` can be retained or migrated to the `minimal` scenario without affecting the rest of the feature.
- Dependency relationships between flags (parent → child) are stable and documented in `DEPENDS_ON` comments in `property-*.xml`. If a new flag is added without a `DEPENDS_ON` annotation, manual fixture curation is required to keep the dependency rule valid.
- The archetype is always installed cleanly before a suite run (Constitution Principle VI). A dirty local cache is not in scope for the feature to detect or repair.
- Combination counts are bounded by manual curation, not exhaustive enumeration. The `binding-properties` scenario covers one fixture per sub-module rather than the full combinatorial space, in line with the assessment's recommendation.
- Versions of Spring Boot, Lombok, and other dependencies in the generated project are not pinned by the feature. Upstream breakage is acknowledged as fragility of the mechanism, not a defect of this feature.
- The maintenance team for this archetype consists of a single maintainer (per Constitution's TODO note on amendment approval process).

## Scope Boundary: Automated Suite vs. Manual Validation Checklist

The **automated suite** (executed by `mvn clean install`) covers: FR-001, FR-002, FR-003, FR-005, FR-006, FR-008, FR-009, FR-010, FR-011, FR-012, and SC-001, SC-003, SC-004. The 9 fixtures under `src/test/resources/projects/` execute end-to-end (generation + sub-build + verify.groovy for `json-contract`).

A **maintainer validation checklist** (NOT part of the automated suite, run manually on demand) covers three additional items: regression-bait (correlates to SC-002), offline-runnable (correlates to FR-007), and dependency-rule (correlates to FR-004). These are listed in `quickstart.md` §Verifying the matrix catches a real regression, §Verifying the offline-runnable constraint, and §Verifying the dependency rule. Rationale for keeping these manual rather than automated:

1. **Regression-bait** would require mutating production source code (`archetype-post-generate.groovy`); a failed restore would leave the archetype broken. Equivalent implicit coverage is provided by T029 (full matrix): any regression in the post-processor breaks all 9 fixtures, so the automated suite already detects the same class of regression that T030 would surface.
2. **Offline-runnable** is an environmental test requiring actual network disconnection. FR-007 is satisfied by construction: `verify.groovy` makes no network calls (audited), and the archetype reads only local files.
3. **Dependency-rule** would require a fixture that intentionally violates FR-004. The 8 existing fixtures already prove the rule by induction — none combine `parent=false` with a child having a non-default value.