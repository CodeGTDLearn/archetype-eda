# Tasks: Renomear os Arquivos de Entrada e Operação do Procedimento

**Input**: Design documents from `/specs/010-rename-procedure-entry-files/`

**Prerequisites**: plan.md (required), spec.md (required), research.md, data-model.md, contracts/rename-manifest.md, quickstart.md

**Tests**: O plano exige a atualização das suítes existentes e uma nova suíte de contrato phase-aware (`ProcedureEntryRenameContractTest`). Tarefas de teste são obrigatórias nesta feature.

**Organization**: Tarefas agrupadas por fase da feature para permitir implementação, teste e demonstração independentes.

## Formato: `[ID] [P?] [Story] Descrição`

- **[P]**: pode rodar em paralelo (arquivos diferentes, sem dependência de tarefa incompleta)
- **[Story]**: user story da tarefa (`US1`, `US2`); ausente em Setup, Foundational e Polish
- Todo task inclui caminho exato de arquivo ou diretório

## Convenções de caminho

- Projeto único: `src/main/resources/llm-maven-flags-inference/`, `src/main/resources/utils/`, `src/test/archetype-post-generate/`, `src/test/groovy/` e `specs/010-rename-procedure-entry-files/`.

## Mapa de fases

| Fase da feature | Nome | User Story | Prioridade | Depende de | Testes | Fase da task |
|---|---|---|---|---|---|---|
| Fase 1 | Subpasta de entrada e inicialização | US1 | P1 | — | Atualizar suítes existentes; nova suíte `ProcedureEntryRenameContractTest` | Phase 3 |
| Fase 2 | Subpasta de operação e procedimento do agente | US2 | P2 | Fase 1 (conferência final) | Atualizar suítes existentes; varredura final | Phase 4 |

> A suíte `ProcedureEntryRenameContractTest` é infraestrutura compartilhada: criada na Phase 2 (Foundational) e usada pelas Phase 3 e Phase 4.

## Como executar por fase da feature

- Fase 1:
  `/speckit.implement Implemente somente a Phase 3: User Story 1 - Fase 1 — Subpasta de entrada e inicialização. Não execute as demais.`

- Fase 2:
  `/speckit.implement Implemente somente a Phase 4: User Story 2 - Fase 2 — Subpasta de operação e procedimento do agente. Requer a Fase 1 (conferência final). Não execute as demais.`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Confirmar a linha de base e preparar a renomeação.

- [ ] T001 Confirmar a linha de base em `src/main/resources/llm-maven-flags-inference/`: 19 arquivos, 13 Markdown, 6 JSON, 4 subpastas, 2 em `01-entrada-inicializacao` e 5 em `02-operacao-procedimento-do-agente`.
- [ ] T002 [P] Registrar o SHA-256 dos sete arquivos de `src/main/resources/llm-maven-flags-inference/01-entrada-inicializacao/` e `src/main/resources/llm-maven-flags-inference/02-operacao-procedimento-do-agente/` em um diretório temporário fora do repositório.
- [ ] T003 [P] Confirmar que os sete nomes novos não existem em `src/main/resources/llm-maven-flags-inference/01-entrada-inicializacao/` e `src/main/resources/llm-maven-flags-inference/02-operacao-procedimento-do-agente/`, sem colisão.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Criar a suíte de contrato compartilhada e registrá-la no runner.

**⚠️ CRITICAL**: Nenhuma tarefa de user story começa antes desta fase.

- [ ] T004 [P] Criar `src/test/archetype-post-generate/ProcedureEntryRenameContractTest.groovy` com verificações phase-aware: sete nomes antigos/novos, nomes das subpastas `01-entrada-inicializacao` e `02-operacao-procedimento-do-agente`, contagens 19/13/6/4, ordem de leitura e papel dos arquivos, índice, redirect legado, ausência de referências antigas no corpus operacional e exceções para o redirect e asserções negativas.
- [ ] T005 Registrar `ProcedureEntryRenameContractTest.groovy` na lista de suítes de `src/test/groovy/run-archetype-post-generate-tests.groovy`.

**Checkpoint**: suíte nova criada e registrada; as fases de user story podem começar.

---

## Phase 3: User Story 1 - Fase 1 — Subpasta de entrada e inicialização (Priority: P1) 🎯 MVP

