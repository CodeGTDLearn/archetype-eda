# Implementation Plan: Renomear os Arquivos das Subpastas 03 e 04 do Procedimento

**Branch**: `010-rename-procedure-files` | **Date**: 2026-11-25 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/009-rename-procedure-files/spec.md`

## Summary

Renomear os 12 arquivos das subpastas `03-derivacao-regras-de-formacao-do-contrato` e `04-referencia-templates-de-contrato`, removendo os prefixos `regras-` e `template-` e preservando o restante do nome e o conteúdo normativo. Atualizar todas as referências operacionais — índice, prompts, regras, exemplos JSON, README e suítes de teste — para os novos nomes, sem criar nem remover arquivos do procedimento. A implementação é dividida nas duas fases da spec: a Fase 1 renomeia a subpasta 03 e atualiza suas referências; a Fase 2 renomeia a subpasta 04, atualiza suas referências e conclui o README e a varredura final. O procedimento mantém 19 arquivos (13 Markdown e 6 JSON) em 4 subpastas; o catálogo e `archetype-resources/` não mudam; nenhuma dependência nova é adicionada. A validação usa `mvn -B test`, `mvn clean install`, a verificação opt-in do JAR instalado e uma revisão de diff que prova que apenas os tokens de referência mudaram.

## Technical Context

**Language/Version**: Markdown e JSON (procedimento promptware); Groovy 4.0.28 (suítes de contrato); Java 17 (constitution) e JDK 21 no CI; Maven 3.9.16. O `pom.xml` não fixa `maven.compiler.release`.

**Primary Dependencies**: Maven Archetype Plugin 3.4.1; archetype-packaging 3.4.1; xml-maven-plugin 1.2.1; groovy-maven-plugin 2.1.1; groovy-json 4.0.28; JUnit 4.13.2; Velocity 2.4.1 (test). Nenhuma dependência nova é necessária.

**Storage**: Sistema de arquivos e JAR; 19 arquivos do procedimento (13 Markdown e 6 JSON) em 4 subpastas; catálogo XML com 35 propriedades e 18 filesets; sem banco de dados.

**Testing**: Suítes Groovy em `src/test/archetype-post-generate/`, executadas pelo runner `src/test/groovy/run-archetype-post-generate-tests.groovy` na fase `test`; `mvn -B test` e `mvn clean install`; CI com `mvn -B clean install "-Darchetype.test.skip=true"` e `mvn -B clean integration-test`; verificação opt-in do JAR instalado com `-Deda.verify.installed.jar=true`.

**Target Platform**: JAR consumido por agentes e operadores; Windows e POSIX; procedimento sem motor, CLI ou script em runtime.

**Project Type**: Maven archetype / promptware (documentação executável + suítes de contrato).

**Performance Goals**: A feature não define meta própria. Preservar o orçamento de build da feature 007 — aumento máximo de 10% sobre a linha de base local de `mvn -B test` — como guarda de regressão; ele não é gate de aceite desta feature.

**Constraints**: 19 arquivos no procedimento (13 Markdown e 6 JSON), 4 subpastas, 6 arquivos em 03 e 04; conteúdo normativo dos 12 arquivos preservado; apenas referências textuais aos nomes antigos atualizadas; referências operacionais no índice, prompts, regras, exemplos, README e suítes de teste; specs históricas, planos/tarefas e saída gerada ficam fora; não criar nem remover arquivos; não renomear subpastas; não alterar regras, catálogo, estrutura de templates, ordem de leitura ou formato do contrato; não expor segredos; o JAR instalado é a fonte de runtime.

**Scale/Scope**: 12 arquivos renomeados em 2 fases; 2 subpastas; 19 arquivos no total (13 Markdown e 6 JSON) em 4 subpastas; 18 arquivos operacionais com referências; 1 suíte de contrato nova; 0 arquivos novos no procedimento; 0 dependências novas.

**Paradigm**: NÃO OOP — a feature é procedural/documental; o projeto é um archetype com scripts Groovy, XML/Markdown e templates Java, sem modelagem OOP no escopo.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Princípio | Gate aplicável | Status |
|---|---|---|
| I. Modularidade por Chave de Ativação | Nenhuma flag, capacidade opcional ou comportamento condicional novo; a mudança é de nomenclatura do procedimento e de suas referências | PASS |
| II. Convenção Fixa de Nomenclatura | Os arquivos do procedimento são promptware empacotado como recurso, não artefatos de extensão selecionados por padrão de build; a estrutura de 4 subpastas e a contagem de 19 arquivos permanecem, e os novos nomes seguem a numeração `01`–`06` da própria subpasta | PASS |
| III. Pacote por Funcionalidade no Código Gerado | Nenhum código Java gerado ou template de entidade é alterado | PASS |
| IV. Binding Duplo de Configuração Externalizada | Nenhuma propriedade externalizada, variável de binding ou configuração é criada ou alterada | PASS |
| V. Credenciais Nunca Entram no Repositório | As alterações são nomes e referências; nenhum segredo, credencial ou valor sensível é adicionado ou exposto | PASS |
| VI. Validar Antes de Culpar o Código | A validação reinstala o artefato com `mvn clean install` e confere a cópia empacotada; a verificação opt-in do JAR instalado continua disponível | PASS |
| Technology Stack | Java 17, Maven 3.9.16, plugin 3.4.1, Groovy 4.0.28 e dependências existentes permanecem | PASS |

**Reavaliação após a Fase 1**: PASS — o desenho não cria flag, artefato de extensão, dependência, estrutura de dados ou alteração no código gerado. A única adição fora do procedimento é uma suíte de contrato em `src/test/archetype-post-generate/`, registrada no runner; o catálogo, `archetype-resources/` e os 19 arquivos do procedimento permanecem com a mesma contagem.

## Project Structure

### Documentation (this feature)

```text
specs/009-rename-procedure-files/
├── plan.md                         # Este arquivo
├── research.md                     # Fase 0: decisões e alternativas
├── data-model.md                   # Fase 1: entidades, fases e invariantes
├── quickstart.md                   # Fase 1: roteiro de validação por fase
├── contracts/
│   └── rename-manifest.md          # Mapa old→new, tokens e inventário por fase
└── checklists/
    └── requirements.md             # Checklist de qualidade da spec
