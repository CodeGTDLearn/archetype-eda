# STATUS — SUPERSEDED

**Data:** 2026-11-12
**Estado:** arquivada. Não governa mais nenhuma implementação.

## Motivo

A Opção B (motor de inferência LLM em Groovy — `MavenFlagsViaLlmStarter` e
módulos) foi **removida** do repositório: código, suítes, runner, stub, wiring
do `pom.xml` e documentação ativa.

## O que substitui

A **Opção A** — procedimento fixo empacotado no JAR do archetype:

```text
src/main/resources/utils/
```

- `inicializacao/` — porta de entrada e mapa de leitura;
- `procedimento-do-agente/` — papel, leitura do JAR, regras, execução, entregáveis;
- `regras-de-formacao-do-contrato/` — derivação, tipos, aninhados, precedência,
  materialização e proveniência;
- `templates-de-contrato/` — schema e exemplos.

O agente lê o procedimento de dentro do JAR instalado
(`com.eda-archetype:eda-archetype`); o README deixou de ser fonte de runtime.

## Sobre estes documentos

O conteúdo abaixo é o registro histórico do desenho da Opção B. Ele **não
descreve** o estado atual do projeto. As regras de formação do contrato que
foram implementadas aqui vivem hoje em `utils/regras-de-formacao-do-contrato/`.
