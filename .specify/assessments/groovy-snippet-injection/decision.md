# Decision: Compositor nativo Groovy para `archetype-post-generate.groovy`

- **Slug**: groovy-snippet-injection
- **Decided**: 2026-09-28
- **Verdict**: go
- **Artifacts reviewed**: intake.md ✅ | research.md ✅ | problem.md ✅ | concept.md ✅

## Scorecard

| Criterion | Rating | Justification |
|-----------|--------|---------------|
| Problem validity | strong | Problema real, declarado pelo mantenedor (`problem.md` linhas 7–9); alinhado com diretriz estratégica explícita de eliminar bash pre-processamento (`intake.md` linha 32). |
| Evidence strength | adequate | Precedente interno forte (`xml-maven-plugin:1.2.1` cumpre função análoga para `archetype-metadata.xml` — `concept.md` linha 56); versão Groovy 4.0.28 confirmada via POM público allowlisted (`repo1.maven.org`). Limitado pela estagnação do `gmaven:2.1.1` desde 2019 (`research.md` linha 40) — risco latente sem release planejado. |
| Value vs. inaction | strong | Cost of inaction concreto (`problem.md` linhas 46–54): `sed -i` não-portável, `read -n 1 -s` trava CI sem TTY, ordem implícita de `ls -1` frágil. Apetite small (1-3 dias) elimina fragilidade crescente a cada novo snippet. |
| Feasibility / appetite | strong | Opção B do `concept.md` (linha 19) cumpre os 5 goals declarados em `problem.md` (linhas 21–27) com appetite small (1-3 dias); equivalência byte-a-byte permite validação mecânica da implementação. |
| Strategic fit | strong | Alinhamento direto com diretriz declarada de "eliminar bash em futuro bem breve" (`intake.md` linha 32). Duas restrições estruturais adicionais capturadas em conversa recente: (R1) design deve facilitar eliminação total do bash em fase futura; (R2) design deve ser legível para extensibilidade via IA/flags futuras. |
| Risk posture | adequate | Riscos identificados e mitigáveis: estagnação do `gmaven:2.1.1` (mitigado por equivalência byte-a-byte); fixar Groovy 4.0.28 no classpath (mitigável em <1h via tentativa e erro); ausência de testes unitários do compositor (aceitável — 9 perfis IT exercitam a saída). Não há risco sério não-mitigado. |

## Verdict & Rationale

**Verdict: go** com a Opção B do `concept.md` — `SnippetComposer.groovy` + `org.codehaus.gmaven:groovy-maven-plugin:2.1.1` na fase `process-resources`.

Atingimos os limiares de todos os 6 critérios do scorecard: problem validity `strong`, evidence strength `adequate` (não `weak`/`unknown`), value vs. inaction `strong`, feasibility `strong`, strategic fit `strong`, risk posture `adequate`. A Opção B é o único caminho que cumpre **todos os 5 goals** declarados em `problem.md` (linhas 21–27) com appetite small e precedente interno replicável. As 2 restrições estruturais capturadas (R1 — design deve facilitar eliminação do bash; R2 — design deve ser legível para extensibilidade via IA/flags) **reforçam** a recomendação, não a enfraquecem: ambas pedem design mais legível e auditável, exatamente o que `List<String> SECTIONS` literal + acoplamento flag↔snippet óbvio entrega.

A `needs-clarification` foi descartada porque as 7 assumptions de `concept.md` (linhas 73–79) são **empíricas e de baixo custo de validação** — não bloqueiam a especificação, podem ser fechadas dentro do ciclo de implementação. A `kill` foi descartada porque o problema é real, o apetite é small, e a Opção A ("do nothing") viola 2 dos 5 goals declarados.

## If go — Handoff to `/speckit.specify`

- **Problem**: A montagem do `archetype-post-generate.groovy` no build-time do `archetype-v2-eda` é feita por um script bash frágil (`sed -i` não-portável, `read -n 1 -s` trava CI, ordem implícita de `ls -1`). Substituir por composição nativa Groovy no mesmo sítio (build-time, fase pré-processamento) preservando a ordem obrigatória dos 32 arquivos de entrada.

