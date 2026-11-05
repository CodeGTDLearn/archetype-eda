# Graph Report - archetype-v2-eda  (2026-11-05)

## Corpus Check
- 282 files · ~247,998 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 96 file(s) not represented in the graph (top: .xml 36, (none) 22, .properties 20)

## Summary
- 1157 nodes · 1662 edges · 173 communities (57 shown, 116 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 127 edges (avg confidence: 0.84)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `8d77d105`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- assert
- ListPropertiesTest
- ApplicationPropertiesValidationTest.java
- Archetype WebService README
- Feature Specification: LLM Flag Inference
- speckit.specify command
- MapPropertiesTest
- AuthenticationCredentials
- CacheKeyFactory
- Data Model: Native Groovy Composer
- $
- bufferedreader
- Concept: Multi-Fixture Integration-Test Matrix for eda-archetype
- create_new_feature_branch.py
- MapPropertiesRecordTest
- common.ps1
- Decision: Compositor nativo Groovy — go (Opção B)
- create-new-feature-branch.sh
- ApplicationPropertiesRecordTest
- create-new-feature-branch.ps1
- InferenceApplication.groovy
- extension.yml - assess extension manifest
- path
- ApplicationPropertiesTest
- .run
- MapPropertiesImmutableTest
- Suspected Code Paths
- Bug Fix: Contrato JSON dirige a entidade gerada (derivação + allowlist FR-009) e é persistido no módulo (`src/test/resources/contracts/`)
- trim
- .mergeWithProvenance
- Maven archetype build success and catalog XML screenshot
- create-new-feature.ps1
- Archetype Maven Install Log
- Five-Stage Assessment Pipeline (intake -> research -> define -> shape -> decide)
- Spring Initializr Configuration Form
- .deriveFromContract
- DevSecOps Principle
- $
- Basic Project Goal
- Binding Default Only Goal
- Binding List Only Goal
- Binding Map Only Goal
- Binding Mutable Only Goal
- Binding Validated Only Goal
- Full Project Goal
- Bug Fix: Flag `absolute_path_jsonfile_contract` removida do catálogo (o contrato JSON já chega inline no prompt da LLM)
- Minimal Project Goal
- speckit workflow.yml - Full SDD Cycle
- inference-stub.sh
- Main Environment Startup Banner
- AdminRoutes.java
- ApiRoutes.java
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
- JsonCanonicalizer
- jsonslurper
- Auto-Commit Changes
- .write
- Bug Verification: Contrato JSON dirige a entidade gerada e é persistido no módulo (`src/test/resources/contracts/`)
- Auto-Commit Changes
- MavenFlagsViaLlmStarter.groovy
- Initialize Git Repository
- Detect Git Remote URL
- Validate Feature Branch
- Initialize Git Repository
- Detect Git Remote URL
- Validate Feature Branch
- git-common.ps1
- .collectSecretLikeKeys
- auto-commit.sh
- initialize-repo.sh
- ProvenanceReportRenderer
- rule
- ValidationResult
- TypeResolution.groovy
- jsonoutput
- temporaryfolder
- test
- InferenceConfig

## God Nodes (most connected - your core abstractions)
1. `$` - 24 edges
2. `main()` - 20 edges
3. `AuthenticationCredentials` - 20 edges
4. `ListPropertiesTest` - 19 edges
5. `ApplicationPropertiesValidationTest` - 16 edges
6. `create-new-feature-branch.sh script` - 15 edges
7. `MapPropertiesTest` - 15 edges
8. `MapPropertiesImmutableTest` - 14 edges
9. `MapPropertiesRecordTest` - 14 edges
10. `Concept: Multi-Fixture Integration-Test Matrix for eda-archetype` - 14 edges

## Surprising Connections (you probably didn't know these)
- `Tests Added or Updated` --references--> `FlagCatalogExtractor`  [INFERRED]
  .specify/bugs/flag-contrato-absoluto-obsoleta/fix.md → src/main/pre-processamento-maven-flags-via-llm/catalog/FlagCatalogExtractor.groovy
- `defaults-only IT profile goal (clean + verify)` --conceptually_related_to--> `Problem Definition: Inferência LLM das flags do mvn archetype:generate`  [INFERRED]
  src/test/resources/projects/defaults-only/goal.txt → .specify/assessments/llm-flag-inference/problem.md
- `Eda Archetype README` --references--> `Idea Intake: Substituir bash pre-processor por composição nativa Groovy`  [INFERRED]
  README.md → .specify/assessments/groovy-snippet-injection/intake.md
- `Eda Archetype README` --references--> `Decision: Inferência LLM das flags — go (Opção B)`  [INFERRED]
  README.md → .specify/assessments/llm-flag-inference/decision.md
- `Decision: Multi-Fixture Integration-Test Matrix for eda-archetype` --conceptually_related_to--> `Constitution do EDA Archetype (v1.0.0)`  [AMBIGUOUS]
  .specify/assessments/archetype-integration-tests/decision.md → .specify/memory/constitution.md

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
- **Spec 003 documents: spec, plan, data-model, research, quickstart, contracts, checklists, tasks** — specs_003_llm_flag_inference_spec_specification, specs_003_llm_flag_inference_plan_implementation_plan, specs_003_llm_flag_inference_data_model_data_model, specs_003_llm_flag_inference_research_phase_0_research, specs_003_llm_flag_inference_quickstart_quickstart, specs_003_llm_flag_inference_contracts_cli_contract, specs_003_llm_flag_inference_checklists_requirements_requirements_quality_checklist, specs_003_llm_flag_inference_tasks_tasks [EXTRACTED 1.00]
- **Workflow: run update-local-catalog, then edit archetype-catalog.xml to append new archetype entry** — maven_archetype_plugin_update_local_catalog, build_success_log, archetype_catalog_xml_file, custom_archetype_eda_entry, tutorial_annotation_open_editor, tutorial_annotation_append_list [INFERRED 0.85]
- **Maven Archetype Install and Catalog Lifecycle** — assets_archetype-log_archetype_install_log, assets_archetype-log_1_archetype_catalog_append, assets_archetype-log_2_archetype_catalog_filter [INFERRED 0.85]
- **Spring Boot Project Initialization Flow** — assets_spr_init_config_spring_initializr_form, assets_spr_init_folder_structure_spring_project_layout, assets_spr_init_pom_spring_boot_pom_xml [INFERRED 0.85]

## Communities (173 total, 116 thin omitted)

### Community 1 - "ListPropertiesTest"
Cohesion: 0.10
Nodes (16): ListObjects, ListProperties, Override, ListPropertiesRecord, BeforeEach, Order, OrderAnnotation, Test (+8 more)

### Community 2 - "ApplicationPropertiesValidationTest.java"
Cohesion: 0.05
Nodes (60): after_each_test_method, annotation, api, assertdoesnotthrow, assertions, assertthrows, AuthenticationService, autowired (+52 more)

### Community 3 - "Archetype WebService README"
Cohesion: 0.08
Nodes (40): Advanced Multi-layer Logging System, Constructor Binding (Immutable), Credentials Protection Strategy (3-tier), DevSecOps, EhCache, Global GitIgnore Implementation, Grafana (typo 'Graphana' in README), JavaBean Binding (Mutable) (+32 more)

### Community 4 - "Feature Specification: LLM Flag Inference"
Cohesion: 0.08
Nodes (38): Fixture Entity, Generated Project Entity, Scenario Entity, DEPENDS_ON parent-child flag dependency rule, Spec Quality Checklist: LLM Flag Inference, CLI Contract: LLM Flag Inference, cache.jsonl JSONL cache storage, eda-llm-flag-inference CLI invocation (+30 more)

### Community 5 - "speckit.specify command"
Cohesion: 0.11
Nodes (35): speckit.analyze command, speckit.assess.decide command, speckit.assess.define command, speckit.assess.intake command, speckit.assess.research command, speckit.assess.shape command, speckit.bug.assess command, speckit.bug.fix command (+27 more)

### Community 6 - "MapPropertiesTest"
Cohesion: 0.07
Nodes (22): collections, org.springframework.boot.context.properties.bind.ConstructorBinding, org.springframework.boot.context.properties.ConfigurationProperties, org.springframework.stereotype.Component, ListPropertiesImmutable, MapItem, MapProperties, MapPropertiesImmutable (+14 more)

### Community 7 - "AuthenticationCredentials"
Cohesion: 0.07
Nodes (10): component, configurationproperties, lombok.Getter, lombok.Setter, notempty, org.springframework.validation.annotation.Validated, pattern, ApplicationProperties (+2 more)

### Community 8 - "CacheKeyFactory"
Cohesion: 0.18
Nodes (4): messagedigest, CacheKeyFactory, ContractStore, Sha256

### Community 9 - "Data Model: Native Groovy Composer"
Cohesion: 0.11
Nodes (23): Spec Quality Checklist: Native Groovy Composer, Composer Build-Time Contract, Byte-equivalence acceptance criterion, org.codehaus.gmaven:groovy-maven-plugin:2.1.1, Composer Manifest (List<String> SECTIONS), Data Model: Native Groovy Composer, Imported-Block Entity, Section Entity (+15 more)

### Community 10 - "$"
Cohesion: 0.04
Nodes (44): , bcryptpasswordencoder, bean, collection, collectors, crudrepository, custom_import_trim, customizer (+36 more)

### Community 12 - "Concept: Multi-Fixture Integration-Test Matrix for eda-archetype"
Cohesion: 0.11
Nodes (32): Concept: Multi-Fixture Integration-Test Matrix for eda-archetype, Decision: Multi-Fixture Integration-Test Matrix for eda-archetype, Idea Intake: Comprehensive Integration Test Suite for eda-archetype, Problem Definition: Lack of Regression Coverage Across eda-archetype Flag Combinations, Idea Research: Comprehensive Integration Test Suite for eda-archetype, Constitution do EDA Archetype (v1.0.0), Constitution Template, Apache maven-archetype#1032 precedent (PR merged 2026-09-21; 33 ITs passing) (+24 more)

### Community 13 - "create_new_feature_branch.py"
Cohesion: 0.05
Nodes (67): dataclasses, datetime, importlib_util, json, os, pathlib, re, shutil (+59 more)

### Community 15 - "MapPropertiesRecordTest"
Cohesion: 0.26
Nodes (6): BeforeEach, Order, OrderAnnotation, Test, TestMethodOrder, MapPropertiesRecordTest

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

### Community 21 - "InferenceApplication.groovy"
Cohesion: 0.12
Nodes (4): CommandLineArguments, CommandLineAssembler, ContractFormatException, MaterializationResult

### Community 22 - "extension.yml - assess extension manifest"
Cohesion: 0.21
Nodes (13): speckit.assess.decide command, speckit.assess.define command, speckit.assess.intake command, speckit.assess.research command, speckit.assess.shape command, extension.yml - assess extension manifest, speckit.bug.assess command, speckit.bug.fix command (+5 more)

### Community 23 - "path"
Cohesion: 0.10
Nodes (4): files, path, paths, PostProcessorHarness

### Community 24 - "ApplicationPropertiesTest"
Cohesion: 0.28
Nodes (6): ApplicationPropertiesTest, BeforeEach, Order, OrderAnnotation, Test, TestMethodOrder

### Community 26 - "MapPropertiesImmutableTest"
Cohesion: 0.26
Nodes (6): BeforeEach, Order, OrderAnnotation, Test, TestMethodOrder, MapPropertiesImmutableTest

### Community 27 - "Suspected Code Paths"
Cohesion: 0.04
Nodes (45): Alternatives (com trade-offs), Bug Assessment: Contrato JSON no prompt não dirige a entidade gerada (FR-009) **e** o contrato não é persistido no projeto gerado, Documentação — divergente do código, Especificação — lacunas, Files likely to change, Metade B, Metade B — a premissa do relato, verificada como verdadeira, Metade B — bloqueante: o caminho de cache HIT está quebrado (pré-existente, agora alcançável) (+37 more)

### Community 28 - "Bug Fix: Contrato JSON dirige a entidade gerada (derivação + allowlist FR-009) e é persistido no módulo (`src/test/resources/contracts/`)"
Cohesion: 0.22
Nodes (8): Bug Fix: Contrato JSON dirige a entidade gerada (derivação + allowlist FR-009) e é persistido no módulo (`src/test/resources/contracts/`), Changes, Deviations from Assessment, Diff Highlights, Follow-ups, Local Verification, Summary, Tests Added or Updated

### Community 29 - "trim"
Cohesion: 0.14
Nodes (11): ContractAttributeAssembler, ContractEntityNameResolver, ContractTypeResolver, mvnw script, clean(), die(), exec_maven(), hash_string() (+3 more)

### Community 30 - ".mergeWithProvenance"
Cohesion: 0.11
Nodes (6): CatalogEntry, ContractValueReader, DerivedFlagProjector, FlagProposalMerger, FlagValidationPolicy, InferenceEligibilityPolicy

### Community 32 - "Maven archetype build success and catalog XML screenshot"
Cohesion: 0.46
Nodes (8): archetype-catalog.xml local catalog at C:\Users\SERVIDOR\.m2\repository\archetype-catalog.xml, Maven archetype build success and catalog XML screenshot, BUILD SUCCESS log panel (Total time 3.333 s), Custom archetype XML entry (groupId com.custom-archetype, artifactId eda-archetype, version 0.0.1-SNAPSHOT), PowerShell prompt showing git status (main f1 +1 -3 -12), maven-archetype-plugin:3.3.0:update-local-catalog execution, Tutorial annotation: append new archetype at the end of the list, Tutorial annotation: open file in text editor to view it

### Community 36 - "Archetype Maven Install Log"
Cohesion: 0.50
Nodes (5): Archetype Catalog Append Step, archetype-catalog.xml File, Archetype Catalog Filter Selection, Archetype Maven Install Log, Log Level Enablement Matrix

### Community 38 - "Five-Stage Assessment Pipeline (intake -> research -> define -> shape -> decide)"
Cohesion: 0.50
Nodes (4): README - Idea Assessment Pipeline Extension, README - Bug Triage Workflow Extension, Five-Stage Assessment Pipeline (intake -> research -> define -> shape -> decide), Three-Stage Bug Triage Workflow (assess -> fix -> test)

### Community 39 - "Spring Initializr Configuration Form"
Cohesion: 0.67
Nodes (4): Spring Boot Project Metadata Model, Spring Initializr Configuration Form, Spring Boot Project Folder Structure, Spring Boot pom.xml Screenshot

### Community 42 - ".deriveFromContract"
Cohesion: 0.14
Nodes (5): Tests Added or Updated, FlagCatalogExtractor, ContractEntityDeriver, TypeNameListFormatter, xmlslurper

### Community 43 - "DevSecOps Principle"
Cohesion: 0.67
Nodes (3): DevSecOps Triangle Diagram, DevSecOps Principle, Global Git Ignore Secret Patterns

### Community 50 - "$"
Cohesion: 0.67
Nodes (3): jparepository, org.springframework.stereotype.Repository, $

### Community 51 - "Basic Project Goal"
Cohesion: 1.00
Nodes (3): Basic Project Goal, Clean Step, Verify Step

### Community 52 - "Binding Default Only Goal"
Cohesion: 1.00
Nodes (3): Binding Default Only Goal, Clean Step, Verify Step

### Community 53 - "Binding List Only Goal"
Cohesion: 1.00
Nodes (3): Binding List Only Goal, Clean Step, Verify Step

### Community 54 - "Binding Map Only Goal"
Cohesion: 1.00
Nodes (3): Binding Map Only Goal, Clean Step, Verify Step

### Community 55 - "Binding Mutable Only Goal"
Cohesion: 1.00
Nodes (3): Binding Mutable Only Goal, Clean Step, Verify Step

### Community 56 - "Binding Validated Only Goal"
Cohesion: 1.00
Nodes (3): Binding Validated Only Goal, Clean Step, Verify Step

### Community 57 - "Full Project Goal"
Cohesion: 1.00
Nodes (3): Full Project Goal, Clean Step, Verify Step

### Community 58 - "Bug Fix: Flag `absolute_path_jsonfile_contract` removida do catálogo (o contrato JSON já chega inline no prompt da LLM)"
Cohesion: 0.17
Nodes (11): Bug Fix: Flag `absolute_path_jsonfile_contract` removida do catálogo (o contrato JSON já chega inline no prompt da LLM), Changes, Comandos não executados (por quê), Código / configuração (fonte da verdade), Deviations from Assessment, Diff Highlights, Documentação, Follow-ups (+3 more)

### Community 59 - "Minimal Project Goal"
Cohesion: 1.00
Nodes (3): Minimal Project Goal, Clean Step, Verify Step

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

### Community 111 - "jsonslurper"
Cohesion: 0.22
Nodes (3): jsonslurper, ContractReader, InferenceClient

### Community 112 - "Auto-Commit Changes"
Cohesion: 0.29
Nodes (6): Auto-Commit Changes, Behavior, Commit Message Styles, Configuration, Execution, Graceful Degradation

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

## Ambiguous Edges - Review These
- `Decision: Multi-Fixture Integration-Test Matrix for eda-archetype` → `Constitution do EDA Archetype (v1.0.0)`  [AMBIGUOUS]
  .specify/assessments/archetype-integration-tests/decision.md · relation: conceptually_related_to

## Knowledge Gaps
- **171 isolated node(s):** `git-common.sh script`, `com.eda-archetype:eda-archetype`, `inference-stub.sh script`, `TypeResolution`, `${groupId}:${artifactId}` (+166 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 486 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **116 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **What is the exact relationship between `Decision: Multi-Fixture Integration-Test Matrix for eda-archetype` and `Constitution do EDA Archetype (v1.0.0)`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._
- **Why does `ApplicationPropertiesTest` connect `ApplicationPropertiesTest` to `ApplicationPropertiesValidationTest.java`, `AuthenticationCredentials`?**
  _High betweenness centrality (0.016) - this node is a cross-community bridge._
- **Why does `MapPropertiesRecordTest` connect `MapPropertiesRecordTest` to `ApplicationPropertiesValidationTest.java`, `MapPropertiesTest`?**
  _High betweenness centrality (0.014) - this node is a cross-community bridge._
- **Why does `ApplicationProperties` connect `AuthenticationCredentials` to `ApplicationPropertiesTest`, `ApplicationPropertiesValidationTest.java`?**
  _High betweenness centrality (0.010) - this node is a cross-community bridge._
- **What connects `git-common.sh script`, `com.eda-archetype:eda-archetype`, `inference-stub.sh script` to the rest of the system?**
  _171 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `ListPropertiesTest` be split into smaller, more focused modules?**
  _Cohesion score 0.10409745293466224 - nodes in this community are weakly interconnected._
- **Should `ApplicationPropertiesValidationTest.java` be split into smaller, more focused modules?**
  _Cohesion score 0.054746835443037975 - nodes in this community are weakly interconnected._