**Goal**: Renomear os dois arquivos de `01-entrada-inicializacao` e atualizar todas as referências a eles, mantendo os cinco nomes de `02-operacao-procedimento-do-agente` intactos.

**Independent Test**: listar `src/main/resources/llm-maven-flags-inference/01-entrada-inicializacao/`, conferir exatamente `00-entry-point.md` e `01-mapa-dos-arquivos-e-ordem-de-leitura.md`, confirmar que 02 mantém os cinco nomes antigos, que o índice lista os novos caminhos de 01, que o redirect legado aponta para `00-entry-point.md` e que as suítes passam.

### Tests for User Story 1

> **NOTE**: Atualize as suítes primeiro; elas devem falhar até a renomeação e as referências serem aplicadas.

- [ ] T006 [P] [US1] Atualizar `src/test/archetype-post-generate/BuildArtifactsTest.groovy` para os dois caminhos novos de 01 e o caminho novo do índice, mantendo os cinco caminhos antigos de 02.
- [ ] T007 [P] [US1] Atualizar `src/test/archetype-post-generate/PathRenameContractTest.groovy` para os dois caminhos novos de 01, `canonical` novo, `legacyEntry` antigo, README e verificação opt-in do JAR.
- [ ] T008 [P] [US1] Atualizar `src/test/archetype-post-generate/HardStopContractTest.groovy` para ler `01-entrada-inicializacao/00-entry-point.md`, incluindo rótulos e entrada do ZIP.
- [ ] T009 [P] [US1] Atualizar `src/test/archetype-post-generate/DestinationContractTest.groovy` para ler `01-entrada-inicializacao/00-entry-point.md`; manter os caminhos de 03, 04 e 05 antigos nesta fase.
- [ ] T010 [P] [US1] Atualizar `src/test/archetype-post-generate/ReadmeConsistencyTest.groovy` para `prompt00Path` novo; manter `prompt04` antigo nesta fase.
- [ ] T011 [P] [US1] Atualizar `src/test/archetype-post-generate/FileRenameContractTest.groovy` para o caminho novo do índice `01-entrada-inicializacao/01-mapa-dos-arquivos-e-ordem-de-leitura.md`.

### Implementation for User Story 1

- [ ] T012 [US1] Renomear `src/main/resources/llm-maven-flags-inference/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md` para `00-entry-point.md` com `Rename-Item`, após confirmar que o destino não existe.
- [ ] T013 [US1] Renomear `src/main/resources/llm-maven-flags-inference/01-entrada-inicializacao/indice-01-mapa-dos-arquivos-e-ordem-de-leitura.md` para `01-mapa-dos-arquivos-e-ordem-de-leitura.md` com `Rename-Item`, após confirmar que o destino não existe.
- [ ] T014 [P] [US1] Atualizar em `src/main/resources/llm-maven-flags-inference/01-entrada-inicializacao/00-entry-point.md` as referências `prompt-00` dos rodapés do dicionário e o caminho do índice na linha 620.
- [ ] T015 [P] [US1] Atualizar em `src/main/resources/llm-maven-flags-inference/01-entrada-inicializacao/01-mapa-dos-arquivos-e-ordem-de-leitura.md` as linhas 11 e 12 da tabela e a ordem global `prompt-00`.
- [ ] T016 [P] [US1] Atualizar em `src/main/resources/llm-maven-flags-inference/02-operacao-procedimento-do-agente/prompt-01-papel-e-restricoes-do-agente.md` a linha 26 de `prompt-00` para `00-entry-point`.
- [ ] T017 [P] [US1] Atualizar em `src/main/resources/llm-maven-flags-inference/02-operacao-procedimento-do-agente/prompt-03-regras-obrigatorias-de-geracao.md` a linha 68 de `prompt-00` para `00-entry-point`.
- [ ] T018 [P] [US1] Atualizar em `src/main/resources/llm-maven-flags-inference/03-derivacao-regras-de-formacao-do-contrato/01-como-derivar-a-entidade-do-contrato.md` a linha 17 de `prompt-00` para `00-entry-point`.
- [ ] T019 [P] [US1] Atualizar em `src/main/resources/llm-maven-flags-inference/03-derivacao-regras-de-formacao-do-contrato/04-como-aplicar-precedencia-e-validacao.md` as linhas 35 e 66 de `prompt-00` para `00-entry-point`.
- [ ] T020 [P] [US1] Atualizar em `README.md` as linhas 604, 845, 995, 2067, 2099, 2111, 2151, 2152, 2207, 2513, 2567, 2639 e 2704 para o novo caminho/token da entrada e do índice.
- [ ] T021 [P] [US1] Atualizar em `src/main/resources/utils/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md` a linha 7 para apontar para `llm-maven-flags-inference/01-entrada-inicializacao/00-entry-point.md`; não renomear o redirect.
- [ ] T022 [US1] Executar a validação da Fase 1: listar `src/main/resources/llm-maven-flags-inference/01-entrada-inicializacao/` e `02-operacao-procedimento-do-agente/`; confirmar os nomes das subpastas `01-entrada-inicializacao` e `02-operacao-procedimento-do-agente` e a ordem/papel; rodar a varredura em `README.md`, `src/main/resources/llm-maven-flags-inference/` e `src/test/` excluindo `src/main/resources/utils/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md`, `ProcedureEntryRenameContractTest.groovy` e `PathRenameContractTest.groovy`; executar `mvn -B clean test` em `pom.xml`.
- [ ] T023 [US1] Comparar o SHA-256 e o diff de `00-entry-point.md` e `01-mapa-dos-arquivos-e-ordem-de-leitura.md` com o baseline em `src/main/resources/llm-maven-flags-inference/01-entrada-inicializacao/`; confirmar que só tokens de referência mudaram.

