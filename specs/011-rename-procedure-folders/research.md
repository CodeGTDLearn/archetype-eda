# Phase 0 — Research: 011-rename-procedure-folders

**Feature**: 011-rename-procedure-folders | **Date**: 2026-11-26

## Objetivo da pesquisa

Fechar as decisões que a spec não fixa: como renomear sem perder conteúdo, como distinguir referência textual de conteúdo normativo, qual é o inventário completo de referências, como dividir o trabalho entre as três fases, como provar a ausência dos nomes antigos, como tratar o redirect legado e o artefato empacotado, e como atualizar as suítes de verificação sem ampliar o escopo.

## Decisões

### D1 — Renomeação com `Rename-Item`, sem copiar e apagar

- **Decision**: renomear cada pasta e o arquivo com `Rename-Item`, no mesmo diretório, após verificar que o destino não existe. Não usar copiar + apagar. Interromper a fase na primeira colisão.
- **Rationale**: preserva o conteúdo, os atributos e a identidade dos itens; a operação é simples de auditar. A checagem prévia evita sobrescrita silenciosa.
- **Alternatives considered**: `git mv` (rejeitada — o processo não deve executar git fora dos hooks); copiar e apagar (rejeitada — risco de perda e de arquivo temporário); renomear em lote sem pré-checagem (rejeitada — sobrescrita silenciosa).

### D2 — Fronteira entre referência textual e conteúdo normativo

- **Decision**: "referência textual" é qualquer ocorrência de um nome antigo de pasta ou de arquivo — completo (`llm-maven-flags-inference/01-entrada-inicializacao/00-entry-point.md`, `02-operacao-procedimento-do-agente/02-como-localizar-e-ler-o-archetype.md`) ou curto (`01-entrada-inicializacao`, `02-operacao-procedimento-do-agente`, `02-como-localizar-e-ler-o-archetype`) — usada para apontar para o item. "Conteúdo normativo" é todo o restante: regras, definições, exemplos de domínio, chaves e schema JSON, tipos, `defaultValue`, `validationRegex`, metadados estruturais e texto de prosa que não seja a referência. Só o token do nome é substituído; `§`, pontuação, tabelas, links de diretório e o restante da frase permanecem.
- **Rationale**: a spec exige atualizar todas as referências operacionais, inclusive as internas aos arquivos do procedimento. Sem uma fronteira explícita, uma substituição ampla poderia alterar regras ou exemplos; sem atualizar os arquivos, sobrariam referências quebradas.
- **Alternatives considered**: não atualizar referências dentro dos arquivos do procedimento (rejeitada — o índice e a entrada apontariam para nomes novos, mas as regras ainda citariam os antigos); reescrever descrições JSON inteiras (rejeitada — mudaria conteúdo além da referência).

### D3 — Manifesto único de renomeação e referências

- **Decision**: `contracts/rename-manifest.md` é a fonte única do mapa `old → new`, do mapa de tokens curtos, do inventário de referências por arquivo/linha/fase e das exceções. A implementação consome esse manifesto; as suítes existentes validam o resultado.
- **Rationale**: a feature tem 3 renomeações e um corpus operacional com referências no procedimento, no README, no redirect e em 8 suítes de teste. Um manifesto único evita substituições divergentes e permite revisar o escopo antes de editar.
- **Alternatives considered**: substituição global sem manifesto (rejeitada — risco de atingir specs históricas e contextos não referenciais); atualização manual arquivo a arquivo sem inventário (rejeitada — não auditável).

### D4 — Inventário nominal fechado

