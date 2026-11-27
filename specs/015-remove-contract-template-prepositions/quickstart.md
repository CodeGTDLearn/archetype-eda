# Quickstart — Remover as Preposições "do" e "com" dos Nomes dos Templates de Contrato

**Feature**: 015-remove-contract-template-prepositions | **Date**: 2026-11-26 | **Spec**: [spec.md](./spec.md)

Roteiro de validação end-to-end. Os comandos assumem a raiz do repositório e PowerShell no Windows. Não execute comandos git: os hooks do Spec Kit cuidam dos commits.

## 1. Pré-requisitos

- JDK 17+ e Maven 3.9.16 no `PATH`.
- Dependências já resolvidas no cache local do Maven.
- Working tree com a feature aplicada, mas ainda sem commit manual.
- `target/classes` pode conter cópias antigas; o roteiro usa `clean` de propósito.

## 2. Linha de base (antes da implementação)

```powershell
$folder = "src/main/resources/llm-maven-flags-inference/04-templates-de-contrato"
$before = Get-ChildItem -Path $folder -File |
    Sort-Object Name |
    Get-FileHash -Algorithm SHA256

$before | Select-Object Path, Hash
$before.Count                            # esperado: 6
Get-ChildItem -Path "src/main/resources/llm-maven-flags-inference" -Recurse -File |
    Group-Object Extension | Select-Object Name, Count
```

**Esperado**
- 6 arquivos na subpasta 04.
- 19 arquivos no procedimento: 13 `.md` e 6 `.json`.
- 4 subpastas de primeiro nível.

Guarde os hashes em um arquivo temporário fora do repositório, por exemplo:

```powershell
$before | ConvertTo-Json | Set-Content "$env:TEMP\spec015-before.json" -Encoding UTF8
```

## 3. Fase 1 — schema sem `do`

```powershell
$folder = "src/main/resources/llm-maven-flags-inference/04-templates-de-contrato"
$old = Join-Path $folder "01-schema-do-contrato.json"
$new = Join-Path $folder "01-schema-contrato.json"

if (Test-Path $new) { throw "Colisão: $new já existe" }
Rename-Item -Path $old -NewName (Split-Path $new -Leaf)
```

**Validar a fase**

```powershell
Get-ChildItem -Path $folder -File | Select-Object -ExpandProperty Name
# deve conter 01-schema-contrato.json e não conter 01-schema-do-contrato.json

$hash = (Get-FileHash -Path $new -Algorithm SHA256).Hash
$expected = (Get-Content "$env:TEMP\spec015-before.json" | ConvertFrom-Json |
    Where-Object { $_.Path -like "*01-schema-do-contrato.json" }).Hash
$hash -eq $expected        # esperado: True
```

Atualize as referências ao arquivo 01 no índice, na porta de entrada, no README e nas suítes conforme o [manifesto de renomeação](./contracts/rename-manifest.md). Depois:

```powershell
mvn -B clean test
```

**Esperado**: build verde; as suítes phase-aware aceitam o estado 1 (schema novo, arquivos 3 a 6 antigos).

## 4. Fase 2 — arquivos 3 a 6 sem `com`

```powershell
$folder = "src/main/resources/llm-maven-flags-inference/04-templates-de-contrato"
$map = [ordered]@{
    "03-contrato-com-objetos-aninhados.json"     = "03-contrato-objetos-aninhados.json"
    "04-contrato-com-enums.json"                 = "04-contrato-enums.json"
    "05-contrato-com-tipos-do-projeto.json"      = "05-contrato-tipos-do-projeto.json"
    "06-contrato-com-attributes-explicito.json"  = "06-contrato-attributes-explicito.json"
}

foreach ($old in $map.Keys) {
    $source = Join-Path $folder $old
    $target = Join-Path $folder $map[$old]
    if (-not (Test-Path $source)) { throw "Origem ausente: $source" }
    if (Test-Path $target) { throw "Colisão: $target já existe" }
    Rename-Item -Path $source -NewName $map[$old]
}
```

**Validar a fase**

```powershell
Get-ChildItem -Path $folder -File | Sort-Object Name | Select-Object -ExpandProperty Name
```

**Esperado**

```text
01-schema-contrato.json
02-contrato-plano.json
03-contrato-objetos-aninhados.json
04-contrato-enums.json
05-contrato-tipos-do-projeto.json
06-contrato-attributes-explicito.json
```

