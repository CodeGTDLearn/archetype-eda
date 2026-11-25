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

Pare com STOP-IN-01 quando o use-case não existir, estiver em branco ou não tiver marcador.

- Não leia nem avalie o contrato.
- Não derive entidade, atributos ou flags.

**Gate B — contrato ausente**

Pare com STOP-IN-02 quando o use-case existir e o contrato:

- não existir;
- estiver em branco;
- for `{}`;
- não for um objeto JSON.

Não infira o nome pelo use-case.

Não use default (`MyEntity`).

Não emita `entity_contract_file_path=none`.

**Gate C — ordem violada**

Pare com STOP-IN-03 quando o contrato aparecer antes do use-case no spec.

A posição do contrato precisa ser maior que a posição da última ocorrência do marcador de use-case.

**Gate D — destino de geração ausente, inválido ou proibido**

Avalie depois dos Gates A, B e C.

Pare com STOP-IN-04 e pergunte — sem derivar e sem executar — quando a pasta de destino:

- não estiver declarada no spec (marcação `PASTA DE DESTINO` ausente);
- for caminho relativo, contiver `~` ou sinalização de fim de arquivo;
- estiver dentro da árvore do archetype.

O destino dentro da árvore do archetype é proibido de forma **absoluta**: não existe confirmação do operador que o autorize. Ao recusar, avise e pergunte onde fica o destino. Se o operador insistir que o destino é a própria pasta do archetype, ou continuar sem informar um destino, finalize o fluxo com a mensagem de STOP-IN-04 em §0.5.

Nunca presuma destino: não use o diretório de trabalho corrente, a raiz do archetype, nem `src/`, `target/` ou qualquer pasta do próprio archetype. Uma sugestão de pasta temporária, fora da árvore do archetype, só pode ser aplicada após confirmação explícita do operador.

### 0.3 Contrato de Parada Dura

Toda parada dura usa este contrato. A mensagem tem 10 campos e é escrita em português.

| # | Campo | Regra |
|---:|---|---|
| 1 | Código | `STOP-<CATEGORIA>-<NN>`, único, no título |
| 2 | O que aconteceu | Fato objetivo, em uma frase curta |
| 3 | Onde está | Nome legível da seção e caminho, campo ou comando |
| 4 | Por que parou | A regra que exigiu a parada, em linguagem simples |
| 5 | O que não foi executado | Nenhuma derivação, nenhum Maven, nenhum módulo |
| 6 | Opções de correção | 2 ou 3 opções, com exemplo em cada uma |
| 7 | Sugestão | A opção recomendada e o motivo |
| 8 | Como retomar | O que corrigir e de onde recomeçar |
| 9 | O que não fazer | Proibições específicas do caso |
| 10 | Segurança | Não expor segredos, credenciais ou o contrato completo |

Regras de redação:

- Uma ideia por frase.
- Cada frase tem no máximo 20 palavras.
- Use voz ativa: "corrija", "salve", "recomece".
- Traduza a sigla ou o termo técnico na primeira aparição.
- Localize o problema pelo nome legível da seção e pelo caminho.
- Use linguagem neutra e factual; não culpe o operador.
- Dê um exemplo concreto em cada opção.

**Template canônico**

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

A linha `Dicionário` é adicional; não conta como um dos 10 campos.

### 0.4 Dicionário de linguagem simples

Consulte este dicionário em toda mensagem de parada.

| Termo técnico | Tradução em linguagem simples |
|---|---|
| regex | regra de formato (a regra que diz quais valores são aceitos) |
| JAR | arquivo empacotado do archetype |
| Maven | ferramenta que gera o projeto |
| archetype | modelo que gera o projeto |
| plugin | extensão do Maven |
| goal | comando do Maven |
| package | pacote (agrupamento de classes Java) |
| FQN | nome completo da classe, incluindo o pacote |
| POJO | classe simples de dados |
| hash | impressão digital do conteúdo |
| canônico | formato único e fixo |
| idempotente | rodar de novo dá o mesmo resultado |
| proveniência | tabela que mostra de onde cada valor veio |
| materializar | gravar o contrato em um arquivo |
| precedência | ordem de prioridade |
| fixture | projeto de teste |
| suíte | conjunto de testes |
| derivação | cálculo dos valores a partir do contrato |
| default | valor padrão |
| command line | comando do Maven |

