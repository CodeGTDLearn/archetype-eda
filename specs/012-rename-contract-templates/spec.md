# Feature Specification: Renomear a Subpasta 04 e os Nomes dos Arquivos de Templates de Contrato

**Feature Branch**: `014-rename-contract-templates`

**Created**: 2026-11-26

**Status**: Draft

**Input**: User description: "1. Encontre a pasta '04 referência a templates de contrato' dentro deste projeto. 2. Renomeie a pasta de '04 referência templates de contrato' para '04 templates de contrato'. 3. observe que dentro desta pasta você tem seis arquivos. 4. Do arquivo 2 (segundo arquivo) ao arquivo 6 (sexto arquivo), nós temos a palavra 'exemplo' depois o numeral. 5. Essa palavra 'exemplo' deve ser eliminada renomeando-se os arquivos desta subpasta que tem essa palavra, para seus respectivos nomes MAS sem ela. 5.1 ... 5.2 Os arquivos devem possuir as suas respectivas extensões normalmente, independente da renomeação do corpo do nome do artigo."

## Contexto e Problema

- O procedimento empacotado `llm-maven-flags-inference/` tem 19 arquivos em quatro subpastas numeradas (01 a 04): 13 documentos e 6 JSON. A subpasta 04 reúne o schema permissivo do contrato e cinco exemplos de contrato.
- O nome atual detectado da subpasta é `04-referencia-templates-de-contrato`; a demanda o descreve como "04 referência a templates de contrato" e fixa o destino "04 templates de contrato".
- Os cinco arquivos de exemplo carregam a palavra `exemplo` depois do numeral (`02-exemplo-...` a `06-exemplo-...`). O numeral é o critério de ordem; a palavra `exemplo` não acrescenta papel ao nome, porque todos os cinco já são exemplos de contrato.
- O mapa dos arquivos e ordem de leitura, a porta de entrada, o README e as suítes de verificação citam o nome atual da subpasta e os nomes atuais dos arquivos; as referências operacionais precisam acompanhar a renomeação.
- O redirect legado da porta de entrada permanece no caminho atual por compatibilidade e não faz parte desta renomeação.

## Clarifications

### Session 2026-11-26

- Q: O nome final da pasta usa hífens ou os espaços da demanda? → A: `04-templates-de-contrato`, preservando a convenção de kebab-case e o numeral das demais subpastas do procedimento.
- Q: As referências aos nomes antigos entram no escopo? → A: Sim, apenas as referências operacionais (mapa dos arquivos e ordem de leitura, porta de entrada, README e suítes de verificação); specs históricas, planos, tarefas e saída gerada ficam fora.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Fase 1 — Subpasta `04-templates-de-contrato` (Priority: P1)

Como mantenedor do procedimento, quando renomeio a subpasta 04 para `04-templates-de-contrato` e atualizo as referências operacionais ao caminho dela, quero que o nome curto seja o único encontrado no corpus operacional, para que o procedimento continue encontrando os templates de contrato sem repetir "referência a" no nome.

**Why this priority**: é a primeira fatia independente; entrega a subpasta com o nome de destino e mantém os seis arquivos com os nomes atuais, sem tocar na remoção de `exemplo`.

**Independent Test**: listar `llm-maven-flags-inference/`, conferir a subpasta `04-templates-de-contrato` e a ausência da pasta com o nome antigo, os seis arquivos com os nomes atuais dentro dela, o mapa dos arquivos e a porta de entrada com os caminhos atualizados, o README e as suítes de verificação sem referência operacional ao nome antigo da pasta e as suítes passando.

**Acceptance Scenarios**:

1. **Given** a subpasta 04 com o nome `04-referencia-templates-de-contrato`, **When** a fase é aplicada, **Then** existe exatamente `04-templates-de-contrato` e a pasta com o nome antigo não permanece.
2. **Given** a pasta renomeada, **When** os seis arquivos são listados, **Then** os nomes atuais `01-schema-do-contrato.json` a `06-exemplo-contrato-com-attributes-explicito.json` permanecem inalterados dentro da nova pasta.
3. **Given** o mapa dos arquivos e ordem de leitura, a porta de entrada, o README e as suítes de verificação, **When** as referências à pasta são atualizadas, **Then** todas usam `04-templates-de-contrato` e nenhuma referência operacional ao nome antigo da pasta permanece.
4. **Given** a pasta renomeada e as referências atualizadas, **When** o procedimento é contado, **Then** há 19 arquivos em quatro subpastas, 13 documentos e 6 JSON, com seis arquivos na subpasta 04.
5. **Given** o redirect legado da porta de entrada, **When** a fase é concluída, **Then** ele permanece no caminho atual e funcional.

