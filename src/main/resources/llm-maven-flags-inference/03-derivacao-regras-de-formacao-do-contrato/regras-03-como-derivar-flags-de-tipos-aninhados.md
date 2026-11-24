# Como derivar flags de tipos aninhados

Esta regra cobre os objetos aninhados e os metadados dos tipos do projeto.

## 1. Objetos aninhados

Um objeto JSON sem tipo declarado vira um POJO.

- O POJO vive em `<package>.<entity_package_name>.nested`.
- A entidade referencia o POJO pelo tipo totalmente qualificado (`<package>.<entity_package_name>.nested.<Tipo>`) e marca o campo com `@Transient`.
- O POJO é gerado apenas quando o tipo aparece em `custom_entity_attributes_list`.

### 1.1 Nome do POJO

O nome é o PascalCase da chave, com acentos normalizados:

```text
endereco -> Endereco
endereço -> Endereco
endereco_complemento -> EnderecoComplemento
```

Um nome que colide com palavra reservada recebe o sufixo `Type`:

```text
class -> ClassType
```

Um nome que começa com dígito recebe o prefixo `Type`.

### 1.2 Campos do POJO

- Os nomes dos campos são as chaves do contrato, normalizadas para camelCase.
- Nomes inválidos são descartados com aviso.
- Nomes duplicados recebem sufixo `_2`, `_3`, ...
- A ordem é a ordem do contrato.
- O pai é emitido antes do filho.

### 1.3 Profundidade

- A entidade é a geração 1.
- Um objeto de topo é a geração 2.
- O limite é 25 gerações por cadeia.
- A geração 26 e seguintes caem em `String` com aviso.

### 1.4 Objetos inválidos

Caem em `String` com aviso:

- objeto vazio;
- nome de tipo inválido;
- nenhum campo válido;
- profundidade acima do limite.

## 2. `contract_nested_type_definitions`

Formato:

```text
Tipo|Campo:Tipo,Campo:Tipo;Tipo|Campo:Tipo
```

Exemplo:

```text
Endereco|logradouro:String,numero:Integer;Contato|telefone:String,email:String
```

Regras:

- `none` quando não houver POJO referenciado pela entidade. Esse é o valor efetivo: o `none` é o `defaultValue` do catálogo, e a flag não é emitida na command line.
- O valor explícito do contrato sempre vence.
- Sem tipo aninhado referenciado, a flag não é emitida na command line: registre-a na proveniência como `DEFAULT` com a nota `none`, e não a omita do relatório.
- A flag deve satisfazer a regex do catálogo.
- Valor derivado que viola a regex cai no default com aviso e nota de proveniência.

## 3. `custom_entity_attribute_imports`

Formato:

```text
fqn1,fqn2,fqn3
```

Regras:

- `none` quando não houver tipo do projeto com pacote.
- Cada FQN aparece uma vez.
- O template emite um `import` por FQN.
- O import do próprio pacote da entidade é omitido pelo template.
- Dois tipos com o mesmo nome simples e pacotes diferentes geram aviso: a entidade pode não compilar.
- A flag deve satisfazer a regex do catálogo.
- Valor derivado que viola a regex cai no default com aviso.

## 4. `contract_enum_definitions`

Formato:

```text
Type:CONSTANT|CONSTANT;Type:CONSTANT
```

Regras:

- `none` quando não houver enum.
- Constantes na ordem de derivação.
- Constantes duplicadas são removidas.
- `nested.Type` gera o enum em `<entity_package>.nested`.
- `Type` sem pacote gera no pacote da entidade.
- `Type` com pacote gera no próprio pacote.
- Arquivo existente não é sobrescrito; a geração é pulada com aviso.

## 5. Ordem e determinismo

- Objetos: ordem do contrato, pai antes do filho.
- Campos: ordem do contrato.
- Enums: ordem de derivação.
- Imports: ordem de aparição nos atributos.
- Mesmo contrato → mesmas flags, byte a byte.

## 6. Interação com a entidade

Um tipo aninhado só chega ao projeto quando aparece em `custom_entity_attributes_list`.

- `Endereco` em `Endereco:endereco` → POJO gerado.
- `Endereco` fora da lista → POJO não gerado.

Não gere POJO órfão.
