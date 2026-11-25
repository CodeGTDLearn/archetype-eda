# Feature Specification: Renomear os Arquivos das Subpastas 03 e 04 do Procedimento

**Feature Branch**: `010-rename-procedure-files`

**Created**: 2026-11-25

**Status**: Draft

**Input**: User description: "Renomear os 12 arquivos das duas últimas subpastas do procedimento empacotado `llm-maven-flags-inference`: remover o prefixo `regras-` dos seis arquivos da subpasta 03 e o prefixo `template-` dos seis arquivos da subpasta 04, preservando o restante do nome e o conteúdo normativo, e atualizar todas as referências aos arquivos renomeados na extensão do projeto."

## Contexto e Problema

- O procedimento empacotado `llm-maven-flags-inference` tem 19 arquivos em quatro subpastas numeradas (01 a 04). As subpastas 03 e 04 têm seis arquivos cada.
- Os nomes atuais repetem o papel da subpasta no prefixo (`regras-` em 03, `template-` em 04) e adiam o numeral, que é o critério de ordenação.
- Remover os prefixos alinha os nomes ao numeral e à numeração das subpastas, sem alterar o conteúdo normativo, a ordem de leitura ou a quantidade de arquivos.
- O índice, os prompts, as regras, os exemplos, o README e as suítes de teste citam os nomes atuais. A demanda exige que todas essas referências sejam atualizadas, incluindo as referências internas aos 12 arquivos.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Fase 1 — Renomear os arquivos de derivação (Priority: P1)

Como mantenedor do procedimento, quando removo o prefixo `regras-` dos seis arquivos da subpasta 03, quero que os nomes comecem no numeral `01`–`06` e que as referências a esses arquivos sejam atualizadas, para que a subpasta use a mesma convenção de numeração das pastas e o leitor encontre a regra pelo numeral, sem alterar o conteúdo normativo.

**Why this priority**: é a primeira fatia independente; entrega a convenção e as referências da subpasta 03 sem tocar nos arquivos da subpasta 04.

**Independent Test**: listar a subpasta 03, conferir exatamente os seis novos nomes, a ausência de `regras-*`, o conteúdo normativo idêntico, as referências aos arquivos de 03 atualizadas e a subpasta 04 intacta.

**Acceptance Scenarios**:

1. **Given** os seis arquivos da subpasta 03 com o prefixo `regras-`, **When** a renomeação é aplicada, **Then** existem exatamente os seis novos nomes e nenhum arquivo `regras-*` permanece.
2. **Given** os seis arquivos renomeados, **When** o conteúdo normativo é comparado ao anterior, **Then** ele é idêntico, exceto pelos tokens de referência atualizados.
3. **Given** a renomeação da subpasta 03, **When** a subpasta 04 é inspecionada, **Then** seus seis arquivos permanecem inalterados.
4. **Given** as referências aos arquivos da subpasta 03 no índice, prompts, regras, exemplos e suítes de teste, **When** a atualização é aplicada, **Then** nenhuma referência aos nomes antigos de 03 permanece.

---

### User Story 2 - Fase 2 — Renomear os arquivos de referência (Priority: P2)

Como mantenedor do procedimento, quando removo o prefixo `template-` dos seis arquivos da subpasta 04, quero que os nomes comecem no numeral `01`–`06` e que as referências a esses arquivos sejam atualizadas, para que os templates e exemplos sigam a mesma convenção, sem alterar o conteúdo normativo.

**Why this priority**: completa a convenção nas duas subpastas; depende da primeira fatia apenas para a contagem final, não para a própria renomeação.

**Independent Test**: listar a subpasta 04, conferir os seis novos nomes, a ausência de `template-*`, o conteúdo normativo idêntico, as referências aos arquivos de 04 atualizadas e o total de 19 arquivos.

**Acceptance Scenarios**:

