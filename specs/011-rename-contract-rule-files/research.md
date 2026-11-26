# Phase 0 — Research: 011-rename-contract-rule-files

**Feature**: 011-rename-contract-rule-files | **Date**: 2026-11-26

## Objetivo da pesquisa

Fechar as decisões que a spec não fixa: como renomear a subpasta 03 e os seis arquivos sem perder conteúdo, como distinguir referência textual de conteúdo normativo, qual é o inventário completo de referências operacionais, como dividir o trabalho entre as três fases, como manter as suítes verdes em cada fase, como tratar o redirect legado e o artefato empacotado, e como atualizar âncoras, títulos e links do README sem ampliar o escopo.

## Decisões

### D1 — Renomeação com `Rename-Item`, sem copiar e apagar

- **Decision**: renomear a pasta e cada arquivo com `Rename-Item` no mesmo diretório, após verificar que o destino não existe. Ordem: pasta 03 primeiro; depois os arquivos `01`–`03` (Fase 2); depois `04`–`06` (Fase 3). Interromper a fase na primeira colisão. Não usar copiar + apagar. Não executar `git mv` fora dos hooks.
- **Rationale**: preserva o arquivo, o conteúdo e os atributos; a operação é auditável. A pré-checagem impede sobrescrita silenciosa. A ordem crescente de numeral evita confusão entre nomes que compartilham prefixo.
- **Alternatives considered**: `git mv` (rejeitada — o processo não deve executar git fora dos hooks); copiar e apagar (rejeitada — risco de perda e de arquivo temporário); renomear em lote sem pré-checagem (rejeitada — sobrescrita silenciosa).

### D2 — Fronteira entre referência textual e conteúdo normativo

- **Decision**: "referência textual" é qualquer ocorrência do nome antigo da pasta ou de um dos seis nomes antigos usada para apontar para a pasta/arquivo: caminho completo, caminho só da pasta, nome curto de arquivo, âncora, título de seção, link, linha de tabela, comentário de fixture ou expectativa de teste. "Conteúdo normativo" é todo o restante: regras, definições, exemplos de domínio, chaves e schema JSON, tipos, `defaultValue`, `validationRegex`, metadados estruturais e prosa que não seja a referência. Só o token do nome é substituído; `§`, pontuação, tabelas, links e o restante da frase permanecem.
- **Rationale**: a spec exige atualizar todas as referências operacionais, inclusive as internas ao procedimento. Sem uma fronteira explícita, uma substituição ampla poderia alterar regras ou exemplos; sem atualizar os arquivos, sobrariam referências quebradas.
- **Alternatives considered**: não atualizar referências dentro dos seis arquivos (rejeitada — o índice e a porta de entrada apontariam para nomes novos, mas as regras ainda citariam os antigos); reescrever descrições JSON inteiras (rejeitada — mudaria conteúdo além da referência).

### D3 — Manifesto único de renomeação e referências

- **Decision**: `contracts/rename-manifest.md` é a fonte única do mapa `old → new`, do mapa de tokens, do inventário de referências por arquivo/linha/fase, das exceções e das verificações. A implementação consome esse manifesto; a nova suíte valida o resultado.
- **Rationale**: a feature tem 1 pasta + 6 arquivos renomeados e um corpus operacional com referências no README, no procedimento, nas fixtures e em oito suítes. Um manifesto único evita substituições divergentes e permite revisar o escopo antes de editar.
- **Alternatives considered**: substituição global sem manifesto (rejeitada — risco de atingir specs históricas e contextos não referenciais); atualização manual arquivo a arquivo sem inventário (rejeitada — não auditável).

### D4 — Inventário nominal fechado

