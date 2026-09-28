# Idea Intake: Inferência LLM das flags do `mvn archetype:generate`

- **Slug**: llm-flag-inference
- **Created**: 2026-10-12
- **Source**: pasted text
- **Type**: new-capability

## Idea (as captured)

> 1) O objetivo principal seria eliminar a ideia de Bash desse projeto, Substituindo as perguntas feitas pelo BESH por decisões tomadas e entendidas pela LLM..
> 2) In outras palavras, ao invés de eu ter scripts que captariam as informações, captariam minhas decisões, fariam indagações com promptes para mim, para eu tomar a decision. E esses scripts captariam as decisões, e essas decisões fossem ofertadas pelas FLEGs, eu gostaria que isso fosse feito e inferido com auxílio da LLM.
> 3) No caso, todas as flags do MVN Archetype Generate possuem valores padrão, de maneira que os valores que a LLM não captasse ou entendesse por necessario, ela não precisaria apresentar, porque automaticamente os valores padrão, no caso de não apresentação, seriam assumidos.
> 4) A ideia principal is passar a descrição textual do caso de uso para LLM. Dentro dessa descrição textual a llm infere e identificar quais flags devem ser acionadas andy elas devem ser fornecidas, and a própria LLM fornecer this for a linha de comando MVN Archetype Generate.
> 5) É impreterível que um JSON, que seria o contrato a ser implementado no respectivo módulo, seja fornecido no caso de uso. De maneira que, se na descrição do caso de uso, esse JSON não tenha já originariamente sido fornecido, ele deverá ser solicitado.
> 6) A ideia seria esse mecanismo ser usado pelo Github spec-kit. E na descrição das etapas do Github Spec Kitchen na hora da criação da feature, Seria orientado em cada etapa do SpecKit, nas especificações dadas ao Spec Kit para usar essa ferramenta.

Observação sobre a transcrição: "BESH" no ponto 1 é typo de "Bash"; "FLEGs" no ponto 2 significa "flags". Mantidos verbatim na citação por fidelidade ao original.

## Restated

Substituir os scripts bash que capturam decisões via prompts interativos no fluxo de `mvn archetype:generate` por um mecanismo em que a LLM recebe a descrição textual do caso de uso, infere o mapeamento de flags, exige um JSON contrato (pedindo-o quando ausente) e produz a linha de comando pronta, reaproveitando os valores padrão declarados em `archetype-metadata.xml`. O resultado cacheado por (descrição + JSON contrato) é reutilizado sem nova chamada à LLM. A integração com o `github/spec-kit` acontece por orientação textual nas etapas de spec/plan/test instruindo o implementador a usar o Maven Archetype. Após adoção, os scripts bash legados são deletados.

## Origin & Context

- **Raised by**: dono do projeto `archetype-v2-eda` — único mantenedor.
- **Trigger**: intenção estratégica declarada de eliminar pré-processamento em bash do projeto, ainda não cumprida para o caso da geração da linha `mvn archetype:generate` (o compositor de `archetype-post-generate.groovy` já foi migrado em outro ciclo, ver assessment `groovy-snippet-injection`). Adoção via orientação textual nas etapas do `github/spec-kit`.

## Resolved Decisions

- Catálogo canônico das flags: `archetype-metadata.xml` é a fonte única; nenhum script injeta flags adicionais.
- JSON contrato: formato livre; todo campo presente é obrigatório por padrão (regra de negócio pode afrouxar); pode conter objetos aninhados ou não.
- Precedência em conflito: o JSON contrato sempre vence sobre a inferência da LLM.
- Integração com `github/spec-kit`: orientação textual nas etapas spec/plan/test instruindo o implementador a usar o Maven Archetype; sem ponto de injeção técnica no template.
- Operador: único mantenedor.
- Destino dos scripts bash após adoção: deletar `bash_script/maven_command_line_archetype_generate.sh` e `bash_script/general_functions.sh`.
- Custo de inferência: aceita gastar tokens na LLM; a economia esperada vem da geração do projeto pelo Maven, não da redução de chamadas à LLM.
- Cache: quando (descrição textual + JSON contrato) se repetem, devolver o mesmo comando gerado sem nova chamada à LLM.

## First-Glance Unknowns

- (nenhum no momento — todas as clarificações foram respondidas na conversa antes da escrita)
