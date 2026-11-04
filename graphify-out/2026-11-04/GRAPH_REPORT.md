# Graph Report - archetype-v2-eda  (2026-10-13)

## Corpus Check
- 59 files · ~182,569 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 841 nodes · 1288 edges · 108 communities (49 shown, 59 thin omitted)
- Extraction: 93% EXTRACTED · 7% INFERRED · 0% AMBIGUOUS · INFERRED: 93 edges (avg confidence: 0.84)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- LLM Flag Catalog Spock Tests
- ListProperties (Mutable) Archetype Resources
- JUnit 5 / Spock Test Bindings
- Archetype Concepts (Logging, Security, DevSecOps)
- Feature 001 & 003 Spec Documents
- SpecKit OpenCode Commands
- Spring Boot Configuration Properties
- Spring/Lombok Annotations (Core)
- LLM Flag Cache and JSON Canonicalizer
- Feature 002 Spec Documents (Composer)
- Spring Security Beans (BCrypt, JWT)
- LLM Flag Orchestrator and JSON I-O
- SpecKit Assess Documents
- Java Standard Library Collections
- FlagCatalogExtractor and Drift Tests
- ListPropertiesRecord Archetype Resources
- SpecKit PowerShell Common Functions
- Assessments groovy-snippet and llm-flag
- CatalogEntry and FlagProposalMerger
- ApplicationPropertiesRecordTest
- MapPropertiesImmutableTest
- MapPropertiesRecordTest
- SpecKit Assess Extensions Commands
- Archetype Post-Generate Groovy Templates
- ApplicationPropertiesTest
- Authentication UserDetails (Archetype)
- JWT Service (JWT Claims Encoder)
- Feature 001 Plan Research Quickstart
- Orchestrator Contract Gate Test
- Maven Wrapper Functions
- Spring Boot Configuration Annotations
- Archetype Goal Generation defaults-only
- Test Assertions and Verifications
- Spring Boot Maven Plugin and Build
- Pre and Post Generation Hooks
- SnippetComposer Groovy Sections
- Spring Framework Annotations
- Feature 001 Integration Tests Docs
- JPA Hibernate Entity Patterns
- Feature 002 Research and Plan
- JUnit Test Annotations
- Spring Boot Test MockMvc JUnit
- Feature 003 Research and Plan
- Archetype Goal Output defaults-only
- Spring Data Repositories
- Feature 001 Research and Plan
- SpecKit PowerShell Scripts
- Project README and Overview
- SpecKit Templates plan-spec-tasks
- LLM Flag Catalog Data Model
- JSON Utilities Canonicalizer Helpers
- Maven Archetype Configuration
- Spring Boot Configuration Files
- Spring Boot Starter Dependencies
- Feature 003 Tasks and Spec Content
- JUnit Assertions and Test Cases
- Project Tasks and Implementation
- Archetype Generation Metadata
- Property Binding Patterns
- Resource Templates entity-name-based
- JSON Property Tree Walking
- Project Bootstrapping
- Archetype Post-Generation Scripts
- JUnit Test Patterns
- Project Concept Notes
- PowerShell Utility Functions
- Test Data Models Specs
- Repository Implementation
- JUnit 5 Lifecycle Annotations
- Spring Boot Architecture
- Hibernate Validation Annotations
- Configuration Type Bindings
- Data Flow Patterns
- Test Patterns MockMvc
- JUnit Lifecycle Methods
- Test Verification Methods
- Task Templates and Plans
- Spring Boot Components
- User Stories Specs

## God Nodes (most connected - your core abstractions)
1. `$` - 24 edges
2. `AuthenticationCredentials` - 20 edges
3. `ListPropertiesTest` - 19 edges
4. `ApplicationPropertiesValidationTest` - 16 edges
5. `MapPropertiesTest` - 15 edges
6. `MapPropertiesImmutableTest` - 14 edges
7. `MapPropertiesRecordTest` - 14 edges
8. `Concept: Multi-Fixture Integration-Test Matrix for eda-archetype` - 14 edges
9. `ListPropertiesImmutableTest` - 13 edges
10. `ListPropertiesRecordTest` - 13 edges

