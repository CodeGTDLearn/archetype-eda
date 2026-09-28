# Research: Compositor Nativo Groovy para `archetype-post-generate.groovy`

**Feature**: 002-native-groovy-composer
**Phase**: 0 (research)
**Created**: 2026-09-28

**Source**: Consolidação das 7 carried-forward assumptions de `.specify/assessments/groovy-snippet-injection/concept.md`, validadas empiricamente contra o estado atual do projeto.

## Decisão 1: Fase Maven para invocação do compositor

- **Decision**: `process-resources`
- **Rationale**: ordem de phases Maven padrão é `validate → initialize → generate-sources → process-sources → generate-resources → process-resources → compile → ... → package → integration-test → verify`. `process-resources` vem imediatamente após `generate-resources` (onde `xml-maven-plugin:transform` monta `archetype-metadata.xml`) e antes de `package` (que empacota o archetype jar) e de `integration-test` (que gera 9 projetos via invoker). Mesmo sítio que o `xml-maven-plugin:transform` ocupa no ciclo, o que garante simetria com o precedente interno.
- **Alternatives considered**:
  - `generate-resources` — descartado porque o `xml-maven-plugin:transform` já está em `generate-resources`; misturar dois transformadores na mesma fase é confuso.
  - `generate-sources` — descartado porque o compositor não gera código Java, só processa recursos.
  - Fase customizada — não há razão para desviar do padrão; `process-resources` é o local canônico para "gerar recursos a partir de outros recursos".
- **Validated**: ✅ por inspeção da ordem padrão Maven3 Lifecycle reference.

## Decisão 2: Como fixar Groovy 4.0.28 no classpath do `gmaven:2.1.1`

- **Decision**: declarar `<dependencies>` dentro do `<plugin>` com `org.apache.groovy:groovy:4.0.28` e `org.apache.groovy:groovy-json:4.0.28` (runtime). Sem exclusion inicial de `gmaven-adapter-impl`. Validar empiricamente em build — se `NoSuchMethodError` ou similar aparecer, adicionar exclusion.
- **Rationale**: `<dependencies>` em `<plugin>` é mecanismo padrão Maven3 para adicionar dependências ao classpath do plugin. Isso **adiciona** ao classpath, não substitui — por isso a exclusion só é necessária se houver conflito (improvável entre `org.apache.groovy:groovy:4.0.28` e `gmaven-adapter-impl`, que depende de `org.codehaus.groovy:groovy-all` antigo). Risco é baixo; custo de reversão é trivial.
- **Alternatives considered**:
  - Usar `org.codehaus.gmavenplus:gmavenplus-plugin` — alternativa mais moderna, mas exigiria novo investigation. Fora de escopo (research.md descarta plugins diferentes).
  - Usar `org.codehaus.mojo:exec-maven-plugin` chamando Groovy CLI — descartado por adicionar fragilidade (Groovy no PATH).
- **Validated**: 🟡 provável; validar empiricamente no build.

## Decisão 3: Regex de consolidação de imports

- **Decision**: usar `Pattern.compile("^\\s*import")` e `Pattern.compile("^\\s*//\\s*import")` aplicados via `String.replaceAll(...)`. Validar byte-equivalência com `diff`.
- **Rationale**: Groovy `String.replaceAll` usa regex Java por padrão. O regex bash `^[[:space:]]*(import|//[[:space:]]*import)` é POSIX; em Java, `\\s` é semanticamente equivalente a `[[:space:]]` para casos ASCII normais (tabs, espaços). Para máxima previsibilidade, **separar os dois patterns** (um para `import` puro, outro para `//import` ou `// import`) e processá-los sequencialmente, em vez de tentar replicar o POSIX compound regex.
- **Alternatives considered**:
  - Replicar o regex bash literal em Groovy — `^\\s*(import|//\\s*import)` — funcionaria, mas é menos legível.
  - `Pattern.UNICODE_CHARACTER_CLASS` — desnecessário para ASCII; `\\s` cobre whitespace ASCII.
- **Validated**: 🟡 a validar com `diff` byte-a-byte.

## Decisão 4: Ordem de variables/snippets

