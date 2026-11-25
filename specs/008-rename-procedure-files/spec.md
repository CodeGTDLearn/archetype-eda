# Feature Specification: Renomear Arquivos das Subpastas 01 e 02 em Três Fases

**Feature Branch**: `008-rename-procedure-files`

**Created**: 2026-11-25

**Status**: Draft

**Input**: User description: "Dividir em duas fases os renomes de arquivos nas subpastas iniciadas por 01 e 02 do procedimento empacotado (`llm-maven-flags-inference`)." A expressão "duas fases" refere-se aos renomes; a Fase 3 cobre a atualização das referências operacionais.

## Clarifications

### Session 2026-11-25

- Q: A atualização das referências aos nomes antigos entra no escopo desta feature? → A: Sim, como Fase 3, dependente das Fases 1 e 2; specs históricas e saídas geradas ficam de fora.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Fase 1 — Entrada e mapa da subpasta 01 (Priority: P1)

O mantenedor do archetype renomeia os dois arquivos da subpasta iniciada por 01. A porta de entrada passa a se chamar `01-entryPoint.md` e o mapa de leitura passa a se chamar `02-mapa-dos-arquivos-e-ordem-de-leitura.md`. O operador e o agente que consultam o procedimento de dentro do artefato empacotado passam a reconhecer a entrada pelo próprio nome e o mapa como o segundo item da sequência.

**Why this priority**: é a menor fatia que entrega valor de ponta a ponta — a porta de entrada e a ordem de leitura ficam explícitas sem depender da outra subpasta.

**Independent Test**: aplicar somente a Fase 1 e listar a subpasta 01; devem existir exatamente dois arquivos, com os novos nomes, e os nomes antigos devem estar ausentes.

**Acceptance Scenarios**:

1. **Given** a subpasta 01 contendo `prompt-00-leia-este-arquivo-primeiro.md` e `indice-01-mapa-dos-arquivos-e-ordem-de-leitura.md`, **When** a Fase 1 é aplicada, **Then** a subpasta passa a conter `01-entryPoint.md` e `02-mapa-dos-arquivos-e-ordem-de-leitura.md`, e os dois nomes antigos deixam de existir.
2. **Given** a subpasta 01 após a Fase 1, **When** os arquivos são lidos em ordem, **Then** o primeiro item é a entrada `01-entryPoint.md` e o segundo é o mapa `02-mapa-dos-arquivos-e-ordem-de-leitura.md`, sem o prefixo `indice-`.
3. **Given** a Fase 1 aplicada isoladamente, **When** as subpastas 02, 03 e 04 são inspecionadas, **Then** seus nomes de arquivos permanecem inalterados.

---

### User Story 2 - Fase 2 — Operação do agente sem o prefixo prompt- (Priority: P2)

O mantenedor do archetype remove o prefixo `prompt-` dos cinco arquivos da subpasta iniciada por 02, preservando o numeral e a descrição de cada nome. O agente que lê o procedimento encontra a mesma ordem de operação, agora sem o prefixo redundante.

**Why this priority**: incremento coeso e independente da Fase 1; padroniza os arquivos de operação sem repetir o escopo da fase anterior.

**Independent Test**: aplicar somente a Fase 2 e listar a subpasta 02; devem existir cinco arquivos, nenhum começando com `prompt-`, numerados de 01 a 05.

**Acceptance Scenarios**:

1. **Given** a subpasta 02 com cinco arquivos prefixados por `prompt-`, **When** a Fase 2 é aplicada, **Then** os cinco nomes resultantes são `01-papel-e-restricoes-do-agente.md`, `02-como-localizar-e-ler-o-archetype.md`, `03-regras-obrigatorias-de-geracao.md`, `04-fluxo-de-execucao-e-verificacao.md` e `05-entregaveis-e-relatorios.md`.
2. **Given** a subpasta 02 após a Fase 2, **When** seus arquivos são listados, **Then** nenhum nome começa com `prompt-` e a numeração 01 a 05 permanece.
3. **Given** a Fase 2 aplicada isoladamente, **When** as subpastas 01, 03 e 04 são inspecionadas, **Then** seus nomes de arquivos permanecem inalterados.