## Surprising Connections (you probably didn't know these)
- `defaults-only IT profile goal (clean + verify)` --conceptually_related_to--> `Problem Definition: Inferência LLM das flags do mvn archetype:generate`  [INFERRED]
  src/test/resources/projects/defaults-only/goal.txt → .specify/assessments/llm-flag-inference/problem.md
- `Decision: Multi-Fixture Integration-Test Matrix for eda-archetype` --conceptually_related_to--> `Constitution do EDA Archetype (v1.0.0)`  [AMBIGUOUS]
  .specify/assessments/archetype-integration-tests/decision.md → .specify/memory/constitution.md
- `Eda Archetype README` --references--> `Idea Intake: Substituir bash pre-processor por composição nativa Groovy`  [INFERRED]
  README.md → .specify/assessments/groovy-snippet-injection/intake.md
- `Eda Archetype README` --references--> `Decision: Inferência LLM das flags — go (Opção B)`  [INFERRED]
  README.md → .specify/assessments/llm-flag-inference/decision.md
- `Fixture Entity` --semantically_similar_to--> `Snippet Entity`  [INFERRED] [semantically similar]
  specs/001-archetype-integration-tests/data-model.md → specs/002-native-groovy-composer/data-model.md

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Post-implementation verification loop (implement → converge, then optionally analyze)** — .opencode_commands_speckit_implement_md, .opencode_commands_speckit_converge_md, .opencode_commands_speckit_analyze_md, .opencode_commands_speckit_checklist_md [EXTRACTED 0.95]
- **Assess commands implement the five-stage pipeline** — _specify_extensions_assess_commands_speckit_assess_intake, _specify_extensions_assess_commands_speckit_assess_research, _specify_extensions_assess_commands_speckit_assess_define, _specify_extensions_assess_commands_speckit_assess_shape, _specify_extensions_assess_commands_speckit_assess_decide [EXTRACTED 1.00]
- **Shared path-safety / URL-trust protocol across all assess.* commands** — .opencode_commands_speckit_assess_intake_md, .opencode_commands_speckit_assess_research_md, .opencode_commands_speckit_assess_define_md, .opencode_commands_speckit_assess_shape_md, .opencode_commands_speckit_assess_decide_md [EXTRACTED 1.00]
- **Bug commands implement the three-stage triage workflow** — _specify_extensions_bug_commands_speckit_bug_assess, _specify_extensions_bug_commands_speckit_bug_fix, _specify_extensions_bug_commands_speckit_bug_test [EXTRACTED 1.00]
- **Six principles form the EDA Archetype constitution** — rationale_principle_modularidade, rationale_principle_nomenclatura, rationale_principle_pacote, rationale_principle_binding_duplo, rationale_principle_credenciais, rationale_principle_validar_antes [EXTRACTED 1.00]
- **API Runtime Container Stack** — src_main_resources_archetype-resources_docker_docker-compose, src_main_resources_archetype-resources_docker_api-compose, src_main_resources_archetype-resources_docker_db-mongo-compose, src_main_resources_archetype-resources_docker_elk-compose, src_main_resources_archetype-resources_docker_nginx-compose [EXTRACTED 1.00]
- **Credentials Protection 3-Tier Defense** — concept_os_encrypted_vault, concept_global_gitignore, concept_protected_properties_folder [EXTRACTED 1.00]
- **ELK Logging Pipeline (Logstash -> Elasticsearch -> Kibana)** — service_logstash, service_elasticsearch, service_kibana [EXTRACTED 1.00]
- **Core Spec-Driven Development chain (specify → clarify → plan → tasks → implement)** — .opencode_commands_speckit_specify_md, .opencode_commands_speckit_clarify_md, .opencode_commands_speckit_plan_md, .opencode_commands_speckit_tasks_md, .opencode_commands_speckit_implement_md [EXTRACTED 1.00]
- **Workflow: run update-local-catalog, then edit archetype-catalog.xml to append new archetype entry** — maven_archetype_plugin_update_local_catalog, build_success_log, archetype_catalog_xml_file, custom_archetype_eda_entry, tutorial_annotation_open_editor, tutorial_annotation_append_list [INFERRED 0.85]
- **Maven Archetype Install and Catalog Lifecycle** — assets_archetype-log_archetype_install_log, assets_archetype-log_1_archetype_catalog_append, assets_archetype-log_2_archetype_catalog_filter [INFERRED 0.85]
- **Spring Boot Project Initialization Flow** — assets_spr_init_config_spring_initializr_form, assets_spr_init_folder_structure_spring_project_layout, assets_spr_init_pom_spring_boot_pom_xml [INFERRED 0.85]
- **Test Fixture Goal Pattern (clean + verify)** — src_test_resources_projects_basic_goal, src_test_resources_projects_binding_default_only_goal, src_test_resources_projects_binding_list_only_goal, src_test_resources_projects_binding_map_only_goal, src_test_resources_projects_binding_mutable_only_goal, src_test_resources_projects_binding_validated_only_goal, src_test_resources_projects_full_goal, src_test_resources_projects_json_contract_goal, src_test_resources_projects_minimal_goal [INFERRED 0.95]
- **groovy-snippet-injection assessment bundle** — .specify_assessments_groovy_snippet_injection_intake_intake, .specify_assessments_groovy_snippet_injection_research_research, .specify_assessments_groovy_snippet_injection_problem_problem, .specify_assessments_groovy_snippet_injection_concept_concept, .specify_assessments_groovy_snippet_injection_decision_decision [EXTRACTED 1.00]
- **llm-flag-inference assessment bundle** — .specify_assessments_llm_flag_inference_intake_intake, .specify_assessments_llm_flag_inference_research_research, .specify_assessments_llm_flag_inference_problem_problem, .specify_assessments_llm_flag_inference_concept_concept, .specify_assessments_llm_flag_inference_decision_decision [EXTRACTED 1.00]
- **spec-kit templates orient to eda-llm-flag-inference** — .specify_templates_spec_template_spec_template, .specify_templates_plan_template_plan_template, .specify_templates_tasks_template_tasks_template, .specify_assessments_llm_flag_inference_concept_concept [EXTRACTED 1.00]
- **Spec 001 documents: spec, plan, data-model, research, quickstart, checklists, tasks** — specs_001_archetype_integration_tests_spec_specification, specs_001_archetype_integration_tests_plan_implementation_plan, specs_001_archetype_integration_tests_data_model_data_model, specs_001_archetype_integration_tests_research_phase_0_research, specs_001_archetype_integration_tests_quickstart_quickstart, specs_001_archetype_integration_tests_checklists_requirements_requirements_quality_checklist, specs_001_archetype_integration_tests_tasks_tasks [EXTRACTED 1.00]
- **Spec 003 documents: spec, plan, data-model, research, quickstart, contracts, checklists, tasks** — specs_003_llm_flag_inference_spec_specification, specs_003_llm_flag_inference_plan_implementation_plan, specs_003_llm_flag_inference_data_model_data_model, specs_003_llm_flag_inference_research_phase_0_research, specs_003_llm_flag_inference_quickstart_quickstart, specs_003_llm_flag_inference_contracts_cli_contract, specs_003_llm_flag_inference_checklists_requirements_requirements_quality_checklist, specs_003_llm_flag_inference_tasks_tasks [EXTRACTED 1.00]
- **Composer build-time pipeline: Groovy source + plugin + 3 input folders + SECTIONS manifest** — specs_002_native_groovy_composer_plan_snippet_composer_groovy, specs_002_native_groovy_composer_contracts_composer_contract_groovy_maven_plugin, specs_002_native_groovy_composer_data_model_snippet, specs_002_native_groovy_composer_data_model_section, specs_002_native_groovy_composer_data_model_composer_manifest [EXTRACTED 0.95]