### 0.5 Registro de paradas

O registro tem 16 paradas e 2 confirmações. Cada código tem uma mensagem completa abaixo.

| Código | Gatilho | Site principal |
|---|---|---|
| `STOP-IN-01` | use-case ausente, em branco ou sem marcador | Gate A |
| `STOP-IN-02` | contrato ausente, em branco, vazio ou não-objeto | Gate B |
| `STOP-IN-03` | contrato antes do use-case | Gate C |
| `STOP-IN-04` | destino ausente, relativo, com `~`, com fim de arquivo ou dentro do archetype | Gate D |
| `STOP-IN-05` | package base ausente ou ambíguo | §6 |
| `STOP-ENV-01` | JAR instalado não encontrado | prompt-02 §1 |
| `STOP-ENV-02` | arquivo ou árvore obrigatória ausente | índice §5; §8 |
| `STOP-ENV-03` | catálogo ausente ou ilegível | §6; prompt-02 §6 |
| `STOP-ENV-04` | pós-processador ausente | §6; prompt-02 §6 |
| `STOP-ENV-05` | várias versões e o spec não declara qual usar | prompt-02 §1.4 |
| `STOP-DER-01` | nome de entidade ambíguo | regras-01 §2 |
| `STOP-DER-02` | lista de atributos derivada inválida | regras-01 §6; regras-04 §3 |
| `STOP-DER-03` | duas fontes de mesma prioridade divergem | regras-04 §7 |
| `STOP-DER-04` | caminho do contrato não absoluto ou falha ao gravar | regras-05 §5/§7 |
| `STOP-EXEC-01` | falha de catálogo ou pós-processador na geração | prompt-04 §4 |
| `STOP-RULE-01` | o procedimento não cobre um caso | prompt-01 §4 |
| `STOP-CONF-01` | pasta de destino ainda não existe | prompt-04 §1.3 |
| `STOP-CONF-02` | já existe módulo com o mesmo nome | prompt-04 §1.3 |

**STOP-IN-01 — use-case ausente**

```text
---
> ## PARADA DURA — STOP-IN-01
>
> **O que aconteceu:** o spec não tem a seção de use-case, ou ela está em branco, ou falta o marcador.
> **Onde está:** no spec, antes do contrato. A seção esperada é "3. USE-CASE".
> **Por que parou:** o procedimento precisa do use-case para inferir a entidade. Gate A.
> **O que não foi executado:** o contrato não foi lido, nada foi derivado e nenhum comando do Maven foi rodado.
>
> **Opções de correção:**
> 1. Acrescente a seção "3. USE-CASE" com o texto do caso. Exemplo: "O cliente faz um pedido."
> 2. Acrescente o marcador `USE-CASE` à seção que já existe. Exemplo: `USE-CASE: o cliente faz um pedido`.
> 3. Use o template de spec completo. Exemplo: copie as cinco entradas do template.
>
> **Sugestão:** use a opção 1 se você tem o texto; use a opção 3 se quer garantir a estrutura.
> **Como retomar:** salve o spec e recomece pelo Gate A de §0.2.
> **O que não fazer:** não infira o nome pelo contrato; não invente um use-case.
> **Segurança:** não coloque segredos ou credenciais no use-case.
> **Dicionário:** `prompt-00` §0.4.
---
```

**STOP-IN-02 — contrato ausente ou inválido**

