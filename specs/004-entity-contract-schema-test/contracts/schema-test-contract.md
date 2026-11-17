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
3. **Tipo**: `type` declarado (`java.lang`/projeto) vence; rejeitado → estimativa; sem tipo → estimativa; arrays: MAIÚSCULAS → enum, resto → `String`.
4. **Id**: `id` do contrato ≡ `<entidade>Id` (`Long`).
5. **Enum**: nome do tipo derivado + conjunto de constantes.
6. **Round-trip**: visão normalizada (nomes Java; `id` → `<entidade>Id`) desserializada com `FAIL_ON_UNKNOWN_PROPERTIES=true` e re-serializada; conjuntos de chaves e hierarquia iguais.
7. **Lombok**: veredito idêntico on/off (introspecção de campos).

## 4. Falha agregada

Uma única falha por classe de teste; corpo = lista determinística:

```text
[<direction>] <path> | expected: <expected> | found: <found|absent> | kind: <kind>
```

- `direction`: `CONTRACT_TO_ENTITY` | `ENTITY_TO_CONTRACT`
- `path`: caminho hierárquico (ex.: `endereco.uf`) ou `<contract>` em falha estrutural
- `kind`: `MISSING_FIELD`, `EXTRA_FIELD`, `TYPE_MISMATCH`, `ENUM_NAME_MISMATCH`, `ENUM_CONSTANT_MISMATCH`, `STRUCTURAL`
- ordem: ordem do contrato; excedentes por nome (determinismo JVM-safe)
- cabeçalho: classe/método que falhou + `src/test/resources/contracts/contract.json`

## 5. Verificação (Frente 02 — o archetype não interpreta contrato)

| Prova | Onde |
|---|---|
| presença/ausência por condição | `SnippetContractSchemaTestTest.groovy` (harness) |
| execução verde na app gerada | `entity-contract/verify.groovy`: relatório surefire da app com `<Entidade>ContractTest`, 0 falhas/erros |
| sabotagem falha com diagnóstico | `entity-contract/verify.groovy`: cópia em temp + adulteração + `mvnw test` aninhado → exit ≠ 0 e mensagem |
| ausência nos perfis sem contrato | 21 `verify.groovy` |
| zero dependência nova | diff do `pom.xml` da app gerada |
