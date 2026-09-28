# Bug Verification: Contrato JSON dirige a entidade gerada e é persistido no módulo (`src/test/resources/contracts/`)

- **Slug**: contrato-json-inferido-inoperante
- **Tested**: 2026-10-23
- **Assessment**: ./assessment.md
- **Fix**: ./fix.md
- **Result**: verified

## Summary

O sintoma original **não reproduz mais**: o contrato deixou de ser inerte e agora dirige a entidade gerada
(`entity_name`, `entity_name_package`, `enable_custom_attributes` e `custom_attributes_list` saem do contrato, com
fonte `CONTRACT` / `CONTRACT_DERIVED` no relatório de proveniência, contra `0 CONTRACT / 0 INFERENCE / 32 DEFAULT`
antes). O caminho de cache HIT — que abortava com `StackOverflowError` (exit 1) — agora devolve a linha de comando
com exit 0 e byte-idêntica. O contrato é materializado pelo mecanismo e **copiado byte a byte** para
`src/test/resources/contracts/contract.json` no projeto gerado — e **não** é criado quando
`enable_folder_src_test_java=false`, conforme a decisão do maintainer. Nenhuma regressão foi encontrada: build
completo e as 11 fixtures de IT passam, e as 4 suítes unitárias (110 checks) passam.

A reprodução do assessment **foi executada de fato** (mesmo contrato, mesmo comando, mesmo stub), e não apenas
inferida a partir de testes.

## Checks Performed

| Check | Command / Action | Result | Notes |
|-------|------------------|--------|-------|
| Reproduction (post-fix) | Contrato de domínio do assessment por stdin → `groovy src/main/groovy/eda/postgenerate/llmflag/Orchestrator.groovy --use-case-description "..." --use-case-contract --inference-bin <stub> --cache-dir <tmp>` (cache limpo) | **pass** | exit 0; `-Dentity_name=ContractEntity -Dentity_name_package=contractentity -Denable_custom_attributes=true -Dcustom_attributes_list=Long:id,String:name,String:createdAt`; proveniência `1 CONTRACT / 4 CONTRACT_DERIVED / 0 INFERENCE / 28 DEFAULT / 8 descartada(s)`. **O sintoma original (`32 DEFAULT`, contrato inerte) não reproduz.** |
| Reproduction (cache HIT — o `StackOverflowError`) | Mesma invocação, 2ª vez (mesmo cache-dir) | **pass** | exit 0, `## Relatorio de Proveniencia - cache: HIT`, `StackOverflow` **ausente**, nenhum `ERROR:`. Antes: `StackOverflowError` + exit 1. |
| New / updated tests | `groovy src/test/groovy/eda/postgenerate/llmflag/ContractDerivationTest.groovy` | **pass** | `ALL CHECKS PASSED` — 32 checks (derivação, tipo por valor, `java.lang`, formato estrito, precedências, *allowlist*, paridade Orchestrator × FlagProposalMerger, materialização determinística). |
| New / updated tests | `groovy src/test/groovy/eda/postgenerate/llmflag/OrchestratorCacheTest.groovy` | **pass** | `ALL CHECKS PASSED` — 34 checks (inclui a regressão do `StackOverflowError` com relatório realista e linha corrompida não fatal). |
| New / updated tests | `groovy src/test/groovy/eda/postgenerate/llmflag/ProvenanceReportTest.groovy` | **pass** | `ALL CHECKS PASSED` — 24 checks (cenário E: *allowlist* do FR-009). |
| New / updated tests | `groovy src/test/groovy/eda/postgenerate/llmflag/FlagCatalogExtractorTest.groovy` | **pass** | `ALL CHECKS PASSED` — 20 checks (lock de 33 flags + `entity_contract_path`). |
| Regression suite | `mvn -o clean install integration-test` | **pass** | exit 0 — **23 `BUILD SUCCESS`, 0 `BUILD FAILURE`, 0 `[ERROR]`**; 11 fixtures: `basic`, `binding-default-only`, `binding-list-only`, `binding-map-only`, `binding-mutable-only`, `binding-validated-only`, `custom-attributes`, `defaults-only`, **`entity-contract`** (nova), `full`, `minimal`. |
| Persistência no projeto gerado (IT) | Inspeção de `target/test-classes/projects/entity-contract/project/entity-contract/` | **pass** | `src/test/resources/contracts/contract.json` existe com **191 bytes, byte a byte idêntico** ao contrato fonte; a entidade gerada tem `private Integer id; private String name; private Boolean active;` — os tipos **estimados pelos valores** do contrato (1024 → `Integer`, "Acme Corp" → `String`, true → `Boolean`). |
| Caso negativo (D2) | Varredura dos 11 projetos gerados | **pass** | Só `entity-contract` tem `src/test/resources/contracts/`; os outros 10 (incluindo `custom-attributes`, que tem `enable_folder_src_test_java=false` **e** um `entity_contract_path` válido) **não** têm a pasta. |
| Contrato inexistente (ramo "aviso, não quebrar") | Snippet real do repo executado em harness isolado com `entity_contract_path=nao/existe/contrato.json` | **pass** | `WARNING: entity_contract_path 'nao/existe/contrato.json' does not exist - the contract was NOT persisted.` e **nenhuma** pasta/criada. A geração não quebra. |
| Caminho feliz do snippet (cópia byte a byte) | Mesmo snippet, com caminho válido | **pass** | `PROBE_EXISTS=true`, `PROBE_IDENTICAL=true`, `PROBE_BYTES=191`. |
| Guarda de chaves secret-like (FR-017 / Princípio V) | Contrato com `db_password` por stdin | **pass** | `WARNING: the contract contains keys that look like secrets: db_password` em stderr; e **`db_password` e `s3cr3t` estão AUSENTES** do corpo do relatório de proveniência **e** do `cache.jsonl` em disco. |
| FR-012 (byte-equivalência) | Duas invocações com a mesma entrada | **pass** | Linhas de comando **byte-idênticas** (`True`); o arquivo materializado tem nome determinístico (`<sha256 do contrato canônico>.json`). |
| Lint / type-check | — | **skipped** | O projeto **não tem** plugin de lint/analysis (sem checkstyle, PMD, SpotBugs, Sonar no `pom.xml`). A compilação do build (Groovy 4/5) é o type-check — coberta pelo `mvn clean install` (exit 0). |

