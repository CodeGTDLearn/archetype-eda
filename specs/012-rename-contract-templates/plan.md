# Implementation Plan: Renomear a Subpasta 04 e os Nomes dos Arquivos de Templates de Contrato

**Branch**: `014-rename-contract-templates` | **Date**: 2026-11-26 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/012-rename-contract-templates/spec.md`

## Summary

Renomear a subpasta `04-referencia-templates-de-contrato` para `04-templates-de-contrato` e remover a palavra `exemplo` do corpo do nome dos arquivos 2 a 6: `02-exemplo-contrato-plano.json` → `02-contrato-plano.json`, `03-exemplo-contrato-com-objetos-aninhados.json` → `03-contrato-com-objetos-aninhados.json`, `04-exemplo-contrato-com-enums.json` → `04-contrato-com-enums.json`, `05-exemplo-contrato-com-tipos-do-projeto.json` → `05-contrato-com-tipos-do-projeto.json` e `06-exemplo-contrato-com-attributes-explicito.json` → `06-contrato-com-attributes-explicito.json`. Atualizar todas as referências operacionais — mapa dos arquivos e ordem de leitura, porta de entrada, README (índice, tabela de subpastas, âncora, título da subseção e tabela de templates) e suítes de verificação — sem criar, remover ou mover arquivos do procedimento e sem alterar o conteúdo normativo. A implementação segue as duas fases da spec: a Fase 1 renomeia a pasta e atualiza suas referências; a Fase 2 renomeia os cinco arquivos e atualiza as referências a eles. O procedimento mantém 19 arquivos (13 Markdown e 6 JSON) em 4 subpastas, com 6 arquivos na subpasta 04; catálogo, `archetype-resources/`, `pom.xml` e specs históricas não mudam. A validação usa `mvn -B test`, `mvn clean install`, `mvn -B clean integration-test` e a verificação opt-in do JAR instalado.

## Technical Context

**Language/Version**: Markdown e JSON (procedimento promptware); Groovy 4.0.28 (suítes de verificação); Java 17 (constitution) e JDK 21 no CI; Maven 3.9.16. O `pom.xml` não fixa `maven.compiler.release`.

**Primary Dependencies**: Maven Archetype Plugin 3.4.1; archetype-packaging 3.4.1; xml-maven-plugin 1.2.1; groovy-maven-plugin 2.1.1; groovy-json 4.0.28; JUnit 4.13.2; Velocity 2.4.1 (test). Nenhuma dependência nova é necessária.

**Storage**: Sistema de arquivos e JAR; 19 arquivos do procedimento (13 Markdown e 6 JSON) em 4 subpastas, 6 na subpasta 04; catálogo XML com 35 propriedades e 18 filesets; sem banco de dados.

**Testing**: Suítes Groovy em `src/test/archetype-post-generate/`, executadas pelo runner `src/test/groovy/run-archetype-post-generate-tests.groovy` na fase `test`; `mvn -B test`, `mvn clean install` e `mvn -B clean integration-test`; CI em Windows com JDK 21; verificação opt-in do JAR instalado com `-Deda.verify.installed.jar=true`.

**Target Platform**: JAR consumido por agentes e operadores; Windows e POSIX; procedimento sem motor, CLI ou script em runtime.

**Project Type**: Maven archetype / promptware (documentação executável + suítes de contrato).

**Performance Goals**: A feature não define meta própria. Preservar o orçamento da feature 007 — aumento máximo de 10% sobre a linha de base local de `mvn -B test` — como guarda de regressão, não como gate de aceite.

**Constraints**: 19 arquivos (13 Markdown e 6 JSON), 4 subpastas, 6 arquivos na subpasta 04; conteúdo dos 6 JSON preservado; apenas nomes e referências textuais mudam; referências operacionais no mapa dos arquivos e ordem de leitura, na porta de entrada, no README e nas suítes de verificação; specs históricas, planos/tarefas, `target/`, `graphify-out/` e artefato instalado ficam fora; não criar, remover ou mover arquivos do procedimento; não renomear as subpastas 01 a 03 nem `01-schema-do-contrato.json`; não alterar regras de formação do contrato, catálogo, templates ou formato do contrato; o redirect legado permanece no caminho atual; não expor segredos.

**Scale/Scope**: 1 pasta renomeada e 5 arquivos renomeados em 2 fases; 19 arquivos no procedimento; 0 arquivos novos ou removidos no procedimento; 1 suíte de contrato nova em `src/test/archetype-post-generate/`; 0 dependências novas; 0 módulos novos.

**Paradigm**: NÃO OOP — a feature é procedural/documental; o projeto é um archetype com scripts Groovy, XML/Markdown e templates Java, sem modelagem OOP no escopo. SOLID não se aplica.

**Sources**: `.specify/memory/constitution.md`; `specs/011-rename-contract-rule-files/plan.md`; `specs/011-rename-contract-rule-files/research.md`; `specs/012-rename-contract-templates/spec.md`; `README.md`; `pom.xml`; `.github/workflows/ci.yml`; `src/test/groovy/run-archetype-post-generate-tests.groovy`; `src/test/archetype-post-generate/` (BuildArtifactsTest, PathRenameContractTest, FileRenameContractTest, ProcedureContractTest, ContractRuleRenameContractTest, ProcedureEntryRenameContractTest); `src/main/resources/llm-maven-flags-inference/01-inicializacao/00-entry-point.md`; `src/main/resources/llm-maven-flags-inference/01-inicializacao/01-mapa-dos-arquivos-e-ordem-de-leitura.md`.

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
specs/012-rename-contract-templates/
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
├── 01-inicializacao/                                            # Nomes de pasta e arquivo preservados
│   ├── 00-entry-point.md                                        # FASE 1: bloco de integridade (721–726)
│   └── 01-mapa-dos-arquivos-e-ordem-de-leitura.md               # FASE 1: 24–29; FASE 2: linhas 25–29
├── 02-procedimento-do-agente/                                   # Nomes preservados
├── 03-regras-de-formacao-do-contrato/                           # Nomes preservados
└── 04-templates-de-contrato/                                    # FASE 1: pasta renomeada de 04-referencia-...
    ├── 01-schema-do-contrato.json                               # Nome preservado
    ├── 02-contrato-plano.json                                   # FASE 2: renomeado de 02-exemplo-contrato-plano.json
    ├── 03-contrato-com-objetos-aninhados.json                   # FASE 2: renomeado de 03-exemplo-...
    ├── 04-contrato-com-enums.json                               # FASE 2: renomeado de 04-exemplo-...
    ├── 05-contrato-com-tipos-do-projeto.json                    # FASE 2: renomeado de 05-exemplo-...
    └── 06-contrato-com-attributes-explicito.json                # FASE 2: renomeado de 06-exemplo-...

src/main/resources/utils/01-entrada-inicializacao/
└── prompt-00-leia-este-arquivo-primeiro.md                      # Nome preservado (redirect legado)

README.md                                                        # FASE 1: 55, 610, 652, 821–822 e 2178; FASE 2: 2183–2187

src/test/archetype-post-generate/
├── BuildArtifactsTest.groovy                                   # FASE 1: 85 e 102–107; FASE 2: 102–107
├── PathRenameContractTest.groovy                               # FASE 1: 44 e 61–66
├── FileRenameContractTest.groovy                               # FASE 1: 14, 79–80, 105–116, 123–134; FASE 2: 62–77, 102–134, 173–179
├── ProcedureContractTest.groovy                                # FASE 1: 11, 49, 60; FASE 2: 199
├── ContractRuleRenameContractTest.groovy                       # FASE 1: 77–89; FASE 2: 77–89
├── ProcedureEntryRenameContractTest.groovy                     # FASE 1: 81
└── ContractTemplateRenameContractTest.groovy                   # NOVO: contrato phase-aware da pasta e dos 5 nomes

src/test/groovy/run-archetype-post-generate-tests.groovy         # Registra a suíte nova
```

