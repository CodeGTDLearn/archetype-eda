# Feature Specification: Contrato de Destino de Geração (outputDirectory) no Procedimento Empacotado

**Feature Branch**: `005-output-directory-contract`

**Created**: 2026-11-19

**Status**: Draft

**Input**: User description: "Definir o destino de geração como uma entrada explícita do spec e tornar normativo, no procedimento empacotado, (1) de onde vem o caminho, (2) quando o agente deve perguntar em vez de agir, (3) como emitir a geração, (4) como validar o destino e (5) como reportá-lo ao operador antes e depois da execução."

**Contexto de origem**: O procedimento empacotado é o conjunto de 19 arquivos que viajam dentro do JAR do archetype e que o agente de código lê de dentro do artefato instalado para transformar o prompt do operador na linha de comando de geração. Esse procedimento declara que o próprio JAR é a fonte da verdade e proíbe usar o README como fonte de runtime. Hoje o spec do operador tem exatamente quatro entradas: tipo de módulo, package base, use-case e contrato JSON.

A palavra que identifica o destino não aparece em nenhum arquivo do procedimento nem no catálogo de flags. Ela aparece apenas no README, como exemplo, e dentro do pós-processador, como atributo do objeto do próprio Maven — não como decisão do procedimento. Resultado: o agente não sabe onde criar o projeto, não pergunta ao operador, pode gerar dentro da própria árvore do archetype (arquivos não versionados, dois `pom.xml` aninhados) ou falhar por ausência de projeto no diretório de execução. Convivem ainda dois mecanismos conflitantes: um procedimento de "mudança de diretório + movimentação posterior" e a opção de "-DoutputDirectory" documentada no README.

Não se trata de um defeito contra uma intenção acordada: é uma intenção que ainda não foi definida. Por isso esta feature cria o contrato, em vez de restaurar um comportamento perdido.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Operador informa o destino e o projeto nasce onde ele pediu (Priority: P1)

O operador entrega o use-case, o contrato e a pasta de destino. O agente lê o procedimento de dentro do JAR empacotado, deriva as flags, exibe a proveniência e o destino absoluto, e executa a geração com esse destino. O módulo nasce dentro da pasta indicada, com o próprio `pom.xml`, e a verificação de compilação roda ali — não na árvore do archetype.

**Why this priority**: é a razão de existir da feature. Sem destino definido, cada geração depende de um palpite do agente, e o palpite errado contamina o repositório do archetype ou aborta a geração.

**Independent Test**: pode ser testado de forma isolada gerando um módulo com destino informado e verificando que (a) a linha de comando emitida carrega o destino, (b) o módulo existe na pasta pedida, (c) nada foi criado dentro da árvore do archetype e (d) a compilação de verificação ocorreu no módulo gerado.

**Acceptance Scenarios**:

1. **Given** um spec com use-case, contrato e destino absoluto, **When** o agente prepara a geração, **Then** a linha de comando emitida carrega o destino informado e o agente mostra o caminho absoluto ao operador antes de executar.
2. **Given** a geração concluída, **When** se verifica o disco, **Then** o módulo existe em `<destino>/<artifactId>` e **nenhum** arquivo novo apareceu dentro da árvore do archetype.
3. **Given** o módulo gerado, **When** a verificação de compilação é executada, **Then** ela ocorre dentro de `<destino>/<artifactId>` e o resultado é reportado com o caminho absoluto.
4. **Given** o relatório final, **When** o operador o lê, **Then** ele informa o caminho absoluto do módulo gerado e a linha de comando executada.
5. **Given** um spec sem destino, **When** o agente percebe a ausência, **Then** ele **não** deriva nem executa nada e faz uma única pergunta ao operador sobre a pasta de destino.

---

### User Story 2 - Agente pergunta em vez de chutar (Priority: P1)

Quando o operador não informa o destino, o agente interrompe o fluxo antes de qualquer derivação, explica o que falta e pergunta uma vez. Ele pode sugerir uma pasta temporária fora da árvore do archetype, mas só a usa depois de confirmação explícita. Em execução sem operador disponível, ele para e reporta em vez de inventar um caminho.

