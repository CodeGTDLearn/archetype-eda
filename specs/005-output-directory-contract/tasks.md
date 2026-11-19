---

description: "Task list template for feature implementation"
---

# Tasks: Contrato de Destino de Geração (outputDirectory) no Procedimento Empacotado

**Input**: Design documents from `/specs/005-output-directory-contract/`

**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: As tasks de teste **SÃO** incluídas — a spec exige verificação automatizada (FR-028..FR-032) e a feature entrega prova ao mantenedor (User Story 4).

**Organization**: Tasks agrupadas por user story, permitindo implementação e validação independentes de cada história.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: pode rodar em paralelo (arquivos diferentes, sem dependências)
- **[Story]**: história de usuário à qual a task pertence (US1, US2, US3…)
- Caminhos exatos de arquivo em todas as descrições

## Path Conventions

Projeto único. O alvo da feature é o **procedimento empacotado** em `src/main/resources/utils/` e a **verificação** em `src/test/archetype-post-generate/`.

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Preparar o terreno — baseline verde e convenções do projeto confirmadas antes de qualquer edição.

- [ ] T001 Registrar o baseline: rodar `mvn -B clean test` e confirmar que as 40 suítes Groovy existentes passam verdes (nenhuma alteração feita até aqui)
- [ ] T002 Registrar o baseline de integração: rodar `mvn -B clean integration-test` e confirmar que as 22 fixtures de integração passam verdes
- [ ] T003 [P] Confirmar as contagens de referência que a feature NÃO pode alterar: 19 arquivos em `src/main/resources/utils/`, 35 `requiredProperty` e 18 `fileSet` em `src/main/resources/META-INF/maven/`
- [ ] T004 [P] Confirmar o gate `ProcedureContractTest` 1.4: nenhum termo entre backticks em `src/main/resources/utils/**/*.md` pode casar com `^[a-z][a-z0-9_]*_[a-z0-9_]+$` sem ser flag do catálogo — o marcador `PASTA DE DESTINO` (maiúsculas, com espaço) é a única forma segura

**Checkpoint**: baseline verde e contagens registradas — nenhuma regressão pode ser atribuída à feature.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Regras **transversais** que todas as histórias dependem. Devem ser escritas primeiro porque definem o vocabulário e o gate compartilhados.

**?? CRITICAL**: nenhuma história pode começar antes desta fase — o gate de destino e o catálogo de flags são a base de todas.

- [ ] T005 Declarar a entrada de destino como 5ª entrada do spec em `src/main/resources/utils/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md` §2 (Entradas externas permitidas) — listar tipo de módulo, package base, use-case, contrato JSON e **pasta de destino**, com a marcação `PASTA DE DESTINO`
- [ ] T006 Declarar o **Gate de destino** em `src/main/resources/utils/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md` §0.2 — avaliação após os Gates A, B e C; destino ausente, relativo, ambíguo ou dentro da árvore do archetype → parar e perguntar; **nunca** aplicar default presumido (FR-005, FR-007, FR-014)
- [ ] T007 Estender o **relatório de parada** em `src/main/resources/utils/01-entrada-inicializacao/prompt-00-leia-esta-arquivo-primeiro.md` §0.3 para cobrir o Gate de destino, com o mesmo formato dos demais: gate acionado, o que falta, o que o operador deve fornecer, e a afirmação de que nenhuma derivação e nenhuma execução ocorreram (FR-008)
- [ ] T008 Acrescentar a condição de destino em `src/main/resources/utils/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md` §6 (Condições de parada) e atualizar o item do checklist em §7 — substituir "Executei fora do projeto pai" por item que nomeie o destino absoluto e a emissão da propriedade (FR-021)
- [ ] T009 Declarar no `src/main/resources/utils/03-derivacao-regras-de-formacao-do-contrato/regras-01-como-derivar-a-entidade-do-contrato.md` §1 (Entradas) que o spec tem 5 entradas, e em §4.2 (Campos de negócio) que a marcação `PASTA DE DESTINO` pertence ao spec e **não** é chave de contrato — a lista de metachaves **permanece inalterada** (FR-001, FR-022, FR-023)
- [ ] T010 Declarar em `src/main/resources/utils/03-derivacao-regras-de-formacao-do-contrato/regras-06-como-relatar-proveniencia.md` §2 e §8 que o destino **não** é flag: não ocupa linha na tabela de proveniência, não recebe fonte nova e não aparece na seção de descartadas (FR-020)