**Structure Decision**: manter o módulo único existente. A feature não cria módulo de produção, dependência ou estrutura de dados; renomeia a pasta e os cinco arquivos no lugar, atualiza referências no corpus operacional e acrescenta uma suíte de contrato. A Fase 1 altera a pasta e suas referências; a Fase 2 altera os arquivos 2 a 6 e conclui a varredura. O catálogo, `archetype-resources/`, `pom.xml` e as specs históricas permanecem intocados. O redirect legado é o único arquivo que mantém um caminho antigo.

## Complexity Tracking

Nenhuma violação da constitution; não há complexidade a justificar.

## Faseamento

| Fase | Nome | User Story | Prioridade | Depende de | Testes |
|---|---|---|---|---|---|
| Fase 1 | Subpasta `04-templates-de-contrato` | US1 | P1 | — | Atualizar suítes existentes |
| Fase 2 | Arquivos sem "exemplo" | US2 | P2 | Fase 1 (caminho da pasta) | Atualizar suítes existentes; varredura final de nomes antigos |

## Implementation Strategy

### Fundação compartilhada

- Confirmar a linha de base: 19 arquivos, 13 Markdown, 6 JSON, 4 subpastas, 6 arquivos na subpasta 04.
- Registrar SHA-256 dos seis JSON antes de qualquer mudança, para provar que o conteúdo normativo permanece.
- Materializar `contracts/rename-manifest.md` com o mapa `old → new`, o mapa de tokens, o inventário por arquivo/linha/fase e as exceções.
- Criar `ContractTemplateRenameContractTest.groovy` com as verificações phase-aware da pasta, dos cinco nomes, das contagens 19/13/6/4, do índice, do README, das seis suítes existentes e do redirect legado. Registrar a suíte no runner.
- Estender `FileRenameContractTest.groovy` para três gerações de nomes da subpasta 04: `p2LegacyOld` (`template-0X-...`, feature 009 pré-renomeação), `p2Current` (`0X-exemplo-...`, estado inicial desta feature) e `p2New` (`0X-contrato-...`, destino desta feature), com detecção `templatePhase` 0/1/2 (0 = pasta antiga, 1 = pasta nova com nomes atuais, 2 = nomes novos).
- Manter `data-model.md`, `contracts/rename-manifest.md` e `quickstart.md` sincronizados com o estado de cada fase: transições, referências e roteiro de validação.
- Confirmar que a pasta de destino e os cinco nomes de destino não existem e que nenhum arquivo será sobrescrito.
- Não alterar o conteúdo dos seis JSON; apenas tokens de referência a nomes, fora dos arquivos, mudam.

