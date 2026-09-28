# Implementation Plan: Compositor Nativo Groovy para `archetype-post-generate.groovy`

**Branch**: `002-native-groovy-composer` | **Date**: 2026-09-28 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/002-native-groovy-composer/spec.md`

## Summary

Substituir o `bash_script/create_archetype_post_generate.sh` (225 linhas, com `sed -i`/`tr`/`ls`/`read -n 1 -s`) por um compositor Groovy nativo executado em build-time via `org.codehaus.gmaven:groovy-maven-plugin:2.1.1` na fase `process-resources`. O compositor (`SnippetComposer.groovy`, ~50-80 linhas) lê os 32 arquivos `.groovy` das 3 pastas (`archetype_post_generate_body/`, `..._snippets/`, `..._variables/`), aplica consolidação de imports (remover `^import …` e `^//import …` dos snippets, prepor `archetype_post_generate_body/archetype-imports.groovy` no topo), concatena em ordem canônica de 12 seções, e gera `target/classes/META-INF/archetype-post-generate.groovy`. Equivalência byte-a-byte com a saída atual do bash é o critério de aceitação primário (SC-003); passar 9/9 perfis IT em `mvn clean install` + `mvn integration-test` é o critério secundário (SC-001+SC-002).

## Technical Context

**Language/Version**: Groovy 4.0.28 (fixado no classpath do `groovy-maven-plugin` via `<dependencies>`); Java 17+ no runtime do plugin.

**Primary Dependencies**:
- `org.codehaus.gmaven:groovy-maven-plugin:2.1.1` (estável desde 2019-08-04; única opção Groovy build-time publicamente disponível).
- `org.apache.groovy:groovy:4.0.28` + `org.apache.groovy:groovy-json:4.0.28` (forçado via `<dependencies>`).
- Plugin já em uso: `org.codehaus.mojo:xml-maven-plugin:1.2.1` (precedente direto para a fase `generate-resources`).
- Plugin de packaging: `org.apache.maven.archetype:archetype-packaging:3.4.1`.

**Storage**: N/A — o compositor gera arquivos no sistema de arquivos local do build, sem persistência externa.

**Testing**:
- 9 perfis IT (`basic`, `full`, `minimal`, `binding-default-only`, `binding-list-only`, `binding-map-only`, `binding-mutable-only`, `binding-validated-only`, `json-contract`) em `src/test/resources/projects/*` — exercitam o output do compositor.
- Validação adicional: `diff` byte-a-byte com golden file (capturado antes da migração).

**Target Platform**: Maven 3.9.16 (atual do projeto) ou Maven 4.x; Windows com Git Bash ou Linux/macOS nativo (TTY-independence é um goal).

**Project Type**: Build script / Maven plugin integration (não uma library/web-service/mobile).

**Performance Goals**:
- Tempo de execução do passo de composição ≤ tempo do bash legado (~0.3s × 9 perfis = ~2.7s no ciclo IT).
- Zero overhead perceptível no build total.

**Constraints**:
- Equivalência byte-a-byte obrigatória (SC-003) — não há tolerância para divergência.
- Nenhuma invocação de bash no pipeline principal (FR-001).
- Constituição do projeto (Princípios I, II, VI) é vinculante.

**Scale/Scope**:
- 32 arquivos `.groovy` de entrada (11 body + 10 variables + 11 snippets).
- 12 seções no output.
- 1 único mantenedor (decisor único).
- Apetite: small (1-3 dias) conforme `concept.md`.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### Princípios Verificados