- **Decision**: o inventário bruto é o seguinte:
  - **Procedimento**: 21 linhas correspondentes em 2 arquivos — `01-entrada-inicializacao/00-entry-point.md` (12) e `01-entrada-inicializacao/01-mapa-dos-arquivos-e-ordem-de-leitura.md` (9).
  - **README.md**: 17 linhas correspondentes — 52, 53, 603, 605, 652, 806, 807, 811, 812, 995, 2111, 2152, 2207, 2513, 2567, 2639 e 2704.
  - **Redirect legado**: 1 linha correspondente em `src/main/resources/utils/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md` (linha 7).
  - **Suítes de verificação**: 53 linhas correspondentes — `BuildArtifactsTest` (10), `CatalogSyncContractTest` (1), `DestinationContractTest` (5), `FileRenameContractTest` (2), `HardStopContractTest` (2), `PathRenameContractTest` (13), `ProcedureEntryRenameContractTest` (18) e `ReadmeConsistencyTest` (2).
  - **Total bruto**: 92 linhas correspondentes. Nem todas são referências operacionais a atualizar: o caminho do redirect legado e as asserções negativas das suítes permanecem.
- **Rationale**: o inventário fecha a lacuna da entrada do Plan e permite provar que nenhuma referência operacional ficou para trás. As specs históricas, `graphify-out/`, `target/` e `.git/` não entram no corpus.
- **Alternatives considered**: confiar apenas na busca global pelos nomes antigos (rejeitada — atingiria specs históricas, `graphify-out/` e asserções negativas); limitar a atualização ao procedimento (rejeitada — README, redirect e suítes quebrariam).

### D5 — Redirect legado preservado

- **Decision**: o arquivo `src/main/resources/utils/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md` permanece com o nome antigo e passa a apontar para `llm-maven-flags-inference/01-inicializacao/00-entry-point.md`. Nenhum redirect é criado para as pastas ou para o arquivo renomeados.
- **Rationale**: a feature 006 manteve um único redirect no caminho antigo da porta de entrada para que specs anteriores continuem funcionando. Renomear a pasta `utils/01-entrada-inicializacao` quebraria essa compatibilidade; criar redirects adicionais ampliaria o escopo.
- **Alternatives considered**: renomear a pasta de redirect (rejeitada — quebra specs antigas); remover o redirect (rejeitada — quebra compatibilidade); criar redirects para as pastas 01/02 (rejeitada — não há exigência e amplia o escopo).

### D6 — README atualizado por fase

- **Decision**: a Fase 1 atualiza no README as referências e âncoras da subpasta 01 — linhas 52, 603, 806, 807, 995, 2111, 2152, 2207, 2513, 2567, 2639 e 2704, além da parte 01 da linha 652. A Fase 2 atualiza as referências e âncoras da subpasta 02 — linhas 53, 605, 811 e 812, além da parte 02 da linha 652. A Fase 3 não edita o README, porque o inventário não encontrou referência ao nome do arquivo 02 no README.
- **Rationale**: o README contém referências às duas subpastas, mas não ao arquivo 02. Dividir por subpasta mantém cada fase demonstrável sem antecipar a fase seguinte.
- **Alternatives considered**: atualizar tudo na Fase 1 (rejeitada — mistura o escopo de 02); deixar o README para o fim (rejeitada — a Fase 1 ficaria com caminho de entrada quebrado).

### D7 — Suítes existentes atualizadas, sem suíte nova

- **Decision**: atualizar os caminhos e rótulos das suítes existentes: `BuildArtifactsTest`, `PathRenameContractTest`, `HardStopContractTest`, `DestinationContractTest`, `CatalogSyncContractTest`, `ReadmeConsistencyTest`, `FileRenameContractTest` e `ProcedureEntryRenameContractTest`. Não criar suíte nova e não alterar o runner.
- **Rationale**: as suítes existentes leem caminhos fixos; sem atualização, o build falha antes de validar a feature. A spec exige atualizar as suítes existentes, não criar uma nova. O `ProcedureEntryRenameContractTest` já é phase-aware e cobre a estrutura das pastas e do arquivo; ele é o lugar natural para ajustar as expectativas.
- **Alternatives considered**: criar uma suíte nova (rejeitada — amplia o escopo sem exigência da spec); confiar apenas na revisão manual (rejeitada — não falha o build).

### D8 — Artefato empacotado: `target/classes` no gate; JAR instalado opt-in

