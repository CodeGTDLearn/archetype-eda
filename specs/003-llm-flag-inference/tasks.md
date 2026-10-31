# Tasks: Inferência LLM das flags do `mvn archetype:generate`

**Input**: Design documents from `/specs/003-llm-flag-inference/`
- `plan.md` (required) — Groovy 4.0.28 build-time extension, no new dependencies, single project layout.
- `spec.md` (required) — 6 user stories (P1, P1, P2, P2, P3, P3), 17 functional requirements, 7 success criteria.
- `data-model.md` — 7 entities (Textual Description, JSON Contract, Flag Catalog Entry, Flag Proposal, Generation Command Line, Cache Key + Entry, External Inference Script).
- `contracts/cli-contract.md` — operator-facing CLI args, external inference-script contract, JSONL cache format, 5 error codes.
- `research.md` — 6 technology decisions; reuse of `gmaven:2.1.1`, `xml-maven-plugin:1.2.1`, Groovy 4.0.28 transitive.
- `quickstart.md` — 10 validation scenarios mapped to the 7 SCs.

**Tests**: Plan.md mandates a unit test suite for the validator + the existing 10 IT profiles serve as integration tests. Tests are included in this task list and grouped per user story.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies on incomplete tasks).
- **[Story]**: Which user story this task belongs to (`US1` through `US6`).
- File paths are absolute Windows paths rooted at `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\`.

> **⚠️ NOTA DE ESTADO DA MATRIZ (2026-10-23)**: as Fases 3–9 (T010–T042) e 10–11 (T051–T077) são
> **listas duplicadas** do mesmo trabalho que as Fases 12–13 (T078–T116) executaram de fato. Os
> checkboxes `[ ]` dessas fases são **obsoletos** — o trabalho foi feito pelas T equivalentes das
> Fases 12–13. A **fonte da verdade é o estado em disco**, não o checkbox.
>
> Além disso, 9 arquivos de teste que a matriz marcava como `[X]` **não existiam**; foram criados
> em 2026-10-23 (ver Fase 14). A matriz de cobertura auditada (T × FR × teste × fragilidade) está
> em `C:\Users\edufi\.opencode\plan\matriz-cobertura-testes.md`.

## Path Conventions

- Single project (matches `plan.md`): `src/main/groovy/`, `src/test/groovy/`, `src/main/resources/`, `src/test/resources/` at repository root.
- Build-time orchestration wired into the existing `pom.xml`.

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [X] T001 Create Groovy source package directory `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\src\main\groovy\eda\postgenerate\llmflag\`
- [X] T002 [P] Create Groovy test package directory `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\src\test\groovy\eda\postgenerate\llmflag\`
- [X] T003 [P] Create external inference script directory `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\scripts\` (will hold the operator-supplied inference stub for testing)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [X] T004 [P] Implement `JsonCanonicalizer` in `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\src\main\groovy\eda\postgenerate\llmflag\JsonCanonicalizer.groovy` — accepts a parsed `Map<String, Object>` (from `groovy-json` `JsonSlurper`), recursively sorts object keys alphabetically, serializes to a canonical JSON string, and computes the SHA-256 hash via JDK `MessageDigest`. Per research.md Decision 4 and data-model.md Entity 6.
- [X] T005 [P] Implement `FlagCatalogExtractor` in `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\src\main\groovy\eda\postgenerate\llmflag\FlagCatalogExtractor.groovy` — reads `target/classes/META-INF/maven/archetype-metadata.xml` (already XInclude-merged by `xml-maven-plugin:1.2.1` + `identity.xsl` in phase `generate-resources`), parses with Groovy 4.0.28 transitive `groovy-xml` (`XmlSlurper`), returns `Map<String, CatalogEntry>` keyed by `requiredProperty` name. **Asserts every entry has a non-empty `defaultValue` (FR-018); if any flag has `<defaultValue></defaultValue>`, the extractor MUST throw an exception naming the offending flag and the mechanism MUST exit with code 4.** Per research.md Decision 3 and data-model.md Entity 3.
- [X] T006 [P] Implement `InvocationCache` in `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\src\main\groovy\eda\postgenerate\llmflag\InvocationCache.groovy` — read/write/lookup on a JSONL file at `$XDG_CACHE_HOME/eda-archetype/llm-flag-inference/cache.jsonl` (Windows fallback: `%LOCALAPPDATA%\eda-archetype\llm-flag-inference\cache.jsonl`). Writes use `tmp + File.renameTo` for atomicity. Stores only `{k, v, t}` — NEVER the contract payload. Per research.md Decision 5, FR-017, and Principle V.
- [X] T007 Extend `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\pom.xml` by adding a second `<execution>` inside the existing `org.codehaus.gmaven:groovy-maven-plugin:2.1.1` `<plugin>` entry, pointing to the Orchestrator Groovy script (T012) at phase `process-resources`. **NO new dependencies**. The existing `<execution>` for `SnippetComposer.groovy` MUST remain unchanged.
- [X] T008 [P] Create external inference script stub at `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\scripts\inference-stub.sh` — accepts `--use-case-description <text>` and `--use-case-contract <json>` (or `--use-case-description-file` + `--use-case-contract-file` per cli-contract.md Section 2), prints a single-line JSON object `Map<String, String>` to stdout, exits 0. Defaults are hardcoded for testing; the real operator-supplied script replaces this in production. Stub MUST set LLM `temperature = 0` per FR-012.

**Checkpoint**: Foundation ready — JsonCanonicalizer, FlagCatalogExtractor, InvocationCache, pom.xml wiring, and inference stub exist; user story implementation can now begin.

---

## Phase 3: User Story 1 — Happy path: operator generates a project from textual description + JSON contract (Priority: P1) 🎯 MVP

**Goal**: The mechanism returns a single non-interactive command line that, when executed, generates a project without prompts. All 10 existing IT profiles must pass when run via the produced command lines.

**Independent Test**: Run `mvn integration-test` on each of the 10 existing profiles (`basic`, `binding-default-only`, `binding-list-only`, `binding-map-only`, `binding-mutable-only`, `binding-validated-only`, `defaults-only`, `full`, `custom-attributes`, `minimal`). For each profile, derive a JSON contract from its `archetype.properties`, run the mechanism, and execute the produced command line. All 10 must pass.

### Implementation for User Story 1

- [X] T009 [P] [US1] Create `CatalogEntry` class in `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\src\main\groovy\eda\postgenerate\llmflag\CatalogEntry.groovy` — fields: `key` (String, non-empty, unique), `defaultValue` (String, MUST be non-empty per FR-018), `validationRegex` (String, optional, MUST compile as `java.util.regex.Pattern` if present), `description` (String, metadata only). Constructor MUST reject empty `defaultValue` and throw an exception naming the flag.
- [ ] T010 [US1] Implement `FlagProposalMerger` in `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\src\main\groovy\eda\postgenerate\llmflag\FlagProposalMerger.groovy` — takes `(llmInference: Map<String, String>, contract: Map<String, Object>, catalog: Map<String, CatalogEntry>)` and returns `Map<String, String>` applying precedence: contract value > LLM inference > default (FR-007/008); discards LLM-inferred flags absent from the contract (FR-009); replaces values failing `<validationRegex>` with the default (FR-006). Per data-model.md Entity 4.
- [ ] T011 [US1] Implement `CommandLineAssembler` in `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\src\main\groovy\eda\postgenerate\llmflag\CommandLineAssembler.groovy` — takes `Map<String, String>` (validated proposal) and returns a single-line string beginning with `mvn archetype:generate --batch-mode`, followed by `-D<key>=<value>` pairs in catalog order. **Escape rule (per data-model.md Entity 5): wrap a value in double quotes iff the value contains whitespace, `$`, `` ` ``, `\`, `"`, `&`, `|`, `<`, `>`, `;`, `(`, `)`, `*`, `?`, `[`, `]`, `{`, `}`; escape embedded `"` as `\"` inside the wrap. Empty values are emitted as `-D<key>=` (no quotes).** MUST emit at most one `-D<key>=<value>` per catalog key (data-model.md Entity 5 cross-entity invariant).
- [ ] T012 [US1] Implement `Orchestrator` in `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\src\main\groovy\eda\postgenerate\llmflag\Orchestrator.groovy` — CLI entry script. Parses args (`--use-case-description`, `--use-case-contract` (valueless → reads contract JSON from stdin), `--cache-dir`, `--inference-bin` per cli-contract.md Section 1); orchestrates the flow: Extractor → (cache check, deferred to Phase 5) → external Inference shell-out → Merger → Assembler → (cache write, deferred). For US1 only: no contract gate yet (US2), no cache yet (US3).
- [ ] T013 [P] [US1] Unit test `FlagCatalogExtractorTest` in `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\src\test\groovy\eda\postgenerate\llmflag\FlagCatalogExtractorTest.groovy` — reads the real `target/classes/META-INF/maven/archetype-metadata.xml` after a clean `mvn process-resources`, asserts exactly 32 entries, asserts presence of `enable_jwt_authentication`, `api_port`, `mutable_binding_properties`, and `properties_filetype`. **Plus a negative case using a synthetic `archetype-metadata.xml` with one `<requiredProperty key="enable_test_xyz"><defaultValue></defaultValue></requiredProperty>` — asserts the extractor throws an exception naming `enable_test_xyz` per FR-018.**
- [ ] T014 [P] [US1] Unit test `CatalogEntryTest` in `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\src\test\groovy\eda\postgenerate\llmflag\CatalogEntryTest.groovy` — covers validation-regex compilation (rejects invalid patterns), default-value semantics (empty `defaultValue` is REJECTED by the constructor per FR-018, throwing an exception naming the flag), and key-uniqueness enforcement.
- [ ] T015 [P] [US1] Unit test `FlagProposalMergerTest` in `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\src\test\groovy\eda\postgenerate\llmflag\FlagProposalMergerTest.groovy` — covers all 4 precedence rules: contract > LLM, LLM fills in, default fills in, LLM-outside-contract discarded. Includes the `api_port=999` (out-of-range) case asserting fallback to default `8080`.
- [ ] T016 [US1] Unit test `CommandLineAssemblerTest` in `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\src\test\groovy\eda\postgenerate\llmflag\CommandLineAssemblerTest.groovy` — asserts command-line format (`mvn archetype:generate --batch-mode` prefix), single `-D<key>=<value>` per catalog key, no duplicates, deterministic key ordering. **Plus escape-rule cases (per data-model.md Entity 5):** (a) a value containing whitespace (e.g., `entity_name=My Entity`) MUST be wrapped in double quotes; (b) a value containing a literal `"` (e.g., `project_description=He said "hi"`) MUST be wrapped with embedded `\"`; (c) a plain value (e.g., `api_port=8080`) MUST be emitted verbatim without quotes; (d) an empty value MUST be emitted as `-D<key>=` (no quotes, empty right-hand side).
- [ ] T017 [US1] Run `mvn clean install` followed by `mvn integration-test` in `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\` — assert all 10 IT profiles pass using command lines produced by the mechanism. Maps to quickstart.md Scenario 1 and SC-001.

**Checkpoint**: User Story 1 is fully functional — the mechanism produces a passing command line for each of the 10 IT profiles. Test scenarios US1 pass.

---

## Phase 4: User Story 2 — JSON contract missing is rejected (Priority: P1)

**Goal**: Without a JSON contract, the mechanism exits with code 2 and produces no command line; the inference step is not triggered.

**Independent Test**: Invoke the mechanism with only `--use-case-description` (no `--use-case-contract`). Assert exit code 2, stderr matches the format from cli-contract.md Section 1, stdout is empty, no inference-shell-out was made.

### Tests for User Story 2

- [ ] T018 [P] [US2] Unit test `OrchestratorContractGateTest` in `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\src\test\groovy\eda\postgenerate\llmflag\OrchestratorContractGateTest.groovy` — invokes Orchestrator with description only, asserts exit code 2, asserts stderr contains `ERROR: JSON contract is required.`, asserts stdout is empty. **Plus CLI-argument parsing tests for FR-001 and FR-002:** (a) `--use-case-description "<text>"` parsed as arg; (b) `--use-case-contract` reads the JSON contract from stdin (multi-line JSON accepted); (c) unknown args are rejected with a non-zero exit and a usage message. **Plus FR-001 description gate (description checked BEFORE contract):** (d) no `--use-case-description` exits with code 2 and the `USE CASE DESCRIPTION is missing or blank.` error; (e) `--use-case-description` as the last argument with no value exits with code 2 and the same error; (f) blank/whitespace-only description exits with code 2 and the same error; (g) `--use-case-description` followed by another `--` token (e.g. `--use-case-description --use-case-contract`) exits with code 2 and the FR-001 error — the next flag MUST NOT be swallowed as a description value; in all four cases stdin MUST NOT be read and inference MUST NOT be invoked.

### Implementation for User Story 2

- [ ] T019 [US2] Add contract-missing gate at the top of `Orchestrator.groovy` in `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\src\main\groovy\eda\postgenerate\llmflag\Orchestrator.groovy` — before Extractor, before any other work. On missing contract: write the stderr message from cli-contract.md Section 1, exit with code 2 (FR-003). MUST NOT shell out to the inference script (verifiable by `inference-stub.sh` invocation count).

**Checkpoint**: User Story 2 passes — missing contract halts before inference. US1 still passes (US2's gate is the only addition).

---

## Phase 5: User Story 3 — Repeated invocation with same input returns cached command line (Priority: P2)

**Goal**: Same `(description + contract)` input produces byte-equivalent command line across invocations; the inference step is skipped on cache hit.

**Independent Test**: Run the mechanism twice with identical inputs. Assert (a) the inference stub's invocation counter increments only once, (b) the two stdout lines are byte-equivalent, (c) the cache file contains exactly one entry for the input. Repeat with field-reordered JSON contracts and assert cache hit (Scenario 4 of quickstart.md).

### Tests for User Story 3

- [ ] T020 [P] [US3] Unit test `JsonCanonicalizerTest` in `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\src\test\groovy\eda\postgenerate\llmflag\JsonCanonicalizerTest.groovy` — asserts sorted-key serialization (two maps with same content but different key insertion order produce identical canonical strings), asserts SHA-256 hex output is 64 lowercase chars, asserts stability across calls.
- [ ] T021 [P] [US3] Unit test `InvocationCacheTest` in `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\src\test\groovy\eda\postgenerate\llmflag\InvocationCacheTest.groovy` — uses a temp cache directory; asserts lookup returns the stored value, write appends atomically (no partial lines visible to a concurrent reader), and the file contains NO occurrence of any sentinel value the caller would have stored in the contract (covers FR-017 / Principle V).

### Implementation for User Story 3

- [ ] T022 [US3] Wire `JsonCanonicalizer` + `InvocationCache` into `Orchestrator.groovy` in `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\src\main\groovy\eda\postgenerate\llmflag\Orchestrator.groovy` — after contract gate (US2) and before inference shell-out: compute cache key via JsonCanonicalizer, lookup in InvocationCache; on hit, return cached command line and exit 0. On miss: run inference, merge, assemble, write cache entry, return produced command line (FR-011/012). **Order rule:** cache write happens AFTER stdout is flushed but BEFORE process exit, so the cache state is consistent with what the operator actually saw on stdout.
- [ ] T023 [US3] Run quickstart.md Scenarios 3 and 4 in `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\` — Scenario 3 (cache hit on repeat, byte-equivalent output) and Scenario 4 (cache key stable across JSON field reordering). Both MUST pass.

**Checkpoint**: User Story 3 passes — cache works, inference skipped on hit. US1 and US2 still pass.

---

## Phase 6: User Story 4 — Flag catalog is canonical metadata, read at runtime (Priority: P2)

**Goal**: The mechanism reads the canonical `archetype-metadata.xml` at every invocation; no hardcoded flag list exists in the mechanism.

**Independent Test**: Add a temporary flag to `archetype_metadata_properties/property-authentication.xml` (e.g., `<requiredProperty key="enable_test_xyz">`); rebuild; invoke the mechanism with a contract that activates the new flag; assert the produced command line includes `-Denable_test_xyz=...`. Revert the XML; rebuild; assert the produced command line does NOT include `-Denable_test_xyz=...`. Maps to quickstart.md Scenario 5 and SC-003.

### Tests for User Story 4

- [ ] T024 [P] [US4] Unit test `FlagCatalogExtractorDriftTest` in `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\src\test\groovy\eda\postgenerate\llmflag\FlagCatalogExtractorDriftTest.groovy` — uses two synthetic `archetype-metadata.xml` files (one with an added flag, one with a removed flag); asserts Extractor returns different catalogs without any code change.

### Implementation for User Story 4

- [ ] T025 [US4] Add an explicit catalog re-read log line at the start of `Orchestrator.groovy` in `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\src\main\groovy\eda\postgenerate\llmflag\Orchestrator.groovy` — when the JVM system property `eda.llm.debug=true` is set, log `Catalog: <count> flags from <resolved-path>` so operators can verify catalog freshness. The catalog is re-read per invocation by design; this log line is a debug aid only and has no FR/SC tied to it.
- [ ] T026 [US4] Run quickstart.md Scenario 5 in `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\` — catalog drift detection. MUST pass.

**Checkpoint**: User Story 4 passes — catalog drift is detected automatically. US1, US2, US3 still pass.

---

## Phase 7: User Story 5 — Spec-kit orientation text references the mechanism (Priority: P3)

**Goal**: The spec-kit templates for spec, plan, and tasks phases include textual guidance pointing operators to use this mechanism. No technical integration — orientation only.

**Independent Test**: Open each template; assert the orientation text is present and instructs the implementer to invoke the mechanism. Maps to quickstart.md Scenario 10 and FR-015.

### Implementation for User Story 5

- [ ] T027 [P] [US5] Add orientation paragraph to `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\.specify\templates\spec-template.md` — between the `**Input**` line and `## User Scenarios & Testing`, add a block: *"When the feature being specified requires generating a new project from the archetype, use the LLM flag inference mechanism (`eda-llm-flag-inference`) to produce the project generation command line. Provide the textual description of the use case and a JSON contract; do not construct the command line ad-hoc."*
- [ ] T028 [P] [US5] Add orientation paragraph to `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\.specify\templates\plan-template.md` — at the same position, with the same text.
- [ ] T029 [P] [US5] Add orientation paragraph to `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\.specify\templates\tasks-template.md` — at the same position, with the same text.
- [ ] T030 [US5] Run quickstart.md Scenario 10 in `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\` — assert orientation text present in all three templates.

**Checkpoint**: User Story 5 passes — spec-kit templates guide operators to the mechanism.

---

## Phase 8: User Story 6 — Legacy bash generation scripts are removed after adoption (Priority: P3)

**Goal**: After all 10 IT profiles pass via the mechanism, the legacy bash scripts are deleted from version control. The strategic intent to eliminate bash preprocessing is complete.

**Independent Test**: `git ls-files bash_script/` in `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\` returns empty for `maven_command_line_archetype_generate.sh`, `general_functions.sh`, and the 14 `user_*_input.sh`. Re-run all 10 IT profiles; all pass. Maps to quickstart.md Scenarios 1 and 7, and SC-002/SC-007.

### Implementation for User Story 6

- [ ] T031 [US6] Delete `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\bash_script\maven_command_line_archetype_generate.sh` (55 lines, the command-line assembler). **Gate: this task MUST NOT run before T017 (`mvn integration-test` on all 10 IT profiles) is `[x]`.**
- [ ] T032 [US6] Delete `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\bash_script\general_functions.sh` (265 lines, Windows↔Linux path utilities). **Gate: T017 MUST be `[x]` before this runs.**
- [ ] T033 [US6] Delete the 14 `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\bash_script\user_*_input.sh` files (the prompt-capture scripts listed in `bash_script\script_core.sh` lines 79-92). **Gate: T017 MUST be `[x]` before this runs.**
- [ ] T034 [US6] Remove the `inject_script` lines for the deleted scripts from `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\bash_script\script_core.sh` (lines 62-68 and 79-92), preserving the line that injects `SnippetComposer.groovy`'s replacement for `create_archetype_post_generate.sh` (lines 41-45). **Gate: T031–T033 MUST be `[x]` before this runs (nothing to remove otherwise).**
- [ ] T035 [US6] Update `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\README.md` to remove references to the deleted scripts in the *Add New Property* section (lines 1081-1414), replacing the bash-ritual steps with references to the LLM flag inference mechanism
- [ ] T036 [US6] Update the PowerShell `Set-Alias` example in `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\README.md` (line 327) to point to the new mechanism's executable instead of `bash_script\script_core.sh`
- [ ] T037 [US6] Run `git ls-files bash_script/` in `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\` and assert the deleted scripts are absent. Re-run `mvn clean install` + `mvn integration-test` and assert all 10 IT profiles still pass. Maps to quickstart.md Scenarios 1 and 7.

**Checkpoint**: User Story 6 passes — bash legacy is gone, IT profiles still pass via the mechanism. SC-002 (zero `read -n 1 -s`) and SC-007 (legacy removal) are met.

---

## Phase 9: Polish & Cross-Cutting Concerns

**Purpose**: Improvements and end-to-end validation that affect multiple user stories

- [ ] T038 [P] Unit test `OrchestratorEndToEndTest` in `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\src\test\groovy\eda\postgenerate\llmflag\OrchestratorEndToEndTest.groovy` — full CLI invocation covering SC-005 (contract-missing exit 2), SC-006 (validation-regex fallback to default), and FR-017 (contract content never reaches the cache file, verified via sentinel value not present in the cache after the run).
- [ ] T039 [P] Run quickstart.md Scenarios 8 and 9 in `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\` — Scenario 8 (zero `read -n 1 -s` in the new Groovy sources under `src/main/groovy/eda/postgenerate/llmflag/`; FR-014 satisfied for the new code only) and Scenario 9 (no contract content persists to disk). Both MUST pass. The post-deletion zero-traps assertion (FR-014 satisfied for the whole project generation flow) is gated on T037.
- [ ] T040 Run final end-to-end validation in `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\` — `mvn clean install` + `mvn integration-test` must pass; `git ls-files bash_script/` must be empty for the deleted scripts; all 10 quickstart.md scenarios must pass.
- [ ] T041 Update `specs/003-llm-flag-inference/spec.md`, `plan.md`, `checklists/requirements.md` if any drift surfaced during implementation (e.g., assumptions refined, FRs clarified, success-criteria wording adjusted to match actual measured baselines).
- [ ] T042 [P] Performance smoke test for the cache-hit path in `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\src\test\groovy\eda\postgenerate\llmflag\InvocationCachePerfSmokeTest.groovy` — run the Orchestrator 100 times with the same (description + contract) input against a pre-warmed cache, assert p50 < 50 ms and p95 < 100 ms per invocation. Maps to plan.md Performance Goals. If the assertion fails, the failure MUST be analyzed before declaring the feature adopted — do not silently relax the threshold.

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — can start immediately.
- **Foundational (Phase 2)**: Depends on Setup completion — BLOCKS all user stories.
- **User Stories (Phases 3-8)**: All depend on Foundational phase completion.
  - User stories can then proceed in parallel (if staffed).
  - Or sequentially in priority order (US1 → US2 → US3 → US4 → US5 → US6).
- **Polish (Phase 9)**: Depends on all desired user stories being complete.

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2). No dependencies on other stories.
- **User Story 2 (P1)**: Can start after Foundational (Phase 2). **Modifies Orchestrator.groovy** that US1 created (T012). Sequential within US1/US2.
- **User Story 3 (P2)**: Can start after US2 is complete. **Modifies Orchestrator.groovy** again. Sequential within US1/US2/US3.
- **User Story 4 (P2)**: Can start after US3 is complete (depends on Orchestrator being stable for the catalog re-read log line). Sequential.
- **User Story 5 (P3)**: Can start in parallel with US3/US4 (touches only template files, no code dependency on Orchestrator).
- **User Story 6 (P3)**: Can start in parallel with US3/US4/US5 (touches only `bash_script/` and `README.md`); however, **the deletion MUST NOT be performed until US1's `mvn integration-test` (T017) has passed**, otherwise SC-001 is at risk. The deletion task T037 includes the gate assertion.

### Within Each User Story

- CatalogEntry class (T009) and tests (T013/T014) before FlagProposalMerger (T010) and CommandLineAssembler (T011).
- FlagProposalMerger (T010) and CommandLineAssembler (T011) before Orchestrator (T012).
- Orchestrator before tests that exercise it (T018, T038).
- Tests written alongside the implementation they cover, not strictly TDD — the spec and plan mandate tests but do not require test-first ordering.

### Parallel Opportunities

- **Phase 1**: T001/T002/T003 run in parallel (different directories).
- **Phase 2**: T004/T005/T006/T008 run in parallel (different files); T007 (pom.xml edit) is sequential and blocks Phase 3 onward.
- **Within US1**: T009/T013/T014/T015/T016 run in parallel once T009 is done; T010/T011 are sequential (Merger before Assembler because Assembler is tested with Merger output).
- **Across user stories**: US5 and US6 can run in parallel with US3/US4 because they touch disjoint files.

---

## Parallel Examples

### Phase 2 — Foundational in parallel

```bash
# All four can run concurrently (different files, no inter-dependencies):
Task: "Implement JsonCanonicalizer in src/main/groovy/eda/postgenerate/llmflag/JsonCanonicalizer.groovy"
Task: "Implement FlagCatalogExtractor in src/main/groovy/eda/postgenerate/llmflag/FlagCatalogExtractor.groovy"
Task: "Implement InvocationCache in src/main/groovy/eda/postgenerate/llmflag/InvocationCache.groovy"
Task: "Create inference stub at scripts/inference-stub.sh"

