# Research: Renomear Arquivos das Subpastas 01 e 02 em Três Fases

**Feature**: `008-rename-procedure-files` | **Date**: 2026-11-25 | **Spec**: [spec.md](./spec.md)

## Contexto

A spec tem três fases: duas de renomeação e uma de atualização de referências. O inventário operacional encontrou 115 ocorrências dos sete nomes antigos em 15 arquivos. A Fase 3 depende das Fases 1 e 2 e é a única que restaura `mvn -B test` verde.

## Decisão 1 — Dependência da Fase 3

**Decision**: A Fase 3 é executada depois das Fases 1 e 2; as Fases 1 e 2 continuam independentes entre si.

**Rationale**: A Fase 3 atualiza referências aos sete novos nomes; sem os renomes, as referências apontariam para arquivos inexistentes. A FR-010 declara essa dependência.

**Alternatives considered**:
- Distribuir as referências entre as Fases 1 e 2: contraria a decisão da spec de criar a Fase 3; rejeitado.
- Executar a Fase 3 antes dos renomes: deixaria o procedimento apontando para nomes inexistentes; rejeitado.

## Decisão 2 — Interpretação da FR-009 para a Fase 3

**Decision**: A FR-009 é interpretada como proibição de renomear, adicionar ou remover arquivos nas subpastas 03 e 04. A Fase 3 pode substituir apenas linhas de referência de caminho/nome nos dois arquivos da subpasta 03 que citam `prompt-00`; nenhum arquivo da subpasta 04 contém referência antiga.

**Rationale**: A FR-014 limita a Fase 3 a referências de caminho e nome, e a SC-006 exige zero referências operacionais aos sete nomes antigos. Sem atualizar `regras-01` e `regras-04`, a SC-006 seria impossível. A subpasta 04 tem 0 ocorrências.

**Alternatives considered**:
- Deixar a subpasta 03 intocada: falharia a SC-006 e o `Select-String` final; rejeitado.
- Renomear arquivos da subpasta 03: violaria a FR-009 e o escopo; rejeitado.

**Registro**: esta interpretação MUST aparecer no relatório de implementação da Fase 3.

## Decisão 3 — Inventário das referências operacionais

**Decision**: A Fase 3 atua sobre 115 ocorrências em 15 arquivos, conforme a tabela.

| Arquivo | Ocorrências |
|---|---:|
| `README.md` | 15 |
| `01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md` | 35 |
| `01-entrada-inicializacao/indice-01-mapa-dos-arquivos-e-ordem-de-leitura.md` | 14 |
| `02-operacao-procedimento-do-agente/prompt-01-papel-e-restricoes-do-agente.md` | 1 |
| `02-operacao-procedimento-do-agente/prompt-03-regras-obrigatorias-de-geracao.md` | 1 |
| `02-operacao-procedimento-do-agente/prompt-04-fluxo-de-execucao-e-verificacao.md` | 1 |
| `03-derivacao-regras-de-formacao-do-contrato/regras-01-como-derivar-a-entidade-do-contrato.md` | 1 |
| `03-derivacao-regras-de-formacao-do-contrato/regras-04-como-aplicar-precedencia-e-validacao.md` | 2 |
| `utils/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md` | 1 |
| `src/test/archetype-post-generate/BuildArtifactsTest.groovy` | 8 |
| `src/test/archetype-post-generate/CatalogSyncContractTest.groovy` | 2 |
| `src/test/archetype-post-generate/DestinationContractTest.groovy` | 16 |
| `src/test/archetype-post-generate/HardStopContractTest.groovy` | 4 |
| `src/test/archetype-post-generate/PathRenameContractTest.groovy` | 11 |
| `src/test/archetype-post-generate/ReadmeConsistencyTest.groovy` | 3 |
| **Total** | **115** |

**Rationale**: O número é reproduzível por `Select-String` com o padrão `prompt-0[0-5]|indice-01-mapa` nas superfícies operacionais. `src/test/resources/projects/`, `.specify/templates/`, `pom.xml` e CI têm 0 ocorrências.

**Alternatives considered**:
- Inventariar apenas os caminhos completos: deixaria radicais isolados (`prompt-00 §0.4`) como referências antigas; rejeitado.
- Incluir specs históricas: violaria a FR-016; rejeitado.

## Decisão 4 — Ordem de substituição dos tokens

**Decision**: Substituir primeiro os sete nomes completos com `.md` e depois os radicais isolados, na ordem abaixo.