**Checkpoint**: User Story 1 funcional e testável isoladamente; 02 ainda com os cinco nomes antigos.

---

## Phase 4: User Story 2 - Fase 2 — Subpasta de operação e procedimento do agente (Priority: P2)

**Goal**: Renomear os cinco arquivos de `02-operacao-procedimento-do-agente` e atualizar todas as referências a eles, concluindo a nomenclatura numeral-first.

**Independent Test**: listar `src/main/resources/llm-maven-flags-inference/02-operacao-procedimento-do-agente/`, conferir exatamente os cinco nomes novos, confirmar que não há `prompt-*`, que o procedimento mantém 19/13/6/4, que o índice lista os 19 caminhos novos e que as suítes passam.

### Tests for User Story 2

- [ ] T024 [P] [US2] Atualizar `src/test/archetype-post-generate/BuildArtifactsTest.groovy` para os cinco caminhos novos de 02 e a lista final dos 19 caminhos.
- [ ] T025 [P] [US2] Atualizar `src/test/archetype-post-generate/PathRenameContractTest.groovy` para os cinco caminhos novos de 02.
- [ ] T026 [P] [US2] Atualizar `src/test/archetype-post-generate/DestinationContractTest.groovy` para os caminhos novos de `03-regras-obrigatorias-de-geracao.md`, `04-fluxo-de-execucao-e-verificacao.md` e `05-entregaveis-e-relatorios.md`, incluindo rótulos.
- [ ] T027 [P] [US2] Atualizar `src/test/archetype-post-generate/CatalogSyncContractTest.groovy` para `02-operacao-procedimento-do-agente/03-regras-obrigatorias-de-geracao.md` e o rótulo correspondente.
- [ ] T028 [P] [US2] Atualizar `src/test/archetype-post-generate/ReadmeConsistencyTest.groovy` para `02-operacao-procedimento-do-agente/04-fluxo-de-execucao-e-verificacao.md` e o rótulo correspondente.

### Implementation for User Story 2

