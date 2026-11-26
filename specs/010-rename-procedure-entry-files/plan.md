# Implementation Plan: Renomear os Arquivos de Entrada e Operação do Procedimento

**Branch**: `011-rename-procedure-entry-files` | **Date**: 2026-11-25 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/010-rename-procedure-entry-files/spec.md`

## Summary

Renomear os sete arquivos das subpastas `01-entrada-inicializacao` e `02-operacao-procedimento-do-agente` do procedimento empacotado `llm-maven-flags-inference`: os dois arquivos de 01 passam a `00-entry-point.md` e `01-mapa-dos-arquivos-e-ordem-de-leitura.md`; os cinco de 02 perdem o prefixo `prompt-` e passam a `01-papel-e-restricoes-do-agente.md` a `05-entregaveis-e-relatorios.md`. Atualizar todas as referências operacionais — índice, arquivos do procedimento, README, redirect legado e suítes de verificação — sem criar, remover ou mover arquivos do procedimento e sem alterar o conteúdo normativo. A implementação é dividida nas duas fases da spec: a Fase 1 renomeia 01 e atualiza suas referências; a Fase 2 renomeia 02, atualiza suas referências e conclui a varredura. O procedimento mantém 19 arquivos (13 Markdown e 6 JSON) em 4 subpastas; o catálogo, `archetype-resources/`, `pom.xml` e as specs históricas não mudam. A validação usa `mvn -B test`, `mvn clean install`, `mvn -B clean integration-test` e a verificação opt-in do JAR instalado.

## Technical Context

**Language/Version**: Markdown e JSON (procedimento promptware); Groovy 4.0.28 (suítes de contrato); Java 17 (constitution) e JDK 21 no CI; Maven 3.9.16. O `pom.xml` não fixa `maven.compiler.release`.

**Primary Dependencies**: Maven Archetype Plugin 3.4.1; archetype-packaging 3.4.1; xml-maven-plugin 1.2.1; groovy-maven-plugin 2.1.1; groovy-json 4.0.28; JUnit 4.13.2; Velocity 2.4.1 (test). Nenhuma dependência nova é necessária.

**Storage**: Sistema de arquivos e JAR; 19 arquivos do procedimento (13 Markdown e 6 JSON) em 4 subpastas; catálogo XML com 35 propriedades e 18 filesets; sem banco de dados.

**Testing**: Suítes Groovy em `src/test/archetype-post-generate/`, executadas pelo runner `src/test/groovy/run-archetype-post-generate-tests.groovy` na fase `test`; `mvn -B test`, `mvn clean install` e `mvn -B clean integration-test`; CI em Windows com JDK 21; verificação opt-in do JAR instalado com `-Deda.verify.installed.jar=true`.

**Target Platform**: JAR consumido por agentes e operadores; Windows e POSIX; procedimento sem motor, CLI ou script em runtime.

**Project Type**: Maven archetype / promptware (documentação executável + suítes de contrato).

**Performance Goals**: A feature não define meta própria. Preservar o orçamento da feature 007 — aumento máximo de 10% sobre a linha de base local de `mvn -B test` — como guarda de regressão, não como gate de aceite.

**Constraints**: 19 arquivos (13 Markdown e 6 JSON), 4 subpastas, 2 arquivos em 01 e 5 em 02; conteúdo normativo dos 7 arquivos preservado; apenas referências textuais aos nomes antigos atualizadas; referências operacionais no índice, procedimento, README, redirect legado e suítes de verificação; specs históricas, planos/tarefas e saída gerada ficam fora; não criar, remover ou mover arquivos do procedimento; não renomear subpastas; não alterar regras, catálogo, templates, ordem de leitura ou formato do contrato; não expor segredos; o JAR instalado é a fonte de runtime.

**Scale/Scope**: 7 arquivos renomeados em 2 fases; 2 subpastas; 19 arquivos no procedimento (13 Markdown e 6 JSON) em 4 subpastas; 0 arquivos novos ou removidos no procedimento; 1 suíte de contrato nova em `src/test/archetype-post-generate/`; 0 dependências novas.

**Paradigm**: NÃO OOP — a feature é procedural/documental; o projeto é um archetype com scripts Groovy, XML/Markdown e templates Java, sem modelagem OOP no escopo.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Princípio | Gate aplicável | Status |
|---|---|---|
| I. Modularidade por Chave de Ativação | Nenhuma flag, capacidade opcional ou comportamento condicional novo; a mudança é de nomenclatura do procedimento e de suas referências | PASS |
| II. Convenção Fixa de Nomenclatura | Os arquivos do procedimento são promptware empacotado como recurso, não artefatos de extensão selecionados por padrão de build; a estrutura de 4 subpastas e a contagem de 19 arquivos permanecem, e os novos nomes seguem a numeração da própria subpasta. O redirect legado mantém o nome antigo de propósito, como compatibilidade, e não é artefato de extensão | PASS |
| III. Pacote por Funcionalidade no Código Gerado | Nenhum código Java gerado ou template de entidade é alterado | PASS |
| IV. Binding Duplo de Configuração Externalizada | Nenhuma propriedade externalizada, variável de binding ou configuração é criada ou alterada | PASS |
| V. Credenciais Nunca Entram no Repositório | As alterações são nomes e referências; nenhum segredo, credencial ou valor sensível é adicionado ou exposto | PASS |
| VI. Validar Antes de Culpar o Código | A validação reinstala o artefato com `mvn clean install` e confere a cópia empacotada; a verificação opt-in do JAR instalado continua disponível | PASS |
| Technology Stack | Java 17, Maven 3.9.16, plugin 3.4.1, Groovy 4.0.28 e dependências existentes permanecem | PASS |

**Reavaliação após a Fase 1**: PASS — o desenho não cria flag, artefato de extensão, dependência, estrutura de dados ou alteração no código gerado. A única adição fora do procedimento é uma suíte de contrato em `src/test/archetype-post-generate/`, registrada no runner; o catálogo, `archetype-resources/` e os 19 arquivos do procedimento permanecem com a mesma contagem.

## Project Structure

### Documentation (this feature)

```text
specs/010-rename-procedure-entry-files/
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
├── 01-entrada-inicializacao/
│   ├── 00-entry-point.md                                      # FASE 1: renomeado de prompt-00-leia-este-arquivo-primeiro.md
│   └── 01-mapa-dos-arquivos-e-ordem-de-leitura.md             # FASE 1: renomeado de indice-01-mapa-dos-arquivos-e-ordem-de-leitura.md
├── 02-operacao-procedimento-do-agente/
│   ├── 01-papel-e-restricoes-do-agente.md                     # FASE 2: renomeado de prompt-01-papel-e-restricoes-do-agente.md
│   ├── 02-como-localizar-e-ler-o-archetype.md                 # FASE 2: renomeado de prompt-02-como-localizar-e-ler-o-archetype.md
│   ├── 03-regras-obrigatorias-de-geracao.md                   # FASE 2: renomeado de prompt-03-regras-obrigatorias-de-geracao.md
│   ├── 04-fluxo-de-execucao-e-verificacao.md                  # FASE 2: renomeado de prompt-04-fluxo-de-execucao-e-verificacao.md
│   └── 05-entregaveis-e-relatorios.md                         # FASE 2: renomeado de prompt-05-entregaveis-e-relatorios.md
├── 03-derivacao-regras-de-formacao-do-contrato/               # Nomes preservados; referências a prompt-00 atualizadas
└── 04-referencia-templates-de-contrato/                       # Nomes preservados; sem referências aos 7 nomes

