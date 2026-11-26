# Phase 1 — Data Model: 014-rename-contract-templates

**Feature**: 014-rename-contract-templates | **Date**: 2026-11-26 | **Spec**: [spec.md](./spec.md)

Este documento descreve as entidades nominais da renomeação, os invariantes que o conteúdo e a contagem preservam e as transições entre as duas fases. Não há banco de dados, schema novo ou estrutura de código: as entidades são artefatos do procedimento empacotado e as referências textuais a eles.

## Entidades

### E1 — Subpasta renomeada (`ProcedureFolder`)

| Campo | Valor |
|---|---|
| `old_name` | `04-referencia-templates-de-contrato` |
| `new_name` | `04-templates-de-contrato` |
| `parent` | `src/main/resources/llm-maven-flags-inference/` |
| `file_count` | 6 |
| `role` | schema permissivo do contrato e cinco exemplos de contrato |
| `renamed_in` | Fase 1 |

### E2 — Arquivo renomeado (`ProcedureFile`)

| # | `old_name` | `new_name` | `numeral` | `extension` | `role` | `renamed_in` |
|---|---|---|---|---|---|---|
| 1 | `01-schema-do-contrato.json` | `01-schema-do-contrato.json` | 01 | `.json` | schema permissivo do contrato | — (preservado) |
| 2 | `02-exemplo-contrato-plano.json` | `02-contrato-plano.json` | 02 | `.json` | exemplo com campos de negócio no topo | Fase 2 |
| 3 | `03-exemplo-contrato-com-objetos-aninhados.json` | `03-contrato-com-objetos-aninhados.json` | 03 | `.json` | exemplo com objetos aninhados | Fase 2 |
| 4 | `04-exemplo-contrato-com-enums.json` | `04-contrato-com-enums.json` | 04 | `.json` | exemplo com enums | Fase 2 |
| 5 | `05-exemplo-contrato-com-tipos-do-projeto.json` | `05-contrato-com-tipos-do-projeto.json` | 05 | `.json` | exemplo com tipos do projeto | Fase 2 |
| 6 | `06-exemplo-contrato-com-attributes-explicito.json` | `06-contrato-com-attributes-explicito.json` | 06 | `.json` | exemplo com `attributes` explícito | Fase 2 |

- `sha256_before`: registrado na fundação, antes da Fase 1, para os seis arquivos.
- `sha256_after`: deve ser idêntico ao anterior ao fim de cada fase.
- `order`: posições 14 a 19 no mapa dos arquivos e ordem de leitura.

### E3 — Referência operacional (`OperationalReference`)

| Campo | Descrição |
|---|---|
| `artifact` | arquivo que contém a ocorrência |
| `location` | linha ou bloco |
| `old_token` | nome antigo da pasta ou do arquivo |
| `new_token` | nome novo correspondente |
| `phase` | Fase 1 (token de pasta) ou Fase 2 (token de arquivo) |

Referências por artefato:

| Artefato | Local | Token | Fase |
|---|---|---|---|
| `README.md` | 55 | pasta (texto e `href` da âncora) | 1 |
| `README.md` | 610 | pasta (lista de composição) | 1 |
| `README.md` | 652 | pasta (tabela de artefatos) | 1 |
| `README.md` | 821 | pasta (`id`/`name` da âncora) | 1 |
| `README.md` | 822 | pasta (título da subseção) | 1 |
| `README.md` | 2178 | pasta (cabeçalho da seção de templates) | 1 |
| `README.md` | 2183 a 2187 | cinco nomes de arquivo (tabela) | 2 |
| `01-inicializacao/01-mapa-dos-arquivos-e-ordem-de-leitura.md` | 24 a 29 | pasta e cinco nomes (seis caminhos) | 1 e 2 |
| `01-inicializacao/00-entry-point.md` | 721 a 726 e 220 | pasta, cinco nomes (seis caminhos) e um nome curto | 1 e 2 |
| `BuildArtifactsTest.groovy` | 85, 102 a 107 | pasta e cinco nomes | 1 e 2 |
| `PathRenameContractTest.groovy` | 44, 61 a 66 | pasta e cinco nomes | 1 e 2 |
| `FileRenameContractTest.groovy` | 14, 62 a 77, 79 a 80, 102 a 116, 123 a 134, 173 a 179 | pasta e nomes (três gerações) | 1 e 2 |
| `ProcedureContractTest.groovy` | 11, 49, 60, 199 | pasta e nome do arquivo 03 | 1 e 2 |
| `ContractRuleRenameContractTest.groovy` | 77 a 89 | pasta e cinco nomes | 1 e 2 |
| `ProcedureEntryRenameContractTest.groovy` | 81 | pasta | 1 |
| `ContractTemplateRenameContractTest.groovy` (novo) | — | valida as duas fases | 1 e 2 |

### E4 — Procedimento empacotado (`PackagedProcedure`)

| Campo | Valor |
|---|---|
| `root` | `src/main/resources/llm-maven-flags-inference/` |
| `folders` | `01-inicializacao`, `02-procedimento-do-agente`, `03-regras-de-formacao-do-contrato`, `04-templates-de-contrato` |
| `files_total` | 19 |
| `markdown_total` | 13 |
| `json_total` | 6 |
| `files_in_folder_04` | 6 |
| `packaged_copy` | `target/classes/llm-maven-flags-inference/` (gerada pelo build) |

