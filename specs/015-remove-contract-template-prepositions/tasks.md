# Tasks: Remover as Preposições "do" e "com" dos Nomes dos Templates de Contrato

**Input**: Design documents from `/specs/015-remove-contract-template-prepositions/`

**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/, quickstart.md

**Tests**: Nenhuma suíte nova. A spec exige apenas atualizar as suítes de verificação existentes (FR-020), portanto não há fase de TDD; as atualizações de teste fazem parte da implementação de cada história.

**Organization**: Tarefas agrupadas por user story para permitir implementação e teste independentes.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Pode rodar em paralelo (arquivos diferentes, sem dependência de tarefa incompleta)
- **[Story]**: User story da tarefa (US1, US2)
- Caminhos exatos em todas as descrições

## Path Conventions

- Repositório único: `src/`, `specs/` na raiz.
- Procedimento: `src/main/resources/llm-maven-flags-inference/`.
- Suítes: `src/test/archetype-post-generate/`.

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Confirmar a linha de base e os pré-requisitos da renomeação.

- [ ] T001 Registrar o SHA-256 dos seis JSON de `src/main/resources/llm-maven-flags-inference/04-templates-de-contrato/` em um arquivo temporário fora do repositório, conforme `quickstart.md` §2
- [ ] T002 Confirmar a linha de base: 19 arquivos, 13 `.md`, 6 `.json`, 4 subpastas e 6 arquivos na subpasta 04; confirmar que os cinco nomes de destino (`01-schema-contrato.json`, `03-contrato-objetos-aninhados.json`, `04-contrato-enums.json`, `05-contrato-tipos-do-projeto.json`, `06-contrato-attributes-explicito.json`) não existem em `src/main/resources/llm-maven-flags-inference/04-templates-de-contrato/` (VR-01)
- [ ] T003 Conferir que `specs/015-remove-contract-template-prepositions/contracts/rename-manifest.md` cobre as 5 renomeações, o arquivo 02 preservado e as exceções, antes de qualquer edição

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Preparar as suítes phase-aware para reconhecer os estados intermediários e o estado final. Nenhuma renomeação pode começar antes desta fase.

**⚠️ CRITICAL**: Nenhuma user story pode começar até esta fase estar completa.

- [ ] T004 Estender `src/test/archetype-post-generate/ContractTemplateRenameContractTest.groovy` com `p3New` e `p3RenamedCurrent` (`01-schema-do-contrato.json` e os quatro `0X-contrato-com-...`), detecção independente de `schemaRenamed` (presença de `01-schema-contrato.json`) e `othersRenamed` (presença de `03-contrato-objetos-aninhados.json`); compor `expected` para os quatro estados; trocar a checagem 2.3 para usar `expected[0]`; aplicar `p3RenamedCurrent` nas checagens 2.4 e 7.1; manter as asserções negativas e o anti-drift das gerações anteriores
- [ ] T005 Estender `src/test/archetype-post-generate/FileRenameContractTest.groovy` com `p3New` e `p3RenamedCurrent`, a mesma detecção de dois estados, e compor `expected04` para os quatro estados: estado 1 = schema novo + arquivos 3 a 6 antigos; estado 2 = schema antigo + arquivos 3 a 6 novos; estado final = `p3New`; aplicar a composição em `expected04`, nas checagens 2.2/2.4, no índice 3.2/3.4, no corpus 4.2 e no README 6.1; preservar `p2LegacyOld`, `p2Current` e `p2New`
- [ ] T006 Rodar `mvn -B clean test` a partir de `pom.xml` na raiz e confirmar que as suítes de `src/test/archetype-post-generate/` permanecem verdes no estado 0 (baseline), sem renomeações aplicadas

**Checkpoint**: as suítes aceitam os estados 0, 1, 2 e final; a linha de base está verde.

---

## Phase 3: User Story 1 - Fase 1 — Remover "do" do nome do schema (Priority: P1) 🎯 MVP

**Goal**: Renomear `01-schema-do-contrato.json` para `01-schema-contrato.json` e atualizar as referências operacionais ao nome antigo.

**Independent Test**: listar a subpasta 04, conferir `01-schema-contrato.json` presente e `01-schema-do-contrato.json` ausente, o SHA-256 do schema idêntico ao inicial, as referências operacionais atualizadas e `mvn -B clean test` verde no estado 1.

