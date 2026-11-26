# Feature Specification: Renomear a Pasta e os Seis Arquivos das Regras de Formação do Contrato

**Feature Branch**: `013-rename-contract-rule-files`

**Created**: 2026-11-26

**Status**: Draft

**Input**: User description: "Trocar o nome da pasta '03 Derivação Regras de Formação do contrato' para '03-regras-de-formacao-do-contrato'; eliminar a palavra 'como' do nome dos seis arquivos, mantendo o numeral e o restante do nome; e atualizar todos os itens do projeto que referenciam os nomes antigos da pasta e dos arquivos."

## Contexto e Problema

- O procedimento empacotado `llm-maven-flags-inference` tem 19 arquivos em quatro subpastas numeradas (01 a 04). A subpasta 03 tem seis regras normativas de formação do contrato.
- O nome atual detectado da subpasta é `03-derivacao-regras-de-formacao-do-contrato`; a demanda o descreve como "03 Derivação Regras de Formação do contrato" e fixa o destino `03-regras-de-formacao-do-contrato`.
- Os seis arquivos têm o numeral seguido de `como-` (`01-como-...` a `06-como-...`). O numeral é o critério de ordem; o `como-` não acrescenta papel ao nome.
- O índice, a porta de entrada, a regra de localização do procedimento, o README, os comentários das fixtures e as suítes de verificação citam os nomes atuais; todas as referências operacionais precisam acompanhar a renomeação.
- O redirect legado da porta de entrada permanece no caminho antigo por compatibilidade e não faz parte desta renomeação.

## Clarifications

### Session 2026-11-26

- Q: Quais referências aos nomes antigos devem ser atualizadas além do corpus operacional? → A: Somente o corpus operacional (índice, arquivos do procedimento, README, comentários de fixtures e suítes de verificação). Specs históricas, planos, tarefas e saída gerada ficam fora.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Fase 1 — Renomear a pasta e atualizar as referências à pasta (Priority: P1)

Como mantenedor do procedimento, quando troco o nome da subpasta 03 para `03-regras-de-formacao-do-contrato` e atualizo todas as referências à pasta, quero que o novo nome seja o único encontrado no corpus operacional, para que a subpasta fique alinhada ao papel de regras de formação do contrato sem quebrar nenhum caminho.

**Why this priority**: é a primeira fatia independente; entrega a pasta com o nome de destino e mantém os seis arquivos com os nomes atuais, sem tocar na remoção de `como-`.

**Independent Test**: listar `src/main/resources/llm-maven-flags-inference/`, conferir a subpasta `03-regras-de-formacao-do-contrato` e a ausência da pasta com o nome antigo, os seis arquivos com os nomes atuais dentro dela, o índice e a porta de entrada com os caminhos atualizados, o README e as suítes de verificação sem referências ao nome antigo da pasta e as suítes passando.

**Acceptance Scenarios**:

1. **Given** a subpasta 03 com o nome `03-derivacao-regras-de-formacao-do-contrato`, **When** a fase é aplicada, **Then** existe exatamente `03-regras-de-formacao-do-contrato` e a pasta com o nome antigo não permanece.
2. **Given** a pasta renomeada, **When** os seis arquivos são listados, **Then** os nomes `01-como-derivar-a-entidade-do-contrato.md` a `06-como-relatar-proveniencia.md` permanecem inalterados dentro da nova pasta.
3. **Given** o índice, a porta de entrada, a regra de localização, o README, os comentários das fixtures e as suítes de verificação, **When** as referências à pasta são atualizadas, **Then** todas usam `03-regras-de-formacao-do-contrato` e nenhuma referência operacional ao nome antigo da pasta permanece.
4. **Given** a pasta renomeada e as referências atualizadas, **When** o procedimento é contado, **Then** há 19 arquivos, 13 Markdown e 6 JSON, em quatro subpastas, com seis arquivos na subpasta 03.
5. **Given** o redirect legado da porta de entrada, **When** a fase é concluída, **Then** ele permanece no caminho antigo e funcional.

---

### User Story 2 - Fase 2 — Renomear as três primeiras regras e atualizar as referências (Priority: P2)

Como mantenedor do procedimento, quando removo `como-` dos nomes dos arquivos `01`, `02` e `03` e atualizo as referências a eles, quero que os três nomes comecem pelo numeral e sigam direto para o papel da regra, para que a leitura fique mais curta e a ordem pelo numeral seja preservada.

