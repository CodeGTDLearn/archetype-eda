# Research — Remover as Preposições "do" e "com" dos Nomes dos Templates de Contrato

**Feature**: 015-remove-contract-template-prepositions | **Date**: 2026-11-26 | **Spec**: [spec.md](./spec.md)

Este documento resolve as decisões técnicas da feature. Não restam `NEEDS CLARIFICATION`: as duas dúvidas de escopo foram resolvidas na clarificação de 2026-11-26.

## D1 — Mecânica de renomeação

- **Decision**: renomear com `Rename-Item` do PowerShell, no próprio diretório, sem copiar e apagar e sem `git mv`.
- **Rationale**: o AGENTS.md global proíbe comandos git sem invocação explícita do usuário; apenas os hooks declarados em `.specify/extensions.yml` executam git. `Rename-Item` preserva o conteúdo e o git detecta a renomeação pela similaridade no commit do hook. A operação é atômica o suficiente para arquivos locais e falha se o destino já existir.
- **Alternatives considered**:
  - `git mv`: rejeitado por violar a regra de não executar git fora dos hooks.
  - copiar + apagar: rejeitado por risco de perda de conteúdo e por não ser uma renomeação.
  - renomear via script Groovy de build: rejeitado por acoplar uma mudança de conteúdo a uma fase de build e por criar efeito colateral repetido.

## D2 — Integridade do conteúdo

- **Decision**: registrar SHA-256 dos seis JSON antes da primeira renomeação e comparar depois de cada fase.
- **Rationale**: a spec exige conteúdo idêntico (FR-010, SC-007). O hash prova que a renomeação não tocou em bytes, independentemente do tamanho ou da formatação do JSON. A renomeação de arquivo não reescreve conteúdo.
- **Alternatives considered**:
  - comparação visual: rejeitada por não ser verificável e por não cobrir arquivos grandes.
  - `git diff`: útil, mas o working tree só mostra renomeações; o hash é a prova direta e independente.

## D3 — Estratégia de atualização das referências

- **Decision**: substituir os tokens exatos dos cinco nomes antigos, com caminho completo e forma curta, apenas em quatro destinos operacionais: mapa dos arquivos e ordem de leitura, porta de entrada, README e suítes de verificação.
- **Rationale**: são os pontos que o procedimento e o build leem. A substituição por token evita reescrever frases, papéis, âncoras e ordem das linhas. Specs históricas, planos, tarefas, `target/` e `graphify-out/` não são corpus operacional (FR-022).
- **Alternatives considered**:
  - varredura global no repositório: rejeitada por reescrever specs históricas e artefatos gerados.
  - atualização só do mapa: rejeitada porque a porta de entrada, o README e as suítes também citam os nomes e passariam a apontar para caminhos inexistentes.

## D4 — Estratégia das suítes de verificação

- **Decision**: estender `ContractTemplateRenameContractTest.groovy` e `FileRenameContractTest.groovy` para reconhecer o estado final desta feature; atualizar as listas fixas de `BuildArtifactsTest.groovy`, `PathRenameContractTest.groovy`, `ContractRuleRenameContractTest.groovy` e `ProcedureContractTest.groovy`. Não criar suíte nova.
- **Rationale**: as duas primeiras já são phase-aware e concentram as provas de nomes, contagens, índice, README e anti-drift; estendê-las mantém a evolução histórica testável e evita duplicação. As quatro restantes têm listas explícitas de caminhos e precisam apenas dos tokens novos. Uma suíte nova aumentaria o escopo sem cobrir nada que as existentes não cubram.
- **Alternatives considered**:
  - nova suíte `ContractTemplatePrepositionRenameContractTest.groovy`: rejeitada por duplicar verificações e exigir registro no runner, contrariando o escopo cirúrgico.
  - substituir as suítes phase-aware por uma única suíte final: rejeitada por perder a prova de que as gerações antigas não voltaram.

## D5 — Estado final esperado nos nomes

- **Decision**: o estado final é `01-schema-contrato.json`, `02-contrato-plano.json`, `03-contrato-objetos-aninhados.json`, `04-contrato-enums.json`, `05-contrato-tipos-do-projeto.json` e `06-contrato-attributes-explicito.json`.
- **Rationale**: aplica as duas clarificações: `05` remove apenas `com` e preserva `do` em `tipos-do-projeto`; `02` não tem preposição-alvo.
- **Alternatives considered**:
  - remover também `do` de `tipos-do-projeto`: rejeitado na clarificação Q1.
  - renomear `02-contrato-plano.json`: rejeitado por não carregar preposição-alvo.

