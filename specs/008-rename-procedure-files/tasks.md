# Tasks: Renomear Arquivos das Subpastas 01 e 02 em Três Fases

**Input**: Design documents from `/specs/008-rename-procedure-files/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/, quickstart.md

**Tests**: as seis suítes existentes são atualizadas na Fase 3; não há criação de suítes novas nem TDD.

**Organization**: tarefas agrupadas por fase da feature, na ordem P1 → P2 → P3. A Fase 3 depende das Fases 1 e 2.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: pode rodar em paralelo (arquivos diferentes, sem dependência de tarefa incompleta)
- **[Story]**: US1, US2 ou US3, apenas nas fases de user story
- Caminhos exatos em todas as tarefas

## Path Conventions

- Módulo Maven único na raiz do repositório.
- Procedimento: `src/main/resources/llm-maven-flags-inference/`
- Suítes: `src/test/archetype-post-generate/`
- Runner: `src/test/groovy/run-archetype-post-generate-tests.groovy`
- Documentação da feature: `specs/008-rename-procedure-files/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: confirmar a estrutura e a linha de base antes de qualquer edição

- [ ] T001 Confirmar 19 arquivos e 4 subpastas em `src/main/resources/llm-maven-flags-inference/`; registrar a listagem nominal em `$env:TEMP\eda-008\baseline-listing.txt`
- [ ] T002 Rodar `mvn -B test` no módulo raiz (`pom.xml`) e registrar a linha de base verde; se houver falha inesperada, parar e reportar
- [ ] T003 [P] Gravar o manifesto SHA-256 dos 19 arquivos do procedimento em `$env:TEMP\eda-008\baseline-hashes.json`
- [ ] T004 [P] Confirmar a existência de `src/test/groovy/run-archetype-post-generate-tests.groovy` e das seis suítes afetadas em `src/test/archetype-post-generate/`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: registrar o mapa de tokens e o inventário que a Fase 3 usará

**⚠️ CRITICAL**: a Fase 3 não pode começar antes desta fase

- [ ] T005 [P] Registrar os 14 tokens de substituição de `specs/008-rename-procedure-files/contracts/reference-update-contract.md` em `$env:TEMP\eda-008\token-map.md`
- [ ] T006 [P] Rodar `Select-String -Pattern "prompt-0[0-5]|indice-01-mapa"` nas superfícies operacionais e confirmar 115 ocorrências em 15 arquivos; gravar a contagem por arquivo em `$env:TEMP\eda-008\reference-inventory.txt`
- [ ] T007 Confirmar a ordem de execução das três fases e a dependência da Fase 3 em relação às Fases 1 e 2, conforme `specs/008-rename-procedure-files/plan.md`; nenhuma edição de produção

**Checkpoint**: mapa e inventário prontos; as fases de renomeação podem começar

---

## Phase 3: User Story 1 - Fase 1 — Entrada e mapa da subpasta 01 (Priority: P1) 🎯 MVP

**Goal**: renomear a porta de entrada e o mapa de leitura, preservando conteúdo e deixando a subpasta 01 com exatamente dois arquivos.

**Independent Test**: aplicar somente esta fase e listar `src/main/resources/llm-maven-flags-inference/01-entrada-inicializacao/`; devem existir `01-entryPoint.md` e `02-mapa-dos-arquivos-e-ordem-de-leitura.md`, com hashes idênticos e sem os nomes antigos.

### Implementation for User Story 1

- [ ] T008 [US1] Capturar o `PhaseManifest` da Fase 1 em `$env:TEMP\eda-008\fase1-manifest.json`: `RenameOperation` dos 2 alvos e `FolderSnapshot` das subpastas 02, 03 e 04, conforme `data-model.md`
- [ ] T009 [US1] Verificar as pré-condições: `01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md` e `01-entrada-inicializacao/indice-01-mapa-dos-arquivos-e-ordem-de-leitura.md` existem; `01-entrada-inicializacao/01-entryPoint.md` e `01-entrada-inicializacao/02-mapa-dos-arquivos-e-ordem-de-leitura.md` não existem
- [ ] T010 [P] [US1] Renomear `src/main/resources/llm-maven-flags-inference/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md` para `src/main/resources/llm-maven-flags-inference/01-entrada-inicializacao/01-entryPoint.md` com `Rename-Item`
- [ ] T011 [P] [US1] Renomear `src/main/resources/llm-maven-flags-inference/01-entrada-inicializacao/indice-01-mapa-dos-arquivos-e-ordem-de-leitura.md` para `src/main/resources/llm-maven-flags-inference/01-entrada-inicializacao/02-mapa-dos-arquivos-e-ordem-de-leitura.md` com `Rename-Item`
- [ ] T012 [US1] Validar `src/main/resources/llm-maven-flags-inference/01-entrada-inicializacao/`: exatamente 2 arquivos, nomes corretos, `targetHash` igual a `sourceHash` nos 2 `RenameOperation` e `FolderSnapshot` das subpastas 02, 03 e 04 inalterado
- [ ] T013 [US1] Rodar `mvn -B test` em `src/test/archetype-post-generate/` e registrar as cinco falhas esperadas: `PathRenameContractTest`, `BuildArtifactsTest`, `HardStopContractTest`, `DestinationContractTest` e `ReadmeConsistencyTest`; confirmar que não há falha fora dessa lista
- [ ] T014 [US1] Registrar o checkpoint da Fase 1 em `$env:TEMP\eda-008\fase1-resultado.md` com listagem, hashes e suítes vermelhas; build vermelho é esperado até a Fase 3

