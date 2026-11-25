# Phase 0 — Research: 008-rename-procedure-files

**Feature**: 008-rename-procedure-files | **Date**: 2026-11-25

## Objetivo da pesquisa

Fechar as decisões que a spec não fixa: como renomear sem perder conteúdo, como distinguir referência textual de conteúdo normativo, qual é o inventário completo de referências, como provar a ausência de nomes antigos, como tratar o artefato empacotado e o orçamento de build, e como atualizar os testes existentes sem ampliar o escopo.

## Decisões

### D1 — Manifesto único de renomeação e referências

- **Decision**: `contracts/rename-manifest.md` é a fonte única do mapa `old → new`, do mapa de tokens curtos e do inventário de referências por arquivo e linha. A implementação consome esse manifesto; a suíte `FileRenameContractTest` valida o resultado.
- **Rationale**: a feature tem 12 renomeações e 18 arquivos operacionais com referências. Um manifesto único evita substituições divergentes e permite revisar o escopo antes de editar.
- **Alternatives considered**: substituição global sem manifesto (rejeitada — risco de atingir specs históricas e contextos não referenciais); atualização manual arquivo a arquivo sem inventário (rejeitada — não auditável).

### D2 — Fronteira entre referência textual e conteúdo normativo

- **Decision**: "referência textual" é qualquer ocorrência de um nome antigo de arquivo — completo (`regras-01-...md`, `template-01-...json`) ou curto (`regras-01`, `template-01`) — usada para apontar para o arquivo. "Conteúdo normativo" é todo o restante: regras, definições, exemplos de domínio, chaves e schema JSON, tipos, `defaultValue`, `validationRegex`, metadados estruturais e texto de prosa que não seja a referência. Só o token do nome é substituído; `§`, pontuação, tabelas, links de diretório e o restante da frase permanecem.
- **Rationale**: o Q1 da spec determina atualizar todas as referências, inclusive as internas aos 12 arquivos. Sem uma fronteira explícita, uma substituição ampla poderia alterar regras ou exemplos; sem atualizar os 12 arquivos, sobrariam referências quebradas.
- **Alternatives considered**: não atualizar referências dentro dos 12 arquivos (rejeitada — o índice e os prompts apontariam para nomes novos, mas as regras ainda citariam os antigos); reescrever descrições JSON inteiras (rejeitada — mudaria conteúdo além da referência).

### D3 — Renomeação com `Rename-Item`, sem copiar e apagar

- **Decision**: renomear cada arquivo com `Rename-Item` no mesmo diretório, em ordem crescente de numeral, após verificar que o destino não existe. Não usar copiar + apagar, que muda identidade e pode deixar resíduos. Interromper a subpasta na primeira colisão.
- **Rationale**: preserva o arquivo, o conteúdo e os atributos; a operação é reversível e simples de auditar. A ordem crescente evita confusão quando dois nomes compartilham prefixo.
- **Alternatives considered**: `git mv` (rejeitada — o processo não deve executar git fora dos hooks); copiar e apagar (rejeitada — risco de perda e de arquivo temporário); renomear em lote sem pré-checagem (rejeitada — sobrescrita silenciosa).

### D4 — Substituição por token exato, com exclusões explícitas

- **Decision**: substituir apenas os tokens antigos conhecidos: nomes completos dos 12 arquivos e formas curtas `regras-NN`/`template-NN`. Não usar regex genérica sobre `NN`. Excluir `specs/**`, `graphify-out/**`, `target/**` e `.git/**`.
- **Rationale**: os tokens antigos são inequívocos; uma regex sobre `NN` atingiria números de seção, exemplos e outros identificadores. As specs históricas e a saída gerada não são fontes operacionais.
- **Alternatives considered**: substituir `regras-` e `template-` por vazio (rejeitada — pode atingir texto que não é referência); varrer o repositório inteiro (rejeitada — alteraria specs históricas e `graphify-out/`).

### D5 — JSON: somente o valor de `description`

- **Decision**: nos 6 JSON, o único campo que pode mudar é o valor textual de `description` que contém uma referência antiga. Chaves, schema, tipos, `properties`, exemplos e formatação permanecem. O JSON deve continuar válido.
- **Rationale**: as referências em `template-01`, `template-02` e `template-06` estão em `description`. O schema e os exemplos são o conteúdo normativo que a feature não pode alterar.
- **Alternatives considered**: remover as referências das descrições (rejeitada — perde a navegação); reescrever as descrições (rejeitada — amplia o escopo).

### D6 — README: atualizar tabelas e prosa, preservar links de diretório

- **Decision**: atualizar as referências curtas e os nomes completos nas tabelas e na prosa do README; manter os links para os diretórios `03-derivacao-regras-de-formacao-do-contrato/` e `04-referencia-templates-de-contrato/`, que não mudam. Não reestruturar o tópico nem corrigir dívidas editoriais fora das referências.
- **Rationale**: o README é documentação operacional; as tabelas listam os arquivos e precisam refletir os novos nomes. A estrutura e os links de diretório continuam válidos.
- **Alternatives considered**: deixar o README desatualizado (rejeitada — o Q1 exige todas as referências operacionais); reescrever o tópico (rejeitada — fora de escopo).

### D7 — Testes: atualizar caminhos e rótulos; nova suíte de contrato

