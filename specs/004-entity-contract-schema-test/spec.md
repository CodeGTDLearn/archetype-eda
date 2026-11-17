# Feature Specification: Gate de Conformidade Entidade–Contrato (Schema Test na Aplicação Gerada)

**Feature Branch**: `004-entity-contract-schema-test`

**Created**: 2026-11-13

**Status**: Draft

**Input**: User description: "Toda aplicação gerada COM contrato persistido (`src/test/resources/contracts/contract.json`) deve nascer com um teste automatizado de conformidade na sua própria suíte de testes, que prova a cada execução que a entidade (e seus tipos derivados: aninhados e enums) continua 100% fiel ao contrato — a fonte única da verdade. Duas frentes: Frente 01 (o teste vive e roda no ciclo de testes da aplicação gerada, pelo resto da vida dela) e Frente 02 (o archetype, em si, prova via testes de integração que entrega o artefato da Frente 01 funcionando). Ativação derivada, sem flag nova: o teste só existe quando `enable_folder_src_test_java=true` E o contrato foi persistido; caso contrário a classe é removida durante a geração, no mesmo padrão de guarda condicional do pós-processador. Falhas devem nomear o atributo divergente, o sentido da divergência (contrato→entidade ou entidade→contrato), tipo esperado vs. encontrado/ausência, arquivo e teste. Casos de borda: contrato ausente/none/null/vazio, árvore de testes desativada, Lombok ligado/desligado, entidade sem atributos customizados, id presente/ausente, aninhados multinível, arrays, enums divergentes, diferença de caixa. Fora de escopo: ArchUnit, enforcement em compile-time, validação em runtime no startup, governança externa de CI/CD, alteração do formato do contrato ou do mecanismo de inferência. Sucesso: aplicação gerada com o perfil `entity-contract` executa o schema test verde na sua própria suíte; sabotagem controlada faz o teste falhar com mensagem clara; aplicações sem contrato não contêm a classe; os 15 perfis de IT existentes continuam verdes; nenhuma dependência nova no `pom.xml` da aplicação gerada."

**Contexto de origem**: o pós-processador já persiste o contrato no projeto gerado (`generator-03-entity_contract_file_path.groovy`, FR-023) com a intenção declarada de que um futuro teste valide a entidade contra ele. Esse teste ainda não existe — esta feature o cria.

## Clarifications

### Session 2026-11-13

- Q: Quando o contrato declara um `type` explícito para um atributo, o teste gerado deve respeitar esse tipo declarado ou ignorá-lo e estimar o tipo apenas pelo valor? → A: Espelhar o mecanismo: tipo declarado `java.lang`/tipo do projeto vence; tipo rejeitado cai para estimativa pelo valor.
- Q: Se o arquivo `contract.json` for apagado ou ficar ilegível na aplicação já gerada, o schema test deve falhar ou ser ignorado? → A: Falhar com mensagem estrutural explícita; nunca skip nem pass silencioso.
- Q: Quando há várias divergências ao mesmo tempo, o schema test deve reportar todas numa única execução ou parar na primeira? → A: Agregar todas as divergências numa única falha, com lista ordenada e determinística.
- Q: Em contratos de forma plana, como o teste deve distinguir atributos de flags e meta-chaves sem o catálogo de flags? → A: O contrato de forma plana jamais contempla flags; flags são relevantes para o archetype, não para a aplicação/módulo gerado. O teste não incorpora catálogo de flags: exclui apenas meta-chaves e trata toda chave de topo restante como atributo.
- Q: Quais formas de contrato o schema test gerado deve saber interpretar? → A: Todas as formas aceitas hoje (forma plana, `attributes` objeto, `attributes` array de objetos ou de strings, e `fields`), com precedência `attributes` > `fields` > campos de topo.
- Q: Como o schema test deve comparar um enum derivado do contrato — pelo nome do tipo, pelo conjunto de constantes, ou por ambos? → A: Por ambos: nome do tipo derivado da chave e conjunto de constantes.
- Q: O que o teste deve fazer quando uma chave do contrato, após a normalização prevista no mecanismo, continua sem formar um identificador Java válido? → A: Espelhar o mecanismo: normalizar como ele normaliza, e ignorar com aviso não fatal as chaves que ele descartaria (não fazem parte do conjunto efetivo de atributos).