**Why this priority**: entrega a convenção para a primeira metade das regras; depende da Fase 1 apenas para o caminho da pasta, não para a própria renomeação.

**Independent Test**: listar a subpasta 03, conferir `01-derivar-a-entidade-do-contrato.md`, `02-resolver-tipos-e-enums.md` e `03-derivar-flags-de-tipos-aninhados.md`, a ausência dos três nomes antigos, o conteúdo normativo idêntico, as referências aos três arquivos atualizadas e os arquivos `04` a `06` com os nomes atuais.

**Acceptance Scenarios**:

1. **Given** os três arquivos com `como-`, **When** a fase é aplicada, **Then** existem exatamente os três novos nomes e nenhum dos três nomes antigos permanece como nome de arquivo.
2. **Given** os três arquivos renomeados, **When** o conteúdo normativo é comparado ao anterior, **Then** ele é idêntico, exceto pelos tokens de referência aos nomes antigos.
3. **Given** o índice, a porta de entrada, as regras, o README e as suítes de verificação, **When** as referências aos três arquivos são atualizadas, **Then** nenhuma referência operacional aos três nomes antigos permanece.
4. **Given** a renomeação dos três primeiros arquivos, **When** os arquivos `04`, `05` e `06` são inspecionados, **Then** eles mantêm os nomes atuais.
5. **Given** a subpasta 03, **When** os arquivos são contados, **Then** há exatamente seis arquivos.

---

### User Story 3 - Fase 3 — Renomear as três últimas regras e atualizar as referências (Priority: P3)

Como mantenedor do procedimento, quando removo `como-` dos nomes dos arquivos `04`, `05` e `06` e atualizo as referências a eles, quero que os seis arquivos da subpasta 03 fiquem sem `como-`, para que a convenção de nomenclatura fique completa e nenhum nome antigo permaneça no corpus operacional.

**Why this priority**: completa a renomeação dos seis arquivos; depende das fases anteriores apenas para o caminho da pasta e para a conferência final de zero nomes antigos.

**Independent Test**: listar a subpasta 03, conferir os seis novos nomes e a ausência de `como-`, o conteúdo normativo idêntico, todas as referências operacionais atualizadas, o total de 19 arquivos e as suítes passando.

**Acceptance Scenarios**:

1. **Given** os três últimos arquivos com `como-`, **When** a fase é aplicada, **Then** existem exatamente `04-aplicar-precedencia-e-validacao.md`, `05-materializar-e-emitir-o-contrato.md` e `06-relatar-proveniencia.md`, e nenhum dos três nomes antigos permanece como nome de arquivo.
2. **Given** os seis arquivos renomeados, **When** a subpasta 03 é inspecionada, **Then** nenhum nome de arquivo contém `como-`.
3. **Given** o índice, a porta de entrada, as regras, o README e as suítes de verificação, **When** as referências aos três últimos arquivos são atualizadas, **Then** nenhuma referência operacional aos seis nomes antigos permanece.
4. **Given** o procedimento completo, **When** os arquivos são contados, **Then** há 19 arquivos, 13 Markdown e 6 JSON, em quatro subpastas, com seis arquivos na subpasta 03.
5. **Given** todos os seis arquivos renomeados, **When** o corpus operacional é verificado, **Then** nenhum dos seis nomes antigos permanece como nome de arquivo ou referência operacional, exceto o caminho do redirect legado e a verificação que o assegura.

---

### Edge Cases