- [ ] T029 [US2] Renomear os cinco arquivos de `src/main/resources/llm-maven-flags-inference/02-operacao-procedimento-do-agente/` com `Rename-Item`, em ordem crescente, após confirmar cada destino: `prompt-01`→`01-papel-e-restricoes-do-agente.md`, `prompt-02`→`02-como-localizar-e-ler-o-archetype.md`, `prompt-03`→`03-regras-obrigatorias-de-geracao.md`, `prompt-04`→`04-fluxo-de-execucao-e-verificacao.md` e `prompt-05`→`05-entregaveis-e-relatorios.md`.
- [ ] T030 [P] [US2] Atualizar em `src/main/resources/llm-maven-flags-inference/01-entrada-inicializacao/01-mapa-dos-arquivos-e-ordem-de-leitura.md` as linhas 13 a 17, 33, 46, 47, 49, 50 e 51 para os novos caminhos/tokens de 02.
- [ ] T031 [P] [US2] Atualizar em `src/main/resources/llm-maven-flags-inference/01-entrada-inicializacao/00-entry-point.md` o registro de paradas (linhas 168, 170, 171, 172, 177, 178, 179 e 180) e os caminhos completos (linhas 710 a 714) para os novos nomes de 02.
- [ ] T032 [P] [US2] Atualizar em `src/main/resources/llm-maven-flags-inference/02-operacao-procedimento-do-agente/04-fluxo-de-execucao-e-verificacao.md` a linha 64 de `prompt-03` para `03-regras-obrigatorias-de-geracao`.
- [ ] T033 [P] [US2] Atualizar em `README.md` as linhas 804 e 991 para `04-fluxo-de-execucao-e-verificacao`, `05-entregaveis-e-relatorios` e `03-regras-obrigatorias-de-geracao`.
- [ ] T034 [US2] Executar a validação da Fase 2: listar `src/main/resources/llm-maven-flags-inference/02-operacao-procedimento-do-agente/`; contar 19/13/6/4; confirmar os nomes das subpastas `01-entrada-inicializacao` e `02-operacao-procedimento-do-agente` e a ordem/papel; rodar a varredura em `README.md`, `src/main/resources/llm-maven-flags-inference/` e `src/test/` excluindo `src/main/resources/utils/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md`, `ProcedureEntryRenameContractTest.groovy` e `PathRenameContractTest.groovy`; executar `mvn -B clean test` em `pom.xml`.
- [ ] T035 [US2] Executar `mvn clean install` e `mvn -B clean integration-test` em `pom.xml`.
- [ ] T036 [US2] Comparar o SHA-256 e o diff dos cinco arquivos de `src/main/resources/llm-maven-flags-inference/02-operacao-procedimento-do-agente/` com o baseline; confirmar que só tokens de referência mudaram.

**Checkpoint**: User Stories 1 e 2 completas; os sete nomes novos, 19/13/6/4 e zero referências antigas no corpus operacional.

---

## Phase 5: Polish & Cross-Cutting Concerns

**Purpose**: Validação final, documentação e verificação do artefato.

- [ ] T037 [P] Executar a varredura final em `README.md`, `src/main/resources/llm-maven-flags-inference/` e `src/test/` para `prompt-0[0-5]` e `indice-01`, excluindo o redirect legado `src/main/resources/utils/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md` e as asserções negativas; confirmar 0 ocorrências.
- [ ] T038 [P] Confirmar que `src/main/resources/META-INF/`, `src/main/resources/archetype-resources/`, `pom.xml` e as specs históricas em `specs/` não foram alterados; confirmar os nomes das subpastas `01-entrada-inicializacao` e `02-operacao-procedimento-do-agente` e a ordem de leitura; revisar o diff dos arquivos planejados.
- [ ] T039 Executar os cenários de validação de `specs/010-rename-procedure-entry-files/quickstart.md` e registrar os resultados.
- [ ] T040 Executar `mvn -B test "-Deda.verify.installed.jar=true"` em `pom.xml`, após o `mvn clean install` da T035; confirmar o JAR instalado com os novos nomes e o redirect legado.
- [ ] T041 Registrar o relatório final da feature no relatório de conclusão da implementação, referenciando `specs/010-rename-procedure-entry-files/quickstart.md`: 7/7 nomes, 19/13/6/4, 0 referências antigas, suítes aprovadas, verificação opt-in e riscos remanescentes.

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: sem dependência; começa imediatamente.
- **Foundational (Phase 2)**: depende de Setup; bloqueia todas as fases de user story.
- **User Story 1 (Phase 3)**: depende de Foundational; é o MVP; não depende de US2.
- **User Story 2 (Phase 4)**: depende de Foundational; a renomeação dos cinco arquivos é independente, mas a conferência final de zero nomes antigos depende de US1.
- **Polish (Phase 5)**: depende de US1 e US2 completas.

### User Story Dependencies

- **User Story 1 (P1)**: pode começar após Foundational; não depende de outra story.
- **User Story 2 (P2)**: pode começar após Foundational para a própria renomeação; a varredura final de zero nomes antigos depende de US1.

### Within Each User Story

- Atualizar as suítes de teste antes da renomeação; elas devem falhar até a implementação.
- Renomear os arquivos antes de atualizar as referências.
- Atualizar referências antes da validação.
- Validar a fase antes de avançar.
- Não alterar o conteúdo normativo; somente tokens de referência.

### Parallel Opportunities

