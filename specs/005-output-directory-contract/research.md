# Research: Contrato de Destino de Geração (outputDirectory) no Procedimento Empacotado

**Feature**: `005-output-directory-contract`
**Date**: 2026-11-19
**Spec**: [spec.md](./spec.md)

Pesquisa que resolve os pontos técnicos em aberto da spec. Cada decisão registra o que foi escolhido, por quê, e o que foi descartado.

---

## D1. Qual propriedade transporta o destino de geração?

**Decisão**: a propriedade `outputDirectory` — **nativa do `maven-archetype-plugin`**, Specifically o parâmetro `outputDirectory` do goal `archetype:generate`.

**Evidência (3 níveis)**:

1. **Documentação oficial** — `https://maven.apache.org/plugins/maven-archetype-plugin/generate-mojo.html`
   A URL não é versionada no path (`/3.4.1/generate-mojo.html` retorna **404**); a URL canônica serve a 3.4.1, confirmado pelo cabeçalho da própria página (`Version: 3.4.1`) e pelo `Full name: org.apache.maven.plugins:maven-archetype-plugin:3.4.1:generate`.
   ```
   <outputDirectory>
   Type: java.io.File
   Required: No
   User Property: outputDirectory
   Default: ${basedir}
   ```

2. **Bytecode do plugin** — `maven-archetype-plugin-3.4.1.jar` → `org/apache/maven/archetype/mojos/CreateProjectFromArchetypeMojo.class`
   ```
   outputDirectory >> Ljava/io/File; >> getAbsolutePath >> setOutputDirectory
   ```

3. **Bytecode do núcleo** — `archetype-common-3.4.1.jar` → `org/apache/maven/archetype/ArchetypeGenerationRequest.class`
   ```
   getOutputDirectory / setOutputDirectory / outputDirectory
   ```

**Rationale**: o `User Property` declarado é o que torna a propriedade passável por `-D`. Nenhuma invention é necessária — a ferramenta já oferece o mecanismo exato.

**Alternatives considered**:
- **Criar uma flag no catálogo** (`output_directory`) — **rejeitada**: violaria o Princípio I da Constitution (capacidade opcional condicional por flag — mas o destino é entrada obrigatória), quebraria `BuildArtifactsTest` 2.3 (35 flags), `ProcedureContractTest` 1.10 (allowlist de flags não citadas) e `ReadmeConsistencyTest` 2.1. A spec proíbe explicitamente (FR-024).
- **Propriedade ad-hoc do archetype** (ex.: `-Deda_output_dir`) — **rejeitada**: exigiria interceptar o mojo do plugin ou pós-processar a saída; o mecanismo nativo já resolve.

**Impacto na spec**: FR-009 (propriedade nativa, não flag), FR-020 (catálogo inalterado), FR-028 (escopo exclusivo no destino).

---

## D2. Qual é o default e o que ele significa?

**Decisão**: o default é `${basedir}` — o diretório onde o Maven executa. **Não pode valer sem decisão explícita do operador.**

**Rationale**: quando o agente opera dentro da pasta do archetype (que tem `pom.xml`, satisfazendo o `executePhase` do goal), `${basedir}` **é** a própria árvore do archetype. Ou seja, o modo de falha que a spec 005 combatia — geração dentro da árvore do archetype, arquivos não versionados, dois `pom.xml` aninhados — **é o comportamento padrão da ferramenta quando o destino não é informado**. Isso explica e motivationa a exigência de destino explícito: a feature não cria a propriedade, torna obrigatório o seu preenchimento.

**Alternatives considered**:
- **Assumir um default seguro** (ex.: pasta temporária) sem perguntar — **rejeitada**: violaria SC-003 (0 destinos presumidos em silêncio) e o espírito de `prompt-01` §4 ("pare; diga qual regra falta; não improvise"). Um destino presumido produz resultado plausível e errado.
- **Exigir que o agente rode de fora da árvore** (mecanismo `cd` + `robocopy` já existente) — **rejeitada**: é o mecanismo conflitante que a spec manda remover (FR-011); move o módulo depois de gerado e usa caminho fixo de usuário.

**Impacto na spec**: FR-021 (nova), SC-001, e o parágrafo de `Contexto de origem`.

---

## D3. Como o marcador `PASTA DE DESTINO` é escrito, e por que essa forma?

**Decisão**: o marcador é `PASTA DE DESTINO` — **palavra composta, em maiúsculas**.

**Rationale (três restrições convergentes)**:

