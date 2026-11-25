# Research: Renomear Arquivos das Subpastas 01 e 02 em Duas Fases

**Feature**: `008-rename-procedure-files` | **Date**: 2026-11-25 | **Spec**: [spec.md](./spec.md)

## Contexto

A spec tem três lacunas registradas no contexto do Plan: critério de verificação automatizada diante das suítes que travam os nomes antigos, tratamento do redirect legado e verificação do JAR instalado. As decisões abaixo resolvem as três sem alterar o escopo da spec.

## Decisão 1 — Aceitação por inspeção de diretório e hash, sem atualizar as suítes

**Decision**: A aceitação de cada fase usa listagem de diretório, manifesto SHA-256 e conferência das subpastas não tocadas. Nenhuma suíte, fixture ou documento operacional é editado. `mvn -B test` não é gate da feature.

**Rationale**: A FR-013 proíbe atualizar referências cruzadas nas verificações existentes, e a SC-006 exige zero atualizações. A própria spec registra a defasagem das citações antigas como consequência conhecida e aceita. Atualizar as suítes Now violaria a spec e anteciparia a feature posterior de referências.

**Alternatives considered**:
- Atualizar as seis suítes para os novos nomes: viola FR-013 e SC-006; rejeitado.
- Criar uma suíte paralela só para esta feature: aumenta escopo, não conserta o build e continua deixando as suítes antigas vermelhas; rejeitado.
- Não verificar nada: viola SC-001 a SC-005; rejeitado.
- Rodar `mvn -B test` e declarar falha esperada como aceite: ruidoso e não prova os critérios da fase; rejeitado como gate, mantido apenas como registro opcional.

**Consequência aceita**: o build de testes fica vermelho até a feature de atualização de referências. O relatório de implementação MUST nomear as suítes e os checks afetados.

## Decisão 2 — Redirect legado permanece intocado

**Decision**: `src/main/resources/utils/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md` não é editado, nem removido, nem duplicado.

**Rationale**: O redirect é um documento operacional fora das subpastas 01 e 02. Atualizá-lo seria uma referência cruzada (FR-013); removê-lo seria destrutivo e fora do escopo; duplicá-lo violaria FR-012.

**Alternatives considered**:
- Apontar o redirect para `01-entryPoint.md`: viola FR-013 e amplia o escopo para `utils/`; rejeitado.
- Remover o redirect: ação destrutiva não pedida; rejeitado.
- Manter o redirect e também criar um novo em `llm-maven-flags-inference/`: viola FR-012 e cria caminho duplicado; rejeitado.

**Consequência aceita**: specs antigas que seguem o redirect chegam a um caminho que não existe mais. O conserto fica para a feature posterior de referências.

## Decisão 3 — Verificação do JAR instalado fica fora do aceite

**Decision**: A verificação opt-in do JAR instalado não é executada nesta feature. A conferência do conteúdo empacotado, se necessária, usa `target/classes` após `mvn -B clean process-resources`.

**Rationale**: A checagem opt-in existente espera os caminhos antigos e não pode passar sem edição da suíte, o que a FR-013 proíbe. `package`/`install` executam a fase `test`, que falha pelas suítes conhecidas. `process-resources` copia `src/main/resources` sem rodar testes e prova que os novos nomes entram na árvore de classes.

**Alternatives considered**:
- Editar a checagem opt-in: viola FR-013; rejeitado.
- Rodar `mvn -B package -DskipTests`: o runner Groovy não é o surefire e não há garantia de que o plugin seja pulado; rejeitado como critério.
- Montar um ZIP manual de `target/classes`: não é o JAR real do archetype e não prova o empacotamento; rejeitado.

**Consequência aceita**: a validação do artefato instalado é adiada para a feature posterior de referências.

## Decisão 4 — Mecanismo de renomeação

**Decision**: Renomear com `Rename-Item` do PowerShell, dentro do mesmo diretório. Em POSIX, o equivalente é `mv` no mesmo diretório.

**Rationale**: Não usa comando git, não reescreve o arquivo, preserva o conteúdo e falha se o destino já existir. O git reconhece a renomeação por similaridade de conteúdo quando o working tree for analisado.

**Alternatives considered**:
- `git mv`: comando git não autorizado pelo protocolo do projeto; rejeitado.
- Copiar e apagar: cria duplicata temporária, abre margem para edição acidental e não é atômico; rejeitado.
- `Move-Item` com caminho completo de destino: funciona, mas aumenta a chance de erro de caminho; `Rename-Item` é mais restrito e seguro.
- Renomear pelo IDE: risco de reescrita/formatação e de edição de conteúdo; rejeitado.

