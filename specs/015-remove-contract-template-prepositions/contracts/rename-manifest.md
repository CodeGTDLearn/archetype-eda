# Contract — Manifesto de Renomeação: 015-remove-contract-template-prepositions

**Feature**: 015-remove-contract-template-prepositions | **Date**: 2026-11-26 | **Spec**: [../spec.md](../spec.md)

Este manifesto é a fonte única do mapa `old → new`, do mapa de tokens, do inventário de referências por fase, das exceções e das verificações. A implementação consome este contrato; as suítes `ContractTemplateRenameContractTest.groovy` e `FileRenameContractTest.groovy` validam o resultado final, e as quatro suítes de lista fixa validam os caminhos explícitos.

## 1. Mapa de renomeação

### 1.1 Fase 1 — schema

| # | `old` | `new` | Numeral | Papel |
|---|---|---|---|---|
| 1 | `01-schema-do-contrato.json` | `01-schema-contrato.json` | 01 (preservado) | Schema permissivo do contrato |

### 1.2 Fase 2 — arquivos 3 a 6

| # | `old` | `new` | Numeral | Papel |
|---|---|---|---|---|
| 3 | `03-contrato-com-objetos-aninhados.json` | `03-contrato-objetos-aninhados.json` | 03 (preservado) | Exemplo com objetos aninhados |
| 4 | `04-contrato-com-enums.json` | `04-contrato-enums.json` | 04 (preservado) | Exemplo com enums |
| 5 | `05-contrato-com-tipos-do-projeto.json` | `05-contrato-tipos-do-projeto.json` | 05 (preservado) | Exemplo com tipos do projeto |
| 6 | `06-contrato-com-attributes-explicito.json` | `06-contrato-attributes-explicito.json` | 06 (preservado) | Exemplo com `attributes` explícito |

### 1.3 Arquivo preservado

| # | Nome | Motivo |
|---|---|---|
| 2 | `02-contrato-plano.json` | Não carrega `do` nem `com` no corpo do nome |

## 2. Mapa de tokens

Só o token do nome é substituído. O restante da linha, da célula, do link e da frase permanece.

| Token antigo | Token novo | Fase | Observação |
|---|---|---|---|
| `01-schema-do-contrato.json` | `01-schema-contrato.json` | 1 | inclui o caminho completo |
| `03-contrato-com-objetos-aninhados.json` | `03-contrato-objetos-aninhados.json` | 2 | inclui o caminho completo |
| `04-contrato-com-enums.json` | `04-contrato-enums.json` | 2 | inclui o caminho completo |
| `05-contrato-com-tipos-do-projeto.json` | `05-contrato-tipos-do-projeto.json` | 2 | remove só `com`; preserva `tipos-do-projeto` |
| `06-contrato-com-attributes-explicito.json` | `06-contrato-attributes-explicito.json` | 2 | inclui o caminho completo |

Tokens que não mudam: `04-templates-de-contrato`, `02-contrato-plano.json` e o trecho `tipos-do-projeto`.

## 3. Inventário por fase

### 3.1 Fase 1 — arquivo 01

| Artefato | Linha | Ocorrência |
|---|---:|---|
| `src/main/resources/llm-maven-flags-inference/01-inicializacao/01-mapa-dos-arquivos-e-ordem-de-leitura.md` | 24 | caminho completo na posição 14 |
| `src/main/resources/llm-maven-flags-inference/01-inicializacao/00-entry-point.md` | 721 | caminho completo no bloco de integridade |
| `README.md` | 2182 | linha da tabela de templates |
| `src/test/archetype-post-generate/BuildArtifactsTest.groovy` | 102 | lista `procedureFiles` |
| `src/test/archetype-post-generate/PathRenameContractTest.groovy` | 61 | lista `procedureFiles` |
| `src/test/archetype-post-generate/ContractRuleRenameContractTest.groovy` | 78 | lista `p04` |
| `src/test/archetype-post-generate/ProcedureContractTest.groovy` | 12, 139, 148 | comentário, variável `schemaFile` e mensagem |
| `src/test/archetype-post-generate/FileRenameContractTest.groovy` | listas `p2Current`/`p2New`; fase; índice; README | nome do schema nos estados esperados |
| `src/test/archetype-post-generate/ContractTemplateRenameContractTest.groovy` | listas `p2Current`/`p2New`; checagem 2.3; fase | nome do schema nos estados esperados |

