# Tasks: Paradas Duras Claras, Completas e em Linguagem Simples

**Input**: Design documents from `/specs/007-hard-stop-contract/`

**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/`, `quickstart.md`

**Tests**: Required. FR-018, FR-019, FR-020 and SC-003/SC-006 explicitly require build-time contract suites; the tasks below include them.

**Organization**: Tasks are grouped by user story. US1 and US2 are P1; US3 and US4 are P2; US5 is P3.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: `[US1]`, `[US2]`, `[US3]`, `[US4]`, `[US5]`
- Every task includes an exact file path or a command with the file under validation

## Path Conventions

- Procedure: `src/main/resources/llm-maven-flags-inference/`
- Catalog: `src/main/resources/META-INF/maven/archetype_metadata_required_properties/`
- Tests: `src/test/archetype-post-generate/`
- Runner: `src/test/groovy/run-archetype-post-generate-tests.groovy`
- Feature docs: `specs/007-hard-stop-contract/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Confirm the baseline and create the two failing contract suites before touching the procedure.

- [ ] T001 Confirm the baseline in `src/main/resources/llm-maven-flags-inference/` and `src/main/resources/META-INF/maven/archetype_metadata_required_properties/`: 19 procedure files (13 Markdown, 6 JSON), 35 properties, 18 filesets and the 9 critical flags; record the counts.
- [ ] T002 [P] Create `src/test/archetype-post-generate/HardStopContractTest.groovy` with the initial checks for the 18 codes, registry, messages, 10 fields, dictionary, `Pare`/`PARADA DURA` sites and counts; the suite must fail before the procedure is updated.
- [ ] T003 [P] Create `src/test/archetype-post-generate/CatalogSyncContractTest.groovy` with the initial checks for the 3-column table, 9 defaults, 3 literal regexes, 6 descriptive formats, catalog description and provenance exception; the suite must fail before the procedure is updated.
- [ ] T004 Register `HardStopContractTest.groovy` and `CatalogSyncContractTest.groovy` in `src/test/groovy/run-archetype-post-generate-tests.groovy`.
- [ ] T005 Run `mvn -B test` and record the expected failures of `HardStopContractTest.groovy` and `CatalogSyncContractTest.groovy`; do not change the procedure in this task.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Put the canonical contract, dictionary, registry and 18 messages in the entry point. Every story depends on this.

**CRITICAL**: No user story work can begin until T006–T012 are complete.

- [ ] T006 Add `§0.3 Contrato de Parada Dura` to `src/main/resources/llm-maven-flags-inference/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md`: the 10 fields, the writing rules from `specs/007-hard-stop-contract/contracts/hard-stop-message.md`, the canonical template with `**Segurança:**` and the dictionary reference.
- [ ] T007 Add `§0.4 Dicionário de linguagem simples` to `prompt-00` with the 20 terms and translations from `specs/007-hard-stop-contract/spec.md` (FR-006/FR-030).
- [ ] T008 Add `§0.5 Registro de paradas` to `prompt-00` with the canonical table of 16 stops + 2 confirmations, their triggers and primary sites from `specs/007-hard-stop-contract/contracts/stop-registry.md`.
- [ ] T009 Add the 5 `STOP-IN-*` message blocks to `prompt-00` §0.5 using the template and the options/suggestions from `specs/007-hard-stop-contract/contracts/hard-stop-message.md` and `contracts/stop-registry.md`.
- [ ] T010 Add the 5 `STOP-ENV-*` message blocks to `prompt-00` §0.5 using the same template and the options/suggestions from the contracts.
- [ ] T011 Add the 4 `STOP-DER-*`, `STOP-EXEC-01`, `STOP-RULE-01` and the 2 `STOP-CONF-*` message blocks to `prompt-00` §0.5 using the same template and the options/suggestions from the contracts.
- [ ] T012 Run `mvn -B test` against `src/test/archetype-post-generate/HardStopContractTest.groovy` and fix registry/message gaps in `prompt-00`; checkpoint: 18 codes, 18 messages and 20 dictionary terms present.

**Checkpoint**: The entry point has the contract, dictionary and registry; user stories can start.

---

## Phase 3: User Story 1 — Orientação completa em qualquer parada dura (Priority: P1) — MVP

**Goal**: Every one of the 16 stops and 2 confirmations has a registered code at its site and a complete message that lets the operator answer the 5 orientation questions.

**Independent Test**: Provoke each stop and verify the code, the 10 fields, at least 2 options, the suggestion, the resume step and that no Maven command is emitted.