**Why this priority**: um destino presumido em silêncio é pior do que nenhum destino: produz um resultado plausível e errado, com módulos gerados em lugares que o operador não pediu. Perguntar é o comportamento coerente com as demais lacunas do procedimento, que já obrigam o agente a parar quando falta informação.

**Independent Test**: pode ser testado de forma isolada entregando um spec sem destino e verificando que nenhuma derivação ocorreu, que houve exatamente uma pergunta, que nada foi gerado e que a sugestão oferecida está fora da árvore do archetype.

**Acceptance Scenarios**:

1. **Given** um spec sem destino, **When** o agente inicia o processamento, **Then** ele para antes de derivar, informa que o destino falta e pergunta uma única vez qual pasta usar.
2. **Given** a pergunta feita, **When** o operador responde com um caminho absoluto válido, **Then** o fluxo prossegue e a geração usa esse caminho.
3. **Given** uma execução sem operador para responder, **When** o destino falta, **Then** o agente para com um relatório do que falta e **não** aplica nenhum caminho presumido.
4. **Given** a pergunta feita, **When** o agente oferece uma sugestão, **Then** a sugestão é uma pasta temporária fora da árvore do archetype e só é usada após confirmação.
5. **Given** um spec sem destino, **When** se inspeciona a saída, **Then** a linha de comando emitida não carrega nenhum destino inventado.

---

### User Story 3 - Destino inválido ou ambíguo é recusado com clareza (Priority: P2)

O operador informa um destino relativo, um caminho dentro da árvore do archetype, uma pasta que já contém um módulo com o mesmo nome, ou coloca a chave de destino por engano dentro do contrato. Nenhum desses casos pode passar em silêncio: o agente recusa, pergunta ou ignora com aviso — e, sobretudo, o contrato jamais vira um campo da entidade gerada.

**Why this priority**: destinos dentro da árvore do archetype são exatamente o dano que a feature existe para evitar, e uma chave de destino dentro do contrato seria silenciosamente transformada em atributo de negócio, produzindo uma entidade com campo espúrio.

**Independent Test**: pode ser testado de forma isolada gerando com cada variação inválida e verificando a recusa, o aviso e a ausência do campo espúrio na entidade gerada.

**Acceptance Scenarios**:

1. **Given** um destino relativo, **When** o agente o valida, **Then** ele recusa ou pede um caminho absoluto, e não gera nada.
2. **Given** um destino dentro da árvore do archetype, **When** o agente o valida, **Then** ele recusa com um aviso explícito e não gera nada.
3. **Given** um destino cuja pasta de módulo já existe, **When** o agente valida, **Then** ele pergunta antes de sobrescrever e nunca apaga o conteúdo existente por conta própria.
4. **Given** uma chave de destino dentro do contrato, **When** o agente deriva os atributos, **Then** a chave é ignorada com aviso, não vira campo da entidade e não aparece como atributo na lista derivada.
5. **Given** um destino inexistente mas criável, **When** o agente valida, **Then** ele cria a pasta e prossegue; se a criação falhar, ele para e reporta.

---

### User Story 4 - Mantenedor comprova que o procedimento empacotado está atualizado (Priority: P2)

O mantenedor altera o procedimento e precisa de prova, dentro do próprio archetype, de que a regra nova está no procedimento empacotado, de que o JAR reinstalado a carrega, e de que nada mais regrediu: o número de flags, o número de arquivos do procedimento, as suítes e as fixtures de integração.

**Why this priority**: o modo de falha recorrente deste projeto é o artefato desatualizado — o código foi edited, mas o JAR em uso é o antigo. Sem essa prova, a regra nova pode existir no repositório e nunca chegar ao agente que lê do artefato instalado.

**Independent Test**: pode ser testado de forma isolada rodando a build completa e conferindo o procedimento dentro do JAR reinstalado, além das verificações de contagem e as suítes existentes.

**Acceptance Scenarios**:

1. **Given** a alteração aplicada, **When** a build completa roda, **Then** as suítes existentes e as fixtures de integração continuam verdes.
2. **Given** a build concluída, **When** se examina o JAR reinstalado, **Then** os arquivos do procedimento dentro dele contêm a regra de destino.
3. **Given** a verificação automatizada do procedimento, **When** ela executa, **Then** ela falha se a regra de destino não estiver declarada, se o número de arquivos do procedimento mudar, ou se o número de flags do catálogo mudar.
4. **Given** o repositório do archetype, **When** se examina o estado do versionamento, **Then** nenhuma geração de teste deixou resíduo dentro da árvore.