**Checkpoint**: US1 concluída e verificável isoladamente por listagem e hash

---

## Phase 4: User Story 2 - Fase 2 — Operação do agente sem o prefixo prompt- (Priority: P2)

**Goal**: remover o prefixo `prompt-` dos cinco arquivos da subpasta 02, preservando numeral, descrição e conteúdo.

**Independent Test**: aplicar somente esta fase e listar `src/main/resources/llm-maven-flags-inference/02-operacao-procedimento-do-agente/`; devem existir cinco arquivos sem `prompt-`, numerados de 01 a 05, com hashes idênticos.

### Implementation for User Story 2

- [ ] T015 [US2] Capturar o `PhaseManifest` da Fase 2 em `$env:TEMP\eda-008\fase2-manifest.json`: `RenameOperation` dos 5 alvos e `FolderSnapshot` das subpastas 01, 03 e 04, conforme `data-model.md`
- [ ] T016 [US2] Verificar as pré-condições: os cinco arquivos `prompt-01` a `prompt-05` existem em `src/main/resources/llm-maven-flags-inference/02-operacao-procedimento-do-agente/`; os cinco destinos não existem
- [ ] T017 [P] [US2] Renomear `src/main/resources/llm-maven-flags-inference/02-operacao-procedimento-do-agente/prompt-01-papel-e-restricoes-do-agente.md` para `src/main/resources/llm-maven-flags-inference/02-operacao-procedimento-do-agente/01-papel-e-restricoes-do-agente.md`
- [ ] T018 [P] [US2] Renomear `src/main/resources/llm-maven-flags-inference/02-operacao-procedimento-do-agente/prompt-02-como-localizar-e-ler-o-archetype.md` para `src/main/resources/llm-maven-flags-inference/02-operacao-procedimento-do-agente/02-como-localizar-e-ler-o-archetype.md`
- [ ] T019 [P] [US2] Renomear `src/main/resources/llm-maven-flags-inference/02-operacao-procedimento-do-agente/prompt-03-regras-obrigatorias-de-geracao.md` para `src/main/resources/llm-maven-flags-inference/02-operacao-procedimento-do-agente/03-regras-obrigatorias-de-geracao.md`
- [ ] T020 [P] [US2] Renomear `src/main/resources/llm-maven-flags-inference/02-operacao-procedimento-do-agente/prompt-04-fluxo-de-execucao-e-verificacao.md` para `src/main/resources/llm-maven-flags-inference/02-operacao-procedimento-do-agente/04-fluxo-de-execucao-e-verificacao.md`
- [ ] T021 [P] [US2] Renomear `src/main/resources/llm-maven-flags-inference/02-operacao-procedimento-do-agente/prompt-05-entregaveis-e-relatorios.md` para `src/main/resources/llm-maven-flags-inference/02-operacao-procedimento-do-agente/05-entregaveis-e-relatorios.md`
- [ ] T022 [US2] Validar `src/main/resources/llm-maven-flags-inference/02-operacao-procedimento-do-agente/`: exatamente 5 arquivos, nenhum com prefixo `prompt-`, `targetHash` igual a `sourceHash` nos 5 `RenameOperation` e `FolderSnapshot` das subpastas 01, 03 e 04 inalterado
- [ ] T023 [US2] Rodar `mvn -B test` em `src/test/archetype-post-generate/` e registrar as seis falhas esperadas, incluindo `CatalogSyncContractTest`; confirmar que não há falha fora dessa lista
- [ ] T024 [US2] Registrar o checkpoint da Fase 2 em `$env:TEMP\eda-008\fase2-resultado.md` com listagem, hashes e suítes vermelhas

**Checkpoint**: US2 concluída e verificável isoladamente por listagem e hash

---