---

### User Story 2 - Fase 2 — Arquivos sem "exemplo" (Priority: P2)

Como mantenedor do procedimento, quando removo a palavra `exemplo` do corpo do nome dos arquivos 2 a 6 e atualizo as referências operacionais a eles, quero que cada nome comece pelo numeral e siga direto para o papel do template, para que os seis arquivos da subpasta fiquem nomeados pela ordem e pelo conteúdo, sem palavras que repetem a categoria.

**Why this priority**: completa a convenção de nomes da subpasta 04; depende da Fase 1 apenas para o caminho da pasta, não para a própria renomeação dos arquivos.

**Independent Test**: listar a subpasta 04, conferir os cinco nomes novos e a ausência dos cinco nomes antigos, o arquivo 01 inalterado, o conteúdo dos seis arquivos idêntico ao anterior, as referências operacionais aos cinco nomes atualizadas, o total de 19 arquivos e as suítes de verificação passando.

**Acceptance Scenarios**:

1. **Given** os arquivos `02-exemplo-contrato-plano.json` a `06-exemplo-contrato-com-attributes-explicito.json`, **When** a fase é aplicada, **Then** existem exatamente `02-contrato-plano.json`, `03-contrato-com-objetos-aninhados.json`, `04-contrato-com-enums.json`, `05-contrato-com-tipos-do-projeto.json` e `06-contrato-com-attributes-explicito.json`, e nenhum dos cinco nomes antigos permanece como nome de arquivo.
2. **Given** os cinco arquivos renomeados, **When** o conteúdo é comparado ao anterior, **Then** ele é idêntico, inclusive chaves, tipos, valores de exemplo e formato JSON.
3. **Given** o mapa dos arquivos e ordem de leitura, a porta de entrada, o README e as suítes de verificação, **When** as referências aos cinco arquivos são atualizadas, **Then** nenhuma referência operacional aos cinco nomes antigos permanece, incluindo formas curtas e caminhos completos.
4. **Given** a renomeação dos arquivos 2 a 6, **When** o arquivo 01 é inspecionado, **Then** ele mantém exatamente `01-schema-do-contrato.json`.
5. **Given** a subpasta 04 completa, **When** os arquivos são contados, **Then** há exatamente seis arquivos, e o procedimento mantém 19 arquivos, 13 documentos e 6 JSON em quatro subpastas.
6. **Given** o mapa dos arquivos, **When** ele é lido, **Then** os seis arquivos da subpasta 04 aparecem nas posições 14 a 19, com os nomes novos, na mesma ordem e com os mesmos papéis.

---

### Edge Cases

