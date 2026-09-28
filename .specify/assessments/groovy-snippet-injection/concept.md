# Concept: Compositor nativo Groovy para `archetype-post-generate.groovy`

- **Slug**: groovy-snippet-injection
- **Created**: 2026-09-28
- **Recommended option**: B — `SnippetComposer` Groovy em `process-resources` via `gmaven:2.1.1`

## Options

### Option A — "Do nothing / buy instead of build"

- **Sketch**: Não construir nada novo. Continuar usando o `bash_script/create_archetype_post_generate.sh` atual. Para CI Linux sem TTY, contornar `read -n 1 -s` substituindo-o por `exit 1` ou setando `< /dev/null`. Aceitar a fragilidade cross-platform e a ordem implícita de `ls -1` como custo de manter o script.
- **Appetite**: small (algumas horas de patch por incidente, sem novo design).
- **Trade-offs**:
  - **Wins**: zero risco de regressão nos 9 perfis IT; nenhuma mudança no `pom.xml`; nenhum novo plugin.
  - **Sacrifices**: não cumpre o Goal #1 (eliminar dependência do pre-processamento em bash); não cumpre o Goal #2 (mantém bash em vez de build-time nativo); o `read -n 1 -s` continua travando o pipeline se esquecido.
  - **Risks**: cada novo snippet reintroduz o risco de ordem alfabética implícita errada; macOS nativo (BSD sed) quebraria se algum dia o mantenedor sair do Windows.
- **Rabbit holes**: cada "patch" incremental vira desculpa para nunca atacar o problema de raiz; a dívida de manutenção cresce monotonicamente com o número de snippets.

### Option B — `SnippetComposer.groovy` + `gmaven:2.1.1` em `process-resources` (concatenação, arquivo congelado)

- **Sketch**: Adicionar `org.codehaus.gmaven:groovy-maven-plugin:2.1.1` ao `pom.xml`, fixando `org.apache.groovy:groovy:4.0.28` via `<dependencies>`. Criar um arquivo Groovy `src/main/groovy/eda/postgenerate/SnippetComposer.groovy` que contém uma `List<String> SECTIONS` ordenada (header → global-vars → path-vars → variables → snippets → footer → file-functions → folder-functions) e concatena esses arquivos em `target/classes/META-INF/archetype-post-generate.groovy`. A transformação de consolidação de imports (remover `^import …` dos snippets, prepor `archetype-imports.groovy`) é parte do mesmo composer. Bound no hook: goal `execute` na fase `process-resources` (imediatamente após o `xml-maven-plugin:transform` em `generate-resources` montar o `archetype-metadata.xml`, e antes do `package`). O `bash_script/create_archetype_post_generate.sh` permanece como fallback durante a transição, mas deixa de ser invocado.
- **Appetite**: small (1 a 3 dias) — o trabalho é essencialmente: 1 script Groovy de ~50-80 linhas, 1 entry no `pom.xml`, 1 iteração de validação byte-a-byte com a saída atual do bash, 1 rodada de `mvn clean install` + `mvn integration-test` confirmando 9/9.
- **Trade-offs**:
  - **Wins**: cumpre os 5 goals declarados; elimina o `read -n 1 -s`; elimina `sed -i`/`tr`/`ls`; ordem passa a ser uma `List<String>` literal e auditável; equivalência byte-a-byte com a saída atual é verificável mecanicamente; alinha com o precedente interno `xml-maven-plugin:1.2.1` que já cumpre função análoga para `archetype-metadata.xml`.
  - **Sacrifices**: adiciona 1 plugin Maven (`gmaven:2.1.1`) ao `pom.xml`; exige fixar `groovy:4.0.28` no classpath do plugin via `<dependencies>`; exige decidir se a `archetype-post-generate.groovy` commitada continua versionada ou migra para `.gitignore`.
  - **Risks**: o `gmaven:2.1.1` está estagnado desde 2019-08-04 (research.md linha 40); fixar Groovy 4 pode exigir exclusion explícita de `gmaven-adapter-impl` (research.md linha 41); sem testes unitários do compositor, regressões na ordem só são detectadas no `integration-test` (custo de detecção alto).
- **Rabbit holes**:
  - Tentar reproduzir fielmente cada quirk do bash (ex.: `tr -d '/'` no `archetype-imports.groovy`) — se a equivalência byte-a-byte não for objetivo, isso é trabalho desperdiçado.
  - Adicionar uma DSL de configuração de ordem com sintaxe própria.
  - Implementar a transformação de imports com regex própria se `Pattern.matches()` Groovy divergir do regex bash.

### Option C — `groovy-maven-plugin:2.1.1` (via `groovy-maven-plugin` "oficial" sob gmaven) com testes unitários Groovy do compositor

- **Sketch**: Igual à Opção B, mas com uma suíte de testes Groovy dedicada ao compositor — `groovy:test` invocando o `SnippetComposer.groovy` com fixtures (diretórios stub de snippets), comparando o output contra golden files. A suíte roda em `test` (antes do `integration-test`), dando feedback rápido sobre regressões na ordem/transformação.
- **Appetite**: medium (1 a 2 semanas) — adiciona ~3-5 arquivos de teste Groovy, fixtures com snippets de exemplo, golden files, e integração com `gmaven:2.1.1:executeTests` ou `surefire-test`.
- **Trade-offs**:
  - **Wins**: tudo da Opção B; detecção de regressão de ordem/transformação em segundos em vez de minutos; confiança maior para evoluir a topologia.
  - **Sacrifices**: appetite ~3x maior; overhead de manter fixtures e golden files sincronizados com o estado real; mais código = mais superfície de bug.
  - **Risks**: golden files tendem a "viralat" — qualquer mudança intencional no output exige regenerar manualmente, e o teste vira guardrail inflexível. Sem CI próprio para validar o `groovy:test`, o valor do teste é parcial.
