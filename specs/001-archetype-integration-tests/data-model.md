# Data Model: Integration Test Matrix for eda-archetype

**Slug**: archetype-integration-tests
**Created**: 2026-09-25

This data model describes the **test artifacts** (not the generated Spring Boot project). The generated project is described only insofar as it is a verification target.

## Entities

### Fixture

A self-contained description of one scenario's flag values, plus a verification goal, located in a dedicated directory.

| Attribute | Type | Required | Description |
|---|---|---|---|
| `name` | string (kebab-case) | yes | Unique identifier; matches the subdirectory name under `src/test/resources/projects/<name>/`. Examples: `minimal`, `full`, `json-contract`, `binding-default-only`. |
| `archetype_properties` | map<string,string> | yes | The set of flag values. Sourced from `property-*.xml` defaults unless explicitly overridden. May only contain keys declared in `property-*.xml` (archetype rejects unknowns). |
| `goal_txt` | string | yes | The Maven goal to run in the generated project. Always `clean verify` per intake. |
| `verify_groovy` | string (file content) | conditional | Present only for the `json-contract` fixture. Asserts on the generated entity's structure against the input JSON contract. |
| `scenario` | enum | yes | Logical grouping. One of: `minimal`, `full`, `json-contract`, `binding-properties`. |
| `parent_flags_off` | map<string,bool> | no | Documents which parent flags are off in this fixture, so child flags are not set. (Informational; the actual `archetype.properties` is the source of truth.) |

**Validation rules**:
- The `name` must be unique across all fixtures in `src/test/resources/projects/`.
- Every key in `archetype_properties` must correspond to a `<requiredProperty key="…">` in `src/main/resources/META-INF/maven/archetype_metadata_properties/*.xml`.
- Values must satisfy the `validationRegex` declared in the corresponding `requiredProperty`.
- If `parent_flags_off[parent] = true`, then no child of `parent` may appear in `archetype_properties` (per spec FR-004).

### Scenario

A logical grouping of fixtures sharing a thematic purpose.

| Attribute | Type | Description |
|---|---|---|
| `name` | enum | One of: `minimal`, `full`, `json-contract`, `binding-properties`. |
| `purpose` | string | Plain-language description (matches the spec's user-story narrative). |
| `fixtures` | list<Fixture> | The fixtures belonging to this scenario. |
| `acceptance_signal` | enum | `mvn-verify-only` or `mvn-verify-plus-structural` (the latter only for `json-contract`). |

**Validation rules**:
- `minimal` and `full` each have exactly one fixture.
- `json-contract` has exactly one fixture, and that fixture MUST have `verify_groovy` non-null.
- `binding-properties` has at least one fixture per sub-module: `default`, `list`, `map`, `mutable`, `validated` (5 minimum).

### Generated Project

The temporary Spring Boot project produced by running the archetype generation with a given fixture's flags. Exists only for the duration of a fixture run.

| Attribute | Type | Description |
|---|---|---|
| `source_fixture` | Fixture (ref) | The fixture whose flags were used. |
| `build_status` | enum | `pass` (build succeeded), `fail` (build failed), `error` (post-processor or generation error). |
| `generated_layout` | map<path,exists> | Per-file existence map used by post-build assertions (only populated for `json-contract` via `verify.groovy`). |
| `generated_entity_shape` | map<field,type> | Only populated for `json-contract`; asserts structural match with the input contract. |

**State transitions** (per fixture run):
1. `pending` (before archetype generation)
2. `generating` (archetype running)
3. `verifying` (`mvn verify` running in the generated project, plus `verify.groovy` for `json-contract`)
4. → `pass` OR `fail` OR `error`

## Relationships

```
Scenario (1) ──< (n) Fixture
Fixture (1) ──< (0..1) VerifyGroovy
Fixture (1) ──< (0..n) FlagValue  (one per key in archetype_properties)
FlagValue (n) ──> (1) RequiredProperty (defined in property-*.xml)
Fixture (1) ──< (n) GeneratedProject (one per run, ephemeral)
GeneratedProject (n) ──> (1) Scenario (via source_fixture)
```

## Storage

* **Fixtures**: filesystem (subdirectories under `src/test/resources/projects/`).
* **Generated projects**: ephemeral, in `target/test-classes/projects/<fixture-name>/target/` (the standard `maven-archetype-plugin` IT layout).
* **No persistent data store** — the test matrix is configuration, not state.

## Schema Notes

* This data model is **not** a database schema; it is a conceptual model that the directory tree + `archetype.properties` files implement.
* The schema is implicitly enforced by:
  - Maven's archetype generator (rejects unknown properties).
  - The regex validations in each `requiredProperty` (rejects malformed values).
  - The fixture name uniqueness (rejected by Maven at suite setup).
  - The dependency rule (not enforced automatically; relies on manual curation, with archetype failure as the safety net).