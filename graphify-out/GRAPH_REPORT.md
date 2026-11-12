# Graph Report - archetype-v2-eda  (2026-11-12)

## Corpus Check
- 277 files · ~239,531 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 126 file(s) not represented in the graph (top: .xml 63, .properties 23, (none) 22)

## Summary
- 1343 nodes · 1737 edges · 197 communities (76 shown, 121 thin omitted)
- Extraction: 96% EXTRACTED · 4% INFERRED · 0% AMBIGUOUS · INFERRED: 67 edges (avg confidence: 0.83)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `00613c68`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- assert
- entity_name
- ApplicationPropertiesValidationTest.java
- Archetype WebService README
- Data Model: Inferência LLM das flags do `mvn archetype:generate`
- speckit.specify command
- MapPropertiesTest
- AuthenticationCredentials
- messagedigest
- Data Model: Native Groovy Composer
- $
- bufferedreader
- Concept: Multi-Fixture Integration-Test Matrix for eda-archetype
- create_new_feature_branch.py
- properties
- ListPropertiesTest
- common.ps1
- Decision: Compositor nativo Groovy — go (Opção B)
- create-new-feature-branch.sh
- ApplicationPropertiesRecordTest
- create-new-feature-branch.ps1
- Tasks: Inferência LLM das flags do `mvn archetype:generate`
- extension.yml - assess extension manifest
- path
- Como localizar e ler o archetype
- auto_commit.py
- 1. Mechanism invocation contract (operator-facing)
- Suspected Code Paths
- ListPropertiesRecordTest
- mvnw
- Quickstart Validation Guide: Inferência LLM das flags do `mvn archetype:generate`
- Como resolver tipos e enums
- Maven archetype build success and catalog XML screenshot
- 2. Propriedades obrigatórias
- Fluxo de execução e verificação
- create-new-feature.ps1
- Archetype Maven Install Log
- Como derivar a entidade do contrato
- Five-Stage Assessment Pipeline (intake -> research -> define -> shape -> decide)
- Spring Initializr Configuration Form
- Como derivar flags de tipos aninhados
- __entity_name__.java
- User Scenarios & Testing *(mandatory)*
- DevSecOps Principle
- MapPropertiesImmutableTest
- Leia este arquivo primeiro
- Bug Assessment: Flag `absolute_path_jsonfile_contract` obsoleta e sem fonte (o contrato JSON já chega inline no prompt da LLM)
- AdminRoutes.java
- ApiRoutes.java
- data
- $
- Basic Project Goal
- files
- noargsconstructor
- Full Project Goal
- Bug Fix: Flag `absolute_path_jsonfile_contract` removida do catálogo (o contrato JSON já chega inline no prompt da LLM)
- Minimal Project Goal
- speckit workflow.yml - Full SDD Cycle
- Implementation Plan: Inferência LLM das flags do `mvn archetype:generate`
- Main Environment Startup Banner
- Checklist Template
- com.eda-archetype:eda-archetype
- ${groupId}:${artifactId}
- Principle IV: Binding Duplo de Configuracao Externalizada
- Principle V: Credenciais Nunca Entram no Repositorio
- Principle I: Modularidade por Chave de Ativacao (NON-NEGOTIABLE)
- Principle II: Convencao Fixa de Nomenclatura para Artefatos de Extensao (NON-NEGOTIABLE)
- Principle III: Pacote por Funcionalidade no Codigo Gerado
- Principle VI: Validar Antes de Culpar o Codigo
- Option B - Custom Groovy/JUnit Driver (rejected)
- Option C - Buy Instead of Build (non-option)
- Create Feature Branch
- Bug Verification: Flag `absolute_path_jsonfile_contract` removida — o contrato JSON já chega inline no prompt da LLM
- Create Feature Branch
- Git Branching Workflow Extension
- Phase 0 Research: Inferência LLM das flags do `mvn archetype:generate`
- jsonslurper
- Auto-Commit Changes
- Specification Quality Checklist: Inferência LLM das flags do `mvn archetype:generate`
- Bug Verification: Contrato JSON dirige a entidade gerada e é persistido no módulo (`src/test/resources/contracts/`)
- Auto-Commit Changes
- groovyclassloader
- Initialize Git Repository
- Detect Git Remote URL
- Validate Feature Branch
- Initialize Git Repository
- Detect Git Remote URL
- Validate Feature Branch
- git-common.ps1
- auto-commit.sh
- initialize-repo.sh
- STATUS — SUPERSEDED
- rule
- Implementation Drift (Recorded 2026-10-12, during `/speckit.implement`)
- groovyresourceloader
- jsonoutput
- xmlslurper
- temporaryfolder
- test
- Como aplicar precedência e validação
- Como materializar e emitir o contrato
- Como relatar proveniência
- $
- Entregáveis e relatórios
- $
- Índice — mapa dos arquivos e ordem de leitura
- Papel e restrições do agente
- $
- $