**Checkpoint**: vocabulário e gate de destino declarados — as histórias podem começar em paralelo.

---

## Phase 3: User Story 1 - Operador informa o destino e o projeto nasce onde ele pediu (Priority: P1) ?? MVP

**Goal**: O destino entra no spec como marcação `PASTA DE DESTINO`, é emitido na linha de comando como a propriedade nativa `outputDirectory`, e o módulo nasce em `<destino>/<artifactId>`.

**Independent Test**: gerar um módulo com destino informado e verificar que (a) a linha de comando emitida carrega o destino, (b) o módulo existe na pasta pedida com `pom.xml` próprio, (c) nada foi criado dentro da árvore do archetype, (d) a compilação de verificação ocorreu no módulo gerado.

### Tests for User Story 1 ?? (OPTIONAL - only if tests requested)

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [ ] T011 [P] [US1] Criar `src/test/archetype-post-generate/DestinationContractTest.groovy` com os checks de declaração: a marcação `PASTA DE DESTINO` está em `prompt-00` e `regras-01`; a propriedade `outputDirectory` está no template de command line de `prompt-04`; o campo de destino está no resumo final de `prompt-05` — **deve falhar antes da implementação**
- [ ] T012 [P] [US1] Acrescentar em `src/test/archetype-post-generate/ProcedureContractTest.groovy` os checks de que a lista de metachaves de `regras-01` permanece inalterada e de que a propriedade `outputDirectory` **não** aparece no catálogo de flags — **deve falhar antes da implementação**

### Implementation for User Story 1

- [ ] T013 [US1] Reconciliar `src/main/resources/utils/02-operacao-procedimento-do-agente/prompt-03-regras-obrigatorias-de-geracao.md` §3 — substituir o mecanismo "mudar de diretório + `robocopy` /MOVE" pelo mecanismo único `-DoutputDirectory`, removendo o caminho fixo `C:\Users\edufi\Downloads` e a movimentação posterior do módulo (FR-011, FR-012)
- [ ] T014 [US1] Declarar em `src/main/resources/utils/02-operacao-procedimento-do-agente/prompt-03-regras-obrigatorias-de-geracao.md` §4 (PowerShell) que o caminho do destino recebe a mesma proteção contra interpretação do shell já aplicada às demais propriedades, e que o valor usa barras normais, sem barra final e sem quebra de linha (FR-009, FR-010)
- [ ] T015 [US1] Em `src/main/resources/utils/02-operacao-procedimento-do-agente/prompt-04-fluxo-de-execucao-e-verificacao.md` §2, acrescentar `-DoutputDirectory=<destino>` ao template da command line, com nota de que a propriedade é **nativa do plugin de geração de archetype** e não uma flag do catálogo (FR-009, FR-020)
- [ ] T016 [US1] Em `src/main/resources/utils/02-operacao-procedimento-do-agente/prompt-04-fluxo-de-execucao-e-verificacao.md` §1.2, acrescentar ao checklist os itens: destino absoluto, destino fora da árvore do archetype, propriedade emitida na command line (FR-013, FR-014, FR-021)
- [ ] T017 [US1] Em `src/main/resources/utils/02-operacao-procedimento-do-agente/prompt-04-fluxo-de-execucao-e-verificacao.md` §3, declarar a validação pós-geração: confirmar que a raiz é `<destino>/<artifactId>`, que ela tem `pom.xml` próprio, e que a compilação de verificação ocorre **dentro** dela, nunca na raiz do archetype (FR-016, FR-017)
- [ ] T018 [US1] Em `src/main/resources/utils/02-operacao-procedimento-do-agente/prompt-05-entregaveis-e-relatorios.md` §4 (Resumo final), acrescentar o campo "destino de geração" com o caminho absoluto do módulo gerado, ao lado da command line executada (FR-018, FR-019)
- [ ] T019 [US1] Registrar a suíte nova em `src/test/groovy/run-archetype-post-generate-tests.groovy` — obrigatória por `CoverageRegistryTest` 2.1/2.2, que falha se uma suíte existir em disco e não estiver no runner

**Checkpoint**: US1 funcional — o agente emite a propriedade nativa e o módulo nasce no destino pedido.

---

## Phase 4: User Story 2 - Agente pergunta em vez de chutar (Priority: P1)

**Goal**: Quando o destino falta, o agente interrompe antes de qualquer derivação, pergunta uma vez e — sem operador — para em vez de inventar um caminho.

