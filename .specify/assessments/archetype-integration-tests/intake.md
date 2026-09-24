# Idea Intake: Comprehensive Integration Test Suite for eda-archetype

- **Slug**: archetype-integration-tests
- **Created**: 2026-09-25
- **Source**: pasted text + codebase pointer (this repository, groupId=com.eda-archetype, artifactId=eda-archetype)
- **Type**: improvement

## Idea (as captured)

> "Criar uma suíte de testes de integração abrangente para o eda-archetype, validando que todas as combinações relevantes das flags de geração (Git, entidades, Lombok, Docker, properties, logging, segurança, observabilidade) produzem projetos funcionais."

## Restated

Propor uma suíte de testes de integração, baseada no `maven-archetype-plugin` goal `integration-test` e nas fixtures em `src/test/resources/projects/`, que gere projetos a partir do archetype sob uma matriz curada de combinações de flags e verifique, via `mvn verify` no projeto gerado, que cada combinação produz um projeto Spring Boot funcional.

## Clarified Scope

- **Critério de aceite (padrão)** — Projeto gerado deve passar em `mvn verify`.
- **Critério de aceite (exceção — cenário `json-contract`)** — Além do `mvn verify`, asserts estruturais via `verify.groovy` validam a entidade gerada a partir de `absolute_path_jsonfile_contract`, pois o pós-processador depende desse arquivo externo.
- **Ferramenta** — Apache `maven-archetype-plugin` goal `integration-test`; fixtures em `src/test/resources/projects/<cenário>/archetype.properties` + `goal.txt` (todos os `goal.txt` declaram `clean verify`).
- **Combinações** — Matriz curada (não exaustiva 2^N), organizada por domínio funcional:
  - `minimal` — todas as flags opcionais desligadas (baseline).
  - `full` — todas as flags opcionais ligadas (stress test de conflitos).
  - `json-contract` — foco em `enable_json_entity_contracts` + `absolute_path_jsonfile_contract` (usa `verify.groovy`).
  - `binding-properties` — variações de `enable_binding_properties` (default/list/map/mutable/validated).
- **Regra de dependência** — Quando uma flag "pai" for false, suas flags filhas são ignoradas na combinação. A relação pai→filha é obtida dos comentários `DEPENDS_ON` em `src/main/resources/META-INF/maven/archetype_metadata_properties/property-*.xml`.
- **Side-effects externos** — Mocks e stubs apenas; nenhuma chamada real a Git, Docker, rede.
- **Defaults** — Flags não listadas no cenário usam o `defaultValue` declarado em cada `property-*.xml` em `src/main/resources/META-INF/maven/archetype_metadata_properties/`.
- **Tempo de execução** — Sem orçamento fixo. A suíte não roda em CI; priorizar cobertura e simplicidade sobre velocidade.

## Out of Scope

- Execução em pipeline de CI.
- Reuso ou integração com os scripts em `bash_script/` (incl. `maven_command_line_archetype_generate.sh`).
- Combinações da família **segurança + observabilidade** (JWT + Logback + cPanel/ELK) — endereçadas em assessment futuro, fora deste intake.

## Origin & Context

- **Raised by**: [NEEDS CLARIFICATION: usuário/equipe que submeteu o pedido]
- **Trigger**: [NEEDS CLARIFICATION: causa específica — regressões observadas em PRs anteriores, lacuna de cobertura além da fixture `src/test/resources/projects/basic/`, demanda de release, etc.]

## First-Glance Unknowns

- (nenhum restante — todas as clarificações aplicáveis foram respondidas.)
