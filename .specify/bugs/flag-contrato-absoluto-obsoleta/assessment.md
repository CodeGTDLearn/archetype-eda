# Bug Assessment: Flag `absolute_path_jsonfile_contract` obsoleta e sem fonte (o contrato JSON já chega inline no prompt da LLM)

- **Slug**: flag-contrato-absoluto-obsoleta
- **Created**: 2026-10-23
- **Source**: pasted text (relatório do operador, em português)
- **Verdict**: valid
- **Severity**: high

## Report (verbatim)

> "O operador fornece um contrato JSON junto da descrição do caso de uso, no prompt do mecanismo de inferência LLM. Dessa maneira a flag absolute_path_jsonfile_contract não tem mais utilidade. Because a utility dessa flag era eu indicar o caminho do contrato, que seria a fonte da verdade para extrair o nome da entidade e os atributos da entidade. A custom list attributos. And agora essas informações já vem no prompt da LLM no processo de inferência da LLM."

## Symptom

A flag `absolute_path_jsonfile_contract` está **sem fonte** (*un-sourced*): nenhum código do repositório a preenche, e o contrato que ela deveria apontar **já chega inline** ao mecanismo (stdin, FR-002) e **já é entregue à LLM** (`runInference` → `--use-case-contract-file`). Como ela é uma `<requiredProperty>` do archetype, o operador é obrigado a passar na linha de comando um caminho **sem sentido** (o README prescreve o dummy `/dev/null/null.json`), mesmo com `enable_json_entity_contracts=false`.

Agravante: o `<defaultValue/>` do catálogo está **vazio**, o que viola a FR-018 e aborta o mecanismo inteiro com **exit code 4** antes de qualquer inferência — a flag está literalmente **inoperante**, e o mecanismo está 100% fora de operação.

Esperado: a flag deixa de ser exigida do operador. Como o contrato já vem no prompt, o caminho é redundante como **entrada**; e a extração do nome da entidade e dos atributos (`custom_attributes_list`) deve ocorrer a partir do contrato **no prompt**, sem arquivo externo.

## Reproduction

Reproduzido neste repositório em 2026-10-23 (Windows, Groovy 5.1.3, `target/classes` já construído).

1. Invocar o mecanismo com descrição + contrato (o contrato **é** inline, por stdin):

   ```powershell
   '{"id":1024,"nome":"Caramelo","dono":"Antonio"}' |
     groovy src/main/groovy/eda/postgenerate/llmflag/Orchestrator.groovy `
       --use-case-description "criar entidade de teste" --use-case-contract
   ```

   Saída observada (**exit code 4**):

   ```
   ERROR: Flag catalog is malformed or unreadable.
     Flag 'entity_name_package' has empty defaultValue. Catalog is invalid per FR-018.
     Every <requiredProperty> MUST have a non-empty <defaultValue>.
   ```

   O mecanismo aborta **antes** da inferência, **antes** do merge e **antes** de emitir qualquer linha de comando.

2. Confirmar que **nenhum** escritor da flag existe no mecanismo:

   ```powershell
   Select-String -Path "src/main/groovy/eda/postgenerate/llmflag/*.groovy" `
     -Pattern "absolute_path_jsonfile_contract"
   # → sem resultados
   ```

   A flag entra no laço genérico de `merge()`/`assemble()` apenas pelo `defaultValue` do catálogo — que hoje é vazio. Nenhum módulo do pacote `llmflag` possui lista hardcoded de flags (verificado: 0 ocorrências de `entity_name`, `enable_json`, `absolute_path`).

3. Confirmar que o contrato **já** vai para o prompt da LLM: `Orchestrator.groovy:645-654` grava o contrato em arquivo temporário e o passa como `--use-case-contract-file`; `:667-669` apaga o arquivo no `finally`.

4. Confirmar que o archetype **exige** um caminho mesmo com a feature desligada: `README.md:1617-1619` — *"Mesmo com `enable_json_entity_contracts=false`, passe sempre (validação regex do archetype): `-Dabsolute_path_jsonfile_contract=/dev/null/null.json` (dummy Linux, ignorado porque a flag é false)"*.

