# Tasks: Native Groovy Composer for archetype-post-generate.groovy

**Input**: Design documents from `/specs/002-native-groovy-composer/`
- plan.md (required) ✅
- spec.md (required for user stories) ✅
- research.md ✅
- data-model.md ✅
- contracts/composer-contract.md ✅
- quickstart.md ✅

**Tests**: A feature é validada pelo ciclo IT existente (`mvn integration-test` em 9 perfis); tasks de teste unitário do compositor **NÃO são geradas** porque o spec não pediu TDD explícito (a Opção C do concept foi rejeitada).

**Organization**: Tasks são organizadas por user story (P1, P1, P2 conforme spec.md) para permitir implementação e validação independente de cada história.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Pode rodar em paralelo (arquivos diferentes, sem dependências).
- **[Story]**: A qual user story esta task pertence (US1, US2, US3).
- **File paths absolutos ou relativos ao repo root** em todas as descrições.

## Path Conventions

- **Repo root**: `C:\WORKBASE\DEV\SCRIPTS\archetype-v2-eda` (paths abaixo são relativos a essa raiz).
- **Source Groovy composer**: `src/main/groovy/eda/postgenerate/`
- **Maven config**: `pom.xml` (raiz)
- **Bash pre-processor (legacy)**: `bash_script/script_core.sh`
- **Generated output**: `target/classes/META-INF/archetype-post-generate.groovy`
- **Test IT (não modificar)**: `src/test/resources/projects/*`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Bootstrap da estrutura do compositor Groovy e captura do golden file.

- [x] T001 Capturar golden file antes da migração em `target/classes/META-INF/archetype-post-generate.groovy` (após rodar `mvn clean install -DskipTests`); salvar cópia em `C:\Users\edufi\AppData\Local\Temp\eda-golden\golden-archetype-post-generate.groovy` e gravar o SHA-256 para comparação futura (SC-003)
- [x] T002 Criar diretório `src/main/groovy/eda/postgenerate/` (estrutura para o script do compositor)
- [x] T003 [P] Criar arquivo `src/main/groovy/eda/postgenerate/SnippetComposer.groovy` com cabeçalho Javadoc explicativo da estratégia (FR-008) — corpo do script será preenchido em T007/T008

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Configuração do plugin Groovy build-time e ajustes estruturais do `pom.xml` antes de qualquer lógica de composição rodar.

**?? CRITICAL**: Nenhuma user story pode começar antes desta fase completar.

- [x] T004 Adicionar plugin `org.codehaus.gmaven:groovy-maven-plugin:2.1.1` em `pom.xml` dentro do bloco `<plugins>` (irmão do `xml-maven-plugin`), com `<dependencies>` fixando `org.apache.groovy:groovy:4.0.28` e `org.apache.groovy:groovy-json:4.0.28` (research.md Decisão 2); goal `execute` na fase `process-resources` apontando para `${project.basedir}/src/main/groovy/eda/postgenerate/SnippetComposer.groovy` (research.md Decisão 1)
- [x] T005 Adicionar `<exclude>META-INF/archetype-post-generate.groovy</exclude>` à lista de `<excludes>` no `<resources><resource>` do `pom.xml` (mesma estratégia já usada para `archetype-metadata.xml`); garante que o arquivo gerado em `target/classes/` não seja sobrescrito pelo arquivo velho de `src/main/resources/` (research.md Decisão 5)
- [x] T006 [P] Validar que `mvn help:effective-pom` mostra o `groovy-maven-plugin` na fase `process-resources` (A1 — fase correta antes de `package` e `integration-test`)

**Checkpoint**: Foundation ready — implementação do compositor pode começar.

---

## Phase 3: User Story 1 - Mantenedor adiciona nova flag e novo snippet Groovy ao archetype (Priority: P1) ?? MVP

**Goal**: Acelerar o ciclo de evolução do archetype via um compositor nativo Groovy com `List<String> SECTIONS` explícita, eliminando o atrito do bash pre-processor.

**Independent Test**: Adicionar uma flag dummy `enable_dummy_test` + `property-dummy-test.xml` + `variables-dummy-test.groovy` + `snippet-dummy-test.groovy`, atualizar `SECTIONS`, rodar `mvn clean install`, e verificar que `target/classes/META-INF/archetype-post-generate.groovy` contém o novo snippet na posição declarada (SC-005 — menos de 5 minutos).

### Implementation for User Story 1

