# Fixtures de integracao (matriz de flags)

Esta pasta e a matriz de integracao: **uma subpasta = um cenario** = um vetor de valores de flag.

## O que e uma fixture

| Camada | Onde | O que e |
|---|---|---|
| **Coordenadas de geracao** | `groupId`, `artifactId`, `package`, `version` | **Nao sao flags** - nao existem no catalogo; o plugin exige |
| **Flags do catalogo** | as outras chaves de `archetype.properties` | viram os `-D` do `mvn archetype:generate` |
| **O que roda no projeto gerado** | `goal.txt` (`clean,verify`) + `verify.groovy` | a assercao pos-build |

**Combinacao de flags = um PONTO num hipercubo, nao um subconjunto.** Cada fixture declara ~29-32 flags, mas so as que **diferem do default do catalogo** identificam o cenario. Essas formam a *assinatura* da fixture, declarada no cabecalho `# FIXTURE SIGNATURE` do `archetype.properties` e verificada por `FixturesMatrixTest.groovy`.

Ficam fora da assinatura (identidade/metadados): `project_description`, `entity_name`, `entity_package_name`, `java_version`, `show_archetype_post_generate_log`, `api_port`.

## Matriz por cluster

### `core`

| Fixture | Flags sob teste | O que o `verify.groovy` assere |
|---|---|---|
| `basic/` | `enable_folder_src_test_java`, `enable_lombok_support`, `extra_pom_dependencies` | a dependencia extra entra no pom e a arvore src/test e mantida |
| `config-yml/` | `config_file_format`, `custom_entity_attributes_list`, `enable_folder_src_test_java` | os .properties sao podados e o .yml sobrevive |
| `defaults-only/` | *(nenhuma)* | as flags omitidas caem no default do catalogo |
| `java21/` | `custom_entity_attributes_list`, `enable_folder_src_test_java` | o default 21 chega ao pom do projeto gerado |
| `minimal/` | `custom_entity_attributes_list` | todo modulo opcional e podado do projeto gerado |

### `binding`

| Fixture | Flags sob teste | O que o `verify.groovy` assere |
|---|---|---|
| `binding-tests-default-only/` | `custom_entity_attributes_list`, `enable_binding_external_properties`, `enable_binding_external_properties_tests`, `enable_folder_src_test_java` | fontes default sobrevivem; list/map podados; testes do projeto gerado mantidos |
| `binding-tests-list-only/` | `custom_entity_attributes_list`, `enable_binding_external_properties`, `enable_binding_external_properties_tests`, `enable_folder_src_test_java`, `enable_list_binding_external_properties` | fontes list sobrevivem; default/map podados |
| `binding-tests-map-only/` | `custom_entity_attributes_list`, `enable_binding_external_properties`, `enable_binding_external_properties_tests`, `enable_folder_src_test_java`, `enable_map_binding_external_properties` | fontes map sobrevivem; default/list podados |
| `external-binding-default-only/` | `custom_entity_attributes_list`, `enable_binding_external_properties`, `enable_default_binding_external_properties` | fontes default sobrevivem; list/map e seus recursos podados |
| `external-binding-list-only/` | `custom_entity_attributes_list`, `enable_binding_external_properties`, `enable_list_binding_external_properties` | fontes list sobrevivem; default/map podados |
| `external-binding-map-only/` | `custom_entity_attributes_list`, `enable_binding_external_properties`, `enable_map_binding_external_properties` | fontes map sobrevivem; default/list podados |
| `external-binding-mutable-only/` | `binding_external_properties_mutability`, `custom_entity_attributes_list`, `enable_binding_external_properties`, `enable_default_binding_external_properties` | bean mutavel sobrevive; record imutavel podado |
| `external-binding-validated-only/` | `custom_entity_attributes_list`, `enable_binding_external_properties`, `enable_binding_external_properties_validation`, `enable_default_binding_external_properties` | anotacoes Jakarta Validation na classe gerada |

### `logback`

| Fixture | Flags sob teste | O que o `verify.groovy` assere |
|---|---|---|
| `logback-console-granular/` | `custom_entity_attributes_list`, `enable_folder_src_test_java`, `enable_logback_support`, `logback_console_type` | console granular sobrevive; o tabular e podado |
| `logback-no-elk/` | `custom_entity_attributes_list`, `enable_folder_src_test_java`, `enable_logback_support` | artefatos ELK removidos |

### `contract`

| Fixture | Flags sob teste | O que o `verify.groovy` assere |
|---|---|---|
| `custom-attributes/` | `contract_enum_definitions`, `custom_entity_attribute_imports`, `custom_entity_attributes_list`, `enable_custom_entity_attributes`, `entity_contract_file_path` | imports FQN, enums nos pacotes declarados, classe referenciada intacta, contracts/ ausente |
| `custom-enums-from-payload/` | `contract_enum_definitions`, `contract_nested_type_definitions`, `custom_entity_attributes_list`, `enable_custom_entity_attributes`, `enable_folder_src_test_java`, `entity_contract_file_path` | enums gerados a partir de array UPPERCASE + schema test presente e verde |
| `entity-contract/` | `contract_enum_definitions`, `contract_nested_type_definitions`, `custom_entity_attributes_list`, `enable_custom_entity_attributes`, `enable_folder_src_test_java`, `entity_contract_file_path` | contract.json byte-a-byte + entidade coerente + pacote `contract/` fatiado (21 arquivos de teste + 6 de apoio) + surefire verde com 43 testes + sabotagem com o golden (cadeia de 7 niveis + enums) |
| `id-normalization/` | `custom_entity_attributes_list`, `enable_custom_entity_attributes` | uma unica PK; as grafias id/iD sao descartadas |

### `nested`

| Fixture | Flags sob teste | O que o `verify.groovy` assere |
|---|---|---|
| `custom-nested-edge-cases/` | `contract_nested_type_definitions`, `custom_entity_attributes_list`, `enable_custom_entity_attributes`, `enable_folder_src_test_java`, `enable_lombok_support` | nome reservado, palavra-chave, acento, array->String |
| `custom-nested-objects/` | `contract_nested_type_definitions`, `custom_entity_attributes_list`, `enable_custom_entity_attributes`, `enable_folder_src_test_java`, `entity_contract_file_path` | @Transient, referencia por FQN, pacote nested, POJOs plain + schema test presente e verde |

### `multi`

| Fixture | Flags sob teste | O que o `verify.groovy` assere |
|---|---|---|
| `full/` | `binding_external_properties_mutability`, `custom_entity_attributes_list`, `enable_api_cpanel_elk_log_system`, `enable_binding_external_properties`, `enable_binding_external_properties_tests`, `enable_binding_external_properties_validation`, `enable_custom_entity_attributes`, `enable_default_binding_external_properties`, `enable_docker_support`, `enable_folder_src_test_java`, `enable_git_support`, `enable_jwt_authentication`, `enable_list_binding_external_properties`, `enable_logback_appender_file`, `enable_logback_appender_smtp`, `enable_logback_support`, `enable_lombok_support`, `enable_map_binding_external_properties`, `git_repo_branch`, `logback_console_type` | JWT + par de chaves + SecurityConfig; todos os modulos sobrevivem |

## Como rodar

- Todas: `mvn clean integration-test` na raiz do projeto.
- Uma so: copie a fixture para `target/it-single/` e rode `mvn clean integration-test -Darchetype.test.projectsDirectory=target/it-single` (o plugin 3.4.1 nao tem filtro por nome).
- Contexto no README principal: secao **Testabilidade**.
