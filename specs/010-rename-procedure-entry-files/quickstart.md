# Quickstart — Renomear os Arquivos de Entrada e Operação do Procedimento

**Feature**: 010-rename-procedure-entry-files | **Date**: 2026-11-25

Roteiro de validação da renomeação dos sete arquivos do procedimento. Execute a partir da raiz do repositório.

## Pré-requisitos

- Windows com PowerShell, ou ambiente POSIX equivalente.
- JDK 21 e Maven 3.9.16 disponíveis.
- Branch `011-rename-procedure-entry-files`.
- `README.md`, `src/main/resources/llm-maven-flags-inference/` e `src/test/` presentes.
- Nenhuma alteração manual em `target/` ou no artefato instalado.

## 1. Baseline

```powershell
$procedure = "src/main/resources/llm-maven-flags-inference"
(Get-ChildItem $procedure -Recurse -File).Count
(Get-ChildItem $procedure -Recurse -File -Filter *.md).Count
(Get-ChildItem $procedure -Recurse -File -Filter *.json).Count
(Get-ChildItem $procedure -Directory).Count
(Get-ChildItem (Join-Path $procedure "01-entrada-inicializacao") -File).Count
(Get-ChildItem (Join-Path $procedure "02-operacao-procedimento-do-agente") -File).Count
```

Esperado: `19`, `13`, `6`, `4`, `2`, `5`.

```powershell
Get-ChildItem (Join-Path $procedure "01-entrada-inicializacao") -File |
  Select-Object -ExpandProperty Name
Get-ChildItem (Join-Path $procedure "02-operacao-procedimento-do-agente") -File |
  Select-Object -ExpandProperty Name
```

Esperado no baseline:

- 01: `prompt-00-leia-este-arquivo-primeiro.md` e `indice-01-mapa-dos-arquivos-e-ordem-de-leitura.md`.
- 02: `prompt-01-papel-e-restricoes-do-agente.md`, `prompt-02-como-localizar-e-ler-o-archetype.md`, `prompt-03-regras-obrigatorias-de-geracao.md`, `prompt-04-fluxo-de-execucao-e-verificacao.md` e `prompt-05-entregaveis-e-relatorios.md`.

Registre o SHA-256 dos sete arquivos antes de qualquer edição, para comparar o conteúdo normativo depois.

## 2. Fase 1 — 01 renomeada

### 2.1 Renomear

```powershell
$p01 = Join-Path $procedure "01-entrada-inicializacao"
Rename-Item (Join-Path $p01 "prompt-00-leia-este-arquivo-primeiro.md") "00-entry-point.md"
Rename-Item (Join-Path $p01 "indice-01-mapa-dos-arquivos-e-ordem-de-leitura.md") "01-mapa-dos-arquivos-e-ordem-de-leitura.md"
```

Antes de cada `Rename-Item`, confirme que o destino não existe.

### 2.2 Atualizar referências

Siga `contracts/rename-manifest.md`:

- 01: linhas 11, 12 e 44 do índice; rodapés `prompt-00`, caminho do índice e caminhos completos de 01 na entrada.
- 02: referências a `prompt-00` em `prompt-01` e `prompt-03`; os nomes dos cinco arquivos de 02 permanecem antigos.
- 03: referências a `prompt-00` em `03/01` e `03/04`.
- README: linhas 604, 845, 995, 2067, 2099, 2111, 2151, 2152, 2207, 2513, 2567, 2639 e 2704.
- Redirect legado: linha 7 aponta para `00-entry-point.md`.
- Suítes: `BuildArtifactsTest`, `PathRenameContractTest`, `HardStopContractTest`, `DestinationContractTest`, `ReadmeConsistencyTest` e `FileRenameContractTest`.

### 2.3 Verificar a Fase 1

```powershell
Get-ChildItem (Join-Path $procedure "01-entrada-inicializacao") -File |
  Select-Object -ExpandProperty Name
Get-ChildItem (Join-Path $procedure "02-operacao-procedimento-do-agente") -File |
  Select-Object -ExpandProperty Name
```

Esperado:

- 01: exatamente `00-entry-point.md` e `01-mapa-dos-arquivos-e-ordem-de-leitura.md`; 2 arquivos.
- 02: os cinco nomes antigos; 5 arquivos.

```powershell
Get-ChildItem $procedure, "README.md", "src/test" -Recurse -File |
  Where-Object { $_.FullName -notmatch '\\specs\\|\\graphify-out\\|\\target\\|\\utils\\|ProcedureEntryRenameContractTest|PathRenameContractTest' } |
  Select-String -Pattern 'prompt-00|indice-01'
```

Esperado: 0 linhas. O caminho do redirect legado e as asserções negativas das suítes são exceções.

```powershell
mvn -B test
```

Esperado: 100% das suítes passam; a nova `ProcedureEntryRenameContractTest` reconhece a Fase 1 (01 nova, 02 antiga).

## 3. Fase 2 — 02 renomeada

### 3.1 Renomear

