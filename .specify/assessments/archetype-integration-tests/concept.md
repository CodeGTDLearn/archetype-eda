# Concept: Multi-Fixture Integration-Test Matrix for eda-archetype

- **Slug**: archetype-integration-tests
- **Created**: 2026-09-25
- **Recommended option**: Option A — Curated Matrix via `maven-archetype-plugin:integration-test`

## Options

### Option A — Curated Matrix via `maven-archetype-plugin:integration-test`
- **Sketch**: Add new fixture directories under `src/test/resources/projects/` (one per scenario: `minimal`, `full`, `json-contract`, `binding-properties` and its sub-fixtures), each containing an `archetype.properties` that sets the relevant flags and a `goal.txt` with `clean verify`. For `json-contract` only, also add a `verify.groovy` that asserts on the generated entity against an external JSON contract file. The existing `basic/` fixture can be kept (or migrated to `minimal`).
- **Appetite**: small (days, not weeks) — adding fixtures is mechanical; the only non-trivial piece is the `json-contract` `verify.groovy`.
- **Trade-offs**:
  - Wins: uses the same mechanism Apache itself uses for its own archetype (`apache/maven-archetype#1032`, 33 ITs, all green); minimal new code; no new dependencies; matrix is visible in the filesystem.
  - Sacrifices: still requires `mvn verify` to resolve Spring Boot etc. from Maven Central (network/cache dependency); does not auto-cover new flags added in the future (each new flag needs a fixture edit).
  - Risks: a regression in the post-processor Groovy that does not break compilation (e.g., wrong file removed) is only caught if the fixture's `verify.groovy` (where present) inspects the filesystem — but that is exactly the carve-out already planned for `json-contract`.
- **Rabbit holes**:
  - Designing the `verify.groovy` for `json-contract` to be robust against JSON-schema variations of the contract.
  - Pinning/curating the Spring Boot version in the generated POM to reduce noise from upstream changes.
  - Avoiding over-curation: keep the `binding-properties` scenario small (one fixture per sub-module, not all ~32 combinations).

### Option B — Build a Custom Groovy/JUnit Driver in `src/test/java` that Programmatically Generates Projects
- **Sketch**: Skip the `maven-archetype-plugin:integration-test` mechanism. Write a JUnit-driven harness that, for each scenario, programmatically invokes the post-processor on a temp directory with a given flag set, then runs `mvn verify` (or skips it) and asserts on the resulting filesystem. Combinations are encoded as `@ParameterizedTest` cases.
- **Appetite**: medium (weeks) — substantial new test infrastructure, plus a programmatic wrapper around the post-processor.
- **Trade-offs**:
  - Wins: type-safe combination enumeration via JUnit parameters; no manual fixture maintenance per flag; can run subsets easily.
  - Sacrifices: re-implements the wheel — Apache already provides the `integration-test` goal; the harness becomes project-specific maintenance debt; bypasses the standard tool and may diverge from how downstream consumers actually use the archetype.
  - Risks: subtle differences between "the harness's invocation" and "what `archetype:generate` actually does" can hide the very regressions the test should catch; high implementation cost for marginal benefit.
- **Rabbit holes**:
  - Re-creating the property-resolution and post-processing pipeline outside Maven.
  - Parameter expansion in `@ParameterizedTest` (combinations, not permutations) for 34 flags is unmanageable — must curate anyway.
  - JUnit/Maven Surefire interactions with the Groovy post-processor.

### Option C — Buy Instead of Build: Adopt an External Archetype-Testing Tool
- **Sketch**: Use a third-party tool (none widely adopted — closest analog would be `archetype-testing` libraries from the Spring or JBoss ecosystems) to drive scenario testing.
- **Appetite**: small (days) to evaluate; medium if a tool is adopted and integrated.
- **Trade-offs**:
  - Wins: no new code in the repo.
  - Sacrifices: no widely-adopted tool exists for this exact use case; the standard mechanism IS `maven-archetype-plugin:integration-test` — there is nothing meaningfully "external" to buy; this is essentially a non-option.
  - Risks: adopting a niche tool creates a new external dependency with no clear migration path.
- **Rabbit holes**:
  - Searching for a tool that does not materially exist.
  - Vendor lock-in for an internal CI/quality need.

## Recommendation

**Option A — Curated Matrix via `maven-archetype-plugin:integration-test`**.

Reasons tied to goals and metrics:
1. **Directly serves the "make coverage visible" goal** — fixtures in `src/test/resources/projects/` are browsable and self-documenting.
2. **Directly serves "incremental extension" goal** — adding a new flag means editing one or a few fixtures, no test framework changes.
3. **Directly serves "guardrail for the post-processor" goal** — `verify.groovy` in the `json-contract` scenario provides the structural assertion; other scenarios catch compile-time regressions of the post-processor's output.
4. **Appetite is small** — the work is dominated by writing fixture `archetype.properties` files, which is mechanical and parallelizable.
5. **Evidence is strong** — Apache itself uses this mechanism in `apache/maven-archetype#1032` (33 ITs passing); `msgbuf/msgbuf#18` is a real-world example of the same recipe.

The novel work in this assessment is the **curation** of the matrix, not the mechanism. Option B would reinvent the mechanism for no measurable benefit; Option C is essentially a non-option in this ecosystem.

## Out of Scope (for the recommended option)

- Running the suite on CI (per intake `Out of Scope`).
- Replacing or modifying `bash_script/` tooling (per intake `Out of Scope`).
- Combinations from the security + observability family (per intake `Out of Scope`).
- Pinning or upgrading the Spring Boot / dependency versions used by the generated project.
- Building a UI, dashboard, or external report of test results.
- Auto-discovering new flags from `property-*.xml` to spawn new fixtures — manual curation is the explicit choice.

## Assumptions to Validate

- **`mvn verify` will succeed in the dev environment** — requires network access to Maven Central (or a populated local cache). To be confirmed by the implementer.
- **Curated matrix is sufficient** — the 4 scenarios × curated sub-fixtures cover the high-value combinations; not validated empirically, so the first run may surface gaps.
- **`verify.groovy` for `json-contract` is feasible with a small (~50–100 line) script** — the post-processor already writes the entity file under `__entity_name__.java`; the script must locate it and assert structural properties. Rough size estimate; not validated.
- **The existing `basic/` fixture is the only blocking prior work** — no other test infrastructure competes for the `src/test/resources/projects/` namespace.
- **`archetype-metadata.xml` and `property-*.xml` are stable enough** that the matrix does not need to be re-curated every sprint. The DEPENDS_ON graph is the source of truth; if a flag is added without updating the `DEPENDS_ON` comment, the matrix needs manual update.