- Um novo nome colide com um nome existente: a renomeação deve parar e reportar a colisão; nenhum arquivo pode ser sobrescrito.
- O sistema de arquivos é insensível a maiúsculas e minúsculas: o novo nome deve ser exatamente o especificado.
- Um nome antigo permanece em uma referência operacional: a atualização está incompleta e a feature falha no critério de referências.
- A contagem de arquivos muda: o resultado viola o invariante; a mudança deve ser rejeitada.
- O conteúdo normativo de um arquivo muda durante a renomeação: o resultado viola o invariante; a mudança deve ser rejeitada.
- A Fase 1 altera os nomes dos seis arquivos: a fatia deixa de ser independente; a mudança deve ser rejeitada.
- A Fase 2 toca nos arquivos `04` a `06`: a fatia deixa de ser independente; a mudança deve ser rejeitada.
- A Fase 3 toca nas outras subpastas: a mudança está fora do escopo e deve ser rejeitada.
- A pasta de destino é um nome com espaços ou acentos: o destino é exatamente `03-regras-de-formacao-do-contrato`.
- O nome antigo aparece apenas como parte de outro nome ou em texto histórico: somente as referências operacionais exatas devem ser atualizadas.
- Specs históricas, planos, tarefas e saída gerada citam nomes antigos: não são corpus operacional e permanecem fora do escopo.
- O artefato empacotado é construído antes da renomeação: ele pode continuar expondo os nomes antigos; a verificação no artefato não é gate desta feature.
- O redirect legado é removido, renomeado ou deixa de apontar para `00-entry-point.md`: a compatibilidade é quebrada; a mudança deve ser rejeitada.
- Um arquivo renomeado é usado como ponto de entrada: somente `00-entry-point.md` é a porta de entrada; os seis arquivos não precisam de redirect.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: A subpasta `03-derivacao-regras-de-formacao-do-contrato` MUST ser renomeada exatamente para `03-regras-de-formacao-do-contrato`.
- **FR-002**: Os seis arquivos da subpasta 03 MUST ser renomeados exatamente para `01-derivar-a-entidade-do-contrato.md`, `02-resolver-tipos-e-enums.md`, `03-derivar-flags-de-tipos-aninhados.md`, `04-aplicar-precedencia-e-validacao.md`, `05-materializar-e-emitir-o-contrato.md` e `06-relatar-proveniencia.md`.
- **FR-003**: Nenhum dos seis nomes antigos MUST permanecer como nome de arquivo na subpasta 03, e o nome antigo da pasta MUST NOT permanecer como nome de pasta.
- **FR-004**: O conteúdo normativo dos seis arquivos MUST permanecer inalterado; apenas as referências textuais aos nomes antigos podem ser atualizadas.
- **FR-005**: Nenhum arquivo MUST ser criado, removido ou movido; o procedimento MUST manter 19 arquivos, 13 Markdown e 6 JSON, em quatro subpastas, com seis arquivos na subpasta 03.
- **FR-006**: Os nomes das subpastas `01-entrada-inicializacao`, `02-operacao-procedimento-do-agente` e `04-referencia-templates-de-contrato` MUST permanecer inalterados.
- **FR-007**: A ordem de leitura e o papel de cada um dos seis arquivos no procedimento MUST permanecer inalterados.
- **FR-008**: Todas as referências operacionais ao nome antigo da pasta e aos seis nomes antigos MUST ser atualizadas, incluindo o índice, a porta de entrada, as regras do procedimento, o README, os comentários das fixtures e as suítes de verificação, inclusive formas curtas.
- **FR-009**: Nenhuma referência operacional aos nomes antigos MUST permanecer após a atualização, exceto o caminho do redirect legado e a verificação que o assegura; nenhum caminho operacional pode ficar quebrado.
- **FR-010**: O índice MUST listar os 19 arquivos do procedimento com os novos nomes e MUST permanecer a única fonte da ordem global.
- **FR-011**: O redirect legado MUST permanecer em `src/main/resources/utils/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md` e MUST continuar apontando para `00-entry-point.md`; nenhum redirect MUST ser criado para os seis arquivos renomeados.
- **FR-012**: Specs históricas, planos, tarefas, saída gerada em `target/` e artefato instalado MUST NOT ser reescritos por esta feature; somente o corpus operacional é atualizado.
- **FR-013**: As suítes de verificação MUST ser atualizadas para os novos nomes e MUST passar após as renomeações.
- **FR-014**: Nenhuma regra, definição, exemplo de domínio, metadado, default ou formato de contrato MUST ser alterado; somente nomes de pasta, nomes de arquivo e referências textuais a eles mudam.
- **FR-015**: O novo nome da pasta MUST ser `03-regras-de-formacao-do-contrato`, tratando a descrição "03 Derivação Regras de Formação do contrato" como referência ao nome atual `03-derivacao-regras-de-formacao-do-contrato`.
- **FR-016**: As referências à pasta dentro do procedimento MUST ser atualizadas em todos os pontos que listam os caminhos dos 19 arquivos, inclusive a lista da porta de entrada e a regra de localização do procedimento.

### Key Entities

