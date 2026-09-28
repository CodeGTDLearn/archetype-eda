# Implementation Plan: Integration Test Matrix for eda-archetype

**Branch**: `001-archetype-integration-tests` | **Date**: 2026-09-25 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/001-archetype-integration-tests/spec.md`

## Summary

Create a curated multi-fixture integration test matrix for `com.eda-archetype:eda-archetype` using the `maven-archetype-plugin` goal `integration-test`, with fixtures under `src/test/resources/projects/`. The matrix covers four scenarios: `minimal` (baseline), `full` (all-flags-on stress), `custom-attributes` (structural assertion via `verify.groovy`), and one fixture per `binding-properties` sub-module (default, list, map, mutable, validated). The generated project for each fixture must pass `mvn verify`; the `custom-attributes` fixture additionally asserts on the generated entity shape. No CI integration, no `bash_script/` changes, no security+observability combinations — all excluded by design (see [decision.md](../../.specify/assessments/archetype-integration-tests/decision.md) and Spec FR-011/FR-012).

## Technical Context

**Language/Version**: Java 17 (per Constitution Technology Stack); Groovy for the `custom-attributes` `verify.groovy`.
**Primary Dependencies**: Apache `maven-archetype-plugin` `integration-test` goal (the standard mechanism — same one used by `apache/maven-archetype#1032`). Generated project depends on Spring Boot 4.0.0, Lombok, Logback, Jakarta Validation (per Constitution).
**Storage**: N/A — fixtures are file-system resources under `src/test/resources/projects/`. No persistent data store.
**Testing**: `maven-archetype-plugin:integration-test` for the matrix run; for `custom-attributes`, a `verify.groovy` script co-located with the fixture.
**Target Platform**: Windows / Linux / macOS developer machines. The feature is a local-runnable test matrix, not a deployed service.
**Project Type**: Library/framework tooling (the archetype itself is a `maven-archetype`-packaged project). The feature adds test fixtures and (optionally) a `verify.groovy`.
**Performance Goals**: None fixed. Per intake, the suite is not on CI; the maintainer prioritizes coverage and simplicity over speed.
**Constraints**:
* Must not introduce real network, Git, or Docker calls (FR-007).
* Must not modify `bash_script/` (FR-011).
* Must not include security+observability combinations (FR-012).
* Must respect `DEPENDS_ON` rule (parent=false ⇒ children ignored) (FR-004).
* Must work with a clean local install of the archetype (Constitution Principle VI / FR-009).
**Scale/Scope**: ≥ 8 fixtures total (Spec SC-001): 1 `minimal`, 1 `full`, 1 `custom-attributes`, and 5 binding-properties sub-fixtures. Each fixture is a directory under `src/test/resources/projects/<scenario>/` with an `archetype.properties` and a `goal.txt` (`clean verify`); the `custom-attributes` fixture additionally has a `verify.groovy`.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Applicable? | Compliance | Notes |
|-----------|-------------|------------|-------|
| **I. Modularidade por Chave de Ativação** (NON-NEGOTIABLE) | Yes | ✅ Pass | The matrix is the explicit expression of this principle in test form: each fixture asserts that a flag does what it claims, and the `DEPENDS_ON` rule enforces that a disabled parent disables its children. |
| **II. Convenção Fixa de Nomenclatura** (NON-NEGOTIABLE) | Yes | ✅ Pass | New fixtures follow the existing `src/test/resources/projects/<name>/archetype.properties` convention. Naming of fixture subdirectories (`minimal`, `full`, `custom-attributes`, `binding-default-only`, etc.) is consistent with the archetype's existing `archetype-` prefix discipline. |
| **III. Pacote por Funcionalidade no Código Gerado** | Indirect | ✅ Pass | The matrix exercises the *generator*; it does not change the *generated* code's package layout. Generated projects continue to follow this principle; the tests simply verify the existing behavior is preserved. |
| **IV. Binding Duplo de Configuração Externalizada** | Yes | ✅ Pass | The `binding-properties` scenario explicitly provides one fixture per sub-module (default, list, map, mutable, validated) so the immutable and mutable variants are exercised independently. This is the auditable surface for Principle IV. |
| **V. Credenciais Nunca Entram no Repositório** | Yes | ✅ Pass | Fixtures use the same placeholder credentials already in `archetype-resources/protected_properties/*.properties` (admin_credentials, application_keys, authentication_credentials). No new credentials are introduced; the existing non-secret placeholders are reused. |
| **VI. Validar Antes de Culpar o Código** | Yes | ✅ Pass | The feature is explicitly designed to be run after a clean local install of the archetype (FR-009, Assumptions). The suite assumes the cache is fresh — a dirty cache is a known environmental issue, not a feature failure mode. |
| **Technology Stack** | Yes | ✅ Pass | Java 17, Spring Boot 4.0.0, JUnit 5, Groovy for `verify.groovy` — all consistent with the Constitution's stack. Maven `integration-test` goal is the standard mechanism (no new tool introduced). |
| **Governance / Compliance Review** | Yes | ✅ Pass | This change touches `src/test/resources/projects/` (test resources, not in the listed change-gate paths: `bash_script/`, `META-INF/maven/archetype-metadata.xml`, `META-INF/archetype-post-generate.groovy`, `archetype-resources/`). No compliance review required under the current governance rule. The optional `verify.groovy` for `custom-attributes` is a new test-only file; it does not modify the post-processor or file sets. |

