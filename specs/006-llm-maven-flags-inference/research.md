# Phase 0 — Research: llm-maven-flags-inference

**Feature**: 006-llm-maven-flags-inference | **Date**: 2026-11-23

## Inventário das referências ao caminho antigo

| Origem | Ocorrências | Ação |
|---|---|---|
| Arquivos do procedimento | 4 dos 19 arquivos (`prompt-00`, `indice-01`, `prompt-01`, `prompt-02`) | Trocar o prefixo do caminho; manter o texto das regras |
| README | Tópicos de tecnologias, etapas, inferência, especificação e testabilidade | Trocar o caminho e acrescentar o tópico novo |
| Templates do kit | `spec-template.md`, `plan-template.md`, `tasks-template.md` | Trocar o nome do conceito |
| Suítes de contrato | 4 suítes leem `utils/` | Apontar para a nova raiz e para o stub |
| Fixtures | 3 comentários em `archetype.properties` | Trocar o caminho |
| Specs históricas | 003, 004 e 005 | Não tocar |
| Saída gerada | `graphify-out/` | Não tocar |

## Decisões

### D1 — Mover a árvore e trocar apenas o prefixo das referências

- **Decision**: mover no sistema de arquivos os 4 subdiretórios para `src/main/resources/llm-maven-flags-inference/` e trocar `utils/` por `llm-maven-flags-inference/` nas referências internas dos 4 arquivos que as contêm.
- **Rationale**: preserva os 19 nomes, a distribuição e a ordem de leitura; mantém um único caminho canônico; não duplica regra.
- **Alternatives considered**: copiar a árvore (rejeitado — duas fontes e drift); usar link simbólico/junction (rejeitado — não viaja no JAR); manter `utils/` como raiz (rejeitado — nome genérico, objetivo da feature).

### D2 — Redirect único no entry point antigo

- **Decision**: o caminho antigo fica com um único arquivo, no mesmo caminho relativo do `prompt-00` antigo, contendo apenas o ponteiro para a nova entrada.
- **Rationale**: todo spec antigo cita esse arquivo no preâmbulo obrigatório; o redirect resolve de forma determinística, sem depender de listagem de diretório e sem duplicar regra.
- **Alternatives considered**: um `README.md` na raiz antiga (rejeitado — o spec antigo falha ao abrir o arquivo citado); stub nos 19 caminhos (rejeitado — ruído e 19 arquivos sem valor); manter a árvore antiga inteira (rejeitado — duplicação).

### D3 — Nome único do conceito nos templates do kit

- **Decision**: substituir `eda-llm-flag-inference` por `llm-maven-flags-inference` nos templates de spec, plano e tarefas.
- **Rationale**: o nome vale para a pasta física e para o conceito; dois nomes perpetuam a ambiguidade que a feature elimina.
- **Alternatives considered**: manter o nome antigo do conceito (rejeitado — contraria a spec); trocar só no README (rejeitado — templates continuariam divergentes).

### D4 — Tópico novo do README

- **Decision**: criar um tópico H2 independente, com o título exato `llm-maven-flags-inference`, logo após a seção de etapas de processamento; nele, a sequência em 5 passos e uma subseção H3 curta por subpasta. Incluir as entradas no índice do README.
- **Rationale**: o tópico responde pelo pré-processamento e precisa ser encontrável a partir do índice; a posição o liga às três etapas sem reescrever as seções existentes.
- **Alternatives considered**: subseção dentro da seção de inferência (rejeitado — menos descobrível); reescrever a seção de pré-processamento existente (rejeitado — fora de escopo e risco de duplicar regra).

### D5 — Verificação anti-drift em suíte dedicada

- **Decision**: criar `PathRenameContractTest.groovy`, registrada no runner, que valida a contagem e o layout da nova raiz, o stub, a estrutura do README e a ausência do caminho antigo em documentos operacionais.
- **Rationale**: isola a responsabilidade do rename; mantém `ReadmeConsistencyTest` focado nas contagens publicadas e `ProcedureContractTest` focado no conteúdo das regras; a falha é diagnosticável.
- **Alternatives considered**: estender `ReadmeConsistencyTest` (rejeitado — mistura responsabilidades); verificação manual no quickstart (rejeitado — não falha o build).

### D6 — Atualizar as quatro suítes existentes

- **Decision**: `BuildArtifactsTest`, `ProcedureContractTest`, `DestinationContractTest` e `ReadmeConsistencyTest` passam a ler a nova raiz; `ProcedureContractTest` 1.5 aceita o prefixo `llm-maven-flags-inference/`; `DestinationContractTest` valida a cópia empacotada e o entry point dentro do JAR instalado.
- **Rationale**: são o contrato executável atual; precisam acompanhar a evolução do caminho público.
- **Alternatives considered**: apagar ou fundir suítes (rejeitado — perda de cobertura).

### D7 — Prova no artefato instalado

- **Decision**: a validação executa `mvn -B clean install "-Darchetype.test.skip=true"` (suítes + JAR), depois `mvn -B clean integration-test` (fixtures) e inspeciona o JAR; a suíte nova tem verificação opt-in do artefato instalado, no mesmo padrão da feature 005.
- **Rationale**: Princípio VI da constitution — o JAR em `~/.m2` é a fonte de verdade em runtime.
- **Alternatives considered**: apenas `mvn test` (rejeitado — não prova o JAR); inspeção manual (rejeitado — não reproduzível).

### D8 — Specs históricas intocadas

- **Decision**: 003, 004 e 005 permanecem como estão; a verificação anti-drift as exclui.
- **Rationale**: são registro histórico e estão explicitamente fora de escopo.
- **Alternatives considered**: reescrever as citações (rejeitado — proibido pela spec).

## Riscos e mitigações

| Risco | Mitigação |
|---|---|
| Alguma referência ao caminho antigo escapar | Suíte anti-drift varre README, templates, procedimento empacotado e comentários de fixtures |
| O stub virar uma segunda cópia do procedimento | Teste exige que o caminho antigo tenha exatamente 1 arquivo e que ele não contenha regra |
| Contagem de 19 arquivos mudar durante o move | Teste trava a lista nominal dos 19 arquivos na nova raiz |
| JAR instalado ficar desatualizado | `clean install` antes da inspeção e check opt-in do artefato instalado |
| README documentar nomes de subpasta errados | Teste exige as 4 subseções com os nomes atuais |
