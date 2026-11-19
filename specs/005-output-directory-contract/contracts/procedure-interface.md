# Contracts: Contrato de Destino de Geração

**Feature**: `005-output-directory-contract`
**Date**: 2026-11-19
**Spec**: [spec.md](../spec.md) · **Research**: [research.md](../research.md) · **Data Model**: [data-model.md](../data-model.md)

O archetype expõe **quatro interfaces externas**. Este documento registra apenas a que muda; as demais seguem inalteradas.

| Interface | Muda? |
|---|---|
| **I1. Spec do operador → procedimento** (entrada textual) | ✅ **SIM** — nova 5ª entrada |
| **I2. Procedimento → linha de comando Maven** | ✅ **SIM** — nova propriedade emitida |
| **I3. Procedimento → relatório ao operador** | ✅ **SIM** — novo campo |
| **I4. Arquétipo → projeto gerado** (artefatos em disco) | ❌ não — o conteúdo gerado é idêntico |

---

## I1. Spec do operador → procedimento

### Formato geral

O spec é texto livre. As entradas são reconhecidas por **marcadores**, não por posição.

```text
# MÓDULO
[X] RAIZ

# PACKAGE BASE
org.clinica

# USE-CASE
Cadastro de clientes de uma clínica. API REST na porta 8080.

# CONTRATO
{
  "entityName": "Cliente",
  "attributes": [
    { "name": "nome", "value": "Maria" },
    { "name": "ativo", "value": true }
  ]
}

# PASTA DE DESTINO
C:\Temp\eda-sandbox
```

### Entradas reconhecidas

| Marcador | Entrada | Obrigatória |
|----------|---------|-------------|
| `USE-CASE` / `USE CASE` | texto do caso de uso | ✅ sim (Gate A) |
| `CONTRATO` / `CONTRATO JSON` | objeto JSON do domínio | ✅ sim (Gate B, depois do use-case — Gate C) |
| **`PASTA DE DESTINO`** | caminho absoluto da geração | ✅ **sim** (novo gate de destino) |

### Regras da entrada de destino

| # | Regra |
|---|-------|
| D-1 | O valor MUST ser um caminho **absoluto** |
| D-2 | MUST NOT ser relativo, conter `~` ou sinalização de fim de arquivo |
| D-3 | MUST NOT apontar para dentro da árvore do archetype |
| D-4 | MUST NOT ser escrito **dentro do contrato** — a marcação pertence ao spec |
| D-5 | Se ausente → o agente **pergunta uma vez**, antes de qualquer derivação |
| D-6 | Se o agente oferecer sugestão, ela MUST ser uma pasta temporária **fora** da árvore do archetype, e MUST NOT ser aplicada sem confirmação |

### Exemplos

**Válido**

```text
# PASTA DE DESTINO
C:\Temp\eda-sandbox
```

```text
# PASTA DE DESTINO
D:/projetos/cliente-modulo
```

**Inválidos e o que acontece**

| Entrada | Motivo | Comportamento |
|---------|--------|----------------|
| `C:\Temp\cliente\` | barra final | normalizada antes de emitir |
| `.\cliente` | relativo | recusa + nova pergunta |
| `~/projetos` | expansão de usuário | recusa + nova pergunta |
| `C:\DEV\SCRIPTS\archetype-v2-eda\modulos` | dentro da árvore do archetype | **recusa absoluta**; se o operador insistir → finalizado |
| ausente | — | 1 pergunta antes de derivar |

---

## I2. Procedimento → linha de comando Maven

### Forma emitida

O destino viaja como a propriedade **nativa** `outputDirectory` do `maven-archetype-plugin` — **não** como flag do catálogo.

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
  '-Dentity_package_name=cliente' `
  '-DoutputDirectory=C:\Temp\eda-sandbox'
