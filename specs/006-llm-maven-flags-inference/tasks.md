# Tasks: Renomear o Procedimento Empacotado para llm-maven-flags-inference

**Input**: Design documents from `/specs/006-llm-maven-flags-inference/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/, quickstart.md

**Tests**: incluídos — a spec exige suítes de contrato (FR-011) e verificação anti-drift (FR-012).

**Organization**: tarefas agrupadas por user story para implementação e teste independentes.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: pode rodar em paralelo (arquivos diferentes, sem dependência pendente)
- **[Story]**: US1, US2 ou US3
- Todo task tem caminho de arquivo explícito

## Path Conventions

- Módulo único: `src/main/resources/`, `src/test/`, `README.md`, `.specify/templates/`
- Raiz nova: `src/main/resources/llm-maven-flags-inference/`
- Caminho legado: `src/main/resources/utils/`

---

## Phase 1: Setup

**Purpose**: confirmar a linha de base antes de qualquer alteração.

- [X] T001 [P] Confirmar a linha de base: 19 arquivos em 4 subpastas em `src/main/resources/utils/` e registrar os 19 caminhos relativos.
- [X] T002 [P] Confirmar as referências ao caminho antigo nas 4 suítes de `src/test/archetype-post-generate/` e nos 3 comentários de `src/test/resources/projects/*/archetype.properties`.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: mover a árvore e trocar as referências que não pertencem a uma história específica.

**CRITICAL**: nenhuma user story começa antes desta fase.

- [X] T003 Mover as 4 subpastas de `src/main/resources/utils/` para `src/main/resources/llm-maven-flags-inference/` no sistema de arquivos, preservando os 19 arquivos e os nomes.
- [X] T004 [P] Atualizar as referências de caminho em `src/main/resources/llm-maven-flags-inference/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md`; trocar só o prefixo, sem alterar regra.
- [X] T005 [P] Atualizar as referências de caminho em `src/main/resources/llm-maven-flags-inference/01-entrada-inicializacao/indice-01-mapa-dos-arquivos-e-ordem-de-leitura.md`; trocar só o prefixo.
- [X] T006 [P] Atualizar as referências de caminho em `src/main/resources/llm-maven-flags-inference/02-operacao-procedimento-do-agente/prompt-01-papel-e-restricoes-do-agente.md`.
- [X] T007 [P] Atualizar as referências de caminho em `src/main/resources/llm-maven-flags-inference/02-operacao-procedimento-do-agente/prompt-02-como-localizar-e-ler-o-archetype.md`, incluindo o comando `jar xf <jar> utils`.
- [X] T008 Remover as subpastas vazias remanescentes em `src/main/resources/utils/`.
- [X] T009 [P] Atualizar o comentário `utils/03-...` em `src/test/resources/projects/custom-nested-edge-cases/archetype.properties`.
- [X] T010 [P] Atualizar o comentário `utils/03-...` em `src/test/resources/projects/custom-enums-from-payload/archetype.properties`.
- [X] T011 [P] Atualizar os comentários `utils/03-...` em `src/test/resources/projects/entity-contract/archetype.properties` e `src/test/resources/projects/custom-nested-objects/archetype.properties`.
- [X] T012 [P] Trocar `eda-llm-flag-inference` por `llm-maven-flags-inference` em `.specify/templates/spec-template.md`.
- [X] T013 [P] Trocar `eda-llm-flag-inference` por `llm-maven-flags-inference` em `.specify/templates/plan-template.md`.
- [X] T014 [P] Trocar `eda-llm-flag-inference` por `llm-maven-flags-inference` em `.specify/templates/tasks-template.md`.

**Checkpoint**: 19 arquivos na nova raiz; caminho legado vazio.

---

## Phase 3: User Story 1 - Novo caminho no prompt do operador (Priority: P1) — MVP

**Goal**: o preâmbulo aponta para `llm-maven-flags-inference/` e o agente produz a linha `mvn archetype:generate`.

**Independent Test**: executar um spec novo contra o JAR instalado e conferir a linha completa e a ausência do caminho antigo.

### Implementation for User Story 1

- [X] T015 [P] [US1] Atualizar `src/test/archetype-post-generate/BuildArtifactsTest.groovy` §5 para a nova raiz e o alvo empacotado.
- [X] T016 [P] [US1] Atualizar `src/test/archetype-post-generate/ProcedureContractTest.groovy`: raiz nova e prefixo `llm-maven-flags-inference/` no check 1.5.
- [X] T017 [P] [US1] Atualizar `src/test/archetype-post-generate/DestinationContractTest.groovy`: raiz, cópia em `target/classes` e entrada do JAR instalado.
- [X] T018 [P] [US1] Atualizar `src/test/archetype-post-generate/ReadmeConsistencyTest.groovy`: diretório da contagem e caminhos do `prompt-00` e do `prompt-04`.
- [X] T019 [US1] Atualizar todas as referências ao caminho antigo em `README.md` para `llm-maven-flags-inference/`, incluindo o template canônico e os cenários A/B.
- [X] T020 [US1] Criar `src/test/archetype-post-generate/PathRenameContractTest.groovy` com os checks de US1: 4 subpastas, 19 arquivos nominais e nova entrada na árvore-fonte e em `target/classes`.
- [X] T021 [US1] Registrar `PathRenameContractTest.groovy` em `src/test/groovy/run-archetype-post-generate-tests.groovy`.
- [X] T022 [US1] Rodar `mvn -B test` a partir de `pom.xml` e confirmar as suítes verdes.
- [X] T023 [US1] Rodar `mvn -B clean install "-Darchetype.test.skip=true"` a partir de `pom.xml` e confirmar o JAR instalado.
- [X] T024 [US1] Executar o Cenário 7 do `specs/006-llm-maven-flags-inference/quickstart.md` com um spec novo e registrar a linha emitida.

**Checkpoint**: o caminho novo está funcional e a suíte está verde.

---

## Phase 4: User Story 2 - Spec antiga continua funcionando (Priority: P2)

**Goal**: o caminho antigo contém apenas um redirect e a spec antiga chega ao mesmo procedimento.

**Independent Test**: executar uma spec antiga contra o JAR instalado e comparar a linha com a de uma spec nova.

### Implementation for User Story 2

- [X] T025 [US2] Criar o único redirect em `src/main/resources/utils/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md`, apontando para a nova entrada e sem regra.
- [X] T026 [US2] Estender `src/test/archetype-post-generate/PathRenameContractTest.groovy` com os checks do redirect: exatamente 1 arquivo no caminho antigo, alvo correto e nenhuma regra.
- [X] T027 [US2] Estender `src/test/archetype-post-generate/PathRenameContractTest.groovy` com o check opt-in do JAR instalado: nova entrada e stub presentes.
- [X] T028 [US2] Rodar `mvn -B test` a partir de `pom.xml` e executar o Cenário 7 do `specs/006-llm-maven-flags-inference/quickstart.md` com um spec antigo; comparar a linha com a do spec novo.

**Checkpoint**: spec antiga e spec nova produzem a mesma linha.

---

## Phase 5: User Story 3 - Leitor entende a pasta e as subpastas (Priority: P3)

**Goal**: o README explica a pasta, a sequência e cada subpasta.

**Independent Test**: ler apenas o tópico e narrar a sequência do prompt até a linha `mvn`.

### Implementation for User Story 3

- [X] T029 [US3] Adicionar a `README.md` o tópico H2 `llm-maven-flags-inference` com a declaração de alvo do prompt, a responsabilidade pelo pré-processamento, a sequência de 5 passos e a frase de fronteira.
- [X] T030 [US3] Adicionar a `README.md` as 4 subseções H3 com os nomes atuais e um parágrafo curto cada, sem repetir regras.
- [X] T031 [US3] Adicionar ao índice de `README.md` as entradas do tópico e das 4 subseções.
- [X] T032 [US3] Estender `src/test/archetype-post-generate/PathRenameContractTest.groovy` com os checks do README e o anti-drift: título, 5 passos, frase de fronteira, 4 subseções e 0 termos proibidos no corpus operacional.
- [X] T033 [US3] Rodar `mvn -B test` a partir de `pom.xml` e confirmar a suíte verde.

**Checkpoint**: o README cobre a pasta e as 4 subpastas; o anti-drift passa.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: provar o contrato completo no artefato instalado.

- [X] T034 [P] Verificar que `specs/004-entity-contract-schema-test/`, `specs/005-output-directory-contract/` e `specs/_archive/003-llm-flag-inference/` permanecem intactos.
- [X] T035 [P] Executar os Cenários 1 a 3 do `specs/006-llm-maven-flags-inference/quickstart.md` (layout, redirect, anti-drift).
- [X] T036 Executar `mvn -B clean install "-Darchetype.test.skip=true"` a partir de `pom.xml`.
- [X] T037 Executar `mvn -B clean integration-test` a partir de `pom.xml`.
- [X] T038 Executar o Cenário 6 do `specs/006-llm-maven-flags-inference/quickstart.md` (JAR instalado: 19 arquivos + redirect).
- [X] T039 Executar o Cenário 7 do `specs/006-llm-maven-flags-inference/quickstart.md` com spec antiga e nova e registrar a mesma linha.
- [X] T040 Revisar `specs/006-llm-maven-flags-inference/spec.md` SC-001 a SC-007 e registrar a evidência de cada critério.

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: sem dependência.
- **Foundational (Phase 2)**: depende do Setup; bloqueia todas as stories.
- **US1 (Phase 3)**: depende da Fase 2.
- **US2 (Phase 4)**: depende da Fase 2 e de US1 para o arquivo da suíte nova.
- **US3 (Phase 5)**: depende de US1 para o arquivo da suíte; o README é independente.
- **Polish (Phase 6)**: depende de US1, US2 e US3.

### User Story Dependencies

- **US1 (P1)**: entrega o caminho novo; é o MVP.
- **US2 (P2)**: acrescenta o redirect; depende do move e da suíte criada em US1.
- **US3 (P3)**: acrescenta a documentação; depende do README já sem o caminho antigo (US1) e da suíte (US1).

### Within Each User Story

- US1: suítes existentes → README → suíte nova → runner → testes → instalação → smoke.
- US2: stub → checks da suíte → teste → smoke.
- US3: tópico → subseções → índice → checks anti-drift → teste.

### Parallel Opportunities

- Fase 2: T004–T007 em arquivos distintos; T009–T014 em arquivos distintos. T003 antes; T008 depois do move.
- US1: T015–T018 em suítes distintas; T019 e T020 em arquivos distintos. T021 depende de T020.
- US2: T026 e T027 no mesmo arquivo; sequenciar.
- US3: T029–T031 no mesmo arquivo; sequenciar. T032 depois.

---

## Parallel Example: User Story 1

```text
Task: "Atualizar BuildArtifactsTest §5 para a nova raiz"
Task: "Atualizar ProcedureContractTest: raiz e prefixo 1.5"
Task: "Atualizar DestinationContractTest: raiz, target/classes e JAR"
Task: "Atualizar ReadmeConsistencyTest: contagem e caminhos do prompt"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Fase 1: Setup.
2. Fase 2: Foundational.
3. Fase 3: US1.
4. **PARAR e VALIDAR**: rodar o Cenário 7 com um spec novo.
5. Só então seguir para US2 e US3.

### Incremental Delivery

1. Fase 2 entrega a árvore movida.
2. US1 entrega o caminho novo funcionando.
3. US2 entrega a compatibilidade com specs antigas.
4. US3 entrega a documentação e o anti-drift.
5. Polish entrega a prova no JAR instalado.

---

## Notes

- [P] = arquivos diferentes, sem dependência pendente.
- Não usar comando de versionamento para mover arquivos; o move é no sistema de arquivos.
- Não alterar regra, contagem de 19, nomes de subpasta, catálogo de 35 flags ou 18 filesets.
- Não tocar em `specs/003`, `specs/004`, `specs/005` nem em `graphify-out/`.
- Parar em cada checkpoint para validar a story de forma independente.

---

## Phase 7: Convergence

- [X] T041 Atualizar o comentário de `pom.xml` que cita `utils/04-referencia-templates-de-contrato/` para `llm-maven-flags-inference/04-referencia-templates-de-contrato/` per FR-006 (partial)
- [X] T042 Incluir `pom.xml` no corpus da verificação anti-drift em `src/test/archetype-post-generate/PathRenameContractTest.groovy` e no contrato em `specs/006-llm-maven-flags-inference/contracts/documentation-contract.md` per FR-012 (partial)
