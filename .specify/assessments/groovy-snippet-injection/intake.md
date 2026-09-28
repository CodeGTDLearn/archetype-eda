# Idea Intake: Substituir bash pre-processor do `archetype-post-generate.groovy` por composição nativa Groovy

- **Slug**: groovy-snippet-injection
- **Created**: 2026-09-28
- **Source**: pasted text
- **Type**: improvement

> **STATUS**: Addressed by feature `specs/002-native-groovy-composer/` (verdict: **go**, Opção B do concept.md). Implementação concluída em 2026-09-28 com composer Groovy em `src/main/groovy/eda/postgenerate/SnippetComposer.groovy` invocado via `gmaven:2.1.1` na fase `process-resources`. 9/9 perfis IT passando em `mvn clean install` + `mvn integration-test`. Bash legacy mantido em disco como referência, `bash_script/script_core.sh` linhas 41-42 comentadas.

## Idea (as captured)

```
"
1) Atualmente meu arquivo archetypepostgenerate.groov é gerado a partir de snippets.
2) Esses snippets encontram-se na pasta Archetype PostGenerator Body, Archetype PostGenerate Snippets, Archetype PostGenerate Variables.
3) A construção e montagem do arquivo Archetype Post Generate Groove se dá através do script bash.
4) Esse script Bash se encontra na pasta Bash Script\create_archetype_post_generate.sh
5) Definitivamente, esse script tem que ter uma ordem obrigatória para ser processado e montado.
5.1) Essa ordem, atualmente, pode ser encontrada no script bash de montagem do Arquivo Archetype pósgenerate.Groovy
6) Eu preciso criar uma estratégia, seja com includes, imports, alguma estratégia de injeção que injete os meus snippets dentro do arquivo archetypepostgenerate.groove.
7) Essa estratégia tem que ser nativa do Groovy, pois eu não quero mais usar nenhum sistema de bash no pré-processamento para a composição do arquivo archetype postgenerate.groovy
8) É muito importante que a minha atual versão do Groovy seja observada e a atual documentação do Groov também, para que implementar-se a estratégia de injecao nativacorreta.
9) Eu quero uma estratégia que seja o mais limpa possível, compreensível possível, simples possível e nativa do Groovy.
"
```

## Restated

O `archetype-post-generate.groovy` é hoje composto, em build-time, por concatenação de snippets (em `META-INF/archetype_post_generate_body/`, `..._snippets/` e `..._variables/`) feita por um script bash (`bash_script/create_archetype_post_generate.sh`), numa ordem obrigatória. Propõe-se substituir esse pre-processamento em bash por um mecanismo **nativo Groovy** que execute a mesma composição no build-time do archetype, eliminando a dependência do bash para esse fim.

## Origin & Context

- **Raised by**: o próprio usuário (dono do projeto `archetype-v2-eda`).
- **Trigger**: decisão estratégica de eliminar pré-processamento em bash do projeto. Já declarada em interações anteriores: *"Bash Script, nosso objetivo é eliminá-los num futuro bem breve."*

## First-Glance Unknowns

- [NEEDS CLARIFICATION: A composição nativa Groovy deve rodar em build-time (mesmo sítio do `xml-maven-plugin:transform` que hoje monta `archetype-metadata.xml`) e produzir um arquivo `archetype-post-generate.groovy` final já congelado, antes da fase `package` empacotar o archetype jar.]
- [NEEDS CLARIFICATION: A topologia atual (imports/defs globais no topo, snippets intermediários, helpers no rodapé) pode ser repensada desde que a ordem obrigatória dos snippets seja preservada.]
- [NEEDS CLARIFICATION: Os snippets compartilham estado via Groovy (defs globais, helpers `deleteFile`/`deleteFolder`/`copyFile`/etc.). A estratégia deve preservar esse compartilhamento.]
- [NEEDS CLARIFICATION: O usuário aceita a estratégia mais simples, "1 arquivo consolidado gerado uma vez e congelado", desde que seja nativa Groovy.]
- [NEEDS CLARIFICATION: O ciclo de testes IT (`mvn clean install` + `mvn integration-test` nos 9 perfis `basic`, `full`, `minimal`, `binding-default-only`, `binding-list-only`, `binding-map-only`, `binding-mutable-only`, `binding-validated-only`, `json-contract`) deve continuar passando. Idealmente, a invocação do composer deve ser movida para uma fase do archetype principal (anterior ao `integration-test`) para que os testes herdem o arquivo já montado.]

## Decisions Captured (during intake conversation)

| # | Decisão | Valor |
|---|---|---|
| 1 | Slug | `groovy-snippet-injection` |
| 2 | Fase de execução | **build-time** (pré-processamento do archetype) |
| 3 | Topologia do arquivo final | pode ser repensada; ordem dos snippets obrigatória |
| 4 | Estado compartilhado | sim, deve funcionar (closures/sharing) |
| 5 | Estratégia concreta | **Opção B — Concatenação Groovy** (`SnippetComposer.groovy` build-time com `List<String>` ordenada, gera 1 arquivo final congelado) |
| 6 | Versão Groovy | **4.0.28** (declarada no `groovy-bom` do `maven-archetype:3.4.1`) |
| 7 | Separação dos testes IT | **sim** — composer em fase anterior ao `integration-test`; testes herdam o arquivo já montado |
| 8 | Compatibilidade com `mvn clean install` + `mvn integration-test` (9 perfis) | obrigatória |

## Research Notes (pré-research)

- `maven-archetype-plugin:3.4.1` depende transitivamente de `archetype-common:3.4.1`, que importa `org.apache.groovy:groovy-bom:4.0.28` (via `maven-archetype:3.4.1` parent).
- Módulos Groovy disponíveis transitivamente: `groovy` (compile), `groovy-json`, `groovy-templates`, `groovy-xml`, `groovy-yaml` (runtime).
- Documentação Groovy 4: https://groovy-lang.org/single-page-documentation.html
- Mecanismos nativos Groovy 4 candidatos considerados: `GroovyShell.evaluate(new File(...))`; `GroovyScriptEngine`; `@BaseScript`; `groovy.util.ScriptRunner`; `Template` (groovy-templates). A Opção B escolhida usa apenas I/O simples + `String` (sem AST/sem script engine), sendo a mais limpa e a mais alinhada à estratégia atual de "1 arquivo final congelado" que o `xml-maven-plugin:transform`/`identity.xsl` já pratica para `archetype-metadata.xml`.
