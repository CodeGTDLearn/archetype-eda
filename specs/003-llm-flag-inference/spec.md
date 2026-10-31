# Feature Specification: Inferência LLM das flags do `mvn archetype:generate`

**Feature Branch**: `[003-llm-flag-inference]`

**Created**: 2026-10-12

**Status**: Draft

**Input**: Assessment pipeline handoff from `.specify/assessments/llm-flag-inference/decision.md` (verdict: go, recommended approach: option B — LLM suggests a flag mapping; a deterministic validator against the canonical archetype metadata applies defaults and assembles the command line).

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Operator generates a project from a textual description and a JSON contract (Priority: P1)

An operator runs the mechanism providing (a) a free-form textual description of the use case and (b) a JSON contract. The mechanism returns a single command line that, when executed, generates a project without interactive prompts. The generated project compiles and packages successfully.

**Why this priority**: This is the core value proposition. It replaces the current prompt-based interactive flow with declarative inputs and a single reproducible command line. Every other user story either supports or extends this one.

**Independent Test**: Run each of the 10 existing integration test profiles (`basic`, `binding-default-only`, `binding-list-only`, `binding-map-only`, `binding-mutable-only`, `binding-validated-only`, `custom-attributes`, `defaults-only`, `full`, `minimal`) by invoking the command line produced by the mechanism. All 10 profiles must pass `mvn clean install` and `mvn integration-test`.

**Acceptance Scenarios**:

1. **Given** a textual description and a JSON contract that together cover the decisions needed for a `basic` profile, **When** the operator invokes the mechanism, **Then** a single non-interactive command line is returned and the generated project compiles and packages.
2. **Given** a textual description and a JSON contract that together activate the `custom-attributes` profile, **When** the operator invokes the mechanism, **Then** the produced command line enables custom attributes on the entity and produces a project whose entity carries the expected fields.
3. **Given** a textual description that omits some decisions but the JSON contract covers them, **When** the operator invokes the mechanism, **Then** values from the JSON contract appear on the command line and unspecified values use the catalog's defaults.

---

### User Story 2 — Operator without a JSON contract is forced to provide one (Priority: P1)

An operator provides only a textual description (no JSON contract). The mechanism immediately and explicitly requests the JSON contract and stops — no command line is produced, and no inference step is run.

**Why this priority**: The JSON contract is the single source of truth for business decisions. Producing any output before validating the contract would let inferred values silently override declared decisions.

**Independent Test**: Invoke the mechanism with only a textual description. Verify (a) the mechanism outputs an explicit request for the JSON contract, (b) no command line is produced, and (c) the inference step is not triggered.

**Acceptance Scenarios**:

1. **Given** only a textual description, **When** the operator invokes the mechanism, **Then** the mechanism outputs a request for the JSON contract and exits without producing a command line.
2. **Given** only a textual description, **When** the operator invokes the mechanism, **Then** no inference step is triggered (verified by absence of inference-related logs or timing).

---

### User Story 3 — Repeated invocation with the same input returns a cached command line (Priority: P2)

An operator invokes the mechanism twice with the same textual description and the same JSON contract. The second invocation returns a byte-equivalent command line without re-running the inference step.

**Why this priority**: Cost efficiency (each inference call has a token cost) and reproducibility (the same input must produce the same output).

**Independent Test**: Invoke the mechanism twice with byte-identical input. Verify that (a) the second invocation does not trigger the inference step and (b) the two produced command lines are byte-equivalent.

**Acceptance Scenarios**:

1. **Given** a textual description and a JSON contract, **When** the operator invokes the mechanism a second time with the same inputs, **Then** the inference step is skipped and the previously produced command line is returned.
2. **Given** two invocations with semantically identical inputs but differently-ordered JSON fields, **When** the operator invokes the mechanism, **Then** both invocations hit the same cache entry (verified by identical cache keys).

---

### User Story 4 — The flag catalog is the canonical archetype metadata, read at runtime (Priority: P2)