### Session 2026-11-17

- Q: Para qual público a documentação da feature no README deve ser escrita? → A: Ambos (desenvolvedor que usa o módulo gerado e mantenedor do archetype), sempre em linguagem simples, clara e objetiva, sem jargão técnico — o texto deve ser compreensível por um desenvolvedor estagiário sem experiência prévia.
- Q: O subtópico **Contratos** deve documentar apenas a feature do schema test ou também o mecanismo de contrato de forma mais ampla? → A: Apenas esta feature (schema test), apontando para as seções existentes do README, sem repetir o conteúdo do mecanismo de contrato.
- Q: Qual nome deve ser usado na documentação para identificar a feature? → A: **Teste de Conformidade Entidade–Contrato** (nome canônico na documentação; "schema test" permanece como nome de trabalho interno).
- Q: Como o subtópico **Contratos** deve se relacionar com a seção existente *Testabilidade → 2.1) SCHEMA TEST ENTIDADE-CONTRATO*? → A: Manter a seção 2.1 como está e linkar a partir do subtópico; o subtópico documenta a funcionalidade e a seção 2.1 continua sendo a referência de testabilidade.
- Q: O que a seção "como usar" deve ensinar? → A: Apenas o que a feature faz e como funciona, sem passo a passo de uso (sem tutorial).

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Desenvolvedor do módulo gerado detecta drift entidade–contrato na sua própria suíte (Priority: P1)

Com a aplicação já gerada e pronta, o desenvolvedor do módulo altera indevidamente a entidade (remove ou renomeia um campo, muda um tipo, ou adiciona um campo ausente do contrato) — ou adultera o contrato — e roda os testes DA APLICAÇÃO (build local, CI do módulo). A execução falha imediatamente, com mensagem que nomeia o atributo divergente e o sentido da divergência.

**Why this priority**: é a razão de existir da feature — o contrato é a fonte única da verdade para o Front-End e para a geração do artefato; hoje a fidelidade depende de disciplina humana e a divergência só é percebida tardiamente. Sem este gate, nada impede drift silencioso.

**Independent Test**: pode ser totalmente testado gerando uma aplicação com contrato persistido, adulterando um atributo da entidade (ou do contrato) e executando o ciclo de testes da aplicação gerada: a suíte deve falhar e a mensagem deve apontar o atributo e o sentido da divergência. Entrega valor sozinha: o gate existe e fiscaliza.

**Acceptance Scenarios**:

1. **Given** uma aplicação gerada com contrato persistido em `src/test/resources/contracts/contract.json`, **When** o desenvolvedor remove um campo da entidade e roda os testes da aplicação, **Then** o schema test falha nomeando o atributo ausente e indicando o sentido contrato→entidade (chave do contrato sem campo correspondente na entidade).
2. **Given** a mesma aplicação, **When** o desenvolvedor adiciona na entidade um campo que não existe no contrato (campo "clandestino") e roda os testes, **Then** o schema test falha nomeando o campo excedente e indicando o sentido entidade→contrato.
3. **Given** a mesma aplicação, **When** o desenvolvedor altera o tipo de um campo (sem renomeá-lo) e roda os testes, **Then** o schema test falha indicando o atributo, o tipo esperado (declarado no contrato quando houver, senão estimado do valor) e o tipo encontrado.
4. **Given** a mesma aplicação gerada com Lombok ativado, **When** qualquer adulteração acima é repetida, **Then** o veredito do schema test é idêntico ao cenário sem Lombok (a validação é independente de boilerplate gerado).
5. **Given** a mesma aplicação, **When** nada é adulterado, **Then** o schema test passa verde e consta como executado na suíte da aplicação.
6. **Given** uma aplicação com múltiplas divergências simultâneas (ex.: um campo removido e outro com tipo alterado), **When** os testes da aplicação rodam, **Then** uma única falha lista todas as divergências em ordem determinística, cada uma com atributo, tipo esperado, tipo encontrado/ausência e sentido.

---

### User Story 2 - Mantenedor do archetype comprova que a geração entrega o gate funcionando (Priority: P1)

