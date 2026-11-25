# Feature Specification: Renomear os Arquivos de Entrada e Operação do Procedimento

**Feature Branch**: `011-rename-procedure-entry-files`

**Created**: 2026-11-25

**Status**: Draft

**Input**: User description: "Renomear sete arquivos de duas subpastas do procedimento empacotado `llm-maven-flags-inference`, em `src/main/resources`, para que os nomes comecem pelo numeral e não repitam o papel da subpasta: na subpasta `01-entrada-inicializacao`, `prompt-00-leia-este-arquivo-primeiro.md` passa a `00-entry-point.md` e `indice-01-mapa-dos-arquivos-e-ordem-de-leitura.md` passa a `01-mapa-dos-arquivos-e-ordem-de-leitura.md`; na subpasta `02-operacao-procedimento-do-agente`, os cinco arquivos perdem o radical `prompt-`. Atualizar todas as referências operacionais aos nomes antigos, sem criar, remover ou mover arquivos e sem alterar o conteúdo normativo."

## Contexto e Problema

- O procedimento empacotado `llm-maven-flags-inference` tem 19 arquivos em quatro subpastas numeradas (01 a 04), sendo 13 Markdown e 6 JSON.
- As subpastas `01-entrada-inicializacao` e `02-operacao-procedimento-do-agente` ainda repetem o papel da subpasta no prefixo dos nomes (`prompt-`, `indice-`) e adiam o numeral, que é o critério de ordenação.
- Renomear os sete arquivos alinha os nomes ao numeral e à ordem de leitura, sem mudar o papel de cada arquivo, a ordem global ou a quantidade de arquivos.
- O índice, os próprios arquivos do procedimento, o README, o redirect legado e as suítes de verificação citam os nomes atuais; todas as referências operacionais precisam acompanhar a renomeação.
- O redirect legado da porta de entrada é uma exceção de compatibilidade: ele permanece no caminho antigo e passa a apontar para o novo nome.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Fase 1 — Subpasta de entrada e inicialização (Priority: P1)

Como mantenedor do procedimento, quando renomeio os dois arquivos da subpasta `01-entrada-inicializacao` para começar pelo numeral e atualizo todas as referências a eles, quero que a porta de entrada e o índice sejam encontrados pelo numeral, para que o gateway de leitura fique coerente e o procedimento continue funcionando.

**Why this priority**: é a primeira fatia independente; entrega a convenção para a porta de entrada e o índice sem renomear os cinco arquivos da subpasta 02.

**Independent Test**: listar a subpasta 01, conferir exatamente os dois novos nomes, a ausência dos dois nomes antigos como nomes de arquivo, o conteúdo normativo idêntico exceto pelas referências atualizadas, o índice com os novos caminhos, o redirect legado apontando para `00-entry-point.md`, a subpasta 02 com os cinco nomes atuais e as suítes de verificação passando.

**Acceptance Scenarios**:

1. **Given** os dois arquivos da subpasta 01 com os nomes antigos, **When** a renomeação é aplicada, **Then** existem exatamente `00-entry-point.md` e `01-mapa-dos-arquivos-e-ordem-de-leitura.md`, e nenhum dos dois nomes antigos permanece como nome de arquivo na subpasta 01.
2. **Given** os dois arquivos renomeados, **When** o conteúdo normativo é comparado ao anterior, **Then** ele é idêntico, exceto pelos tokens de referência aos nomes antigos.
3. **Given** o índice e as referências operacionais aos dois arquivos, **When** a atualização é aplicada, **Then** o índice lista os novos caminhos na mesma ordem e nenhuma referência textual aos dois nomes antigos permanece, exceto o caminho do redirect legado.
4. **Given** a renomeação da subpasta 01, **When** as duas subpastas são inspecionadas, **Then** os nomes das subpastas permanecem `01-entrada-inicializacao` e `02-operacao-procedimento-do-agente`, os cinco arquivos da subpasta 02 mantêm os nomes atuais e a ordem de leitura e o papel dos dois arquivos renomeados permanecem os mesmos, ainda que as referências aos dois arquivos renomeados sejam atualizadas.
5. **Given** o redirect legado, **When** ele é lido, **Then** ele permanece em `utils/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md` e aponta para `00-entry-point.md`.