## D6 — Detecção de fase nas suítes phase-aware

- **Decision**: detectar dois estados independentes por presença de arquivo — schema renomeado quando `01-schema-contrato.json` existe; arquivos 3 a 6 renomeados quando `03-contrato-objetos-aninhados.json` existe — e compor o estado esperado a partir deles.
- **Rationale**: as duas user stories podem ser aplicadas em ordens diferentes; um único número de fase não representa as quatro combinações. A composição mantém as suítes verdes em estados intermediários e no estado final.
- **Alternatives considered**:
  - um único `templatePhase = 3`: rejeitado por não tolerar a aplicação isolada de US1 ou US2.
  - exigir o estado final e falhar em estados intermediários: rejeitado por quebrar a independência das histórias.

## D7 — Validação de build

- **Decision**: `mvn -B clean test` é o gate; `mvn clean install` confirma o artefato; `mvn -B clean integration-test` cobre as fixtures.
- **Rationale**: o `maven-resources-plugin` copia recursos para `target/classes` mas não remove cópias de arquivos que deixaram de existir na origem. Sem `clean`, os seis nomes antigos permanecem em `target/classes` e as suítes que comparam a árvore empacotada falham por contagem e por nome. Isso é uma cópia obsoleta, não um defeito da renomeação (Princípio VI da constitution).
- **Alternatives considered**:
  - `mvn -B test` sem `clean`: rejeitado por deixar cópias antigas e produzir falha falsa.
  - apagar `target/` à mão: rejeitado por editar derivados; `clean` é o caminho suportado.

## D8 — Artefato instalado

- **Decision**: a verificação do JAR instalado permanece opt-in (`-Deda.verify.installed.jar=true`) e não é gate.
- **Rationale**: repete a política da feature 014 e da spec (FR-025). O gate é a árvore de origem e o `target/classes` reconstruído.
- **Alternatives considered**:
  - tornar o JAR instalado gate: rejeitado por exigir instalação local e ampliar o tempo de validação sem cobrir um risco novo.

## D9 — Normalização da pasta citada

- **Decision**: a referência `04-templates-de-contato` da demanda é tratada como a pasta existente `04-templates-de-contrato`; o nome da pasta não muda.
- **Rationale**: os exemplos da própria demanda usam `contrato` e a pasta real é `04-templates-de-contrato`.
- **Alternatives considered**:
  - criar ou renomear uma pasta `contato`: rejeitado por não existir e por estar fora do escopo.

## Inventário confirmado

- Subpasta: `src/main/resources/llm-maven-flags-inference/04-templates-de-contrato/` com 6 arquivos.
- Nomes atuais: `01-schema-do-contrato.json`, `02-contrato-plano.json`, `03-contrato-com-objetos-aninhados.json`, `04-contrato-com-enums.json`, `05-contrato-com-tipos-do-projeto.json`, `06-contrato-attributes-explicito.json`.
- Referências operacionais aos cinco nomes antigos:
  - `01-inicializacao/01-mapa-dos-arquivos-e-ordem-de-leitura.md` — linhas 24, 26, 27, 28 e 29.
  - `01-inicializacao/00-entry-point.md` — linhas 721, 723, 724, 725 e 726.
  - `README.md` — linhas 2182, 2184, 2185, 2186 e 2187.
  - `src/test/archetype-post-generate/BuildArtifactsTest.groovy` — linhas 102, 104, 105, 106 e 107.
  - `src/test/archetype-post-generate/PathRenameContractTest.groovy` — linhas 61, 63, 64, 65 e 66.
  - `src/test/archetype-post-generate/ContractRuleRenameContractTest.groovy` — linhas 78, 80, 81, 82 e 83.
  - `src/test/archetype-post-generate/ProcedureContractTest.groovy` — linhas 12, 139, 148 e 199.
  - `src/test/archetype-post-generate/FileRenameContractTest.groovy` — listas `p2Current`/`p2New` e verificações de fase, índice, corpus e README.
  - `src/test/archetype-post-generate/ContractTemplateRenameContractTest.groovy` — listas `p2Current`/`p2New`, checagem 2.3, anti-drift e README.
- Fora do corpus operacional: `specs/**`, `target/**`, `graphify-out/**`, `.git/**` e o JAR instalado.
