# Contract — Sincronia Catálogo × Procedimento

**Feature**: 007-hard-stop-contract | **Date**: 2026-11-24

## Objetivo

Provar que a tabela-resumo de `regras-04` §3.1 não diverge em silêncio do catálogo de flags. A tabela é uma cópia manual: 9 defaults e as regras de formato literais precisam falhar o build quando o catálogo mudar.

## Fonte da verdade

| Fonte | Uso |
|---|---|
| `target/classes/META-INF/maven/archetype-metadata.xml` | Catálogo construído, com os comentários dos fragmentos; defaults e regex |
| `src/main/resources/META-INF/maven/archetype_metadata_required_properties/property-*.xml` | Fragmentos com `defaultValue`, `validationRegex` e comentários |
| `src/main/resources/llm-maven-flags-inference/03-derivacao-regras-de-formacao-do-contrato/regras-04-como-aplicar-precedencia-e-validacao.md` | Tabela-resumo de 9 flags |

O catálogo é a autoridade final. A tabela é um resumo legível.

## As 9 flags críticas

| Flag | Default | Regra de formato |
|---|---|---|
| `entity_name` | `MyEntity` | `^[A-Z][a-zA-Z0-9_]+$` |
| `entity_package_name` | `myentity` | `^[a-z][a-z0-9_.]+$` |
| `custom_entity_attributes_list` | `String:name` | `^(false\|Tipo:nome(,Tipo:nome)*)$` |
| `custom_entity_attribute_imports` | `none` | FQNs separados por vírgula |
| `contract_enum_definitions` | `none` | `Tipo:CONST\|CONST` separados por `;` |
| `contract_nested_type_definitions` | `none` | `Tipo\|Campo:Tipo` separados por `;` |
| `entity_contract_file_path` | `none` | `none` ou caminho `.json` |
| `enable_custom_entity_attributes` | `false` | `true` ou `false` |
| `enable_folder_src_test_java` | `false` | `true` ou `false` |

O valor `none` de `entity_contract_file_path` é o default do catálogo; o procedimento não o usa como caminho. A nota "catálogo; não usado aqui" fica fora da célula de default.

## Regras de sincronia

### S1 — 3 colunas

A tabela de `regras-04` §3.1 tem cabeçalho com exatamente 3 células (`Flag`, `Default`, `Regex`) e cada linha tem exatamente 3 células. Pipes internos são escapados com `\|`. A suíte falha se uma linha tiver 2 ou 4 células.

### S2 — 9 defaults exatos

Para cada uma das 9 flags, o valor da coluna `Default` é igual ao `defaultValue` do catálogo. A suíte falha e nomeia a flag quando o valor diverge.

### S3 — 3 regexes literais

As linhas `entity_name`, `entity_package_name` e `custom_entity_attributes_list` contêm uma regex literal. A comparação é feita depois de:

1. desescapar o pipe de tabela (`\|` → `|`);
2. normalizar os placeholders `Tipo` → `[A-Z][a-zA-Z0-9_]*` e `nome` → `[a-z][a-zA-Z0-9_]*`.

O resultado precisa ser idêntico ao `validationRegex` do catálogo. A suíte falha e nomeia a flag quando diverge.

### S4 — 6 regras descritivas

As outras 6 linhas descrevem o formato em prosa. A suíte valida um exemplo positivo e um negativo contra o `validationRegex` do catálogo:

| Flag | Exemplo positivo | Exemplo negativo |
|---|---|---|
| `custom_entity_attribute_imports` | `br.com.clinica.Convenio` | `Convenio` |
| `contract_enum_definitions` | `Status:ABERTO\|PAGO` | `Status:aberto` |
| `contract_nested_type_definitions` | `Endereco\|logradouro:String` | `Endereco:logradouro` |
| `entity_contract_file_path` | `none` e `C:/Temp/contract.json` | `contract.txt` |
| `enable_custom_entity_attributes` | `true` e `false` | `yes` |
| `enable_folder_src_test_java` | `true` e `false` | `1` |

Se o catálogo mudar a regex e o exemplo documentado deixar de ser aceito, a suíte falha e nomeia a flag.

Além dos exemplos, a suíte exige que a célula de formato contenha os tokens canônicos:

| Flag | Token esperado |
|---|---|
| `custom_entity_attribute_imports` | `FQNs separados por vírgula` |
| `contract_enum_definitions` | `Tipo:CONST\|CONST` |
| `contract_nested_type_definitions` | `Tipo\|Campo:Tipo` |
| `entity_contract_file_path` | `none` ou caminho `.json` |
| `enable_custom_entity_attributes` | `true` ou `false` |
| `enable_folder_src_test_java` | `true` ou `false` |

## Descrição do caminho do contrato

- O fragmento `property-20-entity_contract_file_path.xml` descreve que o procedimento exige caminho absoluto.
- O `defaultValue` continua `none`.
- A `validationRegex` continua `^(none|[^\r\n]*\.json)$`; a regra permissiva do catálogo é preservada por projeto.
- A suíte falha se a descrição perder a palavra "absoluto" ou se o default/regex mudar.

## Proveniência

- A regra geral de `regras-06` §2 permite omitir defaults inertes.
- A exceção explícita manda registrar o default que uma regra específica exigir, como `contract_nested_type_definitions` = `none`.
- A suíte falha se a exceção desaparecer ou se `contract_nested_type_definitions` deixar de aparecer com a nota `none`.

## FR-006 em linguagem simples

`regras-04` §3 explica que `FR-006` é a regra histórica que exige registrar toda rejeição e o default aplicado. Em linguagem simples: nenhuma rejeição pode ficar escondida. A suíte falha se a explicação desaparecer.

## Pacote da entidade × artefato

`prompt-03` §2.1 explica a diferença:

- `entity_package_name` remove separadores: `PedidoItem` → `pedidoitem`.
- `artifactId` usa kebab-case: `PedidoItem` → `pedido-item`.

A suíte falha se um dos dois exemplos ou a frase de diferença desaparecer.

## Invariantes

| ID | Invariante | Verificação |
|---|---|---|
| SYNC-01 | Tabela com exatamente 3 colunas | `CatalogSyncContractTest` |
| SYNC-02 | 9 defaults iguais ao catálogo | `CatalogSyncContractTest` |
| SYNC-03 | 3 regexes literais iguais ao catálogo | `CatalogSyncContractTest` |
| SYNC-04 | 6 formatos descritivos aceitos pelo catálogo | `CatalogSyncContractTest` |
| SYNC-05 | Descrição de caminho absoluto presente | `CatalogSyncContractTest` |
| SYNC-06 | `defaultValue` e `validationRegex` de `entity_contract_file_path` intactos | `CatalogSyncContractTest` |
| SYNC-07 | Exceção de proveniência presente | `CatalogSyncContractTest` |
| SYNC-08 | `FR-006` explicado | `CatalogSyncContractTest` |
| SYNC-09 | `pedidoitem` × `pedido-item` explícitos | `CatalogSyncContractTest` |
| SYNC-10 | 35 propriedades e 18 filesets | Suítes existentes + `CatalogSyncContractTest` |

## Mapeamento para os requisitos

| Requisito | Cobertura |
|---|---|
| FR-013 | Descrição de caminho absoluto |
| FR-014 | Exceção de proveniência |
| FR-015 | Pipes escapados e 3 colunas |
| FR-016 | Nota FR-006 |
| FR-017 | Pacote × artefato |
| FR-018 | 9 defaults e regras literais |
| FR-023 | 35/18 e default/regex intactos |
