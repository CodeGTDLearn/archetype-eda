# Implementation Plan: Renomear a Pasta e os Seis Arquivos das Regras de Formação do Contrato

**Branch**: `013-rename-contract-rule-files` | **Date**: 2026-11-26 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/011-rename-contract-rule-files/spec.md`

## Summary

Renomear a subpasta `03-derivacao-regras-de-formacao-do-contrato` para `03-regras-de-formacao-do-contrato` e remover `como-` dos nomes dos seis arquivos de regras de formação do contrato: os três primeiros passam a `01-derivar-a-entidade-do-contrato.md`, `02-resolver-tipos-e-enums.md` e `03-derivar-flags-de-tipos-aninhados.md`; os três últimos passam a `04-aplicar-precedencia-e-validacao.md`, `05-materializar-e-emitir-o-contrato.md` e `06-relatar-proveniencia.md`. Atualizar todas as referências operacionais — índice, porta de entrada, regras do procedimento, README (incluindo âncoras, títulos e links), comentários das fixtures e suítes de verificação — sem criar, remover ou mover arquivos do procedimento e sem alterar o conteúdo normativo. A implementação é dividida nas três fases da spec: a Fase 1 renomeia a pasta e atualiza suas referências; a Fase 2 renomeia os arquivos `01`–`03`; a Fase 3 renomeia `04`–`06` e conclui a varredura. O procedimento mantém 19 arquivos (13 Markdown e 6 JSON) em 4 subpastas, com 6 arquivos na subpasta 03; o catálogo, `archetype-resources/`, `pom.xml` e as specs históricas não mudam. A validação usa `mvn -B clean test`, `mvn clean install`, `mvn -B clean integration-test` e a verificação opt-in do JAR instalado.

## Technical Context

**Language/Version**: Markdown e JSON (procedimento promptware); Groovy 4.0.28 (suítes de contrato); Java 17 (constitution) e JDK 21 no CI; Maven 3.9.16. O `pom.xml` não fixa `maven.compiler.release`.

**Primary Dependencies**: Maven Archetype Plugin 3.4.1; archetype-packaging 3.4.1; xml-maven-plugin 1.2.1; groovy-maven-plugin 2.1.1; groovy-json 4.0.28; JUnit 4.13.2; Velocity 2.4.1 (test). Nenhuma dependência nova é necessária.

**Storage**: Sistema de arquivos e JAR; 19 arquivos do procedimento (13 Markdown e 6 JSON) em 4 subpastas, 6 na subpasta 03; catálogo XML com 35 propriedades e 18 filesets; sem banco de dados.

**Testing**: Suítes Groovy em `src/test/archetype-post-generate/`, executadas pelo runner `src/test/groovy/run-archetype-post-generate-tests.groovy` na fase `test`; `mvn -B test`, `mvn clean install` e `mvn -B clean integration-test`; CI em Windows com JDK 21; verificação opt-in do JAR instalado com `-Deda.verify.installed.jar=true`.

**Target Platform**: JAR consumido por agentes e operadores; Windows e POSIX; procedimento sem motor, CLI ou script em runtime.

**Project Type**: Maven archetype / promptware (documentação executável + suítes de contrato).

**Performance Goals**: A feature não define meta própria. Preservar o orçamento da feature 007 — aumento máximo de 10% sobre a linha de base local de `mvn -B test` — como guarda de regressão, não como gate de aceite.

**Constraints**: 19 arquivos (13 Markdown e 6 JSON), 4 subpastas, 6 arquivos na subpasta 03; conteúdo normativo dos 6 arquivos preservado; apenas referências textuais aos nomes antigos atualizadas; referências operacionais no índice, procedimento, README, comentários de fixtures e suítes de verificação; specs históricas, planos/tarefas, `target/`, `graphify-out/` e artefato instalado ficam fora; não criar, remover ou mover arquivos do procedimento; não renomear as outras subpastas; não alterar regras, catálogo, templates, ordem de leitura ou formato do contrato; não expor segredos; o JAR instalado é a fonte de runtime.

**Scale/Scope**: 1 pasta renomeada; 6 arquivos renomeados em 3 fases; 19 arquivos no procedimento (13 Markdown e 6 JSON) em 4 subpastas; 0 arquivos novos ou removidos no procedimento; 1 suíte de contrato nova em `src/test/archetype-post-generate/`; 0 dependências novas.

**Paradigm**: NÃO OOP — a feature é procedural/documental; o projeto é um archetype com scripts Groovy, XML/Markdown e templates Java, sem modelagem OOP no escopo.

**Sources**: `.specify/memory/constitution.md`; `specs/010-rename-procedure-entry-files/plan.md`; `specs/010-rename-procedure-entry-files/research.md`; `README.md`; `pom.xml`; `.github/workflows/ci.yml`; `src/test/groovy/run-archetype-post-generate-tests.groovy`; `specs/011-rename-contract-rule-files/spec.md`.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Princípio | Gate aplicável | Status |
|---|---|---|
| I. Modularidade por Chave de Ativação | Nenhuma flag, capacidade opcional ou comportamento condicional novo; a mudança é de nomenclatura e referências | PASS |
| II. Convenção Fixa de Nomenclatura | Os arquivos do procedimento são promptware empacotado como recurso, não artefatos de extensão selecionados por padrão de build; a estrutura de 4 subpastas e a contagem de 19 arquivos permanecem, e os novos nomes seguem a numeração da própria subpasta. O redirect legado mantém o nome antigo de propósito, como compatibilidade | PASS |
| III. Pacote por Funcionalidade no Código Gerado | Nenhum código Java gerado ou template de entidade é alterado | PASS |
| IV. Binding Duplo de Configuração Externalizada | Nenhuma propriedade externalizada, variável de binding ou configuração é criada ou alterada | PASS |
| V. Credenciais Nunca Entram no Repositório | As alterações são nomes e referências; nenhum segredo, credencial ou valor sensível é adicionado ou exposto | PASS |
| VI. Validar Antes de Culpar o Código | A validação reinstala o artefato com `mvn clean install` e confere a cópia empacotada; a verificação opt-in do JAR instalado continua disponível | PASS |
| Technology Stack | Java 17, Maven 3.9.16, plugin 3.4.1, Groovy 4.0.28 e dependências existentes permanecem | PASS |

**Reavaliação após o design (Phase 1)**: PASS — o desenho não cria flag, artefato de extensão, dependência, estrutura de dados ou alteração no código gerado. A única adição fora do procedimento é uma suíte de contrato em `src/test/archetype-post-generate/`, registrada no runner; o catálogo, `archetype-resources/` e os 19 arquivos do procedimento permanecem com a mesma contagem.

## Project Structure

### Documentation (this feature)

```text
specs/011-rename-contract-rule-files/
├── plan.md                         # Este arquivo
├── research.md                     # Fase 0: decisões e inventário
├── data-model.md                   # Fase 1: entidades, invariantes e transições
├── quickstart.md                   # Fase 1: roteiro de validação por fase
├── contracts/
│   └── rename-manifest.md          # Mapa old→new, tokens, inventário por fase e exceções
└── checklists/
    └── requirements.md             # Checklist de qualidade da spec
