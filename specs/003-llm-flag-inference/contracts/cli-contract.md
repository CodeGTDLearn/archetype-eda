# CLI Contract: Inferência LLM das flags do `mvn archetype:generate`

- **Slug**: llm-flag-inference
- **Created**: 2026-10-12
- **Spec**: [spec.md](./spec.md)
- **Plan**: [plan.md](./plan.md)
- **Data model**: [data-model.md](./data-model.md)

This document defines the input/output contract of the mechanism's CLI invocation, plus the contract between the mechanism and the external LLM inference script (Entity 7 in the data model).

---

## 1. Mechanism invocation contract (operator-facing)

### Invocation

The mechanism reads the JSON contract from stdin when the operator passes the valueless flag `--use-case-contract`. The contract is always multi-line.

```
eda-llm-flag-inference \
    --use-case-description "<textual description>" \
    --use-case-contract \
    [--cache-dir "<path>"] \
    [--inference-bin "<path>"] \
    < contract.json
```

Or with a heredoc:

```
eda-llm-flag-inference \
    --use-case-description "minimal profile" \
    --use-case-contract <<'EOF'
{
  "id": 1024,
  "name": "João Silva",
  "active": true
}
EOF
```

**Rule**: the JSON contract is always multi-line and is always read from stdin via the `--use-case-contract` valueless flag. No single-line inline contract input is accepted.

### Arguments

| Argument | Required | Type | Description |
|---|---|---|---|
| `--use-case-description` | yes | string | The textual description of the use case, passed as a single-line argument. If the next token starts with `--`, the description is treated as missing (this prevents `--use-case-description --use-case-contract` from swallowing the contract flag as a value). |
| `--use-case-contract` | yes | flag (valueless) | Reads the JSON contract from stdin. The contract MUST be a JSON object at the root. Multi-line JSON is the only supported form. End-of-input terminates the read. |
| `--cache-dir` | no | path | Override the cache directory (default: `$XDG_CACHE_HOME/eda-archetype/llm-flag-inference/` with Windows fallback). |
| `--contract-dir` | no | path | Override the directory where the contract is materialized (default: `<cache-dir>/contracts/`). The materialized file is referenced by the `entity_contract_path` flag on the command line and copied into the generated module by the archetype (FR-023). Point it at a stable directory if the cache directory is disposable. |
| `--inference-bin` | no | path | Override the external inference script (default: `$EDA_LLM_INFERENCE_BIN`, with documented fallback path). |

### Output (success)

A single line on stdout:

```
mvn archetype:generate --batch-mode -D<flag1>=<value1> -D<flag2>=<value2> ... -D<flagN>=<valueN>
```

Followed by an exit code of 0.

### Output (provenance report — FR-020)

After the stdout line is flushed and before exit, the mechanism prints a provenance report on **stderr** (stdout always remains exactly one command line). Exit code stays 0.

Header line — `## Relatorio de Proveniencia - cache: MISS` (fresh inference) or `... cache: HIT` (served from cache) — followed by a Markdown table with one row per catalog flag:

```markdown
| flag | valor final | fonte | notas |
|---|---|---|---|
| api_port | 9090 | CONTRACT |  |
| enable_jwt_authentication | true | INFERENCE |  |
| log_level | INFO | DEFAULT |  |
| server_port | 8080 | DEFAULT | valor '99999' rejeitado pela regex; default aplicado (FR-006) |
```

`fonte` is exactly one of `CONTRACT` (FR-007), `CONTRACT_DERIVED` (value derived from the contract by the mechanism: FR-021/FR-022/FR-023), `INFERENCE` (FR-009: LLM value, key present in the contract or on the allowlist), or `DEFAULT` (FR-008; also the source after an FR-006 rejection, with the note recording the rejected value).

When the inference step proposed flags whose keys are absent from the contract (FR-009), a second section follows:

```markdown
### Propostas do LLM descartadas (ausentes do contrato - FR-009)

| flag | valor proposto |
|---|---|
| enable_swagger | true |
```

A summary line closes the report: `**Resumo**: <c> CONTRACT / <d> CONTRACT_DERIVED / <i> INFERENCE / <x> DEFAULT / <n> descartada(s)`.

