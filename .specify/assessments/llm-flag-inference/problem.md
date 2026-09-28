# Problem Definition: Inferência LLM das flags do `mvn archetype:generate`

- **Slug**: llm-flag-inference
- **Created**: 2026-10-12
- **Inputs used**: intake.md? yes | research.md? yes | user input only? no

## Problem Statement

O fluxo de geração de projeto a partir de `com.eda-archetype:eda-archetype` é amarrado a `bash_script/` (≈28 scripts `.sh`, ~28 chamadas `inject_script` em `script_core.sh`, 4 ocorrências `read -n 1 -s` que travam em ambientes sem TTY) para capturar, via prompt interativo, o valor de 34 `requiredProperty` declaradas em 17 XMLs sob `src/main/resources/META-INF/maven/archetype_metadata_properties/`; o mantenedor único do projeto precisa manter essa cadeia de scripts sincronizada a cada nova flag, responder os mesmos prompts a cada geração e ainda assim não dispor de um contrato JSON explícito por caso de uso — fazendo a dor tripla (manutenção operacional, fricção de uso repetitivo e ausência de contrato formal) bloquear a intenção estratégica declarada de eliminar pré-processamento em bash do projeto, parcialmente cumprida em outro ciclo para o compositor do `archetype-post-generate.groovy`.

## Affected Users & Stakeholders

- **Users — dono do projeto `archetype-v2-eda`** — único mantenedor; sente a dor operacional de manter `bash_script/`, a fricção de responder os mesmos prompts a cada `mvn archetype:generate`, e a fragilidade cross-platform (Windows-only hoje por causa de `sed -i` GNU e `read -n 1 -s`). [source: `bash_script/maven_command_line_archetype_generate.sh`; `bash_script/script_core.sh` linhas 41–45, 79–98; `.specify/assessments/groovy-snippet-injection/research.md`] (confidence: high)
- **Users — consumidores downstream do archetype** — quem usa `mvn archetype:generate` para criar projetos a partir do `eda-archetype` herda o mesmo ritual de prompts ou precisa copiar/colar uma linha `mvn` gigantesca; sem JSON contrato explícito, divergências entre a descrição do caso de uso e as flags viram regressões que aparecem apenas em runtime do projeto gerado. [ASSUMPTION — sem telemetry no projeto] (confidence: medium)
- **Stakeholders — dono do projeto** — único decisor, financiador e executor; também é o operador real do `github/spec-kit` neste projeto. [source: `.specify/assessments/llm-flag-inference/intake.md` — Resolved Decisions] (confidence: high)
- **Stakeholders — `github/spec-kit`** — consome o resultado via orientação textual nos templates de spec/plan/test; interesse é que a orientação aponte para um mecanismo determinístico em vez de pedir ao operador que construa a linha `mvn` ad-hoc. [source: `.specify/assessments/llm-flag-inference/intake.md` — Resolved Decisions] (confidence: high)

## Goals

- Eliminar a dependência dos scripts bash de `bash_script/` que montam a linha `mvn archetype:generate` e capturam flags via prompt interativo. [source: `.specify/assessments/llm-flag-inference/intake.md`; `bash_script/script_core.sh` linhas 79–98] (confidence: high)
- Tornar obrigatório um JSON contrato formal por caso de uso, com fail-fast explícito quando ausente. [source: `.specify/assessments/llm-flag-inference/intake.md` — Resolved Decisions] (confidence: high)
- Manter o `archetype-metadata.xml` como única fonte de verdade das flags — nenhuma flag deve ser injetada por código fora dos 17 XMLs em `src/main/resources/META-INF/maven/archetype_metadata_properties/`. [source: enumeração de 34 `<requiredProperty>` via grep em 2026-10-12; `.specify/assessments/llm-flag-inference/intake.md`] (confidence: high)
- Garantir que, no caminho feliz (descrição textual + JSON contrato cobrindo as decisões), o operador não receba nenhum prompt interativo. [ASSUMPTION — comportamento desejado declarado pelo mantenedor] (confidence: medium)
- Reaproveitar, quando viável, o tooling já disponível no projeto (`xml-maven-plugin:1.2.1`, `gmaven:2.1.1` + Groovy 4.0.28, `SnippetComposer.groovy`) para evitar novo stack tecnológico. [source: `.specify/assessments/groovy-snippet-injection/decision.md`; `pom.xml`] (confidence: medium)
- Atingir a integração com `github/spec-kit` por orientação textual nos templates de spec/plan/test, sem exigir fork do template nem ponto de injeção técnica. [source: `.specify/assessments/llm-flag-inference/intake.md` — Resolved Decisions] (confidence: medium)

