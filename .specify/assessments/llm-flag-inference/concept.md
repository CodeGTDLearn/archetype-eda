# Concept: Inferência LLM das flags do `mvn archetype:generate`

- **Slug**: llm-flag-inference
- **Created**: 2026-10-12
- **Recommended option**: B — LLM sugere mapeamento, validador determinístico aplica defaults do archetype

## Options

### Option A — Prompt único cospe a linha `mvn archetype:generate`

- **Sketch**: o operador cola a descrição textual do caso de uso (e, opcionalmente, um JSON contrato) em um prompt; a LLM devolve a linha `mvn archetype:generate --batch-mode -D…` completa. Nenhum código novo no projeto: o fluxo vive dentro do opencode/spec-kit. Defaults do archetype não precisam ser citados porque a LLM pode omiti-los; quando omitidos, o `archetype:generate` aplica o `defaultValue` do `<requiredProperty>`. Se o JSON contrato não vier no texto, a LLM é instruída a pedir antes de continuar. O cache por (descrição + JSON contrato) é opcional e pode ser feito por hashing simples no opencode.
- **Appetite**: small (dias — 1 a 3, incluindo prompt template e validação manual contra os 10 perfis IT).
- **Trade-offs**:
  - Wins: zero código novo no archetype; entra em uso no mesmo dia; serve como linha de base para medir o que se ganha com B.
  - Sacrifícios: nenhum catálogo de flags; se a LLM alucinar uma flag inválida (ex.: `enable_jwt=false` em vez de `enable_jwt_authentication=false`), o `mvn archetype:generate` falha em runtime com mensagem confusa; sem reprodutibilidade byte-a-byte (mesma descrição pode gerar comandos diferentes entre execuções se a temperatura não for fixada); o custo de tokens é pago a cada invocação sem mitigação automatizada.
  - Riscos principais: alucinação de flags inexistentes em `archetype-metadata.xml`; alucinação de tipos (ex.: `mutable_binding_properties=2`, que casa `number` mas fura o `validationRegex ^(false|([1-3]))$`); aplicação de valor inválido para campos validados sem aviso.
- **Rabbit holes**:
  - Definir o prompt template e suas guard-rails contra alucinação, o que naturalmente leva a embutir o catálogo de flags no prompt — isso é o que B resolve estruturalmente.
  - Decidir a precedência entre descrição textual e JSON contrato (B responde com o validador; A responde com texto livre no prompt).
  - Suportar os 10 perfis IT como casos de regressão manual — sem validador automatizado, cada perfil vira um teste ad-hoc.

### Option B — LLM sugere mapeamento, validador determinístico contra `archetype-metadata.xml` aplica defaults e monta a linha

- **Sketch**: o operador passa (a) a descrição textual do caso de uso e (b) um JSON contrato. A LLM devolve um mapeamento estruturado `requiredProperty → valor` (em vez da linha de comando pronta). Um módulo determinístico — Groovy/CLI dentro do projeto archetype-v2-eda — lê `archetype-metadata.xml` (via XInclude sobre os 17 XMLs), valida cada par proposto (nome existe? valor passa no `validationRegex`?), funde com o JSON contrato (precedência do contrato, já decidida no intake), aplica `<defaultValue>` para o que ficou de fora e monta a linha `mvn archetype:generate` final. Cache por `hash(canonical-JSON + descrição textual)` evita nova chamada à LLM em invocações repetidas. A integração com `github/spec-kit` acontece via orientação textual nos templates de spec/plan/test, instruindo o implementador a usar o Maven Archetype gerado por este mecanismo.
- **Appetite**: medium (semanas — 1 a 3, dependendo de quão fundo vai a integração textual com spec-kit).
- **Trade-offs**:
  - Wins: reprodutível (mesma entrada ⇒ mesma linha, com temperatura 0 da LLM + cache determinístico); fail-fast contra alucinação da LLM (validador recusa flag inválida antes de montar a linha); defaults do archetype aplicados pelo código, não pela LLM; JSON contrato ganha um lugar formal com fail-fast explícito; serve como ponto único de entrada tanto para uso humano quanto para a orientação no spec-kit.
  - Sacrifícios: código novo a manter (o validador precisa acompanhar `archetype-metadata.xml`, embora possa extrair dinamicamente os 34 `<requiredProperty>` via XInclude); um nível a mais de indireção para o operador (descrição + JSON ⇒ mapeamento ⇒ linha); precisa de testes do próprio validador.
  - Riscos principais: drift entre o validador e o `archetype-metadata.xml` quando uma flag nova entra (mitigável extraindo o catálogo em tempo de execução); ambiguidade no JSON contrato quando o mesmo campo pode ter múltiplas representações; comportamento indefinido quando a LLM e o JSON contrato discordam (precisa de regra explícita — a regra "contrato vence" já está decidida, mas a extensão "LLM propõe flag fora do contrato ⇒ descarta" ainda é `[NEEDS CLARIFICATION]`).