src/main/resources/utils/01-entrada-inicializacao/
└── prompt-00-leia-este-arquivo-primeiro.md                    # Nome preservado (redirect legado); conteúdo aponta para 00-entry-point.md

README.md                                                      # FASE 1: referências de entrada/índice; FASE 2: prompt-03/04/05

src/test/archetype-post-generate/
├── BuildArtifactsTest.groovy                                  # Lista dos 19 caminhos e caminho do índice
├── PathRenameContractTest.groovy                              # 19 caminhos, canonical, redirect legado e README
├── HardStopContractTest.groovy                                # Caminho da entrada e do JAR
├── DestinationContractTest.groovy                             # Caminhos de entrada, 03, 04 e 05
├── CatalogSyncContractTest.groovy                             # Caminho de prompt-03
├── ReadmeConsistencyTest.groovy                               # Caminho da entrada e de prompt-04
├── FileRenameContractTest.groovy                              # Caminho do índice
├── ProcedureEntryRenameContractTest.groovy                    # NOVO: contrato dos 7 nomes, índice, redirect e anti-drift
└── ProcedureContractTest.groovy                               # Sem alteração de caminho; valida os paths citados

src/test/groovy/run-archetype-post-generate-tests.groovy       # Registra a nova suíte

pom.xml                                                        # Sem alteração
src/main/resources/META-INF/                                  # Catálogo sem alteração (35 propriedades / 18 filesets)
src/main/resources/archetype-resources/                        # Sem alteração
```

**Structure Decision**: manter o módulo único existente. A feature não cria módulo de produção, dependência ou estrutura de dados; renomeia arquivos no lugar, atualiza referências no corpus operacional e acrescenta uma suíte de contrato. A Fase 1 altera 01 e suas referências; a Fase 2 altera 02, conclui o README e a varredura. O catálogo, `archetype-resources/`, `pom.xml` e as specs históricas permanecem intocados. O redirect legado é o único arquivo que mantém um nome antigo.

## Faseamento

| Fase | Nome | User Story | Prioridade | Depende de | Testes |
|---|---|---|---|---|---|
| Fase 1 | Subpasta de entrada e inicialização | US1 | P1 | — | Atualizar suítes existentes; nova suíte `ProcedureEntryRenameContractTest` |
| Fase 2 | Subpasta de operação e procedimento do agente | US2 | P2 | Fase 1 (conferência final) | Atualizar suítes existentes; varredura final |

## Implementation Strategy

### Fundação compartilhada

- Copiar os sete arquivos para um diretório temporário fora do repositório e registrar SHA-256 antes de qualquer mudança.
- Confirmar a linha de base: 19 arquivos, 13 Markdown, 6 JSON, 4 subpastas, 2 arquivos em 01 e 5 em 02.
- Materializar `contracts/rename-manifest.md` com o mapa `old → new`, o mapa de tokens curtos, o inventário de referências por arquivo/linha/fase e as exceções.
- Criar `ProcedureEntryRenameContractTest.groovy` com as verificações compartilhadas e phase-aware: na Fase 1, 01 tem os dois nomes novos e 02 ainda tem os cinco nomes antigos; na Fase 2, os sete nomes são novos. Registrar a suíte no runner.
- Confirmar que os sete nomes novos não existem e que nenhum arquivo será sobrescrito.
- Não alterar o conteúdo normativo dos sete arquivos; apenas tokens de referência a nomes de arquivo.

### Fase 1 — Subpasta de entrada e inicialização (P1)

- Renomear `prompt-00-leia-este-arquivo-primeiro.md` para `00-entry-point.md` com `Rename-Item` (não copiar e apagar), após pré-checar a ausência do destino.
- Renomear `indice-01-mapa-dos-arquivos-e-ordem-de-leitura.md` para `01-mapa-dos-arquivos-e-ordem-de-leitura.md`, na mesma subpasta.
- Atualizar o índice: linhas 1 e 2 da tabela, a ordem global e as referências internas aos dois nomes novos.
- Atualizar as referências aos dois arquivos em todos os arquivos operacionais do procedimento, inclusive dentro dos cinco arquivos de 02 (que mantêm os nomes antigos nesta fase), nos arquivos de 03 e no README.
- Atualizar o redirect legado em `src/main/resources/utils/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md` para apontar para `00-entry-point.md`; não renomear nem remover o redirect.
- Atualizar as suítes que citam a entrada e o índice: `BuildArtifactsTest`, `PathRenameContractTest`, `HardStopContractTest`, `DestinationContractTest`, `ReadmeConsistencyTest` e `FileRenameContractTest`; ajustar `PathRenameContractTest` para separar o caminho canônico novo do caminho legado do redirect.
- Validar a Fase 1 isoladamente: 01 com exatamente dois nomes novos; 02 ainda com os cinco nomes antigos; índice listando os novos caminhos de 01 e os antigos de 02; redirect apontando para `00-entry-point.md`; 0 referências textuais aos dois nomes antigos, exceto o caminho do redirect legado e as asserções negativas das suítes; `mvn -B clean test` passa.
- Checkpoint: a Fase 1 deve ser demonstrável sem a Fase 2.

### Fase 2 — Subpasta de operação e procedimento do agente (P2)

- Renomear os cinco arquivos de 02 com `Rename-Item`, em ordem crescente, após pré-checar cada destino.
- Atualizar o índice: linhas 3 a 7 da tabela e a ordem global.
- Atualizar as referências aos cinco arquivos em todos os arquivos operacionais do procedimento, incluindo as referências de `prompt-04` a `prompt-03`, as referências de `prompt-01` e `prompt-03` a `prompt-00` (já renomeado) e o registro de paradas da entrada.
- Atualizar o README: referências curtas e caminhos completos de `prompt-03`, `prompt-04` e `prompt-05`; manter os links de diretório.
- Atualizar as suítes que citam os cinco arquivos: `BuildArtifactsTest`, `PathRenameContractTest`, `DestinationContractTest`, `CatalogSyncContractTest`, `ReadmeConsistencyTest` e `ProcedureEntryRenameContractTest`.
- Concluir a varredura do corpus operacional: 0 referências textuais a `prompt-0[0-5]` e `indice-01`, exceto o caminho do redirect legado e as asserções negativas da própria suíte.
- Atualizar o README e o índice para refletir os 19 arquivos com os novos nomes.
- Validar a Fase 2: 02 com exatamente cinco nomes novos e nenhum `prompt-*`; 19 arquivos, 13 Markdown, 6 JSON, 4 subpastas; 0 referências antigas no corpus operacional; `mvn -B clean test`, `mvn clean install` e `mvn -B clean integration-test` passam.
- Dependência: a Fase 2 depende do estado final dos arquivos compartilhados (índice, entrada, README e testes) e da contagem de 19; a renomeação dos cinco arquivos de 02 é independente da Fase 1, mas a conferência final de zero nomes antigos depende dela.

### Polish

- Revisão final do diff: os sete arquivos renomeados devem ter o conteúdo normativo idêntico ao baseline, exceto pelos tokens de referência; arquivos sem referências aos nomes antigos devem ter hash idêntico.
- `mvn -B clean test`, `mvn clean install`, `mvn -B clean integration-test`.
- `mvn -B test "-Deda.verify.installed.jar=true"` — verificação opt-in do JAR, após `install`.
- Executar os cenários do `quickstart.md` e registrar o resultado.
- Não criar nem remover arquivos do procedimento; não alterar catálogo, `archetype-resources/`, `pom.xml` ou specs históricas.
- Confirmar que `ProcedureEntryRenameContractTest.groovy` está registrada no runner e que `CoverageRegistryTest` e `ReadmeConsistencyTest` passam.

## Validation Plan

### Fase 1

```powershell
$procedure = "src/main/resources/llm-maven-flags-inference"
Get-ChildItem (Join-Path $procedure "01-entrada-inicializacao") -File |
  Select-Object -ExpandProperty Name
