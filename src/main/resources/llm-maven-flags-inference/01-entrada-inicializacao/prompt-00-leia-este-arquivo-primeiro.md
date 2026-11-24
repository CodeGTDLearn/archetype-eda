# Leia este arquivo primeiro

Este é o ponto de entrada do **Procedimento para Especificação** do archetype EDA.

Você está lendo um arquivo que viaja dentro do JAR do archetype:

```text
com.eda-archetype:eda-archetype
```

A partir daqui, o JAR é a fonte da verdade do procedimento. O spec que você recebeu contém apenas as entradas personalizáveis.

## 0. Premissa de entrada — irretratável

Toda inferência começa por exatamente um spec com duas partes, nesta ordem:

1. use-case: texto;
2. contrato: objeto JSON.

O contrato vem sempre depois do use-case.

### 0.1 Marcadores

Identifique as partes pelos marcadores:

- use-case: `USE-CASE` ou `USE CASE`;
- contrato: `CONTRATO` ou `CONTRATO JSON`.

Sem marcador reconhecível, a parte é tratada como ausente.

### 0.2 Gates

Avalie sempre nesta ordem: Gate A, Gate B, Gate C, Gate D.

**Gate A — use-case ausente**

Pare quando o use-case não existir, estiver em branco ou não tiver marcador.

- Não leia nem avalie o contrato.
- Não derive entidade, atributos ou flags.

**Gate B — contrato ausente**

Pare quando o use-case existir e o contrato:

- não existir;
- estiver em branco;
- for `{}`;
- não for um objeto JSON.

Não infira o nome pelo use-case.

Não use default (`MyEntity`).

Não emita `entity_contract_file_path=none`.

**Gate C — ordem violada**

Pare quando o contrato aparecer antes do use-case no spec.

A posição do contrato precisa ser maior que a posição da última ocorrência do marcador de use-case.

**Gate D — destino de geração ausente, inválido ou proibido**

Avalie depois dos Gates A, B e C.

Pare e pergunte — sem derivar e sem executar — quando a pasta de destino:

- não estiver declarada no spec (marcação `PASTA DE DESTINO` ausente);
- for caminho relativo, contiver `~` ou sinalização de fim de arquivo;
- estiver dentro da árvore do archetype.

O destino dentro da árvore do archetype é proibido de forma **absoluta**: não existe confirmação do operador que o autorize. Ao recusar, avise e pergunte onde fica o destino. Se o operador insistir que o destino é a própria pasta do archetype, ou continuar sem informar um destino, finalize o fluxo com o relatório de parada de §0.3.

Nunca presuma destino: não use o diretório de trabalho corrente, a raiz do archetype, nem `src/`, `target/` ou qualquer pasta do próprio archetype. Uma sugestão de pasta temporária, fora da árvore do archetype, só pode ser aplicada após confirmação explícita do operador.

### 0.3 Relatório de parada

Ao parar, informe apenas:

- o gate acionado;
- o que falta;
- o que o operador deve fornecer;
- o destino informado, quando houver, e o motivo da recusa quando o gate for o de destino;
- que nenhuma derivação e nenhum Maven foram executados.

Nenhum gate admite exceção, contorno ou tentativa de prosseguir.

O que um gate **admite** é a correção da entrada: um gate acionado interrompe o fluxo e reporta o que falta, e o operador pode fornecer a entrada que falta e o procedimento recomeça do começo. Isso não é exceção — é o caminho normal. O que nenhum gate admite é **prosseguir** com o defeito presente.

## 1. O que fazer agora

1. Leia `llm-maven-flags-inference/01-entrada-inicializacao/indice-01-mapa-dos-arquivos-e-ordem-de-leitura.md`.
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
- contrato JSON;
- pasta de destino da geração, na marcação `PASTA DE DESTINO`.

Nada disso é regra do archetype. São dados da geração. A pasta de destino pertence ao spec e **nunca** ao contrato.

## 3. Fonte da verdade

Use, nesta ordem:

1. o catálogo de flags dentro do JAR;
2. os arquivos de `llm-maven-flags-inference/`;
3. os templates e o pós-processador dentro do JAR;
4. o spec, apenas para as entradas personalizáveis.

Não use o `README.md` como fonte de runtime.

Não use código de motor, CLI ou script externo para redescobrir semântica.

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
- Não usar o README, nem qualquer motor, CLI ou script externo, como substitutos deste procedimento.

