# Decision: Inferência LLM das flags do `mvn archetype:generate`

- **Slug**: llm-flag-inference
- **Decided**: 2026-10-12
- **Verdict**: go
- **Artifacts reviewed**: intake.md? yes | research.md? yes | problem.md? yes | concept.md? yes

## Scorecard

| Criterion | Rating | Justification |
|-----------|--------|---------------|
| Problem validity | strong | Problema triplo documentado com métricas mensuráveis: 4 `read -n 1 -s` em `bash_script/`, 34 `<requiredProperty>` em 17 XMLs, 10 perfis IT afetados. Intenção estratégica declarada de eliminar bash do projeto está parcialmente cumprida (`groovy-snippet-injection`) e este assessment é a continuação natural. |
| Evidence strength | adequate | Evidência interna forte (citações diretas a `bash_script/script_core.sh`, `maven_command_line_archetype_generate.sh`, `archetype-metadata.xml`, `property-*.xml`, `README.md` linhas 1081–1414). Prior art externo limitado pela URL Trust Policy desta sessão (hosts fora da allowlist), marcado como `[ASSUMPTION]` em `research.md`. Precedente direto do próprio projeto (`groovy-snippet-injection`) confirma a viabilidade do caminho Groovy build-time. |
| Value vs. inaction | strong | Custo de não fazer é documentado em `problem.md` com 5 consequências concretas (manutenção contínua via ritual de 10 passos do `README.md`, travamento em CI sem TTY, barreira de onboarding, ausência de JSON contrato, intenção estratégica permanentemente parcial). Bate o baseline zero da opção C em todos os 6 goals. |
| Feasibility / appetite | strong | Opção B cotada como `medium` (semanas, 1–3) com precedente direto no próprio projeto: `groovy-snippet-injection` já migrou um compositor similar via `gmaven:2.1.1` + Groovy 4.0.28 em fase `process-resources`. Reuso de `xml-maven-plugin:1.2.1` + `gmaven:2.1.1` (já no `pom.xml`) elimina risco de stack novo. Spikes de 1–2 dias podem validar extração XInclude dos 34 `<requiredProperty>` e serialização canônica do JSON antes de comprometer o appetite. |
| Strategic fit | strong | Alinhamento explícito com (a) intenção declarada de eliminar bash, (b) follow-up de `groovy-snippet-injection` (mesmo mantenedor, mesmo projeto, mesmo padrão "Groovy build-time em vez de bash"), (c) integração com `github/spec-kit` por orientação textual (decidido no intake). Constituição implícita do projeto — preferir Groovy nativo sobre bash — já está codificada no `script_core.sh` linhas 41–45. |
| Risk posture | adequate | Riscos principais mapeados em `research.md` (alucinação de flags, alucinação de tipos, não-determinismo, custo por feature, manutenção do validador, integração textual sem hook técnico). Todos têm mitigação identificada: validador determinístico contra `archetype-metadata.xml`, `validationRegex` aplicado, temperatura 0 + cache por hash canônico, reaproveitamento dinâmico do catálogo, decisão textual no template do spec-kit. Os 5 open questions restantes são concretos e resolvíveis em `/speckit.specify` (temperatura, granularidade, flags fora do contrato, serialização canônica, defaults vazios). |

## Verdict & Rationale

**Go.** Todos os 6 critérios atingem `adequate` ou acima; nenhum está em `weak` ou `unknown`. A evidência interna do `research.md` é forte o suficiente para suportar um `go` mesmo com a limitação da URL Trust Policy sobre prior art externo (marcado como `[ASSUMPTION]` em cada menção). O conceito B é o único que cobre os 6 goals do `problem.md` com reprodutibilidade e fail-fast, e a opção C (não fazer) já está descartada como linha de base pela própria clareza do custo de inação. O precedente direto de `groovy-snippet-injection` no mesmo projeto reduz significativamente o risco de feasibility — não é um caminho verde, mas é um caminho conhecido. As 5 open questions carregadas para `/speckit.specify` são decisões pontuais (não bloqueios arquiteturais), e cada uma tem sugestão concreta em `problem.md` e `concept.md`.

## If needs-clarification

(não aplicável — ver `If go`)

## If go — Handoff to `/speckit.specify`