## God Nodes (most connected - your core abstractions)
1. `$` - 24 edges
2. `Tasks: Inferência LLM das flags do `mvn archetype:generate`` - 21 edges
3. `main()` - 20 edges
4. `AuthenticationCredentials` - 20 edges
5. `ListPropertiesTest` - 19 edges
6. `ApplicationPropertiesValidationTest` - 16 edges
7. `Quickstart Validation Guide: Inferência LLM das flags do `mvn archetype:generate`` - 16 edges
8. `create-new-feature-branch.sh script` - 15 edges
9. `MapPropertiesTest` - 15 edges
10. `MapPropertiesImmutableTest` - 14 edges

## Surprising Connections (you probably didn't know these)
- `defaults-only IT profile goal (clean + verify)` --conceptually_related_to--> `Problem Definition: Inferência LLM das flags do mvn archetype:generate`  [INFERRED]
  src/test/resources/projects/defaults-only/goal.txt → .specify/assessments/llm-flag-inference/problem.md
- `Eda Archetype README` --references--> `Idea Intake: Substituir bash pre-processor por composição nativa Groovy`  [INFERRED]
  README.md → .specify/assessments/groovy-snippet-injection/intake.md
- `Eda Archetype README` --references--> `Decision: Inferência LLM das flags — go (Opção B)`  [INFERRED]
  README.md → .specify/assessments/llm-flag-inference/decision.md
- `Decision: Multi-Fixture Integration-Test Matrix for eda-archetype` --conceptually_related_to--> `Constitution do EDA Archetype (v1.0.0)`  [AMBIGUOUS]
  .specify/assessments/archetype-integration-tests/decision.md → .specify/memory/constitution.md
- `Eda Archetype README` --references--> `Quickstart: Integration Test Matrix`  [EXTRACTED]
  README.md → specs/001-archetype-integration-tests/quickstart.md

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Post-implementation verification loop (implement → converge, then optionally analyze)** — .opencode_commands_speckit_implement_md, .opencode_commands_speckit_converge_md, .opencode_commands_speckit_analyze_md, .opencode_commands_speckit_checklist_md [EXTRACTED 0.95]
- **Composer build-time pipeline: Groovy source + plugin + 3 input folders + SECTIONS manifest** — specs_002_native_groovy_composer_plan_snippet_composer_groovy, specs_002_native_groovy_composer_contracts_composer_contract_groovy_maven_plugin, specs_002_native_groovy_composer_data_model_snippet, specs_002_native_groovy_composer_data_model_section, specs_002_native_groovy_composer_data_model_composer_manifest [EXTRACTED 0.95]
- **Assess commands implement the five-stage pipeline** — _specify_extensions_assess_commands_speckit_assess_intake, _specify_extensions_assess_commands_speckit_assess_research, _specify_extensions_assess_commands_speckit_assess_define, _specify_extensions_assess_commands_speckit_assess_shape, _specify_extensions_assess_commands_speckit_assess_decide [EXTRACTED 1.00]
- **Shared path-safety / URL-trust protocol across all assess.* commands** — .opencode_commands_speckit_assess_intake_md, .opencode_commands_speckit_assess_research_md, .opencode_commands_speckit_assess_define_md, .opencode_commands_speckit_assess_shape_md, .opencode_commands_speckit_assess_decide_md [EXTRACTED 1.00]
- **Bug commands implement the three-stage triage workflow** — _specify_extensions_bug_commands_speckit_bug_assess, _specify_extensions_bug_commands_speckit_bug_fix, _specify_extensions_bug_commands_speckit_bug_test [EXTRACTED 1.00]
- **Six principles form the EDA Archetype constitution** — rationale_principle_modularidade, rationale_principle_nomenclatura, rationale_principle_pacote, rationale_principle_binding_duplo, rationale_principle_credenciais, rationale_principle_validar_antes [EXTRACTED 1.00]
- **groovy-snippet-injection assessment bundle** — .specify_assessments_groovy_snippet_injection_intake_intake, .specify_assessments_groovy_snippet_injection_research_research, .specify_assessments_groovy_snippet_injection_problem_problem, .specify_assessments_groovy_snippet_injection_concept_concept, .specify_assessments_groovy_snippet_injection_decision_decision [EXTRACTED 1.00]
- **llm-flag-inference assessment bundle** — .specify_assessments_llm_flag_inference_intake_intake, .specify_assessments_llm_flag_inference_research_research, .specify_assessments_llm_flag_inference_problem_problem, .specify_assessments_llm_flag_inference_concept_concept, .specify_assessments_llm_flag_inference_decision_decision [EXTRACTED 1.00]
- **spec-kit templates orient to eda-llm-flag-inference** — .specify_templates_spec_template_spec_template, .specify_templates_plan_template_plan_template, .specify_templates_tasks_template_tasks_template, .specify_assessments_llm_flag_inference_concept_concept [EXTRACTED 1.00]
- **API Runtime Container Stack** — src_main_resources_archetype-resources_docker_docker-compose, src_main_resources_archetype-resources_docker_api-compose, src_main_resources_archetype-resources_docker_db-mongo-compose, src_main_resources_archetype-resources_docker_elk-compose, src_main_resources_archetype-resources_docker_nginx-compose [EXTRACTED 1.00]
- **Credentials Protection 3-Tier Defense** — concept_os_encrypted_vault, concept_global_gitignore, concept_protected_properties_folder [EXTRACTED 1.00]
- **ELK Logging Pipeline (Logstash -> Elasticsearch -> Kibana)** — service_logstash, service_elasticsearch, service_kibana [EXTRACTED 1.00]
- **Core Spec-Driven Development chain (specify → clarify → plan → tasks → implement)** — .opencode_commands_speckit_specify_md, .opencode_commands_speckit_clarify_md, .opencode_commands_speckit_plan_md, .opencode_commands_speckit_tasks_md, .opencode_commands_speckit_implement_md [EXTRACTED 1.00]
- **Spec 001 documents: spec, plan, data-model, research, quickstart, checklists, tasks** — specs_001_archetype_integration_tests_spec_specification, specs_001_archetype_integration_tests_plan_implementation_plan, specs_001_archetype_integration_tests_data_model_data_model, specs_001_archetype_integration_tests_research_phase_0_research, specs_001_archetype_integration_tests_quickstart_quickstart, specs_001_archetype_integration_tests_checklists_requirements_requirements_quality_checklist, specs_001_archetype_integration_tests_tasks_tasks [EXTRACTED 1.00]
- **Workflow: run update-local-catalog, then edit archetype-catalog.xml to append new archetype entry** — maven_archetype_plugin_update_local_catalog, build_success_log, archetype_catalog_xml_file, custom_archetype_eda_entry, tutorial_annotation_open_editor, tutorial_annotation_append_list [INFERRED 0.85]
- **Maven Archetype Install and Catalog Lifecycle** — assets_archetype-log_archetype_install_log, assets_archetype-log_1_archetype_catalog_append, assets_archetype-log_2_archetype_catalog_filter [INFERRED 0.85]
- **Spring Boot Project Initialization Flow** — assets_spr_init_config_spring_initializr_form, assets_spr_init_folder_structure_spring_project_layout, assets_spr_init_pom_spring_boot_pom_xml [INFERRED 0.85]

