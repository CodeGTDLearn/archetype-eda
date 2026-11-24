# Contract — Mensagem de Parada Dura

**Feature**: 007-hard-stop-contract | **Date**: 2026-11-24

## Objetivo

Definir o formato único de toda mensagem de parada e de confirmação do procedimento empacotado. O contrato é normativo para o `prompt-00` e para as suítes de teste.

## Código

- Padrão: `STOP-<CATEGORIA>-<NN>`.
- Categorias: `IN`, `ENV`, `DER`, `EXEC`, `RULE`, `CONF`.
- `NN`: dois dígitos, começando em `01`.
- O código é único e precisa ter uma linha no Registro de Paradas.

## Os 10 campos

| # | Campo | Rótulo no template | Regra |
|---:|---|---|---|
| 1 | Código | `## PARADA DURA — STOP-...` | Título destacado, delimitadores e código |
| 2 | O que aconteceu | `**O que aconteceu:**` | Fato objetivo, uma frase curta |
| 3 | Onde está | `**Onde está:**` | Nome legível da seção + arquivo/caminho/campo/comando |
| 4 | Por que parou | `**Por que parou:**` | Regra que exigiu a parada, em linguagem simples |
| 5 | O que não foi executado | `**O que não foi executado:**` | Nenhuma derivação, nenhum Maven, nenhum módulo |
| 6 | Opções de correção | `**Opções de correção:**` | 2 ou 3 opções acionáveis, com exemplo |
| 7 | Sugestão | `**Sugestão:**` | Opção recomendada + motivo em uma linha |
| 8 | Como retomar | `**Como retomar:**` | O que corrigir e de onde o procedimento recomeça |
| 9 | O que não fazer | `**O que não fazer:**` | Proibições específicas do caso |
| 10 | Segurança | `**Segurança:**` | Não expor segredos, credenciais ou o contrato completo |

Além dos 10 campos, toda mensagem termina com **Dicionário:** `prompt-00` §0.4 (FR-007).

## Template canônico

```text
---
> ## PARADA DURA — STOP-XXX
>
> **O que aconteceu:** fato objetivo, em uma frase curta.
> **Onde está:** arquivo, seção, campo ou comando — com nome legível.
> **Por que parou:** a regra em linguagem simples; se citar a seção, dê o nome dela.
> **O que não foi executado:** nada foi gerado; nenhum comando do Maven foi rodado.
>
> **Opções de correção:**
> 1. opção acionável, com exemplo.
> 2. opção acionável, com exemplo.
> 3. opção acionável, quando existir.
>
> **Sugestão:** opção recomendada + motivo em uma linha.
> **Como retomar:** o que corrigir e de onde o procedimento recomeça.
> **O que não fazer:** proibições específicas do caso.
> **Segurança:** não exponha segredos, credenciais ou o contrato completo.
> **Dicionário:** `prompt-00` §0.4.
---
```

## Exemplo completo — STOP-DER-02

```text
---
> ## PARADA DURA — STOP-DER-02
>
> **O que aconteceu:** um dos campos da entidade tem um nome ou um tipo que o sistema não aceita.
> **Onde está:** no contrato que você enviou, na seção "4. CONTRATO JSON". O trecho com problema é: <segmento>.
> **Por que parou:** o sistema só aceita campos no formato "Tipo:nome" — por exemplo, "String:nome". O valor padrão "String:name" não serve, porque criaria um campo chamado "name" que não existe no seu contrato.
> **O que não foi executado:** o comando do Maven não foi montado, nada foi gerado e nenhum arquivo foi criado.
>
> **Opções de correção:**
> 1. Corrija o campo no contrato para o formato "Tipo:nome" — por exemplo, troque "nome" por "String:nome".
> 2. Remova o campo do contrato, se ele não faz parte da entidade.
> 3. Escreva a lista completa de campos no contrato, no formato "Tipo:nome,Tipo:nome".
>
> **Sugestão:** use a opção 1 se o campo é necessário; use a opção 3 se você quer definir a lista inteira.
> **Como retomar:** salve o contrato corrigido e comece o procedimento de novo, desde a leitura do use-case.
> **O que não fazer:** não use "String:name"; não edite o código gerado; não rode o Maven.
> **Segurança:** não coloque senhas, chaves ou credenciais no contrato; não exponha o contrato completo.
> **Dicionário:** `prompt-00` §0.4.
---
```

## Regras de redação

- Uma ideia por frase.
- Cada frase tem no máximo 20 palavras.
- Voz ativa: "corrija", "salve", "recomece".
- Toda sigla e todo termo técnico são traduzidos na primeira aparição, entre parênteses ou em uma frase curta.
- O local usa o nome legível da seção e o caminho, não apenas o número.
- A linguagem é neutra e factual; não culpa o operador.
- Cada opção tem um exemplo concreto.
- A mensagem não imprime segredos, credenciais nem o contrato completo.

## Invariantes

| ID | Invariante | Verificação |
|---|---|---|
| MSG-01 | Título "PARADA DURA" + código + delimitadores | `HardStopContractTest` |
| MSG-02 | 10 campos na ordem | `HardStopContractTest` |
| MSG-03 | 2+ opções; `STOP-DER-02` com 3 | `HardStopContractTest` |
| MSG-04 | Sugestão, retomada, proibição e segurança não vazias | `HardStopContractTest` |
| MSG-05 | Referência ao dicionário `§0.4` | `HardStopContractTest` |
| MSG-06 | Nenhuma ocorrência de "informe apenas" no procedimento | `HardStopContractTest` |
| MSG-07 | `STOP-DER-02` não usa `String:name` como fallback | `HardStopContractTest` |
| MSG-08 | Mensagens em português e sem segredos | Revisão + teste de padrões proibidos |
| MSG-09 | Cada frase de prosa tem no máximo 20 palavras | `HardStopContractTest` |
| MSG-10 | `String:name` pode aparecer como valor proibido, mas não como fallback de lista inválida | `HardStopContractTest` |

## Notas de teste

- O teste de 20 palavras ignora conteúdo entre backticks, caminhos e URLs; conta as palavras da prosa.
- Voz ativa e tradução de siglas são verificadas por revisão; o dicionário prova que os 20 termos têm tradução.
- O teste de `STOP-DER-02` não exige a ausência da string `String:name`: a mensagem precisa citá-la como valor proibido. O teste exige que o parágrafo de fallback de `regras-01` §6 tenha sido removido.

## Mapeamento para os requisitos

| Requisito | Cobertura |
|---|---|
| FR-001 | 10 campos |
| FR-003 | Título, código, delimitadores, rótulos em negrito |
| FR-004 | Causa, local, regra, não executado, opções, sugestão, retomada, proibição |
| FR-005 | Regras de redação |
| FR-007 | Referência ao dicionário em todas as mensagens |
| FR-010 | 3 opções em `STOP-DER-02` |
| FR-012 | 15 outras paradas + 2 confirmações no mesmo template |
| FR-025 | Português e proteção de segredos |