## Communities (108 total, 59 thin omitted)

### Community 0 - "LLM Flag Catalog Spock Tests"
Cohesion: 0.08
Nodes (10): assert, rule, CommandLineAssemblerTest, FlagProposalMergerTest, InvocationCachePerfSmokeTest, InvocationCacheTest, OrchestratorEndToEndTest, RunResult (+2 more)

### Community 1 - "ListProperties (Mutable) Archetype Resources"
Cohesion: 0.10
Nodes (15): ListObjects, ListProperties, ListPropertiesImmutable, BeforeEach, Order, OrderAnnotation, Test, TestMethodOrder (+7 more)

### Community 2 - "JUnit 5 / Spock Test Bindings"
Cohesion: 0.09
Nodes (30): after_each_test_method, assertdoesnotthrow, assertthrows, BeforeAll, bindable, binder, bindexception, configurationpropertysource (+22 more)

### Community 3 - "Archetype Concepts (Logging, Security, DevSecOps)"
Cohesion: 0.08
Nodes (40): Advanced Multi-layer Logging System, Constructor Binding (Immutable), Credentials Protection Strategy (3-tier), DevSecOps, EhCache, Global GitIgnore Implementation, Grafana (typo 'Graphana' in README), JavaBean Binding (Mutable) (+32 more)

