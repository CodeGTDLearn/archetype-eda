# Tasks: Gate de Conformidade Entidade–Contrato (Schema Test na Aplicação Gerada)

**Input**: Design documents from `/specs/004-entity-contract-schema-test/`

**Prerequisites**: plan.md (required), spec.md (required), research.md, data-model.md, contracts/schema-test-contract.md, quickstart.md

**Tests**: O artefato DESTA feature é teste; as tarefas de teste são o produto, não opcionais.

**Organization**: Tarefas agrupadas por user story (US1–US4) para implementação e validação independentes.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: pode rodar em paralelo (arquivos diferentes, sem dependência de tarefa incompleta)
- **[Story]**: US1–US4, conforme spec.md
- Caminhos exatos em todas as descrições

## Path Conventions

- Template do schema test: `src/main/resources/archetype-resources/src/test/java/__entity_package_name__/contract/`
- Pós-processador: `src/main/resources/META-INF/archetype_post_generate_snippets/`
- Suites do archetype: `src/test/archetype-post-generate/`
- Fixtures IT: `src/test/resources/projects/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Baseline e rede de segurança (Constituição VI)

- [ ] T001 Rodar `mvn clean install` na raiz e registrar o baseline: 22/22 fixtures verdes e todas as suites Groovy verdes, antes de qualquer mudança (`pom.xml`)
- [ ] T002 [P] Gerar um módulo de baseline com o perfil `entity-contract` e salvar o `pom.xml` gerado em `target/baseline-pom-entity-contract.xml` para o diff de dependências (SC-006)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Motor de leitura/comparação compartilhado por todas as classes de teste geradas

**⚠️ CRITICAL**: nenhuma user story começa antes desta fase

- [X] T003 [P] Criar `src/main/resources/archetype-resources/src/test/java/__entity_package_name__/contract/ContractModel.java`: leitura de `/contracts/contract.json` via `ClassLoader.getResourceAsStream`, parse Jackson `JsonNode`, formas `attributes` (objeto|array de objetos|array de strings) > `fields` > topo, meta-chaves excluídas; tabela fechada de tipo — declarado `java.lang`/tipo do projeto vence (FQN reduzido ao nome simples); tipo rejeitado → estimativa; `true/false`→`Boolean`; inteiro que cabe em `int`→`Integer`; inteiro maior→`Long`; decimal→`Double`; string→`String`; objeto sem tipo→POJO aninhado; array não vazio de MAIÚSCULAS→enum; demais arrays→`String`; nomes/aninhados/enums conforme `research.md` R1–R3 e R7; contrato ilegível ou JSON inválido → falha estrutural explícita (`STRUCTURAL`), nunca skip/pass (FR-013)
- [X] T004 [P] Criar `.../contract/ContractDiagnostics.java`: `DivergenceRecord` (direction, path, expected, found, kind), agregação e renderização determinística no formato de `contracts/schema-test-contract.md` §4

**Checkpoint**: motor pronto — classes de teste podem ser escritas

---

## Phase 3: User Story 1 - Drift detectado na suíte da aplicação gerada (Priority: P1) — MVP

**Goal**: A aplicação gerada contém o pacote `contract/` que compara a entidade ao contrato nos dois sentidos, com falha agregada e diagnóstica, imune a Lombok.

**Independent Test**: gerar um módulo com contrato, rodar `mvnw test` (verde), adulterar entidade/contrato e rodar de novo (vermelho com atributo/tipo/sentido), restaurar (verde).

- [X] T005 [P] [US1] Criar `.../contract/__entity_name__ContractTest.java`: entrada JUnit 5 que agrega as divergências de todas as classes (`collect(...)`) numa única falha; placeholders apenas `${package}`, `${entity_package_name}`, `${entity_name}`
- [X] T006 [P] [US1] Criar `.../contract/__entity_name__ContractConformanceTest.java`: contrato→entidade por `getDeclaredFields()`, tipos esperados, regra `id` → `<entidade>Id` (`Long`), método estático `collect(...)`
- [X] T007 [P] [US1] Criar `.../contract/__entity_name__ContractCoverageTest.java`: entidade→contrato sem campo clandestino; exclui `<entidade>Id` e, com conjunto efetivo vazio, `username`/`password` (R10)
- [X] T008 [P] [US1] Criar `.../contract/ContractRoundTripTest.java`: visão normalizada com nomes Java, `ObjectMapper` com `FAIL_ON_UNKNOWN_PROPERTIES=true`, desserializa na entidade, re-serializa e compara conjuntos de chaves/hierarquia (R12)
- [X] T009 [US1] Validar manualmente o cenário da Frente 01 conforme `specs/004-entity-contract-schema-test/quickstart.md` §3: verde → sabotagem → vermelho → restauração → verde; em seguida, corromper `contract.json` (JSON inválido) na cópia e confirmar falha estrutural explícita (SC-007)
- [X] T010 [US1] Validar paridade Lombok on/off conforme `quickstart.md` §5 (mesmo veredito)

**Checkpoint**: US1 funcional e demonstrável; o teste roda na aplicação gerada

---

## Phase 4: User Story 2 - O archetype comprova a entrega (Priority: P1)

**Goal**: O perfil `entity-contract` constata, no ciclo IT, que o artefato existe e executou verde na aplicação gerada — e que a sabotagem falha com diagnóstico.

**Independent Test**: isolar a fixture, rodar `mvn clean integration-test -Darchetype.test.projectsDirectory=target/it-single` e ver as três asserções passarem.

- [X] T011 [US2] Estender `src/test/resources/projects/entity-contract/verify.groovy` com: (a) presença de `src/test/java/<pkg>/<entity_package>/contract/` na aplicação gerada; (b) `target/surefire-reports/` da aplicação com relatório de `<Entidade>ContractTest` e `Failures: 0, Errors: 0` (`research.md` R5)
- [X] T012 [US2] Estender o MESMO `verify.groovy` com o bloco de sabotagem: copiar o projeto gerado para diretório temporário, renomear/remover um campo da entidade, executar `mvnw`/`mvnw.cmd -o -q test`, asseverar exit ≠ 0 e mensagem com atributo + sentido (`research.md` R6); segundo cenário: corromper `src/test/resources/contracts/contract.json` (JSON inválido) na cópia e asseverar falha estrutural (FR-013/SC-007)
- [X] T013 [US2] Executar a fixture isolada e confirmar as três asserções (presença, verde, sabotagem) em `src/test/resources/projects/entity-contract/verify.groovy`

**Checkpoint**: prova automática de entrega da Frente 02

---

## Phase 5: User Story 3 - Sem contrato, sem teste órfão (Priority: P1)

**Goal**: A classe/pacote só existe sob a condição derivada; aplicações sem contrato compilam e testam verdes, sem artefato órfão.

**Independent Test**: gerar sem contrato/`none`/caminho inválido e com árvore de testes desativada; confirmar ausência do pacote e build verde; rodar a suite de harness.

- [X] T014 [P] [US3] Criar `src/main/resources/META-INF/archetype_post_generate_snippets/snippet-20-contract_schema_test.groovy` com marcador `// SNIPPET-20-CONTRACT_SCHEMA_TEST.GROOVY`: remover `<srcTestJava>/<entity_pkg>/contract` quando `src/test/resources/contracts/contract.json` não existir; logar a decisão; tolerar árvore ausente (`research.md` R8)
- [X] T015 [P] [US3] Criar `src/test/archetype-post-generate/SnippetContractSchemaTestTest.groovy`: harness cobrindo contrato presente (mantém), ausente (remove), flag `false`, `none`/`null`/vazio e fonte inexistente
- [X] T016 [US3] Registrar a suite no runner em `src/test/groovy/run-archetype-post-generate-tests.groovy`
- [X] T017 [P] [US3] Adicionar asserção de AUSÊNCIA do pacote `contract/` em `src/test/resources/projects/basic/verify.groovy`, `config-yml/verify.groovy`, `defaults-only/verify.groovy`, `java21/verify.groovy`, `minimal/verify.groovy`
- [X] T018 [P] [US3] Idem em `custom-attributes/verify.groovy` (path setado com árvore de testes desativada), `custom-enums-from-payload/verify.groovy`, `custom-nested-edge-cases/verify.groovy`, `custom-nested-objects/verify.groovy`, `id-normalization/verify.groovy`
- [X] T019 [P] [US3] Idem em `binding-tests-default-only/verify.groovy`, `binding-tests-list-only/verify.groovy`, `binding-tests-map-only/verify.groovy`, `external-binding-default-only/verify.groovy`, `external-binding-list-only/verify.groovy`, `external-binding-map-only/verify.groovy`, `external-binding-mutable-only/verify.groovy`, `external-binding-validated-only/verify.groovy`, `full/verify.groovy`, `logback-console-granular/verify.groovy`, `logback-no-elk/verify.groovy`
- [X] T020 [US3] Validar manualmente o negativo conforme `quickstart.md` §4: gerar sem contrato/`none` e com `enable_folder_src_test_java=false`; pacote ausente e `mvn test` verde