O mantenedor do archetype precisa de prova automatizada, dentro do próprio archetype, de que a mecânica de geração entrega a Frente 01 corretamente: o perfil de integração `entity-contract` gera a aplicação e executa `mvn clean verify` DENTRO dela; o schema test — rodado pela aplicação gerada, nunca pelo archetype sobre si mesmo — deve constar como executado e verde. Adicionalmente, um cenário de sabotagem (entidade ou contrato adulterado na aplicação gerada) deve produzir a falha do schema test na execução dos testes dela, provando que o gate entregue funciona de verdade.

**Why this priority**: sem esta frente, o archetype pode passar a entregar um teste órfão, quebrado ou que não roda — defeito invisível até o primeiro usuário do módulo. É a garantia de entrega da Frente 01 e a proteção contra regressão futura da mecânica.

**Independent Test**: pode ser testado executando a suíte de integração do archetype: o perfil `entity-contract` deve rodar o verify da aplicação gerada com o schema test verde, e o cenário de sabotagem deve falhar o schema test da aplicação adulterada com mensagem diagnóstica. As duas metades (verde e sabotagem) são verificáveis de forma independente.

**Acceptance Scenarios**:

1. **Given** o archetype em versão candidata, **When** o perfil de integração `entity-contract` é executado, **Then** a aplicação gerada contém a classe do schema test, o ciclo `clean verify` dela executa o schema test e ele termina verde.
2. **Given** a aplicação gerada pelo perfil `entity-contract`, **When** um cenário de sabotagem adultera a entidade (ou o contrato persistido) antes da execução dos testes dela, **Then** o schema test falha e a mensagem nomeia o atributo divergente e o sentido da divergência.
3. **Given** a feature integrada, **When** a suíte completa de integração do archetype é executada, **Then** os 22 perfis existentes (`basic`, `binding-tests-default-only`, `binding-tests-list-only`, `binding-tests-map-only`, `config-yml`, `custom-attributes`, `custom-enums-from-payload`, `custom-nested-edge-cases`, `custom-nested-objects`, `defaults-only`, `entity-contract`, `external-binding-default-only`, `external-binding-list-only`, `external-binding-map-only`, `external-binding-mutable-only`, `external-binding-validated-only`, `full`, `id-normalization`, `java21`, `logback-console-granular`, `logback-no-elk`, `minimal`) continuam verdes.

---

### User Story 3 - Aplicação gerada SEM contrato não carrega teste órfão (Priority: P1)

Quando o módulo é gerado sem o mecanismo de inferência (sem `entity_contract_file_path`, ou com valor `none`/`null`/vazio, ou caminho inválido), ou quando a árvore de testes está desativada (`enable_folder_src_test_java=false`), a aplicação gerada NÃO contém a classe do schema test e continua compilando e testando verde. Nenhum flag novo é criado: a existência do teste é derivada da presença do contrato persistido somada à árvore de testes habilitada.

**Why this priority**: é o complemento obrigatório da ativação derivada (Constituição, Princípio I — NON-NEGOTIABLE): um teste órfão sem contrato quebraria o build de toda aplicação gerada sem o mecanismo de inferência, reintroduzindo silenciosamente uma capacidade que a configuração não pediu. Nota: pelo projeto de inferência, o contrato jamais deixará de existir em gerações reais — mas esta jornada é válida como garantia estrutural e de robustez.

**Independent Test**: pode ser testado gerando aplicações nas configurações negativas (sem contrato; contrato `none`; árvore de testes desativada) e verificando que (a) a classe do schema test não existe no projeto gerado e (b) o build e os testes da aplicação passam verdes.

**Acceptance Scenarios**:

1. **Given** uma geração sem `entity_contract_file_path` (ou com valor `none`), **When** a aplicação é gerada, **Then** a classe do schema test está ausente e o build/testes da aplicação passam.
2. **Given** uma geração com `enable_folder_src_test_java=false`, **When** a aplicação é gerada, **Then** nem o contrato nem a classe do schema test existem e o build/testes da aplicação passam.
3. **Given** uma geração com `entity_contract_file_path` apontando para caminho inexistente, **When** a aplicação é gerada, **Then** o contrato não é persistido (comportamento atual: aviso, sem quebrar a geração), a classe do schema test está ausente e o build/testes da aplicação passam.