- **Decision**: atualizar os caminhos e rótulos de `BuildArtifactsTest`, `PathRenameContractTest`, `ProcedureContractTest`, `HardStopContractTest`, `DestinationContractTest` e `CatalogSyncContractTest`. Adicionar `FileRenameContractTest.groovy` e registrá-la no runner. A nova suíte valida nomes, contagens, índice, ausência de referências antigas no corpus operacional e o catálogo 35/18.
- **Rationale**: as suítes existentes leem caminhos fixos; sem atualização, o build falha antes de validar a feature. A nova suíte transforma os critérios da spec em falha de build.
- **Alternatives considered**: confiar apenas na revisão manual (rejeitada — não falha o build); adicionar as verificações a `PathRenameContractTest` (rejeitada — a suíte é sobre o rename de pasta da feature 006; a responsabilidade nova fica mais clara em uma suíte própria).
- **Nota de autoexclusão**: `FileRenameContractTest` contém os padrões antigos apenas em asserções negativas. A varredura do corpus deve excluir a própria suíte, `specs/`, `graphify-out/` e `target/`.

### D8 — Artefato empacotado: `target/classes` no gate; JAR instalado opt-in

- **Decision**: a verificação obrigatória lê `target/classes/llm-maven-flags-inference` e a árvore-fonte. A verificação do JAR instalado continua opt-in com `-Deda.verify.installed.jar=true` e não é gate de aceite desta feature.
- **Rationale**: o build já empacota `src/main/resources` em `target/classes`; as suítes existentes leem essa cópia. O JAR instalado depende de `mvn install` e de um ambiente limpo; a spec não definiu esse gate.
- **Alternatives considered**: exigir o JAR instalado como gate (rejeitada — a spec não define esse critério e o CI já cobre `clean install`); validar apenas a árvore-fonte (rejeitada — não prova o artefato empacotado).

### D9 — Performance: sem meta nova; guarda de regressão de 10%

- **Decision**: a feature não cria meta de performance. Se o build for medido, usar o orçamento da feature 007 — aumento máximo de 10% sobre a linha de base local de `mvn -B test` — apenas como guarda de regressão, não como critério de aceite.
- **Rationale**: renomear arquivos e atualizar referências não deve alterar o tempo de build de forma relevante; a meta de 10% já existe no projeto e evita uma decisão nova sem evidência.
- **Alternatives considered**: definir um teto absoluto (rejeitada — depende da máquina); ignorar o tempo de build (rejeitada — perde a guarda existente).

### D10 — Nenhuma dependência, flag ou arquivo de extensão novo

- **Decision**: a implementação usa as ferramentas e dependências já presentes. Não cria flag, propriedade de catálogo, fileset, arquivo em `archetype-resources/` ou arquivo novo no procedimento.
- **Rationale**: a feature é nominal e documental; qualquer adição violaria as restrições da spec e os Princípios I e II.
- **Alternatives considered**: script dedicado de renomeação (rejeitada — pode ser feito com PowerShell e o manifesto); biblioteca de manipulação de JSON (rejeitada — `groovy-json` já existe e a mudança é textual).

## Inventário consolidado

- **12 arquivos renomeados**: 6 em `03-derivacao-regras-de-formacao-do-contrato` e 6 em `04-referencia-templates-de-contrato`.
- **18 arquivos operacionais com referências antigas**: 11 arquivos do procedimento, `README.md` e 6 suítes de teste.
- **6 dos 12 arquivos renomeados contêm referências**: `regras-02`, `regras-04`, `regras-06`, `template-01`, `template-02`, `template-06`.
- **6 dos 12 não contêm referências**: `regras-01`, `regras-03`, `regras-05`, `template-03`, `template-04`, `template-05`; nesses, o hash antes/depois deve ser idêntico.
- **Contagens preservadas**: 19 arquivos, 13 Markdown, 6 JSON, 4 subpastas, 6 arquivos em cada uma das subpastas 03 e 04.
- **Catálogo**: 35 propriedades e 18 filesets; sem alteração.

## Lacunas resolvidas

| Lacuna da entrada do Plan | Decisão |
|---|---|
| Metas de performance da feature 008 | D9: sem meta nova; guarda de regressão de 10% da feature 007, não gate |
| Verificação no artefato instalado/JAR | D8: `target/classes` no gate; JAR instalado opt-in, não gate |
| Inventário completo de referências | D1 + `contracts/rename-manifest.md`: 18 arquivos operacionais mapeados por linha |
| Limite entre referência textual e conteúdo normativo | D2 + D5: só tokens de nome mudam; regras, schema, chaves e exemplos permanecem |

## Restrições preservadas

- Não criar nem remover arquivos do procedimento.
- Não renomear as subpastas 03 e 04.
- Não alterar regras, catálogo, estrutura de templates, ordem de leitura ou formato do contrato.
- Não reescrever specs históricas, `graphify-out/` ou `target/`.
- Não adicionar dependências, flags ou arquivos de extensão.
- Não executar git fora dos hooks declarados.

## Riscos e mitigações

| Risco | Mitigação |
|---|---|
| Referência antiga escapar | Manifesto por arquivo/linha + `FileRenameContractTest` |
| Token `NN` ser substituído em contexto errado | Substituir apenas tokens antigos `regras-NN`/`template-NN` e nomes completos |
| Teste existente apontar para nome antigo | Atualizar os 6 suites e o runner; `mvn -B test` |
| Conteúdo normativo mudar | Baseline + revisão de diff; apenas tokens podem diferir |
| JAR instalado obsoleto | `mvn clean install` antes da verificação opt-in |
| Specs históricas ou saída gerada serem alteradas | Exclusão explícita na varredura e no manifesto |
| Colisão de nome | Pré-checagem de destino; abortar a subpasta |
| README com link quebrado | Manter links de diretório; só trocar nomes de arquivo |