5. Confirmar que o contrato **não** é entregue por outro caminho: `src/main/resources/contracts/` **não existe** no archetype; o fileset `fileset-src-main-resources.xml:55-61` aponta para um diretório inexistente. Logo, `copyFile` (`archetype-post-generate.groovy:851`) é o **único** populador de `src/main/resources/contracts/` — e ele depende da flag removida.

6. Confirmar a contagem do catálogo: 34 `<requiredProperty>` em `target/classes/META-INF/maven/archetype-metadata.xml`; as duas flags com default vazio são `entity_name_package` (`:200-203`) e `absolute_path_jsonfile_contract` (`:251-254`).

7. [NEEDS CLARIFICATION: o comportamento do `maven-archetype-plugin` 3.4.1 quando uma `<requiredProperty>` é removida do catálogo mas o `-D` correspondente continua na linha de comando (ou vice-versa) não foi exercitado empiricamente — requer build do archetype. Impacto provável: aviso de propriedade desconhecida, não erro.]

## Suspected Code Paths

- `src/main/groovy/eda/postgenerate/llmflag/Orchestrator.groovy:66-89` — o contrato é lido de **stdin** (FR-002/FR-003) e fica em memória: a premissa do relato, **confirmada**.
- `src/main/groovy/eda/postgenerate/llmflag/Orchestrator.groovy:640-671` — `runInference()` entrega o contrato à LLM (`--use-case-contract-file`) e o apaga no `finally`; **não há** qualquer caminho de código que o persista de forma permanente.
- `src/main/groovy/eda/postgenerate/llmflag/` (pacote inteiro) — **0 ocorrências** de `absolute_path_jsonfile_contract`: o mecanismo **nunca** popula a flag.
- `src/main/groovy/eda/postgenerate/llmflag/Orchestrator.groovy:229-300` — `merge()`: `pickFromContract()` lê **só o nível superior** (`contract.get(key)`); o gate FR-009 (`:248`, `:279`) exige a chave **literal** no contrato.
- `src/main/groovy/eda/postgenerate/llmflag/Orchestrator.groovy:181-217` — `extractCatalog()` rejeita `<defaultValue/>` vazio (FR-018) e aborta com exit 4.
- `src/main/groovy/eda/postgenerate/llmflag/Orchestrator.groovy:462-467` — `makeCacheKey()`: a chave de cache é `sha256(description + "\0" + contrato canônico)`, **sem** incorporate qualquer assinatura do catálogo ⇒ as entradas em cache permanecem válidas (as-is) após a mudança de catálogo.
- `src/main/resources/META-INF/maven/archetype_metadata_properties/property-entity.xml:77-88` — `absolute_path_jsonfile_contract` com `<defaultValue/>` **vazio** e regex `^\/([^\/]+\/)*[^\/]+\.json$` (só caminho absoluto Linux).
- `src/main/resources/META-INF/maven/archetype_metadata_properties/property-entity.xml:64-75` — `enable_json_entity_contracts` (default `false`), cuja única função é governar o par delete/copy de `contracts/`.
- `src/main/resources/META-INF/maven/archetype_metadata_properties/property-entity.xml:30-33` — `entity_name_package` com `<defaultValue/>` **vazio** → **primeiro** a disparar o FR-018.
- `target/classes/META-INF/maven/archetype-metadata.xml:200-203, 251-254` — o catálogo construído carrega os dois defaults vazios (34 flags).
- `src/main/resources/META-INF/archetype_post_generate_body/archetype-global-variables.groovy:15` — `def absolute_path_jsonfile_contract = "${properties?.absolute_path_jsonfile_contract}"`; `:14` define `keep_json_entity_contracts`; `:16` define `json_entity_contracts_folder = "contracts"`.
- `src/main/resources/META-INF/archetype_post_generate_body/archetype-path-src-main-variables.groovy:27-28` — `Path srcMainResourcesContracts = srcMainResources.resolve(json_entity_contracts_folder)` e `Path jsonfileEntityContractSourcePath = Paths.get(absolute_path_jsonfile_contract)`.
- `src/main/resources/META-INF/archetype_post_generate_snippets/snippet-src-main-resources.groovy:9-21` — ramos `deleteFolder` / `copyFile` de `src/main/resources/contracts/`.
- `src/main/resources/META-INF/archetype-post-generate.groovy:28, 93, 840-852` — **artefato composto** (excluído do build por `pom.xml:40`, regenerado por `SnippetComposer.groovy:75` em `target/classes/`): **não** é o alvo correto da edição.
- `src/main/resources/META-INF/archetype_post_generate_body/archetype-file-functions.groovy:49-71` — `copyFile()` faz no-op silencioso quando a origem não existe (a pasta `contracts/` fica vazia).
- `src/main/resources/META-INF/maven/archetype_metadata_filesets/fileset-src-main-resources.xml:55-61` — fileset de `src/main/resources/contracts/**/*.json` que aponta para diretório **inexistente** no archetype ⇒ não substitui o `copyFile`.
- `bash_script/script_core.sh:83-100` — os 14 coletores `user_*_input*.sh` (US6/T033) eram quem perguntava o caminho do JSON ao operador; **não existe mais** ⇒ nenhum escritor legado sobrou.
- `src/test/resources/projects/json-contract/archetype.properties:13-14` — perfil de IT que fixa `enable_json_entity_contracts=true` e o caminho via placeholder de substituição manual.
- `src/test/resources/projects/{basic,binding-default-only,binding-list-only,binding-map-only,binding-mutable-only,binding-validated-only,defaults-only,full,minimal}/archetype.properties` — as outras 9 fixtures também fixam `absolute_path_jsonfile_contract=absolute_path_jsonfile_contract` (valor literal, que **viola** a regex atual).
- `src/test/groovy/eda/postgenerate/llmflag/OrchestratorContractBridgeTest.groovy:34-36, 64, 69-83, 143-154` — teste WIP que especifica a **arquitetura oposta** a este bug (`persistContractFile` + `injectContractBridge`, fonte `SYNTHETIC`, default sentinela `/dev/null/null.json`); seu check `0.1` fixa a contagem de **34** flags.
- `src/test/groovy/eda/postgenerate/llmflag/OrchestratorCacheTest.groovy:27-31` e `ProvenanceReportTest.groovy:19-22` — usam catálogos **sintéticos** inline; não dependem das 34 flags reais ⇒ não quebram com a remoção.
- `scripts/inference-stub.sh:40-42` — o stub devolve sempre o mesmo conjunto fixo de flags; não deriva nada do contrato, logo não exercita a extração descrita no relato.
- `specs/003-llm-flag-inference/spec.md:118-137` — FR-002 (contrato via stdin), FR-009 (gate), FR-017 (não persistir o contrato no VCS), FR-018 (default não-vazio). **Nenhuma FR cobre** a remoção da flag nem a extração da entidade a partir do contrato no prompt.
- `specs/003-llm-flag-inference/contracts/cli-contract.md:42` — "the JSON contract is always multi-line and is always read from stdin"; `:192` — "the 34 flag names".
- `specs/003-llm-flag-inference/spec.md:168` (SC-003), `data-model.md:84,107`, `plan.md:43,130`, `research.md:42,52`, `tasks.md:66,316,353` — contagem de **34** flags em sete pontos documentais.
- `README.md:1616-1619, 1947-1950, 2131-2132` — documentação que prescreve o dummy `/dev/null/null.json` e descreve a dependência `enable_json_entity_contracts → absolute_path_jsonfile_contract`.

