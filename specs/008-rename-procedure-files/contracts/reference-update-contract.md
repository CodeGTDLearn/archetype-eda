# Contract: Atualização das Referências Operacionais (Fase 3)

**Feature**: `008-rename-procedure-files` | **Date**: 2026-11-25 | **Spec**: [../spec.md](../spec.md)

## Escopo

- **Dentro**: 115 ocorrências dos sete nomes antigos em 15 arquivos operacionais.
- **Superfícies**: README; conteúdo do índice; conteúdo da entrada; três arquivos da subpasta 02; dois arquivos da subpasta 03; redirect em `utils/`; seis suítes de contrato.
- **Fora**: regras, textos normativos, formatação, estrutura, numeração interna, specs históricas 003–007, `target/`, `graphify-out/`, identificadores de variável e nomes de arquivos das subpastas 03 e 04.
- **Dependência**: Fase 3 exige as Fases 1 e 2 concluídas.

## Inventário canônico

| Arquivo | Ocorrências |
|---|---:|
| `README.md` | 15 |
| `src/main/resources/llm-maven-flags-inference/01-entrada-inicializacao/01-entryPoint.md` | 35 |
| `src/main/resources/llm-maven-flags-inference/01-entrada-inicializacao/02-mapa-dos-arquivos-e-ordem-de-leitura.md` | 14 |
| `src/main/resources/llm-maven-flags-inference/02-operacao-procedimento-do-agente/01-papel-e-restricoes-do-agente.md` | 1 |
| `src/main/resources/llm-maven-flags-inference/02-operacao-procedimento-do-agente/03-regras-obrigatorias-de-geracao.md` | 1 |
| `src/main/resources/llm-maven-flags-inference/02-operacao-procedimento-do-agente/04-fluxo-de-execucao-e-verificacao.md` | 1 |
| `src/main/resources/llm-maven-flags-inference/03-derivacao-regras-de-formacao-do-contrato/regras-01-como-derivar-a-entidade-do-contrato.md` | 1 |
| `src/main/resources/llm-maven-flags-inference/03-derivacao-regras-de-formacao-do-contrato/regras-04-como-aplicar-precedencia-e-validacao.md` | 2 |
| `src/main/resources/utils/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md` | 1 |
| `src/test/archetype-post-generate/BuildArtifactsTest.groovy` | 8 |
| `src/test/archetype-post-generate/CatalogSyncContractTest.groovy` | 2 |
| `src/test/archetype-post-generate/DestinationContractTest.groovy` | 16 |
| `src/test/archetype-post-generate/HardStopContractTest.groovy` | 4 |
| `src/test/archetype-post-generate/PathRenameContractTest.groovy` | 11 |
| `src/test/archetype-post-generate/ReadmeConsistencyTest.groovy` | 3 |
| **Total** | **115** |

Zero ocorrências: `src/test/resources/projects/`, `.specify/templates/`, `pom.xml`, `.github/workflows/ci.yml`.

## Tokens canônicos

### Nomes completos (substituir primeiro)

| De | Para |
|---|---|
| `prompt-00-leia-este-arquivo-primeiro.md` | `01-entryPoint.md` |
| `indice-01-mapa-dos-arquivos-e-ordem-de-leitura.md` | `02-mapa-dos-arquivos-e-ordem-de-leitura.md` |
| `prompt-01-papel-e-restricoes-do-agente.md` | `01-papel-e-restricoes-do-agente.md` |
| `prompt-02-como-localizar-e-ler-o-archetype.md` | `02-como-localizar-e-ler-o-archetype.md` |
| `prompt-03-regras-obrigatorias-de-geracao.md` | `03-regras-obrigatorias-de-geracao.md` |
| `prompt-04-fluxo-de-execucao-e-verificacao.md` | `04-fluxo-de-execucao-e-verificacao.md` |
| `prompt-05-entregaveis-e-relatorios.md` | `05-entregaveis-e-relatorios.md` |

### Radicais isolados (substituir depois)

| De | Para |
|---|---|
| `prompt-00` | `01-entryPoint` |
| `indice-01` | `02-mapa-dos-arquivos-e-ordem-de-leitura` |
| `prompt-01` | `01-papel-e-restricoes-do-agente` |
| `prompt-02` | `02-como-localizar-e-ler-o-archetype` |
| `prompt-03` | `03-regras-obrigatorias-de-geracao` |
| `prompt-04` | `04-fluxo-de-execucao-e-verificacao` |
| `prompt-05` | `05-entregaveis-e-relatorios` |

**Ordem obrigatória**: nomes completos primeiro, radicais depois. Inverter produz caminhos parciais inválidos.

## Invariantes

- **R-001**: 0 ocorrências de `prompt-0[0-5]` e `indice-01-mapa` nas 15 superfícies operacionais após a Fase 3.
- **R-002**: Nenhuma regra, texto normativo, formatação ou numeração interna alterada.
- **R-003**: Cada hunk é apenas a troca de um token de caminho/nome.
- **R-004**: As subpastas 03 e 04 não têm arquivos renomeados, adicionados ou removidos.
- **R-005**: O redirect legado continua no caminho antigo e passa a apontar para `01-entryPoint.md`.
- **R-006**: As seis suítes são atualizadas e `mvn -B test` fica verde.
- **R-007**: `specs/003` a `specs/007`, `target/` e `graphify-out/` permanecem inalterados.
- **R-008**: Nenhum alias, cópia ou novo redirect com nome antigo é criado.
- **R-009**: Identificadores de variável (`prompt00Path`, `prompt03`, `p00`, `p03`) não entram no critério de 0 tokens.
- **R-010**: A interpretação da FR-009 é registrada: 03/04 não são renomeadas; só referências são atualizadas.

## Sequência de atualização

1. README (15).
2. Conteúdo do índice (14).
3. Conteúdo da entrada (35).
4. Subpasta 02: `01-papel...` (1), `03-regras...` (1), `04-fluxo...` (1).
5. Subpasta 03: `regras-01` (1), `regras-04` (2).
6. Redirect em `utils/` (1).
7. Seis suítes (44).

## Verificação

| Comando | Esperado |
|---|---|
| `Select-String` com `prompt-0[0-5]\|indice-01-mapa` nas 15 superfícies | 0 ocorrências |
| `Select-String` nas specs 003–007 | inalterado |
| `git diff --stat` | somente 15 arquivos operacionais |
| `mvn -B test` | todas as suítes passam |
| `mvn -B clean install` (opcional) | passa |

## Critérios de rejeição

- Qualquer token antigo restante nas superfícies operacionais.
- Alteração de regra, texto normativo, formatação ou numeração interna.
- Alteração em specs históricas, `target/` ou `graphify-out/`.
- Suíte não atualizada ou `mvn -B test` vermelho.
- Arquivo renomeado nas subpastas 03 ou 04.
- Novo alias, cópia ou redirect com nome antigo.
