# Contract — Registro de Paradas e Sites

**Feature**: 007-hard-stop-contract | **Date**: 2026-11-24

## Objetivo

Fixar os 18 códigos canônicos, seus gatilhos, as opções obrigatórias e os arquivos onde cada código aparece. O registro fica no `prompt-00` §0.5; os sites apenas citam o código.

## Registro canônico

| Código | Gatilho | Site principal | Opções | Sugestão |
|---|---|---|---:|---|
| `STOP-IN-01` | use-case ausente, em branco ou sem marcador | `prompt-00` Gate A | 3 | acrescentar a seção "3. USE-CASE" |
| `STOP-IN-02` | contrato ausente, em branco, `{}` ou não-objeto | `prompt-00` Gate B; `regras-01` §2; `prompt-03` §2 | 3 | acrescentar um objeto JSON válido |
| `STOP-IN-03` | contrato antes do use-case | `prompt-00` Gate C | 3 | mover o contrato para depois do use-case |
| `STOP-IN-04` | destino ausente, relativo, com `~`, com fim de arquivo ou dentro do archetype | `prompt-00` Gate D; `prompt-03` §3; `prompt-04` §1.3 | 3 | informar um caminho absoluto fora do archetype |
| `STOP-IN-05` | package base ausente ou ambíguo | `prompt-00` §6; `prompt-03` §1 | 3 | declarar um único package base |
| `STOP-ENV-01` | JAR instalado não encontrado | `prompt-02` §1 | 3 | instalar o JAR com `mvn clean install` |
| `STOP-ENV-02` | arquivo obrigatório do procedimento ou árvore obrigatória ausente | `indice-01` §5; `prompt-00` §8; `prompt-02` §6 | 3 | reinstalar o JAR completo |
| `STOP-ENV-03` | catálogo ausente ou ilegível | `prompt-00` §6; `prompt-02` §6 | 3 | reinstalar e conferir 35 propriedades/18 filesets |
| `STOP-ENV-04` | pós-processador ausente | `prompt-00` §6; `prompt-02` §6 | 3 | reinstalar o JAR completo |
| `STOP-ENV-05` | várias versões instaladas e o spec não declara qual usar | `prompt-02` §1.4 | 3 | declarar a versão no spec |
| `STOP-DER-01` | nome de entidade ambíguo | `regras-01` §2 | 3 | declarar `entity_name` no contrato |
| `STOP-DER-02` | lista de atributos derivada inválida | `regras-01` §6; `regras-04` §3; `prompt-04` §1.2; `regras-06` | 3 | corrigir o campo para `Tipo:nome` |
| `STOP-DER-03` | duas fontes de mesma prioridade divergem | `regras-04` §7; `prompt-00` §6 | 3 | escolher a fonte correta |
| `STOP-DER-04` | caminho do contrato não absoluto ou falha ao gravar | `regras-05` §5/§7; `prompt-03` §2; `prompt-04` §1.4 | 3 | usar um diretório absoluto gravável |
| `STOP-EXEC-01` | falha de catálogo ou de pós-processador durante a geração | `prompt-04` §4 | 3 | reinstalar o JAR e repetir |
| `STOP-RULE-01` | o procedimento não cobre um caso | `prompt-01` §4; `prompt-00` §6 | 3 | registrar a regra que falta |
| `STOP-CONF-01` | pasta de destino ainda não existe | `prompt-04` §1.3 | 3 | criar a pasta |
| `STOP-CONF-02` | já existe módulo com o mesmo nome | `prompt-04` §1.3 | 3 | escolher outro destino |

## Opções obrigatórias

### Entrada

- `STOP-IN-01`: acrescentar a seção; acrescentar o marcador; usar o template de spec.
- `STOP-IN-02`: acrescentar um objeto; corrigir a sintaxe; usar um exemplo do template.
- `STOP-IN-03`: mover o contrato; mover o use-case; refazer o spec na ordem.
- `STOP-IN-04`: informar um caminho absoluto; escolher outra pasta; corrigir o caminho inválido.
- `STOP-IN-05`: declarar o package base; remover o nome da entidade; escolher um dos candidatos.

