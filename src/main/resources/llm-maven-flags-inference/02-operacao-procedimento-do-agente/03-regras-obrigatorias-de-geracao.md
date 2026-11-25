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
- Se o package base estiver ausente ou ambíguo, pare com STOP-IN-05.

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
- O caminho precisa ser absoluto; se não for ou a gravação falhar, pare com STOP-DER-04.
- Nunca use `none`: sem contrato, pare com STOP-IN-02 pelo Gate B de `01-entryPoint` §0.
- O pós-processador copia o arquivo apenas quando `enable_folder_src_test_java=true`.

### `enable_custom_entity_attributes`

- `true` sempre que houver atributos derivados.
- `false` quando não houver atributos.

### `enable_folder_src_test_java`

- `true` quando o contrato precisar ser copiado para o módulo.
- `false` quando a árvore de testes não for mantida.

### 2.1 Identidade do módulo (propriedades nativas)

Estas propriedades não são flags do catálogo; são nativas do plugin e derivadas pelo procedimento:

- `groupId` = package base do spec.
- `entity_package_name` não leva separador: `PedidoItem` → `pedidoitem`.
- `artifactId` usa kebab-case: `PedidoItem` → `pedido-item`.
  - `Cliente` → `cliente`
  - `PedidoItem` → `pedido-item`
- `version` = default fixo `1.0.0-SNAPSHOT`.
- A SPEC não informa `groupId`, `artifactId` nem `version`.

## 3. Destino da geração

O destino vai **sempre** na propriedade `outputDirectory` da linha de comando, com o caminho absoluto confirmado no spec. A propriedade é nativa do plugin de geração de archetype e **não** é uma flag do catálogo.

- O valor precisa ser um caminho absoluto. Caminho relativo, caminho com `~` ou com sinalização de fim de arquivo é recusado com aviso. Pare com STOP-IN-04.
- Use barras normais (`/`) no valor emitido, mesmo no Windows. O caminho do destino é normalizado para barras normais antes de comparar e de emitir.
- Sem barra final e sem quebra de linha. Normalize espaços e barra final antes de comparar e de emitir.
- Destino dentro da árvore do archetype é proibido, sem exceção. Pare com STOP-IN-04.
- Não use caminho absoluto de usuário ou de máquina no procedimento.
- O módulo nasce em `<destino>/<artifactId>`; não existe movimentação posterior.

O diretório onde o Maven executa não precisa ter um `pom.xml`: o goal `archetype:generate` declara `requiresProject=false`. Rode de um diretório neutro ou de qualquer pasta; o destino de saída é outra coisa.

## 4. PowerShell

- Coloque todos os `-D...` entre aspas simples.
- Não use parênteses nos valores.
- Não quebre a linha com `\`; use uma linha única ou a continuação do PowerShell.
- O caminho do destino também vai entre aspas — inclusive quando contiver espaços, acentos ou barras invertidas.

Exemplo:

```powershell
mvn org.apache.maven.plugins:maven-archetype-plugin:3.4.1:generate --batch-mode `
  '-DarchetypeGroupId=com.eda-archetype' `
  '-DarchetypeArtifactId=eda-archetype' `
  '-DarchetypeVersion=0.0.1-SNAPSHOT' `
  '-DgroupId=org.clinica' `
  '-DartifactId=cliente' `
  '-Dversion=1.0.0-SNAPSHOT' `
  '-Dpackage=org.clinica' `
  '-Dentity_name=Cliente' `
  '-Dentity_package_name=cliente' `
  '-DoutputDirectory=C:/Temp/eda-sandbox'
```

O `-DartifactId` segue §2.1: `entity_name` em kebab-case (`Cliente` → `cliente`, `PedidoItem` → `pedido-item`). O `-DoutputDirectory` é obrigatório em toda geração (§3).

## 5. Lembrete final

Antes de executar, confira:

- [ ] `package` não contém o nome da entidade.
- [ ] `entity_name` está em PascalCase.
- [ ] `entity_package_name` está em minúsculas.
- [ ] `artifactId` é o `entity_name` em kebab-case.
- [ ] `entity_contract_file_path` usa barras normais.
- [ ] `enable_custom_entity_attributes` está coerente com os atributos.
- [ ] `outputDirectory` está presente, é absoluto e usa barras normais.