### Community 4 - "Feature 001 & 003 Spec Documents"
Cohesion: 0.07
Nodes (38): Fixture Entity, Generated Project Entity, Scenario Entity, DEPENDS_ON parent-child flag dependency rule, Spec Quality Checklist: LLM Flag Inference, CLI Contract: LLM Flag Inference, cache.jsonl JSONL cache storage, eda-llm-flag-inference CLI invocation (+30 more)

### Community 5 - "SpecKit OpenCode Commands"
Cohesion: 0.11
Nodes (35): speckit.analyze command, speckit.assess.decide command, speckit.assess.define command, speckit.assess.intake command, speckit.assess.research command, speckit.assess.shape command, speckit.bug.assess command, speckit.bug.fix command (+27 more)

### Community 6 - "Spring Boot Configuration Properties"
Cohesion: 0.11
Nodes (15): collections, org.springframework.boot.context.properties.bind.ConstructorBinding, org.springframework.boot.context.properties.ConfigurationProperties, org.springframework.stereotype.Component, MapItem, MapProperties, MapPropertiesImmutable, Override (+7 more)

### Community 7 - "Spring/Lombok Annotations (Core)"
Cohesion: 0.08
Nodes (10): component, configurationproperties, lombok.Getter, lombok.Setter, notempty, org.springframework.validation.annotation.Validated, pattern, ApplicationProperties (+2 more)

### Community 8 - "LLM Flag Cache and JSON Canonicalizer"
Cohesion: 0.11
Nodes (3): messagedigest, JsonCanonicalizer, JsonCanonicalizerTest

### Community 9 - "Feature 002 Spec Documents (Composer)"
Cohesion: 0.11
Nodes (23): Spec Quality Checklist: Native Groovy Composer, Composer Build-Time Contract, Byte-equivalence acceptance criterion, org.codehaus.gmaven:groovy-maven-plugin:2.1.1, Composer Manifest (List<String> SECTIONS), Data Model: Native Groovy Composer, Imported-Block Entity, Section Entity (+15 more)

### Community 10 - "Spring Security Beans (BCrypt, JWT)"
Cohesion: 0.09
Nodes (22): bcryptpasswordencoder, bean, customizer, httpsecurity, immutablejwkset, jwk, jwkset, jwksource (+14 more)

