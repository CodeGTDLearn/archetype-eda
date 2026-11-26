# Data Model — Remover as Preposições "do" e "com" dos Nomes dos Templates de Contrato

**Feature**: 015-remove-contract-template-prepositions | **Date**: 2026-11-26 | **Spec**: [spec.md](./spec.md)

A feature é documental: não há banco de dados, API ou modelo em memória em runtime. O “modelo de dados” descreve as entidades persistidas no repositório (arquivos, referências e invariantes) que a implementação e as suítes de verificação manipulam.

## 1. Entidades

### 1.1 `ProcedureFile` — arquivo do procedimento empacotado

| Campo | Tipo | Descrição | Regra |
|---|---|---|---|
| `numeral` | string de 2 dígitos | Ordem de leitura (`01` a `06`) | Preservado em toda renomeação |
| `extension` | string | Extensão do arquivo (`.json`) | Preservada em toda renomeação |
| `currentName` | string | Nome antes da feature | Um dos seis nomes da subpasta 04 |
| `targetName` | string | Nome depois da feature | Igual a `currentName` quando não há preposição-alvo |
| `role` | string | Papel na tabela do índice | Imutável |
| `contentHash` | string (SHA-256) | Hash do conteúdo antes da feature | Deve permanecer idêntico |
| `renamed` | booleano | Indica se o arquivo já usa o `targetName` | `false` no estado 0; `true` após a fase que renomeia o arquivo |

**Instâncias**

| # | `currentName` | `targetName` | `role` | Muda? |
|---|---|---|---|---|
| 01 | `01-schema-do-contrato.json` | `01-schema-contrato.json` | Schema permissivo do contrato | Sim (Fase 1) |
| 02 | `02-contrato-plano.json` | `02-contrato-plano.json` | Exemplo de contrato plano | Não |
| 03 | `03-contrato-com-objetos-aninhados.json` | `03-contrato-objetos-aninhados.json` | Exemplo com objetos aninhados | Sim (Fase 2) |
| 04 | `04-contrato-com-enums.json` | `04-contrato-enums.json` | Exemplo com enums | Sim (Fase 2) |
| 05 | `05-contrato-com-tipos-do-projeto.json` | `05-contrato-tipos-do-projeto.json` | Exemplo com tipos do projeto | Sim (Fase 2) |
| 06 | `06-contrato-com-attributes-explicito.json` | `06-contrato-attributes-explicito.json` | Exemplo com `attributes` explícito | Sim (Fase 2) |

**Invariantes**
- `numeral`, `extension`, `role` e `contentHash` são imutáveis.
- `targetName` casa com `^0[1-6]-[a-z0-9-]+\.json$`.
- A posição de leitura permanece 14 (arquivo 01) a 19 (arquivo 06) no índice.
- Exatamente um arquivo (02) tem `currentName == targetName`.

### 1.2 `RenameOperation` — operação de renomeação

| Campo | Tipo | Descrição | Regra |
|---|---|---|---|
| `source` | caminho | Caminho absoluto ou relativo ao repositório do nome atual | Deve existir e ser arquivo |
| `target` | caminho | Caminho do nome de destino | Não pode existir antes da operação |
| `phase` | inteiro 1–2 | Fase que executa a operação | 1 = schema; 2 = arquivos 3 a 6 |
| `order` | inteiro | Ordem crescente de numeral dentro da fase | 03, 04, 05, 06 |
| `contentPreserved` | booleano | Hash antes igual ao hash depois | Obrigatoriamente `true` |

**Transições**
- `pending → applied` quando `Rename-Item` conclui sem colisão.
- `pending → rejected` quando o destino já existe, quando o numeral ou a extensão mudariam, ou quando o alvo está fora do mapa.
- Não existe transição `applied → pending` nesta feature; reverter exige operação manual fora do escopo.

### 1.3 `OperationalReference` — referência textual a um nome

| Campo | Tipo | Descrição | Regra |
|---|---|---|---|
| `artifact` | caminho | Documento ou suíte que cita o nome | Um dos quatro destinos do escopo |
| `line` | inteiro | Linha da ocorrência | Preservada; só o token muda |
| `oldToken` | string | Nome antigo, curto ou com caminho completo | Um dos cinco nomes |
| `newToken` | string | Nome novo correspondente | Derivado do mapa |
| `kind` | enum | `map`, `entry-point`, `readme` ou `verification-suite` | Define o destino |

**Invariantes**
- Toda ocorrência operacional de `oldToken` tem exatamente uma `newToken`.
- O restante da linha (papel, frase, caminho da pasta, âncora) não muda.
- `oldToken` pode permanecer apenas em asserção negativa de suíte, em spec histórica ou em artefato derivado.

### 1.4 `VerificationSuite` — suíte que valida os nomes

