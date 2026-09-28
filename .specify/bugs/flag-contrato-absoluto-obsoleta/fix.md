# Bug Fix: Flag `absolute_path_jsonfile_contract` removida do catálogo (o contrato JSON já chega inline no prompt da LLM)

- **Slug**: flag-contrato-absoluto-obsoleta
- **Fixed**: 2026-10-23
- **Assessment**: ./assessment.md
- **Status**: applied

## Summary

As duas flags do fluxo de contrato JSON (`absolute_path_jsonfile_contract` e `enable_json_entity_contracts`) foram removidas do catálogo do archetype, e o default vazio de `entity_name_package` foi preenchido com `myentity`. O mecanismo de inferência LLM, que abortava com **exit code 4** (FR-018) antes de qualquer inferência, voltou a operar: emite **32** flags e nenhuma linha de comando com as flags removidas. O cache legado foi invalidado explicitamente (impressão digital do catálogo na chave), e o perfil de IT `json-contract` foi reformulado e renomeado para `custom-attributes`.

## Changes

### Código / configuração (fonte da verdade)

| File | Change | Notes |
|------|--------|-------|
| `src/main/resources/META-INF/maven/archetype_metadata_properties/property-entity.xml` | modified | Removidos os blocos `enable_json_entity_contracts` (linhas 64-75) e `absolute_path_jsonfile_contract` (linhas 77-88). `entity_name_package`: `<defaultValue/>` → `<defaultValue>myentity</defaultValue>` — elimina o gate FR-018. Catálogo 34 → 32 flags. |
| `src/main/resources/META-INF/archetype_post_generate_body/archetype-global-variables.groovy` | modified | Removidas as definições `keep_json_entity_contracts`, `absolute_path_jsonfile_contract` e `json_entity_contracts_folder` (linhas 14-16). |
| `src/main/resources/META-INF/archetype_post_generate_body/archetype-path-src-main-variables.groovy` | modified | Removidos `srcMainResourcesContracts` e `jsonfileEntityContractSourcePath` (linhas 27-28). |
| `src/main/resources/META-INF/archetype_post_generate_snippets/snippet-src-main-resources.groovy` | **removed** | O arquivo continha **apenas** os ramos `deleteFolder`/`copyFile` de `contracts/`. Removido por inteiro — a pasta `contracts/` deixa de ser criada, apagada ou populada. |
| `src/main/resources/META-INF/maven/archetype_metadata_filesets/fileset-src-main-resources.xml` | modified | **Expansão de escopo** — removido o fileSet `SRC\MAIN\RESOURCES\CONTRACTS`. Sem isso o plugin criava a pasta `contracts/` **vazia** no projeto gerado, contrariando a decisão "a pasta deixa de existir". Ver *Deviations*. |
| `src/main/groovy/eda/postgenerate/llmflag/Orchestrator.groovy` | modified | Impressão digital do catálogo (SHA-256 sobre chave + default + regex de cada flag) incluída no payload da chave de cache (`makeCacheKey`) + novo método `fingerprintCatalog()`. Entradas de cache gravadas antes da mudança ficam inalcançáveis. |
| `src/main/groovy/eda/postgenerate/llmflag/FlagCatalogExtractor.groovy` | modified | **Expansão de escopo** — adicionado `import groovy.xml.XmlSlurper`. O módulo não compilava isoladamente (erro 193 / "unable to resolve class XmlSlurper"); sem isso o módulo nomeado na task T013/T047/T077 do spec não podia ser testado. Ver *Deviations*. |
| `pom.xml` | modified | **Expansão de escopo** — `<forceCreation>true</forceCreation>` na execução do `xml-maven-plugin:transform`. Ver *Deviations*. |

### Testes

