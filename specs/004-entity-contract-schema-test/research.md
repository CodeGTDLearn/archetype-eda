# Research: Gate de Conformidade Entidade–Contrato (replan)

**Feature**: `004-entity-contract-schema-test` | **Date**: 2026-11-13

Investigação feita sobre o código atual, atendendo à lista de pesquisa do mantenedor.

## R1 — Fonte canônica da tabela de estimativa de tipos

- **Decision**: fonte canônica única = `src/main/resources/utils/03-derivacao-regras-de-formacao-do-contrato/regras-01..03` (empacotadas no JAR). Consolidação histórica em `specs/_archive/003-llm-flag-inference/{research,data-model}.md`. Regras operacionais em `utils/02-operacao-procedimento-do-agente/prompt-03`.
- **Rationale**: `src/main/pre-processamento-maven-flags-via-llm` **não existe** (verificado); as `utils/` são as regras que o agente de inferência consome e que o template espelha.
- **Alternatives**: apontar para o diretório inexistente (quebraria a rastreabilidade); reimplementar a tabela "de cabeça" (proibido pela spec FR-006).

Tabela consolidada (regras-02 §1–2, §5): `type` declarado `java.lang` (FQN reduzido) ou tipo do projeto vence; tipo rejeitado → estimativa; `true/false`→`Boolean`; inteiro que cabe em `int`→`Integer`; inteiro maior→`Long`; decimal→`Double`; string→`String`; objeto sem tipo→POJO aninhado; array não vazio de MAIÚSCULAS→enum; qualquer outro array→`String`.

## R2 — Arrays/coleções e o caso `id-normalization`

- **Decision**: a entidade **nunca** tem `List<T>` (regras-02 §5). Arrays de MAIÚSCULAS não vazios → enum; os demais → `String` com aviso. Id: a entidade sempre declara `private Long <entidade>Id;` e o template descarta qualquer grafia `id/Id/iD/ID` da lista de atributos (verificado em `__entity_name__.java` linhas 79–115).
- **Rationale**: espelhar o mecanismo é o invariante da feature; o perfil `id-normalization` prova a normalização de caixa (fixture com `Long:id,Integer:iD,String:name` → uma única PK).
- **Alternatives**: tratar arrays como listas (divergiria da geração); comparar `id` como campo comum (contraria FR-008).

## R3 — Formato real dos contratos com aninhados e enums

- **Decision**: calibrar o parser com as fixtures reais:
  - `custom-nested-objects`: `attributes` array; valor objeto com 2 níveis (`endereco.complemento.descricao`).
  - `custom-enums-from-payload`: forma plana; `status: ["ATIVO","CANCELADO"]` (enum de topo) e `endereco.uf: ["SP","RJ"]` (enum **dentro** de objeto aninhado → `nested.Uf`).
  - `custom-nested-edge-cases`: chave acentuada (`endereço` → `endereco`), palavra reservada (`class` → `ClassType`), array de minúsculas (`tags` → `String`), aninhado `pedidoAnterior`, e atributo `pedido` (objeto) cujo POJO se chama `Pedido` — mesmo nome simples da entidade, resolvido por FQN (`...nested.Pedido`).
- **Rationale**: esses são os casos que o teste gerado vai encontrar; o FQN dos aninhados elimina ambiguidade com a entidade.
- **Alternatives**: sintetizar contratos novos (não provaria os formatos reais).

## R4 — Cobertura do `fileset-16` sobre subdiretórios

- **Decision**: **não** criar fileset dedicado. `fileset-16` declara `<directory>src/test/java</directory>` + `<include>**/*.java</include>` e `filtered="true"` — subdiretórios (inclusive `contract/`) e filtro Velocity já cobertos.
- **Rationale**: verificado no próprio XML; `TemplateResourceContractTest` valida includes.
- **Alternatives**: fileset dedicado (duplicaria a seleção; `FilesetConsistencyTest` tenderia a acusar).

## R5 — Como o `verify.groovy` constata "teste executou verde na aplicação gerada"

