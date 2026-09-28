# Bug Assessment: Contrato JSON no prompt não dirige a entidade gerada (FR-009) **e** o contrato não é persistido no projeto gerado

- **Slug**: contrato-json-inferido-inoperante
- **Created**: 2026-10-23
- **Updated**: 2026-10-23 (esta versão **substitui** a anterior, escrita antes da correção do bug irmão `flag-contrato-absoluto-obsoleta`; a versão anterior cobria só a metade "contrato inerte" do relato — a metade "persistência" é nova)
- **Source**: pasted text (relatório do operador, em português)
- **Verdict**: valid
- **Severity**: high (Metade B) / medium (Metade C isolada)

## Relação com bugs adjacentes

Este assessment consolida **as duas metades ainda abertas** do mesmo relato:

| Metade do relatório | Estado | Evidência |
|---|---|---|
| *"a flag `absolute_path_jsonfile_contract` não tem mais utilidade"* | **JÁ RESOLVIDA** | A flag e `enable_json_entity_contracts` saíram do catálogo (34 → **32** flags) em `.specify/bugs/flag-contrato-absoluto-obsoleta/`. `property-entity.xml` não as declara mais; o pós-processador não referencia `absolute_path_jsonfile_contract`, `srcMainResourcesContracts`, `jsonfileEntityContractSourcePath`, `keep_json_entity_contracts` nem `json_entity_contracts_folder`; `src/main/resources/contracts/` não é mais materializado. |
| *"agora essas informações já vêm no prompt da LLM … extrair o nome da entidade e os atributos"* | **ABERTA — Metade B** | O portão FR-009 descarta **todas** as propostas da LLM quando a chave literal da flag não existe no contrato. Reproduzido em 2026-10-23: `0 CONTRACT / 0 INFERENCE / 32 DEFAULT / 5 descartada(s)`. |
| *"esse mesmo contrato deve ser persistido em `src/test/resources/contracts` … para testes no futuro que garantirão que a entidade permaneça inalterada"* | **ABERTA — Metade C** | Nada persiste o contrato (FR-017 proíbe); **não existe canal** do mecanismo para o projeto gerado; e o **destino pedido é apagado pelo próprio archetype** no caminho default. Detalhado abaixo. |

## Report (verbatim)

> "O operador fornece um contrato JSON junto da descrição do caso de uso, no prompt do mecanismo de inferência LLM.
> Dessa maneira a flag absolute_path_jsonfile_contract não tem mais utilidade. Because a utility dessa flag era eu
> indicar o caminho do contrato, que seria a fonte da verdade para extrair o nome da entidade e os atributos da
> entidade. A custom list attributos. And agora essas informações já vem no prompt da LLM no processo de inferência
> da LLM. **esse mesmo contrato deve ser persistido em src\test\resources\contracts (essa arvore de pastas e do
> arquetipo gerado) pois esse contrato persistido será usado para testes no futuro que garantirá que a entidade
> permaneça inalterada, pois uma vez gerada a classe Java da entidade, ela passa a ser a fonte da verdade, E toda e
> qualquer alteração a essa classe, que por sua vez representa a pedra angular de todos outros recursos do módulo, a
> alteração a essa classe deve ser burocratizada e esse contrato e espécies que garantirão que a classe segue o
> contrato serão essa ferramenta de burocratização.**"

Direcionamento do maintainer sobre o comportamento desejado (paraphrased da resposta de 2026-10-23):

* O contrato no prompt é a **fonte da verdade** e deve ser lido **fielmente**.
* Os **atributos** descritos no contrato passam a ser **os atributos da entidade**; o **valor** de cada
  atributo permite **estimar o tipo** do atributo na entidade.
* O **nome da entidade** é **estimado** a partir do contexto do prompt e do próprio contrato.
* Como os atributos vêm do contrato, `enable_custom_attributes` é **obrigatoriamente** habilitado.
* O **formato** do valor de `custom_attributes_list` (`Tipo:nome,Tipo:nome,…`) é **obrigatoriamente** seguido.

Direcionamento adicional (esta rodada, Metade C):

* O contrato **deve ser persistido** em `src/test/resources/contracts` **dentro da árvore do projeto gerado**.
* O contrato persistido será o **baseline** de um teste futuro que garante que a **entidade permaneça inalterada**.
* Depois de gerada, a **classe Java da entidade passa a ser a fonte da verdade**; qualquer alteração nela deve ser
  **burocratizada** — o contrato persistido é a ferramenta que garante que a classe segue o contrato.

## Symptom

### Metade B — o contrato declarado "fonte da verdade" tem efeito zero

O mecanismo de inferência **opera** (exit 0, 32 flags) mas o contrato JSON — declarado pelo operador como
*fonte da verdade* — não influencia o artefato gerado: o portão FR-009 descarta **toda** proposta da LLM cuja chave
literal não exista no contrato, e um contrato de domínio (`entityName`, `attributes[]`) nunca contém as chaves
`entity_name` nem `custom_attributes_list`. Resultado: `entity_name=MyEntity`, `entity_name_package=myentity`,
`enable_custom_attributes=false`, `custom_attributes_list=Long:id,MyId:id,My_Id:my_id` — todos **defaults do
catálogo**, com o contrato literalmente descartado.

Esperado: a entidade gerada (nome, pacote, atributos e tipos) é **construída a partir do contrato** que já chega ao
prompt da LLM; `enable_custom_attributes` é habilitado; e o valor de `custom_attributes_list` obedece rigorosamente ao
formato `Tipo:nome,…`.

### Metade C — o contrato não chega ao projeto gerado, e o destino pedido é destruído

Três fatos independentes, todos verificados:

1. **Nada persiste o contrato.** O mecanismo mantém o contrato **em memória** (`Orchestrator.groovy:66-105`); o
   único artefato em disco é o cache, que grava o **hash** da chave, a linha de comando e o relatório de proveniência
   (`Orchestrator.groovy:553-575`). O contrato cru não é gravado em lugar nenhum — coerente com a FR-017.
2. **Não existe canal do mecanismo para o projeto gerado.** O stdout do mecanismo é **exatamente uma linha de
   comando** (FR-010/FR-020), e o pós-processador do archetype só enxerga as propriedades `-D` e a árvore já gerada.
   O único mecanismo que já transportou *bytes* do contrato para o projeto gerado foi
   `absolute_path_jsonfile_contract` + `enable_json_entity_contracts` — **removidos** em 2026-10-23 pelo bug irmão.
3. **O destino pedido é apagado pelo próprio archetype, no caminho default.**
   `enable_folder_src_test_java` (default `false`) faz o pós-processador executar
   `deleteFolder(srcTestResources.toFile())` (`snippet-src-test-java.groovy:16-17`), apagando `src/test/resources/`
   **inteiro** — contratos incluídos. 6 dos 10 perfis de IT têm a flag em `false`, inclusive o perfil
   `custom-attributes`, que é justamente o que exercita atributos customizados.

Esperado: o projeto gerado contém, em `src/test/resources/contracts/`, o contrato (ou um snapshot derivado dele) que
servirá de baseline para o teste que garante a entidade inalterada.

## Reproduction

Reproduzido neste repositório em 2026-10-23 (Windows 11, Groovy 5.1.3, JVM 21.0.11, catálogo de 32 flags já
construído em `target/classes/META-INF/maven/archetype-metadata.xml`).