- **Rabbit holes**:
  - Extrair a lista de `<requiredProperty>` do `archetype-metadata.xml` + 17 XMLs via XInclude sem parser XML novo (caminho natural: reaproveitar `xml-maven-plugin:1.2.1` + `identity.xsl` que já existem no projeto).
  - Decidir precedência: contrato × LLM × default — a regra do intake resolve contrato × LLM e LLM × default (default se aplica ao que ficou de fora); o gap em research.md é "LLM propõe flag fora do contrato ⇒ descarta".
  - Mapear os tipos do archetype (`string`, `number`, `boolean`, `mixed`, `regex-validated`) para o que o JSON contrato aceita — flag com `<defaultValue></defaultValue>` (vazio) hoje depende do `user_*_input.sh`; no mecanismo novo, deve ser inferida da descrição (decisão em open question).
  - Definir o ponto de orientação textual no template do spec-kit (qual arquivo, qual etapa) e o texto literal a incluir.
  - Custo de inferência e cache: serialização canônica do JSON contrato para a chave de cache (problema documentado em research.md); decisão sobre temperatura da LLM (zero, para reprodutibilidade).

### Option C — Não fazer: manter o `bash_script/`

- **Sketch**: nenhuma mudança neste ciclo. A intenção de eliminar bash do projeto continua declarada, mas não é cumprida para este caso. O operador continua respondendo aos prompts do script, a integração com `github/spec-kit` continua sendo uma instrução manual para usar bash, e os `bash_script/*.sh` continuam crescendo a cada nova flag. A diretriz do intake "scripts devem ser deletados após o mecanismo entrar em uso" fica suspensa.
- **Appetite**: zero.
- **Trade-offs**:
  - Wins: zero risco imediato; CI atual continua passando nos 10 perfis IT; nenhum trabalho novo.
  - Sacrifícios: nenhum dos 6 goals do `problem.md` é atendido; o JSON contrato continua implícito; o `bash_script/` continua crescendo a cada nova flag (ritual de 10 passos por flag conforme `README.md` linhas 1081–1414); a intenção estratégica declarada de eliminar bash do projeto fica permanentemente parcial.
- **Rabbit holes**: nenhum — é a linha de base.

## Recommendation

Recomendo **B**. É a única opção que cobre os 6 goals do `problem.md` de uma só vez, com reprodutibilidade e fail-fast:

* Resolve os **4 `read -n 1 -s`** ao eliminar o `bash_script/` do fluxo de geração.
* Zera o `bash_script/maven_command_line_archetype_generate.sh` + `general_functions.sh` + 14 `user_*_input.sh` do versionamento, atendendo o segundo critério de tamanho do `bash_script/`.
* Garante que a linha `mvn archetype:generate` passa em todos os **10 perfis IT** porque o validador reproduz exatamente o que hoje é montado por bash + prompts.
* Cobre **100% dos 34 `<requiredProperty>`** porque o validador extrai dinamicamente dos 17 XMLs.
* Garante reprodutibilidade via temperatura 0 + cache determinístico.
* Garante o fail-fast do JSON contrato ausente com mensagem explícita.