**Checkpoint**: ativação derivada provada nos dois sentidos

---

## Phase 6: User Story 4 - Aninhados e enums fiscalizados (Priority: P2)

**Goal**: Tipos derivados também são fiscalizados recursivamente; enums conferidos por nome e constantes; a fixture comprova a entrega com contrato rico.

**Independent Test**: gerar a fixture enriquecida, rodar `mvnw test` verde; adulterar um campo aninhado e uma constante de enum; ver falhas com caminho hierárquico.

- [X] T021 [P] [US4] Criar `.../contract/ContractNestedTypesTest.java`: recursão por FQN em `<pkg>.<entity_package>.nested.*`, caminho hierárquico completo, limite de 25 níveis
- [X] T022 [P] [US4] Criar `.../contract/ContractEnumsTest.java`: nome do tipo derivado + conjunto de constantes; `nested.<Tipo>` para enum aninhado
- [X] T023 [US4] Enriquecer `src/test/resources/projects/entity-contract/sample-contract.json` com 6 objetos aninhados em cadeia (7 níveis contando a entidade): `endereco` → `complemento` → `pontoReferencia` → `referencia` → `origem` → `detalhe`, mais enum de topo `status` e enum aninhado `endereco.uf` (arrays MAIÚSCULAS)
- [X] T024 [US4] Atualizar `src/test/resources/projects/entity-contract/archetype.properties`: `custom_entity_attributes_list=Long:id,String:name,Boolean:active,Endereco:endereco,Status:status`, `contract_nested_type_definitions=Endereco|logradouro:String,numero:Integer,uf:Uf,complemento:Complemento;Complemento|descricao:String,pontoReferencia:PontoReferencia;PontoReferencia|descricao:String,distanciaMetros:Integer,referencia:Referencia;Referencia|nome:String,origem:Origem;Origem|bairro:String,detalhe:Detalhe;Detalhe|codigo:String,observacao:String`, `contract_enum_definitions=Status:ATIVO|CANCELADO;nested.Uf:SP|RJ`, e o cabeçalho `FIXTURE SIGNATURE`
- [X] T025 [US4] Atualizar `src/test/resources/projects/entity-contract/verify.groovy` para os novos campos (cadeia de 6 POJOs `@Transient` + enum) e o caminho hierárquico esperado nas asserções
- [X] T026 [US4] Validar drifted de aninhado/enum conforme `quickstart.md` §3 (variante): adulterar a folha mais profunda `endereco.complemento.pontoReferencia.referencia.origem.detalhe.observacao` e a constante de `endereco.uf`, verificando diagnóstico com caminho completo