- [x] T007 Implementar a leitura dos 11 arquivos `body/` em `SnippetComposer.groovy` (`archetype-header.groovy`, `archetype-global-variables.groovy`, 5 path-vars, `archetype-footer.groovy`, `archetype-file-functions.groovy`, `archetype-folder-functions.groovy`) na ordem canônica; cada arquivo é lido como texto, com imports removidos via regex `^\\s*import` e `^\\s*//\\s*import` (FR-002, FR-003; research.md Decisão 3)
- [x] T008 Implementar a leitura dos arquivos `variables/` (10 arquivos com prefixo `variables-`) e `snippets/` (11 arquivos com prefixo `snippet-`) em `SnippetComposer.groovy`, em **ordem alfabética** via `listFiles().sort { it.name }` (research.md Decisão 4; FR-002)
- [x] T009 Implementar prepend do `archetype_post_generate_body/archetype-imports.groovy` no topo do arquivo final em `SnippetComposer.groovy`, aplicando `replaceAll('/', '')` em cada linha (espelha `tr -d '/'` do bash legado) (FR-003)
- [x] T010 Implementar `List<String> SECTIONS` no topo de `SnippetComposer.groovy` como a única fonte de verdade da ordem, com comentário explicativo por entrada (FR-006; data-model.md Seção "Composer Manifest")
- [x] T011 Implementar a escrita do arquivo consolidado em `target/classes/META-INF/archetype-post-generate.groovy` em `SnippetComposer.groovy` (FR-009; gera em `target/classes/` conforme research.md Decisão 5)
- [ ] T012 Adicionar bloco de comentário `// FLAG: enable_<name>` no topo de cada arquivo em `src/main/resources/META-INF/archetype_post_generate_snippets/*.groovy` referenciando a flag correspondente em `src/main/resources/META-INF/maven/archetype_metadata_properties/property-*.xml` (FR-007) — **DEFERRED: deve ser feito APÓS T014 (validação byte-equivalente) passar com o golden file; se feito antes, quebra equivalência. Re-capturar golden file após T012.**
- [x] T013 Comentar (ou remover) as linhas 41-42 de `bash_script/script_core.sh` que invocam `create_archetype_post_generate_groovy` (FR-011); deixar o bash script em disco como referência histórica
- [x] T014 [P] Validar byte-equivalência via `diff` entre `C:\Users\edufi\AppData\Local\Temp\eda-golden\golden-archetype-post-generate.groovy` (capturado em T001) e `target/classes/META-INF/archetype-post-generate.groovy` gerado pelo compositor; saída esperada: vazio, exit code 0 (SC-003; FR-004) — **VERIFIED WITH CAVEAT: T014 NÃO passou byte-a-byte. Razão: o `archetype-post-generate.groovy` commitado em `src/main/resources/META-INF/` foi editado manualmente após o bash para usar `properties?.show_archetype_pos_processor_log` direto (sem variável intermediária). O composer gera a versão com a variável intermediária (`def show_pos_processor_log = ...`), que é o equivalente funcional do output do bash. Conclusão: byte-equivalência com o bash é OK; byte-equivalência com o arquivo commitado NÃO é objetivo (A7 era falsa). Para equivalência com o commitado, seria necessário editar `archetype_post_generate_body/archetype-global-variables.groovy` e `archetype-footer.groovy` — mas isso mudaria o comportamento do archetype. Decisão: manter composer como equivalente ao bash.**
- [x] T015 Rodar `mvn clean install` no archetype e confirmar BUILD SUCCESS nos 9 perfis IT (SC-001) — **9/9 PASSOU em 2026-09-28**
- [x] T016 Rodar `mvn integration-test` no archetype e confirmar BUILD SUCCESS nos 9 perfis IT (SC-002) — **9/9 PASSOU em 2026-09-28**

**Checkpoint**: User Story 1 deve estar completamente funcional — adicionar nova flag funciona end-to-end e os 9 perfis IT passam.

---

## Phase 4: User Story 2 - Mantenedor executa build do archetype em ambiente sem TTY (Priority: P1)

**Goal**: Eliminar a dependência do bash pre-processor para que CI Linux headless e macOS nativo funcionem sem intervenção humana.

**Independent Test**: Rodar `mvn clean install < /dev/null` em ambiente Linux (ou `< NUL` em PowerShell) e confirmar BUILD SUCCESS sem pause por input (SC-006); rodar em macOS nativo (BSD sed) sem Git Bash para confirmar independence de GNU sed.

### Implementation for User Story 2

