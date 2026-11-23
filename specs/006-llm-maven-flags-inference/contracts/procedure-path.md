# Contrato do Caminho — llm-maven-flags-inference

**Feature**: 006-llm-maven-flags-inference | **Date**: 2026-11-23

## Escopo

Este contrato define o caminho público do procedimento empacotado, consumido por:

- o preâmbulo `0. PROCEDIMENTO` dos templates de spec;
- o agente que lê o procedimento de dentro do JAR instalado;
- as suítes de contrato do repositório;
- a documentação operacional.

## Caminho canônico

```text
llm-maven-flags-inference/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md
```

O caminho é relativo à raiz do JAR e à raiz `src/main/resources/`.

## Layout

| # | Caminho relativo à raiz | Papel |
|---|---|---|
| 1 | `01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md` | Entrada |
| 2 | `01-entrada-inicializacao/indice-01-mapa-dos-arquivos-e-ordem-de-leitura.md` | Índice |
| 3 | `02-operacao-procedimento-do-agente/prompt-01-papel-e-restricoes-do-agente.md` | Papel |
| 4 | `02-operacao-procedimento-do-agente/prompt-02-como-localizar-e-ler-o-archetype.md` | Leitura do JAR |
| 5 | `02-operacao-procedimento-do-agente/prompt-03-regras-obrigatorias-de-geracao.md` | Regras |
| 6 | `02-operacao-procedimento-do-agente/prompt-04-fluxo-de-execucao-e-verificacao.md` | Execução |
| 7 | `02-operacao-procedimento-do-agente/prompt-05-entregaveis-e-relatorios.md` | Entregáveis |
| 8 | `03-derivacao-regras-de-formacao-do-contrato/regras-01-como-derivar-a-entidade-do-contrato.md` | Derivação |
| 9 | `03-derivacao-regras-de-formacao-do-contrato/regras-02-como-resolver-tipos-e-enums.md` | Derivação |
| 10 | `03-derivacao-regras-de-formacao-do-contrato/regras-03-como-derivar-flags-de-tipos-aninhados.md` | Derivação |
| 11 | `03-derivacao-regras-de-formacao-do-contrato/regras-04-como-aplicar-precedencia-e-validacao.md` | Derivação |
| 12 | `03-derivacao-regras-de-formacao-do-contrato/regras-05-como-materializar-e-emitir-o-contrato.md` | Derivação |
| 13 | `03-derivacao-regras-de-formacao-do-contrato/regras-06-como-relatar-proveniencia.md` | Derivação |
| 14 | `04-referencia-templates-de-contrato/template-01-schema-do-contrato.json` | Schema |
| 15 | `04-referencia-templates-de-contrato/template-02-exemplo-contrato-plano.json` | Exemplo |
| 16 | `04-referencia-templates-de-contrato/template-03-exemplo-contrato-com-objetos-aninhados.json` | Exemplo |
| 17 | `04-referencia-templates-de-contrato/template-04-exemplo-contrato-com-enums.json` | Exemplo |
| 18 | `04-referencia-templates-de-contrato/template-05-exemplo-contrato-com-tipos-do-projeto.json` | Exemplo |
| 19 | `04-referencia-templates-de-contrato/template-06-exemplo-contrato-com-attributes-explicito.json` | Exemplo |

## Contrato do redirect

| Atributo | Valor |
|---|---|
| Caminho legado | `utils/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md` |
| Alvo | `llm-maven-flags-inference/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md` |
| Conteúdo | Ponteiro para o alvo |
| Regras | Nenhuma |
| Outros arquivos no caminho antigo | Nenhum |

O redirect é o único artefato autorizado a citar o caminho antigo.

## Compatibilidade

- Spec antiga continua válida quando segue o preâmbulo obrigatório: o stub do `prompt-00` leva à nova entrada.
- Referências internas antigas fora do preâmbulo não são suportadas.
- Spec nova usa o caminho canônico direto.
- As duas formas devem produzir a mesma linha `mvn archetype:generate`.

## Invariantes verificáveis

| ID | Invariante |
|---|---|
| PC-01 | A raiz tem exatamente 19 arquivos |
| PC-02 | Existem exatamente 4 subpastas, com os nomes atuais |
| PC-03 | Os 19 caminhos relativos são exatamente os da tabela |
| PC-04 | O caminho antigo tem exatamente 1 arquivo, no caminho do `prompt-00` |
| PC-05 | O redirect não contém regra |
| PC-06 | O JAR contém a nova raiz e o redirect |
| PC-07 | Nenhum documento operacional cita o caminho antigo fora do redirect |

## Falhas previstas

| Falha | Efeito |
|---|---|
| Entrada nova ausente | A verificação falha; o agente não tem porta de entrada |
| Redirect ausente | Spec antiga não chega ao procedimento; a verificação falha |
| Arquivo extra no caminho antigo | A verificação falha (duplicação) |
| Contagem diferente de 19 | A verificação falha |
| Subpasta renomeada | A verificação falha |
| Citação do caminho antigo fora do redirect | A verificação anti-drift falha |

## Política de mudança

- O caminho é contrato público: uma nova evolução exige redirect próprio e atualização das suítes.
- Nunca duplicar regra no caminho antigo.
- O caminho antigo não recebe conteúdo novo.
