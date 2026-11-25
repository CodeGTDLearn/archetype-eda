# Data Model: 009-rename-procedure-files

**Feature**: 009-rename-procedure-files | **Date**: 2026-11-25

## Visão geral

A feature não introduz banco de dados nem modelo de domínio de negócio. Ela opera sobre o sistema de arquivos e sobre o corpus textual do procedimento empacotado. O modelo abaixo descreve os artefatos, as referências, as fases e as invariantes que a implementação e os testes precisam preservar.

## Entidades

### RenamedFile

Representa um dos 12 arquivos do procedimento que mudam de nome.

| Campo | Tipo | Descrição |
|---|---|---|
| `old_name` | string | Nome atual, com prefixo `regras-` ou `template-` |
| `new_name` | string | Nome novo, começando no numeral `01`–`06` |
| `subfolder` | string | `03-derivacao-regras-de-formacao-do-contrato` ou `04-referencia-templates-de-contrato` |
| `phase` | integer | `1` para a subpasta 03; `2` para a subpasta 04 |
| `extension` | string | `.md` ou `.json` |
| `role` | string | Papel do arquivo na ordem de leitura |
| `has_old_references` | boolean | Se o próprio arquivo contém referências a nomes antigos |
| `baseline_hash` | string | SHA-256 do arquivo antes da feature |

**Regras de validação**:
- `new_name` MUST ser exatamente o nome mapeado em `contracts/rename-manifest.md`.
- `old_name` MUST NOT existir após a renomeação.
- `new_name` MUST NOT colidir com um arquivo existente.
- `has_old_references = false` implica `baseline_hash` igual ao hash pós-renomeação.
- `has_old_references = true` implica que o diff contém apenas tokens de referência.

### FeaturePhase

Representa uma das duas fases da spec.

| Campo | Tipo | Descrição |
|---|---|---|
| `number` | integer | `1` ou `2` |
| `name` | string | `Renomear os arquivos de derivação` ou `Renomear os arquivos de referência` |
| `priority` | string | `P1` ou `P2` |
| `subfolder` | string | Subpasta renomeada pela fase |
| `files` | list<RenamedFile> | Os 6 arquivos da fase |
| `scope` | list<string> | Arquivos de referência atualizados na fase |
| `independent_test` | string | Como provar a fase isoladamente |

**Regras de validação**:
- A Fase 1 MUST cobrir 03 e suas referências; a Fase 2 MUST cobrir 04, suas referências e o README.
- A Fase 1 MUST NOT tocar no README nem nos arquivos de 04.
- A Fase 2 MUST concluir a contagem de 19 e a varredura final.
- Cada fase MUST ter um resultado demonstrável sem depender de fase futura.

### Subfolder

Representa uma das duas subpastas alteradas.

| Campo | Tipo | Descrição |
|---|---|---|
| `name` | string | Nome fixo da subpasta |
| `number` | string | `03` ou `04` |
| `file_count` | integer | 6 |
| `files` | list<RenamedFile> | Os 6 arquivos da subpasta |

**Regras de validação**:
- O nome MUST permanecer inalterado.
- `file_count` MUST ser 6.
- Nenhum arquivo pode ser criado ou removido.

### OperationalReference

Representa uma ocorrência de nome antigo em um arquivo operacional.

| Campo | Tipo | Descrição |
|---|---|---|
| `source_path` | string | Arquivo que contém a referência |
| `lines` | list<integer> | Linhas afetadas |
| `old_token` | string | Nome completo ou forma curta antiga |
| `new_token` | string | Nome completo ou forma curta nova |
| `phase` | integer | `1` para referências de 03; `2` para referências de 04/README |
| `kind` | enum | `full_filename`, `short_rule`, `short_template`, `label`, `comment` |
| `required` | boolean | Se a ocorrência deve ser substituída nesta feature |

**Regras de validação**:
- Toda ocorrência operacional MUST ser substituída na fase indicada.
- O token novo MUST preservar `§`, pontuação, tabela e link de diretório.
- Referências em `specs/**`, `graphify-out/**` e `target/**` MUST NOT ser alteradas.
- Referências de 03 no README são atualizadas na Fase 2, junto com as de 04.

### RenameManifest

Contrato único da feature.

| Campo | Tipo | Descrição |
|---|---|---|
| `files` | list<RenamedFile> | Os 12 mapeamentos |
| `phases` | list<FeaturePhase> | As duas fases |
| `token_map` | map<string,string> | `regras-NN`/`template-NN` → `NN` |
| `references` | list<OperationalReference> | Inventário operacional |
| `exclusions` | list<string> | `specs/**`, `graphify-out/**`, `target/**`, `.git/**` |

**Regras de validação**:
- `files` MUST ter 12 entradas únicas.
- `phases` MUST ter 2 entradas, na ordem P1 → P2.
- `token_map` MUST cobrir os 12 prefixos.
- `references` MUST cobrir os 18 arquivos operacionais.
- `exclusions` MUST impedir alteração de specs históricas e saída gerada.

### ProcedureInvariant

Invariantes globais do procedimento.

