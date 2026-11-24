# Quickstart: Contrato de Destino de Geração

**Feature**: `005-output-directory-contract`
**Date**: 2026-11-19
**Spec**: [spec.md](./spec.md) · [Research](./research.md) · [Data Model](./data-model.md) · [Contracts](./contracts/procedure-interface.md)

Guia de validação da feature. Cada cenário é **executável** e tem um resultado observável. Este documento valida; a implementação pertence a `tasks.md`.

---

## Pré-requisitos

| # | Requisito | Verificação |
|---|-----------|-------------|
| P1 | JDK 21 e Maven 3.9.16 | `mvn -v` |
| P2 | Repositório limpo (só o README pode estar modificado) | `git status --short` |
| P3 | Artefato instalado e registrado no catálogo local | existe `~/.m2/repository/com/eda-archetype/eda-archetype/0.0.1-SNAPSHOT/eda-archetype-0.0.1-SNAPSHOT.jar` e `~/.m2/repository/archetype-catalog.xml` registra `com.eda-archetype:eda-archetype` |

---

## Cenário 1 — A regra está declarada no procedimento empacotado

**O que prova**: a regra de destino existe nos arquivos esperados do procedimento.

```powershell
mvn -B clean process-resources
```

**Verificação** (por leitura do procedimento montado):

```powershell
Select-String -Path "target/classes/utils/**/*.md" -Pattern "PASTA DE DESTINO"
Select-String -Path "target/classes/utils/**/*.md" -Pattern "outputDirectory"
```

**Resultado esperado**: `PASTA DE DESTINO` aparece em `prompt-00` e `regras-01`; `outputDirectory` aparece em `prompt-03` e `prompt-04`.

---

## Cenário 2 — O procedimento compilado está íntegro

**O que prova**: as contagens travadas por gate continuam válidas — nenhum arquivo novo, nenhuma flag nova.

```powershell
mvn -B clean test
```

**Resultado esperado**: `ALL CHECKS PASSED` em `ProcedureContractTest`, `BuildArtifactsTest` e `DestinationContractTest`. Nenhuma suíte falha.

**Detecta**: arquivos adicionados/removidos de `utils/`, flags criadas, metachaves ampliadas, caminho de usuário residual, `robocopy` residual.

---

## Cenário 3 — O JAR reinstalado carrega a regra

**O que prova**: o agente, que lê do artefato instalado, recebe a regra nova. Este é o modo de falha recorrente do projeto (Princípio VI).

```powershell
mvn -B clean install "-Darchetype.test.skip=true"
jar tf "$env:USERPROFILE/.m2/repository/com/eda-archetype/eda-archetype/0.0.1-SNAPSHOT/eda-archetype-0.0.1-SNAPSHOT.jar" | Select-String "utils/"
```

**Resultado esperado**: os 19 arquivos de `utils/` presentes no JAR.

```powershell
$jar = "$env:USERPROFILE/.m2/repository/com/eda-archetype/eda-archetype/0.0.1-SNAPSHOT/eda-archetype-0.0.1-SNAPSHOT.jar"
$tmp = "$env:TEMP/opencode/eda-005-jar"
Remove-Item $tmp -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force -Path $tmp | Out-Null
Push-Location $tmp
jar xf $jar utils
Pop-Location
Select-String -Path "$tmp/utils/**/*.md" -Pattern "PASTA DE DESTINO"
```

**Resultado esperado**: o marcador aparece **dentro do JAR reinstalado** — não apenas na árvore de desenvolvimento.

---

## Cenário 4 — Geração real com destino informado

**O que prova**: o agente lê a 5ª entrada, emite a propriedade nativa e o módulo nasce onde foi pedido.

**Spec de teste** (formato do contrato em [contracts](./contracts/procedure-interface.md)):

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
C:\Temp\eda-005-destino
```

**Verificações**:

```powershell
Test-Path "C:\Temp\eda-005-destino\cliente\pom.xml"
Get-ChildItem "C:\Temp\eda-005-destino"
git status --short
```

**Resultado esperado**:
- o módulo existe em `C:\Temp\eda-005-destino\cliente\` com `pom.xml` próprio;
- a linha de comando emitida contém `-DoutputDirectory=C:\Temp\eda-005-destino`;
- `git status` **não** mostra nada novo dentro da árvore do archetype;
- o relatório final informa o caminho absoluto.

---

## Cenário 5 — Destino ausente: o agente pergunta antes de derivar

**O que prova**: nenhum destino é presumido em silêncio.

**Spec de teste**: idêntico ao Cenário 4, **sem** o bloco `# PASTA DE DESTINO`.

