# Implementation Plan: Gate de Conformidade Entidade–Contrato (Schema Test na Aplicação Gerada)

**Branch**: `004-entity-contract-schema-test` | **Date**: 2026-11-13 | **Spec**: [spec.md](spec.md)

**Input**: Instruções de replanejamento fornecidas pelo mantenedor (2026-11-13).

## Summary

O schema test é um artefato **da aplicação gerada**: um pacote de testes em `<pkg>.<entity_package>.contract`, materializado em duas camadas — 7 arquivos fixos (4 de teste + 3 de apoio) por template Velocity e 15 arquivos de teste derivados das flags pelo `generator-04` —, que lê `src/test/resources/contracts/contract.json` do classpath de teste, compara a entidade por **reflexão de campos declarados** (bidirecional, recursivo em aninhados, com enums e `id` técnico) e faz um **round-trip estrito** com Jackson (`FAIL_ON_UNKNOWN_PROPERTIES=true`). O archetype **não interpreta contrato nem entidade**: ele apenas (a) materializa/remove o artefato por condição derivada e (b) verifica, nos ITs, presença, ausência, execução verde e falha sob sabotagem — sempre executando o teste **dentro** da aplicação gerada.

A entrega inclui ainda a **documentação da feature no README**: subtópico **Contratos** dentro de **Funcionalidades**, em padrão `<details>` e linguagem simples para dois públicos (quem usa o módulo gerado e quem mantém o archetype).

## Technical Context

**Language/Version**: Java 17 (aplicação gerada); Groovy 4.0.28 (pós-processador); Velocity 2.4.1 (templates)

**Primary Dependencies**: Maven 3.9.16; maven-archetype-plugin 3.4.1; Spring Boot 4.0.0 (aplicação gerada); JUnit 5 via `spring-boot-starter-test`; Jackson (JsonNode/ObjectMapper) via `spring-boot-starter-web` — **zero dependências novas no pom gerado**

**Storage**: `src/test/resources/contracts/contract.json` (persistido por `generator-03`, cópia byte-a-byte)

**Testing**: aplicação gerada — JUnit 5 no ciclo dela (`mvn test`/`verify`); archetype — suites Groovy-script (fase `test`) + fixtures IT (fase `integration-test`)

**Target Platform**: build Maven do archetype e das aplicações geradas (Windows/POSIX)

**Project Type**: Maven Archetype (gerador de módulos Spring Boot)

**Performance Goals**: irrelevante — leitura única de um JSON pequeno; O(atributos) por execução

**Constraints**: sem flag nova; zero dependência nova; ativação derivada; o archetype não interpreta contrato/entidade; matriz IT preservada; §Technology Stack da Constituição intocada

**Scale/Scope**: 22 fixtures IT; 36 suites Groovy existentes + 2 novas (38); contratos até 25 níveis; 19 arquivos de teste + 3 de apoio no pacote `contract` do perfil de referência (41 testes); 15 arquivos emitidos pelo `generator-04`

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Princípio | Avaliação | Evidência |
|-----------|-----------|-----------|
| I. Modularidade por chave de ativação (NON-NEGOTIABLE) | ✅ PASS | Nenhum flag novo. Existência do artefato derivada de `enable_folder_src_test_java=true` ∧ contrato persistido; remoção garantida quando `entity_contract_file_path` é ausente/`none`/`null`/vazio **ou** o contrato não foi persistido (árvore de testes inexistente) |
| II. Convenção fixa de nomenclatura (NON-NEGOTIABLE) | ✅ PASS | `snippet-20-contract_schema_test.groovy` com marcador `// SNIPPET-20-CONTRACT_SCHEMA_TEST.GROOVY`; ordem garantida por GENERATORS → SNIPPETS (alfabética). **Sem edição de SECTIONS** — ver D9 |
| III. Pacote por funcionalidade | ✅ PASS | Testes em `<pkg>.<entity_package>.contract` (subpacote da entidade), nunca em pacote técnico |
| IV. Binding duplo de config | N/A | Nenhuma propriedade externalizada nova |
| V. Credenciais fora do repositório | ✅ PASS | Nenhum segredo; regra-05 §8 permanece |
| VI. Validar antes de culpar o código | ✅ PASS (processo) | Todo ciclo começa com `mvn clean install` completo (recompõe `archetype-post-generate.groovy` no `process-resources`) |