## Root Cause Hypothesis

A causa raiz é uma **mudança de contrato de entrada não propagada**: o contrato deixou de ser um **arquivo em disco** (referenciado por caminho) e passou a ser **conteúdo inline entregue junto da descrição** (FR-002 → stdin → prompt da LLM). A flag `absolute_path_jsonfile_contract` é a herança do modelo antigo — existia apenas para transportar o *caminho* do contrato até o archetype, que o copiaria para `src/main/resources/contracts/`. Com o contrato já em memória e já entregue à LLM, o caminho é redundante **como entrada do operador**, e nada no código o preenche: os coletores bash que o perguntavam foram removidos (US6/T033) e o `Orchestrator` nunca teve essa responsabilidade.

Agravante: a flag continua classificada como `<requiredProperty>` no catálogo, com `<defaultValue/>` **vazio**. Isso (a) viola a FR-018 e aborta o mecanismo com exit 4 antes de qualquer inferência — o mecanismo está **100% fora de operação** — e (b) obriga o operador a passar na linha de comando um caminho sem sentido, conforme documentado em `README.md:1619`.

Observação de escopo: a extração do **nome da entidade** e dos **atributos** a partir do contrato no prompt é um problema **distinto** (e adjacente), tratado em `.specify/bugs/contrato-json-inferido-inoperante/assessment.md`. Remover a flag **não** resolve aquela extração — apenas deixa de exigir um caminho inútil. As duas correções convergem para o mesmo destino (o contrato do prompt como fonte da verdade), mas são independentes.

