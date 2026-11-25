# Feature Specification: Renomear Arquivos das Subpastas 01 e 02 em Duas Fases

**Feature Branch**: `008-rename-procedure-files`

**Created**: 2026-11-25

**Status**: Draft

**Input**: User description: "Dividir em duas fases os renomes de arquivos nas subpastas iniciadas por 01 e 02 do procedimento empacotado (`llm-maven-flags-inference`)."

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

### Edge Cases

- A Fase 1 é executada novamente sobre uma subpasta 01 já renomeada: nenhum arquivo duplicado, órfão ou com nome intermediário é criado.
- A Fase 2 é executada novamente sobre uma subpasta 02 já sem prefixo: os cinco nomes permanecem estáveis.
- Uma das fases é aplicada sem a outra: a subpasta da fase aplicada fica coerente isoladamente e a outra permanece como estava.
- Os nomes antigos continuam citados no índice, em documentos operacionais e em verificações automatizadas existentes: essas citações ficam desatualizadas porque a atualização de referências está fora do escopo desta feature.
- A verificação existente que confere os caminhos publicados e a listagem no índice não reconhece os novos nomes até que uma decisão posterior trate das referências: a consequência é conhecida e aceita nesta feature.
- Diferença de maiúsculas e minúsculas no nome `01-entryPoint.md`: o nome é adotado literalmente, com `E` maiúsculo.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O procedimento empacotado MUST manter as quatro subpastas existentes, identificadas pelos numerais 01, 02, 03 e 04.
- **FR-002**: A Fase 1 MUST renomear, na subpasta 01, `prompt-00-leia-este-arquivo-primeiro.md` para `01-entryPoint.md`.
- **FR-003**: A Fase 1 MUST renomear, na subpasta 01, `indice-01-mapa-dos-arquivos-e-ordem-de-leitura.md` para `02-mapa-dos-arquivos-e-ordem-de-leitura.md`, alterando o numeral de 01 para 02 e eliminando o prefixo `indice-`, preservando o restante do nome.
- **FR-004**: Após a Fase 1, a subpasta 01 MUST conter exatamente dois arquivos, com os nomes `01-entryPoint.md` e `02-mapa-dos-arquivos-e-ordem-de-leitura.md`, e MUST NOT conter os dois nomes antigos.
- **FR-005**: A Fase 2 MUST eliminar o prefixo `prompt-` dos cinco arquivos da subpasta 02, produzindo exatamente `01-papel-e-restricoes-do-agente.md`, `02-como-localizar-e-ler-o-archetype.md`, `03-regras-obrigatorias-de-geracao.md`, `04-fluxo-de-execucao-e-verificacao.md` e `05-entregaveis-e-relatorios.md`.
- **FR-006**: Após a Fase 2, a subpasta 02 MUST conter exatamente cinco arquivos, nenhum começando com `prompt-`, com os numerais 01 a 05 preservados.
- **FR-007**: Os sete renomes MUST preservar integralmente o conteúdo de cada arquivo, sem alteração de regras, textos, numeração interna ou formato.
- **FR-008**: Os sete renomes MUST manter cada arquivo na mesma subpasta de origem.
- **FR-009**: As subpastas 03 e 04 e todos os seus arquivos MUST permanecer inalterados.
- **FR-010**: Cada fase MUST ser executável, testável e demonstrável isoladamente, sem depender da conclusão da outra fase.
- **FR-011**: A especificação MUST apresentar as duas fases de forma explícita e ordenada como Fase 1 e Fase 2.
- **FR-012**: Esta feature MUST NOT criar aliases, redirects ou cópias com os nomes antigos.
- **FR-013**: Esta feature MUST NOT atualizar referências cruzadas nos documentos operacionais, no índice, nas fixtures ou nas verificações automatizadas existentes.

### Key Entities

- **Procedimento empacotado (`llm-maven-flags-inference`)**: conjunto de arquivos que viaja no artefato e é lido pelo agente; mantém quatro subpastas e 19 arquivos.
- **Subpasta 01**: contém a porta de entrada e o mapa de leitura; alvo da Fase 1.
- **Subpasta 02**: contém os cinco arquivos de operação do agente; alvo da Fase 2.
- **Arquivo renomeado**: arquivo cujo caminho muda, mantendo conteúdo e localização na mesma subpasta.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A subpasta 01 termina com exatamente 2 arquivos, nomeados `01-entryPoint.md` e `02-mapa-dos-arquivos-e-ordem-de-leitura.md`, e 0 ocorrências dos nomes antigos.
- **SC-002**: A subpasta 02 termina com exatamente 5 arquivos, 0 começando com `prompt-`, com os numerais 01 a 05 presentes.
- **SC-003**: 0 arquivos das subpastas 03 e 04 são adicionados, removidos ou renomeados.
- **SC-004**: 7 de 7 arquivos renomeados mantêm o conteúdo idêntico ao original.
- **SC-005**: Cada fase é verificável isoladamente: uma inspeção da subpasta da fase confirma o resultado sem exigir a outra fase.
- **SC-006**: 0 referências cruzadas, aliases ou redirects são criados ou atualizados por esta feature.

## Assumptions

- Os nomes citados correspondem aos arquivos reais da árvore empacotada na pasta `llm-maven-flags-inference/`.
- A grafia `01-entryPoint.md` é adotada literalmente como escrita na demanda, com `E` maiúsculo e camelCase.
- "Renomear" significa alterar apenas o caminho do arquivo; nenhum conteúdo normativo é reescrito.
- A numeração das subpastas (01, 02, 03, 04) é preservada; apenas nomes de arquivos mudam.
- A ordem de referência é Fase 1 e depois Fase 2, mas a implementação e a verificação de cada fase são independentes.
- A atualização de referências cruzadas, aliases e redirects fica fora do escopo desta feature por decisão da demanda; a defasagem de citações antigas é uma consequência conhecida e será tratada em decisão posterior.
- A contagem total de 19 arquivos do procedimento é preservada, com a mesma distribuição pelas quatro subpastas.
