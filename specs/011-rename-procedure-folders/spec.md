# Feature Specification: Renomear as Subpastas Iniciais e o Arquivo de Leitura do Procedimento

**Feature Branch**: `012-rename-procedure-folders`

**Created**: 2026-11-26

**Status**: Draft

**Input**: User description: "Renomear duas subpastas numeradas do procedimento empacotado e um arquivo da segunda subpasta: `01-entrada-inicializacao` passa a `01-inicializacao`; `02-operacao-procedimento-do-agente` passa a `02-procedimento-do-agente`; e, dentro da pasta que passa a se chamar `02-procedimento-do-agente`, o arquivo `02, como localizar e ler o archtype` passa a `02 localizar e ler o archetype`. Concluídas as renomeações, atualizar todos os itens do projeto que fazem referência a essas alterações, com os novos nomes."

## Contexto e Problema

- O procedimento empacotado `llm-maven-flags-inference/` tem 19 arquivos em quatro subpastas numeradas (01 a 04), sendo 13 documentos e 6 exemplos de contrato.
- As duas primeiras subpastas ainda carregam nomes longos que repetem o papel da categoria (`entrada-inicializacao` e `operacao-procedimento-do-agente`), e o arquivo 02 da segunda subpasta repete o papel da subpasta e usa uma descrição longa.
- Renomear as duas subpastas e o arquivo 02 encurta os nomes, mantém o numeral como critério de ordenação e preserva o papel de cada item, sem alterar o conteúdo normativo, a ordem de leitura ou a quantidade de arquivos.
- O índice, a porta de entrada, o README, o redirect legado e as suítes de verificação citam os nomes atuais; todas as referências operacionais precisam acompanhar a renomeação.
- O redirect legado da porta de entrada é uma exceção de compatibilidade: ele permanece no caminho atual e passa a apontar para a porta de entrada no caminho novo.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Fase 1 — Subpasta de inicialização (Priority: P1)

Como mantenedor do procedimento, quando renomeio a primeira subpasta para `01-inicializacao` e atualizo todas as referências a ela, quero que a porta de entrada e o índice sejam encontrados pelo nome curto, para que o gateway de leitura fique coerente e o procedimento continue funcionando.

**Why this priority**: é a primeira fatia independente; entrega a convenção para a subpasta de entrada e inicialização sem renomear a segunda subpasta nem o arquivo 02.

**Independent Test**: listar a primeira subpasta, conferir exatamente os dois arquivos com os nomes preservados, a ausência da subpasta antiga, o índice com os novos caminhos, o redirect legado apontando para o caminho novo, a segunda subpasta intacta e as suítes de verificação passando.

**Acceptance Scenarios**:

1. **Given** a subpasta `llm-maven-flags-inference/01-entrada-inicializacao` com dois arquivos, **When** a renomeação é aplicada, **Then** existe exatamente `llm-maven-flags-inference/01-inicializacao` com `00-entry-point.md` e `01-mapa-dos-arquivos-e-ordem-de-leitura.md`, e a subpasta antiga não existe mais.
2. **Given** os dois arquivos renomeados, **When** o conteúdo é comparado ao anterior, **Then** ele é idêntico, exceto pelos tokens de referência ao caminho da subpasta.
3. **Given** o índice, a porta de entrada, o README, o redirect legado e as suítes de verificação, **When** a atualização é aplicada, **Then** nenhuma referência operacional ao caminho `llm-maven-flags-inference/01-entrada-inicializacao` permanece.
4. **Given** o redirect legado, **When** ele é lido, **Then** ele permanece em `utils/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md` e aponta para `llm-maven-flags-inference/01-inicializacao/00-entry-point.md`.
5. **Given** a renomeação da primeira subpasta, **When** as duas subpastas são inspecionadas, **Then** a segunda subpasta mantém o nome `02-operacao-procedimento-do-agente`, seus cinco arquivos mantêm os nomes atuais e o procedimento mantém 19 arquivos, 13 documentos e 6 exemplos em quatro subpastas.
6. **Given** o índice, **When** ele é lido, **Then** os dois arquivos da primeira subpasta aparecem nas posições 1 e 2, com os novos caminhos e os mesmos papéis.

