# Tasks: Renomear os Arquivos das Subpastas 03 e 04 do Procedimento

**Input**: Design documents from `/specs/009-rename-procedure-files/`

**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/, quickstart.md

**Tests**: Included because `plan.md` requires `FileRenameContractTest.groovy`, updates to the 6 existing suites and validation with `mvn -B test`/`mvn clean install`.

**Organization**: Tasks are grouped by the two feature phases (US1/Fase 1 and US2/Fase 2) so each phase can be implemented, tested and demonstrated independently.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (`US1`, `US2`)
- Include exact file paths in descriptions

## Path Conventions

- Single Maven module at repository root.
- Procedure: `src/main/resources/llm-maven-flags-inference/`
- Test suites: `src/test/archetype-post-generate/`
- Runner: `src/test/groovy/run-archetype-post-generate-tests.groovy`
- Feature docs: `specs/009-rename-procedure-files/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Confirm the baseline, preserve the original files and prepare the rename manifest.

- [X] T001 Confirm the baseline in `src/main/resources/llm-maven-flags-inference/`: 19 files, 13 Markdown, 6 JSON, 4 subfolders, 6 files in `03-derivacao-regras-de-formacao-do-contrato` and 6 in `04-referencia-templates-de-contrato`.
- [X] T002 Copy the 12 files listed in `specs/009-rename-procedure-files/contracts/rename-manifest.md` to `%TEMP%\opencode\rename-009-baseline` and record their SHA-256 hashes.
- [X] T003 Preflight the 12 rename mappings from `specs/009-rename-procedure-files/contracts/rename-manifest.md`: confirm each old file exists, each new name does not exist and no destination collides.
- [X] T004 Validate that `specs/009-rename-procedure-files/contracts/rename-manifest.md` contains 12 mappings, 18 operational files and the phase assignment for each reference.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Create the contract suite and register it before any rename. This suite is phase-aware: it treats the project as Phase 1 while the 04 new names do not exist, and as Phase 2 once `04-referencia-templates-de-contrato/01-schema-do-contrato.json` exists.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [X] T005 Create `src/test/archetype-post-generate/FileRenameContractTest.groovy` with the phase-aware harness, the 12-entry mapping from `contracts/rename-manifest.md`, the 19/13/6/4 counts, the catalog 35/18 check and the anti-drift scan excluding `specs/`, `graphify-out/`, `target/` and the suite itself.
- [X] T006 Register `FileRenameContractTest.groovy` in the packaged-procedure block of `src/test/groovy/run-archetype-post-generate-tests.groovy`.
- [X] T007 Add the phase-aware expected maps to `FileRenameContractTest.groovy`: Phase 1 expects the 6 new names in 03 and the 6 old names in 04; Phase 2 expects the 12 new names and 0 old names. Infer the phase from the existence of `04-referencia-templates-de-contrato/01-schema-do-contrato.json`.
- [X] T008 Run `mvn -B test` from the repository root and confirm `FileRenameContractTest.groovy` (runner: `src/test/groovy/run-archetype-post-generate-tests.groovy`) fails on the missing 03 new names while the other suites still pass; record the expected failure.

**Checkpoint**: The contract suite exists, is registered and fails for the right reason. User story work can begin.

---

## Phase 3: User Story 1 - Fase 1 — Renomear os arquivos de derivação (Priority: P1) 🎯 MVP

**Goal**: Renomear os 6 arquivos de `03-derivacao-regras-de-formacao-do-contrato`, atualizar as referências de 03 e preservar o conteúdo normativo, sem tocar no README nem nos arquivos de 04.

**Independent Test**: Listar 03 e encontrar exatamente os 6 nomes novos, 0 `regras-*`, conteúdo normativo preservado e referências de 03 atualizadas; 04 e README permanecem intactos.

### Tests for User Story 1

- [X] T009 [P] [US1] Update the 03 paths in `src/test/archetype-post-generate/BuildArtifactsTest.groovy` lines 96–101 to the new names; leave the 04 paths for T027.
- [X] T010 [P] [US1] Update the 03 paths in `src/test/archetype-post-generate/PathRenameContractTest.groovy` lines 55–60 to the new names; leave the 04 paths for T028.
- [X] T011 [P] [US1] Update the 03 path and labels in `src/test/archetype-post-generate/ProcedureContractTest.groovy` lines 198, 252 and 260 (`regras-03` → `03`, `regras-01` → `01`).
- [X] T012 [P] [US1] Update the 03 path in `src/test/archetype-post-generate/HardStopContractTest.groovy` line 110 (`regras-01` → `01`).
- [X] T013 [P] [US1] Update the 03 paths and labels in `src/test/archetype-post-generate/DestinationContractTest.groovy` lines 40, 58, 83, 87 and 91 (`regras-01` → `01`; `regras-06` → `06`).
- [X] T014 [P] [US1] Update the 03 paths and labels in `src/test/archetype-post-generate/CatalogSyncContractTest.groovy` lines 9, 162 and 168 (`regras-04` → `04`; `regras-06` → `06`).
- [X] T015 [US1] Add the 03-specific assertions to `src/test/archetype-post-generate/FileRenameContractTest.groovy`: 6 new names in 03, 0 `regras-*`, 03 references absent from 01/02/03 and the test suites, 04 and README untouched. Depends on T005/T007.

### Implementation for User Story 1

- [X] T016 [US1] Rename the 6 files in `src/main/resources/llm-maven-flags-inference/03-derivacao-regras-de-formacao-do-contrato/` with `Rename-Item`, in numeral order, per `data-model.md` `RenamedFile.old_name`/`new_name`: `regras-01-como-derivar-a-entidade-do-contrato.md` → `01-como-derivar-a-entidade-do-contrato.md`, `regras-02-como-resolver-tipos-e-enums.md` → `02-como-resolver-tipos-e-enums.md`, `regras-03-como-derivar-flags-de-tipos-aninhados.md` → `03-como-derivar-flags-de-tipos-aninhados.md`, `regras-04-como-aplicar-precedencia-e-validacao.md` → `04-como-aplicar-precedencia-e-validacao.md`, `regras-05-como-materializar-e-emitir-o-contrato.md` → `05-como-materializar-e-emitir-o-contrato.md`, `regras-06-como-relatar-proveniencia.md` → `06-como-relatar-proveniencia.md`.
- [X] T017 [P] [US1] Update the 03 references in `src/main/resources/llm-maven-flags-inference/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md` lines 173–176 (`regras-01`, `regras-04`, `regras-05` → `01`, `04`, `05`) and lines 715–720 (full 03 paths); leave the 04 references on line 220 and lines 721–726 for T032.
- [X] T018 [P] [US1] Update the 03 references in `src/main/resources/llm-maven-flags-inference/01-entrada-inicializacao/indice-01-mapa-dos-arquivos-e-ordem-de-leitura.md` lines 18–23 (full 03 paths) and line 52 (`regras-01`..`regras-06` → `01`..`06`); leave the 04 rows 24–29 and line 53 for T033.
- [X] T019 [P] [US1] Update `src/main/resources/llm-maven-flags-inference/02-operacao-procedimento-do-agente/prompt-01-papel-e-restricoes-do-agente.md` line 35 (`regras-01` → `01`).
- [X] T020 [P] [US1] Update `src/main/resources/llm-maven-flags-inference/02-operacao-procedimento-do-agente/prompt-02-como-localizar-e-ler-o-archetype.md` line 109 (`regras-01` → `01`).
- [X] T021 [P] [US1] Update `src/main/resources/llm-maven-flags-inference/02-operacao-procedimento-do-agente/prompt-04-fluxo-de-execucao-e-verificacao.md` lines 7, 26 and 44 (`regras-06`, `regras-01`, `regras-05` → `06`, `01`, `05`).
- [X] T022 [P] [US1] Update `src/main/resources/llm-maven-flags-inference/03-derivacao-regras-de-formacao-do-contrato/02-como-resolver-tipos-e-enums.md` lines 67 and 70 (`regras-03` → `03`).
- [X] T023 [P] [US1] Update `src/main/resources/llm-maven-flags-inference/03-derivacao-regras-de-formacao-do-contrato/04-como-aplicar-precedencia-e-validacao.md` line 41 (`regras-01` → `01`).
- [X] T024 [P] [US1] Update `src/main/resources/llm-maven-flags-inference/03-derivacao-regras-de-formacao-do-contrato/06-como-relatar-proveniencia.md` line 61 (`regras-01` → `01`).
- [X] T025 [US1] Validate Phase 1 per `quickstart.md` Scenarios 1–2 and `data-model.md` `FeaturePhase.scope`: 6 new names in 03, 0 `regras-*`, content normative preserved, 03 references updated in 01/02/03 and the test suites; 04 and README untouched.
- [X] T026 [US1] Run `mvn -B test` from the repository root (runner: `src/test/groovy/run-archetype-post-generate-tests.groovy`) and confirm all suites pass with the phase-aware `FileRenameContractTest.groovy` still in Phase 1.

**Checkpoint**: User Story 1 is complete and demonstrable independently. 04 and README remain in their old state by design.

---

## Phase 4: User Story 2 - Fase 2 — Renomear os arquivos de referência (Priority: P2)

**Goal**: Renomear os 6 arquivos de `04-referencia-templates-de-contrato`, atualizar as referências de 04, atualizar o README para 03 e 04 e concluir a varredura final.

**Independent Test**: Listar 04 e encontrar exatamente os 6 nomes novos, 0 `template-*`, conteúdo normativo preservado, 19/13/6/4, README atualizado e 0 referências antigas no corpus operacional.

**Dependency**: Depends on User Story 1 because the index, prompts, README and test suites are shared and because the final count of 19 includes 03.

### Tests for User Story 2

- [X] T027 [P] [US2] Update the 04 paths in `src/test/archetype-post-generate/BuildArtifactsTest.groovy` lines 102–107 to the new names.
- [X] T028 [P] [US2] Update the 04 paths in `src/test/archetype-post-generate/PathRenameContractTest.groovy` lines 61–66 to the new names.
- [X] T029 [P] [US2] Update the 04 paths and labels in `src/test/archetype-post-generate/ProcedureContractTest.groovy` lines 12, 139, 148 and 199 (`template-01`/`template-03` → `01`/`03`).
- [X] T030 [US2] Extend `src/test/archetype-post-generate/FileRenameContractTest.groovy` with the 04-specific and final assertions: 6 new names in 04, 0 `template-*`, 0 old references in the full operational corpus, README updated, 19/13/6/4. Depends on T005/T007.

### Implementation for User Story 2

- [X] T031 [US2] Rename the 6 files in `src/main/resources/llm-maven-flags-inference/04-referencia-templates-de-contrato/` with `Rename-Item`, in numeral order, per `data-model.md` `RenamedFile.old_name`/`new_name`: `template-01-schema-do-contrato.json` → `01-schema-do-contrato.json`, `template-02-exemplo-contrato-plano.json` → `02-exemplo-contrato-plano.json`, `template-03-exemplo-contrato-com-objetos-aninhados.json` → `03-exemplo-contrato-com-objetos-aninhados.json`, `template-04-exemplo-contrato-com-enums.json` → `04-exemplo-contrato-com-enums.json`, `template-05-exemplo-contrato-com-tipos-do-projeto.json` → `05-exemplo-contrato-com-tipos-do-projeto.json`, `template-06-exemplo-contrato-com-attributes-explicito.json` → `06-exemplo-contrato-com-attributes-explicito.json`.
- [X] T032 [P] [US2] Update the 04 references in `src/main/resources/llm-maven-flags-inference/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md` line 220 (`template-02-exemplo-contrato-plano.json` → `02-exemplo-contrato-plano.json`) and lines 721–726 (full 04 paths).
- [X] T033 [P] [US2] Update the 04 references in `src/main/resources/llm-maven-flags-inference/01-entrada-inicializacao/indice-01-mapa-dos-arquivos-e-ordem-de-leitura.md` lines 24–29 (full 04 paths) and line 53 (`template-01` → `01`).
- [X] T034 [P] [US2] Update `src/main/resources/llm-maven-flags-inference/04-referencia-templates-de-contrato/01-schema-do-contrato.json` line 4 (`regras-01` → `01`) without changing keys, schema, types or examples.
- [X] T035 [P] [US2] Update `src/main/resources/llm-maven-flags-inference/04-referencia-templates-de-contrato/02-exemplo-contrato-plano.json` line 2 (`regras-01` → `01`) without changing keys, schema, types or examples.
- [X] T036 [P] [US2] Update `src/main/resources/llm-maven-flags-inference/04-referencia-templates-de-contrato/06-exemplo-contrato-com-attributes-explicito.json` line 2 (`regras-01`, `regras-02` → `01`, `02`) without changing keys, schema, types or examples.
- [X] T037 [P] [US2] Update `README.md` lines 910, 917, 933, 941, 995, 2171–2176, 2182–2187, 2391, 2868, 2871, 2882, 2890, 2942, 2950 and 2954 to the new short and full names; keep the directory links unchanged.
- [X] T038 [US2] Run the final reference scan and counts from `quickstart.md` Scenarios 3–4: 0 `regras-0[1-6]`/`template-0[1-6]` in the operational corpus (excluding `specs/`, `graphify-out/`, `target/` and `FileRenameContractTest`), 19/13/6/4, index lists the 12 new paths.
- [X] T039 [US2] Run `mvn -B test` from the repository root (runner: `src/test/groovy/run-archetype-post-generate-tests.groovy`) and confirm all suites pass with `FileRenameContractTest.groovy` in Phase 2.

**Checkpoint**: User Stories 1 and 2 are complete. The full operational corpus uses only the new names.

---

## Phase 5: Polish & Cross-Cutting Concerns

**Purpose**: Prove normative preservation and run the full validation.

- [X] T040 [P] Review the diff and hashes per `quickstart.md` Scenario 5: the 6 renamed files without references have identical hashes; the 6 with references differ only in the mapped tokens; the 5 non-renamed procedure files and `README.md` differ only in references.
- [X] T041 Run `mvn clean install` from the repository root (`pom.xml`) and confirm the build, the 6 updated suites and `FileRenameContractTest.groovy` pass.
- [X] T042 Run `mvn -B clean integration-test` from the repository root (`pom.xml`) and confirm the integration fixtures do not regress.
- [X] T043 Run `mvn -B test "-Deda.verify.installed.jar=true"` from the repository root (`pom.xml`) after T041 and confirm the installed JAR carries the 12 new names and no old names.
- [X] T044 Run the `quickstart.md` scenarios and record the results, including the negative checks that must fail when a name or reference is reintroduced.
- [X] T045 Run the final invariants check in `src/main/resources/llm-maven-flags-inference/` and `src/main/resources/META-INF/maven/`: 19 files (13 Markdown, 6 JSON), 4 subfolders, 6 files in 03 and 04, catalog 35/18, no new procedure files, no changes to `archetype-resources/`, `specs/` historical artifacts or `graphify-out/`.

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies; can start immediately.
- **Foundational (Phase 2)**: Depends on Setup; blocks all user stories.
- **User Story 1 (Phase 3)**: Depends on Foundational; MVP.
- **User Story 2 (Phase 4)**: Depends on User Story 1 because the index, prompts, README and test suites are shared and the final count of 19 includes 03.
- **Polish (Phase 5)**: Depends on both user stories.

### Within User Story 1

- T009–T014 (test path updates) can run in parallel after Foundational.
- T015 depends on T005/T007.
- T016 (rename 03) depends on T009–T015.
- T017–T024 (reference updates) depend on T016; after T016 they can run in parallel because they touch different files.
- T025 depends on T017–T024.
- T026 depends on T025.

### Within User Story 2

- T027–T029 (test path updates) can run in parallel after T026.
- T030 depends on T005/T007.
- T031 (rename 04) depends on T027–T030.
- T032–T036 (reference updates) depend on T031; after T031 they can run in parallel because they touch different files.
- T037 (README) depends on T031 and can run in parallel with T032–T036.
- T038 depends on T032–T037.
- T039 depends on T038.

### Parallel Opportunities

- Setup: T001–T004 are mostly read-only and can be prepared together, but T002/T003 should follow T001.
- Foundational: T005–T007 are sequential because they edit the same suite/runner; T008 follows.
- US1 tests: T009–T014 touch different test files and can run in parallel.
- US1 reference updates: T017–T024 touch different files and can run in parallel after T016.
- US2 tests: T027–T029 touch different test files and can run in parallel.
- US2 reference updates: T032–T036 touch different files and can run in parallel after T031; T037 touches README and can run in parallel with them.
- Polish: T040 can run before the build; T041–T043 are sequential because they share the same build tree and T043 depends on T041; T044 depends on T040–T043; T045 follows T044.

### Parallel Example: User Story 1

```text
# After T016:
Task: "Update prompt-00 03 references" (T017)
Task: "Update indice-01 03 references" (T018)
Task: "Update prompt-01 reference" (T019)
Task: "Update prompt-02 reference" (T020)
Task: "Update prompt-04 references" (T021)
Task: "Update 02-como-resolver internal reference" (T022)
Task: "Update 04-como-aplicar internal reference" (T023)
Task: "Update 06-como-relatar internal reference" (T024)
```

### Parallel Example: User Story 2

```text
# After T031:
Task: "Update prompt-00 04 references" (T032)
Task: "Update indice-01 04 references" (T033)
Task: "Update 01-schema description" (T034)
Task: "Update 02-exemplo description" (T035)
Task: "Update 06-exemplo description" (T036)
Task: "Update README references" (T037)
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup.
2. Complete Phase 2: Foundational.
3. Complete Phase 3: User Story 1.
4. **STOP and VALIDATE**: run Phase 1 scenarios and `mvn -B test`; 04 and README must remain old.
5. Demo the 03 convention if ready.