`CONTRACT_DERIVED` denotes a value the mechanism derived from the contract itself: the entity name and its package, the attribute list, and the path of the materialized contract file (FR-021, FR-022, FR-023). It is neither a literal of the contract nor an LLM proposal.

On cache hit the cached report body (field `r`, Section 3) is served. Legacy entries without `r` print the note `(relatorio indisponivel: entrada de cache legada, gravada antes da FR-020)` instead. The report never contains the textual description or contract fields absent from the catalog (FR-017). Pipe characters in values are escaped (`\|`) so the table stays well-formed.

### Output (secret-like keys in the contract)

The contract is persisted inside the generated module (FR-023), and the module is versioned. When the contract carries keys that look like secrets (`password`, `senha`, `secret`, `token`, `credential`, `api_key`, `private_key`), the mechanism prints a warning on stderr:

```
WARNING: the contract contains keys that look like secrets: db.password
         The contract is persisted in the generated project (src/test/resources/contracts/contract.json) and WILL be versioned.
         Do not put credentials in the contract (Principle V).
```

The warning never reaches the provenance report body: the report must not carry contract fields absent from the catalog (FR-017). The mechanism does not fail — a command line is always produced (FR-010).

### Output (description missing)

Triggered when `--use-case-description` is absent, is the last argument with no value, is followed by another `--` token (which is treated as a flag, not a value), or is blank/whitespace-only. The contract and inference are NOT touched.

```
ERROR: USE CASE DESCRIPTION is missing or blank.
Provide it via --use-case-description "<text>".
Processing stopped BEFORE reading the contract and BEFORE inference.
See spec.md FR-001.
```

Followed by an exit code of 2. Checked **before** the contract gate.

### Output (contract missing)

Triggered when `--use-case-description` is present but `--use-case-contract` is absent or its stdin is empty. Inference is NOT triggered.

```
ERROR: USE CASE CONTRACT is missing or empty.
Provide it via --use-case-contract, passing the multi-line JSON through stdin.
Processing stopped BEFORE inference.
See spec.md FR-003.
```

Followed by an exit code of 2 (distinct from inference failures).

### Output (inference failed)

A single line on stderr:

```
ERROR: External inference script exited with code <N>.
       Output: <truncated script stderr>
```

Followed by an exit code of 3.

### Output (validation failed)

For each failed flag, one line on stderr:

```
ERROR: Flag '<key>' value '<value>' fails validation regex '<regex>'.
       Default value '<defaultValue>' will be used instead.
```

Followed by exit code 0 (warnings, not failures — the command line is still produced using defaults).

### Output (catalog malformed)

A single line on stderr:

```
ERROR: Flag catalog is malformed or unreadable.
       Path: <resolved path to archetype-metadata.xml>
       <parser error message>
```

Followed by an exit code of 4. This is a build-time failure: the operator must fix the metadata.

---

## 2. External inference script contract (LLM-facing)

The mechanism shells out to the external inference script with the following contract.

### Invocation

```
<inference-bin> --use-case-description "<textual description>" < contract.json
```

Or, equivalently, both on stdin (the mechanism writes them to a temp file and passes the path):

```
<inference-bin> --use-case-description-file <tmp-path-1> --use-case-contract-file <tmp-path-2>
```

The mechanism uses whichever form the script advertises in its first line of stdout (a single-line JSON `{"contract-mode": "argv", "stdin-mode": "files"}`). Default: argv form.

### Required behavior

> **Terminology note:** the inference script's **output** (this section) is a `Map<String, String>` of flag → value. The mechanism's **output** (Section 1) is the assembled `mvn archetype:generate` command line. The two are different artifacts in different stages; the inference script's map is intermediate input to the mechanism's assembler (data-model.md Entity 5).

- **Exit code**: MUST be 0 on success.
- **Stdout (success)**: a single-line JSON object mapping flag names to values. Example:

  ```json
  {"enable_jwt_authentication":"true","api_port":"8080","enable_binding_properties":"false"}
  ```

