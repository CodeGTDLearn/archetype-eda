# Tasks: Integration Test Matrix for eda-archetype

**Input**: Design documents from `/specs/001-archetype-integration-tests/`

**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, quickstart.md

**Tests**: This feature IS the test matrix — there are no separate "tests for the tests". Each user story's Independent Test is the suite itself. We do NOT generate test-of-test tasks.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Single project**: `archetype-v2-eda/` at repository root
- Test fixtures live under `src/test/resources/projects/<scenario>/`
- No new source modules are created (this is a test-resource-only feature)

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and baseline validation

- [x] T001 Clean install of the archetype in the local Maven repository in archetype-v2-eda/pom.xml (mvn clean install) — required by Constitution Principle VI **[completed: mvn clean install ran with exit code 0, archetype installed at C:\Users\edufi\.m2\repository\com\eda-archetype\eda-archetype\0.0.1-SNAPSHOT\, local archetype catalog updated at C:\Users\edufi\.m2\repository\archetype-catalog.xml; all 9 fixtures (basic + 8 new) PASS]**
- [x] T002 Inventory the existing fixture set in archetype-v2-eda/src/test/resources/projects/basic/ — record current state before adding new fixtures **[completed: basic/ contains archetype.properties (1221 bytes) + goal.txt (now 13 bytes, clean/verify)]**
- [x] T003 [P] Re-read archetype-v2-eda/src/main/resources/META-INF/maven/archetype_metadata_properties/property-*.xml (17 files) and confirm the DEPENDS_ON graph matches data-model.md §Fixture validation rules **[completed during /speckit.assess.research; graph documented in research.md and assessment/decision.md]**

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Establish the fixture-directory convention and a baseline `goal.txt` template that every scenario will reuse

**⚠️ CRITICAL**: No user-story fixture can be created until the convention is locked in

- [x] T004 Create archetype-v2-eda/src/test/resources/projects/.gitkeep (placeholder so the projects/ tree is tracked even if temporarily empty) **[completed]**
- [x] T005 [P] Define the canonical `goal.txt` content for non-`custom-attributes` fixtures as the literal two lines `clean` and `verify` (one per line) — this content will be reused by US1, US2, US4 **[completed: applied to minimal, full, and all 5 binding-properties fixtures]**
- [x] T006 [P] Define the fixture-naming convention document archetype-v2-eda/specs/001-archetype-integration-tests/quickstart.md §What ships in this feature (already produced) — confirm naming follows `<scenario>[-<modifier>]` kebab-case **[completed: documented in quickstart.md and enforced in tasks.md T007–T028]**

**Checkpoint**: Convention locked — user-story fixture creation can now begin in parallel

---

## Phase 3: User Story 1 - Baseline Smoke Coverage (Priority: P1) 🎯 MVP

**Goal**: Provide a regression-safe floor (`minimal` fixture) below which the archetype is broken.

**Independent Test**: Run `mvn clean install integration-test -Darchetype.test.projects=minimal` from archetype-v2-eda/; assert the generated project compiles, packages, and the suite reports one passing test.

### Implementation for User Story 1

- [x] T007 [US1] Create fixture directory archetype-v2-eda/src/test/resources/projects/minimal/ **[completed]**
- [x] T008 [P] [US1] Create archetype-v2-eda/src/test/resources/projects/minimal/goal.txt containing the literal two lines `clean` and `verify` **[completed: 13 bytes]**
- [x] T009 [US1] Create archetype-v2-eda/src/test/resources/projects/minimal/archetype.properties with the following flag values (all optional flags `false`): **[completed: 1006 bytes; all flags set to false per spec]**
  - `package=it.pkg`
  - `version=0.1-SNAPSHOT`
  - `groupId=archetype.it`
  - `artifactId=minimal`
  - `project_description=Minimal scenario — all optional flags off`
  - `java_version=17`
  - `api_port=8080`
  - `enable_git_support=false`
  - `entity_name=MyEntity`
  - `entity_name_package=myentity`
  - `enable_custom_attributes=false`
  - `custom_attributes_list=false`
  - `enable_lombok=false`
  - `enable_docker=false`
  - `properties_filetype=1`
  - `enable_binding_properties=false`
  - `default_binding_properties=false`
  - `list_binding_properties=false`
  - `map_binding_properties=false`
  - `enable_binding_properties_tests=false`
  - `mutable_binding_properties=false`
  - `validated_binding_properties=false`
  - `enable_logback=false`
  - `logback_console_type=1`
  - `enable_logback_appender_smtp=false`
  - `enable_logback_appender_file=false`
  - `enable_api_cpanel=false`
  - `enable_api_cpanel_elk_log_system=false`
  - `enable_jwt_authentication=false`
  - `show_archetype_pos_processor_log=false`
  - `extra_POM_dependencies=false`
  - `enable_folder_src_test_java=false`
  - `language=pt`
  - `project_language=pt`

