# Feature Specification: Paradas Duras Claras, Completas e em Linguagem Simples

**Feature Branch**: `007-hard-stop-contract`

**Created**: 2026-11-24

**Status**: Draft

**Input**: User description: "O procedimento empacotado do archetype (llm-maven-flags-inference) sabe decidir quando parar, mas não orienta o operador depois da parada. Toda parada dura deve permitir responder o que aconteceu, onde está o problema, quais são as opções, qual é a recomendada e o que fazer para retomar. Uma lista de atributos inválida não pode gerar módulo. Uma parada nova sem código e sem registro não pode passar no build. O catálogo e o procedimento não podem divergir em silêncio."

## Contexto e Problema

- O procedimento empacotado do archetype (`llm-maven-flags-inference`) sabe decidir quando parar, mas não orienta o operador depois da parada.
- Hoje existem 16 pontos de parada dura e 2 confirmações; nenhum atende integralmente a um padrão de mensagem completa.
- Doze são one-liners do tipo "pare e reporte"; quatro (Gates A–D) têm orientação parcial; nenhum tem código, formato destacado, opções de correção e sugestão recomendada.
- O caso mais grave é a lista de atributos derivada do contrato. Se ela viola a regra de formato do catálogo, o procedimento cai no valor padrão `String:name` com um aviso. O aviso não corrige o resultado: o módulo nasce com um campo `name` que não corresponde ao contrato, e o operador pode seguir com um módulo plausível e errado.
- Há divergências documentais que confundem o operador:
  - o catálogo descreve o caminho do contrato como "absoluto ou relativo", mas o procedimento exige absoluto;
  - a regra geral de proveniência permite omitir defaults, enquanto uma regra específica manda registrar `none`;
  - a tabela de flags críticas tem pipes não escapados que quebram a renderização;
  - a referência "FR-006" não é explicada no procedimento empacotado;
  - o nome do pacote da entidade e o nome do artefato têm formatos diferentes, e isso não está explícito.
- O operador-alvo pode ter começado a trabalhar no sistema ontem. A mensagem de parada precisa ser compreensível sem conhecimento prévio de Maven, archetype, JAR, regex ou pacotes Java.
- Observação registrada sobre o README: o tópico relacionado ao `llm-maven-flags-inference` está formatado de forma trivial, e há explicações espalhadas sobre a atuação e a operação do procedimento. Por decisão do operador, a reestruturação do README permanece fora do escopo desta feature.

## Quem é Afetado

- Operador iniciante que escreve a spec e recebe uma parada dura.
- Operador experiente que precisa corrigir a entrada rapidamente.
- Agente que executa o procedimento e precisa saber exatamente o que reportar.
- Mantenedor que altera o procedimento e precisa de um contrato testável.

## Clarifications

### Session 2026-11-24

- Q: A R-11 deve exigir explicitamente uma ideia por frase, tradução de siglas, localização pelo nome legível da seção e linguagem sem culpa? → A: Sim, as quatro regras.
- Q: O dicionário deve apresentar a tradução de cada um dos 20 termos, e não apenas a lista? → A: Sim.
- Q: O spec deve incluir o template canônico da mensagem de parada e um exemplo completo de STOP-DER-02? → A: Sim.
- Q: As confirmações STOP-CONF-01 e STOP-CONF-02 devem ter opções explícitas em requisito funcional? → A: Sim.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Orientação completa em qualquer parada dura (Priority: P1)

Como operador que começou ontem, quando o procedimento para, quero receber uma mensagem clara com causa, local, opções, sugestão e próximo passo, para corrigir a entrada sem precisar adivinhar.

**Why this priority**: é o objetivo central e cobre todos os pontos de parada.

**Independent Test**: provocar cada uma das 16 paradas e verificar se a mensagem contém os 10 campos obrigatórios, pelo menos duas opções e uma sugestão.

**Acceptance Scenarios**:

1. **Dado** um spec sem use-case, **Quando** o agente avalia a entrada, **Então** ele para com o código `STOP-IN-01` e a mensagem informa o que faltou, onde está a seção, duas opções de correção, a sugestão e como retomar; nenhum comando do Maven é executado.
2. **Dado** um contrato que não é objeto JSON, **Quando** o agente avalia a entrada, **Então** ele para com `STOP-IN-02` e a mensagem mostra um exemplo de contrato válido.
3. **Dado** um contrato antes do use-case, **Quando** o agente avalia a ordem, **Então** ele para com `STOP-IN-03` e a mensagem explica como reordenar.
4. **Dado** um destino dentro da árvore do archetype, **Quando** o agente valida o destino, **Então** ele para com `STOP-IN-04` e a mensagem oferece um caminho absoluto válido como exemplo.