Confiança: **alta**. Premissa e inoperância reproduzidas por execução real (exit code 4), varredura completa do repositório (0 escritores da flag) e leitura do pós-processador, do catálogo construído e do fileset de recursos.

## Proposed Remediation

**Preferred**: remover as duas flags do fluxo de contrato JSON (`absolute_path_jsonfile_contract` e `enable_json_entity_contracts`), eliminar o default vazio remanescente, invalidar o cache legado e reformular o perfil de IT. O contrato passa a existir **apenas** no prompt da LLM; a pasta `src/main/resources/contracts/` deixa de existir no projeto gerado.

**Parte 1 — Catálogo** (`src/main/resources/META-INF/maven/archetype_metadata_properties/property-entity.xml`):
* Remover o bloco `<requiredProperty key="absolute_path_jsonfile_contract">` (linhas 77-88).
* Remover o bloco `<requiredProperty key="enable_json_entity_contracts">` (linhas 64-75).
* `entity_name_package`: `<defaultValue/>` → `<defaultValue>myentity</defaultValue>` — valor **completo e no formato correto** (casa com `^[a-z][a-z0-9_.]+$`), **eliminando a pendência do FR-018** que hoje aborta o mecanismo com exit 4. É o mesmo valor já esperado pelo check `0.4` do teste WIP.
* Resultado: o catálogo passa de **34 → 32** flags.

**Parte 2 — Pós-processador** (editar os **fragmentos**, não o arquivo composto):
* `archetype_post_generate_body/archetype-global-variables.groovy:14-16` — remover as definições de `keep_json_entity_contracts`, `absolute_path_jsonfile_contract` e `json_entity_contracts_folder`.
* `archetype_post_generate_body/archetype-path-src-main-variables.groovy:27-28` — remover `srcMainResourcesContracts` e `jsonfileEntityContractSourcePath`, agora sem uso.
* `archetype_post_generate_snippets/snippet-src-main-resources.groovy:9-21` — remover **ambos** os ramos (`deleteFolder` e `copyFile`); a pasta `contracts/` deixa de ser criada, apagada ou populada.
* **Não** editar `src/main/resources/META-INF/archetype-post-generate.groovy`: ele é excluído do build (`pom.xml:40`) e regenerado em `target/classes/` por `SnippetComposer.groovy:75`. Editá-lo gera ruído e é descartado no próximo build.

**Parte 3 — Invalidação do cache** (decisão explícita do maintainer):
* O cache é indexado por `sha256(description + "\0" + contrato canônico)` (`Orchestrator.groovy:462-467`), **sem** assinatura do catálogo ⇒ as linhas gravadas antes da remoção continuam sendo servidas, contendo o `-Dabsolute_path_jsonfile_contract=` obsoleto.
* Mecanismo preferido: incluir uma **impressão digital do catálogo** no payload da chave. Como o catálogo é lido em `:107-120` **antes** da consulta de cache em `:122-140`, a impressão digital está sempre disponível. Entradas antigas (chave sem a impressão) ficam **inalcançáveis** — invalidação sem migração e sem alteração do formato do arquivo de cache.
* Alternativa: versionar o formato da linha para v3 com o campo `"c":"<catalogHash>"` e descartar v1/v2 na leitura (força um cache miss único).
* Em qualquer caso: amendar FR-011/FR-012 (a definição da chave de cache muda) e documentar em `cli-contract.md` §3 e §5.

**Parte 4 — Fixtures de IT**: remover as linhas `enable_json_entity_contracts=...` e `absolute_path_jsonfile_contract=...` dos **10** arquivos `src/test/resources/projects/*/archetype.properties`. Observar que 9 deles usam o valor literal `absolute_path_jsonfile_contract` — que **viola** a regex atual (placeholder nunca substituído, tolerado porque a feature ficava desligada).

