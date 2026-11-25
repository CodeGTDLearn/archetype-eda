# Validation Report — Paradas Duras

**Feature**: 007-hard-stop-contract | **Date**: 2026-11-24
**Status**: Pending implementation

Este relatório é o registro oficial de SC-002, NFR-001 e NFR-002. Preencha durante a validação; não grave resultados no `quickstart.md`.

## SC-002 — Operador iniciante

**Amostra fixa**: 6 mensagens, incluindo `STOP-DER-02` e as 2 confirmações.
**Revisor**: não participou da implementação.
**Aceite**: 6/6 linhas com as 5 respostas corretas e `Resultado = OK`.

| Mensagem | O que aconteceu | Onde está | Opções | Sugestão | Retomada | Resultado | Revisor | Data |
|---|---|---|---|---|---|---|---|---|
| `STOP-IN-01` | | | | | | | | |
| `STOP-ENV-03` | | | | | | | | |
| `STOP-DER-01` | | | | | | | | |
| `STOP-DER-02` | | | | | | | | |
| `STOP-CONF-01` | | | | | | | | |
| `STOP-CONF-02` | | | | | | | | |

## NFR-001 — Tempo de build

**Procedimento**: `mvn -B test` para aquecer; medir baseline; implementar; medir final.
**Aceite**: `(final - baseline) / baseline ≤ 10%`.

| Baseline (s) | Final (s) | Delta (%) | Resultado | Data |
|---:|---:|---:|---|---|
| 23.87 | | | Pendente final | 2026-11-24 |

## NFR-002 — Determinismo offline

**Procedimento**: `mvn -o -B test` duas vezes; comparar as linhas `[archetype-post-generate]`.
**Aceite**: saídas idênticas, sem chamada de rede.

| Execução 1 | Execução 2 | Diferenças | Resultado | Data |
|---|---|---|---|---|
| | | | | |

## STOP-DER-02 — Prova manual

**Cenário**: contrato com lista final inválida; ver `quickstart.md` §Cenário 5.
**Resultado**: a mensagem exibe o segmento, o formato `Tipo:nome`, 3 opções e proíbe o Maven. A prova de 0 módulos em execução real do agente fica pendente.
**Data**: 2026-11-24.

## Notas

- O revisor de SC-002 não pode ser a pessoa que implementou as mensagens.
- Não inclua segredos, credenciais ou o contrato completo neste relatório.
- Anexe ou referencie os arquivos de saída das duas execuções offline, se necessário.
- Se NFR-001 ou NFR-002 falhar, registre a causa e a correção antes de repetir.
