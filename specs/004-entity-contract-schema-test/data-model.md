# Data Model: Gate de Conformidade Entidade–Contrato (delta 2026-11-18)

**Feature**: `004-entity-contract-schema-test` | **Date**: 2026-11-13 | **Updated**: 2026-11-18

## A. Pacote de teste gerado (`<pkg>.<entity_package>.contract`)

| Arquivo | Papel | Roda como teste? | Origem |
|---|---|---|---|
| `TechnicalIdMappingTest` | `id` do contrato ↔ `<entidade>Id`; sem `id` comum | sim | Velocity |
| `MalformedContractTest` | falha estrutural explícita, sem skip | sim | Velocity |
| `ContractKeyNormalizationTest` | acento, camelCase, caixa, grafias de `id` | sim | Velocity |
| `ContractKeyDiscardTest` | chave inválida descartada | sim | Velocity |
| `EntityFields` | leitura de campo declarado (apoio) | não | Velocity |
| `ContractJson` | leitura do contrato (apoio) | não | Velocity |
| `ContractKeys` | normalização de chaves (apoio) | não | Velocity |
| `ContractNames` | transformações puras de nome (apoio) | não | Velocity |
| `ContractAttributes` | extração da lista efetiva do contrato (apoio) | não | Velocity |
| `ContractNavigation` | navegação do contrato por caminho (apoio) | não | Velocity |
| `SimpleAttributeFieldsTest` | atributos simples da entidade | sim | `generator-04` |
| `NestedAttributeReferenceTest` | referência a tipo aninhado | sim | `generator-04` |
| `EnumAttributeReferenceTest` | referência a enum | sim | `generator-04` |
| `NoExtraEntityFieldTest` | nenhum campo fora do conjunto esperado | sim | `generator-04` |
| `ContractRoundTripTest` | payload sintético entra e sai sem perder chave | sim | `generator-04` |
| `SavedContractMatchesGeneratedModelTest` | contrato salvo × modelo gerado (chaves) | sim | `generator-04` |
| `SavedContractNestedKeysTest` | chaves de cada objeto aninhado do contrato | sim | `generator-04` |
| `SavedContractEnumsMatchTest` | constantes de enum do contrato | sim | `generator-04` |
| `ContractTypeMappingTest` | `type` declarado aceito × tipo efetivo das flags | sim | `generator-04` |
| `<Tipo>FieldsTest` | campos de cada POJO aninhado | sim | `generator-04` |
| `<Enum>EnumTest` | constantes de cada enum | sim | `generator-04` |
| `ScaffoldFieldsTest` | `username`/`password` no ramo sem atributos | sim | `generator-04` (condicional) |

**Perfil de referência**: 21 arquivos de teste, 43 testes, 6 de apoio.
**Remoção**: a guarda (`snippet-20`) apaga o diretório `contract` inteiro quando não há contrato persistido.

## B. Modelo de contrato em runtime

### ContractJson

| Campo | Tipo | Regra |
|---|---|---|
| `entityName` | `String` | `entity_name` > `entityName` > `entity` |
| `attributes` | `List<Attribute>` | precedência `attributes` > `fields` > topo; meta-chaves excluídas |
| `Attribute.key` | `String` | chave bruta do contrato |
| `Attribute.value` | `JsonNode` | valor bruto (sem inferência de tipo) |
| `Attribute.declaredType` | `String` | `type` declarado, quando houver |
| `Attribute.explicitConstants` | `List<String>` | `enumValues` / `values`, quando houver |

`ContractJson` carrega uma vez por arquivo (ver D15), é imutável e expõe `load()` e `parse(String)` (este último para a falha estrutural).

### ContractKeys

| Método | Regra |
|---|---|
| `topLevelName(key)` | `id` (qualquer caixa) → `<entidade>Id`; senão `stripAccents` + `lowerFirst`; válido se `^[a-z][a-zA-Z0-9_]*$`; senão `null` |
| `nestedName(key)` | camelCase; palavra reservada ganha sufixo `_`; senão `null` |
| `typeName(key)` | PascalCase; palavra reservada ganha `Type`; vazio → `Type` |
| `discarded(key)` | `true` quando o nome aplicável é `null` |

### DivergenceRecord

| Campo | Tipo |
|---|---|
| `direction` | `CONTRACT_TO_ENTITY` \| `ENTITY_TO_CONTRACT` |
| `path` | `String` |
| `expected` | `String` |
| `found` | `String` (`absent` quando ausente) |
| `kind` | `MISSING_FIELD`, `EXTRA_FIELD`, `TYPE_MISMATCH`, `ENUM_NAME_MISMATCH`, `ENUM_CONSTANT_MISMATCH`, `STRUCTURAL` |

Uma divergência = uma falha. O arquivo de regra agrupa as suas; não há agregador entre arquivos (FR-012).

## C. Modelo de geração (flags)