1. Contrato de domínio usado (o que já existe no repositório):
   `src/test/resources/projects/custom-attributes/sample-contract-for-archetype-test.json`

   ```json
   { "entityName": "ContractEntity",
     "package": "it.pkg.contractentity",
     "attributes": [ {"name":"id","type":"Long"},
                     {"name":"name","type":"String"},
                     {"name":"createdAt","type":"java.time.Instant"} ] }
   ```

2. Stub executável que se comporta como uma LLM **bem comportada** — lê o contrato que já recebe no prompt e
   devolve as flags derivadas (script temporário **fora** do repositório,
   `C:\Users\edufi\AppData\Local\Temp\opencode\llmstub\stub.groovy` + wrapper `.cmd`, necessário porque
   `ProcessBuilder` no Windows não executa `.sh`):

   ```json
   {"entity_name":"ContractEntity","entity_name_package":"it.pkg.contractentity",
    "enable_custom_attributes":"true","custom_attributes_list":"Long:id,String:name,Instant:createdAt",
    "api_port":"8080"}
   ```

3. Invocação:

   ```powershell
   Get-Content src/test/resources/projects/custom-attributes/sample-contract-for-archetype-test.json -Raw |
     groovy src/main/groovy/eda/postgenerate/llmflag/Orchestrator.groovy `
       --use-case-description "Criar entidade ContractEntity com atributos id, name, createdAt" `
       --use-case-contract --inference-bin <stub.cmd> --cache-dir <tmp>
   ```

4. Saída observada (**exit code 0**):

   ```
   mvn archetype:generate --batch-mode … -Dentity_name=MyEntity -Dentity_name_package=myentity
     -Denable_custom_attributes=false -Dcustom_attributes_list=Long:id,MyId:id,My_Id:my_id …
   ## Relatorio de Proveniencia - cache: MISS
   …
   | entity_name            | MyEntity                    | DEFAULT |  |
   | entity_name_package    | myentity                    | DEFAULT |  |
   | enable_custom_attributes | false                     | DEFAULT |  |
   | custom_attributes_list | Long:id,MyId:id,My_Id:my_id | DEFAULT |  |

   ### Propostas do LLM descartadas (ausentes do contrato - FR-009)

   | flag | valor proposto |
   |---|---|
   | entity_name | ContractEntity |
   | entity_name_package | it.pkg.contractentity |
   | enable_custom_attributes | true |
   | custom_attributes_list | Long:id,String:name,Instant:createdAt |
   | api_port | 8080 |

   **Resumo**: 0 CONTRACT / 0 INFERENCE / 32 DEFAULT / 5 descartada(s)
   ```

   → **As 5 propostas foram descartadas**, inclusive as 4 que carregavam exatamente a entidade extraída do
   contrato. `api_port` foi descartado pelo mesmo portão e só "coincidiu" com o default `8080`.

5. **Segunda invocação (cache HIT)** — o mecanismo não entrega nenhuma linha de comando:

   ```
   Caught: java.lang.StackOverflowError
   === EXIT CODE: 1 ===
   ```

   Ver *Bloqueante: o caminho de cache HIT está quebrado* abaixo.

6. **Metade C — verificação estática do destino e do fluxo de limpeza** (sem execução, por leitura de código):

   * `src/main/resources/archetype-resources/src/test/resources/contracts/` **existe, mas VAZIA** — não está em
     nenhum fileset (`fileset-src-test-resources.xml` cobre apenas `application.yml`, `application.properties`,
     `logback-test.xml`, `banner.txt` e `src/test/resources/properties/**`), e pasta vazia não é copiada pelo
     `maven-archetype-plugin` ⇒ **hoje o diretório não é materializado no projeto gerado**.
   * `snippet-src-test-java.groovy:16-17` — `deleteFolder(srcTestJava)` **e** `deleteFolder(srcTestResources)`
     quando `enable_folder_src_test_java == 'false'`.
   * `README.md:2095-2097` — *"quando `false`, deleta `src/test/java/` e `src/test/resources/` inteiros"*.
   * Perfis de IT com `enable_folder_src_test_java=false`: `binding-default-only`, `binding-list-only`,
     `binding-map-only`, `binding-mutable-only`, `binding-validated-only`, `custom-attributes`, `defaults-only`,
     `minimal` (8 de 10). Com `true`: apenas `basic` e `full`.

### [NEEDS CLARIFICATION: o nome exato do arquivo persistido e se o contrato deve ser o cru ou um snapshot derivado]

### [NEEDS CLARIFICATION: o teste futuro que consumirá o contrato persistido fica no projeto gerado ou no suite de IT do archetype]

## Suspected Code Paths

### Metade B — causa raiz: o portão FR-009

- `src/main/groovy/eda/postgenerate/llmflag/Orchestrator.groovy:254` — passo 2 do `merge()`:
  `if (value == null && contract != null && contract.containsKey(key))`. A chave precisa existir
  **literalmente** no contrato. Um contrato de domínio nunca contém `entity_name` nem
  `custom_attributes_list` ⇒ a proposta da LLM é descartada.
- `src/main/groovy/eda/postgenerate/llmflag/Orchestrator.groovy:281-289` — coleta de `discarded`: mesma
  condição, aplicada a **todas** as chaves propostas pela LLM que não constem do contrato. É o que popula
  a seção *"Propostas do LLM descartadas"* do relatório de proveniência.
- `src/main/groovy/eda/postgenerate/llmflag/Orchestrator.groovy:299-306` — `pickFromContract()`: lê apenas
  o **nível superior** do contrato (`contract.get(key)`). Um contrato de domínio (`entityName`,
  `attributes[]`) nunca casa com uma chave de flag.
- `src/main/groovy/eda/postgenerate/llmflag/FlagProposalMerger.groovy:61` e `:92` — **segunda cópia** da
  mesma lógica, no módulo unit-testável irmão. Qualquer correção precisa ser aplicada nos dois lugares
  (ou o mecanismo passa a divergir do módulo).

### Metade B — a premissa do relato, verificada como verdadeira

- `src/main/groovy/eda/postgenerate/llmflag/Orchestrator.groovy:671-702` — `runInference()`: o contrato
  **é** entregue ao script de inferência, escrito num arquivo temporário (`tmpContract`, linha 677) e
  passado como `--use-case-contract-file` (linhas 682-685). A LLM **tem** o contrato em mãos. O defeito
  não está na entrega, está no que o mecanismo faz com a resposta.

### Metade B — derivação do pacote, ausente

- `src/main/resources/META-INF/maven/archetype_metadata_properties/property-entity.xml:31-34` —
  `entity_name_package` com default fixo `myentity`. **Nada** o deriva de `entity_name`.
- `src/main/resources/META-INF/archetype_post_generate_body/archetype-global-variables.groovy:12` —
  o pós-processador apenas copia `properties?.entity_name`; não há derivação de pacote.
- `src/main/resources/archetype-resources/src/main/java/__entity_name_package__/__entity_name__.java:5` —
  `package ${package}.${entity_name_package};` — o valor é usado **verbatim**, sem `${entity_name,,}`.

### Metade B — formato da lista de atributos, rígido no template

- `src/main/resources/archetype-resources/src/main/java/__entity_name_package__/__entity_name__.java:37-51` —
  os campos extras são renderizados **somente** no ramo `#if(${enable_custom_attributes} == "true")`, e o
  tipo é usado **verbatim**: `private ${attrib.type} ${attrib.name};`.
- `src/main/resources/META-INF/maven/archetype_metadata_properties/property-entity.xml:60-63` — a regex do
  catálogo `^(false|[A-Z][a-zA-Z0-9_]*:[a-z][a-zA-Z0-9_]*(,…)*)$` exige `Tipo:nome` em minúscula no nome e
  tipo sem ponto.
