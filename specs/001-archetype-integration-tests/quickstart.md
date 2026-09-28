# Quickstart: Integration Test Matrix for eda-archetype

**Slug**: archetype-integration-tests
**Created**: 2026-09-25

This guide walks a maintainer through running the new test matrix locally and verifying it catches regressions. Implementation details (which file goes where) live in the spec and plan; this document focuses on the **end-to-end validation flow**.

## Prerequisites

* Java 17 (per Constitution Technology Stack).
* Maven 3.9.16 (or compatible 3.9.x).
* The archetype is installed **cleanly** in the local Maven repository. Per Constitution Principle VI ("validate before blaming the code"), a dirty cache masks regressions. Clean install:
  ```bash
  cd archetype-v2-eda
  mvn clean install
  ```
* Network access to Maven Central (first run only, to fetch Spring Boot 4.0.0, Lombok, Logback, etc.). Subsequent runs can be offline.
* (Optional, for the `json-contract` fixture) A sample JSON contract file matching the structure the post-processor expects.

## What ships in this feature

Under `src/test/resources/projects/`, one new subdirectory per scenario (plus the existing `basic/` if retained):

| Directory | Scenario | Has `verify.groovy`? |
|---|---|---|
| `minimal/` | All optional flags off. Baseline. | No |
| `full/` | All optional flags on. Stress. | No |
| `json-contract/` | `enable_json_entity_contracts=true`. | **Yes** |
| `binding-default-only/` | Parent on + only `default_*` on. | No |
| `binding-list-only/` | Parent on + only `list_*` on. | No |
| `binding-map-only/` | Parent on + only `map_*` on. | No |
| `binding-mutable-only/` | Parent on + `mutable_binding_properties` set to `1`. | No |
| `binding-validated-only/` | Parent on + `default_*` on + `validated_*` on. | No |

Each fixture directory contains:
* `archetype.properties` — flag values for that scenario.
* `goal.txt` — Maven goal to run in the generated project (always `clean verify`).
* (`json-contract` only) `verify.groovy` — structural assertion script.

## Running the full matrix

From the repository root:

```bash
cd archetype-v2-eda
mvn clean install
mvn integration-test
```

Or as a single line: `mvn clean install integration-test`. Do NOT use `mvn archetype:integration-test` — that goal fails with "Unable to get the archetypes' artifact which should have just been built" because it looks for the .jar in `~/.m2/repository/` instead of `target/`.

Expected outcome: a passing run for each fixture, in sequence. The output should report one passing test per fixture. The `json-contract` fixture takes longer (extra `verify.groovy` step).

## Running a single fixture (faster local loop)

To iterate on a single scenario without running the whole matrix:

```bash
cd archetype-v2-eda
mvn clean install integration-test -Darchetype.test.projects=minimal
```

(The `archetype.test.projects` property filters which fixtures under `src/test/resources/projects/` are run. Confirm the exact property name in `pom.xml` / plugin docs if the default differs.)

## Verifying the matrix catches a real regression

To prove the matrix is doing its job, induce a regression and confirm the corresponding fixture fails:

1. **Baseline**: run the matrix; all fixtures should pass.
2. **Inject a regression**: edit `archetype-post-generate.groovy` to delete an unconditional line, or edit a `defaultValue` in a `property-*.xml` to an invalid value.
3. **Re-run**: at least one fixture should fail with a diagnostic pointing at the changed file.
4. **Revert**: restore the change; re-run; all fixtures should pass again.

For the `json-contract` scenario specifically, edit the input JSON contract to drop an attribute and re-run: the `verify.groovy` should report the missing attribute (not just a generic build failure).

## Verifying the dependency rule (FR-004)

Pick a parent flag with known children (e.g., `enable_logback` with children `logback_console_type`, `enable_logback_appender_smtp`, `enable_logback_appender_file`). In one of the binding-properties sub-fixtures (or a temporary new fixture), set the parent to `false` and a child to a non-default value. The archetype generation must fail with the child's value being ignored, or — depending on the archetype's behavior — the child value being passed but unused. Either way, the failure must be unambiguous.

## Verifying isolation (FR-001)

Run the full matrix twice in a row. The second run should be a no-op or a clean run; no state from the first run should leak. To stress-test isolation, induce a failure in one fixture (e.g., break the `goal.txt` of `full/` temporarily) and confirm the other fixtures still report their normal pass/fail independent of the `full/` failure.

## Verifying the offline-runnable constraint (FR-007)

After the first online run, disconnect from the network and re-run the matrix. All fixtures should still pass (Maven uses the populated local cache). The `json-contract` fixture's `verify.groovy` MUST NOT perform any network call (e.g., no HTTP fetches inside the script). If a future edit of `verify.groovy` introduces a network call, this offline test will fail.

## Out of scope (per spec / intake / constitution)

* CI integration — the matrix is local-only by design (spec FR-010, intake `Out of Scope`).
* Modifying `bash_script/` — forbidden by spec FR-011.
* Security + observability combinations (JWT + Logback + cPanel/ELK) — out of scope per spec FR-012, deferred to a future assessment.

## Troubleshooting

| Symptom | Likely cause | Fix |
|---|---|---|
| All fixtures fail with "unknown property" | A flag in `archetype.properties` is not declared in any `property-*.xml`. | Re-check the property name against `property-*.xml`. |
| One fixture fails, others pass | The failing fixture's flags exercise a code path with a regression. | Inspect the build log; cross-reference with `archetype-post-generate.groovy` for the relevant `if` block. |
| `json-contract` reports a malformed-contract error | The sample JSON contract file is not parseable or doesn't match the schema the post-processor expects. | Re-check the contract against the `archetype-post-generate.groovy` JSON-entity generation path. |
| Build hangs on dependency download | First run only — Maven is downloading Spring Boot 4.0.0, Lombok, etc. | Wait. Subsequent runs use the local cache. |
| `mvn integration-test` reports "no projects found" | The `src/test/resources/projects/` directory is empty or the filter is wrong. | Verify the directory layout; re-check `-Darchetype.test.projects` value. |
| `mvn archetype:integration-test` reports "Unable to get the archetypes' artifact which should have just been built" | You ran the goal directly without installing the archetype first. | Use `mvn clean install integration-test` instead, or run `mvn clean install` followed by `mvn integration-test`. |