- Um nome novo de pasta ou de arquivo colide com um nome existente: a renomeação deve parar e reportar a colisão; nenhum arquivo pode ser sobrescrito.
- O sistema de arquivos é insensível a maiúsculas e minúsculas: o resultado deve ser exatamente o nome especificado, em minúsculas.
- A extensão do arquivo muda ou desaparece durante a renomeação: o resultado viola a demanda; a mudança deve ser rejeitada.
- O numeral de ordenação muda: a ordem de leitura deixa de coincidir com o índice; a mudança deve ser rejeitada.
- A palavra `exemplo` é removida também de dentro do conteúdo JSON: o resultado viola o invariante de conteúdo; a mudança deve ser rejeitada.
- O arquivo `01-schema-do-contrato.json` é renomeado: a fatia deixa de estar no escopo; a mudança deve ser rejeitada.
- Os arquivos 2 a 6 são renomeados na Fase 1, ou a pasta 04 é renomeada na Fase 2: a fatia deixa de ser independente; a mudança deve ser rejeitada.
- Um nome antigo permanece em uma referência operacional: a atualização está incompleta e a feature falha no critério de referências.
- A contagem de arquivos ou de subpastas muda: o resultado viola o invariante; a mudança deve ser rejeitada.
- As subpastas 01, 02 ou 03 são renomeadas ou alteradas: o resultado sai do escopo; a mudança deve ser rejeitada.
- O redirect legado é removido, renomeado ou deixa de apontar para a porta de entrada: a compatibilidade é quebrada; a mudança deve ser rejeitada.
- Um nome antigo aparece apenas como parte de outro nome ou de um texto histórico: somente as referências operacionais exatas devem ser atualizadas.
- Specs históricas, planos, tarefas e saída gerada citam nomes antigos: não são corpus operacional; permanecem fora do escopo.
- O artefato empacotado é construído antes da renomeação: ele pode continuar expondo os nomes antigos; a verificação no artefato não é gate desta feature.
- O nome de destino é escrito com espaços na demanda: o nome final aplicado é `04-templates-de-contrato`, com hífens, como as demais subpastas do procedimento.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: A subpasta `llm-maven-flags-inference/04-referencia-templates-de-contrato` MUST ser renomeada exatamente para `04-templates-de-contrato`.
- **FR-002**: O nome antigo da subpasta MUST NOT permanecer como nome de pasta em `llm-maven-flags-inference/`.
- **FR-003**: O arquivo `02-exemplo-contrato-plano.json` MUST ser renomeado exatamente para `02-contrato-plano.json`.
- **FR-004**: O arquivo `03-exemplo-contrato-com-objetos-aninhados.json` MUST ser renomeado exatamente para `03-contrato-com-objetos-aninhados.json`.
- **FR-005**: O arquivo `04-exemplo-contrato-com-enums.json` MUST ser renomeado exatamente para `04-contrato-com-enums.json`.
- **FR-006**: O arquivo `05-exemplo-contrato-com-tipos-do-projeto.json` MUST ser renomeado exatamente para `05-contrato-com-tipos-do-projeto.json`.
- **FR-007**: O arquivo `06-exemplo-contrato-com-attributes-explicito.json` MUST ser renomeado exatamente para `06-contrato-com-attributes-explicito.json`.
- **FR-008**: Nenhum dos cinco nomes antigos MUST permanecer como nome de arquivo na subpasta 04.
- **FR-009**: O arquivo `01-schema-do-contrato.json` MUST permanecer com o nome atual e MUST NOT ser renomeado.
- **FR-010**: Cada arquivo renomeado MUST preservar a mesma extensão do nome anterior (`.json`).
- **FR-011**: Cada arquivo renomeado MUST preservar o numeral de dois dígitos do nome anterior e a ordem de leitura.
- **FR-012**: O conteúdo dos seis arquivos MUST permanecer inalterado, inclusive regras, definições, chaves, tipos, valores de exemplo e formato JSON; apenas referências textuais a nomes antigos, se existirem fora dos arquivos, podem ser atualizadas.
- **FR-013**: O procedimento MUST manter 19 arquivos em quatro subpastas, sendo 13 documentos e 6 JSON, com seis arquivos na subpasta 04.
- **FR-014**: Nenhum arquivo do procedimento MUST ser criado, removido ou movido além da renomeação da pasta 04 e dos cinco arquivos.
- **FR-015**: O mapa dos arquivos e ordem de leitura MUST listar os 19 arquivos com os caminhos novos, nas mesmas posições e com os mesmos papéis, e MUST permanecer a única fonte da ordem global.
- **FR-016**: Todas as referências operacionais ao nome antigo da pasta e aos cinco nomes antigos de arquivo MUST ser atualizadas, incluindo o mapa dos arquivos e ordem de leitura, a porta de entrada, o README (texto, caminhos e âncoras) e as suítes de verificação, inclusive formas curtas e caminhos completos.
- **FR-017**: Nenhuma referência operacional aos nomes antigos MUST permanecer após a atualização.
- **FR-018**: O README MUST permanecer internamente consistente, com os links e âncoras das subpastas e dos arquivos renomeados funcionando.
- **FR-019**: As suítes de verificação MUST ser atualizadas para os nomes novos e MUST passar após as renomeações.
- **FR-020**: O redirect legado da porta de entrada MUST permanecer no caminho atual e funcional.
- **FR-021**: As subpastas 01, 02 e 03 MUST permanecer com os nomes e os arquivos atuais.
- **FR-022**: Specs históricas, planos, tarefas e saída gerada MUST NOT ser reescritos por esta feature; a atualização MUST ficar restrita ao corpus operacional.
- **FR-023**: A ordem de leitura e o papel de cada um dos seis arquivos da subpasta 04 MUST permanecer inalterados.
- **FR-024**: O nome final da subpasta MUST ser exatamente `04-templates-de-contrato`, tratando a forma com espaços da demanda como descrição legível e preservando a convenção de hífens das demais subpastas.
- **FR-025**: Referências aos nomes antigos em asserções negativas das suítes de verificação MUST ser permitidas, desde que não representem caminho operacional.
- **FR-026**: O artefato reconstruído MUST expor os nomes novos e nenhum nome antigo; a verificação no artefato instalado MUST NOT ser gate desta feature.

### Key Entities