## Communities (197 total, 121 thin omitted)

### Community 1 - "entity_name"
Cohesion: 0.25
Nodes (9): annotation, AuthenticationService, RestController, Service, $, $, pattern, type (+1 more)

### Community 2 - "ApplicationPropertiesValidationTest.java"
Cohesion: 0.08
Nodes (40): after_each_test_method, api, assertdoesnotthrow, assertions, assertthrows, autowired, BeforeAll, bindable (+32 more)

### Community 3 - "Archetype WebService README"
Cohesion: 0.08
Nodes (40): Advanced Multi-layer Logging System, Constructor Binding (Immutable), Credentials Protection Strategy (3-tier), DevSecOps, EhCache, Global GitIgnore Implementation, Grafana (typo 'Graphana' in README), JavaBean Binding (Mutable) (+32 more)

### Community 4 - "Data Model: Inferência LLM das flags do `mvn archetype:generate`"
Cohesion: 0.05
Nodes (41): Cache entry shape, Cache key shape, Contract, Cross-entity invariants, Data Model: Inferência LLM das flags do `mvn archetype:generate`, Definition, Definition, Definition (+33 more)

### Community 5 - "speckit.specify command"
Cohesion: 0.11
Nodes (35): speckit.analyze command, speckit.assess.decide command, speckit.assess.define command, speckit.assess.intake command, speckit.assess.research command, speckit.assess.shape command, speckit.bug.assess command, speckit.bug.fix command (+27 more)

### Community 6 - "MapPropertiesTest"
Cohesion: 0.08
Nodes (21): collections, org.springframework.boot.context.properties.bind.ConstructorBinding, org.springframework.boot.context.properties.ConfigurationProperties, org.springframework.stereotype.Component, MapItem, MapProperties, MapPropertiesImmutable, Override (+13 more)

### Community 7 - "AuthenticationCredentials"
Cohesion: 0.06
Nodes (16): component, configurationproperties, lombok.Getter, lombok.Setter, notempty, org.springframework.validation.annotation.Validated, pattern, ApplicationProperties (+8 more)

