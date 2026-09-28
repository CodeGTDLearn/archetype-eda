# Quickstart Validation Guide: Inferência LLM das flags do `mvn archetype:generate`

- **Slug**: llm-flag-inference
- **Created**: 2026-10-12
- **Spec**: [spec.md](./spec.md)
- **Plan**: [plan.md](./plan.md)
- **Data model**: [data-model.md](./data-model.md)
- **CLI contract**: [contracts/cli-contract.md](./contracts/cli-contract.md)

This guide lists the runnable validation scenarios that prove the mechanism works end-to-end. It is **not** an implementation recipe — implementation details live in `tasks.md` and the source code. The scenarios below are what `/speckit.implement` will run to declare the feature done.

---

## Prerequisites

- Maven 3.9.16 (per `pom.xml` of the archetype).
- Java 17 (per the archetype's generated projects).
- Groovy 4.0.28 on the classpath (already pulled in by `maven-archetype-plugin:3.4.1`).
- The 10 existing IT profiles in `src/test/resources/projects/` (unmodified).
- `$EDA_LLM_INFERENCE_BIN` set to a path that produces the contract documented in [cli-contract.md](./contracts/cli-contract.md) Section 2. For CI, this points to a deterministic stub that ignores its inputs and returns a fixed `Map<String, String>` for testing.

---

## Scenario 1 — Happy path on all 10 IT profiles (covers SC-001)

**Goal**: Confirm that for each of the 10 IT profiles, the mechanism produces a command line that runs `mvn archetype:generate` end-to-end without prompts and the resulting project passes `mvn clean install` + `mvn integration-test`.

**Steps**:

0. **Pre-flight audit (covers SC-002 baseline):** run
   ```
   grep -nP 'read\s+-n\s+1\s+-s' bash_script/create_archetype_metadata.sh bash_script/create_archetype_post_generate.sh
   ```
   in `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\`. Assert the output contains exactly the 4 lines enumerated in `spec.md` SC-002 baseline enumeration (lines 67, 77, 84, plus the post-generate ones). If the count differs, abort: SC-002 baseline has drifted; the spec MUST be updated and the assessment pipeline re-run before continuing.

1. For each of the 10 profiles (`basic`, `binding-default-only`, `binding-list-only`, `binding-map-only`, `binding-mutable-only`, `binding-validated-only`, `defaults-only`, `full`, `custom-attributes`, `minimal`):
   1. Read the profile's `archetype.properties` to derive the expected flag set.
   2. Construct a JSON contract that mirrors those flags.
   3. Provide a textual description that names the profile.
   4. Run the mechanism:
      ```
      eda-llm-flag-inference --use-case-description "<profile-name> profile" \
                             --use-case-contract \
                             --inference-bin "$EDA_LLM_INFERENCE_BIN" \
                             < contract.json
      ```
   5. Capture the command line from stdout.
   6. Execute the captured command line from a temp directory.
   7. Run `mvn clean install` and `mvn integration-test` in the generated project.

**Expected outcome**: All 10 profiles produce a passing build. The captured command line, when executed, generates a project that compiles, packages, and surfaces the expected file system layout per the profile's fixture. The pre-flight audit returned exactly 4 `read -n 1 -s` lines (matches SC-002 baseline).

**Failure signal**: Any profile fails `mvn clean install` or `mvn integration-test`. Investigate the diff between the produced command line and the one expected from `archetype.properties`. Or the pre-flight audit returns a different count.

---

## Scenario 2 — Required inputs missing are rejected (covers SC-005; FR-001, FR-003)

**Goal**: Confirm that the mechanism rejects invocation when either the use-case description or the use-case contract is missing. The description gate runs first, so a missing description prevents stdin from being read.

**Steps** (PowerShell; substitute the equivalent syntax for other shells):

1. **Description missing**: run
   ```powershell
   groovy src/main/groovy/eda/postgenerate/llmflag/Orchestrator.groovy --use-case-contract
   ```
   (or simply omit `--use-case-description`). Observe exit code and stderr.

2. **Description as the last argument with no value**: run
   ```powershell
   groovy src/main/groovy/eda/postgenerate/llmflag/Orchestrator.groovy --use-case-description
   ```
   Observe exit code and stderr.

3. **Description blank**: run
   ```powershell
   groovy src/main/groovy/eda/postgenerate/llmflag/Orchestrator.groovy --use-case-description "   "
   ```
   Observe exit code and stderr.

4. **Contract missing (description present)**: run
   ```powershell
   groovy src/main/groovy/eda/postgenerate/llmflag/Orchestrator.groovy --use-case-description "basic profile"
   ```
   Observe exit code and stderr. The mechanism MUST NOT shell out to the inference script.

5. **Contract empty via empty stdin**: run
   ```powershell
   '' | groovy src/main/groovy/eda/postgenerate/llmflag/Orchestrator.groovy --use-case-description "basic profile" --use-case-contract
   ```
   Observe exit code and stderr.

6. **Description followed by another flag (parser must not swallow the next flag as a value)**: run
   ```powershell
   '' | groovy src/main/groovy/eda/postgenerate/llmflag/Orchestrator.groovy --use-case-description --use-case-contract
   ```
   Observe exit code and stderr. The parser MUST reject this as a missing description (FR-001), not as a missing contract (FR-003) — the contract flag must remain free to be parsed as a real flag.

**Expected outcome**:

* All six cases exit with code `2`.
* Cases 1, 2, 3 and 6 (description missing) print the `USE CASE DESCRIPTION is missing or blank.` error block. Stderr references `FR-001`. The mechanism MUST NOT read stdin.
* Cases 4 and 5 (contract missing) print the `USE CASE CONTRACT is missing or empty.` error block. Stderr references `FR-003`.
* In every case, stdout is empty and no inference shell-out happens.

---

## Scenario 2.1 — Provenance report on stderr (covers FR-020)

**Goal**: Confirm that, on both cache miss and cache hit, the mechanism prints a Markdown provenance report on stderr after the command line on stdout — listing per-flag source (`CONTRACT` / `INFERENCE` / `DEFAULT`), FR-006 regex-rejection notes, FR-009 discarded LLM proposals, and a summary count line — and that no description or non-catalog contract content leaks (FR-017).

```
<contract.json echo or pipe>
groovy src/main/groovy/eda/postgenerate/llmflag/Orchestrator.groovy \
  --use-case-description "<profile-name> profile" \
  --use-case-contract \
  --inference-bin "<path-to-real-or-stub>" \
  < contract.json
```

**Expected (cache MISS, fresh inference)**:
* stdout: exactly one line — the `mvn archetype:generate --batch-mode ...` command (unchanged contract).
* stderr: starts with `## Relatorio de Proveniencia - cache: MISS` followed by the Markdown table, an optional `### Propostas do LLM descartadas ...` section, and a `**Resumo**: <c> CONTRACT / <i> INFERENCE / <d> DEFAULT / <x> descartada(s)` line.
* Exit code 0.

**Expected (cache HIT, repeat invocation with identical input)**:
* stdout: the same command line (byte-equivalent to the previous run).
* stderr: `## Relatorio de Proveniencia - cache: HIT` followed by the same report body just written on the previous miss.
* If the cache entry was written before FR-020 (legacy v1 line), stderr shows the explicit note `(relatorio indisponivel: entrada de cache legada, gravada antes da FR-020)` instead of a body.
* Exit code 0.

**Guards to verify**:
* The textual description and any contract field whose key is NOT a catalog flag MUST NOT appear anywhere in the report body or the cached `r` field (FR-017).
* Pipe characters inside values are escaped (`\|`) so the table stays well-formed.
* stdout remains exactly one command line — capture `stdout` and assert that grep matches only the `mvn archetype:generate` line.
* The 6 gate cases from Scenario 2 (description missing / contract missing) still emit the original error blocks and exit code 2 — FR-020 is additive and must not regress FR-001/FR-003.

---

## Scenario 3 — Cache hit on repeated invocation (covers SC-004)

**Goal**: Confirm that two consecutive invocations with identical inputs skip the inference step on the second call and produce byte-equivalent command lines.

**Steps**:

1. Choose a fixed description and contract.
2. Run the mechanism twice in succession. Between the two runs, count how many times the external inference script is invoked (it must support a `--count-invocations` flag for this test).
3. Diff the two stdout lines byte-by-byte.

**Expected outcome**: Invocation count = 1 (one call on the first run, zero on the second). The two stdout lines are byte-equivalent.

**Failure signal**: Invocation count > 1, or any byte difference between the two stdout lines.

---

## Scenario 4 — Cache key is stable across JSON field reordering (covers User Story 3 acceptance scenario 2)

**Goal**: Confirm that semantically identical contracts with different field orderings produce the same cache key.

**Steps**:

1. Construct contract A: `{"api_port":"8080","enable_jwt_authentication":"true"}`.
2. Construct contract B: `{"enable_jwt_authentication":"true","api_port":"8080"}`.
3. Run the mechanism with each.
4. Inspect the cache file (`$XDG_CACHE_HOME/eda-archetype/llm-flag-inference/cache.jsonl`). Both entries MUST have the same `k` field.

**Expected outcome**: Both invocations write/lookup the same cache key. Only one cache entry is present after both runs.

**Failure signal**: Two distinct cache entries, or invocation count = 2 (the second should hit the cache).

---

## Scenario 5 — Catalog drift is detected (covers User Story 4 acceptance scenario 2)

**Goal**: Confirm that a flag removed from the canonical catalog causes any proposal referencing it to be rejected.

**Steps**:

1. Temporarily add a new flag `enable_test_xyz` to `archetype_metadata_properties/property-authentication.xml` (or any existing property file). Rebuild.
2. Run the mechanism with a description and contract that activate the new flag.
3. Observe that the command line includes `-Denable_test_xyz=...`.
4. Revert the XML change, rebuild.
5. Run the mechanism with the same description and contract.
6. Observe: either (a) the inference script no longer emits the key (the contract's other fields still work), or (b) the merger discards the unknown key and the command line does NOT include `-Denable_test_xyz=...`.

**Expected outcome**: Step 3 produces a command line with `-Denable_test_xyz=...`. Step 6 produces a command line without it.

**Failure signal**: Step 6 still includes the deleted flag — indicates the validator is using a stale catalog.

---

## Scenario 6 — Validation regex is enforced (covers FR-006, SC-006)

**Goal**: Confirm that a value failing `<validationRegex>` is rejected and the default is applied.

**Steps**:

1. Construct a contract that includes `"api_port":"999"` (out of the `^([1-9][0-9]{3,4})$` regex range for `property-api-general.xml`).
2. Run the mechanism.
3. Observe stderr for the warning line.
4. Observe stdout: the command line uses the default `8080`, not `999`.

**Expected outcome**: Stderr contains the warning `Flag 'api_port' value '999' fails validation regex '^([1-9][0-9]{3,4})$'. Default value '8080' will be used instead.` Stdout's command line contains `-Dapi_port=8080`.

**Failure signal**: Command line contains `-Dapi_port=999`, or no warning is printed.

---

## Scenario 7 — Legacy bash scripts are removed (covers SC-002, SC-007)

**Goal**: Confirm that after all 10 IT profiles pass via the mechanism, the legacy bash scripts are removed from version control.

**Steps**:

1. Run Scenario 1 successfully for all 10 profiles.
2. Run:
   ```
   git ls-files bash_script/
   ```
3. Confirm: `maven_command_line_archetype_generate.sh`, `general_functions.sh`, and the 14 `user_*_input.sh` files are absent.

**Expected outcome**: Empty output (or only files explicitly excluded from the deletion, if any — none currently are).

**Failure signal**: Any of the listed files still appears in `git ls-files`.

---

## Scenario 8 — Zero `read -n 1 -s` in the project generation flow (covers SC-002; scoped to the new mechanism code only)

**Goal**: Confirm that the new mechanism has no interactive-prompt calls. (The legacy bash traps `read -n 1 -s` are tracked separately by SC-002 post-US6, verified by Scenario 7.)

**Steps**:

1. Grep the new Groovy sources under `src/main/groovy/eda/postgenerate/llmflag/` for `System.console().readPassword`, `Scanner.nextLine` blocking calls, `readLine` without timeout, or any equivalent. The legacy bash traps are out of scope here — they are removed in US6 (T031–T034) and asserted in Scenario 7.

**Expected outcome**: No blocking-read calls in the new mechanism code.

**Failure signal**: Any blocking-read call appears — the new mechanism MUST be non-interactive.

---

## Scenario 9 — Contract content never persists to disk (covers FR-017, Principle V)

**Goal**: Confirm that the JSON contract's content never reaches the file system outside the invocation's memory.

**Steps**:

1. Construct a contract with a sentinel value like `"SECRET_MARKER_42": "do-not-persist"`.
2. Run the mechanism.
3. Inspect the cache file: it MUST NOT contain the literal string `SECRET_MARKER_42`.
4. Inspect any temp files the mechanism creates during the run (the inference script's `--use-case-description-file` and `--use-case-contract-file` paths): they MUST be deleted by the time the mechanism returns.

**Expected outcome**: No occurrence of `SECRET_MARKER_42` in the cache or any persistent file.

**Failure signal**: The literal `SECRET_MARKER_42` appears anywhere on disk after the invocation returns.

---

## Scenario 10 — Spec-kit orientation text references the mechanism (covers FR-015)

**Goal**: Confirm that the spec-kit templates (spec, plan, test phases) include textual guidance pointing operators to this mechanism.

**Steps**:

1. Locate the spec-kit template files (typically under `.specify/templates/` or the spec-kit repo's templates directory, depending on the operator's setup).
2. Grep for the mechanism's name or the keyword `eda-llm-flag-inference`.

**Expected outcome**: Each of the spec, plan, and test templates contains an instruction like "Use `eda-llm-flag-inference` to produce the project generation command line."

**Failure signal**: The orientation text is missing from one or more templates.

---

## Scenario 11 — Empty `defaultValue` is rejected at build time (covers FR-018)

**Goal**: Confirm that any flag with empty `<defaultValue></defaultValue>` causes the mechanism to fail the build with exit code 4, before any inference or contract processing.

**Steps**:

1. Temporarily add a new flag to `archetype_metadata_properties/property-authentication.xml` with empty default:
   ```xml
   <requiredProperty key="enable_test_xyz">
       <defaultValue></defaultValue>
   </requiredProperty>
   ```
2. Rebuild: `mvn clean install`.
3. Observe exit code and stderr.

**Expected outcome**: Exit code is `4`. Stderr contains `Flag 'enable_test_xyz' has empty defaultValue. Catalog is invalid per FR-018.` No command line is produced.

**Failure signal**: Exit code 0 (build succeeded with the empty default), or any exit code other than 4, or no error message naming the flag.

---

## How these scenarios map to spec success criteria

| SC | Covered by scenario |
|---|---|
| SC-001 (10 IT profiles pass) | Scenario 1 |
| SC-002 (0 `read -n 1 -s`) | Scenarios 7 (post-deletion) + 8 (new code only) |
| SC-003 (100% catalog recognized) | Scenario 5 |
| SC-004 (cache hit, byte-equivalent) | Scenarios 3, 4 |
| SC-005 (contract required, exit 2) | Scenario 2 |
| SC-006 (validation regex enforced) | Scenario 6 |
| SC-007 (legacy scripts removed) | Scenario 7 |
| FR-013 (no hardcoded flag list) | Scenario 5 |
| FR-015 (spec-kit orientation) | Scenario 10 |
| FR-017 (no contract content on disk) | Scenario 9 |

---

## What to do if a scenario fails

1. Do **not** declare the feature adopted.
2. File a bug with: scenario number, expected vs. actual outcome, the diff (if any) between the produced command line and the expected one.
3. Re-run the entire scenario suite after the fix. SC-001 (Scenario 1) is the strongest gate — if it fails, the legacy bash scripts MUST remain in place.
4. Only after **all** scenarios pass is SC-007 (Scenario 7) actionable.
