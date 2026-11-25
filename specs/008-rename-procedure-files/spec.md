# Feature Specification: Renomear os Arquivos das Subpastas 03 e 04 do Procedimento

**Feature Branch**: `009-rename-procedure-files`

**Created**: 2026-11-25

**Status**: Draft

**Input**: User description: "Renomear os 12 arquivos das duas últimas subpastas do procedimento empacotado `llm-maven-flags-inference`: remover o prefixo `regras-` dos seis arquivos da subpasta 03 e o prefixo `template-` dos seis arquivos da subpasta 04, mantendo o restante do nome e o conteúdo, sem criar nem remover arquivos."

## Contexto e Problema

- O procedimento empacotado `llm-maven-flags-inference` tem 19 arquivos em quatro subpastas numeradas (01 a 04). As subpastas 03 e 04 têm seis arquivos cada.
- Os nomes atuais repetem o papel da subpasta no prefixo (`regras-` em 03, `template-` em 04) e adiam o numeral, que é o critério de ordenação.
- Remover os prefixos alinha os nomes ao numeral e à numeração das subpastas, sem alterar o conteúdo normativo, a ordem de leitura ou a quantidade de arquivos.
- O índice, os prompts, as regras, os exemplos, o README e as suítes de teste citam os nomes atuais. A decisão registrada nesta spec é atualizar todas essas referências, incluindo as que aparecem dentro dos próprios 12 arquivos.

## Clarifications

### Session 2026-11-25

- Q: As referências operacionais aos nomes antigos devem ser atualizadas nesta feature? → A: Sim — todas: índice, prompts, regras, exemplos, README e suítes de teste. As referências dentro dos 12 arquivos também são atualizadas; o conteúdo normativo permanece.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Renomear os arquivos de derivação (Priority: P1)

Como mantenedor do procedimento, quando removo o prefixo `regras-` dos seis arquivos da subpasta 03, quero que os nomes comecem no numeral `01`–`06`, para que a subpasta use a mesma convenção de numeração das pastas e o leitor encontre a regra pelo numeral, sem alterar o conteúdo.

**Why this priority**: é a primeira fatia independente; entrega a convenção para as regras de derivação sem tocar na subpasta 04.

**Independent Test**: listar a subpasta 03, conferir exatamente os seis novos nomes, a ausência de `regras-*`, o conteúdo idêntico e a subpasta 04 intacta.

**Acceptance Scenarios**:

1. **Given** os seis arquivos da subpasta 03 com o prefixo `regras-`, **When** a renomeação é aplicada, **Then** existem exatamente os seis novos nomes e nenhum arquivo `regras-*` permanece.
2. **Given** os seis arquivos renomeados, **When** o conteúdo é comparado ao anterior, **Then** ele é idêntico.
3. **Given** a renomeação da subpasta 03, **When** a subpasta 04 é inspecionada, **Then** ela permanece inalterada.
4. **Given** a subpasta 03, **When** os arquivos são contados, **Then** há exatamente seis arquivos.

---

### User Story 2 - Renomear os arquivos de referência (Priority: P2)

Como mantenedor do procedimento, quando removo o prefixo `template-` dos seis arquivos da subpasta 04, quero que os nomes comecem no numeral `01`–`06`, para que os templates e exemplos sigam a mesma convenção, sem alterar o conteúdo.

**Why this priority**: completa a convenção nas duas subpastas; depende da primeira fatia apenas para a contagem final, não para a própria renomeação.

**Independent Test**: listar a subpasta 04, conferir os seis novos nomes, a ausência de `template-*`, o conteúdo idêntico e o total de 19 arquivos.

**Acceptance Scenarios**:

1. **Given** os seis arquivos da subpasta 04 com o prefixo `template-`, **When** a renomeação é aplicada, **Then** existem exatamente os seis novos nomes e nenhum arquivo `template-*` permanece.
2. **Given** os seis arquivos renomeados, **When** o conteúdo é comparado ao anterior, **Then** ele é idêntico.
3. **Given** as duas subpastas, **When** os arquivos são contados, **Then** há 19 arquivos no procedimento, 13 Markdown e 6 JSON, em quatro subpastas.
4. **Given** as subpastas 03 e 04, **When** seus nomes são inspecionados, **Then** eles permanecem os atuais.

---

### Edge Cases