### Ambiente

- `STOP-ENV-01`: instalar o JAR; conferir a versão; apontar para o caminho correto.
- `STOP-ENV-02`: reinstalar; conferir os 19 arquivos; usar um JAR completo.
- `STOP-ENV-03`: reinstalar; reconstruir o catálogo; conferir 35 propriedades e 18 filesets.
- `STOP-ENV-04`: reinstalar; reconstruir; usar um JAR completo.
- `STOP-ENV-05`: declarar a versão; escolher uma versão instalada; remover versões antigas.

### Derivação

- `STOP-DER-01`: declarar `entity_name`; nomear a entidade no use-case; escolher um candidato.
- `STOP-DER-02`: corrigir o campo; remover o campo; declarar a lista completa.
- `STOP-DER-03`: escolher a fonte A; escolher a fonte B; declarar o valor no contrato.
- `STOP-DER-04`: informar um diretório absoluto; corrigir a permissão; usar o diretório padrão de contratos.

### Execução e regra

- `STOP-EXEC-01`: reinstalar e repetir; corrigir catálogo/pós-processador; reportar comando e saída.
- `STOP-RULE-01`: acrescentar a regra; consultar o mantenedor; usar uma regra existente.

### Confirmações

- `STOP-CONF-01`: criar a pasta; escolher outra pasta; cancelar.
- `STOP-CONF-02`: sobrescrever o módulo; manter o existente e encerrar; escolher outro destino.

## Sites e convenção de teste

- Toda ordem de interrupção usa a forma **Pare com `STOP-...`**.
- O teste `HardStopContractTest` varre as linhas dos 13 Markdown e exige código em toda linha com a palavra isolada `pare` e em todo bloco `PARADA DURA`.
- As mensagens do registro usam `Por que parou`, não `pare`, para não se autoidentificarem como site.
- Um código pode aparecer em mais de um site; todos precisam estar registrados.
- Um código registrado sem mensagem falha.
- Um site com código não registrado falha.
- Um site com `Pare` ou um bloco `PARADA DURA` sem código falha e nomeia o arquivo.

## Mapa de sites

| Arquivo | Seções com código |
|---|---|
| `prompt-00` | Gates A–D, §6, §8; mensagens de §0.5 |
| `indice-01` | §5 |
| `prompt-01` | §4 |
| `prompt-02` | §1, §1.4, §6 |
| `prompt-03` | §1, §2, §3 |
| `prompt-04` | §1.2, §1.3, §1.4, §4 |
| `regras-01` | §2, §6 |
| `regras-04` | §3, §7 |
| `regras-05` | §5, §7 |
| `regras-06` | §2, §5 |

`prompt-05`, `regras-02`, `regras-03` e os 6 JSON não têm site de parada.

## Invariantes

| ID | Invariante | Verificação |
|---|---|---|
| REG-01 | 16 paradas + 2 confirmações | `HardStopContractTest` |
| REG-02 | 5 IN, 5 ENV, 4 DER, 1 EXEC, 1 RULE, 2 CONF | `HardStopContractTest` |
| REG-03 | 18 códigos únicos | `HardStopContractTest` |
| REG-04 | 18 mensagens, uma por código | `HardStopContractTest` |
| REG-05 | Todo site com `Pare` e todo bloco `PARADA DURA` citam código registrado | `HardStopContractTest` |
| REG-06 | `STOP-CONF-01` e `STOP-CONF-02` com exatamente as opções da FR-031/FR-032 | `HardStopContractTest` |
| REG-07 | `STOP-DER-02` com exatamente 3 opções | `HardStopContractTest` |

## Mapeamento para os requisitos

| Requisito | Cobertura |
|---|---|
| FR-002 | Código único + linha no registro |
| FR-008 | `STOP-DER-02` para lista inválida |
| FR-009 | Sem `String:name` e sem Maven |
| FR-010 | 3 opções de `STOP-DER-02` |
| FR-012 | 15 outras paradas + 2 confirmações |
| FR-019 | Build falha para código/site ausente |
| FR-031 | Opções da confirmação de pasta |
| FR-032 | Opções da confirmação de módulo |