- **Rabbit holes**: cobertura de testes de "edge cases" que nunca acontecem (ex.: snippets com CRLF em vez de LF, BOM, encoding misto); golden file que vira "snapshot test" sem valor semântico.

## Recommendation

**Opção B — `SnippetComposer.groovy` + `gmaven:2.1.1` em `process-resources`.**

Razões atreladas aos goals e métricas de `problem.md`:

1. **Goal #1 (eliminar bash)**: cumpre integralmente — o `bash_script/create_archetype_post_generate.sh` deixa de ser invocado (Success Metric #4 verificável).
2. **Goal #2 (build-time, pré-processamento)**: cumpre — fase `process-resources` é estritamente anterior a `package` e a `integration-test`; mesmo sítio do `xml-maven-plugin:transform`.
3. **Goal #3 (ordem obrigatória)**: cumpre — `List<String> SECTIONS` literal, auditável, sem dependência de `ls -1` alfabético.
4. **Goal #4 (consolidação de imports)**: cumpre — passo equivalente ao `remove_lines_with_import_keyword` do bash, dentro do mesmo `SnippetComposer.groovy`.
5. **Goal #5 (compatibilidade IT)**: mantém como **constraint** — qualquer implementação da Opção B que quebrar 9/9 perfis falha no critério de aceitação primário.
6. **Success Metrics #1, #2, #3**: equivalência byte-a-byte permite validação mecânica.
7. **Success Metrics #5, #6**: elimina `read -n 1 -s`; tempo de execução comparável ou menor.
8. **Apetite small** (1-3 dias) é compatível com o custo de inação atual: 0 dias de maintenance por mês agora, mas com fragilidade crescente.
9. **Consistência com precedente interno** — `xml-maven-plugin:1.2.1` + `identity.xsl` já cumpre função análoga para `archetype-metadata.xml`; a Opção B replica o padrão textualmente (mesma fase, mesmo "transform → output congelado em `target/classes/`").

A Opção A é defensável **apenas** se o mantenedor decidir que a fragilidade atual é aceitável; nesse caso, a recomendação é **não proceder**. A Opção C é过度设计 (over-engineering) para um compositor de ~50 linhas; deve ser considerada se a Opção B se mostrar instável na prática, mas não é justificada ex-ante.

## Out of Scope (for the recommended option)

- Migrar `bash_script/create_archetype_metadata.sh` (já migrado de fato via `xml-maven-plugin`).
- Migrar ou eliminar **outros** bash scripts (`script_core.sh`, `maven_command_line_archetype_generate.sh`, etc.) — diretriz declarada, mas fora do escopo deste problema.
- Migrar para o novo formato declarativo de archetype do Maven 4.0.0-rc-7.
- Mudar a versão Groovy 4.0.28 do classpath do `maven-archetype-plugin`.
- Adotar plugins Groovy build-time diferentes de `gmaven:2.1.1` (descartados em research.md).
- Adicionar suíte de testes unitários do compositor (Opção C) — pode ser adicionada depois se a Opção B mostrar instabilidade.
- Decidir destino do `bash_script/create_archetype_post_generate.sh` (deletar / fallback / converter em teste) — deferido para o mantenedor por diretriz declarada.
- Decidir destino da `archetype-post-generate.groovy` atualmente commitada em `src/main/resources/META-INF/` (manter versionada ou mover para `.gitignore`).

## Assumptions to Validate

- **A1**: A fase `process-resources` no archetype principal é estritamente anterior a `package` e a `integration-test`. A confirmar empiricamente com `mvn help:effective-pom` ou `mvn validate` listando phases.
- **A2**: `<dependencies><dependency>org.apache.groovy:groovy:4.0.28</dependency></dependencies>` dentro do `<plugin>` do `gmaven:2.1.1` é suficiente para sobrescrever o Groovy 1.x/2.x default. Pode exigir `<exclusion>` explícito de `org.codehaus.gmaven:gmaven-adapter-impl` — a testar.
- **A3**: O regex bash `^[[:space:]]*(import|//[[:space:]]*import)` reproduz exatamente via Groovy `String.replaceAll(...)` com a mesma semântica POSIX. A validar com golden file de comparação.
- **A4**: A `List<String> SECTIONS` com a mesma ordem do bash (header → global-vars → 5 path-vars → variables (alfabético) → snippets (alfabético) → footer → file-functions → folder-functions) produz arquivo **byte-equivalente** ao bash. A validar com `diff` ou hash.
- **A5**: O `archetype-post-generate.groovy` gerado dentro de `target/classes/META-INF/` é empacotado automaticamente pelo `maven-archetype-plugin:3.4.1` quando o `packaging=maven-archetype` está configurado. A confirmar — pode exigir mover o arquivo de `target/classes` para `src/main/resources` (via `<resources>` no `pom.xml`) ou aceitar a sobrescrita durante o `process-resources`.
- **A6**: O `bash_script/create_archetype_post_generate.sh` é invocado **somente** pelo `script_core.sh:42` e por `bash_script/general_functions.sh` (a confirmar com `grep -l` no projeto). Se houver outros pontos de invocação, eles precisam ser atualizados em fase posterior.
- **A7**: O `archetype-post-generate.groovy` atualmente commitado em `src/main/resources/META-INF/` é a saída **literal** do bash — sem modificações manuais. Se houver edições manuais, a equivalência byte-a-byte falha e o A4 é invalidado.
