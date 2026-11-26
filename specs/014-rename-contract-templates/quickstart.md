# Quickstart — Validação: 014-rename-contract-templates

**Feature**: 014-rename-contract-templates | **Date**: 2026-11-26 | **Spec**: [spec.md](./spec.md)

Roteiro executável para provar cada fase da renomeação. As regras de nome estão em [contracts/rename-manifest.md](./contracts/rename-manifest.md); as entidades e invariantes estão em [data-model.md](./data-model.md). Este documento não repete o mapa `old → new` além do necessário para os comandos.

## Pré-requisitos

- Windows com PowerShell (mesmo ambiente do CI) e Git no `PATH`.
- JDK 21 (CI) ou Java 17+ local; Maven 3.9.16.
- Raiz do repositório: `C:\DEV\SCRIPTS\archetype-v2-eda`.
- Nenhum comando destrutivo: os cenários abaixo são de verificação e de build.

## Linha de base (antes da Fase 1)

```powershell
$proc = "src/main/resources/llm-maven-flags-inference"
Get-ChildItem $proc -Directory | Select-Object -ExpandProperty Name
(Get-ChildItem $proc -Recurse -File).Count
(Get-ChildItem $proc -Recurse -File | Where-Object Extension -eq ".json").Count
(Get-ChildItem $proc -Recurse -File | Where-Object Extension -eq ".md").Count
Get-ChildItem "$proc/04-referencia-templates-de-contrato" -File | Select-Object Name
Get-ChildItem "$proc/04-referencia-templates-de-contrato" -File |
    Get-FileHash -Algorithm SHA256 | Select-Object Path, Hash |
    Out-File "C:\Users\edufi\AppData\Local\Temp\opencode\012-hashes-antes.txt"
```

Esperado: 4 subpastas; 19 arquivos; 6 JSON; 13 Markdown; 6 arquivos na subpasta 04, com os nomes `01-schema-do-contrato.json` a `06-exemplo-contrato-com-attributes-explicito.json`; arquivo de hashes gravado fora do repositório.

## Fase 1 — Subpasta `04-templates-de-contrato`

Validação depois da renomeação da pasta e da atualização das referências de pasta:

```powershell
$proc = "src/main/resources/llm-maven-flags-inference"
Test-Path "$proc/04-templates-de-contrato"
Test-Path "$proc/04-referencia-templates-de-contrato"
Get-ChildItem "$proc/04-templates-de-contrato" -File | Select-Object -ExpandProperty Name
(Get-ChildItem $proc -Recurse -File).Count
Select-String -Path "README.md","$proc/01-inicializacao/*.md","src/test/archetype-post-generate/*.groovy" `
    -Pattern "04-referencia-templates-de-contrato" -SimpleMatch |
    Select-Object Path, LineNumber, Line
mvn -B test
```

Esperado:

- `True` para `04-templates-de-contrato` e `False` para o nome antigo.
- Seis arquivos com os nomes atuais (ainda com `exemplo`).
- Nenhuma linha retornada pela busca da pasta antiga no corpus operacional.
- `mvn -B test` verde, incluindo `ContractTemplateRenameContractTest` no estado da Fase 1.

## Fase 2 — Arquivos sem "exemplo"

Validação depois da renomeação dos cinco arquivos e da atualização das referências de arquivo:

```powershell
$proc = "src/main/resources/llm-maven-flags-inference/04-templates-de-contrato"
Get-ChildItem $proc -File | Select-Object -ExpandProperty Name
Get-ChildItem $proc -File | Where-Object Name -match "exemplo" | Select-Object -ExpandProperty Name
Get-ChildItem $proc -File | Get-FileHash -Algorithm SHA256 | Select-Object Path, Hash |
    Out-File "C:\Users\edufi\AppData\Local\Temp\opencode\012-hashes-depois.txt"
Select-String -Path "README.md",`
    "src/main/resources/llm-maven-flags-inference/01-inicializacao/*.md",`
    "src/test/archetype-post-generate/*.groovy" `
    -Pattern "exemplo-contrato" -SimpleMatch |
    Select-Object Path, LineNumber, Line
mvn -B test
```

Esperado:

- Exatamente `01-schema-do-contrato.json`, `02-contrato-plano.json`, `03-contrato-com-objetos-aninhados.json`, `04-contrato-com-enums.json`, `05-contrato-com-tipos-do-projeto.json` e `06-contrato-com-attributes-explicito.json`.
- Nenhum nome de arquivo com `exemplo`.
- Nenhuma ocorrência de `exemplo-contrato` no corpus operacional (as asserções negativas das suítes são a exceção).
- `mvn -B test` verde, incluindo `ContractTemplateRenameContractTest` no estado da Fase 2.

Comparação de conteúdo:

```powershell
Compare-Object `
    (Get-Content "C:\Users\edufi\AppData\Local\Temp\opencode\012-hashes-antes.txt") `
    (Get-Content "C:\Users\edufi\AppData\Local\Temp\opencode\012-hashes-depois.txt")
```

Esperado: diferenças apenas no caminho dos arquivos renomeados; nenhuma diferença de `Hash`. Se houver diferença de hash, a feature falha (INV-06).

## Índice e README

```powershell
Select-String -Path "src/main/resources/llm-maven-flags-inference/01-inicializacao/01-mapa-dos-arquivos-e-ordem-de-leitura.md" `
    -Pattern "^\| \d+ \|" | Measure-Object | Select-Object -ExpandProperty Count
Select-String -Path "README.md" -Pattern "llm-maven-flags-inference-04-templates-de-contrato" -SimpleMatch |
    Select-Object LineNumber, Line
```

Esperado: 19 linhas de tabela no mapa, com as posições 14 a 19 apontando para a pasta nova e os nomes novos; âncora, `href` e título do README com o nome novo.

## Build e artefato

```powershell
mvn clean install
mvn -B clean integration-test
mvn -B clean install "-Deda.verify.installed.jar=true"
```

Esperado:

- `mvn clean install` reconstrói `target/classes/llm-maven-flags-inference/` com 19 arquivos e os nomes novos.
- `mvn -B clean integration-test` verde nas fixtures.
- A verificação do JAR instalado é opt-in: prova que o artefato expõe os nomes novos; não é gate de aceite (FR-026).

## Critérios de aceite resumidos

| Critério | Evidência |
|---|---|
| Pasta renomeada (1/1) | `Test-Path` da pasta nova e da antiga |
| Arquivos renomeados (5/5) | listagem da subpasta 04 |
| Conteúdo preservado | hashes iguais |
| Ordem e papéis preservados | 19 linhas do mapa, posições 14 a 19 |
| README consistente | âncora, título, `href` e tabela |
| Suítes verdes | `mvn -B test` |
| Contagem 19/13/6/4 | contagens da linha de base |
| Redirect legado intacto | `Test-Path` e leitura do arquivo em `utils/01-entrada-inicializacao/` |
