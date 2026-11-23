# Quickstart — Validar o rename para llm-maven-flags-inference

**Feature**: 006-llm-maven-flags-inference | **Date**: 2026-11-23

## Pré-requisitos

- Java 17+ e Maven 3.9.x no PATH.
- Execução na raiz do repositório.
- PowerShell (os comandos abaixo) ou equivalente.

## Cenário 1 — Layout e contagem

```powershell
$root = "src/main/resources/llm-maven-flags-inference"
(Get-ChildItem -Recurse $root -File).Count
(Get-ChildItem $root -Directory).Name
(Get-ChildItem -Recurse "src/main/resources/utils" -File).Count
```

**Esperado**:

- 19 arquivos na nova raiz.
- 4 subpastas: `01-entrada-inicializacao`, `02-operacao-procedimento-do-agente`, `03-derivacao-regras-de-formacao-do-contrato`, `04-referencia-templates-de-contrato`.
- 1 arquivo no caminho antigo.

**Cobre**: SC-001, FR-001, FR-002, FR-005.

## Cenário 2 — Redirect

```powershell
Get-Content "src/main/resources/utils/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md"
```

**Esperado**: apenas o ponteiro para `llm-maven-flags-inference/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md`; nenhuma regra.

**Cobre**: FR-005, SC-001.

## Cenário 3 — Anti-drift

```powershell
$files = @("README.md") +
    (Get-ChildItem .specify/templates/*.md).FullName +
    (Get-ChildItem -Recurse src/main/resources/llm-maven-flags-inference -Filter *.md).FullName +
    (Get-ChildItem -Recurse src/test/resources/projects -Filter archetype.properties).FullName
$files | Select-String -Pattern 'utils/','eda-llm-flag-inference'
```

**Esperado**: nenhuma ocorrência.

**Cobre**: FR-006, FR-007, FR-012, SC-004.

## Cenário 4 — Estrutura do README

```powershell
Select-String -Path README.md -Pattern '^## llm-maven-flags-inference','^### 01-entrada-inicializacao','^### 02-operacao-procedimento-do-agente','^### 03-derivacao-regras-de-formacao-do-contrato','^### 04-referencia-templates-de-contrato'
```

**Esperado**: 5 ocorrências — 1 tópico e 4 subseções com os nomes atuais.

**Cobre**: FR-008, FR-009, FR-010, SC-005, SC-006.

## Cenário 5 — Suítes e instalação

```powershell
mvn -B clean install "-Darchetype.test.skip=true"
```

**Esperado**: todas as suítes passam, incluindo a suíte de rename e anti-drift; o artefato é instalado em `~/.m2`.

```powershell
mvn -B clean integration-test
```

**Esperado**: todas as fixtures passam; nenhuma regressão no catálogo de 35 flags e nos 18 filesets.

**Cobre**: SC-002, FR-011, FR-012.

## Cenário 6 — JAR instalado

```powershell
$jar = "$env:USERPROFILE\.m2\repository\com\eda-archetype\eda-archetype\0.0.1-SNAPSHOT\eda-archetype-0.0.1-SNAPSHOT.jar"
(jar tf $jar | Where-Object { $_ -match '^llm-maven-flags-inference/.+\.(md|json)$' }).Count
jar tf $jar | Where-Object { $_ -match '^utils/.+\.md$' }
```

**Esperado**:

- 19 arquivos sob `llm-maven-flags-inference/`.
- 1 arquivo sob `utils/`, exatamente o stub do `prompt-00`.

**Cobre**: FR-013, SC-007.

## Cenário 7 — Spec antiga e spec nova (manual)

1. Monte dois specs idênticos em use-case, contrato e pasta de destino.
2. No spec novo, o preâmbulo aponta para `llm-maven-flags-inference/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md`.
3. No spec antigo, o preâmbulo aponta para `utils/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md`.
4. Execute cada spec com o agente contra o JAR instalado.

**Esperado**:

- As duas execuções chegam ao mesmo procedimento.
- As duas emitem a mesma linha `mvn archetype:generate`.
- O pré-processamento termina na linha; a geração Maven começa depois.

**Cobre**: FR-015, SC-003, SC-006.

## Cenário 8 — Somente as suítes

```powershell
mvn -B test
```

**Esperado**: todas as suítes passam, inclusive a de rename e anti-drift.

## Matriz de cobertura

| Cenário | Requisitos | Critérios |
|---|---|---|
| 1 | FR-001, FR-002, FR-005 | SC-001 |
| 2 | FR-005 | SC-001 |
| 3 | FR-006, FR-007, FR-012 | SC-004 |
| 4 | FR-008, FR-009, FR-010 | SC-005, SC-006 |
| 5 | FR-011, FR-012 | SC-002 |
| 6 | FR-013 | SC-007 |
| 7 | FR-015 | SC-003, SC-006 |