---

### User Story 2 - Fase 2 — Subpasta de operação e procedimento do agente (Priority: P2)

Como mantenedor do procedimento, quando removo o radical `prompt-` dos cinco arquivos da subpasta `02-operacao-procedimento-do-agente` e atualizo todas as referências a eles, quero que os nomes comecem pelo numeral, para que o bloco de operação siga a mesma convenção e nenhum nome antigo permaneça no corpus operacional.

**Why this priority**: completa a convenção nas duas subpastas; depende da Fase 1 apenas para a conferência final de zero nomes antigos, não para a própria renomeação.

**Independent Test**: listar a subpasta 02, conferir exatamente os cinco novos nomes, a ausência de `prompt-*`, o conteúdo normativo idêntico exceto pelas referências atualizadas, o índice com os novos caminhos, todas as referências operacionais atualizadas, o total de 19 arquivos, 13 Markdown e 6 JSON, e as suítes de verificação passando.

**Acceptance Scenarios**:

1. **Given** os cinco arquivos da subpasta 02 com o prefixo `prompt-`, **When** a renomeação é aplicada, **Then** existem exatamente `01-papel-e-restricoes-do-agente.md`, `02-como-localizar-e-ler-o-archetype.md`, `03-regras-obrigatorias-de-geracao.md`, `04-fluxo-de-execucao-e-verificacao.md` e `05-entregaveis-e-relatorios.md`, e nenhum arquivo `prompt-*` permanece.
2. **Given** os cinco arquivos renomeados, **When** o conteúdo normativo é comparado ao anterior, **Then** ele é idêntico, exceto pelos tokens de referência aos nomes antigos.
3. **Given** o índice e as referências operacionais aos cinco arquivos, **When** a atualização é aplicada, **Then** o índice lista os 19 arquivos com os novos nomes e nenhuma referência textual aos cinco nomes antigos permanece.
4. **Given** as duas subpastas e o procedimento completo, **When** os arquivos são contados, **Then** há 19 arquivos, 13 Markdown e 6 JSON, em quatro subpastas, com dois arquivos em 01 e cinco em 02, os nomes das subpastas permanecem inalterados e a ordem de leitura e o papel dos cinco arquivos renomeados permanecem os mesmos.
5. **Given** todos os sete arquivos renomeados, **When** o corpus operacional é verificado, **Then** nenhum dos sete nomes antigos permanece como nome de arquivo ou referência textual, exceto o caminho do redirect legado da porta de entrada.

---

### Edge Cases