**Compliance review obrigatório**: toca `META-INF/archetype-post-generate.groovy` (via compositor) e `archetype-resources/` — revisar contra I–VI (Governança).

**Documentação (README)**: fora da lista de artefatos sujeitos a compliance review (Governança); não cria flag, não altera nomenclatura de extensão, não toca código gerado. Nenhum princípio afetado.

**Re-check pós-Phase 1**: nenhuma violação introduzida; `Complexity Tracking` vazio.

## Project Structure

### Documentation (this feature)

```text
specs/004-entity-contract-schema-test/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/schema-test-contract.md
└── checklists/{requirements.md,conformance.md}
```

### Source Code (repository root)

```text
src/main/resources/archetype-resources/src/test/java/__entity_package_name__/contract/
├── TechnicalIdMappingTest.java                 # FIXO: id do contrato ↔ <entidade>Id
├── MalformedContractTest.java                  # FIXO: falha estrutural explícita
├── ContractKeyNormalizationTest.java           # FIXO: acento, camelCase, caixa, grafias de id
├── ContractKeyDiscardTest.java                 # FIXO: chave inválida descartada
├── EntityFields.java                           # FIXO: leitura de campo declarado (apoio)
├── ContractJson.java                           # FIXO: leitura do contrato (apoio)
├── ContractKeys.java                           # FIXO: normalização de chaves (apoio)
└── <15 arquivos emitidos pelo generator-04>    # GERADO: ver "Camada gerada" na seção do delta

src/main/resources/META-INF/archetype_post_generate_generators/
└── generator-04-contract_schema_tests.groovy   # NOVO: deriva os 15 arquivos das flags

src/main/resources/META-INF/archetype_post_generate_snippets/
└── snippet-20-contract_schema_test.groovy      # mantido: remove o pacote inteiro sem contrato

src/test/archetype-post-generate/
├── SnippetContractSchemaTestTest.groovy        # MOD: nome semeado realista
├── EntityContractLombokParityTest.groovy       # MOD: gateFiles 7 + membresia nova
├── FixturesMatrixTest.groovy                   # MOD: assinaturas das 2 fixtures
├── ContractSchemaTestGeneratorTest.groovy      # NOVO: 3 variações + colisão
└── ContractTestLayoutTest.groovy               # NOVO: limites 120/20/60 e regras de forma

src/test/groovy/run-archetype-post-generate-tests.groovy   # MOD: wire das 2 novas suites

src/test/resources/projects/
├── entity-contract/
│   ├── expected/divergences.txt                # NOVO: golden capturado antes do refactor
│   ├── sample-contract.json                    # mantido
│   ├── archetype.properties                    # mantido
│   └── verify.groovy                           # MOD: 19 arquivos derivados, 41 testes, golden
├── custom-nested-objects/                      # MOD: contrato persistido + presença + assinatura
├── custom-enums-from-payload/                  # MOD: contrato persistido + presença + assinatura
├── <19 fixtures sem contrato>/verify.groovy     # MOD: ausência pelo diretório contract/
└── README.md                                   # MOD: linha das fixtures com contrato

README.md                                       # MOD: subtópico "Contratos" em "Funcionalidades" — título no <summary>, 6 itens numerados, diagrama do ciclo
```

**Structure Decision**: o artefato é um **pacote de teste** dentro do pacote da entidade, dividido em camada fixa (Velocity) e camada gerada (flags); a guarda remove o diretório inteiro, mantendo a remoção atômica. O IT reutiliza `entity-contract` e passa a usar também `custom-nested-objects` e `custom-enums-from-payload`; nenhuma fixture nova.

## Documentação (README.md)

Origem: FR-019..FR-023 (clarify 2026-11-17, revisado no clarify 2026-11-18) · Decisão de pesquisa: R14 (revisada em 2026-11-18).