- **Decision**: o inventário nominal é o seguinte:
  - **README.md**: 10 linhas com o nome antigo da pasta (54, 607, 652, 816, 817, 995, 2167, 2208, 2391 e 2868) e 6 linhas com os nomes curtos dos seis arquivos (2171–2176). As linhas 816 e 995 contêm duas ocorrências cada (âncora id/name e link texto/href); a linha 54 é o link do índice; a 817 é o título da subseção.
  - **Procedimento**: 13 linhas — `01-entrada-inicializacao/00-entry-point.md` (715–720, seis caminhos completos), `01-entrada-inicializacao/01-mapa-dos-arquivos-e-ordem-de-leitura.md` (18–23, seis caminhos completos) e `02-operacao-procedimento-do-agente/02-como-localizar-e-ler-o-archetype.md` (141, caminho só da pasta).
  - **Fixtures**: 4 linhas — `src/test/resources/projects/custom-nested-edge-cases/archetype.properties` (16), `custom-nested-objects/archetype.properties` (16), `entity-contract/archetype.properties` (20) e `custom-enums-from-payload/archetype.properties` (17).
  - **Suítes existentes**: 8 arquivos com referências à pasta 03 — `BuildArtifactsTest.groovy` (84, 96–101), `CatalogSyncContractTest.groovy` (9, 162), `DestinationContractTest.groovy` (40, 41), `FileRenameContractTest.groovy` (12, 32–37, 40–45, 99, 107, 112, 125), `HardStopContractTest.groovy` (110), `PathRenameContractTest.groovy` (43, 55–60), `ProcedureContractTest.groovy` (198, 252) e `ProcedureEntryRenameContractTest.groovy` (71). `ReadmeConsistencyTest.groovy` não cita a pasta 03 nem os seis nomes.
  - **Suíte nova**: `ContractRuleRenameContractTest.groovy`, phase-aware, registrada no runner.
  - **Fora do corpus**: `specs/`, `graphify-out/`, `target/`, `.git/`, artefato instalado e o redirect legado em `src/main/resources/utils/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md`.
- **Rationale**: o inventário fecha a lacuna da entrada do Plan e permite provar que nenhuma referência operacional ficou para trás. As specs históricas, `graphify-out/`, `target/` e `.git/` não entram no corpus.
- **Alternatives considered**: confiar apenas na busca global por `como-` (rejeitada — atingiria `02-como-localizar-e-ler-o-archetype.md`, da subpasta 02, que não é renomeado); limitar a atualização ao procedimento (rejeitada — README, fixtures e suítes quebrariam).

### D5 — README: âncoras, títulos e links atualizados

- **Decision**: atualizar o nome antigo da pasta no README em todas as suas formas: link do índice (linha 54), `id`/`name` da âncora (816), título da subseção (817), texto e `href` do link (995) e caminhos em prosa (607, 652, 2167, 2208, 2391 e 2868). Manter o prefixo `llm-maven-flags-inference-` e substituir apenas o segmento `03-derivacao-regras-de-formacao-do-contrato` por `03-regras-de-formacao-do-contrato`. Atualizar as seis linhas da tabela de arquivos (2171–2176) nas fases correspondentes.
- **Rationale**: o README é a documentação operacional do procedimento; âncoras e links quebrados quebram a navegação e as suítes de consistência. A atualização por token preserva o restante do texto.
- **Alternatives considered**: atualizar só o texto visível e deixar a âncora antiga (rejeitada — o link do índice continuaria apontando para o id antigo); atualizar o README apenas no fim (rejeitada — a Fase 1 ficaria com link quebrado).

### D6 — Redirect legado preservado

- **Decision**: o arquivo `src/main/resources/utils/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md` permanece com o nome antigo e continua apontando para `llm-maven-flags-inference/01-entrada-inicializacao/00-entry-point.md`. Ele não cita a pasta 03 nem os seis arquivos; nenhum redirect é criado para os seis.
- **Rationale**: a feature 006 manteve um único redirect no caminho antigo da porta de entrada para que specs anteriores continuem funcionando. Renomear o redirect quebraria essa compatibilidade; criar redirects para os seis ampliaria o escopo e não é exigido.
- **Alternatives considered**: renomear o redirect (rejeitada — quebra specs antigas); remover o redirect (rejeitada — quebra compatibilidade); criar redirects para os seis (rejeitada — não há ponto de entrada nem exigência).

### D7 — Suítes existentes atualizadas por fase e uma suíte de contrato nova

- **Decision**: atualizar as oito suítes existentes que citam a pasta 03 por fase: a Fase 1 troca apenas o nome da pasta; a Fase 2 troca os nomes dos arquivos `01`–`03`; a Fase 3 troca os nomes dos arquivos `04`–`06`. Tornar `FileRenameContractTest.groovy` phase-aware para o estado da pasta 03, preservando a lógica de 04 da feature 009. Criar `ContractRuleRenameContractTest.groovy`, phase-aware, para validar a pasta, os seis nomes, as contagens 19/13/6/4, o índice, o README, as fixtures, o redirect legado e a ausência de referências antigas. Registrar a suíte nova no runner.
- **Rationale**: as suítes existentes leem caminhos fixos; sem atualização, o build falha antes de validar a feature. A nova suíte transforma os critérios da spec em falha de build e segue o precedente das features 008, 009 e 010.
- **Alternatives considered**: confiar apenas na revisão manual (rejeitada — não falha o build); atualizar todas as suítes apenas no fim (rejeitada — as fases intermediárias ficariam sem build verde); adicionar as verificações a `PathRenameContractTest` (rejeitada — a suíte é sobre o rename de raiz da feature 006; a responsabilidade nova fica mais clara em uma suíte própria).