- [ ] T013 [US1] Update `prompt-00` Gates A–D, §6 and §8 to cite `STOP-IN-01` to `STOP-IN-05`, `STOP-ENV-01` to `STOP-ENV-04`, `STOP-DER-03` and `STOP-RULE-01`, and replace "Ao parar, informe apenas" with the full-message instruction.
- [ ] T014 [P] [US1] Add code references to `src/main/resources/llm-maven-flags-inference/01-entrada-inicializacao/indice-01-mapa-dos-arquivos-e-ordem-de-leitura.md` §5, `02-operacao-procedimento-do-agente/prompt-01-papel-e-restricoes-do-agente.md` §4 and `prompt-02-como-localizar-e-ler-o-archetype.md` §1/§1.4/§6.
- [ ] T015 [P] [US1] Add code references to `02-operacao-procedimento-do-agente/prompt-03-regras-obrigatorias-de-geracao.md` §1/§2/§3 and `prompt-04-fluxo-de-execucao-e-verificacao.md` §1.2/§1.3/§1.4/§4.
- [ ] T016 [P] [US1] Add code references to `03-derivacao-regras-de-formacao-do-contrato/regras-01-como-derivar-a-entidade-do-contrato.md` §2/§6, `regras-04-como-aplicar-precedencia-e-validacao.md` §3/§7, `regras-05-como-materializar-e-emitir-o-contrato.md` §5/§7 and `regras-06-como-relatar-proveniencia.md` §2/§5.
- [ ] T017 [US1] Verify every message in `prompt-00` §0.5 has the 10 fields, 2+ options, suggestion, resume, forbidden and security lines, and a dictionary reference; fix the gaps.
- [ ] T018 [US1] Run `mvn -B test` against `src/test/archetype-post-generate/HardStopContractTest.groovy` and fix missing codes, missing labels or sites without codes in the procedure files.
- [ ] T019 [US1] Run the manual sample from `specs/007-hard-stop-contract/quickstart.md` §Cenário 12 and record whether an operator answers the 5 questions for the reviewed stops (SC-002).

**Checkpoint**: US1 is independently testable; all stops are coded and complete.

---

## Phase 4: User Story 2 — Lista de atributos inválida não gera módulo (Priority: P1)

**Goal**: An invalid final attribute list stops with `STOP-DER-02`, uses no `String:name` fallback and emits no Maven command.

**Independent Test**: Use a contract whose final derived list violates the catalog format and verify 0 modules, `STOP-DER-02`, the invalid segment, 3 options and no `mvn` line.

- [ ] T020 [P] [US2] Update `regras-01-como-derivar-a-entidade-do-contrato.md` §6 to validate the final list against the catalog regex and stop with `STOP-DER-02`; remove the `String:name` fallback and preserve the existing name/type warnings.
- [ ] T021 [P] [US2] Update `regras-04-como-aplicar-precedencia-e-validacao.md` §3 with the explicit exception: invalid derived `custom_entity_attributes_list` does not apply the default; other flags keep the current fallback.
- [ ] T022 [P] [US2] Update `prompt-04-fluxo-de-execucao-e-verificacao.md` §1.2 and `regras-06-como-relatar-proveniencia.md` §5/§6 to reflect `STOP-DER-02` in the checklist and provenance.
- [ ] T023 [US2] Verify and adjust the `STOP-DER-02` message in `prompt-00` §0.5: invalid segment, `Tipo:nome`, 3 options, suggestion, no `String:name` fallback, no Maven.
- [ ] T024 [US2] Add the `STOP-DER-02` checks to `src/test/archetype-post-generate/HardStopContractTest.groovy` (3 options, no fallback, no Maven, 0 modules) and run `mvn -B test`.
- [ ] T025 [US2] Run the manual negative scenario from `specs/007-hard-stop-contract/quickstart.md` §Cenário 5 and record 0 modules and no Maven line.

**Checkpoint**: US2 is independently testable; the invalid list cannot generate a module.

---

## Phase 5: User Story 3 — Nenhuma parada muda passa no build (Priority: P2)

**Goal**: The build fails when a stop lacks a code, when a code is not registered or when a stop site does not cite a code.

**Independent Test**: Remove a code, add an unregistered `STOP-*`, remove a registry message or insert "informe apenas"; `mvn -B test` fails and names the file/code.

- [ ] T026 [US3] Complete the registry/site scan in `src/test/archetype-post-generate/HardStopContractTest.groovy`: 18 unique codes, 1:1 messages, `Pare` lines and `PARADA DURA` blocks, file names and counts.
- [ ] T027 [US3] Add the dictionary and prohibition checks to `HardStopContractTest.groovy`: 20 terms, 0 "informe apenas", no `String:name` fallback.
- [ ] T028 [US3] Add the opt-in installed-JAR checks to `HardStopContractTest.groovy` for `-Deda.verify.installed.jar=true`: registry, dictionary and `STOP-DER-02` in the installed artifact.
- [ ] T029 [US3] Run `mvn -B test` and fix the suite failures; confirm all checks pass at the current state.
- [ ] T030 [US3] Run the negative scenarios from `specs/007-hard-stop-contract/quickstart.md` §Cenário 10 and restore the files after each failure.
- [ ] T031 [US3] Verify each negative scenario names the missing code or file; record the results in `specs/007-hard-stop-contract/quickstart.md` §Cenário 10.

