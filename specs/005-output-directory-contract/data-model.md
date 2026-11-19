# Data Model: Contrato de Destino de Geração

**Feature**: `005-output-directory-contract`
**Date**: 2026-11-19
**Spec**: [spec.md](./spec.md) · **Research**: [research.md](./research.md)

Este feature **não cria estruturas de dados de produção**. Ele define **vocabulário** — os conceitos que o procedimento empacotado passa a reconhecer — e as **regras de validação** que incidem sobre eles.

---

## 1. Conceitos

### 1.1 Destino de geração

**O que é**: o caminho absoluto onde o módulo gerado será criado.

| Atributo | Tipo | Regra |
|---|---|---|
| Valor | caminho absoluto | MUST ser absoluto (FR-003) — relativo, `~` ou com sinalização de fim de arquivo é recusado |
| Origem | marcação `PASTA DE DESTINO` no spec | MUST NEVER vir do contrato (FR-022) |
| Ausência | — | MUST ser tratada como destino ausente → Gate de destino (FR-005) |
| Natureza | entrada **obrigatória** | NOT uma flag do catálogo, NOT um atributo da entidade, NOT uma fonte de proveniência (FR-020) |
| Transporte | propriedade nativa `outputDirectory` | `-DoutputDirectory=<destino>` (FR-009) |

**Relação com o módulo gerado**: o módulo nasce em `<destino>/<artifactId>` — composição já provada pelo pós-processador (`body-05-path-root-variables.groovy:13` → `Paths.get(request.outputDirectory, request.artifactId)`).

**Relação com a propriedade nativa**: o destino **é** o valor da propriedade `outputDirectory`. O default nativo (diretório de execução) é o lugar proibido e não pode valer sem decisão explícita (FR-021).

### 1.2 Marcação `PASTA DE DESTINO`

**O que é**: o marcador que separa, no spec do operador, a entrada de destino das demais entradas.

| Atributo | Valor |
|---|---|
| Forma | `PASTA DE DESTINO` — maiúsculas, palavra composta |
| Convenção | segue os marcadores existentes do spec (`USE-CASE`, `CONTRATO`) |
| Colisão | impossível com chave de contrato (palavra composta) |
| Gate de suíte | **não** casa com `^[a-z][a-z0-9_]*_[a-z0-9_]+$` → não quebra `ProcedureContractTest` 1.4 |

**Formas equivalentes reconhecidas**: a spec define `PASTA DE DESTINO` como marcador canônico.

### 1.3 Entradas do spec do operador

Antes → 4 entradas. **Depois → 5 entradas**.

| # | Entrada | Origem | É flag? | É metachave? |
|---|---------|--------|---------|--------------|
| 1 | Tipo de módulo (`RAIZ`/`SECUNDARIO`) | spec | não | — |
| 2 | Package base | spec | não | — |
| 3 | Use-case | spec | não | — |
| 4 | Contrato JSON | spec | não | — |
| 5 | **Pasta de destino** (`PASTA DE DESTINO`) | spec | **não** | **não** |

**Regra de classificação**: dentro do contrato, toda chave que não seja flag do catálogo permanece **atributo da entidade** — a lista de metachaves **não** é ampliada por esta feature (FR-022, FR-023).

### 1.4 Procedimento empacotado

**O que é**: o conjunto de **19** arquivos que viajam dentro do JAR e que o agente lê em runtime. É a fonte da verdade sobre o destino.

| Atributo | Valor | Regra |
|---|---|---|
| Contagem | 19 | imutável (FR-025) |
| Ordem de leitura | definida por `indice-01` | imutável (nenhum arquivo novo) |
| Natureza | linguagem natural para o agente | nenhuma flag nova (FR-024) |

---

## 2. Estados do destino

O destino percorre um ciclo de validação com **transições** determinísticas.

```
        [AUSENTE]
            │
            │ agente pergunta (1x)
            ▼
      [PERGUNTADO] ──── operador responde ────► [INFORMADO]
            │                                        │
            │ sem operador                          │ valida
            ▼                                        ▼
      [PARADO]  ◄───┐                    ┌───────────┴───────────┐
                    │                    │                       │
                    │            [DENTRO DA ÁRVORE]      [RELATIVO / INVÁLIDO]
                    │              (recusa absoluta)      (recusa + nova pergunta)
                    │                    │                       │
                    │                    └───────────┬───────────┘
                    │                                │
                    │                        nova pergunta (1x)
                    │                                │
                    │◄───────────────────────────────┘
                    │
                    │ recusa absoluta + nova pergunta
                    ▼
          [DENTRO DA ÁRVORE] ──── operador insiste ────► [FINALIZADO]
                                                        (relatório de parada)

        [INFORMADO]
            │
            │ já existe?
            ├── não ──► [A CRIAR] ──► pergunta com caminho completo ──► confirma ──► cria
            │                       └── não confirma ──► [PARADO]
            │
            └── sim ──► [JÁ EXISTE] ──► pergunta: sobrescrever | manter
                                          ├── manter ──► [FINALIZADO]
                                          └── sobrescrever ──► [PRONTO]
                                            (nunca apaga sem escolha explícita)

        [PRONTO] ──► gera ──► [GERADO]
                       │
                       ├─► confirma raiz = <destino>/<artifactId> + pom.xml próprio
                       ├─► mvn clean compile DENTRO da raiz gerada
                       └─► relatório final: caminho absoluto + command line + resultado
```