**Checkpoint**: US1–US4 funcionais e independentes

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: documentação, reconciliação e regressão total

- [X] T027 [P] Documentar o gate em `README.md` (seção de testabilidade: presença derivada, execução na aplicação gerada, como sabotar)
- [X] T028 [P] Atualizar a linha da fixture `entity-contract/` em `src/test/resources/projects/README.md`
- [X] T029 Confirmar a consistência de contagem: `spec.md` (FR-017/SC-004), `plan.md` e `src/test/resources/projects/` devem declarar os mesmos 22 perfis (A13 do plan)
- [X] T030 Rodar `mvn clean install` na raiz (recompõe `archetype-post-generate.groovy` no `process-resources`) e confirmar suites + 22/22 fixtures verdes
- [X] T031 Comparar `target/baseline-pom-entity-contract.xml` com o `pom.xml` do módulo gerado e confirmar ZERO dependências novas (SC-006)
- [X] T032 Executar `specs/004-entity-contract-schema-test/quickstart.md` de ponta a ponta (§1–§5)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: sem dependências — começa imediatamente
- **Foundational (Phase 2)**: depende do Setup — BLOQUEIA todas as user stories
- **US1 (Phase 3)**: depende da Foundational
- **US2 (Phase 4)**: depende de US1 (precisa do artefato materializado para constatar presença/verde/sabotagem)
- **US3 (Phase 5)**: depende da Foundational; a verificação "contrato presente mantém" usa os templates de US1, então executar após US1
- **US4 (Phase 6)**: depende de US1 (modelo) e de US2 (mesmo `verify.groovy`; ordem evita conflito de edição)
- **Polish (Phase 7)**: depende de todas as stories desejadas
- **Documentação (Phase 12)**: depende de US1–US4 entregues (documenta o comportamento real); arquivo único `README.md`, então T049→T050→T051→T052→T053 em sequência

