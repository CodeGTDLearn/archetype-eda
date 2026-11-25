# Implementation Plan: Renomear Arquivos das Subpastas 01 e 02 em Duas Fases

**Branch**: `008-rename-procedure-files` | **Date**: 2026-11-25 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/008-rename-procedure-files/spec.md`

## Summary

Renomear sete arquivos do procedimento empacotado, em duas fases independentes: a Fase 1 renomeia a porta de entrada e o mapa da subpasta `01-entrada-inicializacao`; a Fase 2 remove o prefixo `prompt-` dos cinco arquivos da subpasta `02-operacao-procedimento-do-agente`. O conteúdo dos arquivos permanece byte a byte idêntico, as subpastas 03 e 04 não são tocadas e nenhuma referência cruzada, alias ou redirect é criado ou atualizado. Como a spec proíbe atualizar as suítes que travam os caminhos antigos, a aceitação é feita por inspeção de diretório e comparação de hash SHA-256; as suítes de contrato ficam vermelhas por consequência conhecida até uma feature posterior de atualização de referências. Nenhuma dependência nova, nenhum código de produção e nenhum arquivo novo no procedimento.

## Technical Context

**Language/Version**: Markdown no conteúdo do procedimento; Groovy 4.0.28 nas suítes de contrato; build declarado com Java 17 e Maven 3.9.16; CI usa JDK 21 e o `pom.xml` não fixa `maven.compiler.release`.

**Primary Dependencies**: Maven Archetype Plugin 3.4.1; `archetype-packaging` 3.4.1; `xml-maven-plugin` 1.2.1; `groovy-maven-plugin` 2.1.1; `groovy-json` 4.0.28 (teste); JUnit 4.13.2 (teste); `velocity-engine-core` 2.4.1 (teste). Nenhuma dependência nova.

**Storage**: Sistema de arquivos e JAR; 19 arquivos no procedimento (13 Markdown e 6 JSON) em 4 subpastas; catálogo XML com 35 propriedades e 18 filesets; sem banco de dados.

**Testing**: Suítes Groovy em `src/test/archetype-post-generate/`, executadas pelo runner `src/test/groovy/run-archetype-post-generate-tests.groovy` na fase `test`. `mvn -B test` NÃO é o gate desta feature: as suítes `PathRenameContractTest`, `BuildArtifactsTest`, `HardStopContractTest`, `DestinationContractTest`, `ReadmeConsistencyTest` e `CatalogSyncContractTest` resolvem os nomes antigos e falharão por consequência conhecida. A aceitação usa listagem de diretório, hashes SHA-256 e, opcionalmente, `mvn -B clean process-resources` para conferir `target/classes`.

**Target Platform**: JAR consumido por agentes e operadores; Windows e POSIX; CI em `windows-latest`.

**Project Type**: Maven archetype / promptware — documentação executável empacotada mais suítes de contrato.

**Performance Goals**: Nenhuma meta específica nesta feature; o baseline do projeto é aumento de no máximo 10% no build de `mvn -B test` sobre a linha de base local. A feature não altera runtime nem build.

**Constraints**: Manter 19 arquivos e 4 subpastas; alterar apenas as subpastas 01 e 02; preservar o conteúdo dos 7 arquivos; não criar aliases, redirects ou cópias; não atualizar referências cruzadas em README, índice, fixtures ou verificações existentes; não editar `target/` manualmente; o redirect legado em `src/main/resources/utils/` permanece intocado e quebrado por decisão de escopo.

**Scale/Scope**: 7 renomes — 2 na subpasta 01 e 5 na subpasta 02; 2 fases; 19 arquivos no total.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Princípio | Gate aplicável | Status |
|---|---|---|
| I. Modularidade por Chave de Ativação | Nenhuma flag, capacidade opcional ou comportamento condicional é criado; a mudança é apenas de caminho de arquivo | PASS |
| II. Convenção Fixa de Nomenclatura para Artefatos de Extensão | A feature não cria artefato de extensão do archetype; os 19 arquivos e as 4 subpastas do procedimento permanecem, e a montagem do JAR copia `src/main/resources` sem selecionar o procedimento por padrão de nome | PASS |
| III. Pacote por Funcionalidade no Código Gerado | Nenhum código Java gerado ou template de entidade é alterado | PASS |
| IV. Binding Duplo de Configuração Externalizada | Nenhuma propriedade externalizada, variável de binding ou configuração é criada ou alterada | PASS |
| V. Credenciais Nunca Entram no Repositório | A feature não lê nem grava credenciais; nenhum valor sensível é manipulado | PASS |
| VI. Validar Antes de Culpar o Código | A verificação da feature não conclui efeito de runtime; a validação do JAR instalado fica explicitamente adiada e não gera conclusão sobre o artefato | PASS |
| Technology Stack | Java 17, Maven 3.9.16, Spring Boot 4.0.0, plugin 3.4.1 e dependências existentes permanecem | PASS |

**Reavaliação após a Fase 1**: PASS — o desenho não cria flag, artefato de extensão, dependência, estrutura de dados ou alteração no código gerado. As únicas mudanças são dois caminhos de arquivo na subpasta 01; nenhuma suíte, fixture, README ou redirect é editado.

## Project Structure

### Documentation (this feature)

```text
specs/008-rename-procedure-files/
├── plan.md              # Este arquivo
├── research.md          # Fase 0: decisões e alternativas
├── data-model.md        # Fase 1: entidades e mapa de renomes
├── quickstart.md        # Fase 1: roteiro de validação
├── contracts/
│   └── file-rename-contract.md  # Contrato dos caminhos, invariantes e pós-condições
├── checklists/
│   └── requirements.md  # Checklist de qualidade da spec
└── spec.md              # Especificação da feature
```

### Source Code (repository root)

```text
src/main/resources/llm-maven-flags-inference/
├── 01-entrada-inicializacao/
│   ├── 01-entryPoint.md                              # Fase 1: renomeado de prompt-00-leia-este-arquivo-primeiro.md
│   └── 02-mapa-dos-arquivos-e-ordem-de-leitura.md    # Fase 1: renomeado de indice-01-mapa-dos-arquivos-e-ordem-de-leitura.md
├── 02-operacao-procedimento-do-agente/
│   ├── 01-papel-e-restricoes-do-agente.md            # Fase 2: renomeado de prompt-01-...
│   ├── 02-como-localizar-e-ler-o-archetype.md        # Fase 2: renomeado de prompt-02-...
│   ├── 03-regras-obrigatorias-de-geracao.md          # Fase 2: renomeado de prompt-03-...
│   ├── 04-fluxo-de-execucao-e-verificacao.md         # Fase 2: renomeado de prompt-04-...
│   └── 05-entregaveis-e-relatorios.md                # Fase 2: renomeado de prompt-05-...
├── 03-derivacao-regras-de-formacao-do-contrato/      # INTOCADO
└── 04-referencia-templates-de-contrato/              # INTOCADO