The mechanism reads the existing canonical flag catalog (the archetype metadata that defines all available flags, their default values, and their validation rules) at runtime. There is no hardcoded list of flags inside the mechanism itself.

**Why this priority**: Single source of truth. If the mechanism had its own hardcoded flag list, it would silently drift from the catalog every time a new flag is added — exactly the failure mode that the previous bash-based approach suffered from.

**Independent Test**: Add a new flag to the canonical archetype metadata; invoke the mechanism; verify that the new flag is recognized and validated without any change to the mechanism code.

**Acceptance Scenarios**:

1. **Given** the canonical flag catalog has been extended with a new flag, **When** the operator invokes the mechanism, **Then** the new flag is recognized by the validator without any change to the mechanism.
2. **Given** the canonical flag catalog has been modified to remove a flag, **When** the operator invokes the mechanism, **Then** any proposal referencing the removed flag is rejected by the validator.

---

### User Story 5 — Integration with the spec-kit happens through textual orientation only (Priority: P3)

The github/spec-kit templates (spec, plan, and test phases) include textual guidance pointing operators to this mechanism. No technical integration is required — orientation only, no fork of the template.

**Why this priority**: Adoption vector. The strategic intent is that every spec-kit-driven feature ends up using this mechanism instead of constructing the command line ad-hoc.

**Independent Test**: Open the spec-kit templates for the spec, plan, and test phases. Verify that the orientation text is present and that it directs the operator to invoke this mechanism.

**Acceptance Scenarios**:

1. **Given** the spec-kit template for the spec phase, **When** an implementer reads it, **Then** the template explicitly instructs the implementer to use this mechanism to produce the project generation command line.

---

### User Story 6 — Legacy bash generation scripts are removed after adoption (Priority: P3)

After the mechanism has been validated against all 10 integration test profiles, the legacy bash scripts for project generation (the command-line assembler, the general-functions script, and the 14 input-collection scripts) are removed from version control.

**Why this priority**: Closes the loop on the strategic intent to eliminate bash preprocessing from the project generation flow. Without this step, the legacy scripts remain as a parallel path that drifts from the new mechanism.

**Independent Test**: Run `git ls-files bash_script/` after adoption. Verify that the command-line assembler, the general-functions script, and the 14 input scripts are absent from version control.

**Acceptance Scenarios**:

1. **Given** the mechanism has passed all 10 integration test profiles, **When** the operator removes the legacy bash scripts, **Then** the project generation flow continues to work end-to-end via the mechanism.

---

[Add more user stories as needed, each with an assigned priority]

### Edge Cases