(Get-ChildItem (Join-Path $procedure "01-entrada-inicializacao") -File).Count
```

Esperado: exatamente `00-entry-point.md` e `01-mapa-dos-arquivos-e-ordem-de-leitura.md`; 2 arquivos; 0 nomes antigos como nome de arquivo em 01; 02 ainda com os cinco nomes antigos; índice com os novos caminhos de 01; redirect apontando para `00-entry-point.md`.

```powershell
Get-ChildItem README.md, $procedure, "src/test" -Recurse -File |
  Where-Object { $_.FullName -notmatch '\\specs\\|\\graphify-out\\|\\target\\|\\utils\\|ProcedureEntryRenameContractTest|PathRenameContractTest' } |
  Select-String -Pattern 'prompt-00|indice-01'
```

Esperado: 0 linhas, exceto as asserções negativas das suítes e o caminho do redirect legado.

```powershell
mvn -B clean test
```

### Fase 2

```powershell
$procedure = "src/main/resources/llm-maven-flags-inference"
Get-ChildItem (Join-Path $procedure "02-operacao-procedimento-do-agente") -File |
  Select-Object -ExpandProperty Name
(Get-ChildItem $procedure -Recurse -File).Count
(Get-ChildItem $procedure -Recurse -File -Filter *.md).Count
(Get-ChildItem $procedure -Recurse -File -Filter *.json).Count
```

Esperado: exatamente os cinco nomes novos; 0 `prompt-*`; 19 arquivos; 13 Markdown; 6 JSON; 4 subpastas.

```powershell
Get-ChildItem README.md, $procedure, "src/test" -Recurse -File |
  Where-Object { $_.FullName -notmatch '\\specs\\|\\graphify-out\\|\\target\\|\\utils\\|ProcedureEntryRenameContractTest|PathRenameContractTest' } |
  Select-String -Pattern 'prompt-0[0-5]|indice-01'