| File | Change | Notes |
|------|--------|-------|
| `src/test/groovy/eda/postgenerate/llmflag/OrchestratorContractBridgeTest.groovy` | **removed** | Descartado por decisão do maintainer: especificava a arquitetura **oposta** (sintetizar a flag, fonte `SYNTHETIC`). |
| `src/test/groovy/eda/postgenerate/llmflag/FlagCatalogExtractorTest.groovy` | added | 16 checks: contagem de 32 flags, guard FR-018, ausência das duas flags, default `entity_name_package`, auto-coerência default×regex, extrator real sobre o catálogo construído, Orchestrator sem exit 4, caso negativo FR-018. |
| `src/test/groovy/eda/postgenerate/llmflag/OrchestratorCacheTest.groovy` | modified | 6 checks novos (4.1-4.6) sobre a impressão digital do catálogo e a invalidação do cache legado. |
| `src/test/resources/projects/json-contract/` → `src/test/resources/projects/custom-attributes/` | **renamed** | Perfil reformulado (decisão do maintainer). `archetype.properties`: `artifactId=custom-attributes`, `project_description=Custom attributes scenario`, `enable_custom_attributes=true`, `custom_attributes_list=Long:id,String:name,String:createdAt`; as 2 flags de contrato JSON removidas. `verify.groovy`: `expectedArtifactId = 'custom-attributes'`. `goal.txt` inalterado. `sample-contract-for-archetype-test.json` mantido como **documento de referência** da forma da entidade (não é lido em tempo de execução). |
| `src/test/resources/projects/{basic,binding-default-only,binding-list-only,binding-map-only,binding-mutable-only,binding-validated-only,defaults-only,full,minimal}/archetype.properties` | modified | 9 arquivos — removidas as linhas `enable_json_entity_contracts=` e `absolute_path_jsonfile_contract=`. |

### Documentação

| File | Change | Notes |
|------|--------|-------|
| `README.md` | modified | 4.2: removido o dummy `/dev/null/null.json`; documentado que `entity_name_package` deve ser passado pelo operador. Seção de flags: entradas das 2 flags removidas substituídas por nota de remoção (34 → 32). Grafo de dependências: aresta `enable_json_entity_contracts → absolute_path_jsonfile_contract` removida. |
| `specs/003-llm-flag-inference/spec.md` | modified | Perfil `json-contract` → `custom-attributes` (linhas 19, 24); **FR-011 emendada** (chave de cache inclui a impressão digital do catálogo) e FR-012 ajustada; SC-003: 34 → 32 flags. |
| `specs/003-llm-flag-inference/contracts/cli-contract.md` | modified | Seção 3 ganhou "Key derivation" (fórmula da chave + justificativa da impressão digital); §5: breaking changes de 2026-10-23 (chave de cache + catálogo sem as 2 flags); "34 flag names" → 32. |
| `specs/003-llm-flag-inference/{data-model,plan,research,tasks}.md` | modified | Contagem de flags 34 → 32 (7 pontos); perfil renomeado. `tasks.md`: `asserts exactly 34 entries` → `32` (3 pontos). |
| `specs/001-archetype-integration-tests/spec.md` | modified | US3 reescrita ("Custom Attributes Entity Generation"); FR-003, FR-005, Key Entities, SC-001 ajustados. |
| `specs/001-archetype-integration-tests/{data-model,plan,quickstart,research,tasks,checklists/requirements}.md` | modified | Perfil renomeado em toda a spec; Phase 5 (T015/T016/T017) reescrita para refletir a implementação real (sem substituição manual de caminho; `verify.groovy` com expectativas fixas). |

## Diff Highlights

```xml
<!-- property-entity.xml -->
-<requiredProperty key="absolute_path_jsonfile_contract">
-    <defaultValue/>
-    <validationRegex>^\/([^\/]+\/)*[^\/]+\.json$</validationRegex>
-</requiredProperty>

 <requiredProperty key="entity_name_package">
-    <defaultValue/>
+    <defaultValue>myentity</defaultValue>
     <validationRegex>^[a-z][a-z0-9_.]+$</validationRegex>
 </requiredProperty>
```

```groovy
// Orchestrator.groovy — invalidação do cache
+String catalogFingerprint = fingerprintCatalog(catalog)
-String cacheKey = makeCacheKey(description, canonicalContract)
+String cacheKey = makeCacheKey(description, canonicalContract, catalogFingerprint)

+private String makeCacheKey(String description, String canonicalContract, String catalogFingerprint) {
+    String payload = (catalogFingerprint == null ? "" : catalogFingerprint) + "\u0000" +
+                     (description == null ? "" : description) + "\u0000" +
+                     (canonicalContract == null ? "" : canonicalContract)
+    ...
+}
```

## Tests Added or Updated