**Verdict**: No Constitution violations detected. All NON-NEGOTIABLE principles are explicitly reinforced by the design, not weakened.

## Project Structure

### Documentation (this feature)

```text
specs/001-archetype-integration-tests/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── checklists/
│   └── requirements.md  # Spec quality checklist
└── tasks.md             # Phase 2 output (created by /speckit.tasks, NOT this command)
```

### Source Code (repository root)

The feature adds fixtures under the existing archetype test resources tree and (optionally) a single `verify.groovy` for the `custom-attributes` scenario. It does not introduce new source modules.

```text
# (Existing layout, unchanged for this feature)
archetype-v2-eda/
├── pom.xml                              # archetype packaging
├── src/
│   ├── main/
│   │   ├── resources/
│   │   │   ├── archetype-resources/     # generator template tree
│   │   │   └── META-INF/
│   │   │       ├── maven/
│   │   │       │   └── archetype-metadata.xml
│   │   │       └── archetype-post-generate.groovy
│   └── test/
│       └── resources/
│           └── projects/                # ← THIS FEATURE ADDS NEW FIXTURES HERE
│               ├── basic/               # (existing, may be migrated to `minimal`)
│               ├── minimal/             # NEW: all optional flags off
│               ├── full/                # NEW: all optional flags on
│               ├── custom-attributes/       # NEW: enable_custom_attributes + verify.groovy
│               ├── binding-default-only/    # NEW: enable_binding_properties + default_*
│               ├── binding-list-only/       # NEW: enable_binding_properties + list_*
│               ├── binding-map-only/        # NEW: enable_binding_properties + map_*
│               ├── binding-mutable-only/    # NEW: + mutable_binding_properties
│               └── binding-validated-only/  # NEW: + validated_binding_properties
└── bash_script/                         # (untouched per FR-011)
```

**Structure Decision**: Reuse the existing `src/test/resources/projects/` tree (Option 1 in the plan template — single project, default for this archetype). No new modules, no new top-level directories. The only structural addition is a `verify.groovy` co-located with the `custom-attributes` fixture.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

No Constitution Check violations. This section is intentionally empty.

---

## Phase 0: Research

See [research.md](./research.md). All technical-context unknowns (NEEDS CLARIFICATION) were resolved during the assessment phase (`/speckit.assess.research`) and carried into the spec; no new unknowns surfaced in Technical Context. Phase 0 research is therefore a confirmation pass, not an exploration.

## Phase 1: Design

See [data-model.md](./data-model.md) for the Fixture / Generated Project / Scenario entity model and [quickstart.md](./quickstart.md) for the runnable validation guide. No external interface contracts (this is an internal test tool — `contracts/` is intentionally not created).

### Re-evaluation of Constitution Check (post-Phase 1)

| Principle | Status after design | Change vs pre-Phase 0 |
|-----------|---------------------|------------------------|
| I. Modularidade | ✅ Pass — fixture graph encodes the rule. | No change. |
| II. Convenção | ✅ Pass — fixture subdir names follow `<scenario>[-<modifier>]` convention. | No change. |
| III. Pacote | ✅ Pass — generator unchanged. | No change. |
| IV. Binding Duplo | ✅ Pass — 5 binding sub-fixtures, each with the relevant variant on. | No change. |
| V. Credenciais | ✅ Pass — no new credentials; existing placeholders reused. | No change. |
| VI. Validar Antes | ✅ Pass — Assumptions document the clean-install requirement. | No change. |
| Technology Stack | ✅ Pass — Java 17, Spring Boot 4.0.0, Groovy for verify. | No change. |
| Governance | ✅ Pass — change is in test resources only, outside compliance-review paths. | No change. |

**Verdict (post-Phase 1)**: All gates still pass. No violations introduced by the design artifacts.