Atualize as referências aos arquivos 3 a 6 conforme o [manifesto de renomeação](./contracts/rename-manifest.md).

## 5. Verificação de conteúdo

```powershell
$folder = "src/main/resources/llm-maven-flags-inference/04-templates-de-contrato"
$before = Get-Content "$env:TEMP\spec015-before.json" | ConvertFrom-Json
$after = Get-ChildItem -Path $folder -File | Get-FileHash -Algorithm SHA256

$beforeMap = @{}
$before | ForEach-Object { $beforeMap[(Split-Path $_.Path -Leaf)] = $_.Hash }

$mismatches = @()
foreach ($h in $after) {
    $name = Split-Path $h.Path -Leaf
    $oldName = switch ($name) {
        "01-schema-contrato.json" { "01-schema-do-contrato.json" }
        "03-contrato-objetos-aninhados.json" { "03-contrato-com-objetos-aninhados.json" }
        "04-contrato-enums.json" { "04-contrato-com-enums.json" }
        "05-contrato-tipos-do-projeto.json" { "05-contrato-com-tipos-do-projeto.json" }
        "06-contrato-attributes-explicito.json" { "06-contrato-com-attributes-explicito.json" }
        default { $name }
    }
    if ($beforeMap[$oldName] -ne $h.Hash) { $mismatches += $name }
}
$mismatches      # esperado: vazio
```

## 6. Verificação de referências

```powershell
$oldNames = @(
    "01-schema-do-contrato.json",
    "03-contrato-com-objetos-aninhados.json",
    "04-contrato-com-enums.json",
    "05-contrato-com-tipos-do-projeto.json",
    "06-contrato-com-attributes-explicito.json"
)

$targets = @(
    "src/main/resources/llm-maven-flags-inference/01-inicializacao",
    "README.md",
    "src/test/archetype-post-generate"
)

$hits = Get-ChildItem -Path $targets -Recurse -File -Include *.md,*.groovy |
    Select-String -SimpleMatch -Pattern $oldNames

$hits
```

**Esperado**
- 0 ocorrências como caminho operacional.
- Ocorrências permitidas apenas em asserções negativas das suítes (ex.: `every { !new File(folder, it).exists() }`) e nas specs históricas fora dos `$targets`.

## 7. Contagens e invariantes

```powershell
$root = "src/main/resources/llm-maven-flags-inference"
$files = Get-ChildItem -Path $root -Recurse -File
$files.Count                                              # 19
($files | Where-Object Extension -eq ".md").Count          # 13
($files | Where-Object Extension -eq ".json").Count        # 6
(Get-ChildItem -Path $root -Directory).Count               # 4
(Get-ChildItem -Path "$root/04-templates-de-contrato" -File).Count  # 6
```

## 8. Build e suítes

```powershell
mvn -B clean test
mvn clean install
mvn -B clean integration-test
```

**Esperado**
- Todas as suítes verdes, incluindo `ContractTemplateRenameContractTest`, `FileRenameContractTest`, `BuildArtifactsTest`, `PathRenameContractTest`, `ContractRuleRenameContractTest` e `ProcedureContractTest`.
- `target/classes/llm-maven-flags-inference/04-templates-de-contrato/` com os seis nomes novos e sem os cinco nomes antigos.
- `mvn -B clean test` é o gate; `mvn clean install` confirma o artefato.

## 9. Verificação opcional do JAR instalado

```powershell
mvn -Deda.verify.installed.jar=true verify
```

**Esperado**: o JAR instalado expõe os seis nomes novos. Esta verificação não é gate de aceite.

## 10. Critérios de aceite

- [ ] 5 de 5 arquivos renomeados; 0 nomes antigos como nome de arquivo.
- [ ] `02-contrato-plano.json` inalterado.
- [ ] SHA-256 dos 6 JSON idêntico ao inicial.
- [ ] Índice com 19 linhas, posições 14 a 19 e papéis preservados.
- [ ] README com a tabela de templates nos nomes novos.
- [ ] 0 referências operacionais aos nomes antigos.
- [ ] 19/13/6/4 preservados; 6 arquivos na subpasta 04.
- [ ] `mvn -B clean test` e `mvn clean install` verdes.
