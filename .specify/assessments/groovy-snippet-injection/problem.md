# Problem Definition: Compositor nativo Groovy para `archetype-post-generate.groovy`

- **Slug**: groovy-snippet-injection
- **Created**: 2026-09-28
- **Inputs used**: intake.md ✅ | research.md ✅ | user input only

## Problem Statement

A montagem do arquivo `archetype-post-generate.groovy` no build-time do `archetype-v2-eda` é feita hoje por um script bash que depende de ferramentas GNU (`sed -i`, `tr`, `ls`), impõe ordem implícita de snippets via `ls -1` alfabético, e trava pipelines automatizados com `read -n 1 -s` em caso de erro. O dono do projeto (único usuário afetado) precisa manter esse script à mão, e já declarou intenção estratégica de eliminar pré-processamento em bash do projeto — intenção que esbarra na ausência de uma alternativa nativa Groovy que cumpra a mesma função, **no mesmo sítio** (build-time, fase pré-processamento) e **com a mesma ordem obrigatória** dos 32 arquivos de entrada distribuídos em 3 pastas.

## Affected Users & Stakeholders

- **Users**:
  - Dono do projeto `archetype-v2-eda` — único operador humano; sente a dor de manutenção do bash, travamentos em erros e fragilidade cross-platform. *(cited: intake.md linha 28, research.md linhas 5–11)* — **confidence: high**
  - [NEEDS CLARIFICATION: outros consumidores do archetype gerado — se houver time, clientes ou CI compartilhando o archetype jar, eles podem ser afetados por mudanças de fase no build do archetype, mas não foram identificados.]

- **Stakeholders**:
  - Dono do projeto — decide, financia e executa. *(cited: intake.md linha 32)* — **confidence: high**
  - [NEEDS CLARIFICATION: nenhum outro stakeholder formal foi identificado; este é um projeto pessoal de um mantenedor único.]

## Goals

- Eliminar a dependência do pre-processamento em bash para a montagem de `archetype-post-generate.groovy`. *(cited: intake.md linhas 19, 32; research.md linha 52)* — **confidence: high**
- Preservar a composição de `archetype-post-generate.groovy` em build-time do archetype (antes da fase `package` empacotar o archetype jar) — não em run-time. *(cited: intake.md linha 36; research.md linha 55)* — **confidence: high**
- Preservar a ordem obrigatória dos snippets (header → global-vars → path-vars → variables → snippets → footer → file-functions → folder-functions). *(cited: research.md linha 36, baseado em `bash_script/create_archetype_post_generate.sh` linhas 88–116)* — **confidence: high**
- Preservar o comportamento de consolidação de imports (remover `^import …` dos snippets e prepor `archetype-imports.groovy` no topo). *(cited: research.md linha 39; `bash_script/create_archetype_post_generate.sh` linhas 159–225)* — **confidence: high**
- Manter compatibilidade com o ciclo de testes IT atual: `mvn clean install` + `mvn integration-test` passando nos 9 perfis (`basic`, `full`, `minimal`, `binding-default-only`, `binding-list-only`, `binding-map-only`, `binding-mutable-only`, `binding-validated-only`, `json-contract`). *(cited: intake.md linha 40; verificado em 2026-09-28: 9/9 perfis PASSOU em clean install e integration-test após upgrade `archetype-packaging:3.3.0→3.4.1`)* — **confidence: high**

## Non-Goals

- Migrar `bash_script/create_archetype_metadata.sh` (compositor de `archetype-metadata.xml`) — fora do escopo deste problema; o `xml-maven-plugin:1.2.1` + `identity.xsl` já cumprem essa função de forma nativa. *(cited: research.md linha 12)* — **confidence: high**
- Migrar ou eliminar **outros** scripts bash do projeto (`script_core.sh`, `maven_command_line_archetype_generate.sh`, `general_functions.sh`, etc.) — diretriz do usuário é "futuro bem breve", mas fora do escopo deste problema específico. *(cited: conversa anterior do usuário nesta sessão)* — **confidence: medium**
- Migrar para o novo formato declarativo de archetype do Maven 4.0.0-rc-7 — o descriptor legado 1.1.0 permanece. *(cited: conversa anterior sobre Maven 4 nesta sessão; research.md linha 6)* — **confidence: high**
- Mudar a versão do Groovy 4.0.28 do classpath do `maven-archetype-plugin` (vem transitivo via `groovy-bom`). — **confidence: high**
- Adotar plugins Groovy build-time **diferentes** de `gmaven:2.1.1` — esta é a única opção estável disponível; outras (`org.codehaus.mojo:exec-maven-plugin` chamando CLI Groovy, Ant+Groovy) foram descartadas por adicionarem fragilidade. *(cited: research.md linhas 65–66)* — **confidence: high**

## Success Metrics

