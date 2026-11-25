# Implementation Plan: Renomear Arquivos das Subpastas 01 e 02 em Três Fases

**Branch**: `008-rename-procedure-files` | **Date**: 2026-11-25 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/008-rename-procedure-files/spec.md`

## Summary

Renomear sete arquivos do procedimento empacotado nas Fases 1 e 2 e, numa terceira fase dependente, substituir 112 ocorrências dos sete nomes antigos em 13 arquivos operacionais. A Fase 3 cobre README, conteúdo do índice, conteúdo da entrada, os três arquivos da subpasta 02 que citam nomes antigos, o redirect em `utils/` e as seis suítes de contrato. As subpastas 03 e 04 permanecem totalmente intactas, inclusive as 3 referências antigas em `regras-01` e `regras-04`, que ficam para uma feature futura. As Fases 1 e 2 deixam `mvn -B test` vermelho nas suítes conhecidas; a Fase 3 as atualiza e restaura o verde. Nenhuma dependência nova e nenhum código de produção novo.

## Technical Context

**Language/Version**: Markdown no conteúdo do procedimento; Groovy 4.0.28 nas suítes de contrato; build declarado com Java 17 e Maven 3.9.16; CI usa JDK 21 e o `pom.xml` não fixa `maven.compiler.release`.

**Primary Dependencies**: Maven Archetype Plugin 3.4.1; `archetype-packaging` 3.4.1; `xml-maven-plugin` 1.2.1; `groovy-maven-plugin` 2.1.1; `groovy-json` 4.0.28 (teste); JUnit 4.13.2 (teste); `velocity-engine-core` 2.4.1 (teste). Nenhuma dependência nova.

**Storage**: Sistema de arquivos e JAR; 19 arquivos no procedimento (13 Markdown e 6 JSON) em 4 subpastas; catálogo XML com 35 propriedades e 18 filesets; sem banco de dados.

**Testing**: Suítes Groovy em `src/test/archetype-post-generate/`, executadas pelo runner `src/test/groovy/run-archetype-post-generate-tests.groovy` na fase `test` via `mvn -B test`. A Fase 1 deixa vermelhas cinco suítes: `PathRenameContractTest`, `BuildArtifactsTest`, `HardStopContractTest`, `DestinationContractTest` e `ReadmeConsistencyTest`; a Fase 2 acrescenta `CatalogSyncContractTest`. A Fase 3 atualiza as seis suítes e restaura `mvn -B test` verde. A verificação de 0 tokens é limitada às 13 superfícies da FR-013; as 3 referências em `regras-01` e `regras-04` ficam fora e não são verificadas. `mvn -B clean install` é paridade opcional com o CI.

**Target Platform**: JAR consumido por agentes e operadores; Windows e POSIX; CI em `windows-latest`.

**Project Type**: Maven archetype / promptware — documentação executável empacotada mais suítes de contrato.

**Performance Goals**: Nenhuma meta específica nesta feature; o baseline do projeto é aumento de no máximo 10% no build de `mvn -B test` sobre a linha de base local. A feature não altera runtime nem dependências.

**Constraints**: Manter 19 arquivos e 4 subpastas; renomear apenas nas subpastas 01 e 02; preservar o conteúdo nas Fases 1 e 2; na Fase 3 alterar apenas referências de caminho e nome nas superfícies da FR-013; as subpastas 03 e 04 permanecem totalmente intactas, inclusive referências; não criar aliases, redirects ou cópias; não reescrever specs históricas 003–007, `target/` nem `graphify-out/`; a Fase 3 depende das Fases 1 e 2. As 3 referências antigas em `regras-01` e `regras-04` são explicitamente diferidas para uma feature futura.

**Scale/Scope**: 7 renomes — 2 na subpasta 01 e 5 na subpasta 02 — mais 112 ocorrências de nomes antigos em 13 arquivos operacionais. Fora do escopo: 3 ocorrências em 2 arquivos da subpasta 03. 3 fases; 19 arquivos no total.

**Paradigma**: NÃO OOP — o código do repositório é composto por scripts Groovy, XML e Markdown; o código Java OOP existe apenas nos projetos gerados e fica fora desta feature.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Princípio | Gate aplicável | Status |
|---|---|---|
| I. Modularidade por Chave de Ativação | Nenhuma flag, capacidade opcional ou comportamento condicional é criado; a mudança é de caminho e de referência | PASS |
| II. Convenção Fixa de Nomenclatura para Artefatos de Extensão | A feature não cria artefato de extensão; os 19 arquivos e as 4 subpastas permanecem, e a montagem copia `src/main/resources` sem selecionar o procedimento por padrão de nome | PASS |
| III. Pacote por Funcionalidade no Código Gerado | Nenhum código Java gerado ou template de entidade é alterado | PASS |
| IV. Binding Duplo de Configuração Externalizada | Nenhuma propriedade externalizada, variável de binding ou configuração é criada ou alterada | PASS |
| V. Credenciais Nunca Entram no Repositório | A feature não lê nem grava credenciais; nenhum valor sensível é manipulado | PASS |
| VI. Validar Antes de Culpar o Código | A Fase 3 fecha com `mvn -B test`; a paridade opcional `mvn -B clean install` valida o JAR e o catálogo local | PASS |
| Technology Stack | Java 17, Maven 3.9.16, Spring Boot 4.0.0, plugin 3.4.1 e dependências existentes permanecem | PASS |

**Reavaliação após a Fase 1**: PASS — o desenho não cria flag, artefato de extensão, dependência, estrutura de dados ou alteração no código gerado. As Fases 1 e 2 mudam apenas caminhos; a Fase 3 muda apenas referências de caminho e nome, sem tocar nas subpastas 03 e 04.

## Project Structure

### Documentation (this feature)

```text
specs/008-rename-procedure-files/
├── plan.md                          # Este arquivo
├── research.md                      # Fase 0: decisões e inventário
├── data-model.md                    # Fase 1: entidades e mapas
├── quickstart.md                    # Fase 1: roteiro de validação
├── contracts/
│   ├── file-rename-contract.md      # Contrato das Fases 1 e 2
│   └── reference-update-contract.md # Contrato da Fase 3
├── checklists/
│   └── requirements.md              # Checklist de qualidade da spec
└── spec.md                          # Especificação da feature
```

### Source Code (repository root)

```text
src/main/resources/llm-maven-flags-inference/
├── 01-entrada-inicializacao/
│   ├── 01-entryPoint.md                              # Fase 1: renomeado; Fase 3 atualiza referências
│   └── 02-mapa-dos-arquivos-e-ordem-de-leitura.md    # Fase 1: renomeado; Fase 3 atualiza as 7 linhas de caminho e a ordem global
├── 02-operacao-procedimento-do-agente/
│   ├── 01-papel-e-restricoes-do-agente.md            # Fase 2: renomeado; Fase 3 atualiza `prompt-00`
│   ├── 02-como-localizar-e-ler-o-archetype.md        # Fase 2: renomeado; sem referência antiga
│   ├── 03-regras-obrigatorias-de-geracao.md          # Fase 2: renomeado; Fase 3 atualiza `prompt-00`
│   ├── 04-fluxo-de-execucao-e-verificacao.md         # Fase 2: renomeado; Fase 3 atualiza `prompt-03`
│   └── 05-entregaveis-e-relatorios.md                # Fase 2: renomeado; sem referência antiga
├── 03-derivacao-regras-de-formacao-do-contrato/      # TOTALMENTE INTOCADO; 3 referências antigas ficam para feature futura
└── 04-referencia-templates-de-contrato/              # TOTALMENTE INTOCADO; 0 referências antigas

