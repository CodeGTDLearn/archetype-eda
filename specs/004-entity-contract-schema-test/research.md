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

- **Decision**: 6 classes de teste (entrada, conformidade, cobertura, aninhados, enums, round-trip) + 2 de apoio no pacote `contract`; a guarda remove o **diretório** inteiro.
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

## Decisões reaproveitadas da rodada anterior (ainda válidas)

- Reflexão de campos declarados (imune a Lombok); Jackson já presente; teste lê o contrato do classpath; falha agregada e determinística; sabotagem não cria fixture.

## Achado factual

- **Baseline de fixtures**: 22 fixtures em `src/test/resources/projects/` (a spec cita "15 perfis", desatualizada — ver plan A13).