1. **Colisão semântica** — a decisão do Clarify: a marcação é uma palavra composta que **jamais** pode ser uma chave de contrato. Dentro do contrato, toda chave é atributo da entidade; a lista de metachaves do procedimento **não ganha** entrada nova (FR-022).
2. **Gate `ProcedureContractTest` 1.4** — todo termo entre backticks em `utils/*.md` que case com `^[a-z][a-z0-9_]*_[a-z0-9_]+$` e **não** seja flag do catálogo **quebra a suíte**. Verificado:

   | Termo | Casa com a regex? | Resultado |
   |---|---|---|
   | `PASTA DE DESTINO` | não | ✅ seguro |
   | `DESTINO` | não | ✅ seguro |
   | `OUTPUT DIRECTORY` | não | ✅ seguro |
   | `outputDirectory` | não | ✅ seguro (camelCase) |
   | `output_directory` | **sim** | ❌ quebraria a suíte 1.4 |
   | `PASTA_DESTINO` | **sim** | ❌ quebraria a suíte 1.4 |
   | `pasta_de_destino` | **sim** | ❌ quebraria a suíte 1.4 |

   A forma maiúscula com espaço é, portanto, **obrigatória**, não apenas preferida.
3. **Convenção do spec** — os marcadores existentes (`USE-CASE`, `CONTRATO`) são em maiúsculas.

**Alternatives considered**: `DESTINO` (curto, mas palavra simples — poderia colidir com chave de contrato); `OUTPUT DIRECTORY` (inglês, fora do vocabulário do procedimento).

---

## D4. Quais arquivos do procedimento carregam a regra?

**Decisão**: 6 dos 19 arquivos são alterados; **nenhum é criado**.

| Arquivo | O que muda | FR |
|---|---|---|
| `prompt-00-leia-este-arquivo-primeiro.md` | §0.2 (avaliação dos gates), §0.3 (relatório de parada), §2 (entradas permitidas → 5), §6 (condições de parada), §7 (checklist rápido) | FR-001..005, FR-008, FR-021 |
| `prompt-03-regras-obrigatorias-de-geracao.md` | §3 (mecanismo único: `-DoutputDirectory`, remoção do `cd`+`robocopy` e do caminho `C:\Users\edufi\Downloads`), §4 (escape do caminho) | FR-010..012, FR-021 |
| `prompt-04-fluxo-de-execucao-e-verificacao.md` | §1.2 (checklist), §2 (command line com `-DoutputDirectory`), §3 (validação pós-geração) | FR-009, FR-013..017 |
| `prompt-05-entregaveis-e-relatorios.md` | §4 (resumo final: novo campo "destino de geração") | FR-018, FR-019 |
| `regras-01-como-derivar-a-entidade-do-contrato.md` | §1 (entradas do spec → 5), §4.2 (lista de metachaves: a marcação não é chave de contrato) | FR-001, FR-022 |
| `regras-06-como-relatar-proveniencia.md` | §2/§8 (o destino não é flag, não entra na tabela, não ganha fonte nova) | FR-020 |

**Não alterados**: `indice-01` (nenhum arquivo novo → lista e ordem intactas), `prompt-01` (§4 já cobre a atitude "pare, não improvise"), `prompt-02`, `regras-02..05`, `template-01..06`.

**Rationale**: a distribuição acompanha o fluxo natural do procedimento — `prompt-00` (entrada e gate), `prompt-03` (regra de emissão), `prompt-04` (execução e validação), `prompt-05` (relato), `regras-01` (classificação do que é do spec e do que é do contrato), `regras-06` (o que não é flag).

**Alternatives considered**:
- **Criar um 20º arquivo** (ex.: `prompt-06-destino-de-geracao.md`) — **rejeitada**: quebraria `BuildArtifactsTest` 5.2/5.3/5.4, a lista de integridade de `prompt-00` §8 e `ReadmeConsistencyTest` 2.3. A spec proíbe (FR-025).
- **Colapsar tudo em `prompt-04`** — **rejeitada**: `prompt-00` é a porta de entrada; a regra do destino precisa estar visível **antes** da derivação (é o gate que interrompe o fluxo), não só na fase de execução.

---

## D5. Como a regra é verificada?

**Decisão**: **duas camadas** de verificação automatizada.

