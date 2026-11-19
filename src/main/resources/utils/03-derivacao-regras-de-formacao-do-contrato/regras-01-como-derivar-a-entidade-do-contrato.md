# Como derivar a entidade do contrato

Esta regra transforma o contrato JSON no nome da entidade, no pacote da entidade e na lista de atributos.

## 1. Entradas

- Spec: tipo de módulo, package base, use-case e contrato JSON.
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
- O default só pode ser usado quando o use-case não permitir inferir o nome.
- Nunca use `MyEntity` silenciosamente.
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

Atenção:

- `entity_name` é flag; ela não vira atributo.
- `entity_package_name` é flag; ela não vira atributo.
- Chaves de negócio viram atributos, mesmo que o valor seja um objeto ou um array.

## 5. Nome do atributo

- O nome deve satisfazer `^[a-z][a-zA-Z0-9_]*$`.
- Normalize acentos e caracteres inválidos.
- Um nome inválido é descartado com aviso.
- Nomes duplicados são descartados com aviso.
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
Integer:id,String:nome,Boolean:ativo
```

Se a lista violar a regex do catálogo, o erro é barulhento: avise, registre a rejeição e não caia em um valor plausível mas errado.

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