### Community 9 - "Data Model: Native Groovy Composer"
Cohesion: 0.11
Nodes (23): Spec Quality Checklist: Native Groovy Composer, Composer Build-Time Contract, Byte-equivalence acceptance criterion, org.codehaus.gmaven:groovy-maven-plugin:2.1.1, Composer Manifest (List<String> SECTIONS), Data Model: Native Groovy Composer, Imported-Block Entity, Section Entity (+15 more)

### Community 10 - "$"
Cohesion: 0.10
Nodes (21): bcryptpasswordencoder, bean, customizer, httpsecurity, immutablejwkset, jwk, jwkset, jwksource (+13 more)

### Community 12 - "Concept: Multi-Fixture Integration-Test Matrix for eda-archetype"
Cohesion: 0.09
Nodes (36): Concept: Multi-Fixture Integration-Test Matrix for eda-archetype, Decision: Multi-Fixture Integration-Test Matrix for eda-archetype, Idea Intake: Comprehensive Integration Test Suite for eda-archetype, Problem Definition: Lack of Regression Coverage Across eda-archetype Flag Combinations, Idea Research: Comprehensive Integration Test Suite for eda-archetype, Constitution do EDA Archetype (v1.0.0), Constitution Template, Apache maven-archetype#1032 precedent (PR merged 2026-09-21; 33 ITs passing) (+28 more)

### Community 13 - "create_new_feature_branch.py"
Cohesion: 0.11
Nodes (39): dataclasses, datetime, importlib_util, json, os, Args, branch_token(), _byte_length() (+31 more)

### Community 14 - "properties"
Cohesion: 0.04
Nodes (45): additionalProperties, type, oneOf, additionalProperties, properties, type, $ref, definitions (+37 more)

### Community 15 - "ListPropertiesTest"
Cohesion: 0.10
Nodes (15): ListObjects, ListProperties, ListPropertiesImmutable, BeforeEach, Order, OrderAnnotation, Test, TestMethodOrder (+7 more)

### Community 16 - "common.ps1"
Cohesion: 0.23
Nodes (13): Find-SpecifyRoot(), Format-SpecKitCommand(), Get-CurrentBranch(), Get-FeaturePathsEnv(), Get-InvokeSeparator(), Get-NormalizedPriority(), Get-Python3Command(), Get-RepoRoot() (+5 more)

### Community 17 - "Decision: Compositor nativo Groovy — go (Opção B)"
Cohesion: 0.33
Nodes (15): Concept: Compositor nativo Groovy para archetype-post-generate.groovy, Decision: Compositor nativo Groovy — go (Opção B), Idea Intake: Substituir bash pre-processor por composição nativa Groovy, Problem Definition: Compositor nativo Groovy para archetype-post-generate.groovy, Idea Research: Substituir bash pre-processor por composição nativa Groovy, Concept: Inferência LLM das flags do mvn archetype:generate, Decision: Inferência LLM das flags — go (Opção B), Idea Intake: Inferência LLM das flags do mvn archetype:generate (+7 more)

### Community 18 - "create-new-feature-branch.sh"
Cohesion: 0.16
Nodes (24): branch_scope_prefix(), branch_token(), build_branch_name(), _byte_length(), check_existing_branches(), clean_branch_name(), extract_feature_num_from_branch(), _extract_highest_number() (+16 more)

### Community 19 - "ApplicationPropertiesRecordTest"
Cohesion: 0.25
Nodes (7): ApplicationPropertiesRecordTest, ApplicationPropertiesRecord, BeforeEach, Order, OrderAnnotation, Test, TestMethodOrder

### Community 20 - "create-new-feature-branch.ps1"
Cohesion: 0.16
Nodes (15): ConvertTo-BranchToken(), ConvertTo-CleanBranchName(), Expand-BranchTemplate(), Get-AppToken(), Get-BranchName(), Get-BranchScopePrefix(), Get-GitAuthorToken(), Get-HighestNumberFromBranches() (+7 more)

### Community 21 - "Tasks: Inferência LLM das flags do `mvn archetype:generate`"
Cohesion: 0.05
Nodes (40): Dependencies & Execution Order, Format: `[ID] [P?] [Story] Description`, Implementation drift recorded in plan.md (see "Implementation Drift" section in plan.md):, Implementation for User Story 1, Implementation for User Story 2, Implementation for User Story 3, Implementation for User Story 4, Implementation for User Story 5 (+32 more)

### Community 22 - "extension.yml - assess extension manifest"
Cohesion: 0.21
Nodes (13): speckit.assess.decide command, speckit.assess.define command, speckit.assess.intake command, speckit.assess.research command, speckit.assess.shape command, extension.yml - assess extension manifest, speckit.bug.assess command, speckit.bug.fix command (+5 more)

