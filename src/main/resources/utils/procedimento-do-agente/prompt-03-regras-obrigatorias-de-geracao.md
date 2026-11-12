# Regras obrigatórias de geração

## 1. Regra do `package`

O archetype declara:

```text
package ${package}.${entity_package_name}
```

Portanto:

- `-Dpackage` é o **pacote-base**.
- O archetype concatena `entity_package_name` automaticamente.
- Nunca inclua o nome da entidade em `-Dpackage`.

### Exemplos

Entidade `Cliente`, pacote base `org.clinica`:

```text
-Dpackage=org.clinica
```

Resultado:

```java
package org.clinica.cliente;
```

Errado:

```text
-Dpackage=org.clinica.cliente
```

Resultado:

```java
package org.clinica.cliente.cliente;
```

## 2. Propriedades obrigatórias

Passe sempre:

### `entity_name`

- PascalCase.
- Deve satisfazer a regex do catálogo.
- Não use `MyEntity` silenciosamente.
- Se o contrato não trouxer o nome e o use-case for ambíguo, pergunte.

### `entity_package_name`

- Minúsculas.
- Derivado de `entity_name`, salvo valor explícito no contrato.
- Deve satisfazer a regex do catálogo.
- Não use `myentity` silenciosamente.
- Passe explicitamente, inclusive ao operar o archetype direto.

### `entity_contract_file_path`

- Caminho do arquivo `.json`.
- Use barras normais (`/`), nunca barras invertidas.
- `none` somente quando não houver contrato.
- O pós-processador copia o arquivo apenas quando `enable_folder_src_test_java=true`.

### `enable_custom_entity_attributes`

- `true` sempre que houver atributos derivados.
- `false` quando não houver atributos.

### `enable_folder_src_test_java`

- `true` quando o contrato precisar ser copiado para o módulo.
- `false` quando a árvore de testes não for mantida.

## 3. Execução fora do projeto pai

Execute o `mvn archetype:generate` fora do diretório do projeto pai.

Exemplo:

```text
cd C:\Users\edufi\Downloads
```

Depois mova a pasta gerada:

```powershell
robocopy .\cliente-module ..\projeto\cliente-module /E /MOVE
```

Isso evita o erro:

```text
Unable to add module to the current project as it is not of packaging type 'pom'
```

## 4. PowerShell

- Coloque todos os `-D...` entre aspas simples.
- Não use parênteses nos valores.
- Não quebre a linha com `\`; use uma linha única ou a continuação do PowerShell.

Exemplo:

```powershell
mvn archetype:generate --batch-mode `
  '-DarchetypeGroupId=com.eda-archetype' `
  '-DarchetypeArtifactId=eda-archetype' `
  '-DarchetypeVersion=0.0.1-SNAPSHOT' `
  '-DgroupId=org.clinica' `
  '-DartifactId=cliente-module' `
  '-Dversion=1.0.0-SNAPSHOT' `
  '-Dpackage=org.clinica' `
  '-Dentity_name=Cliente' `
  '-Dentity_package_name=cliente'
```

## 5. Lembrete final

Antes de executar, confira:

- [ ] `package` não contém o nome da entidade.
- [ ] `entity_name` está em PascalCase.
- [ ] `entity_package_name` está em minúsculas.
- [ ] `entity_contract_file_path` usa barras normais.
- [ ] `enable_custom_entity_attributes` está coerente com os atributos.