| Campo | Valor |
|---|---|
| `total_files` | 19 |
| `markdown_files` | 13 |
| `json_files` | 6 |
| `subfolders` | 4 |
| `files_in_03` | 6 |
| `files_in_04` | 6 |
| `catalog_properties` | 35 |
| `catalog_filesets` | 18 |

**Regras de validação**:
- Todos os valores MUST permanecer iguais após a feature.
- O catálogo e `archetype-resources/` MUST NOT ser alterados.

## Mapa de renomeação

| Fase | Subpasta | Nome antigo | Nome novo | Referências internas |
|---|---|---|---|---|
| 1 | 03 | `regras-01-como-derivar-a-entidade-do-contrato.md` | `01-como-derivar-a-entidade-do-contrato.md` | Não |
| 1 | 03 | `regras-02-como-resolver-tipos-e-enums.md` | `02-como-resolver-tipos-e-enums.md` | Sim (`regras-03`) |
| 1 | 03 | `regras-03-como-derivar-flags-de-tipos-aninhados.md` | `03-como-derivar-flags-de-tipos-aninhados.md` | Não |
| 1 | 03 | `regras-04-como-aplicar-precedencia-e-validacao.md` | `04-como-aplicar-precedencia-e-validacao.md` | Sim (`regras-01`) |
| 1 | 03 | `regras-05-como-materializar-e-emitir-o-contrato.md` | `05-como-materializar-e-emitir-o-contrato.md` | Não |
| 1 | 03 | `regras-06-como-relatar-proveniencia.md` | `06-como-relatar-proveniencia.md` | Sim (`regras-01`) |
| 2 | 04 | `template-01-schema-do-contrato.json` | `01-schema-do-contrato.json` | Sim (`regras-01`) |
| 2 | 04 | `template-02-exemplo-contrato-plano.json` | `02-exemplo-contrato-plano.json` | Sim (`regras-01`) |
| 2 | 04 | `template-03-exemplo-contrato-com-objetos-aninhados.json` | `03-exemplo-contrato-com-objetos-aninhados.json` | Não |
| 2 | 04 | `template-04-exemplo-contrato-com-enums.json` | `04-exemplo-contrato-com-enums.json` | Não |
| 2 | 04 | `template-05-exemplo-contrato-com-tipos-do-projeto.json` | `05-exemplo-contrato-com-tipos-do-projeto.json` | Não |
| 2 | 04 | `template-06-exemplo-contrato-com-attributes-explicito.json` | `06-exemplo-contrato-com-attributes-explicito.json` | Sim (`regras-01`, `regras-02`) |

## Transições de estado

```text
[baseline]
  ├─ 12 arquivos com nomes antigos
  ├─ 19 arquivos no procedimento
  └─ 18 arquivos operacionais com referências
        │
        ▼
[Fase 1 concluída]
  ├─ 6 arquivos de 03 com nomes novos
  ├─ 0 nomes antigos em 03
  ├─ referências de 03 atualizadas
  ├─ conteúdo normativo de 03 preservado
  └─ 04 e README intactos
        │
        ▼
[Fase 2 concluída]
  ├─ 6 arquivos de 04 com nomes novos
  ├─ 0 nomes antigos em 04
  ├─ referências de 04 atualizadas
  ├─ README atualizado para 03 e 04
  ├─ 19 arquivos, 13 Markdown, 6 JSON, 4 subpastas
  └─ 0 referências antigas no corpus operacional
        │
        ▼
[validado]
  ├─ FileRenameContractTest passa
  ├─ mvn -B test passa
  ├─ mvn clean install passa
  └─ JAR instalado (opt-in) contém os nomes novos
```

## Regras de validação derivadas dos requisitos

| Requisito | Regra de validação |
|---|---|
| FR-001 | Os 6 arquivos de 03 são renomeados removendo `regras-` |
| FR-002 | Os 6 arquivos de 04 são renomeados removendo `template-` |
| FR-003 | Os nomes novos são exatamente os 12 mapeados |
| FR-004 | 0 nomes antigos nas subpastas 03 e 04 |
| FR-005 | Conteúdo normativo idêntico; apenas tokens de referência podem diferir |
| FR-006 | 19 arquivos, 13 Markdown, 6 JSON, 4 subpastas, 6 por subpasta |
| FR-007 | Nomes das subpastas inalterados |
| FR-008 | Ordem de leitura e papel dos arquivos inalterados |
| FR-009 | 0 referências operacionais antigas no corpus atualizado |
| FR-010 | Nenhum caminho interno do procedimento quebrado |
| FR-011 | Specs históricas, planos/tarefas e saída gerada não são reescritos |
| FR-012 | Nenhum redirect é criado para os nomes antigos |

## Observações

- O modelo é procedural: não há tabela, schema de banco ou migração.
- O “estado” é o conteúdo do sistema de arquivos e o resultado das suítes.
- O hash baseline é uma ferramenta de revisão; a prova final combina hash, diff e a suíte de contrato.
- A Fase 1 é o MVP: entrega 03 renomeada, referências de 03 atualizadas e 04 intacta.
