# Implementation Plan: Renomear as Subpastas Iniciais e o Arquivo de Leitura do Procedimento

**Branch**: `012-rename-procedure-folders` | **Date**: 2026-11-26 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/011-rename-procedure-folders/spec.md`

## Status do artefato

STATUS DO ARTEFATO: CRIAÇÃO
- Ação: criar o plan.md do zero.

## Summary

Renomear as duas primeiras subpastas do procedimento empacotado `llm-maven-flags-inference/` e o arquivo 02 da segunda subpasta: `01-entrada-inicializacao` passa a `01-inicializacao`; `02-operacao-procedimento-do-agente` passa a `02-procedimento-do-agente`; e `02-como-localizar-e-ler-o-archetype.md` passa a `02-localizar-e-ler-o-archetype.md`. Atualizar todas as referências operacionais — índice, porta de entrada, README (texto, caminhos, índice do README e âncoras), redirect legado e suítes de verificação — sem criar, remover ou mover arquivos do procedimento e sem alterar o conteúdo normativo. A implementação é dividida nas três fases da spec: a Fase 1 renomeia a primeira subpasta e atualiza suas referências; a Fase 2 renomeia a segunda subpasta; a Fase 3 renomeia o arquivo 02 e conclui a varredura. O procedimento mantém 19 arquivos (13 Markdown e 6 JSON) em 4 subpastas, com 2 arquivos na primeira e 5 na segunda; o catálogo, `archetype-resources/` e `pom.xml` não mudam. A validação usa `mvn -B test`, `mvn clean install`, `mvn -B clean integration-test` e a verificação opt-in do JAR instalado.

## Technical Context

**Language/Version**: Markdown e JSON (procedimento empacotado); Groovy 4.0.28 (suítes de contrato); Java 17 (constitution) e JDK 21 no CI; Maven 3.9.16. O `pom.xml` não fixa `maven.compiler.release`.

**Primary Dependencies**: Maven Archetype Plugin 3.4.1; archetype-packaging 3.4.1; xml-maven-plugin 1.2.1; groovy-maven-plugin 2.1.1; groovy-json 4.0.28; JUnit 4.13.2; Velocity 2.4.1 (test). Nenhuma dependência nova é necessária.

**Storage**: Sistema de arquivos e JAR; 19 arquivos do procedimento (13 Markdown e 6 JSON) em 4 subpastas; catálogo XML com 35 propriedades e 18 filesets; sem banco de dados.

**Testing**: Suítes Groovy em `src/test/archetype-post-generate/`, registradas no runner `src/test/groovy/run-archetype-post-generate-tests.groovy` e executadas na fase `test`; `mvn -B test`, `mvn clean install` e `mvn -B clean integration-test`; CI em Windows com JDK 21; verificação opt-in do JAR instalado com `-Deda.verify.installed.jar=true`.

**Target Platform**: JAR consumido por agentes e operadores; Windows e POSIX; procedimento sem motor, CLI ou script em runtime.

**Project Type**: Maven archetype / promptware (documentação executável + suítes de contrato).

**Performance Goals**: A feature não define meta própria. Preservar o orçamento da feature 007 — aumento máximo de 10% sobre a linha de base local de `mvn -B test` — como guarda de regressão, não como gate de aceite.

**Constraints**: 19 arquivos (13 Markdown e 6 JSON), 4 subpastas, 2 arquivos em 01 e 5 em 02; conteúdo normativo dos arquivos renomeados preservado; apenas referências operacionais aos nomes antigos atualizadas; referências no índice, porta de entrada, README, redirect legado e suítes de verificação; specs históricas, planos/tarefas e saída gerada ficam fora; não criar, remover ou mover arquivos do procedimento; não renomear as subpastas 03 e 04; não alterar catálogo, templates, regras de derivação, ordem de leitura ou formato do contrato; o redirect legado permanece em `utils/01-entrada-inicializacao`; não expor segredos.

**Scale/Scope**: 2 subpastas e 1 arquivo renomeados em 3 fases; 19 arquivos no procedimento (13 Markdown e 6 JSON) em 4 subpastas; 0 arquivos novos ou removidos no procedimento; 0 dependências novas; atualização do índice, porta de entrada, README, redirect legado e suítes de verificação.

**Paradigm**: NÃO OOP — a feature é procedural/documental; o projeto é um archetype com scripts Groovy, XML/Markdown e templates Java, sem modelagem OOP no escopo.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Princípio | Gate aplicável | Status |
|---|---|---|
| I. Modularidade por Chave de Ativação | Nenhuma flag, capacidade opcional ou comportamento condicional novo; a mudança é de nomenclatura do procedimento e de suas referências | PASS |
| II. Convenção Fixa de Nomenclatura para Artefatos de Extensão | Os arquivos do procedimento são promptware empacotado como recurso, não artefatos de extensão selecionados por padrão de build; a estrutura de 4 subpastas e a contagem de 19 arquivos permanecem, e os novos nomes seguem a numeração da própria subpasta. O redirect legado mantém o nome antigo de propósito, como compatibilidade, e não é artefato de extensão | PASS |
| III. Pacote por Funcionalidade no Código Gerado | Nenhum código Java gerado ou template de entidade é alterado | PASS |
| IV. Binding Duplo de Configuração Externalizada | Nenhuma propriedade externalizada, variável de binding ou configuração é criada ou alterada | PASS |
| V. Credenciais Nunca Entram no Repositório | As alterações são nomes e referências; nenhum segredo, credencial ou valor sensível é adicionado ou exposto | PASS |
| VI. Validar Antes de Culpar o Código | A validação reinstala o artefato com `mvn clean install` e confere a cópia empacotada; a verificação opt-in do JAR instalado continua disponível | PASS |
| Technology Stack | Java 17, Maven 3.9.16, plugin 3.4.1, Groovy 4.0.28 e dependências existentes permanecem | PASS |

**Reavaliação após a Fase 1**: PASS — o desenho não cria flag, artefato de extensão, dependência, estrutura de dados ou alteração no código gerado. A feature apenas renomeia pastas e um arquivo do procedimento, atualiza referências no corpus operacional e ajusta as suítes existentes; o catálogo, `archetype-resources/` e os 19 arquivos do procedimento permanecem com a mesma contagem.

## Project Structure

### Documentation (this feature)

```text
specs/011-rename-procedure-folders/
├── plan.md                         # Este arquivo
├── research.md                     # Fase 0: decisões e inventário
├── data-model.md                   # Fase 1: entidades, fases e invariantes
├── quickstart.md                   # Fase 1: roteiro de validação por fase
├── contracts/
│   └── rename-manifest.md          # Mapa old→new, tokens e inventário por fase
└── checklists/
    └── requirements.md             # Checklist de qualidade da spec
