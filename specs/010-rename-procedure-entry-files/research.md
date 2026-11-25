# Phase 0 — Research: 010-rename-procedure-entry-files

**Feature**: 010-rename-procedure-entry-files | **Date**: 2026-11-25

## Objetivo da pesquisa

Fechar as decisões que a spec não fixa: como renomear sem perder conteúdo, como distinguir referência textual de conteúdo normativo, qual é o inventário completo de referências, como dividir o trabalho entre as duas fases, como provar a ausência de nomes antigos, como tratar o redirect legado e o artefato empacotado, e como atualizar as suítes de verificação sem ampliar o escopo.

## Decisões

### D1 — Renomeação com `Rename-Item`, sem copiar e apagar

- **Decision**: renomear cada arquivo com `Rename-Item` no mesmo diretório, em ordem crescente de numeral, após verificar que o destino não existe. Não usar copiar + apagar. Interromper a subpasta na primeira colisão.
- **Rationale**: preserva o arquivo, o conteúdo e os atributos; a operação é simples de auditar. A ordem crescente evita confusão quando dois nomes compartilham prefixo.
- **Alternatives considered**: `git mv` (rejeitada — o processo não deve executar git fora dos hooks); copiar e apagar (rejeitada — risco de perda e de arquivo temporário); renomear em lote sem pré-checagem (rejeitada — sobrescrita silenciosa).

### D2 — Fronteira entre referência textual e conteúdo normativo

- **Decision**: "referência textual" é qualquer ocorrência de um nome antigo de arquivo — completo (`prompt-00-leia-este-arquivo-primeiro.md`, `indice-01-mapa-dos-arquivos-e-ordem-de-leitura.md`, `prompt-01...` a `prompt-05...`) ou curto (`prompt-00`, `indice-01`, `prompt-01` a `prompt-05`) — usada para apontar para o arquivo. "Conteúdo normativo" é todo o restante: regras, definições, exemplos de domínio, chaves e schema JSON, tipos, `defaultValue`, `validationRegex`, metadados estruturais e texto de prosa que não seja a referência. Só o token do nome é substituído; `§`, pontuação, tabelas, links de diretório e o restante da frase permanecem.
- **Rationale**: a spec exige atualizar todas as referências operacionais, inclusive as internas aos sete arquivos. Sem uma fronteira explícita, uma substituição ampla poderia alterar regras ou exemplos; sem atualizar os arquivos, sobrariam referências quebradas.
- **Alternatives considered**: não atualizar referências dentro dos sete arquivos (rejeitada — o índice e os prompts apontariam para nomes novos, mas as regras ainda citariam os antigos); reescrever descrições JSON inteiras (rejeitada — mudaria conteúdo além da referência).

### D3 — Manifesto único de renomeação e referências

- **Decision**: `contracts/rename-manifest.md` é a fonte única do mapa `old → new`, do mapa de tokens curtos, do inventário de referências por arquivo/linha/fase e das exceções. A implementação consome esse manifesto; a suíte `ProcedureEntryRenameContractTest` valida o resultado.
- **Rationale**: a feature tem 7 renomeações e um corpus operacional com referências em 7 arquivos do procedimento, no README, no redirect e em 7 suítes de teste. Um manifesto único evita substituições divergentes e permite revisar o escopo antes de editar.
- **Alternatives considered**: substituição global sem manifesto (rejeitada — risco de atingir specs históricas e contextos não referenciais); atualização manual arquivo a arquivo sem inventário (rejeitada — não auditável).

### D4 — Inventário nominal fechado