- **Subpasta renomeada**: a subpasta 04 do procedimento empacotado, com nome antigo, nome novo, quantidade de arquivos e papel preservado.
- **Arquivo renomeado**: um dos cinco arquivos de exemplo da subpasta 04, com nome antigo, nome novo, numeral e extensão preservados.
- **Procedimento empacotado**: conjunto de 19 arquivos em quatro subpastas, 13 documentos e 6 JSON, lido pelo agente.
- **Referência operacional**: ocorrência de um nome de pasta ou de arquivo no mapa dos arquivos e ordem de leitura, na porta de entrada, no README ou nas suítes de verificação.
- **Mapa dos arquivos e ordem de leitura**: documento que lista os 19 arquivos, seus caminhos e papéis, e define a ordem global de leitura.
- **Redirect legado**: arquivo mantido no caminho antigo da porta de entrada para compatibilidade.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 1 de 1 subpasta renomeada; 0 nomes antigos de subpasta permanecem como nome de pasta em `llm-maven-flags-inference/`.
- **SC-002**: 5 de 5 arquivos renomeados; 0 nomes antigos permanecem como nome de arquivo na subpasta 04.
- **SC-003**: 0 ocorrências da palavra `exemplo` no corpo dos nomes dos seis arquivos da subpasta 04.
- **SC-004**: O procedimento mantém 19 arquivos em quatro subpastas, sendo 13 documentos e 6 JSON, com seis arquivos na subpasta 04.
- **SC-005**: 100% das referências operacionais aos nomes antigos atualizadas; 0 referências quebradas no mapa dos arquivos, na porta de entrada, no README e nas suítes de verificação.
- **SC-006**: O mapa dos arquivos lista 19 de 19 arquivos com os caminhos novos, nas mesmas posições e com os mesmos papéis.
- **SC-007**: Os conteúdos dos seis arquivos permanecem idênticos ao anterior; 0 mudanças em regras, definições, chaves, tipos, valores de exemplo ou formato.
- **SC-008**: 0 mudanças nas subpastas 01, 02 e 03 e no arquivo `01-schema-do-contrato.json`.
- **SC-009**: As suítes de verificação existentes passam após a atualização das expectativas de nome.
- **SC-010**: 100% dos links e âncoras do README que citam a subpasta e os arquivos renomeados permanecem funcionais.
- **SC-011**: Se o procedimento for empacotado novamente, o artefato expõe os nomes novos e 0 nomes antigos; a verificação no artefato instalado não é gate desta feature.

## Assumptions

- O nome final da subpasta é `04-templates-de-contrato`, com hífens e numeral, seguindo a convenção vigente das subpastas 01 a 04; a forma "04 templates de contrato" da demanda é descrição legível.
- O separador dos nomes é hífen e a extensão dos seis arquivos é JSON, como nos nomes atuais.
- As referências operacionais aos nomes antigos acompanham a renomeação para não deixar caminhos quebrados; o escopo da atualização fica restrito ao mapa dos arquivos, à porta de entrada, ao README e às suítes de verificação.
- A palavra `exemplo` aparece como token isolado no nome dos arquivos 2 a 6 e sua remoção não altera o restante do nome nem a ordem por numeral.
- Os novos nomes não colidem com nomes existentes e podem ser aplicados no sistema de arquivos em uso, inclusive em caso de insensibilidade a maiúsculas e minúsculas.
- O redirect legado da porta de entrada permanece no caminho atual e apenas aponta para a porta de entrada.
- Specs históricas, planos, tarefas e saída gerada não são corpus operacional e permanecem fora da atualização.
- A verificação no artefato empacotado não está definida como gate; se o artefato for reconstruído, espera-se que reflita os nomes novos.
- A feature não define meta de performance própria; o orçamento de build existente pode ser usado apenas como guarda de regressão.

## Out of Scope

- Renomear as subpastas 01, 02 e 03.
- Renomear o arquivo `01-schema-do-contrato.json`.
- Alterar o conteúdo dos seis arquivos; apenas nomes e referências textuais a eles mudam.
- Criar, remover ou mover arquivos do procedimento além da renomeação da pasta 04 e dos cinco arquivos.
- Criar redirects adicionais.
- Reescrever specs históricas, planos, tarefas ou saída gerada.
- Verificar o artefato instalado como gate de aceite desta feature.
- Alterar regras de formação do contrato, catálogo, estrutura dos templates ou formato do contrato.

## Compliance Constraints

- Não criar arquivos novos em `llm-maven-flags-inference` nem remover os existentes.
- Não alterar a quantidade de arquivos ou de subpastas do procedimento.
- Não alterar o conteúdo dos arquivos renomeados.
- Manter o redirect legado funcional e no caminho atual.
- Toda alteração deve passar pelas suítes de verificação existentes.
- Não editar `target/` nem o artefato instalado à mão.
