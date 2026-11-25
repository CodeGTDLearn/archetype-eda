# Phase 1 — Data Model: 007-hard-stop-contract

**Feature**: 007-hard-stop-contract | **Date**: 2026-11-24

## Entidades

### HardStop

| Campo | Regra | Requisito |
|---|---|---|
| `code` | `STOP-<CATEGORIA>-<NN>`, único, registrado | FR-002 |
| `category` | `IN`, `ENV`, `DER`, `EXEC`, `RULE` ou `CONF` | FR-002 |
| `trigger` | Gatilho objetivo, em uma frase | FR-002, FR-004 |
| `whatHappened` | Fato objetivo, uma frase | FR-001, FR-004 |
| `where` | Nome legível da seção + caminho/campo/comando | FR-001, FR-004, FR-028 |
| `why` | Regra que exigiu a parada, em linguagem simples | FR-001, FR-004 |
| `notExecuted` | O que não foi executado | FR-001, FR-004 |
| `options` | 2 ou 3 opções acionáveis, com exemplo | FR-004, FR-005, FR-010 |
| `suggestion` | Opção recomendada + motivo em uma linha | FR-004 |
| `resume` | O que corrigir e de onde recomeçar | FR-001, FR-004 |
| `forbidden` | Proibições específicas | FR-001, FR-004 |
| `security` | Regra de segurança e de segredos | FR-001, FR-025 |
| `dictionaryRef` | Referência ao dicionário `§0.4` | FR-007 |
| `message` | Bloco com o template de 10 campos | FR-001, FR-003 |

### StopRegistry

| Campo | Regra | Requisito |
|---|---|---|
| `stops` | 16 códigos `STOP-IN/ENV/DER/EXEC/RULE` | FR-002, FR-012 |
| `confirmations` | 2 códigos `STOP-CONF` | FR-012 |
| `total` | 18 códigos únicos | SC-001 |
| `messages` | 18 blocos completos, 1 por código | FR-001, FR-012 |
| `siteLinks` | Cada site com `Pare` cita um código registrado | FR-019 |

Distribuição fixa:

| Categoria | Códigos | Quantidade |
|---|---|---:|
| Entrada | `STOP-IN-01` a `STOP-IN-05` | 5 |
| Ambiente | `STOP-ENV-01` a `STOP-ENV-05` | 5 |
| Derivação | `STOP-DER-01` a `STOP-DER-04` | 4 |
| Execução | `STOP-EXEC-01` | 1 |
| Regra | `STOP-RULE-01` | 1 |
| Confirmação | `STOP-CONF-01`, `STOP-CONF-02` | 2 |

### MessageTemplate

| Campo | Regra | Requisito |
|---|---|---|
| `title` | `## PARADA DURA — STOP-...` | FR-003 |
| `labels` | 9 rótulos em negrito + o código no título = 10 campos | FR-001, FR-003 |
| `order` | Código, o que aconteceu, onde, por que, não executado, opções, sugestão, retomar, não fazer, segurança | FR-001 |
| `optionCount` | Mínimo 2; `STOP-DER-02` com exatamente 3 | FR-004, FR-010 |
| `language` | Uma ideia por frase; até 20 palavras; voz ativa; sem culpa | FR-005, FR-026, FR-029 |
| `terms` | Sigla/termo traduzido na primeira aparição | FR-005, FR-027 |
| `dictionary` | Referência a `§0.4` em toda mensagem | FR-007 |
| `secrets` | Não imprime segredo, credencial ou contrato completo | FR-025 |

### DictionaryTerm

| Campo | Regra | Requisito |
|---|---|---|
| `term` | Um dos 20 termos obrigatórios | FR-006 |
| `translation` | Tradução em linguagem simples | FR-030 |
| `location` | `prompt-00` §0.4, acessível no ponto de entrada | FR-007 |
| `reference` | Toda mensagem aponta para o dicionário | FR-007 |

Termos obrigatórios: regex, JAR, Maven, archetype, plugin, goal, package, FQN, POJO, hash, canônico, idempotente, proveniência, materializar, precedência, fixture, suíte, derivação, default e command line.

### CriticalFlagRow