```text
---
> ## PARADA DURA — STOP-IN-02
>
> **O que aconteceu:** o spec não tem um contrato JSON válido; ele está ausente, em branco, vazio ou não é um objeto.
> **Onde está:** na seção "4. CONTRATO JSON".
> **Por que parou:** o contrato é a fonte da entidade e dos atributos. Gate B.
> **O que não foi executado:** nada foi derivado; nenhum comando do Maven foi rodado.
>
> **Opções de correção:**
> 1. Acrescente um objeto JSON. Exemplo: `{"entity_name":"Cliente","attributes":{"nome":"Maria"}}`.
> 2. Corrija a sintaxe do objeto existente. Exemplo: feche as chaves e use aspas duplas.
> 3. Use um exemplo do template de contrato. Exemplo: `template-02-exemplo-contrato-plano.json`.
>
> **Sugestão:** use a opção 1 para um contrato mínimo; use a opção 3 para um exemplo completo.
> **Como retomar:** salve o spec e recomece pelo Gate B de §0.2.
> **O que não fazer:** não use `{}`; não use array ou texto no lugar do objeto; não infira o nome.
> **Segurança:** não inclua segredos no contrato.
> **Dicionário:** `prompt-00` §0.4.
---
```

**STOP-IN-03 — contrato antes do use-case**

```text
---
> ## PARADA DURA — STOP-IN-03
>
> **O que aconteceu:** o contrato aparece antes do use-case no spec.
> **Onde está:** nas seções "3. USE-CASE" e "4. CONTRATO JSON".
> **Por que parou:** o procedimento lê o use-case antes do contrato. Gate C.
> **O que não foi executado:** nada foi derivado; nenhum comando do Maven foi rodado.
>
> **Opções de correção:**
> 1. Mova a seção do contrato para depois do use-case. Exemplo: "3. USE-CASE" antes de "4. CONTRATO JSON".
> 2. Mova a seção do use-case para antes do contrato. Exemplo: recorte e cole "3. USE-CASE" no topo.
> 3. Refaga o spec na ordem do template. Exemplo: use as cinco entradas na ordem.
>
> **Sugestão:** use a opção 1; é a menor mudança.
> **Como retomar:** salve o spec e recomece pelo Gate C de §0.2.
> **O que não fazer:** não reordene apenas os marcadores; não prossiga.
> **Segurança:** não mova segredos para o use-case.
> **Dicionário:** `prompt-00` §0.4.
---
```

**STOP-IN-04 — destino inválido**

```text
---
> ## PARADA DURA — STOP-IN-04
>
> **O que aconteceu:** a pasta de destino está ausente, é relativa, tem `~`, tem marca de fim de arquivo ou fica dentro do archetype.
> **Onde está:** na seção "5. PASTA DE DESTINO" do spec.
> **Por que parou:** o procedimento não pode presumir destino. Gate D.
> **O que não foi executado:** nada foi gerado; nenhum comando do Maven foi rodado.
>
> **Opções de correção:**
> 1. Informe um caminho absoluto fora do archetype. Exemplo: `C:/Temp/eda-sandbox`.
> 2. Escolha outra pasta fora do archetype. Exemplo: `D:/projetos/eda`.
> 3. Corrija o caminho inválido. Exemplo: remova `~` e use barras normais.
>
> **Sugestão:** use a opção 1; um caminho absoluto simples evita ambiguidade.
> **Como retomar:** salve o spec e recomece pelo Gate D de §0.2.
> **O que não fazer:** não use a pasta do archetype; não use `src/` nem `target/`; não presuma o diretório atual.
> **Segurança:** não exponha caminhos de usuário em relatórios.
> **Dicionário:** `prompt-00` §0.4.
---
```

**STOP-IN-05 — package base ausente ou ambíguo**