- `src/main/resources/META-INF/maven/archetype_metadata_properties/property-entity.xml:44-47` —
  `enable_custom_attributes` default `false` — o ramo que renderiza os atributos.

### Metade B — bloqueante: o caminho de cache HIT está quebrado (pré-existente, agora alcançável)

- `src/main/groovy/eda/postgenerate/llmflag/Orchestrator.groovy:521-551` — `cacheLookup()`: valida cada
  linha do `cache.jsonl` com o regex
  `^\{"k":"([0-9a-f]{64})","v":"((?:[^"\\]|\\.)*)","r":"((?:[^"\\]|\\.)*)","t":(\d+)\}$`. O grupo
  `((?:[^"\\]|\\.)*)` é guloso sobre um grupo alternado e o `java.util.regex` o compila como
  `Pattern$Loop` **recursivo**, com 1 frame de pilha por caractere. O relatório de proveniência real tem
  ≈1965 caracteres (limiar medido ≈1726) ⇒ `StackOverflowError` ⇒ **exit 1** (reproduzido acima).
- Já registrado como risco residual em `.specify/bugs/flag-contrato-absoluto-obsoleta/test.md:90-99`. Era
  **inalcançável** enquanto o gate FR-018 abortava tudo antes da consulta de cache
  (`Orchestrator.groovy:107-120`); hoje é o caminho normal da 2ª invocação.

### Metade C — não existe canal do mecanismo para o projeto gerado

- `src/main/groovy/eda/postgenerate/llmflag/Orchestrator.groovy:110` — a única saída do mecanismo é
  `assemble(proposal)` (`:163`, `:169`), isto é, **uma linha de comando** (FR-010). Não há como o mecanismo
  escrever no projeto gerado: ele roda **antes** de `mvn archetype:generate` e não conhece o diretório
  destino.
- `src/main/resources/META-INF/maven/archetype_metadata_properties/property-entity.xml` — o catálogo tem
  hoje **32** `<requiredProperty>`; **nenhum** transporta conteúdo de contrato. O único que já fez isso
  (`absolute_path_jsonfile_contract`) foi removido — e a remoção é documentada como decisão explícita em
  `.specify/bugs/flag-contrato-absoluto-obsoleta/fix.md` ("O contrato passa a existir **apenas** no prompt da
  LLM; a pasta `src/main/resources/contracts/` deixa de existir no projeto gerado") e em `README.md:1944-1948`.
  ⇒ **A Metade C reabre uma decisão já fechada.**

### Metade C — o destino pedido é destruído pelo pós-processador

- `src/main/resources/META-INF/archetype_post_generate_snippets/snippet-src-test-java.groovy:16-17` —
  `deleteFolder(srcTestJava.toFile())` e `deleteFolder(srcTestResources.toFile())` quando
  `keep_src_test_java == 'false'` (`variables-src-test-java.groovy:3`).
- `src/main/resources/META-INF/archetype_post_generate_snippets/snippet-properties-filetype.groovy:20-21` —
  `deleteFilesByExtensionRecursivelyFromFolder(srcTestResources, deleteFileType)` apaga recursivamente `.yml`
  (ou `.properties`) em `src/test/resources` — um `.json` sobrevive a esta limpeza, mas **não** à anterior.
- `src/main/groovy/eda/postgenerate/SnippetComposer.groovy:52-65` — a seção `SNIPPETS` concatena os arquivos
  **em ordem alfabética**. Um novo snippet chamado `snippet-src-test-contracts.groovy` ordenaria **antes** de
  `snippet-src-test-java.groovy` (`c` < `j`) e seria **apagado em seguida**. A escrita precisa acontecer
  **depois** do bloco `SNIPPETS` (nova entrada em `SECTIONS`) ou em um snippet cujo nome ordene depois.
- `src/main/resources/META-INF/archetype_post_generate_body/archetype-path-src-test-variables.groovy:24-25` —
  `Path srcTestResources` e `srcTestResourcesPropertiesFolder` **já existem** → o destino é alcançável sem
  inventar paths.
- `src/main/resources/META-INF/archetype_post_generate_body/archetype-file-functions.groovy:49` —
  `copyFile(File sourceFile, File destinationFolder)` já existe como helper.

### Metade C — onde o teste futuro vive

- `src/main/resources/archetype-resources/src/test/java/__entity_name_package__/config/properties/` — o projeto
  gerado já embarca **10 classes de teste Java** (binding properties). É a casa natural do teste do contrato.
- `src/test/resources/projects/custom-attributes/verify.groovy` — o IT do archetype já valida os atributos da
  entidade gerada (`private <tipo> <nome>;`), mas com **valores fixados à mão** → cego para a Metade B.

### Metade B — cobertura de teste que não exercita o caminho do bug

- `src/test/resources/projects/custom-attributes/archetype.properties:9-12` — o perfil de IT **fixa** os
  valores esperados à mão (`entity_name=ContractEntity`, `entity_name_package=contractentity`,
  `enable_custom_attributes=true`, `custom_attributes_list=Long:id,String:name,String:createdAt`). O IT
  **jamais** pode detectar este bug.
- `src/test/groovy/eda/postgenerate/llmflag/ProvenanceReportTest.groovy:50-53` — os checks A6/A7/A8
  **afirmam** o descarte FR-009 (`log_level`, `unknown_flag` fora do contrato ⇒ descartados). Qualquer
  relaxamento do portão precisa ser reconciliado com esses checks.
- `src/test/groovy/eda/postgenerate/llmflag/FlagCatalogExtractorTest.groovy:68-69,101` — trava a ausência
  das duas flags removidas e as 32 flags do catálogo.

### Documentação — divergente do código

- `README.md:1930-1932` — afirma que `entity_name_package` é *"derivado automaticamente de `entity_name`"*,
  *"recalculado sempre que `entity_name` muda"*, e que *"o Velocity aplica `${entity_name,,}`"*. **Falso**:
  não há `${entity_name,,}` no template (`__entity_name__.java:5`) nem derivação em lugar nenhum. O default é
  o literal fixo `myentity`.
- `README.md:2131-2132` — o grafo de dependências repete a mesma aresta `entity_name → entity_name_package
  (derivada automaticamente)`.
- `README.md:1944-1950` — a nota de remoção das duas flags afirma que *"não existe mais caminho de arquivo a
  apontar"* — frase que fica obsoleta se a Metade C for implementada por reintrodução de flag.

### Especificação — lacunas

- `specs/003-llm-flag-inference/spec.md:126` — **FR-009**: *"System MUST discard any flag proposed by
  inference that is absent from the contract"*. Redigido para um contrato cujos campos **já seriam nomes
  de flags**; não cobre o contrato de domínio que o operador de fato entrega.
- `specs/003-llm-flag-inference/spec.md:118-137` — FR-001..FR-020. **Nenhuma FR** cobre derivar a entidade
  a partir do contrato, habilitar `enable_custom_attributes` por consequência, fazer o pacote acompanhar o
  nome da entidade, ou **persistir** o contrato/snapshot no projeto gerado.
- `specs/003-llm-flag-inference/spec.md:134` — **FR-017**: *"System MUST NOT persist the contract's content in
  version control; only the cache key (hash) may be persisted"*. **A Metade C colide frontalmente com esta FR**
  se o contrato **cru** for persistido no projeto gerado (que é versionado).
- `specs/003-llm-flag-inference/spec.md:192` — **Princípio V (Constituição)**: *"The JSON contract may carry
  sensitive values. … no contract content is persisted to version control."* O contrato de exemplo do próprio
  README (`README.md:1594-1600`) carrega dado de negócio (`"nome_cachorro": "Caramelo"`, `"dono": "Antonio Jose"`).
- `specs/003-llm-flag-inference/contracts/cli-contract.md:174-192` — o contrato do script de inferência
  define o formato de saída (`Map<String,String>` flag→valor) mas **não** diz que o script pode (ou deve)
  devolver valores derivados dos campos de domínio do contrato.
- `specs/003-llm-flag-inference/contracts/cli-contract.md:200-238` — o formato do cache é **JSON válido**
  por especificação, o que torna a substituição do parser por regex desnecessária.

### Vazamento sutil já presente (FR-017, pré-existente)

- `src/main/groovy/eda/postgenerate/llmflag/Orchestrator.groovy:337-348` — a seção *"Propostas do LLM
  descartadas"* grava no relatório os **valores** que a LLM derivou do contrato (verificado: `cache.jsonl`
  contém `ContractEntity`). O relatório é persistido no cache (`:176`, `cacheWrite`). Ou seja: embora o
  contrato **cru** não vaze, **valores derivados dele** vazam para o disco. Relevante porque a Metade C
  propõe justamente persistir o contrato no projeto gerado.