- **Decision**: o inventário nominal é o seguinte:
  - **Procedimento**: 55 ocorrências em 7 arquivos — `01-entrada-inicializacao/00-entry-point.md` (35), `01-entrada-inicializacao/01-mapa-dos-arquivos-e-ordem-de-leitura.md` (14), `02-operacao-procedimento-do-agente/01-papel-e-restricoes-do-agente.md` (1), `02-operacao-procedimento-do-agente/03-regras-obrigatorias-de-geracao.md` (1), `02-operacao-procedimento-do-agente/04-fluxo-de-execucao-e-verificacao.md` (1), `03-derivacao-regras-de-formacao-do-contrato/01-como-derivar-a-entidade-do-contrato.md` (1) e `03-derivacao-regras-de-formacao-do-contrato/04-como-aplicar-precedencia-e-validacao.md` (2).
  - **README.md**: 17 ocorrências em 15 linhas (linhas 604, 804, 845, 991, 995, 2067, 2099, 2111, 2151, 2152, 2207, 2513, 2567, 2639 e 2704).
  - **Redirect legado**: 1 ocorrência em `src/main/resources/utils/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md` (linha 7).
  - **Suítes de verificação**: `BuildArtifactsTest` (8 referências), `PathRenameContractTest` (11), `HardStopContractTest` (4), `DestinationContractTest` (16), `CatalogSyncContractTest` (2), `ReadmeConsistencyTest` (3) e `FileRenameContractTest` (1). A nova `ProcedureEntryRenameContractTest` contém os padrões antigos apenas em asserções negativas.
- **Rationale**: o inventário fecha a lacuna da entrada do Plan e permite provar que nenhuma referência operacional ficou para trás. As specs históricas, `graphify-out/`, `target/` e `.git/` não entram no corpus.
- **Alternatives considered**: confiar apenas na busca global `prompt-` (rejeitada — atingiria nomes não relacionados e specs históricas); limitar a atualização ao procedimento (rejeitada — README e suítes quebrariam).

### D5 — Redirect legado preservado

- **Decision**: o arquivo `src/main/resources/utils/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md` permanece com o nome antigo e passa a apontar para `llm-maven-flags-inference/01-entrada-inicializacao/00-entry-point.md`. Nenhum redirect é criado para os outros seis arquivos.
- **Rationale**: a feature 006 manteve um único redirect no caminho antigo da porta de entrada para que specs anteriores continuem funcionando. Renomear o redirect quebraria essa compatibilidade; criar redirects para os outros seis ampliaria o escopo e não é exigido.
- **Alternatives considered**: renomear o redirect (rejeitada — quebra specs antigas); remover o redirect (rejeitada — quebra compatibilidade); criar redirects para os seis (rejeitada — não há ponto de entrada nem exigência).

### D6 — README atualizado por token, em duas fases

- **Decision**: a Fase 1 atualiza no README as referências a `prompt-00` e `indice-01` e os caminhos completos da entrada; a Fase 2 atualiza `prompt-03`, `prompt-04` e `prompt-05`. Os cinco templates de spec do README devem apontar para o novo caminho da entrada. Os links de diretório permanecem.
- **Rationale**: o README contém referências às duas subpastas; dividir por token mantém a Fase 1 demonstrável sem antecipar a renomeação de 02. Os templates de spec são verificados por `ReadmeConsistencyTest` 3.5.2.
- **Alternatives considered**: atualizar tudo na Fase 1 (rejeitada — mistura o escopo de 02); deixar o README para o fim (rejeitada — a Fase 1 ficaria com caminho de entrada quebrado).

### D7 — Suítes existentes atualizadas e uma suíte de contrato nova

- **Decision**: atualizar os caminhos e rótulos de `BuildArtifactsTest`, `PathRenameContractTest`, `HardStopContractTest`, `DestinationContractTest`, `CatalogSyncContractTest`, `ReadmeConsistencyTest` e `FileRenameContractTest`. Adicionar `ProcedureEntryRenameContractTest.groovy`, phase-aware, e registrá-la no runner. A nova suíte valida os 7 nomes, as contagens 19/13/6/4, o índice, o redirect legado, a ausência de referências antigas no corpus operacional e a separação entre caminho canônico e caminho legado.
- **Rationale**: as suítes existentes leem caminhos fixos; sem atualização, o build falha antes de validar a feature. A nova suíte transforma os critérios da spec em falha de build e segue o precedente das features 008/009.
- **Alternatives considered**: confiar apenas na revisão manual (rejeitada — não falha o build); adicionar as verificações a `PathRenameContractTest` (rejeitada — a suíte é sobre o rename de raiz da feature 006; a responsabilidade nova fica mais clara em uma suíte própria).