- `src/test/groovy/eda/postgenerate/llmflag/FlagCatalogExtractorTest.groovy::1.1` — o catálogo tem exatamente **32** flags (trava a remoção).
- `…::1.2` — nenhum `<requiredProperty>` com `defaultValue` vazio (guard FR-018; era o que abortava com exit 4).
- `…::1.3` / `::1.4` — `absolute_path_jsonfile_contract` e `enable_json_entity_contracts` **ausentes** do catálogo.
- `…::1.6` / `::1.7` — `entity_name_package` com default `myentity`, casando com a própria `validationRegex`.
- `…::1.8` — todo `defaultValue` do catálogo satisfaz a sua própria `validationRegex` (o valor emitido na linha de comando é aceito pelo archetype).
- `…::2.1-2.3` — o `FlagCatalogExtractor` real lê o catálogo construído com 32 flags e sem as flags de contrato.
- `…::3.1` / `::3.2` — o `Orchestrator` real carrega o catálogo **sem lançar** (o caminho de exit 4 está fechado).
- `…::4.1` / `::4.2` — caso negativo FR-018: `defaultValue` vazio é rejeitado e o erro nomeia a flag.
- `src/test/groovy/eda/postgenerate/llmflag/OrchestratorCacheTest.groovy::4.1-4.3` — a chave de cache é determinística, muda com a impressão digital e **não** é alcançável pela chave legada.
- `…::4.4-4.6` — a impressão digital é estável para o mesmo catálogo, muda quando um default muda e é um sha256 hex.
- `src/test/resources/projects/custom-attributes/verify.groovy` — asserções estruturais sobre a entidade gerada a partir de `custom_attributes_list` (`Long id`, `String name`, `String createdAt`).

## Local Verification

- `mvn -o clean install -DskipTests` → **BUILD SUCCESS** (exit 0); catálogo gerado com **32** flags e **0** defaults vazios.
- `mvn -o integration-test` → **BUILD SUCCESS**; os **10** perfis de IT rodaram com `Post-archetype-generation invoker exit code: 0` (incluindo o `custom-attributes`, com `run post-build script verify.groovy` executado e finalizado).
- `mvn -o integration-test "-Darchetype.test.projects=custom-attributes"` → **BUILD SUCCESS**; `verify.groovy` executado; entidade gerada contém `private Long id;`, `private String name;`, `private String createdAt;`.
- `groovy src/test/groovy/eda/postgenerate/llmflag/FlagCatalogExtractorTest.groovy` → **ALL CHECKS PASSED** (16 checks).
- `groovy src/test/groovy/eda/postgenerate/llmflag/OrchestratorCacheTest.groovy` → **ALL CHECKS PASSED** (34 checks).
- `groovy src/test/groovy/eda/postgenerate/llmflag/ProvenanceReportTest.groovy` → **ALL CHECKS PASSED** (24 checks; sem regressão).
- `groovy src/main/groovy/eda/postgenerate/llmflag/Orchestrator.groovy --use-case-description "..." --use-case-contract` (contrato pelo stdin) → **exit 0** (antes: **exit 4**); stdout com uma única linha de comando com **32** flags e **nenhuma** referência a `absolute_path_jsonfile_contract` / `enable_json_entity_contracts`.
- Verificação da invalidação do cache (script temporário, fora do repositório): chave antiga `fa2f1cdf…` ≠ chave nova `89330dc7…`; `cacheLookup` com a chave nova **não** serviu a entrada legada → **OK, entrada legada inalcançável**.
- Verificação do pós-processador composto (`target/classes/META-INF/archetype-post-generate.groovy`): **0** referências a `absolute_path_jsonfile_contract`, `enable_json_entity_contracts`, `srcMainResourcesContracts`, `jsonfileEntityContractSourcePath`, `keep_json_entity_contracts` e `json_entity_contracts_folder`.
- Verificação do artefato gerado: `src/main/resources/contracts/` **ausente** no projeto gerado (após `mvn clean` do diretório de IT, para descartar resíduo de execuções anteriores).

### Comandos não executados (por quê)

- `groovy …/Orchestrator.groovy` com o stub padrão `scripts/inference-stub.sh` → **exit 3** (`Cannot run program "scripts/inference-stub.sh": CreateProcess error=193`). É uma **limitação pré-existente do Windows** (`ProcessBuilder` não executa scripts `.sh`), **não** uma regressão desta correção: antes da correção o mecanismo abortava ainda antes, com exit 4. A verificação end-to-end foi feita com um stub `.cmd` temporário fora do repositório.

## Deviations from Assessment