**Independent Test**: entregar spec sem destino e verificar que nenhuma derivação ocorreu, que houve exatamente uma pergunta, que nada foi gerado e que a sugestão oferecida está fora da árvore do archetype.

### Tests for User Story 2 ?? (OPTIONAL - only if tests requested)

- [ ] T020 [P] [US2] Acrescentar em `src/test/archetype-post-generate/DestinationContractTest.groovy` os checks de que o procedimento declara o gate de destino, o relatório de parada correspondente e a proibição de destino presumido — **deve falhar antes da implementação**

### Implementation for User Story 2

- [ ] T021 [US2] Em `src/main/resources/utils/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md` §0.2, detalhar que a ausência do destino interrompe o fluxo **antes** de qualquer derivação ou execução, e que a ausência de marcador reconhecível é tratada como ausência de destino (FR-002, FR-005)
- [ ] T022 [US2] Em `src/main/resources/utils/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md` §0.2, declarar que a pergunta é feita **uma única vez**, que ela pode oferecer uma sugestão de pasta temporária **fora** da árvore do archetype, e que a sugestão não é aplicada sem confirmação explícita (FR-006)
- [ ] T023 [US2] Em `src/main/resources/utils/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md` §0.2, declarar que, sem operador disponível, o agente para e reporta e **nunca** usa o diretório de trabalho corrente, a raiz do archetype, nem qualquer pasta de código, de recursos ou de build do próprio archetype (FR-007)
- [ ] T024 [US2] Em `src/main/resources/utils/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md` §7 (Checklist rápido), acrescentar item de que o destino foi confirmado antes de derivar (FR-005, FR-021)

**Checkpoint**: US1 e US2 funcionam — o agente ou usa o destino informado ou pergunta; nunca chuta.

---

## Phase 5: User Story 3 - Destino inválido ou ambíguo é recusado com clareza (Priority: P2)

**Goal**: Destino relativo, dentro da árvore do archetype, inexistente ou com módulo de mesmo nome é tratado com recusa clara, aviso e pergunta — sem criar, apagar ou sobrescrever nada por conta própria.

**Independent Test**: gerar com cada variação inválida e verificar a recusa, o aviso e a ausência de campo espúrio na entidade gerada.

### Tests for User Story 3 ?? (OPTIONAL - only if tests requested)