- **Decision**: a verificação obrigatória lê `target/classes/llm-maven-flags-inference` e a árvore-fonte. A verificação do JAR instalado continua opt-in com `-Deda.verify.installed.jar=true` e não é gate de aceite desta feature.
- **Rationale**: o build já empacota `src/main/resources` em `target/classes`; as suítes existentes leem essa cópia. O JAR instalado depende de `mvn install` e de um ambiente limpo; a spec não definiu esse gate.
- **Alternatives considered**: exigir o JAR instalado como gate (rejeitada — a spec não define esse critério e o CI já cobre `clean install`); validar apenas a árvore-fonte (rejeitada — não prova o artefato empacotado).

### D9 — Performance: sem meta nova; guarda de regressão de 10%

- **Decision**: a feature não cria meta de performance. Se o build for medido, usar o orçamento da feature 007 — aumento máximo de 10% sobre a linha de base local de `mvn -B test` — apenas como guarda de regressão, não como critério de aceite.
- **Rationale**: renomear pastas e um arquivo e atualizar referências não deve alterar o tempo de build de forma relevante; a meta de 10% já existe no projeto e evita uma decisão nova sem evidência.
- **Alternatives considered**: definir um teto absoluto (rejeitada — depende da máquina); ignorar o tempo de build (rejeitada — perde a guarda existente).

### D10 — Nenhuma dependência, flag ou arquivo de procedimento novo

- **Decision**: a implementação usa as ferramentas e dependências já presentes. Não cria flag, propriedade de catálogo, fileset, arquivo em `archetype-resources/` ou arquivo novo no procedimento. Nenhuma suíte nova é adicionada.
- **Rationale**: a feature é nominal e documental; qualquer adição ao procedimento violaria as restrições da spec e os Princípios I e II.
- **Alternatives considered**: script dedicado de renomeação (rejeitada — pode ser feito com PowerShell e o manifesto); biblioteca de manipulação de JSON (rejeitada — a mudança é textual e `groovy-json` já existe).

### D11 — Divisão entre as fases

- **Decision**: a Fase 1 renomeia a subpasta 01 e atualiza suas referências; a Fase 2 renomeia a subpasta 02 e atualiza suas referências; a Fase 3 renomeia o arquivo 02, atualiza suas referências e conclui a varredura. As fases são incrementais, mas a Fase 2 depende da Fase 1 apenas para a conferência final de zero nomes antigos, e a Fase 3 depende da Fase 2 apenas para o caminho da subpasta.
- **Rationale**: cada fase entrega um resultado observável e testável. A subpasta 01 é o gateway de leitura e pode ser demonstrada isoladamente; a subpasta 02 é o bloco de operação; o arquivo 02 é a leitura do artefato.
- **Alternatives considered**: uma fase única (rejeitada — perde o checkpoint e o MVP da Fase 1); quatro fases com README separado (rejeitada — não há resultado independente que justifique).

### D12 — Nomes exatos e sistema de arquivos

- **Decision**: os nomes finais são exatamente `01-inicializacao`, `02-procedimento-do-agente` e `02-localizar-e-ler-o-archetype.md`. O separador é hífen, a extensão do arquivo é `.md` e os nomes são aplicados exatamente como listados, inclusive em sistema de arquivos insensível a maiúsculas e minúsculas. Uma colisão aborta a fase antes de qualquer sobrescrita.
- **Rationale**: a spec registra a premissa do nome final e a exigência de nomes exatos; a pré-checagem evita perda de arquivo.
- **Alternatives considered**: manter os espaços da descrição da demanda (rejeitada — quebra a convenção numeral-first com hífen); omitir a extensão (rejeitada — o arquivo é Markdown como os demais); aceitar `archtype` (rejeitada — a grafia correta é `archetype`).

### D13 — Âncoras do README

