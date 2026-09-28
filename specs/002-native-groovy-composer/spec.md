# Feature Specification: Compositor Nativo Groovy para `archetype-post-generate.groovy`

**Feature Branch**: `002-native-groovy-composer`

**Created**: 2026-09-28

**Status**: Draft

**Input**: Substituir o pre-processamento em bash de `archetype-post-generate.groovy` por composição nativa Groovy em build-time, eliminando dependência do bash e facilitando extensibilidade via flags/IA.

**Decisão de origem**: `.specify/assessments/groovy-snippet-injection/decision.md` (verdict: **go**, Opção B do concept)

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Mantenedor adiciona nova flag e novo snippet Groovy ao archetype (Priority: P1)

O mantenedor do `archetype-v2-eda` precisa adicionar uma nova capacidade opcional (ex.: uma flag `enable_xxx` que ativa/desativa um comportamento no projeto gerado). Para isso, ele cria/atualiza três arquivos: `property-xxx.xml` em `archetype_metadata_properties/`, `variables-xxx.groovy` em `archetype_post_generate_variables/`, `snippet-xxx.groovy` em `archetype_post_generate_snippets/`, e adiciona o nome do novo snippet à lista ordenada de seções no script compositor. O ciclo completo entre editar e ver o snippet no arquivo gerado leva minutos, sem necessidade de tocar em nenhum script bash.

**Why this priority**: é o caminho primário de evolução do archetype declarado como requisito estrutural (restrição R2 da decisão) — toda nova capacidade opcional entra por aqui. Sem isso, o archetype estagna e a dívida de manutenção cresce.

**Independent Test**: pode ser totalmente testado adicionando uma flag + snippet fictícios (`enable_dummy_test`), executando `mvn clean install` no archetype, e verificando que o arquivo `target/classes/META-INF/archetype-post-generate.groovy` contém o novo snippet na posição esperada.

**Acceptance Scenarios**:

1. **Given** o compositor nativo Groovy está em produção e o mantenedor adiciona um novo `snippet-xxx.groovy` + `variables-xxx.groovy` + `property-xxx.xml`, **When** ele atualiza a `List<String> SECTIONS` no compositor e roda `mvn clean install`, **Then** o arquivo final contém o novo snippet na posição declarada, e o build termina com BUILD SUCCESS.
2. **Given** o compositor está em produção, **When** o mantenedor reordena dois itens da `SECTIONS` para corrigir uma dependência lógica, **Then** o `mvn integration-test` continua passando 9/9 perfis (a ordem dos snippets é preservada na nova ordem).
3. **Given** o compositor está em produção, **When** o build é re-executado em sequência sem mudança nos arquivos, **Then** o arquivo gerado é idêntico ao anterior (regeneração idempotente — byte-equivalente).

---

### User Story 2 - Mantenedor executa build do archetype em ambiente sem TTY (Priority: P1)

O mantenedor (ou um agente de CI) precisa executar `mvn clean install` em um ambiente headless (CI Linux sem TTY, agente GitHub Actions, GitLab CI) sem que o build trave esperando entrada humana.

**Why this priority**: cumpre o Goal #1 declarado em `.specify/assessments/groovy-snippet-injection/problem.md` (eliminar dependência do pre-processamento em bash) e a Success Metric #5 (zero `read -n 1 -s` no pipeline). Sem isso, o archetype nunca chega a ser empacotado em CI automatizado — limitação operacional bloqueante.

**Independent Test**: pode ser testado executando `mvn clean install` com stdin redirecionado de `/dev/null` (Linux) ou `< NUL` (Windows), e verificando que o build completa sem travar.

**Acceptance Scenarios**:

1. **Given** o compositor nativo Groovy está em produção, **When** o mantenedor roda `mvn clean install` em um agente Linux headless com `</dev/null` no stdin, **Then** o build completa BUILD SUCCESS sem pausar esperando input.
2. **Given** o compositor está em produção, **When** o mantenedor roda `mvn clean install` em um macOS nativo (BSD sed, sem Git Bash), **Then** o build completa BUILD SUCCESS — não há dependência de GNU sed.

---

### User Story 3 - Mantenedor verifica equivalência byte-a-byte com a saída do bash legado (Priority: P2)