| Princípio | Status | Justificativa |
|---|---|---|
| I. Modularidade por Chave de Ativação | ✅ SATISFEITO | Composer preserva o acoplamento flag ↔ snippet; cada snippet continua começando com bloco de comentário indicando a flag (FR-007). |
| II. Convenção Fixa de Nomenclatura | ✅ SATISFEITO | Prefixos `snippet-`, `variables-`, `archetype-` mantidos; o compositor valida o prefixo ao ler arquivos. |
| III. Pacote por Funcionalidade | N/A | Composer não gera código Java. |
| IV. Binding Duplo de Configuração Externalizada | N/A | Composer não toca em configuration binding. |
| V. Credenciais Nunca Entram no Repositório | ✅ SATISFEITO | Composer não processa credenciais; nenhum arquivo de credencial entra no escopo. |
| VI. Validar Antes de Culpar o Código | ✅ SATISFEITO | FR-001+SC-001+SC-002 exigem `mvn clean install` (não `install` direto) como critério; cumpre o princípio de "instalação limpa antes de validar". |

**GATE PASSED** — nenhuma violação; nenhum item em "Complexity Tracking".

## Project Structure

### Documentation (this feature)

```text
specs/002-native-groovy-composer/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
│   └── composer-contract.md
├── checklists/
│   └── requirements.md
└── spec.md              # Input from /speckit.specify
```

### Source Code (repository root)

**Modificações no archetype-v2-eda:**

```text
archetype-v2-eda/
├── pom.xml                                                (modificado: adiciona gmaven:2.1.1; ajusta <resources>)
├── src/main/groovy/                                       (novo diretório)
│   └── eda/
│       └── postgenerate/
│           └── SnippetComposer.groovy                     (novo, ~50-80 linhas)
├── src/main/resources/META-INF/
│   ├── archetype-post-generate.groovy                     (REMOVIDO do controle de versão; git rm + .gitignore)
│   ├── archetype_post_generate_body/                      (preservado, sem mudanças)
│   ├── archetype_post_generate_snippets/                  (preservado, FR-007 adiciona comentário em cada)
│   └── archetype_post_generate_variables/                 (preservado, sem mudanças)
├── .gitignore                                             (modificado: adiciona archetype-post-generate.groovy)
└── bash_script/
    ├── create_archetype_post_generate.sh                  (mantido em disco, não invocado)
    └── script_core.sh                                     (modificado: linhas 41-42 removidas/comentadas)
```

**Estrutura do `SnippetComposer.groovy` (esboço conceitual):**

```groovy
import java.nio.file.Path
import java.nio.file.Paths

/**
 * SnippetComposer — Compositor Nativo Groovy para archetype-post-generate.groovy
 *
 * Lê os snippets das 3 pastas em src/main/resources/META-INF/, aplica a
 * consolidação de imports, e gera o arquivo final em
 * target/classes/META-INF/archetype-post-generate.groovy.
 *
 * Estratégia: concatenação controlada com ordem declarativa (List<String> SECTIONS).
 * Equivalente à Opção B do .specify/assessments/groovy-snippet-injection/concept.md.
 */

// 1. Ordem canônica das 12 seções (auditável)
private static final List<String> SECTIONS = [
    "archetype-header.groovy",                                  // 0: header
    "archetype-global-variables.groovy",                        // 1: global-vars
    "archetype-path-root-variables.groovy",                     // 2: path-vars-root
    "archetype-path-src-main-variables.groovy",                 // 3: path-vars-src-main
    "archetype-path-src-test-variables.groovy",                 // 4: path-vars-src-test
    "archetype-path-logback-variables.groovy",                  // 5: path-vars-logback
    "archetype-path-security-variables.groovy",                 // 6: path-vars-security
    "VARIABLES",                                                // 7: variables (alfabético)
    "SNIPPETS",                                                 // 8: snippets (alfabético)
    "archetype-footer.groovy",                                  // 9: footer
    "archetype-file-functions.groovy",                          // 10: file-functions
    "archetype-folder-functions.groovy",                        // 11: folder-functions
]

// 2. Paths
def baseDir = new File("src/main/resources/META-INF")
def bodyDir = new File(baseDir, "archetype_post_generate_body")
def variablesDir = new File(baseDir, "archetype_post_generate_variables")
def snippetsDir = new File(baseDir, "archetype_post_generate_snippets")
def outputFile = new File("target/classes/META-INF/archetype-post-generate.groovy")

// 3. Imports consolidation regex
def importPattern = Pattern.compile("^\\s*import")
def commentedImportPattern = Pattern.compile("^\\s*//\\s*import")

// 4. Compose
outputFile.parentFile.mkdirs()
def sb = new StringBuilder()

// 4a. Prepend imports
new File(bodyDir, "archetype-imports.groovy").eachLine { line ->
    sb.append(line.replaceAll("/", "")).append("\n")
}

// 4b. Concatenate body files in SECTIONS order (sem imports/alfabético)
SECTIONS.each { section ->
    if (section == "VARIABLES" || section == "SNIPPETS") {
        // Processa pasta correspondente em ordem alfabética
        def dir = (section == "VARIABLES") ? variablesDir : snippetsDir
        dir.listFiles().sort { it.name }.each { file ->
            def content = file.text
            content = importPattern.matcher(content).replaceAll("")
            content = commentedImportPattern.matcher(content).replaceAll("")
            sb.append(content)
        }
    } else {
        // Lê arquivo único de body
        def file = new File(bodyDir, section)
        def content = file.text
        content = importPattern.matcher(content).replaceAll("")
        content = commentedImportPattern.matcher(content).replaceAll("")
        sb.append(content)
    }
}

// 5. Write
outputFile.text = sb.toString()
println "[SnippetComposer] Written: ${outputFile.path} (${sb.toString().length()} bytes)"
```

