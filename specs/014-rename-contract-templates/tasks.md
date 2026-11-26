# Tasks: Renomear a Subpasta 04 e os Nomes dos Arquivos de Templates de Contrato

**Input**: Design documents from `/specs/014-rename-contract-templates/`

**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/, quickstart.md

**Tests**: Atualização das suítes existentes nas Fases 1 e 2; TDD não solicitado. A suíte nova `ContractTemplateRenameContractTest.groovy` é prevista pelo plan.md e é criada na fase Foundational.

**Organization**: Tarefas agrupadas por user story para permitir implementação e teste independentes de cada fase da feature.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: pode rodar em paralelo (arquivos diferentes, sem dependência de tarefa incompleta)
- **[Story]**: user story da tarefa (US1, US2); ausente em Setup, Foundational e Polish
- Caminhos de arquivo exatos em todas as tarefas

## Path Conventions

- Módulo único na raiz do repositório: `src/main/resources/`, `src/test/`, `specs/`
- Procedimento empacotado: `src/main/resources/llm-maven-flags-inference/`
- Suítes de verificação: `src/test/archetype-post-generate/`
- Runner das suítes: `src/test/groovy/run-archetype-post-generate-tests.groovy`

## Mapa de fases

| Fase da feature | Nome | User Story | Prioridade | Depende de | Testes | Fase da task |
|---|---|---|---|---|---|---|
| Fase 1 | Subpasta `04-templates-de-contrato` | US1 | P1 | — | Atualizar suítes existentes | Phase 3 |
| Fase 2 | Arquivos sem "exemplo" | US2 | P2 | Fase 1 (caminho da pasta) | Atualizar suítes existentes; varredura final de nomes antigos | Phase 4 |

Pré-requisitos: Phase 1 (Setup), Phase 2 (Foundational), Phase 5 (Polish).

## Como executar por fase da feature

- Fase 1:
  /speckit.implement Implemente somente a Phase 3: User Story 1 - Fase 1 — Subpasta `04-templates-de-contrato`. Não execute as demais.

- Fase 2:
  /speckit.implement Implemente somente a Phase 4: User Story 2 - Fase 2 — Arquivos sem "exemplo". Requer Fase 1. Não execute as demais.

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Confirmar a linha de base e as pré-condições da renomeação, sem alterar arquivos.

- [ ] T001 Confirmar a linha de base do procedimento em `src/main/resources/llm-maven-flags-inference/`: 4 subpastas (`01-inicializacao`, `02-procedimento-do-agente`, `03-regras-de-formacao-do-contrato`, `04-referencia-templates-de-contrato`), 19 arquivos, 13 `.md`, 6 `.json` e 6 arquivos na subpasta 04; registrar as listagens por arquivo de `src/main/resources/llm-maven-flags-inference/01-inicializacao`, `02-procedimento-do-agente` e `03-regras-de-formacao-do-contrato` em `C:\Users\edufi\AppData\Local\Temp\opencode\012-listas-01-03-antes.txt`
- [ ] T002 [P] Registrar `sha256_before` dos seis JSON de `src/main/resources/llm-maven-flags-inference/04-referencia-templates-de-contrato/` em `C:\Users\edufi\AppData\Local\Temp\opencode\012-hashes-antes.txt` (fora do repositório), conforme `data-model.md` E2.sha256_before
- [ ] T003 [P] Confirmar que o destino não existe: `src/main/resources/llm-maven-flags-inference/04-templates-de-contrato` e os cinco nomes finais (`02-contrato-plano.json`, `03-contrato-com-objetos-aninhados.json`, `04-contrato-com-enums.json`, `05-contrato-com-tipos-do-projeto.json`, `06-contrato-com-attributes-explicito.json`), conforme `data-model.md` E1.new_name e E2.new_name