1. **Given** os seis arquivos da subpasta 04 com o prefixo `template-`, **When** a renomeação é aplicada, **Then** existem exatamente os seis novos nomes e nenhum arquivo `template-*` permanece.
2. **Given** os seis arquivos renomeados, **When** o conteúdo normativo é comparado ao anterior, **Then** ele é idêntico, exceto pelos tokens de referência atualizados.
3. **Given** as duas subpastas, **When** os arquivos são contados, **Then** há 19 arquivos no procedimento, 13 Markdown e 6 JSON, em quatro subpastas.
4. **Given** as referências aos arquivos da subpasta 04 no índice, prompts, exemplos, README e suítes de teste, **When** a atualização é aplicada, **Then** nenhuma referência aos nomes antigos de 04 permanece.

---

### Edge Cases

- Um novo nome colide com um nome existente: a renomeação deve parar e reportar a colisão; nenhum arquivo pode ser sobrescrito.
- O sistema de arquivos é insensível a maiúsculas e minúsculas: o novo nome difere apenas pelo prefixo removido; o resultado deve ser exatamente o novo nome.
- Um nome antigo permanece em uma referência operacional: a atualização está incompleta e a feature falha no critério de referências.
- A contagem de arquivos muda: o resultado viola o invariante; a mudança deve ser rejeitada.
- O conteúdo normativo de um arquivo muda durante a renomeação: o resultado viola o invariante; a mudança deve ser rejeitada.
- A subpasta 04 é tocada durante a Fase 1: a fatia deixa de ser independente; a mudança deve ser rejeitada.
- Specs históricas, planos, tarefas e saída gerada citam nomes antigos: não são fontes operacionais; permanecem fora do escopo.
- O artefato empacotado é construído antes da renomeação: ele pode continuar expondo os nomes antigos; a verificação no artefato não está definida nesta feature.
- Um nome antigo é usado como ponto de entrada: os arquivos renomeados não são pontos de entrada; o ponto de entrada continua sendo `prompt-00`, que não muda de nome.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Os seis arquivos da subpasta `03-derivacao-regras-de-formacao-do-contrato` MUST ser renomeados removendo o prefixo `regras-`, preservando o restante do nome.
- **FR-002**: Os seis arquivos da subpasta `04-referencia-templates-de-contrato` MUST ser renomeados removendo o prefixo `template-`, preservando o restante do nome.
- **FR-003**: Os novos nomes MUST ser exatamente: na subpasta 03, `01-como-derivar-a-entidade-do-contrato.md`, `02-como-resolver-tipos-e-enums.md`, `03-como-derivar-flags-de-tipos-aninhados.md`, `04-como-aplicar-precedencia-e-validacao.md`, `05-como-materializar-e-emitir-o-contrato.md` e `06-como-relatar-proveniencia.md`; na subpasta 04, `01-schema-do-contrato.json`, `02-exemplo-contrato-plano.json`, `03-exemplo-contrato-com-objetos-aninhados.json`, `04-exemplo-contrato-com-enums.json`, `05-exemplo-contrato-com-tipos-do-projeto.json` e `06-exemplo-contrato-com-attributes-explicito.json`.
- **FR-004**: Nenhum dos 12 nomes antigos MUST permanecer nas duas subpastas.
- **FR-005**: O conteúdo normativo dos 12 arquivos MUST permanecer inalterado; apenas as referências textuais aos nomes antigos podem ser atualizadas, sem mudar regras, definições, exemplos de domínio ou metadados estruturais.
- **FR-006**: Nenhum arquivo MUST ser criado ou removido; as duas subpastas MUST manter seis arquivos cada e o procedimento MUST manter 19 arquivos, 13 Markdown e 6 JSON, em quatro subpastas.
- **FR-007**: Os nomes das subpastas MUST permanecer `03-derivacao-regras-de-formacao-do-contrato` e `04-referencia-templates-de-contrato`.
- **FR-008**: A ordem de leitura e o papel de cada um dos 12 arquivos no procedimento MUST permanecer inalterados.
- **FR-009**: Todas as referências operacionais aos nomes antigos MUST ser atualizadas para os novos nomes no índice, nos prompts, nas regras, nos exemplos, no README e nas suítes de teste, incluindo as referências internas aos 12 arquivos.
- **FR-010**: Nenhuma referência operacional aos 12 nomes antigos MUST permanecer no corpus operacional após a atualização; nenhum caminho interno do procedimento pode ficar quebrado.
- **FR-011**: Specs históricas, planos, tarefas e saída gerada MUST NOT ser reescritos; eles não fazem parte do corpus operacional desta feature.
- **FR-012**: Nenhum arquivo de redirect MUST ser criado para os nomes antigos; os arquivos renomeados não são pontos de entrada do procedimento.