---

### User Story 2 - Fase 2 — Subpasta de procedimento do agente (Priority: P2)

Como mantenedor do procedimento, quando renomeio a segunda subpasta para `02-procedimento-do-agente` e atualizo todas as referências a ela, quero que o bloco de operação do agente use o nome curto, para que nenhuma referência à subpasta antiga permaneça no corpus operacional.

**Why this priority**: completa a convenção de nomes das subpastas; depende da Fase 1 apenas para a conferência final de zero nomes antigos, não para a própria renomeação.

**Independent Test**: listar a segunda subpasta, conferir exatamente os cinco arquivos com os nomes preservados, a ausência da subpasta antiga, o índice com os novos caminhos, todas as referências operacionais atualizadas, o total de 19 arquivos e as suítes de verificação passando.

**Acceptance Scenarios**:

1. **Given** a subpasta `llm-maven-flags-inference/02-operacao-procedimento-do-agente` com cinco arquivos, **When** a renomeação é aplicada, **Then** existe exatamente `llm-maven-flags-inference/02-procedimento-do-agente` com os cinco arquivos preservados, e a subpasta antiga não existe mais.
2. **Given** os cinco arquivos renomeados, **When** o conteúdo é comparado ao anterior, **Then** ele é idêntico, exceto pelos tokens de referência ao caminho da subpasta.
3. **Given** o índice, a porta de entrada, o README e as suítes de verificação, **When** a atualização é aplicada, **Then** nenhuma referência operacional ao caminho `llm-maven-flags-inference/02-operacao-procedimento-do-agente` permanece.
4. **Given** a Fase 2, **When** a segunda subpasta é inspecionada, **Then** o arquivo `02-como-localizar-e-ler-o-archetype.md` ainda mantém o nome atual e o procedimento mantém 19 arquivos, 13 documentos e 6 exemplos em quatro subpastas.
5. **Given** o índice, **When** ele é lido, **Then** os cinco arquivos da segunda subpasta aparecem nas posições 3 a 7, com os novos caminhos e os mesmos papéis.

---

### User Story 3 - Fase 3 — Arquivo de localização e leitura do archetype (Priority: P3)

Como mantenedor do procedimento, quando renomeio o arquivo 02 da segunda subpasta para `02-localizar-e-ler-o-archetype.md` e atualizo todas as referências a ele, quero que o arquivo seja encontrado pelo numeral e pela grafia corrigida, para que nenhum nome antigo permaneça no corpus operacional.

**Why this priority**: fecha a convenção de nomes iniciada nas fases anteriores; depende da Fase 2 apenas para o caminho da subpasta, não para a decisão de renomear o arquivo.

**Independent Test**: listar a segunda subpasta, conferir exatamente os cinco nomes novos, a ausência do nome antigo do arquivo, o conteúdo idêntico exceto pelas referências, o índice com o caminho novo na posição 4, todas as referências operacionais atualizadas, o total de 19 arquivos e as suítes de verificação passando.

**Acceptance Scenarios**:

1. **Given** o arquivo `02-como-localizar-e-ler-o-archetype.md` na segunda subpasta, **When** a renomeação é aplicada, **Then** existe exatamente `02-localizar-e-ler-o-archetype.md` e o nome antigo não permanece como nome de arquivo.
2. **Given** o arquivo renomeado, **When** o conteúdo é comparado ao anterior, **Then** ele é idêntico, exceto pelos tokens de referência ao próprio nome.
3. **Given** o índice, a porta de entrada, o README e as suítes de verificação, **When** a atualização é aplicada, **Then** nenhuma referência operacional ao nome antigo permanece, incluindo formas curtas como `02-como-localizar-e-ler-o-archetype`.
4. **Given** o procedimento completo, **When** o corpus operacional é verificado, **Then** nenhum dos três nomes antigos permanece como nome de pasta ou de arquivo, exceto o caminho do redirect legado e as asserções negativas das suítes.
5. **Given** o índice, **When** ele é lido, **Then** o arquivo renomeado aparece na posição 4, com o mesmo papel de localização e leitura do artefato.
6. **Given** o procedimento completo, **When** os arquivos são contados, **Then** há 19 arquivos, 13 documentos e 6 exemplos em quatro subpastas, com dois arquivos na primeira e cinco na segunda.

