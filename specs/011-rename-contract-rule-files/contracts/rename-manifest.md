# Rename Manifest: Pasta 03 e Seis Arquivos de Regras

**Feature**: 011-rename-contract-rule-files | **Date**: 2026-11-26

## Propósito

Este manifesto é a fonte única do mapa `old → new`, do inventário de referências por arquivo/linha/fase, das exceções e das verificações. A implementação consome este arquivo; `ContractRuleRenameContractTest.groovy` valida o resultado.

## Baseline (Fase 0)

- Contagens: 19 arquivos, 13 Markdown, 6 JSON, 4 subpastas, 6 arquivos em 03.
- Destinos ausentes: `03-regras-de-formacao-do-contrato/` e os seis nomes novos.
- SHA-256 dos seis arquivos:
  - `01-como-derivar-a-entidade-do-contrato.md`: `086B6C7685DA36FB8A33BF905677649F3E2D4B446DF8E6AF50CC7FDAE958250B`
  - `02-como-resolver-tipos-e-enums.md`: `C61D421AD3C9461DA40559DCA573E1FD284C78FF576C9234279F3CB67CA04CD1`
  - `03-como-derivar-flags-de-tipos-aninhados.md`: `52A6976F03D6113552AA5805A56F770D6074B1024CC624FEFD77B72D8312A8FA`
  - `04-como-aplicar-precedencia-e-validacao.md`: `9C7F3986CDB566F4C086B32480C1368F3DCF2D5EB0FD7115D07CF4B9CB09A894`
  - `05-como-materializar-e-emitir-o-contrato.md`: `90C070120A09100D5553790F432AFC57C38BDADC80A7BCF68869BCFC298DE2F5`
  - `06-como-relatar-proveniencia.md`: `E908BC67A589F76FCB0974D636B2C5F1AAD0282C135DA0F70531F2BAF78FFD08`

## Mapa old → new

### Pasta

| Old | New | Fase |
|---|---|---|
| `03-derivacao-regras-de-formacao-do-contrato` | `03-regras-de-formacao-do-contrato` | 1 |

### Arquivos

| # | Old | New | Fase |
|---|---|---|---|
| 01 | `01-como-derivar-a-entidade-do-contrato.md` | `01-derivar-a-entidade-do-contrato.md` | 2 |
| 02 | `02-como-resolver-tipos-e-enums.md` | `02-resolver-tipos-e-enums.md` | 2 |
| 03 | `03-como-derivar-flags-de-tipos-aninhados.md` | `03-derivar-flags-de-tipos-aninhados.md` | 2 |
| 04 | `04-como-aplicar-precedencia-e-validacao.md` | `04-aplicar-precedencia-e-validacao.md` | 3 |
| 05 | `05-como-materializar-e-emitir-o-contrato.md` | `05-materializar-e-emitir-o-contrato.md` | 3 |
| 06 | `06-como-relatar-proveniencia.md` | `06-relatar-proveniencia.md` | 3 |

## Mapa de tokens

| Token | Uso | Ação |
|---|---|---|
| `03-derivacao-regras-de-formacao-do-contrato` | pasta, caminho, âncora, título, link | substituir por `03-regras-de-formacao-do-contrato` na Fase 1 |
| `01-como-derivar-a-entidade-do-contrato.md` | caminho/nome | substituir por `01-derivar-a-entidade-do-contrato.md` na Fase 2 |
| `02-como-resolver-tipos-e-enums.md` | caminho/nome | substituir por `02-resolver-tipos-e-enums.md` na Fase 2 |
| `03-como-derivar-flags-de-tipos-aninhados.md` | caminho/nome | substituir por `03-derivar-flags-de-tipos-aninhados.md` na Fase 2 |
| `04-como-aplicar-precedencia-e-validacao.md` | caminho/nome | substituir por `04-aplicar-precedencia-e-validacao.md` na Fase 3 |
| `05-como-materializar-e-emitir-o-contrato.md` | caminho/nome | substituir por `05-materializar-e-emitir-o-contrato.md` na Fase 3 |
| `06-como-relatar-proveniencia.md` | caminho/nome | substituir por `06-relatar-proveniencia.md` na Fase 3 |
| `01..03`, `01`, `02`, `03`, `04`, `05`, `06` | formas curtas sem `como-` | não mudam; o numeral permanece |