## Decisão 5 — Prova de conteúdo por hash SHA-256

**Decision**: Capturar um manifesto SHA-256 dos arquivos-alvo antes de cada fase, em arquivo temporário fora do repositório, e comparar os hashes depois do rename.

**Rationale**: Prova que o conteúdo é byte a byte idêntico sem depender de git, de cópia de backup ou de leitura manual. O manifesto fica fora do repositório para não criar arquivo novo no projeto.

**Alternatives considered**:
- Detecção de rename pelo git: exigiria stage/index e comandos git não autorizados; rejeitado.
- Cópia de backup dos arquivos antigos: cria cópias com nomes antigos, o que conflita com FR-012; rejeitado.
- `Compare-Object` entre origem e destino: a origem deixa de existir após o rename; rejeitado.
- Revisão visual: não é mensurável; rejeitado.

**Formato do manifesto**: lista de objetos com caminho absoluto, hash e data de captura, em JSON no diretório temporário do harness.

## Decisão 6 — Independência das fases

**Decision**: Cada fase captura seu próprio manifesto imediatamente antes de executar. "Pasta não tocada" significa "inalterada durante a fase", não "igual ao estado original do repositório".

**Rationale**: A FR-010 exige que cada fase seja executável e verificável isoladamente. Se a Fase 2 rodar depois da Fase 1, a subpasta 01 já estará renomeada; se rodar sozinha, estará no estado original. O manifesto local resolve os dois casos.

**Alternatives considered**:
- Um manifesto global único capturado antes da Fase 1: obrigaria a ordem 1 → 2 e violaria a independência; rejeitado.
- Assumir sempre a ordem 1 → 2: viola FR-010; rejeitado.

## Decisão 7 — Nomes exatos e sensibilidade a maiúsculas

**Decision**: Usar os nomes literalmente como na spec, incluindo `01-entryPoint.md` com `E` maiúsculo. Não há renomeação apenas de caixa.

**Rationale**: Todos os renomes mudam o nome inteiro, não apenas a caixa; por isso funcionam igualmente em Windows e POSIX. A grafia literal atende à premissa da spec.

**Alternatives considered**:
- Normalizar para minúsculas (`01-entrypoint.md`): contraria a demanda e a premissa; rejeitado.
- Criar variantes de caixa: violaria a unicidade e a contagem; rejeitado.

## Decisão 8 — Suítes afetadas e ordem das falhas

**Decision**: Registrar nominalmente as suítes que falharão; não corrigi-las nesta feature.

**Suítes e motivo**:

| Suíte | Dependência dos nomes antigos | Falha a partir de |
|---|---|---|
| `PathRenameContractTest` | Lista os 19 caminhos, o caminho canônico da entrada e o redirect | Fase 1 |
| `BuildArtifactsTest` | Lista os 19 nomes exatos na origem e em `target/classes` | Fase 1 |
| `HardStopContractTest` | Lê `prompt-00` para registro, mensagens e dicionário | Fase 1 |
| `DestinationContractTest` | Lê `prompt-00`, `prompt-03`, `prompt-04` e `prompt-05` | Fase 1 (p00); Fase 2 (p03–p05) |
| `ReadmeConsistencyTest` | Verifica o caminho `prompt-00` nos templates e lê `prompt-04` | Fase 1 (p00); Fase 2 (p04) |
| `CatalogSyncContractTest` | Lê `prompt-03` em `target/classes` | Fase 2 |

**Rationale**: A lista permite distinguir falha esperada de regressão real e alimenta o relatório de implementação e a feature posterior.

**Alternatives considered**:
- Corrigir só as suítes: viola FR-013 e cria inconsistência com README/redirect; rejeitado.
- Ignorar as suítes sem registrar: impede distinguir falha esperada de regressão; rejeitado.

## Decisão 9 — Ordem recomendada

**Decision**: Executar Fase 1 e depois Fase 2. A ordem é recomendada, não obrigatória.

**Rationale**: Mantém a leitura do procedimento na sequência planejada e simplifica a conferência manual. Como cada fase tem manifesto próprio, a ordem inversa também é válida.

**Alternatives considered**:
- Tornar a ordem obrigatória: violaria FR-010; rejeitado.

## Lacunas remanescentes

Nenhuma. As três lacunas do contexto do Plan foram resolvidas por decisão registrada; nenhuma exige marcação de clarificação no plano.
