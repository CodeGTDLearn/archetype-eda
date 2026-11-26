# Rename Manifest: 011-rename-procedure-folders

**Feature**: 011-rename-procedure-folders | **Date**: 2026-11-26

**Purpose**: fonte única do mapa `old → new`, do mapa de tokens, do inventário de referências por arquivo/linha/fase e das exceções. A implementação consome este manifesto; as suítes existentes validam o resultado.

## 1. Mapa de renomeação

| Fase | Tipo | Nome antigo | Nome novo | Papel |
|---|---|---|---|---|
| 1 | Subpasta | `01-entrada-inicializacao` | `01-inicializacao` | Porta de entrada e índice |
| 2 | Subpasta | `02-operacao-procedimento-do-agente` | `02-procedimento-do-agente` | Operação e procedimento do agente |
| 3 | Arquivo | `02-como-localizar-e-ler-o-archetype.md` | `02-localizar-e-ler-o-archetype.md` | Localização e leitura do artefato |

**Raiz do procedimento**: `src/main/resources/llm-maven-flags-inference/`.

## 2. Mapa de tokens

| Token antigo | Token novo | Escopo | Fase |
|---|---|---|---|
| `01-entrada-inicializacao` | `01-inicializacao` | Nome de pasta e segmento de caminho | 1 |
| `llm-maven-flags-inference-01-entrada-inicializacao` | `llm-maven-flags-inference-01-inicializacao` | Âncora do README | 1 |
| `02-operacao-procedimento-do-agente` | `02-procedimento-do-agente` | Nome de pasta e segmento de caminho | 2 |
| `llm-maven-flags-inference-02-operacao-procedimento-do-agente` | `llm-maven-flags-inference-02-procedimento-do-agente` | Âncora do README | 2 |
| `02-como-localizar-e-ler-o-archetype` | `02-localizar-e-ler-o-archetype` | Nome de arquivo e forma curta | 3 |
| `02-como-localizar-e-ler-o-archetype.md` | `02-localizar-e-ler-o-archetype.md` | Nome completo do arquivo | 3 |

## 3. Inventário de referências

**Total bruto**: 92 linhas correspondentes — 21 no procedimento, 17 no README, 1 no redirect legado e 53 nas suítes. Nem todas são referências operacionais a atualizar; as exceções estão na seção 3.5.

### 3.1 Procedimento

#### `01-entrada-inicializacao/00-entry-point.md`

| Linhas | Token antigo | Token novo | Fase | Observação |
|---|---|---|---|---|
| 168, 170, 171, 172 | `02-como-localizar-e-ler-o-archetype` | `02-localizar-e-ler-o-archetype` | 3 | Registro de paradas; forma curta |
| 620 | `.../01-entrada-inicializacao/01-mapa-dos-arquivos-e-ordem-de-leitura.md` | `.../01-inicializacao/01-mapa-dos-arquivos-e-ordem-de-leitura.md` | 1 | Caminho do índice |
| 708, 709 | `.../01-entrada-inicializacao/...` | `.../01-inicializacao/...` | 1 | Lista de integridade |
| 710, 712, 713, 714 | `.../02-operacao-procedimento-do-agente/...` | `.../02-procedimento-do-agente/...` | 2 | Lista de integridade |
| 711 | `.../02-operacao-procedimento-do-agente/02-como-localizar-e-ler-o-archetype.md` | `.../02-procedimento-do-agente/02-localizar-e-ler-o-archetype.md` | 2 + 3 | Linha composta: pasta na Fase 2; arquivo na Fase 3 |

#### `01-entrada-inicializacao/01-mapa-dos-arquivos-e-ordem-de-leitura.md`

