# Implementation Plan: Contrato de Destino de Geração (outputDirectory) no Procedimento Empacotado

**Branch**: `005-output-directory-contract` | **Date**: 2026-11-19 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `specs/005-output-directory-contract/spec.md`

## Summary

Torna a pasta de destino da geração uma **entrada obrigatória e explícita** do spec do operador, normatizada dentro do procedimento empacotado (`utils/`, 19 arquivos que viajam no JAR).

A propriedade que transporta o destino **já existe e é nativa** do `maven-archetype-plugin` — é o parâmetro `outputDirectory` do goal `archetype:generate` (`User Property: outputDirectory`, `Default: ${basedir}`). Ela **não** pertence ao catálogo de flags (0 ocorrências em `archetype-metadata.xml` e nos 35 `property-*.xml`) e **não** aparece em nenhum arquivo de `utils/`. O pós-processador já a consome (`body-05-path-root-variables.groovy:13` → `Paths.get(request.outputDirectory, request.artifactId)`).

O achado que dá sentido à feature: o **default nativo é o diretório onde o Maven executa**. Rodando dentro da pasta do archetype, `${basedir}` **é** a própria árvore do archetype — precisamente o lugar proibido. O modo de falha que a spec combatia não era hipótese: era o comportamento padrão da ferramenta. A feature **não cria** a propriedade; torna obrigatório o seu preenchimento.

Abordagem técnica: **edição Surgical dos 19 arquivos existentes de `utils/`** (nenhum arquivo novo, nenhuma contagem alterada), mais a adição de uma verificação automatizada que prova, por leitura do procedimento empacotado, que a regra de destino está declarada nas instâncias esperadas.

## Technical Context

**Language/Version**: Java 17 (projeto gerado) / Groovy 2.4.9 (gmaven 2.1.1) / Markdown (procedimento em `utils/`)
**Primary Dependencies**: `maven-archetype-plugin` 3.4.1 · `maven-archetype-packaging` 3.4.1 · `org.codehaus.gmaven:groovy-maven-plugin` 2.1.1 · `org.codehaus.mojo:xml-maven-plugin` 1.2.1
**Storage**: N/A — a feature altera documentos procedurais empacotados (Markdown) e adiciona uma suíte de verificação Groovy
**Testing**: suítes Groovy plain-script executadas por `run-archetype-post-generate-tests.groovy` (fase `test`) · `maven-archetype-plugin:integration-test` com 22 fixtures (fase `integration-test`) · JUnit 5 apenas dentro dos projetos gerados
**Target Platform**: Windows 11 / Maven 3.9.16 / Java 21.0.11 (ambiente validado); o procedimento é redigido para o shell do operador (PowerShell no caminho crítico)
**Project Type**: build-tool / archetype-packaging (gera projetos Spring Boot)
**Performance Goals**: N/A — feature sem impacto de runtime. O custo é o tempo de build (leitura de 19 arquivos de procedimento)
**Constraints**:
- `utils/` MUST permanecer com **exatamente 19 arquivos** — travado por `BuildArtifactsTest` 5.2/5.3/5.4, `prompt-00` §8 e `ReadmeConsistencyTest` 2.3
- O catálogo MUST permanecer com **35 flags / 18 filesets** — travado por `BuildArtifactsTest` 2.3/2.4 e `ProcedureContractTest` 1.10
- Todo termo `snake_case` entre backticks em `utils/*.md` que não seja flag do catálogo **quebra** `ProcedureContractTest` 1.4 (allowlist `nonFlagTerms`: `CONTRACT_DERIVED`, `DEPENDS_ON`, `XDG_CACHE_HOME`) — logo o marcador **NÃO** pode ser escrito como `output_directory` nem `PASTA_DESTINO`
- Toda rota entre backticks que comece com `utils/`, `META-INF/` ou `archetype-resources/` é validada contra o layout real por `ProcedureContractTest` 1.5
- Nenhuma alteração em `archetype-metadata.xml`, `archetype-post-generate.groovy`, `archetype-resources/`, `pom.xml`, fixtures, CI ou Constitution
**Scale/Scope**: 19 arquivos de procedimento (6 alterados), 1 suíte nova de verificação, 1 registro no runner, 2 seções do README, 1 feature directory de spec

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Princípio | Avaliação | Resultado |
|-----------|-----------|-----------|
| **I. Modularidade por Chave de Ativação** (NON-NEGOTIABLE) | A feature **não cria flag**. O destino é entrada **obrigatória**, não capacidade opcional — a escolha do destino não habilita/desabilita nenhuma capacidade gerada. Nenhuma adição incondicional de funcionalidade. | ✅ PASS |
| **II. Convenção Fixa de Nomenclatura** | Nenhum arquivo novo em `utils/` (a regra é distribuída por edição nos 6 arquivos existentes). Nenhum arquivo em `archetype_post_generate_*` criado. | ✅ PASS |
| **III. Pacote por Funcionalidade no Código Gerado** | Não toca no código Java gerado. | ✅ PASS |
| **IV. Binding Duplo de Configuração Externalizada** | Não toca em binding de propriedades do projeto gerado. | ✅ PASS |
| **V. Credenciais Nunca Entram no Repositório** | O destino é um caminho de filesystem; o procedimento proíbe credenciais no contrato (regras-05 §8) e isso não muda. | ✅ PASS |
| **VI. Validar Antes de Culpar o Código** | A feature inclui reinstalação limpa do artefato + verificação lendo o procedimento **de dentro do JAR reinstalado** (FR-032), honrando o modo de falha recorrente deste projeto (artefato em cache desatualizado). | ✅ PASS |
| **Technology Stack & Constraints** | Java 17, Maven 3.9.16, Spring Boot 4.0.0, Logback/ELK, JWT, Jakarta Validation, JUnit 5 — **nenhum item alterado**. | ✅ PASS |
| **Governance — cláusula de compliance** | A cláusula exige compliance review para mudanças em `bash_script/`, `META-INF/maven/archetype-metadata.xml`, `META-INF/archetype-post-generate.groovy`, `archetype-resources/`. **A feature não toca nenhum desses.** | ✅ PASS (sem cláusula acionada) |
| **Governance — conflito futuro** | Nenhum princípio é violado e nenhuma lacuna nova é revelada. A Constitution **não** é emendada (FR-027). | ✅ PASS (sem emenda necessária) |

