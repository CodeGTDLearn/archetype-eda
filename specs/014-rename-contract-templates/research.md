# Phase 0 — Research: 014-rename-contract-templates

**Feature**: 014-rename-contract-templates | **Date**: 2026-11-26

## Objetivo da pesquisa

Fechar as decisões que a spec não fixa: como renomear a subpasta 04 e os cinco arquivos sem perder conteúdo, como distinguir referência textual de conteúdo normativo, qual é o inventário completo de referências operacionais, como dividir o trabalho entre as duas fases, como manter as suítes verdes em cada fase, como tratar as âncoras do README, o redirect legado e o artefato empacotado, e se uma suíte nova de contrato é necessária.

## Decisões

### D1 — Renomeação com `Rename-Item`, sem copiar e apagar

- **Decision**: renomear a pasta e cada arquivo com `Rename-Item` no mesmo diretório, após verificar que o destino não existe. Ordem: pasta 04 primeiro (Fase 1); depois os arquivos `02` a `06` em ordem crescente de numeral (Fase 2). Interromper a fase na primeira colisão. Não usar copiar + apagar. Não executar `git mv` fora dos hooks.
- **Rationale**: preserva o arquivo, o conteúdo e os atributos; a operação é auditável. A pré-checagem impede sobrescrita silenciosa. A ordem crescente de numeral evita confusão entre nomes que compartilham prefixo.
- **Alternatives considered**: `git mv` (rejeitada — o processo não deve executar git fora dos hooks); copiar e apagar (rejeitada — risco de perda e de arquivo temporário); renomear em lote sem pré-checagem (rejeitada — sobrescrita silenciosa).

### D2 — Fronteira entre referência textual e conteúdo normativo

- **Decision**: "referência textual" é qualquer ocorrência do nome antigo da pasta ou de um dos cinco nomes antigos de arquivo usada para apontar para a pasta/arquivo: caminho completo, caminho só da pasta, nome curto de arquivo, âncora, título de seção, link, linha de tabela, comentário ou expectativa de teste. "Conteúdo normativo" é todo o restante: regras, definições, chaves, tipos, valores de exemplo e schema JSON. Só o token do nome é substituído; pontuação, tabelas, links e o restante da frase permanecem.
- **Rationale**: a spec exige atualizar todas as referências operacionais sem alterar o conteúdo; sem uma fronteira explícita, uma substituição ampla poderia alterar exemplos ou descrições de papel.
- **Alternatives considered**: substituição global de `exemplo` no repositório (rejeitada — atingiria descrições de papel no README e o texto "Exemplo de contrato plano" do índice, que descrevem o papel e não o nome); não atualizar o índice (rejeitada — o mapa deixaria de listar os arquivos reais).

### D3 — Manifesto único de renomeação e referências

- **Decision**: `contracts/rename-manifest.md` é a fonte única do mapa `old → new`, do mapa de tokens, do inventário de referências por arquivo/linha/fase, das exceções e das verificações. A implementação consome esse manifesto; a suíte nova valida o resultado.
- **Rationale**: a feature tem 1 pasta + 5 arquivos renomeados e referências no README, no procedimento e em seis suítes. Um manifesto único evita substituições divergentes e permite revisar o escopo antes de editar.
- **Alternatives considered**: substituição global sem manifesto (rejeitada — risco de atingir specs históricas, `graphify-out/` e descrições de papel); atualização manual arquivo a arquivo sem inventário (rejeitada — não auditável).

### D4 — Inventário nominal fechado