### Key Entities

- **Arquivo renomeado**: arquivo do procedimento com nome antigo, nome novo, subpasta e conteúdo normativo preservado.
- **Subpasta do procedimento**: subpasta 03 ou 04, com seis arquivos e nome fixo.
- **Procedimento empacotado**: conjunto de 19 arquivos em quatro subpastas, 13 Markdown e 6 JSON, lido pelo agente em runtime.
- **Referência operacional**: ocorrência de um nome de arquivo no índice, nos prompts, nas regras, nos exemplos, no README ou nas suítes de teste.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 12 de 12 arquivos renomeados; 0 nomes antigos permanecem nas duas subpastas.
- **SC-002**: 12 de 12 conteúdos normativos idênticos ao anterior; apenas referências a nomes antigos foram atualizadas.
- **SC-003**: O procedimento mantém 19 arquivos, 13 Markdown e 6 JSON, em quatro subpastas, com seis arquivos em 03 e seis em 04.
- **SC-004**: 0 nomes de subpasta alterados.
- **SC-005**: 100% das referências operacionais aos nomes antigos atualizadas; 0 referências quebradas no índice, prompts, regras, exemplos, README e suítes de teste.
- **SC-006**: 0 arquivos criados ou removidos; 0 redirects criados.
- **SC-007**: 0 mudanças em regras, catálogo, estrutura de templates, ordem de leitura ou formato do contrato; apenas referências textuais aos nomes antigos são atualizadas.
- **SC-008**: O procedimento continua legível na mesma ordem e cada arquivo mantém seu papel.
- **SC-009**: As suítes de verificação existentes passam após a atualização.
- **SC-010**: Se o procedimento for empacotado novamente, o artefato expõe os novos nomes e nenhum nome antigo; a verificação no artefato instalado não é gate desta feature.

## Assumptions

- "Todas as referências na extensão do projeto" significa o corpus operacional: índice, prompts, regras, exemplos, README e suítes de teste, incluindo as referências internas aos 12 arquivos.
- Specs históricas, planos, tarefas e saída gerada não são fontes operacionais e permanecem fora da atualização, conforme o precedente da feature 006.
- Os 12 arquivos são os únicos com os prefixos `regras-` e `template-` nas duas subpastas.
- Os novos nomes não colidem com nomes existentes e podem ser aplicados no sistema de arquivos em uso.
- A renomeação é nominal; o conteúdo normativo dos 12 arquivos não muda. Referências textuais aos nomes antigos podem ser atualizadas dentro deles.
- As subpastas 03 e 04 mantêm os nomes atuais.
- A ordem de leitura e o papel de cada arquivo permanecem os mesmos.
- Os arquivos renomeados não são pontos de entrada; o ponto de entrada continua sendo `prompt-00`, que não muda de nome, portanto não há redirect.
- A verificação no artefato empacotado não está definida; se o artefato for reconstruído, espera-se que reflita os novos nomes.
- A feature não define meta de performance própria; o orçamento de build existente pode ser usado apenas como guarda de regressão.

## Out of Scope

- Alterar o conteúdo normativo dos 12 arquivos; referências textuais aos nomes antigos são atualizadas.
- Renomear ou mover as subpastas 03 e 04.
- Criar ou remover arquivos do procedimento.
- Criar redirects para os nomes antigos.
- Alterar regras, catálogo, estrutura de templates, ordem de leitura ou formato do contrato.
- Reescrever specs históricas, planos, tarefas ou saída gerada.
- Verificar o artefato instalado como gate de aceite desta feature.