---

### User Story 4 - Aplicação gerada fiscaliza tipos aninhados e enums derivados do contrato (Priority: P2)

Além dos atributos de primeiro nível, o desenvolvedor do módulo quer que estruturas derivadas do contrato também sejam fiscalizadas: tipos aninhados gerados a partir de objetos do contrato são validados recursivamente (inclusive em mais de um nível), e enums derivados do contrato têm suas constantes conferidas contra a definição do contrato. A entidade deve representar integralmente o conteúdo do contrato: nenhuma chave sem correspondente é tolerada e nenhuma incompatibilidade de hierarquia passa despercebida.

**Why this priority**: estende a mesma garantia de fidelidade às estruturas compostas. É P2 porque o núcleo do drift (atributos planos da entidade) já está coberto pela US1; a cobertura recursiva fecha a lacuna restante sem bloquear o MVP.

**Independent Test**: pode ser testado gerando uma aplicação cujo contrato contém objetos aninhados multinível e enums, adulterando um campo de tipo aninhado (ou uma constante de enum) e verificando que o schema test falha apontando o caminho completo do atributo divergente (ex.: `endereco.logradouro`) ou a constante de enum divergente.

**Acceptance Scenarios**:

1. **Given** uma aplicação gerada a partir de contrato com objeto aninhado, **When** um campo do tipo aninhado é removido ou renomeado, **Then** o schema test falha identificando o caminho hierárquico completo do atributo divergente.
2. **Given** uma aplicação gerada a partir de contrato com enum, **When** uma constante é removida ou renomeada apenas no enum Java (ou apenas no contrato), **Then** o schema test falha nomeando a constante divergente e o enum afetado.
3. **Given** a mesma aplicação íntegra, **When** os testes rodam, **Then** o schema test passa verde validando recursivamente todos os níveis de aninhamento e todos os enums derivados.
4. **Given** a fixture IT `entity-contract` com 6 objetos aninhados em cadeia (7 níveis contando a entidade) e enums (topo e aninhado), **When** o ciclo IT do archetype roda, **Then** o schema test executado pela aplicação gerada passa verde e a sabotagem de um campo em qualquer nível — inclusive a folha mais profunda — ou de uma constante de enum falha com o caminho hierárquico completo.

---

### Edge Cases

- **Contrato ausente, caminho inválido ou valor `none`/`null`/vazio**: a classe do schema test não é gerada; a aplicação compila e testa verde (coberto pela US3).
- **Árvore de testes desativada** (`enable_folder_src_test_java=false`): nem contrato nem schema test existem no projeto gerado (mesma guarda que já remove `src/test` inteiro).
- **Lombok ativado e desativado**: o veredito do schema test é idêntico nos dois modos (a comparação não pode depender de getters/setters ou boilerplate gerado).
- **Entidade sem atributos customizados** (`enable_custom_entity_attributes=false`): o schema test valida o que houver no contrato — no mínimo a regra especial do id; não falha por ausência de atributos de negócio quando o contrato também não os tem. Nesse cenário, `username`/`password` são scaffold conhecido do template e ficam excluídos da direção entidade→contrato.
- **Atributo `id` presente no contrato**: mapeia para o campo técnico de identidade da entidade (`<entidade>Id`, sempre `Long`), não para um campo comum com tipo estimado do valor; o campo técnico é excluído da direção entidade→contrato.
- **Atributo `id` ausente do contrato**: o campo técnico `<entidade>Id` continua existindo na entidade e continua excluído da comparação (não é campo clandestino).
- **Objetos aninhados em mais de um nível**: validação recursiva até a folha, com caminho hierárquico completo na mensagem.
- **Coleções/arrays no contrato**: comportamento idêntico ao das regras atuais do mecanismo de inferência (o teste aplica a mesma leitura de tipo que originou a entidade; nenhuma semântica nova é inventada).
- **Constantes de enum removidas ou renomeadas em apenas um dos lados**: falha nomeando a constante e o enum (US4).
- **Contrato apagado ou ilegível na aplicação já gerada (pós-geração)**: o schema test falha como falha estrutural explícita (arquivo ausente/ilegível), nunca como skip ou pass silencioso — deletar o contrato é, ele próprio, uma violação da fonte única da verdade.
- **Contrato plano com chave de aparência de flag**: cenário não previsto pelo contrato de forma plana (que jamais contempla flags); pela regra de classificação, a chave seria tratada como atributo. O contrato persistido é responsabilidade do mecanismo de inferência e não carrega chaves de flag.
- **Chave que permanece inválida após a normalização** (não forma identificador Java válido): descartada do conjunto efetivo de atributos com aviso não fatal, espelhando o mecanismo; não gera falha nem exige campo na entidade.
- **Diferença apenas de caixa (maiúsculas/minúsculas) em nome de atributo**: resolvida pela mesma normalização de nomes do mecanismo (ver perfil de integração `id-normalization`); o que o mecanismo trata como mesmo nome, o teste trata como mesmo nome; o que o mecanismo distingue, o teste distingue.