## Root Cause Hypothesis

### Metade B

A causa raiz é a **tese de precedência do FR-009**, escrita para um contrato cujo vocabulário coincide com o
vocabulário das flags. O gate `contract.containsKey(flag)` pressupõe que "a LLM propõe" e "o contrato declara"
são o mesmo conjunto — o que só vale se o contrato for um envelope de flags. O contrato real do operador é um
**contrato de domínio** (`entityName`, `attributes[]`), cujo vocabulário não coincide com nenhuma flag.
Resultado: a LLM extrai corretamente a entidade do contrato que já está no prompt, e o mecanismo **descarta
exatamente essa extração**; tudo cai no `defaultValue` e o contrato, que o operador declara "fonte da verdade",
fica **inerte** — `0 CONTRACT / 0 INFERENCE / 32 DEFAULT`.

Sobre isso incidem três defeitos independentes:

1. **Ausência de derivação determinística dos atributos.** O mecanismo tem um caminho para ler o contrato
   (`pickFromContract`) que só enxerga o nível superior e por *chave de flag*. Não há caminho algum que
   transforme `attributes[]` em `custom_attributes_list`. Como o relatório determina que "os atributos passam a
   ser os atributos em si da entidade" e que o tipo é estimado **a partir do valor** do atributo, essa
   estimativa — que é **mecânica** (o tipo JSON do valor → tipo Java) — precisa existir em código, e não pode
   depender da boa vontade da LLM. Sem ela, o formato `Tipo:nome` exigido pela flag nunca é garantido: a LLM
   pode devolver `Long:id,String:name` (ok), `id:Long,name:String` (invertido) ou `Long:id, Instant:createdAt`
   (espaço), e a regex do catálogo rejeita os dois últimos, caindo silenciosamente no default
   `Long:id,MyId:id,My_Id:my_id`.
2. **Ausência de derivação de `entity_name_package`.** O bug irmão trocou a derivação por um default fixo
   `myentity` (para satisfazer o FR-018). O pacote deixou de acompanhar o nome da entidade: sem
   `-Dentity_name_package` explícito, o operador obtém a classe `ContractEntity` no pacote `myentity`. O README
   ainda promete a derivação (ver *Documentação* acima).
3. **Cache HIT quebrado.** `cacheLookup` estoura a pilha com o relatório de proveniência real ⇒ a 2ª invocação
   sai com **exit 1** e não entrega linha de comando nenhuma. Isso **impede a validação de qualquer correção
   deste bug** (a verificação natural é "rode duas vezes, compare") e mantém FR-011, FR-012 e FR-020 inoperantes
   na prática.

Confiança: **alta**. Os sintomas 1 e 2 foram reproduzidos por execução real do `Orchestrator.groovy`
(exit 0, 5 propostas descartadas, defaults vencendo) e o sintoma 3 por execução real da 2ª invocação
(exit 1, `StackOverflowError`). A ausência de derivação foi confirmada por varredura do código-fonte
(template, pós-processador, catálogo) — não há caminho de código que a produza.

### Metade C

A causa raiz é uma **lacuna de canal**, agravada por uma **decisão recently fechada**. O contrato deixou de ser um
arquivo em disco (referenciado por caminho) e passou a ser **conteúdo inline entregue junto da descrição**
(FR-002 → stdin → prompt da LLM); em consequência, o único artifato que materializava o contrato dentro do projeto
gerado — `absolute_path_jsonfile_contract` + `enable_json_entity_contracts`, que o pós-processador copiava para
`src/main/resources/contracts/` — foi removido em 2026-10-23. O resultado é que hoje existe **zero** caminho de
dados entre o contrato (que vive em memória no mecanismo) e a árvore do projeto gerado. A Metade C pede
exatamente o que foi desfeito: um caminho de dados que materialize o contrato em `src/test/resources/contracts/`.

Somam-se a isso dois obstáculos concretos no destino pedido:

* **O destino é limpo pelo próprio archetype.** `enable_folder_src_test_java=false` (default) →
  `deleteFolder(srcTestResources)` — a pasta `src/test/resources/contracts/` só sobreviveria se a escrita
  ocorrer **depois** da limpeza, o que impõe uma nova posição no SECTIONS do `SnippetComposer` (ou um snippet
  com ordenação alfabética posterior a `snippet-src-test-java`).
* **A pasta de destino não existe no template materializado.** A pasta `contracts` existe **vazia** em
  `archetype-resources/src/test/resources/contracts/`, não é coberta por nenhum fileset e, por estar vazia, não é
  copiada pelo `maven-archetype-plugin` ⇒ o arquétipo não a cria no projeto gerado.

E há um conflito de especificação: a FR-017 e o Princípio V da Constituição proíbem, explicitamente, persistir
o conteúdo do contrato em controle de versão — e o projeto gerado **é** versionado. Persistir o contrato **cru**
exige, portanto, uma **emenda explícita** da FR-017 (como foi feito com a FR-011 no bug irmão), não uma mudança
silenciosa.

Confiança: **alta** para os três obstáculos (todos verificados por leitura direta do código-fonte e do template) e
**média** para o formato final do artefato persistido (raw vs. snapshot derivado), que depende de decisão do
maintainer.

## Proposed Remediation

**Preferred**: seis partes coordenadas. A ideia central da Metade B é mover para o **mecanismo** tudo o que é
determinístico (ler os atributos do contrato, estimar o tipo pelo valor, montar a lista no formato exigido,
habilitar `enable_custom_attributes`) e deixar à LLM apenas o que é genuinamente semântico (estimar o **nome** da
entidade). Na Metade C, a recomendação é materializar no projeto gerado um **snapshot derivado da forma da
entidade** — não o contrato cru — porque ele atende integralmente ao propósito declarado (baseline para o teste
que garante a entidade inalterada) **sem** reabrir a flag removida, **sem** persistir dado de negócio no VCS e
**sem** colidir com a FR-017.

### Parte 1 — Leitura fiel do contrato e derivação determinística dos atributos (no mecanismo)

* Novo leitor de contrato, tolerante às duas formas de `attributes` que aparecem em uso:
  * **mapa** — `{"attributes": {"id": 1024, "name": "Acme Corp"}}` → nome = chave, valor = valor;
  * **lista** — `{"attributes": [{"name": "id", "value": 1024}]}` → nome = campo `name`, valor = campo
    `value` (aceitar também `type` explícito quando presente).