| Flag | Uso no `generator-04` |
|---|---|
| `custom_entity_attributes_list` | atributos simples; `id` é técnico e não vira teste simples |
| `contract_nested_type_definitions` | um `<Tipo>FieldsTest` por tipo e os campos de cada um |
| `contract_enum_definitions` | um `<Enum>EnumTest` por tipo; `nested.` resolve para o subpacote |
| `enable_custom_entity_attributes` | gera `ScaffoldFieldsTest` e escolhe o ramo do `NoExtraEntityFieldTest` |
| `entity_name` | nome da entidade e campo técnico `<entidade>Id` |
| `entity_package_name` | pacote de destino do pacote `contract` |

**Regras de emissão**:

- Ordem das flags é a ordem de emissão (determinística).
- Tipo sem pacote na flag é comparado por nome simples; `java.lang`, pacote da entidade e `nested` usam `Class<?>`.
- Nenhum arquivo com zero testes.
- Colisão de nome de arquivo ⇒ exceção explícita com os dois nomes; nunca sobrescrever.

## D. Artefatos do archetype

| Artefato | Caminho | Papel |
|---|---|---|
| `SchemaTestTemplateSet` | `archetype-resources/src/test/java/__entity_package_name__/contract/*.java` | 10 arquivos fixos (4 de teste + 6 de apoio) |
| `SchemaTestGenerator` | `generators/generator-04-contract_schema_tests.groovy` | 17 arquivos derivados das flags |
| `RemovalGuard` | `snippets/snippet-20-contract_schema_test.groovy` | remove o pacote inteiro quando não há contrato |
| `GuardSuite` | `src/test/archetype-post-generate/SnippetContractSchemaTestTest.groovy` | prova remoção/presença por diretório |
| `GeneratorSuite` | `src/test/archetype-post-generate/ContractSchemaTestGeneratorTest.groovy` | 3 variações + colisão |
| `LayoutSuite` | `src/test/archetype-post-generate/ContractTestLayoutTest.groovy` | limites 120/20/60 e regras de forma |
| `FixtureAsserts` | `entity-contract/verify.groovy` | 21 arquivos derivados, 43 testes, 9 sabotagens, golden |
| `PresenceAsserts` | `custom-nested-objects/verify.groovy`, `custom-enums-from-payload/verify.groovy` | presença + surefire verde |
| `AbsenceAsserts` | 19 `verify.groovy` | ausência pelo diretório `contract/` |

## E. Mapeamento FR → design

| FR | Onde |
|---|---|
| FR-001/FR-002 | `RemovalGuard` + `GuardSuite` + condição do `generator-04` |
| FR-004/FR-006 | `<Tipo>FieldsTest`, `SimpleAttributeFieldsTest`, `NoExtraEntityFieldTest` |
| FR-007 | `generator-04` (tipos das flags) + `ContractTypeMappingTest` + `EntityFields` |
| FR-008 | `TechnicalIdMappingTest` + `ContractKeys` |
| FR-009 | `ContractKeys` + `ContractKeyNormalizationTest` + `ContractKeyDiscardTest` |
| FR-010 | `<Tipo>FieldsTest` + `<Enum>EnumTest` |
| FR-011 | comparação por campos declarados (`EntityFields`) |
| FR-012/FR-013 | uma falha por divergência; `@BeforeAll` + `MalformedContractTest` |
| FR-014..FR-018 | `FixtureAsserts`, `PresenceAsserts`, matriz 22, pom |
| FR-019..FR-023 | README + 2.1 (4 bullets) |
| FR-024 | tabela da seção A |
| FR-025 | `generator-04` + `ContractSchemaTestGeneratorTest` |
| FR-026 | `ContractTestLayoutTest` |
| FR-027 | 6 classes de apoio, em 3 responsabilidades |
| FR-028 | nome do arquivo = sujeito; nome do método = caso |
| FR-029 | `ContractSchemaTestGeneratorTest` (3 variações) |

## F. Regras de comparação

- **Campos**: `Class.getDeclaredFields()` (nunca getters) → imune a Lombok.
- **Id**: `id` do contrato ≡ `<entidade>Id` (`Long`); sempre excluído de ENTITY_TO_CONTRACT.
- **Scaffold**: contrato sem atributos efetivos ⇒ `username` e `password` permitidos em ENTITY_TO_CONTRACT.
- **Aninhados**: um arquivo por tipo; o tipo esperado é resolvido para `Class<?>` quando o pacote é conhecido.
- **Enums**: nome do tipo + conjunto de constantes; sem tolerar remoção/renomeação em um dos lados.
- **Tipo declarado**: `ContractTypeMappingTest` aplica a precedência de FR-007 ao `type` aceito (`java.lang`/tipo do projeto) e compara com o tipo efetivo das flags; a inferência por valor permanece no mecanismo de geração (D17).
- **Round-trip**: payload sintético com nomes Java (`id` → `<entidade>Id`), `FAIL_ON_UNKNOWN_PROPERTIES=true`, re-serialização e comparação do conjunto de chaves.
- **Ordem**: `@TestMethodOrder(MethodOrderer.MethodName.class)` nos arquivos com mais de um teste.