### Implementation for User Story 1

- [ ] T007 [US1] Renomear `src/main/resources/llm-maven-flags-inference/04-templates-de-contrato/01-schema-do-contrato.json` para `01-schema-contrato.json` com `Rename-Item`, após checar colisão (VR-01) e preservar numeral `01` e extensão `.json` (VR-02)
- [ ] T008 [P] [US1] Atualizar a linha 24 de `src/main/resources/llm-maven-flags-inference/01-inicializacao/01-mapa-dos-arquivos-e-ordem-de-leitura.md` para `llm-maven-flags-inference/04-templates-de-contrato/01-schema-contrato.json`, mantendo papel, ordem 14 e obrigatoriedade
- [ ] T009 [P] [US1] Atualizar a linha 721 de `src/main/resources/llm-maven-flags-inference/01-inicializacao/00-entry-point.md` para `llm-maven-flags-inference/04-templates-de-contrato/01-schema-contrato.json`
- [ ] T010 [P] [US1] Atualizar a linha 2182 de `README.md` para citar `01-schema-contrato.json` na tabela de templates
- [ ] T011 [P] [US1] Atualizar a linha 102 de `src/test/archetype-post-generate/BuildArtifactsTest.groovy` para `04-templates-de-contrato/01-schema-contrato.json`
- [ ] T012 [P] [US1] Atualizar a linha 61 de `src/test/archetype-post-generate/PathRenameContractTest.groovy` para `04-templates-de-contrato/01-schema-contrato.json`
- [ ] T013 [P] [US1] Atualizar a linha 78 de `src/test/archetype-post-generate/ContractRuleRenameContractTest.groovy` para `01-schema-contrato.json`
- [ ] T014 [P] [US1] Atualizar as linhas 12, 139 e 148 de `src/test/archetype-post-generate/ProcedureContractTest.groovy` para `01-schema-contrato.json`
- [ ] T015 [US1] Verificar que o SHA-256 de `01-schema-contrato.json` é idêntico ao registrado em T001 (VR-03) e rodar `mvn -B clean test` no estado 1

**Checkpoint**: US1 completa e testável isoladamente; arquivos 02 a 06 ainda com os nomes antigos.

---

## Phase 4: User Story 2 - Fase 2 — Remover "com" dos nomes dos arquivos 3 a 6 (Priority: P2)

**Goal**: Renomear os arquivos 3 a 6 removendo apenas `com`, preservando `tipos-do-projeto` no arquivo 05, e atualizar as referências operacionais.

**Independent Test**: listar a subpasta 04, conferir os quatro nomes novos e a ausência dos quatro nomes antigos, `02-contrato-plano.json` e o arquivo 01 inalterados, o SHA-256 dos seis JSON idêntico ao inicial, as referências atualizadas e `mvn -B clean test` verde no estado final.

### Implementation for User Story 2