- Um novo nome colide com um nome existente: a renomeação deve parar e reportar a colisão; nenhum arquivo pode ser sobrescrito.
- O sistema de arquivos é insensível a maiúsculas e minúsculas: o novo nome difere apenas pelo prefixo removido; o resultado deve ser exatamente o novo nome.
- Um nome antigo permanece em uma referência operacional: a atualização está incompleta e a feature falha no critério de referências.
- A contagem de arquivos muda: o resultado viola o invariante; a mudança deve ser rejeitada.
- O conteúdo normativo de um arquivo muda durante a renomeação: o resultado viola o invariante; a mudança deve ser rejeitada.
- A subpasta 04 é tocada durante a User Story 1: a fatia deixa de ser independente; a mudança deve ser rejeitada.
- Specs históricas, planos, tarefas e saída gerada citam nomes antigos: não são fontes operacionais; permanecem fora do escopo.
- O artefato empacotado é construído antes da renomeação: ele pode continuar expondo os nomes antigos; a verificação no artefato não está definida nesta feature.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Os seis arquivos da subpasta `03-derivacao-regras-de-formacao-do-contrato` MUST ser renomeados removendo o prefixo `regras-`, preservando o restante do nome.
- **FR-002**: Os seis arquivos da subpasta `04-referencia-templates-de-contrato` MUST ser renomeados removendo o prefixo `template-`, preservando o restante do nome.
- **FR-003**: Os novos nomes MUST ser exatamente: na subpasta 03, `01-como-derivar-a-entidade-do-contrato.md`, `02-como-resolver-tipos-e-enums.md`, `03-como-derivar-flags-de-tipos-aninhados.md`, `04-como-aplicar-precedencia-e-validacao.md`, `05-como-materializar-e-emitir-o-contrato.md` e `06-como-relatar-proveniencia.md`; na subpasta 04, `01-schema-do-contrato.json`, `02-exemplo-contrato-plano.json`, `03-exemplo-contrato-com-objetos-aninhados.json`, `04-exemplo-contrato-com-enums.json`, `05-exemplo-contrato-com-tipos-do-projeto.json` e `06-exemplo-contrato-com-attributes-explicito.json`.
- **FR-004**: Nenhum dos 12 nomes antigos MUST permanecer nas duas subpastas.
- **FR-005**: O conteúdo normativo dos 12 arquivos MUST permanecer inalterado; apenas as referências textuais aos nomes antigos podem ser atualizadas, sem mudar regras, definições, exemplos de domínio ou metadados.
- **FR-006**: Nenhum arquivo MUST ser criado ou removido; as duas subpastas MUST manter seis arquivos cada e o procedimento MUST manter 19 arquivos, 13 Markdown e 6 JSON, em quatro subpastas.
- **FR-007**: Os nomes das subpastas MUST permanecer `03-derivacao-regras-de-formacao-do-contrato` e `04-referencia-templates-de-contrato`.
- **FR-008**: A ordem de leitura e o papel de cada um dos 12 arquivos no procedimento MUST permanecer inalterados.
- **FR-009**: As referências operacionais aos nomes antigos no índice, nos prompts, nas regras, nos exemplos, no README e nas suítes de teste MUST ser atualizadas para os novos nomes, incluindo as referências internas aos 12 arquivos.

### Key Entities

- **Arquivo renomeado**: arquivo do procedimento com nome antigo, nome novo, subpasta e conteúdo normativo preservado.
- **Subpasta do procedimento**: subpasta 03 ou 04, com seis arquivos e nome fixo.
- **Procedimento empacotado**: conjunto de 19 arquivos em quatro subpastas, 13 Markdown e 6 JSON, lido pelo agente em runtime.
- **Referência operacional**: ocorrência de um nome de arquivo no índice, nos prompts, nas regras, nos exemplos, no README ou nas suítes de teste.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 12 de 12 arquivos renomeados; 0 nomes antigos permanecem nas duas subpastas.
- **SC-002**: 12 de 12 conteúdos normativos idênticos ao anterior; apenas referências a nomes antigos foram atualizadas.
- **SC-003**: O procedimento mantém 19 arquivos, 13 Markdown e 6 JSON, em quatro subpastas.
- **SC-004**: Cada subpasta mantém seis arquivos; 0 colisões de nome e 0 arquivos sobrescritos.
- **SC-005**: 0 nomes de subpasta alterados.
- **SC-006**: 100% das referências operacionais aos nomes antigos atualizadas; 0 referências quebradas no índice, prompts, regras, exemplos, README e suítes de teste.
- **SC-007**: 0 arquivos criados ou removidos.
- **SC-008**: 0 mudanças em regras, catálogo, estrutura de templates, ordem de leitura ou formato do contrato; apenas referências textuais aos nomes antigos são atualizadas.
- **SC-009**: O procedimento continua legível na mesma ordem e cada arquivo mantém seu papel.
- **SC-010**: Se o procedimento for empacotado novamente, o artefato expõe os novos nomes e nenhum nome antigo; a verificação no artefato não está definida nesta feature.

## Assumptions

- Os 12 arquivos são os únicos com os prefixos `regras-` e `template-` nas duas subpastas.
- Os novos nomes não colidem com nomes existentes e podem ser aplicados no sistema de arquivos em uso.
- A renomeação é nominal; o conteúdo normativo dos 12 arquivos não muda. Referências textuais aos nomes antigos podem ser atualizadas dentro deles.
- As subpastas 03 e 04 mantêm os nomes atuais.
- A ordem de leitura e o papel de cada arquivo permanecem os mesmos.
- Specs históricas, planos, tarefas e saída gerada não são reescritos.
- Todas as referências operacionais aos nomes antigos são atualizadas nesta feature, inclusive as internas aos 12 arquivos.
- A verificação no artefato empacotado não está definida; se o artefato for reconstruído, espera-se que reflita os novos nomes.

## Out of Scope

- Alterar o conteúdo normativo dos 12 arquivos; referências textuais aos nomes antigos são atualizadas.
- Renomear ou mover as subpastas 03 e 04.
- Criar ou remover arquivos do procedimento.
- Alterar regras, catálogo, estrutura de templates, ordem de leitura ou formato do contrato; referências textuais aos nomes antigos são atualizadas.
- Reescrever specs históricas, planos, tarefas ou saída gerada.
- Verificar o artefato instalado, enquanto essa verificação não for definida.