* **Estimativa do tipo a partir do VALOR** (regra determinística, a ser fixada em spec):

  | valor no contrato | tipo estimado |
  |---|---|
  | booleano JSON (`true`/`false`) | `Boolean` |
  | inteiro JSON que cabe em `int` | `Integer` |
  | inteiro JSON maior | `Long` |
  | decimal JSON | `Double` |
  | string JSON | `String` |
  | array JSON de escalares | `List` do tipo do elemento **ou** `String` (decisão do maintainer) |
  | objeto JSON | `String` (serializado) |

  * Se o contrato declarar um `type` explícito, ele tem precedência — mas **deve** ser normalizado
    (FQCN → nome simples) e validado.
* **Restrição dura: o tipo precisa ser um tipo de `java.lang`.** O template da entidade
  (`__entity_name__.java:37-51`) emite `private ${attrib.type} ${attrib.name};` **sem emitir import** para o
  tipo do atributo — o bloco de imports cobre apenas `jakarta.persistence.*` e `lombok.*`. Um tipo fora de
  `java.lang` (`Instant`, `LocalDateTime`, `BigDecimal`, `UUID`) produz um projeto que **não compila**.
  Corroboração: o próprio contrato de exemplo do repositório declara `"type": "java.time.Instant"`, mas a
  fixture de IT rebaixa para `String createdAt`. (Alternativa de escopo maior: fazer o template emitir os
  imports necessários — decisão do maintainer.)
* Montagem **estrita** da flag: `Tipo:nome,Tipo:nome,…`, sem espaços, nomes em minúscula inicial, na ordem do
  contrato. O resultado é validado contra a `validationRegex` do catálogo; se **não** casar, a falha precisa ser
  **ruidosa** (nota no relatório de proveniência + `ERROR:` em stderr), porque o fallback silencioso do FR-006
  para `Long:id,MyId:id,My_Id:my_id` gera uma entidade plausível e **errada** — exatamente o tipo de falha
  silenciosa que este bug existe para eliminar.
* `enable_custom_attributes` é **forçado** a `true` sempre que a lista derivada for não vazia (§
  *Direcionamento do maintainer*). Sem isso, o template nem renderiza os campos (§
  `__entity_name__.java:37-51`).
* **Fonte de proveniência**: os valores derivados do contrato **não** são literais do contrato (FR-007) nem
  propostas da LLM (FR-009). Precisam de uma fonte própria — proponha `CONTRACT_DERIVED` (ou `SYNTHETIC`),
  a ser somada à tabela e ao resumo do FR-020 (`Orchestrator.groovy:312-355`, especialmente o *Resumo* em
  `:350-353`).

### Parte 2 — Nome da entidade estimado pela LLM, e pacote que o acompanha

* **Relaxamento limitado do FR-009.** O portão `contract.containsKey(flag)` passa a admitir uma
  **allowlist explícita e mínima** de flags cuja semântica é derivável do contrato:
  `entity_name` (e, opcionalmente, `entity_name_package`). Para essas flags, uma proposta `INFERENCE`
  passa a ser aceita mesmo sem a chave literal no contrato — **desde que** o valor case com a
  `validationRegex` do catálogo. **Todas as demais flags mantêm o portão estrito**, e as chaves da allowlist
  **não** devem aparecer na seção *"Propostas do LLM descartadas"* quando forem aceitas.
* **Precedência resultante** para `entity_name`: campo explícito do contrato (`entityName` /
  `entity_name` / `entity`) ⇒ proposta da LLM (`INFERENCE`) ⇒ default do catálogo (`MyEntity`).
* **Derivação de `entity_name_package`**: a partir do `entity_name` **resolvido**, em minúsculas —
  consistente com o que o README já promete (`README.md:1930-1932`) e com a fixture de IT
  (`entity_name=ContractEntity` ⇒ `entity_name_package=contractentity`). Isso fecha a lacuna do pacote
  desacoplado e evita a armadilha do campo `package` do contrato: ele é um pacote **completo**
  (`it.pkg.contractentity`), enquanto `entity_name_package` é um **sub-pacote** relativo a `-Dpackage` —
  um mapeamento ingênuo produziria `it.pkg.it.pkg.contractentity`.
* **Origem da derivação do pacote**: fonte nova (`SYNTHETIC`) ou reaproveitamento de `CONTRACT_DERIVED` —
  decisão do maintainer; em qualquer caso, precisa entrar na tabela do FR-020.

### Parte 3 — Pré-requisito bloqueante: corrigir a leitura do cache

* `cacheLookup` (`Orchestrator.groovy:521-551`) deve passar a fazer o parse com
  `JsonSlurper.parseText(line)` — o formato em disco **já é JSON válido** por `cli-contract.md:204-218` — com
  fallback para a forma legada v1 (sem `r`). O regex atual é desnecessário e é a causa do `StackOverflowError`.
* Teste com **tamanho realista** de relatório (≥32 flags ⇒ `r` com ~1965 caracteres). O
  `OrchestratorCacheTest` atual não detecta o problema porque usa um `r` de ~100 caracteres.
* Sem esta parte, a 2ª invocação continua saindo com exit 1 e a correção das Partes 1/2 **não pode ser
  verificada** por repetição — que é a verificação natural desta feature (FR-011/FR-012).

### Parte 4 — Persistência no projeto gerado (Metade C): **snapshot derivado, sem flag nova**

Esta é a parte que **não** exige mexer no contrato do mecanismo nem no catálogo de flags:

* **O archetype já tem tudo o que precisa.** A forma da entidade é integralmente descrita por
  `entity_name`, `entity_name_package`, `enable_custom_attributes` e `custom_attributes_list` — flags que o
  archetype **já recebe**. Logo, o próprio pós-processador pode materializar o baseline:
  `src/test/resources/contracts/<entity_name>.entity-contract.json`, com um conteúdo como:

  ```json
  {
    "entityName": "ContractEntity",
    "entityNamePackage": "contractentity",
    "customAttributesEnabled": true,
    "attributes": [ { "name": "id", "type": "Long" }, { "name": "name", "type": "String" } ]
  }
  ```

  Esse arquivo é o **baseline da forma da entidade** — exatamente o que o teste futuro precisa para afirmar
  *"a classe gerada ainda corresponde ao contrato"*.
* **Onde escrever.** `srcTestResources` já existe (`archetype-path-src-test-variables.groovy:24`). Criar
  `srcTestResources.resolve("contracts")` e gravar o arquivo (helper `copyFile` já existe; para escrita de
  texto, um helper simples com `File.text =`).
* **QUANDO escrever — constraint de ordem.** A escrita precisa acontecer **depois** do bloco `SNIPPETS`, porque
  `snippet-src-test-java.groovy` apaga `src/test/resources/` inteiro quando `enable_folder_src_test_java=false`.
  Duas opções, ambas válidas:
  * nova entrada em `SECTIONS` no `SnippetComposer.groovy:52-65` (ex.: `"snippet-entity-contract.groovy"`
    posicionado **após** `"SNIPPETS"`), ou
  * um snippet cujo **nome** ordene depois de `snippet-src-test-java` (ex.: `snippet-t-entity-contract.groovy`),
    já que a seção `SNIPPETS` é concatenada em ordem alfabética.
  Em qualquer caso, o teste de regressão deve exigir que o arquivo **sobreviva** a
  `enable_folder_src_test_java=false`.