---

### Edge Cases

- **Destino ausente**: o agente para antes de derivar e pergunta uma vez; em execução sem operador, para e reporta.
- **Destino relativo**: recusado ou convertido para absoluto após confirmação; nunca aceito em silêncio.
- **Destino dentro da árvore do archetype** (inclusive nas pastas de código, de recursos ou de build): recusado com aviso.
- **Pasta de destino inexistente**: criada pelo agente; se a criação falhar, o agente para e reporta o erro.
- **Pasta de destino já existente e já contendo um módulo de mesmo nome**: o agente pergunta antes de sobrescrever e nunca remove conteúdo por conta própria.
- **Destino informado dentro do contrato em vez do spec**: a chave é ignorada com aviso e tratada como metachave — nunca como atributo da entidade.
- **Destino informado em mais de um lugar**: o spec tem precedência; a ocorrência no contrato é ignorada com aviso.
- **Destino com caractere especial no caminho**: o valor é transportado sem perda, com a proteção de escape exigida pelo shell em uso.
- **Sinalização de fim de arquivo ou espaços no caminho**: o agente normaliza antes de comparar e de emitir.
- **Agente suggestivo de pasta temporária**: a sugestão jamais é aplicada sem confirmação do operador.
- **Divergência entre o README e o procedimento empacotado**: o procedimento empacotado prevalece, por ser a fonte da verdade em runtime.
- **Contradição remanescente entre o procedimento e o catálogo**: detectada por verificação automatizada.
- **Contagem de arquivos do procedimento ou de flags do catálogo alterada**: detectada por verificação automatizada, que falha o build.
- **Artefato instalado desatualizado em relação ao repositório**: a build reinstala o artefato e a verificação passa a ler o procedimento de dentro do artefato, não da árvore de desenvolvimento.

## Requirements *(mandatory)*

### Functional Requirements

**Entrada do destino**

- **FR-001**: O procedimento MUST declarar o destino de geração como entrada explícita do spec do operador, distinta do contrato, e MUST declarar de forma inequívoca que o destino nunca faz parte do contrato.
- **FR-002**: A entrada de destino MUST ser identificada por marcador reconhecível, e a ausência de marcador reconhecível MUST ser tratada como ausência de destino.
- **FR-003**: A entrada de destino MUST aceitar um caminho absoluto de destino e MUST rejeitar, com aviso, caminho relativo, caminho com caractere de expansão de diretório do usuário, ou caminho com sinalização de fim de arquivo.
- **FR-004**: A entrada de destino MUST ser registrada como entrada permitida do spec e MUST ser listada na verificação de integridade do artefato empacotado.

**Comportamento quando o destino falta**

- **FR-005**: Quando o destino não for informado, o agente MUST interromper o fluxo antes de qualquer derivação ou execução, MUST reportar que o destino falta e MUST perguntar ao operador uma única vez.
- **FR-006**: A pergunta MUST oferecer uma sugestão de pasta temporária situated fora da árvore do archetype, e a sugestão MUST NOT ser aplicada sem confirmação explícita do operador.
- **FR-007**: Quando não houver operador disponível para responder, o agente MUST parar e reportar, e MUST NOT aplicar nenhum destino presumido — em particular, MUST NOT usar o diretório de trabalho corrente, a raiz do archetype, ou qualquer pasta de código, de recursos ou de build do próprio archetype.
- **FR-008**: O relatório de parada por destino ausente MUST seguir o mesmo formato de relatório de parada já previsto no procedimento para as demais paradas, incluindo a afirmação de que nenhuma derivação e nenhuma execução ocorreram.

**Emissão da geração**

- **FR-009**: A linha de comando de geração MUST carregar o destino informado, sob a forma de propriedade de diretório de saída do arquétipo, e o valor MUST usar barras normais, sem barra final e sem quebra de linha.
- **FR-010**: Todas as propriedades da linha de comando MUST permanecer protegidas contra a interpretação do shell em uso, e o caminho do destino MUST receber a mesma proteção quando contiver caracteres especiais.
- **FR-011**: O procedimento MUST-establishar um único mecanismo de definição de destino, removendo o mecanismo alternativo de mudança de diretório seguido de movimentação posterior do módulo gerado.
- **FR-012**: O procedimento MUST NOT conter caminho absoluto pertencente a um usuário ou a uma máquina específicos.

