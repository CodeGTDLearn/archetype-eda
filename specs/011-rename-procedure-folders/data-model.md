# Data Model: 011-rename-procedure-folders

**Feature**: 011-rename-procedure-folders | **Date**: 2026-11-26

## Visão geral

A feature não introduz dados de aplicação. O modelo abaixo descreve os artefatos do procedimento empacotado, as referências operacionais e as invariantes que a implementação deve preservar.

## Entidades

### PackagedProcedure

Representa o procedimento empacotado `llm-maven-flags-inference/`.

| Campo | Tipo | Descrição |
|---|---|---|
| `root` | caminho | `src/main/resources/llm-maven-flags-inference/` |
| `fileCount` | inteiro | 19 arquivos |
| `markdownCount` | inteiro | 13 arquivos Markdown |
| `jsonCount` | inteiro | 6 arquivos JSON |
| `subfolderCount` | inteiro | 4 subpastas |
| `firstSubfolderFileCount` | inteiro | 2 arquivos na subpasta 01 |
| `secondSubfolderFileCount` | inteiro | 5 arquivos na subpasta 02 |
| `indexPath` | caminho | `01-inicializacao/01-mapa-dos-arquivos-e-ordem-de-leitura.md` após a Fase 1 |

### RenamedFolder

Representa uma das duas subpastas renomeadas.

| Campo | Tipo | Descrição |
|---|---|---|
| `phase` | inteiro | 1 ou 2 |
| `oldName` | texto | `01-entrada-inicializacao` ou `02-operacao-procedimento-do-agente` |
| `newName` | texto | `01-inicializacao` ou `02-procedimento-do-agente` |
| `fileCount` | inteiro | 2 na subpasta 01; 5 na subpasta 02 |
| `role` | texto | Porta de entrada/índice ou operação/procedimento do agente |
| `parent` | caminho | `src/main/resources/llm-maven-flags-inference/` |

### RenamedFile

Representa o arquivo 02 da segunda subpasta.

| Campo | Tipo | Descrição |
|---|---|---|
| `phase` | inteiro | 3 |
| `oldName` | texto | `02-como-localizar-e-ler-o-archetype.md` |
| `newName` | texto | `02-localizar-e-ler-o-archetype.md` |
| `folderAfterPhase2` | texto | `02-procedimento-do-agente` |
| `role` | texto | Localização e leitura do artefato |
| `normativeContentHashBefore` | hash | SHA-256 do conteúdo antes da renomeação |
| `normativeContentHashAfter` | hash | SHA-256 do conteúdo depois da atualização das referências |

### OperationalReference

Representa uma ocorrência de nome antigo no corpus operacional.

| Campo | Tipo | Descrição |
|---|---|---|
| `source` | caminho | Arquivo que contém a referência |
| `line` | inteiro | Linha da ocorrência |
| `kind` | enum | `folder-path`, `file-path`, `short-file`, `anchor`, `legacy-path` |
| `oldToken` | texto | Nome ou caminho antigo |
| `newToken` | texto | Nome ou caminho novo |
| `phase` | inteiro | Fase que aplica a substituição |
| `action` | enum | `update`, `keep-legacy`, `keep-negative-assertion` |

### LegacyRedirect

Representa o arquivo de compatibilidade da porta de entrada.

| Campo | Tipo | Descrição |
|---|---|---|
| `path` | caminho | `src/main/resources/utils/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md` |
| `targetAfterPhase1` | caminho | `llm-maven-flags-inference/01-inicializacao/00-entry-point.md` |
| `renameAllowed` | booleano | `false` — o caminho permanece |

### ReadmeAnchor

Representa uma âncora interna do README afetada pela renomeação.

| Campo | Tipo | Descrição |
|---|---|---|
| `oldId` | texto | `llm-maven-flags-inference-01-entrada-inicializacao` ou `llm-maven-flags-inference-02-operacao-procedimento-do-agente` |
| `newId` | texto | `llm-maven-flags-inference-01-inicializacao` ou `llm-maven-flags-inference-02-procedimento-do-agente` |
| `hrefLine` | inteiro | Linha do índice do README (52 ou 53) |
| `anchorLine` | inteiro | Linha da âncora de seção (806 ou 811) |
| `headingLine` | inteiro | Linha do título da seção (807 ou 812) |
| `phase` | inteiro | 1 ou 2 |

### VerificationSuite

Representa uma suíte existente que lê caminhos fixos do procedimento.

| Campo | Tipo | Descrição |
|---|---|---|
| `file` | caminho | Arquivo `.groovy` em `src/test/archetype-post-generate/` |
| `referenceCount` | inteiro | Ocorrências brutas dos nomes antigos |
| `phase` | inteiro | Fase principal de atualização |
| `keepsLegacyPath` | booleano | `true` quando a referência é ao redirect legado |
| `hasNegativeAssertions` | booleano | `true` quando o nome antigo aparece apenas para provar ausência |

