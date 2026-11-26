# Quickstart: Renomear a Pasta e os Seis Arquivos das Regras de Formação do Contrato

**Feature**: 011-rename-contract-rule-files | **Date**: 2026-11-26

## Propósito

Roteiro executável para validar a renomeação da subpasta 03 e dos seis arquivos de regras em cada fase, sem antecipar o escopo das fases seguintes. O mapa `old → new` e o inventário completo estão em [contracts/rename-manifest.md](./contracts/rename-manifest.md); as entidades e invariantes estão em [data-model.md](./data-model.md).

## Pré-requisitos

- Windows ou POSIX com PowerShell.
- JDK 21 e Maven 3.9.16 disponíveis.
- Repositório no branch da feature.
- Nenhuma alteração pendente fora do escopo da fase.

## Baseline (Fase 0)

```powershell
$procedure = "src/main/resources/llm-maven-flags-inference"
Test-Path (Join-Path $procedure "03-derivacao-regras-de-formacao-do-contrato")
Get-ChildItem (Join-Path $procedure "03-derivacao-regras-de-formacao-do-contrato") -File |
  Select-Object -ExpandProperty Name
(Get-ChildItem $procedure -Recurse -File).Count
(Get-ChildItem $procedure -Recurse -File -Filter *.md).Count
(Get-ChildItem $procedure -Recurse -File -Filter *.json).Count
```

Esperado:

- A pasta antiga existe.
- Os seis arquivos têm o token `como-` após o numeral.
- 19 arquivos; 13 Markdown; 6 JSON; 4 subpastas; 6 arquivos em 03.
- `mvn -B clean test` passa antes da mudança.

## Fase 1 — Pasta e referências à pasta

### Verificação estrutural

```powershell
Test-Path (Join-Path $procedure "03-regras-de-formacao-do-contrato")
Test-Path (Join-Path $procedure "03-derivacao-regras-de-formacao-do-contrato")
Get-ChildItem (Join-Path $procedure "03-regras-de-formacao-do-contrato") -File |
  Select-Object -ExpandProperty Name
```

Esperado:

- A pasta nova existe; a antiga não.
- Os seis arquivos dentro dela mantêm os nomes atuais com `como-`.
- 6 arquivos em 03.

### Varredura da pasta antiga

```powershell
Get-ChildItem README.md, $procedure, "src/test" -Recurse -File |
  Where-Object { $_.FullName -notmatch '\\specs\\|\\graphify-out\\|\\target\\|\\utils\\|ContractRuleRenameContractTest|FileRenameContractTest' } |
  Select-String -Pattern '03-derivacao-regras-de-formacao-do-contrato'
```

Esperado: 0 linhas. O caminho do redirect legado em `utils/` não cita a pasta 03 e não aparece na varredura.

### Build

```powershell
mvn -B clean test
```

Esperado: todas as suítes passam, incluindo `ContractRuleRenameContractTest` no estado da Fase 1.

## Fase 2 — Três primeiras regras

### Verificação estrutural

```powershell
Get-ChildItem (Join-Path $procedure "03-regras-de-formacao-do-contrato") -File |
  Select-Object -ExpandProperty Name
```

Esperado:

- `01-derivar-a-entidade-do-contrato.md`
- `02-resolver-tipos-e-enums.md`
- `03-derivar-flags-de-tipos-aninhados.md`
- `04-como-aplicar-precedencia-e-validacao.md`
- `05-como-materializar-e-emitir-o-contrato.md`
- `06-como-relatar-proveniencia.md`

### Varredura dos três nomes antigos

```powershell
Get-ChildItem README.md, $procedure, "src/test" -Recurse -File |
  Where-Object { $_.FullName -notmatch '\\specs\\|\\graphify-out\\|\\target\\|\\utils\\|ContractRuleRenameContractTest|FileRenameContractTest' } |
  Select-String -Pattern '01-como-derivar-a-entidade-do-contrato|02-como-resolver-tipos-e-enums|03-como-derivar-flags-de-tipos-aninhados'
```

Esperado: 0 linhas, exceto asserções negativas das suítes.

### Build

```powershell
mvn -B clean test
```

Esperado: todas as suítes passam no estado da Fase 2.

## Fase 3 — Três últimas regras

### Verificação estrutural

```powershell
Get-ChildItem (Join-Path $procedure "03-regras-de-formacao-do-contrato") -File |
  Select-Object -ExpandProperty Name
```