src/main/resources/utils/                             # INTOCADO (redirect legado permanece)
src/test/archetype-post-generate/                     # INTOCADO (suítes continuam com os nomes antigos)
README.md                                             # INTOCADO
```

**Structure Decision**: mantém o módulo Maven único existente. A feature não cria diretório de produção, dependência ou estrutura de dados; apenas renomeia arquivos dentro de duas subpastas existentes. `target/` continua sendo saída gerada e não é editado manualmente.

## Phase 0 — Research

Ver [research.md](./research.md) para as decisões sobre verificação sem atualizar suítes, redirect legado, verificação do JAR, mecanismo de renomeação, prova de conteúdo e independência das fases.

## Phase 1 — Design

Ver [data-model.md](./data-model.md), [contracts/file-rename-contract.md](./contracts/file-rename-contract.md) e [quickstart.md](./quickstart.md).

## Implementation Strategy

### Fase 1 — Entrada e mapa da subpasta 01 (P1)

**Objetivo**: renomear a porta de entrada e o mapa de leitura, preservando conteúdo e deixando a subpasta com exatamente dois arquivos.

**Passos**:
1. Capturar um manifesto SHA-256 dos dois arquivos-alvo e de todos os arquivos das subpastas 02, 03 e 04, fora do repositório.
2. Conferir as pré-condições: arquivos de origem existentes, destinos ausentes e mesmo diretório.
3. Renomear `prompt-00-leia-este-arquivo-primeiro.md` para `01-entryPoint.md`.
4. Renomear `indice-01-mapa-dos-arquivos-e-ordem-de-leitura.md` para `02-mapa-dos-arquivos-e-ordem-de-leitura.md`.
5. Validar: exatamente 2 arquivos na subpasta 01, nomes exatos, hashes idênticos ao manifesto e subpastas 02/03/04 inalteradas.
6. Registrar as falhas conhecidas das suítes que citam os nomes antigos.

**Critério de conclusão**: SC-001, SC-003 (parcial), SC-004 (2 de 2) e SC-005 para a Fase 1.

### Fase 2 — Operação do agente sem o prefixo prompt- (P2)

**Objetivo**: remover o prefixo `prompt-` dos cinco arquivos da subpasta 02, preservando numeral, descrição e conteúdo.

**Passos**:
1. Capturar um manifesto SHA-256 dos cinco arquivos-alvo e de todos os arquivos das subpastas 01, 03 e 04, fora do repositório.
2. Conferir as pré-condições: origens existentes, destinos ausentes e mesmo diretório.
3. Renomear os cinco arquivos para `01-papel-e-restricoes-do-agente.md`, `02-como-localizar-e-ler-o-archetype.md`, `03-regras-obrigatorias-de-geracao.md`, `04-fluxo-de-execucao-e-verificacao.md` e `05-entregaveis-e-relatorios.md`.
4. Validar: exatamente 5 arquivos, nenhum começando com `prompt-`, numerais 01 a 05, hashes idênticos e subpastas 01/03/04 inalteradas.
5. Registrar as falhas conhecidas das suítes que citam os nomes antigos.

**Critério de conclusão**: SC-002, SC-003 (parcial), SC-004 (5 de 5) e SC-005 para a Fase 2.

### Dependências entre as fases

- A Fase 2 não depende da Fase 1 e pode ser executada e validada isoladamente.
- A Fase 1 não depende da Fase 2.
- A ordem recomendada é Fase 1 e depois Fase 2, apenas para leitura; a verificação de "pastas não tocadas" usa o estado imediatamente anterior a cada fase, não o estado original.
- As falhas das suítes se acumulam com as duas fases aplicadas; elas são consequência conhecida, não regressão da feature.

### Estratégia de verificação

1. **Listagem**: conferir os nomes e as contagens exatas em cada subpasta.
2. **Hash SHA-256**: comparar cada arquivo renomeado com o hash capturado antes da fase.
3. **Pastas não tocadas**: comparar o manifesto das subpastas fora do escopo com o estado anterior à fase.
4. **Conteúdo publicado em `target/classes`** (opcional): `mvn -B clean process-resources` e conferir a cópia com os novos nomes.
5. **Suítes**: não usar `mvn -B test` como gate; se executado, as falhas esperadas são as seis suítes listadas no research.
6. **JAR instalado**: fora do aceite desta feature; a checagem opt-in existente depende de atualização futura.

### Riscos e mitigação

| Risco | Mitigação |
|---|---|
| Edição acidental de conteúdo durante o rename | Hash SHA-256 antes e depois; renomear sem abrir/reescrever o arquivo |
| Sobrescrita de destino já existente | Pré-condição explícita de destino ausente; parar se existir |
| Alteração indevida nas subpastas 03/04 | Manifesto de arquivos e hashes antes da fase |
| Suítes vermelhas confundidas com regressão | Lista nominal das seis suítes e vínculo com FR-013 |
| Redirect legado quebrado passar despercebido | Registro explícito no research e no relatório de implementação |
| Comando git não autorizado | Usar `Rename-Item` do PowerShell; nenhum `git mv`, commit, push ou branch na implementação |

## Complexity Tracking

Nenhuma violação da constitution. Nenhum item de complexidade adicional precisa ser justificado.