- **Chosen approach**: **Opção B** — `SnippetComposer.groovy` (script Groovy de ~50-80 linhas em `src/main/groovy/eda/postgenerate/`) com `List<String> SECTIONS` ordenada, invocado via `org.codehaus.gmaven:groovy-maven-plugin:2.1.1` (`groovy:execute` goal) na fase `process-resources`, fixando `org.apache.groovy:groovy:4.0.28` no classpath via `<dependencies>` no `<plugin>`. Concatena em `target/classes/META-INF/archetype-post-generate.groovy`, com consolidação de imports (remover `^import …` dos snippets, prepor `archetype-imports.groovy`).

- **In scope**:
  1. Adicionar `gmaven:2.1.1` ao `pom.xml` do archetype com Groovy 4.0.28 fixado no classpath.
  2. Criar `SnippetComposer.groovy` com `List<String> SECTIONS` ordenada alfabeticamente (variables, snippets) e explícita para os blocos fixos.
  3. Lógica de consolidação de imports equivalente ao `remove_lines_with_import_keyword` do bash.
  4. Mover `archetype-post-generate.groovy` atualmente commitado em `src/main/resources/META-INF/` para `.gitignore` (decisão R3 do `problem.md`).
  5. Validar equivalência byte-a-byte com `diff`/`hash` da saída atual do bash.
  6. Validar 9/9 perfis IT passando em `mvn clean install` + `mvn integration-test`.

- **Out of scope** (herdado de `concept.md` linhas 60–69 + restrições estruturais R1/R2):
  - Migrar `bash_script/create_archetype_metadata.sh` (já migrado de fato).
  - Migrar ou eliminar **outros** bash scripts.
  - Migrar para novo formato declarativo do Maven 4.0.0-rc-7.
  - Mudar a versão Groovy 4.0.28 do classpath do `maven-archetype-plugin`.
  - Adotar plugins Groovy build-time diferentes de `gmaven:2.1.1`.
  - Adicionar suíte de testes unitários do compositor (Opção C) — desejável, não bloqueador.
  - **R1 — Eliminação do `bash_script/create_archetype_post_generate.sh`**: deferido para fase posterior. O bash permanece como fallback **não invocado** durante a transição.
  - **R2 — Extensibilidade via IA/flags futuras**: o `SnippetComposer.groovy` deve ser escrito com:
    - Cabeçalho explicativo da estratégia.
    - Comentário em cada item da `List<String> SECTIONS` explicando o papel.
    - Cada snippet começa com bloco de comentário indicando a qual flag (`enable_*`) está acoplado.
    - Acoplamento flag ↔ snippet deve ser 1-1 e óbvio pelo nome.

- **Success metrics** (herdado de `problem.md` linhas 37–44):
  1. `mvn clean install` BUILD SUCCESS nos 9 perfis IT.
  2. `mvn integration-test` BUILD SUCCESS nos 9 perfis IT.
  3. Equivalência byte-a-byte entre `target/classes/META-INF/archetype-post-generate.groovy` gerado pelo `SnippetComposer.groovy` e a saída atual do bash (`diff` deve retornar vazio).
  4. `bash_script/create_archetype_post_generate.sh` deixa de ser invocado pelo `script_core.sh` no fluxo `mvn clean install` do archetype.
  5. Zero `read -n 1 -s` no pipeline de build.
  6. Tempo de execução do passo de composição ≤ tempo atual do bash.

- **Carried-forward open questions** (a fechar dentro de `/speckit.specify` e/ou implementação):
  - **A1**: Fase `process-resources` é estritamente anterior a `package` e `integration-test`. Validar empiricamente.
  - **A2**: `<dependencies>` fixando `groovy:4.0.28` no `<plugin>` do `gmaven:2.1.1` é suficiente. Pode exigir `<exclusion>` de `gmaven-adapter-impl`.
  - **A3**: Regex bash `^[[:space:]]*(import|//[[:space:]]*import)` reproduz exatamente via Groovy `String.replaceAll(...)`.
  - **A4**: `List<String> SECTIONS` com mesma ordem do bash produz arquivo byte-equivalente.
  - **A5**: O `archetype-post-generate.groovy` gerado em `target/classes/META-INF/` é empacotado automaticamente pelo `maven-archetype-plugin:3.4.1`. Pode exigir mover para `src/main/resources` via `<resources>` ou ajustar `<exclude>`.
  - **A6**: O `bash_script/create_archetype_post_generate.sh` é invocado somente pelo `script_core.sh:42` e `bash_script/general_functions.sh` — confirmar com `grep -l` no projeto.
  - **A7**: O `archetype-post-generate.groovy` atualmente commitado é a saída literal do bash (sem edições manuais).