## Phase 5: User Story 3 - Fase 3 — Atualização das referências operacionais (Priority: P3)

**Goal**: substituir as 115 ocorrências dos sete nomes antigos em 15 arquivos operacionais e restaurar `mvn -B test` verde.

**Independent Test**: após as Fases 1 e 2, aplicar esta fase, rodar `Select-String -Pattern "prompt-0[0-5]|indice-01-mapa"` nas 15 superfícies e obter 0 ocorrências; rodar `mvn -B test` e obter todas as suítes verdes.

**Dependência**: esta fase depende das Fases 1 (US1) e 2 (US2).

### Implementation for User Story 3

- [ ] T025 [US3] Confirmar que as Fases 1 e 2 estão aplicadas: os sete novos caminhos existem e os sete nomes antigos não existem em `src/main/resources/llm-maven-flags-inference/`
- [ ] T026 [P] [US3] Atualizar as 15 referências em `README.md` com o mapa de `contracts/reference-update-contract.md`; apenas caminhos e nomes
- [ ] T027 [P] [US3] Atualizar as 14 referências em `src/main/resources/llm-maven-flags-inference/01-entrada-inicializacao/02-mapa-dos-arquivos-e-ordem-de-leitura.md`, incluindo as 7 linhas de caminho e a ordem global
- [ ] T028 [P] [US3] Atualizar as 35 referências em `src/main/resources/llm-maven-flags-inference/01-entrada-inicializacao/01-entryPoint.md`; preservar regras, dicionário e registro de paradas
- [ ] T029 [P] [US3] Atualizar a referência a `prompt-00` em `src/main/resources/llm-maven-flags-inference/02-operacao-procedimento-do-agente/01-papel-e-restricoes-do-agente.md`
- [ ] T030 [P] [US3] Atualizar a referência a `prompt-00` em `src/main/resources/llm-maven-flags-inference/02-operacao-procedimento-do-agente/03-regras-obrigatorias-de-geracao.md`
- [ ] T031 [P] [US3] Atualizar a referência a `prompt-03` em `src/main/resources/llm-maven-flags-inference/02-operacao-procedimento-do-agente/04-fluxo-de-execucao-e-verificacao.md`
- [ ] T032 [P] [US3] Atualizar a referência a `prompt-00` em `src/main/resources/llm-maven-flags-inference/03-derivacao-regras-de-formacao-do-contrato/regras-01-como-derivar-a-entidade-do-contrato.md`
- [ ] T033 [P] [US3] Atualizar as 2 referências a `prompt-00` em `src/main/resources/llm-maven-flags-inference/03-derivacao-regras-de-formacao-do-contrato/regras-04-como-aplicar-precedencia-e-validacao.md`
- [ ] T034 [P] [US3] Atualizar o alvo do redirect em `src/main/resources/utils/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md` para `llm-maven-flags-inference/01-entrada-inicializacao/01-entryPoint.md`; não renomear o stub

### Test maintenance for User Story 3 (required by FR-013)

- [ ] T035 [P] [US3] Atualizar as 8 referências em `src/test/archetype-post-generate/BuildArtifactsTest.groovy`
- [ ] T036 [P] [US3] Atualizar as 2 referências em `src/test/archetype-post-generate/CatalogSyncContractTest.groovy`
- [ ] T037 [P] [US3] Atualizar as 16 referências em `src/test/archetype-post-generate/DestinationContractTest.groovy`
- [ ] T038 [P] [US3] Atualizar as 4 referências em `src/test/archetype-post-generate/HardStopContractTest.groovy`
- [ ] T039 [P] [US3] Atualizar as 11 referências em `src/test/archetype-post-generate/PathRenameContractTest.groovy`
- [ ] T040 [P] [US3] Atualizar as 3 referências em `src/test/archetype-post-generate/ReadmeConsistencyTest.groovy`

### Validation for User Story 3

- [ ] T041 [US3] Rodar `Select-String -Pattern "prompt-0[0-5]|indice-01-mapa"` nas 15 superfícies operacionais listadas em `specs/008-rename-procedure-files/contracts/reference-update-contract.md` e exigir 0 ocorrências; registrar o resultado do `ReferenceUpdate`
- [ ] T042 [US3] Rodar `mvn -B test` no módulo raiz (`pom.xml`) e exigir todas as suítes verdes
- [ ] T043 [US3] Inspecionar `git diff` e confirmar que cada hunk é apenas troca de referência; nenhuma regra alterada e nenhuma alteração em `specs/003` a `specs/007`, `target/` ou `graphify-out/`
- [ ] T044 [US3] Registrar o checkpoint da Fase 3 em `$env:TEMP\eda-008\fase3-resultado.md` com 0 tokens, `mvn -B test` verde e a interpretação da FR-009