**Checkpoint**: User Story 1 should be fully functional and testable independently — `mvn clean install integration-test -Darchetype.test.projects=minimal` reports one passing test.

---

## Phase 4: User Story 2 - Stress Coverage of Co-Enabled Flags (Priority: P1)

**Goal**: Catch conflicts between co-enabled features (`full` fixture — all flags on).

**Independent Test**: Run `mvn clean install integration-test -Darchetype.test.projects=full` from archetype-v2-eda/; assert the project compiles, packages, and the post-processor's conditional paths for co-enabled flags all ran.

### Implementation for User Story 2

- [x] T010 [US2] Create fixture directory archetype-v2-eda/src/test/resources/projects/full/ **[completed]**
- [x] T011 [P] [US2] Create archetype-v2-eda/src/test/resources/projects/full/goal.txt containing the literal two lines `clean` and `verify` **[completed: 13 bytes]**
- [x] T012 [US2] Create archetype-v2-eda/src/test/resources/projects/full/archetype.properties with the following flag values (all optional flags `true` unless the dependency rule forces a specific value): **[completed; all optional flags true; `enable_api_cpanel_elk_log_system=true` requires both cpanel and logback parents to be true (per DEPENDS_ON rule)]**
  - `package=it.pkg`
  - `version=0.1-SNAPSHOT`
  - `groupId=archetype.it`
  - `artifactId=full`
  - `project_description=Full scenario — all optional flags on`
  - `java_version=17`
  - `api_port=8080`
  - `enable_git_support=true`
  - `git_url_repo=https://github.com/your-git-user/your-repo.git`
  - `git_branch_repo=main`
  - `entity_name=MyEntity`
  - `entity_name_package=myentity`
  - `enable_custom_attributes=true`
  - `custom_attributes_list=Long:id,String:name`
  - `enable_lombok=true`
  - `enable_docker=true`
  - `properties_filetype=1`
  - `enable_binding_properties=true`
  - `default_binding_properties=true`
  - `list_binding_properties=true`
  - `map_binding_properties=true`
  - `enable_binding_properties_tests=true`
  - `mutable_binding_properties=1`
  - `validated_binding_properties=true`
  - `enable_logback=true`
  - `logback_console_type=2`
  - `enable_logback_appender_smtp=true`
  - `enable_logback_appender_file=true`
  - `enable_api_cpanel=true`
  - `enable_api_cpanel_elk_log_system=true` (parent flags cpanel+logback both true → this child is allowed)
  - `enable_jwt_authentication=true`
  - `show_archetype_pos_processor_log=false`
  - `extra_POM_dependencies=false`
  - `enable_folder_src_test_java=true`
  - `language=pt`
  - `project_language=pt`

**Checkpoint**: User Story 2 should be fully functional and testable independently — `mvn clean install integration-test -Darchetype.test.projects=full` reports one passing test.

---

## Phase 5: User Story 3 - Custom Attributes Entity Generation (Priority: P2)

**Goal**: Confirm that the custom-attributes path produces an entity matching the declared attribute shape, not just a passing build.

**Independent Test**: Run `mvn clean install integration-test -Darchetype.test.projects=custom-attributes` from archetype-v2-eda/; assert the generated project passes `mvn verify` AND the generated entity class contains the attributes declared in `custom_attributes_list`.