- **Decision**: atualizar tanto o texto quanto os identificadores das âncoras do README: `#llm-maven-flags-inference-01-entrada-inicializacao` passa a `#llm-maven-flags-inference-01-inicializacao`; `#llm-maven-flags-inference-02-operacao-procedimento-do-agente` passa a `#llm-maven-flags-inference-02-procedimento-do-agente`. Atualizar `href`, `id` e `name` correspondentes.
- **Rationale**: o índice do README e os links internos quebram se apenas o texto do título mudar. A atualização das âncoras mantém a navegação funcional.
- **Alternatives considered**: atualizar apenas o texto e deixar as âncoras antigas (rejeitada — links quebrados); remover as âncoras (rejeitada — perde navegação existente).

### D14 — Specs históricas fora do corpus

- **Decision**: specs históricas, planos, tarefas e saída gerada não são reescritos. A atualização fica restrita ao corpus operacional: README, índice, porta de entrada, redirect legado e suítes de verificação.
- **Rationale**: a clarificação da spec confirmou que o histórico não é corpus operacional; reescrevê-lo aumentaria o diff e o risco sem afetar o procedimento.
- **Alternatives considered**: reescrever todas as specs históricas (rejeitada — ruído e risco); reescrever apenas as specs que citam os nomes antigos (rejeitada — ainda é histórico, não operação).

## Inventário consolidado

- **3 itens renomeados**: 2 subpastas e 1 arquivo.
- **19 arquivos no procedimento**: 13 Markdown e 6 JSON, em 4 subpastas, com 2 em 01 e 5 em 02.
- **21 linhas correspondentes no procedimento**: 12 na porta de entrada e 9 no índice.
- **17 linhas correspondentes no README**: incluindo índice, âncoras e caminhos.
- **1 linha correspondente no redirect legado**: linha 7; o caminho do redirect permanece e o destino muda.
- **53 linhas correspondentes nas suítes**: 8 suítes existentes; parte é caminho legado ou asserção negativa e permanece.
- **0 arquivos novos ou removidos no procedimento**; **0 dependências novas**; **0 suítes novas**.

## Lacunas resolvidas

Nenhuma lacuna de Technical Context. As decisões acima fecham os pontos de implementação: estratégia de renomeação, fronteira de conteúdo, inventário, redirect legado, README, suítes, artefato, performance, dependências, faseamento, nomes exatos, âncoras e histórico.

## Restrições preservadas

- Não criar nem remover arquivos do procedimento.
- Não renomear as subpastas 03 e 04.
- Não renomear a pasta de redirect legado `utils/01-entrada-inicializacao`.
- Não alterar regras, catálogo, estrutura de templates, ordem de leitura ou formato do contrato.
- Não reescrever specs históricas, `graphify-out/` ou `target/`.
- Não adicionar dependências, flags, suítes ou arquivos de extensão.
- Não executar git fora dos hooks declarados.
- Manter o redirect legado funcional.

## Riscos e mitigações

| Risco | Mitigação |
|---|---|
| Colisão de nome no destino | Checar o destino antes de cada `Rename-Item`; abortar a fase na primeira colisão |
| Sistema de arquivos insensível a maiúsculas e minúsculas | Aplicar os nomes exatos e conferir a grafia final |
| Referência duplicada na mesma linha (pasta 02 + arquivo 02) | Tratar a linha 711 da entrada e a linha 14 do índice em duas etapas: pasta na Fase 2, arquivo na Fase 3 |
| Âncora do README esquecida | Atualizar `href`, `id` e `name` na mesma fase da subpasta |
| Suíte confundir caminho legado com caminho operacional | Manter `utils/01-entrada-inicializacao` e as asserções negativas; atualizar apenas o corpus operacional |
| Conteúdo normativo alterado por substituição ampla | Substituir somente tokens de nome; revisar o diff e comparar hashes |
| Artefato empacotado desatualizado | Rodar `mvn -B clean install` e conferir `target/classes`; JAR instalado é opt-in |
| Specs históricas alteradas por engano | Excluir `specs/` do corpus de edição e varrer apenas README, procedimento, redirect e suítes |