```text
---
> ## PARADA DURA — STOP-IN-05
>
> **O que aconteceu:** o package base está ausente ou há mais de um candidato.
> **Onde está:** na seção "2. PACKAGE BASE" do spec.
> **Por que parou:** o package base é obrigatório e não vem do contrato.
> **O que não foi executado:** nada foi derivado; nenhum comando do Maven foi rodado.
>
> **Opções de correção:**
> 1. Declare um package base. Exemplo: `org.clinica`.
> 2. Remova o nome da entidade do package. Exemplo: troque `org.clinica.cliente` por `org.clinica`.
> 3. Escolha um dos candidatos. Exemplo: use `org.clinica` e descarte `org.loja`.
>
> **Sugestão:** use a opção 1; o package base deve ser único e sem o nome da entidade.
> **Como retomar:** salve o spec e recomece pela leitura da entrada.
> **O que não fazer:** não use `org.clinica.cliente` como base; não deixe dois candidatos.
> **Segurança:** não inclua dados sensíveis no package.
> **Dicionário:** `prompt-00` §0.4.
---
```

**STOP-ENV-01 — JAR não encontrado**

```text
---
> ## PARADA DURA — STOP-ENV-01
>
> **O que aconteceu:** o JAR do archetype não foi encontrado no repositório local.
> **Onde está:** em `~/.m2/repository/com/eda-archetype/eda-archetype/`.
> **Por que parou:** o procedimento precisa do JAR instalado para ler as regras.
> **O que não foi executado:** nada foi derivado; nenhum comando do Maven foi rodado.
>
> **Opções de correção:**
> 1. Instale o JAR. Exemplo: rode `mvn clean install` na raiz do archetype.
> 2. Confira a versão declarada no spec. Exemplo: use a versão que existe em `.m2`.
> 3. Aponte para o caminho correto. Exemplo: copie o JAR para a pasta esperada.
>
> **Sugestão:** use a opção 1; o `clean install` recria o artefato.
> **Como retomar:** volte ao início e leia o JAR instalado.
> **O que não fazer:** não edite o JAR à mão; não use a árvore-fonte como substituta.
> **Segurança:** não baixe JARs de fontes não confiáveis.
> **Dicionário:** `prompt-00` §0.4.
---
```

**STOP-ENV-02 — arquivo ou árvore obrigatória ausente**

```text
---
> ## PARADA DURA — STOP-ENV-02
>
> **O que aconteceu:** um arquivo obrigatório do procedimento ou a árvore `archetype-resources/` está ausente no JAR.
> **Onde está:** no JAR, em `llm-maven-flags-inference/` ou em `archetype-resources/`.
> **Por que parou:** o JAR está incompleto; o procedimento não pode continuar.
> **O que não foi executado:** nada foi derivado; nenhum comando do Maven foi rodado.
>
> **Opções de correção:**
> 1. Reinstale o JAR. Exemplo: rode `mvn clean install` na raiz.
> 2. Confira os 19 arquivos. Exemplo: use o índice para listar os arquivos.
> 3. Use um JAR completo. Exemplo: copie o artefato de uma build verde.
>
> **Sugestão:** use a opção 1; a reinstalação recoloca os arquivos.
> **Como retomar:** volte ao início e confira a lista de integridade de §8.
> **O que não fazer:** não continue com um JAR parcial; não crie os arquivos à mão.
> **Segurança:** não altere o JAR instalado.
> **Dicionário:** `prompt-00` §0.4.
---
```

**STOP-ENV-03 — catálogo ausente ou ilegível**

```text
---
> ## PARADA DURA — STOP-ENV-03
>
> **O que aconteceu:** o catálogo de flags está ausente ou não pode ser lido.
> **Onde está:** em `META-INF/maven/archetype-metadata.xml`.
> **Por que parou:** o catálogo é a autoridade sobre defaults e regex.
> **O que não foi executado:** nada foi derivado; nenhum comando do Maven foi rodado.
>
> **Opções de correção:**
> 1. Reinstale o JAR. Exemplo: rode `mvn clean install`.
> 2. Reconstrua o catálogo. Exemplo: rode `mvn generate-resources`.
> 3. Confira 35 propriedades e 18 filesets. Exemplo: conte os fragmentos.
>
> **Sugestão:** use a opção 1; o JAR completo traz o catálogo.
> **Como retomar:** volte ao início e leia o catálogo.
> **O que não fazer:** não invente flags, defaults ou regex; não use o README como catálogo.
> **Segurança:** não altere o catálogo à mão.
> **Dicionário:** `prompt-00` §0.4.
---
```