**Checkpoint**: US3 protects the registry against future silent stops.

---

## Phase 6: User Story 4 — Catálogo e procedimento sem divergência silenciosa (Priority: P2)

**Goal**: The 3-column critical table, the 9 defaults and the 3 literal format rules fail the build when they diverge from the catalog.

**Independent Test**: Change a default or a literal regex in the table and verify `CatalogSyncContractTest` fails naming the flag; the current state passes.

- [ ] T032 [P] [US4] Escape the internal pipes in `regras-04-como-aplicar-precedencia-e-validacao.md` §3.1 (`contract_enum_definitions` and `contract_nested_type_definitions`) and ensure exactly 3 columns.
- [ ] T033 [P] [US4] Implement `src/test/archetype-post-generate/CatalogSyncContractTest.groovy`: 3-column parsing, 9 defaults, 3 literal regexes (with placeholder normalization for `custom_entity_attributes_list`), 6 descriptive examples/tokens.
- [ ] T034 [P] [US4] Update `src/main/resources/META-INF/maven/archetype_metadata_required_properties/property-20-entity_contract_file_path.xml` comments to say the procedure requires an absolute path; keep `defaultValue` `none` and the permissive `validationRegex`.
- [ ] T035 [P] [US4] Update `regras-06-como-relatar-proveniencia.md` §2 with the explicit default exception for `contract_nested_type_definitions` = `none`.
- [ ] T036 [US4] Run `mvn -B test` against `CatalogSyncContractTest.groovy`; fix table/default/regex mismatches and confirm 35 properties/18 filesets.
- [ ] T037 [US4] Change a default and a literal regex in the table temporarily, confirm `CatalogSyncContractTest` fails naming the flag, then restore.
- [ ] T038 [US4] Verify the catalog description in `property-20-entity_contract_file_path.xml` says "absoluto", the regex still accepts `none` and a relative `.json`, and no `defaultValue`/`validationRegex` changed in `target/classes/META-INF/maven/archetype-metadata.xml`.

**Checkpoint**: US4 prevents silent catalog/procedure divergence.

---

## Phase 7: User Story 5 — Documentação do procedimento legível e correta (Priority: P3)

**Goal**: The critical table renders with 3 columns, `FR-006` is explained simply, and `pedidoitem` vs `pedido-item` is explicit.

**Independent Test**: Render the table, read the FR-006 note and read the package/artifact text; all three are clear without another document.

- [ ] T039 [P] [US5] Add the simple-language `FR-006` explanation to `regras-04-como-aplicar-precedencia-e-validacao.md` §3.
- [ ] T040 [P] [US5] Add the explicit package-vs-artifact text to `prompt-03-regras-obrigatorias-de-geracao.md` §2.1: `PedidoItem` → `pedidoitem` for `entity_package_name`; `PedidoItem` → `pedido-item` for `artifactId`.
- [ ] T041 [US5] Run `mvn -B test` against `CatalogSyncContractTest.groovy` and verify the 3-column table, FR-006 note and naming text.
- [ ] T042 [US5] Manually read `regras-04` §3.1/§3 and `prompt-03` §2.1; record that the three items are understandable without external help.

**Checkpoint**: US5 improves readability without changing behavior.

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Validate the whole feature against the spec, the JAR and the quickstart.

- [ ] T043 Run `mvn -B test` and fix any remaining failure in `src/test/archetype-post-generate/HardStopContractTest.groovy` or `CatalogSyncContractTest.groovy`.
- [ ] T044 Run `mvn clean install` and fix any packaging or catalog failure in `pom.xml`, `src/main/resources/META-INF/maven/archetype-metadata.xml` or `src/main/resources/llm-maven-flags-inference/`.
- [ ] T045 Run `mvn -B test "-Deda.verify.installed.jar=true"` and confirm `~/.m2/repository/com/eda-archetype/eda-archetype/0.0.1-SNAPSHOT/eda-archetype-0.0.1-SNAPSHOT.jar` contains the registry, dictionary and `STOP-DER-02`.
- [ ] T046 Validate the counts in `src/main/resources/llm-maven-flags-inference/` and `src/main/resources/META-INF/maven/archetype_metadata_required_properties/`: 19/13/6 and 35/18; no file added or removed.
- [ ] T047 Run the 12 scenarios in `specs/007-hard-stop-contract/quickstart.md` and record the outcomes, including the negative build checks.
- [ ] T048 Review `src/main/resources/llm-maven-flags-inference/` for Portuguese, 20-word sentences, active voice, translated acronyms, no secrets and no "informe apenas"; fix the findings.
- [ ] T049 Final diff review: `README.md` unchanged, no new dependency, no `defaultValue`/`validationRegex` change, no new procedure file, no generated code or fixture change.

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: starts immediately.
- **Foundational (Phase 2)**: depends on Setup; blocks all user stories.
- **US1 (Phase 3)**: depends on Foundational.
- **US2 (Phase 4)**: depends on US1 because both touch `regras-01`, `regras-04`, `prompt-04` and `regras-06`.
- **US3 (Phase 5)**: can start after Foundational; its final validation depends on US1, US2 and US4.
- **US4 (Phase 6)**: depends on US1 because both touch `regras-04` and `regras-06`.
- **US5 (Phase 7)**: depends on US4 because both touch `regras-04`; also touches `prompt-03` from US1.
- **Polish (Phase 8)**: depends on US1–US5.