### Community 23 - "path"
Cohesion: 0.07
Nodes (12): path, paths, Bug Fix: Contrato JSON dirige a entidade gerada (derivação + allowlist FR-009) e é persistido no módulo (`src/test/resources/contracts/`), Changes, Deviations from Assessment, Diff Highlights, Follow-ups, Local Verification (+4 more)

### Community 24 - "Como localizar e ler o archetype"
Cohesion: 0.13
Nodes (14): 1. Localizar o artefato, 2. Listar e extrair, 3.1 Catálogo consolidado, 3.2 Fragmentos por flag, 3.3 Pós-processador, 3.4 Templates, 3. O que ler, 4. Como interpretar o catálogo (+6 more)

### Community 25 - "auto_commit.py"
Cohesion: 0.10
Nodes (28): pathlib, re, shutil, _find_project_root(), main(), _parse_auto_commit_config(), Path, Git extension: auto_commit.py Automatically commit changes after a Spec Kit… (+20 more)

### Community 26 - "1. Mechanism invocation contract (operator-facing)"
Cohesion: 0.08
Nodes (25): 1. Mechanism invocation contract (operator-facing), 2. External inference script contract (LLM-facing), 3. Cache contract (storage-format), 4. Error code summary, 5. Stability guarantees, Arguments, CLI Contract: Inferência LLM das flags do `mvn archetype:generate`, Eviction (+17 more)

### Community 27 - "Suspected Code Paths"
Cohesion: 0.05
Nodes (36): Alternatives (com trade-offs), Bug Assessment: Contrato JSON no prompt não dirige a entidade gerada (FR-009) **e** o contrato não é persistido no projeto gerado, Documentação — divergente do código, Especificação — lacunas, Files likely to change, Metade B, Metade B — a premissa do relato, verificada como verdadeira, Metade B — bloqueante: o caminho de cache HIT está quebrado (pré-existente, agora alcançável) (+28 more)

### Community 28 - "ListPropertiesRecordTest"
Cohesion: 0.21
Nodes (8): Override, ListPropertiesRecord, BeforeEach, Order, OrderAnnotation, Test, TestMethodOrder, ListPropertiesRecordTest

### Community 29 - "mvnw"
Cohesion: 0.38
Nodes (8): mvnw script, clean(), die(), exec_maven(), hash_string(), set_java_home(), trim(), verbose()

### Community 30 - "Quickstart Validation Guide: Inferência LLM das flags do `mvn archetype:generate`"
Cohesion: 0.12
Nodes (16): How these scenarios map to spec success criteria, Prerequisites, Quickstart Validation Guide: Inferência LLM das flags do `mvn archetype:generate`, Scenario 10 — Spec-kit orientation text references the mechanism (covers FR-015), Scenario 11 — Empty `defaultValue` is rejected at build time (covers FR-018), Scenario 1 — Happy path on all 10 IT profiles (covers SC-001), Scenario 2.1 — Provenance report on stderr (covers FR-020), Scenario 2 — Required inputs missing are rejected (covers SC-005; FR-001, FR-003) (+8 more)

### Community 31 - "Como resolver tipos e enums"
Cohesion: 0.14
Nodes (13): 1.1 Tipos de `java.lang`, 1.2 Tipos do projeto, 1.3 Tipos rejeitados, 1. Tipo declarado, 2. Estimativa pelo valor, 3. Nome do atributo com tipo do projeto, 4.1 Sinais de enum, 4.2 Nome do enum (+5 more)

### Community 32 - "Maven archetype build success and catalog XML screenshot"
Cohesion: 0.46
Nodes (8): archetype-catalog.xml local catalog at C:\Users\SERVIDOR\.m2\repository\archetype-catalog.xml, Maven archetype build success and catalog XML screenshot, BUILD SUCCESS log panel (Total time 3.333 s), Custom archetype XML entry (groupId com.custom-archetype, artifactId eda-archetype, version 0.0.1-SNAPSHOT), PowerShell prompt showing git status (main f1 +1 -3 -12), maven-archetype-plugin:3.3.0:update-local-catalog execution, Tutorial annotation: append new archetype at the end of the list, Tutorial annotation: open file in text editor to view it

### Community 33 - "2. Propriedades obrigatórias"
Cohesion: 0.15
Nodes (12): 1. Regra do `package`, 2. Propriedades obrigatórias, 3. Execução fora do projeto pai, 4. PowerShell, 5. Lembrete final, `enable_custom_entity_attributes`, `enable_folder_src_test_java`, `entity_contract_file_path` (+4 more)