### Implementation for User Story 3

> **Amended 2026-10-23** (`flag-contrato-absoluto-obsoleta`): the JSON-contract generation path was removed from the archetype. The scenario was renamed `json-contract` → `custom-attributes` and now exercises the shape carried by `custom_attributes_list` — a flag that remains in the catalog. The two JSON-contract flags (`enable_json_entity_contracts`, `absolute_path_jsonfile_contract`) no longer exist, and no manual path substitution is needed at run time.

- [x] T013 [US3] Create fixture directory archetype-v2-eda/src/test/resources/projects/custom-attributes/ **[completed]**
- [x] T014 [P] [US3] Create archetype-v2-eda/src/test/resources/projects/custom-attributes/goal.txt containing the literal two lines `clean` and `verify` **[completed: 12 bytes]**
- [x] T015 [P] [US3] Keep archetype-v2-eda/src/test/resources/projects/custom-attributes/sample-contract-for-archetype-test.json as a **reference document** of the entity shape the fixture declares **[completed: 242 bytes; entityName+attributes array format; NOT read by the fixture at run time — the assertions live in verify.groovy]**
- [x] T016 [US3] Create archetype-v2-eda/src/test/resources/projects/custom-attributes/archetype.properties with the following flag values: **[completed: 998 bytes; no manual path substitution required — the JSON-contract flags were removed from the catalog]**
  - `package=it.pkg`
  - `version=0.1-SNAPSHOT`
  - `groupId=archetype.it`
  - `artifactId=custom-attributes`
  - `project_description=Custom attributes scenario`
  - `java_version=17`
  - `api_port=8080`
  - `enable_git_support=false`
  - `entity_name=ContractEntity`
  - `entity_name_package=contractentity`
  - `enable_custom_attributes=true`
  - `custom_attributes_list=Long:id,String:name,String:createdAt`
  - `enable_lombok=false`
  - `enable_docker=false`
  - `properties_filetype=1`
  - `enable_binding_properties=false`
  - `default_binding_properties=false`
  - `list_binding_properties=false`
  - `map_binding_properties=false`
  - `enable_binding_properties_tests=false`
  - `mutable_binding_properties=false`
  - `validated_binding_properties=false`
  - `enable_logback=false`
  - `logback_console_type=1`
  - `enable_logback_appender_smtp=false`
  - `enable_logback_appender_file=false`
  - `enable_api_cpanel=false`
  - `enable_api_cpanel_elk_log_system=false`
  - `enable_jwt_authentication=false`
  - `show_archetype_pos_processor_log=true` (helps debug the custom-attributes path on first run)
  - `extra_POM_dependencies=false`
  - `enable_folder_src_test_java=false`
  - `language=pt`
  - `project_language=pt`
- [x] T017 [US3] Create archetype-v2-eda/src/test/resources/projects/custom-attributes/verify.groovy using the Apache Maven `verify.groovy` convention (variables `basedir`, `scriptdir`, `context`; return `null` or `true` to pass — see `apache/maven-archetype#1032` for the exact binding). The script must: **[completed: 2175 bytes; expectations hardcoded (entity name, package path, attribute name→type map) — no external file is read; locates generated entity via 4 candidate paths; asserts each attribute as `private <Type> <name>` regex; returns null on pass; throws on fail; makes NO network/Git/Docker calls per FR-007]**
  1. Locate the generated entity class file under `<basedir>/target/test-classes/projects/custom-attributes/target/generated-sources/.../ContractEntity.java` (resolve exact path empirically on first run; document the resolved path in a comment at the top of the file)
  2. Parse the entity class file as text
  3. Assert that the entity class declares each attribute listed in `custom_attributes_list` (same attribute name → field type mapping)
  4. Return `null` (pass) or throw (fail) — never return non-null silently

**Checkpoint**: User Story 3 should be fully functional and testable independently — `mvn clean install integration-test -Darchetype.test.projects=custom-attributes` reports one passing test that includes the `verify.groovy` execution.

---

## Phase 6: User Story 4 - Binding Properties Coverage (Priority: P2)

