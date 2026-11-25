# Quickstart: 008-rename-procedure-files

**Feature**: 008-rename-procedure-files | **Date**: 2026-11-25

## Purpose

Roteiro executável para validar a renomeação dos 12 arquivos, a atualização das referências operacionais e a preservação do conteúdo normativo. O roteiro assume que a implementação foi aplicada conforme `tasks.md`; ele não substitui as tarefas de implementação.

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

## Scenario 1 — Nomes e contagens

**Objetivo**: provar que os 12 arquivos têm os nomes novos e que nenhum nome antigo permanece.

```powershell
$procedure = "src/main/resources/llm-maven-flags-inference"
$r03 = Join-Path $procedure "03-derivacao-regras-de-formacao-do-contrato"
$r04 = Join-Path $procedure "04-referencia-templates-de-contrato"

Get-ChildItem $r03 -File | Select-Object -ExpandProperty Name
Get-ChildItem $r04 -File | Select-Object -ExpandProperty Name

(Get-ChildItem $procedure -Recurse -File).Count
(Get-ChildItem $procedure -Recurse -File -Filter *.md).Count
(Get-ChildItem $procedure -Recurse -File -Filter *.json).Count
```

**Expected**:
- Subpasta 03: exatamente `01-como-derivar-a-entidade-do-contrato.md`, `02-como-resolver-tipos-e-enums.md`, `03-como-derivar-flags-de-tipos-aninhados.md`, `04-como-aplicar-precedencia-e-validacao.md`, `05-como-materializar-e-emitir-o-contrato.md`, `06-como-relatar-proveniencia.md`.
- Subpasta 04: exatamente `01-schema-do-contrato.json`, `02-exemplo-contrato-plano.json`, `03-exemplo-contrato-com-objetos-aninhados.json`, `04-exemplo-contrato-com-enums.json`, `05-exemplo-contrato-com-tipos-do-projeto.json`, `06-exemplo-contrato-com-attributes-explicito.json`.
- 19 arquivos no total; 13 Markdown; 6 JSON.
- 0 ocorrências de `regras-` ou `template-` nos nomes.

## Scenario 2 — Referências operacionais

**Objetivo**: provar que nenhum arquivo operacional aponta para os nomes antigos.

```powershell
Get-ChildItem README.md, $procedure, "src/test" -Recurse -File |
  Where-Object { $_.FullName -notmatch '\\specs\\|\\graphify-out\\|\\target\\|FileRenameContractTest' } |
  Select-String -Pattern 'regras-0[1-6]|template-0[1-6]'
```

**Expected**: nenhuma linha. Se houver, o caminho e a linha indicam a referência a corrigir.

**Nota**: `specs/**`, `graphify-out/**`, `target/**` e a própria `FileRenameContractTest.groovy` são excluídos por serem históricos, saída gerada ou conterem padrões negativos.

## Scenario 3 — Índice e prompt de entrada

**Objetivo**: provar que o índice e o ponto de entrada listam os 12 caminhos novos.

```powershell
$index = Join-Path $procedure "01-entrada-inicializacao/indice-01-mapa-dos-arquivos-e-ordem-de-leitura.md"
$prompt = Join-Path $procedure "01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md"

Select-String -Path $index -Pattern '03-derivacao-regras-de-formacao-do-contrato/0[1-6]-|04-referencia-templates-de-contrato/0[1-6]-'
Select-String -Path $prompt -Pattern '03-derivacao-regras-de-formacao-do-contrato/0[1-6]-|04-referencia-templates-de-contrato/0[1-6]-'
```

**Expected**: 6 caminhos novos na subpasta 03 e 6 na subpasta 04, em cada arquivo; 0 caminhos antigos.

## Scenario 4 — Conteúdo normativo

**Objetivo**: provar que a renomeação não alterou regras, definições, exemplos ou metadados.

**Método**:
1. Comparar o hash dos 6 arquivos renomeados que não continham referências com o baseline: `regras-01`, `regras-03`, `regras-05`, `template-03`, `template-04`, `template-05`. Os hashes devem ser idênticos.
2. Para os 6 arquivos que continham referências — `regras-02`, `regras-04`, `regras-06`, `template-01`, `template-02`, `template-06` — revisar o diff. As únicas diferenças permitidas são os tokens de referência mapeados em `contracts/rename-manifest.md`.
3. Para os 5 arquivos do procedimento que não foram renomeados, mas continham referências — `prompt-00`, `indice-01`, `prompt-01`, `prompt-02`, `prompt-04` — revisar o diff. As únicas diferenças permitidas são os tokens de referência.
4. Para o `README.md`, revisar o diff. As únicas diferenças permitidas são os nomes nas tabelas e na prosa; links de diretório não mudam.

**Expected**: nenhuma mudança de regra, definição, exemplo de domínio, chave JSON, `defaultValue`, `validationRegex` ou metadado estrutural.

## Scenario 5 — Build e suítes

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

## Scenario 6 — JAR instalado (opcional)

**Objetivo**: provar que o artefato instalado contém os nomes novos e nenhum nome antigo.

```powershell
mvn -B test "-Deda.verify.installed.jar=true"
```

**Expected**: a verificação opt-in passa; o JAR contém os 12 nomes novos e não contém os 12 nomes antigos.

## Scenario 7 — Verificação negativa

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