**Parte 5 — Perfil `json-contract` → `custom-attributes`** (reformulado e renomeado):
* Renomear o diretório `src/test/resources/projects/json-contract/` → `src/test/resources/projects/custom-attributes/`.
* `archetype.properties`: remover as duas linhas de contrato JSON; manter `entity_name=ContractEntity`, `entity_name_package=contractentity`, `enable_custom_attributes=true`, `custom_attributes_list=Long:id,String:name,String:createdAt`.
* `verify.groovy`: **manter** as asserções estruturais — elas validam a forma da entidade gerada a partir de `custom_attributes_list` (flag que permanece no catálogo), e **não** leem o arquivo JSON. O `sample-contract-for-archetype-test.json` é apenas documentação da forma e pode ser mantido como referência ou removido.
* Alterar `artifactId=json-contract` → `artifactId=custom-attributes` para coerência com o novo nome; ajustar `verify.groovy:14` (`expectedArtifactId`) em conjunto.
* A **contagem de perfis permanece 10** (renomeação, não remoção) ⇒ SC-001 de `specs/003-llm-flag-inference/spec.md:19` não muda de número, mas o **nome** citado em `:24` muda.

**Parte 6 — Contagem de flags 34 → 32**: atualizar `specs/003-llm-flag-inference/spec.md:168` (SC-003), `contracts/cli-contract.md:192`, `data-model.md:84,107`, `plan.md:43,130`, `research.md:42,52`, `tasks.md:66,316,353` (a task T077/T013 do `FlagCatalogExtractorTest` — hoje **inexistente em disco** — deve passar a esperar 32 entradas). **Não** reescrever os documentos históricos em `.specify/assessments/llm-flag-inference/` (registram o que era verdadeiro na data da avaliação).

**Parte 7 — Teste WIP**: **descartar** `src/test/groovy/eda/postgenerate/llmflag/OrchestratorContractBridgeTest.groovy` (e `sample-contract-for-archetype-test.json`, se não for mantido). Ele especifica a arquitetura oposta a esta decisão (sintetizar a flag, fonte `SYNTHETIC`). A dele saem apenas as asserções reutilizáveis — em especial o guard FR-018 (nenhum default vazio) — que devem ser portadas para um teste de catálogo.

**Parte 8 — Docs**: `README.md:1619` (remover o dummy `/dev/null/null.json`), `README.md:1947-1950` (remover a entrada da flag e reescrever a de `enable_json_entity_contracts`), `README.md:2131-2132` (remover a aresta do grafo de dependências). Atualizar `specs/001-archetype-integration-tests/` (spec.md:42,50-51,84,86,105; data-model.md:19-20,35,38,42,53-54,59; plan.md:13,16,26,62,82,91; quickstart.md:18,28,38,52,74,100; research.md:18,23,33,37-38,48; tasks.md:42,157-209,276,299,369; checklists/requirements.md:28) para o novo nome e escopo do perfil.

**Alternatives** (com trade-offs):
* **A — Sintetizar a flag a partir do prompt** (o desenho já escrito no teste WIP `OrchestratorContractBridgeTest`): o mecanismo persiste o contrato do prompt em `<cacheDir>/contracts/<sha256>/contract.json` e aponta a flag para ele (fonte `SYNTHETIC`), ligando `enable_json_entity_contracts=true`. Pro: preserva `contracts/` e o perfil de IT; resolve o FR-018 com o default sentinela `/dev/null/null.json`. Contra: **mantém a flag que o relator quer eliminar**, carrega um caminho absoluto na linha de comando (portabilidade e FR-012 byte-equivalence) e grava o contrato fora do repositório — exigindo emenda explícita da FR-017.
* **B — Deprecar sem remover**: manter as duas flags com defaults sentinela, marcadas como obsoletas no catálogo e no README. Pro: menor blast radius — o `archetype:generate` em batch mode continua válido sem alteração nos consumidores. Contra: mantém 34 flags, mantém a exigência de valores sem sentido e deixa o fluxo morto documentado como vivo.