## Requirements *(mandatory)*

### Functional Requirements

**Ativação (derivada — Constituição, Princípio I, NON-NEGOTIABLE)**

- **FR-001**: Nenhum flag novo voltado ao usuário pode ser criado; a existência do schema test na aplicação gerada é DERIVADA exclusivamente de: `enable_folder_src_test_java=true` E contrato efetivamente persistido em `src/test/resources/contracts/contract.json`.
- **FR-002**: Quando a condição de FR-001 não se aplica, a classe do schema test é removida durante a geração, seguindo o mesmo padrão de guarda condicional que o pós-processador já usa para a própria árvore de testes.
- **FR-003**: O schema test lê o contrato persistido como ele existe hoje (`src/test/resources/contracts/contract.json`); o formato do contrato e o mecanismo de inferência NÃO são alterados.

**Semântica de conformidade (o que o teste gerado verifica)**

- **FR-004**: Direção contrato→entidade: toda chave/atributo do contrato deve existir como campo na entidade, com o tipo correspondente, recursivamente nos tipos aninhados; qualquer ausência ou incompatibilidade falha o teste.
- **FR-005**: Leitura do contrato: o teste deve reconhecer todas as formas aceitas hoje — campos de negócio no topo, `attributes` como objeto (mapa nome→valor), `attributes` como lista de objetos (`name`/`attribute`/`field` + `value`/`default`) ou lista de strings, e `fields` — aplicando a precedência `attributes` > `fields` > campos de topo. Exclui apenas as meta-chaves (`entity_name`, `entityName`, `entity`, `package`, `description`, `attributes`, `fields`) e, em contratos planos, trata toda chave de topo restante como atributo. O teste NÃO incorpora nem consulta o catálogo de flags: flags pertencem ao archetype, nunca ao contrato persistido no módulo gerado.
- **FR-006**: Direção entidade→contrato: nenhum campo da entidade pode existir sem chave correspondente no contrato, EXCETO o campo técnico de identidade e, quando o contrato não possui atributos efetivos, os campos de scaffold `username`/`password` do ramo `enable_custom_entity_attributes=false`.
- **FR-007**: Resolução de tipo: o teste deve espelhar exatamente a precedência do mecanismo de inferência — (a) `type` declarado em `java.lang` ou tipo do projeto VENCE, com o FQN reduzido ao nome simples; (b) `type` declarado fora dessas famílias é rejeitado e o tipo volta a ser estimado pelo valor; (c) sem tipo declarado válido, estimar pelo valor com as mesmas regras que originaram a entidade. Teste e geração nunca podem divergir de critério.
- **FR-008**: Regra especial do id: o atributo `id` do contrato mapeia para o campo técnico de identidade da entidade (`<entidade>Id`); ele não é comparado como campo comum com tipo estimado do valor, e o campo técnico é excluído da direção entidade→contrato.
- **FR-009**: Normalização de nomes: a derivação de nomes Java a partir das chaves do contrato deve espelhar a derivação do mecanismo (mesmo comportamento comprovado pelo perfil de integração `id-normalization`), incluindo normalização de acentos, camelCase de campos aninhados, tratamento de caixa e das grafias de `id`. Chaves que permanecem inválidas após a normalização são descartadas do conjunto efetivo de atributos, com aviso não fatal — exatamente como o mecanismo faz na geração.
- **FR-010**: Estruturas derivadas: tipos aninhados gerados a partir de objetos do contrato são validados recursivamente, e enums derivados do contrato são comparados por AMBOS — nome do tipo derivado da chave (PascalCase; `nested.<Tipo>` para enum de campo aninhado) e conjunto de constantes contra a definição do contrato. Constante removida ou renomeada em apenas um dos lados falha o teste.
- **FR-011**: O veredito do schema test deve ser independente de boilerplate gerado: mesmo resultado com Lombok ativado ou desativado.