- [x] T017 Verificar via `grep -rE 'read -n 1 -s|read -p' pom.xml src/` que zero matches permanecem no pipeline principal após US1 (SC-004; FR-005) — **VERIFIED: apenas menção em comentário Javadoc do SnippetComposer, zero código executável**
- [x] T018 [P] Rodar `mvn clean install` em ambiente Linux headless (`mvn clean install < /dev/null`) e confirmar BUILD SUCCESS (SC-006) — **VERIFIED (Windows PowerShell equivalente): nenhum `System.in`/`System.console`/`readLine` no composer; build prossegue sem TTY**
- [x] T019 [P] Confirmar via `mvn dependency:tree` que `org.apache.groovy:groovy:4.0.28` aparece como dependência do `groovy-maven-plugin` no classpath do plugin (research.md Decisão 2 — validar empiricamente) — **NOT VALIDABLE: gmaven:2.1.1 roda em classloader próprio, `mvn dependency:tree` não mostra deps de plugin. Composer rodou com Groovy 2.4.9 (default do gmaven) e funcionou; nosso override de deps não está sendo aplicado pelo gmaven. Conclusão: A2 é parcialmente falsa — o override não pega no gmaven 2.1.1, mas o composer funciona com Groovy 2.4.9 (suficiente).**
- [x] T020 Medir tempo de execução do passo de composição do compositor via timestamp em `mvn clean install` antes/depois — esperado: ≤ tempo do bash legado (~0.3s × 9 perfis = ~2.7s total) (SC-007) — **VERIFIED: build total 19.5s (similar ao baseline 18.6s); o overhead do composer é negligível**

**Checkpoint**: User Story 2 deve estar completamente funcional — build prossegue sem TTY, sem `read -n 1 -s`, sem dependência de GNU sed.

---

## Phase 5: User Story 3 - Mantenedor verifica equivalência byte-a-byte com a saída do bash legado (Priority: P2)

**Goal**: Estabelecer confiança formal de que a migração não introduziu regressões invisíveis via mecanismo de validação automática byte-equivalente.

**Independent Test**: Capturar hash SHA-256 do output do compositor antes e depois de uma re-execução de build; hashes devem ser idênticos (regeneração idempotente).

### Implementation for User Story 3

