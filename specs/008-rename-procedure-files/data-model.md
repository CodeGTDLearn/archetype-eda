# Data Model: Renomear Arquivos das Subpastas 01 e 02 em Três Fases

**Feature**: `008-rename-procedure-files` | **Date**: 2026-11-25

## Entidades

### RenameOperation

Representa um único rename de arquivo em uma das fases de renomeação.

| Campo | Tipo | Regra |
|---|---|---|
| `phase` | inteiro (1 ou 2) | Fase dona do rename |
| `sourcePath` | caminho relativo | MUST existir antes da operação |
| `targetPath` | caminho relativo | MUST NOT existir; mesmo diretório-pai de `sourcePath` |
| `sourceHash` | SHA-256 | Capturado antes do rename |
| `targetHash` | SHA-256 | Calculado depois; MUST ser igual a `sourceHash` |
| `status` | `pending` / `renamed` / `verified` / `failed` | `verified` exige hash igual e listagem correta |

### FolderSnapshot

Estado de uma subpasta em um instante, para provar que ela não foi tocada durante a fase.

| Campo | Tipo | Regra |
|---|---|---|
| `folderPath` | caminho relativo | Subpasta 01, 02, 03 ou 04 |
| `capturedAt` | data/hora | Imediatamente antes da fase |
| `files` | lista de `{name, sha256}` | Ordem alfabética |
| `purpose` | `target` / `untouched` | Pasta da fase ou pasta de controle |

### PhaseManifest

Agrupa operações e snapshots de uma fase.

| Campo | Tipo | Regra |
|---|---|---|
| `phase` | inteiro (1 ou 2) | Fase correspondente |
| `capturedAt` | data/hora | Início da fase |
| `operations` | lista de `RenameOperation` | 2 na Fase 1; 5 na Fase 2 |
| `untouchedFolders` | lista de `FolderSnapshot` | Subpastas fora da fase |
| `location` | caminho temporário | Fora do repositório |

### RenameMap

Mapa estático dos sete renomes.

| Fase | De | Para |
|---|---|---|
| 1 | `01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md` | `01-entrada-inicializacao/01-entryPoint.md` |
| 1 | `01-entrada-inicializacao/indice-01-mapa-dos-arquivos-e-ordem-de-leitura.md` | `01-entrada-inicializacao/02-mapa-dos-arquivos-e-ordem-de-leitura.md` |
| 2 | `02-operacao-procedimento-do-agente/prompt-01-papel-e-restricoes-do-agente.md` | `02-operacao-procedimento-do-agente/01-papel-e-restricoes-do-agente.md` |
| 2 | `02-operacao-procedimento-do-agente/prompt-02-como-localizar-e-ler-o-archetype.md` | `02-operacao-procedimento-do-agente/02-como-localizar-e-ler-o-archetype.md` |
| 2 | `02-operacao-procedimento-do-agente/prompt-03-regras-obrigatorias-de-geracao.md` | `02-operacao-procedimento-do-agente/03-regras-obrigatorias-de-geracao.md` |
| 2 | `02-operacao-procedimento-do-agente/prompt-04-fluxo-de-execucao-e-verificacao.md` | `02-operacao-procedimento-do-agente/04-fluxo-de-execucao-e-verificacao.md` |
| 2 | `02-operacao-procedimento-do-agente/prompt-05-entregaveis-e-relatorios.md` | `02-operacao-procedimento-do-agente/05-entregaveis-e-relatorios.md` |

### ReferenceOccurrence

Uma ocorrência de nome antigo em uma superfície operacional em escopo.

| Campo | Tipo | Regra |
|---|---|---|
| `filePath` | caminho relativo | Um dos 13 arquivos do inventário |
| `line` | inteiro | Linha da ocorrência |
| `oldToken` | texto | Um dos 14 tokens da tabela de substituição |
| `newToken` | texto | Token correspondente |
| `surface` | `README` / `procedimento` / `redirect` / `suíte` | Classificação da superfície |

### DeferredReference