- **Problem**: o fluxo de geração de projeto via `mvn archetype:generate` é amarrado a `bash_script/` (~28 scripts, 4 `read -n 1 -s` que travam em ambientes sem TTY) para capturar 34 `<requiredProperty>` declaradas em 17 XMLs, sem JSON contrato explícito por caso de uso, bloqueando a intenção estratégica declarada de eliminar pré-processamento em bash do projeto.
- **Chosen approach**: **Option B** — a LLM recebe (descrição textual + JSON contrato) e devolve um mapeamento estruturado `requiredProperty → valor`; um módulo determinístico Groovy (reaproveitando `xml-maven-plugin:1.2.1` para extrair o catálogo via XInclude e `gmaven:2.1.1` + Groovy 4.0.28 na fase `process-resources` para o validador) funde o mapeamento com o JSON contrato (contrato vence), aplica `<defaultValue>` para o que ficou de fora, valida cada valor contra `<validationRegex>`, e monta a linha `mvn archetype:generate --batch-mode -D…` final. Cache por `hash(canonical-JSON + descrição textual)` evita nova chamada à LLM em invocações repetidas. Temperatura da LLM fixada em 0 para reprodutibilidade.
- **In scope / out of scope**:
  - **In scope**: implementar o validador Groovy que extrai os 34 `<requiredProperty>` de `archetype-metadata.xml` + 17 XMLs via XInclude e valida cada par proposto; implementar a fusão com JSON contrato e aplicação de defaults; implementar o cache por hash canônico; definir a temperatura 0 da LLM e o texto de prompt estruturado que devolve mapeamento (não linha pronta); orientar textualmente os templates de spec/plan/test do `github/spec-kit` a referenciar o mecanismo; deletar `bash_script/maven_command_line_archetype_generate.sh`, `bash_script/general_functions.sh` e os 14 `bash_script/user_*_input.sh` após validação em todos os 10 perfis IT.
  - **Out of scope**: editar `archetype-metadata.xml` ou qualquer `property-*.xml`/`fileset-*.xml`; migrar `archetype-post-generate.groovy` (já tratado em `.specify/assessments/groovy-snippet-injection/`); adicionar cobertura automatizada de combinações de flag (já tratado em `.specify/assessments/archetype-integration-tests/`); treinar, afinar ou trocar o LLM em uso; construir UI/dashboard/serviço HTTP/relatório externo; alterar a versão do `maven-archetype-plugin` ou migrar para descriptor do Maven 4.
- **Success metrics** (herdados de `problem.md`, na ordem de enforcement):
  1. Zero `read -n 1 -s` no fluxo de geração de projeto (baseline: 4).
  2. `bash_script/maven_command_line_archetype_generate.sh` + `general_functions.sh` + 14 `user_*_input.sh` removidos do controle de versão após adoção (baseline: presentes).
  3. `mvn clean install` + `mvn integration-test` passam em todos os 10 perfis IT usando a linha produzida pelo mecanismo (baseline: 10/10 perfis já passam via bash legado).
  4. 100% dos 34 `<requiredProperty>` reconhecidos pelo validador determinístico.
  5. Mesma (descrição textual + JSON contrato) produz exatamente a mesma linha de comando em execuções distintas (baseline: hoje depende de respostas humanas).
  6. 100% das invocações repetidas com mesma entrada命中 no cache, sem nova chamada à LLM.
  7. 100% das invocações sem JSON contrato são interrompidas com solicitação explícita antes de prosseguir.
- **Carried-forward open questions** (de `problem.md` linhas finais, com sugestões de resolução já registradas):
  1. Temperatura da LLM — fixar em 0 (sugestão; ratificar em `/speckit.specify`).
  2. Granularidade da extração de flags — extrair `key`, `<defaultValue>` e `<validationRegex>` de cada `<requiredProperty>` (sugestão; ratificar).
  3. Comportamento quando LLM propõe flag fora do contrato — descartar e aplicar `<defaultValue>` (sugestão; ratificar).
  4. Serialização canônica do JSON contrato para chave de cache — considerar RFC 8785 / JCS ou `Jackson` com `SORT_PROPERTIES_ALPHABETICALLY` (sugestão; ratificar antes de implementar o cache).
  5. Flags com `<defaultValue></defaultValue>` (vazio) — devem ser inferidas da descrição textual em vez de promptar o operador (sugestão; ratificar).