### 2.1 Transições e gatilhos

| De | Para | Gatilho | Resultado |
|----|------|---------|-----------|
| AUSENTE | PERGUNTADO | ausência da marcação | 1 pergunta; nenhuma derivação/execução antes dela (FR-005) |
| PERGUNTADO | INFORMADO | resposta do operador | prossegue |
| PERGUNTADO | PARADO | sem operador / sem resposta | relatório de parada; **0** destinos presumidos (FR-007) |
| INFORMADO | DENTRO DA ÁRVORE | destino dentro da árvore do archetype | **recusa absoluta**, sem exceção (FR-014) |
| DENTRO DA ÁRVORE | FINALIZADO | operador insiste na própria pasta do archetype | fluxo finalizado com relatório (FR-014) |
| INFORMADO | RELATIVO/INVÁLIDO | caminho relativo, `~`, EOF | recusa + nova pergunta (FR-003) |
| INFORMADO | A CRIAR | pasta inexistente | pergunta com caminho completo; cria **só** após confirmação (FR-013) |
| INFORMADO | JÁ EXISTE | módulo de mesmo nome presente | pergunta "sobrescrever" \| "manter" (FR-015) |
| A CRIAR | PRONTO | confirmação do operador | pasta criada |
| A CRIAR | PARADO | criação falhou | relatório de parada |
| JÁ EXISTE | PRONTO | escolha "sobrescrever" | sobrescreve; **nunca** apaga sozinho |
| JÁ EXISTE | FINALIZADO | escolha "manter" | encerra sem alterar nada |
| PRONTO | GERADO | execução bem-sucedida | módulo em `<destino>/<artifactId>` (FR-016) |

**Invariante do ciclo**: a partir de qualquer estado, o agente **nunca** executa a geração sem um destino **informado**, **válido** e **fora da árvore do archetype**.

---

## 3. Regras de validação consolidadas

| # | Regra | FR | Resultado da falha |
|---|-------|-----|--------------------|
| V1 | Destino MUST ser absoluto | FR-003 | recusa + nova pergunta |
| V2 | Destino MUST NOT conter `~` ou sinalização de fim de arquivo | FR-003 | recusa + nova pergunta |
| V3 | Destino MUST NOT estar dentro da árvore do archetype | FR-014 | **recusa absoluta** + nova pergunta; se insistir → finalizado |
| V4 | Destino MUST existir ou ser criável | FR-013 | pergunta com caminho completo; falha na criação → parado |
| V5 | Criação de pasta MUST exigir confirmação | FR-013 | sem confirmação, nada é criado |
| V6 | Sobrescrita MUST exigir escolha explícita | FR-015 | nada é sobrescrito/apagado sem escolha |
| V7 | Marcação MUST NOT estar dentro do contrato | FR-022 | reconhecida como entrada do spec; não altera a lista de atributos |
| V8 | Destino MUST ser emitido na linha de comando | FR-009 | — (emissão obrigatória) |
| V9 | Destino MUST NOT ser tratado como flag | FR-020 | — (restrição estrutural) |
| V10 | Default nativo MUST NOT valer sem decisão | FR-021 | agente pergunta antes de derivar |

---

## 4. Verificação

As regras acima são verificadas por leitura do procedimento empacotado — o objeto verificado é o **texto normativo**, não o runtime do agente (que é um LLM, não determinístico).

| Regra | Verificada por |
|-------|----------------|
| V8, V9 | `ProcedureContractTest` — o template da command line cita `outputDirectory`; a propriedade não aparece no catálogo |
| V3, V11 (remoção do `cd`+`robocopy`) | `DestinationContractTest` — nenhum caminho absoluto de usuário; nenhum `robocopy` |
| V7 | `ProcedureContractTest` — a lista de metachaves não foi ampliada |
| V1..V10 (declaração) | `DestinationContractTest` — as regras estão declaradas nos arquivos esperados |
| Contagens (19 / 35 / 18) | `DestinationContractTest` + `BuildArtifactsTest` + `ReadmeConsistencyTest` |
| Artefato reinstalado | `DestinationContractTest` lê `utils/` **de dentro do JAR reinstalado**, não da árvore de desenvolvimento (FR-032) |
