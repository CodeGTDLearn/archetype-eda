# Leia este arquivo primeiro

Este é o ponto de entrada do procedimento fixo da **Opção A** do archetype EDA.

Você está lendo um arquivo que viaja dentro do JAR do archetype:

```text
com.eda-archetype:eda-archetype
```

A partir daqui, o JAR é a fonte da verdade do procedimento. O spec que você recebeu contém apenas as entradas personalizáveis.

## 1. O que fazer agora

1. Leia `utils/inicializacao/indice-01-mapa-dos-arquivos-e-ordem-de-leitura.md`.
2. Siga a ordem indicada no índice.
3. Leia todos os arquivos marcados como obrigatórios antes de derivar ou executar.
4. Só então derive a entidade, os atributos e as flags.
5. Mostre a proveniência antes de executar.
6. Execute o Maven, valide o resultado e entregue o relatório.

## 2. Entradas externas permitidas

O spec é o único arquivo externo que você deve consumir. Ele contém:

- tipo de módulo (`RAIZ` ou `SECUNDARIO`);
- package base;
- use-case;
- contrato JSON.

Nada disso é regra do archetype. São dados da geração.

## 3. Fonte da verdade

Use, nesta ordem:

1. o catálogo de flags dentro do JAR;
2. os arquivos de `utils/`;
3. os templates e o pós-processador dentro do JAR;
4. o spec, apenas para as entradas personalizáveis.

Não use o `README.md` como fonte de runtime.

Não use o código do motor `src/main/pre-processamento-maven-flags-via-llm/` para redescobrir semântica.

## 4. Saídas obrigatórias

- Tabela de proveniência por flag.
- Command line do `mvn archetype:generate`.
- Execução do comando.
- Validação do `package` do arquivo gerado.
- Verificação do contrato copiado, quando aplicável.
- Compilação do módulo gerado.
- Relatório final com classificação, tokens e economia.

## 5. Proibições

- Não escrever o módulo à mão.
- Não editar código gerado, inclusive o `package`.
- Não inventar flags, defaults ou regex.
- Não gerar se faltar um arquivo obrigatório.
- Não expor segredos do contrato.
- Não usar o README ou o motor como substitutos deste procedimento.

## 6. Condições de parada

Pare e reporte antes de executar quando:

- o JAR instalado não for encontrado;
- um arquivo obrigatório de `utils/` estiver ausente;
- o catálogo consolidado estiver ausente ou ilegível;
- o pós-processador estiver ausente;
- o contrato não for um objeto JSON;
- o use-case estiver ausente;
- o package base estiver ausente ou ambíguo;
- uma flag derivada violar a regex e não houver default válido;
- houver conflito que exigiria adivinhar.

## 7. Checklist rápido

- [ ] Localizei o JAR instalado.
- [ ] Li o índice.
- [ ] Li todos os arquivos obrigatórios.
- [ ] Li o catálogo e o pós-processador.
- [ ] Separei flags, meta-chaves e campos de negócio.
- [ ] Derivei entidade, package, atributos e flags.
- [ ] Validei tudo contra as regex do catálogo.
- [ ] Materializei o contrato.
- [ ] Montei a proveniência.
- [ ] Executei fora do projeto pai.
- [ ] Validei o `package` e compilei o módulo.

## 8. Verificação de integridade do JAR

Antes de derivar, confirme que o JAR contém:

```text
utils/inicializacao/prompt-00-leia-este-arquivo-primeiro.md
utils/inicializacao/indice-01-mapa-dos-arquivos-e-ordem-de-leitura.md
utils/procedimento-do-agente/prompt-01-papel-e-restricoes-do-agente.md
utils/procedimento-do-agente/prompt-02-como-localizar-e-ler-o-archetype.md
utils/procedimento-do-agente/prompt-03-regras-obrigatorias-de-geracao.md
utils/procedimento-do-agente/prompt-04-fluxo-de-execucao-e-verificacao.md
utils/procedimento-do-agente/prompt-05-entregaveis-e-relatorios.md
utils/regras-de-formacao-do-contrato/regras-01-como-derivar-a-entidade-do-contrato.md
utils/regras-de-formacao-do-contrato/regras-02-como-resolver-tipos-e-enums.md
utils/regras-de-formacao-do-contrato/regras-03-como-derivar-flags-de-tipos-aninhados.md
utils/regras-de-formacao-do-contrato/regras-04-como-aplicar-precedencia-e-validacao.md
utils/regras-de-formacao-do-contrato/regras-05-como-materializar-e-emitir-o-contrato.md
utils/regras-de-formacao-do-contrato/regras-06-como-relatar-proveniencia.md
utils/templates-de-contrato/template-01-schema-do-contrato.json
utils/templates-de-contrato/template-02-exemplo-contrato-plano.json
utils/templates-de-contrato/template-03-exemplo-contrato-com-objetos-aninhados.json
utils/templates-de-contrato/template-04-exemplo-contrato-com-enums.json
utils/templates-de-contrato/template-05-exemplo-contrato-com-tipos-do-projeto.json
utils/templates-de-contrato/template-06-exemplo-contrato-com-attributes-explicito.json
META-INF/maven/archetype-metadata.xml
META-INF/archetype-post-generate.groovy
archetype-resources/
```

Se qualquer item obrigatório faltar, pare e reporte o JAR como incompleto.