## 6. Condições de parada

Pare e reporte antes de executar quando:

- o JAR instalado não for encontrado;
- um arquivo obrigatório de `llm-maven-flags-inference/` estiver ausente;
- o catálogo consolidado estiver ausente ou ilegível;
- o pós-processador estiver ausente;
- os Gates A, B ou C de §0 forem acionados:
  - Gate A: use-case ausente, em branco ou sem marcador;
  - Gate B: contrato ausente, em branco, `{}` ou não-objeto;
  - Gate C: contrato antes do use-case;
- o package base estiver ausente ou ambíguo;
- a pasta de destino estiver ausente, for inválida ou estiver dentro da árvore do archetype (Gate D);
- uma flag derivada violar a regex e não houver default válido;
- houver conflito que exigiria adivinhar.

## 7. Checklist rápido

- [ ] Localizei o JAR instalado.
- [ ] Li o índice.
- [ ] Li todos os arquivos obrigatórios.
- [ ] Li o catálogo e o pós-processador.
- [ ] Separei flags, meta-chaves e campos de negócio.
- [ ] Derivei entidade, package, atributos e flags.
- [ ] Tratei o `id` do contrato como campo técnico `<entidade>Id`, não como atributo comum.
- [ ] Validei tudo contra as regex do catálogo.
- [ ] Materializei o contrato.
- [ ] Montei a proveniência.
- [ ] Confirmei o destino absoluto, fora da árvore do archetype.
- [ ] Emiti `-DoutputDirectory` com o destino confirmado.
- [ ] Validei o `package` e compilei o módulo.

## 8. Verificação de integridade do JAR

Antes de derivar, confirme que o JAR contém:

```text
llm-maven-flags-inference/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md
llm-maven-flags-inference/01-entrada-inicializacao/indice-01-mapa-dos-arquivos-e-ordem-de-leitura.md
llm-maven-flags-inference/02-operacao-procedimento-do-agente/prompt-01-papel-e-restricoes-do-agente.md
llm-maven-flags-inference/02-operacao-procedimento-do-agente/prompt-02-como-localizar-e-ler-o-archetype.md
llm-maven-flags-inference/02-operacao-procedimento-do-agente/prompt-03-regras-obrigatorias-de-geracao.md
llm-maven-flags-inference/02-operacao-procedimento-do-agente/prompt-04-fluxo-de-execucao-e-verificacao.md
llm-maven-flags-inference/02-operacao-procedimento-do-agente/prompt-05-entregaveis-e-relatorios.md
llm-maven-flags-inference/03-derivacao-regras-de-formacao-do-contrato/regras-01-como-derivar-a-entidade-do-contrato.md
llm-maven-flags-inference/03-derivacao-regras-de-formacao-do-contrato/regras-02-como-resolver-tipos-e-enums.md
llm-maven-flags-inference/03-derivacao-regras-de-formacao-do-contrato/regras-03-como-derivar-flags-de-tipos-aninhados.md
llm-maven-flags-inference/03-derivacao-regras-de-formacao-do-contrato/regras-04-como-aplicar-precedencia-e-validacao.md
llm-maven-flags-inference/03-derivacao-regras-de-formacao-do-contrato/regras-05-como-materializar-e-emitir-o-contrato.md
llm-maven-flags-inference/03-derivacao-regras-de-formacao-do-contrato/regras-06-como-relatar-proveniencia.md
llm-maven-flags-inference/04-referencia-templates-de-contrato/template-01-schema-do-contrato.json
llm-maven-flags-inference/04-referencia-templates-de-contrato/template-02-exemplo-contrato-plano.json
llm-maven-flags-inference/04-referencia-templates-de-contrato/template-03-exemplo-contrato-com-objetos-aninhados.json
llm-maven-flags-inference/04-referencia-templates-de-contrato/template-04-exemplo-contrato-com-enums.json
llm-maven-flags-inference/04-referencia-templates-de-contrato/template-05-exemplo-contrato-com-tipos-do-projeto.json
llm-maven-flags-inference/04-referencia-templates-de-contrato/template-06-exemplo-contrato-com-attributes-explicito.json
META-INF/maven/archetype-metadata.xml
META-INF/archetype-post-generate.groovy
archetype-resources/
```

Se qualquer item obrigatório faltar, pare e reporte o JAR como incompleto.