---

### User Story 2 - Lista de atributos inválida não gera módulo (Priority: P1)

Como operador, quando a lista de atributos derivada do contrato não respeita o formato aceito, quero que o procedimento pare antes de montar o comando, para não gerar um módulo com um campo errado.

**Why this priority**: hoje o fallback `String:name` produz um resultado plausível e incorreto.

**Independent Test**: usar um contrato com um atributo de nome ou tipo inválido e verificar que nenhum módulo é gerado e que a mensagem `STOP-DER-02` oferece três opções.

**Acceptance Scenarios**:

1. **Dado** um contrato cuja lista derivada viola a regra de formato, **Quando** o agente deriva os atributos, **Então** ele para com `STOP-DER-02` e não emite o comando do Maven.
2. **Dada** a parada `STOP-DER-02`, **Quando** a mensagem é lida, **Então** ela mostra o segmento inválido, o formato esperado `Tipo:nome`, três opções de correção e a sugestão.
3. **Dado** que o operador corrige o contrato, **Quando** o procedimento recomeça, **Então** a lista é derivada novamente e o fluxo segue.
4. **Dado** o valor padrão `String:name` no catálogo, **Quando** o procedimento encontra a lista inválida, **Então** ele não usa esse valor como fallback.

---

### User Story 3 - Nenhuma parada muda passa no build (Priority: P2)

Como mantenedor, quando alguém adiciona uma parada dura sem código e sem registro, quero que o build falhe, para impedir que o operador fique sem orientação.

**Why this priority**: protege a regra contra regressão futura.

**Independent Test**: adicionar uma parada sem código em um arquivo do procedimento e verificar que a suíte de contrato falha.

**Acceptance Scenarios**:

1. **Dada** uma parada com código `STOP-*` não registrado, **Quando** o build roda, **Então** a suíte falha e nomeia o código ausente.
2. **Dado** um site de parada sem código, **Quando** o build roda, **Então** a suíte falha e nomeia o arquivo.
3. **Dado** o registro completo, **Quando** o build roda, **Então** a suíte passa.

---

### User Story 4 - Catálogo e procedimento sem divergência silenciosa (Priority: P2)

Como mantenedor, quando o catálogo muda um default ou uma regra de formato, quero que o build mostre a divergência, para que a tabela-resumo do procedimento não fique desatualizada.

**Why this priority**: a tabela é uma cópia manual e hoje nenhum teste compara os valores.

**Independent Test**: alterar um default no catálogo e verificar que a suíte de sincronia falha.

**Acceptance Scenarios**:

1. **Dado** que o default de uma das 9 flags críticas muda no catálogo, **Quando** o build roda, **Então** a suíte falha e nomeia a flag.
2. **Dado** que a regra de formato literal de uma das flags muda, **Quando** o build roda, **Então** a suíte falha e nomeia a flag.
3. **Dado** o estado atual, **Quando** o build roda, **Então** a suíte passa.

---

### User Story 5 - Documentação do procedimento legível e correta (Priority: P3)

Como leitor do procedimento, quero que a tabela de flags críticas renderize corretamente e que os termos históricos estejam explicados, para não interpretar mal a norma.

**Why this priority**: melhora a leitura, sem mudar o comportamento.

**Independent Test**: renderizar a tabela e conferir três colunas; ler a nota FR-006 e entender a regra sem consultar outro documento.

**Acceptance Scenarios**:

1. **Dada** a tabela de flags críticas, **Quando** ela é renderizada, **Então** aparecem exatamente três colunas.
2. **Dada** a nota de rejeição, **Quando** ela é lida, **Então** `FR-006` está explicado em uma frase simples.
3. **Dado** o texto sobre nome de pacote e nome de artefato, **Quando** ele é lido, **Então** a diferença entre `pedidoitem` e `pedido-item` está explícita.

---

### Edge Cases

