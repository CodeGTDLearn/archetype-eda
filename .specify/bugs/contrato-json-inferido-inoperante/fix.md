# Bug Fix: Contrato JSON dirige a entidade gerada (derivação + allowlist FR-009) e é persistido no módulo (`src/test/resources/contracts/`)

- **Slug**: contrato-json-inferido-inoperante
- **Fixed**: 2026-10-23
- **Assessment**: ./assessment.md
- **Status**: applied

## Summary

O contrato JSON que o operador entrega no prompt deixou de ser **inerte** e passou a **dirigir a entidade
gerada**: o mechanism deriva o nome da entidade, o pacote e a lista de atributos (com o tipo estimado pelo
**valor** do atributo) a partir do contrato, em vez de descartar tudo pelo portão literal do FR-009. Em
paralelo, o contrato agora é **materializado em disco** pelo mecanismo e **copiado para dentro do módulo
gerado** (`src/test/resources/contracts/contract.json`), passando a ser a base do módulo e a referência dos
testes de esquema futuros. O caminho de cache HIT — quebrado por um `StackOverflowError` que fazia **toda**
segunda invocação sair com exit 1 — foi corrigido (parser JSON em vez de regex recursiva).

## Changes

| File | Change | Notes |
|------|--------|-------|
| `src/main/groovy/eda/postgenerate/llmflag/Orchestrator.groovy` | modified | Deriva a entidade do contrato (FR-021/FR-022); *allowlist* do FR-009 para `entity_name`; nova fonte de proveniência `CONTRACT_DERIVED`; materializa o contrato em disco e emite `-Dentity_contract_path` (FR-023); guarda de chaves secret-like; `cacheLookup` passa a parsear JSON (corrige o `StackOverflowError`); novo argumento `--contract-dir`. |
| `src/main/groovy/eda/postgenerate/llmflag/FlagProposalMerger.groovy` | modified | Espelho exato da lógica acima no módulo unit-testável (o `Orchestrator` precisa ser auto-contido). A paridade é assegurada por teste. |
| `src/main/resources/META-INF/maven/archetype_metadata_properties/property-entity.xml` | modified | Nova flag `entity_contract_path` (default `none`, regex `^(none\|[^\r\n]*\.json)$`). Catálogo: 32 → **33** flags. |
| `src/main/resources/META-INF/archetype_post_generate_snippets/snippet-entity-contract.groovy` | added | Copia o contrato para `src/test/resources/contracts/contract.json` **guardado por `enable_folder_src_test_java`**; caminho absoluto ou relativo (resolvido contra o CWD e contra a raiz do projeto, subindo níveis); arquivo ausente ⇒ aviso, nunca quebra a geração. |
| `scripts/inference-stub.sh` | modified | O stub passa a **derivar a entidade do contrato** (nome + lista de atributos), exercitando o caminho ponta a ponta sem LLM real. |
| `src/test/groovy/eda/postgenerate/llmflag/ContractDerivationTest.groovy` | added test | 38 checks: derivação (mapa/lista/campos de negócio), tipo por valor, tipo declarado, restrição `java.lang`, formato estrito, `enable_custom_attributes` forçado, precedências, *allowlist*, **paridade Orchestrator × FlagProposalMerger**, materialização determinística. |
| `src/test/groovy/eda/postgenerate/llmflag/OrchestratorCacheTest.groovy` | modified | Sumário do relatório passa a contar `CONTRACT_DERIVED`; nova seção 5: regressão do `StackOverflowError` (relatório realista > 1900 chars) e linha corrompida não fatal. |
| `src/test/groovy/eda/postgenerate/llmflag/ProvenanceReportTest.groovy` | modified | Cenário E: *allowlist* do FR-009 (proposta aceita **não** aparece como descartada); flag fora da *allowlist* continua descartada. |
| `src/test/groovy/eda/postgenerate/llmflag/FlagCatalogExtractorTest.groovy` | modified | Lock do catálogo: 32 → **33** flags; checks do `entity_contract_path` (default não-vazio, default válida, aceita `.json`, rejeita o resto). |
| `src/test/resources/projects/entity-contract/*` | added | Perfil de IT: contrato com `entityName` + `attributes` (valores), `enable_folder_src_test_java=true`, `entity_contract_path` apontando para o contrato. `verify.groovy` exige: (1) o contrato existe em `src/test/resources/contracts/contract.json`; (2) conteúdo **byte a byte** igual ao contrato fonte; (3) a entidade gerada corresponde ao contrato. |
| `src/test/resources/projects/custom-attributes/archetype.properties` | modified | Passa a oferecer `entity_contract_path` — este perfil mantém `enable_folder_src_test_java=false` e passa a ser o **caso negativo** da persistência. |
| `src/test/resources/projects/custom-attributes/verify.groovy` | modified | Asserção negativa: a pasta `src/test/resources/contracts/` **não** pode existir com `enable_folder_src_test_java=false`. |
| `specs/003-llm-flag-inference/spec.md` | modified | FR-009 emendada (*allowlist*); **FR-017 emendada** (exceção do módulo gerado + regra "sem credenciais"); FR-020 emendada (fonte `CONTRACT_DERIVED` + novo formato do sumário); **FR-021** (derivação da entidade), **FR-022** (pacote acompanha a entidade), **FR-023** (persistência do contrato); Key Entities e Princípio V atualizados. |
| `specs/003-llm-flag-inference/contracts/cli-contract.md` | modified | §1: novo `--contract-dir`; §1: aviso de chaves secret-like; §2: papel do script (estimar `entity_name`; a lista de atributos é do mecanismo); §3: parser do cache; §5: *breaking changes* (33 flags, semântica do FR-009, sumário, parser, FR-017). |
| `README.md` | modified | §2 Domínio reescrito (derivação real de `entity_name_package`, `entity_contract_path`); §10 (consequência da pasta de contratos); grafo de dependências; seção "campos de negócio vs. chaves de flag" (campos de negócio **viram atributos**); lista de fixtures e `verify.groovy` corrigidas (`json-contract/` → `custom-attributes/` + `entity-contract/`). |