- **Decision**: ler `target/surefire-reports/` do projeto gerado — o arquivo `.txt` do relatório traz `Tests run: N, Failures: 0, Errors: 0, Skipped: 0` e o nome da classe no próprio nome do arquivo. Asserção: existe relatório cujo nome contém a classe `<Entidade>ContractTest` e cujos contadores de falha/erro são zero.
- **Rationale**: artefato determinístico do Surefire 3.2.5 (versão já no `pluginManagement`), independente de idioma/layout de saída do Maven; não depende de parsing de console.
- **Alternatives**: parsear `build.log` (frágil a mudanças de formato); confiar só no exit code do build (prova que o build passou, não que o teste rodou).

## R6 — Automação da sabotagem dentro do ciclo IT

- **Decision**: etapa adicional no `verify.groovy` da fixture `entity-contract` (pós-build): copiar o projeto gerado para um diretório temporário, adulterar a entidade (renomear/remover campo) **ou** o contrato, executar o wrapper do próprio projeto (`mvnw`/`mvnw.cmd`, conforme o SO) com `-o -q test`, e asseverar: exit ≠ 0 **e** saída contendo o diagnóstico (atributo + sentido). Nenhuma fixture nova.
- **Rationale**: preserva a matriz IT (SC-004); o archetype não depende de suporte a "build esperado-falho" do maven-archetype-plugin 3.4.1 (não documentado); o teste é executado pela aplicação gerada, como manda a premissa. Precedente de processo aninhado: `ComposerFailurePathTest` (ProcessBuilder).
- **Alternatives**: fixture nova com falha esperada (suporte incerto; matriz +1); sabotar in-place e restaurar (mutaria o artefato inspecionável do IT).

## R7 — Formas de contrato e precedência (da spec/clarify)

- **Decision**: `attributes` (objeto | array de objetos | array de strings) > `fields` > campos de topo; meta-chaves excluídas (`entity_name`, `entityName`, `entity`, `package`, `description`, `attributes`, `fields`); sem catálogo de flags.
- **Rationale**: regras-01 §4 + decisões do clarify.
- **Alternatives**: forma canônica apenas (falso positivo em formas válidas).

## R8 — Gatilho da guarda de remoção

- **Decision**: a guarda remove o diretório `contract` quando `src/test/resources/contracts/contract.json` **não existir** no projeto gerado; o log registra a decisão. Verificação adicional da existência de `src/test/java`.
- **Rationale**: a existência do contrato é a conjunção exata de FR-001 e cobre o caso "flag válida, mas arquivo-fonte ausente" (que deixaria teste órfão se a guarda olhasse só a string da flag). `generator-03` roda antes (GENERATORS → SNIPPETS); `snippet-19` já apagou a árvore quando a flag é falsa (`deleteFolder` é no-op seguro).
- **Alternatives**: olhar apenas `entity_contract_file_path` (não cobre fonte ausente).

## R9 — Posição do snippet no compositor

- **Decision**: não editar `SECTIONS`; confiar na expansão dinâmica `GENERATORS` → `SNIPPETS` + ordem alfabética (`snippet-19` < `snippet-20`).
- **Rationale**: `SECTIONS` valida que entradas estáticas existem em `body/`; um snippet ali (a) falha o `ComposerTest` e (b) duplica a seção.
- **Alternatives**: alterar o compositor para aceitar snippets em SECTIONS (mudança de contrato fora do escopo; rejeitada).

## R10 — Contrato sem atributos vs scaffold `username/password`

- **Decision**: quando o conjunto efetivo de atributos do contrato é vazio, a direção ENTITY_TO_CONTRACT exclui exatamente `username` e `password` (o scaffold do ramo `enable_custom_entity_attributes=false` do template). Com ≥1 atributo, nada é excluído além do id técnico.
- **Rationale**: sem isso, todo contrato sem atributos nasceria vermelho por boilerplate gerado, contrariando o caso de borda da spec.
- **Alternatives**: embutir o valor da flag no template (mais acoplamento); ignorar todos os campos extras quando o contrato é vazio (abriria buraco para campo clandestino).

## R11 — Múltiplas classes e remoção atômica