| Item | Definição |
|------|-----------|
| Local | Subtópico **Contratos**, dentro de **Funcionalidades** (README.md, tópico na linha 2642) |
| Formato | `<details style="margin-left: 2em;"><summary><strong> Contratos📜: a 'Fonte da Verdade' do Módulo da API </strong></summary>` … `</details>`, mesmo padrão de collapse do README; sem título interno repetindo o nome |
| Nome canônico | **Contratos: a 'Fonte da Verdade' do Módulo da API** (o ícone 📜 é decorativo e não integra o nome; "schema test" fica como nome de trabalho interno) |
| Públicos | Quem usa o módulo gerado e quem mantém o archetype — atendidos pelo item 4 (**Para quem serve**) |
| Linguagem | Simples, clara e objetiva; sem jargão; frases curtas, uma ideia por linha; compreensível por estagiário sem experiência |
| Conteúdo | Seis itens numerados, nesta ordem: 1) **Raciocínio central**; 2) **O que faz**; 3) **Como funciona**; 4) **Para quem serve**; 5) **Quando ele existe**; 6) **Detalhes técnicos** — **sem** tutorial passo a passo |
| Raciocínio central | Abre a documentação com o ciclo fechado — desenho da tela (Design de Front-End) → contrato → módulo da API → tela —, deixando explícito que a conferência não é um mero teste de conformidade; inclui diagrama em bloco de código `text` (padrão do "Grafo de dependências") com fluxo vertical DESENHO DA TELA → CONTRATO → MÓDULO DA API → TELA e seta de retorno da TELA ao DESENHO |
| Fronteira | Não reexplica o mecanismo de contrato (inferência, formato, flags); os itens 2–5 descrevem apenas o comportamento observável; linka as seções existentes |
| Seção 2.1 | *Testabilidade → 2.1) SCHEMA TEST ENTIDADE-CONTRATO* preserva título, âncora, premissa, ativação, regras espelhadas e nota de JSON; os 4 bullets técnicos são atualizados (ver abaixo); o item 6 linka para ela com texto e href inalterados |
| Não faz | Não duplica conteúdo, não move seções, não renomeia a 2.1 |

### 2.1 — bullets atualizados (FR-023)

| Linha | Novo texto |
|---|---|
| 2026 | **Conteudo**: um arquivo de teste por sujeito — campos simples, cada tipo aninhado e cada enum — mais os arquivos das regras transversais (id tecnico, campos excedentes, round-trip, contrato salvo, contrato malformado, normalizacao de chaves). Apoio: `EntityFields`, `ContractJson` e `ContractKeys`. |
| 2027 | **Execucao**: pelo ciclo de testes da propria aplicacao (`mvnw test`), para sempre. Cada divergencia produz uma falha propria, agrupada no arquivo de regra, em ordem deterministica, com atributo, tipo esperado/encontrado, direcao (contrato->entidade ou entidade->contrato) e caminho hierarquico. |
| 2030 | **Prova no archetype (Frente 02)**: as fixtures `entity-contract`, `custom-nested-objects` e `custom-enums-from-payload` verificam a presenca do pacote, o relatorio surefire verde da aplicacao e a matriz de sabotagem da Frente 02. |
| 2031 | **Sabotagem coberta**: contrato (chave removida, folha mais profunda renomeada, JSON corrompido), entidade (campo renomeado, campos clandestinos, tipo alterado), enums de topo e aninhado, e multiplas divergencias reportadas uma por falha, em ordem deterministica - mais a paridade Lombok on/off com o mesmo veredito. O contrato da fixture cobre 6 POJOs aninhados em cadeia (7 niveis), enums de topo/aninhado, tipo de projeto sobre array e dois campos do mesmo enum. |
| 2022/2024/2025/2028/2029 | inalterados (título, premissa, ativação derivada, regras espelhadas, JSON) |

## Delta 2026-11-18: Refactor de Estrutura e Legibilidade (FR-024..FR-029)