## Inventário de referências

### README.md — pasta

| # | Linha | Referência | Ação | Fase |
|---|---|---|---|---|
| 1 | 54 | link do índice (`href` + texto) | substituir o segmento da pasta | 1 |
| 2 | 607 | caminho só da pasta em lista | substituir o segmento da pasta | 1 |
| 3 | 652 | lista de categorias na tabela | substituir o segmento da pasta | 1 |
| 4 | 816 | `id` e `name` da âncora (2 ocorrências) | substituir o segmento da pasta | 1 |
| 5 | 817 | título `### 03-derivacao-...` | substituir o segmento da pasta | 1 |
| 6 | 995 | texto e `href` do link (2 ocorrências) | substituir o segmento da pasta | 1 |
| 7 | 2167 | caminho em prosa | substituir o segmento da pasta | 1 |
| 8 | 2208 | caminho em prosa | substituir o segmento da pasta | 1 |
| 9 | 2391 | caminho + `01..03` | substituir só o segmento da pasta | 1 |
| 10 | 2868 | caminho + `01` | substituir só o segmento da pasta | 1 |

### README.md — nomes curtos dos seis arquivos

| Linha | Old | New | Fase |
|---|---|---|---|
| 2171 | `01-como-derivar-a-entidade-do-contrato.md` | `01-derivar-a-entidade-do-contrato.md` | 2 |
| 2172 | `02-como-resolver-tipos-e-enums.md` | `02-resolver-tipos-e-enums.md` | 2 |
| 2173 | `03-como-derivar-flags-de-tipos-aninhados.md` | `03-derivar-flags-de-tipos-aninhados.md` | 2 |
| 2174 | `04-como-aplicar-precedencia-e-validacao.md` | `04-aplicar-precedencia-e-validacao.md` | 3 |
| 2175 | `05-como-materializar-e-emitir-o-contrato.md` | `05-materializar-e-emitir-o-contrato.md` | 3 |
| 2176 | `06-como-relatar-proveniencia.md` | `06-relatar-proveniencia.md` | 3 |

### Procedimento

| Arquivo | Linha | Referência | Pasta (fase) | Arquivo (fase) |
|---|---|---|---|---|
| `01-entrada-inicializacao/00-entry-point.md` | 715 | caminho completo do 01 | 1 | 2 |
| | 716 | caminho completo do 02 | 1 | 2 |
| | 717 | caminho completo do 03 | 1 | 2 |
| | 718 | caminho completo do 04 | 1 | 3 |
| | 719 | caminho completo do 05 | 1 | 3 |
| | 720 | caminho completo do 06 | 1 | 3 |
| `01-entrada-inicializacao/01-mapa-dos-arquivos-e-ordem-de-leitura.md` | 18 | caminho completo do 01 | 1 | 2 |
| | 19 | caminho completo do 02 | 1 | 2 |
| | 20 | caminho completo do 03 | 1 | 2 |
| | 21 | caminho completo do 04 | 1 | 3 |
| | 22 | caminho completo do 05 | 1 | 3 |
| | 23 | caminho completo do 06 | 1 | 3 |
| `02-operacao-procedimento-do-agente/02-como-localizar-e-ler-o-archetype.md` | 141 | caminho só da pasta | 1 | — |

### Fixtures