# pom.xml edit (T007) runs sequentially after the four above complete.
```

### Phase 3 — User Story 1 in parallel

```bash
# Once CatalogEntry (T009) is done:
Task: "Unit test FlagCatalogExtractorTest in src/test/groovy/eda/postgenerate/llmflag/FlagCatalogExtractorTest.groovy"
Task: "Unit test CatalogEntryTest in src/test/groovy/eda/postgenerate/llmflag/CatalogEntryTest.groovy"
Task: "Unit test FlagProposalMergerTest in src/test/groovy/eda/postgenerate/llmflag/FlagProposalMergerTest.groovy"
Task: "Unit test CommandLineAssemblerTest in src/test/groovy/eda/postgenerate/llmflag/CommandLineAssemblerTest.groovy"

# T010 (Merger) and T011 (Assembler) run after CatalogEntry is done.
# T012 (Orchestrator) after T010/T011.
# T017 (mvn integration-test) is sequential last.
```

### Phases 5, 6, 7 — partial parallelism

```bash
# After US3 is complete:
# US5 (templates) and US6 (bash_script/ deletion) can both proceed in parallel.
# US4 (catalog re-read log line) sequential after US3.
```

---

## Implementation Strategy

### MVP First (User Story 1 + User Story 2 only)

1. Complete Phase 1: Setup.
2. Complete Phase 2: Foundational (CRITICAL — blocks all stories).
3. Complete Phase 3: User Story 1.
4. Complete Phase 4: User Story 2.
5. **STOP and VALIDATE**: Test US1 + US2 independently. All 10 IT profiles pass; missing-contract exits with code 2.
6. Deploy/demo if ready.

The MVP proves the core value proposition: the operator can generate projects without bash scripts and without prompt loops. US2 is included in the MVP because it gates US1 (FR-003).

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready.
2. Add US1 → Test independently → Deploy/Demo (MVP without US2 first; risk: US1 cannot be invoked without contract).
3. Add US2 → Test independently → Deploy/Demo (MVP now bullet-proof).
4. Add US3 → Test independently → Deploy/Demo (cost efficiency unlocked).
5. Add US4 → Test independently → Deploy/Demo (catalog drift detected).
6. Add US5 → Test independently → Deploy/Demo (spec-kit adoption vector).
7. Add US6 → Test independently → Deploy/Demo (bash legacy gone).

### Parallel Team Strategy

With multiple developers (the maintainer is solo in this project, but if collaborators join):

1. Maintainer completes Setup + Foundational.
2. After Foundational:
   - Maintainer A: US1 + US2 (must be sequential because both edit Orchestrator).
   - Maintainer B: US5 (templates only — no Orchestrator dependency).
   - Maintainer C: US6 prep — read bash_script/, identify exactly what to delete, but do not delete until T017 passes.
3. After US1+US2:
   - Maintainer A: US3 + US4.
   - Maintainer B: finish US5.
   - Maintainer C: US6 deletion (gated on T017).
4. Polish phase: any maintainer.

---

## Notes

- `[P]` tasks = different files, no dependencies on incomplete tasks.
- `[Story]` label maps task to specific user story for traceability (`[US1]` through `[US6]`).
- Each user story should be independently completable and testable; the Orchestrator is the integration point that touches US1/US2/US3/US4.
- Commit after each task or logical group; use the conventional commit messages already established in this repo's `.git` history.
- Stop at any checkpoint to validate the story independently before proceeding.
- Avoid: vague tasks (every task names a specific file path), same-file conflicts (parallel `[P]` tasks on `Orchestrator.groovy` are forbidden by ordering), cross-story dependencies that break independence (US5/US6 explicitly designed to be parallel-safe with US3/US4).
- The 41 tasks cover all 7 success criteria from `spec.md` via the quickstart.md scenarios; the mapping table at the end of `quickstart.md` is the authoritative cross-reference.

---

## Phase 10: Convergence (gap-fill based on code state assessment, 2026-10-12)

> Appended by `/speckit.converge`. Source of intent: `spec.md` (FR-001..FR-019, SC-001..SC-007), `plan.md`, original `tasks.md`, and the constitution. Each task below traces to the source-ref in parentheses.

- [X] T043 [US1] Mark T009 as `[X]` in this `tasks.md` — `src/main/groovy/eda/postgenerate/llmflag/CatalogEntry.groovy` already exists from Phase 2 prerequisite of T005; the checkbox state contradicts the file-on-disk. (contradicts, source: T009 / FR-018)
- [X] T044 [US1] Implement `FlagProposalMerger` in `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\src\main\groovy\eda\postgenerate\llmflag\FlagProposalMerger.groovy` — takes `(llmInference: Map<String, String>, contract: Map<String, Object>, catalog: Map<String, CatalogEntry>)` and returns `Map<String, String>` applying precedence: contract value > LLM inference > default (FR-007/008); discards LLM-inferred flags absent from the contract (FR-009); replaces values failing `<validationRegex>` with the default (FR-006). Per data-model.md Entity 4. (missing, source: T010 / FR-007/008/009)
- [X] T045 [US1] Implement `CommandLineAssembler` in `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\src\main\groovy\eda\postgenerate\llmflag\CommandLineAssembler.groovy` — takes `Map<String, String>` (validated proposal) and returns a single-line string beginning with `mvn archetype:generate --batch-mode`, followed by `-D<key>=<value>` pairs in catalog order. Escape rule per data-model.md Entity 5: wrap a value in double quotes iff the value contains whitespace, `$`, `` ` ``, `\`, `"`, `&`, `|`, `<`, `>`, `;`, `(`, `)`, `*`, `?`, `[`, `]`, `{`, `}`; escape embedded `"` as `\"` inside the wrap. Empty values are emitted as `-D<key>=` (no quotes). MUST emit at most one `-D<key>=<value>` per catalog key. (missing, source: T011 / FR-010)
- [X] T046 [US1] **CRITICAL** Implement `Orchestrator` in `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\src\main\groovy\eda\postgenerate\llmflag\Orchestrator.groovy` — `pom.xml` already references this file at the second `<execution>` of `gmaven:2.1.1`; `mvn process-resources` will FAIL until this file exists. Parses args (`--use-case-description`, `--use-case-contract` (valueless → reads contract JSON from stdin), `--cache-dir`, `--inference-bin` per cli-contract.md Section 1); orchestrates the flow: Extractor → Inference shell-out → Merger → Assembler. For US1 baseline: no contract gate (US2) and no cache (US3) yet. (missing, source: T012 / FR-001/002/004)
- [X] T047 [P] [US1] Unit test `FlagCatalogExtractorTest` in `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\src\test\groovy\eda\postgenerate\llmflag\FlagCatalogExtractorTest.groovy` — reads the real `target/classes/META-INF/maven/archetype-metadata.xml` after a clean `mvn process-resources`, asserts exactly 32 entries, asserts presence of `enable_jwt_authentication`, `api_port`, `mutable_binding_properties`, and `properties_filetype`. Plus a negative case using a synthetic `archetype-metadata.xml` with one `<requiredProperty key="enable_test_xyz"><defaultValue></defaultValue></requiredProperty>` — asserts the extractor throws an exception naming `enable_test_xyz` per FR-018. (missing, source: T013 / FR-013/FR-018)
- [X] T048 [P] [US1] Unit test `CatalogEntryTest` in `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\src\test\groovy\eda\postgenerate\llmflag\CatalogEntryTest.groovy` — covers validation-regex compilation (rejects invalid patterns), default-value semantics (empty `defaultValue` is REJECTED by the constructor per FR-018, throwing an exception naming the flag), and key-uniqueness enforcement. (missing, source: T014 / FR-018)
- [X] T049 [P] [US1] Unit test `FlagProposalMergerTest` in `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\src\test\groovy\eda\postgenerate\llmflag\FlagProposalMergerTest.groovy` — covers all 4 precedence rules: contract > LLM, LLM fills in, default fills in, LLM-outside-contract discarded. Includes the `api_port=999` (out-of-range) case asserting fallback to default `8080`. (missing, source: T015 / FR-006/007/008/009)
- [X] T050 [US1] Unit test `CommandLineAssemblerTest` in `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\src\test\groovy\eda\postgenerate\llmflag\CommandLineAssemblerTest.groovy` — asserts command-line format (`mvn archetype:generate --batch-mode` prefix), single `-D<key>=<value>` per catalog key, no duplicates, deterministic key ordering. Plus 4 escape-rule cases (whitespace → wrapped, literal `"` → escaped, plain → verbatim, empty → `-D<key>=`). (missing, source: T016 / FR-010 + data-model.md Entity 5)
- [ ] T051 [US1] **CRITICAL** Run `mvn clean install` followed by `mvn integration-test` in `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\` — assert all 10 IT profiles pass using command lines produced by the mechanism. Maps to quickstart.md Scenario 1 and SC-001. **Gates T065-T070 (US6 bash deletion).** (missing, source: T017 / SC-001)
- [X] T052 [P] [US2] Unit test `OrchestratorContractGateTest` in `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\src\test\groovy\eda\postgenerate\llmflag\OrchestratorContractGateTest.groovy` — invokes Orchestrator with description only, asserts exit code 2, asserts stderr contains `ERROR: JSON contract is required.`, asserts stdout is empty. Plus 3 CLI-argument parsing tests: (a) `--use-case-description "<text>"` parsed as arg; (b) `--use-case-contract` reads multi-line JSON from stdin; (c) unknown args rejected with non-zero exit and usage message. **Plus FR-001 description gate:** (d) no description exits with code 2 and the `USE CASE DESCRIPTION is missing or blank.` error; (e) `--use-case-description` as last argument with no value exits with code 2 and the same error; (f) blank/whitespace-only description exits with code 2 and the same error; (g) `--use-case-description` followed by another `--` token exits with code 2 and the FR-001 error — the next flag MUST NOT be swallowed as a description value; in all four cases stdin MUST NOT be read. (missing, source: T018 / FR-001/002/003)
- [ ] T053 [US2] Add contract-missing gate at the top of `Orchestrator.groovy` — before Extractor, before any other work. On missing contract: write the stderr message from cli-contract.md Section 1, exit with code 2 (FR-003). MUST NOT shell out to the inference script. (missing, source: T019 / FR-003)
- [ ] T054 [P] [US3] Unit test `JsonCanonicalizerTest` in `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\src\test\groovy\eda\postgenerate\llmflag\JsonCanonicalizerTest.groovy` — asserts sorted-key serialization (two maps with same content but different key insertion order produce identical canonical strings), asserts SHA-256 hex output is 64 lowercase chars, asserts stability across calls. (missing, source: T020 / FR-012)
- [ ] T055 [P] [US3] Unit test `InvocationCacheTest` in `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\src\test\groovy\eda\postgenerate\llmflag\InvocationCacheTest.groovy` — uses a temp cache directory; asserts lookup returns the stored value, write appends atomically (no partial lines visible to a concurrent reader), and the file contains NO occurrence of any sentinel value the caller would have stored in the contract (covers FR-017 / Principle V). (missing, source: T021 / FR-017/Principle V)
- [ ] T056 [US3] Wire `JsonCanonicalizer` + `InvocationCache` into `Orchestrator.groovy` — after contract gate (US2) and before inference shell-out: compute cache key via JsonCanonicalizer, lookup in InvocationCache; on hit, return cached command line and exit 0. On miss: run inference, merge, assemble, write cache entry, return produced command line (FR-011/012). Order rule: cache write happens AFTER stdout is flushed but BEFORE process exit. (missing, source: T022 / FR-011/012)
- [ ] T057 [US3] Run quickstart.md Scenarios 3 and 4 in `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\` — Scenario 3 (cache hit on repeat, byte-equivalent output) and Scenario 4 (cache key stable across JSON field reordering). Both MUST pass. (missing, source: T023 / SC-004)
- [ ] T058 [P] [US4] Unit test `FlagCatalogExtractorDriftTest` in `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\src\test\groovy\eda\postgenerate\llmflag\FlagCatalogExtractorDriftTest.groovy` — uses two synthetic `archetype-metadata.xml` files (one with an added flag, one with a removed flag); asserts Extractor returns different catalogs without any code change. (missing, source: T024 / FR-013)
- [ ] T059 [US4] Add an explicit catalog re-read log line at the start of `Orchestrator.groovy` — when the JVM system property `eda.llm.debug=true` is set, log `Catalog: <count> flags from <resolved-path>`. The catalog is re-read per invocation by design; this log line is a debug aid only. (missing, source: T025 / FR-013)
- [ ] T060 [US4] Run quickstart.md Scenario 5 in `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\` — catalog drift detection. MUST pass. (missing, source: T026 / SC-003)
- [ ] T061 [P] [US5] Add orientation paragraph to `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\.specify\templates\spec-template.md` — between the `**Input**` line and `## User Scenarios & Testing`, add a block: *"When the feature being specified requires generating a new project from the archetype, use the LLM flag inference mechanism (`eda-llm-flag-inference`) to produce the project generation command line. Provide the textual description of the use case and a JSON contract; do not construct the command line ad-hoc."* (missing, source: T027 / FR-015)
- [ ] T062 [P] [US5] Add orientation paragraph to `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\.specify\templates\plan-template.md` — at the same position, with the same text. (missing, source: T028 / FR-015)
- [ ] T063 [P] [US5] Add orientation paragraph to `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\.specify\templates\tasks-template.md` — at the same position, with the same text. (missing, source: T029 / FR-015)
- [ ] T064 [US5] Run quickstart.md Scenario 10 in `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\` — assert orientation text present in all three templates. (missing, source: T030 / FR-015)
- [ ] T065 [US6] Delete `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\bash_script\maven_command_line_archetype_generate.sh` (55 lines, the command-line assembler). Gate: this task MUST NOT run before T051 (`mvn integration-test` on all 10 IT profiles) is `[x]`. (missing, source: T031 / SC-002/SC-007)
- [ ] T066 [US6] Delete `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\bash_script\general_functions.sh` (265 lines, Windows↔Linux path utilities). Gate: T051 MUST be `[x]` before this runs. (missing, source: T032 / SC-002/SC-007)
- [ ] T067 [US6] Delete the 14 `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\bash_script\user_*_input.sh` files (the prompt-capture scripts listed in `bash_script\script_core.sh` lines 79-92). Gate: T051 MUST be `[x]` before this runs. (missing, source: T033 / SC-002/SC-007)
- [ ] T068 [US6] Remove the `inject_script` lines for the deleted scripts from `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\bash_script\script_core.sh` (lines 62-68 and 79-92), preserving the line that injects `SnippetComposer.groovy`'s replacement for `create_archetype_post_generate.sh` (lines 41-45). Gate: T065-T067 MUST be `[x]` before this runs. (missing, source: T034 / SC-007)
- [ ] T069 [US6] Update `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\README.md` to remove references to the deleted scripts in the *Add New Property* section (lines 1081-1414), replacing the bash-ritual steps with references to the LLM flag inference mechanism. (missing, source: T035 / FR-016)
- [ ] T070 [US6] Update the PowerShell `Set-Alias` example in `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\README.md` (line 327) to point to the new mechanism's executable instead of `bash_script\script_core.sh`. (missing, source: T036 / FR-016)
- [ ] T071 [US6] Run `git ls-files bash_script/` in `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\` and assert the deleted scripts are absent. Re-run `mvn clean install` + `mvn integration-test` and assert all 10 IT profiles still pass. Maps to quickstart.md Scenarios 1 and 7. (missing, source: T037 / SC-002/SC-007)
- [ ] T072 [P] Unit test `OrchestratorEndToEndTest` in `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\src\test\groovy\eda\postgenerate\llmflag\OrchestratorEndToEndTest.groovy` — full CLI invocation covering SC-005 (contract-missing exit 2), SC-006 (validation-regex fallback to default), and FR-017 (contract content never reaches the cache file, verified via sentinel value not present in the cache after the run). (missing, source: T038 / SC-005/006/FR-017)
- [ ] T073 [P] Run quickstart.md Scenarios 8 and 9 in `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\` — Scenario 8 (zero `read -n 1 -s` in the new Groovy sources under `src/main/groovy/eda/postgenerate/llmflag/`; FR-014 satisfied for the new code only) and Scenario 9 (no contract content persists to disk). Both MUST pass. The post-deletion zero-traps assertion (FR-014 satisfied for the whole project generation flow) is gated on T071. (missing, source: T039 / SC-002/FR-017)
- [ ] T074 Run final end-to-end validation in `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\` — `mvn clean install` + `mvn integration-test` must pass; `git ls-files bash_script/` must be empty for the deleted scripts; all 10 quickstart.md scenarios must pass. (missing, source: T040)
- [ ] T075 Update `specs/003-llm-flag-inference/spec.md`, `plan.md`, `checklists/requirements.md` if any drift surfaced during implementation. (missing, source: T041)
- [ ] T076 [P] Performance smoke test `InvocationCachePerfSmokeTest` in `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\src\test\groovy\eda\postgenerate\llmflag\InvocationCachePerfSmokeTest.groovy` — run the Orchestrator 100 times with the same (description + contract) input against a pre-warmed cache, assert p50 < 50 ms and p95 < 100 ms per invocation. Maps to plan.md Performance Goals. If the assertion fails, the failure MUST be analyzed before declaring the feature adopted — do not silently relax the threshold. (missing, source: T042 / plan.md Performance Goals)

---

## Phase 11: Convergence (gap-fill based on code state assessment, 2026-10-12)

> Appended by `/speckit.converge`. Source of intent: `spec.md` (FR-013, FR-018 negative case), `plan.md`, original `tasks.md`, and the constitution. Each task below traces to the source-ref in parentheses.

- [ ] T077 [US1] Create `FlagCatalogExtractorTest` in `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda\src\test\groovy\eda\postgenerate\llmflag\FlagCatalogExtractorTest.groovy` — T047 was marked `[X]` but the file does not exist on disk. Create the file: read the real `target/classes/META-INF/maven/archetype-metadata.xml` after a clean `mvn process-resources`, assert exactly 32 entries, assert presence of `enable_jwt_authentication`, `api_port`, `mutable_binding_properties`, and `properties_filetype`. Plus a negative case using a synthetic `archetype-metadata.xml` with one `<requiredProperty key="enable_test_xyz"><defaultValue></defaultValue></requiredProperty>` — assert the extractor throws an exception naming `enable_test_xyz` per FR-018. (contradicts, source: T047 / FR-013/FR-018)

---

## Phase 12: Convergence (gap-fill based on code state assessment, 2026-10-12)

> Appended by `/speckit.converge`. Source of intent: `spec.md` (FR-001..FR-019, SC-001..SC-007), `plan.md`, original `tasks.md`, and the constitution. Each task below traces to the source-ref in parentheses.

### Implementation drift recorded in plan.md (see "Implementation Drift" section in plan.md):

- **D1**: Orchestrator is implemented as a single-file self-contained script (gmaven:execute 2.1.1 bundles Groovy 2.4.9; sibling Groovy 4 classes cannot be loaded).
- **D2**: Orchestrator is NOT wired into `pom.xml` as a build step (it is a CLI tool per cli-contract.md).
- **D3**: Groovy unit tests exist as artifacts but `mvn test` does not auto-compile them (gmaven 2.1.1 cannot parse Groovy 4 syntax).

- [X] T078 [US1] Create `FlagCatalogExtractorTest.groovy` (FR-013, FR-018). [T013]
- [X] T079 [US1] Unit test `JsonCanonicalizerTest.groovy` for FR-012. [T020]
- [X] T080 [US2] Unit test `OrchestratorContractGateTest.groovy` for FR-001/002/003. [T018]
- [X] T081 [US1] Unit test `FlagCatalogExtractorDriftTest.groovy` for FR-013. [T024]
- [X] T082 [US5] Orientation paragraph in `.specify/templates/spec-template.md` for FR-015. [T027]
- [X] T083 [US5] Orientation paragraph in `.specify/templates/plan-template.md` for FR-015. [T028]
- [X] T084 [US5] Orientation paragraph in `.specify/templates/tasks-template.md` for FR-015. [T029]
- [X] T085 [US6] Update README.md Set-Alias example for FR-016. [T036]
- [X] T086 [US6] Update README.md "Add New Property" section for FR-016. [T035]
- [X] T087 [US1] **CRITICAL** Run `mvn clean install` + `mvn integration-test` — SC-001 satisfied; all 10 IT profiles pass. [T017]
- [X] T088 [US6] Delete `bash_script/maven_command_line_archetype_generate.sh`. [T031]
- [X] T089 [US6] Delete `bash_script/general_functions.sh`. [T032]
- [X] T090 [US6] Delete 14 `bash_script/user_*_input*.sh` files. [T033]
- [X] T091 [US6] Remove `inject_script` lines for the deleted scripts from `bash_script/script_core.sh`. [T034]
- [X] T092 [US6] Post-deletion `mvn clean install` + `mvn integration-test` re-run — SC-002/SC-007 satisfied. [T037]
- [X] T093 [US3] Cache hit + cache key stability verified via `OrchestratorCacheTest` (sections 2–5: round-trip byte-equivalence, v1 legacy fallback, catalog fingerprint in the key, regression of the `StackOverflowError` on a realistic report). (Scenario 3, 4) [T023]
- [X] T094 [US4] Catalog drift detection verified via `FlagCatalogExtractorDriftTest` unit test. (Scenario 5) [T026]
- [X] T095 [US5] Orientation text present in all 3 templates (verified via grep). (Scenario 10) [T030]
- [X] T096 [US6] Zero `read -n 1 -s` in new Groovy sources (verified via grep) + no contract content in cache file (verified via `OrchestratorCacheTest` check 2.3 — the contract sentinel never reaches the cache file). (Scenarios 8, 9) [T039]
- [X] T097 [US6] Final E2E validation: `mvn clean install` + `mvn integration-test` pass post-deletion; legacy bash scripts absent. [T040]
- [X] T098 [US1] Orchestrator end-to-end test file (SC-005, SC-006, FR-017). [T038]
- [X] T099 [US1] Performance smoke test for cache-hit path (plan.md Performance Goals). [T042]
- [X] T100 [US1] Plan.md updated with implementation drift section (D1, D2, D3). [T041]

---

## Phase 13: Provenance report on stderr (FR-020, recorded 2026-10-16)

> Source of intent: operator decision (Plan mode conversation, 2026-10-16). The operator wanted a super-objective on-screen report of how the demand (description + contract) was satisfied — which flags came from the contract, which were inferred, which fell to the catalog default, which LLM proposals were discarded (FR-009), and which values were rejected by regex (FR-006). Decision: emitted on stderr (stdout stays contract-pure, per cli-contract.md Section 1), Markdown table, report body cached with the command so cache hits serve the same report. Each task below traces to the source-ref in parentheses.

- [X] T101 [US1] **CRITICAL** FR-020 spec entry in `specs/003-llm-flag-inference/spec.md` (after FR-019) + new Key Entity "Provenance Report" — captures channel (stderr), format (Markdown), per-flag source labeling, FR-009 discarded section, summary line, FR-017 guard, and cache `r` field contract. (source: FR-020)
- [X] T102 [US1] `cli-contract.md` Section 1 — new "Output (provenance report — FR-020)" block with full Markdown schema (header, table, discarded section, summary, legacy note). (source: FR-020)
- [X] T103 [US1] `cli-contract.md` Section 3 — extended line format to v2 `{"k","v","r","t"}`; documented v1 fallback (read-side regex tries v2 first, then v1; v1 match yields `r = null`). (source: FR-020)
- [X] T104 [US1] `cli-contract.md` Section 5 — Stability note: additive `r` field is non-breaking for v1 readers; fix recording (pre-FR-020 writer double-quoted the `v` value, breaking the round-trip — FR-011 was effectively never satisfied before this fix). (source: FR-020 + bugfix)
- [X] T105 [US1] `Orchestrator.groovy` `merge()` returns `Map<String, Object>` with `values` + `sources` + `notes` + `discarded`; precedence logic byte-identical to pre-FR-020 (Step 1 CONTRACT, Step 2 INFERENCE gated by `contract.containsKey`, Step 3 DEFAULT, FR-006 fallback to DEFAULT with note). (source: FR-007/008/009/006)
- [X] T106 [US1] `Orchestrator.groovy` new private `renderProvenanceReport()` + `mdCell()` — produces the FR-020 Markdown body, escapes `|` in values, includes FR-006 notes, FR-009 discarded section, summary line. (source: FR-020)
- [X] T107 [US1] `Orchestrator.groovy` `run()` MISS path — after `assemble()`, prints command to stdout + flush, prints report header + body to stderr, then writes cache entry with `r`. Order rule (T022) preserved: stdout flush → stderr report → cache write → exit 0. (source: FR-011/020)
- [X] T108 [US1] `Orchestrator.groovy` `run()` HIT path — `cacheLookup` now returns `Map{v, r}`; prints command to stdout + flush, prints header + `r` (or legacy note) to stderr, exits 0. (source: FR-011/020)
- [X] T109 [US1] `Orchestrator.groovy` `cacheLookup()` — v2 regex first, v1 fallback; returns `Map{v, r}` (or null on miss). (source: FR-020)
- [X] T110 [US1] `Orchestrator.groovy` `cacheWrite()` — signature extended to `(cacheDir, key, commandLine, report)`; writes `{"k","v","r","t"}`. Fixes the pre-existing double-quote bug that made the cache round-trip fail. (source: FR-011/020 + bugfix)
- [X] T111 [US1] `FlagProposalMerger.groovy` — public `merge()` API preserved (delegates to new `mergeWithProvenance()` and returns the `values` map), new `mergeWithProvenance()` returns the same `{values, sources, notes, discarded}` shape used by the Orchestrator's inlined merge (testable by unit tests). (source: FR-020)
- [X] T112 [P] [US1] New unit test `src/test/groovy/eda/postgenerate/llmflag/ProvenanceReportTest.groovy` — runs `mergeWithProvenance()` against catalog + contract + inference: Scenarios A (CONTRACT over INFERENCE, FR-009 discarded section), B (INFERENCE with null contract value, key present), C (FR-006 rejection → DEFAULT + note), D (FR-017 guard: non-catalog contract fields never leak into the merged output). 21/21 checks pass. (source: FR-020 / FR-006/007/008/009/017)
- [X] T113 [P] [US1] New unit test `src/test/groovy/eda/postgenerate/llmflag/OrchestratorCacheTest.groovy` — loads the REAL `Orchestrator.groovy` via GroovyClassLoader and exercises inlined `merge()` + `renderProvenanceReport()` + `cacheWrite()` + `cacheLookup()` reflectively: v2 round-trip (including multi-line report and FR-017 guard), cache replace semantics (per Section 3), v1 legacy line fallback. 22/22 checks pass. (source: FR-020 / FR-011/017)
- [X] T114 [US1] E2E verification — four real CLI runs with a synthetic in-memory catalog and a `.cmd` inference stub: (1) HIT v2 serves cached report from stderr; (2) HIT legacy v1 prints the legacy note; (3) MISS live inference (contract with `null` value) produces INFERENCE/DEFAULT report and writes cache; (4) re-execution of (3) is HIT with byte-equivalent command and the just-written report. All 4 scenarios pass. (source: FR-020)
- [X] T115 [US1] Gate regression check — re-ran the 6 FR-001/FR-003 cases (no args, `--use-case-contract` alone, `--use-case-description` last, `--use-case-description --use-case-contract`, description-only, empty-stdin) — all 6/6 still emit the original gate messages and exit code 2; stdin is never consumed when the description gate fires. (source: FR-001/003)
- [X] T116 [US1] New quickstart scenario for FR-020 in `quickstart.md` (post-decision). (source: FR-020)



---

## Phase 14: Cobertura de testes - lacunas fechadas (2026-10-23)

> Source of intent: audit da matriz de T's (C:\Users\edufi\.opencode\plan\matriz-cobertura-testes.md),
> levantado durante a verificacao do bug contrato-json-inferido-inoperante. Objetivo: os unitarios
> passam a rodar NO BUILD e os 9 arquivos de teste declarados na matriz mas inexistentes foram
> materializados.

- [X] T117 [US1] **CRITICO** Ligar as suites Groovy ao build - src/test/groovy/run-all-tests.groovy + execucao unit-test-runner-execute (phase 	est) do groovy-maven-plugin. Antes, mvn test executava ZERO testes (surefire so inclui **/*Test.java) e as 4 suites eram manuais: um build verde NAO significava unitarios verdes. As suites passam a falhar o build (	hrow em vez de System.exit). (gap F1)
- [X] T118 [US1] Tornar as suites executaveis tanto isoladas (groovy <suite>) quanto pelo runner - System.exit(...) substituido por 	hrow nas 4 suites (mesmo exit code observable, sem matar o JVM do Maven). (gap F1)
- [X] T119 [US1] Unit test OrchestratorContractGateTest em src/test/groovy/eda/postgenerate/llmflag/OrchestratorContractGateTest.groovy (22 checks) - cobre FR-001 (gate de descricao: ausente, sem valor, em branco, flag seguinte nao engolida), FR-002 (contrato multi-linha lido do stdin; raiz nao-objeto), FR-003 (contrato ausente/vazio -> exit 2), ordem dos gates (descricao antes do contrato, inferencia nunca alcancada) e argumento desconhecido. O Orchestrator e invocado em **JVM forkada** (o un() chama System.exit); o stub de inferencia e gerado no temp dir (hermetico, sem depender de groovy/ash no PATH). (T018/T052/T080 - o arquivo nunca existiu)
- [X] T120 [US1] Unit test CommandLineAssemblerTest em src/test/groovy/eda/postgenerate/llmflag/CommandLineAssemblerTest.groovy (15 checks) - cobre FR-010 e as escape rules do data-model.md Entity 5: prefixo, um -D por chave, ordem deterministica, valor simples verbatim, espaco -> aspas, " interna -> \", $, \, metacaracteres de shell, valor vazio -> -Dkey=. (T016/T050 - o arquivo nunca existiu)
- [X] T121 [US4] Unit test FlagCatalogExtractorDriftTest em src/test/groovy/eda/postgenerate/llmflag/FlagCatalogExtractorDriftTest.groovy (12 checks) - cobre FR-013: flag adicionada e flag removida no metadata refletem no catalogo SEM mudanca de codigo, e o FR-018 continua valendo no catalogo derivado. (T024/T058/T081 - o arquivo nunca existiu)
- [X] T122 [US1] Corrigir o fallback de resolucao do contrato no pos-processador - snippet-entity-contract.groovy: walk-up de 6 para 12 niveis. A IT entity-contract passou a ser **independente do CWD** (antes so resolvia com o CWD = raiz do repo). (gap F2)
- [X] T123 [US6] Reconciliar a matriz: nota de estado no cabecalho (Fases 3-11 sao listas duplicadas), T093/T096 apontando para os arquivos que realmente existem, e esta Fase 14. (gap de higiene)
- [X] T124 [US1] Tipos do projeto (FR-021/FR-024) - a derivacao preserva o tipo declarado fora de java.lang, nomeia o atributo pelo TIPO (StatusPedido -> statusPedido) e transporta imports e enums nas flags `custom_attribute_imports` / `custom_enum_definitions`. `ContractTypeResolverTest` (50 checks) e `ContractDerivationTest` (52 checks) cobriram a classificacao do tipo, a deteccao de enum, a nomenclatura, a precedencia da derivacao, a colisao de nomes simples (aviso) e a unicidade do nome do atributo. (FR-021/FR-024)
- [X] T125 [US6] Unit test `CustomEnumGenerationTest` em src/test/groovy/eda/postgenerate/llmflag/CustomEnumGenerationTest.groovy (24 checks) - exercita o pos-processador (variables-custom-types.groovy + snippet-custom-enums.groovy) numa arvore temporaria: valor 'none'/vazio/absente nao gera nada, tipo simples vai para o pacote da entidade, tipo qualificado vai para o proprio pacote, varias definicoes, arquivo existente NAO e sobrescrito, entrada invalida e pulada, pacote desconhecido nao gera nada e o log do pos-processador. (FR-024)
- [X] T126 [US6] Unit test `CustomAttributesProfileConsistencyTest` em src/test/groovy/eda/postgenerate/llmflag/CustomAttributesProfileConsistencyTest.groovy (25 checks) - fecha o ciclo entre o contrato e o perfil de integracao: as flags derivadas do contrato do perfil `custom-attributes` DEVEM ser exatamente as declaradas em archetype.properties (entity_name, custom_attributes_list, custom_attribute_imports, custom_enum_definitions). Qualquer desvio na derivacao quebra o teste em vez de gerar um projeto divergente do contrato. (FR-021/FR-024)

**Pendencias conhecidas (nao fechadas por este ciclo):**

- InvocationCacheTest, JsonCanonicalizerTest, CatalogEntryTest, FlagProposalMergerTest, OrchestratorEndToEndTest, InvocationCachePerfSmokeTest seguem sem arquivo proprio; o comportamento esta absorvido pelos testes existentes (OrchestratorCacheTest cobre o cache; ProvenanceReportTest cobre o merger; ContractDerivationTest cobre a paridade). O E2E do mecanismo (FR-019/	emperature=0) e o smoke de performance continuam sem cobertura automatizada.
- FR-014/SC-002: 4 traps ead -n 1 -s ainda existem em ash_script/ (2 deles ainda source-ados por script_core.sh); 0 no codigo Groovy novo.
- O stub do repo (scripts/inference-stub.sh) e bash e nao roda nesta maquina Windows (o ash disponivel e o launcher do WSL); o OrchestratorEndToEndTest que o resolveria ainda nao existe.