### D8 — Artefato empacotado: `target/classes` no gate; JAR instalado opt-in

- **Decision**: a verificação obrigatória lê `target/classes/llm-maven-flags-inference` e a árvore-fonte. A verificação do JAR instalado continua opt-in com `-Deda.verify.installed.jar=true` e não é gate de aceite desta feature.
- **Rationale**: o build já empacota `src/main/resources` em `target/classes`; as suítes existentes leem essa cópia. O JAR instalado depende de `mvn install` e de um ambiente limpo; a spec não definiu esse gate.
- **Alternatives considered**: exigir o JAR instalado como gate (rejeitada — a spec não define esse critério e o CI já cobre `clean install`); validar apenas a árvore-fonte (rejeitada — não prova o artefato empacotado).

### D9 — Nenhuma dependência, flag ou arquivo de procedimento novo

- **Decision**: a implementação usa as ferramentas e dependências já presentes. Não cria flag, propriedade de catálogo, fileset, arquivo em `archetype-resources/` ou arquivo novo no procedimento. A única adição de código é a suíte `ContractRuleRenameContractTest.groovy` em `src/test/archetype-post-generate/` e seu registro no runner.
- **Rationale**: a feature é nominal e documental; qualquer adição ao procedimento violaria as restrições da spec e os Princípios I e II.
- **Alternatives considered**: script dedicado de renomeação (rejeitada — pode ser feito com PowerShell e o manifesto); biblioteca de manipulação de JSON (rejeitada — `groovy-json` já existe e a mudança é textual).

### D10 — Divisão entre as fases

- **Decision**: a Fase 1 renomeia a pasta 03 e atualiza todas as referências à pasta no procedimento, README, fixtures e suítes; não renomeia os seis arquivos. A Fase 2 renomeia os arquivos `01`–`03` e atualiza suas referências. A Fase 3 renomeia os arquivos `04`–`06`, atualiza suas referências e conclui a varredura final.
- **Rationale**: a pasta é o primeiro checkpoint demonstrável; os dois grupos de três arquivos são incrementos coesos e cada fase pode ser validada isoladamente. A Fase 2 depende da Fase 1 apenas para o caminho da pasta; a Fase 3 depende das anteriores para a conferência final de zero nomes antigos.
- **Alternatives considered**: uma fase única (rejeitada — perde os checkpoints e o MVP da Fase 1); duas fases com os seis arquivos juntos (rejeitada — a entrada do Plan define três fases e a prioridade P1/P2/P3).

### D11 — Performance: sem meta nova; guarda de regressão de 10%

- **Decision**: a feature não cria meta de performance. Se o build for medido, usar o orçamento da feature 007 — aumento máximo de 10% sobre a linha de base local de `mvn -B test` — apenas como guarda de regressão, não como critério de aceite.
- **Rationale**: renomear arquivos e atualizar referências não deve alterar o tempo de build de forma relevante; a meta de 10% já existe no projeto e evita uma decisão nova sem evidência.
- **Alternatives considered**: definir um teto absoluto (rejeitada — depende da máquina); ignorar o tempo de build (rejeitada — perde a guarda existente).

### D12 — Nomes exatos, colisão e sistema de arquivos

- **Decision**: aplicar exatamente `03-regras-de-formacao-do-contrato` e os seis nomes definidos na spec, inclusive em sistema de arquivos insensível a maiúsculas e minúsculas. Uma colisão aborta a fase antes de qualquer sobrescrita. A pasta de destino não pode existir no início; os seis nomes de destino não podem existir.
- **Rationale**: a spec exige nomes exatos e proíbe sobrescrita; a pré-checagem evita perda de arquivo.
- **Alternatives considered**: aceitar o nome antigo com espaços/acentos (rejeitada — o destino é `03-regras-de-formacao-do-contrato`); renomear em lote sem verificar destinos (rejeitada — risco de sobrescrita).