## Diff Highlights

Derivção determinística da entidade (Orchestrator e FlagProposalMerger):

```groovy
// Step 3: entity semantics derived from the contract
case "custom_attributes_list":
    if (!(derived.get("attributes") as List).isEmpty()) {
        value = (String) derived.get("attributeList")
        source = "CONTRACT_DERIVED"
    }
```

*Allowlist* do FR-009 (o portão literal deixa de descartar a extração da entidade):

```groovy
private boolean isInferenceEligible(String key, Map<String, Object> contract) {
    if (FR009_ALLOWLIST.contains(key)) return true          // "entity_name"
    return contract != null && contract.containsKey(key)
}
```

Canal de persistência (o contrato chega ao módulo gerado):

```groovy
// FR-021: materializado ANTES da consulta de cache (o cache HIT também precisa do arquivo)
String contractFilePath = materializeContract(contractDir, canonicalContract, contractJson)
derivedOverrides.put("entity_contract_path", contractFilePath)
```

Parser do cache (corrige o `StackOverflowError`):

```groovy
Object obj = new JsonSlurper().parseText(line)   // era um regex recursivo -> Pattern$Loop
```

## Tests Added or Updated

- `src/test/groovy/eda/postgenerate/llmflag/ContractDerivationTest.groovy` — **novo**, 38 checks:
  * `attributes` como lista / como mapa / campos de negócio avulsos ⇒ a mesma lista de atributos;
  * tipo estimado pelo valor (`Integer`/`Long`/`Double`/`Boolean`/`String`);
  * tipo declarado tem precedência; tipo fora de `java.lang` é rejeitado **com aviso**;
  * formato estrito `Tipo:nome,…` validado contra a regex do catálogo;
  * `enable_custom_attributes` forçado a `true`; pacote acompanha a entidade;
  * explicit contract value vence a derivação;
  * *allowlist*: `entity_name` aceito e **não** listado como descartado; flag fora da *allowlist* continua descartada;
  * **paridade** Orchestrator × FlagProposalMerger (6 cenários);
  * materialização determinística (mesmo contrato ⇒ mesmo caminho) e sem backslash.