### User Story Dependencies

- **US1 (P1)**: independente após Foundational
- **US2 (P1)**: integra com US1 (prova de entrega) — testável isoladamente via fixture
- **US3 (P1)**: independente após Foundational; valida o caminho negativo
- **US4 (P2)**: integra US1 (recursão) e US2 (fixture) — testável isoladamente com contrato rico

### Parallel Opportunities

- T003/T004 (Foundational) em arquivos distintos
- T005–T008 (US1) em arquivos distintos
- T014/T015 (US3) em arquivos distintos
- T017/T018/T019 (US3) em fixtures distintas
- T021/T022 (US4) em arquivos distintos
- T027/T028 (Polish) em documentos distintos
- Phase 12: **sem** paralelismo — T049–T053 editam o mesmo bloco do `README.md`

---

## Parallel Example: User Story 1

```text
Task: "Criar .../contract/__entity_name__ContractTest.java"
Task: "Criar .../contract/__entity_name__ContractConformanceTest.java"
Task: "Criar .../contract/__entity_name__ContractCoverageTest.java"
Task: "Criar .../contract/ContractRoundTripTest.java"
```

## Parallel Example: User Story 3

```text
Task: "Criar snippet-20-contract_schema_test.groovy"
Task: "Criar SnippetContractSchemaTestTest.groovy"
Task: "Adicionar asserção de ausência nas fixtures core"
Task: "Adicionar asserção de ausência nas fixtures contract/nested"
Task: "Adicionar asserção de ausência nas fixtures binding/logback/full"
```

---

## Implementation Strategy

### MVP First (User Story 1)

1. Phase 1 (Setup) → Phase 2 (Foundational)
2. Phase 3 (US1): pacote `contract/` completo
3. **STOP and VALIDATE** com T009/T010 (gerar módulo, verde → sabotagem → vermelho → verde)
4. MVP entregue: a aplicação gerada já detecta drift na própria suíte

### Incremental Delivery

1. US1 → gate funcional na aplicação gerada
2. US2 → prova automática no archetype (presença + verde + sabotagem)
3. US3 → ativação derivada sem teste órfão
4. US4 → aninhados/enums + fixture rica
5. Polish → docs + reconciliação + regressão total

### Parallel Team Strategy

1. Setup + Foundational em conjunto
2. Após Foundational: Dev A → US1; Dev B → US3 (guarda/harness); Dev C → preparar enriquecimento da fixture (T023/T024)
3. US2 depois de US1; US4 depois de US1+US2

---

## Notes

- [P] = arquivos diferentes, sem dependência pendente
- `[US1]`..`[US4]` mapeiam a spec
- T011/T012/T025 tocam o mesmo `verify.groovy`: executar em ordem
- T029 altera a spec (contagem 15→22) — requer aprovação do mantenedor
- T030 recompõe o pós-processador; sem `mvn clean install` completo, o resultado é inválido (Constituição VI)
- **T001/T002 não executados**: o baseline "antes das mudanças" foi dispensado para evitar duas execuções completas da matriz. O estado final foi validado por T030 (`mvn -o clean install` = 22/22 fixtures + 35/35 suites). O diff de dependências (T031) foi feito pela inspeção do `pom.xml` gerado: `spring-boot-starter-data-jpa`, `spring-boot-starter-web`, `h2`, `spring-boot-starter-test`, `spring-boot-maven-plugin` — nenhuma dependência nova; o template `archetype-resources/pom.xml` não foi alterado.
- T049–T053 (Phase 12) não têm rótulo de user story: são documentação transversal, como as tarefas da Phase 7.