```

### Source Code (repository root)

```text
src/main/resources/llm-maven-flags-inference/
├── 01-entrada-inicializacao/                                   # Nomes preservados
│   ├── 00-entry-point.md
│   └── 01-mapa-dos-arquivos-e-ordem-de-leitura.md
├── 02-operacao-procedimento-do-agente/                         # Nomes preservados
│   ├── 01-papel-e-restricoes-do-agente.md
│   ├── 02-como-localizar-e-ler-o-archetype.md
│   ├── 03-regras-obrigatorias-de-geracao.md
│   ├── 04-fluxo-de-execucao-e-verificacao.md
│   └── 05-entregaveis-e-relatorios.md
├── 03-regras-de-formacao-do-contrato/                          # FASE 1: pasta renomeada
│   ├── 01-derivar-a-entidade-do-contrato.md                    # FASE 2: renomeado de 01-como-...
│   ├── 02-resolver-tipos-e-enums.md                            # FASE 2: renomeado de 02-como-...
│   ├── 03-derivar-flags-de-tipos-aninhados.md                  # FASE 2: renomeado de 03-como-...
│   ├── 04-aplicar-precedencia-e-validacao.md                   # FASE 3: renomeado de 04-como-...
│   ├── 05-materializar-e-emitir-o-contrato.md                  # FASE 3: renomeado de 05-como-...
│   └── 06-relatar-proveniencia.md                              # FASE 3: renomeado de 06-como-...
└── 04-referencia-templates-de-contrato/                        # Nomes preservados