### Community 34 - "Fluxo de execução e verificação"
Cohesion: 0.17
Nodes (11): 1.1 Proveniência, 1.2 Checklist, 1.3 Contrato materializado, 1. Antes de executar, 2. Executar, 3.1 Validar o `package`, 3.2 Validar o contrato copiado, 3.3 Compilar (+3 more)

### Community 36 - "Archetype Maven Install Log"
Cohesion: 0.50
Nodes (5): Archetype Catalog Append Step, archetype-catalog.xml File, Archetype Catalog Filter Selection, Archetype Maven Install Log, Log Level Enablement Matrix

### Community 37 - "Como derivar a entidade do contrato"
Cohesion: 0.17
Nodes (11): 1. Entradas, 2. Nome da entidade, 3. Pacote da entidade, 4.1 Formas aceitas de `attributes`, 4.2 Campos de negócio, 4. Extração dos atributos, 5. Nome do atributo, 6. Lista de atributos (+3 more)

### Community 38 - "Five-Stage Assessment Pipeline (intake -> research -> define -> shape -> decide)"
Cohesion: 0.50
Nodes (4): README - Idea Assessment Pipeline Extension, README - Bug Triage Workflow Extension, Five-Stage Assessment Pipeline (intake -> research -> define -> shape -> decide), Three-Stage Bug Triage Workflow (assess -> fix -> test)

### Community 39 - "Spring Initializr Configuration Form"
Cohesion: 0.67
Nodes (4): Spring Boot Project Metadata Model, Spring Initializr Configuration Form, Spring Boot Project Folder Structure, Spring Boot pom.xml Screenshot

### Community 40 - "Como derivar flags de tipos aninhados"
Cohesion: 0.17
Nodes (11): 1.1 Nome do POJO, 1.2 Campos do POJO, 1.3 Profundidade, 1.4 Objetos inválidos, 1. Objetos aninhados, 2. `contract_nested_type_definitions`, 3. `custom_entity_attribute_imports`, 4. `contract_enum_definitions` (+3 more)

### Community 41 - "__entity_name__.java"
Cohesion: 0.33
Nodes (5): custom_import_trim, entity, generatedvalue, generationtype, id

### Community 42 - "User Scenarios & Testing *(mandatory)*"
Cohesion: 0.12
Nodes (16): Assumptions, Constitution Compliance, Edge Cases, Feature Specification: Inferência LLM das flags do `mvn archetype:generate`, Functional Requirements, Key Entities, Measurable Outcomes, Requirements *(mandatory)* (+8 more)

### Community 43 - "DevSecOps Principle"
Cohesion: 0.67
Nodes (3): DevSecOps Triangle Diagram, DevSecOps Principle, Global Git Ignore Secret Patterns

### Community 44 - "MapPropertiesImmutableTest"
Cohesion: 0.26
Nodes (6): BeforeEach, Order, OrderAnnotation, Test, TestMethodOrder, MapPropertiesImmutableTest

### Community 45 - "Leia este arquivo primeiro"
Cohesion: 0.20
Nodes (9): 1. O que fazer agora, 2. Entradas externas permitidas, 3. Fonte da verdade, 4. Saídas obrigatórias, 5. Proibições, 6. Condições de parada, 7. Checklist rápido, 8. Verificação de integridade do JAR (+1 more)

### Community 46 - "Bug Assessment: Flag `absolute_path_jsonfile_contract` obsoleta e sem fonte (o contrato JSON já chega inline no prompt da LLM)"
Cohesion: 0.20
Nodes (9): Bug Assessment: Flag `absolute_path_jsonfile_contract` obsoleta e sem fonte (o contrato JSON já chega inline no prompt da LLM), Open Questions, Proposed Remediation, Report (verbatim), Reproduction, Risks & Considerations, Root Cause Hypothesis, Suspected Code Paths (+1 more)

### Community 50 - "$"
Cohesion: 0.67
Nodes (3): jparepository, org.springframework.stereotype.Repository, $

### Community 51 - "Basic Project Goal"
Cohesion: 1.00
Nodes (3): Basic Project Goal, Clean Step, Verify Step

### Community 57 - "Full Project Goal"
Cohesion: 1.00
Nodes (3): Full Project Goal, Clean Step, Verify Step

### Community 58 - "Bug Fix: Flag `absolute_path_jsonfile_contract` removida do catálogo (o contrato JSON já chega inline no prompt da LLM)"
Cohesion: 0.15
Nodes (12): Bug Fix: Flag `absolute_path_jsonfile_contract` removida do catálogo (o contrato JSON já chega inline no prompt da LLM), Changes, Comandos não executados (por quê), Código / configuração (fonte da verdade), Deviations from Assessment, Diff Highlights, Documentação, Follow-ups (+4 more)