**Objetivo**: substituir o pacote de 8 classes/1023 linhas por um pacote fatiado por sujeito, sem alterar a semântica de comparação (FR-004..FR-011), a ativação derivada (FR-001/002) nem o formato do contrato (FR-003). A entrega é um delta: FR-001..FR-023 permanecem válidas.

### Camada fixa (Velocity, 7 arquivos)

| Arquivo | Papel | Testes |
|---|---|---|
| `TechnicalIdMappingTest` | `id` do contrato ↔ `<entidade>Id`; sem `id` comum | 3 |
| `MalformedContractTest` | falha estrutural explícita, sem skip | 2 |
| `ContractKeyNormalizationTest` | acento, camelCase aninhado, caixa, grafias de `id` | 4 |
| `ContractKeyDiscardTest` | chave que permanece inválida é descartada | 1 |
| `EntityFields` | leitura de campo declarado (apoio) | — |
| `ContractJson` | leitura do contrato, formas e meta-chaves (apoio) | — |
| `ContractKeys` | normalização de chaves (apoio) | — |

`ContractJson` usa Java 17 (streams, text blocks) e fica ≤60 linhas de código; o apoio permanece em 3 classes (FR-027, D14).

### Camada gerada (`generator-04-contract_schema_tests.groovy`, 15 no perfil de referência)

| Arquivo | Sujeito | Testes |
|---|---|---|
| `SimpleAttributeFieldsTest` | atributos simples da entidade | 3 |
| `NestedAttributeReferenceTest` | referência a tipo aninhado | 1 |
| `EnumAttributeReferenceTest` | referência a enum | 2 |
| `NoExtraEntityFieldTest` | nenhum campo fora do conjunto esperado | 2 |
| `ContractRoundTripTest` | payload sintético entra e sai sem perder chave | 2 |
| `SavedContractMatchesGeneratedModelTest` | contrato salvo × modelo gerado (chaves) | 1 |
| `ContractTypeMappingTest` | `type` declarado aceito × tipo efetivo das flags | 1 |
| `EnderecoFieldsTest` · `ComplementoFieldsTest` · `PontoReferenciaFieldsTest` · `ReferenciaFieldsTest` · `OrigemFieldsTest` · `DetalheFieldsTest` | campos de cada POJO aninhado | 4+2+3+2+2+2 = 15 |
| `StatusEnumTest` · `UfEnumTest` | constantes de cada enum | 2+2 = 4 |
| `ScaffoldFieldsTest` (condicional) | `username`/`password` no ramo sem atributos | 2 |

**Total no perfil**: 19 arquivos de teste, 41 testes, 3 de apoio.

**Achado A1 (resolvido — opção A, 2026-11-18)**: a árvore aprovada no clarify não tinha arquivo para a FR-007 (tipo declarado). `ContractTypeMappingTest` cobre o `type` declarado **aceito** (`java.lang`/tipo do projeto) contra o tipo efetivo das flags. A inferência por valor para atributos **sem** `type` aceito continua sendo do mecanismo de geração (D17); uma edição pós-geração apenas no valor não é re-inferida. A paridade total com a FR-007(b)/(c) fica registrada como redução deliberada: se for exigida, o caminho é elevar o limite de apoio (60) ou aceitar mais um arquivo de teste.

**Por que `ContractRoundTripTest` é gerado (D13)**: o payload de teste depende dos tipos das flags. Mantê-lo fixo exigiria reintroduzir a inferência de tipo que o refactor remove. O payload é sintético (não lê valores do contrato): o archetype continua sem interpretar contrato/entidade.

### Gerador