**Diagnóstico de falha**

- **FR-012**: Toda falha do schema test deve produzir mensagem que identifica: o atributo divergente; o tipo esperado; o tipo encontrado (ou a ausência); o sentido da divergência (contrato→entidade ou entidade→contrato); o arquivo onde a falha ocorreu; o teste que falhou; e a falha estrutural ocorrida, quando aplicável. Havendo múltiplas divergências na mesma execução, TODAS devem ser agregadas numa única falha, em ordem determinística.
- **FR-013**: Contrato ilegível ou JSON inválido em tempo de execução do teste (após a geração) DEVE falhar como falha estrutural explícita; é proibido skip ou pass silencioso. A ausência do arquivo mantém o mesmo comportamento por construção, mas não é alvo de teste dedicado — o mecanismo de inferência garante a existência do contrato.

**Prova de entrega (Frente 02 — testes do próprio archetype)**

- **FR-014**: O archetype deve possuir verificação de integração de que a aplicação gerada pelo perfil `entity-contract` contém a classe do schema test, executa-a no seu próprio ciclo de testes (`clean verify` dentro da aplicação gerada) e a executa verde. A verificação MUST usar ao menos um contrato com 6 objetos aninhados em cadeia (7 níveis contando a entidade) e enums (topo e aninhado), cobrindo a US4.
- **FR-015**: O archetype deve possuir verificação de sabotagem: com a entidade (ou o contrato) adulterada na aplicação gerada, a execução dos testes dela falha no schema test, com a mensagem diagnóstica de FR-012.
- **FR-016**: O archetype deve possuir verificação negativa de que aplicações geradas sem contrato persistido (flag ausente, `none`, caminho inválido) ou com a árvore de testes desativada NÃO contêm a classe do schema test e continuam compilando e testando verde.
- **FR-017**: Os 22 perfis de integração existentes do archetype (lista nominal completa na US2, cenário 3) devem continuar verdes após a feature — os 15 originalmente citados mais `binding-tests-*` (3), `config-yml`, `java21` e `logback-*` (2).
- **FR-018**: Nenhuma dependência nova pode ser adicionada ao `pom.xml` da aplicação gerada.

**Documentação (README.md do archetype)**

- **FR-019**: O README.md deve documentar esta feature para dois públicos — o desenvolvedor que usa o módulo gerado e o mantenedor do archetype — em linguagem simples, clara e objetiva, sem jargão técnico, compreensível por um desenvolvedor estagiário sem experiência prévia.
- **FR-020**: O README.md deve conter um subtópico chamado **Contratos**, dentro do tópico **Funcionalidades**, no mesmo padrão `<details><summary>` de collapse/uncollapse já usado no restante da documentação, documentando a feature sob o nome **Teste de Conformidade Entidade–Contrato**.
- **FR-021**: A documentação da feature deve cobrir o que ela faz e como funciona; não deve incluir passo a passo (tutorial) de uso.
- **FR-022**: O escopo do subtópico é exclusivamente esta feature (schema test); o mecanismo de contrato em si não é reexplicado, apenas referenciado por link para as seções existentes do README.
- **FR-023**: A seção existente *Testabilidade → 2.1) SCHEMA TEST ENTIDADE-CONTRATO* permanece como está e continua sendo a referência de testabilidade; o subtópico **Contratos** aponta para ela por link.

### Key Entities *(include if feature involves data)*

