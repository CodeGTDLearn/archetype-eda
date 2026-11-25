# Contract: Rename Manifest — 008-rename-procedure-files

**Feature**: 008-rename-procedure-files | **Date**: 2026-11-25

## Purpose

Definir o mapa único de renomeação, o mapa de tokens curtos e o inventário de referências operacionais que a implementação deve atualizar. Este contrato é a fonte de verdade para a suíte `FileRenameContractTest` e para a revisão de diff.

## Rename map

| # | Subpasta | Nome antigo | Nome novo |
|---|---|---|---|
| 1 | 03 | `regras-01-como-derivar-a-entidade-do-contrato.md` | `01-como-derivar-a-entidade-do-contrato.md` |
| 2 | 03 | `regras-02-como-resolver-tipos-e-enums.md` | `02-como-resolver-tipos-e-enums.md` |
| 3 | 03 | `regras-03-como-derivar-flags-de-tipos-aninhados.md` | `03-como-derivar-flags-de-tipos-aninhados.md` |
| 4 | 03 | `regras-04-como-aplicar-precedencia-e-validacao.md` | `04-como-aplicar-precedencia-e-validacao.md` |
| 5 | 03 | `regras-05-como-materializar-e-emitir-o-contrato.md` | `05-como-materializar-e-emitir-o-contrato.md` |
| 6 | 03 | `regras-06-como-relatar-proveniencia.md` | `06-como-relatar-proveniencia.md` |
| 7 | 04 | `template-01-schema-do-contrato.json` | `01-schema-do-contrato.json` |
| 8 | 04 | `template-02-exemplo-contrato-plano.json` | `02-exemplo-contrato-plano.json` |
| 9 | 04 | `template-03-exemplo-contrato-com-objetos-aninhados.json` | `03-exemplo-contrato-com-objetos-aninhados.json` |
| 10 | 04 | `template-04-exemplo-contrato-com-enums.json` | `04-exemplo-contrato-com-enums.json` |
| 11 | 04 | `template-05-exemplo-contrato-com-tipos-do-projeto.json` | `05-exemplo-contrato-com-tipos-do-projeto.json` |
| 12 | 04 | `template-06-exemplo-contrato-com-attributes-explicito.json` | `06-exemplo-contrato-com-attributes-explicito.json` |

## Token map

| Token antigo | Token novo |
|---|---|
| `regras-01` | `01` |
| `regras-02` | `02` |
| `regras-03` | `03` |
| `regras-04` | `04` |
| `regras-05` | `05` |
| `regras-06` | `06` |
| `template-01` | `01` |
| `template-02` | `02` |
| `template-03` | `03` |
| `template-04` | `04` |
| `template-05` | `05` |
| `template-06` | `06` |

## Reference inventory

### Procedure files

| Arquivo | Linhas | Referências antigas | Referências novas |
|---|---|---|---|
| `src/main/resources/llm-maven-flags-inference/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md` | 173–176, 220, 715–726 | `regras-01`, `regras-04`, `regras-05`, `template-02`, 12 nomes completos | `01`, `04`, `05`, `02`, 12 nomes completos novos |
| `src/main/resources/llm-maven-flags-inference/01-entrada-inicializacao/indice-01-mapa-dos-arquivos-e-ordem-de-leitura.md` | 18–29, 52–53 | 12 nomes completos; `regras-01`..`regras-06`; `template-01` | 12 nomes completos novos; `01`..`06`; `01` |
| `src/main/resources/llm-maven-flags-inference/02-operacao-procedimento-do-agente/prompt-01-papel-e-restricoes-do-agente.md` | 35 | `regras-01` | `01` |
| `src/main/resources/llm-maven-flags-inference/02-operacao-procedimento-do-agente/prompt-02-como-localizar-e-ler-o-archetype.md` | 109 | `regras-01` | `01` |
| `src/main/resources/llm-maven-flags-inference/02-operacao-procedimento-do-agente/prompt-04-fluxo-de-execucao-e-verificacao.md` | 7, 26, 44 | `regras-06`, `regras-01`, `regras-05` | `06`, `01`, `05` |
| `src/main/resources/llm-maven-flags-inference/03-derivacao-regras-de-formacao-do-contrato/regras-02-como-resolver-tipos-e-enums.md` | 67, 70 | `regras-03` | `03` |
| `src/main/resources/llm-maven-flags-inference/03-derivacao-regras-de-formacao-do-contrato/regras-04-como-aplicar-precedencia-e-validacao.md` | 41 | `regras-01` | `01` |
| `src/main/resources/llm-maven-flags-inference/03-derivacao-regras-de-formacao-do-contrato/regras-06-como-relatar-proveniencia.md` | 61 | `regras-01` | `01` |
| `src/main/resources/llm-maven-flags-inference/04-referencia-templates-de-contrato/template-01-schema-do-contrato.json` | 4 | `regras-01` | `01` |
| `src/main/resources/llm-maven-flags-inference/04-referencia-templates-de-contrato/template-02-exemplo-contrato-plano.json` | 2 | `regras-01` | `01` |
| `src/main/resources/llm-maven-flags-inference/04-referencia-templates-de-contrato/template-06-exemplo-contrato-com-attributes-explicito.json` | 2 | `regras-01`, `regras-02` | `01`, `02` |

