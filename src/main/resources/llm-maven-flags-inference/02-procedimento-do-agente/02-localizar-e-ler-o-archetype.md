# Como localizar e ler o archetype

## 1. Localizar o artefato

Coordenadas:

```text
com.eda-archetype:eda-archetype
```

Caminho normal no repositório local:

```text
~/.m2/repository/com/eda-archetype/eda-archetype/<versão>/eda-archetype-<versão>.jar
```

No Windows:

```text
%USERPROFILE%\.m2\repository\com\eda-archetype\eda-archetype\<versão>\eda-archetype-<versão>.jar
```

Se o JAR não for encontrado, pare com STOP-ENV-01 e reporte o caminho ausente.

### Resolução de versão

1. Se o spec declarar a versão, use-a.
2. Senão, leia as versões instaladas em
   `~/.m2/repository/com/eda-archetype/eda-archetype/` (Windows:
   `%USERPROFILE%\.m2\repository\com\eda-archetype\eda-archetype\`).
3. Se houver exatamente uma versão instalada, use-a.
4. Se houver várias versões e o spec não apontar nenhuma, pare com STOP-ENV-05 e pergunte.
5. Fora do repositório-fonte do archetype, nunca use o `pom.xml` do projeto
   hospedeiro como fonte de versão.

## 2. Listar e extrair

Liste as entradas:

```powershell
jar tf <jar>
```

Extraia para um diretório temporário:

```powershell
jar xf <jar> llm-maven-flags-inference
jar xf <jar> META-INF/maven/archetype-metadata.xml
jar xf <jar> META-INF/archetype-post-generate.groovy
jar xf <jar> archetype-resources
```

Nunca altere o JAR.

## 3. O que ler

### 3.1 Catálogo consolidado

```text
META-INF/maven/archetype-metadata.xml
```

É a fonte primária das flags.

### 3.2 Fragmentos por flag

```text
META-INF/maven/archetype_metadata_required_properties/*.xml
```

Se existirem, enriquecem o catálogo. Se não existirem, use o consolidado e avise.

### 3.3 Pós-processador

```text
META-INF/archetype-post-generate.groovy
```

Mostra o comportamento da cópia do contrato, da geração de enums e da geração de POJOs aninhados.

### 3.4 Templates

```text
archetype-resources/**
```

Leia os templates relevantes:

- o template da entidade;
- o template que compõe o `package`;
- os templates que leem `custom_entity_attribute_imports`, `contract_enum_definitions` e `contract_nested_type_definitions`.

## 4. Como interpretar o catálogo

| Campo | Uso |
|---|---|
| `key` | nome da flag |
| `defaultValue` | fallback; não pode ser vazio |
| `validationRegex` | gate final do valor |
| `DESCRIPTION` | descrição humana |
| `PROMPT` | valor esperado |
| `TYPE` | tipo do valor |
| `DOMAIN` | formato ou domínio |
| `DEPENDS_ON` | dependência ou guarda |
| `VALIDATION` | forma legível da regex |

Algumas descrições ainda dizem "mecanismo de inferência LLM", por herança histórica. Neste procedimento, quem deriva é o agente: não procure nem execute motor, CLI ou script externo por causa disso.

Armadilha equivalente: a descrição de `custom_entity_attributes_list` no catálogo declara que o `id` é estrutural. O template o descarta e emite `<entityName>Id` como chave primária. Essa descrição é normativa e vale: o `id` do contrato não vira atributo comum (ver `01` §4.3).

## 5. Separação do contrato

- Chave que existe no catálogo → flag.
- `entityName`, `entity`, `package`, `description`, `attributes`, `fields` → meta-chave.
- Qualquer outra chave de topo → campo de negócio.

Atenção:

- `entity_name` é flag e também fonte do nome da entidade.
- `entity_package_name` é flag.
- `package` no contrato é meta; o package base vem do spec.

## 6. Fallback de leitura

- Se os fragmentos `property-*.xml` não existirem, use o consolidado.
- Se o consolidado existir mas não tiver `defaultValue` ou `validationRegex`, pare com STOP-ENV-03.
- Se o pós-processador não existir, pare com STOP-ENV-04: o JAR está incompleto.
- Se `archetype-resources/` não existir, pare com STOP-ENV-02: o JAR está incompleto.

## 7. Repositório-fonte

Se você estiver operando dentro do repositório do archetype, os mesmos arquivos estão em `src/main/resources/`.

Use o repositório-fonte apenas quando o usuário pedir explicitamente. O caminho normal é o JAR instalado.

## 8. Proibição

Não use código de motor, CLI ou script externo para redescobrir semântica de flags. As regras estão em:

```text
llm-maven-flags-inference/03-derivacao-regras-de-formacao-do-contrato/
```