---

### Edge Cases

- Um novo nome de pasta ou de arquivo colide com um nome existente: a renomeação deve parar e reportar a colisão; nenhum arquivo pode ser sobrescrito.
- O sistema de arquivos é insensível a maiúsculas e minúsculas: o resultado deve ser exatamente o nome especificado.
- Um nome antigo permanece em uma referência operacional: a atualização está incompleta e a feature falha no critério de referências.
- A contagem de arquivos ou de subpastas muda: o resultado viola o invariante; a mudança deve ser rejeitada.
- O conteúdo normativo de um arquivo muda durante a renomeação: o resultado viola o invariante; a mudança deve ser rejeitada.
- A primeira subpasta é renomeada na Fase 2, ou a segunda subpasta é renomeada na Fase 1: a fatia deixa de ser independente; a mudança deve ser rejeitada.
- O arquivo 02 é renomeado na Fase 2: a fatia deixa de ser independente; a mudança deve ser rejeitada.
- O redirect legado é removido, renomeado ou deixa de apontar para a porta de entrada no caminho novo: a compatibilidade é quebrada; a mudança deve ser rejeitada.
- A pasta de redirect legado `utils/01-entrada-inicializacao` é renomeada por engano: a compatibilidade com specs anteriores é quebrada; a mudança deve ser rejeitada.
- Um nome antigo aparece apenas como parte de outro nome ou de um texto histórico: somente as referências operacionais exatas devem ser atualizadas.
- Specs históricas, planos, tarefas e saída gerada citam nomes antigos: não são corpus operacional; permanecem fora do escopo.
- O artefato empacotado é construído antes da renomeação: ele pode continuar expondo os nomes antigos; a verificação no artefato não é gate desta feature.
- O nome do arquivo na demanda é escrito com espaços e sem extensão: o nome final é `02-localizar-e-ler-o-archetype.md`, com hífen e extensão, como os demais arquivos do procedimento.
- A grafia `archtype` aparece na demanda: é um erro de digitação; o nome final usa `archetype`.
- Um link ou âncora do README aponta para a subpasta antiga: o link fica quebrado; a atualização deve cobrir o texto, o caminho e a âncora.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: A subpasta `llm-maven-flags-inference/01-entrada-inicializacao` MUST ser renomeada exatamente para `01-inicializacao`.
- **FR-002**: A subpasta `llm-maven-flags-inference/02-operacao-procedimento-do-agente` MUST ser renomeada exatamente para `02-procedimento-do-agente`.
- **FR-003**: O arquivo `02-como-localizar-e-ler-o-archetype.md` da segunda subpasta MUST ser renomeado exatamente para `02-localizar-e-ler-o-archetype.md`.
- **FR-004**: Nenhum dos dois nomes antigos de subpasta MUST permanecer como nome de pasta em `llm-maven-flags-inference/`.
- **FR-005**: O nome antigo do arquivo MUST NOT permanecer como nome de arquivo na segunda subpasta.
- **FR-006**: O conteúdo normativo dos arquivos renomeados MUST permanecer inalterado; apenas as referências textuais aos nomes antigos podem ser atualizadas.
- **FR-007**: Nenhum arquivo do procedimento MUST ser criado, removido ou movido além das três renomeações; o procedimento MUST manter 19 arquivos, 13 documentos e 6 exemplos, em quatro subpastas, com dois arquivos na primeira e cinco na segunda.
- **FR-008**: O índice MUST listar os 19 arquivos com os caminhos novos, na mesma ordem e com os mesmos papéis, e MUST permanecer a única fonte da ordem global.
- **FR-009**: Todas as referências operacionais MUST ser atualizadas, incluindo o índice, a porta de entrada, o README (texto, caminhos, índice do README e âncoras), o redirect legado e as suítes de verificação, inclusive formas curtas.
- **FR-010**: Nenhuma referência operacional aos caminhos antigos das duas subpastas MUST permanecer após a atualização.
- **FR-011**: O redirect legado MUST permanecer em `utils/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md` e MUST apontar para `llm-maven-flags-inference/01-inicializacao/00-entry-point.md`.
- **FR-012**: A subpasta de redirect legado `utils/01-entrada-inicializacao` MUST NOT ser renomeada.
- **FR-013**: As subpastas 03 e 04 MUST permanecer com os nomes atuais.
- **FR-014**: Specs históricas, planos, tarefas e saída gerada MUST NOT ser reescritos por esta feature.
- **FR-015**: As suítes de verificação MUST ser atualizadas para os novos nomes e MUST passar após as renomeações.
- **FR-016**: O artefato reconstruído MUST expor os nomes novos e nenhum nome antigo, exceto o caminho do redirect legado; a verificação no artefato instalado MUST NOT ser gate desta feature.
- **FR-017**: O nome final do arquivo MUST ser `02-localizar-e-ler-o-archetype.md`, com hífen e extensão, tratando os espaços e o numeral isolado da demanda como descrição legível e `archtype` como erro de digitação.
- **FR-018**: Os nomes finais das subpastas MUST ser exatamente `01-inicializacao` e `02-procedimento-do-agente`, preservando o numeral e a grafia em minúsculas.
- **FR-019**: A ordem de leitura e o papel de cada um dos três itens renomeados MUST permanecer inalterados.
- **FR-020**: Nenhuma regra, definição, exemplo de domínio, metadado, default ou formato de contrato MUST ser alterado; somente nomes de pasta, nome de arquivo e referências textuais a eles mudam.
- **FR-021**: O README MUST permanecer internamente consistente após a atualização, com os links e âncoras das subpastas renomeadas funcionando.
- **FR-022**: As referências aos nomes antigos em asserções negativas das suítes de verificação MUST ser permitidas, desde que não representem caminho operacional.