**Checkpoint**: linha de base e hashes registrados; nenhuma colisão de destino.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Suíte de contrato phase-aware e refatoração da suíte de nomes, necessárias para manter as duas fases verdes.

**⚠️ CRITICAL**: nenhuma tarefa de user story pode começar antes desta fase.

- [x] T004 Criar `src/test/archetype-post-generate/ContractTemplateRenameContractTest.groovy` com detecção `templatePhase` (0 = pasta `04-referencia-templates-de-contrato` e nomes `0X-exemplo-...`; 1 = pasta `04-templates-de-contrato` e nomes `0X-exemplo-...`; 2 = nomes `0X-contrato-...`), validando `data-model.md` E1, E2 e E3: nomes, numeral e extensão, 19/13/6/4, as 19 linhas do índice (posições 14 a 19), âncora e título do README, as seis suítes e o redirect legado; a suíte deve passar em S0, S1 e S2
- [x] T005 Registrar `src/test/archetype-post-generate/ContractTemplateRenameContractTest.groovy` na lista `suites` de `src/test/groovy/run-archetype-post-generate-tests.groovy`, logo após `ContractRuleRenameContractTest.groovy`
- [x] T006 Refatorar `src/test/archetype-post-generate/FileRenameContractTest.groovy` conforme `research.md` D7: substituir `p2Old`/`p2New` por `p2LegacyOld` (`template-0X-...`), `p2Current` (`0X-exemplo-...`) e `p2New` (`0X-contrato-...`), trocar a detecção `phase2 = 01-schema-do-contrato.json` por `templatePhase` 0/1/2 e ajustar as checagens 2.2, 2.4, 3.2, 3.4, 4.1, 5.1 e 6.1 para a fase detectada, citando `data-model.md` E3.phase
- [ ] T007 Rodar `mvn -B test` e confirmar que a suíte nova e as suítes refatoradas passam no estado S0 (baseline verde)

**Checkpoint**: foundation pronta — as duas fases podem começar na ordem de prioridade.

---

## Phase 3: User Story 1 - Fase 1 — Subpasta `04-templates-de-contrato` (Priority: P1) 🎯 MVP

**Goal**: renomear a subpasta 04 para `04-templates-de-contrato` e atualizar as referências operacionais ao caminho, mantendo os seis arquivos com os nomes atuais.

**Independent Test**: listar `src/main/resources/llm-maven-flags-inference/`, conferir a pasta `04-templates-de-contrato` e a ausência de `04-referencia-templates-de-contrato`, os seis arquivos com os nomes atuais dentro dela, o mapa e a porta de entrada com os caminhos novos, o README com a âncora e o título novos, as suítes sem referência operacional ao nome antigo e `mvn -B test` verde.

### Implementation for User Story 1

