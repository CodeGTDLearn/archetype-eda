---
description: "Task list for 011-rename-procedure-folders"
---

# Tasks: Renomear as Subpastas Iniciais e o Arquivo de Leitura do Procedimento

**Input**: Design documents from `/specs/011-rename-procedure-folders/`

**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/, quickstart.md

**Tests**: Atualizar as 8 suítes existentes por fase; nenhuma suíte nova; TDD não solicitado.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Mapa de fases

| Fase da feature | Nome | User Story | Prioridade | Depende de | Testes | Fase da task |
|---|---|---|---|---|---|---|
| Fase 1 | Subpasta de inicialização | US1 | P1 | — | Atualizar suítes existentes; validar nomes, contagens e referências | Phase 3 |
| Fase 2 | Subpasta de procedimento do agente | US2 | P2 | Fase 1 (conferência final) | Atualizar suítes existentes; validar nomes, contagens e referências | Phase 4 |
| Fase 3 | Arquivo de localização e leitura do archetype | US3 | P3 | Fase 2 (caminho da subpasta) | Atualizar suítes existentes; varredura final de referências e contagens | Phase 5 |

## Como executar por fase da feature

- Fase 1:
  /speckit.implement Implemente somente a Phase 3: User Story 1 - Fase 1 — Subpasta de inicialização. Não execute as demais.

- Fase 2:
  /speckit.implement Implemente somente a Phase 4: User Story 2 - Fase 2 — Subpasta de procedimento do agente. Requer a Fase 1. Não execute as demais.

- Fase 3:
  /speckit.implement Implemente somente a Phase 5: User Story 3 - Fase 3 — Arquivo de localização e leitura do archetype. Requer a Fase 2. Não execute as demais.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- Módulo único na raiz do repositório.
- Procedimento: `src/main/resources/llm-maven-flags-inference/`.
- Redirect legado: `src/main/resources/utils/01-entrada-inicializacao/`.
- Suítes: `src/test/archetype-post-generate/`.
- Runner: `src/test/groovy/run-archetype-post-generate-tests.groovy`.
- Artefatos da feature: `specs/011-rename-procedure-folders/`.

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Confirmar o ambiente, a linha de base e a ausência de colisões antes de qualquer renomeação.

- [X] T001 Confirmar a raiz do repositório, o JDK 21, o Maven e a ausência de alterações não commitadas; confirmar a presença de `specs/011-rename-procedure-folders/plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/rename-manifest.md` e `quickstart.md`.
- [X] T002 [P] Confirmar a linha de base em `src/main/resources/llm-maven-flags-inference/`: 19 arquivos, 13 Markdown, 6 JSON, 4 subpastas, 2 arquivos em `01-entrada-inicializacao` e 5 em `02-operacao-procedimento-do-agente`.
- [X] T003 [P] Registrar SHA-256 dos 19 arquivos de `src/main/resources/llm-maven-flags-inference/` com `Get-FileHash` em um diretório temporário fora do repositório.
- [X] T004 [P] Confirmar que `src/main/resources/llm-maven-flags-inference/01-inicializacao`, `src/main/resources/llm-maven-flags-inference/02-procedimento-do-agente` e `src/main/resources/llm-maven-flags-inference/02-procedimento-do-agente/02-localizar-e-ler-o-archetype.md` não existem.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Fixar o contrato de renomeação e a linha de base de testes que bloqueiam todas as user stories.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [X] T005 [P] Revisar `specs/011-rename-procedure-folders/contracts/rename-manifest.md` e `specs/011-rename-procedure-folders/data-model.md`; confirmar o mapa `old → new`, as invariantes I1-I9 e as exceções (redirect legado e asserções negativas).
- [X] T006 [P] Confirmar as 8 suítes existentes no runner `src/test/groovy/run-archetype-post-generate-tests.groovy`: `BuildArtifactsTest`, `CatalogSyncContractTest`, `DestinationContractTest`, `FileRenameContractTest`, `HardStopContractTest`, `PathRenameContractTest`, `ProcedureEntryRenameContractTest` e `ReadmeConsistencyTest`; nenhuma suíte nova.
- [X] T007 [P] Executar `mvn -B test` na raiz (`pom.xml`) e registrar o resultado da linha de base.