- Gate A, B, C e D avaliados fora de ordem: a ordem A → B → C → D deve ser preservada.
- Destino dentro da árvore do archetype: proibição absoluta, sem confirmação possível.
- Destino relativo, com `~` ou com sinalização de fim de arquivo: parada com exemplo de caminho absoluto.
- Várias versões do archetype instaladas: parada com opções para declarar a versão, escolher uma ou remover antigas.
- JAR ausente, catálogo ausente, pós-processador ausente ou arquivo obrigatório ausente: parada com instruções de reconstrução e reinstalação.
- Nome de entidade ambíguo: parada com os candidatos e opções.
- Conflito de mesma prioridade: parada com as duas fontes, as flags afetadas e opções.
- Falha ao gravar o contrato ou caminho não absoluto: parada com opções de diretório e permissão.
- `STOP-CONF-01` — pasta de destino inexistente: as opções criar a pasta, escolher outra pasta ou cancelar MUST seguir a FR-031.
- `STOP-CONF-02` — módulo com o mesmo nome já existente: as opções sobrescrever o módulo, manter o existente e encerrar ou escolher outro destino MUST seguir a FR-032.
- Contrato com chave que parece segredo: aviso antes de gravar, sem imprimir o valor.
- Tabela Markdown com pipe dentro de código: o pipe deve ser escapado.
- Termo em backticks que parece flag: deve ser uma flag real do catálogo ou não pode casar o padrão.
- Caminho em backticks: deve existir no layout do JAR.
- Parada nova sem código ou sem registro: o build deve falhar.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O procedimento MUST definir um Contrato de Parada Dura com 10 campos obrigatórios: código, o que aconteceu, onde está, por que parou, o que não foi executado, opções de correção, sugestão, como retomar, o que não fazer e segurança.
- **FR-002**: Toda parada dura MUST ter um código único no formato `STOP-<CATEGORIA>-<NN>` e uma linha correspondente no registro.
- **FR-003**: Toda mensagem de parada MUST ser destacada com o título "PARADA DURA", o código, delimitadores e rótulos em negrito.
- **FR-004**: Toda mensagem MUST informar a causa, o local exato, a regra que exigiu a parada, o que não foi executado, pelo menos duas opções de correção, uma sugestão recomendada, como retomar e o que não fazer.
- **FR-005**: Toda mensagem MUST usar linguagem simples, com frases de até 20 palavras, voz ativa, termos técnicos explicados na primeira aparição e um exemplo concreto em cada opção.
- **FR-006**: O procedimento MUST conter um dicionário de tradução com, no mínimo, 20 termos técnicos, incluindo regex, JAR, Maven, archetype, plugin, goal, package, FQN, POJO, hash, canônico, idempotente, proveniência, materializar, precedência, fixture, suíte, derivação, default e command line.
- **FR-007**: O dicionário MUST estar acessível no ponto de entrada do procedimento e MUST ser referenciado por todas as paradas.
- **FR-008**: A lista de atributos derivada que viola a regra de formato MUST causar parada dura com o código `STOP-DER-02`.
- **FR-009**: Na parada `STOP-DER-02`, o procedimento MUST NOT usar `String:name` como fallback e MUST NOT emitir o comando do Maven.
- **FR-010**: A parada `STOP-DER-02` MUST mostrar o segmento inválido, o formato esperado e três opções: corrigir o campo, remover o campo ou declarar a lista completa no contrato.
- **FR-011**: As regras de derivação, a regra de precedência, o checklist de execução e o relatório de proveniência MUST refletir a parada `STOP-DER-02`.
- **FR-012**: As 15 outras paradas e as 2 confirmações MUST seguir o mesmo contrato de mensagem.
- **FR-013**: A descrição do catálogo sobre o caminho do contrato MUST dizer que o procedimento exige caminho absoluto, mantendo a regra de formato do catálogo permissiva por projeto.
- **FR-014**: A regra geral de proveniência MUST ter uma exceção explícita para defaults que uma regra específica mande registrar, como `contract_nested_type_definitions` = `none`.
- **FR-015**: A tabela de flags críticas MUST ter os pipes internos escapados e renderizar exatamente três colunas.
- **FR-016**: A referência `FR-006` MUST ser explicada em linguagem simples no próprio procedimento.
- **FR-017**: A diferença entre o nome do pacote da entidade, sem separadores, e o nome do artefato, em kebab-case, MUST estar explícita.
- **FR-018**: A tabela de flags críticas MUST ser verificada contra o catálogo para os 9 defaults e para as regras de formato literais.
- **FR-019**: O build MUST falhar se uma parada não tiver código, se o código não estiver registrado ou se um site de parada não citar um código.
- **FR-020**: O build MUST falhar se o dicionário não contiver os 20 termos obrigatórios.
- **FR-021**: O procedimento MUST NOT conter a expressão "informe apenas".
- **FR-022**: Nenhum arquivo novo MUST ser adicionado ao procedimento; a contagem MUST permanecer em 19 arquivos, 13 Markdown e 6 JSON.
- **FR-023**: Nenhum default ou regra de formato do catálogo MUST ser alterado; a contagem MUST permanecer em 35 propriedades e 18 filesets.
- **FR-024**: O comportamento existente MUST ser preservado, exceto pela nova parada `STOP-DER-02`.
- **FR-025**: As mensagens MUST estar em português e MUST NOT expor segredos, credenciais ou o contrato completo.
- **FR-026**: A mensagem MUST apresentar uma ideia por frase.
- **FR-027**: A mensagem MUST traduzir toda sigla e todo termo técnico na primeira aparição, entre parênteses ou em uma frase curta.
- **FR-028**: A mensagem MUST localizar o problema pelo nome legível da seção e pelo caminho, não apenas pelo número da seção.
- **FR-029**: A mensagem MUST usar linguagem neutra e factual, sem culpar o operador.
- **FR-030**: O dicionário MUST apresentar, para cada um dos 20 termos, a tradução em linguagem simples.
- **FR-031**: `STOP-CONF-01` MUST oferecer as opções: criar a pasta, escolher outra pasta ou cancelar.
- **FR-032**: `STOP-CONF-02` MUST oferecer as opções: sobrescrever o módulo, manter o existente e encerrar ou escolher outro destino.
- **FR-033**: O spec MUST incluir o template canônico da mensagem de parada e um exemplo completo de `STOP-DER-02`.

