# Data Model: Gate de Conformidade Entidade–Contrato (replan)

**Feature**: `004-entity-contract-schema-test` | **Date**: 2026-11-13

## A. Pacote de teste gerado (`<pkg>.<entity_package>.contract`)

| Classe | Papel | Roda como teste? |
|---|---|---|
| `<Entidade>ContractTest` | entrada JUnit 5; agrega as divergências de todas as direções numa única falha | sim |
| `<Entidade>ContractConformanceTest` | contrato→entidade: campos, tipos, id técnico | sim |
| `<Entidade>ContractCoverageTest` | entidade→contrato: nenhum campo clandestino (exceto id e scaffold) | sim |
| `ContractNestedTypesTest` | recursão nos POJOs de `.nested` (2+ níveis, limite 25) | sim |
| `ContractEnumsTest` | nome do tipo derivado + conjunto de constantes | sim |
| `ContractRoundTripTest` | round-trip estrito Jackson (`FAIL_ON_UNKNOWN_PROPERTIES=true`) | sim |
| `ContractModel` | leitura + normalização do contrato (visão efetiva) | não (apoio) |
| `ContractDiagnostics` | coleta de divergências + formatação da mensagem | não (apoio) |

Remoção: a guarda apaga o diretório `contract` inteiro.

## B. Modelo de contrato (runtime do teste)

### ContractDocument

| Campo | Tipo | Regra |
|---|---|---|
| `entityName` | `String` | `entity_name` > `entityName` > `entity`; meta-chave |
| `attributes` / `fields` | `ObjectNode` \| `ArrayNode` | precedência `attributes` > `fields` > topo |
| topo | `ObjectNode` | atributos quando não há coleção; meta-chaves excluídas |

Meta-chaves: `entity_name`, `entityName`, `entity`, `package`, `entity_package_name`, `description`, `attributes`, `fields`.

### EffectiveAttribute

| Campo | Tipo | Regra |
|---|---|---|
| `contractPath` | `String` | ex.: `endereco.complemento.descricao` |
| `javaName` | `String` | camelCase em aninhados; tipo do projeto nomeia; grafias de `id` descartadas |
| `expectedJavaType` | `String` | simples; aninhados = FQN `<pkg>.<entity_package>.nested.<Tipo>` |
| `kind` | enum | `VALUE`, `NESTED`, `ENUM`, `PROJECT_TYPE` |
| `enumConstants` | `List<String>` | `enumValues` > `values` > valor observado |
| `children` | `List<EffectiveAttribute>` | recursivo; profundidade > 25 → `String` |

### DivergenceRecord

| Campo | Tipo |
|---|---|
| `direction` | `CONTRACT_TO_ENTITY` \| `ENTITY_TO_CONTRACT` |
| `path` | `String` |
| `expected` | `String` |
| `found` | `String` (`absent` quando ausente) |
| `kind` | `MISSING_FIELD`, `EXTRA_FIELD`, `TYPE_MISMATCH`, `ENUM_NAME_MISMATCH`, `ENUM_CONSTANT_MISMATCH`, `STRUCTURAL` |

### Regras de comparação

- **Campos**: `Class.getDeclaredFields()` (nunca getters) → imune a Lombok.
- **Id**: `id` do contrato ≡ `<entidade>Id` (`Long`); sempre excluído de ENTITY_TO_CONTRACT.
- **Scaffold**: contrato sem atributos efetivos ⇒ excluir `username` e `password` de ENTITY_TO_CONTRACT (R10).
- **Aninhados**: FQN do campo é carregado e comparado recursivamente; mesmo nome simples da entidade não ambíguo (ex.: `nested.Pedido` vs entidade `Pedido`).
- **Enums**: nome do tipo + constantes; sem tolerar remoção/renomeação em um dos lados.
- **Round-trip**: visão normalizada com nomes Java (id → `<entidade>Id`), `FAIL_ON_UNKNOWN_PROPERTIES=true`, re-serialização e comparação de conjuntos de chaves/hierarquia.

## C. Artefatos do archetype

| Artefato | Caminho | Papel |
|---|---|---|
| `SchemaTestTemplateSet` | `archetype-resources/src/test/java/__entity_package_name__/contract/*.java` | materializa o pacote de teste; interpola só `${package}`, `${entity_package_name}`, `${entity_name}` |
| `RemovalGuard` | `snippets/snippet-20-contract_schema_test.groovy` | remove `<srcTestJava>/<entity pkg>/contract` quando `contract.json` não existe; loga decisão |
| `GuardSuite` | `src/test/archetype-post-generate/SnippetContractSchemaTestTest.groovy` | prova remoção/presença por condição no harness |
| `FixtureAsserts` | `entity-contract/verify.groovy` | presença + surefire verde + sabotagem |
| `AbsenceAsserts` | 21 `verify.groovy` | ausência da classe nos perfis sem contrato |

## D. Mapeamento FR → design

| FR | Onde |
|---|---|
| FR-001/FR-002 | `RemovalGuard` + `GuardSuite` |
| FR-004/FR-006 | `ContractConformanceTest`, `ContractCoverageTest` |
| FR-007 | `ContractModel` (precedência de tipo) |
| FR-008 | `ContractModel` + `ContractConformanceTest` (id) |
| FR-009 | `ContractModel` (normalização/descarte) |
| FR-010 | `ContractNestedTypesTest`, `ContractEnumsTest` |
| FR-011 | comparação por campos declarados |
| FR-012/FR-013 | `ContractDiagnostics` + `ContractRoundTripTest` |
| FR-014..FR-018 | `FixtureAsserts`, `AbsenceAsserts`, matriz 22, pom |