src/main/resources/utils/01-entrada-inicializacao/
└── prompt-00-leia-este-arquivo-primeiro.md                     # Nome preservado (redirect legado); conteúdo aponta para 00-entry-point.md

README.md                                                       # FASE 1: pasta, âncoras, título e links; FASE 2/3: tabela dos 6 nomes

src/test/archetype-post-generate/
├── BuildArtifactsTest.groovy                                   # FASE 1: pasta; FASE 2/3: caminhos
├── CatalogSyncContractTest.groovy                              # FASE 1: pasta; FASE 3: arquivos 04/06
├── DestinationContractTest.groovy                              # FASE 1: pasta; FASE 2/3: arquivos 01/06
├── FileRenameContractTest.groovy                               # FASE 1: pasta; refatorar listas para phase-aware
├── HardStopContractTest.groovy                                 # FASE 1: pasta; FASE 2: arquivo 01
├── PathRenameContractTest.groovy                               # FASE 1: pasta; FASE 2/3: caminhos
├── ProcedureContractTest.groovy                                # FASE 1: pasta; FASE 2: arquivos 01/03
├── ProcedureEntryRenameContractTest.groovy                     # FASE 1: pasta
├── ReadmeConsistencyTest.groovy                                # Sem referência à pasta 03; deve continuar passando
└── ContractRuleRenameContractTest.groovy                       # NOVO: contrato phase-aware da pasta e dos 6 nomes

