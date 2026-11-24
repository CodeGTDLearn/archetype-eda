# Implementation Plan: Paradas Duras Claras, Completas e em Linguagem Simples

**Branch**: `007-hard-stop-contract` | **Date**: 2026-11-24 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/007-hard-stop-contract/spec.md`

## Summary

Adicionar ao procedimento empacotado um Contrato de Parada Dura canônico, com 10 campos, regras de linguagem simples, dicionário de 20 termos, registro de 16 paradas + 2 confirmações e um código em cada site de parada. Substituir o fallback `String:name` por `STOP-DER-02` quando a lista derivada de atributos violar a regra de formato. Corrigir as divergências documentais — caminho absoluto do contrato, exceção de proveniência para `none`, pipes da tabela, nota FR-006 e diferença entre pacote da entidade e artefato — e provar o contrato com suítes Groovy. Nenhum arquivo novo no procedimento, nenhuma dependência nova, nenhum `defaultValue` ou `validationRegex` alterado.

## Technical Context

**Language/Version**: Markdown (promptware do procedimento); Groovy 4.0.28 (suítes de contrato); Java 17 e Maven 3.9.16 (build)

**Primary Dependencies**: Maven Archetype Plugin 3.4.1; xml-maven-plugin 1.2.1; groovy-maven-plugin 2.1.1; groovy-json 4.0.28. Nenhuma dependência nova.

**Storage**: Sistema de arquivos e JAR: 19 arquivos do procedimento (13 Markdown e 6 JSON), catálogo XML com 35 propriedades e 18 filesets; sem banco de dados.

**Testing**: Suítes Groovy em `src/test/archetype-post-generate/`, executadas pelo runner `src/test/groovy/run-archetype-post-generate-tests.groovy` na fase `test`; `mvn -B test`, `mvn clean install` e verificação opt-in do JAR instalado com `-Deda.verify.installed.jar=true`.

**Target Platform**: JAR consumido por agentes e operadores; Windows e POSIX; procedimento sem motor, CLI ou script em runtime.

**Project Type**: Maven archetype / promptware (documentação executável + suítes de contrato).

**Performance Goals**: Aumento do build de no máximo 10% (NFR-001); suítes determinísticas, offline e sem dependência de rede.

**Constraints**: 19 arquivos no procedimento (13 Markdown, 6 JSON); 35 propriedades e 18 filesets; não alterar `defaultValue` nem `validationRegex`; não criar arquivo novo no procedimento; mensagens em português; não expor segredos; o JAR instalado é a fonte de runtime.

**Scale/Scope**: 16 paradas + 2 confirmações; 20 termos no dicionário; 9 flags críticas; 10 dos 13 Markdown alterados; 1 comentário de catálogo; 2 suítes de teste novas.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Princípio | Gate aplicável | Status |
|---|---|---|
| I. Modularidade por Chave de Ativação | Nenhuma flag, capacidade opcional ou comportamento condicional novo; a mudança é promptware e contrato de mensagem | PASS |
| II. Convenção Fixa de Nomenclatura | Os 19 nomes e as 4 subpastas do procedimento permanecem; nenhum artefato de extensão do archetype é criado | PASS |
| III. Pacote por Funcionalidade | Nenhum código Java gerado ou template de entidade é alterado | PASS |
| IV. Binding Duplo de Configuração | Nenhuma propriedade externalizada, variável de binding ou configuração é criada ou alterada | PASS |
| V. Credenciais Nunca no Repositório | As mensagens proíbem segredos e o teste não usa credenciais; nenhum valor sensível é impresso | PASS |
| VI. Validar Antes de Culpar o Código | A validação reinstala o JAR com `mvn clean install` e confere o artefato instalado com a propriedade opt-in | PASS |
| Technology Stack | Java 17, Maven 3.9.16, Spring Boot 4.0.0, plugin 3.4.1 e dependências existentes permanecem | PASS |

**Reavaliação após a Fase 1**: PASS — o desenho não cria flag, arquivo de extensão, dependência, estrutura de dados ou alteração no código gerado. As alterações fora do procedimento são um comentário descritivo no fragmento `property-20` do catálogo (sem tocar em `defaultValue` ou `validationRegex`) e duas suítes de teste.

## Project Structure

### Documentation (this feature)

```text
specs/007-hard-stop-contract/
├── plan.md                         # Este arquivo
├── research.md                     # Fase 0
├── data-model.md                   # Fase 1
├── quickstart.md                   # Fase 1
├── contracts/
│   ├── hard-stop-message.md        # Contrato dos 10 campos e da linguagem
│   ├── stop-registry.md            # Registro, sites e códigos
│   └── catalog-sync.md             # Tabela de flags × catálogo
├── checklists/
│   └── requirements.md             # Checklist de qualidade
└── tasks.md                        # Fase 2 (/speckit.tasks)
```

### Source Code (repository root)

```text
src/main/resources/llm-maven-flags-inference/                  # 19 arquivos; nenhum novo
├── 01-entrada-inicializacao/                                  # 2 arquivos
│   ├── prompt-00-leia-este-arquivo-primeiro.md                # ALTERADO: contrato, dicionário, registro e códigos
│   └── indice-01-mapa-dos-arquivos-e-ordem-de-leitura.md      # ALTERADO: STOP-ENV-02
├── 02-operacao-procedimento-do-agente/                        # 5 arquivos
│   ├── prompt-01-papel-e-restricoes-do-agente.md              # ALTERADO: STOP-RULE-01
│   ├── prompt-02-como-localizar-e-ler-o-archetype.md          # ALTERADO: STOP-ENV-01/02/03/04/05
│   ├── prompt-03-regras-obrigatorias-de-geracao.md            # ALTERADO: pacote × artefato e STOP-IN-02/04, STOP-DER-04
│   ├── prompt-04-fluxo-de-execucao-e-verificacao.md           # ALTERADO: checklist, confirmações e STOP-EXEC-01
│   └── prompt-05-entregaveis-e-relatorios.md                  # SEM ALTERAÇÃO
├── 03-derivacao-regras-de-formacao-do-contrato/               # 6 arquivos
│   ├── regras-01-como-derivar-a-entidade-do-contrato.md       # ALTERADO: STOP-DER-01/02
│   ├── regras-02-como-resolver-tipos-e-enums.md               # SEM ALTERAÇÃO (avisos preservados)
│   ├── regras-03-como-derivar-flags-de-tipos-aninhados.md     # SEM ALTERAÇÃO (avisos preservados)
│   ├── regras-04-como-aplicar-precedencia-e-validacao.md      # ALTERADO: pipes, FR-006, STOP-DER-02/03
│   ├── regras-05-como-materializar-e-emitir-o-contrato.md     # ALTERADO: STOP-DER-04
│   └── regras-06-como-relatar-proveniencia.md                 # ALTERADO: exceção de proveniência e STOP-DER-02
└── 04-referencia-templates-de-contrato/                       # 6 JSON, SEM ALTERAÇÃO