- [ ] T025 [P] [US3] Acrescentar em `src/test/archetype-post-generate/DestinationContractTest.groovy` os checks de que o procedimento não contém caminho absoluto de usuário, não contém `robocopy` nem `cd C:\`, e que a propriedade `outputDirectory` não aparece no catálogo — **deve falhar antes da implementação**

### Implementation for User Story 3

- [ ] T026 [US3] Em `src/main/resources/utils/02-operacao-procedimento-do-agente/prompt-03-regras-obrigatorias-de-geracao.md` §3, declarar as regras de validação do destino: absoluto obrigatório; recusados caminho relativo, `~` e sinalização de fim de arquivo; normalização de espaços e barra final antes de comparar e emitir (FR-003)
- [ ] T027 [US3] Em `src/main/resources/utils/02-operacao-procedimento-do-agente/prompt-04-fluxo-de-execucao-e-verificacao.md` §1.2 ou §2, declarar a **recusa absoluta** de destino dentro da árvore do archetype — nenhuma confirmação do operador a autoriza — acompanhada de aviso explícito e nova pergunta; se o operador insistir na própria pasta do archetype, o fluxo é finalizado com relatório de parada (FR-014)
- [ ] T028 [US3] Em `src/main/resources/utils/02-operacao-procedimento-do-agente/prompt-04-fluxo-de-execucao-e-verificacao.md` §1.2 ou §2, declarar que, quando a pasta de destino ainda não existir, o agente pergunta **uma vez** exibindo o caminho completo que será criado e só prossegue após a confirmação; se a criação falhar depois da confirmação, ele para e reporta (FR-013)
- [ ] T029 [US3] Em `src/main/resources/utils/02-operacao-procedimento-do-agente/prompt-04-fluxo-de-execucao-e-verificacao.md` §1.2 ou §2, declarar que, havendo módulo de mesmo nome na pasta de destino, o agente pergunta **uma vez** oferecendo "sobrescrever o existente" ou "manter o existente e encerrar", e que nada é apagado sem a escolha explícita (FR-015)

**Checkpoint**: US3 funcional — nenhum caso inválido passa em silêncio.

---

## Phase 6: User Story 4 - Mantenedor comprova que o procedimento empacotado está atualizado (Priority: P2)

**Goal**: Prova automatizada, dentro do próprio archetype, de que a regra está no procedimento empacotado, de que o JAR reinstalado a carrega e de que nada regrediu.

**Independent Test**: rodar a build completa e conferir o procedimento dentro do JAR reinstalado, além das verificações de contagem e das suítes existentes.

### Tests for User Story 4 ?? (OPTIONAL - only if tests requested)

- [ ] T030 [P] [US4] Em `src/test/archetype-post-generate/DestinationContractTest.groovy`, adicionar os checks de **contagem travada**: `utils/` com exatamente 19 arquivos, catálogo com exatamente 35 `requiredProperty` e 18 `fileSet` — **deve falhar antes da implementação**
- [ ] T031 [P] [US4] Em `src/test/archetype-post-generate/DestinationContractTest.groovy`, adicionar o check de que o procedimento é lido **de dentro do artefato reinstalado** em `~/.m2/repository/com/eda-archetype/eda-archetype/0.0.1-SNAPSHOT/`, e não da árvore de desenvolvimento (FR-032)

### Implementation for User Story 4

- [ ] T032 [US4] Executar `mvn -B clean install "-Darchetype.test.skip=true"` e confirmar que o JAR reinstalado em `~/.m2/repository/com/eda-archetype/eda-archetype/0.0.1-SNAPSHOT/` contém os 19 arquivos de `utils/` com a regra de destino — **Princípio VI: reinstalação limpa antes de concluir que a mudança "não teve efeito"**
- [ ] T033 [US4] Atualizar `README.md` §7.1 (prompts de homologação manual, linhas ~2189 e ~2244) — trocar a instrução ad-hoc "Gere FORA da árvore do archetype: use uma pasta de trabalho com pom.xml ou passe `-DoutputDirectory=<pasta temporaria>`" pela entrada `PASTA DE DESTINO` no formato do spec
- [ ] T034 [US4] Atualizar `README.md` na seção "Inferência de Contrato e Use-Case: do Prompt ao Maven CLI" (linhas ~645-780) — citar o destino como 5ª entrada e manter o exemplo `-DoutputDirectory=C:\Temp\eda-sandbox` como **ilustração**, referenciando a regra em `utils/`; o README não é fonte de runtime
- [ ] T035 [US4] Verificar que `README.md` continua publicando as contagens inalteradas — 19 arquivos, 35 flags, 18 filesets, 64 fragmentos, 22 fixtures — exigido por `ReadmeConsistencyTest` 2.1-2.5

**Checkpoint**: US4 funcional — a prova existe e roda na build.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Validação de ponta a ponta e confirmação de que nada regrediu.

- [ ] T036 Rodar `mvn -B clean test` e confirmar que as 40 suítes (39 existentes + a nova `DestinationContractTest`) passam verdes
- [ ] T037 Rodar `mvn -B clean integration-test` e confirmar que as 22 fixtures de integração continuam verdes
- [ ] T038 [P] Confirmar que `git status --short` mostra apenas o `README.md` modificado e nenhum resíduo de geração dentro da árvore do archetype
- [ ] T039 [P] Validar o procedimento empacotado montado: `mvn -B clean process-resources` e conferir em `target/classes/utils/` que a marcação `PASTA DE DESTINO` e a propriedade `outputDirectory` estão presentes
- [ ] T040 Executar o guia de validação `specs/005-output-directory-contract/quickstart.md` — Cenários 1, 2, 3 e 10 (determinísticos)
- [ ] T041 Executar os Cenários 4 a 9 de `specs/005-output-directory-contract/quickstart.md` (homologação manual do agente: destino informado, destino ausente, destino dentro da árvore, pasta inexistente, módulo de mesmo nome, marcação dentro do contrato) e limpar os diretórios de teste ao final

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: sem dependências — pode começar imediatamente
- **Foundational (Phase 2)**: depende do Setup — **BLOQUEIA todas as histórias**
- **User Stories (Phase 3+)**: todas dependem do Foundational
  - As histórias podem seguir em paralelo (equipe suficiente)
  - Ou sequencialmente em ordem de prioridade (P1 → P1 → P2 → P2)
- **Polish (Phase Final)**: depende de todas as histórias desejadas

### User Story Dependencies

- **User Story 1 (P1)**: pode começar após o Foundational — sem dependência de outras histórias
- **User Story 2 (P1)**: pode começar após o Foundational — compartilha o gate de destino com US1, mas é testável isoladamente
- **User Story 3 (P2)**: pode começar após o Foundational — depende da linguagem do gate (US2), mas os critérios de validação são próprios
- **User Story 4 (P2)**: pode começar após o Foundational — depende da existência da regra (US1) para o check do artefato reinstalado

### Within Each User Story

- Tests primeiro, e devem **falhar** antes da implementação
- Procedimento antes da suíte (a suíte lê o procedimento)
- US1 → US2 → US3: as histórias do destino descrevem o mesmo gate em profundidades diferentes; US4 fecha a prova

### Parallel Opportunities

- Todas as tasks `[P]` do Setup podem rodar em paralelo
- Todas as tasks `[P]` do Foundational podem rodar em paralelo (arquivos distintos dentro de `utils/`)
- Depois do Foundational, todas as histórias podem começar em paralelo
- Todas as tasks de teste `[P]` de uma história podem rodar em paralelo
- US1, US2, US3 e US4 podem ser trabalhadas em paralelo por pessoas diferentes — cada uma toca arquivos distintos, exceto pela convergência em `prompt-00` e `prompt-04`

### Conflitos de arquivo conhecidos

| Arquivo | Histórias | Como resolver |
|---|---|---|
| `src/main/resources/utils/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md` | US1 (T008), US2 (T021-T024) | US2 depende da estrutura criada por US1; sequenciar |
| `src/main/resources/utils/02-operacao-procedimento-do-agente/prompt-04-fluxo-de-execucao-e-verificacao.md` | US1 (T015-T017), US3 (T027-T029) | US3 depende da command line criada por US1; sequenciar |
| `src/test/archetype-post-generate/DestinationContractTest.groovy` | US1 (T011), US2 (T020), US3 (T025), US4 (T030-T031) | Criar em T011; as demais acrescentam checks ao mesmo arquivo — sequenciar as adições |

---

## Parallel Example: User Story 1

```bash
# Lançar em paralelo os arquivos de procedimento independentes:
Task: "Criar DestinationContractTest.groovy em src/test/archetype-post-generate/DestinationContractTest.groovy"
Task: "Acrescentar checks de metachaves e catálogo em src/test/archetype-post-generate/ProcedureContractTest.groovy"
Task: "Reconciliar §3 do prompt-03 em src/main/resources/utils/02-operacao-procedimento-do-agente/prompt-03-regras-obrigatorias-de-geracao.md"
Task: "Acrescentar campo de destino em §4 do prompt-05 em src/main/resources/utils/02-operacao-procedimento-do-agente/prompt-05-entregaveis-e-relatorios.md"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (baseline verde)
2. Complete Phase 2: Foundational (gate e vocabulário)
3. Complete Phase 3: User Story 1
4. **STOP and VALIDATE**: `mvn -B clean test` + Cenários 1 e 4 do quickstart
5. Demo: o agente gera no destino pedido