### Fase 1 — Subpasta `04-templates-de-contrato` (US1, P1)

- Renomear `src/main/resources/llm-maven-flags-inference/04-referencia-templates-de-contrato` para `04-templates-de-contrato` com `Rename-Item`, sem copiar e apagar.
- Atualizar as referências ao caminho da pasta:
  - `01-inicializacao/01-mapa-dos-arquivos-e-ordem-de-leitura.md` linhas 24–29 (caminhos completos das posições 14 a 19).
  - `01-inicializacao/00-entry-point.md` linhas 721–726 (bloco de integridade do JAR).
  - `README.md` linhas 55 (link do índice), 610 (lista de composição), 652 (tabela de artefatos), 821 (âncora `id`/`name`), 822 (título `### 04-templates-de-contrato`) e 2178 (cabeçalho da seção de templates).
  - Âncora nova: `llm-maven-flags-inference-04-templates-de-contrato`, tanto no `id`/`name` quanto no `href` do índice e no link de topo.
- Atualizar as suítes de verificação quanto à pasta: `BuildArtifactsTest` (85), `PathRenameContractTest` (44), `FileRenameContractTest` (14, 79–80, 106, 116, 123–134), `ProcedureContractTest` (11, 49, 60), `ContractRuleRenameContractTest` (89), `ProcedureEntryRenameContractTest` (81).
- Manter os seis arquivos com os nomes atuais dentro da pasta nova.
- Não tocar nas subpastas 01, 02 e 03 nem no redirect legado.
- Validar: pasta nova existe, pasta antiga não existe, 6 arquivos preservados, índice e README sem o caminho antigo, `mvn -B test` verde.

### Fase 2 — Arquivos sem "exemplo" (US2, P2)

- Renomear, em ordem crescente de numeral, com `Rename-Item` e pré-checagem de colisão:
  - `02-exemplo-contrato-plano.json` → `02-contrato-plano.json`
  - `03-exemplo-contrato-com-objetos-aninhados.json` → `03-contrato-com-objetos-aninhados.json`
  - `04-exemplo-contrato-com-enums.json` → `04-contrato-com-enums.json`
  - `05-exemplo-contrato-com-tipos-do-projeto.json` → `05-contrato-com-tipos-do-projeto.json`
  - `06-exemplo-contrato-com-attributes-explicito.json` → `06-contrato-com-attributes-explicito.json`
- Atualizar as referências aos cinco nomes:
  - `01-inicializacao/01-mapa-dos-arquivos-e-ordem-de-leitura.md` linhas 25–29 (posições 15 a 19).
  - `01-inicializacao/00-entry-point.md` linhas 722–726.
  - `README.md` linhas 2183–2187 (tabela de templates).
- Atualizar as suítes de verificação quanto aos nomes: `BuildArtifactsTest` (102–107), `PathRenameContractTest` (61–66), `FileRenameContractTest` (62–77, 102–134, 173–179), `ProcedureContractTest` (199), `ContractRuleRenameContractTest` (77–89).
- Manter `01-schema-do-contrato.json` intacto e a ordem das posições 14 a 19.
- Validar: cinco nomes novos, cinco nomes antigos ausentes, SHA-256 dos seis JSON idêntico, 19/13/6/4 preservados, `mvn -B test` e `mvn clean install` verdes.

### Validação final

- `mvn -B test` — todas as suítes, incluindo a nova suíte de contrato.
- `mvn clean install` — reconstrói `target/classes` com os nomes novos.
- `mvn -B clean integration-test` — fixtures de integração.
- `-Deda.verify.installed.jar=true` (opt-in) — confere o JAR instalado com os 19 arquivos e os nomes novos; não é gate.
- Varredura final: nenhuma referência operacional aos nomes antigos fora das asserções negativas das suítes e do caminho do redirect legado.
