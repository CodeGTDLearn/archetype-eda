# Data Model: 008-rename-procedure-files

**Feature**: 008-rename-procedure-files | **Date**: 2026-11-25

## Visão geral

A feature não introduz banco de dados nem modelo de domínio de negócio. Ela opera sobre o sistema de arquivos e sobre o corpus textual do procedimento empacotado. O modelo abaixo descreve os artefatos, as referências e as invariantes que a implementação e os testes precisam preservar.

## Entidades

### RenamedFile

Representa um dos 12 arquivos do procedimento que mudam de nome.

| Campo | Tipo | Descrição |
|---|---|---|
| `old_name` | string | Nome atual, com prefixo `regras-` ou `template-` |
| `new_name` | string | Nome novo, começando no numeral `01`–`06` |
| `subfolder` | string | `03-derivacao-regras-de-formacao-do-contrato` ou `04-referencia-templates-de-contrato` |
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
| `kind` | enum | `full_filename`, `short_rule`, `short_template`, `label`, `comment` |
| `required` | boolean | Se a ocorrência deve ser substituída nesta feature |

**Regras de validação**:
- Toda ocorrência operacional MUST ser substituída.
- O token novo MUST preservar `§`, pontuação, tabela e link de diretório.
- Referências em `specs/**`, `graphify-out/**` e `target/**` MUST NOT ser alteradas.

### RenameManifest

Contrato único da feature.

| Campo | Tipo | Descrição |
|---|---|---|
| `files` | list<RenamedFile> | Os 12 mapeamentos |
| `token_map` | map<string,string> | `regras-NN`/`template-NN` → `NN` |
| `references` | list<OperationalReference> | Inventário operacional |
| `exclusions` | list<string> | `specs/**`, `graphify-out/**`, `target/**`, `.git/**` |

**Regras de validação**:
- `files` MUST ter 12 entradas únicas.
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

| Subpasta | Nome antigo | Nome novo | Referências internas |
|---|---|---|---|
| 03 | `regras-01-como-derivar-a-entidade-do-contrato.md` | `01-como-derivar-a-entidade-do-contrato.md` | Não |
| 03 | `regras-02-como-resolver-tipos-e-enums.md` | `02-como-resolver-tipos-e-enums.md` | Sim (`regras-03`) |
| 03 | `regras-03-como-derivar-flags-de-tipos-aninhados.md` | `03-como-derivar-flags-de-tipos-aninhados.md` | Não |
| 03 | `regras-04-como-aplicar-precedencia-e-validacao.md` | `04-como-aplicar-precedencia-e-validacao.md` | Sim (`regras-01`) |
| 03 | `regras-05-como-materializar-e-emitir-o-contrato.md` | `05-como-materializar-e-emitir-o-contrato.md` | Não |
| 03 | `regras-06-como-relatar-proveniencia.md` | `06-como-relatar-proveniencia.md` | Sim (`regras-01`) |
| 04 | `template-01-schema-do-contrato.json` | `01-schema-do-contrato.json` | Sim (`regras-01`) |
| 04 | `template-02-exemplo-contrato-plano.json` | `02-exemplo-contrato-plano.json` | Sim (`regras-01`) |
| 04 | `template-03-exemplo-contrato-com-objetos-aninhados.json` | `03-exemplo-contrato-com-objetos-aninhados.json` | Não |
| 04 | `template-04-exemplo-contrato-com-enums.json` | `04-exemplo-contrato-com-enums.json` | Não |
| 04 | `template-05-exemplo-contrato-com-tipos-do-projeto.json` | `05-exemplo-contrato-com-tipos-do-projeto.json` | Não |
| 04 | `template-06-exemplo-contrato-com-attributes-explicito.json` | `06-exemplo-contrato-com-attributes-explicito.json` | Sim (`regras-01`, `regras-02`) |

## Transições de estado

```text
[baseline]
  ├─ 12 arquivos com nomes antigos
  ├─ 19 arquivos no procedimento
  └─ 18 arquivos operacionais com referências
        │
        ▼
[renomeado]
  ├─ 12 arquivos com nomes novos
  ├─ 0 nomes antigos nas subpastas
  ├─ conteúdo normativo preservado
  └─ referências ainda podem estar antigas
        │
        ▼
[referências atualizadas]
  ├─ 18 arquivos operacionais sem nomes antigos
  ├─ índice e prompts apontam para os nomes novos
  └─ suítes de teste atualizadas
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
| FR-001/FR-002 | Os 12 arquivos são renomeados removendo apenas o prefixo |
| FR-003 | Os nomes novos são exatamente os 12 mapeados |
| FR-004 | 0 nomes antigos nas subpastas 03 e 04 |
| FR-005 | Conteúdo normativo idêntico; apenas tokens de referência podem diferir |
| FR-006 | 19 arquivos, 13 Markdown, 6 JSON, 4 subpastas, 6 por subpasta |
| FR-007 | Nomes das subpastas inalterados |
| FR-008 | Ordem de leitura e papel dos arquivos inalterados |
| FR-009 | 0 referências operacionais antigas no corpus atualizado |

## Observações

- O modelo é procedural: não há tabela, schema de banco ou migração.
- O “estado” é o conteúdo do sistema de arquivos e o resultado das suítes.
- O hash baseline é uma ferramenta de revisão; a prova final combina hash, diff e a suíte de contrato.