- `src/test/groovy/eda/postgenerate/llmflag/OrchestratorCacheTest.groovy` — §1.9 (novo formato do sumário) e §5
  (regressão do `StackOverflowError` com relatório realista; linha corrompida não invalida o cache).
- `src/test/groovy/eda/postgenerate/llmflag/ProvenanceReportTest.groovy` — cenário E (*allowlist* do FR-009).
- `src/test/groovy/eda/postgenerate/llmflag/FlagCatalogExtractorTest.groovy` — lock de 33 flags + checks do
  `entity_contract_path`.
- `src/test/resources/projects/entity-contract/verify.groovy` — **novo**: contrato persistido, byte a byte, e
  entidade correspondente ao contrato.
- `src/test/resources/projects/custom-attributes/verify.groovy` — caso negativo: sem pasta de contratos.

## Local Verification

- `mvn -o clean install integration-test` → **BUILD SUCCESS**, exit 0 — 23 `BUILD SUCCESS`, 0 `BUILD FAILURE`;
  11 fixtures: `basic`, `binding-default-only`, `binding-list-only`, `binding-map-only`,
  `binding-mutable-only`, `binding-validated-only`, `custom-attributes`, `defaults-only`, **`entity-contract`**,
  `full`, `minimal`.
- `groovy src/test/groovy/eda/postgenerate/llmflag/ContractDerivationTest.groovy` → `ALL CHECKS PASSED` (exit 0).
- `groovy src/test/groovy/eda/postgenerate/llmflag/OrchestratorCacheTest.groovy` → `ALL CHECKS PASSED` (exit 0).
- `groovy src/test/groovy/eda/postgenerate/llmflag/ProvenanceReportTest.groovy` → `ALL CHECKS PASSED` (exit 0).
- `groovy src/test/groovy/eda/postgenerate/llmflag/FlagCatalogExtractorTest.groovy` → `ALL CHECKS PASSED` (exit 0).
- Mecanismo ponta a ponta (contrato de domínio, cache limpo):
  * 1ª invocação: exit 0, `-Dentity_name=ContractEntity -Dentity_name_package=contractentity
    -Denable_custom_attributes=true -Dcustom_attributes_list=Long:id,String:name,String:createdAt
    -Dentity_contract_path=<cache>/contracts/<sha256>.json`;
  * relatório: `1 CONTRACT / 4 CONTRACT_DERIVED / 0 INFERENCE / 28 DEFAULT / 8 descartada(s)`;
  * **2ª invocação (cache HIT): exit 0** (antes: `StackOverflowError`, exit 1) e linha de comando
    **byte-idêntica** (FR-012);
  * contrato de campos de negócio (estilo README): `custom_attributes_list=Integer:id,String:nome_cachorro,
    String:dono,Integer:idade,Boolean:tosado`.
- Contrato persistido verificado no projeto gerado:
  `target/test-classes/projects/entity-contract/project/entity-contract/src/test/resources/contracts/contract.json`
  (183 bytes, idêntico byte a byte ao contrato fonte); entidade gerada
  `private Integer id; private String name; private Boolean active;`.
- Caso negativo verificado: `custom-attributes` (`enable_folder_src_test_java=false`) **não** tem
  `src/test/resources/contracts/`.

## Deviations from Assessment

