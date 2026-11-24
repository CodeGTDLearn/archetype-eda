# Quickstart — Validar o Contrato de Parada Dura

**Feature**: 007-hard-stop-contract | **Date**: 2026-11-24

## Pré-requisitos

- Java 17 e Maven 3.9.x no PATH.
- Execução na raiz do repositório.
- PowerShell (os comandos abaixo) ou equivalente.
- O JAR instalado só é necessário nos cenários opt-in.

## Cenário 1 — Suítes de contrato

```powershell
mvn -B test
```

**Esperado**: todas as suítes passam, incluindo `HardStopContractTest` e `CatalogSyncContractTest`.

**Cobre**: SC-001, SC-003, SC-006, SC-008, SC-009, SC-010.

## Cenário 2 — Build completo

```powershell
mvn clean install
```

**Esperado**: `BUILD SUCCESS`; os 19 arquivos do procedimento e o catálogo de 35 propriedades/18 filesets são empacotados.

**Cobre**: SC-005, SC-006, SC-011.

## Cenário 3 — JAR instalado (opt-in)

```powershell
mvn -B test "-Deda.verify.installed.jar=true"
```

**Esperado**: o teste abre o JAR em `~/.m2` e confirma o registro, o dicionário e a mensagem `STOP-DER-02`.

**Cobre**: SC-012.

## Cenário 4 — Registro e dicionário no procedimento

```powershell
$p = "src/main/resources/llm-maven-flags-inference/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md"
$lines = Get-Content $p -Encoding UTF8
$start = ($lines | Select-String '^### 0\.5 ').LineNumber
$end = ($lines | Select-String '^## 1\. ').LineNumber
$messages = $lines[($start)..($end - 2)] | Where-Object { $_ -match '## PARADA DURA — STOP-' }
"MENSAGENS=$($messages.Count)"
$start = ($lines | Select-String '^### 0\.4 ').LineNumber
$end = ($lines | Select-String '^### 0\.5 ').LineNumber
$dictionary = $lines[($start)..($end - 2)] | Where-Object { $_ -match '^\|' }
"DICIONARIO=$($dictionary.Count - 2)"
```

**Esperado**:

- 18 mensagens no registro `§0.5` (16 paradas + 2 confirmações).
- 20 termos no dicionário.
- Template canônico com os 10 campos.

**Cobre**: FR-001, FR-002, FR-006, FR-007, FR-012, SC-001, SC-008.

## Cenário 5 — `STOP-DER-02` sem gerar módulo

1. Monte um spec com use-case, um contrato com lista de atributos inválida e uma pasta de destino absoluta.
2. Peça ao agente para seguir o procedimento.
3. Verifique a resposta.

**Esperado**:

- Código `STOP-DER-02`.
- Segmento inválido, formato `Tipo:nome` e 3 opções.
- Sugestão e forma de retomar.
- Nenhuma linha `mvn archetype:generate` e nenhum módulo criado.

**Cobre**: FR-008, FR-009, FR-010, SC-004.

## Cenário 6 — Tabela de flags críticas

```powershell
$r = "src/main/resources/llm-maven-flags-inference/03-derivacao-regras-de-formacao-do-contrato/regras-04-como-aplicar-precedencia-e-validacao.md"
$lines = Get-Content $r -Encoding UTF8
$start = ($lines | Select-String '^### 3\.1 ').LineNumber
$end = ($lines | Select-String '^## ' | Where-Object { $_.LineNumber -gt $start } | Select-Object -First 1).LineNumber
$lines[($start)..($end - 2)] | Where-Object { $_ -match '^\|' }
```

**Esperado**: 9 linhas de flag e exatamente 3 colunas em cada linha; os pipes internos aparecem escapados.

**Cobre**: FR-015, FR-018, SC-009, SC-010.

## Cenário 7 — Caminho absoluto no catálogo

```powershell
$c = "src/main/resources/META-INF/maven/archetype_metadata_required_properties/property-20-entity_contract_file_path.xml"
Select-String -Path $c -Pattern 'absoluto','absolute'
```

**Esperado**: a descrição diz que o procedimento exige caminho absoluto; `defaultValue` continua `none` e a regex continua permissiva.

**Cobre**: FR-013, FR-023, SC-011.

## Cenário 8 — Proveniência e FR-006

```powershell
$r6 = "src/main/resources/llm-maven-flags-inference/03-derivacao-regras-de-formacao-do-contrato/regras-06-como-relatar-proveniencia.md"
$r4 = "src/main/resources/llm-maven-flags-inference/03-derivacao-regras-de-formacao-do-contrato/regras-04-como-aplicar-precedencia-e-validacao.md"
Select-String -Path $r6 -Pattern 'contract_nested_type_definitions','none'
Select-String -Path $r4 -Pattern 'FR-006'
```

**Esperado**: a exceção de proveniência cita `contract_nested_type_definitions` = `none`; `FR-006` é explicado em uma frase simples.

**Cobre**: FR-014, FR-016.