### Community 59 - "Minimal Project Goal"
Cohesion: 1.00
Nodes (3): Minimal Project Goal, Clean Step, Verify Step

### Community 64 - "Implementation Plan: Inferência LLM das flags do `mvn archetype:generate`"
Cohesion: 0.20
Nodes (10): Complexity Tracking, Constitution Check, Documentation (this feature), Implementation Plan: Inferência LLM das flags do `mvn archetype:generate`, Phase 0 Research, Phase 1 Design, Project Structure, Source Code (repository root) (+2 more)

### Community 95 - "Create Feature Branch"
Cohesion: 0.20
Nodes (9): Branch Name Template, Branch Numbering Mode, Create Feature Branch, Environment Variable Override, Execution, Graceful Degradation, Output, Prerequisites (+1 more)

### Community 107 - "Bug Verification: Flag `absolute_path_jsonfile_contract` removida — o contrato JSON já chega inline no prompt da LLM"
Cohesion: 0.29
Nodes (6): Bug Verification: Flag `absolute_path_jsonfile_contract` removida — o contrato JSON já chega inline no prompt da LLM, Checks Performed, Output Excerpts, Recommendation, Residual Risks, Summary

### Community 108 - "Create Feature Branch"
Cohesion: 0.20
Nodes (9): Branch Name Template, Branch Numbering Mode, Create Feature Branch, Environment Variable Override, Execution, Graceful Degradation, Output, Prerequisites (+1 more)

### Community 109 - "Git Branching Workflow Extension"
Cohesion: 0.20
Nodes (9): Commands, Configuration, Disabling, Git Branching Workflow Extension, Graceful Degradation, Hooks, Installation, Overview (+1 more)

### Community 110 - "Phase 0 Research: Inferência LLM das flags do `mvn archetype:generate`"
Cohesion: 0.22
Nodes (9): Decision 1 — Language for the mechanism: Groovy 4.0.28, Decision 2 — Build-time invocation: `gmaven:2.1.1` goal `execute` in phase `process-resources`, Decision 3 — Flag catalog extraction via XInclude, parsed by `groovy-xml`, Decision 4 — JSON canonicalization: sorted-key serialization, no external library, Decision 5 — Cache storage: single JSONL file outside the repo, atomic rename for concurrency, Decision 6 — LLM inference call: delegated to an external script, not embedded, Notes, Phase 0 Research: Inferência LLM das flags do `mvn archetype:generate` (+1 more)

### Community 112 - "Auto-Commit Changes"
Cohesion: 0.29
Nodes (6): Auto-Commit Changes, Behavior, Commit Message Styles, Configuration, Execution, Graceful Degradation

### Community 113 - "Specification Quality Checklist: Inferência LLM das flags do `mvn archetype:generate`"
Cohesion: 0.33
Nodes (5): Content Quality, Feature Readiness, Notes, Requirement Completeness, Specification Quality Checklist: Inferência LLM das flags do `mvn archetype:generate`

### Community 114 - "Bug Verification: Contrato JSON dirige a entidade gerada e é persistido no módulo (`src/test/resources/contracts/`)"
Cohesion: 0.29
Nodes (6): Bug Verification: Contrato JSON dirige a entidade gerada e é persistido no módulo (`src/test/resources/contracts/`), Checks Performed, Output Excerpts, Recommendation, Residual Risks, Summary

### Community 115 - "Auto-Commit Changes"
Cohesion: 0.29
Nodes (6): Auto-Commit Changes, Behavior, Commit Message Styles, Configuration, Execution, Graceful Degradation

### Community 117 - "Initialize Git Repository"
Cohesion: 0.33
Nodes (5): Customization, Execution, Graceful Degradation, Initialize Git Repository, Output

### Community 118 - "Detect Git Remote URL"
Cohesion: 0.33
Nodes (5): Detect Git Remote URL, Execution, Graceful Degradation, Output, Prerequisites

### Community 119 - "Validate Feature Branch"
Cohesion: 0.33
Nodes (5): Execution, Graceful Degradation, Prerequisites, Validate Feature Branch, Validation Rules

### Community 120 - "Initialize Git Repository"
Cohesion: 0.33
Nodes (5): Customization, Execution, Graceful Degradation, Initialize Git Repository, Output

### Community 121 - "Detect Git Remote URL"
Cohesion: 0.33
Nodes (5): Detect Git Remote URL, Execution, Graceful Degradation, Output, Prerequisites

### Community 122 - "Validate Feature Branch"
Cohesion: 0.33
Nodes (5): Execution, Graceful Degradation, Prerequisites, Validate Feature Branch, Validation Rules

### Community 130 - "STATUS — SUPERSEDED"
Cohesion: 0.40
Nodes (4): Motivo, O que substitui, Sobre estes documentos, STATUS — SUPERSEDED