### Incremental Delivery

1. Setup + Foundational → gate de destino declarado
2. US1 → validar independentemente → o módulo nasce onde foi pedido (MVP!)
3. US2 → validar → o agente pergunta em vez de chutar
4. US3 → validar → nenhum caso inválido passa em silêncio
5. US4 → validar → a prova existe e o JAR reinstalado carrega a regra

### Parallel Team Strategy

Com várias pessoas:

1. Toda a equipe faz Setup + Foundational junto
2. Depois do Foundational:
   - Pessoa A: US1 (emissão da propriedade)
   - Pessoa B: US2 (gate e pergunta)
   - Pessoa C: US3 (validações e recusas)
   - Pessoa D: US4 (suíte de verificação e README)
3. Reconciliar os conflitos de arquivo listados acima antes de integrar

---

## Notes

- [P] = arquivos diferentes, sem dependências
- [Story] mapeia a task para a história, para rastreabilidade
- Cada história deve ser completável e testável isoladamente
- Verificar que os testes falham antes de implementar
- Commitar após cada task ou grupo lógico
- Parar em cada checkpoint para validar a história isoladamente
- Evitar: tasks vagas, conflitos de arquivo, dependências entre histórias que quebrem a independência
- **Ordem obrigatória**: a regra só chega ao agente pelo artefato reinstalado — T032 (reinstalação + verificação do JAR) precede qualquer afirmação de que a feature funciona
- **Princípio VI**: antes de concluir que uma alteração "não teve efeito", confirmar reinstalação limpa
