# Validation Report — Paradas Duras

**Feature**: 007-hard-stop-contract | **Date**: 2026-11-24
**Status**: Implementação concluída; SC-002 aprovado.

Este relatório é o registro oficial de SC-002, NFR-001 e NFR-002. Preencha durante a validação; não grave resultados no `quickstart.md`.

## SC-002 — Operador iniciante

**Amostra fixa**: 6 mensagens, incluindo `STOP-DER-02` e as 2 confirmações.
**Revisor**: Paulo (não participou da implementação).
**Aceite**: 6/6 linhas com as 5 respostas corretas e `Resultado = OK`.

| Mensagem | O que aconteceu | Onde está | Opções | Sugestão | Retomada | Resultado | Revisor | Data |
|---|---|---|---|---|---|---|---|---|
| `STOP-IN-01` | Spec sem a seção de use-case | Antes do contrato | Criar a seção, usar o marcador ou o template | Criar a seção ou usar o template | Salvar e recomeçar pelo Gate A | OK | Paulo | 2026-11-25 |
| `STOP-ENV-03` | Catálogo ausente ou ilegível | `META-INF/maven/archetype-metadata.xml` | Reinstalar o JAR, reconstruir ou conferir 35/18 | Reinstalar o JAR | Voltar ao início e ler o catálogo | OK | Paulo | 2026-11-25 |
| `STOP-DER-01` | Mais de um nome de entidade possível | Use-case ou campo do nome no contrato | Declarar `entity_name`, nomear no use-case ou escolher candidato | Declarar `entity_name` | Salvar e recomeçar pela derivação | OK | Paulo | 2026-11-25 |
| `STOP-DER-02` | Campo fora do formato `Tipo:nome` | Contrato, seção 4 | Corrigir, remover o campo ou escrever a lista completa | Corrigir ou declarar a lista completa | Salvar e recomeçar desde o use-case | OK | Paulo | 2026-11-25 |
| `STOP-CONF-01` | Pasta de destino não existe | Seção 5. PASTA DE DESTINO | Criar, escolher outra ou cancelar | Criar a pasta | Confirmar e continuar | OK | Paulo | 2026-11-25 |
| `STOP-CONF-02` | Módulo com o mesmo nome já existe | `<destino>/<artifactId>` | Sobrescrever, manter e encerrar ou escolher outro destino | Escolher outro destino | Confirmar e continuar | OK | Paulo | 2026-11-25 |

## NFR-001 — Tempo de build

**Procedimento**: `mvn -B test` para aquecer; medir baseline; implementar; medir final.
**Aceite**: `(final - baseline) / baseline ≤ 10%`.

| Baseline (s) | Final (s) | Delta (%) | Resultado | Data |
|---:|---:|---:|---|---|
| 23.87 | 23.97 | 0.41 | OK | 2026-11-24 |

## NFR-002 — Determinismo offline

**Procedimento**: `mvn -o -B test` duas vezes; comparar as linhas `[archetype-post-generate]`.
**Aceite**: saídas idênticas, sem chamada de rede.

| Execução 1 | Execução 2 | Diferenças | Resultado | Data |
|---|---|---|---|---|
| 85 linhas | 85 linhas | 0 | OK | 2026-11-24 |

## STOP-DER-02 — Prova manual

**Cenário**: contrato com lista final inválida; ver `quickstart.md` §Cenário 5.
**Execução**: 2026-11-24, sessão independente (subagente, contexto novo), spec `t025/spec.md` e destino `C:\Users\edufi\AppData\Local\Temp\opencode\t025-destino-a2fa5ee`.
**Resultado**: a mensagem exibiu `STOP-DER-02`, o segmento `"cpf-cliente": "123"` (lista derivada `String:cpf-cliente`), o formato `Tipo:nome`, 3 opções, sugestão e retomada. Nenhuma linha `mvn archetype:generate` apareceu. O destino ficou com 0 módulos.
**Data**: 2026-11-24.

## Negativos US3

**Cenário**: remover o código `STOP-ENV-02` do site `indice-01` §5.
**Resultado**: `HardStopContractTest` 1.12 falhou e nomeou `indice-01-mapa-dos-arquivos-e-ordem-de-leitura.md:66`; o arquivo foi restaurado e o `git diff` ficou sem diferença.
**Cobertura**: os demais negativos de código, mensagem, dicionário e termo são exercitados por 1.1–1.13 e 2.1–2.4; os negativos de tabela ficam para US4.
**Data**: 2026-11-24.

## US5 — Linguagem simples

**Leitura manual**: `regras-04` §3.1/§3 e `prompt-03` §2.1, sem consultar outro documento.
**Tabela**: 3 colunas e 9 linhas; pipes internos escapados.
**FR-006**: explicado em uma frase, apontando para o dicionário de 20 termos em `prompt-00` §0.4.
**Pacote x artefato**: `PedidoItem` → `pedidoitem` e `PedidoItem` → `pedido-item`, na mesma seção.
**Resultado**: os três itens são compreensíveis sem ajuda externa.
**Data**: 2026-11-24.

## Polish — Cenários funcionais (T047)

**Cenário 1**: `mvn -B test` — 42/42 suítes, `BUILD SUCCESS`.
**Cenário 2**: `mvn clean install` — `BUILD SUCCESS`; 19 arquivos, 35 propriedades e 18 filesets empacotados.
**Cenário 3**: `mvn -B test "-Deda.verify.installed.jar=true"` — JAR instalado com 18 códigos, dicionário e `STOP-DER-02` (4/4 checks).
**Cenário 4**: 18 mensagens, 20 termos e template de 10 campos em `prompt-00` §0.4/§0.5.
**Cenário 5**: `STOP-DER-02` exibe segmento, formato `Tipo:nome`, 3 opções, sugestão, retomada e nenhuma linha `mvn archetype:generate`. A prova de 0 módulos em execução real está registrada em T025.
**Cenário 6**: tabela com 9 linhas de flag e 3 colunas; 0 linhas com coluna errada; pipes internos escapados.
**Cenário 7**: `property-20` diz caminho absoluto; `none` e regex permissiva intactos.
**Cenário 8**: `regras-06` cita a exceção de `contract_nested_type_definitions = none`; `regras-04` explica `FR-006`.
**Cenário 9**: `prompt-03` §2.1 traz `pedidoitem`, `pedido-item` e kebab-case.
**Cenário 11**: 19 arquivos, 13 Markdown e 6 JSON.
**Data**: 2026-11-24.

## Revisão de linguagem (T048)

**Escopo**: 13 arquivos Markdown de `src/main/resources/llm-maven-flags-inference/`.
**Frases longas**: 21 frases legadas acima de 20 palavras foram divididas; a varredura final encontrou 0.
**Mensagens**: as 18 mensagens já respeitam o limite de 20 palavras.
**Idioma e tom**: português, voz ativa, tom neutro e nenhuma exclamação.
**Acrônimos**: os 20 termos técnicos estão traduzidos no dicionário de `prompt-00` §0.4.
**Seções e caminhos**: títulos e referências de arquivo legíveis no ponto de uso.
**Segredos**: 0 padrões de senha, token, chave ou chave privada.
**Informe apenas**: 0 ocorrências.
**Data**: 2026-11-24.

## Notas

- O revisor de SC-002 não pode ser a pessoa que implementou as mensagens.
- Não inclua segredos, credenciais ou o contrato completo neste relatório.
- Anexe ou referencie os arquivos de saída das duas execuções offline, se necessário.
- Se NFR-001 ou NFR-002 falhar, registre a causa e a correção antes de repetir.