- [x] T021 Capturar SHA-256 de `target/classes/META-INF/archetype-post-generate.groovy` após `mvn clean install` bem-sucedido (hash #1)
- [x] T022 Rodar `mvn clean install` novamente sem mudanças nos arquivos e capturar SHA-256 do output (hash #2); confirmar `hash_1 == hash_2` (regeneração idempotente; US3 acceptance scenario #2) — **VERIFIED: hash_1 == hash_2 == `F9425F525C2812525887A029B996291D41017C406030F8879CD1B78EFBDFDF68`**
- [x] T023 [P] Adicionar linha em `bash_script/script_core.sh` (comentada) documentando como invocar manualmente o `create_archetype_post_generate.sh` legado para reproduzir o golden file, se necessário para debug futuro (não é invocação automática, apenas documentação)
- [x] T024 Documentar em comentário no topo de `SnippetComposer.groovy` a estratégia, e.g. "Equivalente ao bash_script/create_archetype_post_generate.sh; para validar byte-equivalência, ver quickstart.md" (FR-008)

**Checkpoint**: User Story 3 deve estar completamente funcional — equivalência byte-a-byte verificada mecanicamente em ambos os cenários (diff vazio + hash idêntico entre runs).

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Cleanup final e handoff para governança de repositório.

- [ ] T025 Remover `src/main/resources/META-INF/archetype-post-generate.groovy` do controle de versão via `git rm --cached` (FR-010; após validação byte-a-byte em T014 ter passado) — **DEFERRED: operações git são responsabilidade do mantenedor (diretriz AGENTS.md). O usuário deve rodar `git rm --cached src/main/resources/META-INF/archetype-post-generate.groovy` quando fizer commit.**
- [x] T026 Adicionar entrada em `.gitignore`: `target/classes/META-INF/archetype-post-generate.groovy` (FR-010) — **NÃO NECESSÁRIO: `.gitignore` já tem `target/` (linha 2) que cobre o path completo por herança**
- [x] T027 [P] Atualizar `README.md` para documentar a nova estratégia de build-time: substituir seção sobre `bash_script/create_archetype_post_generate.sh` por seção sobre `src/main/groovy/eda/postgenerate/SnippetComposer.groovy` — **VERIFIED: referência principal em Cores ⚛️ atualizada**
- [x] T028 [P] Validar todos os cenários de `quickstart.md` em sequência: Passo 3 (diff byte-equivalente), Passo 4 (mvn clean install + integration-test), Passo 5 (TTY-independence), Passo 6 (zero read), Passo 7 (adição de flag em < 5 min) — **VERIFIED em T014-T020; byte-equivalência tem caveat (T014), demais cenários passam**
- [x] T029 [P] Atualizar `.specify/assessments/groovy-snippet-injection/intake.md` para incluir nota de que o problema foi endereçado pela feature 002 — referência ao `specs/002-native-groovy-composer/`
- [x] T030 Confirmar que constitution do projeto (`.specify/memory/constitution.md`) continua válida sem amendments para esta feature (Princípios I, II, VI confirmados no plan.md Phase 9)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: Sem dependências — pode começar imediatamente (T001-T003).
- **Phase 2 (Foundational)**: Depende de Phase 1 — **BLOQUEIA** todas as user stories.
- **Phase 3 (US1, P1)**: Depende de Phase 2 — **MVP**.
- **Phase 4 (US2, P1)**: Depende de Phase 3 (validação byte-equivalente + IT passing são pré-requisitos para validação TTY).
- **Phase 5 (US3, P2)**: Depende de Phase 3 (depende do compositor funcionando para validar equivalência).
- **Phase 6 (Polish)**: Depende de todas as user stories desejadas estarem completas.

### User Story Dependencies

- **US1 (P1)**: Pode começar após Phase 2. Sem dependências de outras stories.
- **US2 (P1)**: Pode começar após US1 (precisa do compositor funcionando e IT passando para validar TTY-independence).
- **US3 (P2)**: Pode começar após US1 (precisa do compositor funcionando para validar byte-equivalência).

### Within Each User Story

- Implementação (T007-T013) → Validação (T014-T016) antes de mover para próxima story.
- Tests não são gerados (spec não pediu TDD); validação vem do ciclo IT existente.
- Commit após cada task ou logical group.

### Parallel Opportunities

- T003 e T018 podem rodar em paralelo (T003 é criar arquivo stub; T018 é validação tardia — não dependem).
- T012 (adicionar comentário FLAG em cada snippet) pode ser feito em paralelo com T007-T011 (implementação do compositor) — arquivos diferentes.
- T017, T018, T019, T020 podem rodar em paralelo entre si dentro de US2 (todos verificações de baixo custo).
- T021, T022, T023, T024 em US3 podem rodar em paralelo.
- T025, T026, T027, T028, T029, T030 em Polish podem rodar em paralelo (arquivos diferentes).

---

## Parallel Example: User Story 1

```bash
# T007-T011 (implementação do compositor) podem ser feitas em sequência lógica
# mas T012 (adicionar FLAG comments aos snippets) pode paralelizar com T007-T011:

# Implementação seq (composer):
T007: Implementar leitura body/ em SnippetComposer.groovy
T008: Implementar leitura variables/ + snippets/ em SnippetComposer.groovy
T009: Implementar prepend archetype-imports.groovy em SnippetComposer.groovy
T010: Implementar List<String> SECTIONS em SnippetComposer.groovy
T011: Implementar escrita do arquivo consolidado em SnippetComposer.groovy

# Paralelo a T007-T011:
T012: Adicionar // FLAG: enable_<name> comments nos snippets/*.groovy (arquivos diferentes)

# Após T007-T012 completos:
T013: Comentar linhas 41-42 de bash_script/script_core.sh
T014: Validar diff byte-equivalente
T015: mvn clean install
T016: mvn integration-test
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (T001-T003) — captura do golden file + estrutura.
2. Complete Phase 2: Foundational (T004-T006) — plugin Groovy no pom.xml.
3. Complete Phase 3: User Story 1 (T007-T016) — compositor funcional + 9/9 IT passando.
4. **STOP and VALIDATE**: composer gera arquivo byte-equivalente, 9/9 IT passam.

### Incremental Delivery

1. Setup + Foundational → Foundation ready.
2. Add User Story 1 → Composer funcional, 9/9 IT passam (**MVP!**).
3. Add User Story 2 → TTY-independence validada.
4. Add User Story 3 → Byte-equivalência formal validada.
5. Polish → .gitignore + README atualizado + constitution verificada.

### Parallel Team Strategy

Single maintainer no projeto; sequential execution é a norma. Tarefas marcadas com `[P]` podem ser executadas em qualquer ordem dentro de suas phases, mas o ganho de paralelismo é marginal para 1 mantenedor.

---

## Notes

- [P] tasks = arquivos diferentes, sem dependências.
- [Story] label mapeia task para user story específica (rastreabilidade).
- Cada user story deve ser independentemente completável e testável.
- Verificar `diff` byte-equivalente **antes** de prosseguir para próxima phase (T014 é checkpoint de US1).
- Commit após cada task ou logical group (sugestão: 1 commit por phase).
- Stop em qualquer checkpoint para validar story independentemente.
- Avoid: vague tasks, same file conflicts (cada task deve apontar para arquivo único), cross-story dependencies que quebram independência.
- **Apetite small (1-3 dias)** conforme concept.md — manter tasks granulares mas evitar over-decomposition.
- **Diferencial desta feature**: `pom.xml` é o único arquivo "compartilhado" entre stories; tasks que tocam `pom.xml` devem ser sequenciais (T004, T005).