**Files likely to change**:
* `src/main/resources/META-INF/maven/archetype_metadata_properties/property-entity.xml` — remover 2 flags, dar default a `entity_name_package`.
* `src/main/resources/META-INF/archetype_post_generate_body/archetype-global-variables.groovy`
* `src/main/resources/META-INF/archetype_post_generate_body/archetype-path-src-main-variables.groovy`
* `src/main/resources/META-INF/archetype_post_generate_snippets/snippet-src-main-resources.groovy`
* `src/main/groovy/eda/postgenerate/llmflag/Orchestrator.groovy` — invalidação do cache (impressão digital do catálogo na chave).
* `src/test/resources/projects/json-contract/` → `src/test/resources/projects/custom-attributes/` (renomeado: `archetype.properties`, `verify.groovy`, `goal.txt`, `sample-contract-for-archetype-test.json`).
* `src/test/resources/projects/{basic,binding-default-only,binding-list-only,binding-map-only,binding-mutable-only,binding-validated-only,defaults-only,full,minimal}/archetype.properties` — remover as 2 linhas (9 arquivos).
* `src/test/groovy/eda/postgenerate/llmflag/OrchestratorContractBridgeTest.groovy` — **descartar**; portar o guard FR-018 para um teste de catálogo.
* `specs/003-llm-flag-inference/spec.md`, `data-model.md`, `plan.md`, `research.md`, `tasks.md`, `contracts/cli-contract.md`, `checklists/requirements.md`.
* `specs/001-archetype-integration-tests/spec.md`, `data-model.md`, `plan.md`, `quickstart.md`, `research.md`, `tasks.md`, `checklists/requirements.md`.
* `README.md`.

**Tests to add or update**:
* FR-018: nenhum `<requiredProperty>` com `<defaultValue>` vazio no catálogo real (guard herdado do teste WIP descartado, portado para um teste de catálogo dedicado).
* Contagem de flags: **32** após a remoção (ajustar o check `0.1` do teste WIP, que será descartado, e a expectation de `tasks.md` T013/T047/T077).
* Catálogo: `absolute_path_jsonfile_contract` **ausente**; `enable_json_entity_contracts` **ausente**; `entity_name_package` com default `myentity` casando com `^[a-z][a-z0-9_.]+$`.
* Mecanismo: `groovy Orchestrator.groovy --use-case-description "..." --use-case-contract` (contrato pelo stdin) ⇒ **exit 0** e uma linha de comando em stdout **sem** `-Dabsolute_path_jsonfile_contract` nem `-Denable_json_entity_contracts`.
* Cache: uma entrada gravada com a chave antiga (sem impressão digital do catálogo) **não** é servida após a mudança; a linha de comando emitida contém o catálogo vigente.
* Geração: `mvn archetype:generate` em batch mode com a linha de comando emitida ⇒ **não** falha por propriedade obrigatória ausente.
* Perfil de IT `custom-attributes`: o projeto gerado passa em `clean verify` e o `verify.groovy` confirma a forma da entidade (`Long id`, `String name`, `String createdAt`).
* Geração: o projeto gerado **não** contém `src/main/resources/contracts/`.

## Risks & Considerations