### E5 — Redirect legado (`LegacyRedirect`)

| Campo | Valor |
|---|---|
| `path` | `src/main/resources/utils/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md` |
| `target` | `llm-maven-flags-inference/01-inicializacao/00-entry-point.md` |
| `status` | inalterado nas duas fases |

### E6 — Conjunto de templates de contrato (`ContractTemplateSet`)

- **Composição**: um schema (`01-schema-do-contrato.json`) e cinco exemplos (`02` a `06`), todos válidos em JSON.
- **Relação**: cada exemplo deve permanecer válido contra o schema; o schema não muda.
- **Validação**: `ProcedureContractTest` já valida JSON e o subconjunto draft-07 usado pelo schema.

## Invariantes

- **INV-01**: existe exatamente uma pasta com o nome novo em `llm-maven-flags-inference/` e nenhuma com o nome antigo (FR-002, SC-001).
- **INV-02**: a subpasta 04 contém exatamente seis arquivos, com os cinco nomes novos e nenhum dos cinco antigos ao fim da Fase 2 (FR-008, SC-002).
- **INV-03**: nenhum nome de arquivo da subpasta 04 contém a palavra `exemplo` ao fim da Fase 2 (SC-003).
- **INV-04**: cada arquivo preserva o numeral de dois dígitos e a extensão `.json` (FR-010, FR-011).
- **INV-05**: `01-schema-do-contrato.json` permanece com o nome atual (FR-009, SC-008).
- **INV-06**: o SHA-256 dos seis JSON permanece idêntico antes e depois de cada fase (FR-012, SC-007).
- **INV-07**: o procedimento mantém 19 arquivos, 13 Markdown e 6 JSON em quatro subpastas, com seis na subpasta 04 (FR-013, SC-004).
- **INV-08**: o mapa lista 19 de 19 arquivos, nas posições 1 a 19, com os caminhos finais e os mesmos papéis (FR-015, SC-006).
- **INV-09**: nenhuma referência operacional aos nomes antigos permanece após a fase correspondente; asserções negativas das suítes e o caminho do redirect legado são exceções (FR-017, FR-025).
- **INV-10**: as subpastas 01, 02 e 03 mantêm nomes e arquivos (FR-021).
- **INV-11**: o redirect legado permanece no caminho atual e aponta para a porta de entrada (FR-020).
- **INV-12**: o catálogo mantém 35 propriedades e 18 filesets; `archetype-resources/` e `pom.xml` não mudam.

## Transições de estado

| Estado | Pasta 04 | Arquivos 2 a 6 | Índice | README | Suítes |
|---|---|---|---|---|---|
| S0 — inicial | `04-referencia-templates-de-contrato` | `0X-exemplo-...` | caminhos com a pasta antiga e os nomes atuais | pasta antiga e nomes atuais | estado inicial verde |
| S1 — fim da Fase 1 | `04-templates-de-contrato` | `0X-exemplo-...` (inalterados) | caminhos com a pasta nova e os nomes atuais | pasta nova; tabela de templates ainda com `exemplo` | atualizadas para a pasta nova; verdes |
| S2 — fim da Fase 2 | `04-templates-de-contrato` | `0X-contrato-...` | 19 caminhos finais | tabela de templates com os nomes novos | atualizadas para os nomes novos; verdes |

- A transição S0 → S1 não altera nomes de arquivo.
- A transição S1 → S2 não altera o nome da pasta.
- Não há transição de retorno; o redirect legado não participa das transições.

## Regras de validação

| Regra | Evidência | Requisito |
|---|---|---|
| Nome exato da pasta | comparação do nome do diretório | FR-001, FR-024 |
| Nomes exatos dos cinco arquivos | comparação do nome de cada arquivo | FR-003 a FR-007 |
| Numeral e extensão preservados | regex `^0[2-6]-.*\.json$` | FR-010, FR-011 |
| Conteúdo idêntico | SHA-256 antes/depois | FR-012 |
| Contagem 19/13/6/4 | contagem recursiva | FR-013 |
| Índice com 19 linhas e papéis | leitura das 19 linhas da tabela | FR-015 |
| Referências atualizadas | busca pelos tokens antigos no corpus operacional | FR-016, FR-017 |
| README consistente | âncora, título, `href`, tabela | FR-018 |
| Suítes verdes | runner Groovy na fase `test` | FR-019 |
| Redirect legado intacto | caminho e conteúdo | FR-020 |
| Subpastas 01 a 03 intactas | nomes e arquivos | FR-021 |
| Escopo de escrita | nenhum `specs/`, `graphify-out/`, `target/` editado à mão | FR-022, FR-026 |

## Fora do modelo

- Specs históricas, planos, tarefas e saída gerada não são entidades desta feature.
- `graphify-out/`, `target/` e o artefato instalado são derivados; não são editados à mão.
- Nenhum catálogo, template de entidade, flag ou configuração entra no modelo.
