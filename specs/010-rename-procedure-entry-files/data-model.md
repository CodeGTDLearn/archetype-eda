# Data Model: Renomear os Arquivos de Entrada e Operação do Procedimento

**Feature**: 010-rename-procedure-entry-files | **Date**: 2026-11-25

## Visão geral

A feature é nominal e documental. O modelo descreve os arquivos renomeados, as subpastas do procedimento, as referências operacionais, o redirect legado, as suítes de verificação e os invariantes que precisam permanecer verdadeiros em cada fase.

## Entidades

### RenamedFile

Representa um dos sete arquivos do procedimento que mudam de nome.

| Campo | Descrição | Exemplo |
|---|---|---|
| `oldName` | Nome atual do arquivo | `prompt-00-leia-este-arquivo-primeiro.md` |
| `newName` | Nome novo exato | `00-entry-point.md` |
| `subfolder` | Subpasta do procedimento | `01-entrada-inicializacao` |
| `phase` | Fase que aplica a renomeação | `1` ou `2` |
| `role` | Papel no procedimento | porta de entrada, índice, papel do agente etc. |
| `oldPath` | Caminho relativo ao procedimento | `01-entrada-inicializacao/prompt-00...` |
| `newPath` | Caminho relativo ao procedimento | `01-entrada-inicializacao/00-entry-point.md` |
| `baselineHash` | SHA-256 do conteúdo antes da mudança | hash de 64 caracteres |
| `normativeContentPreserved` | Conteúdo normativo idêntico, exceto tokens de referência | `true` |

**Relações**: pertence a uma `ProcedureSubfolder`; é alvo de `OperationalReference`; é verificado por `VerificationSuite`.

### ProcedureSubfolder

Representa as duas subpastas afetadas.

| Campo | Descrição | Exemplo |
|---|---|---|
| `name` | Nome fixo da subpasta | `01-entrada-inicializacao` |
| `expectedCount` | Quantidade de arquivos que deve permanecer | `2` ou `5` |
| `phase` | Fase que renomeia seus arquivos | `1` ou `2` |
| `files` | Lista de `RenamedFile` | 2 ou 5 arquivos |

**Invariantes**: o nome da subpasta não muda; a contagem não muda; nenhum arquivo é criado ou removido.

### PackagedProcedure

Representa o procedimento completo empacotado.

| Campo | Descrição | Valor |
|---|---|---|
| `root` | Raiz do procedimento | `src/main/resources/llm-maven-flags-inference` |
| `totalFiles` | Total de arquivos | `19` |
| `markdownFiles` | Arquivos Markdown | `13` |
| `jsonFiles` | Arquivos JSON | `6` |
| `subfolders` | Subpastas de primeiro nível | `4` |
| `filesIn01` | Arquivos em 01 | `2` |
| `filesIn02` | Arquivos em 02 | `5` |
| `indexPath` | Caminho do índice | `01-entrada-inicializacao/01-mapa-dos-arquivos-e-ordem-de-leitura.md` |
| `entryPath` | Caminho da porta de entrada | `01-entrada-inicializacao/00-entry-point.md` |

**Invariantes**: 19/13/6/4 e 2/5 permanecem verdadeiros em todas as fases; o índice lista os 19 arquivos; o catálogo tem 35 propriedades e 18 filesets; `archetype-resources/` não contém o procedimento.

### OperationalReference

Representa uma ocorrência de um nome antigo que precisa ser atualizada.

| Campo | Descrição | Exemplo |
|---|---|---|
| `file` | Arquivo que contém a referência | `README.md` |
| `line` | Linha da ocorrência | `604` |
| `oldToken` | Token antigo | `prompt-00` |
| `newToken` | Token novo | `00-entry-point` |
| `kind` | Tipo da referência | caminho completo, token curto, template de spec, teste |
| `phase` | Fase que aplica a atualização | `1` ou `2` |
| `exception` | Exceção de compatibilidade | `legacy-redirect` ou `negative-test` |

**Categorias**: tabela do índice, ordem global, registro de paradas, caminho completo, token curto, template de spec do README, link do README, redirect legado, caminho de teste, rótulo de teste.

### LegacyRedirect

Representa o único arquivo que mantém um nome antigo.

| Campo | Descrição | Valor |
|---|---|---|
| `path` | Caminho fixo | `src/main/resources/utils/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md` |
| `target` | Destino do redirect | `llm-maven-flags-inference/01-entrada-inicializacao/00-entry-point.md` |
| `keptForCompatibility` | Motivo | specs anteriores que apontam para o caminho antigo |
| `additionalRedirects` | Redirects para os outros seis arquivos | `0` |