### Phase

Representa uma fase da spec.

| Campo | Tipo | Descrição |
|---|---|---|
| `number` | inteiro | 1, 2 ou 3 |
| `name` | texto | Nome da fase na spec |
| `userStory` | texto | US1, US2 ou US3 |
| `priority` | texto | P1, P2 ou P3 |
| `dependsOn` | texto | `—`, `Fase 1` ou `Fase 2` |
| `tests` | texto | Estratégia de teste da fase |

## Relacionamentos

- `PackagedProcedure` contém 4 subpastas; as duas primeiras são `RenamedFolder`.
- Cada `RenamedFolder` contém arquivos; a subpasta 02 contém o `RenamedFile`.
- `OperationalReference` aponta para `RenamedFolder`, `RenamedFile`, `LegacyRedirect` ou `ReadmeAnchor`.
- `VerificationSuite` valida `PackagedProcedure`, `RenamedFolder`, `RenamedFile` e `LegacyRedirect`.
- `Phase` agrupa as renomeações e as referências que devem ser atualizadas em cada incremento.

## Mapa de renomeação

| Item | Fase | Nome antigo | Nome novo |
|---|---|---|---|
| Subpasta 01 | 1 | `01-entrada-inicializacao` | `01-inicializacao` |
| Subpasta 02 | 2 | `02-operacao-procedimento-do-agente` | `02-procedimento-do-agente` |
| Arquivo 02 | 3 | `02-como-localizar-e-ler-o-archetype.md` | `02-localizar-e-ler-o-archetype.md` |

## Invariantes

- **I1 — Nomes exatos**: os nomes finais são `01-inicializacao`, `02-procedimento-do-agente` e `02-localizar-e-ler-o-archetype.md`.
- **I2 — Contagens**: 19 arquivos, 13 Markdown, 6 JSON, 4 subpastas, 2 arquivos na subpasta 01 e 5 na subpasta 02.
- **I3 — Conteúdo normativo**: o conteúdo dos arquivos renomeados é idêntico ao anterior, exceto pelos tokens de referência aos nomes antigos.
- **I4 — Sem criação/remoção**: nenhum arquivo do procedimento é criado, removido ou movido além das três renomeações.
- **I5 — Ordem e papéis**: o índice mantém a ordem global e o papel de cada arquivo.
- **I6 — Redirect legado**: o caminho `utils/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md` permanece e aponta para `llm-maven-flags-inference/01-inicializacao/00-entry-point.md`.
- **I7 — Zero referências operacionais antigas**: nenhum nome antigo permanece no corpus operacional, exceto o caminho do redirect legado e as asserções negativas das suítes.
- **I8 — Subpastas 03/04**: os nomes e o conteúdo das subpastas 03 e 04 permanecem inalterados.
- **I9 — Sem mudanças de contrato**: catálogo, templates, regras de derivação, ordem de leitura e formato do contrato não mudam.

## Regras de validação

- Cada fase só é aceita quando a contagem de arquivos da subpasta afetada e a contagem global permanecem corretas.
- Cada fase só é aceita quando o nome antigo da subpasta ou do arquivo afetado não existe mais no caminho operacional.
- A Fase 1 não pode renomear a subpasta 02 nem o arquivo 02.
- A Fase 2 não pode renomear o arquivo 02.
- A Fase 3 não pode alterar conteúdo normativo; só tokens de referência.
- O README só é editado nas fases 1 e 2; a Fase 3 não tem referência ao arquivo 02 no README.
- As suítes existentes devem passar ao final de cada fase.

## Transições de estado

```text
Estado inicial
  llm-maven-flags-inference/
  ├── 01-entrada-inicializacao/          (2 arquivos)
  ├── 02-operacao-procedimento-do-agente/ (5 arquivos, inclui 02-como-localizar-e-ler-o-archetype.md)

Após a Fase 1
  llm-maven-flags-inference/
  ├── 01-inicializacao/                  (2 arquivos)
  └── 02-operacao-procedimento-do-agente/ (5 arquivos, inalterada)

Após a Fase 2
  llm-maven-flags-inference/
  ├── 01-inicializacao/
  └── 02-procedimento-do-agente/         (5 arquivos, inclui 02-como-localizar-e-ler-o-archetype.md)

Após a Fase 3
  llm-maven-flags-inference/
  ├── 01-inicializacao/
  └── 02-procedimento-do-agente/         (5 arquivos, inclui 02-localizar-e-ler-o-archetype.md)
```