- **Decision**: o inventário nominal é o seguinte:
  - **README.md**: 7 linhas com o nome antigo da pasta — 55 (link do índice: `href` e texto), 610 (lista de composição), 652 (tabela de artefatos com as quatro categorias), 821 (âncora `id`/`name`), 822 (título `### 04-referencia-templates-de-contrato`), 2178 (cabeçalho da seção de templates) — e 5 linhas com os nomes curtos dos cinco arquivos — 2183 a 2187 (tabela de templates). A linha 2182 (`01-schema-do-contrato.json`) não muda.
  - **Procedimento**: 13 linhas — `01-inicializacao/01-mapa-dos-arquivos-e-ordem-de-leitura.md` (24 a 29, seis caminhos completos) e `01-inicializacao/00-entry-point.md` (721 a 726, bloco de integridade do JAR, e 220, nome curto de um exemplo). Os arquivos de `02-procedimento-do-agente/` e de `03-regras-de-formacao-do-contrato/` não citam a subpasta 04 nem os seus arquivos.
  - **Suítes existentes**: 6 arquivos — `BuildArtifactsTest.groovy` (85, 102 a 107), `PathRenameContractTest.groovy` (44, 61 a 66), `FileRenameContractTest.groovy` (14, 62 a 77, 79 a 80, 102 a 116, 123 a 134, 173 a 179), `ProcedureContractTest.groovy` (11, 49, 60, 199), `ContractRuleRenameContractTest.groovy` (77 a 89) e `ProcedureEntryRenameContractTest.groovy` (81). `ReadmeConsistencyTest.groovy` não cita a subpasta 04 nem os cinco nomes.
  - **Fixtures**: nenhuma — diferente da subpasta 03, nenhum `src/test/resources/projects/*/archetype.properties` cita o nome da subpasta 04.
  - **Suíte nova**: `ContractTemplateRenameContractTest.groovy`, phase-aware, registrada no runner.
  - **Fora do corpus**: `specs/`, `graphify-out/`, `target/`, `.git/` e o redirect legado em `src/main/resources/utils/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md`.
- **Rationale**: o inventário fecha a lacuna da entrada do Plan e permite provar que nenhuma referência operacional ficou para trás. Specs históricas, `graphify-out/`, `target/` e `.git/` não entram no corpus.
- **Alternatives considered**: confiar apenas na busca global por `exemplo` (rejeitada — atingiria descrições de papel como "Exemplo de contrato plano" no índice, que descrevem o papel e não o nome); limitar a atualização ao procedimento (rejeitada — README e suítes quebrariam).

### D5 — README: âncoras, títulos e links atualizados

- **Decision**: atualizar o nome antigo da pasta em todas as suas formas e fixar a âncora nova em `llm-maven-flags-inference-04-templates-de-contrato`, usada no `id`/`name` (821), no `href` do índice (55) e no link de topo (822). Atualizar as cinco linhas da tabela de templates (2183 a 2187) na Fase 2. Manter o prefixo `llm-maven-flags-inference-` e substituir apenas o segmento `04-referencia-templates-de-contrato` por `04-templates-de-contrato`.
- **Rationale**: `PathRenameContractTest` exige um `id` de âncora e um `href` correspondentes e uma subseção `### <nome>` por subpasta; deixar a âncora antiga quebra o link do índice e a navegação. A âncora segue a convenção já usada pelas subpastas 01 a 03.
- **Alternatives considered**: atualizar só o texto visível e manter a âncora antiga (rejeitada — o link do índice continuaria apontando para o id antigo); usar uma âncora sem o prefixo `llm-maven-flags-inference-` (rejeitada — quebraria a convenção das demais subpastas).

### D6 — Estratégia de testes: atualizar as seis suítes e criar uma suíte dedicada

- **Decision**: atualizar as seis suítes existentes que citam a subpasta 04 e os cinco nomes, e criar `ContractTemplateRenameContractTest.groovy`, phase-aware, registrada no runner. Não refatorar as suítes existentes além do necessário para os nomes novos.
- **Rationale**: a spec exige atualizar as suítes existentes (FR-019), mas não proíbe uma suíte nova; o precedente das features 009 a 011 criou uma suíte de contrato por renomeação. Uma suíte dedicada torna a fronteira entre Fase 1 e Fase 2 verificável sem sobrecarregar `FileRenameContractTest`, que já carrega três gerações de nomes da subpasta 03.
- **Alternatives considered**: apenas atualizar as suítes existentes (rejeitada — a fronteira de fase ficaria implícita e o `FileRenameContractTest` acumularia mais uma geração sem verificação própria); criar a suíte sem atualizar as existentes (inviável — as existentes quebrariam com os nomes novos).

### D7 — `FileRenameContractTest`: três gerações de nomes da subpasta 04

