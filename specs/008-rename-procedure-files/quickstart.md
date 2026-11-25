# Quickstart: Validar a Renomeação das Subpastas 01 e 02

**Feature**: `008-rename-procedure-files` | **Date**: 2026-11-25

## Pré-requisitos

- PowerShell 5.1 ou superior, na raiz do repositório.
- Nenhuma dependência nova.
- Não abrir os arquivos em editor durante a renomeação.
- Não usar `git mv`, commit, push ou branch.
- Não editar README, índice, fixtures, suítes ou o redirect em `src/main/resources/utils/`.

Variáveis de trabalho:

```powershell
$root = "src/main/resources/llm-maven-flags-inference"
$tmp = "$env:TEMP\eda-008"
New-Item -ItemType Directory -Force $tmp | Out-Null
```

## Fase 1 — Entrada e mapa da subpasta 01

### 1. Capturar o manifesto antes da fase

```powershell
@(
  "01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md",
  "01-entrada-inicializacao/indice-01-mapa-dos-arquivos-e-ordem-de-leitura.md"
) |
  ForEach-Object { Get-FileHash (Join-Path $root $_) -Algorithm SHA256 } |
  Select-Object Path, Hash |
  ConvertTo-Json |
  Set-Content "$tmp\fase1-alvos-antes.json" -Encoding UTF8
```

```powershell
@(
  "02-operacao-procedimento-do-agente",
  "03-derivacao-regras-de-formacao-do-contrato",
  "04-referencia-templates-de-contrato"
) |
  ForEach-Object { Get-ChildItem (Join-Path $root $_) -Recurse -File } |
  ForEach-Object { Get-FileHash $_.FullName -Algorithm SHA256 } |
  Select-Object Path, Hash |
  ConvertTo-Json |
  Set-Content "$tmp\fase1-controle-antes.json" -Encoding UTF8
```

### 2. Conferir as pré-condições

```powershell
Test-Path (Join-Path $root "01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md")
Test-Path (Join-Path $root "01-entrada-inicializacao/indice-01-mapa-dos-arquivos-e-ordem-de-leitura.md")
Test-Path (Join-Path $root "01-entrada-inicializacao/01-entryPoint.md")
Test-Path (Join-Path $root "01-entrada-inicializacao/02-mapa-dos-arquivos-e-ordem-de-leitura.md")
```

Esperado: `True`, `True`, `False`, `False`.

### 3. Renomear

```powershell
Rename-Item (Join-Path $root "01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md") "01-entryPoint.md"
Rename-Item (Join-Path $root "01-entrada-inicializacao/indice-01-mapa-dos-arquivos-e-ordem-de-leitura.md") "02-mapa-dos-arquivos-e-ordem-de-leitura.md"
```

### 4. Validar a subpasta 01

```powershell
Get-ChildItem (Join-Path $root "01-entrada-inicializacao") -File |
  Select-Object -ExpandProperty Name
```

Esperado, exatamente nesta ordem alfabética:

```text
01-entryPoint.md
02-mapa-dos-arquivos-e-ordem-de-leitura.md
```

### 5. Validar o conteúdo

```powershell
$antes = Get-Content "$tmp\fase1-alvos-antes.json" | ConvertFrom-Json
$depois = @(
  (Get-FileHash (Join-Path $root "01-entrada-inicializacao/01-entryPoint.md") -Algorithm SHA256).Hash,
  (Get-FileHash (Join-Path $root "01-entrada-inicializacao/02-mapa-dos-arquivos-e-ordem-de-leitura.md") -Algorithm SHA256).Hash
)
Compare-Object @($antes | ForEach-Object Hash) $depois
```

Esperado: nenhuma saída (0 divergências).

### 6. Validar as subpastas de controle

```powershell
$controleAntes = Get-Content "$tmp\fase1-controle-antes.json" | ConvertFrom-Json
$controleDepois = @(
  "02-operacao-procedimento-do-agente",
  "03-derivacao-regras-de-formacao-do-contrato",
  "04-referencia-templates-de-contrato"
) |
  ForEach-Object { Get-ChildItem (Join-Path $root $_) -Recurse -File } |
  ForEach-Object { Get-FileHash $_.FullName -Algorithm SHA256 } |
  Select-Object Path, Hash
Compare-Object @($controleAntes | ForEach-Object { "$($_.Path)|$($_.Hash)" }) @($controleDepois | ForEach-Object { "$($_.Path)|$($_.Hash)" })
```

Esperado: nenhuma saída.

## Fase 2 — Operação do agente sem o prefixo prompt-

### 1. Capturar o manifesto antes da fase

