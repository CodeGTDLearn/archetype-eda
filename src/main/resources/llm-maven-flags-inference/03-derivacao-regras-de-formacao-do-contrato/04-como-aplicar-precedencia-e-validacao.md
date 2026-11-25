# Como aplicar precedência e validação

Esta regra define qual valor vence quando mais de uma fonte oferece a mesma flag.

## 1. Precedência

Da maior para a menor:

1. `CONTRACT` — valor literal do contrato.
2. `CONTRACT_DERIVED` — valor derivado do contrato pelo processo.
3. Semântica da entidade — nome, pacote e atributos derivados.
4. `INFERENCE` — valor proposto a partir do use-case.
5. `DEFAULT` — default do catálogo.

A semântica da entidade pode carregar fontes diferentes:

| Flag | Fonte |
|---|---|
| `entity_name` explícito no contrato | `CONTRACT` |
| `entity_name` inferido do use-case | `INFERENCE` |
| `entity_package_name` derivado | `CONTRACT_DERIVED` |
| `custom_entity_attributes_list` derivado | `CONTRACT_DERIVED` |
| `enable_custom_entity_attributes` forçado | `CONTRACT_DERIVED` |

## 2. Regra do valor explícito

- Um valor explícito no contrato vence qualquer derivação e qualquer inferência.
- Isso vale para `custom_entity_attribute_imports`, `contract_enum_definitions` e `contract_nested_type_definitions`.
- Quando o valor explícito existe, não sobrescreva.

## 3. Validação

Toda flag precisa satisfazer a `validationRegex` do catálogo.

- `FR-006` é o requisito que cria o dicionário de 20 termos técnicos, disponível em `prompt-00` §0.4.

- Valor válido → mantém valor e fonte.
- Valor inválido → usa o `defaultValue` do catálogo, com nota e aviso.
- A nota registra a rejeição: `valor '...' rejeitado pela regex; default aplicado (FR-006)`.
- O erro é barulhento; nunca há fallback silencioso.
- Exceção: a lista de atributos derivada inválida não usa o default; pare com STOP-DER-02 (ver `01` §6).

### 3.1 Flags críticas

| Flag | Default | Regex |
|---|---|---|
| `entity_name` | `MyEntity` | `^[A-Z][a-zA-Z0-9_]+$` |
| `entity_package_name` | `myentity` | `^[a-z][a-z0-9_.]+$` |
| `custom_entity_attributes_list` | `String:name` | `^(false\|Tipo:nome(,Tipo:nome)*)$` |
| `custom_entity_attribute_imports` | `none` | FQNs separados por vírgula |
| `contract_enum_definitions` | `none` | `Tipo:CONST\|CONST` separados por `;` |
| `contract_nested_type_definitions` | `none` | `Tipo\|Campo:Tipo` separados por `;` |
| `entity_contract_file_path` | `none` (catálogo; não usado aqui) | `none` ou caminho `.json` |
| `enable_custom_entity_attributes` | `false` | `true` ou `false` |
| `enable_folder_src_test_java` | `false` | `true` ou `false` |

O catálogo é a autoridade final. As regex acima são um resumo; em caso de divergência, vale o catálogo do JAR.

## 4. Regras de coerência

- `custom_entity_attributes_list` só tem efeito com `enable_custom_entity_attributes=true`.
- `enable_custom_entity_attributes=true` é obrigatório quando há atributos.
- `custom_entity_attribute_imports`, `contract_enum_definitions` e `contract_nested_type_definitions` são derivados apenas quando há atributos.
- `entity_contract_file_path` só produz cópia com `enable_folder_src_test_java=true`.
- `entity_contract_file_path` usa barras normais.
- `entity_contract_file_path` é sempre um caminho `.json`: sem contrato, pare com STOP-IN-02 pelo Gate B de `prompt-00` §0.

## 5. Flags não definidas no catálogo

Uma chave do contrato que não existe no catálogo é campo de negócio, não flag.

- Ela pode virar atributo.
- Ela nunca vira `-D` solto.
- Se o contrato tentar impor uma flag inexistente, ignore com aviso.

## 6. Inferência

- A inferência só pode propor valores para flags elegíveis.
- `entity_name` é a única flag elegível por allowlist.
- Uma flag que **não** está na allowlist nunca recebe valor inferido. Se o contrato trouxer a chave dela literalmente, a fonte é `CONTRACT` — o valor literal do contrato, não uma inferência.
- Nenhuma flag fora da allowlist pode ser preenchida a partir do use-case. Se a chave existir no contrato, trate-a como literal do contrato (`CONTRACT`). O valor do contrato vence.
- Proposta descartada entra na seção de descartadas do relatório.

## 7. Conflito

Se duas fontes de mesma prioridade divergirem e não houver regra que decida:

1. pare com STOP-DER-03;
2. mostre as duas fontes;
3. mostre as flags afetadas;
4. peça decisão.

Nunca adivinhe.
