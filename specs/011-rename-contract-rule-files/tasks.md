# Tasks: Renomear a Pasta e os Seis Arquivos das Regras de Formação do Contrato

**Input**: Design documents from `specs/011-rename-contract-rule-files/`

**Prerequisites**: `plan.md` (required), `spec.md` (required), `research.md`, `data-model.md`, `contracts/rename-manifest.md`, `quickstart.md`

**Tests**: Atualizar suítes existentes em cada fase de user story (Phase 3–5). A suíte nova `ContractRuleRenameContractTest.groovy` é foundational e phase-aware; não há TDD novo.

**Organization**: Tarefas agrupadas por user story para permitir implementação, teste e demonstração independentes. Cada fase de user story termina com build e varredura próprios.

## Mapa de fases

| Fase da feature | Nome | User Story | Prioridade | Depende de | Testes | Fase da task |
|---|---|---|---|---|---|---|
| Fase 1 | Renomear a pasta e atualizar as referências à pasta | US1 | P1 | — | Atualizar suítes existentes | Phase 3 |
| Fase 2 | Renomear as três primeiras regras e atualizar as referências | US2 | P2 | Fase 1 (caminho da pasta) | Atualizar suítes existentes | Phase 4 |
| Fase 3 | Renomear as três últimas regras e atualizar as referências | US3 | P3 | Fase 1, Fase 2 (conferência final) | Atualizar suítes existentes; varredura final | Phase 5 |

## Como executar por fase da feature

- Fase 1:
  `/speckit.implement Implemente somente a Phase 3: User Story 1 - Fase 1 — Renomear a pasta e atualizar as referências à pasta. Não execute as demais.`

- Fase 2:
  `/speckit.implement Implemente somente a Phase 4: User Story 2 - Fase 2 — Renomear as três primeiras regras e atualizar as referências. Não execute as demais.`

- Fase 3:
  `/speckit.implement Implemente somente a Phase 5: User Story 3 - Fase 3 — Renomear as três últimas regras e atualizar as referências. Requer Fase 1 e Fase 2. Não execute as demais.`

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- Módulo Maven único na raiz do repositório.
- Procedimento: `src/main/resources/llm-maven-flags-inference/`.
- Suítes: `src/test/archetype-post-generate/`.
- Runner: `src/test/groovy/run-archetype-post-generate-tests.groovy`.
- Fixtures: `src/test/resources/projects/*/archetype.properties`.
- Artefatos da feature: `specs/011-rename-contract-rule-files/`.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Confirmar a linha de base, os hashes e a ausência de colisões antes de qualquer renomeação.

- [X] T001 Confirmar a linha de base em `src/main/resources/llm-maven-flags-inference/`: 19 arquivos, 13 Markdown, 6 JSON, 4 subpastas e 6 arquivos em `03-derivacao-regras-de-formacao-do-contrato`; registrar a saída na seção "Baseline" de `specs/011-rename-contract-rule-files/contracts/rename-manifest.md`.
- [X] T002 Registrar SHA-256 dos seis arquivos de `src/main/resources/llm-maven-flags-inference/03-derivacao-regras-de-formacao-do-contrato/` na seção "Baseline" de `specs/011-rename-contract-rule-files/contracts/rename-manifest.md` (`FileRename.content_hash_before` de `data-model.md`).
- [X] T003 [P] Confirmar que `src/main/resources/llm-maven-flags-inference/03-regras-de-formacao-do-contrato/` e os seis nomes de destino não existem; abortar se houver colisão.
- [X] T004 [P] Confirmar que `mvn -B clean test` passa antes de qualquer mudança, a partir da raiz (`pom.xml`).

**Checkpoint**: Linha de base, hashes e destinos confirmados; nenhuma colisão.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Criar o contrato phase-aware e preparar as suítes compartilhadas que bloqueiam as fases de user story.

**⚠️ CRITICAL**: Nenhuma fase de user story começa antes desta fase.