### Community 136 - "Implementation Drift (Recorded 2026-10-12, during `/speckit.implement`)"
Cohesion: 0.50
Nodes (4): D1. MavenFlagsViaLlmStarter was a single-file self-contained script (superseded 2026-11-04), D2. MavenFlagsViaLlmStarter is NOT wired into `pom.xml` as a build step, D3. Surefire does not auto-run the Groovy unit tests, Implementation Drift (Recorded 2026-10-12, during `/speckit.implement`)

### Community 217 - "Como aplicar precedência e validação"
Cohesion: 0.20
Nodes (9): 1. Precedência, 2. Regra do valor explícito, 3.1 Flags críticas, 3. Validação, 4. Regras de coerência, 5. Flags não definidas no catálogo, 6. Inferência, 7. Conflito (+1 more)

### Community 218 - "Como materializar e emitir o contrato"
Cohesion: 0.20
Nodes (9): 1. Forma canônica, 2. Nome do arquivo, 3. Diretório, 4. Conteúdo do arquivo, 5. Caminho emitido, 6. Cópia no projeto gerado, 7. Validação, 8. Segredos (+1 more)

### Community 219 - "Como relatar proveniência"
Cohesion: 0.20
Nodes (9): 1. Quando mostrar, 2. Quais flags listar, 3. Colunas, 4. Fontes, 5. Notas, 6. Descartadas, 7. Resumo, 8. O que não fazer (+1 more)

### Community 221 - "$"
Cohesion: 0.25
Nodes (8): , collection, crudrepository, grantedauthority, lombok.AllArgsConstructor, optional, $, $

### Community 222 - "Entregáveis e relatórios"
Cohesion: 0.25
Nodes (7): 1. Classificação do módulo, 2. Relatório de tokens, 3. Economia, 4. Resumo final, Central, Entregáveis e relatórios, Secundário

### Community 223 - "$"
Cohesion: 0.29
Nodes (7): org.springframework.stereotype.Service, Repository, $, $, userdetails, userdetailsservice, usernamenotfoundexception

### Community 224 - "Índice — mapa dos arquivos e ordem de leitura"
Cohesion: 0.29
Nodes (6): 1. Arquivos de `utils/`, 2. Leituras técnicas no JAR, 3. Ordem global, 4. Regra de manutenção, 5. Regra de falha, Índice — mapa dos arquivos e ordem de leitura

### Community 225 - "Papel e restrições do agente"
Cohesion: 0.29
Nodes (6): 1. Papel, 2. Restrições, 3. Fonte da verdade, 4. Se faltar uma regra, 5. Se o resultado estiver errado, Papel e restrições do agente

### Community 226 - "$"
Cohesion: 0.33
Nodes (6): collectors, instant, jwtclaimsset, jwtencoder, jwtencoderparameters, $

### Community 231 - "$"
Cohesion: 0.67
Nodes (3): JwtService, org.springframework.security.core.Authentication, $

## Ambiguous Edges - Review These
- `Decision: Multi-Fixture Integration-Test Matrix for eda-archetype` → `Constitution do EDA Archetype (v1.0.0)`  [AMBIGUOUS]
  .specify/assessments/archetype-integration-tests/decision.md · relation: conceptually_related_to

## Knowledge Gaps
- **430 isolated node(s):** `git-common.sh script`, `com.eda-archetype:eda-archetype`, `${groupId}:${artifactId}`, `AdminRoutes`, `ApiRoutes` (+425 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 732 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **121 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **What is the exact relationship between `Decision: Multi-Fixture Integration-Test Matrix for eda-archetype` and `Constitution do EDA Archetype (v1.0.0)`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._
- **Why does `entity_name` connect `entity_name` to `$`, `$`, `$`, `properties`, `$`?**
  _High betweenness centrality (0.027) - this node is a cross-community bridge._
- **Why does `properties` connect `properties` to `entity_name`?**
  _High betweenness centrality (0.021) - this node is a cross-community bridge._
- **Why does `ApplicationPropertiesTest` connect `AuthenticationCredentials` to `ApplicationPropertiesValidationTest.java`?**
  _High betweenness centrality (0.014) - this node is a cross-community bridge._
- **What connects `git-common.sh script`, `com.eda-archetype:eda-archetype`, `${groupId}:${artifactId}` to the rest of the system?**
  _430 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `ApplicationPropertiesValidationTest.java` be split into smaller, more focused modules?**
  _Cohesion score 0.0847457627118644 - nodes in this community are weakly interconnected._
- **Should `Archetype WebService README` be split into smaller, more focused modules?**
  _Cohesion score 0.07564102564102564 - nodes in this community are weakly interconnected._