- **Decision**: substituir as listas `p2Old`/`p2New` e a detecção `phase2 = 01-schema-do-contrato.json` por três listas — `p2LegacyOld` (`template-0X-...`, feature 009 pré-renomeação), `p2Current` (`0X-exemplo-...`, estado inicial desta feature) e `p2New` (`0X-contrato-...`, destino) — e por `templatePhase` 0/1/2, detectado pela existência da pasta nova (`04-templates-de-contrato`) e dos cinco nomes novos. O índice, a varredura anti-drift e a checagem do README passam a depender de `templatePhase`.
- **Rationale**: manter `phase2` como "schema existe" deixaria a suíte permanentemente no estado final da feature 009 e ela falharia assim que os nomes mudassem. O padrão `rulePhase` 0..3 já usado para a subpasta 03 na mesma suíte é o modelo direto.
- **Alternatives considered**: remover as fases da suíte e validar só o estado final (rejeitada — perde a prova de que a Fase 1 não renomeia arquivos); criar uma segunda suíte para a 04 e deixar `FileRenameContractTest` quebrado (rejeitada — a suíte existente é executada pelo runner e falharia).

### D8 — Fronteira de fase verificável

- **Decision**: a Fase 1 é verificada por: pasta nova presente, pasta antiga ausente, os seis arquivos com os nomes atuais, índice com os caminhos novos da pasta e nomes antigos dos arquivos, README com âncora e título novos, tabela de templates ainda com os nomes antigos. A Fase 2 é verificada por: cinco nomes novos, cinco nomes antigos ausentes, índice com os 19 caminhos finais, README sem os nomes antigos.
- **Rationale**: cada fase é implementável, testável e demonstrável isoladamente, sem depender da fase seguinte; a fronteira é o que permite rodar `mvn -B test` verde ao fim de cada fase.
- **Alternatives considered**: validar só ao fim da Fase 2 (rejeitada — a Fase 1 ficaria sem critério de aceite próprio).

### D9 — Conteúdo provado por hash

- **Decision**: registrar SHA-256 dos seis JSON antes da Fase 1 e comparar ao fim de cada fase; qualquer divergência interrompe a feature.
- **Rationale**: a spec exige conteúdo idêntico (FR-012, SC-007). O hash prova isso sem inspecionar chave por chave.
- **Alternatives considered**: comparar apenas a validade JSON (rejeitada — não detecta mudança de valor); revisão visual (rejeitada — não auditável).

### D10 — Colisão, maiúsculas e artefato

- **Decision**: pré-checar cada destino com `Test-Path` e parar na primeira colisão; validar o resultado exato dos nomes em sistema de arquivos insensível a maiúsculas; reconstruir `target/classes` com `mvn clean install` e conferir a cópia empacotada; a verificação do JAR instalado fica opt-in (`-Deda.verify.installed.jar=true`) e não é gate.
- **Rationale**: a spec trata colisão, insensibilidade a maiúsculas e artefato como casos de borda; o build já copia `src/main/resources` para `target/classes`, então a cópia empacotada é consequência, não edição manual.
- **Alternatives considered**: renomear sem pré-checagem (rejeitada — sobrescrita silenciosa); editar `target/` à mão (rejeitada — fora do escopo e do processo).

## Lacunas resolvidas

| Lacuna da entrada do Plan | Decisão |
|---|---|
| Suíte dedicada de contrato para a renomeação | D6 — criar `ContractTemplateRenameContractTest.groovy` |
| Identificador da âncora do README | D5 — `llm-maven-flags-inference-04-templates-de-contrato` |

## Fontes

`.specify/memory/constitution.md`; `specs/011-rename-contract-rule-files/plan.md`; `specs/011-rename-contract-rule-files/research.md`; `specs/014-rename-contract-templates/spec.md`; `README.md` (linhas 46 a 67, 600 a 659, 812 a 851 e 2120 a 2209); `pom.xml`; `.github/workflows/ci.yml`; `src/test/groovy/run-archetype-post-generate-tests.groovy`; `src/test/archetype-post-generate/BuildArtifactsTest.groovy`; `PathRenameContractTest.groovy`; `FileRenameContractTest.groovy`; `ProcedureContractTest.groovy`; `ContractRuleRenameContractTest.groovy`; `ProcedureEntryRenameContractTest.groovy`; `src/main/resources/llm-maven-flags-inference/01-inicializacao/00-entry-point.md`; `src/main/resources/llm-maven-flags-inference/01-inicializacao/01-mapa-dos-arquivos-e-ordem-de-leitura.md`.
