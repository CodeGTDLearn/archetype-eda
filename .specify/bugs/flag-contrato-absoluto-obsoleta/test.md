# Bug Verification: Flag `absolute_path_jsonfile_contract` removida — o contrato JSON já chega inline no prompt da LLM

- **Slug**: flag-contrato-absoluto-obsoleta
- **Tested**: 2026-10-23
- **Assessment**: ./assessment.md
- **Fix**: ./fix.md
- **Result**: partial

## Summary

O sintoma original está **eliminado e verificado por reprodução direta**: o mecanismo deixou de abortar com `exit code 4` (FR-018, *"empty defaultValue"*) e passou a emitir uma linha de comando com as **32** flags do catálogo, **sem** `absolute_path_jsonfile_contract` nem `enable_json_entity_contracts`; o build completo e os **10** perfis de integração continuam verdes. Porém, a correção **descobriu um defeito latente pré-existente** no caminho de leitura do cache: `cacheLookup` estoura a pilha (`StackOverflowError`) ao processar o relatório de proveniência real (1965 caracteres), de modo que **toda invocação repetida falha com exit 1** — e uma única linha envenenada invalida o cache inteiro, inclusive para entradas diferentes. O defeito está em código **não alterado** pela correção, que era inalcançável enquanto o gate FR-018 abortava tudo antes; por isso o resultado é `partial` e não `verified`.

## Checks Performed

| Check | Command / Action | Result | Notes |
|-------|------------------|--------|-------|
| Reprodução (post-fix) — comando exato do assessment | `'{"id":1024,…}' \| groovy src/main/groovy/eda/postgenerate/llmflag/Orchestrator.groovy --use-case-description "criar entidade de teste" --use-case-contract` | **pass** | `exit 3` (antes: `exit 4`). O abort FR-018 (*"empty defaultValue"*) **não ocorre mais**; a execução avança até o passo de inferência e falha ali por limitação do Windows (stub `.sh` não executável via `CreateProcess`, erro 193) — pré-existente e documentada no `fix.md`. |
| Reprodução (post-fix) — com stub executável | mesmo comando + `--inference-bin <stub .cmd>` + `--cache-dir <tmp>` | **pass** | `exit 0`. stdout = 1 linha de comando com **32** flags; `absolute_path_jsonfile_contract` **ausente**; `enable_json_entity_contracts` **ausente**; `-Dentity_name_package=myentity` presente. |
| Cache HIT (2ª invocação, mesmo input) | mesma repetição com o mesmo `--cache-dir` | **fail** | `exit 1` — `java.lang.StackOverflowError` em `cacheLookup`. Ver *Output Excerpts* e *Residual Risks*. |
| Blast radius da falha | run A (input X) → run B (**input Y**) → run C (input X) | **fail** | run A `exit 0`; run B `exit 1`; run C `exit 1`. Uma única linha longa no `cache.jsonl` envenena o cache para **todos** os inputs. |
| Novos/atualizados testes | `groovy src/test/groovy/eda/postgenerate/llmflag/FlagCatalogExtractorTest.groovy` | **pass** | ALL CHECKS PASSED — 15 checks, 0 falhas. |
| Testes (cache) | `groovy …/OrchestratorCacheTest.groovy` | **pass** | ALL CHECKS PASSED — 28 checks, 0 falhas. Cobre 4.1-4.6 (impressão digital do catálogo, chave legada inalcançável). |
| Testes (regressão do merge/proveniência) | `groovy …/ProvenanceReportTest.groovy` | **pass** | ALL CHECKS PASSED — 20 checks, 0 falhas. |
| Suite de regressão (build + IT) | `mvn -o clean install` | **pass** | BUILD SUCCESS, `exit 0`. **10/10** perfis de IT com `Post-archetype-generation invoker exit code: 0`; `verify.groovy` executado (`Finished post-build script`); **0** ocorrências de `[ERROR]`. |
| Geração batch mode | `mvn archetype:generate --batch-mode -DarchetypeGroupId=com.eda-archetype -DarchetypeArtifactId=eda-archetype -DarchetypeVersion=0.0.1-SNAPSHOT -DartifactId=verify-gen -DgroupId=verify -Dversion=0.1-SNAPSHOT -Dpackage=it.pkg` + a linha de comando emitida pelo mecanismo | **pass** | BUILD SUCCESS, `exit 0`. Entidade gerada em `src/main/java/it/pkg/myentity/MyEntity.java` (mais `MyEntityController/Repository/Service`, `ApiStarter`, `config/**`). Sem aviso de "Unknown property" nem de "property is required". `src/main/resources/contracts/` **ausente**. |
| Invariantes do catálogo | leitura de `target/classes/META-INF/maven/archetype-metadata.xml` | **pass** | 32 flags · 0 defaults vazios · `absolute_path_jsonfile_contract` ausente · `enable_json_entity_contracts` ausente · `entity_name_package` = `myentity`. |
| Pós-processador composto | varredura de `target/classes/META-INF/archetype-post-generate.groovy` | **pass** | **0** referências a `absolute_path_jsonfile_contract`, `enable_json_entity_contracts`, `srcMainResourcesContracts`, `jsonfileEntityContractSourcePath`, `keep_json_entity_contracts`, `json_entity_contracts_folder`. |
| Invalidação do cache legado | `groovy …/verify-cache-invalidation.groovy` (script temporário) | **pass** | chave antiga `fa2f1cdf…` ≠ chave nova `89330dc7…`; `cacheLookup` com a chave nova **não** serviu a entrada legada → entrada legada **inalcançável**. |
| Varredura mecânica de referências | `Select-String` em `*.xml,*.groovy,*.properties,*.sh` (todo o repo) | **pass** | 11 ocorrências, **todas intencionais**: 4 no artefato composto defasado (`src/main/resources/META-INF/archetype-post-generate.groovy`, excluído do build por `pom.xml:40`) e 7 nos guards de regressão do `FlagCatalogExtractorTest.groovy`. |
| Lint / type-check | — | **not-run** | O projeto não configura lint, formatter nem type-check (o `pom.xml` só declara `xml-maven-plugin`, `groovy-maven-plugin`, `surefire` e `archetype-packaging`). Não há o que executar. |

