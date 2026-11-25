# Implementation Plan: Renomear os Arquivos das Subpastas 03 e 04 do Procedimento

**Branch**: `009-rename-procedure-files` | **Date**: 2026-11-25 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/008-rename-procedure-files/spec.md`

## Summary

Renomear os 12 arquivos das subpastas `03-derivacao-regras-de-formacao-do-contrato` e `04-referencia-templates-de-contrato`, removendo os prefixos `regras-` e `template-` e preservando o restante do nome e o conteúdo normativo. Atualizar todas as referências operacionais — índice, prompts, regras, exemplos JSON, README e suítes de teste — para os novos nomes, sem criar nem remover arquivos do procedimento. A implementação mantém 19 arquivos (13 Markdown e 6 JSON) em 4 subpastas, não altera o catálogo nem `archetype-resources/`, não adiciona dependências e acrescenta uma suíte de contrato para provar a renomeação e a ausência de referências antigas. A validação usa `mvn -B test`, `mvn clean install`, a verificação opt-in do JAR instalado e uma revisão de diff que prova que apenas os tokens de referência mudaram.

## Technical Context

**Language/Version**: Markdown e JSON (procedimento promptware); Groovy 4.0.28 (suítes de contrato); Java 17 (constitution) e JDK 21 no CI; Maven 3.9.16. O `pom.xml` não fixa `maven.compiler.release`.

**Primary Dependencies**: Maven Archetype Plugin 3.4.1; archetype-packaging 3.4.1; xml-maven-plugin 1.2.1; groovy-maven-plugin 2.1.1; groovy-json 4.0.28; JUnit 4.13.2; Velocity 2.4.1 (test). Nenhuma dependência nova é necessária.

**Storage**: Sistema de arquivos e JAR; 19 arquivos do procedimento (13 Markdown e 6 JSON) em 4 subpastas; catálogo XML com 35 propriedades e 18 filesets; sem banco de dados.

**Testing**: Suítes Groovy em `src/test/archetype-post-generate/`, executadas pelo runner `src/test/groovy/run-archetype-post-generate-tests.groovy` na fase `test`; `mvn -B test` e `mvn clean install`; CI com `mvn -B clean install "-Darchetype.test.skip=true"` e `mvn -B clean integration-test`; verificação opt-in do JAR instalado com `-Deda.verify.installed.jar=true`.

**Target Platform**: JAR consumido por agentes e operadores; Windows e POSIX; procedimento sem motor, CLI ou script em runtime.

**Project Type**: Maven archetype / promptware (documentação executável + suítes de contrato).

**Performance Goals**: A feature não define meta própria. Preservar o orçamento de build da feature 007 — aumento máximo de 10% sobre a linha de base local de `mvn -B test` — como guarda de regressão; ele não é gate de aceite desta feature.

**Constraints**: 19 arquivos no procedimento (13 Markdown e 6 JSON), 4 subpastas, 6 arquivos em 03 e 04; conteúdo normativo dos 12 arquivos preservado; apenas referências textuais aos nomes antigos atualizadas; não criar nem remover arquivos; não renomear subpastas; não alterar regras, catálogo, estrutura de templates, ordem de leitura ou formato do contrato; não expor segredos; o JAR instalado é a fonte de runtime.

**Scale/Scope**: 12 arquivos renomeados; 18 arquivos operacionais com referências aos nomes antigos (11 arquivos do procedimento, `README.md` e 6 suítes de teste); 1 suíte de contrato nova; 0 arquivos novos no procedimento; 0 dependências novas.

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
specs/008-rename-procedure-files/
├── plan.md                         # Este arquivo
├── research.md                     # Fase 0: decisões e alternativas
├── data-model.md                   # Fase 1: entidades e invariantes
├── quickstart.md                   # Fase 1: roteiro de validação
├── contracts/
│   └── rename-manifest.md          # Mapa old→new, tokens e inventário de referências
└── checklists/
    └── requirements.md             # Checklist de qualidade da spec
```

### Source Code (repository root)