- **Quebra de API do catálogo**: remover duas `<requiredProperty>` altera a superfície pública do archetype. Consumidores externos que passem `-Dabsolute_path_jsonfile_contract=...` ou `-Denable_json_entity_contracts=...` recebem aviso de "propriedade desconhecida" do plugin — não erro. A remoção é coerente com a direção do relato e deve ser registrada como **breaking change** em `cli-contract.md` §5.
- **Perda do artefato `contracts/`**: hoje a única forma de `src/main/resources/contracts/` receber conteúdo é o `copyFile` guiado pela flag removida. Como decisão do maintainer, a pasta **deixa de existir** no projeto gerado. Consumidores que dependiam desse artefato perdem o contrato materializado — a fonte da verdade passa a ser exclusivamente o prompt da LLM (e, para o arquétipo, nenhum).
- **A extração da entidade continua pendente**: remover a flag não faz o nome da entidade e os atributos passarem a vir do contrato no prompt. Enquanto o gate FR-009 (`Orchestrator.groovy:248`, `:279`) exigir chave **literal**, um contrato de domínio (`entityName`, `attributes`) não alimenta flag nenhuma. Este bug e `contrato-json-inferido-inoperante` precisam ser corrigidos **em conjunto** para que o contrato no prompt seja de fato a fonte da verdade.
- **Regressão no perfil de IT**: o perfil `json-contract` é renomeado e perde o aspecto de contrato; o US3 de `specs/001-archetype-integration-tests/spec.md:42` ("JSON-contract generation path") deixa de existir como fluxo — o perfil passa a validar a forma da entidade via `custom_attributes_list`. Ajustar FR-003 (`spec.md:84`) e o cenário no `data-model.md`.
- **Renomeação de diretório × verificação por caminho**: `specs/001-archetype-integration-tests/tasks.md:159,209` invocam o goal com `-Darchetype.test.projects=json-contract`; o novo nome precisa ser propagado.
- **`entity_name_package` desacoplado de `entity_name`**: um default fixo (`myentity`) é válido e satisfaz o FR-018, mas o pacote/diretório dos **37 templates** em `archetype-resources/src/main/java/__entity_name_package__/` deixa de acompanhar `entity_name` (hoje o README obriga o operador a passar `-Dentity_name_package=<entity_name em minúsculas>`). Decisão do maintainer: **aceito** — a derivação automática fica fora deste escopo (bug adjacente).
- **Contagem de flags documentada em oito pontos** (`spec.md:168`, `cli-contract.md:192`, `data-model.md:84,107`, `plan.md:43,130`, `research.md:42,52`, `tasks.md:66,316,353`): esquecê-la produz inconsistência silenciosa entre documentação, contrato do script de inferência e validação.
- **Divergência com o teste WIP**: `OrchestratorContractBridgeTest.groovy` (não rastreado no git) especifica a **arquitetura oposta** (sintetizar a flag, fonte `SYNTHETIC`). A correção deve **descartá-lo**, e não tomar o WIP como verdade.
- **Cache legado servido após a remoção**: sem a Parte 3, o operador pode continuar recebendo uma linha de comando com `-Dabsolute_path_jsonfile_contract=` — que aponta para um caminho vazio e faria o `copyFile` falhar silenciosamente. A invalidação é, portanto, **necessária**, não cosmética.
- **Efeito colateral na FR-017**: não se aplica a esta correção — a FR-017 permanece integralmente respeitada, porque nada é gravado em disco (o contrato continua apenas no prompt e no cache, sob hash).
- **Risco de observabilidade**: sem a flag, não há mais nenhum registro em disco de **qual** contrato originou o projeto. O `Relatorio de Proveniencia` (FR-020) registra valores e fontes, mas por desenho (FR-017) **não** registra o conteúdo do contrato — a rastreabilidade passa a depender exclusivamente do cache (`k` = hash) e do histórico do prompt.

## Open Questions

- [RESOLVIDO] `enable_json_entity_contracts` deve ser removida junto com a flag? → **Sim, removida.**
- [RESOLVIDO] A remoção deve implicar a criação de uma pasta `src/main/resources/contracts/` vazia? → **Não. A pasta deixa de existir.**
- [RESOLVIDO] `entity_name_package` com default fixo `myentity` é aceitável? → **Sim**, sem derivação automática (fora do escopo).
- [RESOLVIDO] O perfil de IT `json-contract` deve ser removido ou reformulado? → **Reformulado e renomeado** para `custom-attributes` (o perfil nunca leu o arquivo JSON: as asserções de `verify.groovy` validam a forma da entidade via `custom_attributes_list`).
- [RESOLVIDO] O teste WIP `OrchestratorContractBridgeTest.groovy` deve ser descartado? → **Sim, descartado** (a flag é removida).
- [RESOLVIDO] A linha de comando cacheada antes da remoção deve ser invalidada explicitamente? → **Sim** (impressão digital do catálogo na chave de cache).
- [RESOLVIDO] Qual o destino da extração do nome da entidade e dos atributos a partir do contrato no prompt? → **Bug adjacente** `contrato-json-inferido-inoperante`; o gate FR-009 (`Orchestrator.groovy:248`, `:279`) exige chave literal no contrato e precisa de relaxamento allowlistado para `entity_name` / `enable_custom_attributes` / `custom_attributes_list`.
