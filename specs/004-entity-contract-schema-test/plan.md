# Implementation Plan: Gate de Conformidade Entidade–Contrato (Schema Test na Aplicação Gerada)

**Branch**: `004-entity-contract-schema-test` | **Date**: 2026-11-13 | **Spec**: [spec.md](spec.md)

**Input**: Instruções de replanejamento fornecidas pelo mantenedor (2026-11-13).

## Summary

O schema test é um artefato **da aplicação gerada**: um conjunto de classes de teste em `<pkg>.<entity_package>.contract` materializado por template Velocity, que lê `src/test/resources/contracts/contract.json` do classpath de teste, compara a entidade por **reflexão de campos declarados** (bidirecional, recursivo em aninhados, com enums e `id` técnico) e faz um **round-trip estrito** com Jackson (`FAIL_ON_UNKNOWN_PROPERTIES=true`). O archetype **não interpreta contrato nem entidade**: ele apenas (a) materializa/remove o artefato por condição derivada e (b) verifica, nos ITs, presença, ausência, execução verde e falha sob sabotagem — sempre executando o teste **dentro** da aplicação gerada.

A entrega inclui ainda a **documentação da feature no README**: subtópico **Contratos** dentro de **Funcionalidades**, em padrão `<details>` e linguagem simples para dois públicos (quem usa o módulo gerado e quem mantém o archetype).

## Technical Context

**Language/Version**: Java 17 (aplicação gerada); Groovy 4.0.28 (pós-processador); Velocity 2.4.1 (templates)

**Primary Dependencies**: Maven 3.9.16; maven-archetype-plugin 3.4.1; Spring Boot 4.0.0 (aplicação gerada); JUnit 5 via `spring-boot-starter-test`; Jackson (JsonNode/ObjectMapper) via `spring-boot-starter-web` — **zero dependências novas no pom gerado**

**Storage**: `src/test/resources/contracts/contract.json` (persistido por `generator-03`, cópia byte-a-byte)

**Testing**: aplicação gerada — JUnit 5 no ciclo dela (`mvn test`/`verify`); archetype — suites Groovy-script (fase `test`) + fixtures IT (fase `integration-test`)

**Target Platform**: build Maven do archetype e das aplicações geradas (Windows/POSIX)

**Project Type**: Maven Archetype (gerador de módulos Spring Boot)

**Performance Goals**: irrelevante — leitura única de um JSON pequeno; O(atributos) por execução

**Constraints**: sem flag nova; zero dependência nova; ativação derivada; o archetype não interpreta contrato/entidade; matriz IT preservada; §Technology Stack da Constituição intocada

**Scale/Scope**: 22 fixtures IT; 34 suites Groovy existentes + 1 nova; contratos até 25 níveis; 8 classes no pacote `contract` (6 de teste + 2 de apoio)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Princípio | Avaliação | Evidência |
|-----------|-----------|-----------|
| I. Modularidade por chave de ativação (NON-NEGOTIABLE) | ✅ PASS | Nenhum flag novo. Existência do artefato derivada de `enable_folder_src_test_java=true` ∧ contrato persistido; remoção garantida quando `entity_contract_file_path` é ausente/`none`/`null`/vazio **ou** o contrato não foi persistido (árvore de testes inexistente) |
| II. Convenção fixa de nomenclatura (NON-NEGOTIABLE) | ✅ PASS | `snippet-20-contract_schema_test.groovy` com marcador `// SNIPPET-20-CONTRACT_SCHEMA_TEST.GROOVY`; ordem garantida por GENERATORS → SNIPPETS (alfabética). **Sem edição de SECTIONS** — ver D9 |
| III. Pacote por funcionalidade | ✅ PASS | Testes em `<pkg>.<entity_package>.contract` (subpacote da entidade), nunca em pacote técnico |
| IV. Binding duplo de config | N/A | Nenhuma propriedade externalizada nova |
| V. Credenciais fora do repositório | ✅ PASS | Nenhum segredo; regra-05 §8 permanece |
| VI. Validar antes de culpar o código | ✅ PASS (processo) | Todo ciclo começa com `mvn clean install` completo (recompõe `archetype-post-generate.groovy` no `process-resources`) |

**Compliance review obrigatório**: toca `META-INF/archetype-post-generate.groovy` (via compositor) e `archetype-resources/` — revisar contra I–VI (Governança).

**Documentação (README)**: fora da lista de artefatos sujeitos a compliance review (Governança); não cria flag, não altera nomenclatura de extensão, não toca código gerado. Nenhum princípio afetado.

**Re-check pós-Phase 1**: nenhuma violação introduzida; `Complexity Tracking` vazio.

## Project Structure

### Documentation (this feature)

```text
specs/004-entity-contract-schema-test/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/schema-test-contract.md
└── checklists/{requirements.md,conformance.md}
```

### Source Code (repository root)