Esperado:

- `01-derivar-a-entidade-do-contrato.md`
- `02-resolver-tipos-e-enums.md`
- `03-derivar-flags-de-tipos-aninhados.md`
- `04-aplicar-precedencia-e-validacao.md`
- `05-materializar-e-emitir-o-contrato.md`
- `06-relatar-proveniencia.md`

### Contagens

```powershell
(Get-ChildItem $procedure -Recurse -File).Count
(Get-ChildItem $procedure -Recurse -File -Filter *.md).Count
(Get-ChildItem $procedure -Recurse -File -Filter *.json).Count
(Get-ChildItem $procedure -Directory).Count
```

Esperado: 19 arquivos; 13 Markdown; 6 JSON; 4 subpastas; 6 arquivos em 03.

### Varredura final

```powershell
Get-ChildItem README.md, $procedure, "src/test" -Recurse -File |
  Where-Object { $_.FullName -notmatch '\\specs\\|\\graphify-out\\|\\target\\|\\utils\\|ContractRuleRenameContractTest|FileRenameContractTest' } |
  Select-String -Pattern '03-derivacao-regras-de-formacao-do-contrato|0[1-6]-como-'
```

Esperado: 0 linhas, exceto asserções negativas. O redirect legado em `utils/` permanece intacto e aponta para `00-entry-point.md`.

### Conteúdo normativo

Comparar os seis arquivos com o baseline (SHA-256 ou `git diff`). Esperado:

- Conteúdo idêntico, exceto pelos tokens de referência atualizados.
- Nenhuma regra, definição, exemplo de domínio, metadado, default ou formato de contrato alterado.

### Build completo

```powershell
mvn -B clean test
mvn clean install
mvn -B clean integration-test
```

Esperado: todas as suítes passam; as fixtures de integração geram e validam os projetos; o artefato é instalado.

### Verificação opt-in do JAR instalado

```powershell
mvn -B test "-Deda.verify.installed.jar=true"
```

Esperado: o JAR instalado expõe `llm-maven-flags-inference/03-regras-de-formacao-do-contrato/` com os seis nomes novos e o redirect legado em `utils/`; nenhum nome antigo, exceto o caminho do redirect. Esta verificação não é gate de aceite.

## Verificações negativas

| Alteração provocada | Falha esperada |
|---|---|
| Deixar a pasta 03 com o nome antigo | `ContractRuleRenameContractTest`, `PathRenameContractTest` e `ProcedureEntryRenameContractTest` |
| Deixar um dos seis arquivos com `como-` | `ContractRuleRenameContractTest`, `FileRenameContractTest` e `PathRenameContractTest` |
| Deixar uma referência antiga no procedimento | `ContractRuleRenameContractTest` e as suítes de contrato que leem o caminho |
| Deixar uma referência antiga no README ou nas fixtures | `ContractRuleRenameContractTest` e `ReadmeConsistencyTest` |
| Criar, remover ou mover um arquivo do procedimento | `BuildArtifactsTest`, `PathRenameContractTest` e `ContractRuleRenameContractTest` |
| Renomear as subpastas 01, 02 ou 04 | `PathRenameContractTest` e `ProcedureEntryRenameContractTest` |
| Alterar uma regra, chave JSON ou estrutura de template | `ProcedureContractTest`, `CatalogSyncContractTest` ou a revisão de diff |
| Remover ou renomear o redirect legado | `PathRenameContractTest` |
| Não registrar a suíte nova no runner | `CoverageRegistryTest` e `ReadmeConsistencyTest` |

## Critérios de aceite

- [ ] 1 pasta renomeada; 0 ocorrências do nome antigo como nome de pasta.
- [ ] 6 arquivos renomeados; 0 nomes antigos como nome de arquivo.
- [ ] 6 conteúdos normativos idênticos ao baseline, exceto referências.
- [ ] 19 arquivos, 13 Markdown, 6 JSON, 4 subpastas, 6 em 03.
- [ ] 0 alterações nos nomes das outras subpastas; 0 arquivos criados/removidos/movidos.
- [ ] 0 referências operacionais antigas, exceto redirect legado e asserções negativas.
- [ ] Índice lista os 19 caminhos novos na mesma ordem.
- [ ] Redirect legado permanece funcional.
- [ ] Suítes existentes e a nova passam.
- [ ] Nenhuma mudança em regras, catálogo, templates, ordem ou formato do contrato.
