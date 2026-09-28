# Phase 0 Research: Inferência LLM das flags do `mvn archetype:generate`

- **Slug**: llm-flag-inference
- **Created**: 2026-10-12
- **Spec**: [spec.md](./spec.md)
- **Plan**: [plan.md](./plan.md)

This research consolidates the technology decisions behind the implementation plan. It resolves the 5 open questions carried forward from `.specify/assessments/llm-flag-inference/problem.md` and records the rationale for each choice.

---

## Decision 1 — Language for the mechanism: Groovy 4.0.28

- **Decision**: Implement the validator, merger, canonicalizer, assembler, and cache in Groovy 4.0.28.
- **Rationale**:
  - Groovy 4.0.28 is already on the archetype's `maven-archetype-plugin:3.4.1` classpath (via the `groovy-bom` transitively declared by the plugin).
  - `SnippetComposer.groovy` already uses Groovy 4.0.28 build-time via `gmaven:2.1.1` in phase `process-resources` (precedent from `.specify/assessments/groovy-snippet-injection/`).
  - The five new components (extractor, merger, assembler, canonicalizer, cache) are state-light and fit Groovy's scripting sweet spot.
- **Alternatives considered**:
  - **Java 17**: would compile cleanly and avoid the Groovy classpath question, but require a separate `<source>` directory layout for the new module and a different plugin invocation. Rejected because Groovy is already proven in this same phase for the same purpose.
  - **Plain Maven plugin (Java)**: would be the "purest" option, but introduces a new build artifact (the plugin jar) and increases the project's surface area. Rejected for scope reasons.
  - **Reuse the bash + a thin Java wrapper**: explicitly rejected — the assessment pipeline's verdict was to eliminate bash from the project-generation flow.

## Decision 2 — Build-time invocation: `gmaven:2.1.1` goal `execute` in phase `process-resources`

- **Decision**: Invoke the orchestrator script via a second `<execution>` in the existing `gmaven:2.1.1` `<plugin>` entry in `pom.xml`, at phase `process-resources`.
- **Rationale**:
  - The existing `<execution>` for `SnippetComposer.groovy` already wires `gmaven:2.1.1` at this phase. Adding a sibling `<execution>` is a minimal pom edit.
  - `process-resources` runs after `generate-resources` (where `xml-maven-plugin` materializes `archetype-metadata.xml`) and before `package` (where the archetype jar is sealed) and before `integration-test` (where the 10 IT profiles run). This places the new mechanism exactly where it needs to be.
  - No new plugin dependency is added.
- **Alternatives considered**:
  - **`xml-maven-plugin:1.2.1` + `identity.xsl`**: rejected as the orchestrator because the orchestration logic (LLM call, cache, merge) is not a pure XML transform. `xml-maven-plugin` is still used for `archetype-metadata.xml` materialization, unchanged.
  - **A new Maven plugin (mojo)**: rejected for the same reason as the Java approach above.
  - **Runtime invocation (outside `mvn`)**: rejected — would reintroduce the manual-prompt problem.

## Decision 3 — Flag catalog extraction via XInclude, parsed by `groovy-xml`

- **Decision**: Read the canonical `archetype-metadata.xml` (already materialized by `xml-maven-plugin:1.2.1` + `identity.xsl` into `target/classes/META-INF/maven/archetype-metadata.xml` at the start of `process-resources`). Resolve the XInclude `<!ENTITY>` references to the 17 `property-*.xml` files. Parse the merged result with the Groovy 4.0.28 transitive `groovy-xml` (`XmlParser` or `XmlSlurper`).
- **Rationale**:
  - The XInclude references are already resolved by `xml-maven-plugin` at materialization time, so the orchestrator reads a flat, fully-merged XML — no second XInclude pass needed at runtime.
  - `groovy-xml` is already on the classpath; no new dependency.
  - The catalog extraction is deterministic and cheap: parse once, build a `Map<String, CatalogEntry>` keyed by flag name, with `defaultValue` and `validationRegex` captured. Memory footprint: 32 entries × a few dozen bytes each.
- **Alternatives considered**:
  - **StAX / SAX streaming**: not needed — the merged XML is small (a few KB) and fits in memory.
  - **Hardcoded flag list**: explicitly rejected — FR-013 forbids it; the precedent of drift between bash prompt scripts and the catalog is the failure mode being eliminated.
  - **Re-parse each `property-*.xml` separately**: rejected because `archetype-metadata.xml` already declares the canonical set of entities; re-reading individually risks missing one.

## Decision 4 — JSON canonicalization: sorted-key serialization, no external library

- **Decision**: Use Groovy 4.0.28 transitive `groovy-json` to canonicalize the JSON contract by recursively sorting object keys alphabetically before serialization. Use the resulting string as input to SHA-256 for the cache key.
- **Rationale**:
  - The catalog has 32 flags, all flat top-level `<requiredProperty key="…">` entries; the contract payload, by user spec, is a free-form JSON object that may contain nested objects and arrays.
  - Sorted-key serialization (RFC 8785 / JCS is overkill for this use case) is sufficient to produce stable cache keys regardless of input field ordering.
  - `groovy-json` is already on the classpath via the Groovy 4.0.28 transitive set; no new dependency.
  - SHA-256 is provided by the JDK (`MessageDigest.getInstance("SHA-256")`) — no new dependency.