```text
src/main/resources/archetype-resources/src/test/java/__entity_package_name__/contract/
├── __entity_name__ContractTest.java            # NOVO: entrada JUnit 5 (agrega as divergências)
├── __entity_name__ContractConformanceTest.java # NOVO: contrato→entidade (campos/tipos/id)
├── __entity_name__ContractCoverageTest.java    # NOVO: entidade→contrato (sem campo clandestino)
├── ContractNestedTypesTest.java                # NOVO: recursão nos POJOs de .nested
├── ContractEnumsTest.java                      # NOVO: nome do tipo + constantes
├── ContractRoundTripTest.java                  # NOVO: round-trip estrito (FAIL_ON_UNKNOWN_PROPERTIES)
├── ContractModel.java                          # NOVO: leitura/normalização do contrato (apoio)
└── ContractDiagnostics.java                    # NOVO: divergências + mensagem padronizada (apoio)

src/main/resources/META-INF/archetype_post_generate_snippets/
└── snippet-20-contract_schema_test.groovy      # NOVO: guarda de remoção (deleteFolder do pacote contract)

src/test/archetype-post-generate/
└── SnippetContractSchemaTestTest.groovy        # NOVO: suite da guarda (harness)

src/test/groovy/run-archetype-post-generate-tests.groovy   # MOD: wire da nova suite

src/test/resources/projects/
├── entity-contract/
│   ├── sample-contract.json                    # MOD: +1 objeto aninhado (2 níveis) +1 enum +1 enum aninhado
│   ├── archetype.properties                    # MOD: listas derivadas (nested/enum) + assinatura
│   └── verify.groovy                           # MOD: presença, execução verde (surefire), sabotagem
├── <21 fixtures sem contrato>/verify.groovy     # MOD: asserção de AUSÊNCIA da classe
└── README.md                                   # MOD: linha da fixture entity-contract

README.md                                       # MOD: subtópico "Contratos" em "Funcionalidades" — título no <summary>, 6 itens numerados, diagrama do ciclo
```

**Structure Decision**: o artefato é um **pacote de teste** dentro do pacote da entidade; a guarda remove o diretório inteiro, mantendo a remoção atômica com múltiplas classes. O IT reutiliza `entity-contract` (goal `clean,verify`); nenhuma fixture nova.

## Documentação (README.md)

Origem: FR-019..FR-023 (clarify 2026-11-17, revisado no clarify 2026-11-18) · Decisão de pesquisa: R14 (revisada em 2026-11-18).

| Item | Definição |
|------|-----------|
| Local | Subtópico **Contratos**, dentro de **Funcionalidades** (README.md, tópico na linha 2642) |
| Formato | `<details style="margin-left: 2em;"><summary><strong> Contratos📜: a 'Fonte da Verdade' do Módulo da API </strong></summary>` … `</details>`, mesmo padrão de collapse do README; sem título interno repetindo o nome |
| Nome canônico | **Contratos: a 'Fonte da Verdade' do Módulo da API** (o ícone 📜 é decorativo e não integra o nome; "schema test" fica como nome de trabalho interno) |
| Públicos | Quem usa o módulo gerado e quem mantém o archetype — atendidos pelo item 4 (**Para quem serve**) |
| Linguagem | Simples, clara e objetiva; sem jargão; frases curtas, uma ideia por linha; compreensível por estagiário sem experiência |
| Conteúdo | Seis itens numerados, nesta ordem: 1) **Raciocínio central**; 2) **O que faz**; 3) **Como funciona**; 4) **Para quem serve**; 5) **Quando ele existe**; 6) **Detalhes técnicos** — **sem** tutorial passo a passo |
| Raciocínio central | Abre a documentação com o ciclo fechado — desenho da tela (Design de Front-End) → contrato → módulo da API → tela —, deixando explícito que a conferência não é um mero teste de conformidade; inclui diagrama em bloco de código `text` (padrão do "Grafo de dependências") com fluxo vertical DESENHO DA TELA → CONTRATO → MÓDULO DA API → TELA e seta de retorno da TELA ao DESENHO |
| Fronteira | Não reexplica o mecanismo de contrato (inferência, formato, flags); os itens 2–5 descrevem apenas o comportamento observável; linka as seções existentes |
| Seção 2.1 | *Testabilidade → 2.1) SCHEMA TEST ENTIDADE-CONTRATO* permanece intacta; o item 6 linka para ela com texto e href inalterados |
| Não faz | Não duplica conteúdo, não move seções, não renomeia a 2.1 |

## Desvios deliberados das instruções

- **D9 — SECTIONS não é editado.** `SECTIONS` só aceita arquivos de `body/` + marcadores dinâmicos `VARIABLES/GENERATORS/SNIPPETS`. Incluir `snippet-20-*.groovy` como entrada estática (a) falha `ComposerTest` ("required body file missing") e (b) **duplica** o snippet, pois `SNIPPETS` expande a pasta inteira em ordem alfabética. A posição pedida ("após o generator-03") já é garantida: GENERATORS antes de SNIPPETS, `snippet-20` > `snippet-19`.
- **D10 — fonte canônica de tipos.** `src/main/pre-processamento-maven-flags-via-llm` **não existe** no repositório. Fontes canônicas: `src/main/resources/utils/03-derivacao-regras-de-formacao-do-contrato/regras-01..03` + `specs/_archive/003-llm-flag-inference/{research,data-model}.md` + `utils/02-operacao-procedimento-do-agente/prompt-03`.
- **D11 — contrato da fixture enriquecido.** Sem contrato com aninhado/enum em nenhum IT, a US4 (P2) ficaria sem prova de entrega. O `entity-contract` ganha **6 objetos aninhados em cadeia (7 níveis contando a entidade)** e enums (topo + aninhado).
- **D12 — scaffold `username/password`.** Quando o contrato não tem atributos efetivos, a entidade gerada carrega o scaffold `username/password` (`enable_custom_entity_attributes=false`). O teste exclui exatamente esses dois nomes nesse cenário (ver research R10).

## Achados a reconciliar com a spec

- **A13 — baseline de fixtures (resolvido).** A spec agora declara **22 perfis** (FR-017, SC-004 e US2, cenário 3), alinhada à matriz real de 22 fixtures em `src/test/resources/projects/`.

## Complexity Tracking

> Nenhuma violação de Constitution Check; tabela vazia por decisão de gate.
