# Phase 1 — Data Model: llm-maven-flags-inference

**Feature**: 006-llm-maven-flags-inference | **Date**: 2026-11-23

## Entidades

### ProcedureRoot

| Atributo | Valor | Regra |
|---|---|---|
| `path` | `src/main/resources/llm-maven-flags-inference/` | Raiz única na árvore-fonte |
| `jarPath` | `llm-maven-flags-inference/` | Mesma raiz dentro do JAR |
| `fileCount` | 19 | Contagem travada |
| `subfolders` | 4 | Nomes atuais, sem renomear |

### Subfolder

| Ordem | Nome | Arquivos | Papel na sequência |
|---|---|---|---|
| 1 | `01-entrada-inicializacao` | 2 | Porta de entrada, índice e ordem de leitura |
| 2 | `02-operacao-procedimento-do-agente` | 5 | Papel, leitura do JAR, regras, execução e entregáveis |
| 3 | `03-derivacao-regras-de-formacao-do-contrato` | 6 | Regras normativas de derivação |
| 4 | `04-referencia-templates-de-contrato` | 6 | Schema e exemplos de contrato |

### ProcedureFile

| Atributo | Regra |
|---|---|
| `relativePath` | Mesmo nome e mesma subpasta de hoje |
| `kind` | Markdown ou JSON, inalterado |
| `contentClass` | Regra, instrução, schema ou exemplo — inalterado |
| `pathReferences` | Apenas o prefixo do caminho muda; o texto da regra permanece |

### LegacyRedirect

| Atributo | Valor | Regra |
|---|---|---|
| `path` | `utils/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md` | Único arquivo no caminho antigo |
| `target` | `llm-maven-flags-inference/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md` | Nova entrada |
| `ruleCount` | 0 | Não pode conter regra nem cópia |
| `fileCountAtLegacyRoot` | 1 | O caminho antigo não guarda mais nada |

### OperatorSpecTemplate

| Atributo | Regra |
|---|---|
| `preambleTarget` | `llm-maven-flags-inference/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md` |
| `entries` | Cinco entradas canônicas, na ordem atual |
| `locations` | Template canônico do README, cenários A e B e templates do kit |

### OperationalDocument

| Atributo | Regra |
|---|---|
| `kind` | README, template do kit, suíte, fixture ou arquivo do procedimento |
| `oldPathAllowed` | Somente o redirect |
| `exclusions` | Specs históricas 003, 004, 005 e `graphify-out/` |

### VerificationSuite

| Atributo | Regra |
|---|---|
| `name` | `PathRenameContractTest.groovy` |
| `scope` | Layout, contagem, redirect, README, templates, fixtures e JAR instalado |
| `wiring` | Registrada no runner de suítes |

## Invariantes

| ID | Invariante | Requisito |
|---|---|---|
| INV-01 | A nova raiz contém exatamente 19 arquivos | FR-002 |
| INV-02 | As 4 subpastas mantêm os nomes atuais e a ordem | FR-002, FR-010 |
| INV-03 | Os 19 nomes de arquivo e a distribuição não mudam | FR-002 |
| INV-04 | O conteúdo normativo das regras não muda | FR-003 |
| INV-05 | O caminho antigo contém exatamente 1 arquivo, no entry point antigo | FR-005 |
| INV-06 | O redirect só aponta; não contém regra nem segunda cópia | FR-005 |
| INV-07 | Documentos operacionais têm 0 referências ao caminho antigo | FR-006, FR-012 |
| INV-08 | O README tem o tópico, a sequência de 5 passos e 4 subseções | FR-008, FR-009, FR-010 |
| INV-09 | Os templates do kit usam apenas `llm-maven-flags-inference` | FR-007 |
| INV-10 | O JAR contém a nova raiz e o redirect | FR-013 |
| INV-11 | As specs históricas permanecem intactas | FR-014 |
| INV-12 | Spec antiga e spec nova produzem a mesma linha `mvn` | FR-015 |

## Fluxos

### Spec nova

```text
prompt do operador
  → llm-maven-flags-inference/01-entrada-inicializacao/prompt-00-...
  → indice-01
  → 02-operacao (papel, JAR, regras, execução, entregáveis)
  → 03-derivacao (regras normativas)
  → 04-referencia (schema e exemplos)
  → contrato materializado
  → linha mvn archetype:generate
```

### Spec antiga

```text
prompt do operador
  → utils/01-entrada-inicializacao/prompt-00-...  (stub)
  → llm-maven-flags-inference/01-entrada-inicializacao/prompt-00-...
  → mesmo fluxo da spec nova
```

### Fronteira do pré-processamento

| Passo | Etapa | Termina em |
|---|---|---|
| 1–5 | Pré-processamento | Linha `mvn archetype:generate` emitida |
| 6 | Geração Maven | Começa depois da linha |

## Regras de validação

| Regra | Verificação |
|---|---|
| Contagem | 19 arquivos na nova raiz; 1 no caminho antigo |
| Nomes | Lista nominal dos 19 arquivos confere |
| Subpastas | 4 diretórios exatos, sem extras |
| Redirect | Só ponteiro; alvo existe na árvore empacotada |
| Anti-drift | Nenhum documento operacional cita o caminho antigo |
| README | Tópico e 4 subseções presentes, com os nomes atuais |
| Templates | Nome do conceito único |
| JAR | Nova raiz e redirect presentes no artefato instalado |