## Cenário 9 — Nome do pacote × nome do artefato

```powershell
$p3 = "src/main/resources/llm-maven-flags-inference/02-operacao-procedimento-do-agente/prompt-03-regras-obrigatorias-de-geracao.md"
Select-String -Path $p3 -Pattern 'pedidoitem','pedido-item'
```

**Esperado**: as duas formas aparecem na mesma seção, com a diferença explícita.

**Cobre**: FR-017.

## Cenário 10 — Negativos de build

Aplicar cada alteração temporariamente e reverter depois:

| Alteração | Falha esperada |
|---|---|
| Remover um código de um site com `Pare` | `HardStopContractTest` nomeia o arquivo |
| Adicionar um `STOP-*` sem registro | `HardStopContractTest` nomeia o código |
| Remover uma mensagem do registro | `HardStopContractTest` nomeia o código |
| Remover um termo do dicionário | `HardStopContractTest` nomeia o termo |
| Inserir "informe apenas" | `HardStopContractTest` falha |
| Mudar um default na tabela | `CatalogSyncContractTest` nomeia a flag |
| Mudar uma regex literal na tabela | `CatalogSyncContractTest` nomeia a flag |
| Adicionar um 20º arquivo ao procedimento | A suíte de contagem falha |
| Build final >10% acima da linha de base | A verificação de NFR-001 falha |
| Duas execuções offline com saídas diferentes | A verificação de NFR-002 falha |
| Amostra de SC-002 com menos de 6 mensagens | A verificação de SC-002 falha |

**Cobre**: FR-019, FR-020, FR-021, FR-022, FR-023, FR-018.

## Cenário 11 — Contagens

```powershell
$root = "src/main/resources/llm-maven-flags-inference"
"TOTAL=$((Get-ChildItem -Recurse $root -File).Count)"
"MD=$((Get-ChildItem -Recurse $root -Filter *.md -File).Count)"
"JSON=$((Get-ChildItem -Recurse $root -Filter *.json -File).Count)"
```

**Esperado**: `19`, `13`, `6`.

**Cobre**: FR-022, SC-005.

## Cenário 12 — Operador iniciante (manual)

Amostra fixa de 6 mensagens: `STOP-IN-01`, `STOP-ENV-03`, `STOP-DER-01`, `STOP-DER-02`, `STOP-CONF-01` e `STOP-CONF-02`.

1. Peça a uma pessoa que começou ontem, e que **não participou da implementação**, para responder sem ajuda externa:
   - O que aconteceu?
   - Onde está o problema?
   - Quais são as opções?
   - Qual é a recomendada?
   - O que fazer agora para retomar?
2. Registre as respostas em `specs/007-hard-stop-contract/validation-report.md`, com revisor e data.

**Esperado**: 6/6 mensagens com as 5 respostas corretas e `Resultado = OK`.

**Cobre**: SC-002, FR-005, FR-026, FR-027, FR-028, FR-029.

## Cenário 13 — NFR-001: tempo de build

```powershell
mvn -B test
$baseline = (Measure-Command { mvn -B test }).TotalSeconds
# depois da implementação
$final = (Measure-Command { mvn -B test }).TotalSeconds
$delta = (($final - $baseline) / $baseline) * 100
$delta
```

**Esperado**: `delta ≤ 10`; registrar `baseline`, `final` e `delta` em `validation-report.md`.

**Cobre**: NFR-001.

## Cenário 14 — NFR-002: determinismo offline

```powershell
$run1 = Join-Path $env:TEMP "eda-run1.txt"
$run2 = Join-Path $env:TEMP "eda-run2.txt"
mvn -o -B test | Select-String '^\[archetype-post-generate\]' | Set-Content $run1
mvn -o -B test | Select-String '^\[archetype-post-generate\]' | Set-Content $run2
Compare-Object (Get-Content $run1) (Get-Content $run2)
```

**Esperado**: nenhuma diferença; nenhuma chamada de rede. Registrar o resultado em `validation-report.md`.

**Cobre**: NFR-002.

## Matriz de cobertura

| Cenário | Requisitos | Critérios |
|---|---|---|
| 1 | FR-019, FR-020 | SC-003, SC-006, SC-008 |
| 2 | FR-022, FR-023 | SC-005, SC-011 |
| 3 | FR-001, FR-002 | SC-012 |
| 4 | FR-001, FR-002, FR-006, FR-007 | SC-001, SC-008 |
| 5 | FR-008, FR-009, FR-010 | SC-004 |
| 6 | FR-015, FR-018 | SC-009, SC-010 |
| 7 | FR-013, FR-023 | SC-011 |
| 8 | FR-014, FR-016 | — |
| 9 | FR-017 | — |
| 10 | FR-018, FR-019, FR-020, FR-021, FR-022 | SC-003, SC-007 |
| 11 | FR-022 | SC-005 |
| 12 | FR-005, FR-026, FR-027, FR-028, FR-029 | SC-002 |
| 13 | — | NFR-001 |
| 14 | — | NFR-002 |