- **Decision**: 6 classes de teste (entrada, conformidade, cobertura, aninhados, enums, round-trip) + 2 de apoio no pacote `contract`; a guarda remove o **diretório** inteiro. *(Superada em 2026-11-18: ver R15 — 7 fixos + 15 gerados.)*
- **Rationale**: responsabilidade única e legibilidade (instrução do mantenedor); remoção continua atômica.
- **Alternatives**: classe única (instrução anterior, substituída); helpers fora do pacote (violaria III).

## R12 — Round-trip estrito com Jackson

- **Decision**: normalizar o contrato para uma visão JSON com **nomes Java** (id do contrato → `<entidade>Id`), desserializar na entidade com `FAIL_ON_UNKNOWN_PROPERTIES=true`, re-serializar e comparar conjuntos de chaves e hierarquia. A tipagem fina e o diagnóstico continuam sendo responsabilidade da comparação por reflexão; o round-trip é um gate adicional para "nenhuma chave sem correspondente".
- **Rationale**: mensagens de `JsonMappingException` não dizem o sentido da divergência; a reflexão dá o diagnóstico, o round-trip dá a prova estrutural de ida e volta. Coerções numéricas do Jackson (ex.: `Integer`↔`Long`) não podem mascarar `TYPE_MISMATCH` da reflexão.
- **Alternatives**: só round-trip (diagnóstico pobre); só reflexão (perderia a prova de re-serialização).

## R13 — Asserção de ausência nos perfis sem contrato

- **Decision**: adicionar bloco de ausência nos `verify.groovy` de **todas as 21** fixtures que não persistem contrato (as 22 do diretório menos `entity-contract`): `basic`, `binding-tests-*` (3), `config-yml`, `custom-attributes` (path setado, mas `enable_folder_src_test_java=false`), `custom-enums-from-payload`, `custom-nested-edge-cases`, `custom-nested-objects`, `defaults-only`, `external-binding-*` (5), `full`, `id-normalization`, `java21`, `logback-*` (2), `minimal`.
- **Rationale**: FR-016 exige prova de ausência; os perfis já rodam `clean,verify`.
- **Alternatives**: asserção só em `basic`/`defaults-only`/`minimal` (cobertura incompleta).

## R14 — Documentação da feature no README

- **Decision**: subtópico **Contratos** dentro do tópico existente **Funcionalidades** (`README.md`, `<details><summary><strong> Funcionalidades 🛠️ </strong></summary>` na linha 2642), no mesmo padrão `<details>`/`<summary>` do restante do README. Título no `<summary>`: `Contratos📜: a 'Fonte da Verdade' do Módulo da API`; o ícone 📜 é decorativo e o nome canônico é **Contratos: a 'Fonte da Verdade' do Módulo da API**. Sem título interno repetindo o nome. Conteúdo em seis itens numerados: 1) Raciocínio central; 2) O que faz; 3) Como funciona; 4) Para quem serve; 5) Quando ele existe; 6) Detalhes técnicos. O item 1 abre com o ciclo fechado (desenho da tela → contrato → módulo da API → tela) e traz diagrama em bloco `text`, no padrão do "Grafo de dependências". Linguagem simples, frases curtas, **sem** tutorial passo a passo e **sem** reexplicar o mecanismo de contrato; link para *Testabilidade → 2.1) SCHEMA TEST ENTIDADE-CONTRATO*, que preserva título, âncora e propósito; os 4 bullets técnicos são atualizados (R22).
- **Rationale**: FR-019..FR-023, revisadas no clarify de 2026-11-18 (a decisão de 2026-11-17 sobre nome canônico e conteúdo fica superada). O README já usa `<details>` como padrão de leitura e o subtópico "Funcionalidades" é o encaixe natural; evitar duplicação mantém uma única fonte por assunto.
- **Alternatives**: mover o conteúdo de 2.1 para o subtópico (descartado — FR-023 manda manter); duplicar (duas versões divergentes); documentar o mecanismo de contrato inteiro (descartado — FR-022); manter título interno repetindo o nome (descartado — duplicação dentro do mesmo bloco).

## R15 - Divisão entre camada fixa e camada gerada