**Camada 1 — `ProcedureContractTest.groovy` (estendida)**: checks novos sobre o corpus do procedimento:
- a marcação `PASTA DE DESTINO` está declarada em `prompt-00` e em `regras-01`;
- `prompt-04` contém `-DoutputDirectory` no template da command line;
- `prompt-05` contém o campo de destino no resumo final;
- a lista de metachaves de `regras-01` **não** foi ampliada (a marcação nunca vira metachave de contrato);
- **nenhum** arquivo de `utils/` contém caminho absoluto de usuário (padrão `C:\Users\` / `\\Users\\`).

**Camada 2 — suíte nova `DestinationContractTest.groovy`**: verificação dedicada, registrada em `run-archetype-post-generate-tests.groovy` (exigida por `CoverageRegistryTest` 2.1/2.2):
- `utils/` tem exatamente 19 arquivos (contagem travada);
- o catálogo tem exatamente 35 `requiredProperty` e 18 `fileSet` (contagens travadas);
- o procedimento **não** contém `robocopy` nem `cd C:\` (mecanismo conflitante removido);
- a propriedade `outputDirectory` **não** aparece no catálogo (é nativa);
- o procedimento cita a propriedade nativa pelo nome exato;
- **leitura a partir do artefato reinstalado**, não da árvore de desenvolvimento (FR-032).

**Rationale**: a camada 1 cobre "a regra está lá"; a camada 2 cobre "a regra está lá **e** nada regrediu" — as contagens que `BuildArtifactsTest` e `ReadmeConsistencyTest` já publicam. A leitura do artefato instalado honra o Princípio VI (o modo de falha recorrente deste projeto é o artefato em cache desatualizado).

**Alternatives considered**:
- **Só a suíte nova** — rejeitada: a extensão da existente evita duplicar o boilerplate de leitura do corpus de `utils/` que `ProcedureContractTest` já implementa.
- **Verificação por fixture de integração** — **rejeitada**: as fixtures geram projetos em diretório temporário do Maven e **não** exercitam o agente; não provam que o procedimento declara a regra.

---

## D6. O que muda no README?

**Decisão**: 2 seções do README são atualizadas.

| Local | Mudança | Razão |
|---|---|---|
| §7 (prompts de homologação manual) | trocar a instrução ad-hoc *"Gere FORA da árvore do archetype: use uma pasta de trabalho com pom.xml ou passe `-DoutputDirectory=<pasta temporaria>`"* pela entrada `PASTA DE DESTINO` no formato do spec | o prompt de homologação **simula o spec** que o operador entrega — ele precisa demonstrar a entrada nova |
| Seção "Inferência de Contrato e Use-Case" (linhas ~645-780) | citar o destino como 5ª entrada; manter o exemplo `-DoutputDirectory=C:\Temp\eda-sandbox` como **ilustração**, referenciando a regra em `utils/` | o README **não** é fonte de runtime (`prompt-00` §3/§5, `prompt-01` §2) — ele aponta para a regra, não a substitui |

**Rationale**: `README.md:1824` já declara *"O detalhamento do procedimento não vive mais no README. Ele está empacotado no JAR"*. O README deve **referenciar** a regra, nunca ser a única fonte.

**Impacto nas contagens**: `ReadmeConsistencyTest` 2.3 exige que o README publique `${utilsCount} arquivos` — a contagem **não muda** (19), logo nenhum número publicado precisa ser corrigido. Verificado: nenhuma das contagens (19 arquivos, 35 flags, 18 filesets, 64 fragmentos, 22 fixtures) muda.

---

## D7. Como o agente executa sem quebrar o requisito de `pom.xml`?

**Decisão**: o agente continua operando no diretório do archetype (que tem `pom.xml`) e emite `-DoutputDirectory` com o destino do spec. **A feature não altera** o requisito de `pom.xml` no diretório de execução.

**Rationale**: o goal `generate` declara `executePhase` (`generate-sources`) → o Maven exige um projeto no diretório de execução. Rodar de uma pasta vazia falha com *"requires a project to execute but there is no POM"* (README linha 777). Como o agente já opera dentro do archetype, o requisito já é satisfeito — a feature apenas garante que a **saída** vá para fora da árvore. Emitir `-DoutputDirectory` **não** elimina o requisito de `pom.xml`; redireciona apenas o destino da escrita.

**Consequência**: o destino do `outputDirectory` (fora da árvore) e o diretório de execução (a raiz do archetype, com `pom.xml`) são **coisas diferentes** — e essa distinção é o que a feature precisa deixar explícita no procedimento, para que ninguém confunda "destino do projeto" com "diretório de execução".

---

## Pendências resolvidas

Todos os `NEEDS CLARIFICATION` do Technical Context foram resolvidos nesta pesquisa:

| Item | Resolvido por |
|---|---|
| Linguagem/versão | D3, D5 (Markdown + Groovy 2.4.9 no build; sem código de produção novo) |
| Dependências primárias | D1 (plugin 3.4.1 e o conjunto existente do `pom.xml`) |
| Storage | N/A — feature documental + verificação |
| Framework de teste | D5 (suítes Groovy plain-script + runner; sem JUnit novo) |
| Plataforma-alvo | D7 (Windows/PowerShell no caminho crítico; procedimento agnóstico quanto ao SO) |
| Tipo de projeto | build-tool / archetype-packaging |
| Metas de performance | N/A |
| Restrições | D3 (gate 1.4), D4 (contagem de 19), D2 (catálogo inalterado) |
| Escala/escopo | D4 (6 arquivos de `utils/`, 1 suíte, 1 registro, 2 seções de README) |

**Nenhum `NEEDS CLARIFICATION` remanescente.**