### Community 11 - "LLM Flag Orchestrator and JSON I-O"
Cohesion: 0.17
Nodes (19): bufferedreader, jsonoutput, jsonslurper, assemble(), canonicalize(), canonicalizeList(), canonicalizeMap(), canonicalizeString() (+11 more)

### Community 12 - "SpecKit Assess Documents"
Cohesion: 0.17
Nodes (20): Concept: Multi-Fixture Integration-Test Matrix for eda-archetype, Decision: Multi-Fixture Integration-Test Matrix for eda-archetype, Idea Intake: Comprehensive Integration Test Suite for eda-archetype, Problem Definition: Lack of Regression Coverage Across eda-archetype Flag Combinations, Idea Research: Comprehensive Integration Test Suite for eda-archetype, Constitution do EDA Archetype (v1.0.0), Constitution Template, Apache maven-archetype#1032 precedent (PR merged 2026-09-21; 33 ITs passing) (+12 more)

### Community 13 - "Java Standard Library Collections"
Cohesion: 0.33
Nodes (9): api, assertions, hashmap, intstream, list, map, methodorderer, org.springframework.boot.test.context.SpringBootTest (+1 more)

### Community 14 - "FlagCatalogExtractor and Drift Tests"
Cohesion: 0.20
Nodes (3): FlagCatalogExtractor, FlagCatalogExtractorDriftTest, FlagCatalogExtractorTest

### Community 15 - "ListPropertiesRecord Archetype Resources"
Cohesion: 0.19
Nodes (8): Override, ListPropertiesRecord, BeforeEach, Order, OrderAnnotation, Test, TestMethodOrder, ListPropertiesRecordTest

### Community 16 - "SpecKit PowerShell Common Functions"
Cohesion: 0.23
Nodes (13): Find-SpecifyRoot(), Format-SpecKitCommand(), Get-CurrentBranch(), Get-FeaturePathsEnv(), Get-InvokeSeparator(), Get-NormalizedPriority(), Get-Python3Command(), Get-RepoRoot() (+5 more)

### Community 17 - "Assessments groovy-snippet and llm-flag"
Cohesion: 0.33
Nodes (15): Concept: Compositor nativo Groovy para archetype-post-generate.groovy, Decision: Compositor nativo Groovy — go (Opção B), Idea Intake: Substituir bash pre-processor por composição nativa Groovy, Problem Definition: Compositor nativo Groovy para archetype-post-generate.groovy, Idea Research: Substituir bash pre-processor por composição nativa Groovy, Concept: Inferência LLM das flags do mvn archetype:generate, Decision: Inferência LLM das flags — go (Opção B), Idea Intake: Inferência LLM das flags do mvn archetype:generate (+7 more)

### Community 18 - "CatalogEntry and FlagProposalMerger"
Cohesion: 0.15
Nodes (3): CatalogEntry, FlagProposalMerger, CatalogEntryTest

### Community 19 - "ApplicationPropertiesRecordTest"
Cohesion: 0.25
Nodes (7): ApplicationPropertiesRecordTest, ApplicationPropertiesRecord, BeforeEach, Order, OrderAnnotation, Test, TestMethodOrder

### Community 20 - "MapPropertiesImmutableTest"
Cohesion: 0.26
Nodes (6): BeforeEach, Order, OrderAnnotation, Test, TestMethodOrder, MapPropertiesImmutableTest

### Community 21 - "MapPropertiesRecordTest"
Cohesion: 0.26
Nodes (6): BeforeEach, Order, OrderAnnotation, Test, TestMethodOrder, MapPropertiesRecordTest

### Community 22 - "SpecKit Assess Extensions Commands"
Cohesion: 0.21
Nodes (13): speckit.assess.decide command, speckit.assess.define command, speckit.assess.intake command, speckit.assess.research command, speckit.assess.shape command, extension.yml - assess extension manifest, speckit.bug.assess command, speckit.bug.fix command (+5 more)

