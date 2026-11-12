# Data Model: Compositor Nativo Groovy

**Feature**: 002-native-groovy-composer
**Phase**: 1 (design)
**Created**: 2026-09-28

## Entidades

### Snippet

**Descrição**: Arquivo `.groovy` individual que será concatenado no arquivo final `archetype-post-generate.groovy`.

**Atributos**:

| Campo | Tipo | Descrição |
|---|---|---|
| `name` | `String` | Nome do arquivo (ex.: `snippet-authentication.groovy`). Determinado pela convenção de prefixo. |
| `path` | `Path` | Caminho absoluto no sistema de arquivos. |
| `category` | `enum { BODY, VARIABLE, SNIPPET }` | Categoria do arquivo — definida pela pasta onde reside. |
| `prefix` | `String` (opcional) | Prefixo esperado: `snippet-` para categoria SNIPPET, `variables-` para categoria VARIABLE. Sem prefixo para BODY. |
| `commentBlock` | `String` (opcional) | Bloco de comentário no topo do arquivo, indicando flag acoplada (FR-007). |

**Regras de Validação**:

- Se `category == SNIPPET`, `name` MUST começar com `snippet-`.
- Se `category == VARIABLE`, `name` MUST começar com `variables-`.
- Se `category == BODY`, `name` MUST ser um dos 15 nomes canônicos, no padrão `body-NN-<papel>.groovy`: `body-01-imports.groovy`, `body-02-header.groovy`, `body-03-logging-variables.groovy`, `body-04-global-variables.groovy`, `body-05-path-root-variables.groovy`, `body-06-path-src-main-variables.groovy`, `body-07-path-src-test-variables.groovy`, `body-08-path-logback-variables.groovy`, `body-09-path-security-variables.groovy`, `body-10-binding-artifact-registry.groovy`, `body-11-footer.groovy`, `body-12-file-functions.groovy`, `body-13-folder-functions.groovy`, `body-14-java-source-functions.groovy`, `body-15-elk-functions.groovy`.
  O `NN` é documental (a ordem é definida por `SECTIONS`), exceto `body-01-imports.groovy`, que é o prelúdio prependido e não tem marcador.

**Transições de Estado**: nenhuma (arquivo imutável uma vez presente).

### Section

**Descrição**: Bloco nomeado no arquivo final. Define a ordem em que cada grupo de snippets aparece.

**Atributos**:

| Campo | Tipo | Descrição |
|---|---|---|
| `name` | `String` | Nome lógico da seção (ex.: `header`, `logging-variables`, `global-vars`, `path-vars-root`, `path-vars-src-main`, `path-vars-src-test`, `path-vars-logback`, `path-vars-security`, `binding-artifact-registry`, `variables`, `generators`, `snippets`, `footer`, `file-functions`, `folder-functions`, `java-source-functions`, `elk-functions`). |
| `category` | `enum { BODY, PATH_VAR, BODY_DATA, VARIABLES, GENERATORS, SNIPPETS, BODY_END }` | Sub-categoria interna. |
| `order` | `int` | Posição na `SECTIONS` (0-indexed). Determinada pelo mantenedor. |

**Ordem Canônica** (17 seções; `body-01-imports.groovy` é o prelúdio prependido, fora desta lista):

```
 0: header                      (BODY)       body-02-header.groovy
 1: logging-variables           (BODY)       body-03-logging-variables.groovy
 2: global-vars                 (BODY)       body-04-global-variables.groovy
 3: path-vars-root              (PATH_VAR)   body-05-path-root-variables.groovy
 4: path-vars-src-main          (PATH_VAR)   body-06-path-src-main-variables.groovy
 5: path-vars-src-test          (PATH_VAR)   body-07-path-src-test-variables.groovy
 6: path-vars-logback           (PATH_VAR)   body-08-path-logback-variables.groovy
 7: path-vars-security          (PATH_VAR)   body-09-path-security-variables.groovy
 8: binding-artifact-registry   (BODY_DATA)  body-10-binding-artifact-registry.groovy
 9: variables                   (VARIABLES, sorted by NN)
10: generators                  (GENERATORS, sorted by NN)
11: snippets                    (SNIPPETS, sorted by NN)
12: footer                      (BODY_END)   body-11-footer.groovy
13: file-functions              (BODY_END)   body-12-file-functions.groovy
14: folder-functions            (BODY_END)   body-13-folder-functions.groovy
15: java-source-functions       (BODY_END)   body-14-java-source-functions.groovy
16: elk-functions               (BODY_END)   body-15-elk-functions.groovy
```

### Composer Manifest

**Descrição**: Lista ordenada de Section definitions que constitui a "fonte da verdade" do compositor.

**Atributos**:

| Campo | Tipo | Descrição |
|---|---|---|
| `sections` | `List<Section>` | Lista ordenada. Declarada como `List<String> SECTIONS` no topo de `ArchetypePostGenerateComposer.groovy`. |
| `commentPerEntry` | `boolean` | Indica se cada entrada da lista tem comentário explicativo (FR-006). |

**Validação**:

- A lista MUST conter exatamente as 17 seções canônicas na ordem canônica.
- Qualquer desvio MUST ser explicitamente justificado em comentário.

### Imported-Block

**Descrição**: Conteúdo do arquivo `archetype_post_generate_body/body-01-imports.groovy`, prependido no topo do arquivo final.

**Atributos**:

| Campo | Tipo | Descrição |
|---|---|---|
| `content` | `String` | Conteúdo textual completo do arquivo (com linhas de import). |
| `transformedLines` | `String` | Após o `tr -d '/'` aplicado pelo bash legado. (Decisão: o compositor Groovy faz o mesmo `replaceAll('/', '')` em cada linha.) |

**Validação**:

- O arquivo MUST existir.
- O conteúdo MUST ser prepended no topo do arquivo final (após o cabeçalho Groovy padrão).

## Relacionamentos

```
Snippet (1..N) ──┐
                 ├──> Section ──(ordem em)──> Composer Manifest
Snippet (1..N) ──┘

Imported-Block ──(prepended em)──> Arquivo Final
                  Section(s) ────(concatenadas em ordem em)──> Arquivo Final
```

## Estado Global

- O Compositor é executado **uma vez por build** (na fase `process-resources`).
- O resultado é um arquivo único `target/classes/META-INF/archetype-post-generate.groovy` que é empacotado no archetype jar.
- O compositor **não mantém estado** entre execuções — cada build começa do zero.