src/test/groovy/run-archetype-post-generate-tests.groovy        # Registra a suíte nova
src/test/resources/projects/*/archetype.properties              # FASE 1: comentários com a pasta

pom.xml                                                         # Sem alteração
src/main/resources/META-INF/                                    # Catálogo sem alteração (35 propriedades / 18 filesets)
src/main/resources/archetype-resources/                         # Sem alteração
```

**Structure Decision**: manter o módulo único existente. A feature não cria módulo de produção, dependência ou estrutura de dados; renomeia a pasta e os seis arquivos no lugar, atualiza referências no corpus operacional e acrescenta uma suíte de contrato. A Fase 1 altera a pasta e suas referências; a Fase 2 altera os arquivos `01`–`03`; a Fase 3 altera `04`–`06` e conclui a varredura. O catálogo, `archetype-resources/`, `pom.xml` e as specs históricas permanecem intocados. O redirect legado é o único arquivo que mantém um caminho antigo.

## Complexity Tracking

Nenhuma violação da constitution; não há complexidade a justificar.

## Faseamento

| Fase | Nome | User Story | Prioridade | Depende de | Testes |
|---|---|---|---|---|---|
| Fase 1 | Renomear a pasta e atualizar as referências à pasta | US1 | P1 | — | Atualizar suítes existentes |
| Fase 2 | Renomear as três primeiras regras e atualizar as referências | US2 | P2 | Fase 1 (caminho da pasta) | Atualizar suítes existentes |
| Fase 3 | Renomear as três últimas regras e atualizar as referências | US3 | P3 | Fase 1, Fase 2 (conferência final) | Atualizar suítes existentes; varredura final |

## Implementation Strategy

### Fundação compartilhada

- Confirmar a linha de base: 19 arquivos, 13 Markdown, 6 JSON, 4 subpastas, 6 arquivos na subpasta 03.
- Registrar SHA-256 dos seis arquivos antes de qualquer mudança, para provar que o conteúdo normativo permanece.
- Materializar `contracts/rename-manifest.md` com o mapa `old → new`, o mapa de tokens, o inventário por arquivo/linha/fase e as exceções.
- Criar `ContractRuleRenameContractTest.groovy` com as verificações phase-aware da pasta, dos seis nomes, das contagens 19/13/6/4, do índice, do README, das fixtures, do redirect legado e da ausência de referências antigas. Registrar a suíte no runner.
- Refatorar `FileRenameContractTest.groovy` para as listas `p1LegacyOld`, `p1Current` e `p1New` e para a detecção `rulePhase`, preservando a lógica de 04 da feature 009.
- Manter `data-model.md`, `contracts/rename-manifest.md` e `quickstart.md` sincronizados com o estado de cada fase: transições, referências e roteiro de validação.
- Confirmar que a pasta de destino e os seis nomes de destino não existem e que nenhum arquivo será sobrescrito.
- Não alterar o conteúdo normativo dos seis arquivos; apenas tokens de referência a nomes.

### Fase 1 — Renomear a pasta e atualizar as referências à pasta (P1)

- Renomear `03-derivacao-regras-de-formacao-do-contrato` para `03-regras-de-formacao-do-contrato` com `Rename-Item`, após pré-checar a ausência do destino.
- Atualizar o índice (linhas 18–23), a porta de entrada (linhas 715–720) e a regra de localização (linha 141) para o novo segmento de pasta, mantendo os seis nomes de arquivo atuais.
- Atualizar o README: link do índice, âncora `id`/`name`, título da subseção, texto/href do link e caminhos em prosa; manter o prefixo `llm-maven-flags-inference-` e trocar apenas o segmento da pasta.
- Atualizar os comentários das quatro fixtures.
- Atualizar as suítes existentes que citam a pasta: `BuildArtifactsTest`, `CatalogSyncContractTest`, `DestinationContractTest`, `FileRenameContractTest`, `HardStopContractTest`, `PathRenameContractTest`, `ProcedureContractTest` e `ProcedureEntryRenameContractTest`.
- Validar a Fase 1 isoladamente: pasta nova existe; pasta antiga não; os seis arquivos mantêm os nomes atuais; índice, README, fixtures e suítes sem referência à pasta antiga; `mvn -B clean test` passa.
- Checkpoint: a Fase 1 deve ser demonstrável sem a Fase 2.

### Fase 2 — Renomear as três primeiras regras e atualizar as referências (P2)

- Renomear `01`, `02` e `03` com `Rename-Item`, em ordem crescente, após pré-checar cada destino.
- Atualizar o índice (linhas 18–20), a porta de entrada (linhas 715–717), a tabela do README (linhas 2171–2173) e as referências nas regras do procedimento.
- Atualizar as suítes que citam os três arquivos: `BuildArtifactsTest`, `DestinationContractTest`, `FileRenameContractTest`, `HardStopContractTest`, `PathRenameContractTest` e `ProcedureContractTest`.
- Atualizar `ContractRuleRenameContractTest` para o estado da Fase 2: `01`–`03` novos, `04`–`06` atuais.
- Validar: 03 com os três nomes novos e os três nomes atuais; 19 arquivos; 0 referências operacionais aos três nomes antigos; `mvn -B clean test` passa.
- Dependência: a Fase 2 depende da Fase 1 apenas para o caminho da pasta; a renomeação dos três arquivos é independente.

### Fase 3 — Renomear as três últimas regras e atualizar as referências (P3)

- Renomear `04`, `05` e `06` com `Rename-Item`, em ordem crescente, após pré-checar cada destino.
- Atualizar o índice (linhas 21–23), a porta de entrada (linhas 718–720), a tabela do README (linhas 2174–2176) e as referências nas regras do procedimento.
- Atualizar as suítes que citam os três arquivos: `BuildArtifactsTest`, `CatalogSyncContractTest`, `DestinationContractTest`, `FileRenameContractTest`, `PathRenameContractTest` e `ProcedureContractTest`.
- Concluir a varredura do corpus operacional: 0 referências à pasta antiga e aos seis nomes antigos, exceto o caminho do redirect legado e as asserções negativas das suítes.
- Atualizar `ContractRuleRenameContractTest` para o estado final: os seis nomes novos.
- Validar: 03 com exatamente seis nomes novos e nenhum `como-`; 19 arquivos, 13 Markdown, 6 JSON, 4 subpastas; 0 referências antigas no corpus operacional; `mvn -B clean test`, `mvn clean install` e `mvn -B clean integration-test` passam.
- Dependência: a Fase 3 depende da Fase 1 e da Fase 2 para a conferência final de zero nomes antigos; a renomeação dos três arquivos é independente.

### Polish

- Revisão final do diff: os seis arquivos renomeados devem ter o conteúdo normativo idêntico ao baseline, exceto pelos tokens de referência; arquivos sem referências aos nomes antigos devem ter hash idêntico.
- `mvn -B clean test`, `mvn clean install`, `mvn -B clean integration-test`.
- `mvn -B test "-Deda.verify.installed.jar=true"` — verificação opt-in do JAR, após `install`.
- Executar os cenários do `quickstart.md` e registrar o resultado.
- Não criar nem remover arquivos do procedimento; não alterar catálogo, `archetype-resources/`, `pom.xml` ou specs históricas.
- Confirmar que `ContractRuleRenameContractTest.groovy` está registrada no runner e que `CoverageRegistryTest` e `ReadmeConsistencyTest` passam.

## Validation Plan

### Fase 1

```powershell
$procedure = "src/main/resources/llm-maven-flags-inference"
Test-Path (Join-Path $procedure "03-regras-de-formacao-do-contrato")
Test-Path (Join-Path $procedure "03-derivacao-regras-de-formacao-do-contrato")
Get-ChildItem (Join-Path $procedure "03-regras-de-formacao-do-contrato") -File |
  Select-Object -ExpandProperty Name
```

Esperado: pasta nova existe; pasta antiga não; 6 arquivos com os nomes atuais (`como-*`).

```powershell
Get-ChildItem README.md, $procedure, "src/test" -Recurse -File |
  Where-Object { $_.FullName -notmatch '\\specs\\|\\graphify-out\\|\\target\\|\\utils\\|ContractRuleRenameContractTest|FileRenameContractTest' } |
  Select-String -Pattern '03-derivacao-regras-de-formacao-do-contrato'
```

Esperado: 0 linhas.

```powershell
mvn -B clean test
```

### Fase 2

```powershell
Get-ChildItem (Join-Path $procedure "03-regras-de-formacao-do-contrato") -File |
  Select-Object -ExpandProperty Name
```

Esperado: `01-derivar-a-entidade-do-contrato.md`, `02-resolver-tipos-e-enums.md`, `03-derivar-flags-de-tipos-aninhados.md` e os três nomes atuais de `04`–`06`.

```powershell
Get-ChildItem README.md, $procedure, "src/test" -Recurse -File |
  Where-Object { $_.FullName -notmatch '\\specs\\|\\graphify-out\\|\\target\\|\\utils\\|ContractRuleRenameContractTest|FileRenameContractTest' } |
  Select-String -Pattern '01-como-derivar-a-entidade-do-contrato|02-como-resolver-tipos-e-enums|03-como-derivar-flags-de-tipos-aninhados'
```

Esperado: 0 linhas, exceto asserções negativas.

```powershell
mvn -B clean test
```

### Fase 3

```powershell
Get-ChildItem (Join-Path $procedure "03-regras-de-formacao-do-contrato") -File |
  Select-Object -ExpandProperty Name
(Get-ChildItem $procedure -Recurse -File).Count
(Get-ChildItem $procedure -Recurse -File -Filter *.md).Count
(Get-ChildItem $procedure -Recurse -File -Filter *.json).Count
```

Esperado: seis nomes novos; 19 arquivos; 13 Markdown; 6 JSON; 4 subpastas.

```powershell
Get-ChildItem README.md, $procedure, "src/test" -Recurse -File |
  Where-Object { $_.FullName -notmatch '\\specs\\|\\graphify-out\\|\\target\\|\\utils\\|ContractRuleRenameContractTest|FileRenameContractTest' } |
  Select-String -Pattern '03-derivacao-regras-de-formacao-do-contrato|0[1-6]-como-'
```

Esperado: 0 linhas, exceto asserções negativas.

```powershell
mvn -B clean test
mvn clean install
mvn -B clean integration-test
mvn -B test "-Deda.verify.installed.jar=true"
```

### Métricas

| Métrica | Procedimento | Aceite |
|---|---|---|
| Pastas renomeadas | Listar `llm-maven-flags-inference/` | 1/1 nome novo; 0 nome antigo como pasta |
| Arquivos renomeados | Listar a subpasta 03 | 6/6 nomes novos; 0 `como-` |
| Conteúdo normativo | Comparar SHA-256/diff com o baseline | 6/6 idênticos, exceto tokens de referência |
| Contagem | `Get-ChildItem` recursivo | 19 arquivos; 13 Markdown; 6 JSON; 4 subpastas; 6 em 03 |
| Referências antigas | `Select-String` no corpus operacional | 0 ocorrências, exceto redirect legado e asserções negativas |
| Índice | Leitura do índice | 19 caminhos novos, na mesma ordem |
| README | Âncoras, título, links e tabela | Nenhum nome antigo; links internos válidos |
| Redirect | Leitura do redirect legado | Permanece no caminho antigo e aponta para `00-entry-point.md` |
| Suítes | `mvn -B clean test` | 100% das suítes passam, incluindo a nova |
| Artefato | `mvn clean install` e verificação opt-in | JAR instalado contém os novos nomes e o redirect legado |

### Negativo (verificações que devem falhar se a feature regredir)

| Alteração provocada | Falha esperada |
|---|---|
| Deixar a pasta 03 com o nome antigo | `ContractRuleRenameContractTest`, `PathRenameContractTest` e `ProcedureEntryRenameContractTest` |
| Deixar um dos seis arquivos com `como-` | `ContractRuleRenameContractTest`, `FileRenameContractTest` e `PathRenameContractTest` |
| Deixar uma referência antiga no procedimento | `ContractRuleRenameContractTest` e as suítes de contrato que leem o caminho |
| Deixar uma referência antiga no README ou nas fixtures | `ContractRuleRenameContractTest` e `ReadmeConsistencyTest` |
| Criar, remover ou mover um arquivo do procedimento | `BuildArtifactsTest`, `PathRenameContractTest` e `ContractRuleRenameContractTest` |
| Renomear as subpastas 01, 02 ou 04 | `PathRenameContractTest` e `ProcedureEntryRenameContractTest` |
| Alterar uma regra, chave JSON ou estrutura de template | `ProcedureContractTest`, `CatalogSyncContractTest` ou a revisão de diff |
| Remover ou renomear o redirect legado | `PathRenameContractTest` |
| Não registrar a suíte nova no runner | `CoverageRegistryTest` e `ReadmeConsistencyTest` |