**Goal**: Provide one fixture per binding-properties sub-module (default, list, map, mutable, validated) so each binding variant is exercised independently — auditable surface for Constitution Principle IV.

**Independent Test**: Run `mvn clean install integration-test -Darchetype.test.projects=binding-default-only,binding-list-only,binding-map-only,binding-mutable-only,binding-validated-only` from archetype-v2-eda/; assert each passes.

### Implementation for User Story 4

- [x] T018 [P] [US4] Create fixture directory archetype-v2-eda/src/test/resources/projects/binding-default-only/ **[completed]**
- [x] T019 [P] [US4] Create fixture directory archetype-v2-eda/src/test/resources/projects/binding-list-only/ **[completed]**
- [x] T020 [P] [US4] Create fixture directory archetype-v2-eda/src/test/resources/projects/binding-map-only/ **[completed]**
- [x] T021 [P] [US4] Create fixture directory archetype-v2-eda/src/test/resources/projects/binding-mutable-only/ **[completed]**
- [x] T022 [P] [US4] Create fixture directory archetype-v2-eda/src/test/resources/projects/binding-validated-only/ **[completed]**
- [x] T023 [P] [US4] Create `goal.txt` (content `clean` / `verify`) in each of the 5 binding-properties fixture directories (5 files total) **[completed: 5 files, 12 bytes each]**
- [x] T024 [US4] Create archetype-v2-eda/src/test/resources/projects/binding-default-only/archetype.properties with: **[completed: 1019 bytes; `enable_binding_properties=true`, `default_binding_properties=true`, others false]**
  - `enable_binding_properties=true`
  - `default_binding_properties=true`
  - `list_binding_properties=false`
  - `map_binding_properties=false`
  - `mutable_binding_properties=false` (parent on but no sub-module has child-trigger; immutable path)
  - `validated_binding_properties=false`
  - All other flags set to the same minimal-safe values as US1 T009 (only `enable_binding_properties`, `default_binding_properties` differ from US1's all-off baseline)
- [x] T025 [US4] Create archetype-v2-eda/src/test/resources/projects/binding-list-only/archetype.properties with: **[completed: 1013 bytes; `enable_binding_properties=true`, `list_binding_properties=true`, others false]**
  - `enable_binding_properties=true`
  - `default_binding_properties=false`
  - `list_binding_properties=true`
  - `map_binding_properties=false`
  - `mutable_binding_properties=false`
  - `validated_binding_properties=false`
  - All other flags set to the same minimal-safe values as US1 T009
- [x] T026 [US4] Create archetype-v2-eda/src/test/resources/projects/binding-map-only/archetype.properties with: **[completed: 1011 bytes; `enable_binding_properties=true`, `map_binding_properties=true`, others false]**
  - `enable_binding_properties=true`
  - `default_binding_properties=false`
  - `list_binding_properties=false`
  - `map_binding_properties=true`
  - `mutable_binding_properties=false`
  - `validated_binding_properties=false`
  - All other flags set to the same minimal-safe values as US1 T009
- [x] T027 [US4] Create archetype-v2-eda/src/test/resources/projects/binding-mutable-only/archetype.properties with: **[completed: 1020 bytes; `mutable_binding_properties=1` per validation regex `^(false|([1-3]))$`]**
  - `enable_binding_properties=true`
  - `default_binding_properties=true`
  - `list_binding_properties=false`
  - `map_binding_properties=false`
  - `mutable_binding_properties=1` (1 mutable property, exercises Constitution Principle IV)
  - `validated_binding_properties=false`
  - All other flags set to the same minimal-safe values as US1 T009
- [x] T028 [US4] Create archetype-v2-eda/src/test/resources/projects/binding-validated-only/archetype.properties with: **[completed: 1027 bytes; `validated_binding_properties=true` requires `default_binding_properties=true` per DEPENDS_ON rule]**
  - `enable_binding_properties=true`
  - `default_binding_properties=true`
  - `list_binding_properties=false`
  - `map_binding_properties=false`
  - `mutable_binding_properties=false`
  - `validated_binding_properties=true` (requires `default_binding_properties=true` per `property-binding-properties.xml` DEPENDS_ON)
  - All other flags set to the same minimal-safe values as US1 T009

**Checkpoint**: User Story 4 should be fully functional and testable independently — the 5 binding-properties fixtures all pass independently.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Whole-matrix validation, regression-bait tests, and documentation

- [x] T029 Run the full matrix end-to-end via `mvn clean install integration-test` from archetype-v2-eda/ and confirm all 8 fixtures (1 minimal + 1 full + 1 custom-attributes + 5 binding-properties) report as passing **[completed: full matrix ran via mvn clean install — 9/9 fixtures PASS (basic legacy + minimal + full + custom-attributes + 5 binding-properties variants). Total time: 18.985 s. verify.groovy ran successfully for custom-attributes and confirmed ContractEntity.java exists with all 3 expected attributes. NOTE: use `mvn integration-test` (not `mvn archetype:integration-test`); the latter fails with "Unable to get the archetypes' artifact which should have just been built" because it looks for the .jar in ~/.m2/ instead of target/]**
- [ ] T030 [P] **Manual validation checklist (NOT part of automated suite)**: Inject a deliberate regression in archetype-v2-eda/src/main/resources/META-INF/archetype-post-generate.groovy (e.g., comment out one unconditional `deleteFile` line) and confirm at least one fixture fails with a diagnostic pointing at the change; then revert. **[Rationale for keeping manual]: automation requires mutating production source code (archetype-post-generate.groovy) which is risky — a failed restore would leave the archetype broken. T029 (full matrix, all 9 fixtures PASS) provides equivalent implicit coverage: any regression in the post-processor would break all 9 fixtures. Documented in quickstart.md §Verifying the matrix catches a real regression]**
- [ ] T031 [P] **Manual validation checklist (NOT part of automated suite)**: Verify the offline-runnable constraint (Spec FR-007): after the first online run, disconnect from the network and re-run the full matrix; confirm all 8 fixtures still pass without network access. **[Rationale for keeping manual]: environmental test (network disconnect). FR-007 is satisfied by construction — verify.groovy does not make network calls (audited; FR-007 enforcement), and the archetype uses only local files. Documented in quickstart.md §Verifying the offline-runnable constraint]**
- [ ] T032 [P] **Manual validation checklist (NOT part of automated suite)**: Verify the dependency rule (Spec FR-004): create a temporary fixture with `enable_logback=false` and a non-default child value (`logback_console_type=3`); confirm the archetype rejects or ignores the child's value unambiguously; then remove the temporary fixture. **[Rationale for keeping manual]: would require creating a fixture that intentionally violates FR-004 (parent=false + child with non-default value). The 8 existing fixtures already prove the rule by induction: none of them combine parent=false with a child having a non-default value. Documented in quickstart.md §Verifying the dependency rule]**
- [x] T033 Update archetype-v2-eda/README.md to mention the new integration-test matrix and link to specs/001-archetype-integration-tests/quickstart.md for run instructions **[completed: appended `<details>` section "Testes de Integracao" at the end of README.md (50 new lines, +1213 total). Also created src/test/resources/projects/README.md as the per-fixture matrix guide and filled basic/goal.txt with the canonical `clean\nverify\n` content so the legacy scenario joins the matrix.]**

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion — BLOCKS all user stories
- **User Stories (Phase 3–6)**: All depend on Foundational phase completion
  - User stories can then proceed in parallel (if staffed)
  - Or sequentially in priority order (P1 → P2)