## Non-Goals

- Migrar ou reescrever os XMLs de descriptor (`archetype-metadata.xml` + 17 `property-*.xml` + 9 `fileset-*.xml`) — o catálogo é fonte de verdade e deve permanecer intocado. [source: `src/main/resources/META-INF/maven/archetype_metadata_properties/*.xml`; `.specify/assessments/llm-flag-inference/intake.md` — Resolved Decisions] (confidence: high)
- Migrar `archetype-post-generate.groovy` novamente — já tratado em `.specify/assessments/groovy-snippet-injection/`. (confidence: high)
- Adicionar cobertura automatizada de combinações de flag — já tratado em `.specify/assessments/archetype-integration-tests/`. (confidence: high)
- Treinar, afinar ou trocar o LLM em uso — usar o LLM já disponível para o `github/spec-kit` e o opencode. (confidence: high)
- Construir UI, dashboard, serviço HTTP ou relatório externo — a entrega é via CLI/arquivo consumidos pelo operador humano e/ou pelo spec-kit. (confidence: high)
- Alterar a versão do `maven-archetype-plugin` ou migrar para o descriptor do Maven 4. (confidence: high)
- Tentar fazer o mecanismo funcionar para `archetype-metadata.xml` (que é gerado por `xml-maven-plugin` + `identity.xsl`) — o mecanismo consome esse arquivo, não o produz. (confidence: high)

## Success Metrics

- **Número de `read -n 1 -s` no fluxo de geração de projeto** — baseline: 4 ocorrências em `bash_script/create_archetype_metadata.sh` linhas 67, 77 e `bash_script/create_archetype_post_generate.sh` linhas 66, 84 (além de `script_core.sh` que depende de TTY); alvo: 0 no fluxo que monta e executa `mvn archetype:generate`. (confidence: high — contável)
- **Tamanho de `bash_script/maven_command_line_archetype_generate.sh` + `general_functions.sh` + `user_*_input.sh`** — baseline: 55 + 265 + ~14 arquivos `user_*_input.sh` mantidos manualmente; alvo: removidos do controle de versão após adoção. (confidence: high — contável em `git ls-files bash_script/`)
- **Linha `mvn archetype:generate` gerada é executável sem prompt** — alvo: `mvn clean install` + `mvn integration-test` passam em **todos os 10 perfis IT** (`basic`, `binding-default-only`, `binding-list-only`, `binding-map-only`, `binding-mutable-only`, `binding-validated-only`, `defaults-only`, `full`, `json-contract`, `minimal`) usando a linha produzida pelo mecanismo. Baseline atual: 10/10 perfis já passam quando o operador responde os prompts via bash legado. (confidence: high — harness objetivo)
- **Cobertura do catálogo de flags** — alvo: 100% dos 34 `<requiredProperty>` reconhecidos pelo validador determinístico; baseline: cobertura desconhecida (a LLM poderia alucinar hoje). (confidence: high — enumerável)
- **Reprodutibilidade** — alvo: mesma (descrição textual + JSON contrato) produzem exatamente a mesma linha de comando em execuções distintas; baseline: hoje cada execução depende das respostas humanas e do estado do terminal. (confidence: medium — depende de configuração da LLM a definir)
- **Cache efetivo** — alvo: 100% das invocações repetidas com mesma entrada命中 no cache, sem nova chamada à LLM; baseline: hoje não há cache. (confidence: medium — depende de serialização canônica)
- **JSON contrato obrigatório** — alvo: 100% das invocações sem JSON contrato são interrompidas com solicitação explícita antes de prosseguir; baseline: hoje o JSON contrato é implícito (disperso em prompts). (confidence: high — verificável)