1. **O artefato persistido é o contrato CRU, não um snapshot derivado.** O *Preferred* do assessment recomendava
   um snapshot derivado (só nome/pacote/atributos) para não colidir com a FR-017. O maintainer decidiu
   ("cada módulo deve ser um contrato", D1) persistir o contrato **extraído do prompt**. Consequência aceita
   e registrada: a **FR-017 foi emendada** (exceção do módulo gerado) e o Princípio V foi revisto — com uma
   **guarda de chaves secret-like** no mecanismo (aviso em stderr, nunca no relatório de proveniência, para
   não violar a própria FR-017).
2. **O canal é uma flag de catálogo com o caminho do arquivo materializado** (`entity_contract_path` +
   `--contract-dir`), e não o JSON inline nem variável de ambiente — as três alternativas do assessment foram
   apresentadas e a **Opção 1** foi aprovada.
3. **O contrato NÃO sobrevive à limpeza de `src/test/resources`.** O assessment propunha que ele sobrevivesse
   a `enable_folder_src_test_java=false`; o maintainer decidiu o contrário (D2): a pasta de contratos só existe
   com a flag `true`. A implementação acompanha essa decisão (a escrita é guardada pela flag), o que também
   eliminou a necessidade de reordenar o pós-processador.
4. **Escopo ampliado (com aprovação):** a instalação do `entity-contract` (fixture nova) e o
   `ContractDerivationTest.groovy` (arquivo novo) constavam como sugestão no assessment; foram materializados
   porque as decisões D2/D3 só são prováveis por execução.
5. **Refinamento não previsto:** o caminho materializado é emitido com **barras forward** (`C:/Users/...`).
   Com backslash, o valor entre aspas chegaria ao archetype como `C:\\Users\\...` no PowerShell (que não
   processa escape de backslash) e o caminho não resolveria. Barra forward é aceita por `java.nio` em qualquer
   plataforma e por qualquer shell.
6. **Respostas às perguntas em aberto do assessment** (o maintainer aprovou seguir as recomendações):
   * forma canônica de `attributes` ⇒ **as duas** são aceitas (mapa e lista), com os campos de negócio avulsos
     como terceiro formato (fallback);
   * tipo por valor ⇒ `Integer` para inteiros que cabem em `int`, `Long` para maiores, `Double` para decimais;
   * nome da entidade ⇒ campo explícito do contrato **primeiro**, estimativa da LLM em segundo, default por
     último;
   * lista malformada ⇒ **falha ruidosa** (aviso em stderr + nota no relatório), sem exit ≠ 0 (o FR-010
     exige sempre uma linha de comando);
   * `entity_name_package` ⇒ derivado do `entity_name` (o campo `package` do contrato é um pacote completo e
     causaria dupla prefixação);
   * `cacheLookup` ⇒ corrigido **neste** bug (é pré-requisito para validar o resto).
7. **Fora do escopo, por decisão do maintainer (D3):** o *teste de esquema* que roda **dentro do projeto
   gerado** e compara a entidade com o contrato não foi implementado — o contrato é materializado e fica
   disponível; a escrita desse teste é trabalho futuro do operador. O que foi entregue agora são os **testes
   do mecanismo** (archetype): a fixture `entity-contract` e o `ContractDerivationTest`.

## Follow-ups

- **Escrever o teste de esquema no projeto gerado** (situação D3-a): uma classe de teste em
  `src/test/java/<pkg>/` que lê `src/test/resources/contracts/contract.json` do classpath de teste e afirma
  que a entidade gerada ainda corresponde. É o próximo passo natural — o contrato já está lá.