### Incremental Delivery

1. Setup + Foundational → contract suite ready.
2. User Story 1 → 03 renamed and referenced → validate independently.
3. User Story 2 → 04 renamed, README updated, final scan → validate.
4. Polish → full build and artifact verification.

### Parallel Team Strategy

Do not run US1 and US2 fully in parallel: they share the index, prompts, README and test suites. Use parallel work only inside each phase (tests and reference updates) after the phase's rename task.

---

## Notes

- `[P]` tasks touch different files and can run in parallel after their phase prerequisite.
- `[US1]`/`[US2]` labels map tasks to the two feature phases.
- Phase 1 must not touch `README.md` or any file in `04-referencia-templates-de-contrato/`.
- Phase 2 must update the README for 03 and 04 and complete the final scan.
- Do not create a separate "references" phase; reference updates are part of each phase's definition of done.
- No new files in the procedure, no catalog changes, no `archetype-resources/` changes, no new dependencies.
- Revert any temporary negative-check change before closing the phase.

## Format Validation

- Every task starts with `- [ ]`.
- Every task has a sequential `TNNN` ID.
- Setup, Foundational and Polish tasks have no `[USn]` label.
- Every User Story task has `[US1]` or `[US2]`.
- `[P]` is used only where files differ and the phase prerequisite is complete.
- Every task description contains an exact file path or a concrete procedure path.
