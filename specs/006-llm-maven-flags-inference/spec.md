# Feature Specification: Renomear o Procedimento Empacotado para llm-maven-flags-inference

**Feature Branch**: `006-llm-maven-flags-inference`

**Created**: 2026-11-23

**Status**: Draft

**Input**: User description: "Renomear o procedimento empacotado de utils/ para llm-maven-flags-inference/ e documentar as fases no README."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Novo caminho no prompt do operador (Priority: P1)

O operador escreve o preâmbulo `0. PROCEDIMENTO` apontando para `llm-maven-flags-inference/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md`. O agente lê a entrada e o índice dentro do JAR, aplica as regras e devolve a linha `mvn archetype:generate`.

**Why this priority**: é o caminho que passa a valer no uso real.

**Independent Test**: gerar com um spec novo contra o JAR instalado e conferir a linha `mvn` e a ausência do caminho antigo.

**Acceptance Scenarios**:

1. **Given** um spec novo apontando para o novo caminho, **When** o agente segue o procedimento, **Then** ele lê a entrada e o índice do JAR e produz a linha `mvn` completa.
2. **Given** a linha produzida, **When** ela é inspecionada, **Then** o pré-processamento termina nela e a geração Maven começa depois.
3. **Given** um documento operacional, **When** ele é inspecionado, **Then** ele cita apenas o novo caminho.

---

### User Story 2 - Spec antiga continua funcionando (Priority: P2)

Uma spec antiga aponta para `utils/`. O caminho antigo existe apenas como redirect para o novo local. O agente chega ao mesmo procedimento, sem regra duplicada.

**Why this priority**: specs já escritas não podem quebrar.

**Independent Test**: executar uma spec antiga contra o JAR instalado e comparar o resultado com o de uma spec nova.

**Acceptance Scenarios**:

1. **Given** uma spec antiga, **When** ela é executada contra o JAR instalado, **Then** o agente chega ao novo procedimento pelo redirect.
2. **Given** o redirect, **When** ele é lido, **Then** ele contém apenas o ponteiro, sem regra.
3. **Given** uma spec antiga e uma nova, **When** as duas geram, **Then** produzem a mesma linha de comando.

---

### User Story 3 - Leitor entende a pasta e as subpastas (Priority: P3)

O README ganha o tópico `llm-maven-flags-inference`, com a sequência didática e uma subseção curta por subpasta.

**Why this priority**: o nome novo só reduz ambiguidade se o papel de cada subpasta estiver explícito.

**Independent Test**: ler apenas o tópico e narrar a sequência do prompt até a linha `mvn`.

**Acceptance Scenarios**:

1. **Given** o tópico do README, **When** o leitor o abre, **Then** ele informa que a pasta é o alvo do prompt do agente e responde pelo pré-processamento.
2. **Given** a sequência, **When** ela é lida na ordem, **Then** vai do prompt do operador até a linha `mvn` e afirma que o pré-processamento termina nela.
3. **Given** as quatro subseções, **When** elas são lidas, **Then** cada uma diz a função e o momento da subpasta, sem repetir as regras.

---

### Edge Cases

- Spec antiga cita um arquivo interno do caminho antigo: o redirect preserva a chegada ao procedimento; as 4 subpastas e os nomes dos arquivos não mudam.
- Spec nova cita o caminho antigo: o caminho antigo é compatibilidade, não segunda cópia.
- Specs históricas 003, 004 e 005 citam o caminho antigo: permanecem como registro e ficam fora da verificação anti-drift.
- Contagem ou distribuição dos 19 arquivos muda: a suíte de contratos falha.
- JAR instalado sem o redirect ou sem o novo caminho: a verificação falha.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O procedimento empacotado MUST ter raiz única `llm-maven-flags-inference/`, na árvore-fonte e no JAR.
- **FR-002**: A nova raiz MUST conter os 19 arquivos, nas mesmas 4 subpastas, com os mesmos nomes e a mesma distribuição.
- **FR-003**: Regras, ordem de leitura, catálogo de flags, templates de contrato e pós-processador MUST permanecer inalterados.
- **FR-004**: Todo template de spec do operador MUST apontar o preâmbulo `0. PROCEDIMENTO` para o novo caminho.
- **FR-005**: O caminho antigo MUST existir apenas como redirect para o novo caminho, sem regra e sem segunda cópia.
- **FR-006**: README, templates de spec, suítes de contrato e arquivos do procedimento MUST citar apenas o novo caminho; a única exceção é o redirect.
- **FR-007**: O nome `llm-maven-flags-inference` MUST substituir o nome anterior do conceito nos templates de spec, plano e tarefas.
- **FR-008**: O README MUST ter um tópico intitulado `llm-maven-flags-inference` que declare a pasta como alvo do prompt e responsável pelo pré-processamento.
- **FR-009**: O tópico MUST apresentar a sequência: prompt na spec; prompt aponta para o archetype e para a pasta no JAR; agente lê entrada e índice; agente aplica as regras e materializa o contrato; resultado é a linha `mvn`. MUST declarar que o pré-processamento termina na linha e que a geração Maven começa depois.
- **FR-010**: O tópico MUST ter uma subseção curta por subpasta, pelos nomes atuais, com função e momento na sequência.
- **FR-011**: As suítes de contrato MUST validar o novo caminho e a contagem de 19 arquivos.
- **FR-012**: A verificação anti-drift MUST falhar se um documento operacional citar o caminho antigo fora do redirect, ou se contagem, subpastas ou distribuição divergirem.
- **FR-013**: O JAR instalado MUST conter o novo caminho e o redirect.
- **FR-014**: As specs históricas 003, 004 e 005 MUST NOT ser reescritas.
- **FR-015**: Uma spec antiga e uma spec nova MUST gerar com sucesso, com a mesma linha de comando.

### Key Entities

- **llm-maven-flags-inference**: raiz do procedimento; 19 arquivos em 4 subpastas.
- **Redirect**: artefato único no caminho antigo; aponta para o novo caminho e não contém regras.
- **Template de spec**: formulário do operador com o preâmbulo e as cinco entradas.
- **Documentação operacional**: README, templates de spec, suítes de contrato e arquivos do procedimento.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 19 arquivos no novo caminho; 0 regras duplicadas no redirect.
- **SC-002**: 100% das suítes de contrato passam.
- **SC-003**: Spec antiga e spec nova geram com a mesma linha de comando.
- **SC-004**: 0 documentos operacionais citam o caminho antigo fora do redirect.
- **SC-005**: O README tem 1 tópico e 4 subseções com os nomes atuais das subpastas.
- **SC-006**: Um leitor narra a sequência completa do prompt até a linha `mvn` usando só o tópico.
- **SC-007**: O JAR instalado contém o novo caminho e o redirect.

## Assumptions

- "Conteúdo dos 19 arquivos" significa o conteúdo normativo: as regras não mudam; apenas as referências de caminho são atualizadas, sem o que FR-006 e SC-004 seriam impossíveis.
- O redirect é um único arquivo no caminho antigo, no ponto de entrada que o preâmbulo antigo cita.
- O nome novo também vale para o conceito, portanto os templates de spec, plano e tarefas deixam de citar o nome anterior.
- A numeração 006 sucede 005; a 003 permanece arquivada.
- O README continua sendo documentação; o JAR instalado permanece a fonte de verdade em runtime.

## Out of Scope

- Renomear as 4 subpastas.
- Alterar conteúdo normativo ou contagem dos 19 arquivos.
- Reescrever as specs históricas 003, 004 e 005.
- Commit ou push.
