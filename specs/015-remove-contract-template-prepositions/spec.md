# Feature Specification: Remover as Preposições "do" e "com" dos Nomes dos Templates de Contrato

**Feature Branch**: `015-remove-contract-template-prepositions`

**Created**: 2026-11-26

**Status**: Draft

**Input**: User description: "na pasta 04-templates-de-contato existem 6 arquivos. o primeiro tem a preposicao 'do'. do 'terceiro' ao 'sexto' existe a preposicao 'com'. mude isso, eliminando a preposicao nesses arquivos. exemplo: de '01-schema-do-contrato.json' para '01-schema-contrato.json'. e os outros arquivos, exemplo: de '03-contrato-com-objetos-aninhados.json' para '03-contrato-objetos-aninhados.json'"

## Contexto e Problema

- A pasta `llm-maven-flags-inference/04-templates-de-contrato` reúne o schema do contrato e cinco exemplos de contrato, nomeados pelo numeral e pelo conteúdo.
- O primeiro arquivo carrega a preposição `do` (`01-schema-do-contrato.json`); o segundo não carrega preposição-alvo (`02-contrato-plano.json`); do terceiro ao sexto os nomes carregam a preposição `com` (`03` a `06`).
- A preposição não acrescenta papel ao nome: o numeral define a ordem e o restante do nome descreve o conteúdo. Mantê-la deixa os nomes mais longos do que o necessário e fora do padrão enxuto já aplicado aos demais arquivos da subpasta.
- O mapa dos arquivos e ordem de leitura, a porta de entrada, o README e as suítes de verificação citam os cinco nomes com preposição; as referências operacionais precisam acompanhar a renomeação para não deixar caminhos quebrados.

## Clarifications

### Session 2026-11-26

- Q: No arquivo `05-contrato-com-tipos-do-projeto.json`, devo remover apenas `com` ou também o `do` de `tipos-do-projeto`? → A: Apenas `com`, resultando em `05-contrato-tipos-do-projeto.json`; o `do` de `tipos-do-projeto` permanece.
- Q: Além de renomear os cinco arquivos, devo atualizar as referências operacionais aos nomes antigos no mapa dos arquivos, na porta de entrada, no README e nas suítes de verificação? → A: Sim, atualizar as referências operacionais nesses quatro destinos.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Fase 1 — Remover "do" do nome do schema (Priority: P1)

Como mantenedor do procedimento, quando removo a preposição `do` do nome do schema do contrato e atualizo as referências operacionais a ele, quero que o arquivo seja identificado apenas por numeral, papel e extensão, para que o nome do schema siga o mesmo padrão enxuto aplicado aos demais arquivos da subpasta.

**Why this priority**: é a menor fatia independente e pode ser aplicada isoladamente, sem depender da renomeação dos arquivos 3 a 6.

**Independent Test**: listar a subpasta 04, conferir `01-schema-contrato.json` presente e `01-schema-do-contrato.json` ausente, o conteúdo do arquivo idêntico ao anterior, as referências operacionais ao nome antigo atualizadas e as suítes de verificação passando.

**Acceptance Scenarios**:

1. **Given** o arquivo `01-schema-do-contrato.json`, **When** a fase é aplicada, **Then** existe exatamente `01-schema-contrato.json` e o nome antigo não permanece como nome de arquivo.
2. **Given** o arquivo renomeado, **When** o conteúdo é comparado ao anterior, **Then** ele é idêntico, inclusive regras, definições, chaves, tipos e formato JSON.
3. **Given** o mapa dos arquivos e ordem de leitura, a porta de entrada, o README e as suítes de verificação, **When** as referências ao nome antigo são atualizadas, **Then** nenhuma referência operacional a `01-schema-do-contrato.json` permanece, incluindo formas curtas e caminhos completos.
4. **Given** a fase concluída, **When** os demais arquivos da subpasta são inspecionados, **Then** `02-contrato-plano.json` e os arquivos 3 a 6 permanecem com os nomes que tinham antes desta fase.

---

### User Story 2 - Fase 2 — Remover "com" dos nomes dos arquivos 3 a 6 (Priority: P2)

Como mantenedor do procedimento, quando removo a preposição `com` do corpo do nome dos arquivos 3 a 6 e atualizo as referências operacionais a eles, quero que cada nome vá do numeral direto para a descrição do conteúdo, para que os exemplos de contrato fiquem nomeados pela ordem e pelo papel, sem a preposição repetida.