**Resultado esperado**:
- o agente **pergunta uma vez** qual é a pasta de destino;
- **nenhuma** derivação e **nenhuma** execução ocorrem antes da resposta;
- nada é criado em disco;
- em modo não interativo, o agente **para** com relatório do que falta.

---

## Cenário 6 — Destino dentro da árvore do archetype: recusa absoluta

**O que prova**: o lugar proibido nunca é aceito, nem por insistência.

**Spec de teste**: bloco `# PASTA DE DESTINO` apontando para dentro da árvore do archetype.

**Resultado esperado**:
- o agente recusa com aviso explícito e pergunta onde fica o destino;
- **nada** é criado;
- se o operador insistir que o destino é a própria pasta do archetype, o fluxo é **finalizado** com relatório de parada.

```powershell
git status --short
```

**Resultado esperado**: nenhuma criação dentro da árvore.

---

## Cenário 7 — Pasta de destino inexistente: confirmação antes de criar

**O que prova**: nada é escrito no disco do operador sem confirmação.

**Spec de teste**: `# PASTA DE DESTINO` apontando para uma pasta que não existe.

**Resultado esperado**:
- o agente pergunta mostrando o caminho completo que será criado;
- a pasta **só** é criada após a confirmação;
- se a criação falhar depois da confirmação, o agente para e reporta.

---

## Cenário 8 — Módulo de mesmo nome já existe: nada é apagado

**O que prova**: o agente não destrói trabalho do operador.

**Pré-condição**: `C:\Temp\eda-005-destino\cliente\` já existe, com conteúdo.

**Resultado esperado**:
- o agente pergunta uma vez, oferecendo "sobrescrever o existente" ou "manter o existente e encerrar";
- **nada** é apagado sem a escolha explícita;
- escolhida a opção "manter", o fluxo encerra sem alterar nada.

---

## Cenário 9 — A regra não contaminou o contrato

**O que prova**: a marcação nunca vira atributo da entidade.

**Spec de teste**: contrato **sem** marcação, e o mesmo contrato **com** a marcação de destino escrita dentro dele.

**Resultado esperado**: a lista de atributos derivados é **idêntica** nos dois casos; a entidade gerada não tem campo de destino.

---

## Cenário 10 — Regressão: nada regrediu

**O que prova**: a feature não alterou nada fora do seu escopo.

```powershell
mvn -B clean install "-Darchetype.test.skip=true"
mvn -B clean integration-test
```

**Resultado esperado**:
- as **40** suítes unitárias verdes;
- as **22** fixtures de integração verdes;
- o catálogo com **35** flags e **18** filesets;
- `utils/` com **19** arquivos.

**Detecta**: qualquer alteração acidental no catálogo, nos templates, no pós-processador ou nas fixtures.

---

## Matriz de rastreio cenário → requisito

| Cenário | FR cobertos |
|---|---|
| 1 | FR-001, FR-002, FR-003, FR-004, FR-009, FR-018, FR-019, FR-021 |
| 2 | FR-010, FR-011, FR-012, FR-020, FR-023, FR-024, FR-025, FR-029, FR-030 |
| 3 | FR-031, FR-032 |
| 4 | FR-009, FR-016, FR-017, FR-018, FR-019, SC-001, SC-002, SC-004, SC-005 |
| 5 | FR-005, FR-006, FR-007, FR-008, SC-003 |
| 6 | FR-014, SC-011 |
| 7 | FR-013 |
| 8 | FR-015 |
| 9 | FR-022, FR-023, SC-006 |
| 10 | FR-024, FR-025, FR-026, FR-027, FR-028, SC-007, SC-008, SC-009, SC-010 |

**Cobertura**: os 32 requisitos funcionais da spec têm pelo menos um cenário de validação.

---

## Nota sobre a automação do agente

Os Cenários 4 a 9 exercitam o **agente** (um LLM), não código determinístico. Eles são **homologação manual** — o mesmo critério dos prompts de homologação já existentes no README (§7, Cenários A a D). A parte determinística da feature (a regra estar declarada, as contagens, a ausência de caminho de usuário) é coberta automaticamente pelo Cenário 2.

---

## Limpeza ao final

```powershell
Remove-Item "C:\Temp\eda-005-destino" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item "$env:TEMP/opencode/eda-005-jar" -Recurse -Force -ErrorAction SilentlyContinue
git status --short
```

**Resultado esperado**: apenas o README modificado (trabalho em andamento, fora do escopo da feature).