```text
src/main/resources/llm-maven-flags-inference/
├── 01-entrada-inicializacao/
│   ├── prompt-00-leia-este-arquivo-primeiro.md                 # ALTERADO: referências curtas e lista dos 19 arquivos
│   └── indice-01-mapa-dos-arquivos-e-ordem-de-leitura.md       # ALTERADO: 12 caminhos e 2 referências curtas
├── 02-operacao-procedimento-do-agente/
│   ├── prompt-01-papel-e-restricoes-do-agente.md               # ALTERADO: `regras-01` → `01`
│   ├── prompt-02-como-localizar-e-ler-o-archetype.md           # ALTERADO: `regras-01` → `01`
│   ├── prompt-03-regras-obrigatorias-de-geracao.md             # SEM ALTERAÇÃO
│   ├── prompt-04-fluxo-de-execucao-e-verificacao.md            # ALTERADO: `regras-06`, `regras-01`, `regras-05` → `06`, `01`, `05`
│   └── prompt-05-entregaveis-e-relatorios.md                   # SEM ALTERAÇÃO
├── 03-derivacao-regras-de-formacao-do-contrato/
│   ├── 01-como-derivar-a-entidade-do-contrato.md               # RENOMEADO de regras-01; SEM referência antiga
│   ├── 02-como-resolver-tipos-e-enums.md                       # RENOMEADO de regras-02; ALTERADO: `regras-03` → `03`
│   ├── 03-como-derivar-flags-de-tipos-aninhados.md             # RENOMEADO de regras-03; SEM referência antiga
│   ├── 04-como-aplicar-precedencia-e-validacao.md              # RENOMEADO de regras-04; ALTERADO: `regras-01` → `01`
│   ├── 05-como-materializar-e-emitir-o-contrato.md             # RENOMEADO de regras-05; SEM referência antiga
│   └── 06-como-relatar-proveniencia.md                         # RENOMEADO de regras-06; ALTERADO: `regras-01` → `01`
└── 04-referencia-templates-de-contrato/
    ├── 01-schema-do-contrato.json                              # RENOMEADO de template-01; ALTERADO: `regras-01` → `01`
    ├── 02-exemplo-contrato-plano.json                          # RENOMEADO de template-02; ALTERADO: `regras-01` → `01`
    ├── 03-exemplo-contrato-com-objetos-aninhados.json          # RENOMEADO de template-03; SEM referência antiga
    ├── 04-exemplo-contrato-com-enums.json                      # RENOMEADO de template-04; SEM referência antiga
    ├── 05-exemplo-contrato-com-tipos-do-projeto.json           # RENOMEADO de template-05; SEM referência antiga
    └── 06-exemplo-contrato-com-attributes-explicito.json       # RENOMEADO de template-06; ALTERADO: `regras-01`, `regras-02` → `01`, `02`

README.md                                                        # ALTERADO: referências curtas e tabelas de arquivos

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

**Structure Decision**: manter o módulo único existente. A feature não cria módulo de produção, dependência ou estrutura de dados; renomeia arquivos no lugar, atualiza referências no corpus operacional e acrescenta uma suíte de contrato. O catálogo, `archetype-resources/`, `pom.xml` (fora de comentários que não citam nomes de arquivo) e as specs históricas permanecem intocados.

## Implementation Strategy

### 1. Baseline e manifesto

- Copiar os 12 arquivos para um diretório temporário fora do repositório e registrar SHA-256 antes de qualquer mudança.
- Confirmar a linha de base: 19 arquivos, 13 Markdown, 6 JSON, 4 subpastas, 6 arquivos em 03 e 04.
- Materializar `contracts/rename-manifest.md` com o mapa old→new, o mapa de tokens curtos e o inventário de referências por arquivo e linha.
- Confirmar que os 12 nomes novos não existem e que nenhum arquivo será sobrescrito.

### 2. Renomeação dos arquivos

- Renomear os 6 arquivos da subpasta 03 e os 6 da subpasta 04 com `Rename-Item` (não copiar e apagar), preservando conteúdo, codificação e line endings.
- Renomear em ordem crescente de numeral; abortar na primeira colisão sem aplicar o restante da subpasta.
- Verificar imediatamente: 6 arquivos novos em cada subpasta, 0 nomes antigos, 19 arquivos no total.
- Não tocar nos nomes das subpastas, no catálogo, em `archetype-resources/` ou em specs históricas.

### 3. Atualização das referências operacionais

- Aplicar o mapa de tokens e nomes completos do manifesto nos 18 arquivos operacionais: 11 arquivos do procedimento, `README.md` e 6 suítes de teste.
- Referências curtas: `regras-NN` e `template-NN` viram `NN`; preservar `§`, pontuação, tabelas e links de diretório.
- Nomes completos: substituir o nome antigo inteiro pelo novo, incluindo extensão, em listas, tabelas e caminhos de teste.
- JSON: alterar apenas os valores de `description` que citam nomes antigos; não alterar chaves, schema, tipos, exemplos de domínio ou estrutura.
- README: atualizar as tabelas e o texto; manter os links para os diretórios `03-.../` e `04-.../`, que não mudam.
- Testes: atualizar caminhos e rótulos de `check`; atualizar comentários que citam nomes antigos; registrar a nova suíte no runner.
- Excluir da substituição `specs/**`, `graphify-out/**`, `target/**` e `.git/**`.

### 4. Suíte de contrato da renomeação

Adicionar `FileRenameContractTest.groovy` e registrá-la no runner. A suíte deve provar:

- os 6 nomes novos em cada subpasta, na árvore-fonte e em `target/classes`;
- 0 nomes antigos nas duas subpastas;
- 19 arquivos no total, 13 Markdown e 6 JSON, 4 subpastas;
- o índice lista os 12 caminhos novos e nenhum caminho antigo;
- o corpus operacional (procedimento, README e suítes de teste, excluindo a própria suíte, `specs/` e `graphify-out/`) não contém `regras-0[1-6]` nem `template-0[1-6]`;
- o catálogo continua com 35 propriedades e 18 filesets;
- a suíte está registrada no runner.

### 5. Build, testes e validação

- `mvn -B test` — todas as suítes, incluindo a nova.
- `mvn clean install` — recompõe o catálogo, empacota o JAR e executa as suítes.
- `mvn -B test "-Deda.verify.installed.jar=true"` — verificação opt-in do JAR instalado.
- `mvn -B clean integration-test` — paridade com o CI.
- Revisão manual do diff: para os 6 arquivos renomeados que continham referências, as únicas diferenças permitidas são os tokens de referência; para os outros 6, o hash deve ser idêntico ao baseline.

### 6. Guardrails

- Não criar nem remover arquivos do procedimento.
- Não renomear as subpastas 03 e 04.
- Não alterar regras, catálogo, estrutura de templates, ordem de leitura ou formato do contrato.
- Não reescrever `specs/**`, `graphify-out/**` ou `target/**`.
- Não adicionar dependências, flags ou arquivos de extensão.
- Não executar git fora dos hooks declarados.

## Validation Plan

### Positivo

```powershell
# contagens e nomes
(Get-ChildItem "src/main/resources/llm-maven-flags-inference" -Recurse -File).Count   # 19
(Get-ChildItem "src/main/resources/llm-maven-flags-inference" -Recurse -File -Filter *.md).Count # 13
(Get-ChildItem "src/main/resources/llm-maven-flags-inference" -Recurse -File -Filter *.json).Count # 6
Get-ChildItem "src/main/resources/llm-maven-flags-inference/03-derivacao-regras-de-formacao-do-contrato" -File
Get-ChildItem "src/main/resources/llm-maven-flags-inference/04-referencia-templates-de-contrato" -File

# referências antigas no corpus operacional (esperado: 0 linhas)
Get-ChildItem README.md, "src/main/resources/llm-maven-flags-inference", "src/test" -Recurse -File |
  Where-Object { $_.FullName -notmatch '\\specs\\|\\graphify-out\\|\\target\\|FileRenameContractTest' } |
  Select-String -Pattern 'regras-0[1-6]|template-0[1-6]'

# build e testes
mvn -B test
mvn clean install
mvn -B clean integration-test
mvn -B test "-Deda.verify.installed.jar=true"   # opcional, após install
```

### Negativo (verificações que devem falhar se a feature regredir)

| Alteração provocada | Falha esperada |
|---|---|
| Deixar um arquivo com nome antigo | `FileRenameContractTest` nomeia o arquivo |
| Deixar uma referência antiga no procedimento | `FileRenameContractTest` nomeia o arquivo e a linha |
| Criar ou remover um arquivo do procedimento | `FileRenameContractTest` falha na contagem 19/13/6 |
| Renomear uma subpasta | `FileRenameContractTest` e `PathRenameContractTest` falham |
| Alterar uma regra, chave JSON ou estrutura de template | `ProcedureContractTest`, `CatalogSyncContractTest` ou a revisão de diff falham |
| Não registrar a nova suíte no runner | `CoverageRegistryTest` e `ReadmeConsistencyTest` falham |

### Métricas

| Métrica | Procedimento | Aceite |
|---|---|---|
| Arquivos renomeados | Listar as duas subpastas | 12/12 novos nomes; 0 antigos |
| Conteúdo normativo | Comparar com o baseline; revisar diff | 6/6 sem referências idênticos; 6/6 com referências com apenas tokens alterados |
| Contagem | `Get-ChildItem` recursivo | 19 arquivos; 13 Markdown; 6 JSON; 4 subpastas |
| Referências antigas | `Select-String` no corpus operacional | 0 ocorrências |
| Suítes | `mvn -B test` | 100% das suítes passam, incluindo a nova |
| Artefato | `mvn clean install` e verificação opt-in | JAR instalado contém os 12 nomes novos e nenhum antigo |

## Complexity Tracking

Nenhuma violação. Não há entradas nesta tabela.

## Risks and Mitigations

| Risco | Mitigação |
|---|---|
| Uma referência antiga escapar | Manifesto com inventário por arquivo/linha + `FileRenameContractTest` |
| O mesmo token aparecer em contexto não referencial | Substituir apenas tokens `regras-NN`/`template-NN` e nomes completos; revisar o diff |
| Um teste existente apontar para o nome antigo | Atualizar as 6 suítes e o runner; rodar `mvn -B test` |
| Conteúdo normativo mudar junto com a referência | Baseline + revisão de diff; apenas tokens podem diferir |
| JAR instalado ficar obsoleto | `mvn clean install` antes da verificação opt-in |
| Specs históricas ou `graphify-out/` serem reescritos | Exclusão explícita no manifesto e na varredura |
| Colisão ou sobrescrita de nome | Pré-checagem de existência; abortar a subpasta antes de aplicar |
| README com link quebrado | Manter os links de diretório; só substituir nomes de arquivo |
