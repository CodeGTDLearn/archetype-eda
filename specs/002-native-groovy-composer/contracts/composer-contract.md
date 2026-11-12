# Compositor Groovy — Contrato de Build-Time

**Feature**: 002-native-groovy-composer
**Phase**: 1 (design)
**Created**: 2026-09-28

## Visão Geral

O compositor é um script Groovy de build-time invocado via `org.codehaus.gmaven:groovy-maven-plugin:2.1.1` (goal `execute`) na fase `process-resources` do archetype principal.

## Contrato de Entrada

### Sistema de Arquivos (4 pastas)

```
src/main/resources/META-INF/
├── archetype_post_generate_body/        (15 arquivos fixos, prefixo body-)
├── archetype_post_generate_variables/   (25 arquivos, prefixo variables-)
├── archetype_post_generate_generators/  (3 arquivos, prefixo generator-)
└── archetype_post_generate_snippets/    (19 arquivos, prefixo snippet-)
```

### Cada arquivo `.groovy` deve:

- Conter apenas código Groovy sintaticamente válido.
- Ser UTF-8 encoded (sem BOM, a menos que decisão posterior).
- Terminar com newline.
- (Para arquivos em `snippets/`) declarar o marcador `// SNIPPET-NN-<nome>.GROOVY` (o nome do arquivo em maiúsculas); o compositor falha no build se o marcador faltar.

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
        <source>${project.basedir}/src/main/groovy/ArchetypePostGenerateComposer.groovy</source>
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
- **Estrutura**: 17 seções concatenadas em ordem canônica

### Empacotamento

O `archetype-post-generate.groovy` não existe em `src/main/resources/META-INF/`; o `BuildArtifactsTest` 1.4 falha se o monólito for recriado. Assim, o único arquivo no classpath é o gerado em `target/classes/META-INF/`.

## Contrato de Execução

### Invocação

- **Goal**: `execute` (do `groovy-maven-plugin`)
- **Phase**: `process-resources` (estritamente após `xml-maven-plugin:transform` em `generate-resources`, antes de `package`)

### Comportamento Esperado

- Lê os 62 arquivos `.groovy` (15 + 25 + 3 + 19) das 4 pastas.
- Remove linhas `^import …` e `^//import …` (com espaços opcionais) de cada arquivo de `snippets/` e `variables/`.
- Concatena em ordem canônica (17 seções).
- Para `archetype_post_generate_body/body-01-imports.groovy`, aplica `replaceAll('/', '')` em cada linha (espelha o `tr -d '/'` do bash legado) e prependa no topo.
- Escreve o resultado em `target/classes/META-INF/archetype-post-generate.groovy`.

### Garantias de Equivalência (histórico)

- O compositor nasceu byte-equivalente à saída histórica do `bash_script/create_archetype_post_generate.sh`, que não existe mais no repositório.
- O critério atual de regressão é o `ComposerTest` (estrutura, ordem, idempotência e parse) — ver T014.

## Contrato de Erros

| Cenário | Comportamento |
|---|---|
| Pasta `body/` faltando | Falha com mensagem clara listando o que está faltando. Não gera arquivo parcial. |
| Arquivo BODY canônico faltando (`body-02-header.groovy`, etc.) | Falha com mensagem clara. |
| Pasta `variables/` ou `snippets/` vazia | Falha com mensagem clara (não é aceitável gerar sem nenhum snippet). |
| Arquivo `.groovy` com encoding inválido | Falha com mensagem de erro de encoding. |
| Exceção Groovy em runtime | Falha com stacktrace completo. Não há supressão de erros. |

## Contrato de Compatibilidade

- O `bash_script/create_archetype_post_generate.sh` não existe mais no repositório.
- O `archetype-post-generate.groovy` não existe em `src/main/resources/META-INF/`; a remoção do índice do git (`git rm --cached`) foi executada e a deleção está staged para o commit (T025). Não é necessário `.gitignore` para o arquivo gerado: `target/` já é ignorado.
- O ciclo de testes IT (`mvn clean install` + `mvn integration-test`) MUST passar 9/9 perfis.