## Output Excerpts

**Reprodução original (assessment, passo 1) — ANTES da correção:**

```
ERROR: Flag catalog is malformed or unreadable.
  Flag 'entity_name_package' has empty defaultValue. Catalog is invalid per FR-018.
exit code 4
```

**Mesma comando — DEPOIS da correção:**

```
ERROR: External inference script failed: Cannot run program "scripts/inference-stub.sh":
       CreateProcess error=193, %1 is not a valid Win32 application
exit code 3
```

→ O gate FR-018 foi superado; a execução alcança o passo de inferência.

**Com stub executável — exit 0, 32 flags:**

```
mvn archetype:generate --batch-mode -Denable_api_cpanel=false -Dapi_port=8080 … -Dentity_name=MyEntity
  -Dentity_name_package=myentity -Denable_custom_attributes=false
  -Dcustom_attributes_list=Long:id,MyId:id,My_Id:my_id … -Dproject_language=pt -Denable_folder_src_test_java=false
## Relatorio de Proveniencia - cache: MISS
**Resumo**: 0 CONTRACT / 0 INFERENCE / 32 DEFAULT / 2 descartada(s)
exit code 0
```

**Falha no cache HIT (2ª invocação, mesmo input):**

```
groovy : Caught: java.lang.StackOverflowError
exit code 1
```

**Isolamento da causa-raiz** (mesmo regex de `cacheLookup`, `Orchestrator.groovy:498`):

```
linha real (32 flags)          v=1049  r=1965  -> StackOverflowError
v real + r pequeno             v=1049  r=100   -> MATCH
v pequeno + r real (32 flags)  v=100   r=1965  -> StackOverflowError
limiar: campo greedy aceita ate 1726 chars; 1727 estoura a pilha
```

Stack trace (recursão do `java.util.regex`):

```
at java.base/java.util.regex.Pattern$CharProperty.match(Pattern.java:4106)
at java.base/java.util.regex.Pattern$Branch.match(Pattern.java:4914)
at java.base/java.util.regex.Pattern$GroupHead.match(Pattern.java:4969)
at java.base/java.util.regex.Pattern$Loop.match(Pattern.java:5078)   <-- 1 frame por caractere
```

## Residual Risks