### D8 — Artefato empacotado: `target/classes` no gate; JAR instalado opt-in

- **Decision**: a verificação obrigatória lê `target/classes/llm-maven-flags-inference` e a árvore-fonte. A verificação do JAR instalado continua opt-in com `-Deda.verify.installed.jar=true` e não é gate de aceite desta feature.
- **Rationale**: o build já empacota `src/main/resources` em `target/classes`; as suítes existentes leem essa cópia. O JAR instalado depende de `mvn install` e de um ambiente limpo; a spec não definiu esse gate.
- **Alternatives considered**: exigir o JAR instalado como gate (rejeitada — a spec não define esse critério e o CI já cobre `clean install`); validar apenas a árvore-fonte (rejeitada — não prova o artefato empacotado).

### D9 — Performance: sem meta nova; guarda de regressão de 10%

- **Decision**: a feature não cria meta de performance. Se o build for medido, usar o orçamento da feature 007 — aumento máximo de 10% sobre a linha de base local de `mvn -B test` — apenas como guarda de regressão, não como critério de aceite.
- **Rationale**: renomear arquivos e atualizar referências não deve alterar o tempo de build de forma relevante; a meta de 10% já existe no projeto e evita uma decisão nova sem evidência.
- **Alternatives considered**: definir um teto absoluto (rejeitada — depende da máquina); ignorar o tempo de build (rejeitada — perde a guarda existente).

### D10 — Nenhuma dependência, flag ou arquivo de procedimento novo

- **Decision**: a implementação usa as ferramentas e dependências já presentes. Não cria flag, propriedade de catálogo, fileset, arquivo em `archetype-resources/` ou arquivo novo no procedimento. A única adição é a suíte de contrato em `src/test/archetype-post-generate/`.
- **Rationale**: a feature é nominal e documental; qualquer adição ao procedimento violaria as restrições da spec e os Princípios I e II.
- **Alternatives considered**: script dedicado de renomeação (rejeitada — pode ser feito com PowerShell e o manifesto); biblioteca de manipulação de JSON (rejeitada — `groovy-json` já existe e a mudança é textual).

### D11 — Divisão entre as fases

- **Decision**: a Fase 1 renomeia 01 e atualiza as referências a entrada e índice no procedimento, README, redirect e suítes; não renomeia os cinco arquivos de 02. A Fase 2 renomeia 02, atualiza as referências aos cinco arquivos, conclui o README e faz a varredura final.
- **Rationale**: a subpasta 01 é o gateway de leitura e pode ser demonstrada isoladamente. A Fase 2 depende da Fase 1 apenas para a conferência final de zero nomes antigos; a renomeação dos cinco arquivos é independente.
- **Alternatives considered**: uma fase única (rejeitada — perde o checkpoint e o MVP da Fase 1); três fases com o README separado (rejeitada — não há resultado independente que justifique).

### D12 — Nomes exatos, apóstrofo e sistema de arquivos

- **Decision**: o novo nome da porta de entrada é `00-entry-point.md`; o apóstrofo final em `00-entry-point'` é pontuação da demanda. Os nomes são aplicados exatamente como listados, inclusive em sistema de arquivos insensível a maiúsculas e minúsculas. Uma colisão aborta a subpasta antes de qualquer sobrescrita.
- **Rationale**: a spec registra a premissa do apóstrofo e a exigência de nomes exatos; a pré-checagem evita perda de arquivo.
- **Alternatives considered**: manter o sufixo `leia-este-arquivo-primeiro` (rejeitada — a demanda pede `00-entry-point`); aceitar o apóstrofo como parte do nome (rejeitada — não é um nome de arquivo válido na intenção da demanda).