- [X] T005 Criar `src/test/archetype-post-generate/ContractRuleRenameContractTest.groovy` phase-aware para a pasta 03, os seis nomes, as contagens 19/13/6/4, o índice (os 19 caminhos na mesma ordem, com papel e como fonte única da ordem global; inclusive a ordem e o papel dos seis arquivos), o README, as fixtures, o redirect legado e o anti-drift; usar `FolderRename.new_name` e `FileRename.new_name` de `specs/011-rename-contract-rule-files/data-model.md`.
- [X] T006 Registrar `ContractRuleRenameContractTest.groovy` no bloco `packaged procedure` de `src/test/groovy/run-archetype-post-generate-tests.groovy`.
- [X] T007 Refatorar `src/test/archetype-post-generate/FileRenameContractTest.groovy` linhas 32–45 para `p1LegacyOld`, `p1Current` e `p1New`, e adicionar a detecção `rulePhase` descrita em `specs/011-rename-contract-rule-files/contracts/rename-manifest.md`.
- [X] T008 [P] Confirmar que o redirect legado `src/main/resources/utils/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md` permanece no caminho antigo e aponta para `llm-maven-flags-inference/01-entrada-inicializacao/00-entry-point.md`.
- [X] T009 [P] Confirmar que `01-entrada-inicializacao`, `02-operacao-procedimento-do-agente` e `04-referencia-templates-de-contrato` permanecem com os nomes atuais em `src/main/resources/llm-maven-flags-inference/`.
- [X] T010 [P] Validar o inventário de `specs/011-rename-contract-rule-files/contracts/rename-manifest.md` contra a árvore real: 10 linhas do README para a pasta, 6 linhas da tabela de arquivos, 13 linhas no procedimento, 4 fixtures e 8 suítes.

**Checkpoint**: Foundation ready — as fases de user story podem começar.

---

## Phase 3: User Story 1 - Fase 1 — Renomear a pasta e atualizar as referências à pasta (Priority: P1) 🎯 MVP

**Goal**: Renomear a subpasta 03 para `03-regras-de-formacao-do-contrato` e atualizar todas as referências à pasta, mantendo os seis nomes de arquivo atuais.

**Independent Test**: Listar `src/main/resources/llm-maven-flags-inference/`, conferir a pasta nova e a ausência da antiga, os seis arquivos com os nomes atuais, o índice/README/fixtures/suítes sem referência à pasta antiga e `mvn -B clean test` passando.

### Implementation for User Story 1

- [X] T011 [US1] Renomear `src/main/resources/llm-maven-flags-inference/03-derivacao-regras-de-formacao-do-contrato/` para `src/main/resources/llm-maven-flags-inference/03-regras-de-formacao-do-contrato/` com `Rename-Item`, após pré-checar a ausência do destino.
- [X] T012 [P] [US1] Atualizar `src/main/resources/llm-maven-flags-inference/01-entrada-inicializacao/01-mapa-dos-arquivos-e-ordem-de-leitura.md` linhas 18–23: trocar o segmento de pasta `03-derivacao-regras-de-formacao-do-contrato` por `03-regras-de-formacao-do-contrato`, mantendo os seis nomes de arquivo atuais.
- [X] T013 [P] [US1] Atualizar `src/main/resources/llm-maven-flags-inference/01-entrada-inicializacao/00-entry-point.md` linhas 715–720: trocar o segmento de pasta, mantendo os seis nomes de arquivo atuais.
- [X] T014 [P] [US1] Atualizar `src/main/resources/llm-maven-flags-inference/02-operacao-procedimento-do-agente/02-como-localizar-e-ler-o-archetype.md` linha 141: trocar o segmento de pasta.
- [X] T015 [P] [US1] Atualizar `README.md` linhas 54, 607, 652, 816, 817, 995, 2167, 2208, 2391 e 2868: link do índice, `id`/`name` da âncora, título da subseção, texto/href do link e caminhos em prosa; trocar apenas o segmento da pasta e preservar as formas curtas.
- [X] T016 [P] [US1] Atualizar os comentários de pasta em `src/test/resources/projects/custom-nested-edge-cases/archetype.properties` (linha 16), `src/test/resources/projects/custom-nested-objects/archetype.properties` (linha 16), `src/test/resources/projects/entity-contract/archetype.properties` (linha 20) e `src/test/resources/projects/custom-enums-from-payload/archetype.properties` (linha 17).

### Tests for User Story 1 (Atualização de suítes existentes)

> Atualize as expectativas de pasta antes de rodar o build da fase.

