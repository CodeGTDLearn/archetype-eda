# Como derivar a entidade do contrato

Esta regra transforma o contrato JSON no nome da entidade, no pacote da entidade e na lista de atributos.

## 1. Entradas

- Spec: tipo de módulo, package base, use-case, contrato JSON e pasta de destino da geração, na marcação `PASTA DE DESTINO`.
- Catálogo de flags do JAR: usado para separar flags de campos de negócio.
- Contrato JSON: pode declarar `attributes`, `fields` ou campos de negócio no topo.

O package base nunca vem do contrato. O contrato pode ter uma chave `package`, mas ela é meta-chave e não substitui o `-Dpackage` do spec.

## 2. Nome da entidade

Antes de resolver o nome, confirme que o contrato existe e é objeto JSON.

Se não for, pare pelo Gate B de `prompt-00` §0: não infira o nome pelo use-case e não use default.

Resolva o nome nesta ordem:

1. `entity_name` no contrato.
2. `entityName` no contrato.
3. `entity` no contrato.
4. Inferência a partir do use-case.
5. Default do catálogo (`MyEntity`).

Regras:

- O valor explícito vence qualquer inferência.
- O valor inferido do use-case precisa ser justificado.
- O default só entra quando o use-case **não trouxer nenhum nome** de entidade: use-case vazio de nome, sem candidato único.
- Se o use-case trouxer **mais de um** nome candidato, ou trouxer um nome que não permite inferir qual entidade é a principal, pare e pergunte.
- Nunca use `MyEntity` silenciosamente: quando o default for aplicado, registre-o na proveniência como `DEFAULT` e notifique o operador.
- O nome final deve satisfazer `^[A-Z][a-zA-Z0-9_]+$`.

Se o contrato não trouxer o nome e o use-case for ambíguo, pare e pergunte.

## 3. Pacote da entidade

1. Se o contrato declarar `entity_package_name`, use o valor explícito.
2. Caso contrário, derive de `entity_name` em minúsculas.
3. O valor final deve satisfazer `^[a-z][a-z0-9_.]+$`.
4. Nunca use `myentity` silenciosamente.

Exemplos:

| Entidade | `entity_package_name` |
|---|---|
| `Cliente` | `cliente` |
| `PedidoItem` | `pedidoitem` |

O pacote completo da entidade é:

```text
<package base>.<entity_package_name>
```

## 4. Extração dos atributos

A fonte dos atributos, nesta ordem:

1. `attributes`, quando presente.
2. `fields`, quando `attributes` não existir.
3. Campos de negócio do topo, quando não houver coleção explícita.

### 4.1 Formas aceitas de `attributes`

Objeto (mapa nome → valor):

```json
{
  "attributes": {
    "nome": "Maria Silva",
    "ativo": true
  }
}
```

Lista de objetos:

```json
{
  "attributes": [
    {"name": "nome", "value": "Maria Silva"},
    {"name": "ativo", "value": true}
  ]
}
```

Na lista, o nome pode vir de `name`, `attribute` ou `field`; o valor pode vir de `value` ou `default`.

Lista de strings:

```json
{
  "attributes": ["nome", "ativo"]
}
```

### 4.2 Campos de negócio

Quando não há `attributes` nem `fields`, use as chaves de topo que:

- não existem no catálogo de flags;
- não são meta-chaves (`entityName`, `entity`, `package`, `description`, `attributes`, `fields`).

A marcação `PASTA DE DESTINO` não é chave de contrato: ela pertence ao spec e nunca vira atributo da entidade. A lista de meta-chaves permanece exatamente a acima.

Atenção:

- `entity_name` é flag; ela não vira atributo.
- `entity_package_name` é flag; ela não vira atributo.
- Chaves de negócio viram atributos, mesmo que o valor seja um objeto ou um array.

### 4.3 Exceção do `id` — campo técnico de identidade

O atributo `id` do contrato **não vira atributo comum**. Ele é estrutural:

- o archetype já gera, de forma incondicional, o campo técnico de identidade `<entidade>Id`, sempre `Long`;
- quando o contrato traz `id`, ele corresponde a esse campo técnico;
- o template descarta qualquer atributo cujo nome seja `id` (em qualquer caixa) ou `<entidade>Id`;
- por isso, `id` **não** entra em `custom_entity_attributes_list`: ele não é um campo comum, e o tipo não é estimado pelo valor.

Consequências no relatório:

- não liste `id` como atributo derivado;
- registre o mapeamento na proveniência, na nota do atributo, como `id` → `<entidade>Id` (campo técnico), e não como atributo comum;
- a ausência de `id` no contrato não é erro: o campo técnico continua existindo na entidade.

Essa regra é a mesma que a descrição da flag `custom_entity_attributes_list` no catálogo declara: o `id` é estrutural e o template o descarta.

## 5. Nome do atributo

- O nome deve satisfazer `^[a-z][a-zA-Z0-9_]*$`.
- Normalize acentos e caracteres inválidos.
- Um nome inválido é descartado com aviso.
- Nomes duplicados são descartados com aviso.
- O nome `id`, em qualquer caixa, é reservado: ver §4.3. Ele não vira atributo.
- Um atributo com tipo do projeto recebe o nome do próprio tipo:
  - `EnderecoCliente` → `enderecoCliente`;
  - `StatusPedido` → `statusPedido`.
- Se o tipo do projeto já nomeou outro atributo, o segundo mantém o nome do contrato.
- A entidade nunca declara o mesmo campo duas vezes.

## 6. Lista de atributos

Monte a lista estritamente como:

```text
Type:name,Type:name
```

Regras:

- Sem espaços.
- Na ordem do contrato.
- Cada tipo deve satisfazer `^[A-Z][a-zA-Z0-9_]*$`.
- Cada nome deve satisfazer `^[a-z][a-zA-Z0-9_]*$`.

Exemplo:

```text
Integer:codigo,String:nome,Boolean:ativo
```

Se a lista violar a regex do catálogo, o erro é barulhento: avise, registre a rejeição e não caia em um valor plausível mas errado. O desfecho é o de `regras-04` §3: o `defaultValue` do catálogo, com nota e aviso — e o `defaultValue` desta flag (`String:name`) **não** é um valor de negócio: registre na proveniência que a lista derivada foi rejeitada e que o campo renderizado não corresponde ao contrato.

## 7. Flag de ativação

- `enable_custom_entity_attributes=true` sempre que houver pelo menos um atributo derivado.
- `enable_custom_entity_attributes=false` quando não houver atributos.

O template da entidade só renderiza os campos no ramo `true`.

## 8. Contrato sem atributos

Se o contrato não tiver `attributes`, `fields` nem campos de negócio:

- não invente atributos;
- mantenha `enable_custom_entity_attributes=false`;
- mantenha `custom_entity_attributes_list` no default do catálogo;
- registre a ausência no relatório.

Se o contrato trouxer **apenas** o atributo `id`, ele não é um atributo comum (ver §4.3): o contrato não tem atributos comuns, e o campo técnico `<entidade>Id` é gerado pelo archetype de qualquer forma. Mantenha `enable_custom_entity_attributes=false`.
