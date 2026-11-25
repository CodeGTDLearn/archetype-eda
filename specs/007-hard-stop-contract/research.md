# Phase 0 — Research: 007-hard-stop-contract

**Feature**: 007-hard-stop-contract | **Date**: 2026-11-24

## Objetivo da pesquisa

Fechar as decisões de desenho que a spec não fixa: onde o contrato, o dicionário e o registro vivem; como as mensagens são distribuídas; como o build prova que nenhum site ficou sem código; como a tabela de flags é comparada ao catálogo; e como a parada `STOP-DER-02` convive com os avisos existentes sem ampliar o escopo.

## Decisões

### D1 — Contrato, dicionário e registro no `prompt-00`

- **Decision**: manter o Contrato de Parada Dura (`§0.3`), o dicionário (`§0.4`) e o registro (`§0.5`) no `prompt-00`, que já é o ponto de entrada do procedimento. Nenhum arquivo novo.
- **Rationale**: FR-007 exige o dicionário acessível no ponto de entrada; o Registro de Paradas é uma entidade mantida no ponto de entrada; FR-022 proíbe arquivo novo. O agente já lê o `prompt-00` primeiro.
- **Alternatives considered**: arquivo novo `paradas.md` (rejeitado — FR-022); registro no `indice-01` (rejeitado — o índice é mapa de arquivos, não contrato de mensagem); registro em cada regra (rejeitado — duplicação e drift).

### D2 — 18 mensagens completas no registro, sites apenas citam o código

- **Decision**: `§0.5` contém uma tabela canônica de códigos e gatilhos e 18 blocos de mensagem completos, um por código. Cada site de parada cita **Pare com `STOP-...`** e aponta para `§0.5`; não repete a mensagem.
- **Rationale**: uma única fonte para as 18 mensagens; o site fica curto e o registro é testável; evita que duas cópias da mesma mensagem divirjam.
- **Alternatives considered**: mensagem completa inline em cada site (rejeitado — duplicação em 10 arquivos e risco de drift); tabela única com 10 colunas (rejeitado — ilegível e sem exemplos por opção).

### D3 — Convenção de site: **Pare com `STOP-...`**

- **Decision**: toda ordem de interrupção usa a forma literal **Pare com `STOP-<CATEGORIA>-<NN>`**. O teste varre cada linha com a palavra isolada `pare` e cada bloco `PARADA DURA`, e exige um código registrado. As mensagens do registro usam `Por que parou` e trazem o código no título, então passam pela varredura.
- **Rationale**: dá ao build uma definição determinística de "site de parada"; qualquer linha com `pare` sem código falha e nomeia o arquivo.
- **Alternatives considered**: detectar sinônimos como "interrompa", "recuse" e "não continue" (rejeitado — heurística frágil); confiar só na revisão humana (rejeitado — FR-019 exige falha de build).

### D4 — Décimo campo `Segurança` explícito

- **Decision**: o template canônico e as 18 mensagens têm uma linha `**Segurança:**`, separada de `**O que não fazer:**`. O texto padrão proíbe expor segredos, credenciais ou o contrato completo.
- **Rationale**: FR-001 lista 10 campos, incluindo "o que não fazer" e "segurança"; o template fornecido no adendo tem 9 rótulos (código + 8). Sem a linha explícita, o teste de 10 campos e o SC-001 falham. A linha é a menor adição que fecha a lacuna sem reabrir o escopo.
- **Alternatives considered**: tratar `O que não fazer` como o campo de segurança (rejeitado — dois requisitos distintos, contagem ambígua); mudar a spec (rejeitado — a spec já está aprovada; a implementação fecha a lacuna).

### D5 — `STOP-DER-02` é parada da lista final, não de cada aviso

- **Decision**: a lista derivada é validada contra a regex do catálogo depois da normalização e do descarte já previstos. Se a lista final violar a regex, o fluxo para com `STOP-DER-02`; se um nome individual for descartado e a lista final continuar válida, o comportamento atual (aviso) permanece.
- **Rationale**: FR-008 fala da "lista de atributos derivada que viola a regra de formato"; o fora de escopo proíbe converter outros avisos em paradas. A parada é barulhenta e não gera módulo; os avisos existentes não mudam.
- **Nota de teste**: o teste não exige a ausência da string `String:name`; a mensagem de `STOP-DER-02` a cita como valor proibido. O teste exige que o parágrafo de fallback de `regras-01` §6 tenha sido removido.
- **Alternatives considered**: parar em qualquer atributo inválido (rejeitado — amplia o escopo além da lista); manter o fallback `String:name` (rejeitado — é o defeito central).

### D6 — `String:name` permanece como default inerte