- [X] T017 [P] [US1] Atualizar `src/test/archetype-post-generate/BuildArtifactsTest.groovy` linha 84 para a nova pasta.
- [X] T018 [P] [US1] Atualizar `src/test/archetype-post-generate/CatalogSyncContractTest.groovy` linhas 9 e 162 para a nova pasta, mantendo os nomes de arquivo atuais.
- [X] T019 [P] [US1] Atualizar `src/test/archetype-post-generate/DestinationContractTest.groovy` linhas 40 e 41 para a nova pasta, mantendo os nomes de arquivo atuais.
- [X] T020 [P] [US1] Atualizar `src/test/archetype-post-generate/HardStopContractTest.groovy` linha 110 para a nova pasta, mantendo o nome atual do arquivo 01.
- [X] T021 [P] [US1] Atualizar `src/test/archetype-post-generate/PathRenameContractTest.groovy` linhas 43 e 55–60 para a nova pasta, mantendo os seis nomes atuais.
- [X] T022 [P] [US1] Atualizar `src/test/archetype-post-generate/ProcedureContractTest.groovy` linhas 198 e 252 para a nova pasta, mantendo os nomes atuais dos arquivos 03 e 01.
- [X] T023 [P] [US1] Atualizar `src/test/archetype-post-generate/ProcedureEntryRenameContractTest.groovy` linha 71 para a nova pasta.
- [X] T024 [P] [US1] Atualizar `src/test/archetype-post-generate/FileRenameContractTest.groovy` (referências de pasta equivalentes às linhas 12, 99, 107, 112 e 125) para a nova pasta, mantendo a detecção `rulePhase` e os nomes atuais nesta fase.

### Validation for User Story 1

- [X] T025 [US1] Executar `mvn -B clean test` a partir da raiz (`pom.xml`) e corrigir falhas da Fase 1.
- [X] T026 [US1] Executar a varredura da Fase 1 em `README.md`, `src/main/resources/llm-maven-flags-inference/` e `src/test/`, excluindo `specs/`, `graphify-out/`, `target/` e as asserções negativas, confirmar 0 referências à pasta antiga e confirmar que o índice mantém os 19 caminhos na mesma ordem, com papel, e como fonte única da ordem global.

**Checkpoint**: Fase 1 demonstrável sem a Fase 2; os seis arquivos ainda têm os nomes atuais.

---

## Phase 4: User Story 2 - Fase 2 — Renomear as três primeiras regras e atualizar as referências (Priority: P2)

**Goal**: Renomear os arquivos `01`–`03` removendo `como-` e atualizar as referências a eles, mantendo `04`–`06` com os nomes atuais.

**Independent Test**: Listar a subpasta 03, conferir os três nomes novos, a ausência dos três nomes antigos, o conteúdo normativo idêntico, as referências aos três atualizadas e os arquivos `04`–`06` com os nomes atuais.

### Implementation for User Story 2

- [X] T027 [US2] Renomear `src/main/resources/llm-maven-flags-inference/03-regras-de-formacao-do-contrato/01-como-derivar-a-entidade-do-contrato.md` para `01-derivar-a-entidade-do-contrato.md`, após pré-checar o destino.
- [X] T028 [US2] Renomear `src/main/resources/llm-maven-flags-inference/03-regras-de-formacao-do-contrato/02-como-resolver-tipos-e-enums.md` para `02-resolver-tipos-e-enums.md`, após pré-checar o destino.
- [X] T029 [US2] Renomear `src/main/resources/llm-maven-flags-inference/03-regras-de-formacao-do-contrato/03-como-derivar-flags-de-tipos-aninhados.md` para `03-derivar-flags-de-tipos-aninhados.md`, após pré-checar o destino.
- [X] T030 [P] [US2] Atualizar `src/main/resources/llm-maven-flags-inference/01-entrada-inicializacao/01-mapa-dos-arquivos-e-ordem-de-leitura.md` linhas 18–20 para os novos nomes `01`–`03`; manter 21–23 com os nomes atuais.
- [X] T031 [P] [US2] Atualizar `src/main/resources/llm-maven-flags-inference/01-entrada-inicializacao/00-entry-point.md` linhas 715–717 para os novos nomes `01`–`03`; manter 718–720 com os nomes atuais.
- [X] T032 [P] [US2] Atualizar `README.md` linhas 2171–2173 para os novos nomes `01`–`03`.

### Tests for User Story 2 (Atualização de suítes existentes)

- [X] T033 [P] [US2] Atualizar `src/test/archetype-post-generate/BuildArtifactsTest.groovy` linhas 96–98 para os novos nomes `01`–`03`.
- [X] T034 [P] [US2] Atualizar `src/test/archetype-post-generate/DestinationContractTest.groovy` linha 40 para o novo nome do arquivo 01.
- [X] T035 [P] [US2] Atualizar `src/test/archetype-post-generate/HardStopContractTest.groovy` linha 110 para o novo nome do arquivo 01.
- [X] T036 [P] [US2] Atualizar `src/test/archetype-post-generate/PathRenameContractTest.groovy` linhas 55–57 para os novos nomes `01`–`03`.
- [X] T037 [P] [US2] Atualizar `src/test/archetype-post-generate/ProcedureContractTest.groovy` linhas 198 e 252 para os novos nomes dos arquivos 03 e 01.

