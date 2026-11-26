# Implementation Plan: Remover as Preposições "do" e "com" dos Nomes dos Templates de Contrato

**Branch**: `015-remove-contract-template-prepositions` | **Date**: 2026-11-26 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/015-remove-contract-template-prepositions/spec.md`

## Summary

Renomear cinco dos seis arquivos da subpasta `llm-maven-flags-inference/04-templates-de-contrato`: `01-schema-do-contrato.json` → `01-schema-contrato.json` e os arquivos 3 a 6 perdem apenas a preposição `com` (`03-contrato-objetos-aninhados.json`, `04-contrato-enums.json`, `05-contrato-tipos-do-projeto.json` e `06-contrato-attributes-explicito.json`). O arquivo `02-contrato-plano.json` permanece inalterado; o `do` de `tipos-do-projeto` no arquivo 05 permanece. Atualizar as referências operacionais ao nome antigo no mapa dos arquivos e ordem de leitura, na porta de entrada, no README (tabela de templates) e nas suítes de verificação, sem alterar o conteúdo dos JSON, sem criar ou remover arquivos e sem tocar nas subpastas 01 a 03. A implementação segue as duas fases da spec: a Fase 1 renomeia o arquivo 01 e atualiza suas referências; a Fase 2 renomeia os arquivos 3 a 6 e conclui a varredura. O procedimento mantém 19 arquivos (13 Markdown e 6 JSON) em 4 subpastas, com 6 arquivos na subpasta 04. A validação usa `mvn -B clean test` (o `clean` é obrigatório para remover as cópias antigas de `target/classes`) e `mvn clean install` como confirmação.

## Technical Context

**Language/Version**: Markdown e JSON (procedimento promptware); Groovy 4.0.28 (suítes de verificação); Java 17 (constitution) e JDK 21 no CI; Maven 3.9.16. O `pom.xml` não fixa `maven.compiler.release`.

**Primary Dependencies**: Maven Archetype Plugin 3.4.1; archetype-packaging 3.4.1; xml-maven-plugin 1.2.1; groovy-maven-plugin 2.1.1; groovy-json 4.0.28; JUnit 4.13.2; Velocity 2.4.1 (test). Nenhuma dependência nova é necessária.

**Storage**: Sistema de arquivos e JAR; 19 arquivos do procedimento (13 Markdown e 6 JSON) em 4 subpastas, 6 na subpasta 04; catálogo XML com 35 propriedades e 18 filesets; sem banco de dados.

**Testing**: Suítes Groovy em `src/test/archetype-post-generate/`, executadas pelo runner `src/test/groovy/run-archetype-post-generate-tests.groovy` na fase `test`; `mvn -B clean test` é o gate. `mvn clean install` confirma a cópia empacotada. Verificação opt-in do JAR instalado com `-Deda.verify.installed.jar=true` continua disponível, mas não é gate.

**Target Platform**: JAR consumido por agentes e operadores; Windows e POSIX; procedimento sem motor, CLI ou script em runtime.

**Project Type**: Maven archetype / promptware (documentação executável + suítes de contrato).

**Performance Goals**: A feature não define meta própria. Preservar o orçamento da feature 007 — aumento máximo de 10% sobre a linha de base local de `mvn -B test` — como guarda de regressão, não como gate de aceite.

**Constraints**: 5 arquivos renomeados; `02-contrato-plano.json` e a subpasta 04 preservados; conteúdo dos 6 JSON inalterado; apenas nomes e referências textuais mudam; referências operacionais restritas ao mapa dos arquivos e ordem de leitura, à porta de entrada, ao README e às suítes de verificação; specs históricas, planos/tarefas, `target/`, `graphify-out/` e artefato instalado ficam fora; não criar, remover ou mover arquivos do procedimento; não renomear as subpastas 01 a 03; não remover `do` de `tipos-do-projeto`; não alterar regras de formação do contrato, catálogo, templates ou formato do contrato; não expor segredos.

**Scale/Scope**: 5 arquivos renomeados em 2 fases; 19 arquivos no procedimento; 0 arquivos novos ou removidos; 0 suítes novas (2 suítes phase-aware estendidas e 4 suítes de lista fixa atualizadas); 0 dependências novas; 0 módulos novos.

**Paradigm**: NÃO OOP — a feature é procedural/documental; o projeto é um archetype com scripts Groovy, XML/Markdown e templates Java, sem modelagem OOP no escopo. SOLID não se aplica.

**Sources**: `.specify/memory/constitution.md`; `specs/015-remove-contract-template-prepositions/spec.md`; `specs/014-rename-contract-templates/plan.md`; `specs/014-rename-contract-templates/contracts/rename-manifest.md`; `README.md`; `pom.xml`; `src/test/groovy/run-archetype-post-generate-tests.groovy`; `src/test/archetype-post-generate/` (BuildArtifactsTest, PathRenameContractTest, FileRenameContractTest, ProcedureContractTest, ContractRuleRenameContractTest, ContractTemplateRenameContractTest); `src/main/resources/llm-maven-flags-inference/01-inicializacao/00-entry-point.md`; `src/main/resources/llm-maven-flags-inference/01-inicializacao/01-mapa-dos-arquivos-e-ordem-de-leitura.md`.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Princípio | Gate aplicável | Status |
|---|---|---|
| I. Modularidade por Chave de Ativação | Nenhuma flag, capacidade opcional ou comportamento condicional novo; a mudança é de nomenclatura e referências | PASS |
| II. Convenção Fixa de Nomenclatura | Os arquivos do procedimento são promptware empacotado como recurso, não artefatos de extensão selecionados por padrão de build; numeral de dois dígitos e extensão `.json` são preservados, e a subpasta 04 mantém o nome `04-templates-de-contrato` | PASS |
| III. Pacote por Funcionalidade no Código Gerado | Nenhum código Java gerado ou template de entidade é alterado | PASS |
| IV. Binding Duplo de Configuração Externalizada | Nenhuma propriedade externalizada, variável de binding ou configuração é criada ou alterada | PASS |
| V. Credenciais Nunca Entram no Repositório | As alterações são nomes e referências; nenhum segredo, credencial ou valor sensível é adicionado ou exposto | PASS |
| VI. Validar Antes de Culpar o Código | A validação usa `mvn -B clean test` para eliminar cópias antigas de `target/classes` e `mvn clean install` para confirmar o artefato reconstruído | PASS |
| Technology Stack | Java 17, Maven 3.9.16, plugin 3.4.1, Groovy 4.0.28 e dependências existentes permanecem | PASS |

**Reavaliação após o design (Phase 1)**: PASS — o desenho não cria flag, artefato de extensão, dependência, estrutura de dados ou alteração no código gerado. Nenhuma suíte nova é necessária: as duas suítes phase-aware existentes são estendidas e as quatro suítes de lista fixa são atualizadas. O catálogo, `archetype-resources/`, `pom.xml` e os 19 arquivos do procedimento permanecem com a mesma contagem.

## Project Structure

### Documentation (this feature)

```text
specs/015-remove-contract-template-prepositions/
├── plan.md                         # Este arquivo
├── research.md                     # Fase 0: decisões e alternativas
├── data-model.md                   # Fase 1: entidades, invariantes e transições
├── quickstart.md                   # Fase 1: roteiro de validação por fase
├── contracts/
│   └── rename-manifest.md          # Mapa old→new, tokens, inventário e verificações
└── checklists/
    └── requirements.md             # Checklist de qualidade da spec