### User Story Dependencies

- **US1 (P1)**: after Foundational; no dependency on US2–US5.
- **US2 (P1)**: after US1; independently testable with an invalid-list contract.
- **US3 (P2)**: test suite can be developed after Foundational; final validation after US1/US2/US4.
- **US4 (P2)**: after US1; independently testable through `CatalogSyncContractTest`.
- **US5 (P3)**: after US4; independently testable by reading the table and notes.

### Within Each Story

- Tests first when the story includes a test suite; confirm the failure before implementation.
- Contract/dictionary/registry before site codes.
- Site codes before behavior changes in the same files.
- Behavior changes before final validation.
- Manual scenarios after the automated suite passes.

### Parallel Opportunities

- T002 and T003 create different suites in parallel.
- T014, T015 and T016 edit different file groups in parallel.
- T020, T021 and T022 edit different files in parallel.
- T032, T033, T034 and T035 edit different files in parallel.
- T039 and T040 edit different files in parallel.
- T043, T044 and T045 are Maven commands on the same tree; run them sequentially, not in parallel.

---

## Parallel Example: User Story 1

```text
Task: "Add code references to indice-01 §5, prompt-01 §4 and prompt-02 §1/§1.4/§6"
Task: "Add code references to prompt-03 §1/§2/§3 and prompt-04 §1.2/§1.3/§1.4/§4"
Task: "Add code references to regras-01 §2/§6, regras-04 §3/§7, regras-05 §5/§7 and regras-06 §2/§5"
```

## Parallel Example: User Story 2

```text
Task: "Update regras-01 §6 for STOP-DER-02"
Task: "Update regras-04 §3 for the custom_entity_attributes_list exception"
Task: "Update prompt-04 §1.2 and regras-06 §5/§6 for STOP-DER-02"
```

## Parallel Example: User Story 4

```text
Task: "Escape the pipes in regras-04 §3.1"
Task: "Implement CatalogSyncContractTest.groovy"
Task: "Update property-20 comments to absolute path"
Task: "Add the provenance exception in regras-06 §2"
```

---

## Implementation Strategy

### MVP First (US1 + US2)

1. Complete Phase 1: Setup.
2. Complete Phase 2: Foundational.
3. Complete Phase 3: US1 (all stops coded and complete).
4. Complete Phase 4: US2 (invalid list cannot generate a module).
5. **STOP and VALIDATE**: run `mvn -B test` and the US1/US2 manual scenarios.
6. US1 alone already delivers orientation; US2 closes the plausible-and-wrong module defect. Both are P1, so the recommended MVP is US1 + US2.

### Incremental Delivery

1. Setup + Foundational → contract, dictionary, registry and messages ready.
2. US1 → all stops coded and complete.
3. US2 → invalid list blocked.
4. US3 → build protects the registry.
5. US4 → table/catalog sync protected.
6. US5 → documentation clarified.
7. Polish → full build, JAR and quickstart validated.

### Parallel Team Strategy

1. One developer completes Setup + Foundational.
2. After Foundational:
   - Developer A: US1 site codes.
   - Developer B: US3 test suite; it edits `src/test`, so it can start with US1, but coordinate with T024 before editing the same `HardStopContractTest.groovy`.
3. After US1:
   - Developer A: US2 behavior.
   - Developer B: US4 table/catalog sync.
4. After US4: US5 documentation.
5. Final: Polish and full validation.

---

## Notes

- [P] tasks touch different files and have no incomplete dependency.
- Every task includes a file path or a command with the file under validation.
- Tests are mandatory because the spec requires build-time failure checks.
- Do not add files to `src/main/resources/llm-maven-flags-inference/`; do not change `defaultValue` or `validationRegex`.
- Do not change `README.md`, generated code or fixtures.
- The `after_tasks` hook commits the tasks file; no manual commit or push is part of the feature.
