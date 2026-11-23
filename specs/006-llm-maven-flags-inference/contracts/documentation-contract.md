# Contrato de Documentação — llm-maven-flags-inference

**Feature**: 006-llm-maven-flags-inference | **Date**: 2026-11-23

## Tópico do README

### Título

H2 com o texto exato:

```text
llm-maven-flags-inference
```

### Declarações obrigatórias

- A pasta é o alvo do prompt do agente.
- A pasta responde pelo pré-processamento.

### Sequência didática

| Passo | Conteúdo obrigatório |
|---|---|
| 1 | O operador escreve o prompt na Spec |
| 2 | O prompt aponta para o archetype e para esta pasta dentro do JAR |
| 3 | O agente lê a entrada e o índice |
| 4 | O agente aplica as regras e materializa o contrato |
| 5 | O resultado é a linha `mvn archetype:generate` |

### Frase de fronteira

O tópico deve declarar, de forma explícita:

- o pré-processamento termina na linha `mvn archetype:generate`;
- a geração Maven começa depois dela.

### Subseções

| Subseção | Conteúdo | Limite |
|---|---|---|
| `01-entrada-inicializacao` | Porta de entrada, índice e ordem de leitura | 1 parágrafo curto |
| `02-operacao-procedimento-do-agente` | Papel, leitura do JAR, regras, execução e entregáveis | 1 parágrafo curto |
| `03-derivacao-regras-de-formacao-do-contrato` | Regras normativas de derivação | 1 parágrafo curto |
| `04-referencia-templates-de-contrato` | Schema e exemplos de contrato | 1 parágrafo curto |

Cada parágrafo diz a função e o momento na sequência. Nenhum parágrafo repete o conteúdo das regras.

## Preâmbulo dos templates de spec

| Local | Regra |
|---|---|
| Template canônico do README | `0. PROCEDIMENTO` aponta para o caminho canônico |
| Cenário A | Idem |
| Cenário B | Idem |
| Templates do kit | Nome do conceito `llm-maven-flags-inference` |

As cinco entradas do spec permanecem na ordem atual.

## Regra anti-drift

### Corpus verificado

- `README.md`
- `pom.xml` (descritor de build)
- `.specify/templates/spec-template.md`, `plan-template.md`, `tasks-template.md`
- Procedimento empacotado em `target/classes/llm-maven-flags-inference/`
- Comentários de `src/test/resources/projects/*/archetype.properties`

### Termos proibidos

| Termo | Exceção |
|---|---|
| `utils/` | Apenas o redirect |
| `` `utils` `` | Apenas o redirect |
| `eda-llm-flag-inference` | Nenhuma |

### Exclusões

- Specs históricas 003, 004 e 005.
- `specs/_archive/`.
- `graphify-out/`.
- O próprio arquivo de redirect.
- As asserções da suíte que verifica o redirect.

## Verificação

| Check | Requisito |
|---|---|
| README tem 1 tópico com o título exato | FR-008 |
| README tem a sequência de 5 passos | FR-009 |
| README tem a frase de fronteira | FR-009 |
| README tem 4 subseções com os nomes atuais | FR-010 |
| Templates citam apenas o nome novo | FR-007 |
| Corpus operacional tem 0 termos proibidos | FR-006, FR-012 |
| Redirect não contém regra | FR-005 |
| Contagem e layout conferem | FR-002 |
| JAR instalado contém a nova raiz e o redirect | FR-013 |