### Non-Functional Requirements

- **NFR-001**: O tempo de build não pode aumentar de forma perceptível.
- **NFR-002**: Os testes devem ser determinísticos e não depender de rede.
- **NFR-003**: Nenhuma dependência nova pode ser adicionada.
- **NFR-004**: As mensagens devem ser legíveis em Markdown simples, sem depender de renderizadores específicos.
- **NFR-005**: O procedimento deve continuar funcionando a partir do JAR instalado, sem depender da árvore-fonte.

### Registro de Paradas

O registro canônico de códigos e gatilhos fica no ponto de entrada do procedimento. Ele cobre, no mínimo, estas 16 paradas e 2 confirmações.

**Paradas de entrada**

- `STOP-IN-01`: use-case ausente, em branco ou sem marcador.
- `STOP-IN-02`: contrato ausente, em branco, vazio ou não-objeto.
- `STOP-IN-03`: contrato antes do use-case.
- `STOP-IN-04`: destino ausente, relativo, com `~`, com sinalização de fim de arquivo ou dentro da árvore do archetype.
- `STOP-IN-05`: package base ausente ou ambíguo.

**Paradas de ambiente**

- `STOP-ENV-01`: JAR instalado não encontrado.
- `STOP-ENV-02`: arquivo obrigatório do procedimento ausente.
- `STOP-ENV-03`: catálogo ausente ou ilegível.
- `STOP-ENV-04`: pós-processador ausente.
- `STOP-ENV-05`: várias versões instaladas e o spec não declara qual usar.

**Paradas de derivação**

- `STOP-DER-01`: nome de entidade ambíguo.
- `STOP-DER-02`: lista de atributos derivada inválida.
- `STOP-DER-03`: duas fontes de mesma prioridade divergem.
- `STOP-DER-04`: caminho do contrato não absoluto ou falha ao gravar o contrato.

**Paradas de execução e de regra**

- `STOP-EXEC-01`: falha de catálogo ou de pós-processador durante a geração.
- `STOP-RULE-01`: o procedimento não cobre um caso.

**Confirmações**

- `STOP-CONF-01`: pasta de destino ainda não existe.
- `STOP-CONF-02`: já existe módulo com o mesmo nome.

### Key Entities

- **Parada Dura**: evento que interrompe o fluxo antes de qualquer geração; possui código, gatilho, causa, local, regra, opções, sugestão e instruções de retomada.
- **Registro de Paradas**: lista canônica de códigos e gatilhos, mantida no ponto de entrada do procedimento.
- **Template de Mensagem**: formato obrigatório com os 10 campos e as regras de linguagem simples.

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
---
```

**Exemplo completo — STOP-DER-02**

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
> **O que não fazer:** não use "String:name"; não edite o código gerado; não rode o Maven; não coloque senhas ou chaves no contrato.
---
```

- **Termo do Dicionário**: termo técnico e sua tradução em linguagem simples.

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