## Inventário consolidado

- **1 pasta renomeada**: `03-derivacao-regras-de-formacao-do-contrato` → `03-regras-de-formacao-do-contrato` (Fase 1).
- **6 arquivos renomeados**: `01`–`03` na Fase 2; `04`–`06` na Fase 3.
- **13 linhas no procedimento**: 6 na porta de entrada, 6 no índice e 1 na regra de localização.
- **10 linhas no README** com o nome da pasta + 6 linhas com os nomes curtos dos arquivos.
- **4 linhas nos comentários das fixtures**.
- **8 suítes existentes** com referências à pasta 03 + 1 suíte nova phase-aware.
- **Contagens preservadas**: 19 arquivos, 13 Markdown, 6 JSON, 4 subpastas, 6 arquivos em 03.
- **Catálogo**: 35 propriedades e 18 filesets; sem alteração.

## Lacunas resolvidas

| Lacuna da entrada do Plan | Decisão |
|---|---|
| Inventário nominal das referências operacionais (arquivo/linha), das suítes e do mapa old→new | D3 + D4 + `contracts/rename-manifest.md`: 10 linhas no README para a pasta, 6 para os nomes curtos, 13 no procedimento, 4 nas fixtures e 8 suítes existentes mapeadas por arquivo/linha/fase |
| Âncoras, títulos e links internos do README | D5: linhas 54, 816, 817, 995, 607, 652, 2167, 2208, 2391 e 2868 atualizadas por token; prefixo `llm-maven-flags-inference-` preservado |
| Estratégia de referências dentro dos seis arquivos | D2: só tokens de nome mudam; regras, schema e exemplos permanecem |
| Estratégia de testes em cada fase | D7: suítes existentes atualizadas por fase; `FileRenameContractTest` phase-aware; nova `ContractRuleRenameContractTest` phase-aware registrada no runner |
| Verificação no artefato instalado | D8: `target/classes` no gate; JAR instalado opt-in, não gate |
| Metas de performance | D11: sem meta nova; guarda de regressão de 10% da feature 007, não gate |
| Divisão das referências do README entre as fases | D10 + manifesto: pasta na Fase 1; linhas 2171–2173 na Fase 2; linhas 2174–2176 na Fase 3 |

## Restrições preservadas

- Não criar nem remover arquivos do procedimento.
- Não renomear as subpastas 01, 02 e 04.
- Não alterar regras, catálogo, estrutura de templates, ordem de leitura ou formato do contrato.
- Não reescrever specs históricas, planos/tarefas, `graphify-out/` ou `target/`.
- Não adicionar dependências, flags ou arquivos de extensão.
- Não executar git fora dos hooks declarados.
- Manter o redirect legado funcional.

## Riscos e mitigações

| Risco | Mitigação |
|---|---|
| Referência antiga esquecida no README, procedimento, fixture ou suíte | Manifesto por arquivo/linha/fase; suíte nova; varredura final com `Select-String` |
| Suíte existente falhar em uma fase intermediária | Atualização incremental por fase; `FileRenameContractTest` phase-aware; `mvn -B clean test` ao fim de cada fase |
| Colisão ou sobrescrita de arquivo | Pré-checar cada destino; abortar a fase na primeira colisão |
| Conteúdo normativo alterado por substituição ampla | Substituir só tokens de nome; comparar SHA-256/diff com o baseline |
| Âncora ou link do README quebrado | Atualizar id/name, título, texto e href; `PathRenameContractTest` e `ReadmeConsistencyTest` |
| Contagem 19/13/6/4 alterada | Suítes de contagem e revisão do diff |
| Nova suíte não registrada | Registrar no runner; `CoverageRegistryTest` e `ReadmeConsistencyTest` |

## Fontes

- `specs/011-rename-contract-rule-files/spec.md`
- `.specify/memory/constitution.md`
- `specs/010-rename-procedure-entry-files/plan.md`
- `specs/010-rename-procedure-entry-files/research.md`
- `README.md`
- `pom.xml`
- `.github/workflows/ci.yml`
- `src/test/groovy/run-archetype-post-generate-tests.groovy`
- Inventário textual em `README.md`, `src/main/resources/llm-maven-flags-inference/`, `src/test/archetype-post-generate/` e `src/test/resources/projects/*/archetype.properties`
