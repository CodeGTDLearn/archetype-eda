# Graph Report - archetype-v2-eda  (2026-09-23)

## Corpus Check
- 130 files · ~79,505 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 84 file(s) not represented in the graph (top: .xml 37, (none) 18, .sample 14)

## Summary
- 453 nodes · 708 edges · 73 communities (19 shown, 54 thin omitted)
- Extraction: 97% EXTRACTED · 3% INFERRED · 0% AMBIGUOUS · INFERRED: 23 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- Junit5 testing primitives
- Spring config-properties & collections API
- Generic core helpers (collection/data)
- Bean wiring: ConfigurationProperties + Lombok
- List binding properties (mutable JavaBean)
- Annotation-driven service layer (Authentication/API)
- Security config: HTTP+JWT+Encoder (HTTP/BCrypt/JWK)
- List properties record (Constructor Binding)
- List properties immutable class
- Map properties tests (basic)
- Application properties record tests
- Map properties record tests
- Post-generate Groovy helpers (path/files)
- Application properties class tests
- Maven wrapper (mvnw) shell scripts
- Bash: Maven command-line wrappers
- Bash: General UI/file helpers
- Bash: Input processing & validation regex
- Bash: Build archetype-post-generate.groovy
- Bash: Progress-bar generator
- Bash: Build archetype-metadata XML
- Bash: Compound input validators
- Bash: JSON processor
- Bash: JSON validator
- Bash: Project viability checker
- Bash: Script injection
- Bash: artifactId prompt
- Bash: entity custom-attributes prompt
- JPA repository annotation
- Bash: config constants
- Bash: general messages constants
- Bash: maven archetype-generate command-line
- Bash: script core
- Bash: API tests flag prompt
- Bash: binding-properties prompt
- Bash: cpanel prompt
- Bash: docker prompt
- Bash: extra-pom-dependencies prompt
- Bash: general prompts
- Bash: github prompt
- Bash: java version prompt
- Bash: jwt authentication prompt
- Bash: logback manager prompt
- Bash: lombok prompt
- Bash: properties filetype prompt
- Safe routes: admin path-routing
- Safe routes: public API path-routing
- Package: com.eda-archetype/eda-archetype
- Package: groupId/artifactId placeholder

## God Nodes (most connected - your core abstractions)
1. `$` - 24 edges
2. `AuthenticationCredentials` - 20 edges
3. `ListPropertiesTest` - 19 edges
4. `ApplicationPropertiesValidationTest` - 16 edges
5. `MapPropertiesTest` - 15 edges
6. `MapPropertiesImmutableTest` - 14 edges
7. `MapPropertiesRecordTest` - 14 edges
8. `MapItem` - 13 edges
9. `ListPropertiesImmutableTest` - 13 edges
10. `ListPropertiesRecordTest` - 13 edges

## Surprising Connections (you probably didn't know these)
- `ApplicationPropertiesTest` --references--> `ApplicationProperties`  [EXTRACTED]
  src/main/resources/archetype-resources/src/test/java/__entity_name_package__/config/properties/ApplicationPropertiesTest.java → src/main/resources/archetype-resources/src/main/java/__entity_name_package__/config/properties/ApplicationProperties.java
- `ApplicationPropertiesValidationTest` --references--> `ApplicationProperties`  [EXTRACTED]
  src/main/resources/archetype-resources/src/test/java/__entity_name_package__/config/properties/ApplicationPropertiesValidationTest.java → src/main/resources/archetype-resources/src/main/java/__entity_name_package__/config/properties/ApplicationProperties.java
- `ListPropertiesImmutable` --references--> `ListObjects`  [EXTRACTED]
  src/main/resources/archetype-resources/src/main/java/__entity_name_package__/config/properties/list_properties/ListPropertiesImmutable.java → src/main/resources/archetype-resources/src/main/java/__entity_name_package__/config/properties/list_properties/ListObjects.java
- `ListPropertiesRecord` --references--> `ListObjects`  [EXTRACTED]
  src/main/resources/archetype-resources/src/main/java/__entity_name_package__/config/properties/list_properties/ListPropertiesRecord.java → src/main/resources/archetype-resources/src/main/java/__entity_name_package__/config/properties/list_properties/ListObjects.java
- `ListPropertiesImmutableTest` --references--> `ListObjects`  [EXTRACTED]
  src/main/resources/archetype-resources/src/test/java/__entity_name_package__/config/properties/list_properties/ListPropertiesImmutableTest.java → src/main/resources/archetype-resources/src/main/java/__entity_name_package__/config/properties/list_properties/ListObjects.java

## Import Cycles
- None detected.

## Communities (73 total, 54 thin omitted)

### Community 0 - "Junit5 testing primitives"
Cohesion: 0.09
Nodes (32): after_each_test_method, assertdoesnotthrow, assertthrows, BeforeAll, bindable, binder, bindexception, configurationpropertysource (+24 more)

### Community 1 - "Spring config-properties & collections API"
Cohesion: 0.10
Nodes (16): collections, map, org.springframework.boot.context.properties.bind.ConstructorBinding, org.springframework.boot.context.properties.ConfigurationProperties, org.springframework.stereotype.Component, MapItem, MapProperties, MapPropertiesImmutable (+8 more)

