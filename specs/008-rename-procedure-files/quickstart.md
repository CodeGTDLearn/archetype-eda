# Quickstart: Validar as Três Fases

**Feature**: `008-rename-procedure-files` | **Date**: 2026-11-25

## Pré-requisitos

- PowerShell 5.1 ou superior, na raiz do repositório.
- Nenhuma dependência nova.
- Não abrir os arquivos em editor durante a renomeação.
- Não usar `git mv`, commit, push ou branch.
- Não editar regras, textos normativos ou formatação na Fase 3.

Variáveis de trabalho:

```powershell
$root = "src/main/resources/llm-maven-flags-inference"
$tmp = "$env:TEMP\eda-008"
New-Item -ItemType Directory -Force $tmp | Out-Null
```

## Fase 1 — Entrada e mapa da subpasta 01

### 1. Manifesto antes da fase

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

### 2. Pré-condições

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

### 4. Validar

```powershell
Get-ChildItem (Join-Path $root "01-entrada-inicializacao") -File |
  Select-Object -ExpandProperty Name
```

Esperado: `01-entryPoint.md` e `02-mapa-dos-arquivos-e-ordem-de-leitura.md`.

```powershell
$antes = Get-Content "$tmp\fase1-alvos-antes.json" | ConvertFrom-Json
$depois = @(
  (Get-FileHash (Join-Path $root "01-entrada-inicializacao/01-entryPoint.md") -Algorithm SHA256).Hash,
  (Get-FileHash (Join-Path $root "01-entrada-inicializacao/02-mapa-dos-arquivos-e-ordem-de-leitura.md") -Algorithm SHA256).Hash
)
Compare-Object @($antes | ForEach-Object Hash) $depois
```

Esperado: nenhuma saída.

- Conferir o snapshot das subpastas 02, 03 e 04 contra o estado atual.
- `mvn -B test` fica vermelho; registrar as suítes esperadas e não corrigir nesta fase.

## Fase 2 — Operação do agente sem o prefixo prompt-

### 1. Manifesto antes da fase

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

### 3. Validar

```powershell
Get-ChildItem (Join-Path $root "02-operacao-procedimento-do-agente") -File |
  Select-Object -ExpandProperty Name
```

Esperado: os cinco nomes sem `prompt-`, numerados de 01 a 05.

- Comparar os hashes com `fase2-alvos-antes.json`.
- Conferir o snapshot das subpastas 01, 03 e 04.
- `mvn -B test` continua vermelho; registrar as suítes.

## Fase 3 — Atualização das referências operacionais

### 1. Confirmar as Fases 1 e 2

```powershell
Get-ChildItem $root -Recurse -File | Select-Object -ExpandProperty FullName
```

Esperado: 19 arquivos, com os sete novos nomes.

### 2. Inventariar as ocorrências

```powershell
$surfaces = @(
  "README.md",
  "src/main/resources/llm-maven-flags-inference",
  "src/main/resources/utils",
  "src/test/archetype-post-generate"
)
$files = foreach ($s in $surfaces) {
  if (Test-Path $s -PathType Leaf) { Get-Item $s }
  else { Get-ChildItem $s -Recurse -File }
}
$files | Select-String -Pattern "prompt-0[0-5]|indice-01-mapa" |
  Group-Object Path |
  ForEach-Object { "{0}|{1}" -f $_.Name, $_.Count }
```

Esperado: 115 ocorrências em 15 arquivos, conforme o contrato da Fase 3.

### 3. Atualizar as referências

- Substituir primeiro os sete nomes completos com `.md`.
- Substituir depois os radicais `prompt-00` a `prompt-05` e `indice-01`.
- Atualizar README, índice, entrada, os três arquivos da subpasta 02, os dois da subpasta 03, o redirect em `utils/` e as seis suítes.
- Não alterar regras, textos normativos, formatação ou numeração interna.
- Não renomear arquivos das subpastas 03 e 04.

### 4. Validar zero tokens antigos

```powershell
$files | Select-String -Pattern "prompt-0[0-5]|indice-01-mapa"
```

Esperado: nenhuma saída.

### 5. Rodar o gate

```powershell
mvn -B test
```

Esperado: todas as suítes passam.

Paridade opcional com o CI:

```powershell
mvn -B clean install
```

### 6. Conferir exclusões

```powershell
git status --porcelain
```

Esperado: somente README, procedimento, redirect e seis suítes. Nenhuma alteração em `specs/003` a `specs/007`, `target/` ou `graphify-out/`.

## Falhas esperadas entre fases

A Fase 1 deixa vermelhas cinco suítes; a Fase 2 acrescenta `CatalogSyncContractTest`. Lista completa:

- `PathRenameContractTest`
- `BuildArtifactsTest`
- `HardStopContractTest`
- `DestinationContractTest`
- `ReadmeConsistencyTest`
- `CatalogSyncContractTest`

A Fase 3 é a única que restaura o verde.

## Critérios de aceite

- [ ] Fase 1: 2 arquivos com os nomes novos; 0 nomes antigos; hashes idênticos.
- [ ] Fase 2: 5 arquivos sem `prompt-`; numerais 01 a 05; hashes idênticos.
- [ ] Fase 3: 0 ocorrências de `prompt-0[0-5]` e `indice-01-mapa` em 15 arquivos operacionais.
- [ ] Fase 3: `mvn -B test` verde.
- [ ] Subpastas 03 e 04 sem arquivos renomeados.
- [ ] Specs históricas 003–007, `target/` e `graphify-out/` inalterados.
- [ ] Nenhum alias, cópia ou redirect com nome antigo.