### README

| Arquivo | Linhas | Referências antigas | Referências novas |
|---|---|---|---|
| `README.md` | 910, 917, 933, 941, 995, 2171–2176, 2182–2187, 2391, 2868, 2871, 2882, 2890, 2942, 2950, 2954 | Formas curtas `regras-01`..`regras-06`, `template-01`..`template-06`; 12 nomes completos nas tabelas | Formas curtas `01`..`06`; 12 nomes completos novos |

### Test suites

| Arquivo | Linhas | Referências antigas | Referências novas |
|---|---|---|---|
| `src/test/archetype-post-generate/BuildArtifactsTest.groovy` | 96–107 | 12 caminhos completos | 12 caminhos completos novos |
| `src/test/archetype-post-generate/PathRenameContractTest.groovy` | 55–66 | 12 caminhos completos | 12 caminhos completos novos |
| `src/test/archetype-post-generate/ProcedureContractTest.groovy` | 12, 139, 148, 198–199, 252, 260 | `template-01`, `regras-03`, `template-03`, `regras-01` em caminhos, comentário e rótulos | `01`, `03`, `03`, `01` |
| `src/test/archetype-post-generate/HardStopContractTest.groovy` | 110 | Caminho de `regras-01` | Caminho de `01` |
| `src/test/archetype-post-generate/DestinationContractTest.groovy` | 40–41, 58, 83, 87, 91 | Caminhos e rótulos de `regras-01` e `regras-06` | Caminhos e rótulos de `01` e `06` |
| `src/test/archetype-post-generate/CatalogSyncContractTest.groovy` | 9, 162, 168 | Caminhos e rótulo de `regras-04` e `regras-06` | Caminhos e rótulo de `04` e `06` |

### New suite (excluded from anti-drift scan)

| Arquivo | Observação |
|---|---|
| `src/test/archetype-post-generate/FileRenameContractTest.groovy` | Contém os padrões antigos apenas como asserções negativas; a varredura do corpus deve excluir esta suíte |

## Replacement rules

1. **Nome completo**: substituir a ocorrência exata do nome antigo, incluindo extensão, pelo nome novo correspondente.
2. **Forma curta de regra**: substituir `regras-NN` por `NN`; preservar `§`, pontuação, crases, tabelas e o restante da frase.
3. **Forma curta de template**: substituir `template-NN` por `NN`; preservar `§`, pontuação, crases, tabelas e o restante da frase.
4. **JSON**: alterar apenas o valor textual de `description` que contém a referência; não alterar chaves, schema, tipos, `properties`, exemplos ou formatação estrutural. O JSON deve continuar válido.
5. **README**: atualizar tabelas e prosa; manter os links para os diretórios `03-derivacao-regras-de-formacao-do-contrato/` e `04-referencia-templates-de-contrato/`.
6. **Testes**: atualizar caminhos, comentários e rótulos de `check`; registrar `FileRenameContractTest.groovy` no runner.
7. **Exclusões**: não alterar `specs/**`, `graphify-out/**`, `target/**` ou `.git/**`.
8. **Semântica**: nenhuma regra, definição, exemplo de domínio, chave JSON, `defaultValue`, `validationRegex` ou metadado estrutural pode mudar.

## Invariants

- 12 arquivos renomeados; 0 nomes antigos nas subpastas 03 e 04.
- 19 arquivos no procedimento; 13 Markdown; 6 JSON; 4 subpastas; 6 arquivos em 03 e 04.
- 18 arquivos operacionais sem referências antigas.
- Catálogo com 35 propriedades e 18 filesets.
- Nomes das subpastas inalterados.
- Ordem de leitura e papel dos arquivos inalterados.

## Validation commands

```powershell
# nomes novos e ausência dos antigos
Get-ChildItem "src/main/resources/llm-maven-flags-inference/03-derivacao-regras-de-formacao-do-contrato" -File
Get-ChildItem "src/main/resources/llm-maven-flags-inference/04-referencia-templates-de-contrato" -File

# contagens
(Get-ChildItem "src/main/resources/llm-maven-flags-inference" -Recurse -File).Count
(Get-ChildItem "src/main/resources/llm-maven-flags-inference" -Recurse -File -Filter *.md).Count
(Get-ChildItem "src/main/resources/llm-maven-flags-inference" -Recurse -File -Filter *.json).Count

# referências antigas no corpus operacional (esperado: 0)
Get-ChildItem README.md, "src/main/resources/llm-maven-flags-inference", "src/test" -Recurse -File |
  Where-Object { $_.FullName -notmatch '\\specs\\|\\graphify-out\\|\\target\\|FileRenameContractTest' } |
  Select-String -Pattern 'regras-0[1-6]|template-0[1-6]'

# build
mvn -B test
mvn clean install
```
