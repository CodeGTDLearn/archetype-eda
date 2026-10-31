# Data Model: Inferência LLM das flags do `mvn archetype:generate`

- **Slug**: llm-flag-inference
- **Created**: 2026-10-12
- **Spec**: [spec.md](./spec.md)
- **Plan**: [plan.md](./plan.md)

This document defines the data entities that the mechanism operates on. It is derived from the 6 key entities listed in `spec.md` and constrained by the FR/SC requirements. The focus is on **what each entity means** and **how it is validated/transformed**; concrete file formats and class layouts live in the implementation phase (`/speckit.tasks` onward).

---

## Entity 1 — Textual Description

### Definition

Free-form natural language describing the use case. Supplied by the operator as the first argument to the mechanism.

### Shape

- **Type**: string (UTF-8).
- **Cardinality**: 1 (exactly one per invocation).
- **Length bounds**: 1 byte minimum; soft cap at 8 KiB (anything larger is treated as an operator error and rejected with a clear message).
- **Encoding**: UTF-8.

### Validation rules

- MUST be non-empty.
- SHOULD contain at least one space-separated token (heuristic to catch typos like a single character).

### Lifecycle

- Read once per invocation from `stdin` or argv.
- Hashed (SHA-256) into the cache key (see Entity 6).
- Not persisted outside the invocation.

---

## Entity 2 — JSON Contract

### Definition

Schema-free JSON document carrying the business decisions that must be respected by the mechanism. All fields present in the document are treated as required by default.

### Shape

- **Type**: JSON object (root MUST be an object — arrays and scalars at the root are rejected).
- **Cardinality**: 1 (exactly one per invocation; absent ⇒ mechanism fails fast).
- **Field types**: arbitrary; nested objects and arrays are allowed; per user clarification, no schema is enforced at this layer.
- **Field naming**: arbitrary; not required to match flag names exactly. The merger's mapping logic uses a configurable name-resolution strategy (default: exact match; future: alias table).
- **Encoding**: UTF-8.

### Validation rules

- MUST parse as valid JSON (`groovy-json` `JsonSlurper`).
- MUST be an object at the root (not array, not scalar).
- Per-field constraints are **not** the mechanism's job — the validator (Entity 3 → Entity 4 transition) handles per-flag constraints.

### Lifecycle

- Read once per invocation.
- Canonicalized (sorted keys, see Decision 4 in [research.md](./research.md)).
- Hashed (SHA-256) into the cache key (Entity 6).
- **Never persisted in plaintext** — Principle V. The cache stores only the produced command line; the contract's content lives only in the invocation's memory.

---

## Entity 3 — Flag Catalog Entry

### Definition

A single flag as declared in the canonical archetype metadata. Read at runtime from the XInclude-merged `archetype-metadata.xml`.

### Shape (per entry)

| Field | Type | Description |
|---|---|---|
| `key` | string | The flag's `-D` name (e.g., `enable_jwt_authentication`). MUST be unique across the catalog. |
| `defaultValue` | string | The flag's default. MUST be non-empty per FR-018; an empty `<defaultValue></defaultValue>` is a catalog build error. |
| `validationRegex` | string (optional) | A regex the value MUST match. If absent, no syntactic validation is performed (any non-empty string is accepted). |
| `description` | string (metadata) | Human-readable description, used only for error messages and tooling logs — not part of the produced command line. |

### Source

`src/main/resources/META-INF/maven/archetype_metadata_properties/*.xml` (17 files). The `<requiredProperty key="…">` declarations across these files constitute the catalog (35 flags).

### Validation rules

- `key` MUST be non-empty and unique.
- `defaultValue` MUST be non-empty (per FR-018); an empty value fails the build at catalog extraction time.
- `validationRegex` MUST compile as a valid `java.util.regex.Pattern` if present.

### State transitions

None. The catalog is read-only at runtime; FR-013 forbids any hardcoded modification. A change to the catalog requires editing the source XMLs and triggering a clean rebuild (constitution Principle VI).

---

## Entity 4 — Flag Proposal

### Definition

A structured `(flag-key, value)` pair produced by either the external LLM inference step or the JSON contract merger.

### Shape

- **Type**: `Map<String, String>` (catalog-key → value).
- **Cardinality**: 0..35 (zero or more per invocation; bounded by the catalog size).

### Production sources

1. **External LLM inference script** (Entity 7 below) — produces an initial `Map<String, String>` from the textual description.
2. **JSON contract** (Entity 2) — contributes entries after the merger's name-resolution pass.

### Validation rules (applied by the merger, not by the producer)

- The `flag-key` MUST exist in the catalog (Entity 3); otherwise the proposal is rejected (FR-005/009).
- The `value` MUST match the entry's `validationRegex` if present; otherwise the proposal is rejected and the default is applied (FR-006/008).

### State transitions

```
LLM inference (proposal) ──┐
                            ├──> Merger ──> Validated Proposal ──> Assembler
JSON contract (entries) ───┘
```