```powershell
$p02 = Join-Path $procedure "02-operacao-procedimento-do-agente"
Rename-Item (Join-Path $p02 "prompt-01-papel-e-restricoes-do-agente.md") "01-papel-e-restricoes-do-agente.md"
Rename-Item (Join-Path $p02 "prompt-02-como-localizar-e-ler-o-archetype.md") "02-como-localizar-e-ler-o-archetype.md"
Rename-Item (Join-Path $p02 "prompt-03-regras-obrigatorias-de-geracao.md") "03-regras-obrigatorias-de-geracao.md"
Rename-Item (Join-Path $p02 "prompt-04-fluxo-de-execucao-e-verificacao.md") "04-fluxo-de-execucao-e-verificacao.md"
Rename-Item (Join-Path $p02 "prompt-05-entregaveis-e-relatorios.md") "05-entregaveis-e-relatorios.md"
```

### 3.2 Atualizar referências

Siga `contracts/rename-manifest.md`:

- Índice: linhas 13 a 17, 33, 46, 47, 49, 50 e 51.
- Entrada: linhas 168, 170, 171, 172, 177, 178, 179, 180 e 710 a 714.
- `prompt-04`: linha 64 (`prompt-03`).
- README: linhas 804 e 991.
- Suítes: `BuildArtifactsTest`, `PathRenameContractTest`, `DestinationContractTest`, `CatalogSyncContractTest`, `ReadmeConsistencyTest` e `ProcedureEntryRenameContractTest`.

### 3.3 Verificar a Fase 2

```powershell
Get-ChildItem (Join-Path $procedure "02-operacao-procedimento-do-agente") -File |
  Select-Object -ExpandProperty Name
(Get-ChildItem $procedure -Recurse -File).Count
(Get-ChildItem $procedure -Recurse -File -Filter *.md).Count
(Get-ChildItem $procedure -Recurse -File -Filter *.json).Count
(Get-ChildItem $procedure -Directory).Count
```

Esperado: exatamente os cinco nomes novos; `19`, `13`, `6`, `4`; 2 em 01 e 5 em 02.

```powershell
Get-ChildItem $procedure, "README.md", "src/test" -Recurse -File |
  Where-Object { $_.FullName -notmatch '\\specs\\|\\graphify-out\\|\\target\\|\\utils\\|ProcedureEntryRenameContractTest|PathRenameContractTest' } |
  Select-String -Pattern 'prompt-0[0-5]|indice-01'
```

Esperado: 0 linhas, exceto o caminho do redirect legado e as asserções negativas.

```powershell
mvn -B test
mvn clean install
mvn -B clean integration-test
```

Esperado: todas as suítes passam; o artefato é instalado; as fixtures de integração passam.

```powershell
mvn -B test "-Deda.verify.installed.jar=true"
```

Opcional, após `install`. Verifica o JAR instalado; não é gate de aceite desta feature.

## 4. Verificação do redirect legado

```powershell
Get-Content "src/main/resources/utils/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md"
```

Esperado:

- O arquivo permanece no caminho antigo.
- O conteúdo aponta para `llm-maven-flags-inference/01-entrada-inicializacao/00-entry-point.md`.
- Nenhum outro redirect foi criado.

## 5. Verificação do conteúdo normativo

Compare o SHA-256 dos sete arquivos com o baseline. O conteúdo deve ser idêntico, exceto pelos tokens de referência mapeados no manifesto. Arquivos sem referências aos nomes antigos devem ter hash idêntico.

## 6. Verificações negativas

| Alteração provocada | Resultado esperado |
|---|---|
| Deixar um dos sete arquivos com o nome antigo | `ProcedureEntryRenameContractTest` e `PathRenameContractTest` falham e nomeiam o arquivo |
| Deixar uma referência antiga no procedimento ou no README | `ProcedureEntryRenameContractTest` falha e nomeia o arquivo e a linha |
| Criar ou remover um arquivo do procedimento | `BuildArtifactsTest`, `PathRenameContractTest` e `ProcedureEntryRenameContractTest` falham na contagem 19/13/6 |
| Renomear uma subpasta | `PathRenameContractTest` e `ProcedureEntryRenameContractTest` falham |
| Alterar uma regra, chave JSON ou estrutura de template | `ProcedureContractTest`, `CatalogSyncContractTest` ou a revisão de diff falham |
| Remover ou renomear o redirect legado | `PathRenameContractTest` e `ProcedureEntryRenameContractTest` falham |
| Não registrar a nova suíte no runner | `CoverageRegistryTest` e `ReadmeConsistencyTest` falham |
| Deixar o README sem o novo caminho da entrada | `ReadmeConsistencyTest` falha |
| Deixar o índice sem um dos 19 caminhos novos | `BuildArtifactsTest` e `FileRenameContractTest` falham |
| Alterar o catálogo (35 propriedades / 18 filesets) | `BuildArtifactsTest`, `CatalogSyncContractTest` e `FileRenameContractTest` falham |

## 7. Critério de conclusão

- 7/7 arquivos renomeados; 0 nomes antigos como arquivo nas duas subpastas.
- 7/7 conteúdos normativos preservados, exceto tokens de referência.
- 19 arquivos, 13 Markdown, 6 JSON, 4 subpastas, 2 em 01 e 5 em 02.
- Índice lista os 19 arquivos com os novos nomes.
- Redirect legado funcional no caminho antigo.
- 0 referências antigas no corpus operacional, exceto redirect legado e asserções negativas.
- `mvn -B test`, `mvn clean install` e `mvn -B clean integration-test` passam.
- Nenhum catálogo, template, `pom.xml` ou spec histórica alterado.