- **🔴 CRÍTICO — caminho de cache HIT quebrado (defeito pré-existente, exposto pela correção).**
  `cacheLookup` (`src/main/groovy/eda/postgenerate/llmflag/Orchestrator.groovy:490-520`) valida cada linha do `cache.jsonl` com o regex
  `^\{"k":"([0-9a-f]{64})","v":"((?:[^"\\]|\\.)*)","r":"((?:[^"\\]|\\.)*)","t":(\d+)\}$`.
  O grupo `((?:[^"\\]|\\.)*)` é um `*` guloso sobre um grupo alternado; o `java.util.regex` compila isso como `Pattern$Loop`, **recursivo**, com **1 frame de pilha por caractere**. O relatório de proveniência real (`r`) tem **1965** caracteres — acima do limite medido de **1726** — e o `StackOverflowError` derruba a invocação (`exit 1`).
  * **Não foi introduzido pela correção*: o regex e o método `cacheLookup` não foram alterados; a correção mudou apenas `makeCacheKey()` e adicionou `fingerprintCatalog()`.
  * **Era inalcançável antes*: o gate FR-018 abortava com `exit 4` em `extractCatalog()` (`Orchestrator.groovy:107-120`) **antes** da consulta de cache (`:122-140`). O defeito era latente.
  * **Pré-existência demonstrada**: o relatório cresce com o número de flags — o formato anterior (34 flags) produz um `r` **maior** (~2100 chars), que também excederia o limiar.
  * **Consequência operacional**: FR-011 (servir do cache), FR-012 (byte-equivalência) e FR-020 (relatório no cache hit) **não funcionam na prática**; toda invocação repetida re-executa a inferência da LLM (custo + risco de não-determinismo) ou falha com `exit 1`.
  * **Raio de alcance amplo**: uma única linha longa envenena o `cache.jsonl` inteiro — um input diferente também falha (verificado: run B com input Y → `exit 1`).
  * **Direção de correção sugerida** (não aplicada — este comando não altera código): substituir o parsing por regex por `JsonSlurper.parseText(line)` (o formato gravado por `cacheWrite` já é JSON válido, conforme `cli-contract.md` §3), ou eliminar a recursão do `Loop` (grupo atômico/possessivo). Um teste que fixe o limite atual passaria com um `r` de ~100 chars — por isso o `OrchestratorCacheTest` **não** detectou o problema.
- **Limitação de ambiente não coberta**: o stub padrão `scripts/inference-stub.sh` não é executável no Windows (`CreateProcess error=193`), logo o caminho exato do `fix.md` não pôde ser exercitado aqui; a validação end-to-end usou um stub `.cmd` temporário **fora** do repositório. O comportamento em POSIX (onde o stub roda) não foi verificado.
- **Geração sem coordenadas explícitas do arquétipo**: `mvn archetype:generate --batch-mode -D…` **sem** `-DarchetypeGroupId/-DarchetypeArtifactId/-DarchetypeVersion` resolve o arquétipo pelo catálogo local (`archetype-catalog.xml`, cuja entrada não declara `<version>`) e produz um projeto com **apenas** o `App.java` padrão do plugin — sem entidade, sem `config/`. Com coordenadas explícitas a geração está correta (verificado). É **pré-existente e alheio a esta correção** (não toquei em `<resources>`, no layout do `archetype-resources` nem na empacotagem), mas afeta diretamente a usabilidade da linha de comando emitida pelo mecanismo.
- **Artefato composto defasado**: `src/main/resources/META-INF/archetype-post-generate.groovy` ainda contém o código antigo (4 ocorrências). É **inerte** — excluído do build por `pom.xml:40` e regenerado em `target/classes/` pelo `SnippetComposer` —, mas confunde leitores. Follow-up já registrado no `fix.md`.
- **Bug adjacente ainda aberto**: `contrato-json-inferido-inoperante` — o gate FR-009 (`Orchestrator.groovy:248`, `:279`) exige chave **literal** no contrato, então um contrato de domínio (`entityName`, `attributes`) continua não alimentando flag nenhuma. Verificado nesta sessão: o resumo de proveniência mostra `0 CONTRACT / 0 INFERENCE / 32 DEFAULT / 2 descartada(s)` — a LLM propôs `api_port` e `enable_custom_attributes` e **ambas foram descartadas** por não constarem literalmente no contrato.
- **`entity_name_package` desacoplado de `entity_name`**: default fixo `myentity` (aceito por decisão do maintainer). O pacote gerado foi `it/pkg/myentity` — correto quando o operador passa `-Dentity_name_package=<entity_name em minúsculas>`, mas `myentity` se a flag for omitida.

## Recommendation

**Manter o bug corrigido e abrir um bug separado para o defeito do cache — resultado `partial`.**
O sintoma reportado está comprovadamente eliminado: o `exit code 4` por FR-018 não reproduz mais, a linha de comando emitida contém as 32 flags corretas e nenhuma das flags removidas, o build e os 10 perfis de integração estão verdes, e o `contracts/` não é mais materializado no projeto gerado. O que impede o `verified` é um defeito **pré-existente** (`cacheLookup` estoura a pilha com o relatório de proveniência real) que a correção tornou **alcançável**: enquanto o gate FR-018 bloqueava tudo, o caminho de cache nunca era exercido. Recomendo **reabrir o ciclo para uma correção dedicada** do parsing do `cache.jsonl` (de regex para `JsonSlurper`, com teste que use um relatório de tamanho realista ≥ 32 flags), e não alterar o assessment nem o fix já aplicados — a causa-raiz do bug relatado (flag obsoleta e sem fonte, mecanismo 100% inoperante) está resolvida e verificada.
