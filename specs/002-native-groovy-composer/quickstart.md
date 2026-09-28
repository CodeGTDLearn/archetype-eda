# Quickstart: Validar Compositor Nativo Groovy

**Feature**: 002-native-groovy-composer
**Phase**: 1 (design)
**Created**: 2026-09-28

## Cenário de Validação Completo (end-to-end)

Este guia permite ao mantenedor validar que o compositor Groovy produz o arquivo `archetype-post-generate.groovy` esperado e que o ciclo IT continua passando.

### Pré-requisitos

- Maven 3.9.x instalado e no PATH.
- JDK 21 (ou JDK 17 — Groovy 4.0.28 suporta ambos; archetype-v2-eda usa JDK 21 conforme stack atual).
- Git configurado para operar no repositório.
- Windows com Git Bash OU Linux/macOS nativo.

### Passo 1: Capturar a "golden file" antes da migração

```bash
# Em uma branch paralela ou antes de aplicar mudanças:
mvn clean install -DskipTests
# Captura o arquivo commitado atualmente como referência:
cp target/classes/META-INF/archetype-post-generate.groovy /tmp/golden-archetype-post-generate.groovy
sha256sum /tmp/golden-archetype-post-generate.groovy  # guarde o hash
```

### Passo 2: Aplicar mudanças do compositor Groovy

1. Adicionar `org.codehaus.gmaven:groovy-maven-plugin:2.1.1` ao `pom.xml` com `<dependencies>` fixando `groovy:4.0.28`.
2. Criar `src/main/groovy/eda/postgenerate/SnippetComposer.groovy` com `List<String> SECTIONS`.
3. Adicionar `<exclude>META-INF/archetype-post-generate.groovy</exclude>` ao `<resources><resource>` do `pom.xml`.
4. Modificar `script_core.sh` para parar de invocar o bash compositor (remover linhas 41-42).

### Passo 3: Validar byte-equivalência

```bash
mvn clean install -DskipTests
diff /tmp/golden-archetype-post-generate.groovy target/classes/META-INF/archetype-post-generate.groovy
# Saída esperada: (vazio, exit code 0)

sha256sum target/classes/META-INF/archetype-post-generate.groovy
# Deve ser idêntico ao hash capturado no Passo 1.
```

### Passo 4: Validar ciclo IT completo

```bash
mvn clean install
# Esperado: BUILD SUCCESS, 9/9 perfis IT (basic, full, minimal, binding-*, json-contract)

mvn integration-test
# Esperado: BUILD SUCCESS, 9/9 perfis IT processados
```

### Passo 5: Validar TTY-independence

```bash
# Linux/macOS:
mvn clean install < /dev/null
# Esperado: BUILD SUCCESS, sem pause

# Windows (PowerShell):
Get-Content NUL | mvn clean install
# Esperado: BUILD SUCCESS, sem pause
```

### Passo 6: Validar zero `read -n 1 -s` no pipeline

```bash
grep -rE 'read -n 1 -s|read -p' pom.xml src/ bash_script/script_core.sh bash_script/create_archetype_post_generate.sh
# Esperado: zero matches nos arquivos do pipeline principal (script_core.sh pode reter o padrão para outros usos legítimos)
```

### Passo 7: Validar adição de nova flag

1. Criar `src/main/resources/META-INF/maven/archetype_metadata_properties/property-test-flag.xml`:
   ```xml
   <requiredProperty key="enable_test_flag">
     <defaultValue>false</defaultValue>
   </requiredProperty>
   ```
2. Criar `src/main/resources/META-INF/archetype_post_generate_variables/variables-test-flag.groovy`:
   ```groovy
   def keep_test_flag = "${properties?.enable_test_flag}"
   ```
3. Criar `src/main/resources/META-INF/archetype_post_generate_snippets/snippet-test-flag.groovy`:
   ```groovy
   // FLAG: enable_test_flag
   // Remove test folder if flag is off
   if (keep_test_flag == 'false') {
     // ... delete logic ...
   }
   ```
4. Adicionar `"variables-test-flag.groovy"` e `"snippet-test-flag.groovy"` na `List<String> SECTIONS` do compositor (em ordem alfabética nas listas de variables/snippets).
5. Rodar `mvn clean install -DskipTests`.
6. Verificar que `target/classes/META-INF/archetype-post-generate.groovy` contém o novo snippet.

**Tempo esperado**: < 5 minutos do passo 1 ao passo 6.

### Critérios de Aceitação Final

- ✅ Diff byte-equivalente entre golden file e output do compositor.
- ✅ 9/9 perfis IT passando em `mvn clean install`.
- ✅ 9/9 perfis IT passando em `mvn integration-test`.
- ✅ Build prossegue sem TTY em Linux headless.
- ✅ Zero `read -n 1 -s` no pipeline principal.
- ✅ Adicionar nova flag leva < 5 minutos.
- ✅ Tempo de execução do compositor ≤ tempo do bash legado.