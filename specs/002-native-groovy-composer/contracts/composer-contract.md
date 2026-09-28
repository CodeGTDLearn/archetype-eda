# Compositor Groovy — Contrato de Build-Time

**Feature**: 002-native-groovy-composer
**Phase**: 1 (design)
**Created**: 2026-09-28

## Visão Geral

O compositor é um script Groovy de build-time invocado via `org.codehaus.gmaven:groovy-maven-plugin:2.1.1` (goal `execute`) na fase `process-resources` do archetype principal.

## Contrato de Entrada

### Sistema de Arquivos (3 pastas)

```
src/main/resources/META-INF/
├── archetype_post_generate_body/        (11 arquivos fixos, prefixo archetype-)
├── archetype_post_generate_variables/   (10 arquivos, prefixo variables-)
└── archetype_post_generate_snippets/    (11 arquivos, prefixo snippet-)
```

### Cada arquivo `.groovy` deve:

- Conter apenas código Groovy sintaticamente válido.
- Ser UTF-8 encoded (sem BOM, a menos que decisão posterior).
- Terminar com newline.
- (Para arquivos em `snippets/`) começar com um bloco de comentário `// FLAG: enable_xxx` (ou similar) referenciando a flag correspondente em `archetype_metadata_properties/`.

### Configuração Groovy Build-Time

No `pom.xml` (raiz do archetype-v2-eda):

```xml
<plugin>
  <groupId>org.codehaus.gmaven</groupId>
  <artifactId>groovy-maven-plugin</artifactId>
  <version>2.1.1</version>
  <dependencies>
    <dependency>
      <groupId>org.apache.groovy</groupId>
      <artifactId>groovy</artifactId>
      <version>4.0.28</version>
    </dependency>
    <dependency>
      <groupId>org.apache.groovy</groupId>
      <artifactId>groovy-json</artifactId>
      <version>4.0.28</version>
      <scope>runtime</scope>
    </dependency>
  </dependencies>
  <executions>
    <execution>
      <phase>process-resources</phase>
      <goals>
        <goal>execute</goal>
      </goals>
      <configuration>
        <source>${project.basedir}/src/main/groovy/eda/postgenerate/SnippetComposer.groovy</source>
      </configuration>
    </execution>
  </executions>
</plugin>
```

## Contrato de Saída

### Arquivo gerado

- **Caminho**: `target/classes/META-INF/archetype-post-generate.groovy`
- **Encoding**: UTF-8
- **Tamanho**: ~1100 linhas (estimado; valor real capturado durante implementação)
- **Estrutura**: 12 seções concatenadas em ordem canônica

### Empacotamento

O `pom.xml` (raiz) deve adicionar `<exclude>META-INF/archetype-post-generate.groovy</exclude>` ao `<resources><resource>` que aponta para `src/main/resources`, para que o arquivo gerado em `target/classes/META-INF/` não seja sobrescrito pelo arquivo velho de `src/main/resources/META-INF/`.

## Contrato de Execução

### Invocação

- **Goal**: `execute` (do `groovy-maven-plugin`)
- **Phase**: `process-resources` (estritamente após `xml-maven-plugin:transform` em `generate-resources`, antes de `package`)

### Comportamento Esperado

- Lê os 32 arquivos `.groovy` (11 + 10 + 11) das 3 pastas.
- Remove linhas `^import …` e `^//import …` (com espaços opcionais) de cada arquivo de `snippets/` e `variables/`.
- Concatena em ordem canônica (12 seções).
- Para `archetype_post_generate_body/archetype-imports.groovy`, aplica `replaceAll('/', '')` em cada linha (espelha o `tr -d '/'` do bash legado) e prependa no topo.
- Escreve o resultado em `target/classes/META-INF/archetype-post-generate.groovy`.

### Garantias de Byte-Equivalência

- O arquivo gerado MUST ser byte-equivalente à saída atual do `bash_script/create_archetype_post_generate.sh`.
- Verificável por `diff` retornando vazio e por hash SHA-256.
- Exceções: NENHUMA. Se houver divergência mínima, é regressão.

## Contrato de Erros

| Cenário | Comportamento |
|---|---|
| Pasta `body/` faltando | Falha com mensagem clara listando o que está faltando. Não gera arquivo parcial. |
| Arquivo BODY canônico faltando (`archetype-header.groovy`, etc.) | Falha com mensagem clara. |
| Pasta `variables/` ou `snippets/` vazia | Falha com mensagem clara (não é aceitável gerar sem nenhum snippet). |
| Arquivo `.groovy` com encoding inválido | Falha com mensagem de erro de encoding. |
| Exceção Groovy em runtime | Falha com stacktrace completo. Não há supressão de erros. |

## Contrato de Compatibilidade

- O `bash_script/create_archetype_post_generate.sh` permanece em disco como referência (não invocado).
- O `archetype-post-generate.groovy` commitado em `src/main/resources/META-INF/` é removido (via `git rm` + `.gitignore`) após validação byte-a-byte.
- O ciclo de testes IT (`mvn clean install` + `mvn integration-test`) MUST passar 9/9 perfis.