---

### User Story 3 - Fase 3 — Atualização das referências operacionais (Priority: P3)

O mantenedor atualiza todas as referências operacionais aos sete nomes antigos — README, conteúdo do índice, conteúdo da entrada, templates de spec, fixtures, as seis suítes de contrato e o redirect em `utils/` — para os novos nomes. O agente e o operador passam a encontrar apenas caminhos existentes, e o build volta a ficar verde.

**Why this priority**: fecha a coerência documental e restaura o gate de testes que as Fases 1 e 2 deixam vermelho.

**Independent Test**: aplicar as Fases 1 e 2, executar a Fase 3 e rodar `mvn -B test`; todas as suítes passam e não resta referência operacional aos sete nomes antigos.

**Acceptance Scenarios**:

1. **Given** as Fases 1 e 2 aplicadas, **When** a Fase 3 atualiza as referências, **Then** README, índice, entrada, templates, fixtures, suítes e redirect citam apenas os novos nomes.
2. **Given** a Fase 3 concluída, **When** `mvn -B test` é executado, **Then** todas as suítes passam.
3. **Given** as specs históricas 003–007, `target/` e `graphify-out/`, **When** a Fase 3 termina, **Then** esses conteúdos permanecem inalterados.

---

### Edge Cases

- A Fase 1 é executada novamente sobre uma subpasta 01 já renomeada: nenhum arquivo duplicado, órfão ou com nome intermediário é criado.
- A Fase 2 é executada novamente sobre uma subpasta 02 já sem prefixo: os cinco nomes permanecem estáveis.
- Uma das fases é aplicada sem a outra: a subpasta da fase aplicada fica coerente isoladamente e a outra permanece como estava.
- As Fases 1 e 2 deixam o build vermelho nas suítes que citam os nomes antigos; a Fase 3 restaura o verde.
- A Fase 3 edita uma única vez os arquivos compartilhados (README, índice, entrada e suítes) depois que os dois renomes existem.
- Specs históricas 003–007 e saídas geradas (`target/`, `graphify-out/`) permanecem como registro.
- Diferença de maiúsculas e minúsculas no nome `01-entryPoint.md`: o nome é adotado literalmente, com `E` maiúsculo.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O procedimento empacotado MUST manter as quatro subpastas existentes, identificadas pelos numerais 01, 02, 03 e 04.
- **FR-002**: A Fase 1 MUST renomear, na subpasta 01, `prompt-00-leia-este-arquivo-primeiro.md` para `01-entryPoint.md`.
- **FR-003**: A Fase 1 MUST renomear, na subpasta 01, `indice-01-mapa-dos-arquivos-e-ordem-de-leitura.md` para `02-mapa-dos-arquivos-e-ordem-de-leitura.md`, alterando o numeral de 01 para 02 e eliminando o prefixo `indice-`, preservando o restante do nome.
- **FR-004**: Após a Fase 1, a subpasta 01 MUST conter exatamente dois arquivos, com os nomes `01-entryPoint.md` e `02-mapa-dos-arquivos-e-ordem-de-leitura.md`, e MUST NOT conter os dois nomes antigos.
- **FR-005**: A Fase 2 MUST eliminar o prefixo `prompt-` dos cinco arquivos da subpasta 02, produzindo exatamente `01-papel-e-restricoes-do-agente.md`, `02-como-localizar-e-ler-o-archetype.md`, `03-regras-obrigatorias-de-geracao.md`, `04-fluxo-de-execucao-e-verificacao.md` e `05-entregaveis-e-relatorios.md`.
- **FR-006**: Após a Fase 2, a subpasta 02 MUST conter exatamente cinco arquivos, nenhum começando com `prompt-`, com os numerais 01 a 05 preservados.
- **FR-007**: Nas Fases 1 e 2, os sete renomes MUST preservar integralmente o conteúdo de cada arquivo, sem alteração de regras, textos, numeração interna ou formato; a Fase 3 MUST alterar apenas referências de caminho e nome.
- **FR-008**: Os sete renomes MUST manter cada arquivo na mesma subpasta de origem.
- **FR-009**: As subpastas 03 e 04 e todos os seus arquivos MUST permanecer inalterados.
- **FR-010**: As Fases 1 e 2 MUST ser executáveis, testáveis e demonstráveis isoladamente; a Fase 3 MUST depender das duas e ser executada depois delas.
- **FR-011**: A especificação MUST apresentar as três fases de forma explícita e ordenada como Fase 1, Fase 2 e Fase 3.
- **FR-012**: Esta feature MUST NOT criar aliases, redirects ou cópias com os nomes antigos.
- **FR-013**: A Fase 3 MUST atualizar todas as referências operacionais aos sete nomes antigos para os novos nomes em `README.md`, no conteúdo do índice, no conteúdo da entrada, nos templates de spec, nas fixtures, nas seis suítes de contrato e no redirect em `src/main/resources/utils/`.
- **FR-014**: A Fase 3 MUST alterar apenas referências de caminho e nome; regras, textos normativos e formato dos arquivos MUST permanecer inalterados.
- **FR-015**: Após a Fase 3, `mvn -B test` MUST passar.
- **FR-016**: Specs históricas 003–007, `target/` e `graphify-out/` MUST NOT ser reescritos.