**Invariantes**: o redirect existe, permanece no caminho antigo, aponta para o novo nome e não contém cópia de outro arquivo do procedimento.

### VerificationSuite

Representa uma suíte que valida a renomeação.

| Campo | Descrição | Exemplo |
|---|---|---|
| `path` | Caminho da suíte | `src/test/archetype-post-generate/BuildArtifactsTest.groovy` |
| `phase` | Fase em que passa a valer | `1` ou `2` |
| `responsibility` | O que valida | nomes, contagem, índice, redirect, anti-drift |
| `updated` | Se precisa de atualização | `true` |
| `new` | Se é uma suíte nova | `true` apenas para `ProcedureEntryRenameContractTest.groovy` |

**Suítes existentes atualizadas**: `BuildArtifactsTest`, `PathRenameContractTest`, `HardStopContractTest`, `DestinationContractTest`, `CatalogSyncContractTest`, `ReadmeConsistencyTest`, `FileRenameContractTest`.

**Suíte nova**: `ProcedureEntryRenameContractTest.groovy`, phase-aware, registrada no runner.

### Phase

Representa uma fase do plano.

| Campo | Descrição | Fase 1 | Fase 2 |
|---|---|---|---|
| `number` | Número da fase | `1` | `2` |
| `name` | Nome da fase | Subpasta de entrada e inicialização | Subpasta de operação e procedimento do agente |
| `userStory` | User story associada | `US1` | `US2` |
| `priority` | Prioridade | `P1` | `P2` |
| `dependsOn` | Dependência | — | Fase 1 (conferência final) |
| `tests` | Estratégia de teste | Atualizar suítes existentes; nova suíte | Atualizar suítes existentes; varredura final |
| `exitCriteria` | Critério de saída | 01 com 2 nomes novos; 02 com 5 nomes antigos; referências de 01 atualizadas; testes passam | 02 com 5 nomes novos; 7/7 nomes; 0 referências antigas; contagens 19/13/6/4; testes passam |

## Transições de estado

```text
Baseline
  ├── 19 arquivos; 13 Markdown; 6 JSON; 4 subpastas; 2 em 01; 5 em 02
  ├── 7 nomes antigos; 7 hashes de baseline
  └── redirect legado no caminho antigo
        │
        ▼
Fase 1 — 01 renomeada
  ├── 00-entry-point.md e 01-mapa-dos-arquivos-e-ordem-de-leitura.md existem
  ├── 02 ainda com os cinco nomes antigos
  ├── referências a entrada/índice atualizadas no procedimento, README, redirect e suítes
  ├── índice lista os novos caminhos de 01 e os antigos de 02
  └── checkpoint: `mvn -B test` passa
        │
        ▼
Fase 2 — 02 renomeada
  ├── os cinco arquivos de 02 com os nomes novos
  ├── índice lista os 19 caminhos novos
  ├── referências aos cinco arquivos atualizadas no procedimento, README e suítes
  ├── 0 referências antigas no corpus operacional, exceto redirect legado e asserções negativas
  ├── 19/13/6/4 e 2/5 preservados
  └── `mvn -B test`, `mvn clean install`, `mvn -B clean integration-test` passam
        │
        ▼
Final
  ├── 7/7 nomes novos
  ├── 0 nomes antigos como arquivo nas duas subpastas
  ├── redirect legado funcional
  ├── nenhum arquivo criado, removido ou movido no procedimento
  └── verificação opt-in do JAR instalado (não gate)
```

## Invariantes

- **I1 — Nomes exatos**: os sete nomes novos são exatamente os definidos na spec.
- **I2 — Contagem**: 19 arquivos, 13 Markdown, 6 JSON, 4 subpastas, 2 em 01 e 5 em 02.
- **I3 — Subpastas**: `01-entrada-inicializacao` e `02-operacao-procedimento-do-agente` mantêm os nomes.
- **I4 — Conteúdo normativo**: regras, definições, exemplos, schema, metadados, defaults e formato do contrato não mudam; só tokens de referência mudam.
- **I5 — Índice**: lista os 19 arquivos e permanece a fonte única da ordem global.
- **I6 — Redirect**: único no caminho antigo da entrada, aponta para `00-entry-point.md`, sem redirects para os outros seis.
- **I7 — Referências**: 0 referências antigas no corpus operacional, exceto o caminho do redirect legado e as asserções negativas das suítes.
- **I8 — Fases**: cada fase é implementável, testável e demonstrável isoladamente; a Fase 2 depende da Fase 1 apenas para a conferência final.
- **I9 — Catálogo**: 35 propriedades e 18 filesets, sem alteração.
- **I10 — Fora de escopo**: specs históricas, planos/tarefas, `target/`, `graphify-out/` e artefato instalado não são reescritos como parte da implementação.