- **Decision**: o valor `String:name` continua no catálogo e continua sendo o valor padrão quando não há atributos (`enable_custom_entity_attributes=false`). Ele não é usado como fallback quando a lista derivada é inválida.
- **Rationale**: FR-023 proíbe alterar o default; a spec assume que ele permanece no catálogo. O defeito é o fallback de uma lista inválida, não o valor inerte do recurso desligado.
- **Alternatives considered**: remover `String:name` do catálogo (rejeitado — FR-023 e comportamento existente); usar a lista vazia (rejeitado — mudaria o default).

### D7 — Descrição do catálogo: comentário apenas

- **Decision**: alterar os comentários `DESCRIPTION` e `DOMAIN` do fragmento `property-20-entity_contract_file_path.xml` para dizer que o procedimento exige caminho absoluto. `defaultValue` `none` e `validationRegex` `^(none|[^\r\n]*\.json)$` permanecem.
- **Rationale**: FR-013 pede a descrição, mantendo a regra de formato do catálogo permissiva por projeto. O comentário viaja para o catálogo construído e pode ser testado.
- **Alternatives considered**: endurecer a regex para exigir absoluto (rejeitado — quebraria projetos que usam caminho relativo e contraria FR-013/FR-023).

### D8 — Sincronia da tabela por defaults + regexes literais + exemplos

- **Decision**: `CatalogSyncContractTest` compara os 9 defaults exatamente; compara as 3 regras de formato que são regex literal (`entity_name`, `entity_package_name` e `custom_entity_attributes_list`, normalizando os placeholders `Tipo`/`nome` para as classes do catálogo); e valida um exemplo positivo e um negativo das 6 regras descritivas contra a regex do catálogo. A tabela continua com exatamente 3 colunas.
- **Rationale**: a tabela é um resumo humano; comparar prosa com regex seria frágil. As 3 regexes literais são o que a History 4 chama de "regra de formato literal"; os exemplos provam que as regras descritivas continuam compatíveis com o catálogo.
- **Nota de implementação**: a célula de default de `entity_contract_file_path` passa a ser `none`; a nota "catálogo; não usado aqui" sai da célula para não confundir a comparação exata dos 9 defaults.
- **Alternatives considered**: comparar todas as células de texto com a regex (rejeitado — formatos diferentes de propósito); substituir a tabela por uma cópia integral das regexes (rejeitado — perde legibilidade).

### D9 — Duas suítes novas, sem dependência nova

- **Decision**: criar `HardStopContractTest.groovy` e `CatalogSyncContractTest.groovy`, registrar as duas no runner e usar apenas Groovy, JSON e XML já presentes.
- **Rationale**: separa contrato de mensagem de sincronia de catálogo; falhas diagnósticas; o runner já existe e o `CoverageRegistryTest` exige que toda suíte esteja registrada.
- **Alternatives considered**: uma suíte única (rejeitado — mistura responsabilidades); teste manual (rejeitado — não falha o build).

### D10 — FR-006, FR-014 e FR-017 nos lugares normativos

- **Decision**: a nota de `FR-006` fica em `regras-04` §3; a exceção de proveniência fica em `regras-06` §2; a diferença pacote × artefato fica em `prompt-03` §2.1 com os dois exemplos.
- **Rationale**: são os pontos onde o leitor já procura a regra; evitam duplicação e mantêm o texto curto.
- **Alternatives considered**: criar uma seção nova (rejeitado — a spec pede usar as seções existentes e o procedimento é promptware); repetir em todos os arquivos (rejeitado — drift).

### D11 — README e JSON intocados

- **Decision**: nenhuma alteração em `README.md`, em `prompt-05`, em `regras-02`, em `regras-03` ou nos 6 JSON.
- **Rationale**: a reestruturação do README está fora de escopo; os avisos de tipo e de aninhamento permanecem; os exemplos de contrato não mudam. Isso preserva FR-022, FR-023 e FR-024.
- **Alternatives considered**: atualizar o README para publicar o registro (rejeitado — fora de escopo e risco de drift); converter avisos de tipo em paradas (rejeitado — fora de escopo).

### D12 — Confirmações seguem o mesmo contrato de mensagem

- **Decision**: `STOP-CONF-01` e `STOP-CONF-02` usam o mesmo template de 10 campos, com `O que não foi executado` dizendo que nada foi gerado até a confirmação. As opções são exatamente as da FR-031/FR-032.
- **Rationale**: FR-012 manda as 2 confirmações seguirem o mesmo contrato; a mensagem de confirmação também precisa orientar o operador e não pode gerar antes da escolha.
- **Alternatives considered**: tratar confirmação como mensagem curta (rejeitado — FR-012); usar template separado (rejeitado — duas linguagens e dois testes).

### D13 — SC-002 com amostra fixa e revisor independente