- T002 e T003 podem rodar em paralelo com T001 (leitura).
- T005 é sequencial e depende da suíte criada em T004; não executar em paralelo.
- T006 a T011 são suítes diferentes; podem rodar em paralelo.
- T014 a T021 são arquivos diferentes; podem rodar em paralelo após T012 e T013.
- T024 a T028 são suítes diferentes; podem rodar em paralelo.
- T030 a T033 são arquivos diferentes; podem rodar em paralelo após T029.
- T037 e T038 são verificações independentes; podem rodar em paralelo.

---

## Parallel Example: User Story 1

```text
# Atualizar as suítes de US1 em paralelo:
Task: "T006 [P] [US1] BuildArtifactsTest.groovy"
Task: "T007 [P] [US1] PathRenameContractTest.groovy"
Task: "T008 [P] [US1] HardStopContractTest.groovy"
Task: "T009 [P] [US1] DestinationContractTest.groovy"
Task: "T010 [P] [US1] ReadmeConsistencyTest.groovy"
Task: "T011 [P] [US1] FileRenameContractTest.groovy"

# Após T012 e T013, atualizar as referências em paralelo:
Task: "T014 [P] [US1] 00-entry-point.md"
Task: "T015 [P] [US1] 01-mapa-dos-arquivos-e-ordem-de-leitura.md"
Task: "T016 [P] [US1] prompt-01-papel-e-restricoes-do-agente.md"
Task: "T017 [P] [US1] prompt-03-regras-obrigatorias-de-geracao.md"
Task: "T018 [P] [US1] 03/01-como-derivar-a-entidade-do-contrato.md"
Task: "T019 [P] [US1] 03/04-como-aplicar-precedencia-e-validacao.md"
Task: "T020 [P] [US1] README.md"
Task: "T021 [P] [US1] redirect legado"
```

## Parallel Example: User Story 2

```text
# Atualizar as suítes de US2 em paralelo:
Task: "T024 [P] [US2] BuildArtifactsTest.groovy"
Task: "T025 [P] [US2] PathRenameContractTest.groovy"
Task: "T026 [P] [US2] DestinationContractTest.groovy"
Task: "T027 [P] [US2] CatalogSyncContractTest.groovy"
Task: "T028 [P] [US2] ReadmeConsistencyTest.groovy"

# Após T029, atualizar as referências em paralelo:
Task: "T030 [P] [US2] 01-mapa-dos-arquivos-e-ordem-de-leitura.md"
Task: "T031 [P] [US2] 00-entry-point.md"
Task: "T032 [P] [US2] 04-fluxo-de-execucao-e-verificacao.md"
Task: "T033 [P] [US2] README.md"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Completar Phase 1: Setup.
2. Completar Phase 2: Foundational.
3. Completar Phase 3: User Story 1.
4. **STOP and VALIDATE**: testar US1 isoladamente; 01 com dois nomes novos, 02 com cinco nomes antigos, índice e redirect corretos.
5. Demonstrar se estiver pronto.

### Incremental Delivery

1. Setup + Foundational → base pronta.
2. User Story 1 → testar isoladamente → demonstrar (MVP).
3. User Story 2 → testar isoladamente → `mvn clean install` e `integration-test` → demonstrar.
4. Polish → varredura final e verificação opt-in do JAR.

### Parallel Team Strategy

- Com mais de uma pessoa: após Foundational, uma pessoa atualiza as suítes de US1 e outra as de US2; as edições de referências podem ser divididas por arquivo.
- A varredura final de zero nomes antigos de US2 só começa após US1 concluída.
- A renomeação dos arquivos de cada subpasta é sequencial dentro da subpasta; não paralelizar `Rename-Item` no mesmo diretório.

---

## Notes

- [P] tasks = arquivos diferentes, sem dependência de tarefa incompleta.
- [US1]/[US2] aparecem apenas nas fases de user story.
- Tarefas de teste são obrigatórias porque o plano pede atualização das suítes e uma nova suíte de contrato.
- O redirect legado é a única exceção que mantém um nome antigo.
- Não editar `specs/` históricas, `target/`, `graphify-out/`, catálogo, `archetype-resources/` ou `pom.xml`.
- Não criar nem remover arquivos do procedimento.
- Verificar o checkpoint de cada fase antes de avançar.
- Evitar tarefas vagas, conflito no mesmo arquivo e dependência cruzada que quebre a independência.