- [x] T008 [US1] Renomear `src/main/resources/llm-maven-flags-inference/04-referencia-templates-de-contrato` para `src/main/resources/llm-maven-flags-inference/04-templates-de-contrato` com `Rename-Item`, sem copiar e apagar, conforme `data-model.md` E1.new_name
- [x] T009 [P] [US1] Atualizar nas linhas 24 a 29 de `src/main/resources/llm-maven-flags-inference/01-inicializacao/01-mapa-dos-arquivos-e-ordem-de-leitura.md` o token do caminho da pasta para `llm-maven-flags-inference/04-templates-de-contrato/`, preservando ordem, papéis e nomes dos arquivos
- [x] T010 [P] [US1] Atualizar nas linhas 721 a 726 de `src/main/resources/llm-maven-flags-inference/01-inicializacao/00-entry-point.md` o token do caminho da pasta para `llm-maven-flags-inference/04-templates-de-contrato/`, preservando os nomes dos arquivos
- [x] T011 [US1] Atualizar `README.md`: linha 55 (texto e `href` do índice), 610 (lista de composição), 652 (tabela de artefatos), 821 (`id`/`name` da âncora), 822 (título `### 04-templates-de-contrato`) e 2178 (cabeçalho da seção de templates), usando a âncora `llm-maven-flags-inference-04-templates-de-contrato` (`research.md` D5)
- [x] T012 [P] [US1] Atualizar a lista `procedureCategories` de `src/test/archetype-post-generate/BuildArtifactsTest.groovy` (linha 85) para `04-templates-de-contrato`
- [x] T013 [P] [US1] Atualizar a lista `categories` de `src/test/archetype-post-generate/PathRenameContractTest.groovy` (linha 44) e a checagem 4.6 da subseção do README para `04-templates-de-contrato`
- [x] T014 [P] [US1] Atualizar o comentário (linha 11), o diretório (linha 49) e a mensagem (linha 60) de `src/test/archetype-post-generate/ProcedureContractTest.groovy` para `04-templates-de-contrato`
- [x] T015 [P] [US1] Atualizar a lista de caminhos esperados (linha 89) de `src/test/archetype-post-generate/ContractRuleRenameContractTest.groovy` para a pasta `04-templates-de-contrato`
- [x] T016 [P] [US1] Atualizar a lista das quatro subpastas (linha 81) de `src/test/archetype-post-generate/ProcedureEntryRenameContractTest.groovy` para `04-templates-de-contrato`
- [ ] T017 [US1] Rodar `mvn -B test` e confirmar o estado S1: pasta nova presente, pasta antiga ausente, seis arquivos com os nomes atuais e todas as suítes verdes

**Checkpoint**: User Story 1 funcional e testável isoladamente (estado S1).

---

## Phase 4: User Story 2 - Fase 2 — Arquivos sem "exemplo" (Priority: P2)

**Goal**: remover a palavra `exemplo` do corpo do nome dos arquivos 2 a 6 e atualizar as referências operacionais aos cinco nomes.

**Independent Test**: listar `src/main/resources/llm-maven-flags-inference/04-templates-de-contrato/`, conferir os cinco nomes novos e a ausência dos cinco antigos, `01-schema-do-contrato.json` inalterado, o conteúdo dos seis JSON idêntico ao inicial, as referências operacionais atualizadas, 19/13/6/4 preservados e `mvn -B test` verde.

### Implementation for User Story 2

- [x] T018 [US2] Renomear, em ordem crescente de numeral e com pré-checagem de colisão, em `src/main/resources/llm-maven-flags-inference/04-templates-de-contrato/`, conforme `data-model.md` E2.new_name: `02-exemplo-contrato-plano.json` → `02-contrato-plano.json`, `03-exemplo-contrato-com-objetos-aninhados.json` → `03-contrato-com-objetos-aninhados.json`, `04-exemplo-contrato-com-enums.json` → `04-contrato-com-enums.json`, `05-exemplo-contrato-com-tipos-do-projeto.json` → `05-contrato-com-tipos-do-projeto.json` e `06-exemplo-contrato-com-attributes-explicito.json` → `06-contrato-com-attributes-explicito.json`
- [x] T019 [P] [US2] Atualizar nas linhas 25 a 29 de `src/main/resources/llm-maven-flags-inference/01-inicializacao/01-mapa-dos-arquivos-e-ordem-de-leitura.md` os cinco nomes de arquivo para `02-contrato-plano.json` a `06-contrato-com-attributes-explicito.json`, preservando as posições 15 a 19 e os papéis
- [x] T020 [P] [US2] Atualizar nas linhas 722 a 726 de `src/main/resources/llm-maven-flags-inference/01-inicializacao/00-entry-point.md` os cinco caminhos completos com os nomes novos, mantendo o caminho da pasta `04-templates-de-contrato/`
- [x] T021 [P] [US2] Atualizar as linhas 2183 a 2187 de `README.md` (tabela de templates) com os cinco nomes novos, mantendo a descrição do papel de cada linha e a linha 2182 (`01-schema-do-contrato.json`) intacta
- [x] T022 [P] [US2] Atualizar a lista `procedureFiles` (linhas 102 a 107) de `src/test/archetype-post-generate/BuildArtifactsTest.groovy` com os cinco caminhos novos
- [x] T023 [P] [US2] Atualizar a lista `procedureFiles` (linhas 61 a 66) de `src/test/archetype-post-generate/PathRenameContractTest.groovy` com os cinco caminhos novos
- [x] T024 [P] [US2] Atualizar o arquivo de exemplo de tipos aninhados (linha 199) de `src/test/archetype-post-generate/ProcedureContractTest.groovy` para `03-contrato-com-objetos-aninhados.json`
- [x] T025 [P] [US2] Atualizar a lista `p04` (linhas 77 a 84) de `src/test/archetype-post-generate/ContractRuleRenameContractTest.groovy` com os cinco nomes novos, preservando a ordem e os papéis
- [x] T026 [US2] Varredura de nomes antigos: buscar `04-referencia-templates-de-contrato` e os cinco nomes `0X-exemplo-...` no corpus operacional (`README.md`, `src/main/resources/llm-maven-flags-inference/**`, `src/test/archetype-post-generate/*.groovy`) e confirmar que só restam asserções negativas e o caminho do redirect legado
- [x] T027 [US2] Rodar `mvn -B test` e confirmar o estado S2: cinco nomes novos, cinco antigos ausentes, índice com os 19 caminhos finais e todas as suítes verdes

