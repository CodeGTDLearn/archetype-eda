# Índice — mapa dos arquivos e ordem de leitura

Este índice é o mapa do **Procedimento para Especificação**.

Todos os caminhos abaixo são caminhos **dentro do JAR** do archetype, não caminhos do projeto gerado.

## 1. Arquivos de `llm-maven-flags-inference/`

| Ordem | Caminho no JAR | Papel | Obrigatório |
|---:|---|---|---|
| 1 | `llm-maven-flags-inference/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md` | Porta de entrada | Sim |
| 2 | `llm-maven-flags-inference/01-entrada-inicializacao/indice-01-mapa-dos-arquivos-e-ordem-de-leitura.md` | Este índice | Sim |
| 3 | `llm-maven-flags-inference/02-operacao-procedimento-do-agente/prompt-01-papel-e-restricoes-do-agente.md` | Papel e restrições | Sim |
| 4 | `llm-maven-flags-inference/02-operacao-procedimento-do-agente/prompt-02-como-localizar-e-ler-o-archetype.md` | Localização e leitura do JAR | Sim |
| 5 | `llm-maven-flags-inference/02-operacao-procedimento-do-agente/prompt-03-regras-obrigatorias-de-geracao.md` | Regras obrigatórias de geração | Sim |
| 6 | `llm-maven-flags-inference/02-operacao-procedimento-do-agente/prompt-04-fluxo-de-execucao-e-verificacao.md` | Execução e verificação | Sim |
| 7 | `llm-maven-flags-inference/02-operacao-procedimento-do-agente/prompt-05-entregaveis-e-relatorios.md` | Entregáveis e relatórios | Sim |
| 8 | `llm-maven-flags-inference/03-derivacao-regras-de-formacao-do-contrato/01-como-derivar-a-entidade-do-contrato.md` | Entidade, nome, pacote e atributos | Sim |
| 9 | `llm-maven-flags-inference/03-derivacao-regras-de-formacao-do-contrato/02-como-resolver-tipos-e-enums.md` | Tipos Java e enums | Sim |
| 10 | `llm-maven-flags-inference/03-derivacao-regras-de-formacao-do-contrato/03-como-derivar-flags-de-tipos-aninhados.md` | Imports, enums e tipos aninhados | Sim |
| 11 | `llm-maven-flags-inference/03-derivacao-regras-de-formacao-do-contrato/04-como-aplicar-precedencia-e-validacao.md` | Precedência e validação | Sim |
| 12 | `llm-maven-flags-inference/03-derivacao-regras-de-formacao-do-contrato/05-como-materializar-e-emitir-o-contrato.md` | Contrato materializado | Sim |
| 13 | `llm-maven-flags-inference/03-derivacao-regras-de-formacao-do-contrato/06-como-relatar-proveniencia.md` | Proveniência | Sim |
| 14 | `llm-maven-flags-inference/04-referencia-templates-de-contrato/template-01-schema-do-contrato.json` | Schema permissivo do contrato | Sim |
| 15 | `llm-maven-flags-inference/04-referencia-templates-de-contrato/template-02-exemplo-contrato-plano.json` | Exemplo de contrato plano | Sob demanda |
| 16 | `llm-maven-flags-inference/04-referencia-templates-de-contrato/template-03-exemplo-contrato-com-objetos-aninhados.json` | Exemplo com objetos aninhados | Sob demanda |
| 17 | `llm-maven-flags-inference/04-referencia-templates-de-contrato/template-04-exemplo-contrato-com-enums.json` | Exemplo com enums | Sob demanda |
| 18 | `llm-maven-flags-inference/04-referencia-templates-de-contrato/template-05-exemplo-contrato-com-tipos-do-projeto.json` | Exemplo com tipos do projeto | Sob demanda |
| 19 | `llm-maven-flags-inference/04-referencia-templates-de-contrato/template-06-exemplo-contrato-com-attributes-explicito.json` | Exemplo com `attributes` explícito | Sob demanda |

## 2. Leituras técnicas no JAR

Depois de ler `prompt-02`, leia também:

| Caminho | Papel | Obrigatório |
|---|---|---|
| `META-INF/maven/archetype-metadata.xml` | Catálogo consolidado de flags | Sim |
| `META-INF/maven/archetype_metadata_required_properties/*.xml` | Detalhe por flag | Não; enriquece o catálogo |
| `META-INF/archetype-post-generate.groovy` | Comportamento do pós-processador | Sim |
| `archetype-resources/**` | Templates do projeto gerado | Sim |

## 3. Ordem global

1. `prompt-00`
2. Este índice
3. `prompt-01`
4. `prompt-02`
5. Leituras técnicas do JAR
6. `prompt-03`
7. `prompt-04`
8. `prompt-05`
9. `01` a `06`
10. `template-01` e o exemplo que combinar com o contrato

Nenhuma derivação ou execução deve começar antes de concluir as leituras obrigatórias.

## 4. Regra de manutenção

- Todo arquivo novo em `llm-maven-flags-inference/` precisa entrar neste índice.
- Todo arquivo removido precisa sair deste índice.
- O índice é a única fonte da ordem global.
- O build valida que os 19 arquivos existem e que o índice os lista.

## 5. Regra de falha

Arquivo obrigatório ausente → pare com STOP-ENV-02 e reporte.

Arquivo opcional ausente → avise e siga usando o consolidado.