**Why this priority**: completa a limpeza de preposições da subpasta 04; depende da Fase 1 apenas para o estado final da pasta, não para a própria renomeação.

**Independent Test**: listar a subpasta 04, conferir os quatro nomes novos e a ausência dos quatro nomes antigos, `02-contrato-plano.json` e o arquivo 01 inalterados, o conteúdo dos seis arquivos idêntico ao anterior, as referências operacionais atualizadas e as suítes de verificação passando.

**Acceptance Scenarios**:

1. **Given** os arquivos `03-contrato-com-objetos-aninhados.json`, `04-contrato-com-enums.json`, `05-contrato-com-tipos-do-projeto.json` e `06-contrato-com-attributes-explicito.json`, **When** a fase é aplicada, **Then** existem exatamente `03-contrato-objetos-aninhados.json`, `04-contrato-enums.json`, `05-contrato-tipos-do-projeto.json` e `06-contrato-attributes-explicito.json`, e nenhum dos quatro nomes antigos permanece como nome de arquivo.
2. **Given** o arquivo `05-contrato-com-tipos-do-projeto.json`, **When** a fase é aplicada, **Then** o resultado é `05-contrato-tipos-do-projeto.json`, removendo apenas `com` e preservando `do` no trecho `tipos-do-projeto`.
3. **Given** os arquivos renomeados, **When** o conteúdo é comparado ao anterior, **Then** ele é idêntico, inclusive regras, definições, chaves, tipos, valores de exemplo e formato JSON.
4. **Given** o mapa dos arquivos e ordem de leitura, a porta de entrada, o README e as suítes de verificação, **When** as referências aos nomes antigos são atualizadas, **Then** nenhuma referência operacional aos quatro nomes antigos permanece, incluindo formas curtas e caminhos completos.
5. **Given** a fase concluída, **When** a subpasta 04 é listada, **Then** há exatamente seis arquivos e `02-contrato-plano.json` permanece com o nome atual.
6. **Given** o mapa dos arquivos, **When** ele é lido, **Then** os seis arquivos da subpasta 04 aparecem nas posições 14 a 19, com os nomes finais, na mesma ordem e com os mesmos papéis.

---

### Edge Cases