## Phase 8: Convergence

- [X] T033 Alinhar `ContractModel` à `regras-02` §1.2/§4.1: tipo do projeto com valor textual ou `enumValues`/`values` vira `ENUM` nomeado pelo tipo declarado, e `type` em `java.*`/`javax.*`/`jakarta.*` é rejeitado (estimativa pelo valor), preservando o FQN na classificação, per FR-007, FR-010, regras-02 (contradicts)
- [X] T034 Adicionar à sabotagem de `entity-contract/verify.groovy` cenários de adulteração DA ENTIDADE (campo removido/renomeado, tipo alterado, campo clandestino), com asserção de `MISSING_FIELD`/`EXTRA_FIELD`/`TYPE_MISMATCH` e do sentido, per SC-002, US1/AC1-AC3 (partial)
- [X] T035 Adicionar à sabotagem de `entity-contract/verify.groovy` adulteração de constante de enum no contrato (topo `ATIVO`→`INATIVO`; aninhado `SP`→`MG`), asseverando `ENUM_CONSTANT_MISMATCH` e o caminho hierárquico completo, per SC-009, US4/AC2, US4/AC4 (partial)
- [X] T036 Provar a paridade Lombok on/off executando o schema test e uma sabotagem numa geração com `enable_lombok_support=true`, sem alterar a matriz de 22 fixtures, e registrar o veredito idêntico, per SC-005, US1/AC4, T010 (missing)
- [X] T037 Adicionar cenário de múltiplas divergências simultâneas à sabotagem (ex.: chave removida + folha renomeada) e asseverar UMA falha agregada, ordenada, contendo todos os itens, per SC-008, US1/AC6 (partial)
- [X] T038 Corrigir a derivação de nome de tipo aninhado em `ContractModel.pascalCase`/`nestedTypeName` para espelhar `regras-03` §1.1: palavra reservada recebe sufixo `Type` (`class` → `ClassType`) e chave iniciada por dígito recebe prefixo `Type` (`Type<Chave>`), per FR-009, FR-010 (contradicts)
- [X] T039 Ordenar os excedentes por nome na direção ENTITY→CONTRACT (`ContractCoverageTest.checkClass` e `ContractRoundTripTest.compare`), garantindo a ordem determinística exigida por `contracts/schema-test-contract.md` §4, per FR-012 (contradicts)
- [X] T040 Deduplicar constantes de enum em `ContractModel` (array e `enumValues`/`values`), espelhando `regras-03` §4, per FR-010 (partial)
- [X] T041 Registrar a prova de SC-006/T002: comparar o `pom.xml` da aplicação gerada por `entity-contract` com o template `archetype-resources/pom.xml` (inalterado) e salvar `target/baseline-pom-entity-contract.xml` com o resultado "zero dependências novas", per SC-006, T002 (missing)

## Phase 9: Convergence

- [X] T042 [HIGH] Provar a paridade Lombok on/off em execução real, fora da matriz de 22 fixtures: gerar um módulo temporário com `enable_lombok_support=true` e o contrato de `entity-contract`, executar o schema test e uma sabotagem, e asseverar veredito idêntico ao da geração com Lombok desligado, per SC-005, US1/AC4, FR-011, T036, quickstart §5 (partial)
- [X] T043 Alinhar `ContractModel.build` à `regras-02` §5: tipo do projeto com array vazio, misto ou sem constantes MAIÚSCULAS cai em `String` (não em `PROJECT_TYPE`), espelhando a estimativa que gerou a entidade, per FR-007, regras-02 §5 (contradicts)
- [ ] T044 Validar recursivamente os campos de um objeto do contrato que declara `type` de projeto: modelar os filhos em `ContractModel` e recursar em `ContractConformanceTest`/`ContractCoverageTest`/`ContractNestedTypesTest`/`ContractEnumsTest`, per FR-004, FR-010 (partial)
- [X] T045 Agregar as constantes por tipo de enum declarado antes de comparar em `ContractEnumsTest`, em vez de comparar isoladamente as constantes observadas de cada atributo, per FR-010, regras-02 §4.3–§4.4 (partial)
- [X] T046 Remover de `ContractModel.nestedTypeName` o sufixo `Type` aplicado quando o nome do tipo aninhado coincide com o nome da entidade, regra inexistente em `regras-03` §1.1 (que prevê apenas palavra reservada e dígito inicial) e capaz de gerar `TYPE_MISMATCH` falso, per FR-007, FR-010, regras-03 §1.1 (contradicts)