```

Esperado: 0 linhas, exceto as asserções negativas das suítes e o caminho do redirect legado.

```powershell
mvn -B clean test
mvn clean install
mvn -B clean integration-test
mvn -B test "-Deda.verify.installed.jar=true"
```

### Métricas

| Métrica | Procedimento | Aceite |
|---|---|---|
| Arquivos renomeados | Listar 01 e 02 | 7/7 nomes novos; 0 nomes antigos como arquivo |
| Conteúdo normativo | Comparar com o baseline; revisar diff | 7/7 idênticos, exceto tokens de referência |
| Contagem | `Get-ChildItem` recursivo | 19 arquivos; 13 Markdown; 6 JSON; 4 subpastas; 2 em 01; 5 em 02 |
| Referências antigas | `Select-String` no corpus operacional | 0 ocorrências, exceto redirect legado e asserções negativas |
| Índice | Leitura do índice | 19 caminhos novos, na mesma ordem |
| Redirect | Leitura do redirect legado | Permanece no caminho antigo e aponta para `00-entry-point.md` |
| Suítes | `mvn -B clean test` | 100% das suítes passam, incluindo a nova |
| Artefato | `mvn clean install` e verificação opt-in | JAR instalado contém os novos nomes e o redirect legado |

### Negativo (verificações que devem falhar se a feature regredir)

| Alteração provocada | Falha esperada |
|---|---|
| Deixar um arquivo com nome antigo em 01 ou 02 | `ProcedureEntryRenameContractTest` e `PathRenameContractTest` nomeiam o arquivo |
| Deixar uma referência antiga no procedimento ou README | `ProcedureEntryRenameContractTest` nomeia o arquivo e a linha |
| Criar ou remover um arquivo do procedimento | `BuildArtifactsTest`, `PathRenameContractTest` e `ProcedureEntryRenameContractTest` falham na contagem 19/13/6 |
| Renomear uma subpasta | `PathRenameContractTest` e `ProcedureEntryRenameContractTest` falham |
| Alterar uma regra, chave JSON ou estrutura de template | `ProcedureContractTest`, `CatalogSyncContractTest` ou a revisão de diff falham |
| Remover ou renomear o redirect legado | `PathRenameContractTest` e `ProcedureEntryRenameContractTest` falham |
| Não registrar a nova suíte no runner | `CoverageRegistryTest` e `ReadmeConsistencyTest` falham |
| Deixar o README sem o novo caminho da entrada | `ReadmeConsistencyTest` falha |
| Deixar o índice sem um dos 19 caminhos novos | `BuildArtifactsTest` e `FileRenameContractTest` falham |

## Risks and Mitigations

| Risco | Mitigação |
|---|---|
| Uma referência antiga escapar na Fase 1 | Manifesto por arquivo/linha + `ProcedureEntryRenameContractTest` + varredura final |
| A Fase 1 renomear os cinco arquivos de 02 | Checklist de fase: 02 mantém os nomes antigos até a Fase 2 |
| O mesmo token aparecer em contexto não referencial | Substituir apenas nomes completos e tokens conhecidos `prompt-00`, `indice-01` e `prompt-0[1-5]` |
| O redirect legado ser confundido com referência quebrada | Exceção explícita no manifesto e na suíte; o nome do redirect permanece |
| `PathRenameContractTest` misturar caminho canônico e caminho legado | Separar `entryRelative` (novo) de `legacyEntry` (redirect) e ajustar as asserções |
| `FileRenameContractTest` apontar para o índice antigo | Atualizar o caminho do índice na Fase 1 |
| `target/classes` ficar obsoleto | `mvn clean install` antes da verificação do artefato; `mvn -B clean test` após process-resources |
| A nova suíte não ser registrada no runner | Atualizar o runner; `CoverageRegistryTest` e `ReadmeConsistencyTest` falham se faltar |
| Conteúdo normativo mudar junto com a referência | Baseline + revisão de diff; apenas tokens podem diferir |
| Specs históricas, `graphify-out/` ou `target/` serem reescritos | Exclusão explícita no manifesto e na varredura |
| Colisão ou sobrescrita de nome | Pré-checagem de destino; abortar a subpasta antes de aplicar |

## Complexity Tracking

Nenhuma violação. Não há entradas nesta tabela.