### 3.2 Fase 2 — arquivos 3 a 6

| Artefato | Linhas | Ocorrência |
|---|---:|---|
| `01-inicializacao/01-mapa-dos-arquivos-e-ordem-de-leitura.md` | 26–29 | 4 caminhos completos nas posições 16 a 19 |
| `01-inicializacao/00-entry-point.md` | 723–726 | 4 caminhos completos |
| `README.md` | 2184–2187 | 4 linhas da tabela de templates |
| `src/test/archetype-post-generate/BuildArtifactsTest.groovy` | 104–107 | 4 caminhos na lista `procedureFiles` |
| `src/test/archetype-post-generate/PathRenameContractTest.groovy` | 63–66 | 4 caminhos na lista `procedureFiles` |
| `src/test/archetype-post-generate/ContractRuleRenameContractTest.groovy` | 80–83 | 4 caminhos na lista `p04` |
| `src/test/archetype-post-generate/ProcedureContractTest.groovy` | 199 | nome do exemplo 03 |
| `src/test/archetype-post-generate/FileRenameContractTest.groovy` | 74–89, 117–149, 182, 199–201 | `p2Current`/`p2New`, fase, índice, corpus e README |
| `src/test/archetype-post-generate/ContractTemplateRenameContractTest.groovy` | 33–57, 126–127, 168 | `p2Current`/`p2New`, fase, README e anti-drift |

## 4. Estados esperados nas suítes phase-aware

| Estado | `01-schema-contrato.json` | `03-contrato-objetos-aninhados.json` | Nomes esperados |
|---|---|---|---|
| 0 (baseline) | ausente | ausente | `p2Current` completo |
| 1 (só US1) | presente | ausente | schema novo + arquivos 3 a 6 antigos |
| 2 (só US2) | ausente | presente | schema antigo + arquivos 3 a 6 novos |
| Final | presente | presente | `p3New` completo |

`p3New` = `01-schema-contrato.json`, `02-contrato-plano.json`, `03-contrato-objetos-aninhados.json`, `04-contrato-enums.json`, `05-contrato-tipos-do-projeto.json`, `06-contrato-attributes-explicito.json`.

## 5. Exceções

- **Asserções negativas**: as suítes podem citar os nomes antigos para provar ausência (FR-024), desde que não representem caminho operacional.
- **Specs históricas**: `specs/**` cita os nomes antigos e não é reescrito (FR-022).
- **Derivados**: `target/`, `graphify-out/` e o artefato instalado não são editados à mão; `target/classes` só é válido após build limpo.
- **Subpasta 04**: o nome `04-templates-de-contrato` não muda.
- **Arquivo 02**: `02-contrato-plano.json` não é renomeado.
- **Trecho `tipos-do-projeto`**: permanece no arquivo 05.
- **Fora do escopo**: subpastas 01 a 03, catálogo, `archetype-resources/`, `pom.xml`, `pom.xml` e regras de formação do contrato.

## 6. Verificações do contrato

| ID | Verificação | Evidência |
|---|---|---|
| V-01 | os cinco arquivos de destino existem; os cinco nomes antigos não existem | listagem da subpasta 04 |
| V-02 | `02-contrato-plano.json` permanece | listagem e nome |
| V-03 | numeral de dois dígitos e extensão `.json` preservados | regex `^0[1-6]-[a-z0-9-]+\.json$` |
| V-04 | SHA-256 dos 6 JSON idêntico ao inicial | hash antes/depois |
| V-05 | 6 arquivos na subpasta 04; 19/13/6/4 no procedimento | contagem |
| V-06 | índice com 19 linhas, ordem 14 a 19 e papéis | leitura da tabela |
| V-07 | README com a tabela de templates nos nomes novos | leitura do README |
| V-08 | 0 referências operacionais aos cinco nomes antigos | busca no corpus |
| V-09 | suítes phase-aware reconhecem os quatro estados | detecção por presença |
| V-10 | as seis suítes passam | `mvn -B clean test` |
| V-11 | `target/classes` reflete os nomes novos | comparação de árvore após `clean` |
| V-12 | `mvn clean install` verde | build |