- Um novo nome colide com um nome existente: a renomeação deve parar e reportar a colisão; nenhum arquivo pode ser sobrescrito.
- O sistema de arquivos é insensível a maiúsculas e minúsculas: o novo nome deve ser exatamente o nome especificado.
- Um nome antigo permanece em uma referência operacional: a atualização está incompleta e a feature falha no critério de referências.
- A contagem de arquivos muda: o resultado viola o invariante; a mudança deve ser rejeitada.
- O conteúdo normativo de um arquivo muda durante a renomeação: o resultado viola o invariante; a mudança deve ser rejeitada.
- A subpasta 02 tem os cinco nomes alterados na Fase 1: a fatia deixa de ser independente; a mudança deve ser rejeitada.
- O redirect legado é removido, renomeado ou deixa de apontar para `00-entry-point.md`: a compatibilidade é quebrada; a mudança deve ser rejeitada.
- Um nome antigo aparece apenas como parte de outro nome ou de um texto histórico: somente as referências operacionais exatas devem ser atualizadas.
- Specs históricas, planos, tarefas e saída gerada citam nomes antigos: não são corpus operacional; permanecem fora do escopo.
- O artefato empacotado é construído antes da renomeação: ele pode continuar expondo os nomes antigos; a verificação no artefato não é gate desta feature.
- O apóstrofo final em `00-entry-point'` é lido como parte do nome: o novo nome é `00-entry-point.md`, sem apóstrofo.
- Um arquivo renomeado é usado como ponto de entrada: somente `00-entry-point.md` é a porta de entrada; os outros seis não precisam de redirect.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Os dois arquivos da subpasta `01-entrada-inicializacao` MUST ser renomeados exatamente para `00-entry-point.md` e `01-mapa-dos-arquivos-e-ordem-de-leitura.md`.
- **FR-002**: Os cinco arquivos da subpasta `02-operacao-procedimento-do-agente` MUST ser renomeados exatamente para `01-papel-e-restricoes-do-agente.md`, `02-como-localizar-e-ler-o-archetype.md`, `03-regras-obrigatorias-de-geracao.md`, `04-fluxo-de-execucao-e-verificacao.md` e `05-entregaveis-e-relatorios.md`.
- **FR-003**: Nenhum dos sete nomes antigos MUST permanecer como nome de arquivo nas duas subpastas.
- **FR-004**: O conteúdo normativo dos sete arquivos MUST permanecer inalterado; apenas as referências textuais aos nomes antigos podem ser atualizadas.
- **FR-005**: Nenhum arquivo MUST ser criado, removido ou movido; o procedimento MUST manter 19 arquivos, 13 Markdown e 6 JSON, em quatro subpastas, com dois arquivos em 01 e cinco em 02.
- **FR-006**: Os nomes das subpastas `01-entrada-inicializacao` e `02-operacao-procedimento-do-agente` MUST permanecer inalterados.
- **FR-007**: A ordem de leitura e o papel de cada um dos sete arquivos no procedimento MUST permanecer inalterados.
- **FR-008**: Todas as referências operacionais aos sete nomes antigos MUST ser atualizadas, incluindo o índice, os próprios arquivos do procedimento, o README, o redirect legado e as suítes de verificação, inclusive formas curtas como `prompt-00` e `prompt-03`.
- **FR-009**: Nenhuma referência operacional aos sete nomes antigos MUST permanecer após a atualização, exceto o caminho do redirect legado da porta de entrada e a verificação que o assegura; nenhum caminho operacional pode ficar quebrado.
- **FR-010**: O índice MUST listar os 19 arquivos do procedimento com os novos nomes e MUST permanecer a única fonte da ordem global.
- **FR-011**: O redirect legado MUST permanecer em `utils/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md` e MUST apontar para `00-entry-point.md`; nenhum redirect MUST ser criado para os outros seis arquivos renomeados.
- **FR-012**: Specs históricas, planos, tarefas, saída gerada em `target/` e artefato instalado MUST NOT ser reescritos por esta feature.
- **FR-013**: As suítes de verificação MUST ser atualizadas para os novos nomes e MUST passar após a renomeação.
- **FR-014**: Nenhuma regra, definição, exemplo de domínio, metadado, default ou formato de contrato MUST ser alterado; somente nomes de arquivo e referências textuais a eles mudam.
- **FR-015**: O novo nome da porta de entrada MUST ser `00-entry-point.md`, tratando o apóstrofo final da demanda como pontuação e não como parte do nome.

### Key Entities