- [ ] T016 [P] [US2] Renomear `src/main/resources/llm-maven-flags-inference/04-templates-de-contrato/03-contrato-com-objetos-aninhados.json` para `03-contrato-objetos-aninhados.json`, checando colisão e preservando numeral `03` e extensão `.json`
- [ ] T017 [P] [US2] Renomear `src/main/resources/llm-maven-flags-inference/04-templates-de-contrato/04-contrato-com-enums.json` para `04-contrato-enums.json`, checando colisão e preservando numeral `04` e extensão `.json`
- [ ] T018 [P] [US2] Renomear `src/main/resources/llm-maven-flags-inference/04-templates-de-contrato/05-contrato-com-tipos-do-projeto.json` para `05-contrato-tipos-do-projeto.json`, removendo apenas `com` e preservando o trecho `tipos-do-projeto` (FR-013)
- [ ] T019 [P] [US2] Renomear `src/main/resources/llm-maven-flags-inference/04-templates-de-contrato/06-contrato-com-attributes-explicito.json` para `06-contrato-attributes-explicito.json`, checando colisão e preservando numeral `06` e extensão `.json`
- [ ] T020 [P] [US2] Atualizar as linhas 26–29 de `src/main/resources/llm-maven-flags-inference/01-inicializacao/01-mapa-dos-arquivos-e-ordem-de-leitura.md` para os quatro caminhos novos, mantendo as posições 16–19 e os papéis
- [ ] T021 [P] [US2] Atualizar as linhas 723–726 de `src/main/resources/llm-maven-flags-inference/01-inicializacao/00-entry-point.md` para os quatro caminhos novos
- [ ] T022 [P] [US2] Atualizar as linhas 2184–2187 de `README.md` para os quatro nomes novos na tabela de templates
- [ ] T023 [P] [US2] Atualizar as linhas 104–107 de `src/test/archetype-post-generate/BuildArtifactsTest.groovy` para os quatro caminhos novos
- [ ] T024 [P] [US2] Atualizar as linhas 63–66 de `src/test/archetype-post-generate/PathRenameContractTest.groovy` para os quatro caminhos novos
- [ ] T025 [P] [US2] Atualizar as linhas 80–83 de `src/test/archetype-post-generate/ContractRuleRenameContractTest.groovy` para os quatro nomes novos
- [ ] T026 [P] [US2] Atualizar a linha 199 de `src/test/archetype-post-generate/ProcedureContractTest.groovy` para `03-contrato-objetos-aninhados.json`
- [ ] T027 [US2] Verificar que o SHA-256 dos seis JSON é idêntico ao registrado em T001 (VR-03), que `02-contrato-plano.json` permanece (VR-05) e rodar `mvn -B clean test` no estado final

**Checkpoint**: US2 completa; o estado final tem os seis nomes sem as preposições-alvo.

---

## Phase 5: Polish & Cross-Cutting Concerns

**Purpose**: Validação final, varredura do corpus e confirmação do artefato.

- [ ] T028 Rodar `mvn -B clean test` a partir de `pom.xml` como gate final e confirmar todas as suítes de `src/test/archetype-post-generate/` verdes, incluindo `ContractTemplateRenameContractTest.groovy`, `FileRenameContractTest.groovy`, `BuildArtifactsTest.groovy`, `PathRenameContractTest.groovy`, `ContractRuleRenameContractTest.groovy` e `ProcedureContractTest.groovy`
- [ ] T029 Rodar `mvn clean install` e confirmar que `target/classes/llm-maven-flags-inference/04-templates-de-contrato/` contém os seis nomes novos e nenhum dos cinco nomes antigos (VR-11)
- [ ] T030 Rodar `mvn -B clean integration-test` a partir de `pom.xml` e confirmar as fixtures de integração
- [ ] T031 [P] Varrer o corpus operacional (`src/main/resources/llm-maven-flags-inference/01-inicializacao/`, `README.md` e `src/test/archetype-post-generate/`) e confirmar 0 referências operacionais aos cinco nomes antigos; aceitar apenas asserções negativas das suítes (VR-06)
- [ ] T032 [P] Executar as verificações de `specs/015-remove-contract-template-prepositions/quickstart.md` §5–§7 (hash, referências e contagens 19/13/6/4) e registrar a evidência
- [ ] T033 [P] Rodar `mvn -Deda.verify.installed.jar=true verify` a partir de `pom.xml` e confirmar o JAR instalado com os nomes novos; registrar como verificação opt-in, não como gate
- [ ] T034 Confirmar que `02-contrato-plano.json`, o nome da subpasta `04-templates-de-contrato`, as subpastas 01 a 03 e as specs históricas permanecem intocados (SC-008, FR-007, FR-011, FR-021, FR-022)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: sem dependências; começa imediatamente.
- **Foundational (Phase 2)**: depende de Setup; bloqueia US1 e US2.
- **User Story 1 (Phase 3)**: depende de Foundational; não depende de US2.
- **User Story 2 (Phase 4)**: depende de Foundational; não depende de US1 para renomear os arquivos, mas a execução sequencial P1 → P2 é o caminho recomendado.
- **Polish (Phase 5)**: depende de US1 e US2 concluídas.

### User Story Dependencies

- **US1 (P1)**: pode começar após Foundational; entrega o schema renomeado.
- **US2 (P2)**: pode começar após Foundational; entrega os arquivos 3 a 6 renomeados. As duas histórias não compartilham arquivos renomeados, mas ambas atualizam o mesmo índice, a mesma porta de entrada, o mesmo README e as mesmas suítes — por isso os lotes de referência são sequenciais, não paralelos entre histórias.