```powershell
@(
  "02-operacao-procedimento-do-agente/prompt-01-papel-e-restricoes-do-agente.md",
  "02-operacao-procedimento-do-agente/prompt-02-como-localizar-e-ler-o-archetype.md",
  "02-operacao-procedimento-do-agente/prompt-03-regras-obrigatorias-de-geracao.md",
  "02-operacao-procedimento-do-agente/prompt-04-fluxo-de-execucao-e-verificacao.md",
  "02-operacao-procedimento-do-agente/prompt-05-entregaveis-e-relatorios.md"
) |
  ForEach-Object { Get-FileHash (Join-Path $root $_) -Algorithm SHA256 } |
  Select-Object Path, Hash |
  ConvertTo-Json |
  Set-Content "$tmp\fase2-alvos-antes.json" -Encoding UTF8
```

```powershell
@(
  "01-entrada-inicializacao",
  "03-derivacao-regras-de-formacao-do-contrato",
  "04-referencia-templates-de-contrato"
) |
  ForEach-Object { Get-ChildItem (Join-Path $root $_) -Recurse -File } |
  ForEach-Object { Get-FileHash $_.FullName -Algorithm SHA256 } |
  Select-Object Path, Hash |
  ConvertTo-Json |
  Set-Content "$tmp\fase2-controle-antes.json" -Encoding UTF8
```

### 2. Renomear

```powershell
Rename-Item (Join-Path $root "02-operacao-procedimento-do-agente/prompt-01-papel-e-restricoes-do-agente.md") "01-papel-e-restricoes-do-agente.md"
Rename-Item (Join-Path $root "02-operacao-procedimento-do-agente/prompt-02-como-localizar-e-ler-o-archetype.md") "02-como-localizar-e-ler-o-archetype.md"
Rename-Item (Join-Path $root "02-operacao-procedimento-do-agente/prompt-03-regras-obrigatorias-de-geracao.md") "03-regras-obrigatorias-de-geracao.md"
Rename-Item (Join-Path $root "02-operacao-procedimento-do-agente/prompt-04-fluxo-de-execucao-e-verificacao.md") "04-fluxo-de-execucao-e-verificacao.md"
Rename-Item (Join-Path $root "02-operacao-procedimento-do-agente/prompt-05-entregaveis-e-relatorios.md") "05-entregaveis-e-relatorios.md"
```

### 3. Validar a subpasta 02

```powershell
Get-ChildItem (Join-Path $root "02-operacao-procedimento-do-agente") -File |
  Select-Object -ExpandProperty Name
```

Esperado, exatamente:

```text
01-papel-e-restricoes-do-agente.md
02-como-localizar-e-ler-o-archetype.md
03-regras-obrigatorias-de-geracao.md
04-fluxo-de-execucao-e-verificacao.md
05-entregaveis-e-relatorios.md
```

### 4. Validar o conteúdo

```powershell
$antes2 = Get-Content "$tmp\fase2-alvos-antes.json" | ConvertFrom-Json
$depois2 = @(
  "01-papel-e-restricoes-do-agente.md",
  "02-como-localizar-e-ler-o-archetype.md",
  "03-regras-obrigatorias-de-geracao.md",
  "04-fluxo-de-execucao-e-verificacao.md",
  "05-entregaveis-e-relatorios.md"
) | ForEach-Object { (Get-FileHash (Join-Path $root "02-operacao-procedimento-do-agente/$_") -Algorithm SHA256).Hash }
Compare-Object @($antes2 | ForEach-Object Hash) $depois2
```

Esperado: nenhuma saída.

### 5. Validar as subpastas de controle

Repetir a comparação do passo 6 da Fase 1 usando `$tmp\fase2-controle-antes.json` e as subpastas `01-entrada-inicializacao`, `03-...` e `04-...`.

## Conferência do total

```powershell
(Get-ChildItem $root -Recurse -File).Count
```

Esperado: `19`.

## Conferência opcional em target/classes

```powershell
mvn -B clean process-resources
Get-ChildItem "target/classes/llm-maven-flags-inference/01-entrada-inicializacao" -File |
  Select-Object -ExpandProperty Name
Get-ChildItem "target/classes/llm-maven-flags-inference/02-operacao-procedimento-do-agente" -File |
  Select-Object -ExpandProperty Name
```

Esperado: os mesmos nomes novos das subpastas 01 e 02.

## Falhas conhecidas (não são regressão)

`mvn -B test` não é gate desta feature. Se executado, espera-se falha em:

- `PathRenameContractTest`
- `BuildArtifactsTest`
- `HardStopContractTest`
- `DestinationContractTest`
- `ReadmeConsistencyTest`
- `CatalogSyncContractTest`

Motivo: essas suítes resolvem os nomes antigos e a FR-013 proíbe atualizá-las nesta feature. O JAR instalado não é validado aqui.

## Critérios de aceite

- [ ] Fase 1: 2 arquivos com os nomes novos; 0 nomes antigos; hashes idênticos.
- [ ] Fase 2: 5 arquivos sem `prompt-`; numerais 01 a 05; hashes idênticos.
- [ ] Subpastas 03 e 04 inalteradas em ambas as fases.
- [ ] Total de 19 arquivos no procedimento.
- [ ] Nenhuma edição em README, índice, fixtures, suítes ou redirect.
- [ ] Nenhum alias, cópia ou redirect com nome antigo.