| Campo | Regra | Requisito |
|---|---|---|
| `flag` | Uma das 9 flags da tabela de `regras-04` §3.1 | FR-018 |
| `default` | Igual ao `defaultValue` do catálogo | FR-018, FR-023 |
| `formatRule` | Regex literal ou formato descritivo | FR-015, FR-018 |
| `columns` | A linha tem exatamente 3 células | FR-015 |

As 9 flags: `entity_name`, `entity_package_name`, `custom_entity_attributes_list`, `custom_entity_attribute_imports`, `contract_enum_definitions`, `contract_nested_type_definitions`, `entity_contract_file_path`, `enable_custom_entity_attributes` e `enable_folder_src_test_java`.

### StopSite

| Campo | Regra | Requisito |
|---|---|---|
| `file` | Um dos 10 Markdown alterados | FR-019 |
| `section` | Seção legível + caminho | FR-028 |
| `line` | Contém **Pare com `STOP-...`** ou bloco **PARADA DURA** | FR-019 |
| `code` | Código registrado | FR-002, FR-019 |
| `messageRef` | Aponta para `§0.5` | FR-007 |

### CatalogDescription

| Campo | Regra | Requisito |
|---|---|---|
| `flag` | `entity_contract_file_path` | FR-013 |
| `absoluteNote` | Diz que o procedimento exige caminho absoluto | FR-013 |
| `regex` | Continua `^(none|[^\r\n]*\.json)$` | FR-013, FR-023 |
| `default` | Continua `none` | FR-023 |

### ProvenanceRule

| Campo | Regra | Requisito |
|---|---|---|
| `defaultOmission` | Defaults inertes podem ficar fora da tabela | FR-014 |
| `exception` | Default que uma regra específica manda registrar aparece com fonte `DEFAULT` e nota | FR-014 |
| `example` | `contract_nested_type_definitions` = `none` | FR-014 |
| `stopDer02` | Lista inválida não vira default; para com `STOP-DER-02` | FR-011 |

### NamingRule

| Campo | Regra | Requisito |
|---|---|---|
| `entityPackageName` | Minúsculas, sem separadores: `PedidoItem` → `pedidoitem` | FR-017 |
| `artifactId` | Kebab-case: `PedidoItem` → `pedido-item` | FR-017 |
| `packageBase` | Não contém o nome da entidade | FR-017 |

### AcceptanceReview

| Campo | Regra | Requisito |
|---|---|---|
| `sample` | 6 mensagens fixas: `STOP-IN-01`, `STOP-ENV-03`, `STOP-DER-01`, `STOP-DER-02`, `STOP-CONF-01`, `STOP-CONF-02` | SC-002 |
| `reviewer` | Não participou da implementação | SC-002 |
| `questions` | O que aconteceu, onde, opções, sugestão, retomada | SC-002 |
| `result` | 6/6 linhas com `Resultado = OK` | SC-002 |
| `evidence` | Tabela em `validation-report.md`, com revisor e data | SC-002 |

### BuildBaseline

| Campo | Regra | Requisito |
|---|---|---|
| `command` | `mvn -B test` no mesmo ambiente, após aquecimento | NFR-001 |
| `baselineSeconds` | Medição antes da mudança | NFR-001 |
| `finalSeconds` | Medição depois da mudança | NFR-001 |
| `delta` | `(final - baseline) / baseline ≤ 0.10` | NFR-001 |
| `evidence` | `validation-report.md` | NFR-001 |

### OfflineRun

| Campo | Regra | Requisito |
|---|---|---|
| `command` | `mvn -o -B test` | NFR-002 |
| `runs` | Duas execuções no mesmo ambiente, sem rede | NFR-002 |
| `filter` | Linhas `[archetype-post-generate]` | NFR-002 |
| `result` | Saídas idênticas | NFR-002 |
| `evidence` | `validation-report.md` | NFR-002 |

## Invariantes