```

### Regras de emissão

| # | Regra | FR |
|---|-------|-----|
| C-1 | A propriedade MUST ser emitida em **toda** geração | FR-009, FR-021 |
| C-2 | O valor MUST usar barras normais, sem barra final, sem quebra de linha | FR-009 |
| C-3 | Todas as propriedades MUST permanecer protegidas contra interpretação do shell | FR-010 |
| C-4 | O caminho MUST receber a mesma proteção quando contiver espaços, acentos ou `\` | FR-010 |
| C-5 | É o **único** mecanismo de destino — o mecanismo "mudar de diretório + mover depois" MUST ser removido | FR-011 |
| C-6 | O destino MUST NOT ser emitido como flag do catálogo | FR-020 |

### Propriedades nativas do plugin — o que NÃO faz parte desta feature

O plugin expõe outras propriedades nativas (identificação do archetype, `groupId`, `artifactId`, `version`, `package`, `goals`, `filter`, `archetypeCatalog`, `interactiveMode`, `askForDefaultPropertyValues`).

**Escopo: exclusivamente o destino** (FR-028). As demais **não** são objeto desta feature e **não** são registradas como trabalho futuro.

> ⚠️ **Gate de suíte**: o nome `outputDirectory` **não** pode ser reescrito em forma `snake_case` (ex.: `output_directory`) dentro de `utils/*.md` — a forma `snake_case` entre backticks que não seja flag do catálogo quebra `ProcedureContractTest` 1.4. A forma canônica é `outputDirectory`.

---

## I3. Procedimento → relatório ao operador

### Antes da execução

```
Destino de geração: C:\Temp\eda-sandbox
```

Exibido **antes** de executar, junto da tabela de proveniência (FR-018).

### No relatório final

Novo campo, junto da command line executada (FR-019):

```text
Destino de geração: C:\Temp\eda-sandbox
Módulo gerado em: C:\Temp\eda-sandbox\cliente-module
Command line executada: mvn archetype:generate --batch-mode ... '-DoutputDirectory=C:\Temp\eda-sandbox'
Resultado do mvn clean compile: BUILD SUCCESS
```

### Regra de proveniência

O destino **não** é flag. Logo:
- **não** ocupa linha na tabela de proveniência de flags;
- **não** recebe fonte (`CONTRACT` / `CONTRACT_DERIVED` / `INFERENCE` / `DEFAULT`);
- **não** aparece na seção de propostas descartadas.

É reportado em campo próprio, nomeado e explícito (FR-020).

---

## I4. Arquétipo → projeto gerado

**inalterada.** O conteúdo gerado — arquivos, estrutura, flags,entity, contrato copiado — é byte-a-byte o mesmo. A feature muda **onde** o módulo nasce, **nada** do que ele contém.

Composição da raiz gerada (já provada pelo pós-processador):

```
<destino>/<artifactId>/
```

**Remoção do movimento posterior**: com o mecanismo único, o módulo **já nasce** no destino final. A movimentação manual (`robocopy … /MOVE`) deixa de existir.

---

## I5. Contrato de integridade do artefato

O procedimento empacotado viaja no JAR e é verificado quanto à sua integridade.

| Elemento | Regra | Verificado por |
|----------|-------|----------------|
| Contagem de arquivos do procedimento | **19**, imutável | `BuildArtifactsTest` 5.2/5.3/5.4 · `DestinationContractTest` |
| Contagem de flags do catálogo | **35**, imutável | `BuildArtifactsTest` 2.3/2.4 · `DestinationContractTest` |
| Contagem de conjuntos de arquivos | **18**, imutável | `DestinationContractTest` |
| Lista de metachaves | **inalterada** — a marcação de destino não entra | `ProcedureContractTest` |
| Caminho absoluto de usuário | **ausente** | `DestinationContractTest` |
| Mecanismo "mudar diretório + mover" | **ausente** | `DestinationContractTest` |
| Procedimento lido de | **artefato reinstalado**, não da árvore de desenvolvimento | `DestinationContractTest` (FR-032) |