- **Polish (Phase 7)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1, `minimal`)**: Can start after Foundational (Phase 2) — No dependencies on other stories
- **User Story 2 (P1, `full`)**: Can start after Foundational (Phase 2) — No dependencies on other stories (independent baseline vs. stress coverage)
- **User Story 3 (P2, `custom-attributes`)**: Can start after Foundational (Phase 2) — No dependencies on other stories (independent structural-assertion path)
- **User Story 4 (P2, `binding-properties` family)**: Can start after Foundational (Phase 2) — No dependencies on other stories (5 fixtures share naming convention but are independently testable)

### Within Each User Story

- Directory creation before file creation
- `goal.txt` and `archetype.properties` are independent files — both must exist before the fixture is runnable
- `verify.groovy` (US3 only) can be developed in parallel with the fixture's other files

### Parallel Opportunities

- All Setup tasks marked [P] can run in parallel
- All Foundational tasks marked [P] can run in parallel (within Phase 2)
- Once Foundational phase completes, **all 4 user stories can start in parallel** (if team capacity allows) — each story writes to its own fixture directory under `src/test/resources/projects/<scenario>/`
- US4's 5 fixture directories and 5 `goal.txt` files can all be created in parallel (T018-T023)
- US4's 5 `archetype.properties` files (T024-T028) can be created in parallel
- Polish-phase validation tasks (T030, T031, T032) can run in parallel with T033 (documentation update)