- Um nome novo colide com um nome existente: a renomeação deve parar e reportar a colisão; nenhum arquivo pode ser sobrescrito.
- O sistema de arquivos é insensível a maiúsculas e minúsculas: o resultado deve ser exatamente o nome especificado, em minúsculas.
- A extensão do arquivo muda ou desaparece durante a renomeação: o resultado viola a demanda; a mudança deve ser rejeitada.
- O numeral de ordenação muda: a ordem de leitura deixa de coincidir com o índice; a mudança deve ser rejeitada.
- A preposição é removida também de dentro do conteúdo JSON: o resultado viola o invariante de conteúdo; a mudança deve ser rejeitada.
- O arquivo `02-contrato-plano.json` é renomeado: ele não carrega preposição-alvo; a mudança está fora do escopo e deve ser rejeitada.
- A preposição `do` é removida de `tipos-do-projeto` no arquivo 05: apenas `com` está no escopo desse arquivo; a mudança deve ser rejeitada.
- A pasta `04-templates-de-contrato` é renomeada: o nome da pasta permanece; a mudança está fora do escopo e deve ser rejeitada.
- Uma referência operacional ao nome antigo permanece: a atualização está incompleta e a feature falha no critério de referências.
- A contagem de arquivos ou de subpastas muda: o resultado viola o invariante; a mudança deve ser rejeitada.
- As subpastas 01, 02 ou 03 são renomeadas ou alteradas: o resultado sai do escopo; a mudança deve ser rejeitada.
- Um nome antigo aparece apenas como parte de outro nome ou de um texto histórico: somente as referências operacionais exatas devem ser atualizadas.
- Specs históricas, planos, tarefas e saída gerada citam nomes antigos: não são corpus operacional; permanecem fora do escopo.
- O artefato empacotado é construído antes da renomeação: ele pode continuar expondo os nomes antigos; a verificação no artefato não é gate desta feature.
- A pasta é citada como `04-templates-de-contato` na demanda: a referência real é `04-templates-de-contrato`; o nome da pasta não muda.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O arquivo `01-schema-do-contrato.json` MUST ser renomeado exatamente para `01-schema-contrato.json`.
- **FR-002**: O arquivo `03-contrato-com-objetos-aninhados.json` MUST ser renomeado exatamente para `03-contrato-objetos-aninhados.json`.
- **FR-003**: O arquivo `04-contrato-com-enums.json` MUST ser renomeado exatamente para `04-contrato-enums.json`.
- **FR-004**: O arquivo `05-contrato-com-tipos-do-projeto.json` MUST ser renomeado exatamente para `05-contrato-tipos-do-projeto.json`.
- **FR-005**: O arquivo `06-contrato-com-attributes-explicito.json` MUST ser renomeado exatamente para `06-contrato-attributes-explicito.json`.
- **FR-006**: Nenhum dos cinco nomes antigos MUST permanecer como nome de arquivo na subpasta 04.
- **FR-007**: O arquivo `02-contrato-plano.json` MUST permanecer com o nome atual e MUST NOT ser renomeado.
- **FR-008**: Cada arquivo renomeado MUST preservar a mesma extensão do nome anterior (`.json`).
- **FR-009**: Cada arquivo renomeado MUST preservar o numeral de dois dígitos do nome anterior e a ordem de leitura.
- **FR-010**: O conteúdo dos seis arquivos MUST permanecer inalterado, inclusive regras, definições, chaves, tipos, valores de exemplo e formato JSON.
- **FR-011**: A subpasta `04-templates-de-contrato` MUST permanecer com o nome atual e com exatamente seis arquivos.
- **FR-012**: Nenhuma preposição `do` MUST permanecer no corpo do nome do arquivo 01, e nenhuma preposição `com` MUST permanecer no corpo dos nomes dos arquivos 3 a 6.
- **FR-013**: O arquivo 05 MUST conservar o trecho `tipos-do-projeto`; apenas a preposição `com` é removida desse nome.
- **FR-014**: O procedimento MUST manter 19 arquivos em quatro subpastas, sendo 13 documentos e 6 JSON, com seis arquivos na subpasta 04.
- **FR-015**: Nenhum arquivo do procedimento MUST ser criado, removido ou movido além das cinco renomeações.
- **FR-016**: O mapa dos arquivos e ordem de leitura MUST listar os 19 arquivos com os caminhos novos, nas mesmas posições e com os mesmos papéis, e MUST permanecer a única fonte da ordem global.
- **FR-017**: Todas as referências operacionais aos cinco nomes antigos MUST ser atualizadas, incluindo o mapa dos arquivos e ordem de leitura, a porta de entrada, o README (texto, caminhos e âncoras) e as suítes de verificação, inclusive formas curtas e caminhos completos.
- **FR-018**: Nenhuma referência operacional aos cinco nomes antigos MUST permanecer após a atualização.
- **FR-019**: O README MUST permanecer internamente consistente, com os links e âncoras dos arquivos renomeados funcionando.
- **FR-020**: As suítes de verificação MUST ser atualizadas para os nomes novos e MUST passar após as renomeações.
- **FR-021**: As subpastas 01, 02 e 03 MUST permanecer com os nomes e os arquivos atuais.
- **FR-022**: Specs históricas, planos, tarefas e saída gerada MUST NOT ser reescritos por esta feature; a atualização MUST ficar restrita ao corpus operacional.
- **FR-023**: A ordem de leitura e o papel de cada um dos seis arquivos da subpasta 04 MUST permanecer inalterados.
- **FR-024**: Referências aos nomes antigos em asserções negativas das suítes de verificação MUST ser permitidas, desde que não representem caminho operacional.
- **FR-025**: O artefato reconstruído MUST expor os nomes novos e nenhum nome antigo; a verificação no artefato instalado MUST NOT ser gate desta feature.

### Key Entities