**STOP-ENV-04 — pós-processador ausente**

```text
---
> ## PARADA DURA — STOP-ENV-04
>
> **O que aconteceu:** o pós-processador não foi encontrado no JAR.
> **Onde está:** em `META-INF/archetype-post-generate.groovy`.
> **Por que parou:** o pós-processador copia o contrato e gera enums e POJOs.
> **O que não foi executado:** nada foi derivado; nenhum comando do Maven foi rodado.
>
> **Opções de correção:**
> 1. Reinstale o JAR. Exemplo: rode `mvn clean install`.
> 2. Reconstrua o pós-processador. Exemplo: rode `mvn process-resources`.
> 3. Use um JAR completo. Exemplo: copie o artefato de uma build verde.
>
> **Sugestão:** use a opção 1; a reinstalação recoloca o arquivo.
> **Como retomar:** volte ao início e confira a lista de integridade.
> **O que não fazer:** não gere sem o pós-processador; não edite o JAR.
> **Segurança:** não altere o pós-processador instalado.
> **Dicionário:** `prompt-00` §0.4.
---
```

**STOP-ENV-05 — várias versões instaladas**

```text
---
> ## PARADA DURA — STOP-ENV-05
>
> **O que aconteceu:** há várias versões do archetype instaladas e o spec não declara qual usar.
> **Onde está:** em `~/.m2/repository/com/eda-archetype/eda-archetype/`.
> **Por que parou:** o procedimento não pode escolher a versão por conta própria.
> **O que não foi executado:** nada foi derivado; nenhum comando do Maven foi rodado.
>
> **Opções de correção:**
> 1. Declare a versão no spec. Exemplo: `archetypeVersion=0.0.1-SNAPSHOT`.
> 2. Escolha uma versão instalada. Exemplo: use a pasta `0.0.1-SNAPSHOT`.
> 3. Remova versões antigas. Exemplo: apague as pastas que não usa.
>
> **Sugestão:** use a opção 1; o spec fica reproduzível.
> **Como retomar:** salve o spec e recomece pela leitura da versão.
> **O que não fazer:** não pegue a versão mais nova em silêncio; não use o pom do projeto hospedeiro.
> **Segurança:** não apague versões sem confirmação.
> **Dicionário:** `prompt-00` §0.4.
---
```

**STOP-DER-01 — nome de entidade ambíguo**

```text
---
> ## PARADA DURA — STOP-DER-01
>
> **O que aconteceu:** o use-case ou o contrato permite mais de um nome de entidade.
> **Onde está:** no use-case ou no contrato, no campo do nome da entidade.
> **Por que parou:** o procedimento não pode escolher a entidade principal no escuro.
> **O que não foi executado:** nada foi derivado; nenhum comando do Maven foi rodado.
>
> **Opções de correção:**
> 1. Declare `entity_name` no contrato. Exemplo: `"entity_name":"Cliente"`.
> 2. Nomeie a entidade no use-case. Exemplo: "A entidade principal é Cliente."
> 3. Escolha um dos candidatos. Exemplo: use `Cliente` e descarte `Pedido`.
>
> **Sugestão:** use a opção 1; o contrato vence a inferência.
> **Como retomar:** salve o spec e recomece pela derivação do nome.
> **O que não fazer:** não use `MyEntity` em silêncio; não adivinhe.
> **Segurança:** não inclua dados pessoais no nome.
> **Dicionário:** `prompt-00` §0.4.
---
```

**STOP-DER-02 — lista de atributos inválida**

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

**STOP-DER-03 — conflito de mesma prioridade**