Antes de remover o script bash legado (`bash_script/create_archetype_post_generate.sh`), o mantenedor precisa de um mecanismo de verificação automática que prove que o compositor nativo produz exatamente o mesmo arquivo que o bash produzia. Isso dá confiança de que a migração não introduziu regressões invisíveis.

**Why this priority**: Success Metric #3 do `problem.md` — equivalência byte-a-byte é o principal critério objetivo. Sem ela, a única forma de validar a migração é rodar `mvn integration-test` e torcer.

**Independent Test**: pode ser testado comparando o `target/classes/META-INF/archetype-post-generate.groovy` gerado pelo compositor com um golden file capturado do bash antes da migração. `diff` deve retornar vazio.

**Acceptance Scenarios**:

1. **Given** o golden file `reference-archetype-post-generate.groovy` foi capturado antes da migração, **When** o compositor nativo gera o arquivo atual, **Then** `diff` entre os dois retorna vazio (sem saída, exit code 0).
2. **Given** o compositor está em produção, **When** o build é re-executado em sequência, **Then** o hash SHA-256 do arquivo gerado é idêntico ao da execução anterior (regeneração idempotente).

---

### Edge Cases

- **Snippet vazio**: o que acontece se um arquivo `.groovy` em `archetype_post_generate_snippets/` for criado vazio? — O compositor deve tratá-lo como string vazia e prosseguir (não falhar).
- **Encoding misto ou BOM**: o que acontece se um snippet contém BOM UTF-8 ou mistura CRLF/LF? — O compositor deve normalizar (ou rejeitar com mensagem clara) para evitar inconsistências byte-a-byte.
- **Comentários com `//import`**: o regex de remoção de imports (`^[[:space:]]*(import|//[[:space:]]*import)`) deve tratar comentários que mencionam a palavra `import` como **não-imports** e mantê-los. Já validado no regex do bash legado.
- **Arquivo `.groovy` faltando**: o que acontece se `archetype-header.groovy` ou outro arquivo obrigatório for removido? — O compositor deve falhar com mensagem clara listando o arquivo faltando, em vez de gerar um arquivo parcial silenciosamente.
- **Re-ordenação da `SECTIONS`**: o mantenedor pode mudar a ordem dos itens livremente (a topologia interna de cada bloco é fixa, mas a ordem dos blocos é configurável). O arquivo gerado reflete a nova ordem.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O archetype-v2-eda MUST compor `META-INF/archetype-post-generate.groovy` em build-time a partir dos snippets em `META-INF/archetype_post_generate_body/`, `META-INF/archetype_post_generate_snippets/` e `META-INF/archetype_post_generate_variables/`, **sem invocar nenhum script bash**.
- **FR-002**: A composição MUST preservar a ordem atual: header → global-vars → 5 path-vars (root, src-main, src-test, logback, security) → variables (alfabético) → snippets (alfabético) → footer → file-functions → folder-functions.
- **FR-003**: A composição MUST consolidar imports removendo `^import …` e `^//[[:space:]]*import` (com espaços opcionais) dos snippets, e prepondo o conteúdo de `archetype_post_generate_body/archetype-imports.groovy` no topo do arquivo final.
- **FR-004**: O arquivo gerado MUST ser **byte-equivalente** à saída atual do `bash_script/create_archetype_post_generate.sh` (verificável por `diff` retornando vazio e por hash SHA-256).
- **FR-005**: O pipeline MUST ser executável em ambiente sem TTY (CI Linux headless) — zero `read`, `pause` ou equivalente que bloqueie esperando input humano.
- **FR-006**: A configuração da ordem MUST estar em uma **única estrutura de dados literal legível** (uma `List<String>` no topo do script compositor), com um comentário curto explicando o papel de cada item.
- **FR-007**: Cada snippet em `archetype_post_generate_snippets/` MUST começar com um bloco de comentário indicando qual flag (`enable_*` em `archetype_metadata_properties/`) o acopla, para suportar extensibilidade via flags/IA.
- **FR-008**: O compositor MUST estar isolado em um único arquivo `.groovy` dedicado (ex.: `src/main/groovy/eda/postgenerate/SnippetComposer.groovy`) com cabeçalho explicativo da estratégia e Javadoc nas funções públicas.
- **FR-009**: A invocação do compositor MUST ocorrer na fase `process-resources` (imediatamente após `xml-maven-plugin:transform` em `generate-resources`, antes de `package`), através de um plugin Groovy build-time.
- **FR-010**: O `archetype-post-generate.groovy` atualmente commitado em `src/main/resources/META-INF/` MUST ser removido do controle de versão (`.gitignore` ou equivalente) — passa a ser artefato gerado, não editado.
- **FR-011**: O `bash_script/create_archetype_post_generate.sh` MUST deixar de ser invocado pelo `script_core.sh` no fluxo `mvn clean install` do archetype. O arquivo pode permanecer em disco como referência histórica, mas não pode estar no caminho de invocação.