### Community 2 - "Generic core helpers (collection/data)"
Cohesion: 0.06
Nodes (33): , collection, collectors, crudrepository, data, entity, generatedvalue, generationtype (+25 more)

### Community 3 - "Bean wiring: ConfigurationProperties + Lombok"
Cohesion: 0.08
Nodes (10): component, configurationproperties, lombok.Getter, lombok.Setter, notempty, org.springframework.validation.annotation.Validated, pattern, ApplicationProperties (+2 more)

### Community 4 - "List binding properties (mutable JavaBean)"
Cohesion: 0.19
Nodes (8): ListObjects, ListProperties, BeforeEach, Order, OrderAnnotation, Test, TestMethodOrder, ListPropertiesTest

### Community 5 - "Annotation-driven service layer (Authentication/API)"
Cohesion: 0.25
Nodes (15): annotation, api, assertions, AuthenticationService, autowired, intstream, list, methodorderer (+7 more)

### Community 6 - "Security config: HTTP+JWT+Encoder (HTTP/BCrypt/JWK)"
Cohesion: 0.09
Nodes (22): bcryptpasswordencoder, bean, customizer, httpsecurity, immutablejwkset, jwk, jwkset, jwksource (+14 more)

### Community 7 - "List properties record (Constructor Binding)"
Cohesion: 0.21
Nodes (8): Override, ListPropertiesRecord, BeforeEach, Order, OrderAnnotation, Test, TestMethodOrder, ListPropertiesRecordTest

### Community 8 - "List properties immutable class"
Cohesion: 0.22
Nodes (7): ListPropertiesImmutable, BeforeEach, Order, OrderAnnotation, Test, TestMethodOrder, ListPropertiesImmutableTest

### Community 9 - "Map properties tests (basic)"
Cohesion: 0.26
Nodes (6): BeforeEach, Order, OrderAnnotation, Test, TestMethodOrder, MapPropertiesTest

### Community 10 - "Application properties record tests"
Cohesion: 0.25
Nodes (7): ApplicationPropertiesRecordTest, ApplicationPropertiesRecord, BeforeEach, Order, OrderAnnotation, Test, TestMethodOrder

### Community 11 - "Map properties record tests"
Cohesion: 0.26
Nodes (6): BeforeEach, Order, OrderAnnotation, Test, TestMethodOrder, MapPropertiesRecordTest

### Community 12 - "Post-generate Groovy helpers (path/files)"
Cohesion: 0.19
Nodes (3): files, path, paths

### Community 13 - "Application properties class tests"
Cohesion: 0.28
Nodes (6): ApplicationPropertiesTest, BeforeEach, Order, OrderAnnotation, Test, TestMethodOrder

### Community 14 - "Maven wrapper (mvnw) shell scripts"
Cohesion: 0.38
Nodes (8): mvnw script, clean(), die(), exec_maven(), hash_string(), set_java_home(), trim(), verbose()

### Community 15 - "Bash: Maven command-line wrappers"
Cohesion: 0.28
Nodes (4): check_empty_switches_in_maven_command_line(), check_maven_build_result(), execute_maven_command_line(), maven_functions.sh script

### Community 16 - "Bash: General UI/file helpers"
Cohesion: 0.32
Nodes (5): check_file_existence(), get_filename_checking_path(), open_ide_manager(), provide_os_path(), general_functions.sh script

### Community 18 - "Bash: Build archetype-post-generate.groovy"
Cohesion: 0.67
Nodes (3): create_archetype_post_generate_groovy(), remove_lines_with_import_keyword(), create_archetype_post_generate.sh script

### Community 28 - "JPA repository annotation"
Cohesion: 0.67
Nodes (3): jparepository, org.springframework.stereotype.Repository, $

## Knowledge Gaps
- **31 isolated node(s):** `config_constants.sh script`, `create_archetype_metadata.sh script`, `create_archetype_post_generate.sh script`, `general_functions.sh script`, `general_functions_for_inputs.sh script` (+26 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 205 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **54 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ApplicationPropertiesTest` connect `Application properties class tests` to `Bean wiring: ConfigurationProperties + Lombok`, `Annotation-driven service layer (Authentication/API)`?**
  _High betweenness centrality (0.096) - this node is a cross-community bridge._
- **Why does `ApplicationProperties` connect `Bean wiring: ConfigurationProperties + Lombok` to `Junit5 testing primitives`, `Application properties class tests`?**
  _High betweenness centrality (0.094) - this node is a cross-community bridge._
- **Why does `$` connect `Generic core helpers (collection/data)` to `Annotation-driven service layer (Authentication/API)`?**
  _High betweenness centrality (0.082) - this node is a cross-community bridge._
- **What connects `config_constants.sh script`, `create_archetype_metadata.sh script`, `create_archetype_post_generate.sh script` to the rest of the system?**
  _31 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Junit5 testing primitives` be split into smaller, more focused modules?**
  _Cohesion score 0.08970099667774087 - nodes in this community are weakly interconnected._
- **Should `Spring config-properties & collections API` be split into smaller, more focused modules?**
  _Cohesion score 0.10241820768136557 - nodes in this community are weakly interconnected._
- **Should `Generic core helpers (collection/data)` be split into smaller, more focused modules?**
  _Cohesion score 0.06386554621848739 - nodes in this community are weakly interconnected._