**Validação do destino**

- **FR-013**: Antes de executar, o agente MUST validar que o destino existe ou pode ser criado, e MUST NOT prosseguir quando a validação falhar.
- **FR-014**: O agente MUST validar que o destino não está dentro da árvore do archetype, e MUST recusar com aviso explícito quando estiver.
- **FR-015**: Quando a pasta de destino já contiver um módulo com o mesmo nome do artefato a gerar, o agente MUST perguntar antes de sobrescrever e MUST NOT remover o conteúdo existente por conta própria.
- **FR-016**: Após a geração, o agente MUST confirmar que o módulo gerado está na pasta de destino informada e que possui o próprio descritor de projeto, e MUST reportar o caminho absoluto do módulo gerado.
- **FR-017**: A verificação de compilação MUST ocorrer dentro do módulo gerado e MUST NOT ocorrer na raiz do archetype, e o resultado MUST ser reportado junto do caminho utilizado.

**Relato ao operador**

- **FR-018**: O agente MUST exibir o destino absoluto ao operador antes de executar a geração.
- **FR-019**: O relatório final MUST incluir o caminho absoluto do módulo gerado, a linha de comando executada e o resultado da verificação de compilação.
- **FR-020**: O destino MUST NOT ser tratado como uma flag do catálogo, MUST NOT ocupar linha na tabela de proveniência de flags e MUST NOT receber uma fonte de proveniência nova.

**Contrato e metachaves**

- **FR-021**: Uma chave de destino presente dentro do contrato MUST ser tratada como metachave: ignorada com aviso, nunca convertida em atributo da entidade e nunca convertida em atributo da lista de atributos derivada.
- **FR-022**: A lista de metachaves reconhecidas pelo procedimento MUST incluir explicitamente a chave de destino, e a verificação automatizada do procedimento MUST falhar quando uma chave de destino não estiver nessa lista.

**Limites de escopo**

- **FR-023**: O catálogo de flags MUST permanecer com o mesmo número de propriedades declaradas e o mesmo número de conjuntos de arquivos; a feature MUST NOT criar uma flag nova.
- **FR-024**: O número de arquivos do procedimento empacotado MUST permanecer o mesmo; a feature MUST NOT criar nem remover arquivo do procedimento.
- **FR-025**: A feature MUST NOT alterar o formato do contrato, o esquema do contrato, a forma de materialização do contrato, o descritor do arquétipo, o pós-processador, os templates do módulo gerado, o descritor do archetype, as fixtures de integração, o fluxo de integração contínua ou a constituição do projeto.
- **FR-026**: A feature MUST NOT criar dependência nova, MUST NOT alterar a versão do artefato e MUST NOT executar operação de versionamento fora dos gatilhos declarados pelo kit de especificação.

**Prova**

- **FR-027**: O archetype MUST possuir verificação automatizada que prove, por leitura do procedimento empacotado, que a regra de destino está declarada nas instâncias esperadas do procedimento.
- **FR-028**: A verificação automatizada MUST falhar quando o número de arquivos do procedimento, o número de flags do catálogo ou a lista de metachaves divergirem do esperado.
- **FR-029**: A verificação automatizada MUST ser executada pela build do archetype e MUST NOT depender de inspeção manual.
- **FR-030**: Após a alteração, o artefato MUST ser reinstalado e a verificação MUST passar a ler o procedimento de dentro do artefato instalado, e não da árvore de desenvolvimento.

### Key Entities