- **Stdout (failure)**: empty. The script's diagnostics go to stderr.
- **Stderr (any case)**: free-form diagnostics, surfaced to the operator with the prefix `Inference script:`.
- **The entity name** (FR-021): the script SHOULD estimate `entity_name` from the context of the prompt and of the contract, and return it under the `entity_name` key. The value is **accepted even when the contract carries no literal `entity_name` key** — `entity_name` is on the FR-009 allowlist. The value MUST satisfy the catalog's `validationRegex` for that flag (`^[A-Z][a-zA-Z0-9_]+$`); an invalid value falls back to the catalog default (FR-006).
- **The attribute list** (FR-021): the mechanism derives `custom_attributes_list` from the contract **itself** — the attributes, their order and their types (estimated from the attribute values) are computed by the mechanism, not by the script. A script proposal for `custom_attributes_list` is therefore ignored whenever the contract carries attributes; the strict `Type:name,Type:name` format is guaranteed by the mechanism, not by the script.

### Required LLM configuration (operator's responsibility)

- `temperature = 0` (enforced contractually by FR-012 byte-equivalence requirement).
- `top_p = 0` (recommended, not enforced).
- The script MUST validate that every key in its output is present in the catalog (a small JSON file listing the 32 flag names is provided to the script via `--catalog-file`).

### Why this contract exists

Documented in [research.md](./research.md) Decision 6. The mechanism stays free of LLM SDK dependencies; the operator brings their own LLM client wrapped in a thin script.

---

## 3. Cache contract (storage-format)

The cache file is `cache.jsonl` in the cache directory (default: `$XDG_CACHE_HOME/eda-archetype/llm-flag-inference/`).

### Line format

One JSON object per line, no trailing commas, no line wrapping. Current format (v2, FR-020):

```json
{"k":"<sha256-hex>","v":"<command-line>","r":"<provenance-report-body>","t":<epoch-seconds>}
```

Legacy format (v1, pre-FR-020) — still readable:

```json
{"k":"<sha256-hex>","v":"<command-line>","t":<epoch-seconds>}
```

`r` is the provenance report body (Section 1, without the `## Relatorio ... cache:` header line, which is re-rendered at emission time). `r` contains no textual description and no non-catalog contract fields (FR-017).

### Key derivation

The cache key is `SHA-256( catalogFingerprint + "\0" + description + "\0" + canonicalContract )`, hex-encoded.

- `canonicalContract` is the sorted-key canonicalization of the contract JSON (Section 3 / Decision 4 in `research.md`).
- `catalogFingerprint` is `SHA-256` over every catalog flag's key, default value and validation rule, in catalog order — i.e. a fingerprint of the *current* canonical catalog.
- **Why the fingerprint is part of the key** (amended 2026-10-23, see `.specify/bugs/flag-contrato-absoluto-obsoleta/`): the produced command line is a function of the catalog. Without the fingerprint, a catalog change (e.g. a flag removed, a default corrected) would leave previously cached command lines reachable, and the operator would keep being served a line carrying flags the catalog no longer defines. With the fingerprint, any catalog change makes every older cache entry **unreachable** — invalidation without migration and without a change to the on-disk line format.

### Lookup

The mechanism computes the cache key (Entity 6 in the data model) and reads the file line by line, matching the `k` field. Each line is tried against the v2 shape first, then against the v1 shape (a v1 match yields `r = null`, and the caller prints the legacy-entry note). The first match wins. If multiple entries with the same `k` exist (corruption or race), the most recent by `t` wins.

**Parser (changed 2026-10-23 — see `.specify/bugs/contrato-json-inferido-inoperante/`)**: the line format is JSON, so each line is parsed with a JSON parser instead of a regular expression. The previous regex (`"r":"((?:[^"\\]|\\.)*)"`) compiled into a recursive `Pattern$Loop` — one stack frame per character — and overflowed the stack on a realistic provenance report (~1965 characters), turning every cache HIT into exit code 1. **The on-disk format did not change**: v2 stays v2, v1 stays readable. A corrupted line is now skipped instead of aborting the lookup.

### Insertion

The mechanism appends a new line under a `tmp` filename, then atomically renames (`File.renameTo`) it to `cache.jsonl`. Concurrent invocations each compute the same key for identical inputs; if both write, the second write wins — but both writes are byte-equivalent, so no corruption.