**Checkpoint**: as três fases concluídas; o procedimento não cita mais os nomes antigos e o build está verde

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: validação final e limpeza

- [ ] T045 [P] Executar os cenários de `specs/008-rename-procedure-files/quickstart.md` e registrar os resultados
- [ ] T046 [P] Rodar `mvn -B clean install` no módulo raiz (`pom.xml`) como paridade opcional com o CI
- [ ] T047 Confirmar 19 arquivos e 4 subpastas, ausência de aliases/cópias e ausência dos sete nomes antigos em `src/main/resources/llm-maven-flags-inference/`
- [ ] T048 Revisar `git status --porcelain` contra `specs/008-rename-procedure-files/quickstart.md` e confirmar que apenas README, procedimento, redirect e as seis suítes foram alterados

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: sem dependência; começa imediatamente
- **Foundational (Phase 2)**: depende do Setup; bloqueia as user stories
- **User Story 1 (Phase 3)**: depende do Foundational; sem dependência de US2
- **User Story 2 (Phase 4)**: depende do Foundational; sem dependência de US1
- **User Story 3 (Phase 5)**: depende de US1 e US2; não é independente
- **Polish (Phase 6)**: depende de US1, US2 e US3

### User Story Dependencies

- **US1 (Fase 1, P1)**: pode começar após o Foundational; não depende de US2 nem de US3
- **US2 (Fase 2, P2)**: pode começar após o Foundational; não depende de US1 nem de US3
- **US3 (Fase 3, P3)**: começa somente após US1 e US2; é a única fase que restaura `mvn -B test`

### Within Each User Story

- **US1**: T008 → T009 → T010/T011 → T012 → T013 → T014
- **US2**: T015 → T016 → T017–T021 → T022 → T023 → T024
- **US3**: T025 → T026–T040 → T041 → T042 → T043 → T044

### Parallel Opportunities

- Setup: T003 e T004 em paralelo
- Foundational: T005 e T006 em paralelo
- US1: T010 e T011 em paralelo após T009
- US2: T017, T018, T019, T020 e T021 em paralelo após T016
- US3: T026 a T040 em paralelo, pois são 15 arquivos diferentes

---

## Parallel Example: User Story 1

```text
Task: "Renomear prompt-00-leia-este-arquivo-primeiro.md para 01-entryPoint.md"
Task: "Renomear indice-01-mapa-dos-arquivos-e-ordem-de-leitura.md para 02-mapa-dos-arquivos-e-ordem-de-leitura.md"
```

## Parallel Example: User Story 2

```text
Task: "Renomear prompt-01-papel-e-restricoes-do-agente.md"
Task: "Renomear prompt-02-como-localizar-e-ler-o-archetype.md"
Task: "Renomear prompt-03-regras-obrigatorias-de-geracao.md"
Task: "Renomear prompt-04-fluxo-de-execucao-e-verificacao.md"
Task: "Renomear prompt-05-entregaveis-e-relatorios.md"
```

## Parallel Example: User Story 3

```text
Task: "Atualizar README.md"
Task: "Atualizar o conteúdo do índice"
Task: "Atualizar o conteúdo da entrada"
Task: "Atualizar os arquivos de 02 e 03"
Task: "Atualizar as seis suítes"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Completar Setup e Foundational
2. Completar a Fase 1 (US1)
3. **PARAR e VALIDAR**: listagem e hashes da subpasta 01
4. O build permanece vermelho até a Fase 3; o MVP é validado por inspeção

### Incremental Delivery

1. Setup + Foundational → base pronta
2. Fase 1 → validar listagem/hash → build vermelho esperado
3. Fase 2 → validar listagem/hash → build vermelho esperado
4. Fase 3 → 0 tokens antigos → `mvn -B test` verde
5. Polish → quickstart e paridade opcional com o CI

### Parallel Team Strategy

- US1 e US2 podem rodar em paralelo após o Foundational
- US3 só começa depois de US1 e US2
- Dentro de US3, as 15 atualizações de arquivo podem rodar em paralelo

---

## Notes

- [P] = arquivos diferentes, sem dependência de tarefa incompleta
- [US1], [US2] e [US3] mapeiam as três fases da feature
- A Fase 3 não é independente: depende das Fases 1 e 2
- As Fases 1 e 2 são validadas por listagem e SHA-256, não por build verde
- As seis suítes são atualizadas apenas na Fase 3
- Não criar suítes novas nem tarefas TDD
- Não alterar regras, textos normativos, formatação ou numeração interna
- Não tocar em `specs/003` a `specs/007`, `target/` ou `graphify-out/`