| Item | Decisão |
|---|---|
| Nome/ordem | `generator-04-contract_schema_tests.groovy`, depois de `generator-03` e antes de `SNIPPETS`; `GENERATORS` é dinâmico, `SECTIONS` intocado |
| Gatilho | derivado: só roda se `src/test/resources/contracts/contract.json` existir |
| Entradas | `custom_entity_attributes_list`, `contract_nested_type_definitions`, `contract_enum_definitions`, `entity_name`, `entity_package_name`, `package`, `enable_custom_entity_attributes` |
| Saída | `src/test/java/<entity_package>/contract/` |
| Escrita | `writeJavaSourceIfAbsent`; retorno `null` ⇒ colisão ⇒ exceção explícita com os dois nomes; nunca sobrescrever nem skip |
| Ordem | ordem das flags, determinística |
| Tipos | `Class<?>` para `java.lang`, pacote da entidade e `nested`; nome simples para tipo de projeto sem pacote na flag |
| Vazio | não emite arquivo com zero testes |
| Dedup | um `<Tipo>FieldsTest` e um `<Enum>EnumTest` por tipo, mesmo com dois campos do mesmo tipo |
| Reuso | `resolveEntityPackage` e `writeJavaSourceIfAbsent` (body-14); nomes de tipo vêm literais das flags (já normalizados na camada de flags) |

### Falha e ordem

- `@TestInstance(PER_CLASS)` + `@BeforeAll` não estático: o contrato é carregado uma vez por arquivo; contrato inválido falha o arquivo uma vez, não uma vez por teste (FR-013, D15).
- Arquivos com mais de um `@Test` usam `@TestMethodOrder(MethodOrderer.MethodName.class)` para ordem determinística (SC-008).
- Cada divergência produz uma falha própria; o arquivo de regra agrupa as suas (FR-012).
- `MalformedContractTest` não carrega o contrato no `@BeforeAll`: testa `ContractJson.parse` diretamente.

### Golden

`src/test/resources/projects/entity-contract/expected/divergences.txt`: uma linha por divergência, no formato canônico, capturada **antes** do refactor. A comparação é por multiconjunto (o surefire não garante ordem global); a ordem dentro de cada arquivo é verificada separadamente. O cenário de JSON corrompido mantém a asserção de prefixo `CONTRACT STRUCTURAL FAILURE`, porque a mensagem do parser é volátil.

### Ripple obrigatório

| Item | Arquivo(s) |
|---|---|
| A | remover 8 templates antigos; adicionar 7 fixos; adicionar `generator-04` |
| B | `entity-contract/verify.groovy`: lista derivada das flags, 19 relatórios/41 testes, 9 sabotagens, golden |
| C | `custom-nested-objects` e `custom-enums-from-payload`: `entity_contract_file_path` + presença + surefire + assinatura |
| D | 19 `verify.groovy`: ausência pelo diretório `contract/`, nunca por glob de nome |
| E | `EntityContractLombokParityTest`: `gateFiles.size() == 7`; membresia nova |
| F | `SnippetContractSchemaTestTest`: nome semeado realista; asserção continua por diretório |
| G | `contracts/schema-test-contract.md` §4/§5 |
| H | `data-model.md` A/B/C/D |
| I | `quickstart.md` §2/§3/§4/§6 |
| J | `README.md` 2.1: 4 bullets |
| K | 2 suites Groovy novas + registro no runner |
| L | `FixturesMatrixTest`: 22 fixtures e assinaturas |

### Sequência baby step

| Passo | Conteúdo | Portão |
|---|---|---|
| C1 | capturar o golden; nenhum template tocado | 22/22 |
| C2 | extrair `ContractKeys`/`ContractJson`/`EntityFields`; `ContractModel` delega | 22/22, veredito idêntico |
| C3 | `generator-04` + 2 suites; templates antigos ainda presentes | projeto gerado roda 47 testes; 22/22 |
| C4 | flip: remover antigos, adicionar fixos, `verify.groovy` novo | 19 arquivos/41 testes; sabotagens = golden |
| C5 | fixtures C (contrato persistido + presença + assinatura) | 22/22 |
| C6 | itens D/E/F (ausência por diretório, Lombok, guard) | 38 suites; 22/22 |
| C7 | docs G/H/I/J | revisão |
| C8 | `mvn -o clean install` completo | 22/22 + 9 sabotagens + golden |

### Constitution Check (re-check)

| Princípio | Avaliação |
|---|---|
| I | ✅ sem flag nova; `generator-03 → generator-04 → snippet-20` |
| II | ✅ `generator-04-...` segue o padrão; `SECTIONS` intocado |
| III | ✅ testes no subpacote `contract` da entidade |
| IV | N/A |
| V | ✅ sem segredo |
| VI | ✅ `clean install` no C8 |