A opção A entrega a mesma UX superficial a um custo menor, mas a alucinação de flags e a falta de reprodutibilidade são riscos que ficam caros depois — especialmente porque o `archetype-post-generate.groovy` de 1062 linhas já mostrou o que acontece quando a validação fica implícita (ver `groovy-snippet-injection`). A opção C é a linha de base que confirma que B vale o investimento.

A B deve ser cotada como `medium`: o validador é o coração (reaproveitando `xml-maven-plugin:1.2.1` + `gmaven:2.1.1` que já rodam no projeto via `groovy-snippet-injection`), e a integração com o spec-kit pode ser feita de forma incremental (primeiro CLI standalone, depois texto de orientação nos templates). O appetite pode ser confirmado ou revisto depois de um spike de 1-2 dias para validar (a) que o validador extrai corretamente os 34 `<requiredProperty>` via XInclude e (b) que a serialização canônica do JSON contrato gera uma chave de cache estável.

## Out of Scope (for the recommended option)

- Editar ou manter qualquer arquivo em `bash_script/` (após adoção, esses scripts são deletados; antes da adoção, permanecem intocados).
- Migrar `archetype-post-generate.groovy` novamente — já coberto por `.specify/assessments/groovy-snippet-injection/`.
- Adicionar cobertura automatizada de combinações de flag — já coberto por `.specify/assessments/archetype-integration-tests/`.
- Treinar, afinar ou trocar o LLM em uso — usar o LLM já disponível.
- Construir UI, dashboard, serviço HTTP ou relatório externo das inferências — entrega é via CLI/arquivo.
- Alterar a versão do `maven-archetype-plugin` ou migrar para o descriptor do Maven 4.
- Tentar fazer o mecanismo funcionar para `archetype-metadata.xml` em si (que é gerado por `xml-maven-plugin` + `identity.xsl`); o mecanismo consome esse arquivo, não o produz.

## Assumptions to Validate

- O LLM em uso (mesmo já usado pelo `github/spec-kit` e pelo opencode) é acessível e tem custo aceitável para uso local/CLI. *(do `intake.md`)*
- `archetype-metadata.xml` é a fonte canônica única das flags; nenhum script injeta flags adicionais fora dele. *(do `intake.md`, confirmado por enumeração de 34 `<requiredProperty>` em 17 XMLs no `research.md`)*
- O `github/spec-kit` aceita orientação textual injetada nos templates de spec/plan/test sem fork do template. *(do `intake.md`)*
- O JSON contrato tem precedência sobre a inferência da LLM, e flags propostas pela LLM fora do contrato são descartadas (silenciadas, com aplicação do default do archetype). *(do `intake.md` + gap em `research.md`)*
- `xml-maven-plugin:1.2.1` + `identity.xsl` (já usados em `groovy-snippet-injection`) são reutilizáveis para extrair a lista de `<requiredProperty>` via XInclude sem parser XML novo. *(do `research.md`)*
- `gmaven:2.1.1` + Groovy 4.0.28 (já usados pelo `SnippetComposer.groovy`) são reutilizáveis para implementar o validador determinístico. *(do `research.md`)*
- A serialização canônica do JSON contrato (ex.: RFC 8785 / JCS, ou `Jackson` com `MapperFeature.SORT_PROPERTIES_ALPHABETICALLY`) gera uma chave de cache estável independente da ordem de campos no input. *(gap em `research.md`)*
- A temperatura da LLM pode ser fixada em 0 para garantir reprodutibilidade byte-a-byte da saída (mapeamento `flag → valor`). *(gap em `research.md`)*
- O `bash_script/` (especificamente `maven_command_line_archetype_generate.sh`, `general_functions.sh` e os 14 `user_*_input.sh`) será deletado após adoção, conforme decisão do `intake.md`. Esta decisão de governança de repositório fica fora do conceito.