src/main/resources/utils/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md
                                                     # NÃO renomeado; Fase 3 atualiza o alvo do redirect

README.md                                            # Fase 3: 15 referências
src/test/archetype-post-generate/                    # Fase 3: 44 referências em 6 suítes
src/test/resources/projects/                         # Fase 3: 0 referências
.specify/templates/                                  # Fase 3: 0 referências a `prompt-0N`
pom.xml, .github/workflows/ci.yml                    # Fase 3: 0 referências
specs/003..007, target/, graphify-out/               # NÃO tocados
```

**Estrutura da subpasta 03 (intocada)**:

```text
03-derivacao-regras-de-formacao-do-contrato/
├── regras-01-como-derivar-a-entidade-do-contrato.md   # NÃO tocado; 1 referência antiga fica para feature futura
├── regras-02-como-resolver-tipos-e-enums.md           # NÃO tocado
├── regras-03-como-derivar-flags-de-tipos-aninhados.md # NÃO tocado
├── regras-04-como-aplicar-precedencia-e-validacao.md  # NÃO tocado; 2 referências antigas ficam para feature futura
├── regras-05-como-materializar-e-emitir-o-contrato.md # NÃO tocado
└── regras-06-como-relatar-proveniencia.md             # NÃO tocado
```

**Structure Decision**: mantém o módulo Maven único existente. A feature não cria diretório de produção, dependência ou estrutura de dados. As Fases 1 e 2 renomeiam arquivos nas subpastas 01 e 02; a Fase 3 atualiza referências em 13 arquivos operacionais e não toca nas subpastas 03 e 04. `target/` continua sendo saída gerada e não é editado manualmente.

## Phase 0 — Research

Ver [research.md](./research.md) para o inventário das 112 ocorrências, o escopo diferido de 03/04, a ordem de substituição dos tokens, o mecanismo de rename, a prova de conteúdo e o gate de build.

## Phase 1 — Design

Ver [data-model.md](./data-model.md), [contracts/file-rename-contract.md](./contracts/file-rename-contract.md), [contracts/reference-update-contract.md](./contracts/reference-update-contract.md) e [quickstart.md](./quickstart.md).

## Implementation Strategy

### Fase 1 — Entrada e mapa da subpasta 01 (P1)

**Objetivo**: renomear a porta de entrada e o mapa de leitura, preservando conteúdo e deixando a subpasta com exatamente dois arquivos.

**Passos**:
1. Capturar um manifesto SHA-256 dos dois arquivos-alvo e de todos os arquivos das subpastas 02, 03 e 04, fora do repositório.
2. Conferir as pré-condições: origens existentes, destinos ausentes e mesmo diretório.
3. Renomear `prompt-00-leia-este-arquivo-primeiro.md` para `01-entryPoint.md`.
4. Renomear `indice-01-mapa-dos-arquivos-e-ordem-de-leitura.md` para `02-mapa-dos-arquivos-e-ordem-de-leitura.md`.
5. Validar: exatamente 2 arquivos na subpasta 01, nomes exatos, hashes idênticos ao manifesto e subpastas 02/03/04 inalteradas.
6. Registrar as falhas esperadas das suítes que citam os nomes antigos; não corrigi-las nesta fase.

**Critério de conclusão**: SC-001, SC-003 (parcial), SC-004 (2 de 2) e SC-005 para a Fase 1.

### Fase 2 — Operação do agente sem o prefixo prompt- (P2)

**Objetivo**: remover o prefixo `prompt-` dos cinco arquivos da subpasta 02, preservando numeral, descrição e conteúdo.

**Passos**:
1. Capturar um manifesto SHA-256 dos cinco arquivos-alvo e de todos os arquivos das subpastas 01, 03 e 04, fora do repositório.
2. Conferir as pré-condições: origens existentes, destinos ausentes e mesmo diretório.
3. Renomear os cinco arquivos para `01-papel-e-restricoes-do-agente.md`, `02-como-localizar-e-ler-o-archetype.md`, `03-regras-obrigatorias-de-geracao.md`, `04-fluxo-de-execucao-e-verificacao.md` e `05-entregaveis-e-relatorios.md`.
4. Validar: exatamente 5 arquivos, nenhum começando com `prompt-`, numerais 01 a 05, hashes idênticos e subpastas 01/03/04 inalteradas.
5. Registrar as falhas esperadas das seis suítes; não corrigi-las nesta fase.

**Critério de conclusão**: SC-002, SC-003 (parcial), SC-004 (5 de 5) e SC-005 para a Fase 2.

### Fase 3 — Atualização das referências operacionais (P3)

**Objetivo**: substituir as 112 ocorrências dos sete nomes antigos em 13 arquivos operacionais, sem alterar regras e sem tocar nas subpastas 03 e 04, e fechar com `mvn -B test` verde.

**Passos**:
1. Confirmar que as Fases 1 e 2 estão aplicadas: os sete novos caminhos existem e os sete antigos não existem no procedimento.
2. Inventariar as ocorrências com `Select-String` nas 13 superfícies em escopo; conferir 112 ocorrências e excluir `regras-01` e `regras-04`.
3. Substituir primeiro os nomes completos dos arquivos (7 tokens com `.md`) e depois os radicais isolados (`prompt-00` a `prompt-05`, `indice-01`). A ordem evita substituição parcial.
4. Atualizar, nesta ordem: README; conteúdo do índice; conteúdo da entrada; os três arquivos da subpasta 02; o redirect em `utils/`; as seis suítes.
5. Não tocar em regras, textos normativos, formatação, estrutura ou numeração interna; cada hunk deve ser apenas a troca de nome ou caminho.
6. Rodar `Select-String` para os tokens antigos nas 13 superfícies em escopo; exigir 0 ocorrências. Confirmar que `regras-01` e `regras-04` continuam com as 3 referências antigas e permanecem intocados.
7. Rodar `mvn -B test`; exigir todas as suítes verdes.
8. Opcional de paridade com o CI: `mvn -B clean install`.
9. Conferir que as subpastas 03 e 04, as specs históricas 003–007, `target/` e `graphify-out/` não foram reescritos.

**Critério de conclusão**: SC-006 (escopo da FR-013), SC-007 e SC-008; `mvn -B test` verde.

### Dependências entre as fases

- A Fase 2 não depende da Fase 1 e pode ser executada e validada isoladamente.
- A Fase 1 não depende da Fase 2.
- A Fase 3 depende das Fases 1 e 2 e MUST ser executada depois delas.
- A ordem recomendada é Fase 1, Fase 2 e Fase 3.
- As falhas das suítes se acumulam nas Fases 1 e 2; a Fase 3 é a única que restaura o verde.

### Estratégia de verificação

1. **Fases 1 e 2 — listagem**: conferir nomes e contagens exatas em cada subpasta.
2. **Fases 1 e 2 — hash SHA-256**: comparar cada arquivo renomeado com o hash capturado antes da fase.
3. **Fases 1 e 2 — pastas não tocadas**: comparar o manifesto das subpastas fora do escopo com o estado anterior à fase.
4. **Fase 3 — inventário**: `Select-String` para os 7 nomes completos e os 6 radicais nas 13 superfícies em escopo; 0 ocorrências.
5. **Fase 3 — escopo diferido**: confirmar 3 ocorrências antigas em `regras-01` e `regras-04`, sem alteração.
6. **Fase 3 — build**: `mvn -B test` verde; opcional `mvn -B clean install`.
7. **Fase 3 — conteúdo**: `git diff` deve mostrar somente linhas de referência; nenhuma regra alterada.
8. **Históricos**: `git diff` sem alterações em `specs/003` a `specs/007`; `target/` e `graphify-out/` fora do commit.

### Riscos e mitigação

| Risco | Mitigação |
|---|---|
| Substituição parcial de token (`prompt-00` antes do nome completo) | Substituir nomes completos primeiro; radicais depois |
| Referência antiga esquecida em arquivo de 02 | Inventário de 112 ocorrências + `Select-String` final com 0 |
| Edição acidental de regra normativa na Fase 3 | Diff por linha; só tokens de caminho/nome; revisão manual |
| Edição acidental de `regras-01`/`regras-04` | Excluir a subpasta 03 do inventário e conferir os hashes antes/depois |
| Suítes atualizadas mascararem regressão | `mvn -B test` verde + inspeção do diff das suítes |
| Sobrescrita de destino já existente no rename | Pré-condição explícita de destino ausente |
| Comando git não autorizado na implementação | Usar `Rename-Item`; nenhum `git mv`, commit, push ou branch |
| Spec histórica ou saída gerada alterada | Excluir `specs/003..007`, `target/` e `graphify-out/` do escopo |

## Complexity Tracking

Nenhuma violação da constitution. Nenhum item de complexidade adicional precisa ser justificado.
