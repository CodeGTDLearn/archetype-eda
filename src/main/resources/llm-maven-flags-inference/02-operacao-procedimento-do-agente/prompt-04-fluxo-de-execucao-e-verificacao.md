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
- [ ] o destino foi informado, é absoluto e está fora da árvore do archetype (Gate D).
- [ ] `package` é o pacote-base, sem o nome da entidade.
- [ ] `entity_name` está em PascalCase.
- [ ] `entity_package_name` está em minúsculas e foi derivado do nome.
- [ ] `custom_entity_attributes_list` está no formato `Type:name,Type:name`; se não estiver, pare com STOP-DER-02.
- [ ] `custom_entity_attributes_list` não contém `id`: o `id` do contrato é o campo técnico `<entidade>Id` (ver `regras-01` §4.3).
- [ ] `enable_custom_entity_attributes=true` quando há atributos.
- [ ] `entity_contract_file_path` aponta para um `.json` existente.
- [ ] `enable_folder_src_test_java=true` quando o contrato precisa ser copiado.

### 1.3 Destino da geração

Antes de montar a command line, confirme o destino:

- o destino vem da marcação `PASTA DE DESTINO` do spec — nunca do contrato;
- o destino é absoluto e está fora da árvore do archetype;
- exiba o destino absoluto ao operador **antes de executar**, junto do resultado da validação (válido e fora da árvore do archetype);
- se o destino estiver dentro da árvore do archetype, recuse com aviso e pergunte onde fica o destino; se o operador insistir, finalize o fluxo com a mensagem de STOP-IN-04;
- se a pasta de destino ainda não existir, pare com STOP-CONF-01 e pergunte uma vez exibindo o caminho completo; ofereça criar a pasta, escolher outra pasta ou cancelar; só crie após a confirmação;
- se já existir um módulo com o mesmo nome do artefato, pare com STOP-CONF-02 e ofereça "sobrescrever o módulo", "manter o existente e encerrar" ou "escolher outro destino"; nunca apague nada por conta própria.

### 1.4 Contrato materializado

Siga `regras-05` antes de montar a command line. Se a materialização falhar, pare com STOP-DER-04.

## 2. Executar

```powershell
mvn org.apache.maven.plugins:maven-archetype-plugin:3.4.1:generate --batch-mode `
  '-DarchetypeGroupId=com.eda-archetype' `
  '-DarchetypeArtifactId=eda-archetype' `
  '-DarchetypeVersion=<versão>' `
  '-DgroupId=<package-base>' `
  '-DartifactId=<entity-name-em-kebab-case>' `
  '-Dversion=1.0.0-SNAPSHOT' `
  '-Dpackage=<package-base>' `
  '-Dentity_name=<Entidade>' `
  '-Dentity_package_name=<pacote-da-entidade>' `
  '-DoutputDirectory=<destino-absoluto>'
```

A propriedade `outputDirectory` é nativa do plugin de geração de archetype — não é flag do catálogo — e precisa estar presente em toda geração. O módulo nasce em `<destino>/<artifactId>`.

`groupId`, `artifactId` e `version` seguem a identidade de `prompt-03` §2.1: `groupId` = package base, `artifactId` = entity name em kebab-case, `version` = `1.0.0-SNAPSHOT`.

### 2.1 Onde executar

Execute de qualquer diretório. O goal é invocado com a versão fixa `maven-archetype-plugin:3.4.1`: ele declara `requiresProject=false` e `executePhase=generate-sources`, então uma pasta vazia funciona e o Maven usa o "Maven Stub Project (No POM)" (`standalone-pom`). O pin impede que outra versão resolvida no `~/.m2` mude esse comportamento. O destino de saída é outra coisa: ele diz onde o módulo nasce, não onde o Maven roda.

## 3. Depois de gerar

### 3.1 Validar o destino

Confirme que o módulo gerado está em:

```text
<destino>/<artifactId>
```

- A raiz do módulo precisa ter seu próprio `pom.xml`.
- Reporte o caminho absoluto do módulo gerado.
- Nenhum arquivo novo pode ter sido criado dentro da árvore do archetype.

### 3.2 Validar o `package`

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

### 3.3 Validar o contrato copiado

Quando `enable_folder_src_test_java=true`, confirme:

```text
src/test/resources/contracts/contract.json
```

O arquivo deve ser byte a byte igual ao contrato materializado.

### 3.4 Compilar

Dentro do módulo gerado — em `<destino>/<artifactId>`, nunca na raiz do archetype:

```powershell
mvn clean compile
```

- `BUILD SUCCESS` = OK.
- Warning de `parent.relativePath` apontando para o projeto pai é benigno; reporte e siga.
- Reporte o caminho absoluto onde a compilação foi executada.

## 4. Em caso de falha

- Não edite o código gerado.
- Reporte o comando executado.
- Reporte a saída relevante.
- Corrija a flag de entrada e reexecute do zero.
- Se a falha vier do catálogo ou do pós-processador, pare com STOP-EXEC-01 e reporte o JAR como incompleto.