| Arquivo | Linha | Referência | Fase |
|---|---|---|---|
| `src/test/resources/projects/custom-nested-edge-cases/archetype.properties` | 16 | caminho da pasta em comentário | 1 |
| `src/test/resources/projects/custom-nested-objects/archetype.properties` | 16 | caminho da pasta em comentário | 1 |
| `src/test/resources/projects/entity-contract/archetype.properties` | 20 | caminho da pasta em comentário | 1 |
| `src/test/resources/projects/custom-enums-from-payload/archetype.properties` | 17 | caminho da pasta em comentário | 1 |

### Suítes existentes

| Suíte | Linhas | Referência | Fase |
|---|---|---|---|
| `BuildArtifactsTest.groovy` | 84 | categoria 03 | 1 |
| | 96–101 | 6 caminhos completos | 1 (pasta) + 2 (01–03) / 3 (04–06) |
| `CatalogSyncContractTest.groovy` | 9 | caminho do 04 em `target/classes` | 1 (pasta) + 3 (arquivo) |
| | 162 | caminho do 06 em `target/classes` | 1 (pasta) + 3 (arquivo) |
| `DestinationContractTest.groovy` | 40 | caminho do 01 | 1 (pasta) + 2 (arquivo) |
| | 41 | caminho do 06 | 1 (pasta) + 3 (arquivo) |
| `FileRenameContractTest.groovy` | 12 | `r03` (pasta) | 1 |
| | 32–37 | `p1Old` (`regras-*`) | refatorar para `p1LegacyOld`; ver abaixo |
| | 40–45 | `p1New` (nomes atuais `como-*`) | renomear para `p1Current`; ver abaixo |
| | 99 | pasta empacotada | 1 |
| | 107 | `idx03` | 1 (pasta) + 2/3 (nomes) |
| | 112 | asserção negativa | 1 (pasta) + 2/3 (nomes) |
| | 125 | lista de diretórios do corpus | 1 |
| `HardStopContractTest.groovy` | 110 | caminho do 01 | 1 (pasta) + 2 (arquivo) |
| `PathRenameContractTest.groovy` | 43 | categoria 03 | 1 |
| | 55–60 | 6 caminhos completos | 1 (pasta) + 2 (01–03) / 3 (04–06) |
| `ProcedureContractTest.groovy` | 198 | caminho do 03 | 1 (pasta) + 2 (arquivo) |
| | 252 | caminho do 01 | 1 (pasta) + 2 (arquivo) |
| `ProcedureEntryRenameContractTest.groovy` | 71 | lista das 4 categorias | 1 |
| `ReadmeConsistencyTest.groovy` | — | sem referência à pasta 03 nem aos seis nomes | — |

### Suíte nova

| Arquivo | Papel |
|---|---|
| `src/test/archetype-post-generate/ContractRuleRenameContractTest.groovy` | Contrato phase-aware da pasta 03 e dos seis nomes; contagens 19/13/6/4; índice; README; fixtures; redirect legado; anti-drift; estado final |
| `src/test/groovy/run-archetype-post-generate-tests.groovy` | Registrar a suíte nova no bloco `packaged procedure` |

## Refatoração de `FileRenameContractTest.groovy`

O teste atual é phase-aware para a feature 009 (`regras-*` → `como-*`) e para 04 (`template-*` → nomes atuais). Para esta feature, refatorar as listas sem perder a cobertura histórica:

| Lista | Conteúdo | Uso |
|---|---|---|
| `p1LegacyOld` | `regras-01-como-...` a `regras-06-como-...` | nomes históricos da feature 009; sempre ausentes |
| `p1Current` | `01-como-...` a `06-como-...` | nomes atuais antes da renomeação |
| `p1New` | `01-derivar-...` a `06-relatar-...` | nomes de destino |

Detecção de fase da pasta 03:

| `rulePhase` | Pasta 03 | Arquivos 03 | Nomes antigos que devem estar ausentes |
|---|---|---|---|
| 0 | `03-derivacao-...` | `p1Current` | `p1LegacyOld` |
| 1 | `03-regras-...` | `p1Current` | `p1LegacyOld` |
| 2 | `03-regras-...` | `p1New[0..2]` + `p1Current[3..5]` | `p1LegacyOld` + `p1Current[0..2]` |
| 3 | `03-regras-...` | `p1New` | `p1LegacyOld` + `p1Current` |

