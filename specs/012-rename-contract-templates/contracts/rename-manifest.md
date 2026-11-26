# Contract — Manifesto de Renomeação: 012-rename-contract-templates

**Feature**: 012-rename-contract-templates | **Date**: 2026-11-26 | **Spec**: [../spec.md](../spec.md)

Este manifesto é a fonte única do mapa `old → new`, do mapa de tokens, do inventário de referências por fase, das exceções e das verificações. A implementação consome este contrato; a suíte `ContractTemplateRenameContractTest.groovy` valida o resultado.

## 1. Mapa de renomeação

### 1.1 Pasta (Fase 1)

| # | `old` | `new` |
|---|---|---|
| 1 | `llm-maven-flags-inference/04-referencia-templates-de-contrato/` | `llm-maven-flags-inference/04-templates-de-contrato/` |

### 1.2 Arquivos (Fase 2)

| # | `old` | `new` | Numeral |
|---|---|---|---|
| 1 | `01-schema-do-contrato.json` | `01-schema-do-contrato.json` | 01 (preservado) |
| 2 | `02-exemplo-contrato-plano.json` | `02-contrato-plano.json` | 02 |
| 3 | `03-exemplo-contrato-com-objetos-aninhados.json` | `03-contrato-com-objetos-aninhados.json` | 03 |
| 4 | `04-exemplo-contrato-com-enums.json` | `04-contrato-com-enums.json` | 04 |
| 5 | `05-exemplo-contrato-com-tipos-do-projeto.json` | `05-contrato-com-tipos-do-projeto.json` | 05 |
| 6 | `06-exemplo-contrato-com-attributes-explicito.json` | `06-contrato-com-attributes-explicito.json` | 06 |

## 2. Mapa de tokens

Só o token do nome é substituído. O restante da linha, da célula, do link e da frase permanece.

| Token antigo | Token novo | Fase | Observação |
|---|---|---|---|
| `04-referencia-templates-de-contrato` | `04-templates-de-contrato` | 1 | inclui `llm-maven-flags-inference-04-referencia-templates-de-contrato` nas âncoras |
| `02-exemplo-contrato-plano.json` | `02-contrato-plano.json` | 2 | inclui o caminho completo |
| `03-exemplo-contrato-com-objetos-aninhados.json` | `03-contrato-com-objetos-aninhados.json` | 2 | inclui o caminho completo |
| `04-exemplo-contrato-com-enums.json` | `04-contrato-com-enums.json` | 2 | inclui o caminho completo |
| `05-exemplo-contrato-com-tipos-do-projeto.json` | `05-contrato-com-tipos-do-projeto.json` | 2 | inclui o caminho completo |
| `06-exemplo-contrato-com-attributes-explicito.json` | `06-contrato-com-attributes-explicito.json` | 2 | inclui o caminho completo |

Âncora fixada: `llm-maven-flags-inference-04-templates-de-contrato` (usada no `id`/`name`, no `href` do índice e no link de topo).

## 3. Inventário por fase

### 3.1 Fase 1 — pasta

| Artefato | Linhas | Ocorrências |
|---|---|---|
| `src/main/resources/llm-maven-flags-inference/01-inicializacao/01-mapa-dos-arquivos-e-ordem-de-leitura.md` | 24 a 29 | 6 caminhos com a pasta |
| `src/main/resources/llm-maven-flags-inference/01-inicializacao/00-entry-point.md` | 721 a 726 | 6 caminhos com a pasta |
| `README.md` | 55 | texto e `href` da âncora |
| `README.md` | 610 | lista de composição da subpasta |
| `README.md` | 652 | tabela de artefatos (quatro categorias) |
| `README.md` | 821 | `id`/`name` da âncora |
| `README.md` | 822 | título `### 04-templates-de-contrato` |
| `README.md` | 2178 | cabeçalho da seção de templates |
| `src/test/archetype-post-generate/BuildArtifactsTest.groovy` | 85 | lista `procedureCategories` |
| `src/test/archetype-post-generate/PathRenameContractTest.groovy` | 44 | lista `categories` |
| `src/test/archetype-post-generate/FileRenameContractTest.groovy` | 14, 79 a 80, 106, 116, 123 a 134 | pasta e fase |
| `src/test/archetype-post-generate/ProcedureContractTest.groovy` | 11, 49, 60 | comentário, diretório e mensagem |
| `src/test/archetype-post-generate/ContractRuleRenameContractTest.groovy` | 89 | caminhos esperados da subpasta 04 |
| `src/test/archetype-post-generate/ProcedureEntryRenameContractTest.groovy` | 81 | lista das quatro subpastas |

### 3.2 Fase 2 — arquivos

| Artefato | Linhas | Ocorrências |
|---|---|---|
| `01-inicializacao/01-mapa-dos-arquivos-e-ordem-de-leitura.md` | 25 a 29 | 5 caminhos com os nomes antigos |
| `01-inicializacao/00-entry-point.md` | 722 a 726 | 5 caminhos com os nomes antigos |
| `README.md` | 2183 a 2187 | 5 linhas da tabela de templates |
| `BuildArtifactsTest.groovy` | 102 a 107 | 5 caminhos na lista `procedureFiles` |
| `PathRenameContractTest.groovy` | 61 a 66 | 5 caminhos na lista `procedureFiles` |
| `FileRenameContractTest.groovy` | 62 a 77, 102 a 134, 173 a 179 | `p2Current`/`p2New`, índice, corpus e README |
| `ProcedureContractTest.groovy` | 199 | nome do arquivo 03 |
| `ContractRuleRenameContractTest.groovy` | 77 a 84 | lista `p04` |

## 4. Exceções

- **Redirect legado**: `src/main/resources/utils/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md` mantém o caminho e o conteúdo; não cita a subpasta 04.
- **Asserções negativas**: as suítes podem citar os nomes antigos para provar ausência (FR-025), desde que não representem caminho operacional.
- **Specs históricas**: `specs/**` cita os nomes antigos e não é reescrito (FR-022).
- **Derivados**: `target/`, `graphify-out/` e o artefato instalado não são editados à mão.
- **Fora do escopo**: `01-schema-do-contrato.json`, subpastas 01 a 03, catálogo, `archetype-resources/` e `pom.xml`.

## 5. Verificações do contrato

| ID | Verificação | Evidência |
|---|---|---|
| V-01 | pasta nova existe; pasta antiga ausente | diretório |
| V-02 | 6 arquivos na subpasta 04 | contagem recursiva |
| V-03 | 5 nomes novos; 5 nomes antigos ausentes | nome de arquivo |
| V-04 | numeral e extensão preservados | regex `^0[2-6]-.*\.json$` |
| V-05 | SHA-256 dos 6 JSON idêntico ao inicial | hash antes/depois |
| V-06 | índice com 19 linhas, ordem 14 a 19 e papéis | leitura da tabela |
| V-07 | README com âncora, título, `href` e tabela novos | leitura do README |
| V-08 | 0 referências operacionais antigas | busca no corpus |
| V-09 | 19/13/6/4 preservados | contagem |
| V-10 | redirect legado intacto | caminho e conteúdo |
| V-11 | suítes existentes e a nova verdes | `mvn -B test` |