## Output Excerpts

Reprodução (post-fix) — o contrato agora dirige a entidade:

```
mvn archetype:generate --batch-mode … -Dentity_name=ContractEntity -Dentity_name_package=contractentity
  -Denable_custom_attributes=true -Dcustom_attributes_list=Long:id,String:name,String:createdAt
  -Dentity_contract_path=C:/…/contracts/2bf965b2ed7236954a88505361e2e575973aef2a0bb86fe5b67bf753597039a0.json …

| entity_name            | ContractEntity | CONTRACT        |  |
| entity_name_package    | contractentity | CONTRACT_DERIVED |  |
| enable_custom_attributes | true         | CONTRACT_DERIVED |  |
| custom_attributes_list | Long:id,String:name,String:createdAt | CONTRACT_DERIVED |  |
| entity_contract_path   | C:/…/contracts/2bf965b2….json | CONTRACT_DERIVED |  |
**Resumo**: 1 CONTRACT / 4 CONTRACT_DERIVED / 0 INFERENCE / 28 DEFAULT / 8 descartada(s)
```

Cache HIT (o defeito pré-existente que bloqueava a validação):

```
## Relatorio de Proveniencia - cache: HIT
**Resumo**: 1 CONTRACT / 4 CONTRACT_DERIVED / 0 INFERENCE / 28 DEFAULT / 8 descartada(s)
exit=0     (antes: Caught: java.lang.StackOverflowError / exit code 1)
```

Regressão:

```
mvn -o clean install integration-test  →  exit 0
BUILD SUCCESS: 23 | BUILD FAILURE: 0 | [ERROR]: 0
fixtures: basic, binding-*-only (5), custom-attributes, defaults-only, entity-contract, full, minimal
contratos persistidos: 2
```

