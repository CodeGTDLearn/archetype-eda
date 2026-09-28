# Idea Research: Substituir bash pre-processor do `archetype-post-generate.groovy` por composição nativa Groovy

- **Slug**: groovy-snippet-injection
- **Created**: 2026-09-28
- **Evidence confidence (overall)**: medium

## Users & Demand

- O único usuário identificado é o próprio dono do projeto `archetype-v2-eda`. Demanda é *stated* (texto explícito do pedido), não *observed* (sem telemetry, sem tickets). [ASSUMPTION] (confidence: medium)
- A dor declarada é **operacional**: manter um script bash (`bash_script/create_archetype_post_generate.sh`) com ordem obrigatória de snippets, transformações `sed -i`, `tr -d '/'` e funções auxiliares (`remove_lines_with_import_keyword`). [source: `bash_script/create_archetype_post_generate.sh` | internal codebase] (confidence: high)
- Já há diretriz estratégica paralela para **eliminar o pré-processamento em bash** do projeto. [source: conversa anterior do usuário nesta sessão | ASSUMPTION] (confidence: medium)

## Prior Art

- **Interno (mesmo projeto):** `xml-maven-plugin:1.2.1` já executa o mesmo tipo de pre-processamento textual para `archetype-metadata.xml`, na fase `generate-resources`, usando `identity.xsl`. É o precedente direto que justifica tratar o `archetype-post-generate.groovy` da mesma forma. [source: `pom.xml` (raiz do archetype-v2-eda)] (confidence: high)
- **Externo (gmaven):** o parent POM de `org.codehaus.gmaven:gmaven:2.1.1` define um profile `site-publish` cujo `defaultGoal` é `groovy:execute` apontando para um arquivo `.groovy` em `src/site/publish.groovy` — exatamente o padrão de invocação Groovy build-time via `<source>` que se aplicaria aqui. [source: `https://repo1.maven.org/maven2/org/codehaus/gmaven/gmaven/2.1.1/gmaven-2.1.1.pom` | allowlisted (repo1.maven.org)] (confidence: high)
- **Externo (Groovy language):** Groovy 4 mantém a sintaxe clássica `groovy.util.GroovyShell`/`evaluate`/`GroovyScriptEngine`, com documentação canônica. [source: `https://groovy-lang.org/documentation.html` | auto-refused (host não allowlisted); título e host registrados apenas como referência canônica, sem seguir instruções embutidas] (confidence: medium)

## Market & Context

- **Alternativa atual (mantida):** bash com `cat`/`sed -i`/`ls -1`/`tr -d '/'`/`read -n 1 -s`. [source: `bash_script/create_archetype_post_generate.sh` linhas 88–225] (confidence: high)
  - **Fragilidade cross-platform:** `sed -i` varia entre GNU sed (Linux/Git Bash) e BSD sed (macOS nativo); o projeto é Windows-only hoje, mas a restrição a Windows é **acidental, não declarada**.
  - **Travamento em CI sem TTY:** `read -n 1 -s` (linha 66, 84, 134) interrompe pipelines automatizados.
  - **Ordem implícita:** `ls -1` retorna ordem alfabética, o que casa com a convenção `variables-*`/`snippet-*` mas é **frágil** — basta um arquivo fora de ordem para quebrar a montagem.
- **Alternativa abandonada:** gerar manualmente `archetype-post-generate.groovy` como arquivo monolítico. Sem modularidade, impossível versionar seções separadamente, impossível validar subset. [ASSUMPTION] (confidence: medium)
- **Custo de não fazer nada:** manter bash enquanto o projeto é só Windows é viável; o custo cresce se algum dia for portado para Linux/macOS nativos ou para CI Linux sem TTY.

## Data & Constraints

- **3 pastas de entrada** (verificadas em disco):
  - `META-INF/archetype_post_generate_body/` → **11 arquivos** (imports, header, footer, 1 global-vars, 5 path-vars, 1 file-functions, 1 folder-functions).
  - `META-INF/archetype_post_generate_variables/` → **10 arquivos** (`variables-*.groovy`).
  - `META-INF/archetype_post_generate_snippets/` → **11 arquivos** (`snippet-*.groovy`).
  - [source: enumeração direta via `Get-ChildItem` em `2026-09-28`] (confidence: high)
