# Contract: Pacote de Schema Test Gerado (`<pkg>.<entity_package>.contract`)

**Feature**: `004-entity-contract-schema-test` | **Date**: 2026-11-13

Contrato comportamental do artefato da Frente 01. Consumido pelo `verify.groovy` de `entity-contract` e pela suíte de sabotagem.

## 1. Presença

| Condição | Resultado |
|---|---|
| `enable_folder_src_test_java=true` E `src/test/resources/contracts/contract.json` persistido | pacote `contract/` presente em `src/test/java/<pkg>/<entity_package>/` |
| qualquer outra (flag falsa, path `none`/`null`/vazio/ausente, fonte inexistente, árvore de testes removida) | pacote `contract/` ausente; build/testes da aplicação verdes |

## 2. Entradas

| Entrada | Origem | Falha |
|---|---|---|
| Contrato | `ClassLoader.getResourceAsStream("/contracts/contract.json")` | ausente/ilegível/inválido → `STRUCTURAL` explícita (nunca skip) |
| Entidade | classe no pacote da entidade | ausente → falha de compilação/execução |
| Aninhados | `<pkg>.<entity_package>.nested.*` por FQN | idem |
| Enums | pacote da entidade ou `.nested` | idem |

## 3. Semântica

1. **Contrato→Entidade**: todo atributo efetivo existe como campo declarado, com o tipo esperado, recursivo em aninhados.
2. **Entidade→Contrato**: nenhum campo foge ao contrato, exceto `<entidade>Id` e o scaffold `username/password` quando o contrato não tem atributos.
3. **Tipo**: o tipo efetivo vem das flags que geraram a entidade; `ContractTypeMappingTest` aplica a precedência de FR-007 ao `type` declarado aceito (`java.lang`/tipo do projeto) e o compara com o tipo efetivo. `type` rejeitado (ex.: `java.time.Clock`) é ignorado; a inferência por valor permanece no mecanismo de geração (D17).
4. **Id**: `id` do contrato ≡ `<entidade>Id` (`Long`).
5. **Enum**: nome do tipo derivado + conjunto de constantes.
6. **Round-trip**: payload sintético com nomes Java (`id` → `<entidade>Id`) desserializado com `FAIL_ON_UNKNOWN_PROPERTIES=true` e re-serializado; conjuntos de chaves e hierarquia iguais.
7. **Lombok**: veredito idêntico on/off (introspecção de campos).

## 4. Falha por divergência

Uma falha por divergência, no teste que a detectou; o arquivo de regra agrupa as suas. Cada falha carrega:

```text
[<direction>] <path> | expected: <expected> | found: <found|absent> | kind: <kind>
```

- `direction`: `CONTRACT_TO_ENTITY` | `ENTITY_TO_CONTRACT`
- `path`: caminho hierárquico (ex.: `endereco.uf`) ou `<contract>` em falha estrutural
- `kind`: `MISSING_FIELD`, `EXTRA_FIELD`, `TYPE_MISMATCH`, `ENUM_NAME_MISMATCH`, `ENUM_CONSTANT_MISMATCH`, `STRUCTURAL`
- ordem: determinística dentro do arquivo (`@TestMethodOrder(MethodName.class)`); o surefire não garante ordem global entre classes
- cabeçalho: classe/método que falhou + `src/test/resources/contracts/contract.json`
- falha estrutural: uma vez por arquivo (`@BeforeAll`), prefixo `CONTRACT STRUCTURAL FAILURE`; `MalformedContractTest` testa o parser diretamente

## 5. Verificação (Frente 02 — o archetype não interpreta contrato)

| Prova | Onde |
|---|---|
| presença/ausência por condição | `SnippetContractSchemaTestTest.groovy` (harness) |
| execução verde na app gerada | `entity-contract/verify.groovy`: 21 relatórios surefire e 43 testes, 0 falhas/erros |
| execução em outro formato | `custom-nested-objects/verify.groovy` e `custom-enums-from-payload/verify.groovy`: presença + surefire verde |
| sabotagem falha com diagnóstico | `entity-contract/verify.groovy`: cópia em temp + adulteração + `mvnw test` aninhado → exit ≠ 0 e mensagem; golden em `expected/divergences.txt` |
| ausência nos perfis sem contrato | 19 `verify.groovy`, pelo diretório `contract/` (nunca por glob de nome) |
| zero dependência nova | diff do `pom.xml` da app gerada |
