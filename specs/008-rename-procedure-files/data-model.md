# Data Model: Renomear Arquivos das Subpastas 01 e 02 em Duas Fases

**Feature**: `008-rename-procedure-files` | **Date**: 2026-11-25

## Entidades

### RenameOperation

Representa um único rename de arquivo dentro de uma fase.

| Campo | Tipo | Regra |
|---|---|---|
| `phase` | inteiro (1 ou 2) | Fase dona do rename |
| `sourcePath` | caminho relativo ao repositório | MUST existir antes da operação |
| `targetPath` | caminho relativo ao repositório | MUST NOT existir antes da operação; MUST ter o mesmo diretório-pai de `sourcePath` |
| `sourceHash` | SHA-256 hexadecimal | Capturado antes do rename |
| `targetHash` | SHA-256 hexadecimal | Calculado depois do rename; MUST ser igual a `sourceHash` |
| `status` | `pending` / `renamed` / `verified` / `failed` | `verified` só com hash igual e listagem correta |

### FolderSnapshot

Estado de uma subpasta em um instante, usado para provar que ela não foi tocada durante a fase.

| Campo | Tipo | Regra |
|---|---|---|
| `folderPath` | caminho relativo | Subpasta 01, 02, 03 ou 04 |
| `capturedAt` | data/hora | Momento imediatamente anterior à fase |
| `files` | lista de `{ name, sha256 }` | Um item por arquivo, em ordem alfabética |
| `purpose` | `target` ou `untouched` | Diferencia a pasta da fase das pastas de controle |

### PhaseManifest

Agrupa as operações e os snapshots de uma fase.

| Campo | Tipo | Regra |
|---|---|---|
| `phase` | inteiro (1 ou 2) | Fase correspondente |
| `capturedAt` | data/hora | Início da fase |
| `operations` | lista de `RenameOperation` | 2 na Fase 1; 5 na Fase 2 |
| `untouchedFolders` | lista de `FolderSnapshot` | Todas as subpastas do procedimento fora da fase |
| `location` | caminho temporário | Fora do repositório |

### RenameMap

Mapa estático dos sete renomes, derivado da spec.

| Fase | De | Para |
|---|---|---|
| 1 | `01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md` | `01-entrada-inicializacao/01-entryPoint.md` |
| 1 | `01-entrada-inicializacao/indice-01-mapa-dos-arquivos-e-ordem-de-leitura.md` | `01-entrada-inicializacao/02-mapa-dos-arquivos-e-ordem-de-leitura.md` |
| 2 | `02-operacao-procedimento-do-agente/prompt-01-papel-e-restricoes-do-agente.md` | `02-operacao-procedimento-do-agente/01-papel-e-restricoes-do-agente.md` |
| 2 | `02-operacao-procedimento-do-agente/prompt-02-como-localizar-e-ler-o-archetype.md` | `02-operacao-procedimento-do-agente/02-como-localizar-e-ler-o-archetype.md` |
| 2 | `02-operacao-procedimento-do-agente/prompt-03-regras-obrigatorias-de-geracao.md` | `02-operacao-procedimento-do-agente/03-regras-obrigatorias-de-geracao.md` |
| 2 | `02-operacao-procedimento-do-agente/prompt-04-fluxo-de-execucao-e-verificacao.md` | `02-operacao-procedimento-do-agente/04-fluxo-de-execucao-e-verificacao.md` |
| 2 | `02-operacao-procedimento-do-agente/prompt-05-entregaveis-e-relatorios.md` | `02-operacao-procedimento-do-agente/05-entregaveis-e-relatorios.md` |

> O contrato em `contracts/file-rename-contract.md` é a fonte canônica dos caminhos.

## Regras de validação

- **V-001**: Cada `sourcePath` existe antes da fase e cada `targetPath` não existe.
- **V-002**: `sourcePath` e `targetPath` têm o mesmo diretório-pai.
- **V-003**: Após a fase, cada `targetPath` existe e tem hash igual a `sourceHash`.
- **V-004**: Após a Fase 1, a subpasta 01 tem exatamente 2 arquivos e nenhum nome antigo.
- **V-005**: Após a Fase 2, a subpasta 02 tem exatamente 5 arquivos, nenhum começando com `prompt-`.
- **V-006**: As subpastas de controle têm exatamente os mesmos arquivos e hashes do snapshot capturado antes da fase.
- **V-007**: O total de arquivos do procedimento permanece 19.
- **V-008**: Nenhum alias, redirect ou cópia com nome antigo é criado.
- **V-009**: README, índice, fixtures, suítes e `utils/` permanecem sem alteração.

## Transições de estado

```text
Fase 1
pending → renamed (entrada) → renamed (mapa) → verified
                                      ↘ failed (parada; reverter os renomes já feitos)

Fase 2
pending → renamed (5 arquivos) → verified
                              ↘ failed (parada; reverter os renomes já feitos)
```

Regras de transição:

- `verified` exige V-001 a V-007.
- `failed` interrompe a fase; os renomes já concluídos são revertidos manualmente antes de nova tentativa.
- Reexecutar uma fase já verificada é idempotente: os nomes de destino existem, os de origem não; a fase reporta "já aplicada" e não cria duplicata.

## Invariantes

- 19 arquivos no procedimento antes e depois de qualquer fase.
- Subpasta 01: 2 arquivos após a Fase 1.
- Subpasta 02: 5 arquivos após a Fase 2.
- Subpastas 03 e 04: mesmos arquivos e hashes em qualquer fase.
- Conteúdo dos 7 arquivos renomeados: idêntico ao original.
- Nenhum caminho antigo sobrevive como alias, cópia ou redirect dentro de `llm-maven-flags-inference/`.