```

### Source Code (repository root)

```text
src/main/resources/llm-maven-flags-inference/
├── 01-entrada-inicializacao/
│   ├── prompt-00-leia-este-arquivo-primeiro.md                 # ALTERADO: referências de 03 na Fase 1; 04 na Fase 2
│   └── indice-01-mapa-dos-arquivos-e-ordem-de-leitura.md       # ALTERADO: caminhos de 03 na Fase 1; 04 na Fase 2
├── 02-operacao-procedimento-do-agente/
│   ├── prompt-01-papel-e-restricoes-do-agente.md               # ALTERADO na Fase 1: `regras-01` → `01`
│   ├── prompt-02-como-localizar-e-ler-o-archetype.md           # ALTERADO na Fase 1: `regras-01` → `01`
│   ├── prompt-03-regras-obrigatorias-de-geracao.md             # SEM ALTERAÇÃO
│   ├── prompt-04-fluxo-de-execucao-e-verificacao.md            # ALTERADO na Fase 1: `regras-06`, `regras-01`, `regras-05`
│   └── prompt-05-entregaveis-e-relatorios.md                   # SEM ALTERAÇÃO
├── 03-derivacao-regras-de-formacao-do-contrato/
│   ├── 01-como-derivar-a-entidade-do-contrato.md               # FASE 1: renomeado de regras-01; sem referência antiga
│   ├── 02-como-resolver-tipos-e-enums.md                       # FASE 1: renomeado de regras-02; `regras-03` → `03`
│   ├── 03-como-derivar-flags-de-tipos-aninhados.md             # FASE 1: renomeado de regras-03; sem referência antiga
│   ├── 04-como-aplicar-precedencia-e-validacao.md              # FASE 1: renomeado de regras-04; `regras-01` → `01`
│   ├── 05-como-materializar-e-emitir-o-contrato.md             # FASE 1: renomeado de regras-05; sem referência antiga
│   └── 06-como-relatar-proveniencia.md                         # FASE 1: renomeado de regras-06; `regras-01` → `01`
└── 04-referencia-templates-de-contrato/
    ├── 01-schema-do-contrato.json                              # FASE 2: renomeado de template-01; `regras-01` → `01`
    ├── 02-exemplo-contrato-plano.json                          # FASE 2: renomeado de template-02; `regras-01` → `01`
    ├── 03-exemplo-contrato-com-objetos-aninhados.json          # FASE 2: renomeado de template-03; sem referência antiga
    ├── 04-exemplo-contrato-com-enums.json                      # FASE 2: renomeado de template-04; sem referência antiga
    ├── 05-exemplo-contrato-com-tipos-do-projeto.json           # FASE 2: renomeado de template-05; sem referência antiga
    └── 06-exemplo-contrato-com-attributes-explicito.json       # FASE 2: renomeado de template-06; `regras-01`, `regras-02` → `01`, `02`