| ID | Invariante | Requisito |
|---|---|---|
| INV-01 | 18 códigos únicos, 16 paradas + 2 confirmações, todos registrados | FR-002, FR-012 |
| INV-02 | Cada código tem exatamente uma mensagem completa | FR-001, SC-001 |
| INV-03 | Cada mensagem tem os 10 campos, 2+ opções, 1 sugestão e 1 referência ao dicionário | FR-001, FR-004, FR-007 |
| INV-04 | `STOP-DER-02` tem 3 opções e não usa `String:name` como fallback | FR-009, FR-010 |
| INV-05 | Toda linha com `Pare` cita um código registrado | FR-019 |
| INV-06 | O dicionário tem os 20 termos com tradução | FR-006, FR-030 |
| INV-07 | A tabela de flags críticas tem 3 colunas e 9 defaults sincronizados | FR-015, FR-018 |
| INV-08 | As 3 regexes literais da tabela conferem com o catálogo | FR-018 |
| INV-09 | O procedimento não contém "informe apenas" | FR-021 |
| INV-10 | 19 arquivos, 13 Markdown e 6 JSON | FR-022 |
| INV-11 | 35 propriedades e 18 filesets; nenhum default/regex alterado | FR-023 |
| INV-12 | O catálogo diz que o procedimento exige caminho absoluto, mas a regex segue permissiva | FR-013 |
| INV-13 | A proveniência tem a exceção de `none` | FR-014 |
| INV-14 | `FR-006` está explicado em linguagem simples | FR-016 |
| INV-15 | `pedidoitem` e `pedido-item` aparecem com a diferença explícita | FR-017 |
| INV-16 | O JAR instalado contém o registro, o dicionário e as mensagens | SC-012 |
| INV-17 | SC-002 usa 6 mensagens fixas, revisor independente e tabela com data/resultado | SC-002 |
| INV-18 | NFR-001 registra baseline e final de `mvn -B test`, com delta ≤10% | NFR-001 |
| INV-19 | NFR-002 registra duas execuções offline com a mesma saída | NFR-002 |

## Fluxo de parada

```text
entrada do operador
  → Gate A/B/C/D ou regra de derivação/execução
  → código STOP-...
  → mensagem completa de §0.5 (10 campos)
  → nenhuma derivação e nenhum Maven
  → operador corrige a entrada
  → procedimento recomeça do ponto indicado
```

### `STOP-DER-02` em detalhe

```text
contrato JSON
  → derivação de attributes/fields/campos de topo
  → normalização e descarte já existentes
  → lista final no formato Tipo:nome
  → validação contra a regex do catálogo
      ├── válida → segue para a command line
      └── inválida → STOP-DER-02, sem default, sem Maven
```

## Regras de validação

| Regra | Verificação | Suíte |
|---|---|---|
| Código único | 18 códigos, sem duplicata, no padrão `STOP-[A-Z]+-\d{2}` | `HardStopContractTest` |
| Registro | Todo código citado está no registro; todo código registrado tem mensagem | `HardStopContractTest` |
| Site | Toda linha com `Pare` cita código registrado | `HardStopContractTest` |
| Mensagem | 10 campos, 2+ opções, sugestão, retomada, proibição, segurança | `HardStopContractTest` |
| Dicionário | 20 termos e traduções não vazias | `HardStopContractTest` |
| `STOP-DER-02` | 3 opções; sem fallback `String:name`; sem command line | `HardStopContractTest` |
| Proibição | 0 ocorrências de "informe apenas" | `HardStopContractTest` |
| Contagem | 19/13/6 e 35/18 | `HardStopContractTest` + suítes existentes |
| Tabela | 3 colunas; 9 defaults; 3 regexes literais | `CatalogSyncContractTest` |
| Catálogo | Caminho absoluto descrito; regex permissiva intacta | `CatalogSyncContractTest` |
| Proveniência | Exceção de `none` presente | `CatalogSyncContractTest` |
| FR-006 | Explicação simples presente | `CatalogSyncContractTest` |
| Nomes | `pedidoitem` × `pedido-item` explícitos | `CatalogSyncContractTest` |
| **SC-002** | 6 mensagens fixas, revisor independente, 5 respostas e tabela em `validation-report.md` | `validation-report.md` |
| **NFR-001** | Baseline/final de `mvn -B test`; delta ≤10% | `validation-report.md` |
| **NFR-002** | Duas execuções `mvn -o -B test` com saídas idênticas | `validation-report.md` |
| JAR | Registro, dicionário e `STOP-DER-02` no artefato instalado | `HardStopContractTest` (opt-in) |