Referência antiga fora do escopo desta feature.

| Campo | Tipo | Regra |
|---|---|---|
| `filePath` | caminho relativo | `regras-01` ou `regras-04` |
| `occurrences` | inteiro | 1 em `regras-01`; 2 em `regras-04` |
| `reason` | texto | Subpastas 03 e 04 totalmente intactas |
| `futureFeature` | texto | Feature futura de referências residuais |

### ReferenceUpdate

Resultado da Fase 3.

| Campo | Tipo | Regra |
|---|---|---|
| `phase` | 3 | Fase de referências |
| `totalOccurrences` | 112 | Inventário em escopo |
| `totalFiles` | 13 | Arquivos operacionais em escopo |
| `deferredOccurrences` | 3 | `regras-01` e `regras-04` |
| `deferredFiles` | 2 | Subpasta 03 |
| `remainingOldTokens` | 0 | Nas 13 superfícies em escopo |
| `buildStatus` | `green` / `red` | `mvn -B test` após a Fase 3 |
| `status` | `pending` / `updated` / `verified` / `failed` | `verified` exige 0 tokens em escopo e build verde |

## Regras de validação

- **V-001**: Origem existe; destino não existe; mesmo diretório-pai.
- **V-002**: Após o rename, hash do destino igual ao hash da origem.
- **V-003**: Após a Fase 1, a subpasta 01 tem exatamente 2 arquivos e 0 nomes antigos.
- **V-004**: Após a Fase 2, a subpasta 02 tem exatamente 5 arquivos, 0 com prefixo `prompt-`.
- **V-005**: As subpastas de controle mantêm arquivos e hashes do snapshot anterior à fase.
- **V-006**: O procedimento mantém 19 arquivos.
- **V-007**: Após a Fase 3, 0 ocorrências dos 14 tokens antigos nas 13 superfícies em escopo.
- **V-008**: A Fase 3 altera somente linhas de referência; regras e textos normativos ficam intactos.
- **V-009**: As subpastas 03 e 04 permanecem totalmente intactas, inclusive as 3 referências antigas de `regras-01` e `regras-04`.
- **V-010**: As seis suítes são atualizadas e `mvn -B test` fica verde.
- **V-011**: `specs/003` a `specs/007`, `target/` e `graphify-out/` permanecem inalterados.
- **V-012**: Nenhum alias, redirect ou cópia com nome antigo é criado.
- **V-013**: A Fase 3 depende das Fases 1 e 2.
- **V-014**: Identificadores de variável (`prompt00Path`, `prompt03`) não são referências operacionais e ficam fora do critério.
- **V-015**: As 3 referências diferidas em 03 não entram no critério de 0 tokens.

## Transições de estado

```text
Fase 1
pending → renamed (entrada) → renamed (mapa) → verified
                                      ↘ failed (reverter renomes já feitos)

Fase 2
pending → renamed (5 arquivos) → verified
                              ↘ failed (reverter renomes já feitos)

Fase 3
pending → updated (112 ocorrências) → verified (0 tokens em escopo + mvn -B test verde)
                                   ↘ failed (corrigir referências restantes)
```

Regras:

- `verified` exige as validações da fase.
- `failed` interrompe a fase; os renomes já feitos são revertidos manualmente.
- Reexecutar fase já verificada é idempotente: destino existe, origem não; reporta “já aplicada”.

## Invariantes

- 19 arquivos no procedimento antes e depois de qualquer fase.
- Subpasta 01: 2 arquivos após a Fase 1.
- Subpasta 02: 5 arquivos após a Fase 2.
- Subpastas 03 e 04: mesmos arquivos, mesmos hashes e mesmas referências em todas as fases.
- Conteúdo dos 7 arquivos renomeados: idêntico nas Fases 1 e 2.
- Fase 3: somente referências de caminho e nome nas 13 superfícies em escopo.
- Após a Fase 3: 0 tokens antigos nas 13 superfícies em escopo, 3 tokens diferidos em 03/04 e `mvn -B test` verde.