- **Decision**: a amostra de SC-002 tem 6 mensagens fixas (`STOP-IN-01`, `STOP-ENV-03`, `STOP-DER-01`, `STOP-DER-02`, `STOP-CONF-01`, `STOP-CONF-02`); o revisor não participou da implementação; o resultado fica em uma tabela com as 5 respostas, revisor e data.
- **Rationale**: amostra fixa evita escolha enviesada; 6 mensagens cobrem entrada, ambiente, derivação, a parada central e as 2 confirmações; o registro em tabela torna SC-002 auditável.
- **Alternatives considered**: amostra livre (rejeitado — não auditável); todas as 18 (rejeitado — custo alto para o mesmo critério); revisor que implementou (rejeitado — viés).

### D14 — NFR-001 com baseline local e delta de 10%

- **Decision**: medir `mvn -B test` no mesmo ambiente, após aquecimento, antes e depois da mudança; aceitar `(final - baseline) / baseline ≤ 10%`.
- **Rationale**: o projeto já usa 10% como interpretação de "aumento perceptível"; a medição antes/depois elimina a ambiguidade e permite repetir a verificação.
- **Alternatives considered**: teto absoluto de tempo (rejeitado — depende da máquina); revisão qualitativa (rejeitado — não auditável).

### D15 — NFR-002 com duas execuções offline

- **Decision**: rodar `mvn -o -B test` duas vezes e comparar as linhas `[archetype-post-generate]`; exigir saídas idênticas e nenhuma chamada de rede.
- **Rationale**: duas execuções provam determinismo; o modo offline prova ausência de dependência de rede; a comparação filtrada ignora timestamps do Maven.
- **Alternatives considered**: uma execução offline (rejeitado — não prova determinismo); inspeção manual do código (rejeitado — não executa a prova).

## Restrições preservadas

- Gates avaliados sempre na ordem A → B → C → D.
- `prompt-05`, `regras-02`, `regras-03`, os 6 JSON e o `README.md` não mudam.
- Nenhum aviso de tipo, enum, aninhamento ou import vira parada.
- Nenhuma flag, fileset, dependência ou versão é criada ou alterada.
- `String:name` continua no catálogo como default inerte.
- O procedimento continua promptware, sem motor, CLI ou script externo.

## Riscos e mitigações

| Risco | Mitigação |
|---|---|
| `prompt-00` ficar grande e difícil de navegar | Seções `§0.3`–`§0.5` com rótulos estáveis; teste extrai os blocos por código |
| Mensagem do registro divergir do site | O site só cita o código; a mensagem vive em um único lugar |
| Tabela de 9 flags voltar a quebrar | Teste exige 3 células por linha e 3 colunas no cabeçalho |
| Placeholder `Tipo`/`nome` impedir comparação exata | Normalização explícita para as classes do catálogo; exemplos para as regras descritivas |
| Conversão indevida de avisos em paradas | `STOP-DER-02` só na lista final; `regras-02`/`regras-03` intocadas |
| Teste novo não rodar | Registro obrigatório no runner; `CoverageRegistryTest` 2.1/2.2 falham se faltar |
| JAR instalado ficar obsoleto | `mvn clean install` antes da verificação opt-in |
| Regressão em testes existentes | Preservar âncoras já testadas (`Nunca presuma destino`, `nativa do plugin`, `meta-chaves`, plugin pin) |
| 10º campo ausente nas mensagens | Template e 18 mensagens com `**Segurança:**`; teste conta 10 rótulos |
| Amostra de SC-002 enviesada | Amostra fixa de 6 mensagens; revisor que não implementou; tabela em `validation-report.md` |
| Medição de build ruidosa | Aquecer o cache; medir antes/depois no mesmo ambiente; repetir se o ambiente estiver sob carga anormal |
| Saída offline divergente | Comparar apenas linhas do runner; corrigir a fonte de não determinismo |
| Resultados de validação espalhados | Registro único em `validation-report.md`; quickstart permanece guia reutilizável |

## Baseline confirmado

- Procedimento: 19 arquivos, 13 Markdown, 6 JSON, 4 subpastas.
- Catálogo: 35 `<requiredProperty>` e 18 `<fileSet>` (o `<fileSets>` raiz não conta).
- Tabela crítica: 9 linhas de flag em `regras-04` §3.1; 2 linhas com pipes internos não escapados (`contract_enum_definitions` e `contract_nested_type_definitions`).
- `prompt-00` §0.3 atual diz "Ao parar, informe apenas"; a expressão "informe apenas" aparece 1 vez no procedimento.
- `property-20` atual diz "absoluto ou relativo" no comentário `DOMAIN`.
- `regras-06` §2 permite omitir defaults; `regras-03` §2 manda registrar `contract_nested_type_definitions` = `none`.