### Key Entities

- **Subpasta renomeada**: uma das duas primeiras subpastas do procedimento, com nome antigo, nome novo, quantidade de arquivos e papel preservado.
- **Arquivo renomeado**: arquivo 02 da segunda subpasta, com nome antigo, nome novo, papel de localização e leitura do artefato e conteúdo normativo preservado.
- **Procedimento empacotado**: conjunto de 19 arquivos em quatro subpastas, 13 documentos e 6 exemplos, lido pelo agente.
- **Referência operacional**: ocorrência de um nome de pasta ou de arquivo no índice, na porta de entrada, no README, no redirect legado ou nas suítes de verificação.
- **Redirect legado**: único arquivo mantido no caminho antigo da porta de entrada para compatibilidade, apontando para a porta de entrada no caminho novo.
- **Âncora do README**: identificador de seção do README que contém o nome antigo da subpasta e precisa acompanhar a renomeação.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 2 de 2 subpastas renomeadas; 0 nomes antigos de subpasta permanecem como nome de pasta em `llm-maven-flags-inference/`.
- **SC-002**: 1 de 1 arquivo renomeado; 0 nomes antigos do arquivo permanecem como nome de arquivo na segunda subpasta.
- **SC-003**: O procedimento mantém 19 arquivos, 13 documentos e 6 exemplos, em quatro subpastas, com dois arquivos na primeira e cinco na segunda.
- **SC-004**: 100% das referências operacionais aos três nomes antigos atualizadas; 0 referências quebradas no índice, na porta de entrada, no README, no redirect legado e nas suítes de verificação.
- **SC-005**: O índice lista 19 de 19 arquivos com os caminhos novos, na mesma ordem e com os mesmos papéis.
- **SC-006**: O redirect legado permanece no caminho atual e aponta para `llm-maven-flags-inference/01-inicializacao/00-entry-point.md`; 0 redirects adicionais são criados.
- **SC-007**: Os conteúdos normativos dos arquivos renomeados permanecem idênticos ao anterior; 0 mudanças em regras, definições, exemplos de domínio, metadados, defaults ou formato do contrato.
- **SC-008**: A ordem de leitura e o papel dos três itens renomeados permanecem os mesmos; 0 mudanças de posição no índice.
- **SC-009**: As suítes de verificação existentes passam após a atualização das expectativas de nome.
- **SC-010**: Se o procedimento for empacotado novamente, o artefato expõe os nomes novos e 0 nomes antigos, exceto o caminho do redirect legado; a verificação no artefato instalado não é gate desta feature.
- **SC-011**: 0 mudanças nas subpastas 03 e 04, no catálogo, nos templates, nas regras de derivação ou no formato do contrato.
- **SC-012**: 100% dos links e âncoras do README que citam as subpastas renomeadas permanecem funcionais.