- What happens when the JSON contract contains a field name that is not present in the flag catalog?
- How does the mechanism handle malformed JSON syntax in the contract?
- What happens when inference proposes a value that fails the catalog's validation rule for that flag?
- How does the mechanism behave when the canonical flag catalog itself is malformed or unreadable?
- What happens when the same textual description and contract combination has been seen before, but with a different field ordering in the JSON contract?
- What happens when a flag in the catalog has no default value and is not specified by the contract or inference?
- How does the mechanism behave when the inference step returns an empty proposal (no flags activated)?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST accept a textual description of the use case as input. If `--use-case-description` is absent, is the last argument with no value, or is blank/whitespace-only, the mechanism MUST reject the invocation with exit code 2 and a clear, specific error message naming `--use-case-description` and pointing to FR-001. The check runs BEFORE the contract gate, so a missing description prevents stdin from being read and inference from being triggered.
- **FR-002**: System MUST accept a JSON contract as multi-line JSON read from stdin when the operator passes the valueless flag `--use-case-contract`. The contract MUST be a JSON object (root); the mechanism MUST reject arrays or scalars at the root. Single-line inline contract input is no longer accepted.
- **FR-003**: System MUST reject invocation and explicitly request the JSON contract when no contract is provided; no command line is produced in this case and the inference step is not triggered.
- **FR-004**: System MUST infer from the textual description which flags to activate, returning a structured proposal of (flag-name, value) pairs.
- **FR-005**: System MUST validate every proposed flag against the canonical flag catalog at runtime.
- **FR-006**: System MUST reject any flag proposal whose value fails the catalog's validation rule for that flag.
- **FR-007**: System MUST prefer JSON contract values over inferred values when both are present for the same flag.
- **FR-008**: System MUST apply the catalog's default value for any flag that is not specified by the contract, not inferred, or whose inferred value was rejected.
- **FR-009**: System MUST discard any flag proposed by inference that is absent from the contract; discarded flags fall back to the catalog's default value. **(Amended 2026-10-23 — see `.specify/bugs/contrato-json-inferido-inoperante/`.)** The gate applies to every flag EXCEPT the ones on an explicit allowlist whose semantic is derivable from the contract — currently `entity_name`. A proposal for an allowlisted flag MUST be accepted even when the contract carries no literal key with that name, provided the value satisfies the flag's `validationRegex`; an accepted proposal MUST NOT be listed among the discarded ones.
- **FR-010**: System MUST produce a single command line that, when executed, generates a project without interactive prompts.
- **FR-011**: System MUST cache the produced command line keyed by a stable hash of (catalog fingerprint + description + contract canonical representation) and serve the cached result on repeat invocations without re-running the inference step. The **catalog fingerprint** is a SHA-256 digest over every flag's key, default value and validation rule, in catalog order. Embedding it guarantees that a catalog change invalidates every previously cached command line: a cached entry MUST NOT carry flag values that the current catalog no longer defines. (Amended 2026-10-23 — see `.specify/bugs/flag-contrato-absoluto-obsoleta/`.)
- **FR-012**: System MUST produce byte-equivalent command lines across repeated invocations with the same (description + contract) input and the same catalog.
- **FR-013**: System MUST read the flag catalog from the canonical archetype metadata at runtime — no hardcoded flag list in the mechanism.
- **FR-014**: System MUST ensure that, after the legacy bash scripts are removed per FR-016 (US6), the project generation flow contains zero interactive-prompt calls.
- **FR-015**: System MUST enable textual orientation in this project's spec/plan/tasks templates under `.specify/templates/` that directs operators to invoke this mechanism.
- **FR-016**: System MUST enable removal of the legacy bash generation scripts from version control after validation succeeds across all 10 integration test profiles.
- **FR-017**: System MUST NOT persist the contract's content in version control; only the cache key (hash) may be persisted if caching is implemented locally. **(Amended 2026-10-23 — see `.specify/bugs/contrato-json-inferido-inoperante/`.)** The **mechanism** persists only the hash of the contract in its cache. The **generated module** is the single, explicit exception: the contract the module was built from is persisted inside the module (FR-023), because it is the module's baseline and the reference of its schema tests. A contract persisted that way MUST NOT carry credentials (Principle V) — the mechanism MUST warn on stderr when it detects secret-like keys in the contract.
- **FR-018**: System MUST treat any `<requiredProperty>` with empty `<defaultValue></defaultValue>` as a build-time catalog error: the mechanism MUST exit with code 4 and emit a message naming the offending flag, before any inference or contract processing happens. The default value MUST be non-empty for every flag in the canonical catalog.
- **FR-019**: The external inference script (per `contracts/cli-contract.md` Section 2) MUST set LLM `temperature = 0` (and `top_p = 0` recommended) so the inference step produces deterministic output for a given input. The mechanism MUST NOT compensate for non-deterministic inference; it is the operator's responsibility to configure the script.
- **FR-020**: System MUST emit a provenance report on stderr after the command line is printed (on cache miss AND on cache hit), formatted as a Markdown table listing, for every catalog flag: final value, source (`CONTRACT` / `CONTRACT_DERIVED` / `INFERENCE` / `DEFAULT`), and notes (including FR-006 regex rejections); followed by a section listing LLM-proposed flags discarded under FR-009 (flag + proposed value), and a summary count line. stdout MUST remain exactly one command line (the report never goes to stdout). The report MUST NOT contain the textual description or any contract field absent from the catalog (FR-017). On cache miss, the report body (without the cache-status header) is stored in the cache entry (field `r`, per `contracts/cli-contract.md` Section 3) and served on subsequent hits; legacy cache entries without `r` produce an explicit "report unavailable (legacy entry)" note. Exit code remains 0. **(Amended 2026-10-23 — see `.specify/bugs/contrato-json-inferido-inoperante/`.)** `CONTRACT_DERIVED` denotes a value derived from the contract by the mechanism itself (FR-021, FR-022, FR-023) — it is neither a literal of the contract nor an LLM proposal. The summary line format is `**Resumo**: <c> CONTRACT / <d> CONTRACT_DERIVED / <i> INFERENCE / <x> DEFAULT / <n> descartada(s)**` — the insertion of the `CONTRACT_DERIVED` counter is a breaking change for consumers that parse the summary.