Três expansões de escopo, todas **necessárias** para satisfazer os critérios de aceitação do próprio assessment. Nenhuma toca código fora do escopo funcional do bug.

1. **`src/main/resources/META-INF/maven/archetype_metadata_filesets/fileset-src-main-resources.xml`** (não listado no assessment).
   *Motivo*: o assessment exige que "a pasta `src/main/resources/contracts/` deixa de existir" e lista o teste *"Geração: o projeto gerado **não** contém `src/main/resources/contracts/`"*. Mesmo com o snippet removido, o `maven-archetype-plugin` criava a pasta **vazia** a partir do fileSet `SRC\MAIN\RESOURCES\CONTRACTS` — comprovado em execução real (run limpo: pasta presente e vazia). O fileSet foi removido; o teste passa.

2. **`pom.xml`** (não listado no assessment).
   *Motivo*: o `xml-maven-plugin:1.2.1` tem `forceCreation` com default `false` e faz **staleness check apenas sobre o agregador** `archetype-metadata.xml` — os fragments `property-*.xml` entram por **entidades XML externas** e o mtime deles não é considerado. Consequência observada: após editar `property-entity.xml`, `mvn process-resources` **não** regenerou o catálogo (continuou com 34 flags e os defaults vazios) — a correção seria aparentemente inexistente. Com `<forceCreation>true</forceCreation>` o catálogo passa a refletir a fonte da verdade a cada build.

3. **`src/main/groovy/eda/postgenerate/llmflag/FlagCatalogExtractor.groovy`** (não listado no assessment).
   *Motivo*: o módulo usa `XmlSlurper` sem `import groovy.xml.XmlSlurper` e **não compilava isoladamente** ("unable to resolve class XmlSlurper"). Como o assessment determina criar um teste de catálogo — e o `tasks.md` do spec (T013/T047/T077) nomeia explicitamente `FlagCatalogExtractorTest` para este módulo — o import faltante foi acrescentado (1 linha). Sem ele o módulo nomeado pelo spec permaneceria intestável.

**Observação sem mudança**: `src/main/resources/META-INF/archetype-post-generate.groovy` **não** foi editado, conforme a instrução do assessment — ele é excluído do build (`pom.xml:40`) e regenerado em `target/classes/` pelo `SnippetComposer`. O arquivo versionado está defasado, mas é inerte. Ver *Follow-ups*.

## Follow-ups

- **Artefato composto defasado**: `src/main/resources/META-INF/archetype-post-generate.groovy` ainda contém o código antigo (excluído do build). Candidatos: removê-lo do repositório, ou regenerá-lo a partir dos fragmentos e passar a versioná-lo de formaderived. Decisão do maintainer.
- **Helper `copyFile` órfão**: `archetype_post_generate_body/archetype-file-functions.groovy:49-71` não é mais chamado por nenhum snippet (o único chamador era o ramo de contrato JSON). Não foi removido por estar fora da lista de arquivos do assessment.
- **`sample-contract-for-archetype-test.json`**: mantido no perfil `custom-attributes` como documento de referência da forma da entidade; não é lido em tempo de execução. Pode ser removido ou renomeado (ex.: `sample-entity-shape.json`) para não sugerir que existe um fluxo de contrato.
- **`entity_name_package` desacoplado de `entity_name`**: o default fixo `myentity` satisfaz o FR-018, mas o pacote/diretório dos 37 templates deixa de acompanhar `entity_name` — o operador/meccanismo deve sempre passar `-Dentity_name_package=<entity_name em minúsculas>`. Decisão do maintainer registrada no assessment; a derivação automática fica no bug adjacente `contrato-json-inferido-inoperante`.
- **Bug adjacente ainda aberto**: `contrato-json-inferido-inoperante` — o gate FR-009 (`Orchestrator.groovy:248`, `:279`) ainda exige chave **literal** no contrato, então um contrato de domínio (`entityName`, `attributes`) não alimenta flag nenhuma. A extração do nome da entidade e dos atributos a partir do contrato **no prompt** continua pendente e depende daquele bug.
- **Observabilidade**: não há mais registro em disco de qual contrato originou o projeto (o `Relatorio de Proveniencia` não inclui o conteúdo do contrato, por FR-017).
- **Cache**: a invalidação é uma janela única — o cache se repopula sozinho. Operadores podem apagar `cache.jsonl` à vontade.