```text
---
> ## PARADA DURA — STOP-DER-03
>
> **O que aconteceu:** duas fontes de mesma prioridade deram valores diferentes para a mesma flag.
> **Onde está:** nas fontes listadas na mensagem e nas flags afetadas.
> **Por que parou:** o procedimento não pode escolher uma fonte no escuro.
> **O que não foi executado:** nada foi derivado; nenhum comando do Maven foi rodado.
>
> **Opções de correção:**
> 1. Escolha a fonte A. Exemplo: use o valor do contrato.
> 2. Escolha a fonte B. Exemplo: use o valor da derivação.
> 3. Declare o valor no contrato. Exemplo: `"entity_package_name":"cliente"`.
>
> **Sugestão:** use a opção 3; o valor explícito vence.
> **Como retomar:** salve o spec e recomece pela precedência.
> **O que não fazer:** não adivinhe; não misture as fontes.
> **Segurança:** não mostre segredos das fontes.
> **Dicionário:** `prompt-00` §0.4.
---
```

**STOP-DER-04 — caminho do contrato inválido ou falha ao gravar**

```text
---
> ## PARADA DURA — STOP-DER-04
>
> **O que aconteceu:** o caminho do contrato não é absoluto ou o arquivo não pôde ser gravado.
> **Onde está:** em `entity_contract_file_path` ou no diretório de contratos.
> **Por que parou:** o pós-processador precisa de um arquivo absoluto e legível.
> **O que não foi executado:** nada foi gerado; nenhum comando do Maven foi rodado.
>
> **Opções de correção:**
> 1. Informe um diretório absoluto. Exemplo: `C:/Temp/eda-contratos`.
> 2. Corrija a permissão. Exemplo: dê escrita ao seu usuário.
> 3. Use o diretório padrão de contratos. Exemplo: `%LOCALAPPDATA%\eda-archetype\contracts`.
>
> **Sugestão:** use a opção 3; o diretório padrão já é gravável.
> **Como retomar:** salve o contrato e recomece pela materialização.
> **O que não fazer:** não use `none`; não use caminho relativo; não grave no archetype.
> **Segurança:** não grave o contrato em pasta pública.
> **Dicionário:** `prompt-00` §0.4.
---
```

**STOP-EXEC-01 — falha de catálogo ou pós-processador**

```text
---
> ## PARADA DURA — STOP-EXEC-01
>
> **O que aconteceu:** o catálogo ou o pós-processador falhou durante a geração.
> **Onde está:** na saída do Maven e no componente do JAR que falhou.
> **Por que parou:** a geração não pode continuar com um componente quebrado.
> **O que não foi executado:** o módulo não foi concluído; o relatório final não foi emitido.
>
> **Opções de correção:**
> 1. Reinstale o JAR e repita. Exemplo: `mvn clean install` e gere de novo.
> 2. Corrija o catálogo ou o pós-processador. Exemplo: restaure o fragmento XML.
> 3. Reporte o comando e a saída. Exemplo: anexe o trecho do erro.
>
> **Sugestão:** use a opção 1; a reinstalação resolve a maioria dos casos.
> **Como retomar:** repita a geração do zero.
> **O que não fazer:** não edite o código gerado; não faça patch no JAR.
> **Segurança:** não anexe segredos à saída.
> **Dicionário:** `prompt-00` §0.4.
---
```

**STOP-RULE-01 — caso não coberto**

