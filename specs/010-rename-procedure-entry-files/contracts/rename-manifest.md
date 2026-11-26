# Rename Manifest — 010-rename-procedure-entry-files

**Feature**: 010-rename-procedure-entry-files | **Date**: 2026-11-25

Este manifesto é a fonte única do mapa `old → new`, dos tokens curtos, do inventário de referências, das exceções e dos critérios de saída por fase.

## 1. Mapa old → new

| # | Fase | Subpasta | Nome antigo | Nome novo | Papel |
|---:|---:|---|---|---|---|
| 1 | 1 | `01-entrada-inicializacao` | `prompt-00-leia-este-arquivo-primeiro.md` | `00-entry-point.md` | Porta de entrada |
| 2 | 1 | `01-entrada-inicializacao` | `indice-01-mapa-dos-arquivos-e-ordem-de-leitura.md` | `01-mapa-dos-arquivos-e-ordem-de-leitura.md` | Índice e ordem de leitura |
| 3 | 2 | `02-operacao-procedimento-do-agente` | `prompt-01-papel-e-restricoes-do-agente.md` | `01-papel-e-restricoes-do-agente.md` | Papel e restrições |
| 4 | 2 | `02-operacao-procedimento-do-agente` | `prompt-02-como-localizar-e-ler-o-archetype.md` | `02-como-localizar-e-ler-o-archetype.md` | Localização e leitura |
| 5 | 2 | `02-operacao-procedimento-do-agente` | `prompt-03-regras-obrigatorias-de-geracao.md` | `03-regras-obrigatorias-de-geracao.md` | Regras obrigatórias |
| 6 | 2 | `02-operacao-procedimento-do-agente` | `prompt-04-fluxo-de-execucao-e-verificacao.md` | `04-fluxo-de-execucao-e-verificacao.md` | Execução e verificação |
| 7 | 2 | `02-operacao-procedimento-do-agente` | `prompt-05-entregaveis-e-relatorios.md` | `05-entregaveis-e-relatorios.md` | Entregáveis e relatórios |

## 2. Mapa de tokens curtos

| Token antigo | Token novo | Fase |
|---|---|---:|
| `prompt-00` | `00-entry-point` | 1 |
| `indice-01` | `01-mapa-dos-arquivos-e-ordem-de-leitura` | 1 |
| `prompt-01` | `01-papel-e-restricoes-do-agente` | 2 |
| `prompt-02` | `02-como-localizar-e-ler-o-archetype` | 2 |
| `prompt-03` | `03-regras-obrigatorias-de-geracao` | 2 |
| `prompt-04` | `04-fluxo-de-execucao-e-verificacao` | 2 |
| `prompt-05` | `05-entregaveis-e-relatorios` | 2 |

## 3. Inventário de referências no procedimento

### 3.1 `01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md` → `00-entry-point.md` (Fase 1)

Total: 35 ocorrências.

| Fase | Linhas | Token | Ação |
|---:|---|---|---|
| 1 | 124, 202, 226, 250, 274, 298, 322, 346, 370, 394, 418, 442, 466, 490, 514, 538, 562, 586, 610 | `prompt-00` | `00-entry-point` (rodapés do dicionário) |
| 1 | 620 | `indice-01-mapa-dos-arquivos-e-ordem-de-leitura.md` | novo caminho do índice |
| 1 | 708 | `prompt-00-leia-este-arquivo-primeiro.md` | `00-entry-point.md` |
| 1 | 709 | `indice-01-mapa-dos-arquivos-e-ordem-de-leitura.md` | `01-mapa-dos-arquivos-e-ordem-de-leitura.md` |
| 2 | 168, 170, 171, 172, 177, 178, 179, 180 | `prompt-01`, `prompt-02`, `prompt-04` | tokens novos de 02 |
| 2 | 710, 711, 712, 713, 714 | `prompt-01` a `prompt-05` | nomes novos de 02 |

### 3.2 `01-entrada-inicializacao/indice-01-mapa-dos-arquivos-e-ordem-de-leitura.md` → `01-mapa-dos-arquivos-e-ordem-de-leitura.md` (Fase 1)

Total: 14 ocorrências.

| Fase | Linhas | Token | Ação |
|---:|---|---|---|
| 1 | 11, 12 | `prompt-00...`, `indice-01...` | novos caminhos de 01 |
| 1 | 44 | `prompt-00` | `00-entry-point` |
| 2 | 13, 14, 15, 16, 17 | `prompt-01...` a `prompt-05...` | novos caminhos de 02 |
| 2 | 33 | `prompt-02` | `02-como-localizar-e-ler-o-archetype` |
| 2 | 46, 47, 49, 50, 51 | `prompt-01` a `prompt-05` | tokens novos de 02 |

### 3.3 Demais arquivos do procedimento

| Fase | Arquivo | Linha | Token | Ação |
|---:|---|---:|---|---|
| 1 | `02-operacao-procedimento-do-agente/prompt-01-papel-e-restricoes-do-agente.md` | 26 | `prompt-00` | `00-entry-point` |
| 1 | `02-operacao-procedimento-do-agente/prompt-03-regras-obrigatorias-de-geracao.md` | 68 | `prompt-00` | `00-entry-point` |
| 2 | `02-operacao-procedimento-do-agente/prompt-04-fluxo-de-execucao-e-verificacao.md` | 64 | `prompt-03` | `03-regras-obrigatorias-de-geracao` |
| 1 | `03-derivacao-regras-de-formacao-do-contrato/01-como-derivar-a-entidade-do-contrato.md` | 17 | `prompt-00` | `00-entry-point` |
| 1 | `03-derivacao-regras-de-formacao-do-contrato/04-como-aplicar-precedencia-e-validacao.md` | 35, 66 | `prompt-00` | `00-entry-point` |