### Key Entities

- **Procedimento empacotado (`llm-maven-flags-inference`)**: conjunto de arquivos que viaja no artefato e é lido pelo agente; mantém quatro subpastas e 19 arquivos.
- **Subpasta 01**: contém a porta de entrada e o mapa de leitura; alvo da Fase 1.
- **Subpasta 02**: contém os cinco arquivos de operação do agente; alvo da Fase 2.
- **Arquivo renomeado**: arquivo cujo caminho muda, mantendo conteúdo e localização na mesma subpasta.
- **Referência operacional**: citação de caminho ou nome em README, índice, entrada, templates, fixtures, suítes ou redirect; alvo da Fase 3.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A subpasta 01 termina com exatamente 2 arquivos, nomeados `01-entryPoint.md` e `02-mapa-dos-arquivos-e-ordem-de-leitura.md`, e 0 ocorrências dos nomes antigos.
- **SC-002**: A subpasta 02 termina com exatamente 5 arquivos, 0 começando com `prompt-`, com os numerais 01 a 05 presentes.
- **SC-003**: 0 arquivos das subpastas 03 e 04 são adicionados, removidos ou renomeados.
- **SC-004**: 7 de 7 arquivos renomeados mantêm o conteúdo idêntico ao original nas Fases 1 e 2; a Fase 3 altera apenas referências de caminho.
- **SC-005**: As Fases 1 e 2 são verificáveis isoladamente: uma inspeção da subpasta da fase confirma o resultado sem exigir a outra; a Fase 3 exige as duas.
- **SC-006**: Após a Fase 3, 0 referências operacionais aos sete nomes antigos e 0 aliases ou cópias; `mvn -B test` passa.
- **SC-007**: README, índice, entrada, templates, fixtures, suítes e redirect refletem os novos nomes.
- **SC-008**: Specs históricas 003–007 e saídas geradas permanecem inalteradas.

## Assumptions

- Os nomes citados correspondem aos arquivos reais da árvore empacotada na pasta `llm-maven-flags-inference/`.
- A grafia `01-entryPoint.md` é adotada literalmente como escrita na demanda, com `E` maiúsculo e camelCase.
- "Renomear" significa alterar apenas o caminho do arquivo; nenhum conteúdo normativo é reescrito.
- A numeração das subpastas (01, 02, 03, 04) é preservada; apenas nomes de arquivos mudam.
- As Fases 1 e 2 são independentes entre si; a Fase 3 depende das duas.
- A atualização de referências operacionais entra no escopo como Fase 3; specs históricas e saídas geradas ficam fora.
- O build fica vermelho entre as Fases 1 e 2 e volta a verde na Fase 3.
- A contagem total de 19 arquivos do procedimento é preservada, com a mesma distribuição pelas quatro subpastas.
