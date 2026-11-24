# Contract — Verificação de SC-002, NFR-001 e NFR-002

**Feature**: 007-hard-stop-contract | **Date**: 2026-11-24

## Objetivo

Tornar auditáveis a compreensão do operador (SC-002), o limite de tempo do build (NFR-001) e o determinismo offline (NFR-002). A evidência é registrada em `specs/007-hard-stop-contract/validation-report.md`.

## SC-002 — Operador iniciante

### Amostra fixa

A amostra mínima tem **6 mensagens** e inclui obrigatoriamente `STOP-DER-02` e as 2 confirmações. Para evitar escolha enviesada, use esta amostra fixa:

| # | Código | Por que está na amostra |
|---:|---|---|
| 1 | `STOP-IN-01` | Parada de entrada mais comum (use-case ausente). |
| 2 | `STOP-ENV-03` | Parada de ambiente com instruções de reinstalação. |
| 3 | `STOP-DER-01` | Parada de derivação com candidatos e escolha. |
| 4 | `STOP-DER-02` | Parada central da feature. |
| 5 | `STOP-CONF-01` | Confirmação de pasta inexistente. |
| 6 | `STOP-CONF-02` | Confirmação de módulo existente. |

### Revisor

- O revisor **não participou da implementação** das mensagens.
- O revisor pode ser o próprio operador iniciante, desde que não tenha implementado.
- O revisor registra nome (ou identificador), data e resultado.

### Perguntas e registro

Para cada mensagem, o revisor responde sem ajuda externa:

1. O que aconteceu?
2. Onde está o problema?
3. Quais são as opções?
4. Qual é a recomendada?
5. O que faço agora para retomar?

Registre em `validation-report.md` com as colunas:

| Mensagem | O que aconteceu | Onde está | Opções | Sugestão | Retomada | Resultado | Revisor | Data |
|---|---|---|---|---|---|---|---|---|
| `STOP-IN-01` | | | | | | | | |
| `STOP-ENV-03` | | | | | | | | |
| `STOP-DER-01` | | | | | | | | |
| `STOP-DER-02` | | | | | | | | |
| `STOP-CONF-01` | | | | | | | | |
| `STOP-CONF-02` | | | | | | | | |

- **Aceite**: 6/6 linhas com as 5 colunas corretas e `Resultado = OK`.
- **Falha**: qualquer linha com resposta ausente ou incorreta; corrija a mensagem e repita a revisão.

## NFR-001 — Tempo de build

### Procedimento

1. No mesmo ambiente, com as dependências já em cache, rode `mvn -B test` uma vez para aquecer.
2. Meça a linha de base:

```powershell
$baseline = (Measure-Command { mvn -B test }).TotalSeconds
```

3. Depois da implementação, meça novamente:

```powershell
$final = (Measure-Command { mvn -B test }).TotalSeconds
$delta = (($final - $baseline) / $baseline) * 100
```

4. Registre `baseline`, `final` e `delta` em `validation-report.md`.

- **Aceite**: `delta ≤ 10%`.
- **Falha**: `delta > 10%`; investigue a causa e otimize antes de concluir. Repita a medição se o ambiente estiver sob carga anormal.

## NFR-002 — Determinismo offline

### Procedimento

1. Rode as suítes em modo offline duas vezes:

```powershell
$run1 = Join-Path $env:TEMP "eda-run1.txt"
$run2 = Join-Path $env:TEMP "eda-run2.txt"
mvn -o -B test | Select-String '^\[archetype-post-generate\]' | Set-Content $run1
mvn -o -B test | Select-String '^\[archetype-post-generate\]' | Set-Content $run2
Compare-Object (Get-Content $run1) (Get-Content $run2)
```

2. Registre o resultado em `validation-report.md`.

- **Aceite**: `Compare-Object` sem diferenças; nenhuma chamada de rede.
- **Falha**: saídas diferentes; identifique e corrija a fonte de não determinismo antes de concluir.
- Se `mvn -o` falhar por dependência ausente no cache, trate como problema de ambiente e prepare o cache antes da medição.

## Evidência

- `specs/007-hard-stop-contract/validation-report.md` é o registro oficial.
- Não grave resultados dentro de `quickstart.md`; ele é o guia reutilizável.
- Não inclua segredos, credenciais ou o contrato completo na evidência.

## Invariantes

| ID | Invariante | Verificação |
|---|---|---|
| VER-01 | Amostra de SC-002 tem exatamente 6 mensagens, incluindo `STOP-DER-02` e as 2 confirmações | `validation-report.md` |
| VER-02 | O revisor de SC-002 não participou da implementação | `validation-report.md` |
| VER-03 | Cada linha de SC-002 tem as 5 respostas e `Resultado = OK` | `validation-report.md` |
| VER-04 | NFR-001 registra baseline, final e delta ≤10% | `validation-report.md` |
| VER-05 | NFR-002 registra duas execuções offline com a mesma saída | `validation-report.md` |

## Mapeamento para os requisitos

| Requisito | Cobertura |
|---|---|
| SC-002 | Amostra de 6, revisor independente, 5 perguntas e tabela |
| NFR-001 | Baseline local, medição antes/depois e delta ≤10% |
| NFR-002 | Duas execuções offline e comparação de saída |