### Key Entities

- **Snippet**: arquivo `.groovy` individual em uma das 3 pastas de entrada (`body/`, `snippets/`, `variables/`). Atributo: nome do arquivo (deve seguir convenção de prefixo `snippet-*` ou `variables-*`, ou estar em `body/` para arquivos fixos). Identifica uma unidade lógica de código Groovy que será concatenada no arquivo final.
- **Section**: bloco nomeado no arquivo final (header, global-vars, path-vars-root, variables-list, snippets-list, footer, file-functions, folder-functions). Atributo: nome, posição na `SECTIONS`. Define a ordem em que cada grupo de snippets aparece.
- **Composer Manifest**: `List<String>` ordenada com os nomes das seções, definida no topo do `SnippetComposer.groovy`. Atributo: lista de strings, comentário explicativo por entrada. É a "fonte da verdade" da ordem de montagem.
- **Imported-Block**: conteúdo de `archetype_post_generate_body/archetype-imports.groovy`, prependido no topo do arquivo final. Atributo: conteúdo textual (linhas de `import …`).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: `mvn clean install` retorna BUILD SUCCESS nos 9 perfis IT: `basic`, `full`, `minimal`, `binding-default-only`, `binding-list-only`, `binding-map-only`, `binding-mutable-only`, `binding-validated-only`, `json-contract`. (Baseline: 9/9 PASSOU em 2026-09-28 com a stack atual.)
- **SC-002**: `mvn integration-test` retorna BUILD SUCCESS nos 9 perfis IT. (Baseline: 9/9 PASSOU em 2026-09-28.)
- **SC-003**: `diff <(composer_output) <(current_bash_output)` retorna vazio (exit code 0, sem saída) — equivalência byte-a-byte entre o arquivo gerado pelo compositor nativo e a saída do bash legado. Capturável mecanicamente.
- **SC-004**: `grep -rE 'read -n 1 -s|read -p' pom.xml src/` retorna zero matches após a migração — nenhum ponto de bloqueio por TTY permanece no pipeline.
- **SC-005**: Adicionar uma nova flag + snippet leva **menos de 5 minutos** entre editar `SECTIONS` no compositor e ver o snippet no arquivo gerado (ciclo: editar `SECTIONS` → `mvn clean install` → inspecionar `target/classes/META-INF/archetype-post-generate.groovy`).
- **SC-006**: O build prossegue sem TTY em Linux headless (CI agent) sem erro de I/O — `mvn clean install < /dev/null` retorna BUILD SUCCESS.
- **SC-007**: O tempo de execução do passo de composição é ≤ tempo atual do bash (~0.3s × 9 perfis ≈ 2.7s no ciclo IT). Mensurável via `mvn clean install` antes e depois.

## Assumptions

- O usuário (mantenedor único do projeto, conforme `problem.md`) opera em Windows com Git Bash na situação atual, mas o pipeline MUST funcionar também em Linux/macOS sem Git Bash.
- O Groovy 4.0.28 está disponível transitivamente via `groovy-bom` do `maven-archetype-plugin:3.4.1` (confirmado em `research.md`).
- O `org.codehaus.gmaven:groovy-maven-plugin:2.1.1` (estagnado desde 2019-08-04) é a única opção estável publicamente disponível para invocação Groovy build-time em Maven; alternativas foram descartadas em `research.md` linhas 65–66.
- A equivalência byte-a-byte é o critério de aceitação primário — divergência mínima (ex.: espaços em branco) é tratada como regressão até prova em contrário.
- A constitution do projeto (Princípios I, II, VI) é vinculante; nenhuma mudança pode violá-la sem amendment formal. Esta feature está alinhada com a constitution como está hoje.
- A decisão de manter o `bash_script/create_archetype_post_generate.sh` em disco como referência (mesmo após deixar de ser invocado) é temporária e pode ser revisada em fase posterior, conforme diretriz do usuário.