- **Arquivo renomeado**: arquivo do procedimento com nome antigo, nome novo, subpasta, papel e conteúdo normativo preservado.
- **Subpasta do procedimento**: `01-entrada-inicializacao` ou `02-operacao-procedimento-do-agente`, com nome fixo e, respectivamente, dois e cinco arquivos.
- **Procedimento empacotado**: conjunto de 19 arquivos em quatro subpastas, 13 Markdown e 6 JSON, lido pelo agente.
- **Referência operacional**: ocorrência de um nome de arquivo no índice, nos arquivos do procedimento, no README, no redirect legado ou nas suítes de verificação.
- **Redirect legado**: único arquivo mantido no caminho antigo da porta de entrada para compatibilidade, apontando para `00-entry-point.md`.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 7 de 7 arquivos renomeados; 0 nomes antigos permanecem como nomes de arquivo nas duas subpastas.
- **SC-002**: 7 de 7 conteúdos normativos idênticos ao anterior; apenas referências textuais aos nomes antigos foram atualizadas.
- **SC-003**: O procedimento mantém 19 arquivos, 13 Markdown e 6 JSON, em quatro subpastas, com dois arquivos em 01 e cinco em 02.
- **SC-004**: 0 nomes de subpasta alterados; 0 arquivos criados, removidos ou movidos.
- **SC-005**: 100% das referências operacionais aos sete nomes antigos atualizadas; 0 referências quebradas no índice, nos arquivos do procedimento, no README, no redirect legado e nas suítes de verificação.
- **SC-006**: O redirect legado permanece no caminho antigo e aponta para `00-entry-point.md`; 0 redirects criados para os outros seis arquivos.
- **SC-007**: 0 mudanças em regras, definições, exemplos de domínio, metadados, defaults ou formato do contrato; apenas nomes e referências textuais mudam.
- **SC-008**: A ordem de leitura e o papel de cada um dos sete arquivos permanecem os mesmos.
- **SC-009**: As suítes de verificação existentes passam após a atualização das expectativas de nome.
- **SC-010**: Se o procedimento for empacotado novamente, o artefato expõe os novos nomes e nenhum nome antigo, exceto o caminho do redirect legado; a verificação no artefato instalado não é gate desta feature.
- **SC-011**: 0 referências textuais ao nome antigo da porta de entrada fora do caminho do redirect legado e da verificação que o assegura.

## Assumptions

- O novo nome da porta de entrada é `00-entry-point.md`; o apóstrofo final em `00-entry-point'` é pontuação da demanda.
- Os sete arquivos são os únicos com os prefixos `prompt-` e `indice-` nas duas subpastas.
- Os novos nomes não colidem com nomes existentes e podem ser aplicados no sistema de arquivos em uso.
- O sistema de arquivos preserva o resultado exato dos nomes, inclusive em caso de insensibilidade a maiúsculas e minúsculas.
- O redirect legado é o único arquivo que mantém um nome antigo, por compatibilidade com specs anteriores.
- Specs históricas, planos, tarefas e saída gerada não são corpus operacional e permanecem fora da atualização.
- A renomeação é nominal; o conteúdo normativo dos sete arquivos não muda, exceto pelas referências textuais aos nomes antigos.
- A ordem de leitura e o papel de cada arquivo permanecem os mesmos.
- A verificação no artefato empacotado não está definida como gate; se o artefato for reconstruído, espera-se que reflita os novos nomes.
- A feature não define meta de performance própria; o orçamento de build existente pode ser usado apenas como guarda de regressão.

## Out of Scope

- Renomear ou mover as subpastas `01-entrada-inicializacao` e `02-operacao-procedimento-do-agente`.
- Alterar o conteúdo normativo dos sete arquivos; somente referências textuais aos nomes antigos são atualizadas.
- Criar, remover ou mover arquivos do procedimento.
- Criar redirects para os seis arquivos que não são a porta de entrada.
- Renomear ou remover o redirect legado da porta de entrada.
- Reescrever specs históricas, planos, tarefas ou saída gerada.
- Verificar o artefato instalado como gate de aceite desta feature.
- Alterar regras, catálogo, estrutura de templates, ordem de leitura ou formato do contrato.

## Compliance Constraints

- Não criar arquivos novos em `llm-maven-flags-inference` nem remover os existentes.
- Não alterar os nomes das subpastas.
- Não alterar regras, catálogo, estrutura de templates, ordem de leitura ou formato do contrato.
- Não editar `target/` nem o artefato instalado à mão.
- Toda alteração deve passar pelas suítes de verificação existentes.
- O redirect legado deve continuar funcional.