```

### Source Code (repository root)

```text
src/main/resources/llm-maven-flags-inference/
├── 01-inicializacao/
│   ├── 00-entry-point.md                                        # 721, 723–726: caminhos dos cinco nomes
│   └── 01-mapa-dos-arquivos-e-ordem-de-leitura.md               # 24, 26–29: caminhos dos cinco nomes
├── 02-procedimento-do-agente/                                   # Nomes preservados
├── 03-regras-de-formacao-do-contrato/                           # Nomes preservados
└── 04-templates-de-contrato/                                    # Nome da pasta preservado
    ├── 01-schema-do-contrato.json                               # FASE 1: → 01-schema-contrato.json
    ├── 02-contrato-plano.json                                   # Nome preservado
    ├── 03-contrato-com-objetos-aninhados.json                   # FASE 2: → 03-contrato-objetos-aninhados.json
    ├── 04-contrato-com-enums.json                               # FASE 2: → 04-contrato-enums.json
    ├── 05-contrato-com-tipos-do-projeto.json                    # FASE 2: → 05-contrato-tipos-do-projeto.json
    └── 06-contrato-com-attributes-explicito.json                # FASE 2: → 06-contrato-attributes-explicito.json

README.md                                                        # 2182, 2184–2187: tabela de templates

src/test/archetype-post-generate/
├── BuildArtifactsTest.groovy                                   # 102, 104–107: lista procedureFiles
├── PathRenameContractTest.groovy                               # 61, 63–66: lista procedureFiles
├── FileRenameContractTest.groovy                               # 74–89, 117–149, 182, 199–201: phase-aware
├── ProcedureContractTest.groovy                                # 12, 139, 148, 199: schema e exemplo 03
├── ContractRuleRenameContractTest.groovy                       # 78, 80–83: lista p04
└── ContractTemplateRenameContractTest.groovy                   # 33–57, 88, 126–127, 168: phase-aware