README.md                                                        # FASE 2: referências de 03 e 04 nas tabelas e na prosa

src/test/archetype-post-generate/
├── BuildArtifactsTest.groovy                                   # ALTERADO: 12 caminhos novos
├── PathRenameContractTest.groovy                               # ALTERADO: 12 caminhos novos
├── ProcedureContractTest.groovy                                # ALTERADO: caminhos e rótulos
├── HardStopContractTest.groovy                                 # ALTERADO: caminho de regras-01
├── DestinationContractTest.groovy                              # ALTERADO: caminhos de regras-01/06 e rótulos
├── CatalogSyncContractTest.groovy                              # ALTERADO: caminhos de regras-04/06 e rótulos
└── FileRenameContractTest.groovy                               # NOVO: invariantes da renomeação e anti-drift

src/test/groovy/run-archetype-post-generate-tests.groovy        # ALTERADO: registra FileRenameContractTest
```

**Structure Decision**: manter o módulo único existente. A feature não cria módulo de produção, dependência ou estrutura de dados; renomeia arquivos no lugar, atualiza referências no corpus operacional e acrescenta uma suíte de contrato. A Fase 1 altera a subpasta 03 e suas referências; a Fase 2 altera a subpasta 04, o README e conclui a varredura. O catálogo, `archetype-resources/`, `pom.xml` e as specs históricas permanecem intocados.

## Implementation Strategy

### Fundação compartilhada

- Copiar os 12 arquivos para um diretório temporário fora do repositório e registrar SHA-256 antes de qualquer mudança.
- Confirmar a linha de base: 19 arquivos, 13 Markdown, 6 JSON, 4 subpastas, 6 arquivos em 03 e 04.
- Materializar `contracts/rename-manifest.md` com o mapa old→new, o mapa de tokens curtos e o inventário de referências por arquivo, linha e fase.
- Criar `FileRenameContractTest.groovy` com as verificações compartilhadas de nomes, contagens e ausência de nomes antigos; registrar a suíte no runner. As asserções específicas de 03 entram na Fase 1; as de 04 e a varredura final entram na Fase 2.
- Confirmar que os 12 nomes novos não existem e que nenhum arquivo será sobrescrito.

### Fase 1 — Renomear os arquivos de derivação (P1)

- Renomear os 6 arquivos da subpasta 03 com `Rename-Item` (não copiar e apagar), em ordem crescente de numeral; abortar na primeira colisão.
- Atualizar as referências aos arquivos de 03 no índice, prompts, regras, exemplos e suítes de teste; não tocar no README nem nos arquivos da subpasta 04.
- Atualizar os caminhos e rótulos das suítes que apontam para os arquivos de 03.
- Validar a Fase 1 isoladamente: 6 nomes novos em 03, 0 `regras-*`, conteúdo normativo preservado, referências de 03 atualizadas, 04 intacta.
- Checkpoint: a Fase 1 deve ser demonstrável sem a Fase 2.

### Fase 2 — Renomear os arquivos de referência (P2)

- Renomear os 6 arquivos da subpasta 04 com `Rename-Item`, em ordem crescente; abortar na primeira colisão.
- Atualizar as referências aos arquivos de 04 no índice, prompts, exemplos e suítes de teste.
- Atualizar o `README.md` para os nomes novos de 03 e 04; manter os links de diretório.
- Concluir a varredura do corpus operacional: 0 referências a `regras-0[1-6]` e `template-0[1-6]`, exceto os padrões negativos da própria `FileRenameContractTest`.
- Validar a Fase 2: 6 nomes novos em 04, 0 `template-*`, 19 arquivos, 13 Markdown, 6 JSON, 4 subpastas, 0 referências antigas.
- Dependência: a Fase 2 depende do estado final dos arquivos compartilhados (índice, prompts, README e testes) e da contagem de 19; a renomeação dos arquivos de 04 é independente da Fase 1.

### Polish

- Revisão final do diff: os 6 arquivos de 03 e os 6 de 04 sem referências devem ter hash idêntico ao baseline; os 6 com referências devem diferir apenas nos tokens mapeados.
- `mvn -B test`, `mvn clean install`, `mvn -B clean integration-test`.
- `mvn -B test "-Deda.verify.installed.jar=true"` — verificação opt-in do JAR.
- Executar os cenários do `quickstart.md` e registrar o resultado.
- Não criar nem remover arquivos do procedimento; não alterar catálogo, `archetype-resources/` ou specs históricas.

## Validation Plan

### Fase 1

```powershell
$procedure = "src/main/resources/llm-maven-flags-inference"
$r03 = Join-Path $procedure "03-derivacao-regras-de-formacao-do-contrato"
Get-ChildItem $r03 -File | Select-Object -ExpandProperty Name
(Get-ChildItem $r03 -File).Count   # 6
```

Esperado: exatamente os 6 nomes novos; 0 `regras-*`; referências de 03 atualizadas; 04 intacta.

### Fase 2

```powershell
$r04 = Join-Path $procedure "04-referencia-templates-de-contrato"
Get-ChildItem $r04 -File | Select-Object -ExpandProperty Name
(Get-ChildItem $procedure -Recurse -File).Count   # 19
(Get-ChildItem $procedure -Recurse -File -Filter *.md).Count # 13
(Get-ChildItem $procedure -Recurse -File -Filter *.json).Count # 6
```

Esperado: exatamente os 6 nomes novos; 0 `template-*`; 19/13/6/4; README atualizado.

### Varredura final

```powershell
Get-ChildItem README.md, $procedure, "src/test" -Recurse -File |
  Where-Object { $_.FullName -notmatch '\\specs\\|\\graphify-out\\|\\target\\|FileRenameContractTest' } |
  Select-String -Pattern 'regras-0[1-6]|template-0[1-6]'