**Checkpoint**: Fundação pronta; as três fases de user story podem começar.

---

## Phase 3: User Story 1 - Fase 1 — Subpasta de inicialização (Priority: P1) 🎯 MVP

**Goal**: Renomear `01-entrada-inicializacao` para `01-inicializacao` e atualizar todas as referências operacionais à subpasta 01.

**Independent Test**: listar `01-inicializacao`, conferir exatamente 2 arquivos, a ausência da subpasta antiga, o índice e a porta de entrada com os caminhos novos, o redirect legado apontando para o caminho novo, as suítes atualizadas e `mvn -B test` verde.

### Implementation for User Story 1

- [X] T008 [US1] Renomear `src/main/resources/llm-maven-flags-inference/01-entrada-inicializacao` para `src/main/resources/llm-maven-flags-inference/01-inicializacao`, após confirmar que o destino não existe.
- [X] T009 [P] [US1] Atualizar `src/main/resources/llm-maven-flags-inference/01-inicializacao/01-mapa-dos-arquivos-e-ordem-de-leitura.md` linhas 11 e 12 para `llm-maven-flags-inference/01-inicializacao/...`.
- [X] T010 [P] [US1] Atualizar `src/main/resources/llm-maven-flags-inference/01-inicializacao/00-entry-point.md` linhas 620 e 708-709 para o caminho `01-inicializacao`.
- [X] T011 [P] [US1] Atualizar `README.md` linhas 52, 603, 652 (parte 01), 806, 807, 995, 2111, 2152, 2207, 2513, 2567, 2639 e 2704, incluindo `href`, `id`, `name` e texto das âncoras da subpasta 01.
- [X] T012 [P] [US1] Atualizar `src/main/resources/utils/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md` linha 7 para apontar para `llm-maven-flags-inference/01-inicializacao/00-entry-point.md`; não renomear o redirect.
- [X] T013 [P] [US1] Atualizar `src/test/archetype-post-generate/BuildArtifactsTest.groovy` linhas 79, 82, 89 e 90 para a subpasta `01-inicializacao`.
- [X] T014 [P] [US1] Atualizar `src/test/archetype-post-generate/PathRenameContractTest.groovy` linhas 41, 48, 49, 69 e 70 para a subpasta `01-inicializacao`; manter as linhas 71 e 116 (redirect legado).
- [X] T015 [P] [US1] Atualizar `src/test/archetype-post-generate/HardStopContractTest.groovy` linhas 9 e 132 para a subpasta `01-inicializacao`.
- [X] T016 [P] [US1] Atualizar `src/test/archetype-post-generate/DestinationContractTest.groovy` linhas 36 e 134 para a subpasta `01-inicializacao`.
- [X] T017 [P] [US1] Atualizar `src/test/archetype-post-generate/ReadmeConsistencyTest.groovy` linha 157 para a subpasta `01-inicializacao`.
- [X] T018 [P] [US1] Atualizar `src/test/archetype-post-generate/ProcedureEntryRenameContractTest.groovy` para as expectativas phase-aware da subpasta `01-inicializacao` (p01, lista de pastas, caminho empacotado e caminhos do índice); manter o caminho legado da linha 16 e a checagem da linha 136.
- [X] T019 [P] [US1] Atualizar `src/test/archetype-post-generate/FileRenameContractTest.groovy` linha 16 e a lista de pastas da linha 125 para `01-inicializacao`.
- [X] T020 [US1] Executar `mvn -B test` na raiz (`pom.xml`) e corrigir qualquer falha da Fase 1.
- [X] T021 [US1] Validar a Fase 1 em `src/main/resources/llm-maven-flags-inference/`: 2 arquivos em `01-inicializacao`, ausência de `01-entrada-inicializacao`, 19/13/6/4, redirect correto e 0 referências operacionais à subpasta 01 antiga.

**Checkpoint**: User Story 1 testável isoladamente; a subpasta 02 e o arquivo 02 permanecem intactos.

---

## Phase 4: User Story 2 - Fase 2 — Subpasta de procedimento do agente (Priority: P2)

**Goal**: Renomear `02-operacao-procedimento-do-agente` para `02-procedimento-do-agente` e atualizar todas as referências operacionais à subpasta 02.

