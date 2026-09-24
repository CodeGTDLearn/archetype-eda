# Decision: Multi-Fixture Integration-Test Matrix for eda-archetype

- **Slug**: archetype-integration-tests
- **Decided**: 2026-09-25
- **Verdict**: go
- **Artifacts reviewed**: intake.md? yes | research.md? yes | problem.md? yes | concept.md? yes

## Scorecard

| Criterion | Rating | Justification |
|-----------|--------|---------------|
| Problem validity | strong | Real, observable gap: 34 flags across 17 `property-*.xml` files, a 1062-line Groovy post-processor, and only one shipped fixture (`basic/`). Two distinct user groups (maintainers, downstream consumers) feel the pain. Cost of inaction articulated across four dimensions. |
| Evidence strength | strong | Authoritative external precedent: Apache's own `apache/maven-archetype#1032` (merged 2026-09-21) shows the same mechanism (`archetype:integration-test` + `verify.groovy`) passing 33 ITs; `apache/maven#13207` confirms Groovy is now the standard verify-script engine (3 240 `verify.groovy` vs 1 628 `verify.bsh` on GitHub); `msgbuf/msgbuf#18` is a real-world concrete example. Internal mapping (flag count, DEPENDS_ON graph) is fully grounded in the repo. |
| Value vs. inaction | adequate | Baseline is qualitative — no prior CI signal to compare against. The reduction in `time-to-detect` (from "consumer reports breakage" to "local run after the change") is a clear directional gain. The non-zero cost is the maintenance of fixtures when flags change; this is bounded because the matrix is curated, not exhaustive. |
| Feasibility / appetite | strong | `concept.md` Option A has appetite `small` (days). No new dependencies; no new test framework; fixtures are mechanical additions. The only non-trivial piece is the `verify.groovy` for `json-contract` (~50–100 lines, rough estimate). |
| Strategic fit | adequate | Aligns with the implicit charter of `eda-archetype` (deliver a reliable, well-tested generator). The intake explicitly excludes CI, bash-script reuse, and security+observability combinations — leaving room for future assessments without overlap. No project constitution document was found, so this is rated by inference. |
| Risk posture | adequate | Major risks identified and credibly mitigated: (1) network/cache requirement of `mvn verify` — assumed satisfiable in dev env, listed as assumption; (2) upstream Spring Boot version drift — explicitly out of scope per concept; (3) initial curation gaps — mitigated by the design's ability to add scenarios incrementally. No serious unmitigated risk. |

## Verdict & Rationale

**Go.** The problem is real and well-observed in the codebase; evidence is strong (Apache itself uses the same mechanism); the recommended concept option has small appetite and a known-good mechanism; risks are understood and bounded. None of the scorecard criteria is `weak` or `unknown`, so the bar for `go` is met cleanly.

The assessment surfaces no reason to over-invest: the curated matrix approach does the work without inventing new infrastructure (Option B) and without chasing a tool that does not exist (Option C).

## If go — Handoff to `/speckit.specify`

- **Problem**: Maintainers of `eda-archetype` lack automated, repeatable assurance that the 34 generation flags declared in `archetype-metadata.xml` (and the 1062-line Groovy post-processor that consumes them) continue to produce Spring Boot projects that compile, package, and surface the expected filesystem layout under combinations beyond the single `basic/` fixture already shipped.

- **Chosen approach**: **Option A — Curated Matrix via `maven-archetype-plugin:integration-test`**. Add fixture directories under `src/test/resources/projects/` for the 4 scenarios (`minimal`, `full`, `json-contract`, `binding-properties` and its sub-fixtures). Each fixture contains `archetype.properties` and `goal.txt` (with `clean verify`); `json-contract` additionally gets a `verify.groovy` that asserts on the entity generated from `absolute_path_jsonfile_contract`.

- **In scope / out of scope**:
  - **In scope**: creating new fixture directories under `src/test/resources/projects/`; writing per-fixture `archetype.properties` following the `DEPENDS_ON` rule (pai=false ⇒ filhas ignoradas); writing `goal.txt` with `clean verify`; for `json-contract` only, a `verify.groovy` asserting on the generated entity.
  - **Out of scope**: running on CI; modifying `bash_script/`; combinations of JWT + Logback + cPanel/ELK; pinning/upgrading Spring Boot versions in the generated POM; building UI/dashboard/reports; auto-discovering new flags from `property-*.xml`.

- **Success metrics** (from `problem.md`):
  - Quantitative: at least the 4 scenarios + their valid sub-combinations per `DEPENDS_ON` graph are exercised automatically (baseline: 1 = `basic`).
  - Quantitative (proxy): `time-to-detect` regression in `archetype-post-generate.groovy` shrinks from "consumer reports" to "local run after the change".
  - Qualitative: every conditional branch in `archetype-post-generate.groovy`'s `if (keep_X == 'true'|'false')` chains is reachable from at least one fixture.
  - Qualitative: adding a new flag requires at most one fixture edit or one new fixture.

- **Carried-forward open questions** (must be answered or accepted during specification):
  1. **Origin & Context** — who raised the need and what triggered it (regression in a consumer, release pressure, missing test for a new flag, etc.).
  2. **`binding-properties` granularity** — enumerate all ~32 valid combinations or curate one per sub-module (recommended path: curate).
  3. **Network/Central availability** — does the dev environment have reliable access to Maven Central, or must the suite run against a populated local cache / mirror?