| Linhas | Token antigo | Token novo | Fase | Observação |
|---|---|---|---|---|
| 11, 12 | `.../01-entrada-inicializacao/...` | `.../01-inicializacao/...` | 1 | Inclui o caminho do próprio índice |
| 13, 15, 16, 17 | `.../02-operacao-procedimento-do-agente/...` | `.../02-procedimento-do-agente/...` | 2 | Tabela de arquivos |
| 14 | `.../02-operacao-procedimento-do-agente/02-como-localizar-e-ler-o-archetype.md` | `.../02-procedimento-do-agente/02-localizar-e-ler-o-archetype.md` | 2 + 3 | Linha composta: pasta na Fase 2; arquivo na Fase 3 |
| 33, 47 | `02-como-localizar-e-ler-o-archetype` | `02-localizar-e-ler-o-archetype` | 3 | Forma curta |

### 3.2 README

| Linhas | Token antigo | Token novo | Fase | Observação |
|---|---|---|---|---|
| 52 | `llm-maven-flags-inference-01-entrada-inicializacao` | `llm-maven-flags-inference-01-inicializacao` | 1 | `href` do índice |
| 603 | `01-entrada-inicializacao/` | `01-inicializacao/` | 1 | Lista de composição |
| 652 | `01-entrada-inicializacao/` | `01-inicializacao/` | 1 | Tabela de artefatos |
| 806, 807 | `llm-maven-flags-inference-01-entrada-inicializacao` / `01-entrada-inicializacao` | `llm-maven-flags-inference-01-inicializacao` / `01-inicializacao` | 1 | `id`, `name` e título da seção |
| 995 | `llm-maven-flags-inference/01-entrada-inicializacao/00-entry-point.md` | `llm-maven-flags-inference/01-inicializacao/00-entry-point.md` | 1 | Caminho da entrada |
| 2111, 2152, 2207, 2513, 2567, 2639, 2704 | `llm-maven-flags-inference/01-entrada-inicializacao/00-entry-point.md` | `llm-maven-flags-inference/01-inicializacao/00-entry-point.md` | 1 | Caminhos de entrada |
| 53 | `llm-maven-flags-inference-02-operacao-procedimento-do-agente` | `llm-maven-flags-inference-02-procedimento-do-agente` | 2 | `href` do índice |
| 605 | `02-operacao-procedimento-do-agente/` | `02-procedimento-do-agente/` | 2 | Lista de composição |
| 652 | `02-operacao-procedimento-do-agente/` | `02-procedimento-do-agente/` | 2 | Tabela de artefatos |
| 811, 812 | `llm-maven-flags-inference-02-operacao-procedimento-do-agente` / `02-operacao-procedimento-do-agente` | `llm-maven-flags-inference-02-procedimento-do-agente` / `02-procedimento-do-agente` | 2 | `id`, `name` e título da seção |

**README na Fase 3**: nenhuma referência ao nome do arquivo 02 foi encontrada; nenhuma edição é necessária.

### 3.3 Redirect legado

| Arquivo | Linha | Ação | Fase |
|---|---:|---|---|
| `src/main/resources/utils/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md` | 7 | Atualizar o destino para `llm-maven-flags-inference/01-inicializacao/00-entry-point.md`; manter o caminho do redirect | 1 |

### 3.4 Suítes de verificação

| Suíte | Linhas | Fase | Ação |
|---|---|---|---|
| `BuildArtifactsTest.groovy` | 79, 82, 83, 89, 90, 91, 92, 93, 94, 95 | 1 + 2 + 3 | Atualizar a lista de pastas/arquivos e o caminho do índice |
| `CatalogSyncContractTest.groovy` | 163 | 2 | Atualizar o caminho da subpasta 02 |
| `DestinationContractTest.groovy` | 36, 37, 38, 39, 134 | 1 + 2 | Atualizar caminhos de entrada, 02, 03 e 04 e a entrada do ZIP |
| `FileRenameContractTest.groovy` | 16, 125 | 1 + 2 | Atualizar o caminho do índice e a lista de subpastas |
| `HardStopContractTest.groovy` | 9, 132 | 1 | Atualizar o caminho da porta de entrada e a entrada do ZIP |
| `PathRenameContractTest.groovy` | 41, 42, 48, 49, 50, 51, 52, 53, 54, 69, 70 | 1 + 2 + 3 | Atualizar a lista de pastas, a lista de arquivos e os caminhos canônico/relativo; **manter** as linhas 71 e 116 (redirect legado) |
| `ProcedureEntryRenameContractTest.groovy` | 27, 34, 49, 51, 52, 70, 94, 96, 102, 103, 104, 108, 109, 112, 115, 131 | 1 + 2 + 3 | Atualizar as expectativas phase-aware para as pastas e o arquivo novos; **manter** a linha 16 (caminho legado) e a linha 136 (checagem da pasta legada); asserções negativas dos nomes antigos podem permanecer |
| `ReadmeConsistencyTest.groovy` | 157, 237 | 1 + 2 | Atualizar o caminho da entrada e o caminho da subpasta 02 |