Suítes unitárias:

```
ContractDerivationTest    exit=0 passes=32 fails=0 | ALL CHECKS PASSED
OrchestratorCacheTest     exit=0 passes=34 fails=0 | ALL CHECKS PASSED
ProvenanceReportTest      exit=0 passes=24 fails=0 | ALL CHECKS PASSED
FlagCatalogExtractorTest  exit=0 passes=20 fails=0 | ALL CHECKS PASSED
```

Persistência verificada no projeto gerado:

```
contrato existe: True
byte-a-byte identico ao contrato fonte: True  (191 vs 191 bytes)
   public class ContractEntity {
   private Integer id;
   private String name;
   private Boolean active;
```

## Residual Risks

- **A linha de comando passou a ser específica da máquina** (`-Dentity_contract_path=<caminho local>`). É
  determinístico (mesmo contrato ⇒ mesmo caminho ⇒ linha byte-idêntica), mas **não é portátil entre máquinas**.
  É a consequência aceita da Opção 1 (canal por flag), registrada como *breaking change* em `cli-contract.md` §5.
- **O `archetype:generate` standalone não pôde ser executado** neste ambiente: o archetype não é resolvido pelo
  catálogo local em modo offline ("The desired archetype does not exist"). Os dois ramos que dependem disso
  (contrato inexistente e cópia byte a byte) foram verificados executando **o snippet real do repo** com o contexto
  do pós-processador stubado; o caminho ponta a ponta está coberto pela fixture de IT `entity-contract`.
- **Limitação do ambiente, não do produto:** o `bash` disponível é o launcher do WSL e não lê caminhos Windows, o
  que impede rodar o `scripts/inference-stub.sh` (do repo) a partir do mecanismo no Windows. O stub do repo foi
  verificado separadamente com caminhos relativos (funciona); as execuções do mecanismo usaram um stub Groovy
  equivalente no diretório temporário.
- **O teste de esquema dentro do projeto gerado não existe** (situação D3-a do maintainer): o contrato está lá e
  disponível, mas quem escreve o teste que compara a entidade com o contrato é o maintainer. Nada aqui valida
  essa peça — ela é o próximo passo natural, não uma regressão.
- **`specs/003-llm-flag-inference/data-model.md` está defasado** (chave de cache com *fingerprint*, campo `r` da
  entrada, nova entidade "Materialized Contract"). Não foi tocado por estar fora da lista de arquivos do
  assessment — registrado como follow-up no `fix.md`.
- **Defeito pré-existente, ainda exposto:** com `enable_custom_attributes=true` o template não renderiza `@Id`
  (`__entity_name__.java:75-95`) — a entidade gerada fica sem `@Id`, salvo se `id` estiver entre os atributos (e
  mesmo assim sem a anotação). Fora do escopo deste bug, mas a derivação amplia a exposição.
- **Vazamento pré-existente:** a seção "Propostas do LLM descartadas" persiste no cache valores que a LLM
  derivou do contrato. O relatório em si não carrega campos do contrato fora do catálogo, e o contrato cru não é
  gravado no cache — mas valores propostos a partir dele ficam em disco.
- **A regra de tipo restrita a `java.lang`** implica que um atributo com tipo declarado fora de `java.lang`
  (ex.: `java.time.Instant`) cai para `String`, com **aviso explícito** no stderr. Ampliar a lista de tipos
  exigiria o template emitir `import`s — escopo maior, não coberto aqui.

## Recommendation

**Close the bug — verified end-to-end.** A reprodução do assessment foi executada antes e depois da correção e
deixou de reproduzir; o defeito pré-existente do cache HIT (que impedia a validação) também está resolvido; o
build completo e as 11 fixtures de IT, mais as 4 suítes unitárias, passam; e a persistência do contrato foi
verificada byte a byte no projeto gerado, com o caso negativo coberto. As pendências listadas em *Residual
Risks* (portabilidade da linha de comando entre máquinas, o `data-model.md` desatualizado e o teste de esquema
do módulo) são follow-ups conhecidos, não regressões deste fix.