| Campo | Tipo | Descrição | Regra |
|---|---|---|---|
| `file` | caminho | Suíte em `src/test/archetype-post-generate/` | Registrada no runner |
| `style` | enum | `fixed-list` ou `phase-aware` | Define a estratégia de atualização |
| `schemaRenamed` | booleano | Detecta `01-schema-contrato.json` | Só em suítes phase-aware |
| `othersRenamed` | booleano | Detecta `03-contrato-objetos-aninhados.json` | Só em suítes phase-aware |
| `expectedNames` | lista | Nomes esperados na subpasta 04 | Composta pelo estado detectado |

**Instâncias**

| Suíte | Estilo | O que muda |
|---|---|---|
| `BuildArtifactsTest.groovy` | fixed-list | Lista `procedureFiles` (5 entradas) |
| `PathRenameContractTest.groovy` | fixed-list | Lista `procedureFiles` (5 entradas) |
| `ContractRuleRenameContractTest.groovy` | fixed-list | Lista `p04` (5 entradas) |
| `ProcedureContractTest.groovy` | fixed-list | Nome do schema e do exemplo 03 |
| `FileRenameContractTest.groovy` | phase-aware | Estado final, índice, corpus e README |
| `ContractTemplateRenameContractTest.groovy` | phase-aware | Estado final, checagem do schema, anti-drift e README |

### 1.5 `ProcedureInvariant` — contagens e estrutura

| Campo | Valor esperado | Verificação |
|---|---|---|
| `totalFiles` | 19 | contagem recursiva da subpasta do procedimento |
| `markdownFiles` | 13 | contagem por extensão |
| `jsonFiles` | 6 | contagem por extensão |
| `topLevelFolders` | 4 | subpastas 01 a 04 |
| `folder04Files` | 6 | contagem na subpasta 04 |
| `indexRows` | 19 | linhas `| N |` no índice |
| `folder04RowRange` | 14 a 19 | posições no índice |

## 2. Regras de validação

| ID | Regra | Evidência |
|---|---|---|
| VR-01 | Nenhum destino de renomeação existe antes da operação | pré-checagem de colisão |
| VR-02 | Cada arquivo renomeado preserva numeral e extensão | regex do nome |
| VR-03 | Cada arquivo renomeado preserva o SHA-256 | hash antes/depois |
| VR-04 | Nenhum dos cinco nomes antigos permanece como nome de arquivo | listagem da subpasta 04 |
| VR-05 | `02-contrato-plano.json` permanece | listagem e nome |
| VR-06 | Nenhuma referência operacional aos nomes antigos permanece | busca no corpus operacional |
| VR-07 | O índice lista 19 caminhos, com as posições 14 a 19 e os papéis | leitura da tabela |
| VR-08 | O README mantém a âncora, o título e a tabela com os nomes novos | leitura do README |
| VR-09 | As contagens 19/13/6/4 permanecem | contagem |
| VR-10 | As seis suítes passam | `mvn -B clean test` |
| VR-11 | `target/classes` reflete os nomes novos após build limpo | comparação de árvore |

## 3. Transições de estado

```text
Estado 0 (baseline)
  01-schema-do-contrato.json
  02-contrato-plano.json
  03-contrato-com-objetos-aninhados.json
  04-contrato-com-enums.json
  05-contrato-com-tipos-do-projeto.json
  06-contrato-attributes-explicito.json

        │ Fase 1 (US1)                     │ Fase 2 (US2)
        ▼                                  ▼
Estado 1 (schema renomeado)         Estado 2 (arquivos 3–6 renomeados)
  01-schema-contrato.json             01-schema-do-contrato.json
  02-contrato-plano.json              02-contrato-plano.json
  03-contrato-com-objetos-aninhados.json   03-contrato-objetos-aninhados.json
  04-contrato-com-enums.json          04-contrato-enums.json
  05-contrato-com-tipos-do-projeto.json    05-contrato-tipos-do-projeto.json
  06-contrato-com-attributes-explicito.json 06-contrato-attributes-explicito.json
        │                                  │
        └──────────────┬───────────────────┘
                       ▼
Estado final (ambas as fases aplicadas)
  01-schema-contrato.json
  02-contrato-plano.json
  03-contrato-objetos-aninhados.json
  04-contrato-enums.json
  05-contrato-tipos-do-projeto.json
  06-contrato-attributes-explicito.json
```

As suítes phase-aware reconhecem os estados 0, 1, 2 e final por presença de arquivo, sem depender da ordem de aplicação. O estado global é o par (`schemaRenamed`, `othersRenamed`).

## 4. Dados fora do modelo

- `target/classes/llm-maven-flags-inference/04-templates-de-contrato/` é derivado: só é válido após `mvn clean test` ou `mvn clean install`.
- `specs/**` histórico, `graphify-out/**` e o JAR instalado não são entidades desta feature.
- O conteúdo interno dos seis JSON não é modelado porque é imutável; o hash é o contrato.