**Independent Test**: listar `02-procedimento-do-agente`, conferir exatamente 5 arquivos, a ausência da subpasta antiga, o índice e a porta de entrada com os caminhos novos, as suítes atualizadas e `mvn -B test` verde.

### Implementation for User Story 2

- [X] T022 [US2] Renomear `src/main/resources/llm-maven-flags-inference/02-operacao-procedimento-do-agente` para `src/main/resources/llm-maven-flags-inference/02-procedimento-do-agente`, após confirmar que o destino não existe.
- [X] T023 [P] [US2] Atualizar `src/main/resources/llm-maven-flags-inference/01-inicializacao/01-mapa-dos-arquivos-e-ordem-de-leitura.md` linhas 13-17 para `llm-maven-flags-inference/02-procedimento-do-agente/...`; na linha 14, alterar somente a pasta, mantendo o nome antigo do arquivo para a Fase 3.
- [X] T024 [P] [US2] Atualizar `src/main/resources/llm-maven-flags-inference/01-inicializacao/00-entry-point.md` linhas 710, 712, 713 e 714 para o caminho `02-procedimento-do-agente`; na linha 711, alterar somente a pasta.
- [X] T025 [P] [US2] Atualizar `README.md` linhas 53, 605, 652 (parte 02), 811 e 812, incluindo `href`, `id`, `name` e texto das âncoras da subpasta 02.
- [X] T026 [P] [US2] Atualizar `src/test/archetype-post-generate/BuildArtifactsTest.groovy` linhas 83 e 91-95 para a subpasta `02-procedimento-do-agente`.
- [X] T027 [P] [US2] Atualizar `src/test/archetype-post-generate/PathRenameContractTest.groovy` linhas 42 e 50-54 para a subpasta `02-procedimento-do-agente`; manter o arquivo 02 com o nome antigo nesta fase.
- [X] T028 [P] [US2] Atualizar `src/test/archetype-post-generate/DestinationContractTest.groovy` linhas 37-39 para a subpasta `02-procedimento-do-agente`.
- [X] T029 [P] [US2] Atualizar `src/test/archetype-post-generate/CatalogSyncContractTest.groovy` linha 163 para a subpasta `02-procedimento-do-agente`.
- [X] T030 [P] [US2] Atualizar `src/test/archetype-post-generate/ReadmeConsistencyTest.groovy` linha 237 para a subpasta `02-procedimento-do-agente`.
- [X] T031 [P] [US2] Atualizar `src/test/archetype-post-generate/ProcedureEntryRenameContractTest.groovy` para as expectativas phase-aware da subpasta `02-procedimento-do-agente`; manter o arquivo 02 com o nome antigo nesta fase.
- [X] T032 [P] [US2] Atualizar `src/test/archetype-post-generate/FileRenameContractTest.groovy` linha 125 (parte 02) para `02-procedimento-do-agente`.
- [X] T033 [US2] Executar `mvn -B test` na raiz (`pom.xml`) e corrigir qualquer falha da Fase 2.
- [X] T034 [US2] Validar a Fase 2 em `src/main/resources/llm-maven-flags-inference/`: 5 arquivos em `02-procedimento-do-agente`, ausência de `02-operacao-procedimento-do-agente`, 19/13/6/4, ordem do índice e 0 referências operacionais à subpasta 02 antiga.

**Checkpoint**: User Stories 1 e 2 testáveis; o arquivo 02 ainda tem o nome antigo.

---

## Phase 5: User Story 3 - Fase 3 — Arquivo de localização e leitura do archetype (Priority: P3)

**Goal**: Renomear `02-como-localizar-e-ler-o-archetype.md` para `02-localizar-e-ler-o-archetype.md` e atualizar todas as referências operacionais ao arquivo 02.

**Independent Test**: listar `02-procedimento-do-agente`, conferir exatamente 5 arquivos, o arquivo novo presente, o arquivo antigo ausente, o índice na posição 4 com o caminho novo, as suítes atualizadas e `mvn -B test` verde.

### Implementation for User Story 3

