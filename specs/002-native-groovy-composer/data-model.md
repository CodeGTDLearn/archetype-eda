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
- Se `category == BODY`, `name` MUST ser um dos 11 nomes canônicos: `archetype-imports.groovy`, `archetype-header.groovy`, `archetype-global-variables.groovy`, `archetype-path-root-variables.groovy`, `archetype-path-src-main-variables.groovy`, `archetype-path-src-test-variables.groovy`, `archetype-path-logback-variables.groovy`, `archetype-path-security-variables.groovy`, `archetype-footer.groovy`, `archetype-file-functions.groovy`, `archetype-folder-functions.groovy`.

**Transições de Estado**: nenhuma (arquivo imutável uma vez presente).

### Section

**Descrição**: Bloco nomeado no arquivo final. Define a ordem em que cada grupo de snippets aparece.

**Atributos**:

| Campo | Tipo | Descrição |
|---|---|---|
| `name` | `String` | Nome lógico da seção (ex.: `header`, `global-vars`, `path-vars-root`, `path-vars-src-main`, `path-vars-src-test`, `path-vars-logback`, `path-vars-security`, `variables`, `snippets`, `footer`, `file-functions`, `folder-functions`). |
| `category` | `enum { BODY, PATH_VAR, VARIABLES, SNIPPETS, BODY_END }` | Sub-categoria interna. |
| `order` | `int` | Posição na `SECTIONS` (0-indexed). Determinada pelo mantenedor. |

**Ordem Canônica**:

```
0: header (BODY)
1: global-vars (BODY)
2: path-vars-root (PATH_VAR)
3: path-vars-src-main (PATH_VAR)
4: path-vars-src-test (PATH_VAR)
5: path-vars-logback (PATH_VAR)
6: path-vars-security (PATH_VAR)
7: variables (VARIABLES, sorted alphabetically)
8: snippets (SNIPPETS, sorted alphabetically)
9: footer (BODY_END)
10: file-functions (BODY_END)
11: folder-functions (BODY_END)
```

### Composer Manifest

**Descrição**: Lista ordenada de Section definitions que constitui a "fonte da verdade" do compositor.

**Atributos**:

| Campo | Tipo | Descrição |
|---|---|---|
| `sections` | `List<Section>` | Lista ordenada. Declarada como `List<String> SECTIONS` no topo de `SnippetComposer.groovy`. |
| `commentPerEntry` | `boolean` | Indica se cada entrada da lista tem comentário explicativo (FR-006). |

**Validação**:

- A lista MUST conter exatamente as 12 seções canônicas na ordem canônica.
- Qualquer desvio MUST ser explicitamente justificado em comentário.

### Imported-Block

**Descrição**: Conteúdo do arquivo `archetype_post_generate_body/archetype-imports.groovy`, prependido no topo do arquivo final.

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