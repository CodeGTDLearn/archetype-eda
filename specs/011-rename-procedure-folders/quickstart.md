# Quickstart: 011-rename-procedure-folders

**Feature**: 011-rename-procedure-folders | **Date**: 2026-11-26

Guia de validação por fase. Os detalhes de mapa e inventário estão em [contracts/rename-manifest.md](./contracts/rename-manifest.md) e [data-model.md](./data-model.md); a fonte dos requisitos é [spec.md](./spec.md).

## Pré-requisitos

- Raiz do repositório: `C:\DEV\SCRIPTS\archetype-v2-eda-feature-1`.
- PowerShell, Maven 3.9.16 e JDK 21 disponíveis.
- Working tree limpo antes de começar.
- Nenhuma pasta ou arquivo de destino existente.

## Linha de base

```powershell
$procedure = "src/main/resources/llm-maven-flags-inference"

Get-ChildItem $procedure -Directory | Select-Object -ExpandProperty Name
$files = Get-ChildItem $procedure -Recurse -File
"total=$($files.Count) md=$(($files | Where-Object Extension -eq '.md').Count) json=$(($files | Where-Object Extension -eq '.json').Count)"
"01=$((Get-ChildItem (Join-Path $procedure '01-entrada-inicializacao') -File).Count) 02=$((Get-ChildItem (Join-Path $procedure '02-operacao-procedimento-do-agente') -File).Count)"
```

**Esperado**: 4 subpastas; 19 arquivos; 13 Markdown; 6 JSON; 2 arquivos em 01; 5 em 02.

## Fase 1 — Subpasta de inicialização

### Estrutura

```powershell
Test-Path (Join-Path $procedure "01-inicializacao")                         # True
Test-Path (Join-Path $procedure "01-entrada-inicializacao")                 # False
(Get-ChildItem (Join-Path $procedure "01-inicializacao") -File).Count       # 2
(Get-ChildItem (Join-Path $procedure "01-inicializacao") -File) | Select-Object -ExpandProperty Name
```

**Esperado**: `00-entry-point.md` e `01-mapa-dos-arquivos-e-ordem-de-leitura.md`; subpasta antiga ausente.

### Referências

```powershell
Select-String -Path "README.md" -Pattern '01-entrada-inicializacao'          # 0 ocorrências
Select-String -Path (Join-Path $procedure "01-inicializacao/00-entry-point.md") -Pattern '01-entrada-inicializacao'  # 0
Select-String -Path (Join-Path $procedure "01-inicializacao/01-mapa-dos-arquivos-e-ordem-de-leitura.md") -Pattern '01-entrada-inicializacao'  # 0
Get-Content "src/main/resources/utils/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md"
```

**Esperado**: nenhuma referência operacional à subpasta 01 antiga; o redirect legado aponta para `llm-maven-flags-inference/01-inicializacao/00-entry-point.md`.

### Teste

```powershell
mvn -B test
```

**Esperado**: build verde. A subpasta 02 ainda mantém o nome e os cinco arquivos antigos.

## Fase 2 — Subpasta de procedimento do agente

### Estrutura

```powershell
Test-Path (Join-Path $procedure "02-procedimento-do-agente")                # True
Test-Path (Join-Path $procedure "02-operacao-procedimento-do-agente")       # False
(Get-ChildItem (Join-Path $procedure "02-procedimento-do-agente") -File).Count  # 5
(Get-ChildItem (Join-Path $procedure "02-procedimento-do-agente") -File) | Select-Object -ExpandProperty Name
```

**Esperado**: os cinco arquivos preservados; o arquivo `02-como-localizar-e-ler-o-archetype.md` ainda tem o nome antigo nesta fase.

### Referências

```powershell
Select-String -Path "README.md" -Pattern '02-operacao-procedimento-do-agente'   # 0
Select-String -Path (Join-Path $procedure "01-inicializacao/00-entry-point.md") -Pattern '02-operacao-procedimento-do-agente'  # 0
Select-String -Path (Join-Path $procedure "01-inicializacao/01-mapa-dos-arquivos-e-ordem-de-leitura.md") -Pattern '02-operacao-procedimento-do-agente'  # 0
```

**Esperado**: nenhuma referência operacional à subpasta 02 antiga.

### Teste

```powershell
mvn -B test
```

**Esperado**: build verde. O índice lista os cinco arquivos da subpasta 02 na ordem 3 a 7.

## Fase 3 — Arquivo de localização e leitura do archetype

### Estrutura

```powershell
$p02 = Join-Path $procedure "02-procedimento-do-agente"
Test-Path (Join-Path $p02 "02-localizar-e-ler-o-archetype.md")              # True
Test-Path (Join-Path $p02 "02-como-localizar-e-ler-o-archetype.md")         # False
(Get-ChildItem $p02 -File).Count                                            # 5
```

**Esperado**: arquivo novo presente; arquivo antigo ausente; cinco arquivos na subpasta 02.

### Referências

```powershell
Select-String -Path "README.md" -Pattern '02-como-localizar-e-ler-o-archetype'  # 0
Select-String -Path (Join-Path $procedure "01-inicializacao/00-entry-point.md") -Pattern '02-como-localizar-e-ler-o-archetype'  # 0
Select-String -Path (Join-Path $procedure "01-inicializacao/01-mapa-dos-arquivos-e-ordem-de-leitura.md") -Pattern '02-como-localizar-e-ler-o-archetype'  # 0
```

**Esperado**: nenhuma referência operacional ao nome antigo do arquivo. As suítes podem manter o nome antigo apenas em asserções negativas.

### Teste

```powershell
mvn -B test
```

**Esperado**: build verde; o índice mantém o arquivo na posição 4, com o papel de localização e leitura do artefato.

## Validação final

```powershell
$files = Get-ChildItem $procedure -Recurse -File
"total=$($files.Count) md=$(($files | Where-Object Extension -eq '.md').Count) json=$(($files | Where-Object Extension -eq '.json').Count)"
Get-ChildItem $procedure -Directory | Select-Object -ExpandProperty Name
(Get-ChildItem (Join-Path $procedure "01-inicializacao") -File).Count
(Get-ChildItem (Join-Path $procedure "02-procedimento-do-agente") -File).Count
```

**Esperado**: 19 arquivos; 13 Markdown; 6 JSON; quatro subpastas (`01-inicializacao`, `02-procedimento-do-agente`, `03-derivacao-regras-de-formacao-do-contrato`, `04-referencia-templates-de-contrato`); 2 arquivos em 01; 5 em 02.

```powershell
mvn -B clean install
mvn -B clean integration-test
```

**Esperado**: build, suítes e fixtures verdes.

### Verificação opt-in do JAR instalado

```powershell
mvn -B clean install "-Deda.verify.installed.jar=true"
```

**Esperado**: o JAR instalado expõe os nomes novos e nenhum nome antigo, exceto o caminho do redirect legado. Esta verificação não é gate de aceite da feature.

## Estado final esperado

- `01-inicializacao/` com 2 arquivos.
- `02-procedimento-do-agente/` com 5 arquivos, incluindo `02-localizar-e-ler-o-archetype.md`.
- Subpastas 03 e 04 inalteradas.
- Redirect legado em `utils/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md`, apontando para a nova porta de entrada.
- Nenhuma referência operacional aos nomes antigos, exceto o caminho do redirect legado e as asserções negativas das suítes.