- **Contrato persistido**: o JSON comum (não json-schema) copiado para `src/test/resources/contracts/contract.json` da aplicação gerada; fonte única da verdade; carrega `entityName` e atributos com VALORES (dos quais os tipos são estimados quando não declarados) e pode declarar `type` explícito por atributo, que tem precedência sobre a estimativa; pode conter objetos aninhados, coleções e definições de enum; nunca contém chaves de flag — flags pertencem ao archetype, não ao módulo gerado.
- **Entidade gerada**: a classe Java da entidade no módulo gerado; deve espelhar o contrato; contém sempre o campo técnico de identidade.
- **Campo técnico de identidade** (`<entidade>Id`, sempre `Long`): gerado incondicionalmente pelo archetype; corresponde ao atributo `id` do contrato; excluído da comparação entidade→contrato.
- **Tipos aninhados derivados**: POJOs gerados a partir de objetos do contrato (subpacote `nested`); validados recursivamente.
- **Enums derivados**: enums gerados a partir do contrato; constantes conferidas contra a definição contratual.
- **Schema test (classe de teste gerada)**: o artefato da Frente 01; vive em `src/test/java` da aplicação gerada; existe somente sob a condição de FR-001; executa no ciclo de testes da aplicação, pelo resto da vida dela.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% das aplicações geradas com contrato persistido e árvore de testes habilitada contêm a classe do schema test, e ela executa verde no primeiro ciclo de testes da aplicação, sem nenhuma intervenção manual.
- **SC-002**: 100% das adulterações controladas de drift (campo removido, renomeado, tipo alterado, campo clandestino — nos dois sentidos, contrato e entidade) produzem falha do schema test na execução dos testes da aplicação, com mensagem que nomeia o atributo e o sentido da divergência.
- **SC-003**: 100% das aplicações geradas sem contrato persistido (ou com árvore de testes desativada) não contêm a classe do schema test e mantêm build e testes verdes.
- **SC-004**: Os 22 perfis de integração existentes do archetype permanecem verdes (22/22) após a entrega, incluindo o perfil `entity-contract` executando o schema test verde dentro da aplicação gerada.
- **SC-005**: O veredito do schema test é idêntico em 100% das execuções comparadas entre geração com Lombok ativado e desativado.
- **SC-006**: Zero dependências novas no `pom.xml` da aplicação gerada (verificável por comparação do POM gerado antes/depois da feature).
- **SC-007**: 100% das execuções com contrato ilegível ou JSON inválido produzem falha estrutural explícita (0 skips silenciosos); a ausência do arquivo mantém o mesmo comportamento por construção, sem teste dedicado.
- **SC-008**: 100% dos cenários de múltiplas divergências simultâneas são reportados em uma única execução/falha, com todos os itens presentes e em ordem determinística.
- **SC-009**: A prova IT com contrato de 7 níveis e enums permanece verde, e adulterar qualquer nível — inclusive a folha mais profunda — falha com o caminho hierárquico completo.

## Assumptions

- O mecanismo de inferência (LLM + flags derivadas) é tratado como caixa-preta estável: o schema test CONSOME o contrato persistido e replica apenas as regras já praticadas hoje de leitura de formas e precedência (`attributes` > `fields` > topo), resolução de tipo (tipo declarado com precedência, estimativa pelo valor como fallback) e normalização de nomes; nenhuma semântica nova é inventada para arrays/coleções — o comportamento segue as regras atuais do mecanismo.
- Embora o projeto de inferência garanta que o contrato sempre existirá em gerações reais (os prompts impedem sua inexistência), os caminhos negativos (ausente/`none`/inválido/árvore de testes desativada) são cobertos como garantia estrutural de robustez (US3).
- O contrato vive exclusivamente em `src/test/resources` da aplicação gerada e não segue para o classpath de produção; portanto a fiscalização existe apenas no ciclo de testes (decisão já tomada, fora de escopo qualquer validação em runtime).
- "Schema test" é o nome de trabalho do artefato; o nome definitivo da classe gerada é decisão de implementação (fase de planejamento), desde que estável e documentado.
- Flags pertencem exclusivamente ao archetype (entrada de geração); o contrato persistido no módulo gerado não carrega chaves de flag, e o schema test não depende do catálogo de flags para interpretar o contrato.
- A sabotagem da Frente 02 é um instrumento de teste do próprio archetype (entidade ou contrato adulterados na aplicação gerada em ambiente de teste); nenhuma adulteração é persistida em fixtures usadas pelos demais perfis.