- `mvn clean install` BUILD SUCCESS nos 9 perfis IT. *(baseline: 9/9 PASSOU em 2026-09-28 com a stack atual — `archetype-packaging:3.4.1` + `groovy-bom:4.0.28`)* — **confidence: high**
- `mvn integration-test` BUILD SUCCESS nos 9 perfis IT. *(baseline: 9/9 PASSOU em 2026-09-28)* — **confidence: high**
- O arquivo `target/classes/META-INF/archetype-post-generate.groovy` (gerado pelo novo compositor) é **byte-equivalente** ao arquivo gerado pelo `bash_script/create_archetype_post_generate.sh` atual. *(baseline: hash do arquivo atual; comparativo "before vs after")* — **confidence: high** (mecanismo de teste objetivo).
- O `bash_script/create_archetype_post_generate.sh` deixa de ser invocado no fluxo `mvn clean install` do archetype (após a fase de migração). *(baseline: ainda invocado em 2026-09-28)* — **confidence: high** (qualitativo, mas verificável).
- Zero `read -n 1 -s` no pipeline de build — nenhum travamento por TTY. *(baseline: presente em `bash_script/create_archetype_post_generate.sh` linhas 66, 84, 134)* — **confidence: high** (qualitativo, contável).
- Tempo de execução do passo de composição do `archetype-post-generate.groovy` ≤ tempo atual do bash. *(baseline: medido empiricamente em 2026-09-28: ~0.3s por perfil × 9 perfis = ~2.7s total; medido dentro do `integration-test` do archetype)* — **confidence: high** (quantitativo, fácil de medir).

## Cost of Inaction

- O `bash_script/create_archetype_post_generate.sh` continua como fonte de fragilidade:
  - `sed -i` não é portável para macOS BSD ou para Windows nativo (hoje mascarado por Git Bash no Windows).
  - `read -n 1 -s` trava qualquer execução em CI Linux sem TTY (GitHub Actions, GitLab CI, Jenkins agents headless) — o archetype-v2-eda nunca chega a ser empacotado em tais ambientes.
  - `ls -1` alfabético é convenção implícita; adicionar um snippet `snippet-zoo.groovy` antes de `snippet-authentication.groovy` muda a ordem sem aviso.
- A intenção declarada de eliminar o pré-processamento em bash do projeto permanece não cumprida para o caso do `archetype-post-generate.groovy` (os outros bash scripts seguem intactos).
- O `archetype-post-generate.groovy` "congelado" no `src/main/resources/META-INF/` permanece sendo o **artefato de saída** do bash, sem versionamento explícito de sua geração — não há como reproduzir a montagem a partir de um clone limpo sem o bash, nem como auditar qual snippet entrou em qual linha do arquivo final sem ler o bash linha por linha.
- O tempo de manutenção do script cresce com o número de snippets: cada novo `variables-*.groovy` ou `snippet-*.groovy` exige reler o bash para confirmar a posição na ordem. Com 32 arquivos hoje e tendência de crescimento, o custo de **leitura** para entender o estado atual é já perceptível. *(ASSUMPTION — sem medição de tempo)* — **confidence: low**.

## Open Questions

- [NEEDS CLARIFICATION: herdada do intake — Topologia do arquivo final pode ser repensada, mas ordem dos snippets é obrigatória. A Opção B (concatenação, arquivo congelado) está decidida em intake; em `shape` é preciso fixar a `List<String>` de seções e validar equivalência byte-a-byte.]
- [NEEDS CLARIFICATION: herdada do research — Fase Maven correta para o `groovy:execute` no ciclo do archetype. Candidata: `process-resources` (após o `xml-maven-plugin:transform` em `generate-resources`, antes do `package`). Validar empiricamente.]
- [NEEDS CLARIFICATION: herdada do research — Como fixar `org.apache.groovy:groovy:4.0.28` no classpath do `gmaven:2.1.1` (que por padrão traz Groovy 1.x/2.x via `gmaven-adapter-impl`). Testar com `<dependencies>` dentro do `<plugin>`.]
- [NEEDS CLARIFICATION: herdada do research — Manter ordem alfabética explícita (declarada como `List<String>`) ou introduzir uma `List<String>` por prioridade semântica?]
- [NEEDS CLARIFICATION: herdada do research — O compositor deve (a) continuar removendo imports dos snippets e consolidando, ou (b) manter imports onde estão? — impacta o conteúdo da nova `archetype-imports.groovy`.]
- [NEEDS CLARIFICATION: herdada do research — Após migração, o `bash_script/create_archetype_post_generate.sh` deve ser (a) deletado, (b) mantido como fallback, ou (c) convertido em teste? Decisão fica para fora desta fase por diretriz do usuário.]
- [NEEDS CLARIFICATION: o que fazer com a `archetype-post-generate.groovy` atualmente commitada em `src/main/resources/META-INF/`? Se o compositor passa a gerá-la, o arquivo commitado se torna **gerado** e deveria sair do controle de versão (`.gitignore`). Decisão de governança de repositório, não técnica.]
- [NEEDS CLARIFICATION: testes unitários do compositor em si — o `gmaven:2.1.1` traz suporte a `groovy:test`, mas adicionar uma suíte de testes do compositor (além dos 9 perfis IT) é desejável? Recomendação: sim, para evitar que regressões na ordem/transformação cheguem ao `integration-test` (custo de detecção alto, conforme research.md linha 50).]