## Cost of Inaction

- **Manutenção contínua do `bash_script/`** — cada nova `requiredProperty` exige editar 4 lugares: um `user_*_input.sh`, o `script_core.sh` (linha `inject_script`), o `maven_command_line_archetype_generate.sh` (linha `-D…`), e o `archetype-metadata.xml` (XML correspondente). O ritual de 10 passos documentado em `README.md` linhas 1081–1414 continua sendo o caminho oficial. (confidence: high)
- **Travamento em CI/pipelines sem TTY** — `read -n 1 -s` em `bash_script/create_archetype_metadata.sh` e `bash_script/create_archetype_post_generate.sh` impede execução automatizada em qualquer ambiente Linux headless (GitHub Actions, GitLab CI, Jenkins agents). O `archetype-v2-eda` nunca chega a ser empacotado em tais ambientes sem workaround manual. [source: `bash_script/create_archetype_metadata.sh` linhas 67, 77; `bash_script/create_archetype_post_generate.sh` linhas 66, 84] (confidence: high)
- **Barreira de entrada para novos contribuidores** — quem quiser adicionar uma flag precisa ler `script_core.sh` (115 linhas), `maven_command_line_archetype_generate.sh` (55 linhas), os 14 `user_*_input.sh`, e o `README.md` linhas 1081–1414 antes de entender onde tocar. Sem onboarding linear. (confidence: medium)
- **Ausência de JSON contrato explícito** — o contrato do módulo continua implícito, espalhado entre prompts do terminal e flags ad-hoc; sem contrato, divergências entre descrição de caso de uso e flags só aparecem no `mvn verify` do projeto gerado, depois de horas de trabalho. (confidence: medium)
- **Acoplamento entre intenção estratégica e progresso real** — a intenção declarada de "eliminar pré-processamento em bash" permanece parcialmente cumprida (apenas o compositor de `archetype-post-generate.groovy` foi migrado via `groovy-snippet-injection`). O restante do bash legado se torna, ciclo a ciclo, uma relíquia que ninguém ousa tocar. (confidence: high)

## Open Questions

- [NEEDS CLARIFICATION: temperatura da LLM — para garantir reprodutibilidade byte-a-byte da linha `mvn archetype:generate`, é necessário zerar `temperature` e `top_p`. Decidir em `/speckit.specify`.] *(carregado de `.specify/assessments/llm-flag-inference/research.md`)*
- [NEEDS CLARIFICATION: granularidade da extração de flags — basta o `key` da `<requiredProperty>`, ou o validador determinístico precisa também de `<defaultValue>` e `<validationRegex>`? Sugestão: ambos, para que o validador aplique regex e use defaults. Ratificar em `/speckit.specify`.] *(carregado de research.md)*
- [NEEDS CLARIFICATION: quando LLM e JSON contrato discordam sobre o valor de uma mesma flag, o contrato vence (decidido). Mas quando a LLM propõe uma flag **ausente do contrato**, ela deve ser descartada? Sugestão: sim — flags fora do contrato são silenciadas e o default do archetype se aplica. Ratificar em `/speckit.specify`.] *(carregado de research.md)*
- [NEEDS CLARIFICATION: serialização canônica do JSON contrato para a chave de cache — `ObjectMapper.writeValueAsString` (Jackson) ordena campos deterministicamente? Se não, considerar `JsonNode.toString()` + canonicalização (ex.: RFC 8785 / JCS) ou hashing semântico. Decidir antes de implementar o cache.] *(carregado de research.md)*
- [NEEDS CLARIFICATION: como tratar flags com `<defaultValue></defaultValue>` (vazio) — hoje o `user_*_input.sh` captura o input antes de montar a linha; no mecanismo novo, a LLM deve inferir essas flags a partir da descrição textual em vez de promptar o operador? Sugestão: sim — descrição textual + JSON contrato são as únicas fontes de input; nenhum prompt humano. Ratificar em `/speckit.specify`.] *(carregado de research.md)*