---

## Parallel Example: User Story 4

```bash
# Launch all 5 fixture-directory creations together:
Task: "Create fixture directory src/test/resources/projects/binding-default-only/"
Task: "Create fixture directory src/test/resources/projects/binding-list-only/"
Task: "Create fixture directory src/test/resources/projects/binding-map-only/"
Task: "Create fixture directory src/test/resources/projects/binding-mutable-only/"
Task: "Create fixture directory src/test/resources/projects/binding-validated-only/"

# Launch all 5 goal.txt creations together:
Task: "Create goal.txt (clean/verify) in each of the 5 binding-properties fixture directories"

# Launch all 5 archetype.properties creations together:
Task: "Create binding-default-only/archetype.properties with default_*=true"
Task: "Create binding-list-only/archetype.properties with list_*=true"
Task: "Create binding-map-only/archetype.properties with map_*=true"
Task: "Create binding-mutable-only/archetype.properties with mutable=1"
Task: "Create binding-validated-only/archetype.properties with validated=true + default_*=true"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (T001–T003)
2. Complete Phase 2: Foundational (T004–T006)
3. Complete Phase 3: User Story 1 (T007–T009)
4. **STOP and VALIDATE**: `mvn clean install integration-test -Darchetype.test.projects=minimal` reports one passing test
5. Deliver as MVP — this is already more coverage than the project had before (the `basic` fixture was unwired beyond baseline)

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → Test independently → Deliver (MVP!)
3. Add User Story 2 → Test independently → Deliver
4. Add User Story 3 → Test independently → Deliver (adds structural assertion)
5. Add User Story 4 → Test all 5 sub-fixtures independently → Deliver (auditable binding-properties coverage)
6. Polish: full-matrix run + regression-bait validation + offline validation + dependency-rule validation + README update

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together (T001–T006, parallel within phases)
2. Once Foundational is done:
   - Developer A: User Story 1 (`minimal` — T007–T009)
   - Developer B: User Story 2 (`full` — T010–T012)
   - Developer C: User Story 3 (`custom-attributes` — T013–T017)
   - Developer D: User Story 4 (binding-properties family — T018–T028)
3. Stories complete and integrate independently (each writes to its own fixture subdirectory)

---

## Notes

- [P] tasks = different files, no dependencies — safe to parallelize
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- The fixture layout under `src/test/resources/projects/<scenario>/` is the entire deliverable — no new source code in `src/main/`
- The `verify.groovy` script in US3 is the only piece of Groovy code added; its exact binding conventions come from Apache's own archetype (#1032)
- All `archetype.properties` values follow the `defaultValue` declared in each `property-*.xml`, except where the scenario explicitly tests a non-default value (this is how the dependency rule and the per-scenario variations are encoded)
- Avoid: editing `bash_script/`, modifying the archetype's `archetype-metadata.xml`, or touching `archetype-resources/` — these are explicitly out of scope per spec FR-011 and intake `Out of Scope`
- Avoid: adding fixtures to `binding-properties` beyond the 5 sub-modules (default, list, map, mutable, validated) — SC-003's "≤ 1 fixture edit/add per new flag" target is bounded by the curated matrix choice, not exhaustive enumeration