src/test/groovy/run-archetype-post-generate-tests.groovy         # Sem alteração: já registra as seis suítes
```

**Structure Decision**: manter o módulo único existente. A feature não cria módulo, dependência, estrutura de dados ou suíte nova; renomeia cinco arquivos no lugar, atualiza referências no corpus operacional e estende as duas suítes phase-aware já existentes. A Fase 1 altera o schema; a Fase 2 altera os arquivos 3 a 6. O catálogo, `archetype-resources/`, `pom.xml`, a subpasta 04, o arquivo 02 e as specs históricas permanecem intocados.

## Complexity Tracking

Nenhuma violação da constitution; não há complexidade a justificar.

## Faseamento

| Fase | Nome | User Story | Prioridade | Depende de | Testes |
|---|---|---|---|---|---|
| Fase 1 | Schema sem `do` | US1 | P1 | — | Atualizar referências ao arquivo 01 nas 6 suítes |
| Fase 2 | Arquivos 3 a 6 sem `com` | US2 | P2 | — (independente da Fase 1) | Atualizar referências aos arquivos 3 a 6; varredura final |

## Implementation Strategy

### Fundação compartilhada

- Confirmar a linha de base: 19 arquivos, 13 Markdown, 6 JSON, 4 subpastas, 6 arquivos na subpasta 04.
- Registrar SHA-256 dos seis JSON antes de qualquer mudança, para provar que o conteúdo normativo permanece.
- Materializar `contracts/rename-manifest.md` com o mapa `old → new`, o mapa de tokens, o inventário por arquivo/linha/fase e as exceções.
- Confirmar que os cinco nomes de destino não existem e que nenhum arquivo será sobrescrito.
- Não alterar o conteúdo dos seis JSON; apenas tokens de referência a nomes, fora dos arquivos, mudam.
- Não criar suíte nova: `ContractTemplateRenameContractTest.groovy` e `FileRenameContractTest.groovy` passam a reconhecer o estado final desta feature, e as quatro suítes de lista fixa recebem os nomes novos.

### Fase 1 — Schema sem `do` (US1, P1)

- Renomear `src/main/resources/llm-maven-flags-inference/04-templates-de-contrato/01-schema-do-contrato.json` para `01-schema-contrato.json` com `Rename-Item`, sem copiar e apagar.
- Atualizar as referências ao nome antigo:
  - `01-inicializacao/01-mapa-dos-arquivos-e-ordem-de-leitura.md` linha 24.
  - `01-inicializacao/00-entry-point.md` linha 721.
  - `README.md` linha 2182.
- Atualizar as suítes de verificação quanto ao schema:
  - `BuildArtifactsTest.groovy` linha 102.
  - `PathRenameContractTest.groovy` linha 61.
  - `ContractRuleRenameContractTest.groovy` linha 78.
  - `ProcedureContractTest.groovy` linhas 12, 139 e 148.
  - `FileRenameContractTest.groovy`: `p2Current`/`p2New` e a detecção de fase.
  - `ContractTemplateRenameContractTest.groovy`: `p2Current`/`p2New`, a checagem 2.3 e a detecção de fase.
- Manter os arquivos 02 a 06 com os nomes atuais.
- Validar: `01-schema-contrato.json` presente, nome antigo ausente, SHA-256 do schema idêntico, referências atualizadas, `mvn -B clean test` verde.

### Fase 2 — Arquivos 3 a 6 sem `com` (US2, P2)

- Renomear, em ordem crescente de numeral, com `Rename-Item` e pré-checagem de colisão:
  - `03-contrato-com-objetos-aninhados.json` → `03-contrato-objetos-aninhados.json`
  - `04-contrato-com-enums.json` → `04-contrato-enums.json`
  - `05-contrato-com-tipos-do-projeto.json` → `05-contrato-tipos-do-projeto.json`
  - `06-contrato-com-attributes-explicito.json` → `06-contrato-attributes-explicito.json`
- Atualizar as referências aos quatro nomes:
  - `01-inicializacao/01-mapa-dos-arquivos-e-ordem-de-leitura.md` linhas 26–29.
  - `01-inicializacao/00-entry-point.md` linhas 723–726.
  - `README.md` linhas 2184–2187.
- Atualizar as suítes de verificação quanto aos quatro nomes:
  - `BuildArtifactsTest.groovy` linhas 104–107.
  - `PathRenameContractTest.groovy` linhas 63–66.
  - `ContractRuleRenameContractTest.groovy` linhas 80–83.
  - `ProcedureContractTest.groovy` linha 199.
  - `FileRenameContractTest.groovy`: `p2New`/`p3New`, fase, índice, corpus e README.
  - `ContractTemplateRenameContractTest.groovy`: `p2New`/`p3New`, fase, anti-drift e README.
- Manter `02-contrato-plano.json` e a ordem das posições 14 a 19.
- Validar: quatro nomes novos, quatro nomes antigos ausentes, SHA-256 dos seis JSON idêntico, 19/13/6/4 preservados, `mvn -B clean test` e `mvn clean install` verdes.

### Estratégia das suítes phase-aware

- `ContractTemplateRenameContractTest.groovy` mantém as gerações históricas da pasta e dos nomes `exemplo` e passa a reconhecer o estado final desta feature:
  - `p3New` = `01-schema-contrato.json`, `02-contrato-plano.json`, `03-contrato-objetos-aninhados.json`, `04-contrato-enums.json`, `05-contrato-tipos-do-projeto.json`, `06-contrato-attributes-explicito.json`.
  - Detecção independente: schema renomeado quando `01-schema-contrato.json` existe; arquivos 3 a 6 renomeados quando `03-contrato-objetos-aninhados.json` existe.
  - `expected` composto pelos dois estados; os nomes antigos (`01-schema-do-contrato.json` e os quatro `0X-contrato-com-...`) são verificados como ausentes no estado final.
  - A checagem “the schema keeps its name” passa a usar `expected[0]`.
- `FileRenameContractTest.groovy` recebe o mesmo estado final:
  - `p3New` com os seis nomes finais e `p3RenamedCurrent` com os cinco nomes que deixam de existir.
  - `expected04`, as checagens 2.2/2.4, o índice 3.2/3.4, o corpus 4.2 e o README 6.1 compõem os estados 1, 2 e final a partir de `schemaRenamed` e `othersRenamed`.
  - O histórico (`p2LegacyOld`, `p2Current`, `p2New`) permanece para provar que as gerações anteriores não voltaram.
- As suítes de lista fixa (`BuildArtifactsTest`, `PathRenameContractTest`, `ContractRuleRenameContractTest`, `ProcedureContractTest`) trocam apenas os tokens dos cinco nomes; nenhuma asserção negativa é removida.

### Validação final

- `mvn -B clean test` — todas as suítes, com `target/classes` reconstruído sem cópias antigas.
- `mvn clean install` — confirma a cópia empacotada com os nomes novos.
- `mvn -B clean integration-test` — fixtures de integração.
- `-Deda.verify.installed.jar=true` (opt-in) — confere o JAR instalado com os 19 arquivos e os nomes novos; não é gate.
- Varredura final: nenhuma referência operacional aos cinco nomes antigos fora das asserções negativas das suítes e das specs históricas.