* **Nenhum dado de negócio é persistido**: o snapshot contém apenas **nomes de campos e tipos** ⇒ FR-017 e
  Princípio V **permanecem válidos**, sem emenda.
* **Quem consome.** O projeto gerado já embarca testes Java em
  `src/test/java/__entity_name_package__/config/properties/`. O teste do contrato pode ser uma classe nova
  nesse pacote (ex.: `EntityContractTest.java`) que lê `src/test/resources/contracts/<Entity>.json` do
  classpath de teste e compara com os campos declarados na entidade — falhando se alguém editar a classe à mão.
  Isso materializa a "burocratização" pedida: **alterar a classe sem atualizar o snapshot quebra o build**.
* **Limite honesto deste desenho**: como o snapshot e a classe derivam **das mesmas flags**, o teste é
  tautológico *enquanto a Metade B não for corrigida* — ele detecta **edição manual** da classe (o objetivo
  declarado), mas **não** valida que o contrato do prompt foi respeitado. Ou seja: a Parte 4 **não substitui** a
  Parte 1; ela se apoia nela.

### Parte 5 — Especificação, contrato do script, cobertura e documentação

* `specs/003-llm-flag-inference/spec.md`:
  * **emendar a FR-009** (`:126`) com a exceção da allowlist — de forma **explícita**, como foi feito com a
    FR-011 no bug irmão (`:128`), não silenciosa;
  * nova FR para a **derivação do contrato** (atributos → `custom_attributes_list`, tipo estimado pelo
    valor, `enable_custom_attributes` forçado, formato estrito);
  * nova FR para o **pacote acompanhar a entidade**;
  * nova FR para a **materialização do snapshot** em `src/test/resources/contracts/` (com aconstraint de
    ordem no pós-processador e a sobrevivência à limpeza de `src/test/resources`);
  * **emendar a FR-020** (`:137`) para incluir a(s) nova(s) fonte(s) na tabela e no resumo.
* `specs/003-llm-flag-inference/contracts/cli-contract.md`:
  * §2 (`:174-192`): documentar que o script **pode/deve** devolver `entity_name` estimado a partir do
    contexto do prompt + contrato, e que esse valor é aceito mesmo sem a chave literal no contrato;
  * §2: documentar o **formato rígido** de `custom_attributes_list` e a regra de tipo por valor;
  * §3: registrar a troca do parser do cache (o **formato em disco não muda** — v2 continua v2);
  * §5: registrar as **breaking changes** (semântica da FR-009, novo(s) fonte(s) de proveniência).
* `scripts/inference-stub.sh`: derivar `entity_name` (e a lista) do contrato lido, para exercitar o caminho
  ponta a ponta sem LLM real. Hoje o stub devolve sempre o mesmo conjunto fixo (`:40-42`).
* **Cobertura de IT**: um perfil que **gere** o comando a partir de um contrato de domínio e afirme que a
  entidade gerada corresponde. O perfil `custom-attributes` atual fixa os valores à mão e, por isso, é cego
  para este bug; o `verify.groovy` do perfil deve ser estendido para checar também o snapshot materializado.
* `README.md`: corrigir a afirmação falsa de derivação automática de `entity_name_package`
  (`:1930-1932`, `:2131-2132`); atualizar a nota de remoção das duas flags (`:1944-1950`) para refletir a nova
  materialização do snapshot; documentar o efeito de `enable_folder_src_test_java` sobre `contracts/`
  (`:2095-2097`).
* `src/test/groovy/eda/postgenerate/llmflag/ProvenanceReportTest.groovy`: reconciliar os checks A6/A7/A8
  com a allowlist (uma flag fora da allowlist e ausente do contrato **continua descartada** — esse check
  deve permanecer, apenas com uma flag de contrato de domínio a mais no cenário).
* `src/test/groovy/eda/postgenerate/llmflag/FlagCatalogExtractorTest.groovy`: **sem alteração esperada** se a
  Parte 4 for adotada (o catálogo não muda, o lock de 32 flags continua valendo). Ver *Alternativa A* para o
  caso de a parte ser implementada com flag nova.

### Alternatives (com trade-offs)

* **A — Persistir o contrato CRU (leitura literal do pedido).**
  *Prós*: atende o texto do pedido ao pé da letra; o arquivo persistido é exatamente o que o operador entregou
  ao mecanismo, incluindo campos que não são da entidade (`api_port`, `enable_jwt_authentication`).
  *Contras*: exige **reabrir** a decisão do bug irmão — um **novo canal** (flag `entity_contract_path`
  apontando para arquivo que o mecanismo grava no cache-dir, ou `entity_contract_inline` com o JSON na linha de
  comando), com risco de recriar a "flag zumbi" que acabou de ser removida; exige **emenda explícita da
  FR-017** e revisão do **Princípio V**; **materializa dado de negócio no VCS do projeto gerado**
  (`"nome_cachorro": "Caramelo"`); aumenta a superfície do comando (JSON na linha de comando é frágil em
  Windows/PowerShell); e o catálogo voltaria de 32 para 33 flags.