### Validation for User Story 2

- [X] T038 [US2] Executar `mvn -B clean test` a partir da raiz (`pom.xml`) e corrigir falhas da Fase 2.
- [X] T039 [US2] Executar a varredura dos três nomes antigos (`01`–`03`) em `README.md`, `src/main/resources/llm-maven-flags-inference/` e `src/test/`, confirmar 0 referências, exceto asserções negativas, e confirmar que o índice mantém os 19 caminhos na mesma ordem, com papel, e como fonte única da ordem global.

**Checkpoint**: Fase 2 demonstrável; `04`–`06` permanecem com os nomes atuais.

---

## Phase 5: User Story 3 - Fase 3 — Renomear as três últimas regras e atualizar as referências (Priority: P3)

**Goal**: Renomear os arquivos `04`–`06` removendo `como-`, atualizar as referências e concluir a varredura final.

**Independent Test**: Listar a subpasta 03, conferir os seis nomes novos e a ausência de `como-`, o conteúdo normativo idêntico, todas as referências operacionais atualizadas, 19 arquivos no procedimento e as suítes passando.

### Implementation for User Story 3

- [X] T040 [US3] Renomear `src/main/resources/llm-maven-flags-inference/03-regras-de-formacao-do-contrato/04-como-aplicar-precedencia-e-validacao.md` para `04-aplicar-precedencia-e-validacao.md`, após pré-checar o destino.
- [X] T041 [US3] Renomear `src/main/resources/llm-maven-flags-inference/03-regras-de-formacao-do-contrato/05-como-materializar-e-emitir-o-contrato.md` para `05-materializar-e-emitir-o-contrato.md`, após pré-checar o destino.
- [X] T042 [US3] Renomear `src/main/resources/llm-maven-flags-inference/03-regras-de-formacao-do-contrato/06-como-relatar-proveniencia.md` para `06-relatar-proveniencia.md`, após pré-checar o destino.
- [X] T043 [P] [US3] Atualizar `src/main/resources/llm-maven-flags-inference/01-entrada-inicializacao/01-mapa-dos-arquivos-e-ordem-de-leitura.md` linhas 21–23 para os novos nomes `04`–`06`.
- [X] T044 [P] [US3] Atualizar `src/main/resources/llm-maven-flags-inference/01-entrada-inicializacao/00-entry-point.md` linhas 718–720 para os novos nomes `04`–`06`.
- [X] T045 [P] [US3] Atualizar `README.md` linhas 2174–2176 para os novos nomes `04`–`06`.

### Tests for User Story 3 (Atualização de suítes existentes)

- [X] T046 [P] [US3] Atualizar `src/test/archetype-post-generate/BuildArtifactsTest.groovy` linhas 99–101 para os novos nomes `04`–`06`.
- [X] T047 [P] [US3] Atualizar `src/test/archetype-post-generate/CatalogSyncContractTest.groovy` linhas 9 e 162 para os novos nomes dos arquivos 04 e 06.
- [X] T048 [P] [US3] Atualizar `src/test/archetype-post-generate/DestinationContractTest.groovy` linha 41 para o novo nome do arquivo 06.
- [X] T049 [P] [US3] Atualizar `src/test/archetype-post-generate/PathRenameContractTest.groovy` linhas 58–60 para os novos nomes `04`–`06`.

### Validation for User Story 3

- [X] T050 [US3] Executar `mvn -B clean test` a partir da raiz (`pom.xml`) e corrigir falhas da Fase 3.
- [X] T051 [US3] Executar a varredura final em `README.md`, `src/main/resources/llm-maven-flags-inference/` e `src/test/`: 0 referências à pasta antiga e aos seis nomes antigos, exceto o redirect legado e as asserções negativas, e confirmar que o índice mantém os 19 caminhos na mesma ordem, com papel, e como fonte única da ordem global.
- [X] T052 [US3] Comparar SHA-256/diff dos seis arquivos de `src/main/resources/llm-maven-flags-inference/03-regras-de-formacao-do-contrato/` com o baseline e confirmar que só tokens de referência mudaram.
- [X] T053 [US3] Executar `mvn clean install` a partir da raiz (`pom.xml`) e confirmar o artefato instalado.
- [X] T054 [US3] Executar `mvn -B clean integration-test` a partir da raiz (`pom.xml`).

**Checkpoint**: Os seis arquivos têm os nomes novos; o corpus operacional está limpo; o build completo passa.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Verificação final do artefato, do quickstart e das invariantes.