- **Lista de Atributos**: valor derivado do contrato no formato `Tipo:nome`, sujeito à regra de formato do catálogo.
- **Divergência Catálogo × Procedimento**: diferença entre o valor declarado no catálogo e o valor resumido no procedimento.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% das 16 paradas e das 2 confirmações têm código, registro e os 10 campos.
- **SC-002**: Um operador que começou ontem consegue responder às 5 perguntas de orientação em 100% das paradas revisadas.
- **SC-003**: Uma parada nova sem código ou sem registro faz o build falhar.
- **SC-004**: Uma lista de atributos inválida gera 0 módulos e uma mensagem com 3 opções.
- **SC-005**: A contagem permanece 19 arquivos, 13 Markdown, 6 JSON, 35 propriedades e 18 filesets.
- **SC-006**: Todas as suítes de contrato passam.
- **SC-007**: 0 ocorrências de "informe apenas" no procedimento.
- **SC-008**: O dicionário contém os 20 termos obrigatórios.
- **SC-009**: Os 9 defaults e as regras de formato literais da tabela conferem com o catálogo.
- **SC-010**: A tabela de flags críticas renderiza exatamente três colunas.
- **SC-011**: Nenhum default ou regra de formato do catálogo foi alterado.
- **SC-012**: O JAR instalado contém as mensagens e o registro.

## Assumptions

- O procedimento continua sendo promptware, sem motor, CLI ou script externo.
- O catálogo de flags continua sendo a autoridade final sobre defaults e regras de formato.
- As mensagens são em português.
- O valor padrão `String:name` continua no catálogo, mas o procedimento não o usa como fallback.
- O registro das 16 paradas e das 2 confirmações está completo. A parada `STOP-DER-02` substitui o aviso com fallback e é a única mudança de comportamento; as "15 outras paradas" de FR-012 são as demais entradas do registro.
- As specs históricas e o README não são fontes de runtime.
- A numeração da feature sucede a 006; a 003 permanece arquivada.
- As observações sobre o README foram registradas como contexto, mas a reestruturação do tópico permanece fora do escopo por decisão explícita do operador.
- "Aumento perceptível" em NFR-001 foi interpretado como aumento relativo de até 10% no build local; acima disso, a mudança deve ser investigada antes da conclusão.
- O estado atual foi conferido como linha de base: 19 arquivos no procedimento (13 Markdown e 6 JSON), 35 propriedades e 18 filesets no catálogo.
- As 9 flags críticas da tabela são `entity_name`, `entity_package_name`, `custom_entity_attributes_list`, `custom_entity_attribute_imports`, `contract_enum_definitions`, `contract_nested_type_definitions`, `entity_contract_file_path`, `enable_custom_entity_attributes` e `enable_folder_src_test_java`; a tabela-resumo fica em `regras-04` §3.1.

## Out of Scope

- Suporte a tipos `java.time.*` e coleções `List<T>`.
- Remoção do redirect legado `utils/`.
- Conversão de avisos em paradas duras, além da lista de atributos.
- Reestruturação do tópico do README.
- Novas flags, novos arquivos no procedimento ou mudanças no catálogo.
- Alteração de versões de Maven, Java ou do plugin de archetype.
- Alteração de código gerado ou de fixtures.
- Commit ou push.

## Compliance Constraints

- Não criar arquivos novos em `llm-maven-flags-inference`.
- Não remover arquivos existentes.
- Não alterar `defaultValue` nem `validationRegex` do catálogo.
- Não alterar o README como fonte de runtime.
- Não editar `target/` nem o JAR instalado à mão.
- Não expor segredos nas mensagens.
- Toda alteração deve passar por `mvn -B test` e `mvn clean install`.
- A verificação do JAR instalado deve passar com a propriedade de verificação opt-in.

## Origins Traceability

- Análise 01: inventário, contrato de flags, riscos operacionais, fallback `String:name`, assimetria de nomenclatura, limitações de tipo.
- Análise 02 — A-2: divergência entre catálogo e procedimento sobre caminho absoluto.
- Análise 02 — A-4: tabela de defaults e regras de formato sem teste de sincronia.
- Análise 02 — A-5: conflito entre a regra geral de proveniência e a regra específica de `none`.
- Análise 02 — A-8: pipes não escapados na tabela de flags críticas.
- Decisão do operador: Opção A para a lista de atributos inválida.
- Decisão do operador: R-11, linguagem simples em toda parada dura.
- Decisão do operador: D8 aprovada, com dicionário de 20 termos.