### Community 23 - "Archetype Post-Generate Groovy Templates"
Cohesion: 0.19
Nodes (3): files, path, paths

### Community 24 - "ApplicationPropertiesTest"
Cohesion: 0.28
Nodes (6): ApplicationPropertiesTest, BeforeEach, Order, OrderAnnotation, Test, TestMethodOrder

### Community 25 - "Authentication UserDetails (Archetype)"
Cohesion: 0.18
Nodes (12): collection, grantedauthority, lombok.AllArgsConstructor, org.springframework.stereotype.Service, Repository, $, $, $ (+4 more)

### Community 26 - "JWT Service (JWT Claims Encoder)"
Cohesion: 0.18
Nodes (11): collectors, instant, jwtclaimsset, jwtencoder, jwtencoderparameters, JwtService, org.springframework.security.core.Authentication, $ (+3 more)

### Community 27 - "Feature 001 Plan Research Quickstart"
Cohesion: 0.27
Nodes (12): Spec Quality Checklist: Integration Test Matrix, Data Model: Integration Test Matrix, Implementation Plan: Integration Test Matrix, Quickstart: Integration Test Matrix, maven-archetype-plugin integration-test mechanism, Phase 0 Research: Integration Test Matrix, Feature Specification: Integration Test Matrix, User Story 1: Baseline Smoke Coverage (minimal) (+4 more)

### Community 29 - "Maven Wrapper Functions"
Cohesion: 0.38
Nodes (8): mvnw script, clean(), die(), exec_maven(), hash_string(), set_java_home(), trim(), verbose()

### Community 30 - "Spring Boot Configuration Annotations"
Cohesion: 0.25
Nodes (9): annotation, AuthenticationService, autowired, RestController, Service, $, entity_name, $ (+1 more)

### Community 31 - "Archetype Goal Generation defaults-only"
Cohesion: 0.28
Nodes (4): check_empty_switches_in_maven_command_line(), check_maven_build_result(), execute_maven_command_line(), maven_functions.sh script

### Community 32 - "Test Assertions and Verifications"
Cohesion: 0.46
Nodes (8): archetype-catalog.xml local catalog at C:\Users\SERVIDOR\.m2\repository\archetype-catalog.xml, Maven archetype build success and catalog XML screenshot, BUILD SUCCESS log panel (Total time 3.333 s), Custom archetype XML entry (groupId com.custom-archetype, artifactId eda-archetype, version 0.0.1-SNAPSHOT), PowerShell prompt showing git status (main f1 +1 -3 -12), maven-archetype-plugin:3.3.0:update-local-catalog execution, Tutorial annotation: append new archetype at the end of the list, Tutorial annotation: open file in text editor to view it

### Community 33 - "Spring Boot Maven Plugin and Build"
Cohesion: 0.29
Nodes (6): data, entity, generatedvalue, generationtype, id, noargsconstructor

### Community 36 - "Spring Framework Annotations"
Cohesion: 0.50
Nodes (5): Archetype Catalog Append Step, archetype-catalog.xml File, Archetype Catalog Filter Selection, Archetype Maven Install Log, Log Level Enablement Matrix

### Community 37 - "Feature 001 Integration Tests Docs"
Cohesion: 0.50
Nodes (4): , crudrepository, optional, $

### Community 38 - "JPA Hibernate Entity Patterns"
Cohesion: 0.50
Nodes (4): README - Idea Assessment Pipeline Extension, README - Bug Triage Workflow Extension, Five-Stage Assessment Pipeline (intake -> research -> define -> shape -> decide), Three-Stage Bug Triage Workflow (assess -> fix -> test)

### Community 39 - "Feature 002 Research and Plan"
Cohesion: 0.67
Nodes (4): Spring Boot Project Metadata Model, Spring Initializr Configuration Form, Spring Boot Project Folder Structure, Spring Boot pom.xml Screenshot