src/main/resources/META-INF/maven/archetype_metadata_required_properties/
└── property-20-entity_contract_file_path.xml                  # ALTERADO: comentário; default e regex intactos

src/test/archetype-post-generate/
├── HardStopContractTest.groovy                                # NOVO: registro, mensagens, dicionário e sites
└── CatalogSyncContractTest.groovy                             # NOVO: tabela × catálogo e divergências documentais

src/test/groovy/run-archetype-post-generate-tests.groovy       # ALTERADO: registra as 2 suítes novas
```

**Structure Decision**: mantém o módulo único existente. A feature não cria módulo de produção, dependência ou estrutura de dados. O contrato de parada e o dicionário vivem no `prompt-00`, que já é o ponto de entrada do procedimento; os 19 arquivos e as 4 subpastas permanecem.

## Fase 0 — Research

Ver [research.md](./research.md) para as decisões e alternativas descartadas.

## Fase 1 — Design

Ver [data-model.md](./data-model.md), [contracts/](./contracts/) e [quickstart.md](./quickstart.md).

## Implementation Strategy

### 1. Contrato de Parada Dura no ponto de entrada

`prompt-00` passa a ter três seções novas depois dos Gates:

- `0.3 Contrato de Parada Dura`: os 10 campos, as regras de linguagem simples (uma ideia por frase, até 20 palavras, voz ativa, sigla traduzida, seção legível + caminho, linguagem neutra), o template canônico e a linha de segurança.
- `0.4 Dicionário de linguagem simples`: os 20 termos com tradução, acessíveis no ponto de entrada.
- `0.5 Registro de paradas`: tabela canônica dos 16 códigos + 2 confirmações e as 18 mensagens completas, cada uma com os 10 campos, pelo menos duas opções e a referência ao dicionário.

O texto "Ao parar, informe apenas" é removido. Cada Gate e cada condição de parada cita o código correspondente e aponta para a mensagem de `§0.5`.

### 2. Códigos nos sites de parada

| Código | Gatilho | Site principal |
|---|---|---|
| `STOP-IN-01` | use-case ausente, em branco ou sem marcador | `prompt-00` Gate A |
| `STOP-IN-02` | contrato ausente, em branco, `{}` ou não-objeto | `prompt-00` Gate B; `regras-01` §2; `prompt-03` §2 |
| `STOP-IN-03` | contrato antes do use-case | `prompt-00` Gate C |
| `STOP-IN-04` | destino ausente, relativo, com `~`, com fim de arquivo ou dentro do archetype | `prompt-00` Gate D; `prompt-03` §3; `prompt-04` §1.3 |
| `STOP-IN-05` | package base ausente ou ambíguo | `prompt-00` §6; `prompt-03` §1 |
| `STOP-ENV-01` | JAR instalado não encontrado | `prompt-02` §1 |
| `STOP-ENV-02` | arquivo obrigatório do procedimento ou árvore obrigatória ausente | `indice-01` §5; `prompt-00` §8; `prompt-02` §6 |
| `STOP-ENV-03` | catálogo ausente ou ilegível | `prompt-00` §6; `prompt-02` §6 |
| `STOP-ENV-04` | pós-processador ausente | `prompt-00` §6; `prompt-02` §6 |
| `STOP-ENV-05` | várias versões instaladas e o spec não declara qual usar | `prompt-02` §1.4 |
| `STOP-DER-01` | nome de entidade ambíguo | `regras-01` §2 |
| `STOP-DER-02` | lista de atributos derivada inválida | `regras-01` §6; `regras-04` §3; `prompt-04` §1.2; `regras-06` |
| `STOP-DER-03` | duas fontes de mesma prioridade divergem | `regras-04` §7; `prompt-00` §6 |
| `STOP-DER-04` | caminho do contrato não absoluto ou falha ao gravar | `regras-05` §5/§7; `prompt-03` §2; `prompt-04` §1.4 |
| `STOP-EXEC-01` | falha de catálogo ou de pós-processador durante a geração | `prompt-04` §4 |
| `STOP-RULE-01` | o procedimento não cobre um caso | `prompt-01` §4; `prompt-00` §6 |
| `STOP-CONF-01` | pasta de destino ainda não existe | `prompt-04` §1.3 |
| `STOP-CONF-02` | já existe módulo com o mesmo nome | `prompt-04` §1.3 |

A convenção de site é literal: toda ordem de interrupção usa a forma **Pare com `STOP-...`**. O teste varre as linhas com a palavra isolada `pare` e os blocos `PARADA DURA`, e exige um código registrado. A ordem A → B → C → D dos Gates é preservada; os códigos são anexados aos gates existentes. A introdução de `§6` é reescrita para que cada bullet comece com **Pare com `STOP-...`**, sem linha solta com `Pare`.

### 3. Lista de atributos inválida

Em `regras-01` §6, o parágrafo que hoje cai no `defaultValue` `String:name` é substituído por:

- validar a lista final contra a regex do catálogo;
- se violar, parar com `STOP-DER-02`, sem emitir a command line e sem gerar módulo;
- registrar a rejeição na proveniência;
- manter as regras atuais de normalização e descarte de nomes inválidos (FR-024); a parada é da lista final, não de cada aviso individual.

`regras-04` §3 ganha a exceção explícita: `custom_entity_attributes_list` derivada e inválida não aplica default; as demais flags continuam com o fallback atual. O default `String:name` permanece no catálogo e continua sendo o valor inerte quando não há atributos; ele não é fallback de lista inválida.

### 4. Divergências documentais

- `property-20-entity_contract_file_path.xml`: comentário passa a dizer que o procedimento exige caminho absoluto; `defaultValue` `none` e `validationRegex` permissiva permanecem.
- `regras-06` §2: exceção explícita para defaults que uma regra específica manda registrar, incluindo `contract_nested_type_definitions` = `none`.
- `regras-04` §3.1: pipes internos escapados (`\|`) na tabela de 9 flags; a tabela passa a renderizar exatamente três colunas.
- `regras-04` §3: nota explica `FR-006` em linguagem simples, sem remeter a documento externo.
- `prompt-03` §2.1: texto explícito de que `entity_package_name` remove separadores (`PedidoItem` → `pedidoitem`) e `artifactId` usa kebab-case (`PedidoItem` → `pedido-item`).

### 5. Testes

Duas suítes novas, registradas no runner:

- `HardStopContractTest.groovy`: lê a cópia empacotada em `target/classes`; valida os 18 códigos, o registro, as 18 mensagens, os 10 campos, as opções, o template, o dicionário de 20 termos, a ausência de "informe apenas", os sites com `Pare` e os blocos `PARADA DURA`, as contagens 19/13/6, o limite de 20 palavras por frase e, de forma opt-in, o JAR instalado.
- `CatalogSyncContractTest.groovy`: lê `target/classes/META-INF/maven/archetype-metadata.xml` e os fragmentos; valida as 3 colunas da tabela, os 9 defaults, as 3 regras de formato literais, os exemplos das 6 regras descritivas, a descrição de caminho absoluto, a exceção de proveniência, a nota FR-006, a diferença pacote × artefato e a ausência de mudança em `defaultValue`/`validationRegex` (35/18).

### 6. Fora de alteração

- `README.md`: permanece como está; a observação do contexto não reabre a reestruturação.
- `prompt-05`, `regras-02`, `regras-03` e os 6 JSON: sem alteração.
- Nenhum aviso vira parada dura além da lista de atributos.
- Nenhuma flag, fileset, dependência ou versão é criada ou alterada.

## Validation Plan

### Positivo

```powershell
mvn -B test
mvn clean install
mvn -B test "-Deda.verify.installed.jar=true"
```

Esperado: todas as suítes passam; o JAR instalado contém o registro, o dicionário e a mensagem `STOP-DER-02`.

### Negativo (temporário, reverter depois)

| Alteração provocada | Falha esperada |
|---|---|
| Remover um código de um site com `Pare` ou de um bloco `PARADA DURA` | `HardStopContractTest` nomeia o arquivo e o código ausente |
| Adicionar um `STOP-*` sem linha no registro | `HardStopContractTest` nomeia o código não registrado |
| Remover uma mensagem do registro | `HardStopContractTest` nomeia o código sem mensagem |
| Remover um termo do dicionário | `HardStopContractTest` nomeia o termo ausente |
| Inserir "informe apenas" no procedimento | `HardStopContractTest` falha |
| Mudar um default na tabela de flags críticas | `CatalogSyncContractTest` nomeia a flag |
| Mudar uma regex literal na tabela | `CatalogSyncContractTest` nomeia a flag |
| Mudar a descrição de caminho absoluto do catálogo | `CatalogSyncContractTest` falha |
| Adicionar um 20º arquivo ao procedimento | A suíte de contagem falha |

### Manual

1. Extrair o JAR instalado e abrir `llm-maven-flags-inference/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md`.
2. Conferir o template, os 20 termos e as 18 mensagens.
3. Provocar `STOP-DER-02` com um contrato de lista inválida e confirmar: nenhuma linha `mvn`, nenhum módulo, 3 opções e a sugestão.
4. Confirmar que a mensagem não imprime o contrato completo nem segredos.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

Nenhuma violação. O gate da Constitution passou antes e depois do desenho; a tabela de complexidade fica vazia.

## Definition of Done

- 16 paradas + 2 confirmações com código, linha no registro e mensagem de 10 campos.
- Dicionário com os 20 termos e traduções, referenciado por todas as mensagens.
- `STOP-DER-02` com 3 opções, sem `String:name` como fallback e sem command line.
- Tabela de flags críticas com 3 colunas e 9 defaults sincronizados com o catálogo.
- Caminho absoluto documentado no catálogo, proveniência com exceção de `none`, FR-006 explicado e pacote × artefato explícito.
- 19 arquivos (13 Markdown, 6 JSON), 35 propriedades e 18 filesets.
- 0 ocorrências de "informe apenas".
- Um operador que começou ontem responde às 5 perguntas de orientação em 100% das paradas revisadas (SC-002, revisão manual).
- `mvn -B test`, `mvn clean install` e a verificação opt-in do JAR passam.
- Nenhum arquivo novo no procedimento, nenhuma dependência nova, nenhum default ou regex alterado.