- **Spec do operador**: entrada externa única do procedimento; passa a conter, além do tipo de módulo, do package base, do use-case e do contrato, a pasta de destino da geração.
- **Destino de geração**: caminho absoluto onde o módulo será criado; não é flag, não é atributo da entidade e não recebe fonte de proveniência; é exibido ao operador antes e depois da execução.
- **Módulo gerado**: artefato produzido pela geração; vive dentro do destino informado, com descritor de projeto próprio, e é onde a verificação de compilação ocorre.
- **Procedimento empacotado**: conjunto de arquivos que viajam dentro do artefato e que o agente lê em runtime; é a fonte da verdade sobre o destino de geração.
- **Contrato**: documento JSON de domínio entregue pelo operador; o destino de geração nunca faz parte dele.
- **Catálogo de flags**: conjunto declarado de propriedades de geração; permanece inalterado por esta feature.
- **Tabela de proveniência**: relatório das flags e de suas origens; não recebe o destino de geração.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% das gerações executadas pelo agente carregam o destino informado na linha de comando, com caminho absoluto.
- **SC-002**: 0 módulos gerados dentro da árvore do archetype e 0 resíduos de geração não versionados na árvore do repositório.
- **SC-003**: 0 destinos presumidos em silêncio; 100% dos casos de destino ausente resultam em interrupção antes da derivação com uma única pergunta, ou em parada com relatório quando não há operador disponível.
- **SC-004**: 100% dos relatórios finais registram o caminho absoluto do módulo gerado e o resultado da verificação de compilação nele executada.
- **SC-005**: 100% das verificações de compilação ocorrem dentro do módulo gerado e nenhuma ocorre na raiz do archetype.
- **SC-006**: 0 módulos gerados contêm atributo de entidade derivado de uma chave de destino informada dentro do contrato.
- **SC-007**: 0 caminhos absolutos de usuário ou de máquina específicos permanecem no procedimento empacotado, e 0 mecanismos concorrentes de definição de destino coexistem nele.
- **SC-008**: O número de flags do catálogo, o número de arquivos do procedimento, o número de conjuntos de arquivos, o número de suítes e o número de fixtures de integração permanecem inalterados após a entrega.
- **SC-009**: 100% das suítes e das fixtures de integração existentes permanecem verdes após a entrega.
- **SC-010**: 100% das verificações automatizadas da feature passam quando lidas a partir do artefato reinstalado, e 0 verificações passam apenas porque a árvore de desenvolvimento foi alterada.
- **SC-011**: 100% das gerações em que o destino aponta para dentro da árvore do archetype são recusadas com aviso explícito, sem criação de arquivos.

## Assumptions

- O fluxo normal é interativo: o operador conduz o agente e pode responder a perguntas.
- O agente executa a geração a partir de um diretório que contém um descritor de projeto próprio, ou de outro modo que torne a execução possível sem que o destino passe a serWNOME implícito do módulo gerado.
- O arquétipo já está instalado no repositório local e registrado no catálogo local, de modo que a geração resolve o artefato empacotado em runtime.
- O agente lê o procedimento de dentro do artefato instalado, e não da árvore de desenvolvimento — a menos que o repositório-fonte seja explicitamente pedido.
- A raiz do módulo gerado é sempre composta pelo destino de saída e pelo identificador do artefato, conforme o comportamento já verificado do pós-processador.
- O shell em uso é o do sistema operacional do operador; a proteção contra interpretação de argumentos é tratada como responsabilidade da regra existente de escape de propriedades, já aplicada a todas as propriedades da linha de comando.
- O procedimento continua sendo escrito em linguagem natural para o agente, sem dependência de ferramenta adicional de linha de comando.
- Nenhuma alteração de versão do artefato é necessária: a entrega reinstala a mesma versão.
- O fluxo de integração contínua não executa geração de projeto, portanto não precisa de destino de geração e permanece intocado.

## Out of Scope

- Criar uma flag nova para o destino no catálogo de flags.
- Criar, remover ou renomear arquivo do procedimento empacotado, ou alterar a ordem global de leitura dos arquivos.
- Alterar o formato do contrato, o esquema do contrato, a forma de materialização do contrato ou o destino da cópia do contrato no módulo gerado.
- Alterar o descritor do arquétipo, o pós-processador, os templates do módulo gerado, o descritor do archetype, as fixtures de integração ou o fluxo de integração contínua.
- Mover o módulo gerado depois da geração, ou automatizar a movimentação posterior.
- Emendar a constituição do projeto.
- Corrigir o comentário obsoleto na tabela de predefinição do kit de especificação que cita um mecanismo de inferência removido do projeto — dívida registrada, fora desta feature.