- [X] T055 [P] Executar `mvn -B test "-Deda.verify.installed.jar=true"` após `mvn clean install` e confirmar o JAR instalado com `llm-maven-flags-inference/03-regras-de-formacao-do-contrato/` e os seis nomes novos.
- [X] T056 [P] Executar os cenários de `specs/011-rename-contract-rule-files/quickstart.md` e registrar o resultado na conclusão da feature.
- [X] T057 [P] Confirmar que `src/test/archetype-post-generate/ContractRuleRenameContractTest.groovy` está registrada em `src/test/groovy/run-archetype-post-generate-tests.groovy` e que `CoverageRegistryTest` e `ReadmeConsistencyTest` passam.
- [X] T058 Revisar o diff e confirmar 0 alterações em `pom.xml`, `src/main/resources/META-INF/`, `src/main/resources/archetype-resources/` e `specs/` históricas.
- [X] T059 Executar a validação final: `mvn -B clean test`, `mvn clean install` e `mvn -B clean integration-test` a partir da raiz (`pom.xml`).
- [X] T060 Confirmar que `src/main/resources/llm-maven-flags-inference/` mantém 19 arquivos, 13 Markdown, 6 JSON, 4 subpastas e 6 arquivos em `03-regras-de-formacao-do-contrato/`.
- [X] T061 Confirmar que nenhum segredo, credencial ou valor sensível foi adicionado a `src/`, `README.md` ou `specs/011-rename-contract-rule-files/`.

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — can start immediately.
- **Foundational (Phase 2)**: Depends on Setup completion — BLOCKS all user stories.
- **User Stories (Phase 3+)**: All depend on Foundational phase completion.
  - Execute in priority order (P1 → P2 → P3): US2 depends on the folder path from Fase 1; US3 depends on Fase 1 and Fase 2 for the final sweep.
- **Polish (Phase 6)**: Depends on all desired user stories being complete.

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) — no dependencies on other stories.
- **User Story 2 (P2)**: Can start after Foundational (Phase 2) — depends on Fase 1 only for the folder path.
- **User Story 3 (P3)**: Can start after Foundational (Phase 2) — depends on Fase 1 and Fase 2 for the final zero-old-names check.

### Within Each User Story

- Renomear a pasta/arquivos antes de atualizar referências.
- Atualizar índice, porta de entrada, README, fixtures e suítes antes do build da fase.
- Rodar `mvn -B clean test` ao final da fase.
- Executar a varredura de referências antigas antes do checkpoint.
- Concluir a fase antes de avançar para a próxima prioridade.

### Parallel Opportunities

- T003 e T004 no Setup.
- T008, T009 e T010 no Foundational.
- Após T011, as tarefas T012–T024 são [P] (arquivos diferentes).
- Após T027–T029, as tarefas T030–T037 são [P] (arquivos diferentes).
- Após T040–T042, as tarefas T043–T049 são [P] (arquivos diferentes).
- T055, T056 e T057 no Polish.

---

## Parallel Example: User Story 1

```text
# Após renomear a pasta (T011), executar em paralelo:
Task: "Atualizar o índice linhas 18–23 para a nova pasta"
Task: "Atualizar a porta de entrada linhas 715–720 para a nova pasta"
Task: "Atualizar a regra de localização linha 141 para a nova pasta"
Task: "Atualizar o README linhas 54, 607, 652, 816, 817, 995, 2167, 2208, 2391 e 2868"
Task: "Atualizar as quatro fixtures"
Task: "Atualizar as oito suítes existentes"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup.
2. Complete Phase 2: Foundational (CRITICAL — blocks all stories).
3. Complete Phase 3: User Story 1.
4. **STOP and VALIDATE**: Testar a Fase 1 independentemente.
5. Demonstrar a pasta nova e os seis arquivos com os nomes atuais.

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready.
2. Add User Story 1 → Test independently → Demo (MVP!).
3. Add User Story 2 → Test independently → Demo.
4. Add User Story 3 → Test independently → Demo.
5. Each story adds value without breaking previous stories.

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together.
2. Once Foundational is done:
   - Developer A: User Story 1.
   - Developer B: User Story 2 (after the folder path from US1).
   - Developer C: User Story 3 (after US1 and US2 for the final sweep).
3. Stories complete and integrate independently.

---

## Notes

- [P] tasks = different files, no dependencies.
- [Story] label maps the task to the specific user story.
- Each user story is independently completable and testable.
- No test-only phase: the plan adds one phase-aware contract suite in Foundational and updates existing suites inside each user story phase.
- Commit after each task or logical group.
- Stop at any checkpoint to validate the story independently.
- Avoid vague tasks, same-file conflicts and cross-story dependencies that break independence.