**Complexity Tracking**: vazio.

## Desvios deliberados das instruções

- **D9 — SECTIONS não é editado.** `SECTIONS` só aceita arquivos de `body/` + marcadores dinâmicos `VARIABLES/GENERATORS/SNIPPETS`. Incluir `snippet-20-*.groovy` como entrada estática (a) falha `ComposerTest` ("required body file missing") e (b) **duplica** o snippet, pois `SNIPPETS` expande a pasta inteira em ordem alfabética. A posição pedida ("após o generator-03") já é garantida: GENERATORS antes de SNIPPETS, `snippet-20` > `snippet-19`.
- **D10 — fonte canônica de tipos.** `src/main/pre-processamento-maven-flags-via-llm` **não existe** no repositório. Fontes canônicas: `src/main/resources/utils/03-derivacao-regras-de-formacao-do-contrato/regras-01..03` + `specs/_archive/003-llm-flag-inference/{research,data-model}.md` + `utils/02-operacao-procedimento-do-agente/prompt-03`.
- **D11 — contrato da fixture enriquecido.** Sem contrato com aninhado/enum em nenhum IT, a US4 (P2) ficaria sem prova de entrega. O `entity-contract` ganha **6 objetos aninhados em cadeia (7 níveis contando a entidade)** e enums (topo + aninhado).
- **D12 — scaffold `username/password`.** Quando o contrato não tem atributos efetivos, a entidade gerada carrega o scaffold `username/password` (`enable_custom_entity_attributes=false`). O teste exclui exatamente esses dois nomes nesse cenário (ver research R10).
- **D13 — `ContractRoundTripTest` é gerado, não fixo.** O payload sintético depende dos tipos das flags; mantê-lo fixo exigiria reintroduzir a inferência de tipo que o refactor remove. A camada fixa fica em 7 arquivos (4 de teste + 3 de apoio) e o `EntityContractLombokParityTest` passa de 8 para 7.
- **D14 — `ContractJson` em ≤60 linhas de código.** O uso de Java 17 (streams e text blocks) mantém o apoio em 3 classes, como FR-027. A contagem do `ContractTestLayoutTest` exclui `package`, `import`, linhas em branco e comentários.
- **D15 — `@TestInstance(PER_CLASS)` + `@BeforeAll` não estático.** Evita campo estático mutável (FR-026) e mantém a falha estrutural uma vez por arquivo (FR-013).
- **D16 — normalização de caixa no topo.** `ContractKeys` mapeia `Name` → `name`, corrigindo o comportamento estrito do `ContractModel` atual para cumprir FR-009; coberto por `ContractKeyNormalizationTest`. Não é requisito novo, é conformidade com o requisito existente.
- **D17 — reconciliação de tipo declarado (A1, aceito em 2026-11-18).** `ContractTypeMappingTest` compara o `type` declarado **aceito** (`java.lang`/tipo do projeto) com o tipo efetivo das flags. A inferência por valor para atributos sem `type` aceito permanece no mecanismo de geração: reimplementá-la exigiria ~150 linhas de apoio, acima do limite de 60 (FR-026). Uma edição pós-geração apenas no valor não é re-inferida.

## Achados a reconciliar com a spec

- **A1 — reconciliação de tipo declarado (resolvido — opção A).** Proposta aceita: `ContractTypeMappingTest` (19º arquivo, 41º teste), cobrindo o `type` declarado aceito; a inferência por valor fica no mecanismo de geração (D17). Alternativas descartadas: (a) aceitar a redução e ajustar a FR-007; (b) elevar o limite de apoio; (c) aceitar mais um arquivo de teste para a inferência por valor.
- **A13 - baseline de fixtures (resolvido).** A spec agora declara **22 perfis** (FR-017, SC-004 e US2, cenário 3), alinhada à matriz real de 22 fixtures em `src/test/resources/projects/`.

## Complexity Tracking

> Nenhuma violação de Constitution Check; tabela vazia por decisão de gate.