**Resultado do gate: PASS — nenhuma violação a justificar. Complexity Tracking permanece vazio.**

## Project Structure

### Documentation (this feature)

```text
specs/005-output-directory-contract/
├── plan.md              # Este arquivo (/speckit.plan output)
├── spec.md              # Especificação (já existe)
├── checklists/
│   └── requirements.md  # Checklist de qualidade (já existe)
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
└── tasks.md             # Phase 2 output (/speckit.tasks — NÃO criado por /speckit.plan)
```

### Source Code (repository root)

```text
src/main/resources/utils/                        # PROCEDIMENTO EMPACOTADO — 19 arquivos, contagem IMUTÁVEL
├── 01-entrada-inicializacao/
│   ├── prompt-00-leia-este-arquivo-primeiro.md   # ALTERADO: §0 (novo gate de destino), §2 (5ª entrada), §6, §7
│   └── indice-01-mapa-dos-arquivos-e-ordem-de-leitura.md   # SEM ALTERAÇÃO (nenhum arquivo novo)
├── 02-operacao-procedimento-do-agente/
│   ├── prompt-01-papel-e-restricoes-do-agente.md  # SEM ALTERAÇÃO (já cobre "pare, não improvise")
│   ├── prompt-02-como-localizar-e-ler-o-archetype.md  # SEM ALTERAÇÃO
│   ├── prompt-03-regras-obrigatorias-de-geracao.md    # ALTERADO: §3 (mecanismo único), §4 (escape)
│   ├── prompt-04-fluxo-de-execucao-e-verificacao.md  # ALTERADO: §1.2 (checklist), §2 (command line), §3 (validação)
│   └── prompt-05-entregaveis-e-relatorios.md     # ALTERADO: §4 (relatório final)
├── 03-derivacao-regras-de-formacao-do-contrato/
│   ├── regras-01-como-derivar-a-entidade-do-contrato.md  # ALTERADO: §1 (entradas), §4.2 (metachaves)
│   ├── regras-02..regras-06                    # SEM ALTERAÇÃO
│   └── (regras-06 tem acréscimo mínimo: destino fora da tabela de proveniência)
└── 04-referencia-templates-de-contrato/          # SEM ALTERAÇÃO (contrato não muda)

src/test/archetype-post-generate/
├── ProcedureContractTest.groovy                  # ALTERADO: novos checks da regra de destino
└── (novo) DestinationContractTest.groovy        # NOVO: suíte dedicada — regras declaradas, sem caminho de usuário, contagens

src/test/groovy/
└── run-archetype-post-generate-tests.groovy      # ALTERADO: registrar a suíte nova (CoverageRegistryTest 2.1/2.2)

README.md                                         # ALTERADO: §7 (prompts de homologação) e seção de inferência
```

**Estrutura de dados affected**: nenhuma estrutura Java/Groovy nova. A feature é **documental + verificação**.

**Structure Decision**: mantém a estrutura existente do repositório. A feature **não cria** diretório, arquivo de produção ou módulo novo em `utils/` — a regra é distribuída por edição surgical nos 6 arquivos existentes que a spec já indica, respeitando o gate de contagem de 19 arquivos.

## Fase 0 — Research

Ver [`research.md`](./research.md) para as decisões de pesquisa.

## Fase 1 — Design

Ver [`data-model.md`](./data-model.md), [`contracts/`](./contracts/) e [`quickstart.md`](./quickstart.md).

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

Nenhuma violação. Tabela vazia — o gate da Constitution passou integralmente.