## Assumptions

- O nome final do arquivo é `02-localizar-e-ler-o-archetype.md`; os espaços e o numeral isolado na demanda são descrição legível, e `archtype` é um erro de digitação.
- O separador dos nomes é hífen e a extensão do arquivo é Markdown, como nos demais arquivos do procedimento.
- A renomeação da primeira subpasta se aplica à subpasta do procedimento empacotado `llm-maven-flags-inference/`; a pasta `utils/01-entrada-inicializacao` é o redirect legado, mantém o nome e apenas o destino é atualizado.
- Os novos nomes não colidem com nomes existentes e podem ser aplicados no sistema de arquivos em uso.
- O sistema de arquivos preserva o resultado exato dos nomes, inclusive em caso de insensibilidade a maiúsculas e minúsculas.
- O arquivo 02 é o único com o nome antigo na segunda subpasta.
- Specs históricas, planos, tarefas e saída gerada não são corpus operacional e permanecem fora da atualização.
- A renomeação é nominal; o conteúdo normativo dos arquivos não muda, exceto pelas referências textuais aos nomes antigos.
- A ordem de leitura e o papel de cada arquivo permanecem os mesmos.
- A verificação no artefato empacotado não está definida como gate; se o artefato for reconstruído, espera-se que reflita os nomes novos.
- A feature não define meta de performance própria; o orçamento de build existente pode ser usado apenas como guarda de regressão.

## Out of Scope

- Renomear as subpastas 03 e 04.
- Renomear a pasta de redirect legado `utils/01-entrada-inicializacao`.
- Alterar o conteúdo normativo dos arquivos; somente referências textuais aos nomes antigos são atualizadas.
- Criar, remover ou mover arquivos do procedimento além das três renomeações.
- Criar redirects adicionais.
- Reescrever specs históricas, planos, tarefas ou saída gerada.
- Verificar o artefato instalado como gate de aceite desta feature.
- Alterar regras, catálogo, estrutura de templates, ordem de leitura ou formato do contrato.
- Alterar os nomes dos arquivos da primeira subpasta e dos demais arquivos da segunda subpasta.

## Compliance Constraints

- Não criar arquivos novos em `llm-maven-flags-inference` nem remover os existentes.
- Não alterar a quantidade de arquivos ou de subpastas do procedimento.
- Não alterar o conteúdo normativo dos arquivos renomeados.
- Manter o redirect legado funcional e no caminho atual.
- Toda alteração deve passar pelas suítes de verificação existentes.
- Não editar `target/` nem o artefato instalado à mão.