- **Decision**: declarar `List<String>` ordenada alfabeticamente; usar `new File(dir).listFiles().sort { it.name }` em Groovy, que retorna a mesma ordem que `ls -1` no bash.
- **Rationale**: equivalência byte-a-byte é o critério. `File.listFiles().sort { it.name }` é equivalente a `ls -1 | sort` em bash (lexicográfica ASCII). Em ambos os casos, `snippet-authentication.groovy` vem antes de `snippet-binding-properties.groovy`.
- **Alternatives considered**:
  - `List<String>` explícita por nome (sem `sort()`) — descartado porque tornaria o composer frágil a novos arquivos (qualquer snippet novo fora da ordem explícita quebraria).
  - `List<String>` por prioridade semântica — descartado porque adiciona decisões subjetivas sem ganho claro.
- **Validated**: ✅ logicamente equivalente ao bash; a validar com `diff` byte-a-byte.

## Decisão 5: Onde gerar `archetype-post-generate.groovy`

- **Decision**: gerar em `target/classes/META-INF/archetype-post-generate.groovy` (mesmo padrão do `archetype-metadata.xml` gerado por `xml-maven-plugin:transform` em `target/classes/META-INF/maven/archetype-metadata.xml`).
- **Rationale**: o `pom.xml` (linha 26-33) já tem `<resources>` que **exclui** `META-INF/maven/archetype-metadata.xml` do `src/main/resources` (porque é gerado). Mesma estratégia: adicionar `<exclude>META-INF/archetype-post-generate.groovy</exclude>` para que o `process-resources` não copie o arquivo velho. O arquivo gerado em `target/classes/META-INF/` será empacotado automaticamente pelo `package`.
- **Alternatives considered**:
  - Gerar em `src/main/resources/META-INF/archetype-post-generate.groovy` — descartado porque misturaria artefato gerado com código-fonte, e o `.gitignore` ficaria dependente de gitignore configuration (coupling com git).
  - Mover o script compositor para uma pasta fora de `src/main/groovy/` (ex.: `scripts/`) — descartado porque Groovy build-time em Maven3 prefere `src/main/groovy/` por convenção, e o classpath do plugin já o encontra.
- **Validated**: ✅ por inspeção do `pom.xml` atual.

## Decisão 6: Caller único de `bash_script/create_archetype_post_generate.sh`

- **Decision**: confirmado por `grep` no projeto. **Único caller é `script_core.sh:42`** (via `source` na linha 41). Para remover a invocação, basta remover (ou comentar) a linha 42 de `script_core.sh` e a chamada `source` na linha 41.
- **Rationale**: validação empírica — `grep -r 'create_archetype_post_generate' bash_script/` retorna apenas 3 matches (definição da função + source + chamada). Nenhuma outra referência em `src/`, `pom.xml`, ou `.specify/`.
- **Alternatives considered**:
  - Manter `script_core.sh` chamando o bash mas com flag `SKIP_BASH_POST_GENERATE=true` — descartado; polui o script com flag condicional, e a diretriz é eliminar o pré-processamento em bash.
- **Validated**: ✅ por `grep`.

## Decisão 7: Estado do `archetype-post-generate.groovy` commitado

- **Decision**: comparar byte-a-byte com a saída do compositor Groovy via `diff`. Se idêntico, remover o arquivo de `src/main/resources/META-INF/` e adicionar entrada em `.gitignore` (ou `git rm --cached` se já estiver em commit anterior).
- **Rationale**: o `archetype-post-generate.groovy` commitado é o "golden file" implícito — a saída do compositor deve ser literalmente esse arquivo. Se houver divergência (qualquer byte), investigar antes de remover.
- **Alternatives considered**:
  - Manter versionado — descartado porque vira source of truth divergente do build, exatamente o anti-padrão que a spec visa evitar.
- **Validated**: �� a validar com `diff` no build de implementação.

## Resumo das Decisões

| # | Decisão | Status |
|---|---|---|
| 1 | Fase Maven: `process-resources` | ✅ Confirmado por ordem Maven3 |
| 2 | `<dependencies>` fixando Groovy 4.0.28, sem exclusion inicial | 🟡 Validar empiricamente |
| 3 | Regex: dois `Pattern` separados (import + //import) | 🟡 Validar com `diff` |
| 4 | Ordem alfabética via `listFiles().sort { it.name }` | ✅ Equivalente a `ls -1` |
| 5 | Gerar em `target/classes/META-INF/`, excluir de `src/main/resources/` | ✅ Padrão já usado para `archetype-metadata.xml` |
| 6 | Único caller é `script_core.sh:42` | ✅ Confirmado por `grep` |
| 7 | Validar byte-equivalência antes de remover do controle de versão | 🟡 Validar com `diff` |
