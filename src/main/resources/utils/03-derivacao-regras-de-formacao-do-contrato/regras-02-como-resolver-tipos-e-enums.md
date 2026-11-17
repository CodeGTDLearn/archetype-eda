# Como resolver tipos e enums

Esta regra decide o tipo Java de cada atributo e quando um valor representa um enum.

## 1. Tipo declarado

Quando o atributo declara `type`:

1. `java.lang` tem precedência.
2. Tipo do projeto é preservado.
3. Qualquer outro tipo é rejeitado com aviso e o tipo é estimado pelo valor.

### 1.1 Tipos de `java.lang`

Aceite o nome simples ou o FQN:

```text
String, Integer, Long, Double, Float, Short, Byte,
Boolean, Character, Number, Object, StringBuilder, StringBuffer
```

O FQN é reduzido ao nome simples. O template renderiza o tipo verbatim, sem import adicional.

### 1.2 Tipos do projeto

Um tipo do projeto:

- é um nome de classe (`^[A-Z][A-Za-z0-9_]*$`);
- não é `java.lang`;
- não é um tipo de `java.*`, `javax.*` ou `jakarta.*`;
- não é um nome JDK conhecido sem pacote (`List`, `Map`, `Set`, `Date`, `BigDecimal`, `UUID`, `Optional`, etc.);
- não contém generics (`<`, `>`) nem arrays (`[`).

O tipo do projeto é preservado pelo nome simples. O FQN viaja em `custom_entity_attribute_imports`.

Exemplos:

```text
br.com.clinica.convenio.Convenio -> Convenio
br.com.clinica.pedido.StatusPedido -> StatusPedido
```

### 1.3 Tipos rejeitados

Um tipo fora de `java.lang` e fora do projeto, por exemplo:

```text
java.time.Instant
java.util.List
List<String>
```

é rejeitado com aviso. O tipo volta a ser estimado pelo valor.

## 2. Estimativa pelo valor

Quando não há tipo declarado válido:

| Valor | Tipo |
|---|---|
| `true` / `false` | `Boolean` |
| inteiro que cabe em `int` | `Integer` |
| inteiro maior que `int` | `Long` |
| decimal | `Double` |
| string | `String` |
| objeto | `String` |
| array/lista | `String` |

Um objeto sem tipo declarado não é `String`: ele é derivado como POJO por `regras-03`.

## 3. Nome do atributo com tipo do projeto

O tipo do projeto nomeia o atributo:

```text
Convenio -> convenio
StatusPedido -> statusPedido
EnderecoCliente -> enderecoCliente
```

O nome declarado no contrato só é mantido quando o nome derivado do tipo já está em uso.

## 4. Enums

### 4.1 Sinais de enum

Um enum existe quando:

- um tipo do projeto tem valor textual (o valor observado);
- um tipo do projeto tem `enumValues` ou `values` explícitos;
- um campo sem tipo declarado tem um array não vazio de strings MAIÚSCULAS.

Um array vazio, misto, com minúsculas ou com não-strings não é enum. Ele cai em `String` com aviso.

### 4.2 Nome do enum

- Array de MAIÚSCULAS sem tipo declarado: o nome vem da chave do campo em PascalCase.
  - `status` → `Status`.
- Tipo do projeto declarado: o nome é o nome simples do tipo.
  - `StatusPedido` → `StatusPedido`.

### 4.3 Constantes

As constantes vêm de:

1. `enumValues`, quando presente;
2. `values`, quando `enumValues` não existir;
3. o próprio valor observado.

Cada constante é normalizada para um identificador Java válido:

- maiúsculas;
- caracteres não alfanuméricos viram `_`;
- o resultado deve satisfazer `^[A-Z][A-Z0-9_]*$`.

### 4.4 Formato de `contract_enum_definitions`

```text
Type:CONSTANT|CONSTANT
```

Vários enums são separados por `;`:

```text
Status:ABERTO|PAGO|CANCELADO;Canal:WEB|APP
```

Regras de pacote:

- `Type` sem pacote → pacote da entidade.
- `Type` com pacote → pacote próprio.
- Enum de campo aninhado → marcador `nested.Type`, gerado em `<entity_package>.nested`.

Um arquivo de enum já existente não é sobrescrito; a geração é pulada com aviso.

## 5. Arrays e listas

A entidade não tem `List<T>`:

- tipo declarado `java.lang` + array → mantém o tipo declarado, com aviso;
- tipo do projeto + array sem constantes MAIÚSCULAS → `String`, com aviso;
- array sem tipo declarado e sem enum → `String`, com aviso.

Nunca invente uma coleção.