```

Esperado: 0 linhas.

### Build

```powershell
mvn -B test
mvn clean install
mvn -B clean integration-test
mvn -B test "-Deda.verify.installed.jar=true"   # opcional, após install
```

### Métricas

| Métrica | Procedimento | Aceite |
|---|---|---|
| Arquivos renomeados | Listar as duas subpastas | 12/12 novos nomes; 0 antigos |
| Conteúdo normativo | Comparar com o baseline; revisar diff | 6/6 sem referências idênticos; 6/6 com referências com apenas tokens alterados |
| Contagem | `Get-ChildItem` recursivo | 19 arquivos; 13 Markdown; 6 JSON; 4 subpastas |
| Referências antigas | `Select-String` no corpus operacional | 0 ocorrências |
| Suítes | `mvn -B test` | 100% das suítes passam, incluindo a nova |
| Artefato | `mvn clean install` e verificação opt-in | JAR instalado contém os 12 nomes novos e nenhum antigo |

### Negativo (verificações que devem falhar se a feature regredir)

| Alteração provocada | Falha esperada |
|---|---|
| Deixar um arquivo com nome antigo | `FileRenameContractTest` nomeia o arquivo |
| Deixar uma referência antiga no procedimento | `FileRenameContractTest` nomeia o arquivo e a linha |
| Criar ou remover um arquivo do procedimento | `FileRenameContractTest` falha na contagem 19/13/6 |
| Renomear uma subpasta | `FileRenameContractTest` e `PathRenameContractTest` falham |
| Alterar uma regra, chave JSON ou estrutura de template | `ProcedureContractTest`, `CatalogSyncContractTest` ou a revisão de diff falham |
| Não registrar a nova suíte no runner | `CoverageRegistryTest` e `ReadmeConsistencyTest` falham |

## Complexity Tracking

Nenhuma violação. Não há entradas nesta tabela.

## Risks and Mitigations

| Risco | Mitigação |
|---|---|
| Uma referência antiga escapar na Fase 1 | Manifesto por arquivo/linha + `FileRenameContractTest` + varredura final |
| A Fase 1 tocar no README ou em 04 | Checklist de fase: README e 04 intocados até a Fase 2 |
| O mesmo token aparecer em contexto não referencial | Substituir apenas tokens `regras-NN`/`template-NN` e nomes completos |
| Um teste existente apontar para o nome antigo | Atualizar as suítes por fase e o runner; `mvn -B test` |
| Conteúdo normativo mudar junto com a referência | Baseline + revisão de diff; apenas tokens podem diferir |
| JAR instalado ficar obsoleto | `mvn clean install` antes da verificação opt-in |
| Specs históricas ou `graphify-out/` serem reescritos | Exclusão explícita no manifesto e na varredura |
| Colisão ou sobrescrita de nome | Pré-checagem de existência; abortar a subpasta antes de aplicar |