### Within Each User Story

- Renomear o arquivo antes de atualizar as referências a ele.
- Atualizar índice, porta de entrada, README e suítes antes de rodar a validação da história.
- A validação da história usa `mvn -B clean test` para remover cópias antigas de `target/classes`.

### Parallel Opportunities

- T004 e T005: arquivos diferentes, podem rodar em paralelo.
- T008–T014: documentos e suítes diferentes, podem rodar em paralelo após T007.
- T016–T019: quatro arquivos diferentes, podem rodar em paralelo.
- T020–T026: documentos e suítes diferentes, podem rodar em paralelo após as renomeações.
- T031–T033: verificações independentes no estado final.

---

## Parallel Example: User Story 1

```text
# Após T007, atualizar referências em paralelo:
Task: "T008 atualizar linha 24 de 01-mapa-dos-arquivos-e-ordem-de-leitura.md"
Task: "T009 atualizar linha 721 de 00-entry-point.md"
Task: "T010 atualizar linha 2182 de README.md"
Task: "T011 atualizar linha 102 de BuildArtifactsTest.groovy"
Task: "T012 atualizar linha 61 de PathRenameContractTest.groovy"
Task: "T013 atualizar linha 78 de ContractRuleRenameContractTest.groovy"
Task: "T014 atualizar linhas 12, 139 e 148 de ProcedureContractTest.groovy"
```

## Parallel Example: User Story 2

```text
# Renomeações independentes:
Task: "T016 renomear 03-contrato-com-objetos-aninhados.json"
Task: "T017 renomear 04-contrato-com-enums.json"
Task: "T018 renomear 05-contrato-com-tipos-do-projeto.json"
Task: "T019 renomear 06-contrato-com-attributes-explicito.json"

# Depois, referências em paralelo:
Task: "T020 atualizar linhas 26–29 do índice"
Task: "T021 atualizar linhas 723–726 da porta de entrada"
Task: "T022 atualizar linhas 2184–2187 do README"
Task: "T023 atualizar linhas 104–107 de BuildArtifactsTest.groovy"
Task: "T024 atualizar linhas 63–66 de PathRenameContractTest.groovy"
Task: "T025 atualizar linhas 80–83 de ContractRuleRenameContractTest.groovy"
Task: "T026 atualizar linha 199 de ProcedureContractTest.groovy"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Completar Phase 1 (T001–T003).
2. Completar Phase 2 (T004–T006) e confirmar o baseline verde.
3. Completar Phase 3 (T007–T015).
4. **PARAR e VALIDAR**: conferir o schema renomeado e as referências, rodar `mvn -B clean test`.
5. US1 é o MVP: um arquivo renomeado, conteúdo preservado e suítes verdes.

### Incremental Delivery

1. Setup + Foundational → suítes prontas para os estados intermediários.
2. US1 → validar isoladamente → o schema perde `do`.
3. US2 → validar isoladamente → os arquivos 3 a 6 perdem `com`.
4. Polish → build limpo, install, integração, varredura e verificação opt-in do JAR.

### Parallel Team Strategy

1. Time conclui Setup + Foundational junto.
2. Depois do Foundational:
   - Developer A: US1 (T007–T015).
   - Developer B: US2 (T016–T027) apenas se as referências compartilhadas forem coordenadas; caso contrário, executar US1 e US2 sequencialmente para evitar conflito nos mesmos documentos e suítes.
3. Polish em conjunto após as duas histórias.

---

## Notes

- [P] = arquivos diferentes, sem dependência de tarefa incompleta.
- [US1]/[US2] mapeiam a tarefa para a história correspondente.
- Não criar suíte nova: as duas suítes phase-aware são estendidas em Foundational e as quatro de lista fixa são atualizadas nas histórias.
- Não executar `git mv`; usar `Rename-Item` e deixar os commits para os hooks do Spec Kit.
- O `clean` em `mvn -B clean test` é obrigatório para remover cópias antigas de `target/classes`.
- Specs históricas, `target/`, `graphify-out/` e o JAR instalado não são editados à mão.