- **DONE (2026-10-23) — a matriz de testes foi auditada e as lacunas fechadas.** A auditoria
  (`C:\Users\edufi\.opencode\plan\matriz-cobertura-testes.md`) mostrou que **9 arquivos de teste
  declarados `[X]` na matriz não existiam** e que **as 4 suítes existentes não rodavam no build**
  (surefire só inclui `**/*Test.java` ⇒ `mvn test` executava ZERO testes: build verde ≠ unitários
  verdes). Execuções:
  * **T117/T118** — as suítes Groovy agora rodam **no build**: `src/test/groovy/run-all-tests.groovy`
    + execução `unit-test-runner-execute` (fase `test`) do `groovy-maven-plugin` já existente
    (zero dependência nova). As suítes trocaram `System.exit(...)` por `throw` — continuam
    executáveis isoladas (`groovy <suite>`, mesmo exit code observável) sem matar o JVM do Maven.
  * **T119** — `OrchestratorContractGateTest.groovy` (22 checks): FR-001/002/003 + ordem dos gates.
    O Orchestrator é invocado em **JVM forkada** (o `run()` chama `System.exit`) e o stub de inferência
    é gerado no temp dir — hermético, sem depender de `groovy`/`bash` no PATH.
  * **T120** — `CommandLineAssemblerTest.groovy` (15 checks): FR-010 e as escape rules do assembler.
  * **T121** — `FlagCatalogExtractorDriftTest.groovy` (12 checks): FR-013 (drift do catálogo sem
    mudança de código) + FR-018 no catálogo derivado.
  * **T122** — o fallback de resolução do contrato no pós-processador passou de 6 para 12 níveis:
    a IT `entity-contract` **não depende mais do CWD** (antes só resolvia com o CWD = raiz do repo).
  * **T123** — matriz reconciliada: nota de estado no cabeçalho (as Fases 3–11 são listas duplicadas
    das Fases 12–13), T093/T096 apontando para os arquivos que existem, e a Fase 14 registrada em
    `specs/003-llm-flag-inference/tasks.md`.
  * **Verificação**: `mvn -o clean install integration-test` → exit 0, **7/7 suítes** executadas pelo
    build (era 0), **23 `BUILD SUCCESS`, 0 `BUILD FAILURE`, 0 `[ERROR]`**, 11/11 fixtures de IT.
- **Pendente** — `OrchestratorEndToEndTest` (E2E do mecanismo com o stub do repo) e
  `InvocationCachePerfSmokeTest` (metas de performance) seguem sem arquivo; o comportamento está
  absorvido pelos testes existentes.
- **`specs/003-llm-flag-inference/data-model.md` está defasado** (a chave do cache já carrega o *fingerprint*
  do catálogo desde o bug irmão, a entrada tem o campo `r` e agora há a entidade "Materialized Contract").
  Não foi tocado por estar fora da lista de arquivos do assessment.
- **A linha de comando passou a ser específica da máquina**: `-Dentity_contract_path=<caminho local>`. É
  determinístico (mesmo contrato ⇒ mesmo caminho) e byte-idêntico entre invocações, mas deixa de ser portátil
  entre máquinas. Se isso incomodar, a alternativa é reemitir a linha sem a flag e passar o contrato por
  `EDA_ENTITY_CONTRACT_PATH` (variável de ambiente) — decisão de produto, não de implementação.
- **A pasta `src/test/resources/contracts/` continua vazia no template do archetype**
  (`archetype-resources/src/test/resources/contracts/`): ela não é materializada (diretório vazio não é
  copiado) e não é necessária — o pós-processador cria a pasta no momento da cópia. Pode ser removida do
  template para evitar confusão.
- **Vazamento pré-existente (não corrigido):** a seção "Propostas do LLM descartadas" do relatório persiste
  no cache valores derivados do contrato (ex.: `ContractEntity`). O relatório em si não carrega campos do
  contrato fora do catálogo, mas valores propostos pela LLM a partir do contrato ficam no disco do cache.
- **Defeito pré-existente, ainda exposto:** com `enable_custom_attributes=true`, o template não renderiza o
  bloco `@Id`/`@GeneratedValue` — a entidade gerada fica sem `@Id`, salvo se `id` estiver entre os atributos
  (e mesmo assim, sem a anotação). Fora do escopo deste bug, mas a derivação amplia a exposição.