## 4. Inventário de referências no README

Total: 17 ocorrências em 15 linhas.

| Fase | Linha | Token | Ação |
|---:|---:|---|---|
| 1 | 604 | `prompt-00`, `indice-01` | `00-entry-point`, `01-mapa-dos-arquivos-e-ordem-de-leitura` |
| 1 | 845 | `prompt-00` | `00-entry-point` |
| 1 | 995 | caminho completo da entrada | novo caminho completo |
| 1 | 2067 | `prompt-00` | `00-entry-point` |
| 1 | 2099 | `prompt-00` | `00-entry-point` |
| 1 | 2111 | caminho completo da entrada | novo caminho completo |
| 1 | 2151 | `prompt-00` | `00-entry-point` |
| 1 | 2152 | caminho completo da entrada | novo caminho completo |
| 1 | 2207 | caminho completo da entrada | novo caminho completo |
| 1 | 2513 | caminho completo da entrada | novo caminho completo |
| 1 | 2567 | caminho completo da entrada | novo caminho completo |
| 1 | 2639 | caminho completo da entrada | novo caminho completo |
| 1 | 2704 | caminho completo da entrada | novo caminho completo |
| 2 | 804 | `prompt-04`, `prompt-05` | `04-fluxo-de-execucao-e-verificacao`, `05-entregaveis-e-relatorios` |
| 2 | 991 | `prompt-03` | `03-regras-obrigatorias-de-geracao` |

**Nota**: as linhas 2111, 2152, 2207, 2513, 2567, 2639 e 2704 são templates de spec; todas devem apontar para o novo caminho da entrada. `ReadmeConsistencyTest` 3.5.2 verifica os cinco templates.

## 5. Redirect legado

| Caminho | Fase | Ação |
|---|---:|---|
| `src/main/resources/utils/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md` | 1 | Manter o nome; atualizar a linha 7 para apontar para `llm-maven-flags-inference/01-entrada-inicializacao/00-entry-point.md` |

- Nenhum redirect é criado para os outros seis arquivos.
- O redirect não contém cópia de outro arquivo do procedimento.
- A verificação que assegura o redirect pode conter o caminho antigo como dado de teste.

## 6. Suítes de verificação

| Suíte | Fase | Atualização |
|---|---:|---|
| `BuildArtifactsTest.groovy` | 1 e 2 | Lista de 19 caminhos; caminho do índice; 7 nomes novos |
| `PathRenameContractTest.groovy` | 1 e 2 | Lista de 19 caminhos; `canonical` novo; `legacyEntry` antigo; README; JAR opt-in |
| `HardStopContractTest.groovy` | 1 | Caminho da entrada; rótulos; entrada do ZIP |
| `DestinationContractTest.groovy` | 1 e 2 | Caminhos de entrada, 03, 04 e 05; rótulos |
| `CatalogSyncContractTest.groovy` | 2 | Caminho de `03-regras-obrigatorias-de-geracao.md` |
| `ReadmeConsistencyTest.groovy` | 1 e 2 | `prompt00Path` novo; `prompt04` novo; rótulos |
| `FileRenameContractTest.groovy` | 1 | Caminho do índice novo |
| `ProcedureEntryRenameContractTest.groovy` (nova) | 1 e 2 | 7 nomes, contagens, índice, redirect, anti-drift; phase-aware |
| `src/test/groovy/run-archetype-post-generate-tests.groovy` | 1 | Registrar a nova suíte |

## 7. Exceções e exclusões

| Item | Regra |
|---|---|
| Redirect legado | O nome antigo é permitido apenas no caminho `src/main/resources/utils/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md` |
| Asserções negativas | `ProcedureEntryRenameContractTest` e `PathRenameContractTest` podem conter nomes antigos como dados de teste negativo |
| Specs históricas | `specs/**` não é reescrito, exceto os artefatos desta feature |
| Planos e tarefas históricos | `specs/**/plan.md` e `specs/**/tasks.md` de outras features não são reescritos |
| Saída gerada | `target/**`, `graphify-out/**` e `.git/**` não são editados |
| Catálogo | `META-INF/maven/**` não muda |
| Templates | `archetype-resources/**` não muda |

## 8. Critérios de saída por fase

### Fase 1

- 01 contém exatamente `00-entry-point.md` e `01-mapa-dos-arquivos-e-ordem-de-leitura.md`.
- 02 contém exatamente os cinco nomes antigos.
- Índice lista os novos caminhos de 01 e os antigos de 02.
- Redirect legado aponta para `00-entry-point.md`.
- 0 referências textuais a `prompt-00` e `indice-01` no corpus operacional, exceto redirect legado e asserções negativas.
- `mvn -B test` passa.

### Fase 2

- 02 contém exatamente os cinco nomes novos; nenhum `prompt-*`.
- 01 mantém os dois nomes novos.
- Índice lista os 19 caminhos novos.
- 0 referências textuais a `prompt-0[0-5]` e `indice-01` no corpus operacional, exceto redirect legado e asserções negativas.
- 19 arquivos, 13 Markdown, 6 JSON, 4 subpastas, 2 em 01 e 5 em 02.
- `mvn -B test`, `mvn clean install` e `mvn -B clean integration-test` passam.
- Verificação opt-in do JAR instalado (não gate).