### Community 40 - "JUnit Test Annotations"
Cohesion: 0.67
Nodes (3): create_archetype_post_generate_groovy(), remove_lines_with_import_keyword(), create_archetype_post_generate.sh script

### Community 43 - "Archetype Goal Output defaults-only"
Cohesion: 0.67
Nodes (3): DevSecOps Triangle Diagram, DevSecOps Principle, Global Git Ignore Secret Patterns

### Community 50 - "JSON Utilities Canonicalizer Helpers"
Cohesion: 0.67
Nodes (3): jparepository, org.springframework.stereotype.Repository, $

### Community 51 - "Maven Archetype Configuration"
Cohesion: 1.00
Nodes (3): Basic Project Goal, Clean Step, Verify Step

### Community 52 - "Spring Boot Configuration Files"
Cohesion: 1.00
Nodes (3): Binding Default Only Goal, Clean Step, Verify Step

### Community 53 - "Spring Boot Starter Dependencies"
Cohesion: 1.00
Nodes (3): Binding List Only Goal, Clean Step, Verify Step

### Community 54 - "Feature 003 Tasks and Spec Content"
Cohesion: 1.00
Nodes (3): Binding Map Only Goal, Clean Step, Verify Step

### Community 55 - "JUnit Assertions and Test Cases"
Cohesion: 1.00
Nodes (3): Binding Mutable Only Goal, Clean Step, Verify Step

### Community 56 - "Project Tasks and Implementation"
Cohesion: 1.00
Nodes (3): Binding Validated Only Goal, Clean Step, Verify Step

### Community 57 - "Archetype Generation Metadata"
Cohesion: 1.00
Nodes (3): Full Project Goal, Clean Step, Verify Step

### Community 58 - "Property Binding Patterns"
Cohesion: 1.00
Nodes (3): Json Contract Goal, Clean Step, Verify Step

### Community 59 - "Resource Templates entity-name-based"
Cohesion: 1.00
Nodes (3): Minimal Project Goal, Clean Step, Verify Step

## Ambiguous Edges - Review These
- `Decision: Multi-Fixture Integration-Test Matrix for eda-archetype` → `Constitution do EDA Archetype (v1.0.0)`  [AMBIGUOUS]
  .specify/assessments/archetype-integration-tests/decision.md · relation: conceptually_related_to

## Knowledge Gaps
- **65 isolated node(s):** `AdminRoutes`, `ApiRoutes`, `maven_functions.sh script`, `input_processor_functions.sh script`, `create_archetype_post_generate.sh script` (+60 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 293 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **59 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **What is the exact relationship between `Decision: Multi-Fixture Integration-Test Matrix for eda-archetype` and `Constitution do EDA Archetype (v1.0.0)`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._
- **Why does `ApplicationPropertiesTest` connect `ApplicationPropertiesTest` to `Java Standard Library Collections`, `Spring/Lombok Annotations (Core)`?**
  _High betweenness centrality (0.028) - this node is a cross-community bridge._
- **Why does `ApplicationProperties` connect `Spring/Lombok Annotations (Core)` to `ApplicationPropertiesTest`, `JUnit 5 / Spock Test Bindings`?**
  _High betweenness centrality (0.028) - this node is a cross-community bridge._
- **Why does `$` connect `JWT Service (JWT Claims Encoder)` to `Authentication UserDetails (Archetype)`, `Spring Boot Configuration Annotations`?**
  _High betweenness centrality (0.024) - this node is a cross-community bridge._
- **What connects `AdminRoutes`, `ApiRoutes`, `maven_functions.sh script` to the rest of the system?**
  _65 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `LLM Flag Catalog Spock Tests` be split into smaller, more focused modules?**
  _Cohesion score 0.07505285412262157 - nodes in this community are weakly interconnected._
- **Should `ListProperties (Mutable) Archetype Resources` be split into smaller, more focused modules?**
  _Cohesion score 0.10409745293466224 - nodes in this community are weakly interconnected._