- [X] T035 [US3] Renomear `src/main/resources/llm-maven-flags-inference/02-procedimento-do-agente/02-como-localizar-e-ler-o-archetype.md` para `src/main/resources/llm-maven-flags-inference/02-procedimento-do-agente/02-localizar-e-ler-o-archetype.md`, após confirmar que o destino não existe.
- [X] T036 [P] [US3] Atualizar `src/main/resources/llm-maven-flags-inference/01-inicializacao/01-mapa-dos-arquivos-e-ordem-de-leitura.md` linha 14 (nome do arquivo), 33 e 47 para `02-localizar-e-ler-o-archetype`.
- [X] T037 [P] [US3] Atualizar `src/main/resources/llm-maven-flags-inference/01-inicializacao/00-entry-point.md` linhas 168, 170, 171, 172 e 711 para `02-localizar-e-ler-o-archetype`.
- [X] T038 [P] [US3] Atualizar `src/test/archetype-post-generate/PathRenameContractTest.groovy` linha 51 para `02-localizar-e-ler-o-archetype.md`.
- [X] T039 [P] [US3] Atualizar `src/test/archetype-post-generate/BuildArtifactsTest.groovy` linha 92 para `02-localizar-e-ler-o-archetype.md`.
- [X] T040 [P] [US3] Atualizar `src/test/archetype-post-generate/ProcedureEntryRenameContractTest.groovy` para as expectativas do arquivo `02-localizar-e-ler-o-archetype.md`, incluindo `p02Old`/`p02New` e asserções de ausência.
- [X] T041 [P] [US3] Confirmar que `src/test/archetype-post-generate/DestinationContractTest.groovy` não referencia o arquivo 02; nenhuma alteração necessária.
- [X] T042 [US3] Executar `mvn -B test` na raiz (`pom.xml`) e corrigir qualquer falha da Fase 3.
- [X] T043 [US3] Validar a Fase 3 em `src/main/resources/llm-maven-flags-inference/`: arquivo novo presente, arquivo antigo ausente, 5 arquivos, 19/13/6/4, índice na posição 4 e 0 referências operacionais ao nome antigo.

**Checkpoint**: As três user stories estão concluídas; o procedimento usa apenas os nomes novos, exceto o redirect legado.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Varredura final, verificação de integridade e validação do artefato.

- [X] T044 [P] Varrer `README.md` e `src/main/resources/llm-maven-flags-inference/` para os três nomes antigos; permitir apenas o caminho do redirect legado e as asserções negativas das suítes.
- [X] T045 [P] Confirmar que `src/main/resources/utils/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md` permanece no caminho antigo e aponta para `llm-maven-flags-inference/01-inicializacao/00-entry-point.md`; confirmar 0 redirects adicionais.
- [X] T046 [P] Comparar o diff e os hashes dos arquivos renomeados com a linha de base de `specs/011-rename-procedure-folders/research.md`; confirmar que apenas tokens de referência mudaram.
- [X] T047 [P] Confirmar que `src/main/resources/llm-maven-flags-inference/03-derivacao-regras-de-formacao-do-contrato/`, `04-referencia-templates-de-contrato/`, `src/main/resources/META-INF/`, `src/main/resources/archetype-resources/` e `pom.xml` não foram alterados.
- [X] T048 Executar `mvn -B clean install` na raiz (`pom.xml`) e corrigir qualquer falha.
- [X] T049 Executar `mvn -B clean integration-test` na raiz (`pom.xml`) e corrigir qualquer falha.
- [X] T050 Executar a verificação opt-in do JAR instalado na raiz (`pom.xml`) com `mvn -B clean install "-Deda.verify.installed.jar=true"`.
- [X] T051 Executar o roteiro de `specs/011-rename-procedure-folders/quickstart.md` e registrar os resultados no relatório de implementação.

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately.
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories.
- **User Stories (Phase 3+)**: All depend on Foundational phase completion.
  - User stories proceed in priority order (P1 → P2 → P3) because P2 and P3 use paths changed by the previous story.
  - P1 can be demonstrated independently.
- **Polish (Phase 6)**: Depends on all desired user stories being complete.

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - no dependencies on other stories.
- **User Story 2 (P2)**: Can start after Foundational (Phase 2) + Fase 1 (conferência final de zero nomes antigos).
- **User Story 3 (P3)**: Can start after Foundational (Phase 2) + Fase 2 (caminho da subpasta).

### Within Each User Story