- **Pasta renomeada**: subpasta 03 com nome antigo, nome novo, seis arquivos e papel preservado.
- **Arquivo renomeado**: arquivo da subpasta 03 com nome antigo, nome novo, numeral, papel e conteúdo normativo preservado.
- **Referência operacional**: ocorrência do nome da pasta ou de um dos seis arquivos no índice, na porta de entrada, nas regras, no README, nos comentários das fixtures ou nas suítes de verificação.
- **Corpus operacional**: conjunto formado pelo índice, pelos arquivos do procedimento, pelo README, pelos comentários das fixtures e pelas suítes de verificação.
- **Redirect legado**: único arquivo mantido no caminho antigo da porta de entrada por compatibilidade, apontando para `00-entry-point.md`.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 1 de 1 pasta renomeada; 0 ocorrências do nome antigo da pasta como nome de pasta.
- **SC-002**: 6 de 6 arquivos renomeados; 0 dos seis nomes antigos permanecem como nomes de arquivo.
- **SC-003**: 6 de 6 conteúdos normativos idênticos ao anterior; apenas referências textuais aos nomes antigos foram atualizadas.
- **SC-004**: O procedimento mantém 19 arquivos, 13 Markdown e 6 JSON, em quatro subpastas, com seis arquivos na subpasta 03.
- **SC-005**: 0 nomes das outras três subpastas alterados; 0 arquivos criados, removidos ou movidos.
- **SC-006**: 100% das referências operacionais ao nome antigo da pasta e aos seis nomes antigos atualizadas; 0 referências quebradas no índice, na porta de entrada, nas regras, no README, nos comentários das fixtures e nas suítes de verificação.
- **SC-007**: O índice lista os 19 arquivos com os novos nomes e permanece a única fonte da ordem global.
- **SC-008**: O redirect legado permanece no caminho antigo e aponta para `00-entry-point.md`; 0 redirects criados para os seis arquivos.
- **SC-009**: As suítes de verificação existentes passam após a atualização das expectativas de nome.
- **SC-010**: 0 mudanças em regras, definições, exemplos de domínio, metadados, defaults ou formato do contrato.
- **SC-011**: A ordem de leitura e o papel de cada um dos seis arquivos permanecem os mesmos.
- **SC-012**: Se o procedimento for empacotado novamente, o artefato expõe os novos nomes e nenhum nome antigo, exceto o caminho do redirect legado; a verificação no artefato instalado não é gate desta feature.

## Assumptions

- O nome atual da pasta é `03-derivacao-regras-de-formacao-do-contrato`; o nome de destino é `03-regras-de-formacao-do-contrato`, que não repete "derivacao".
- Os seis arquivos são os únicos com o token `como-` na subpasta 03.
- Os novos nomes não colidem com nomes existentes e podem ser aplicados no sistema de arquivos em uso.
- O sistema de arquivos preserva o resultado exato dos nomes, inclusive em caso de insensibilidade a maiúsculas e minúsculas.
- O redirect legado é o único arquivo que mantém um caminho antigo, por compatibilidade com specs anteriores.
- Specs históricas, planos, tarefas, saída gerada em `target/` e artefato instalado não são corpus operacional e permanecem fora da atualização.
- A renomeação é nominal; o conteúdo normativo dos seis arquivos não muda, exceto pelas referências textuais aos nomes antigos.
- A ordem de leitura e o papel de cada arquivo permanecem os mesmos.
- A verificação no artefato empacotado não está definida como gate; se o artefato for reconstruído, espera-se que reflita os novos nomes.
- A feature não define meta de performance própria; o orçamento de build existente pode ser usado apenas como guarda de regressão.

## Out of Scope

- Renomear ou mover as subpastas `01-entrada-inicializacao`, `02-operacao-procedimento-do-agente` e `04-referencia-templates-de-contrato`.
- Alterar o conteúdo normativo dos seis arquivos; somente referências textuais aos nomes antigos são atualizadas.
- Criar, remover ou mover arquivos do procedimento.
- Criar redirects para os seis arquivos renomeados.
- Renomear ou remover o redirect legado da porta de entrada.
- Reescrever specs históricas, planos, tarefas ou qualquer documento fora do corpus operacional.
- Reescrever a saída gerada em `target/` ou o artefato instalado.
- Verificar o artefato instalado como gate de aceite desta feature.
- Alterar regras, catálogo, estrutura de templates, ordem de leitura ou formato do contrato.
- Renomear a pasta para um nome com espaços ou acentos; o destino é exatamente `03-regras-de-formacao-do-contrato`.

## Compliance Constraints

- Não criar arquivos novos em `llm-maven-flags-inference` nem remover os existentes.
- Não alterar os nomes das outras três subpastas.
- Não alterar regras, catálogo, estrutura de templates, ordem de leitura ou formato do contrato.
- Não editar `target/` nem o artefato instalado à mão.
- Toda alteração deve passar pelas suítes de verificação existentes.