**Estrutura da modificação do `pom.xml` (trechos):**

```xml
<build>
  <extensions>
    <extension>
      <groupId>org.apache.maven.archetype</groupId>
      <artifactId>archetype-packaging</artifactId>
      <version>3.4.1</version>
    </extension>
  </extensions>

  <resources>
    <resource>
      <directory>src/main/resources</directory>
      <excludes>
        <exclude>META-INF/maven/archetype-metadata.xml</exclude>
        <exclude>META-INF/archetype-post-generate.groovy</exclude>  <!-- NOVO -->
      </excludes>
    </resource>
  </resources>

  <plugins>
    <plugin>
      <groupId>org.codehaus.mojo</groupId>
      <artifactId>xml-maven-plugin</artifactId>
      <!-- (inalterado) -->
    </plugin>

    <plugin>                                                       <!-- NOVO -->
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
  </plugins>

  <pluginManagement>
    <plugins>
      <plugin>
        <artifactId>maven-archetype-plugin</artifactId>
        <version>3.4.1</version>
      </plugin>
    </plugins>
  </pluginManagement>
</build>
```

**Estrutura da modificação do `script_core.sh` (linhas 41-42):**

```bash
# ANTES:
source "${SCRIPT_FOLDER_WIN}\\create_archetype_post_generate.sh"
create_archetype_post_generate_groovy "${RECREATE_ARCHETYPE}" "${META_INF_FOLDER_WIN}" "archetype-post-generate.groovy" "$SHOW_RECREATE_ARCHETYPE_LOG"

# DEPOIS:
# create_archetype_post_generate.sh foi substituído por SnippetComposer.groovy
# (executado via groovy-maven-plugin:2.1.1 na fase process-resources).
# Arquivo mantido em disco como referência histórica.
```

**Estrutura do `.gitignore`:**

```gitignore
# Adicionar (em algum lugar lógico):
target/classes/META-INF/archetype-post-generate.groovy
```

**Structure Decision**: Modificação in-place do archetype-v2-eda, adicionando 1 diretório novo (`src/main/groovy/eda/postgenerate/`), 1 arquivo novo (`SnippetComposer.groovy`), e ajustes pontuais em `pom.xml` / `script_core.sh` / `.gitignore`. Nenhum módulo ou projeto novo é criado — a complexidade do projeto permanece inalterada.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

Nenhuma violação. Tabela vazia por design.