| Ordem | Token antigo | Token novo |
|---:|---|---|
| 1 | `prompt-00-leia-este-arquivo-primeiro.md` | `01-entryPoint.md` |
| 2 | `indice-01-mapa-dos-arquivos-e-ordem-de-leitura.md` | `02-mapa-dos-arquivos-e-ordem-de-leitura.md` |
| 3 | `prompt-01-papel-e-restricoes-do-agente.md` | `01-papel-e-restricoes-do-agente.md` |
| 4 | `prompt-02-como-localizar-e-ler-o-archetype.md` | `02-como-localizar-e-ler-o-archetype.md` |
| 5 | `prompt-03-regras-obrigatorias-de-geracao.md` | `03-regras-obrigatorias-de-geracao.md` |
| 6 | `prompt-04-fluxo-de-execucao-e-verificacao.md` | `04-fluxo-de-execucao-e-verificacao.md` |
| 7 | `prompt-05-entregaveis-e-relatorios.md` | `05-entregaveis-e-relatorios.md` |
| 8 | `prompt-00` | `01-entryPoint` |
| 9 | `indice-01` | `02-mapa-dos-arquivos-e-ordem-de-leitura` |
| 10 | `prompt-01` | `01-papel-e-restricoes-do-agente` |
| 11 | `prompt-02` | `02-como-localizar-e-ler-o-archetype` |
| 12 | `prompt-03` | `03-regras-obrigatorias-de-geracao` |
| 13 | `prompt-04` | `04-fluxo-de-execucao-e-verificacao` |
| 14 | `prompt-05` | `05-entregaveis-e-relatorios` |

**Rationale**: Substituir `prompt-00` antes do nome completo produziria `01-entryPoint-leia-este-arquivo-primeiro.md`. A ordem descendente por comprimento evita substituição parcial.

**Alternatives considered**:
- Substituição única com regex: o risco de captura parcial é o mesmo; a ordem explícita é verificável; rejeitado.
- Renomear também identificadores de variáveis (`prompt00Path`, `prompt03`): não são referências operacionais e ampliariam o diff sem necessidade; rejeitado.

## Decisão 5 — Mecanismo de renomeação

**Decision**: Renomear com `Rename-Item` do PowerShell, dentro do mesmo diretório. Em POSIX, `mv` no mesmo diretório.

**Rationale**: Não usa comando git, não reescreve o arquivo e falha se o destino existir.

**Alternatives considered**:
- `git mv`: comando git não autorizado; rejeitado.
- Copiar e apagar: cria duplicata temporária e não é atômico; rejeitado.
- Renomear pelo IDE: risco de reescrita de conteúdo; rejeitado.

## Decisão 6 — Prova de conteúdo nas Fases 1 e 2

**Decision**: Capturar SHA-256 dos arquivos-alvo antes de cada fase, em manifesto fora do repositório, e comparar depois do rename.

**Rationale**: Prova conteúdo byte a byte idêntico sem backup com nome antigo e sem comando git.

**Alternatives considered**:
- Detecção de rename pelo git: exigiria stage/index; rejeitado.
- Cópia de backup: cria cópias com nomes antigos, conflitando com a FR-012; rejeitado.

## Decisão 7 — Gate de build e estado vermelho entre fases

**Decision**: A Fase 1 é aceita com cinco suítes vermelhas; a Fase 2, com as seis. A Fase 3 fecha com `mvn -B test` verde.

**Rationale**: As suítes leem os nomes antigos; atualizá-las antes da Fase 3 misturaria renomeação e referências e quebraria a separação das fases.

**Alternatives considered**:
- Atualizar as suítes nas Fases 1 e 2: anteciparia a Fase 3 e duplicaria trabalho; rejeitado.
- Ignorar o build até o fim: impediria distinguir falha esperada de regressão; rejeitado.

## Decisão 8 — Exclusões de escopo

**Decision**: Não reescrever `specs/003` a `specs/007`, `target/` nem `graphify-out/`.

**Rationale**: Specs históricas são registro; `target/` e `graphify-out/` são saídas geradas. A FR-016 e a SC-008 fixam a exclusão.

**Alternatives considered**:
- Atualizar specs históricas: reescreveria registro e ampliaria o escopo; rejeitado.
- Atualizar `graphify-out/`: saída gerada, não fonte; rejeitado.

## Decisão 9 — JAR instalado e paridade com o CI

**Decision**: O gate obrigatório é `mvn -B test`; `mvn -B clean install` é paridade opcional com o CI. A checagem opt-in do JAR instalado não é requisito da spec.

**Rationale**: A FR-015 exige `mvn -B test`; o CI já executa `clean install` e `integration-test`. Rodar `clean install` localmente valida o JAR e o catálogo, mas não é necessário para a aceitação da Fase 3.

**Alternatives considered**:
- Exigir `clean install` como gate: amplia o tempo e não é pedido pela spec; rejeitado.
- Ignorar o CI: a paridade é útil para detectar problemas de empacotamento; mantida como opcional.

## Decisão 10 — Dependências e paradigma

**Decision**: Nenhuma dependência nova; o projeto é NÃO OOP e os princípios da seção 3 do input do Plan estão vazios.

**Rationale**: A feature é documental e de testes; não há código OOP no repositório. SOLID não se aplica.

**Alternatives considered**:
- Adicionar lint de Markdown: fora do escopo e sem ferramenta no projeto; rejeitado.

## Lacunas remanescentes

Nenhuma. A única tensão da spec — FR-009 versus referências na subpasta 03 — foi resolvida pela interpretação registrada na Decisão 2.
