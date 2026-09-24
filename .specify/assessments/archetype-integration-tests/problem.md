# Problem Definition: Lack of Regression Coverage Across eda-archetype Flag Combinations

- **Slug**: archetype-integration-tests
- **Created**: 2026-09-25
- **Inputs used**: intake.md? yes | research.md? yes | user input only? no

## Problem Statement

Maintainers of `com.eda-archetype:eda-archetype` lack automated, repeatable assurance that changes to the 34 generation flags declared in `archetype-metadata.xml` (and the 1062-line Groovy post-processor that consumes them) continue to produce Spring Boot projects that compile, package, and surface the expected filesystem layout under combinations that differ from the single `basic` fixture already shipped. Regressions therefore surface only when downstream consumers run the affected flag combinations, by which point the cost has already shifted to the consumer.

## Affected Users & Stakeholders

- **Users — maintainers / contributors of the archetype** — they experience the problem as silent regressions: changes to `property-*.xml`, `archetype-post-generate.groovy`, or file sets pass local sanity but break flag combinations that were never exercised.
- **Users — downstream consumers of the archetype** — they experience the problem as broken generated projects (compilation failures, missing files like `.git/`, `docker/`, `ELK/`, `properties/`, `security/`; or runtime issues from unresolved post-processor branches).
- **Stakeholders — release owner / release process** — has interest in knowing which flag combinations are validated before tagging a release; has decision power over scope and timeline of any new test work. — [NEEDS CLARIFICATION: who holds this role in the current team]
- **Stakeholders — lead maintainer** — has decision power over scope and trade-offs (e.g., "pai=false ⇒ filhas ignoradas" rule, network requirement of `mvn verify`). — [NEEDS CLARIFICATION: who holds this role]

## Goals

- Catch regressions in flag handling before they reach downstream consumers.
- Make flag-combination coverage visible and reproducible (i.e., a developer can answer "what combinations are tested?" by reading a directory).
- Provide a guardrail for the post-processor Groovy transformations, since the post-processor has many conditional filesystem mutations that compile-success does not validate.
- Allow new flag combinations to be added incrementally as the surface grows, without re-architecting the test approach.

## Non-Goals

- Replacing or modifying the `bash_script/` tooling (`maven_command_line_archetype_generate.sh`, etc.) — that is a separate, already-working human-facing flow.
- Running the suite on CI in this assessment — only manual / developer-machine runs are in scope.
- Covering combinations from the security + observability family (JWT + Logback + cPanel/ELK) — explicitly deferred to a future assessment.
- Building a UI, dashboard, or external report of test results.
- Pinning or upgrading the Spring Boot / dependency versions used by the generated project — out of scope for this assessment.

## Success Metrics

- **Number of flag combinations exercised automatically** — baseline: 1 (`basic`); target: at least the 4 scenarios defined in intake (`minimal`, `full`, `json-contract`, `binding-properties`), each with all its valid sub-combinations per the `DEPENDS_ON` graph. — [source: intake.md; research.md, DEPENDS_ON extraction] (confidence: high)
- **Time-to-detect regression in `archetype-post-generate.groovy`** — baseline: end-to-end (consumer reports breakage); target: detectable within a single local run of the test suite after the change. — [ASSUMPTION] (confidence: medium)
- **Coverage of conditional transformations in `archetype-post-generate.groovy`** — qualitative metric: each branch of the `if (keep_X == 'true'|'false')` chains should be reachable from at least one fixture; the gap analysis is done by enumerating branches and matching against fixture `archetype.properties` files. — [source: research.md, pós-processador complexity] (confidence: high)
- **Maintenance cost when a new flag is added** — qualitative metric: a developer adding a new `requiredProperty` in `property-*.xml` should be able to extend at most one existing fixture (or add one new one) to cover the new flag's interactions. — [ASSUMPTION] (confidence: medium)

## Cost of Inaction

- **Continued silent regressions** — bugs in the 1062-line `archetype-post-generate.groovy` and in the 17 `property-*.xml` files keep surfacing in downstream consumers' generated projects.
- **Loss of trust in the archetype** — each unreported-by-tests regression makes the archetype look less reliable vs. alternatives like Spring Initializr or JHipster (not because they are better, but because they are more visibly maintained).
- **Refactoring avoidance** — without a safety net, contributors avoid touching `archetype-post-generate.groovy` or the file sets, so the codebase accumulates technical debt.
- **Release uncertainty** — releases are tagged without knowing how many flag combinations have been validated; the only evidence is the single `basic` fixture.

## Open Questions

- [NEEDS CLARIFICATION: Origin & Context — who raised the need and what triggered it (regression in a consumer, release-pressure, missing test for a new flag, etc.)? Carried from intake.]
- [NEEDS CLARIFICATION: Granularity of the `binding-properties` scenario — should it enumerate all ~32 valid combinations or a curated subset? Carried from research, deferred to `shape`.]
- [NEEDS CLARIFICATION: How will `mvn verify`'s Maven Central dependency be satisfied in the dev environment (online, offline with populated cache, internal mirror)? Carried from research, deferred to `shape`/`decide`.]