# Data Model: Renomear a Pasta e os Seis Arquivos das Regras de Formação do Contrato

**Feature**: 011-rename-contract-rule-files | **Date**: 2026-11-26

## Entidades

### FolderRename

| Campo | Tipo | Regra |
|---|---|---|
| `old_name` | string | `03-derivacao-regras-de-formacao-do-contrato` |
| `new_name` | string | `03-regras-de-formacao-do-contrato` |
| `parent` | path | `src/main/resources/llm-maven-flags-inference/` |
| `phase` | integer | `1` |
| `file_count` | integer | `6` arquivos preservados dentro da pasta |
| `status` | enum | `pending` → `renamed`; falha em colisão |
| `content_preserved` | boolean | `true`; a pasta não tem conteúdo próprio, os seis arquivos são preservados |

### FileRename

| Campo | Tipo | Regra |
|---|---|---|
| `numeral` | string | `01` a `06`; é o critério de ordem e não muda |
| `old_name` | string | nome atual com `como-` após o numeral |
| `new_name` | string | nome de destino sem `como-`, com o numeral e o restante do nome preservados |
| `phase` | integer | `2` para `01`–`03`; `3` para `04`–`06` |
| `role` | string | papel da regra no procedimento (entidade, tipos/enums, aninhados, precedência/validação, materialização, proveniência) |
| `content_hash_before` | string | SHA-256 antes da renomeação |
| `content_hash_after` | string | SHA-256 depois da renomeação; igual ao anterior quando o arquivo não contém referência aos nomes antigos; caso contrário, o diff deve conter apenas os tokens de referência atualizados |
| `status` | enum | `pending` → `renamed`; falha em colisão |

Mapa nominal:

| Numeral | Old name | New name | Fase |
|---|---|---|---|
| 01 | `01-como-derivar-a-entidade-do-contrato.md` | `01-derivar-a-entidade-do-contrato.md` | 2 |
| 02 | `02-como-resolver-tipos-e-enums.md` | `02-resolver-tipos-e-enums.md` | 2 |
| 03 | `03-como-derivar-flags-de-tipos-aninhados.md` | `03-derivar-flags-de-tipos-aninhados.md` | 2 |
| 04 | `04-como-aplicar-precedencia-e-validacao.md` | `04-aplicar-precedencia-e-validacao.md` | 3 |
| 05 | `05-como-materializar-e-emitir-o-contrato.md` | `05-materializar-e-emitir-o-contrato.md` | 3 |
| 06 | `06-como-relatar-proveniencia.md` | `06-relatar-proveniencia.md` | 3 |

### OperationalReference

| Campo | Tipo | Regra |
|---|---|---|
| `file` | path | arquivo do corpus operacional que contém a referência |
| `line` | integer | linha da referência |
| `kind` | enum | `folder_path`, `full_file_path`, `short_file_name`, `readme_anchor`, `readme_heading`, `readme_link`, `fixture_comment`, `test_expectation` |
| `old_token` | string | token antigo exato |
| `new_token` | string | token novo exato |
| `phase` | integer | fase em que a referência deve ser atualizada |
| `exception` | boolean | `true` apenas para o caminho do redirect legado e asserções negativas que precisam conter nomes antigos |

### Corpus

| Campo | Tipo | Regra |
|---|---|---|
| `path` | path | raiz do corpus operacional |
| `roles` | list | índice, porta de entrada, regras do procedimento, README, comentários de fixtures e suítes de verificação |
| `in_scope` | boolean | `true` para o corpus operacional; `false` para `specs/`, `graphify-out/`, `target/`, `.git/` e artefato instalado |

### LegacyRedirect

| Campo | Tipo | Regra |
|---|---|---|
| `path` | path | `src/main/resources/utils/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md` |
| `target` | path | `llm-maven-flags-inference/01-entrada-inicializacao/00-entry-point.md` |
| `exception` | boolean | `true`; permanece no caminho antigo e não cita a pasta 03 nem os seis arquivos |

### Phase

| Campo | Tipo | Regra |
|---|---|---|
| `number` | integer | `1`, `2`, `3` |
| `name` | string | nome detectado na spec |
| `user_story` | string | `US1`, `US2`, `US3` |
| `priority` | string | `P1`, `P2`, `P3` |
| `depends_on` | list | vazio, `Fase 1` ou `Fase 1, Fase 2` |
| `tests` | string | atualização das suítes existentes; suíte nova phase-aware |
| `acceptance` | list | critérios de aceite da user story correspondente |

### Invariant

| Invariante | Valor esperado | Validação |
|---|---|---|
| Arquivos no procedimento | 19 | contagem recursiva em `src/main/resources/llm-maven-flags-inference/` e `target/classes/llm-maven-flags-inference/` |
| Markdown | 13 | contagem por extensão |
| JSON | 6 | contagem por extensão |
| Subpastas | 4 | contagem de diretórios de primeiro nível |
| Arquivos na subpasta 03 | 6 | contagem recursiva em 03 |
| Pastas renomeadas | 1 | nome novo existe; nome antigo não existe |
| Arquivos renomeados | 6 | seis nomes novos; zero nomes antigos como nome de arquivo |
| Conteúdo normativo | 6/6 idênticos, exceto referências | SHA-256/diff contra o baseline |
| Arquivos criados/removidos/movidos | 0 | contagem e diff |
| Outras subpastas renomeadas | 0 | nomes de 01, 02 e 04 inalterados |
| Referências operacionais antigas | 0, exceto redirect legado e asserções negativas | varredura no corpus operacional |
| Índice | 19 caminhos novos, na mesma ordem | leitura do índice |
| Redirect legado | permanece no caminho antigo e aponta para `00-entry-point.md` | leitura do redirect |
| Suítes de verificação | 100% passam | `mvn -B clean test`, `mvn clean install`, `mvn -B clean integration-test` |

## Transições de estado

| Estado | Pasta 03 | Arquivos 01–03 | Arquivos 04–06 | Referências | Build |
|---|---|---|---|---|---|
| Fase 0 — baseline | `03-derivacao-...` | `como-*` | `como-*` | todas antigas | baseline atual |
| Fase 1 — pasta | `03-regras-...` | `como-*` | `como-*` | pasta atualizada; arquivos ainda antigos | `mvn -B clean test` |
| Fase 2 — três primeiras regras | `03-regras-...` | novos | `como-*` | pasta e 01–03 atualizados; 04–06 ainda antigos | `mvn -B clean test` |
| Fase 3 — três últimas regras | `03-regras-...` | novos | novos | todas atualizadas | `mvn -B clean test`, `mvn clean install`, `mvn -B clean integration-test` |

## Regras de validação

- A pasta de destino e os seis nomes de destino não podem existir antes da renomeação; colisão aborta a fase sem sobrescrever.
- Os nomes são aplicados exatamente como na spec, inclusive em sistema de arquivos insensível a maiúsculas e minúsculas.
- O conteúdo normativo não muda; apenas tokens de referência aos nomes antigos podem ser substituídos.
- A ordem de leitura, o papel e a prioridade das fases permanecem os mesmos.
- O índice continua sendo a única fonte da ordem global.
- O redirect legado permanece funcional e é a única exceção de caminho antigo.
- Specs históricas, planos/tarefas, `graphify-out/`, `target/` e artefato instalado não são reescritos.