- Renomear a pasta/arquivo antes de editar as referências no caminho novo.
- Atualizar índice, porta de entrada, README, redirect legado e suítes.
- Executar `mvn -B test` antes da validação da fase.
- Não alterar o conteúdo normativo; somente tokens de referência.

### Parallel Opportunities

- T002-T004 (linha de base, hashes e colisões) podem rodar em paralelo.
- T005-T007 (contrato, suítes e baseline de teste) podem rodar em paralelo.
- Após T008, T009-T019 podem rodar em paralelo (arquivos diferentes).
- Após T022, T023-T032 podem rodar em paralelo (arquivos diferentes).
- Após T035, T036-T041 podem rodar em paralelo (arquivos diferentes).
- T044-T047 (varreduras independentes) podem rodar em paralelo.
- Dentro de cada fase, os testes só rodam depois das atualizações de arquivo.

---

## Parallel Example: User Story 1

```bash
# Após renomear a subpasta 01, atualizar as referências em paralelo:
Task: "Atualizar o índice em 01-inicializacao/01-mapa-dos-arquivos-e-ordem-de-leitura.md"
Task: "Atualizar a porta de entrada em 01-inicializacao/00-entry-point.md"
Task: "Atualizar as linhas 52, 603, 652, 806, 807, 995, 2111, 2152, 2207, 2513, 2567, 2639 e 2704 do README.md"
Task: "Atualizar o redirect legado em src/main/resources/utils/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md"
Task: "Atualizar as suítes BuildArtifactsTest, PathRenameContractTest, HardStopContractTest, DestinationContractTest, ReadmeConsistencyTest, ProcedureEntryRenameContractTest e FileRenameContractTest"
```

## Parallel Example: User Story 2

```bash
# Após renomear a subpasta 02, atualizar as referências em paralelo:
Task: "Atualizar o índice em 01-inicializacao/01-mapa-dos-arquivos-e-ordem-de-leitura.md"
Task: "Atualizar a porta de entrada em 01-inicializacao/00-entry-point.md"
Task: "Atualizar as linhas 53, 605, 652, 811 e 812 do README.md"
Task: "Atualizar as suítes BuildArtifactsTest, PathRenameContractTest, DestinationContractTest, CatalogSyncContractTest, ReadmeConsistencyTest, ProcedureEntryRenameContractTest e FileRenameContractTest"
```

## Parallel Example: User Story 3

```bash
# Após renomear o arquivo 02, atualizar as referências em paralelo:
Task: "Atualizar o índice em 01-inicializacao/01-mapa-dos-arquivos-e-ordem-de-leitura.md"
Task: "Atualizar a porta de entrada em 01-inicializacao/00-entry-point.md"
Task: "Atualizar as suítes PathRenameContractTest, BuildArtifactsTest e ProcedureEntryRenameContractTest"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup.
2. Complete Phase 2: Foundational (CRITICAL - blocks all stories).
3. Complete Phase 3: User Story 1.
4. **STOP and VALIDATE**: listar `01-inicializacao`, conferir 2 arquivos, redirect e `mvn -B test`.
5. Demonstrar a Fase 1 isoladamente.

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready.
2. Add User Story 1 → Test independently → Demo (MVP!).
3. Add User Story 2 → Test independently → Demo.
4. Add User Story 3 → Test independently → Demo.
5. Cada fase adiciona valor sem quebrar as anteriores.

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together.
2. Once Foundational is done:
   - Developer A: User Story 1.
   - Developer B: User Story 2 after User Story 1's final zero-old check.
   - Developer C: User Story 3 after User Story 2's folder path.
3. Stories complete and integrate independently.

---

## Notes

- [P] tasks = different files, no dependencies.
- [Story] label maps task to specific user story for traceability.
- Each user story is independently completable and testable.
- No new test suite; update the 8 existing suites registered in the runner.
- No change to `03-derivacao-regras-de-formacao-do-contrato/`, `04-referencia-templates-de-contrato/`, `META-INF/`, `archetype-resources/`, `pom.xml` or historical specs.
- The legacy redirect path is the only allowed old folder name.
- Stop at any checkpoint to validate the story independently.
- Avoid: vague tasks, same-file conflicts, cross-story dependencies that break independence.