### 3.5 Exceções

- **Redirect legado**: o caminho `utils/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md` permanece; apenas o destino muda.
- **Asserções negativas**: ocorrências de nomes antigos em suítes que provam a ausência podem permanecer, desde que não sejam caminho operacional.
- **Specs históricas**: `specs/*` não são reescritas.
- **Saída gerada**: `graphify-out/` e `target/` não são editados.
- **Subpastas 03/04**: nomes e conteúdo permanecem inalterados.

## 4. Critérios de aceite do manifesto

- 3 de 3 itens renomeados; 0 nomes antigos de pasta/arquivo no caminho operacional.
- 19 arquivos, 13 Markdown, 6 JSON, 4 subpastas, 2 na subpasta 01 e 5 na subpasta 02.
- 0 arquivos novos, removidos ou movidos no procedimento.
- 0 mudanças em regras, catálogo, templates, ordem de leitura ou formato do contrato.
- Redirect legado funcional e no caminho atual.
- 100% das referências operacionais atualizadas; 0 referências quebradas no índice, na porta de entrada, no README, no redirect e nas suítes.

## 5. Checklist por fase

### Fase 1 — Subpasta de inicialização

- [ ] Renomear `01-entrada-inicializacao` → `01-inicializacao`.
- [ ] Atualizar `00-entry-point.md` linhas 620, 708 e 709.
- [ ] Atualizar o índice linhas 11 e 12.
- [ ] Atualizar o README linhas 52, 603, 652 (parte 01), 806, 807, 995, 2111, 2152, 2207, 2513, 2567, 2639 e 2704.
- [ ] Atualizar o redirect legado linha 7.
- [ ] Atualizar as suítes com referências à subpasta 01.
- [ ] Validar 2 arquivos, 19/13/6/4, redirect e `mvn -B test`.

### Fase 2 — Subpasta de procedimento do agente

- [ ] Renomear `02-operacao-procedimento-do-agente` → `02-procedimento-do-agente`.
- [ ] Atualizar `00-entry-point.md` linhas 710, 712, 713 e 714; a linha 711 recebe a pasta nova.
- [ ] Atualizar o índice linhas 13 a 17; a linha 14 recebe a pasta nova.
- [ ] Atualizar o README linhas 53, 605, 652 (parte 02), 811 e 812.
- [ ] Atualizar as suítes com referências à subpasta 02.
- [ ] Validar 5 arquivos, 19/13/6/4, ordem do índice e `mvn -B test`.

### Fase 3 — Arquivo de localização e leitura do archetype

- [ ] Renomear `02-como-localizar-e-ler-o-archetype.md` → `02-localizar-e-ler-o-archetype.md`.
- [ ] Atualizar `00-entry-point.md` linhas 168, 170, 171, 172 e 711.
- [ ] Atualizar o índice linhas 14, 33 e 47.
- [ ] Atualizar as suítes com referências ao arquivo 02.
- [ ] Confirmar que o README não tem referência ao arquivo 02.
- [ ] Executar a varredura final e `mvn -B test`, `mvn -B clean install` e `mvn -B clean integration-test`.