- **Decision**: 7 arquivos fixos (4 de teste + 3 de apoio) por Velocity e 15 arquivos de teste emitidos pelo `generator-04` no perfil de referência. `ContractRoundTripTest` é gerado.
- **Rationale**: o payload do round-trip depende dos tipos das flags; mantê-lo fixo exigiria reintroduzir a inferência de tipo que o refactor remove. Os 15 gerados cobrem exatamente o que varia com o contrato.
- **Alternatives**: 8 fixos (exigiria inferência); gerar os 18 (perderia a legibilidade dos templates Velocity); 18 fixos (impossível sem hardcode).

## R16 - `ContractJson` em ≤60 linhas de código

- **Decision**: Java 17 (streams e text blocks) e apoio em 3 classes: `EntityFields`, `ContractJson`, `ContractKeys`.
- **Rationale**: FR-026/FR-027; o antigo `ContractModel` (491 linhas) concentrava cinco responsabilidades. A contagem exclui `package`, `import`, linhas em branco e comentários.
- **Alternatives**: quatro classes de apoio (contraria a leitura de FR-027); elevar o limite (contraria a spec).

## R17 - `@BeforeAll` sem estado estático mutável

- **Decision**: `@TestInstance(PER_CLASS)` + `@BeforeAll` não estático + campos de instância.
- **Rationale**: FR-013 (falha estrutural uma vez por arquivo) + FR-026 (sem estático mutável).
- **Alternatives**: `static` + `@BeforeAll` (viola FR-026); `@BeforeEach` (carrega o contrato N vezes).

## R18 - Golden por multiconjunto

- **Decision**: `expected/divergences.txt` com uma linha por divergência; comparação por multiconjunto; ordem verificada por arquivo; cenário estrutural por prefixo.
- **Rationale**: o surefire não garante ordem global entre classes; a mensagem do parser é volátil entre versões do Jackson.
- **Alternatives**: dump completo (frágil); só fragmentos (não prova equivalência).

## R19 - Colisão explícita

- **Decision**: pré-computar os nomes; `writeJavaSourceIfAbsent == null` ⇒ `throw` com os dois nomes.
- **Rationale**: FR-025; o comportamento atual (aviso + skip) esconderia a colisão.
- **Alternatives**: aviso + skip (atual); sobrescrever (perde trabalho do usuário).

## R20 - Ausência por diretório

- **Decision**: os 19 `verify.groovy` procuram o diretório `contract/` sob `src/test/java`.
- **Rationale**: o glob `*ContractTest.java` deixa de casar com os novos nomes — nenhum arquivo termina em `ContractTest.java`.
- **Alternatives**: listar nomes fixos (quebraria com contrato variável); manter o glob (falso negativo).

## R21 - `ContractTestLayoutTest`

- **Decision**: uma suite lê os 7 templates e roda o gerador nas 3 variantes; mede linhas de código, tamanho de método, laço dentro de `@Test`, estático não-final, "And" no nome e chamada entre testes.
- **Rationale**: FR-026/SC-010; sem medição, "legível" é opinião.
- **Alternatives**: só revisão manual; só o verify da fixture (não cobre os templates).

## R22 - Bullets da seção 2.1

- **Decision**: os 4 bullets técnicos (2026, 2027, 2030, 2031) são atualizados; título, âncora, premissa, ativação, regras espelhadas e nota de JSON ficam intactos.
- **Rationale**: FR-023 revisada; os bullets descreviam 8 classes e falha agregada.
- **Alternatives**: não tocar (contradição com a FR-012); reescrever a seção (perde a referência de testabilidade).

## Decisões reaproveitadas da rodada anterior (ainda válidas)

- Reflexão de campos declarados (imune a Lombok); Jackson já presente; teste lê o contrato do classpath; sabotagem não cria fixture. **Exceção**: a falha agregada foi substituída por uma falha por divergência (FR-012, 2026-11-18).

## Achado factual

- **Baseline de fixtures**: 22 fixtures em `src/test/resources/projects/` (a spec cita "15 perfis", desatualizada — ver plan A13).