## Inventário consolidado

- **7 arquivos renomeados**: 2 em `01-entrada-inicializacao` (Fase 1) e 5 em `02-operacao-procedimento-do-agente` (Fase 2).
- **55 ocorrências no procedimento**: 35 na entrada, 14 no índice, 1 em `prompt-01`, 1 em `prompt-03`, 1 em `prompt-04`, 1 em `03/01` e 2 em `03/04`.
- **17 ocorrências no README**: 15 linhas, incluindo os cinco templates de spec.
- **1 ocorrência no redirect legado**: linha 7.
- **Suítes**: 7 existentes com referências + 1 nova suíte phase-aware.
- **Contagens preservadas**: 19 arquivos, 13 Markdown, 6 JSON, 4 subpastas, 2 em 01 e 5 em 02.
- **Catálogo**: 35 propriedades e 18 filesets; sem alteração.

## Lacunas resolvidas

| Lacuna da entrada do Plan | Decisão |
|---|---|
| Inventário nominal das suítes de verificação e das referências antigas por arquivo | D3 + D4 + `contracts/rename-manifest.md`: 55 ocorrências no procedimento, 17 no README, 1 no redirect e 7 suítes existentes mapeadas por arquivo/linha/fase |
| Separação entre caminho canônico e caminho legado | D5 + D7: a suíte nova e `PathRenameContractTest` distinguem `00-entry-point.md` do redirect antigo |
| Estratégia de referências dentro dos sete arquivos | D2: só tokens de nome mudam; regras, schema e exemplos permanecem |
| Verificação no artefato instalado | D8: `target/classes` no gate; JAR instalado opt-in, não gate |
| Metas de performance | D9: sem meta nova; guarda de regressão de 10% da feature 007, não gate |
| Divisão das referências do README entre as fases | D6 + D11: entrada/índice na Fase 1; prompt-03/04/05 na Fase 2 |

## Restrições preservadas

- Não criar nem remover arquivos do procedimento.
- Não renomear as subpastas 01 e 02.
- Não alterar regras, catálogo, estrutura de templates, ordem de leitura ou formato do contrato.
- Não reescrever specs históricas, `graphify-out/` ou `target/`.
- Não adicionar dependências, flags ou arquivos de extensão.
- Não executar git fora dos hooks declarados.
- Manter o redirect legado funcional.

## Riscos e mitigações

| Risco | Mitigação |
|---|---|
| Referência antiga escapar na Fase 1 | Manifesto por arquivo/linha + `ProcedureEntryRenameContractTest` + varredura final |
| A Fase 1 tocar nos nomes de 02 | Checklist de fase: 02 mantém os cinco nomes até a Fase 2 |
| Token `prompt-` ser substituído em contexto errado | Substituir apenas nomes completos e tokens conhecidos; não usar regex genérica |
| O redirect legado ser tratado como referência quebrada | Exceção explícita no manifesto e na suíte |
| `PathRenameContractTest` misturar canonical e legacy | Variáveis separadas e asserções específicas |
| `FileRenameContractTest` apontar para o índice antigo | Atualizar o caminho do índice na Fase 1 |
| `target/classes` obsoleto | `mvn clean install` antes da verificação do artefato |
| Nova suíte não registrada | Atualizar o runner; `CoverageRegistryTest` e `ReadmeConsistencyTest` falham |
| Conteúdo normativo mudar | Baseline + revisão de diff; apenas tokens podem diferir |
| Specs históricas ou saída gerada serem alteradas | Exclusão explícita na varredura e no manifesto |
| Colisão de nome | Pré-checagem de destino; abortar a subpasta |
| README com link quebrado | Manter links de diretório; só trocar nomes de arquivo |