- **Arquivo renomeado**: um dos cinco arquivos da subpasta 04 que perdem a preposição-alvo, com nome antigo, nome novo, numeral e extensão preservados.
- **Preposição-alvo**: `do` no arquivo 01 e `com` nos arquivos 3 a 6; `do` em `tipos-do-projeto` não é alvo.
- **Arquivo inalterado**: `02-contrato-plano.json`, que não carrega preposição-alvo.
- **Procedimento empacotado**: conjunto de 19 arquivos em quatro subpastas, 13 documentos e 6 JSON, lido pelo agente.
- **Referência operacional**: ocorrência de um nome de arquivo no mapa dos arquivos e ordem de leitura, na porta de entrada, no README ou nas suítes de verificação.
- **Mapa dos arquivos e ordem de leitura**: documento que lista os 19 arquivos, seus caminhos e papéis, e define a ordem global de leitura.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 5 de 5 arquivos renomeados; 0 nomes antigos permanecem como nome de arquivo na subpasta 04.
- **SC-002**: 1 de 1 arquivo sem preposição-alvo (`02-contrato-plano.json`) permanece inalterado.
- **SC-003**: 0 ocorrências da preposição `do` no corpo do nome do arquivo 01 e 0 ocorrências da preposição `com` no corpo dos nomes dos arquivos 3 a 6.
- **SC-004**: 6 de 6 arquivos permanecem na subpasta 04, e o procedimento mantém 19 arquivos, 13 documentos e 6 JSON em quatro subpastas.
- **SC-005**: 100% das referências operacionais aos nomes antigos atualizadas; 0 referências quebradas no mapa dos arquivos, na porta de entrada, no README e nas suítes de verificação.
- **SC-006**: O mapa dos arquivos lista 19 de 19 arquivos com os caminhos novos, nas mesmas posições e com os mesmos papéis.
- **SC-007**: Os conteúdos dos seis arquivos permanecem idênticos ao anterior; 0 mudanças em regras, definições, chaves, tipos, valores de exemplo ou formato.
- **SC-008**: 0 mudanças nas subpastas 01, 02 e 03 e no nome da subpasta 04.
- **SC-009**: As suítes de verificação existentes passam após a atualização das expectativas de nome.
- **SC-010**: 100% dos links e âncoras do README que citam os arquivos renomeados permanecem funcionais.
- **SC-011**: Se o procedimento for empacotado novamente, o artefato expõe os nomes novos e 0 nomes antigos; a verificação no artefato instalado não é gate desta feature.

## Assumptions

- A pasta citada como `04-templates-de-contato` é a existente `04-templates-de-contrato`; o nome da pasta não muda nesta feature.
- Apenas a preposição identificada para cada arquivo é removida: `do` no arquivo 01 e `com` nos arquivos 3 a 6; o `do` de `tipos-do-projeto` no arquivo 05 permanece.
- O arquivo `02-contrato-plano.json` não carrega nenhuma das preposições-alvo e permanece inalterado.
- O separador dos nomes é hífen e a extensão dos seis arquivos é JSON, como nos nomes atuais.
- As referências operacionais aos nomes antigos acompanham a renomeação para não deixar caminhos quebrados; o escopo da atualização fica restrito ao mapa dos arquivos, à porta de entrada, ao README e às suítes de verificação.
- A palavra `com` aparece como token isolado no nome dos arquivos 3 a 6 e sua remoção não altera o restante do nome nem a ordem por numeral.
- Os novos nomes não colidem com nomes existentes e podem ser aplicados no sistema de arquivos em uso, inclusive em caso de insensibilidade a maiúsculas e minúsculas.
- Specs históricas, planos, tarefas e saída gerada não são corpus operacional e permanecem fora da atualização.
- A verificação no artefato empacotado não está definida como gate; se o artefato for reconstruído, espera-se que reflita os nomes novos.
- A feature não define meta de performance própria; o orçamento de build existente pode ser usado apenas como guarda de regressão.

## Out of Scope

- Renomear a subpasta `04-templates-de-contrato`.
- Renomear o arquivo `02-contrato-plano.json`.
- Remover `do` do trecho `tipos-do-projeto` no arquivo 05.
- Alterar o conteúdo dos seis arquivos; apenas nomes e referências textuais a eles mudam.
- Criar, remover ou mover arquivos do procedimento além das cinco renomeações.
- Renomear as subpastas 01, 02 e 03.
- Reescrever specs históricas, planos, tarefas ou saída gerada.
- Verificar o artefato instalado como gate de aceite desta feature.
- Alterar regras de formação do contrato, catálogo, estrutura dos templates ou formato do contrato.

## Compliance Constraints

- Não criar arquivos novos em `llm-maven-flags-inference` nem remover os existentes.
- Não alterar a quantidade de arquivos ou de subpastas do procedimento.
- Não alterar o conteúdo dos arquivos renomeados.
- Manter o nome da subpasta 04 e o arquivo 02 inalterados.
- Toda alteração deve passar pelas suítes de verificação existentes.
- Não editar `target/` nem o artefato instalado à mão.