**Checkpoint**: User Stories 1 e 2 completas e independentemente verificáveis (estado S2).

---

## Phase 5: Polish & Cross-Cutting Concerns

**Purpose**: Provar os invariantes, reconstruir o artefato e fechar a validação.

- [x] T028 Confirmar `sha256_after` igual ao `sha256_before` dos seis JSON, comparando `C:\Users\edufi\AppData\Local\Temp\opencode\012-hashes-antes.txt` com o hash recalculado (diferenças apenas de caminho; nenhuma de hash), conforme `data-model.md` INV-06
- [x] T029 Rodar `mvn clean install` e confirmar que `target/classes/llm-maven-flags-inference/` passa a expor os 19 arquivos com a pasta e os nomes novos
- [x] T030 [P] Rodar `mvn -B clean integration-test` e confirmar as fixtures de integração verdes
- [x] T031 [P] Rodar `mvn -B clean install "-Deda.verify.installed.jar=true"` (opt-in, não é gate) e confirmar o JAR instalado com os nomes novos
- [x] T032 Validar os cenários de `specs/014-rename-contract-templates/quickstart.md` de ponta a ponta, incluindo as 19 linhas do mapa e a âncora `llm-maven-flags-inference-04-templates-de-contrato` do README
- [x] T033 Confirmar as contagens finais (19 arquivos, 13 `.md`, 6 `.json`, 4 subpastas, 6 na subpasta 04) e a ausência de nomes antigos fora das exceções permitidas (`data-model.md` INV-07, INV-09, INV-11), comparar as listagens de `01-inicializacao`, `02-procedimento-do-agente` e `03-regras-de-formacao-do-contrato` com `012-listas-01-03-antes.txt` (0 diferenças; FR-021/SC-008) e confirmar `01-schema-do-contrato.json` com o nome atual

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: sem dependência — pode começar imediatamente.
- **Foundational (Phase 2)**: depende de Setup; bloqueia todas as fases de user story.
- **User Stories (Phase 3+)**: dependem de Foundational.
  - US1 (Fase 1) e US2 (Fase 2) não devem rodar em paralelo: US2 depende da pasta criada por US1.
- **Polish (Phase 5)**: depende de US1 e US2 completas.

### User Story Dependencies