```text
---
> ## PARADA DURA — STOP-RULE-01
>
> **O que aconteceu:** o procedimento não cobre o caso que o contrato ou o use-case exige.
> **Onde está:** no trecho do contrato ou do use-case que precisa da regra.
> **Por que parou:** o procedimento não pode improvisar.
> **O que não foi executado:** nada foi derivado; nenhum comando do Maven foi rodado.
>
> **Opções de correção:**
> 1. Acrescente a regra ao procedimento. Exemplo: documente o caso e a decisão.
> 2. Consulte o mantenedor. Exemplo: envie o trecho e a dúvida.
> 3. Use uma regra existente. Exemplo: aplique a regra de tipo mais próxima.
>
> **Sugestão:** use a opção 2; a regra nova precisa de decisão do mantenedor.
> **Como retomar:** atualize o procedimento e recomece.
> **O que não fazer:** não improvise; não invente uma flag.
> **Segurança:** não inclua dados sensíveis no relato.
> **Dicionário:** `prompt-00` §0.4.
---
```

**STOP-CONF-01 — pasta de destino inexistente**

```text
---
> ## PARADA DURA — STOP-CONF-01
>
> **O que aconteceu:** a pasta de destino ainda não existe.
> **Onde está:** no caminho absoluto da seção "5. PASTA DE DESTINO".
> **Por que parou:** o procedimento precisa da sua confirmação para criar a pasta.
> **O que não foi executado:** nada foi gerado até você confirmar.
>
> **Opções de correção:**
> 1. Criar a pasta. Exemplo: crie `C:/Temp/eda-sandbox`.
> 2. Escolher outra pasta. Exemplo: use `D:/projetos/eda`.
> 3. Cancelar. Exemplo: responda "cancelar" e o fluxo termina.
>
> **Sugestão:** use a opção 1; a pasta é criada fora do archetype.
> **Como retomar:** confirme a criação e o procedimento continua.
> **O que não fazer:** não crie dentro do archetype; não presuma a pasta.
> **Segurança:** não crie a pasta em local público.
> **Dicionário:** `prompt-00` §0.4.
---
```

**STOP-CONF-02 — módulo com o mesmo nome**

```text
---
> ## PARADA DURA — STOP-CONF-02
>
> **O que aconteceu:** já existe um módulo com o mesmo nome do artefato no destino.
> **Onde está:** em `<destino>/<artifactId>`.
> **Por que parou:** o procedimento não pode apagar ou sobrescrever sem sua confirmação.
> **O que não foi executado:** nada foi gerado ou apagado até você confirmar.
>
> **Opções de correção:**
> 1. Sobrescrever o módulo. Exemplo: confirme "sobrescrever".
> 2. Manter o existente e encerrar. Exemplo: confirme "manter".
> 3. Escolher outro destino. Exemplo: use `C:/Temp/eda-sandbox-2`.
>
> **Sugestão:** use a opção 3; evita perder o módulo existente.
> **Como retomar:** confirme a escolha e o procedimento continua.
> **O que não fazer:** não apague o módulo sem confirmação; não use o archetype como destino.
> **Segurança:** não sobrescreva dados sem backup.
> **Dicionário:** `prompt-00` §0.4.
---
```

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

Interrompa antes de executar quando:

- `STOP-ENV-01`: o JAR instalado não for encontrado.
- `STOP-ENV-02`: um arquivo obrigatório de `llm-maven-flags-inference/` ou a árvore `archetype-resources/` estiver ausente.
- `STOP-ENV-03`: o catálogo consolidado estiver ausente ou ilegível.
- `STOP-ENV-04`: o pós-processador estiver ausente.
- `STOP-IN-01`: o use-case estiver ausente, em branco ou sem marcador (Gate A).
- `STOP-IN-02`: o contrato estiver ausente, em branco, `{}` ou não-objeto (Gate B).
- `STOP-IN-03`: o contrato vier antes do use-case (Gate C).
- `STOP-IN-05`: o package base estiver ausente ou ambíguo.
- `STOP-IN-04`: a pasta de destino estiver ausente, for inválida ou estiver dentro da árvore do archetype (Gate D).
- `STOP-RULE-01`: uma flag derivada violar a regex e não houver default válido.
- `STOP-DER-03`: houver conflito que exigiria adivinhar.

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

Se qualquer item obrigatório faltar, pare com STOP-ENV-02 e reporte o JAR como incompleto.