- **FR-021**: System MUST derive the entity from the contract, deterministically:
  * The entity **attributes** are the contract's `attributes` collection when present (a map — the key is the attribute name and the value the sample; or a list of objects carrying `name` and `value`/`type`), otherwise the contract's **business fields** — the top-level keys that are neither catalog flags nor meta keys (`entityName`, `entity`, `package`, `description`).
  * The attribute **type** MUST be estimated from the attribute **value** (`boolean` → `Boolean`, integer that fits in `int` → `Integer`, larger integer → `Long`, decimal → `Double`, string → `String`, array/object → `String`). A `type` declared in the contract takes precedence when it is a `java.lang` type **or a PROJECT type** (a type outside `java.lang`, e.g. a domain class or an enum). A PROJECT type MUST be kept: the entity references it by its **simple name**, and the archetype MUST receive its FQN through `custom_attribute_imports` (the entity template imports it) and/or its enum definition through `custom_enum_definitions` (the post-processor generates the enum source). A declared type outside both categories (e.g. `java.time.Instant`, a generic type) MUST be rejected with a loud warning and the type estimated from the value instead.
  * The attribute **name** MUST be normalized into a valid Java identifier matching the catalog's `custom_attributes_list` regex (`^[a-z][a-zA-Z0-9_]*$`). For a PROJECT type the name MUST come from the **type** itself (its simple name with the first letter lowercased: `EnderecoCliente` → `enderecoCliente`, `StatusPedido` → `statusPedido`).
  * The attribute names MUST be **unique** — the entity MUST NOT declare the same field twice. When two attributes share a PROJECT type, the first one takes the name of the type and the second one keeps the name of the contract; an attribute whose name is still duplicated MUST be discarded with a loud warning.
  * The resulting `custom_attributes_list` MUST be assembled strictly as `Type:name,Type:name` (no spaces, contract order) and MUST satisfy the flag's `validationRegex`; when it does not, the failure MUST be **loud** (a warning on stderr plus a note in the provenance report) and never a silent fallback to a plausible-but-wrong entity.
  * `enable_custom_attributes` MUST be **forced** to `true` whenever derived attributes exist — the entity template renders the fields only in that branch.
  * The entity **name** MUST be resolved as: an explicit contract field (`entity_name`, `entityName`, `entity`) → an LLM proposal (accepted through the FR-009 allowlist) → the catalog default.

- **FR-022**: System MUST derive `entity_name_package` from the **resolved** entity name (lowercased), unless the contract carries an explicit `entity_name_package`, which MUST win. The derived value MUST satisfy the flag's `validationRegex`.

