# Quickstart: 009-rename-procedure-files

**Feature**: 009-rename-procedure-files | **Date**: 2026-11-25

## Purpose

Roteiro executável para validar as duas fases da renomeação, a atualização das referências operacionais e a preservação do conteúdo normativo. O roteiro assume que a implementação foi aplicada conforme `tasks.md`; ele não substitui as tarefas de implementação.

## Prerequisites

- Branch de trabalho ativa.
- PowerShell 5.1 ou superior.
- Maven 3.9.x e JDK compatível com o build (17 ou 21).
- Acesso ao repositório e ao diretório local do Maven.
- Baseline dos 12 arquivos copiado para fora do repositório antes da implementação.

```powershell
$procedure = "src/main/resources/llm-maven-flags-inference"
$r03 = Join-Path $procedure "03-derivacao-regras-de-formacao-do-contrato"
$r04 = Join-Path $procedure "04-referencia-templates-de-contrato"
```

## Scenario 1 — Fase 1: nomes e contagens de 03

**Objetivo**: provar que os 6 arquivos de 03 têm os nomes novos e que 04 e README permanecem intactos.

```powershell
Get-ChildItem $r03 -File | Select-Object -ExpandProperty Name
(Get-ChildItem $r03 -File).Count
Get-ChildItem $r04 -File | Select-Object -ExpandProperty Name
```

**Expected**:
- Subpasta 03: exatamente `01-como-derivar-a-entidade-do-contrato.md`, `02-como-resolver-tipos-e-enums.md`, `03-como-derivar-flags-de-tipos-aninhados.md`, `04-como-aplicar-precedencia-e-validacao.md`, `05-como-materializar-e-emitir-o-contrato.md`, `06-como-relatar-proveniencia.md`.
- 6 arquivos em 03; 0 `regras-*`.
- Subpasta 04: os 6 nomes antigos permanecem; README ainda pode conter referências de 03.

## Scenario 2 — Fase 1: referências de 03

**Objetivo**: provar que as referências aos arquivos de 03 foram atualizadas, sem tocar em 04 ou README.

```powershell
Get-ChildItem $procedure, "src/test" -Recurse -File |
  Where-Object { $_.FullName -notmatch '\\specs\\|\\graphify-out\\|\\target\\|FileRenameContractTest' } |
  Select-String -Pattern 'regras-0[1-6]'
```

**Expected**: nenhuma ocorrência de `regras-0[1-6]` no procedimento e nas suítes; o README ainda pode conter referências de 03 até a Fase 2.

## Scenario 3 — Fase 2: nomes e contagens de 04

**Objetivo**: provar que os 6 arquivos de 04 têm os nomes novos e que a contagem total permanece 19/13/6/4.

```powershell
Get-ChildItem $r04 -File | Select-Object -ExpandProperty Name
(Get-ChildItem $procedure -Recurse -File).Count
(Get-ChildItem $procedure -Recurse -File -Filter *.md).Count
(Get-ChildItem $procedure -Recurse -File -Filter *.json).Count
```

**Expected**:
- Subpasta 04: exatamente `01-schema-do-contrato.json`, `02-exemplo-contrato-plano.json`, `03-exemplo-contrato-com-objetos-aninhados.json`, `04-exemplo-contrato-com-enums.json`, `05-exemplo-contrato-com-tipos-do-projeto.json`, `06-exemplo-contrato-com-attributes-explicito.json`.
- 19 arquivos no total; 13 Markdown; 6 JSON; 4 subpastas; 0 `template-*`.

## Scenario 4 — Fase 2: varredura final de referências

**Objetivo**: provar que nenhum arquivo operacional aponta para os nomes antigos, incluindo o README.

```powershell
Get-ChildItem README.md, $procedure, "src/test" -Recurse -File |
  Where-Object { $_.FullName -notmatch '\\specs\\|\\graphify-out\\|\\target\\|FileRenameContractTest' } |
  Select-String -Pattern 'regras-0[1-6]|template-0[1-6]'
```

**Expected**: nenhuma linha.

**Nota**: `specs/**`, `graphify-out/**`, `target/**` e a própria `FileRenameContractTest.groovy` são excluídos por serem históricos, saída gerada ou conterem padrões negativos.

## Scenario 5 — Conteúdo normativo

**Objetivo**: provar que a renomeação não alterou regras, definições, exemplos ou metadados.

**Método**:
1. Comparar o hash dos 6 arquivos renomeados que não continham referências com o baseline: `regras-01`, `regras-03`, `regras-05`, `template-03`, `template-04`, `template-05`. Os hashes devem ser idênticos.
2. Para os 6 arquivos que continham referências — `regras-02`, `regras-04`, `regras-06`, `template-01`, `template-02`, `template-06` — revisar o diff. As únicas diferenças permitidas são os tokens de referência mapeados em `contracts/rename-manifest.md`.
3. Para os 5 arquivos do procedimento que não foram renomeados, mas continham referências — `prompt-00`, `indice-01`, `prompt-01`, `prompt-02`, `prompt-04` — revisar o diff. As únicas diferenças permitidas são os tokens de referência.
4. Para o `README.md`, revisar o diff. As únicas diferenças permitidas são os nomes nas tabelas e na prosa; links de diretório não mudam.

**Expected**: nenhuma mudança de regra, definição, exemplo de domínio, chave JSON, `defaultValue`, `validationRegex` ou metadado estrutural.

## Scenario 6 — Build e suítes

**Objetivo**: provar que o build continua verde e que a nova suíte está registrada.

```powershell
mvn -B test
mvn clean install
mvn -B clean integration-test
```

**Expected**:
- Todas as suítes passam, incluindo `FileRenameContractTest.groovy`.
- O catálogo continua com 35 propriedades e 18 filesets.
- O JAR é reconstruído com os 19 arquivos e os nomes novos.
- A integração não regride.

## Scenario 7 — JAR instalado (opcional)

**Objetivo**: provar que o artefato instalado contém os nomes novos e nenhum nome antigo.

```powershell
mvn -B test "-Deda.verify.installed.jar=true"
```

**Expected**: a verificação opt-in passa; o JAR contém os 12 nomes novos e não contém os 12 nomes antigos.

## Scenario 8 — Verificação negativa

**Objetivo**: provar que a suíte detecta regressões.

| Alteração temporária | Resultado esperado |
|---|---|
| Reintroduzir um arquivo com nome antigo | `FileRenameContractTest` falha e nomeia o arquivo |
| Reintroduzir uma referência antiga no procedimento | `FileRenameContractTest` falha e nomeia o arquivo e a linha |
| Criar ou remover um arquivo do procedimento | `FileRenameContractTest` falha na contagem 19/13/6 |
| Renomear uma subpasta | `FileRenameContractTest` e `PathRenameContractTest` falham |
| Alterar uma regra ou chave JSON | `ProcedureContractTest`, `CatalogSyncContractTest` ou a revisão de diff falham |
| Não registrar a nova suíte no runner | `CoverageRegistryTest` e `ReadmeConsistencyTest` falham |

Reverter qualquer alteração temporária depois da verificação.

## Referências

- [spec.md](./spec.md)
- [plan.md](./plan.md)
- [data-model.md](./data-model.md)
- [contracts/rename-manifest.md](./contracts/rename-manifest.md)
- [research.md](./research.md)
