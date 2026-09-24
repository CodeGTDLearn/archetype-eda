# Idea Research: Comprehensive Integration Test Suite for eda-archetype

- **Slug**: archetype-integration-tests
- **Created**: 2026-09-25
- **Evidence confidence (overall)**: medium

## Users & Demand

- **Audience**: maintainers and contributors of `com.eda-archetype:eda-archetype` (single team, internal use). No end-user signal — this is internal engineering-quality tooling, not a shipped product feature. — [ASSUMPTION] (confidence: medium)
- **Stated need vs observed behavior**: stated need is "regression coverage across flag combinations"; observed behavior is the absence of multiple fixtures (`src/test/resources/projects/basic/` is the only one). The gap is observable in the codebase. — [source: repo, `src/test/resources/projects/`] (confidence: high)

## Prior Art

- **Internal precedent** — `src/test/resources/projects/basic/` exists as a fixture with `archetype.properties` and an empty `goal.txt`. It demonstrates the integration-test mechanism is partially wired but unused beyond the baseline. — [source: repo, `src/test/resources/projects/basic/`] (confidence: high)
- **External precedent (authoritative)** — Apache Maven itself uses the same mechanism in `apache/maven-archetype`. PR `apache/maven-archetype#1032` (merged 2026-09-21) ports `verify` scripts to Groovy and reports `Passed: 33, Failed: 0` over 33 ITs. The mechanism is `archetype:integration-test` goal + `src/it/projects/<name>/{archetype.properties,goal.txt,verify.groovy}`. — [source: github.com/apache/maven-archetype#1032] (confidence: high)
- **External precedent (BeanShell → Groovy migration)** — Apache Maven tracking issue `apache/maven#13207` (2026-09) documents `3 240 verify.groovy` vs `1 628 verify.bsh` on GitHub; the deprecation/removal of BeanShell verify scripts is in progress across the whole Maven estate. Apache is migrating to Groovy for verify scripts. — [source: github.com/apache/maven#13207] (confidence: high)
- **External precedent (real-world IT replacement)** — `msgbuf/msgbuf#18` (closed 2026-09-25) is a concrete example of a project replacing the default Maven archetype IT template with a real `verify.groovy`-driven IT. Shows the same recipe is the standard fix when the default IT does not exercise the project. — [source: github.com/msgbuf/msgbuf#18] (confidence: medium)

## Market & Context

- **Tool landscape** — Alternatives for "generate a Spring Boot project from options" exist (Spring Initializr, JHipster, dropwizard-archetype), but those are *generators*, not test frameworks. For *testing an existing archetype*, the only standard mechanism is `maven-archetype-plugin` `integration-test` goal with `verify.groovy`. There is no competing ecosystem tool. — [source: github.com/apache/maven-archetype#1032] (confidence: medium)
- **Cost of doing nothing** — Regressions in the archetype (currently maintained in `archetype-post-generate.groovy`, 1062 lines) are caught only when downstream consumers run into them. The current `basic` fixture tests only one flag combination (essentially defaults). — [ASSUMPTION] (confidence: medium)
- **Tool maturity** — `maven-archetype-plugin` `integration-test` is mature and stable; the BeanShell→Groovy port (Apache `apache/maven#1032`) confirms Groovy is now the standard script language for `verify.groovy`. — [source: github.com/apache/maven#1032] (confidence: high)

## Data & Constraints

- **Surface area** — 34 `requiredProperty` flags across 17 `property-*.xml` files in `src/main/resources/META-INF/maven/archetype_metadata_properties/`. — [source: repo, count via `grep` of `requiredProperty key=`] (confidence: high)
- **Dependency graph** — Extracted from `DEPENDS_ON` comments in the same XMLs:
  - `enable_git_support` → `git_url_repo`, `git_branch_repo`
  - `enable_custom_attributes` → `custom_attributes_list`
  - `enable_json_entity_contracts` → `absolute_path_jsonfile_contract`
  - `enable_binding_properties` → `default_binding_properties`, `list_binding_properties`, `map_binding_properties`, `mutable_binding_properties`
  - `default_binding_properties` → `validated_binding_properties`
  - `enable_logback` → `logback_console_type`, `enable_logback_appender_smtp`, `enable_logback_appender_file`
  - `enable_api_cpanel` ∧ `enable_logback` → `enable_api_cpanel_elk_log_system`
  - `enable_folder_src_test_java` ∧ (any `binding_*_properties=true`) → `enable_binding_properties_tests`
  — [source: repo, `property-*.xml`] (confidence: high)
- **Pós-processador complexity** — `archetype-post-generate.groovy` (1062 lines) contains ~20 conditional `deleteFile`/`deleteFolder`/`renameFolder`/`renameFile` calls driven by flag values; some are guard-rails (e.g., remove `.git_temp/` if `enable_git_support=false`, remove `ELK/` if `enable_logback=false`, remove `elk-compose.yml`). This means "compile passes" ≠ "expected filesystem layout". — [source: repo, `archetype-post-generate.groovy`] (confidence: high)
- **Network constraint** — `mvn verify` in the generated project will resolve Spring Boot, Lombok, Logback, MongoDB driver, etc., from Maven Central. The generated POM does not pin a local repository, so the test requires network or a populated local cache. This is a structural constraint of the mechanism, not a bug in the plan. — [ASSUMPTION] (confidence: medium)
- **No CI in scope** — Per intake, CI is out of scope. — [source: intake.md] (confidence: high)

## Evidence Against the Idea

- **Maintenance burden when flags change** — Adding/removing a flag in `property-*.xml` requires updating fixtures; not auto-discovered from XML. — [ASSUMPTION] (confidence: medium)
- **`verify.groovy` not standard for `json-contract`** — The `json-contract` scenario requires a custom `verify.groovy` that reads an external file (`absolute_path_jsonfile_contract`) and asserts on generated entity contents. This is more brittle than the default `clean verify` and is the only scenario where extra asserts are required. — [source: intake.md, clarified scope] (confidence: high)
- **Fragility from upstream deps** — Tests run `mvn verify` against an unspecified Spring Boot version; if upstream changes break compilation, the test fails for reasons unrelated to the archetype. — [source: archetype-resources/pom.xml, version not pinned] (confidence: medium)
- **Combinatorial surface** — Even after applying the "pai=false ⇒ filhas ignoradas" rule, the full enumeration of `binding-properties` domain has ~32 valid combinations; the curadoria needs to pick a small set or the suite grows quickly. — [source: research, computed from DEPENDS_ON graph] (confidence: high)
- **No comparable prior art on this exact archetype shape** — A search for other Spring Boot archetypes with multi-fixture matrix testing did not surface a directly comparable project; the standard usage of `integration-test` is single-fixture smoke tests, not matrix. The idea's novelty is mostly in curating the matrix, not in the mechanism. — [source: github.com search, limited results] (confidence: low)

## Gaps & Open Questions

- [NEEDS CLARIFICATION: Origin & Context — Raised by and Trigger fields remain unspecified from intake; defer to define stage.]
- [NEEDS CLARIFICATION: Granularity of the `binding-properties` scenario — should it cover all ~32 valid combinations or a curated subset (e.g., one per sub-module)? Defer to `shape`.]
- [NEEDS CLARIFICATION: Whether `mvn verify` should be invoked offline or with a populated local cache to satisfy network dependency. Defer to `shape` / `decide`.]

## Sources

- `https://github.com/apache/maven/issues/13207` (host: github.com, policy: allowlisted) — Apache Maven tracking issue: BeanShell retirement across Maven estate; cites `maven-archetype-plugin` `archetype:integration-test` verify scripts and the 3 240 `verify.groovy` vs 1 628 `verify.bsh` ratio on GitHub.
- `https://github.com/apache/maven-archetype/pull/1032` (host: github.com, policy: allowlisted) — PR merged 2026-09-21: "Run verify scripts with Groovy directly and drop maven-script-interpreter". Documents `VerifyScriptRunner` (variables `basedir`, `scriptdir`, `context`; pass rule `null`/`true`); reports `Passed: 33, Failed: 0` over 33 ITs (32 existing + 1 new).
- `https://github.com/msgbuf/msgbuf/issues/18` (host: github.com, policy: allowlisted) — Concrete example of replacing default archetype IT with a real `verify.groovy`-driven IT.
- `https://stackoverflow.com/search?q=maven-archetype-plugin+integration-test+verify.groovy` (host: stackoverflow.com, policy: allowlisted) — fetch returned CAPTCHA gate; no results extracted. Recorded as UNVERIFIED.
- `https://maven.apache.org/...` (host: maven.apache.org, policy: auto-refused: host not on safe list) — official docs of `maven-archetype-plugin` `integration-test` goal. NOT fetched per intake direction; relied on consolidated knowledge of the mature, stable mechanism.