- **Alternatives considered**:
  - **RFC 8785 (JCS) library**: more rigorous (handles number normalization, Unicode escapes) but requires pulling in `com.fasterxml.jackson.core:jackson-core` or a dedicated JCS library. Rejected — the contracts in scope are operator-supplied and won't contain edge-case encodings; sorted-key is sufficient.
  - **Use `JsonOutput.toJson` directly without canonicalization**: would produce unstable cache keys when field order varies between invocations. Rejected — FR-012 (byte-equivalent command lines for identical input) requires stable keys.
  - **Use `hashCode()` of the parsed `JsonSlurper` result**: non-portable across Groovy versions; rejected for stability.

## Decision 5 — Cache storage: single JSONL file outside the repo, atomic rename for concurrency

- **Decision**: Store the cache at `$XDG_CACHE_HOME/eda-archetype/llm-flag-inference/cache.jsonl` (with a Windows fallback to `%LOCALAPPDATA%\eda-archetype\llm-flag-inference\cache.jsonl` when `XDG_CACHE_HOME` is unset). One line per entry: `{"k": "<sha256-hex>", "v": "<command-line>"}`. Writes use a `tmp + atomic rename` pattern (`File.renameTo`) to avoid corruption from concurrent invocations.
- **Rationale**:
  - XDG Cache Home is the platform-neutral convention for non-essential cached data.
  - Outside the repo: satisfies Principle V (no contract content in the repo) and avoids polluting the repo's `.gitignore` rules.
  - JSONL is append-only-friendly and trivial to grep/inspect by hand if needed.
  - The cache value (command line) does not contain the contract's payload — only the produced `-D…` flags. Even if those flags contained operator-supplied values, they are the public output of the mechanism, not secret input.
- **Alternatives considered**:
  - **In-memory cache only**: rejected — would not survive across invocations, defeating FR-011 (cache hit on repeated invocations).
  - **SQLite**: rejected — overkill for < 100 entries in steady state.
  - **Inside the repo (e.g., `.cache/` with `.gitignore`)**: rejected — even with `.gitignore`, Principle V prefers to keep cache state outside the repo entirely.

## Decision 6 — LLM inference call: delegated to an external script, not embedded

- **Decision**: The mechanism does not embed an LLM client. Instead, it shells out to an external command (path resolved from `$EDA_LLM_INFERENCE_BIN` env var, defaulting to a documented location) that takes the textual description and JSON contract on stdin/argv and prints a `Map<String, String>` of `flag → value` to stdout.
- **Rationale**:
  - Keeps the build-time Groovy code free of HTTP-client, token-handling, and SDK dependencies.
  - Lets the operator reuse whatever LLM client is already wired into their toolchain (opencode, spec-kit, etc.) without coupling the archetype to a specific provider.
  - The 5-open-question carry-over item "temperatura da LLM" is delegated to that external script's configuration — FR-012 (byte-equivalent output) is enforced by requiring the external script to set `temperature = 0`.
- **Alternatives considered**:
  - **Embed an LLM client (e.g., `com.theokanning.openai-gpt3-java`)**: rejected — adds a dependency, version-couples to a specific provider, and re-introduces the maintenance burden the assessment was trying to eliminate.
  - **HTTP call directly via `java.net.http.HttpClient`**: technically feasible, but would require embedding API key handling, retry logic, and provider-specific request shapes. Rejected for scope.

## Resolution of the 5 carried-forward open questions

From `.specify/assessments/llm-flag-inference/problem.md`:

| # | Question | Resolution |
|---|---|---|
| 1 | Temperatura da LLM | Decided in Decision 6: temperature fixed at 0 in the external inference script. Enforced by FR-012 (byte-equivalence) and FR-004's expectation of a structured proposal that the validator can re-derive deterministically. |
| 2 | Granularidade da extração de flags | Decided in Decision 3: the extractor captures `key`, `defaultValue`, `validationRegex` for every `<requiredProperty>`. The merged XML is small enough to read in full. |
| 3 | LLM propõe flag fora do contrato | Resolved by FR-009 in `spec.md`: discarded with default applied. Validated by the merger's contract-precedence rule. |
| 4 | Serialização canônica do JSON contrato | Decided in Decision 4: sorted-key serialization via `groovy-json`, SHA-256 via JDK. |
| 5 | Flags com `<defaultValue></defaultValue>` (vazio) | Resolved by FR-008 + the merger's rule: such flags are inferred from the textual description by the external inference script; if no value is provided, the validator fails the build (because `<validationRegex>` cannot be applied to an empty value consistently). This is a deliberate fail-loud behavior — the bash legacy silently prompted the operator; the new mechanism fails the build with a clear message naming the missing flag. |

## Notes

- All 6 decisions above reuse tooling already present in the project (`gmaven:2.1.1`, Groovy 4.0.28, `xml-maven-plugin:1.2.1`, `groovy-json`, `groovy-xml`). Zero new dependencies.
- The cache key derivation (Decision 4 + Decision 5) is deterministic and testable.
- The external inference script contract is documented in `contracts/cli-contract.md`.