`rulePhase` deve ser calculado pela existência da pasta nova e dos nomes novos; as asserções de 04 permanecem como estão.

## Exceções

| Exceção | Motivo | Tratamento |
|---|---|---|
| `src/main/resources/utils/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md` | compatibilidade com specs anteriores | permanece no caminho antigo; aponta para `00-entry-point.md`; não cita a pasta 03 |
| Asserções negativas das suítes (`p1LegacyOld`, `p1Current`, anti-drift) | precisam conter nomes antigos para provar ausência | permitidas por FR-009; não contam como referência operacional |
| `specs/`, `graphify-out/`, `target/`, `.git/`, artefato instalado | fora do corpus operacional | não reescrever |

## Verificação

### Fase 1

```powershell
$procedure = "src/main/resources/llm-maven-flags-inference"
Test-Path (Join-Path $procedure "03-regras-de-formacao-do-contrato")
Test-Path (Join-Path $procedure "03-derivacao-regras-de-formacao-do-contrato")
Get-ChildItem (Join-Path $procedure "03-regras-de-formacao-do-contrato") -File | Select-Object -ExpandProperty Name
```

Esperado: pasta nova existe; pasta antiga não; 6 arquivos com os nomes atuais (`como-*`).

Varredura da pasta antiga no corpus operacional, excluindo exceções:

```powershell
Get-ChildItem README.md, $procedure, "src/test" -Recurse -File |
  Where-Object { $_.FullName -notmatch '\\specs\\|\\graphify-out\\|\\target\\|\\utils\\|ContractRuleRenameContractTest|FileRenameContractTest' } |
  Select-String -Pattern '03-derivacao-regras-de-formacao-do-contrato'
```

Esperado: 0 linhas.

### Fase 2

```powershell
Get-ChildItem (Join-Path $procedure "03-regras-de-formacao-do-contrato") -File | Select-Object -ExpandProperty Name
```

Esperado: `01-derivar-a-entidade-do-contrato.md`, `02-resolver-tipos-e-enums.md`, `03-derivar-flags-de-tipos-aninhados.md` e os três nomes antigos de 04–06.

```powershell
Get-ChildItem README.md, $procedure, "src/test" -Recurse -File |
  Where-Object { $_.FullName -notmatch '\\specs\\|\\graphify-out\\|\\target\\|\\utils\\|ContractRuleRenameContractTest|FileRenameContractTest' } |
  Select-String -Pattern '01-como-derivar-a-entidade-do-contrato|02-como-resolver-tipos-e-enums|03-como-derivar-flags-de-tipos-aninhados'
```

Esperado: 0 linhas, exceto asserções negativas.

### Fase 3

```powershell
Get-ChildItem (Join-Path $procedure "03-regras-de-formacao-do-contrato") -File | Select-Object -ExpandProperty Name
(Get-ChildItem $procedure -Recurse -File).Count
(Get-ChildItem $procedure -Recurse -File -Filter *.md).Count
(Get-ChildItem $procedure -Recurse -File -Filter *.json).Count
```

Esperado: seis nomes novos; 19 arquivos; 13 Markdown; 6 JSON; 4 subpastas.

```powershell
Get-ChildItem README.md, $procedure, "src/test" -Recurse -File |
  Where-Object { $_.FullName -notmatch '\\specs\\|\\graphify-out\\|\\target\\|\\utils\\|ContractRuleRenameContractTest|FileRenameContractTest' } |
  Select-String -Pattern '03-derivacao-regras-de-formacao-do-contrato|0[1-6]-como-'
```

Esperado: 0 linhas, exceto asserções negativas.

### Comandos de build

```powershell
mvn -B clean test
mvn clean install
mvn -B clean integration-test
mvn -B test "-Deda.verify.installed.jar=true"
```