The merger applies the precedence rule from `intake.md`:
1. Contract value wins over LLM inference for shared keys.
2. LLM inference fills in keys the contract does not address.
3. Defaults fill in keys neither the contract nor the LLM addressed.
4. LLM-inferred keys absent from the contract are discarded (FR-009).

---

## Entity 5 — Generation Command Line

### Definition

The final command line that, when executed, generates a project without interactive prompts.

### Shape

- **Type**: string.
- **Format**: a single-line `mvn archetype:generate --batch-mode -D<flag1>=<value1> -D<flag2>=<value2> … -D<flagN>=<valueN>` string.
- **Quoting**: the assembler applies the following escape rule — a value MUST be wrapped in double quotes within the produced command line if and only if the value contains at least one of these characters: whitespace (` `, `\t`), `$`, `` ` ``, `\`, `"`, `&`, `|`, `<`, `>`, `;`, `(`, `)`, `*`, `?`, `[`, `]`, `{`, `}`. Otherwise the value MUST be emitted verbatim (no quotes). The wrapping double quotes MUST escape any embedded `"` as `\"`. Empty values MUST be emitted as `-D<key>=` (no quotes, empty right-hand side).

### Validation rules

- MUST begin with `mvn archetype:generate`.
- MUST contain `--batch-mode`.
- MUST contain at most one `-D<key>=<value>` per catalog key (no duplicates).
- MUST contain only keys present in the catalog (Entity 3).
- All values MUST pass the catalog's `validationRegex` for their key (FR-006).

### State transitions

```
Validated Proposal (Entity 4, post-merger) ──> Assembler ──> Generation Command Line (Entity 5)
                                                                                  │
                                                                                  ▼
                                                                       Cache (Entity 6) write
                                                                                  │
                                                                                  ▼
                                                                       Returned to operator
```

---

## Entity 6 — Cache Key + Cache Entry

### Definition

The mechanism's persistent memoization key. Lets repeated invocations with the same input skip the LLM inference step.

### Cache key shape

- **Type**: hex string (SHA-256, 64 chars).
- **Derivation**: `SHA-256(textualDescription + "\u0000" + canonicalJSONContract)`. The null byte separator prevents collisions where one input's bytes are a prefix of another's.

### Cache entry shape

- **Type**: JSON object stored one-per-line in `cache.jsonl`.
- **Fields**:
  | Field | Type | Description |
  |---|---|---|
  | `k` | string | The hex cache key. |
  | `v` | string | The produced Generation Command Line. |
  | `t` | integer (epoch seconds) | Timestamp of insertion; used for optional TTL eviction (default: never evict). |

### Validation rules

- `k` MUST be 64 lowercase hex chars.
- `v` MUST be a non-empty string starting with `mvn archetype:generate`.

### State transitions

```
New invocation ──> hash(description + canonical-contract) ──> key
                                                          │
                                                          ├── cache HIT ──> return cached `v`, skip inference
                                                          │
                                                          └── cache MISS ──> inference + merge + assemble ──> write entry {k, v, t} ──> return `v`
```

---

## Entity 7 — External Inference Script (delegated; not embedded)

### Definition

An operator-supplied command that the mechanism shells out to in order to obtain the LLM inference step. Lives outside the archetype repo.

### Contract

- **Inputs**: textual description (on `argv[1]`) and JSON contract (on stdin).
- **Output**: a `Map<String, String>` printed to stdout as a single-line JSON object. Each key MUST be a catalog flag name. Each value MUST be a string.
- **Error handling**: any non-zero exit code is treated as "inference failed"; the mechanism exits with a clear error message and does NOT produce a command line.
- **Configuration**: must set LLM `temperature = 0` for FR-012 byte-equivalence.

### Why external

Documented in [research.md](./research.md) Decision 6. Keeps the build-time Groovy code free of LLM SDK dependencies; lets the operator reuse their existing LLM tooling.

### Validation rules

- Exit code MUST be 0 on success.
- Stdout MUST be valid JSON parseable as an object.
- Each key in the output MUST exist in the catalog (Entity 3); unknown keys are discarded by the merger (FR-009).

---

## Cross-entity invariants

- **Cardinality of command line flags**: at most one `-D<key>=<value>` per catalog key (enforced by the assembler).
- **No flag from outside the catalog**: enforced by the merger (FR-013).
- **Cache value integrity**: the cache stores only the produced command line; the contract's payload never reaches the file system (Principle V).
- **Determinism**: given the same textual description and the same JSON contract, the produced command line is byte-equivalent across invocations (FR-012, enforced by `temperature=0` + sorted-key canonicalization + atomic cache writes).

---

## Out of scope for this data model

- The LLM client's request/response schemas (handled by the external script).
- The Maven Archetype Plugin's internal flag semantics (the archetype already enforces those; this model just produces the right `-D…` list).
- The legacy bash scripts' internal variable names (those scripts will be deleted after SC-007 is met).