* **B — Só a LLM deriva tudo; o mecanismo apenas deixa de descartar.**
  *Prós*: menor mudança de código; é literalmente o que o operador pediu ("observando o contexto do prompt e do
  próprio contrato, você vai supor o nome da entidade"); a LLM é boa em estimar o nome.
  *Contras*: **não garante o formato** — a LLM pode inverter `Tipo:nome`, emitir espaço ou tipo com ponto, e o
  valor é rejeitado pela regex caindo **silenciosamente** no default `Long:id,MyId:id,My_Id:my_id` (a entidade
  gerada fica plausível e errada); a estimativa do tipo **pelo valor** é mecânica e não justifica depender da
  LLM; a reprodutibilidade (FR-012, byte-equivalência) passa a depender do comportamento da LLM.
* **C — Envelope explícito no contrato** (`{"flags": {...}, "entity": {...}}`).
  *Prós*: o FR-009 funciona como escrito; `flags` casa literal com o catálogo.
  *Contras*: quebra todos os contratos já existentes do operador (migração obrigatória); contraria
  "o contrato é um documento livre de esquema" (`spec.md:146`, `:179`).
* **D — Normalização determinística, sem allowlist** (só a Parte 1; o nome da entidade teria de vir de um
  campo explícito do contrato).
  *Prós*: totalmente reprodutível; nenhuma porta de alucinação aberta.
  *Contras*: exige que o operador declare `entityName` no contrato, o que **não** é o formato que o
  operador descreve (ele quer o nome **estimado** a partir do prompt + contrato).

**Recomendação**: **Preferred (Partes 1-6)**, com a allowlist da Parte 2 restrita a `entity_name` e a
**Parte 4 (snapshot derivado)** em vez da Alternativa A (contrato cru).

## Files likely to change

* `src/main/groovy/eda/postgenerate/llmflag/Orchestrator.groovy` — derivação dos atributos a partir do
  contrato, allowlist da FR-009 no `merge()` (`:254`), remoção da allowlist da lista `discarded`
  (`:281-289`), derivação de `entity_name_package`, `enable_custom_attributes` forçado, nova fonte de
  proveniência no `renderProvenanceReport()` (`:312-355`), parser do cache (`:521-551`).
* `src/main/groovy/eda/postgenerate/llmflag/FlagProposalMerger.groovy` — **mesma** allowlist e a mesma
  derivação no módulo unit-testável irmão (`:61`, `:92`), para o mecanismo e o módulo não divergirem.
* `src/main/resources/META-INF/maven/archetype_metadata_properties/property-entity.xml` — só se a regex de
  `custom_attributes_list` precisar ser alargada (`:60-63`); a regex atual já aceita `Tipo:nome` estrito.
* `src/main/resources/META-INF/archetype_post_generate_body/archetype-path-src-test-variables.groovy` —
  path do destino do snapshot (`:24-25`, ao lado de `srcTestResources`).
* **Novo** `src/main/resources/META-INF/archetype_post_generate_snippets/snippet-entity-contract.groovy` —
  materialização do snapshot; **atenção à ordenação** (precisa rodar **depois** de
  `snippet-src-test-java.groovy`) e ao `SECTIONS` do `SnippetComposer.groovy:52-65` se for exigido ordem
  absoluta.
* `src/main/groovy/eda/postgenerate/SnippetComposer.groovy` — `SECTIONS` (`:52-65`), se a ordem do novo
  snippet precisar ser fixada explicitamente.
* `scripts/inference-stub.sh` — stub que deriva a entidade e a lista do contrato lido (`:40-42`).
* `src/test/groovy/eda/postgenerate/llmflag/ProvenanceReportTest.groovy` — reconciliar A6/A7/A8 com a
  allowlist; novos cenários para a derivação.
* `src/test/groovy/eda/postgenerate/llmflag/OrchestratorCacheTest.groovy` — caso de regressão do parser do
  cache com relatório de tamanho realista.
* Novo teste (sugestão: `src/test/groovy/eda/postgenerate/llmflag/ContractDerivationTest.groovy`) — regras de
  estimativa de tipo por valor, montagem estrita da lista, `enable_custom_attributes` forçado, pacote
  acompanhando a entidade.
* `src/test/resources/projects/custom-attributes/archetype.properties` (+ `verify.groovy`) — o perfil passa
  a ser **derivado** do contrato, ou ganha um perfil irmão que o seja; o `verify.groovy` deve checar também o
  snapshot materializado e sua sobrevivência a `enable_folder_src_test_java=false`.
* **Novo** `src/main/resources/archetype-resources/src/test/java/__entity_name_package__/EntityContractTest.java`
  (ou equivalente em Groovy) — o teste do contrato que vive **no projeto gerado**.
* `specs/003-llm-flag-inference/spec.md` — emendar a FR-009, emendar a FR-020, novas FRs de derivação e de
  persistência.
* `specs/003-llm-flag-inference/contracts/cli-contract.md` — §2 (formato rígido e regras de derivação), §3
  (parser do cache), §5 (breaking changes).
* `README.md` — `:1930-1932`, `:2131-2132` (derivação de `entity_name_package`), `:1944-1950` (nota de
  remoção), `:2095-2097` (efeito de `enable_folder_src_test_java` sobre `contracts/`).

## Tests to add or update

* **Derivação dos atributos**: contrato com `attributes` no formato mapa ⇒ `custom_attributes_list` ==
  `Integer:id,Long:bigNumber,String:name,Boolean:active` (na ordem do contrato).
* **Derivação dos atributos**: contrato com `attributes` no formato lista (`name`/`value`) ⇒ mesmo
  resultado.
* **Tipo estimado pelo valor**: número inteiro pequeno → `Integer`; grande → `Long`; decimal → `Double`;
  booleano → `Boolean`; string → `String`.
* **Tipo explícito tem precedência**: contrato com `{"name":"id","type":"Long","value":1}` ⇒ `Long:id`.
* **Tipo fora de `java.lang` é rejeitado ou normalizado**: `java.time.Instant` **não** pode chegar ao
  template (o projeto gerado não compilaria) — o mecanismo deve normalizar ou rejeitar com nota explícita.
* **Formato estrito**: a lista derivada passa pela `validationRegex` do catálogo; uma lista malformada
  produz **nota ruidosa** no relatório de proveniência, não um silêncio.
* **`enable_custom_attributes` forçado**: com atributos derivados ⇒ `enable_custom_attributes=true` na
  linha de comando, mesmo que o contrato não mencione a flag.
* **Nome da entidade pela LLM (allowlist)**: contrato de domínio + LLM propondo `entity_name` ⇒ aceito,
  fonte `INFERENCE`, **ausente** da seção "Propostas descartadas".
* **Regressão do FR-009**: uma flag **fora** da allowlist e ausente do contrato **continua descartada**
  (o check A7 existente serve).
* **Precedência**: `entityName` explícito no contrato **vence** a proposta da LLM (fonte `CONTRACT`).
* **Pacote acompanha a entidade**: `entity_name=ContractEntity` ⇒ `entity_name_package=contractentity`,
  fonte derivada.
* **Derivação x contrato**: se o contrato declarar `entity_name_package` explicitamente, ele **vence** a
  derivação.
* **Cache**: 1ª invocação (MISS) e 2ª invocação (HIT) produzem **linhas de comando byte-idênticas**
  (FR-012) e a 2ª **não** estoura a pilha — com um relatório de proveniência de tamanho realista (≥32
  flags).
* **Cache legado**: uma entrada v1 (sem `r`) continua sendo lida e servida com a nota de "relatório
  indisponível".
* **Snapshot materializado**: projeto gerado com `enable_custom_attributes=true` ⇒ existe
  `src/test/resources/contracts/<Entity>.json` com nome, pacote e atributos.
* **Sobrevivência do snapshot**: com `enable_folder_src_test_java=false`, o snapshot **continua existindo**
  (regressão do bug de ordem do pós-processador).
* **Conteúdo do snapshot**: os atributos listados correspondem **exatamente** aos campos declarados na
  classe gerada (`private <tipo> <nome>;`).
* **Detecção de edição manual**: alterar a classe gerada (remover/renomear um campo) sem atualizar o
  snapshot ⇒ o teste do contrato **falha** (é a "burocratização" pedida).
* **Ponta a ponta**: invocar o mecanismo com um contrato de domínio e um stub que deriva ⇒ a linha de
  comando contém `-Dentity_name=<estimado>`,
  `-Dcustom_attributes_list=Tipo:nome,…`, `-Denable_custom_attributes=true`,
  `-Dentity_name_package=<entity_name em minúsculas>`; em seguida, gerar o projeto e verificar que a
  entidade gerada tem os campos esperados e **compila**.
* **Regressão do catálogo**: 32 flags, 0 defaults vazios, `absolute_path_jsonfile_contract` e
  `enable_json_entity_contracts` ausentes (checks existentes de `FlagCatalogExtractorTest`).

## Risks & Considerations

- **A allowlist abre uma porta de alucinação.** A LLM passa a poder definir `entity_name` sem chave
  literal no contrato. Mitigação: allowlist **mínima** (`entity_name`), `validationRegex` estrita do
  catálogo (`^[A-Z][a-zA-Z0-9_]+$`) e o valor inválido cai no default por FR-006.
- **O fallback silencioso do FR-006 é o pior modo de falha possível aqui.** Um `custom_attributes_list`
  malformado cai no default `Long:id,MyId:id,My_Id:my_id` — uma entidade **plausível e errada**, sem
  sinal visível de que o contrato foi ignorado. A correção deve tornar essa falha **ruidosa** (nota no
  relatório de proveniência + `ERROR:` em stderr).
- **O tipo gerado precisa ser de `java.lang`.** O template da entidade **não emite import** para o tipo do
  atributo; qualquer tipo fora de `java.lang` (`Instant`, `LocalDateTime`, `BigDecimal`, `UUID`) gera um
  projeto que **não compila**. A regra de estimativa deve ficar presa a `java.lang`, ou o template deve
  ser estendido para emitir imports (escopo maior).
- **A estimativa de data/hora é ambígua.** Um valor ISO-8601 em string (`"2026-10-23T10:15:30Z"`) é `String`
  por simplicidade, mas o operador pode esperar `LocalDateTime` — que **não** compila sem import. A regra
  precisa ser declarada explicitamente e testada.
- **O ramo `enable_custom_attributes=true` gera uma entidade SEM `@Id`.** Com atributos customizados, o
  template não renderiza o bloco `@Id`/`@GeneratedValue`/`id`/`username`/`password`
  (`__entity_name__.java:75-95`), e o `@Id` só aparece se `id` estiver entre os atributos — **sem a
  anotação** `@Id`. Defeito **pré-existente**, fora do escopo deste bug, mas a correção amplia a exposição
  dele a todo projeto gerado com contrato. Registrado aqui para não ser perdido.
- **`entity_name_package` × `package`**: o campo `package` do contrato é um pacote **completo**
  (`it.pkg.contractentity`); `entity_name_package` é um **sub-pacote** relativo a `-Dpackage`. Derivar do
  `entity_name` evita a dupla prefixação; usar o `package` do contrato exige extrair o último segmento.
- **Mudança de semântica de `entity_name_package`**: hoje o valor efetivo é `myentity` quando a flag é
  omitida. A derivação muda o resultado **efetivo** para projetos gerados sem a flag — comportamento
  melhor, mas uma mudança de saída observável.
- **Nova(s) fonte(s) de proveniência**: muda o formato do relatório de FR-020 (aditivo, mas quebra
  consumidores que parseiem o sumário `**Resumo**: c CONTRACT / i INFERENCE / d DEFAULT / x descartada(s)`).
- **Contrato do script de inferência**: o papel do script muda — deixa de ser "quem sugere flags comuns" e
  passa a ser "quem estima o **nome** da entidade". O `cli-contract.md` §2 precisa explicitar isso, e o
  `scripts/inference-stub.sh` precisa acompanhar.
- **FR-017 / Princípio V × Metade C (contrato cru)**: o contrato **não** pode ser persistido em controle de
  versão sem **emenda explícita** da FR-017 e revisão do Princípio V — o projeto gerado é versionado e o
  contrato pode carregar dado de negócio. O snapshot derivado (Parte 4) **não** colide com nenhuma das duas.
- **Reabertura de decisão fechada**: a Metade C, se implementada com flag nova, contraria a conclusão
  registrada em `.specify/bugs/flag-contrato-absoluto-obsoleta/fix.md` e em `README.md:1944-1948`. Precisa
  ser registrada como **breaking change** em `cli-contract.md` §5 e como **emenda** explícita, não como
  revogação silenciosa.
- **Ordem de execução no pós-processador**: qualquer escrita em `src/test/resources/` precisa rodar
  **depois** de `snippet-src-test-java` (que apaga a pasta inteira) e **depois** de
  `snippet-properties-filetype` (que apaga por extensão). Sem disciplina de ordem, o artefato é criado e
  destruído na mesma execução — falha silenciosa.
- **O destino pedido não é materializado hoje**: `archetype-resources/src/test/resources/contracts/` está
  **vazia** e fora de qualquer fileset ⇒ o archetype não cria a pasta no projeto gerado. A implementação
  precisa criar a pasta explicitamente (o `maven-archetype-plugin` não copia diretórios vazios).
- **O snapshot derivado é tautológico enquanto a Metade B não for corrigida**: ele detecta edição manual da
  classe (o objetivo declarado), mas não valida que o contrato do prompt foi respeitado. A ordem de
  implementação é obrigatória: **cache → derivação → snapshot**.
- **Vazamento pré-existente de valores derivados do contrato**: a seção "Propostas do LLM descartadas"
  persiste no cache valores que vieram do contrato (verificado: `ContractEntity` em `cache.jsonl`). Relevante
  porque a Metade C propõe ampliar a persistência; vale uma revisão da FR-017 nesse ponto.
- **A correção do cache é pré-requisito, não opcional**: sem ela, FR-011/FR-012/FR-020 seguem inoperantes
  e a verificação por repetição falha com exit 1.
- **A correção do cache muda o parser, não o formato**: o `cache.jsonl` continua v2; leitores antigos
  seguem compatíveis. O `r` ausente (v1) precisa continuar produzindo a nota "relatório indisponível".
- **Inversão de fonte da verdade (decisão de governo, não de código)**: hoje o catálogo é a fonte da verdade e
  a classe da entidade é uma *renderização* dele. O operador propõe o inverso — após a geração, a **classe**
  é a fonte da verdade e o contrato é o baseline que "burocratiza" mudanças. Essa inversão precisa ser
  escrita na spec (e no compliance da Constituição), porque muda quem é dono do formato da entidade: mudar a
  classe passa a exigir atualizar o contrato, e não o contrário.

## Open Questions

- [NEEDS CLARIFICATION: **snapshot derivado** (recomendado, sem flag nova, sem dado de negócio) ou **contrato
  cru** (leitura literal, exige emenda da FR-017 + canal novo)?]
- [NEEDS CLARIFICATION: o snapshot deve sobreviver à limpeza de `src/test/resources` quando
  `enable_folder_src_test_java=false` (escrever depois do snippet), ou o archetype deve exigir
  `enable_folder_src_test_java=true` para materializá-lo?]
- [NEEDS CLARIFICATION: o teste do contrato vive **no projeto gerado**
  (`src/test/java/__entity_name_package__/…`) ou no **suite de IT do archetype**
  (`src/test/resources/projects/*/verify.groovy`)?]
- [NEEDS CLARIFICATION: qual é a **forma canônica** de `attributes` no contrato — mapa
  (`{"attributes": {"id": 1024}}`) ou lista (`{"attributes": [{"name":"id","value":1024}]}`)? As duas
  aparecem em uso; a derivação precisa de uma escolha explícita (e as duas devem ser aceitas?).]
- [NEEDS CLARIFICATION: a estimativa de tipo por valor deve produzir `Integer` ou `Long` para inteiros
  JSON? E `Double` para decimais? A regra de `java.lang` está confirmada como restrição duro?]
- [NEEDS CLARIFICATION: o nome da entidade pode vir de um campo explícito do contrato
  (`entityName`), ou **deve** ser sempre estimado pela LLM a partir do prompt + contrato?]
- [NEEDS CLARIFICATION: quando a lista derivada **não** casa com a `validationRegex`, o mecanismo deve
  **falhar a invocação** (exit ≠ 0) ou manter o default com nota ruidosa? Falhar é mais seguro contra a
  entidade plausível-e-errada, mas tira a característica "always produce a command line" do FR-010.]
- [NEEDS CLARIFICATION: `entity_name_package` deriva do `entity_name` em minúsculas (como o README já
  promete e como a fixture de IT faz) ou do campo `package` do contrato (último segmento)?]
- [NEEDS CLARIFICATION: a correção do `cacheLookup` (`StackOverflowError`) entra no escopo deste bug ou
  deve ser um bug próprio? Ela é **pré-requisito** para validar a correção, mas tem causa-raiz distinta.]
- [NEEDS CLARIFICATION: o perfil de IT `custom-attributes` deve ser **gerado** a partir do contrato
  (passando a ser um teste de verdade), ou continua um fixture com valores esperados fixados e ganha um
  perfil irmão para o caminho derivado?]
- [NEEDS CLARIFICATION: se o contrato cru for persistido (Alternativa A), a flag que o transporta é
  **opt-in** (o contrato só é persistido quando o operador pede) ou **padrão**?]