```

### Source Code (repository root)

```text
src/main/resources/llm-maven-flags-inference/
├── 01-inicializacao/                                       # FASE 1: renomeado de 01-entrada-inicializacao
│   ├── 00-entry-point.md
│   └── 01-mapa-dos-arquivos-e-ordem-de-leitura.md
├── 02-procedimento-do-agente/                              # FASE 2: renomeado de 02-operacao-procedimento-do-agente
│   ├── 01-papel-e-restricoes-do-agente.md
│   ├── 02-localizar-e-ler-o-archetype.md                   # FASE 3: renomeado de 02-como-localizar-e-ler-o-archetype.md
│   ├── 03-regras-obrigatorias-de-geracao.md
│   ├── 04-fluxo-de-execucao-e-verificacao.md
│   └── 05-entregaveis-e-relatorios.md
├── 03-derivacao-regras-de-formacao-do-contrato/             # Nome preservado
└── 04-referencia-templates-de-contrato/                     # Nome preservado

src/main/resources/utils/01-entrada-inicializacao/
└── prompt-00-leia-este-arquivo-primeiro.md                  # Nome preservado (redirect legado); destino atualizado na Fase 1

README.md                                                    # FASE 1: referências/âncoras da subpasta 01; FASE 2: subpasta 02; FASE 3: sem referência ao arquivo 02
src/test/archetype-post-generate/                            # Suítes existentes atualizadas por fase
src/test/groovy/run-archetype-post-generate-tests.groovy    # Sem alteração (nenhuma suíte nova)
pom.xml                                                      # Sem alteração
src/main/resources/META-INF/                                 # Catálogo sem alteração (35 propriedades / 18 filesets)
src/main/resources/archetype-resources/                      # Sem alteração
```

**Structure Decision**: manter o módulo único existente. A feature não cria módulo de produção, dependência ou estrutura de dados; renomeia pastas e um arquivo no lugar, atualiza referências no corpus operacional e ajusta as suítes existentes. A Fase 1 altera a subpasta 01 e suas referências; a Fase 2 altera a subpasta 02; a Fase 3 altera o arquivo 02 e conclui a varredura. O catálogo, `archetype-resources/`, `pom.xml` e as specs históricas permanecem intocados. O redirect legado é o único caminho que mantém o nome antigo de pasta.

## Faseamento

| Fase | Nome | User Story | Prioridade | Depende de | Testes |
|---|---|---|---|---|---|
| Fase 1 | Subpasta de inicialização | US1 | P1 | — | Atualizar suítes existentes; validar nomes, contagens e referências |
| Fase 2 | Subpasta de procedimento do agente | US2 | P2 | Fase 1 (conferência final) | Atualizar suítes existentes; validar nomes, contagens e referências |
| Fase 3 | Arquivo de localização e leitura do archetype | US3 | P3 | Fase 2 (caminho da subpasta) | Atualizar suítes existentes; varredura final de referências e contagens |

## Implementation Strategy

### Fundação compartilhada

- Confirmar a linha de base: 19 arquivos, 13 Markdown, 6 JSON, 4 subpastas, 2 arquivos em 01 e 5 em 02.
- Registrar SHA-256 dos arquivos das duas subpastas antes de qualquer mudança, fora do repositório.
- Materializar `contracts/rename-manifest.md` com o mapa `old → new`, o mapa de tokens curtos, o inventário de referências por arquivo/linha/fase e as exceções.
- Atualizar as suítes existentes de forma incremental, sem criar suíte nova e sem alterar o runner.
- Validar com `mvn -B test` ao final de cada fase.

### Fase 1 — Subpasta de inicialização

- Renomear `src/main/resources/llm-maven-flags-inference/01-entrada-inicializacao` para `01-inicializacao`, após confirmar que o destino não existe.
- Atualizar o índice (`01-mapa-dos-arquivos-e-ordem-de-leitura.md`) nas linhas 11 e 12, incluindo o caminho do próprio índice.
- Atualizar a porta de entrada (`00-entry-point.md`) na linha 620 e nas linhas 708 e 709.
- Atualizar o README nas referências à subpasta 01: linhas 52, 603, 652 (parte 01), 806, 807, 995, 2111, 2152, 2207, 2513, 2567, 2639 e 2704, incluindo âncoras.
- Atualizar o redirect legado na linha 7 para apontar para `llm-maven-flags-inference/01-inicializacao/00-entry-point.md`; não renomear a pasta `utils/01-entrada-inicializacao`.
- Atualizar as suítes que referenciam a subpasta 01: `BuildArtifactsTest`, `PathRenameContractTest`, `HardStopContractTest`, `DestinationContractTest`, `ReadmeConsistencyTest`, `ProcedureEntryRenameContractTest` e `FileRenameContractTest`.
- Validar: 2 arquivos em `01-inicializacao`, ausência de `01-entrada-inicializacao` em `llm-maven-flags-inference`, 19/13/6/4, redirect correto e `mvn -B test`.

### Fase 2 — Subpasta de procedimento do agente

- Renomear `src/main/resources/llm-maven-flags-inference/02-operacao-procedimento-do-agente` para `02-procedimento-do-agente`, após confirmar que o destino não existe.
- Atualizar o índice nas linhas 13 a 17, mantendo a ordem e os papéis.
- Atualizar a porta de entrada nas linhas 710 e 712 a 714; a linha 711 recebe a pasta nova na Fase 2 e o arquivo novo na Fase 3.
- Atualizar o README nas referências à subpasta 02: linhas 53, 605, 652 (parte 02), 811 e 812, incluindo âncoras.
- Atualizar as suítes que referenciam a subpasta 02: `BuildArtifactsTest`, `PathRenameContractTest`, `DestinationContractTest`, `CatalogSyncContractTest`, `ReadmeConsistencyTest` e `ProcedureEntryRenameContractTest`.
- Validar: 5 arquivos em `02-procedimento-do-agente`, ausência de `02-operacao-procedimento-do-agente`, 19/13/6/4, ordem do índice e `mvn -B test`.

### Fase 3 — Arquivo de localização e leitura do archetype

- Renomear `02-como-localizar-e-ler-o-archetype.md` para `02-localizar-e-ler-o-archetype.md` dentro de `02-procedimento-do-agente`, após confirmar que o destino não existe.
- Atualizar o índice nas linhas 14, 33 e 47, incluindo o caminho completo e as formas curtas.
- Atualizar a porta de entrada no registro de paradas (linhas 168, 170, 171 e 172) e na lista de integridade (linha 711).
- Atualizar as suítes que referenciam o arquivo 02, incluindo `PathRenameContractTest`, `BuildArtifactsTest`, `ProcedureEntryRenameContractTest` e `DestinationContractTest`.
- Verificar que o README não contém referência ao nome antigo do arquivo; se não houver, não editar o README nesta fase.
- Executar a varredura final de referências antigas no corpus operacional, com as exceções do redirect legado e das asserções negativas.
- Validar: arquivo novo presente, arquivo antigo ausente, 19/13/6/4, ordem do índice, `mvn -B test`, `mvn -B clean install` e `mvn -B clean integration-test`; verificação opt-in do JAR instalado com `-Deda.verify.installed.jar=true`.

## Complexity Tracking

Nenhuma violação de constitution identificada; nenhuma justificativa de complexidade é necessária.