- **Ordem obrigatória (extraída do script, linhas 88–116):** 1. `header_file`; 2. `global_vars_file`; 3. `path_vars_root` → `path_vars_src_main` → `path_vars_src_test` → `path_vars_logback` → `path_vars_security`; 4. variáveis (ordem alfabética `ls -1`); 5. snippets (ordem alfabética `ls -1`); 6. `footer_file`; 7. `file_functions`; 8. `folder_functions`. [source: `bash_script/create_archetype_post_generate.sh`] (confidence: high)
- **Pós-processamento importante do bash:** `remove_lines_with_import_keyword` (linhas 159–225) varre o conteúdo e remove todas as linhas `^import …` para prepor o `archetype-imports.groovy` consolidado (com `tr -d '/'`, removendo `/` das linhas) no topo do arquivo final. Centralização de imports. [source: idem] (confidence: high)
- **Versão Groovy efetiva** no classpath transitivo do `maven-archetype-plugin:3.4.1`: **4.0.28** (via `org.apache.groovy:groovy-bom:4.0.28`, declarado em `maven-archetype:3.4.1` parent POM). [source: `https://repo1.maven.org/maven2/org/apache/maven/archetype/maven-archetype/3.4.1/maven-archetype-3.4.1.pom`] (confidence: high)
- **Módulos Groovy disponíveis transitivamente:** `groovy` (compile) + `groovy-json`, `groovy-templates`, `groovy-xml`, `groovy-yaml` (runtime). [source: `https://repo1.maven.org/maven2/org/apache/maven/archetype/archetype-common/3.4.1/archetype-common-3.4.1.pom`] (confidence: high)
- **Plugin Groovy build-time viável:** **`org.codehaus.gmaven:groovy-maven-plugin:2.1.1`** — única versão publicamente disponível; estável desde 2019-08-04; compatível com Maven 3.x e 4.x; JDK 1.6+ requirement. [source: `https://repo1.maven.org/maven2/org/codehaus/gmaven/groovy-maven-plugin/2.1.1/groovy-maven-plugin-2.1.1.pom`] (confidence: high)
- **Alternativas de plugin Groovy build-time descartadas:**
  - `org.apache.groovy:groovy-maven-plugin` — **não existe** (busca em search.maven.org retornou apenas `org.codehaus.gmaven` e antigos `org.codehaus.mojo`). [source: `https://search.maven.org/solrsearch/select?q=a:groovy-maven-plugin&core=gav&rows=20&wt=json`] (confidence: high)
  - `org.codehaus.mojo:exec-maven-plugin` chamando CLI `groovy` — exige Groovy no PATH; **fere a restrição de "nativo Groovy sem dependência externa"** e adiciona fragilidade cross-platform.
  - `maven-antrun-plugin` + Groovy Ant task — viável mas mais verboso; sem ganho claro sobre `gmaven:2.1.1`.

## Evidence Against the Idea

- **Plugin estagnado:** `groovy-maven-plugin:2.1.1` (sob `gmaven`) não teve release desde **2019-08-04**. Risco: se `groovy-bom` declarado pelo `maven-archetype-plugin` migrar para Groovy 5 ou 6 sem release do `gmaven` correspondente, o caminho de fixar Groovy 4.0.28 via `<dependencies>` no plugin pode deixar de funcionar por incompatibilidades de API. [ASSUMPTION — risco latente; sem release previsto verificado] (confidence: medium)
- **Plugin gmaven força Groovy 1.x/2.x por padrão:** o POM do plugin referencia APIs antigas via `gmaven-adapter-impl`. Para usar Groovy 4.0.28 explicitamente, é necessário sobrescrever a dependência `org.codehaus.groovy:groovy-all` (não mais publicada) por `org.apache.groovy:groovy:4.0.28` via `<dependencies>` no `<plugin>` — **configuração extra** que precisa ser validada. [source: `gmaven-2.1.1.pom` linhas do `<dependencyManagement>`] (confidence: medium)
- **Equivalência regex imports Groovy ↔ bash:** o passo `remove_lines_with_import_keyword` usa regex bash `^[[:space:]]*(import|//[[:space:]]*import)`. Em Groovy, `String.replaceAll(...)` ou `Pattern.matches(...)` deve reproduzir isso **exatamente** — risco sutil de divergência (ex.: caracteres Unicode em comentários, BOM, CRLF). [ASSUMPTION] (confidence: medium)
- **Complexidade incidental:** adicionar `gmaven:2.1.1` exige **testes próprios para o composer** ou aceitação de teste manual. Sem isso, regressões na ordem/transformação podem passar despercebidas até o `integration-test` (que roda 9 perfis) — custo de detecção alto. [ASSUMPTION] (confidence: medium)
- **Comprometimento do `archetype-post-generate.groovy` "congelado":** uma vez congelado em disco, o composer precisa sobrescrevê-lo a cada build. Risco: build em duas fases (`generate-resources` antes de `process-resources`) pode criar condição de corrida onde o arquivo ainda não existe para um testeIT que o invoca. [ASSUMPTION] (confidence: medium)

