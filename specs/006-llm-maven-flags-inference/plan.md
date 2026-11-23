# Implementation Plan: Renomear o Procedimento Empacotado para llm-maven-flags-inference

**Branch**: `006-llm-maven-flags-inference` | **Date**: 2026-11-23 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/006-llm-maven-flags-inference/spec.md`

## Summary

Mover o procedimento empacotado de 19 arquivos de `src/main/resources/utils/` para `src/main/resources/llm-maven-flags-inference/`, atualizar apenas as referências de caminho dentro dos 4 arquivos que as contêm, deixar um único stub de redirect no caminho antigo do `prompt-00`, atualizar README, templates do kit, suítes e fixtures, e adicionar uma suíte anti-drift. Regras, ordem de leitura, catálogo, templates de contrato e pós-processador permanecem intactos.

## Technical Context

**Language/Version**: Markdown (procedimento); Groovy 4.0.28 (suítes de contrato); Java 17+ (JDK do build); Maven 3.9.x

**Primary Dependencies**: Maven Archetype Plugin 3.4.1; groovy-maven-plugin 2.1.1; groovy-xml e groovy-json 4.0.28

**Storage**: Sistema de arquivos e JAR (`target/classes`, `~/.m2`); sem banco de dados

**Testing**: Suítes Groovy em `src/test/archetype-post-generate/`, executadas pelo `groovy-maven-plugin` na fase `test`; fixtures de integração com `mvn clean integration-test`

**Target Platform**: Máquina de desenvolvimento ou CI que roda Maven (Windows/PowerShell; scripts bash também presentes)

**Project Type**: Maven archetype de módulo único (artefato de build)

**Performance Goals**: N/A — mudança documental e de empacotamento

**Constraints**: exatamente 19 arquivos em 4 subpastas; regras intactas; um único redirect; catálogo de 35 flags, 18 filesets e pós-processador inalterados; sem commit ou push fora dos hooks

**Scale/Scope**: 1 raiz de procedimento, 4 subpastas, 19 arquivos, 3 templates do kit, 4 suítes ajustadas, 1 suíte nova

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Princípio | Gate aplicável | Status |
|---|---|---|
| I. Modularidade por Chave de Ativação | Nenhuma flag criada, removida ou alterada; nenhuma capacidade opcional nova | PASS |
| II. Convenção Fixa de Nomenclatura | Os 19 nomes de arquivo e as 4 subpastas permanecem; o stub mantém o nome do entry point antigo | PASS |
| III. Pacote por Funcionalidade | A árvore Java gerada não é tocada | PASS |
| IV. Binding Duplo de Configuração | Nenhuma propriedade externalizada é criada ou alterada | PASS |
| V. Credenciais Nunca no Repositório | Nenhum segredo ou credencial é tocado | PASS |
| VI. Validar Antes de Culpar o Código | A fase de verificação reinstala o JAR e lê o procedimento do artefato instalado | PASS |

**Reavaliação após a Fase 1**: PASS — o desenho não introduz arquivo de extensão do archetype, flag, dependência ou estrutura de dados nova.

## Project Structure

### Documentation (this feature)

```text
specs/006-llm-maven-flags-inference/
├── plan.md                    # Este arquivo
├── research.md                # Fase 0
├── data-model.md              # Fase 1
├── quickstart.md              # Fase 1
├── contracts/
│   ├── procedure-path.md      # Contrato do caminho público
│   └── documentation-contract.md  # Contrato do README e do anti-drift
├── checklists/
│   └── requirements.md        # Checklist de qualidade
└── tasks.md                   # Fase 2 (/speckit.tasks)
```

### Source Code (repository root)

```text
README.md                                                    # ALTERADO: novo tópico + referências de caminho
.specify/templates/spec-template.md                          # ALTERADO: nome do conceito
.specify/templates/plan-template.md                          # ALTERADO: nome do conceito
.specify/templates/tasks-template.md                         # ALTERADO: nome do conceito

src/main/resources/llm-maven-flags-inference/                # NOVO: raiz do procedimento (19 arquivos)
├── 01-entrada-inicializacao/                                # 2 arquivos
│   ├── prompt-00-leia-este-arquivo-primeiro.md              # ALTERADO: referências de caminho
│   └── indice-01-mapa-dos-arquivos-e-ordem-de-leitura.md    # ALTERADO: referências de caminho
├── 02-operacao-procedimento-do-agente/                      # 5 arquivos
│   ├── prompt-01-papel-e-restricoes-do-agente.md            # ALTERADO: referência de caminho
│   ├── prompt-02-como-localizar-e-ler-o-archetype.md        # ALTERADO: referências de caminho
│   ├── prompt-03-regras-obrigatorias-de-geracao.md          # SEM ALTERAÇÃO
│   ├── prompt-04-fluxo-de-execucao-e-verificacao.md         # SEM ALTERAÇÃO
│   └── prompt-05-entregaveis-e-relatorios.md                # SEM ALTERAÇÃO
├── 03-derivacao-regras-de-formacao-do-contrato/             # 6 arquivos, SEM ALTERAÇÃO
└── 04-referencia-templates-de-contrato/                     # 6 arquivos, SEM ALTERAÇÃO

src/main/resources/utils/01-entrada-inicializacao/
└── prompt-00-leia-este-arquivo-primeiro.md                  # NOVO: único redirect; nenhuma regra

src/test/archetype-post-generate/
├── BuildArtifactsTest.groovy                                # ALTERADO: §5 aponta para a nova raiz
├── ProcedureContractTest.groovy                             # ALTERADO: raiz e prefixo de caminho em 1.5
├── DestinationContractTest.groovy                           # ALTERADO: raiz, cópia empacotada e JAR instalado
├── ReadmeConsistencyTest.groovy                             # ALTERADO: contagem e caminhos do prompt
└── PathRenameContractTest.groovy                            # NOVO: redirect, contagem e anti-drift

src/test/groovy/run-archetype-post-generate-tests.groovy     # ALTERADO: registra a suíte nova

src/test/resources/projects/custom-nested-edge-cases/archetype.properties   # ALTERADO: comentário
src/test/resources/projects/custom-enums-from-payload/archetype.properties  # ALTERADO: comentário
src/test/resources/projects/entity-contract/archetype.properties            # ALTERADO: comentário
```

**Structure Decision**: mantém o módulo único existente. A feature não cria módulo de produção, dependência ou estrutura de dados. O novo diretório é um recurso empacotado; o caminho antigo fica com um único arquivo de redirect.

## Fase 0 — Research

Ver [research.md](./research.md) para as decisões e alternativas descartadas.

## Fase 1 — Design

Ver [data-model.md](./data-model.md), [contracts/](./contracts/) e [quickstart.md](./quickstart.md).

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

Nenhuma violação. Tabela vazia — o gate da Constitution passou integralmente.