| User Story | Fase da feature | Depende de | Testes |
|---|---|---|---|
| US1 (P1) | Fase 1 — Subpasta `04-templates-de-contrato` | Foundational | Atualizar suítes existentes |
| US2 (P2) | Fase 2 — Arquivos sem "exemplo" | Foundational + US1 (caminho da pasta) | Atualizar suítes existentes; varredura final |

### Within Each User Story

- Renomeação primeiro; atualização de referências e de suítes depois.
- Referências de documento e de suíte em arquivos diferentes podem rodar em paralelo dentro da fase.
- A fase termina com `mvn -B test` verde antes de avançar.

### Parallel Opportunities

- T002 e T003 no Setup.
- T004, T005 e T006 tocam arquivos diferentes, mas T005 depende de T004 e T006 depende das listas de `research.md` D7; executar na ordem indicada.
- Dentro da US1: T009, T010, T012, T013, T014, T015 e T016 em paralelo após T008; T011 em paralelo com as suítes.
- Dentro da US2: T019, T020, T021, T022, T023, T024 e T025 em paralelo após T018.
- Polish: T030 e T031 em paralelo após T029.

---

## Parallel Example: User Story 1

```text
Task: "Atualizar mapa dos arquivos (linhas 24 a 29) em src/main/resources/llm-maven-flags-inference/01-inicializacao/01-mapa-dos-arquivos-e-ordem-de-leitura.md"
Task: "Atualizar porta de entrada (linhas 721 a 726) em src/main/resources/llm-maven-flags-inference/01-inicializacao/00-entry-point.md"
Task: "Atualizar README.md (âncora, título e caminhos)"
Task: "Atualizar suítes BuildArtifactsTest, PathRenameContractTest, ProcedureContractTest, ContractRuleRenameContractTest e ProcedureEntryRenameContractTest"
```

## Parallel Example: User Story 2

```text
Task: "Atualizar mapa dos arquivos (linhas 25 a 29)"
Task: "Atualizar porta de entrada (linhas 722 a 726)"
Task: "Atualizar tabela de templates do README (linhas 2183 a 2187)"
Task: "Atualizar listas de nomes nas suítes BuildArtifactsTest, PathRenameContractTest, ProcedureContractTest e ContractRuleRenameContractTest"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Completar Phase 1 (Setup).
2. Completar Phase 2 (Foundational).
3. Completar Phase 3 (US1 — pasta renomeada).
4. **PARAR e VALIDAR**: `mvn -B test` verde no estado S1.
5. Demonstrar a pasta com o nome novo e as referências de pasta atualizadas.

### Incremental Delivery

1. Setup + Foundational → fundação pronta.
2. US1 → validar → estado S1 (MVP).
3. US2 → validar → estado S2.
4. Polish → hashes, `mvn clean install`, integração, quickstart.

---

## Notes

- [P] = arquivos diferentes, sem dependência de tarefa incompleta.
- Caminhos de linha citados vêm de `contracts/rename-manifest.md` (inventário) e `data-model.md`.
- Não editar `target/`, `graphify-out/`, specs históricas nem o redirect legado.
- Não alterar o conteúdo dos seis JSON: `data-model.md` INV-06.
- Cada fase termina com `mvn -B test` verde; a fase seguinte só começa depois.

---

## Phase 6: Convergence

- [x] T034 Atualizar o inventário do procedimento para 13 linhas, incluindo `01-inicializacao/00-entry-point.md:220`, em `specs/014-rename-contract-templates/contracts/rename-manifest.md:48` e `specs/014-rename-contract-templates/research.md:33` per plan: fundação compartilhada (partial)
- [x] T035 Acrescentar a referência `01-inicializacao/00-entry-point.md:220` na tabela E3 de `specs/014-rename-contract-templates/data-model.md:57` per plan: fundação compartilhada (partial)
- [x] T036 Substituir a comparação por `012-hashes-antes.txt` em `specs/014-rename-contract-templates/quickstart.md:81-83` pela verificação por blob do git per plan: validação da Fase 2 (partial)