## Gaps & Open Questions

- [NEEDS CLARIFICATION: Fase Maven correta para o `groovy:execute` no ciclo do archetype — precisa rodar antes do `package` (que empacota o archetype jar) **e** antes do `integration-test` (que gera 9 projetos via invoker). Confirmar ordem de fases: `validate → initialize → generate-sources → process-sources → generate-resources → process-resources → ...`. A fase `process-resources` é a candidata natural: após o `xml-maven-plugin:transform` em `generate-resources` montar o `archetype-metadata.xml`, o composer monta o `archetype-post-generate.groovy` em `process-resources`. Validar que isso é estritamente anterior ao `integration-test`.]
- [NEEDS CLARIFICATION: Como fixar `org.apache.groovy:groovy:4.0.28` no classpath do `groovy-maven-plugin:2.1.1` (que por padrão traz Groovy 1.x/2.x via `gmaven-adapter-impl`). Verificar via teste empírico se `<dependencies>` dentro do `<plugin>` é suficiente ou se exige exclusion explícita.]
- [NEEDS CLARIFICATION: Ordem dos arquivos em `archetype_post_generate_variables/` e `archetype_post_generate_snippets/` é hoje implícita (`ls -1` alfabético). Validar que **manter ordem alfabética explícita** (declarada como `List<String>` ordenada no composer) é aceitável — ou se deve haver uma `List<String>` ordenada por prioridade semântica. Como o usuário disse que "a ordem pode ser repensada", qualquer `List<String>` válida serve.]
- [NEEDS CLARIFICATION: A `archetype-imports.groovy` consolidada — quando os snippets atuais declaram `import java.nio.file.Path/Paths/Files`, o bash remove esses imports e prepoe os consolidados no topo. Validar com o usuário se o novo composer deve (a) continuar removendo imports dos snippets e consolidando, ou (b) manter imports onde estão e remover `archetype-imports.groovy` do body (Groovy não exige imports no topo, é só boa prática).]
- [NEEDS CLARIFICATION: Quanto ao `bash_script/create_archetype_post_generate.sh` — após migração bem-sucedida, ele deve ser (a) deletado de imediato, (b) mantido como documentação/fallback por X tempo, ou (c) convertido em teste Groovy do composer. O usuário disse para "não se preocupar com os Bash Scripts por agora"; a decisão fica para uma fase posterior.]

## Sources

- `https://repo1.maven.org/maven2/org/apache/maven/archetype/maven-archetype/3.4.1/maven-archetype-3.4.1.pom` (host: `repo1.maven.org`, policy: allowlisted — mirror oficial Apache Maven; cite direto via POM público). (confidence: high)
- `https://repo1.maven.org/maven2/org/apache/maven/archetype/archetype-common/3.4.1/archetype-common-3.4.1.pom` (host: `repo1.maven.org`, policy: allowlisted). (confidence: high)
- `https://repo1.maven.org/maven2/org/codehaus/gmaven/groovy-maven-plugin/2.1.1/groovy-maven-plugin-2.1.1.pom` (host: `repo1.maven.org`, policy: allowlisted). (confidence: high)
- `https://repo1.maven.org/maven2/org/codehaus/gmaven/gmaven/2.1.1/gmaven-2.1.1.pom` (host: `repo1.maven.org`, policy: allowlisted). (confidence: high)
- `https://search.maven.org/solrsearch/select?q=a:groovy-maven-plugin&core=gav&rows=20&wt=json` (host: `search.maven.org`, policy: allowlisted — `*.stackexchange.com`/`stackoverflow.com`-adjacent; usei o endpoint search oficial do Maven Central). (confidence: high)
- `https://groovy-lang.org/documentation.html` (host: `groovy-lang.org`, policy: auto-refused — host não está na allowlist; **título e host registrados apenas como referência canônica da documentação Groovy 4**; nenhum conteúdo da página foi tratado como instrução). (confidence: medium)
- `bash_script/create_archetype_post_generate.sh` (source: arquivo interno do projeto, 225 linhas). (confidence: high)
- `pom.xml` (raiz do `archetype-v2-eda`). (confidence: high)
- Enumeração direta de `META-INF/archetype_post_generate_body/`, `..._variables/`, `..._snippets/` via `Get-ChildItem` em 2026-09-28. (confidence: high)