- **FR-024**: System MUST carry the metadata of the PROJECT types (FR-021) to the archetype through two derived flags, both with the default `none`:
  * `custom_attribute_imports` — the FQNs of the referenced types, separated by commas. The entity template MUST emit one `import` per FQN, skipping the FQNs that belong to the entity's own package.
  * `custom_enum_definitions` — the enums the post-processor MUST generate, in the format `Type:CONSTANT|CONSTANT` (several enums separated by `;`). An enum is a PROJECT type with a **textual value** (the observed value) or with explicit constants (`enumValues`/`values`). A `Type` without a package MUST be generated in the entity's package; a qualified `Type` MUST be generated in its own package. A constant MUST be normalized into a valid Java identifier (uppercase, non-alphanumeric characters replaced by `_`). An enum file that already exists MUST NOT be overwritten — the generation MUST be skipped with a warning.
  * Both flags MUST satisfy their `validationRegex`; a derived value that violates it MUST fall back to the catalog default with the FR-006 treatment (warning on stderr + note in the provenance report).
  * Two referenced types with the **same simple name** and different packages MUST be reported with a loud warning — the entity cannot import both.

- **FR-023**: System MUST persist the contract in the generated module:
  * The mechanism MUST materialize the contract on disk (default: `<cache-dir>/contracts/<sha256-of-canonical-contract>.json`; the directory MUST be overridable with `--contract-dir`) and MUST emit its path in the command line through the catalog flag `entity_contract_path`. The file name MUST be deterministic so that repeated invocations produce byte-equivalent command lines (FR-012). The path MUST be emitted with forward slashes so that it survives any shell.
  * The archetype's post-processor MUST copy that file into the generated project at `src/test/resources/contracts/contract.json`, byte for byte, **only when** `enable_folder_src_test_java` is `true` — the contracts folder belongs to the test tree and MUST NOT exist when the test tree is dropped. A missing or unreadable source file MUST NOT break the generation: the post-processor MUST warn and continue.
  * The persisted contract is the module's **baseline**: the schema tests of the module assert that the entity class still matches it.

*Example of marking unclear requirements:*

(none — all functional requirements are resolved in the assessment pipeline; see Assumptions for technical defaults)

### Key Entities

- **Textual Description**: Free-form natural language describing the use case; supplied by the operator.
- **JSON Contract**: Schema-free JSON document with arbitrary fields; all present fields are treated as required by default.
- **Flag Catalog**: The canonical archetype metadata that lists every available flag, its default value, and its validation rule; read at runtime.
- **Flag Proposal**: Structured (flag-name, value) pair produced by inference or supplied by the contract.
- **Generation Command Line**: The final command line that, when executed, generates the project without interactive prompts.
- **Cache Key**: Stable hash of (textual description + contract canonical representation); enables deterministic repeat behavior.
- **External Inference Script**: An operator-supplied command external to the mechanism (per `contracts/cli-contract.md` Section 2) that takes the textual description and JSON contract and returns a `Map<String, String>` of flag → value; MUST satisfy FR-019. Not a part of the mechanism's codebase.
- **Provenance Report**: Markdown table emitted on stderr (FR-020) after the command line, recording per-flag source (`CONTRACT` / `CONTRACT_DERIVED` / `INFERENCE` / `DEFAULT`), FR-006 rejection notes, FR-009 discarded LLM proposals, and summary counts. Cached as field `r` in the cache entry; contains no description or non-catalog contract content.
- **Materialized Contract** (FR-023): the contract written to disk by the mechanism (`<contract-dir>/<sha256-of-canonical-contract>.json`) and referenced by the command line through the `entity_contract_path` flag. The archetype copies it into the generated module as `src/test/resources/contracts/contract.json`.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of the 10 existing integration test profiles pass when invoked via command lines produced by this mechanism. Baseline: 10/10 profiles currently pass via legacy bash scripts.
- **SC-002**: 0 interactive-prompt calls remain in the project generation flow after the legacy bash scripts are removed per US6. Baseline: 4 occurrences of `read -n 1 -s` in the legacy bash scripts (enumerated below), all removed by US6 (T031–T034).

  **Baseline enumeration (4 traps, audited 2026-10-12):**
  - `bash_script/create_archetype_metadata.sh:67` — `read -n 1 -s` (pause on metadata-recreation error)
  - `bash_script/create_archetype_metadata.sh:77` — `read -n 1 -s` (pause on metadata-recreation error)
  - `bash_script/create_archetype_post_generate.sh:66` — `read -n 1 -s` (pause on snippet-composition error; this script is already replaced by `SnippetComposer.groovy` in flow but the bash file remains in the repo)
  - `bash_script/create_archetype_post_generate.sh:84` — `read -n 1 -s` (pause on snippet-composition error; same caveat)

  Note: `bash_script/create_archetype_post_generate.sh:134` also contains a `read -n 1 -s` but is past the line where the script is short-circuited by the Groovy replacement, so it never runs in the current `mvn clean install` flow. It is removed in US6 T031–T034 regardless. (Historical note only; not counted in the 4.)
