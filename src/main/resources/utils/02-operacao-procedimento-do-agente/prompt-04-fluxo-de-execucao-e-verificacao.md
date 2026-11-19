# Fluxo de execução e verificação

## 1. Antes de executar

### 1.1 Proveniência

Monte a tabela de proveniência conforme `regras-06`.

- Todas as flags da command line.
- Todas as flags derivadas.
- Todas as rejeições.
- Resumo por fonte.

### 1.2 Checklist

Confirme em voz alta:

- [ ] o use-case foi lido antes do contrato (Gate A).
- [ ] o contrato é objeto JSON não vazio (Gate B).
- [ ] o contrato veio depois do use-case (Gate C).
- [ ] `package` é o pacote-base, sem o nome da entidade.
- [ ] `entity_name` está em PascalCase.
- [ ] `entity_package_name` está em minúsculas e foi derivado do nome.
- [ ] `custom_entity_attributes_list` está no formato `Type:name,Type:name`.
- [ ] `enable_custom_entity_attributes=true` quando há atributos.
- [ ] `entity_contract_file_path` aponta para um `.json` existente.
- [ ] `enable_folder_src_test_java=true` quando o contrato precisa ser copiado.

### 1.3 Contrato materializado

Siga `regras-05` antes de montar a command line.

## 2. Executar

```powershell
mvn archetype:generate --batch-mode `
  '-DarchetypeGroupId=com.eda-archetype' `
  '-DarchetypeArtifactId=eda-archetype' `
  '-DarchetypeVersion=<versão>' `
  '-DgroupId=<grupo>' `
  '-DartifactId=<artefato>' `
  '-Dversion=<versão-do-módulo>' `
  '-Dpackage=<package-base>' `
  '-Dentity_name=<Entidade>' `
  '-Dentity_package_name=<pacote-da-entidade>'
```

Execute fora do projeto pai.

## 3. Depois de gerar

### 3.1 Validar o `package`

Abra o arquivo da entidade:

```text
<Entity>.java
```

Leia a primeira linha:

```java
package ...;
```

Se o nome da entidade estiver duplicado, por exemplo:

```java
package org.clinica.cliente.cliente;
```

reexecute o archetype do zero com o `-Dpackage` corrigido.

Nunca edite o arquivo.

### 3.2 Validar o contrato copiado

Quando `enable_folder_src_test_java=true`, confirme:

```text
src/test/resources/contracts/contract.json
```

O arquivo deve ser byte a byte igual ao contrato materializado.

### 3.3 Compilar

Dentro do módulo gerado:

```powershell
mvn clean compile
```

- `BUILD SUCCESS` = OK.
- Warning de `parent.relativePath` apontando para o projeto pai é benigno; reporte e siga.

## 4. Em caso de falha

- Não edite o código gerado.
- Reporte o comando executado.
- Reporte a saída relevante.
- Corrija a flag de entrada e reexecute do zero.
- Se a falha vier do catálogo ou do pós-processador, pare e reporte o JAR como incompleto.