### Eviction

No automatic eviction in v1. Operators may delete `cache.jsonl` manually without consequence (cache will rebuild on next invocation).

---

## 4. Error code summary

| Code | Meaning |
|---|---|
| 0 | Success. A command line is on stdout. |
| 2 | Required input missing: either use-case description (FR-001) or use-case contract (FR-003). The description gate runs first. |
| 3 | External inference script failed. |
| 4 | Flag catalog malformed/unreadable (build-time failure). |
| 5 | Internal mechanism error (programming error; please file a bug). |

---

## 5. Stability guarantees

- The CLI argument surface (Section 1) is **stable** for the lifetime of the feature. New optional arguments may be added; existing arguments will not be renamed or removed without a major version bump. **Breaking change recorded 2026-10-15**: flags `--description`/`--description-stdin`/`--contract`/`--contract-stdin` were renamed to `--use-case-description` and `--use-case-contract` (valueless flag reading from stdin). The previous single-line inline contract form is no longer supported. This is a major-version rename; no aliases are kept.
- The cache file format (Section 3) is **stable**. Future versions will be able to read v1 cache files; the `t` field is reserved for future TTL use. **Extension recorded 2026-10-16 (FR-020)**: the optional field `r` (provenance report body) was added. This is additive and non-breaking: v2 readers accept v1 lines (report unavailable → legacy note); v1 readers simply never match v2 lines (treated as cache miss; the cache self-heals on the next write). **Fix recorded 2026-10-16**: the pre-FR-020 writer double-quoted the `v` value (the escaper already emits surrounding quotes), producing malformed lines that no reader regex could match — cache lookups never hit. The writer now emits well-formed lines in the documented shape. **Breaking change recorded 2026-10-23** (`flag-contrato-absoluto-obsoleta`): the cache key now embeds the catalog fingerprint (Section 3, "Key derivation"). The on-disk format is unchanged (still v2), but every entry written before this change is **unreachable** — it will never be served again. The effect is an intentional one-shot cache invalidation: the operator gets a fresh inference for every (description, contract) pair once, and the cache re-populates itself.
- **Breaking change recorded 2026-10-23** (`flag-contrato-absoluto-obsoleta`): the catalog no longer declares `absolute_path_jsonfile_contract` nor `enable_json_entity_contracts` (the JSON-contract flow was removed; the contract reaches the mechanism inline, through stdin). The catalog now exposes **32** flags instead of 34. Command lines produced by this version never carry those two flags. Consumers that passed them explicitly must drop them from their command lines — the archetype ignores unknown `-D` properties with a warning, so no hard failure occurs.
- **Breaking change recorded 2026-10-23** (`contrato-json-inferido-inoperante`):
  * The catalog exposes **33** flags instead of 32: the new `entity_contract_path` flag carries the path of the contract materialized by the mechanism, and the archetype copies that file into the generated module as `src/test/resources/contracts/contract.json` (FR-023). Its default is `none` (no contract to persist).
  * The **semantics of FR-009** changed: an LLM proposal for `entity_name` is accepted even when the contract carries no literal `entity_name` key (allowlist). Every other flag keeps the strict gate.
  * The **summary line of the provenance report** now counts `CONTRACT_DERIVED` between `CONTRACT` and `INFERENCE` (FR-020).
  * The **cache reader** parses the lines as JSON instead of applying a regular expression. The on-disk format is unchanged (still v2); the change only fixes the `StackOverflowError` that broke every cache HIT with a realistic report.
  * A new optional argument `--contract-dir` was added (Section 1). Existing arguments are unchanged.
  * **FR-017 exception**: the contract is now persisted inside the generated module, which is versioned. Contracts MUST NOT carry credentials; the mechanism warns when it detects secret-like keys.
- The external inference script contract (Section 2) is **stable**. The argv vs. files mode negotiation lets future versions add richer I/O without breaking v1 scripts. **Breaking change recorded 2026-10-15**: the inference-bin flags were renamed from `--description`/`--contract` and their `-file` variants to `--use-case-description`/`--use-case-contract` and their `-file` variants. No aliases are kept.