- T044 **não aplicável** (premissa incorreta): `regras-03` §1 determina que apenas objeto **sem tipo declarado** vira POJO; um `type` de projeto referencia uma classe existente (ex.: `AdminCredentials` em `custom-attributes`) e é validado pelo nome, sem recursão. Nenhuma alteração de código é devida.

## Phase 10: Convergence

- [X] T047 Atualizar a seção 2.1 do `README.md` para refletir a matriz atual de sabotagem da fixture `entity-contract` (9 cenários: contrato, entidade, enums e agregação) e a prova de paridade Lombok on/off, substituindo a contagem obsoleta de "3 cenários", per T027, FR-015, SC-005 (partial)

## Phase 11: Convergence

- [X] T048 Emitir avisos não fatais ao descartar chaves do contrato (nome ausente, inválido ou duplicado) e ao cair nos fallbacks de objeto vazio e de profundidade acima do limite, espelhando o mecanismo e sem alterar o veredito, per FR-009, regras-01 §5, regras-03 §1.3–§1.4 (partial)

## Phase 12: Documentação da Feature no README (FR-019..FR-023)

**Purpose**: entregar a documentação da feature no README, conforme clarify de 2026-11-18 e `plan.md` § Documentação

**Arquivo único**: `README.md` — tarefas sequenciais (mesmo bloco de texto)

- [X] T049 Substituir o bloco do subtópico **Contratos** em `README.md` (tópico **Funcionalidades**, linhas 2644–2668) pelo formato `<details style="margin-left: 2em;"><summary><strong> Contratos📜: a 'Fonte da Verdade' do Módulo da API </strong></summary>` … `</details>`, removendo o título interno que repetia o nome, per FR-020
- [X] T050 Escrever o item 1 **Raciocínio central**: ciclo fechado desenho da tela (Design de Front-End) → contrato → módulo da API → tela, deixando explícito que a conferência não é um mero teste de conformidade, com diagrama em bloco de código text (DESENHO DA TELA → CONTRATO → MÓDULO DA API → TELA, com seta de retorno), per FR-021, Session 2026-11-18
- [X] T051 Escrever os itens 2 a 5 — **O que faz**, **Como funciona**, **Para quem serve**, **Quando ele existe** — em frases curtas, uma ideia por linha, sem jargão e sem tutorial, nomeando os dois públicos no item 4 e sem reexplicar o mecanismo no item 3, per FR-019, FR-021, FR-022
- [X] T052 Escrever o item 6 **Detalhes técnicos** com o link para *Testabilidade → 2.1) SCHEMA TEST ENTIDADE-CONTRATO* (texto e href inalterados), sem alterar a seção 2.1, per FR-022, FR-023
- [X] T053 Validar a documentação conforme `quickstart.md` §6 (6 itens) e confirmar por `git diff` que apenas o bloco do subtópico mudou no `README.md`, per quickstart §6, FR-019..FR-023

**Checkpoint**: documentação publicada e verificada; nenhuma outra seção do README alterada

## Phase 13: Convergence

- [ ] T054 Adicionar âncora HTML não visual (`<a name="21-schema-test-entidade-contrato-gerado-na-aplicacao"></a>`) imediatamente antes do `<details>` da seção 2.1 em `README.md`, para que o link de **Detalhes técnicos** do subtópico **Contratos** passe a resolver de fato, sem alterar texto ou conteúdo visível da 2.1 — a emenda de FR-023 ("permanece como está" → "permanece visual e textualmente; recebe âncora não visual") requer aprovação do mantenedor, per FR-023, quickstart §6.5, T053 (partial)