- **SC-003**: 100% of the 32 catalog flags are recognized by the validator without any code change. Baseline: not previously enforced - drift between the bash prompt scripts and the catalog was possible.
- **SC-004**: 100% of repeated invocations with the same (description + contract) input produce byte-equivalent command lines and do not re-trigger the inference step.
- **SC-005**: 100% of invocations without a JSON contract are interrupted before any inference happens; no command line is produced.
- **SC-006**: 100% of inferred flag proposals whose values fail the catalog's validation rule are rejected, with the catalog's default value applied instead.
- **SC-007**: The legacy bash generation scripts (the command-line assembler, the general-functions script, and the 14 input-collection scripts) are removed from version control after validation succeeds in all 10 integration test profiles.

## Assumptions

- The LLM used for inference is accessible and has acceptable cost per call; the cache strategy bounds the cost by eliminating redundant inference.
- The canonical archetype metadata remains the single source of truth for the flag catalog; no external code injects flags outside this catalog.
- The github/spec-kit accepts textual orientation in its templates without requiring a fork. (Operator's wrapper, out of scope for the mechanism itself; FR-015 covers this project's own `.specify/templates/` only.)
- The JSON contract format is free-form; the mechanism treats every present field as required by default unless business rules specify otherwise.
- The catalog's validation rule (where present) is sufficient to reject values that are structurally invalid (e.g., out-of-range ports, malformed numbers).
- A canonical representation of the JSON contract (independent of field ordering) is used to derive the cache key, ensuring that semantically identical contracts produce identical cache keys.
- Inference determinism is enforced by FR-019 on the external script, not by the mechanism itself.
- The mechanism is invoked by a single human operator (the project maintainer), not by a multi-tenant system.
- Validation across the 10 existing integration test profiles is sufficient evidence that the mechanism works correctly across the realistic flag combinations.
- The mechanism reads the flag catalog at startup of each invocation, ensuring it always reflects the latest canonical metadata.

## Constitution Compliance

- **Principle I (Modularidade por Chave de Ativação)**: Respected. Every flag is validated against the canonical catalog before being activated, and defaults are applied for unspecified flags.
- **Principle II (Convenção Fixa de Nomenclatura)**: Not applicable. This feature does not introduce a new file under the archetype's naming-convention directories (`archetype_post_generate_*`).
- **Principle IV (Binding Duplo de Configuração Externalizada)**: Not applicable. This feature does not introduce new externalized configuration bindings.
- **Principle V (Credenciais Nunca Entram no Repositório)**: Respected with one explicit, documented exception (FR-017, amended 2026-10-23). The mechanism itself persists only the **hash** of the contract. The generated **module** persists the contract it was built from (FR-023), because that contract is the module's baseline and the reference of its schema tests — a contract that reaches the module therefore reaches version control. The mechanism warns on stderr when the contract carries secret-like keys, and the contract MUST NOT carry credentials.
- **Principle VI (Validar Antes de Culpar o Código)**: Respected. The 10 integration test profiles serve as the validation harness; the spec mandates that all 10 pass before declaring the mechanism adopted.
