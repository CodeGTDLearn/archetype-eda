<a id="eda-archetype" name="eda-archetype"></a>
# Eda Archetype

<a id="indice" name="indice"></a>
## Índice

- <a href="#tecnologias-utilizadas">Tecnologias Utilizadas 🛠️</a>
  - <a href="#tecnologias-utilizadas-nucleo-do-archetype-build-e-empacotamento">1) Núcleo do Archetype (build e empacotamento)</a>
  - <a href="#tecnologias-utilizadas-templates-e-metadata">2) Templates e Metadata</a>
  - <a href="#tecnologias-utilizadas-scripts-e-automacao">3) Scripts e Automação</a>
  - <a href="#tecnologias-utilizadas-inferencia-agentica-com-ai">4) Inferência Agêntica com IA</a>
  - <a href="#tecnologias-utilizadas-testabilidade">5) Testabilidade</a>

- <a href="#maven-archetype-system">Maven Archetype System 🏗️</a>
  - <a href="#maven-archetype-system-cores">1) Cores ⚛️</a>
    - <a href="#maven-archetype-system-cores-flags-maven-archetype-generate">1.1) Flags p/ 'maven:Archetype:Generate'</a>
    - <a href="#maven-archetype-system-cores-manejo-de-arquivos-groovy">1.2) Manejo de Arquivos c/ Groovy</a>
    - <a href="#maven-archetype-system-cores-testes-relativos-ao-archetype">1.3) Testes Relativos ao Archetype</a>

  - <a href="#maven-archetype-system-fluxo">2) Fluxo 🔄</a>
    - <a href="#maven-archetype-system-fluxo-os-tres-slots-do-archetype-empacotado">Os três slots do archetype empacotado 📦</a>
      - <a href="#maven-archetype-system-fluxo-archetype-metadata-xml-o-indice-nao-contem-conteudo">2.1) <code>archetype-metadata.xml</code> — o índice (não contém conteúdo) 📇</a>
      - <a href="#maven-archetype-system-fluxo-archetype-resources-o-conteudo-nao-sabe-se-existe">2.2) <code>archetype-resources/</code> — o conteúdo (não sabe se existe) 📦</a>
      - <a href="#maven-archetype-system-fluxo-archetype-post-generate-groovy-o-pos-processamento">2.3) <code>archetype-post-generate.groovy</code> — o pós-processamento 🧬</a>

    - <a href="#maven-archetype-system-fluxo-a-relacao-um-pipeline-de-3-tempos">A relação: um pipeline de 3 tempos ⏱️</a>

    - <a href="#maven-archetype-system-fluxo-o-ciclo-do-artefato-o-archetype-packaging-amarra-tudo">O ciclo do artefato (o <code>archetype-packaging</code> amarra tudo) 🔗</a>

  - <a href="#maven-archetype-system-velocity">3) Velocity📍</a>

- <a href="#maven-coordinates">Maven Coordinates 📍</a>
  - <a href="#maven-coordinates-configuration-project">1) Configuration Project 🔧</a>
  - <a href="#maven-coordinates-pom-xml">2) POM.xml 📝</a>
  - <a href="#maven-coordinates-folder-structure">3) Folder Structure 🗂️</a>

- <a href="#etapas-de-processamento">Etapas de Processamento 📈</a>
  - <a href="#etapas-de-processamento-fluxo-das-tres-etapas">Fluxo das três etapas 🧭</a>

  - <a href="#etapas-de-processamento-pre-processamento-empacotado">1) Pré-processamento (empacotado) 🧠</a>
    - <a href="#etapas-de-processamento-pre-processamento-empacotado-suites-unitarias-do-pos-processador">Suítes unitárias do pós-processador 🧪</a>

  - <a href="#etapas-de-processamento-processamento-empacotado">2) Processamento (empacotado) 🏗️</a>

  - <a href="#etapas-de-processamento-pos-processamento-empacotado">3) Pós-processamento (empacotado) 🧬</a>

  - <a href="#etapas-de-processamento-montagem-e-instalacao-do-artefato-fora-das-tres-etapas">4) Montagem e instalação do artefato 📦 (fora das três etapas)</a>
    - <a href="#etapas-de-processamento-montagem-e-instalacao-do-artefato-fora-das-tres-etapas-fluxo">Fluxo 🔄</a>
    - <a href="#etapas-de-processamento-montagem-e-instalacao-do-artefato-fora-das-tres-etapas-regras-estruturais">Regras estruturais 📏</a>

- <a href="#llm-maven-flags-inference">llm-maven-flags-inference 🧠</a>
  - <a href="#llm-maven-flags-inference-01-entrada-inicializacao">01-entrada-inicializacao</a>
  - <a href="#llm-maven-flags-inference-02-operacao-procedimento-do-agente">02-operacao-procedimento-do-agente</a>
  - <a href="#llm-maven-flags-inference-03-regras-de-formacao-do-contrato">03-regras-de-formacao-do-contrato</a>
  - <a href="#llm-maven-flags-inference-04-referencia-templates-de-contrato">04-referencia-templates-de-contrato</a>

- <a href="#inferencia-de-contrato-e-use-case-do-prompt-ao-maven-cli">Inferência de Contrato e Use-Case: do 'Prompt 
  no Agente' ao 'Maven Archetype Generate' 🧠</a>
  - <a href="#inferencia-de-contrato-e-use-case-do-prompt-ao-maven-cli-o-que-entra-na-spec-ou-prompt">1) O que entra na Spec ou Prompt</a>
  - <a href="#inferencia-de-contrato-e-use-case-do-prompt-ao-maven-cli-onde-cada-informacao-vai-parar">2) Onde cada informação vai parar</a>
  - <a href="#inferencia-de-contrato-e-use-case-do-prompt-ao-maven-cli-o-exemplo-ponta-a-ponta">3) O exemplo, ponta a ponta</a>
  - <a href="#inferencia-de-contrato-e-use-case-do-prompt-ao-maven-cli-as-tres-regras-que-mais-surpreendem">4) As três regras que mais surpreendem</a>
  - <a href="#inferencia-de-contrato-e-use-case-do-prompt-ao-maven-cli-o-portao-final-e-o-resultado">5) A Validação final 🚪</a>

- <a href="#instalacao">Instalacao 📦</a>
  - <a href="#instalacao-pre-requisitos">Pré-requisitos</a>
  - <a href="#instalacao-instalar-e-publicar-este-archetype">Instalar e publicar este archetype</a>

- <a href="#cronograma-do-projeto">Cronograma do Projeto 🏗️</a>
  - <a href="#cronograma-do-projeto-milestones">1) Milestones 📅</a>
  - <a href="#cronograma-do-projeto-task-planning">2) Task Planning 📝️</a>
  - <a href="#cronograma-do-projeto-concluded-tasks">3) Concluded Tasks ✅</a>
  - <a href="#cronograma-do-projeto-next-version">4) Next Version 🔮️</a>

- <a href="#acrescentando-novas-flags">Acrescentando Novas Flags ➕️</a>
  - <a href="#acrescentando-novas-flags-areas-alteradas-pela-adicao">1) Areas alteradas pela adição:</a>

  - <a href="#acrescentando-novas-flags-exemplo-pratico-na-mao-flag-c-manipulacao-de-pasta-e-arquivo">2) EXEMPLO PRATICO (na mao) - Flag c/ MANIPULACAO de Pasta e Arquivo:</a>
    - <a href="#acrescentando-novas-flags-exemplo-pratico-na-mao-flag-c-manipulacao-de-pasta-e-arquivo-passo-1-criar-a-pasta-e-os-arquivos-dentro-do-archetype-resources">PASSO 1 - Criar a pasta e os arquivos dentro do ARCHETYPE-RESOURCES:</a>
    - <a href="#acrescentando-novas-flags-exemplo-pratico-na-mao-flag-c-manipulacao-de-pasta-e-arquivo-passo-2-criar-a-property-property-37-enable-s3-storage-xml">PASSO 2 - Criar a property 'PROPERTY-37-ENABLE_S3_STORAGE.XML':</a>
    - <a href="#acrescentando-novas-flags-exemplo-pratico-na-mao-flag-c-manipulacao-de-pasta-e-arquivo-passo-3-registrar-a-entity-da-nova-property-no-archetype-metadata-xml">PASSO 3 - Registrar a ENTITY da nova property no 'ARCHETYPE-METADATA.XML':</a>
    - <a href="#acrescentando-novas-flags-exemplo-pratico-na-mao-flag-c-manipulacao-de-pasta-e-arquivo-passo-4-criar-os-fileset-xml-um-arquivo-por-fileset">PASSO 4 - Criar os 'FILESET-*.XML' (um arquivo por <code>&lt;fileSet&gt;</code>):</a>
    - <a href="#acrescentando-novas-flags-exemplo-pratico-na-mao-flag-c-manipulacao-de-pasta-e-arquivo-passo-5-registrar-os-filesets-no-archetype-metadata-xml">PASSO 5 - Registrar os filesets no 'ARCHETYPE-METADATA.XML':</a>
    - <a href="#acrescentando-novas-flags-exemplo-pratico-na-mao-flag-c-manipulacao-de-pasta-e-arquivo-passo-6-criar-o-variables-nn-s3-storage-groovy">PASSO 6 - Criar o 'VARIABLES-NN-S3-STORAGE.GROOVY':</a>
    - <a href="#acrescentando-novas-flags-exemplo-pratico-na-mao-flag-c-manipulacao-de-pasta-e-arquivo-passo-7-criar-o-snippet-nn-s3-storage-groovy-a-manobra-real">PASSO 7 - Criar o 'SNIPPET-NN-S3-STORAGE.GROOVY' (a manobra real):</a>
    - <a href="#acrescentando-novas-flags-exemplo-pratico-na-mao-flag-c-manipulacao-de-pasta-e-arquivo-passo-8-adicionar-na-fixture-de-teste">PASSO 8 - Adicionar na 'FIXTURE' de teste:</a>
    - <a href="#acrescentando-novas-flags-exemplo-pratico-na-mao-flag-c-manipulacao-de-pasta-e-arquivo-passo-9-recriar-o-archetype-e-testar">PASSO 9 - Recriar o ARCHETYPE e TESTAR:</a>
    - <a href="#acrescentando-novas-flags-exemplo-pratico-na-mao-flag-c-manipulacao-de-pasta-e-arquivo-resumo-9-arquivos-criados-3-editados-em-2-arquivos-existentes">RESUMO - 9 arquivos CRIADOS + 3 EDITADOS (em 2 arquivos existentes):</a>
    - <a href="#acrescentando-novas-flags-exemplo-pratico-na-mao-flag-c-manipulacao-de-pasta-e-arquivo-o-que-nao-foi-feito-e-nao-precisa-ser">O QUE NAO FOI FEITO (e nao precisa ser):</a>

  - <a href="#acrescentando-novas-flags-padronizacao">3) Padronização:</a>

- <a href="#procedimento-para-especificacao-spec">Procedimento para Especificação/Spec</a>
  - <a href="#procedimento-para-especificacao-spec-como-usar-no-opencode-ou-no-github-spec-kit">Como usar no 'OpenCode' OU no 'GitHub Spec Kit'</a>
  - <a href="#exemplo-de-prompt-para-spec">Exemplo de Prompt para Spec</a>
  - <a href="#procedimento-para-especificacao-spec-onde-estao-as-regras-de-formacao-do-contrato">Onde estão as regras de formação do contrato</a>
  - <a href="#procedimento-para-especificacao-spec-onde-usar-cada-abordagem">Onde usar cada abordagem</a>

- <a href="#montagem-dos-arquivos-de-archetype">Montagem dos Arquivos de Archetype 🏗️</a>
  - <a href="#montagem-dos-arquivos-de-archetype-archetype-metadata-xml">1) Archetype-Metadata.xml 🏗️</a>
  - <a href="#montagem-dos-arquivos-de-archetype-archetype-post-generate-groovy">2) Archetype-Post-Generate.groovy 🧬</a>

- <a href="#testabilidade">Testabilidade 🧪</a>
  - <a href="#testabilidade-onde-os-testes-vivem">1) ONDE OS TESTES VIVEM</a>
  - <a href="#testabilidade-o-que-cada-pasta-cobre">2) O QUE CADA PASTA COBRE</a>
  - <a href="#21-schema-test-entidade-contrato-gerado-na-aplicacao">2.1) SCHEMA TEST ENTIDADE-CONTRATO (gerado na aplicacao)</a>
  - <a href="#testabilidade-como-rodar-maven">3) COMO RODAR (Maven)</a>
  - <a href="#testabilidade-verify-groovy-o-hook-pos-build">4) verify.groovy — o hook pós-build 🔬</a>
  - <a href="#testabilidade-trabalhar-com-um-verify-groovy">5) TRABALHAR COM UM <code>verify.groovy</code></a>
  - <a href="#testabilidade-sanity-check-completo-1-comando">6) SANITY CHECK COMPLETO (1 comando)</a>
  - <a href="#testabilidade-prompts-para-homologacao-manual-geracao-via-opencode-04-cenarios">7) PROMPTS PARA HOMOLOGAÇÃO MANUAL (Geração via OpenCode - 04 Cenários) 🧪</a>
    - <a href="#testabilidade-prompts-para-homologacao-manual-geracao-via-opencode-04-cenarios-cenario-a-contrato-plano-3-atributos">7.1) Cenário A — contrato plano (3 atributos)</a>
    - <a href="#testabilidade-prompts-para-homologacao-manual-geracao-via-opencode-04-cenarios-cenario-b-array-enum-contrato-aninhado-filho-neto">7.2) Cenário B — array-enum + contrato aninhado (filho + neto)</a>
    - <a href="#testabilidade-prompts-para-homologacao-manual-geracao-via-opencode-04-cenarios-cenario-c-contrato-com-array-puro">7.3) Cenário C — contrato com array puro</a>
    - <a href="#testabilidade-prompts-para-homologacao-manual-geracao-via-opencode-04-cenarios-cenario-d-contrato-com-objeto-aninhado-filho-neto-bisneto">7.4) Cenário D — contrato com objeto aninhado puro (filho + neto + bisneto)</a>

- <a href="#flags-do-mvn-archetype-generate">Flags do mvn archetype:generate 🤖</a>
  - <a href="#flags-do-mvn-archetype-generate-onde-cada-efeito-acontece">Onde cada efeito acontece</a>
  - <a href="#flags-do-mvn-archetype-generate-property-built-in-fora-do-catalogo">0. Property built-in (fora do catálogo)</a>
  - <a href="#flags-do-mvn-archetype-generate-projeto">1. Projeto</a>
    - <a href="#flags-do-mvn-archetype-generate-projeto-metadados-do-projeto">1.1 Metadados do projeto</a>
    - <a href="#flags-do-mvn-archetype-generate-projeto-ambiente-de-build">1.2 Ambiente de build</a>
    - <a href="#flags-do-mvn-archetype-generate-projeto-idioma-flags-reservadas">1.3 Idioma — flags reservadas</a>
    - <a href="#flags-do-mvn-archetype-generate-projeto-dependencias-externas">1.4 Dependências externas</a>
  - <a href="#flags-do-mvn-archetype-generate-dominio">2. Domínio</a>
  - <a href="#flags-do-mvn-archetype-generate-servidor-http">3. Servidor HTTP</a>
  - <a href="#flags-do-mvn-archetype-generate-seguranca">4. Segurança</a>
  - <a href="#flags-do-mvn-archetype-generate-containers">5. Containers</a>
  - <a href="#flags-do-mvn-archetype-generate-repositorio-git">6. Repositório Git</a>
  - <a href="#flags-do-mvn-archetype-generate-produtividade">7. Produtividade</a>
  - <a href="#flags-do-mvn-archetype-generate-observabilidade">8. Observabilidade</a>
  - <a href="#flags-do-mvn-archetype-generate-externalizacao-de-propriedades">9. Externalização de Propriedades</a>
    - <a href="#flags-do-mvn-archetype-generate-externalizacao-de-propriedades-formato-do-arquivo">9.1 Formato do arquivo</a>
    - <a href="#flags-do-mvn-archetype-generate-externalizacao-de-propriedades-sistema-de-binding">9.2 Sistema de binding</a>
    - <a href="#flags-do-mvn-archetype-generate-externalizacao-de-propriedades-testes-do-binding">9.3 Testes do binding</a>
  - <a href="#flags-do-mvn-archetype-generate-estrutura-de-testes">10. Estrutura de testes</a>
  - <a href="#flags-do-mvn-archetype-generate-grafo-de-dependencias">Grafo de dependências</a>

- <a href="#funcionalidades">Funcionalidades 🛠️</a>
  - <a href="#funcionalidades-contratos-a-fonte-da-verdade-do-modulo-da-api">Contratos📜: a 'Fonte da Verdade' do Módulo da API</a>

---












<a id="tecnologias-utilizadas" name="tecnologias-utilizadas"></a>
## Tecnologias Utilizadas 🛠️ <a href="#indice">↑ topo</a>

* Escopo: as tecnologias **deste projeto** (o gerador). A stack dos projetos **gerados** não faz parte desta seção.

<a id="tecnologias-utilizadas-nucleo-do-archetype-build-e-empacotamento" name="tecnologias-utilizadas-nucleo-do-archetype-build-e-empacotamento"></a>
### 1) Núcleo do Archetype (build e empacotamento) <a href="#indice">↑ topo</a>

| Tecnologia | Versão | Por quê |
|---|---|---|
| **Java** | JDK que roda o Maven (sem `maven.compiler.release` fixado) / 21 (default gerado) | Linguagem base do archetype e dos projetos que ele gera. O `pom.xml` do archetype não fixa a versão de compilação: vale o JDK do build. O default do catálogo é `java_version=21`; as fixtures de integração usam 17. |
| **Apache Maven** | 3.9.x | Coração do projeto: lifecycle, build e distribuição do artefato via `~/.m2`. O archetype **é** um artefato Maven (`packaging=maven-archetype`). |
| **Maven Archetype Plugin** | 3.4.1 | Mecanismo central: `archetype:generate` materializa a árvore do projeto, `archetype:jar` empacota o JAR, `integration-test` valida as fixtures e `update-local-catalog` registra o archetype no catálogo local. |
| **Archetype Packaging** | 3.4.1 | Extension do Maven que habilita o `packaging=maven-archetype` e os **3 slots** do JAR (metadata + resources + post-generate). |

<a id="tecnologias-utilizadas-templates-e-metadata" name="tecnologias-utilizadas-templates-e-metadata"></a>
### 2) Templates e Metadata <a href="#indice">↑ topo</a>

| Tecnologia | Versão | Por quê |
|---|---|---|
| **Apache Velocity** | (via plugin) | Motor de templates do `filtered="true"`: renderiza os placeholders `${...}` nos `archetype-resources/` durante a geração. |
| **XML + XML Entities + XSLT** | — | O catálogo de flags é fragmentado (35 `property-*.xml` + 18 `fileset-*.xml` + DOCTYPE com 53 entities) e materializado por um XSLT identidade — evita um XML monolítico e mantém cada flag revisável isoladamente. |
| **XML Maven Plugin** | 1.2.1 | Executa a transformação XSLT na fase `generate-resources`, gerando o `archetype-metadata.xml` final em `target/classes`. |

<a id="tecnologias-utilizadas-scripts-e-automacao" name="tecnologias-utilizadas-scripts-e-automacao"></a>
### 3) Scripts e Automação <a href="#indice">↑ topo</a>

| Tecnologia | Versão | Por quê |
|---|---|---|
| **Apache Groovy** | 4.0.28 | Linguagem dos scripts do build: pós-processador (`archetype-post-generate.groovy`), compositor e o runner de suíte. Escolhida por rodar nativo dentro do Maven e manipular arquivos/XML com pouca cerimônia. |
| **Groovy Maven Plugin** | 2.1.1 | Executa os scripts Groovy dentro do lifecycle (`process-resources` para compor o pós-processador, `test` para as suítes). |

<a id="tecnologias-utilizadas-inferencia-agentica-com-ai" name="tecnologias-utilizadas-inferencia-agentica-com-ai"></a>
### 4) Inferência Agêntica com IA <a href="#indice">↑ topo</a>

| Tecnologia | Por quê |
|---|---|
| **Markdown empacotado (`llm-maven-flags-inference/`)** | Procedimento de Inferencia: papel do agente, leitura do JAR, regras de formação do contrato e templates. Viaja dentro do JAR. Infere flags do Maven Archetype visando operá-lo a partir das flags inferidas. |
| **JSON** | Formato do contrato de use-case e dos templates de contrato. |
| **SHA-256** | Nome determinístico do arquivo de contrato materializado (hash da forma canônica). |

<a id="tecnologias-utilizadas-testabilidade" name="tecnologias-utilizadas-testabilidade"></a>
### 5) Testabilidade <a href="#indice">↑ topo</a>

| Tecnologia | Versão | Por quê |
|---|---|---|
| **JUnit 4** | 4.13.2 | Uma das três dependências declaradas (com `velocity-engine-core` e `groovy-json`, ambas em `test`) — mas o `surefire` do archetype não roda nada: não existe `*Test.java` no `src/test/java`. As classes JUnit reais viajam no **projeto gerado**. |
| **Groovy (suítes próprias)** | 4.0.28 | As suítes do archetype são scripts Groovy com checks próprios (sem infra JUnit), executados pelo `groovy-maven-plugin` na fase `test` — o build falha se qualquer check falhar. A lista e a contagem ficam em `run-archetype-post-generate-tests.groovy` (fonte da verdade). |








<a id="maven-archetype-system" name="maven-archetype-system"></a>
## Maven Archetype System 🏗️ <a href="#indice">↑ topo</a>

<a id="maven-archetype-system-cores" name="maven-archetype-system-cores"></a>
### 1) Cores ⚛️ <a href="#indice">↑ topo</a>

<a id="maven-archetype-system-cores-flags-maven-archetype-generate" name="maven-archetype-system-cores-flags-maven-archetype-generate"></a>
**1.1) Flags p/ 'maven:Archetype:Generate':**
  * Arquivo: [ARCHETYPE-METADATA.XML](src/main/resources/META-INF/maven/archetype-metadata.xml):
  * Conceito:
    * Arquivo de configuração do archetype
    * Contendo:
      * as propriedades que serao capturadas para o uso no MVN CLI
      * Estrutura do Archetype e seus respectivos arquivos e pastas
  * Formacao do arquivo: Ver o item **'Archetype-Metadata.xml'** desta documentacao
  * Local:
    * [src \ "MAIN" \ resources \ _META-INF_ \ _**"MAVEN"**_ \ ](src/main/resources/META-INF/maven)
    * 02 - Setores (**_FileSet_** + **_RequiredProperties_**):
      * **_Setor 01:_** [_FileSets_](src/main/resources/META-INF/maven/archetype_metadata_filesets)
        * **Distribuicao atual:** **18 arquivos** `fileset-<NN>-<flag>[-parte].xml`, com **um `<fileSet>` por arquivo** (numerados `01` a `18`).
        * **Packaged** Tag:
          * **Conceito:**
            - A tag `packaged` controla como os arquivos serao estruturados
            - durante a geracao do projeto a partir do archetype,
            - especificamente em relacao a estrutura de pacotes Java.
            - Portanto:
              - os arquivos determinados neste _fileSet_
              - serao automaticamente movidos para a estrutura:
                + **"src/main"** OU
                + **"src/test"**

          * **packaged="true":**
            * **Comportamento esperado:**
              - Os arquivos SERAO organizados seguindo
              - a estrutura do _**pacote base do projeto**_
              - Por exemplo:
                + Se o pacote base for _"com.empresa.app"_. Um arquivo:
                  + _'Controller.Java'_ definido em **"src/main/java** / _Controller.java"_
                    + sera movido para _"src/main/java_ / **_com/empresa/app_** / _Controller.java"_
                  + _'ControllerTest.java'_ definido em **"src/test/java** / _ControllerTest.java"_
                    + sera movido para _"src/test/java_ / **_com/empresa/app_** / _ControllerTest.java"_
            * **Casos tipicos:**
              + Arquivos Java (.java)
              + Testes unitarios
              + Recursos especificos de classes.

          * **packaged="false":**
            * **Comportamento esperado:**
              - Os arquivos NAO seguirao a estrutura do _**pacote base do projeto**_
              - Permanecerao exatamente onde foram definidos no archetype
                - _**src/main/resources**_ / _properties_ / _**config.properties**_
            * **Casos Tipicos:**
              - Arquivos de configuracao global (application.properties)
              - Scripts de banco de dados
              - Arquivos de documentacao
              - Recursos estaticos (imagens, CSS, etc)

        * _Filtered_:
          * **Conceito:**
            - E um mecanismo para substituicao de variaveis (placeholders) no arquivo.
            - O Maven aplica o 'motor do velocity' nos elementos anotados por essa tag.
          * **filtered="true":**
            * **Comportamento esperado:**
              - O Maven procura por 'placeholders', nos padroes _**${variavel}**_ no arquivo.
              - Substituindo-os por valores pre-definidos no 'requiredProperties'.
          * **filtered="false":**
            * **Comportamento esperado:**
              - O arquivo seria copiado exatamente como esta.
              - Os placeholders **_${variavel}_** seriam ignorados.

        * _Enconding_:
          * **Conceito:**
            - E crucial para o tratamento correto de caracteres especiais.
          * **encoding="UTF-8":**
            * **Comportamento esperado:**
              - Caracteres como **ç, ã, é** sao processados corretamente.
              - **Emojis** sao mantidos.
              - Idiomas complexos **(chinês, arabe, etc)** sao processados corretamente.
          * **"SEM encoding":**
            * **Comportamento observado:**
              - Pode ocorrer mojibake (corrupcao de caracteres), exemplo:
                - "Configuração" _ficaria_ "ConfiguraÃ§Ã£o"
                - "país" _ficaria_ "paÃ­s"

        * _Outros tipos de Tags_:
          * **Conceito:** tags complementares do `<fileSet>` que configuram aspectos especificos do empacotamento.
            - _directory_: Define o diretorio fonte dos arquivos.
            - _includes_: Define padroes de inclusao de arquivos.
            - _excludes_: Define padroes de exclusao de arquivos.
            - _followSymlinks_: (true/false) - Se deve seguir links simbolicos.
            - _lineEnding_: Define o tipo de quebra de linha (keep, unix, windows, lf, crlf).

        * Estrutura das Tags:
        ```xml
        <fileSet
                filtered="true"
                packaged="true"
                encoding="UTF-8"
                followSymlinks="false"
                lineEnding="unix">
          <directory>src/main/resources</directory>
          <includes>
            <include>**/*.properties</include>
          </includes>
          <excludes>
            <exclude>**/*.tmp</exclude>
          </excludes>
        </fileSet> 
        ```

      * **_Setor 02:_** [_RequiredProperty_](src/main/resources/META-INF/maven/archetype_metadata_required_properties)
         * **Distribuicao atual:** **35 arquivos** `property-<NN>-<flag>.xml` (numerados `02` a `36`, com lacuna em `01`), **um por flag**.
         * Conceito:
           - Define propriedades que serao capturadas pelo **_SWITCHES -D_** do Maven CLI
         * Utilidade Pratica:
           - Captura **_SWITCHES -D_** fornecendo-as p/ a 'montagem' do Archetype
         * Caso de uso:
         1. Usuario deve _obrigatoriamente fornecer_ o **_-Denable_jwt_** no MVN CLI
            - **defaultValue** AUSENTE
            - Caso contrario, o MAVEN "prompetara"(geracao interativa)
              - Indagando o Switch **_-Denable_jwt_** omitido
              ```xml
                <requiredProperty key="enable_jwt">
                    <defaultValue></defaultValue>
                </requiredProperty>
              ```
         2. Usuario OMITE (NAO fornece) a Switch o **_-Denable_jwt_** do Maven CLI
            - (PROMPT INTERATIVO DO MAVEN) Define value for property 'enable_jwt':
               - **defaultValue** PRESENTE
               - MAVEN "nao prompetara"(geracao interativa)
                 - **defaultValue** sera usado
              ```xml
                  <requiredProperty key="enable_jwt">
                      <defaultValue>false</defaultValue>
                  </requiredProperty>
              ```

              - Exemplo de OMISSAO DA SWITCH 'enable_jwt':
                - mvn archetype:generate -DarchetypeGroupId=seu.grupo

<a id="maven-archetype-system-cores-manejo-de-arquivos-groovy" name="maven-archetype-system-cores-manejo-de-arquivos-groovy"></a>
**1.2) Manejo de Arquivos c/ Groovy (seguido da geracao do archetype):**
  * Arquivo: [ARCHETYPE-POST-GENERATE.GROOVY (GERADO)](target/classes/META-INF/archetype-post-generate.groovy)
  * Conceito:
    * Arquivo Groovy de manejo de pastas/arquivos, **APOS** a geracao do ARchetype
    * Contendo:
      * Codigo Groovy de manejo de pastas/arquivos
      * Formacao do arquivo: Ver item **'Archetype-Post-Generate.groovy'** desta documentacao
    * Local: **NAO existe em `src/`**. O unico artefato final fica em [target/classes/META-INF/](target/classes/META-INF) e viaja dentro do JAR.
  * Visibilidade (importante):
    * `target/` esta no `.gitignore`, e o IDE normalmente o exclui da arvore do projeto — por isso o arquivo **nao aparece** no Project view.
    * Ele so existe depois do build: `mvn clean process-resources` (ou qualquer fase posterior) recria.
    * Para abrir no IDE: `Ctrl+Shift+N` (IntelliJ) e digitar `archetype-post-generate.groovy`; ou abrir direto o caminho acima.
  * Conceito:
    * Script GROOVY nativo (build-time) que COMPOE o `archetype-post-generate.groovy` a partir dos snippets em `META-INF/archetype_post_generate_body/`, `..._variables/`, `..._generators/` e `..._snippets/`.
  * Local:
    * [src \ "MAIN" \ groovy \](src/main/groovy/)
  * Invocado por: `org.codehaus.gmaven:groovy-maven-plugin:2.1.1` (goal `execute`) na fase `process-resources`. Configurado em `pom.xml` (raiz).
  * Output: `target/classes/META-INF/archetype-post-generate.groovy` (gerado a cada build; `mvn clean` apaga, o proximo build recria).

<a id="maven-archetype-system-cores-testes-relativos-ao-archetype" name="maven-archetype-system-cores-testes-relativos-ao-archetype"></a>
**1.3) Testes Relativos ao Archetype:**
  * Local: [src \ "TEST" \ resources \ _projects_ \ _**"BASIC"**_ \ ](src/test/resources/projects/basic)
  * Conceito:
	* Arquivo de propriedades(requiredProperties) do archetype
	* Responsavel pelo _**teste do Archetype**_
	* Esse teste é realizado na fase `integration-test`, depois de `package` (o artefato já está montado).

<a id="maven-archetype-system-fluxo" name="maven-archetype-system-fluxo"></a>
### 2) Fluxo 🔄 <a href="#indice">↑ topo</a>

* **Objetivo:** explicar a **essência** do `archetype:generate` do Maven — como os slots
  do JAR empacotado se relacionam entre si, e **por que** o Maven os separa.

<a id="maven-archetype-system-fluxo-os-tres-slots-do-archetype-empacotado" name="maven-archetype-system-fluxo-os-tres-slots-do-archetype-empacotado"></a>
#### Os três slots do archetype empacotado 📦 <a href="#indice">↑ topo</a>

* O archetype empacotado (`packaging=maven-archetype`) é um JAR com **três slots**
  de **nomes fixos**, reconhecidos pelo plugin por constantes próprias
  (`org.apache.maven.archetype.common.Constants`):

| Slot no JAR | Constante | Obrigatório? |
|---|---|---|
| `META-INF/maven/archetype-metadata.xml` | `ARCHETYPE_DESCRIPTOR` | sim |
| `archetype-resources/` | `ARCHETYPE_RESOURCES` | sim |
| `META-INF/archetype-post-generate.groovy` | `ARCHETYPE_POST_GENERATION_SCRIPT` | opcional |

* O slot **é** o nome da pasta/arquivo dentro do JAR — nada mais.
* O `archetype:jar` (goal `jar`, fase `package`) empacota `target/classes` no JAR.

<a id="maven-archetype-system-fluxo-archetype-metadata-xml-o-indice-nao-contem-conteudo" name="maven-archetype-system-fluxo-archetype-metadata-xml-o-indice-nao-contem-conteudo"></a>
#### 2.1) `archetype-metadata.xml` — o índice (não contém conteúdo) 📇 <a href="#indice">↑ topo</a>

* Descreve **o que** o archetype precisa e **o que fazer** com a pasta de templates:
  * `<requiredProperties>` — as **variáveis** pedidas, quais sejam:
    * `key`
    * `defaultValue`
    * `validationRegex`
  * `<fileSets>` — o **roteamento**: 
    * cada `<fileSet>` diz *qual diretório* de `archetype-resources/` vira 
    * *qual lugar* da árvore gerada, e com que tratamento:
      * `filtered`
      * `packaged`
      * `encoding`
      * `includes`/`excludes`
    * O metadata **nunca lista arquivos**:
      * aponta pastas e deixa o `includes` filtrar.
      * 
* **Motivação:** separar a *declaração* do *conteúdo*.
  * o descritor é pequeno, revisável, substituível;
  * o template pode ser gigante e nunca é lido pelo parser — só copiado.

<a id="maven-archetype-system-fluxo-archetype-resources-o-conteudo-nao-sabe-se-existe" name="maven-archetype-system-fluxo-archetype-resources-o-conteudo-nao-sabe-se-existe"></a>
#### 2.2) `archetype-resources/` — o conteúdo (não sabe se existe) 📦 <a href="#indice">↑ topo</a>

* A árvore esqueleto do projeto a ser gerado. Única fonte do *conteúdo* dos arquivos.
* O parser do metadata nunca entra aqui. 
* Quem entra é o **gerador**, que usa o
  metadata como instrução de montagem:
  * `__entity_name_package__` no **nome** de arquivo/pasta → substituído pelo valor da flag;
  * `${entity_name}` no **conteúdo** → renderizado pelo Velocity (só com `filtered="true"`).
* **Motivação:** 
  * o template é puro "material virgem" — não carrega lógica de montagem.
  * Quem decide como usá-lo é o metadata.

<a id="maven-archetype-system-fluxo-archetype-post-generate-groovy-o-pos-processamento" name="maven-archetype-system-fluxo-archetype-post-generate-groovy-o-pos-processamento"></a>
#### 2.3) `archetype-post-generate.groovy` — o pós-processamento 🧬 <a href="#indice">↑ topo</a>

* Script Groovy executado **depois** da cópia.
  * Recebe, via `Binding` do `GroovyShell`:
    * cada `requiredProperty` como **variável direta**;
    * a variável `request` = `ArchetypeGenerationRequest` completo, ex:
      * `request.properties`
      * `request.outputDirectory`
      * `request.artifactId`
      * etc…
* **Motivação (a essencial):** o descritor é **estático e sem condição**.
  * No modelo atual (`archetype-metadata.xml` do Maven 3.4.1) **não existe** forma de
    dizer *"este fileSet só entra se a flag X for true"*.
    * Logo: **quem decide manter/excluir por flag é o Groovy**.
    * Ele existe para o que a forma declarativa não consegue expressar.

<a id="maven-archetype-system-fluxo-a-relacao-um-pipeline-de-3-tempos" name="maven-archetype-system-fluxo-a-relacao-um-pipeline-de-3-tempos"></a>
#### A relação: um pipeline de 3 tempos ⏱️ <a href="#indice">↑ topo</a>

```
   metadata.xml          resources/          post-generate.groovy
   ───────────────       ───────────         ─────────────────────
   "O QUE preciso"  +   "O QUE copio"   →   "O QUE ajusto"
   (variáveis)            (template bruto)     (mantém/exclui/renomeia)

   declarado / passivo                         único que decide
   estático / só conteúdo
```

* Cada slot é consumido em um **momento diferente da mesma execução** do
  `archetype:generate`:
  1. **Metadata primeiro**:
     * o configurador lê o descritor
     * resolve as flags
       * (CLI > `defaultValue` > prompt)
         * monta o `Properties`.
  2. **Resources no meio**:
     * o gerador percorre os `<fileSet>`
     * resolve os templates correspondentes
       * aplica Velocity / `packaged` / `encoding`, e
         * escreve a árvore.
  3. **Groovy no fim**:
     * roda sobre a árvore já escrita, com as mesmas flags em mãos.

* **A lei da relação:** 
  * Fluxo:
    * o metadata **dirige**, 
    * os resources **sofrem**,
    * o Groovy **corrige**.
  * Nenhum dos dois primeiros sabe que o terceiro existe;
  * o Groovy não pode adicionar ao contrato — só intervém no resultado.

<a id="maven-archetype-system-fluxo-o-ciclo-do-artefato-o-archetype-packaging-amarra-tudo" name="maven-archetype-system-fluxo-o-ciclo-do-artefato-o-archetype-packaging-amarra-tudo"></a>
#### O ciclo do artefato (o `archetype-packaging` amarra tudo) 🔗 <a href="#indice">↑ topo</a>

```
process-resources → resources
package           → archetype:jar              (monta o JAR com os 3 slots)
integration-test  → archetype:integration-test (gera projeto, roda goal.txt, compara)
install           → install + archetype:update-local-catalog
                                               (escreve ~/.m2/archetype-catalog.xml)
```

<a id="maven-archetype-system-velocity" name="maven-archetype-system-velocity"></a>
### 3) Velocity📍 <a href="#indice">↑ topo</a>

* [User Guide](https://velocity.apache.org/engine/2.4.1/user-guide.html)
* [VTL Reference](https://velocity.apache.org/engine/2.4.1/vtl-reference.html)








<a id="maven-coordinates" name="maven-coordinates"></a>
## Maven Coordinates 📍 <a href="#indice">↑ topo</a>

* [Click to check the Coordinates Documentation](https://maven.apache.org/pom.html#Maven_Coordinates)

<a id="maven-coordinates-configuration-project" name="maven-coordinates-configuration-project"></a>
### 1) Configuration Project 🔧 <a href="#indice">↑ topo</a>

<img alt="" height="303" src="assets/spr_init_config.png" width="300"/>

<a id="maven-coordinates-pom-xml" name="maven-coordinates-pom-xml"></a>
### 2) POM.xml 📝 <a href="#indice">↑ topo</a>

<img alt="" height="140" src="assets/spr_init_pom.png" width="314"/>

<a id="maven-coordinates-folder-structure" name="maven-coordinates-folder-structure"></a>
### 3) Folder Structure 🗂️ <a href="#indice">↑ topo</a>

<img alt="" height="400" src="assets/spr_init_folder_structure.png" width="150"/>











<a id="etapas-de-processamento" name="etapas-de-processamento"></a>
## Etapas de Processamento 📈 <a href="#indice">↑ topo</a>

* Visão geral: o processamento do archetype acontece em **3 etapas** — e apenas 2
  delas viajam empacotadas dentro do JAR:
  - **1) Pré-processamento**:
    - captura a intenção (use-case + contrato JSON) e demais elementos
    - Essa intencao e capturada no 'TUI' do Agente
    - Os valores das Flags são inferidos 'Agenticamente'
      - ✅ **JAR empacotado** com as 'Diretivas Agenticas de Inferencia' possui:
        - Os Procedimento Agenticos (prompts)
          - Localizados na pasta `llm-maven-flags-inference/`, lido pelo agente a partir do JAR).
    - RESULTADO: 
      - Produz a command line `-D...` do Maven.  

  - **2) Processamento**:
    - o `maven-archetype-plugin` materializa a árvore do
      projeto
    - Isso ocorre a partir dos:
      - catálogo de flags
      - Procedimentos AGentics
      - Templates de Contratos

  - **3) Pós-processamento**:
    - o Groovy modulariza o projeto gerado
      - Mecanismo: mantém / exclui arquivos e pastas.

<a id="etapas-de-processamento-fluxo-das-tres-etapas" name="etapas-de-processamento-fluxo-das-tres-etapas"></a>
### Fluxo de três etapas 🧭 <a href="#indice">↑ topo</a>

| Fluxo | Papel | Tecnologia | Pacote <br> JAR? | Momento no <br>`archetype:generate` |
|---|---|---|---|---|
| Pré-processamento | intenção → `mvn ... -D...` | procedimento do agente (`llm-maven-flags-inference/` no JAR) | ✅ | antes  |
| Processamento | catálogo + templates → árvore do projeto | `archetype-metadata.xml` + `archetype-resources/` (Velocity) | ✅ | durante |
| Pós-processamento | árvore gerada → modularização | `archetype-post-generate.groovy` | ✅ | Depois |

> O `mvn clean install` **não** é uma etapa de processamento: é o passo final do
> *pipeline do artefato* (montagem, testes e instalação em `~/.m2`), detalhado no
> §4 abaixo.

<a id="etapas-de-processamento-pre-processamento-empacotado" name="etapas-de-processamento-pre-processamento-empacotado"></a>
### 1) Pré-processamento (empacotado) 🧠 <a href="#indice">↑ topo</a>

* **Objetivo:** 
  * transformar **use-case + contrato JSON** + demais elementos contidos no 'Prompt dado ao Agente' em mvn-cli
    * command line pronta do `mvn archetype:generate` (todas as flags `-D` resolvidas).

* **Quem faz:** 
  * o **agente** que opera o archetype:
    * Atraves da Inferencia das Flags de `mvn archetype:generate`
    * Construindo a `mvn archetype:generate`
    * Executando a `mvn archetype:generate`

  * Observacoes:
    * Não há motor, CLI ou script externo: 
      * 'Diretivas Agenticas de Inferencia':
        * Sao o conjunto do procedimentos(arquivos) em Markdown e JSON
          * Empacotados no JAR.

* **Composição:** [`src/main/resources/llm-maven-flags-inference/`](src/main/resources/llm-maven-flags-inference/) — 19 arquivos em 4 categorias:
  - `01-entrada-inicializacao/`:
    - porta de entrada (`00-entry-point`) e mapa de leitura (`01-mapa-dos-arquivos-e-ordem-de-leitura`);
  - `02-operacao-procedimento-do-agente/`:
    - papel, leitura do JAR, regras obrigatórias, execução e entregáveis;
  - `03-regras-de-formacao-do-contrato/`:
    - derivação da entidade, tipos, tipos aninhados, precedência, 
      materialização e proveniência;
  - `04-referencia-templates-de-contrato/`:
    - schema e exemplos.

* **Entrada/Saída:** 
  * Entrada: Dados fornecidos ao Agente e inferidos em flags (tipo de módulo, package base, use-case, contrato JSON)
  * Command line do Maven + tabela de proveniência.

* **Fonte da verdade:** 
  * o JAR instalado `com.eda-archetype:eda-archetype` contendo:
    * Diretivas Agenticas de Inferencia
    * O catálogo de flags (`META-INF/maven/archetype-metadata.xml`).

* Detalhes do procedimento → seção **"Procedimento para Especificação"**:
  * Diretivas Agenticas de Inferencia

<a id="etapas-de-processamento-pre-processamento-empacotado-suites-unitarias-do-pos-processador" name="etapas-de-processamento-pre-processamento-empacotado-suites-unitarias-do-pos-processador"></a>
#### Suítes unitárias do pós-processador 🧪 <a href="#indice">↑ topo</a>

Runner: `run-archetype-post-generate-tests.groovy` — as suítes são scripts Groovy com checks próprios, executados pelo `groovy-maven-plugin` na fase `test`.

> Detalhamento por camada, tecnologia, pasta e comando → seção **"Testabilidade 🧪"**

<a id="etapas-de-processamento-processamento-empacotado" name="etapas-de-processamento-processamento-empacotado"></a>
### 2) Processamento (empacotado) 🏗️ <a href="#indice">↑ topo</a>

* **Objetivo:** 
  * o `maven-archetype-plugin` materializa a árvore do projeto gerado
  * Isso ocorre a partir do catálogo de flags + templates
    * durante o `mvn archetype:generate`.

* **Composição (fonte da verdade):**

| # | Artefato | Local | Papel |
|---|----------|-------|-------|
| A1 | Catálogo de flags - `archetype_metadata_required_properties` | `src/main/resources/META-INF/maven/archetype_metadata_required_properties/` | 35 flags: `key`, `defaultValue`, `validationRegex`; descrição e `DEPENDS_ON` em comentário XML |
| A2 | Filesets — `archetype_metadata_filesets` | `src/main/resources/META-INF/maven/archetype_metadata_filesets/` | 18 arquivos, um `<fileSet>` cada; mapeia `archetype-resources/` → árvore do projeto gerado |
| A3 | Core do metadata (Catalogo Montado) | `src/main/resources/META-INF/maven/archetype-metadata.xml` | DOCTYPE com 53 entities (35 `property-<NN>-<flag>` + 18 `fileset-<NN>-<flag>`) |
| A4 | XSLT identidade | `src/main/resources/META-INF/maven/archetype-metadata-identity.xsl` | resolve as entities `SYSTEM` e materializa os blocos inline |

| Artefato | Local | Papel |
|----------|-------|-------|
| Templates de Criacao do Modulo | `src/main/resources/archetype-resources/` | esqueleto do projeto gerado; os `${...}` são renderizados pelo Velocity |
| 'Diretivas Agenticas de Inferencia' | `src/main/resources/llm-maven-flags-inference/` | 19 arquivos em 4 categorias (`01-entrada-inicializacao/`, `02-operacao-procedimento-do-agente/`, `03-regras-de-formacao-do-contrato/`, `04-referencia-templates-de-contrato/`) que viajam no JAR; o agente lê o procedimento de dentro do JAR |
| Fixtures de integração | `src/test/resources/projects/*` | um projeto gerado por fixture, cada um validado com `mvn clean verify` |

* **Montagem do catálogo:** 
  * fragmentos (A1 + A2) + DOCTYPE (A3) →
    * `xml-maven-plugin:transform` + `archetype-metadata-identity.xsl` 
    * (A4) na fase `generate-resources` → `target/classes/META-INF/maven/archetype-metadata.xml`
      (cópia convencional excluída — `pom.xml:38-41`).

* **Execução:**
  * Ciclo:
    * o plugin lê o JAR instalado (`~/.m2`)
    * valida as flags `-D` (default × `validationRegex`) e 
    * renderiza os templates
  * etapa **autossuficiente**: 
    * depende apenas do `.m2`, nunca da pasta-fonte.

<a id="etapas-de-processamento-pos-processamento-empacotado" name="etapas-de-processamento-pos-processamento-empacotado"></a>
### 3) Pós-processamento (empacotado) 🧬 <a href="#indice">↑ topo</a>

* **Objetivo:** 
  * modularizar o projeto recém-gerado
    * Estrategia: mantém / exclui pastas e arquivos conforme as flags escolhidas)
  * Momento:
    * Ao final do `mvn archetype:generate`.

* **Composição:**

| # | Artefato | Local | Papel |
|---|----------|-------|-------|
| A5 | Pós-processador — `body/` + `variables/` + `generators/` + `snippets/` | `src/main/resources/META-INF/archetype_post_generate_{body,variables,generators,snippets}/` | 64 fragmentos Groovy (15 `body/` + 25 `variables/` + 4 `generators/` + 20 `snippets/`) |
| A6 | pós-processador montado | `src/main/groovy/ArchetypePostGenerateComposer.groovy` | remonta A5 em 1 arquivo: ordem canônica + consolidação de imports |

* **Montagem:** `ArchetypePostGenerateComposer` (A6) via `groovy-maven-plugin:execute` na fase
  `process-resources` → `target/classes/META-INF/archetype-post-generate.groovy`
  → levado no JAR.
* **O que faz em runtime:** mantém / exclui por flag; persiste o contrato em
  `src/test/resources/contracts/contract.json` (quando
  `enable_folder_src_test_java=true`); gera enums customizados; log opcional
  (`show_archetype_post_generate_log`).

<a id="etapas-de-processamento-montagem-e-instalacao-do-artefato-fora-das-tres-etapas" name="etapas-de-processamento-montagem-e-instalacao-do-artefato-fora-das-tres-etapas"></a>
### 4) Montagem e instalação do artefato 📦 (fora das três etapas) <a href="#indice">↑ topo</a>

| Fase | Plugin | Entrada | Saída |
|------|--------|---------|-------|
| `generate-resources` | `xml-maven-plugin:1.2.1` (`transform`) | `archetype-metadata.xml` + <br>`archetype-metadata-identity.xsl` | `target/classes/META-INF/maven/archetype-metadata.xml`<br> (catálogo completo, sem DOCTYPE) |
| `process-resources` | `groovy-maven-plugin:2.1.1` (`execute`) | 64 fragmentos `.groovy` | `target/classes/META-INF/archetype-post-generate.groovy` |
| `test` | `groovy-maven-plugin:2.1.1` (`execute`) | `run-archetype-post-generate-tests.groovy` | as suítes unitárias do pós-processador (o build falha se alguma falhar) |
| `integration-test` | `maven-archetype-plugin:3.4.1` (`integration-test`) | `src/test/resources/projects/*` | um projeto gerado por fixture, cada um com `mvn clean verify` |
| `install` | `maven-install-plugin` + <br>`maven-archetype-plugin:3.4.1` (`update-local-catalog`) | JAR do archetype | `~/.m2/repository/` + `archetype-catalog.xml` |

> **Comando canônico:** `mvn clean install`
>
> A fase `integration-test` é **anterior** a `install` e já é executada por ele.<br>
> Portanto `mvn clean install integration-test` roda a suíte de integração
> **duas vezes** (cada fixture é gerada e validada 2×)<br>
> use `mvn clean install` ou, se não quiser instalar, `mvn clean integration-test`.

* **De volta aos 2 momentos do archetype:** 
  * o §4 acima é o **momento de
    montagem** (build + testes + instalação em `~/.m2`); 
  * as **etapas 1 → 3** são o **momento de geração do modulo** (`mvn archetype:generate`).

<a id="etapas-de-processamento-montagem-e-instalacao-do-artefato-fora-das-tres-etapas-fluxo" name="etapas-de-processamento-montagem-e-instalacao-do-artefato-fora-das-tres-etapas-fluxo"></a>
#### Fluxo 🔄 <a href="#indice">↑ topo</a>

```text
TRILHA 1 — MONTAGEM (mvn clean install; fora das três etapas)
────────────────────────────────────────────────────────────────────------------
[1] CATÁLOGO         35 property-*.xml + 18 fileset-*.xml + DOCTYPE (53 entities)
                     └─ xml-maven-plugin:transform + archetype-metadata-identity.xsl (generate-resources)
                     
[2] PÓS-PROCESSADOR  64 fragmentos .groovy
                     └─ ArchetypePostGenerateComposer.groovy via gmaven:execute (process-resources)
                        (local: src/main/groovy/ArchetypePostGenerateComposer.groovy)
                        
[3] TESTES           unitários: as suítes do pós-processador (fase test)
                     integração: uma fixture por combinação de flags (fase integration-test)
                     
[4] INSTALAÇÃO       ~/.m2/repository + archetype-catalog.xml (fase install)

```

```text
TRILHA 2 — USO (mvn archetype:generate; etapas 1 → 3)
──────────────────────────────────────────────────────────-----------───────────
[5] PRÉ PROCESSAMENTO   → use-case + contrato JSON + outros elementos (Originados a partir do Agentes[Opencode])
    (DENTRO DO JAR)     → o agente lê o procedimento em llm-maven-flags-inference/ ('Diretivas Agenticas de Inferencia')
                        → e o catálogo montado dentro no JAR (archetype-metadata.xml) para conhecer as Flags
                          → formula-se a mvn-cli (mvn archetype:generate --batch-mode -D...)
                        
[6] PROCESSAMENTO       → archetype-metadata.xml (flags)
                        → archetype-resources
                          → Velocity montando o modulo + árvore do projeto gerado
                        
[7] PÓS PROCESSAMENTO   → archetype-post-generate.groovy:
                          → mantém / exclui pastas pre-projetadas
                            → Essas pastas contem Modulos[Codigo + Classe] manejadas pelo Velocity
                            → contrato → src/test/resources/contracts/contract.json
 
[8] MODULO CONCLUIDO (projeto final)
```

<a id="etapas-de-processamento-montagem-e-instalacao-do-artefato-fora-das-tres-etapas-regras-estruturais" name="etapas-de-processamento-montagem-e-instalacao-do-artefato-fora-das-tres-etapas-regras-estruturais"></a>
#### Regras estruturais 📏 <a href="#indice">↑ topo</a>

* **Fonte da verdade = os fragmentos** (A1, A2, A5). 
  * Os arquivos em `target/classes/` são **gerados** a cada build
  * editá-los é trabalho perdido.

* **O pré-processamento é empacotado:** 
  * o procedimento para especificação('Diretivas Agenticas de Inferencia') vive em `src/main/resources/llm-maven-flags-inference/`
  * e empacotado no JAR originariamente, a cada Build:
    * O agente lê de dentro do artefato.

* **O catálogo é lido do JAR pelo Agente(Opencode):** 
  * o agente considera fonte da verdade das flags:
      * `META-INF/maven/archetype-metadata.xml`
        * e os fragmentos `META-INF/maven/archetype_metadata_required_properties/*.xml`

* **O catálogo não é alterado em código:** 
  * as flags são lidas em runtime:
    * flag nova = 1 fragmento `property-<NN>-<flag>.xml` + 
    * 1..N fragmentos `fileset-<NN>-<flag>[-parte].xml` (um `<fileSet>` cada) + 
    * registros no DOCTYPE.

* **As flags são auto-descritivas:** 
  * o `defaultValue` nunca é vazio
  * e o `validationRegex` é aplicado por quem emite a command line.











<a id="llm-maven-flags-inference" name="llm-maven-flags-inference"></a>
## llm-maven-flags-inference 🧠 <a href="#indice">↑ topo</a>

* **O que é:** a pasta é o **alvo do prompt do agente**.
* **Pelo que responde:** ela responde pelo **pré-processamento** e pelo **procedimento de execução e verificação** que o agente faz depois da linha de comando.
* **A sequência didática:**
  1. O operador escreve o prompt na Spec.
  2. O prompt aponta para o archetype e para esta pasta dentro do JAR.
  3. O agente lê a entrada e o índice.
  4. O agente aplica as regras e materializa o contrato.
  5. O resultado é a linha `mvn archetype:generate`.
* **Fronteira:** o **pré-processamento** termina nessa linha. A geração Maven **começa depois** dela — e a mesma pasta governa o que vem depois: executar, validar o `package`, conferir o contrato copiado, compilar e reportar (`04-fluxo-de-execucao-e-verificacao` e `05-entregaveis-e-relatorios`).

<a id="llm-maven-flags-inference-01-entrada-inicializacao" name="llm-maven-flags-inference-01-entrada-inicializacao"></a>
### 01-entrada-inicializacao <a href="#indice">↑ topo</a>

É a porta de entrada: reúne o arquivo que abre o procedimento e o índice que fixa a ordem de leitura. É o primeiro momento depois do prompt.

<a id="llm-maven-flags-inference-02-operacao-procedimento-do-agente" name="llm-maven-flags-inference-02-operacao-procedimento-do-agente"></a>
### 02-operacao-procedimento-do-agente <a href="#indice">↑ topo</a>

Concentra o papel do agente, a leitura do JAR, as regras obrigatórias, a execução e os entregáveis. É o momento em que o procedimento vira ação: produz a linha de geração e, depois dela, executa, verifica e reporta.

<a id="llm-maven-flags-inference-03-regras-de-formacao-do-contrato" name="llm-maven-flags-inference-03-regras-de-formacao-do-contrato"></a>
### 03-regras-de-formacao-do-contrato <a href="#indice">↑ topo</a>

Reúne as regras normativas de derivação. É o momento em que o use-case e o JSON de entrada se transformam no contrato.

<a id="llm-maven-flags-inference-04-referencia-templates-de-contrato" name="llm-maven-flags-inference-04-referencia-templates-de-contrato"></a>
### 04-referencia-templates-de-contrato <a href="#indice">↑ topo</a>

Traz o schema e os exemplos de contrato. É a referência consultada para materializar o contrato no formato esperado.

<a id="inferencia-de-contrato-e-use-case-do-prompt-ao-maven-cli" name="inferencia-de-contrato-e-use-case-do-prompt-ao-maven-cli"></a>
## Inferência de Contrato e Use-Case: do 'Prompt no Agente' ao 'Maven Archetype Generate' 🧠 <a href="#indice">↑ topo</a>

* **A pergunta:** 
  * o operador entrega um **use case** (texto) + um **contrato JSON**; 
    * o `mvn archetype:generate` resolve **35 flags `-D`** a partir do catálogo. 
      * Quem faz a ponte?
* **A resposta:** 
  * o **agente**, seguindo os elementos (prompts + JSONs) empacotados dentro do JAR na pasta `llm-maven-flags-inference/`. 
  * Ele não chuta: 
    * classifica cada chave do contrato, 
    * aplica as regras de formação e 
    * valida tudo contra o catálogo antes de emitir a linha de comando.

<a id="inferencia-de-contrato-e-use-case-do-prompt-ao-maven-cli-o-que-entra-na-spec-ou-prompt" name="inferencia-de-contrato-e-use-case-do-prompt-ao-maven-cli-o-que-entra-na-spec-ou-prompt"></a>
### 1) O que entra na Spec ou Prompt <a href="#indice">↑ topo</a>

| Entrada | Exemplo |
|---|---|
| **PROCEDIMENTO** (preâmbulo, não é entrada) | `0. PROCEDIMENTO` — ponteiro para o `00-entry-point` dentro do JAR |
| **Tipo de módulo** | `RAIZ` ou `SECUNDARIO` |
| **Package base** | `br.com.loja` — prefixo; o archetype concatena `entity_package_name` |
| **Use case** | *"Cadastro de clientes de uma clínica. API REST com autenticação JWT na porta 8080."* |
| **Contrato JSON** | objeto JSON completo — corpo abaixo |
| **PASTA DE DESTINO** | `C:\Temp\eda-sandbox` — caminho absoluto, fora da árvore do archetype |

> Não são entradas da SPEC: `groupId` (= package base), `artifactId`, `version` do
> módulo, flags e defaults. O procedimento resolve.

**Exemplo de Contrato JSON:**

```json
{
  "entityName": "Cliente",
  "attributes": {
    "nome": "Maria",
    "ativo": true,
    "nascimento": "1990-05-14"
  },
  "api_port": 8080
}
```

> A **PASTA DE DESTINO** pertence ao spec, nunca ao contrato.
> Ela viaja na propriedade nativa `outputDirectory` do goal de geração — que **não** é uma flag do catálogo.
> O agente nunca a presume: sem a marcação, ele pergunta.
> Essa regra normativa vive em `llm-maven-flags-inference/`, dentro do JAR; o README apenas a referencia.

<a id="inferencia-de-contrato-e-use-case-do-prompt-ao-maven-cli-onde-cada-informacao-vai-parar" name="inferencia-de-contrato-e-use-case-do-prompt-ao-maven-cli-onde-cada-informacao-vai-parar"></a>
### 2) Onde cada informação vai parar <a href="#indice">↑ topo</a>

O agente lê o contrato e separa as chaves em **três destinos** (exemplo com 3 flags do `mvn archetype:generate`):

| Chave do contrato | Destino | Por quê |
|---|---|---|
| `api_port` | **flag literal** (`CONTRACT`) | a chave existe no catálogo → vira `-D` direto |
| `entityName` | **flag derivada** (`CONTRACT_DERIVED`) | meta-chave → nomeia a entidade |
| `nome`, `ativo`, `nascimento` | **atributos da entidade** | não existem no catálogo → viram campos Java |

<a id="inferencia-de-contrato-e-use-case-do-prompt-ao-maven-cli-o-exemplo-ponta-a-ponta" name="inferencia-de-contrato-e-use-case-do-prompt-ao-maven-cli-o-exemplo-ponta-a-ponta"></a>
### 3) O exemplo, ponta a ponta <a href="#indice">↑ topo</a>

```text
SPEC: USE CASE + CONTRATO
        │
        ├─ entityName = "Cliente" ──────────▶ -Dentity_name=Cliente
        │                                     └─▶ -Dentity_package_name=cliente      (derivado: minúsculas)
        │
        ├─ api_port = 8080 ─────────────────▶ -Dapi_port=8080                        (CONTRACT: literal)
        │
        ├─ attributes
        │   ├─ nome       = "Maria"      (string)  ─┐
        │   ├─ ativo      =  true        (boolean)  ├─▶ -Dcustom_entity_attributes_list=
        │   └─ nascimento = "1990-05-14" (string)  ─┘        String:nome,Boolean:ativo,String:nascimento
        │                                                    (tipo estimado pelo VALOR, na ordem do contrato)
        │
        ├─ há atributos? ────────────────────▶ -Denable_custom_entity_attributes=true   (forçado)
        │
        └─ contrato materializado em disco ──▶ -Dentity_contract_file_path=…/<sha256>.json
```

> O `id` do contrato é uma exceção: ele **não** entra em `custom_entity_attributes_list`.
> O archetype gera sempre o campo técnico `<entidade>Id` (`Long`) e o template descarta
> qualquer atributo chamado `id` — o `id` do contrato corresponde a esse campo técnico
> (`01` §4.3).

<a id="inferencia-de-contrato-e-use-case-do-prompt-ao-maven-cli-as-tres-regras-que-mais-surpreendem" name="inferencia-de-contrato-e-use-case-do-prompt-ao-maven-cli-as-tres-regras-que-mais-surpreendem"></a>
### 4) As três regras que mais surpreendem <a href="#indice">↑ topo</a>

| Regra | Efeito |
|---|---|
| **O tipo vem do valor** | ·`true`→`Boolean`<br>· inteiro que cabe em `int`→`Integer`<br>·inteiro maior→`Long`<br>·decimal→`Double`<br>·string→`String`<br>·`null`→`String`<br>·objeto→POJO em `<package>.<entity_package_name>.nested`<br>·array→`String` (mas array não vazio de strings **MAIÚSCULAS**→enum, ver `02` §4) |
| **Valor explícito vence** | ·um `type` declarado no contrato tem precedência **se** for `java.lang` ou um **tipo do projeto** (ex:`br.com.clinica.convenio.Convenio`)<br>·tipos de fora desse conjunto (ex:`java.time.Instant`, `java.util.List`, `List<String>`) são **rejeitados com aviso** e o tipo volta a ser estimado pelo valor |
| **Flag derivada em cascata** | ·`entity_package_name` vem de `entity_name`<br>·`enable_custom_entity_attributes` é forçado a `true` quando há atributos<br>·`entity_contract_file_path` só é emitido depois de gravar o contrato |

**Precedência** (quem vence quando duas fontes discordam):

```text
CONTRACT  >  CONTRACT_DERIVED  >  semântica da entidade  >  INFERENCE (use case)  >  DEFAULT (catálogo)
```

* Um valor **literal** no contrato nunca é sobrescrito.
* Um valor **inferido do use case** só entra quando o contrato silencia — e só para `entity_name`, a única flag da allowlist de inferência.
* O **default** é o último recurso — e nunca é vazio.

**O que é cada fase** (tabela de apoio):

| Nível | O que é numa frase | Exemplo canônico | Reporta como (06) |
|---|---|---|---|
| ① CONTRACT | Valor escrito **literalmente** no JSON pelo operador | `"entityName": "Cliente"` | `CONTRACT` |
| ② CONTRACT_DERIVED | Valor **calculado do contrato** por regra mecânica | `entity_package_name` ← `entity_name` em minúsculas | `CONTRACT_DERIVED` |
| ③ Semântica da entidade | O que o **próprio contrato revela** ser a entidade: nome, pacote, atributos — e o **tipo vem do valor** (`true`→`Boolean`, decimal→`Double`, objeto→POJO em `.nested`, array→`String`) | `"ativo": true` → campo `Boolean ativo` | aparece como `CONTRACT` ou `CONTRACT_DERIVED` — é um **degrau de decisão**, não uma tag do relatório |
| ④ INFERENCE (use case) | Proposta lida do **texto narrativo** — só para flags elegíveis: `entity_name` é a única da allowlist | contrato silencia + use-case menciona "clientes" → `Cliente` | `INFERENCE` (fora da allowlist vai para "descartadas") |
| ⑤ DEFAULT (catálogo) | Valor de fábrica da flag no catálogo | `api_port=8080`, `java_version=21`, `entity_name=MyEntity` | `DEFAULT` |

> **O `id` é campo técnico, não atributo.** O archetype gera sempre o campo `<entidade>Id` (`Long`); o template descarta qualquer atributo chamado `id` ou `<entidade>Id`. O `id` do contrato corresponde a esse campo técnico — ele **não** entra em `custom_entity_attributes_list` nem é listado como atributo derivado (`01` §4.3; a mesma regra está na descrição de `custom_entity_attributes_list` no catálogo).

<a id="inferencia-de-contrato-e-use-case-do-prompt-ao-maven-cli-o-portao-final-e-o-resultado" name="inferencia-de-contrato-e-use-case-do-prompt-ao-maven-cli-o-portao-final-e-o-resultado"></a>
### 5) A Validação final 🚪 <a href="#indice">↑ topo</a>

*   **Regra Geral:**
    * Antes de montar a linha, **cada valor** passa pela `validationRegex` localizada em sua flag 
     (`requiredProperties`). 
    * É importante notar que **todo este fluxo ocorre dentro da LLM**: 
      * o agente não apenas propõe, 
      * mas **constrói e executa** a linha de comando, 
        * apontando para as coordenadas do archetype que deve estar **previamente instalado no repositório local (~/.m2)**.

*   **Validação Estrita:**
    *   valor válido → mantém valor e fonte;
    *   valor inválido → cai no `defaultValue` **com aviso barulhento** (nunca em silêncio).
        *   **Objetivo:** 
          * Garantir que o operador esteja ciente de que 
          * o valor inferido ou fornecido foi rejeitado
          * por não cumprir o contrato técnico da flag
          * forçando o uso do padrão seguro.
        *   *ex.:* derivar `api_port=abc` → a regex `^([1-9][0-9]{3,4})$` rejeita → o valor final vira `8080`.
        * exemplo de codigo `validationRegex'`:
```xml
<requiredProperty key="enable_s3_storage">
    <defaultValue>false</defaultValue>
    <validationRegex>^(true|false)$</validationRegex>   <<< Exemplo
</requiredProperty>
```

*   **Execução pelo Agente:**
    *   O agente materializa o contrato em disco, gera o hash SHA-256 e monta o comando `mvn archetype:generate`.
    *   A execução consome o artefato localmente (`com.eda-archetype:eda-archetype`), garantindo que o comportamento seja o do JAR empacotado e não de arquivos soltos na árvore de desenvolvimento.
    *   O agente valida a árvore gerada e verifica a integridade (compilação via `mvn clean compile`) sem intervenção manual no código.

Uma linha única, sem prompt interativo.
```bash
mvn org.apache.maven.plugins:maven-archetype-plugin:3.4.1:generate --batch-mode \
  -DarchetypeGroupId=com.eda-archetype -DarchetypeArtifactId=eda-archetype -DarchetypeVersion=0.0.1-SNAPSHOT \
  -DgroupId=org.clinica -DartifactId=cliente -Dpackage=org.clinica \
  -Dentity_name=Cliente -Dentity_package_name=cliente \
  -Denable_custom_entity_attributes=true \
  "-Dcustom_entity_attributes_list=String:nome,Boolean:ativo,String:nascimento" \
  -Dapi_port=8080 -Djava_version=21 \
  "-DoutputDirectory=C:/Temp/eda-sandbox"
```

> ⚠️ **Três notas de ambiente** (revalidadas em 2026-11-23, Maven 3.9.16 + Windows):
> 1. O goal `generate` do `maven-archetype-plugin` **3.4.1** (versão fixa na linha de comando e no `pluginManagement` deste `pom.xml`) declara `executePhase` (`generate-sources`) e `requiresProject=false`: rodar de uma pasta sem `pom.xml` funciona — o Maven usa o "Maven Stub Project (No POM)" (`standalone-pom`). O destino continua vindo de `-DoutputDirectory`.
> 2. No **PowerShell**, coloque **cada `-D` entre aspas**. Sem isso, a vírgula de `custom_entity_attributes_list` é tratada como separador de array e o valor chega truncado (`com.eda-archetype` vira `com`, por exemplo).
> 3. O `-DoutputDirectory` é emitido com **barras normais** (`C:/Temp/eda-sandbox`), mesmo no Windows: é a forma que a regra normativa exige (`03-regras-obrigatorias-de-geracao` §3).

> O `-DoutputDirectory` mantém o projeto gerado **fora** da árvore do archetype — sem arquivos `untracked` no Git e sem o IDE indexar dois `pom.xml` aninhados. A propriedade é **nativa do plugin de geração de archetype** (não é uma flag do catálogo) e o valor vem da marcação **PASTA DE DESTINO** do spec. O exemplo acima é ilustração; a regra normativa está em `llm-maven-flags-inference/`.

> **Onde está o detalhe de cada coluna?** Em [`src/main/resources/llm-maven-flags-inference/03-regras-de-formacao-do-contrato/`](src/main/resources/llm-maven-flags-inference/03-regras-de-formacao-do-contrato/): `01` (entidade e atributos), `02` (tipos e enums), `03` (aninhados), `04` (precedência e validação), `05` (materialização do contrato) e `06` (proveniência). O ponto de entrada é `llm-maven-flags-inference/01-entrada-inicializacao/00-entry-point.md`.


















<a id="instalacao" name="instalacao"></a>
## Instalacao 📦 <a href="#indice">↑ topo</a>

<a id="instalacao-pre-requisitos" name="instalacao-pre-requisitos"></a>
### Pré-requisitos <a href="#indice">↑ topo</a>

* **Java 17+** e **Maven 3.9.x** (build/teste validado com Maven 3.9.16 + Java 17.0.2).
  * O default do catálogo é `java_version=21`: um projeto gerado **sem** `-Djava_version` exige Java 21 para compilar.
  * As fixtures de integração usam `java_version=17`.
  * A `constitution.md` e o `specs/001` declaram Java 17; o README não deve tratar 21 como pré-requisito único.
* Conferir a instalação: `mvn -version`.
* Maven 'nao se atualiza' → instalar a versao mais recente:
  * Encontrar a versao mais recente em [Apache Maven](https://maven.apache.org/install.html)
  * Baixar o 'bin.zip', descompactar e atualizar o `PATH` (no Windows).

---

<a id="instalacao-instalar-e-publicar-este-archetype" name="instalacao-instalar-e-publicar-este-archetype"></a>
### Instalar e publicar este archetype <a href="#indice">↑ topo</a>

**a) Instale no repositório local do Maven:**

```bash
mvn clean install
```

* A fase `install` executa, em ordem: as **suítes unitárias** do pós-processador (fase `test`),
  as **fixtures** de integração (fase `integration-test`) e a publicação no repositório local.
  * ⚠️ A fase `integration-test` é **anterior** a `install` → `mvn clean install integration-test`
    roda a suíte de integração **duas vezes**. Use `mvn clean install`.
* Opções:
  * `-Darchetype.test.skip=true` → instala **sem** rodar as fixtures;
  * não há filtro por nome no plugin 3.4.1: `-Darchetype.test.projects` **não existe**.
    Para rodar uma fixture isolada, aponte `-Darchetype.test.projectsDirectory` para um diretório
    que contenha apenas ela (ex.: uma cópia em `target/it-single/`).
* O registro no catálogo local é feito pelo `maven-archetype-plugin:3.4.1:update-local-catalog`,
  disparado na fase `install`.

**b) Resultado da instalação 🎨**

![img.png](assets/archetype-log.png)
**Legenda:**

* **Vermelho:** o nome do archetype criado
* **Verde:** catálogo que registra a instalação
* **Amarelo:** Destino das instalações
* **Azul:** Projeto instalado
* **Cinza:** Plugin de instalação

**c) Localização dos elementos 'Locais' do Maven:**

* Repositorio Local: `%USERPROFILE%\.m2\repository\`
  * Ex.: `C:\Users\<seu-usuario>\.m2\repository\`
* Archetype instalado: `%USERPROFILE%\.m2\repository\com\eda-archetype\eda-archetype\0.0.1-SNAPSHOT\`
* Catalogo de 'Archetypes Locais': `%USERPROFILE%\.m2\repository\archetype-catalog.xml`

**d) Visualizando/Checando a Instalação**
  ![img_1.png](assets/archetype-log_1.png)

**e) Exemplo ilustrativo de seleção no catálogo local**
  ![img_2.png](assets/archetype-log_2.png)
  - A imagem é ilustrativa; o entry deste projeto é `com.eda-archetype:eda-archetype`.
















<a id="cronograma-do-projeto" name="cronograma-do-projeto"></a>
## Cronograma do Projeto 🏗️ <a href="#indice">↑ topo</a>

<a id="cronograma-do-projeto-milestones" name="cronograma-do-projeto-milestones"></a>
### 1) Milestones 📅 <a href="#indice">↑ topo</a>

* CRUD
  - Basic
  - Advanced

* Integration Testes
  - Repo
  - Service
  - Controller

  * DB's for Tests
    - H2
    - TestContainer

* Externalized Configurations
  - App properties
  - Excecoes
  - Mensagens
  - I18N
  - Rotas
  - Credentials (Unpushaveis)
  - etc...

* Type Safe 'Externalized Properties/Configuration' Binding
  - Filetypes:
    - .proprerties
    - .yml
  - Types:
    - Simple
    - List
    - Map
  - Mutable / Immutable
  - Validation

* Good Practices
  - Pageable
  - HATEOAS

* DTO
  - Tipos
    - Request
    - Response
    - Entity protection

* Mapper

* Exceptions
  - Global
  - Customizadas
  - Segurança
  - Autenticação

* BeanValidation
  - Exceções
  - Validações Personalizadas
    - Pattern Regex

* Cache

* I18N

* Testes de Contratos
  - Contratos
    - JSON
  - SchemaValidator
    - JSONSchema

* Documentação
- Swagger

* Database
  - DBs:
    - MongoDB
    - MySQL
    - Postgres
  - DDL
    - Hibernate
    - Flyway
  - Persistence
    - Spring Data
    - JDBC
    - JDBC Clients

* Autenticação (Spring Security)
  - JWT:
    - Simples
    - Oauth Server
  - OAuth2
    - GitHub
    - Google
    - Facebook

* DevOps
  - Docker
    - Folder
    - Dockerfiles
    - Docker Secrets
    - Docker-Compose
  - DevSecOps:
    - ![dev-sec-ops.png](assets/dev-sec-ops.png)
  - CI/CD
  - CloudFlare
  - AWS

* Seguranca
  - SAST (Static Application Security Testing)
    - Credentials:
      - GitGuardian:
        - dificil instalacao
        - Freemium
      - GitLeaks:
        - facil instalacao
        - Open-source
        - Depende de regras personalizadas, para credenciais customizidas
  - Proxy Reverso (Nginx)

* Modularidade

* Performance
  - Load Balancer (Nginx)
  - Cache
  - CDN
  - Virtual Threads

* API Proxies  (Internal UI Clients)
  - DB
  - Docker (Portainer)
  - Spring actuator (graphana)
  - Documentacao (swagger)
  - Log Kibana (logstash + Elasticsearch)

<a id="cronograma-do-projeto-task-planning" name="cronograma-do-projeto-task-planning"></a>
### 2) Task Planning 📝️ <a href="#indice">↑ topo</a>

1. DTO
  - Libraries
    - MapStruct (AutoAPI)
    - MyBatis (Guang)
    - ModelMapper
  - Request / Response
  - AbstractDto
    - Public class to facilitate DTO creation (Guang)

2. Lombok
  - AutoAPI
  - Guang

3. Database Versioning
  - FlyWay
  - Liquibase (AutoAPI)

4. Documentation
  - Swagger (AutoAPI + Guang)
  - Montagem Automática de README.md

5. Testing
  - DataBuilder (Guang)
  - CustomUtils (Guang)
    - Convert Java object to JSON string
    - Handle conversion failure with RuntimeException
  - TestController (Guang)
    - Unit testing with:
      - `@Transactional`
      - Mockito
  - Test-Db:
    - H2
    - TestContainer

6. Service
  - Transactional (Guang)
  - Dto-Mapper (Guang)
  - Apache Commons BeanUtils (Guang)
    - Copy properties from object to entity

7. Controller
  - @Validated on DTO (Guang)
    - BeanValidation
  - Pageable (Guang)
  - Dto-Mapper (Guang)

8. Repository
  - JPA Repository (AutoAPI + Guang)
  - CRUD Repository (AutoAPI)
  - JpaSpecificationExecutor (Guang)
  - Transactions (ACID)

9. Entities
  - POJO
  - Records

10. Safety
  - Input:
    - sanitizing/encode (replace)
    - filtering (block)
  - Shield Dtos:
    - BeanValidation REGEX
  - OWASP
  - Credentials Protection
    - Externalized Properties-File FROM .Env-file
    - Gitleaks (SAST)
      - Nao sera usado, pois depende de instalacao local
      - Apenas 'Recomendado como 2 layer de protecao"'
  - Sanitização de URL:
    - pré despatcherServlet
    - Header Request

11. Log
  - Logback:
    - logback-spring
    - logback-test
  - ELK:
    - Elasticsearch
    - Logstach
    - Kibana
    - Fluxo do ELK:
> 1) O WebApp envia os logs p/ (logstash.conf: "INPUT") APPENDER-TCP-LOGSTASH "<destination>" do Logback-spring.xml
> 2) O Logstasch (logstash.conf: "OUTPUT") envia os 'Logs Processados' p/ 'elasticsearch:9200'
> 3) O kibana exibe os Logs recebidos no 'elasticsearch:9200'

12. Health Check
  - Actuator

13. API Proxies (Internal UI Clients)
  - DBA Client
  - Docker (Portainer)
  - Spring actuator (graphana)
  - Documentacao (swagger)
  - Log Kibana (logstash + Elasticsearch)

14. Type Safe Externalized Properties/Configuration Binding
  - Application.propriedades
  - Mensagens:
    - Gerais
    - Excecoes
  - I18N (Traducoes)
  - Rotas
  - Credentials(secrets) Vault 'Unpushaveis'

<a id="cronograma-do-projeto-concluded-tasks" name="cronograma-do-projeto-concluded-tasks"></a>
### 3) Concluded Tasks ✅ <a href="#indice">↑ topo</a>

- [x] Credentials protection
  - [x] Gitleaks / GitGuardian (Alternative Approaches)
    - CONS:
      - GitGuardian:
        - is not free
        - Must be installed locally
      - GitLeaks:
        - dont works 'efficiently' with Custom-Credentials
  - [x] **'Encrypted Folder + Global GitIgnore'** _(Chosen Approach)_
    - [x] Encripted Folder:
      - Win10 BitLocker
      - ALL **Credentials-Files** (any sensitive data) must be storaged on this folder 'individually'
      - The folder will encrypt All its content automatically (files + subfolders)
      - This folder:
        - "IS NOT VERSIONED/COMMITTED" _(MUST be inserted in GitIgnore Global)_
        - 'dont belong' to any project
        - is not inside any project
        - is not inside any folder, from any project
      - Example:
        - <img alt="credentials-flow" height="140" src="assets/credentials-approuch.png" width="364"/>
    - [x] **GitIgnore-Global**:
      - Will detect (excluding for any commit):
      - Global GitIgnore CLI:
        - _Creating_ an "Global GitIgnore" **'NOT CREATED YET'**:
          - _git config --global core.excludesfile C:\your-path\.gitignore_global_
        - _Finding_ an **'ALREADY EXISTING FILE'**:
          - _git config --global core.excludesfile_
        - _GitIgnore-Global_ **'HELP'** can be found, in: 
          - [GitHub - Global GitIgnore Documentation](https://gist.github.com/subfuzion/db7f57fff2fb6998a16c)
      - Example:
        - <img alt="GitIgnore-Global" height="72" src="assets/git-ignore-global.png" width="75"/>

      - Exclusion 'keywords'(fragment-word in begin/middle/end), such as:
          * docker_secret
          * dockersecret
          * secret
          * authorization
          * authentication
          * secret
          * secretkey
          * credential
          * token
          * etc...
    - [x] **_'Protected-properties-folder'_**
      - App Folder for 'sensitive-properties'
      - These 'sensitive-properties' will data from receive "Environment_Variables"
      - These "Environment_Variables" will be extracted from the **'Credentials-Files'**
      - All **'Credentials-Files'** will be stored in a **'Windows Encrypted Folder'**

- [x] Log
  - [x] Logback:
    - Logback-spring + logback-test
    - Console banner (dev + test)
  - [x] Logback - Console Appender:
    - 03 types:
      - Default (Spring)
      - Tabular
      - Granular
  - [x] Logback - Extra Appenders:
    - Email
    - RollingFile storage
      - classic
      - rolling
      - logstash
    - TCP-SOCKETS for logstash (CPanel)
  - [x] Log Hierarchy:
    - <img alt="Log Hierarchy" height="56" src="assets/log-scale.png" width="179"/>

- [x] Type Safe 'Externalized Properties/Configuration' Binding
  - Types:
    - Constructor Binding (Immutable)
      - Record (native: no setters + canonical constructor + only constants[finals])
        - OBS: Implicit Canonical constructor not allow @ConstructorBinding annotation
      - Class (no setters + canonical constructor + only constants[finals])
        - OBS: All arguments constructor (canonical constructor in Records)
    - JavaBean Binding (Mutable)
      - No arguments constructor (implicit default constructor)
      - Setter mandatory
      - Only Classes (with setters/mutators)
  - **_'Context Scanning'_**:
    - Constructor Binding + JavaBean Binding:
        * EnableConfigurationProperties (define the class to be scanned)
        * _ConfigurationPropertiesScan_ (define the package to be scanned)
    -  JavaBean Binding:
        * Stereotype Annotations: Component or Configuration
  - File Types:
    - Properties
    - YAML
  - Formats:
    - Simple
    - Map
    - List
  - Validation:
    - Spring-Validation
      - jakarta.validation.constraints
    - Externalization of Messages Absence - Explanation:
      - Mensagens de 'falha de validacao' serao hardcoded
      - Pois, servem apenas para 'Log' ou 'debug' da API (Internal Messages)
      - Nao destinando-se ao 'output'(User/Admin Messages) da API
      - Nao sendo entao, objeto de I18N ou Externalizacao

  - [ ] TestDB
    - H2
    - TestContainer

- [ ] CPanel - API Proxies (UI Clients)
  - [ ] Log Kibana (ELK - Elasticsearch + Logstash + Kibana)
  - [ ] Docker (Portainer)
  - [ ] DB (DB-Client)
  - [ ] Documentacao (Swagger)
  - [ ] Spring actuator (Graphana)

<a id="cronograma-do-projeto-next-version" name="cronograma-do-projeto-next-version"></a>
### 4) Next Version 🔮️ <a href="#indice">↑ topo</a>

  - JpaSpecificationExecutor
  - Upload
  - Download
  - Oauth2 (Google + Facebook + Linkedin)
  - Swagger
  - Performance (ECache + Virtual Threads)
  - Multi-Tenant
  - Modularity (Java)
  - "Feature" Generation/Automation
  - "Component" Generation
  - Stripe
  - Document/PDF Visualizer
  - 2FA Authentication
  - Passwordless Authentication
  - UbyKey (OTP)












<a id="acrescentando-novas-flags" name="acrescentando-novas-flags"></a>
## Acrescentando Novas Flags ➕️ <a href="#indice">↑ topo</a>

<a id="acrescentando-novas-flags-areas-alteradas-pela-adicao" name="acrescentando-novas-flags-areas-alteradas-pela-adicao"></a>
### 1) Areas alteradas pela adição: <a href="#indice">↑ topo</a>

   + **Processador:** [_"ARCHETYPE-METADATA"_.XML](src/main/resources/META-INF/maven/archetype-metadata.xml)
     - Local: _"maven"_
       - Root Path: [src/main/resources/META-INF/maven](src/main/resources/META-INF/maven)

  + **Processador Test:** [_"ARCHETYPE-PROPERTIES"_.PROPERTIES](src/test/resources/projects/basic/archetype.properties)
    - Local: _"basic"_
      - Root Path: [src/test/resources/projects/basic](src/test/resources/projects/basic)

  + **Pos-Processador:** [_"ARCHETYPE-POS-GENERATE"_.GROOVY (GERADO)](target/classes/META-INF/archetype-post-generate.groovy)
    - Local: _"META-INF"_
        - Root Path: [src/main/resources/META-INF](src/main/resources/META-INF)

  * **NOTA:**
    * Declare a flag no catálogo canônico
      ([`property-<NN>-<flag>.xml`](src/main/resources/META-INF/maven/archetype_metadata_required_properties/))
      e deixe o procedimento do agente montar a linha de comando a partir de uma
      descrição textual + um contrato JSON.
    * O catálogo é lido **em runtime**:
      + **zero** lista de flags hardcoded.
      + Uma flag nova é reconhecida **sem nenhuma alteração no procedimento**.
    * Veja o passo 1 abaixo, o passo 3 (registro da `ENTITY`) e a seção
      **"Procedimento para Especificação"**.

<a id="acrescentando-novas-flags-exemplo-pratico-na-mao-flag-c-manipulacao-de-pasta-e-arquivo" name="acrescentando-novas-flags-exemplo-pratico-na-mao-flag-c-manipulacao-de-pasta-e-arquivo"></a>
### 2) EXEMPLO PRATICO (na mao) - Flag c/ MANIPULACAO de Pasta e Arquivo: <a href="#indice">↑ topo</a>

  * **Cenario:**
    * Flag: `enable_s3_storage`
    * Ela traz **2 arquivos** e **1 pasta**:
      + `src/main/resources/config/s3-storage.properties` (arquivo)
      + `src/main/java/.../config/StorageProperties.java` (arquivo)
      + pasta `docker/minio/` com `docker-compose.yml` (pasta)

  * **Total de arquivos tocados:** 9 CRIADOS + 3 EDITADOS (em 2 arquivos existentes)

---

<a id="acrescentando-novas-flags-exemplo-pratico-na-mao-flag-c-manipulacao-de-pasta-e-arquivo-passo-1-criar-a-pasta-e-os-arquivos-dentro-do-archetype-resources" name="acrescentando-novas-flags-exemplo-pratico-na-mao-flag-c-manipulacao-de-pasta-e-arquivo-passo-1-criar-a-pasta-e-os-arquivos-dentro-do-archetype-resources"></a>
#### PASSO 1 - Criar a pasta e os arquivos dentro do ARCHETYPE-RESOURCES: <a href="#indice">↑ topo</a>

  * Primeiro se cria a **pasta** e os **arquivos** que a flag traz.
```bash
src/main/resources/archetype-resources/
├── src/main/resources/               <- JA EXISTE
│   └── config/                       <- CRIAR
│       └── s3-storage.properties     <- CRIAR
├── src/main/java/                    <- JA EXISTE
│   └── config/
│       └── StorageProperties.java    <- CRIAR
└── docker/                           <- JA EXISTE
    └── minio/                        <- CRIAR
        └── docker-compose.yml        <- CRIAR
```

  * Conteudo de `src/main/resources/config/s3-storage.properties`:
```properties
storage.type=${enable_s3_storage}
storage.bucket=app-uploads
storage.endpoint=http://localhost:9000
```
    - OBS: o `${enable_s3_storage}` aqui **NAO** faz nada.
    - O Velocity so substitui o que esta entre `#if(...)`.

  * Conteudo de `src/main/java/config/StorageProperties.java`:
```java
package ${package}.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "storage")
public class StorageProperties {
    private String type;
    private String bucket;
    private String endpoint;
    // getters e setters
}
```
    - OBS: aqui o `${package}` **FUNCIONA**, porque o arquivo passa pelo Velocity.

  * Conteudo de `docker/minio/docker-compose.yml`:
```yaml
services:
  minio:
    image: minio/minio:latest
    ports:
      - "9000:9000"
```

---

<a id="acrescentando-novas-flags-exemplo-pratico-na-mao-flag-c-manipulacao-de-pasta-e-arquivo-passo-2-criar-a-property-property-37-enable-s3-storage-xml" name="acrescentando-novas-flags-exemplo-pratico-na-mao-flag-c-manipulacao-de-pasta-e-arquivo-passo-2-criar-a-property-property-37-enable-s3-storage-xml"></a>
#### PASSO 2 - Criar a property 'PROPERTY-37-ENABLE_S3_STORAGE.XML': <a href="#indice">↑ topo</a>

  * Local: [_archetype_metadata_required_properties_](src/main/resources/META-INF/maven/archetype_metadata_required_properties/)
  * Nome do arquivo: `property-37-enable_s3_storage.xml`
  * Convencao: `property-<NN>-<flag>.xml`, com **um arquivo por flag**.
    * `NN` = proximo numero livre na ordem canonica. O catalogo atual tem **35 arquivos** (`02` a `36`), entao esta flag seria a **`37`**.
  * Conteudo completo (FORMATO OBRIGATORIO):
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!-- PROPERTY-37-ENABLE_S3_STORAGE.XML ___________________________ -->
<!-- FLAG: enable_s3_storage -->
<!--   DESCRIPTION : Habilita o armazenamento de arquivos em S3 -->
<!--                  (MinIO local em desenvolvimento). -->
<!--   PROMPT      : Enable 'S3 Storage' -->
<!--   TYPE        : boolean -->
<!--   DEFAULT     : false -->
<!--   DOMAIN      : true | false -->
<!--   VALIDATION  : ^(true|false)$ -->
<requiredProperty key="enable_s3_storage">
    <defaultValue>false</defaultValue>
    <validationRegex>^(true|false)$</validationRegex>
</requiredProperty>
```
    - **REGRA CRITICA:** o `<defaultValue>` **NUNCA** pode ser vazio.
    - Indentacao: **4 ESPACOS** (nao 8).

---

<a id="acrescentando-novas-flags-exemplo-pratico-na-mao-flag-c-manipulacao-de-pasta-e-arquivo-passo-3-registrar-a-entity-da-nova-property-no-archetype-metadata-xml" name="acrescentando-novas-flags-exemplo-pratico-na-mao-flag-c-manipulacao-de-pasta-e-arquivo-passo-3-registrar-a-entity-da-nova-property-no-archetype-metadata-xml"></a>
#### PASSO 3 - Registrar a ENTITY da nova property no 'ARCHETYPE-METADATA.XML': <a href="#indice">↑ topo</a>

  * Local: [archetype-metadata.xml](src/main/resources/META-INF/maven/archetype-metadata.xml)
  * Sao **DUAS** edicoes neste arquivo:

  * Edicao A - Dentro do `<!DOCTYPE>` (ordem numerica, `02` -> `37` — o `01` e lacuna):
```xml
    <!ENTITY property-36-enable_folder_src_test_java SYSTEM "archetype_metadata_required_properties/property-36-enable_folder_src_test_java.xml">
    <!ENTITY property-37-enable_s3_storage SYSTEM "archetype_metadata_required_properties/property-37-enable_s3_storage.xml">
```

  * Edicao B - Dentro do `<requiredProperties>` (mesma ordem numerica):
```xml
        &property-36-enable_folder_src_test_java;
        &property-37-enable_s3_storage;
```
    - **ATENCAO:** se este passo for esquecido, o arquivo criado no Passo 2 **nunca sera lido**.

---

<a id="acrescentando-novas-flags-exemplo-pratico-na-mao-flag-c-manipulacao-de-pasta-e-arquivo-passo-4-criar-os-fileset-xml-um-arquivo-por-fileset" name="acrescentando-novas-flags-exemplo-pratico-na-mao-flag-c-manipulacao-de-pasta-e-arquivo-passo-4-criar-os-fileset-xml-um-arquivo-por-fileset"></a>
#### PASSO 4 - Criar os 'FILESET-*.XML' (um arquivo por `<fileSet>`): <a href="#indice">↑ topo</a>

  * Local: [_archetype_metadata_filesets_](src/main/resources/META-INF/maven/archetype_metadata_filesets)
  * Convencao: `fileset-<NN>-<flag>[-parte].xml`.
    * `NN` = posicao na ordem canonica dos `<fileSet>` (hoje `01` a `18`); o proximo numero livre seria **`19`**.
      - O `NN` e a **ordem** do `<fileSet>` no bloco `<fileSets>` — e o que torna a ordem visivel no nome do arquivo.
      - Escala **propria** dos filesets: nao reaproveita o `NN` das properties (8 filesets `base-*` nao tem flag, e uma flag pode ter varios filesets).
    * **Um arquivo = um `<fileSet>`**.
    * Quando a mesma flag precisa de varios `<fileSet>`, use um sufixo descritivo
      (`-config`, `-docker-minio`, `-src-main-java-config`, ...).
  * Como a flag traz **2 arquivos e 1 pasta**, crie **3 arquivos**:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!-- FILESET-19-ENABLE_S3_STORAGE-CONFIG.XML ___________________ -->
<!-- S3 / MINIO SUPPORT - CONFIG -->
<fileSet filtered="true" encoding="UTF-8">
    <directory>src/main/resources/config</directory>
    <includes>
        <include>s3-storage.properties</include>
    </includes>
</fileSet>
```

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!-- FILESET-20-ENABLE_S3_STORAGE-SRC-MAIN-JAVA-CONFIG.XML _____ -->
<!-- S3 / MINIO SUPPORT - JAVA CONFIG -->
<fileSet filtered="true" encoding="UTF-8">
    <directory>src/main/java/config</directory>
    <includes>
        <include>StorageProperties.java</include>
    </includes>
</fileSet>
```

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!-- FILESET-21-ENABLE_S3_STORAGE-DOCKER-MINIO.XML ____________ -->
<!-- S3 / MINIO SUPPORT - DOCKER -->
<fileSet filtered="true" encoding="UTF-8">
    <directory>docker/minio</directory>
    <includes>
        <include>**/*.*</include>
    </includes>
</fileSet>
```
    - **REGRA:** nunca agrupe varios `<fileSet>` no mesmo arquivo.
    - A associacao com a flag fica no nome do arquivo, nao no agrupamento.

---

<a id="acrescentando-novas-flags-exemplo-pratico-na-mao-flag-c-manipulacao-de-pasta-e-arquivo-passo-5-registrar-os-filesets-no-archetype-metadata-xml" name="acrescentando-novas-flags-exemplo-pratico-na-mao-flag-c-manipulacao-de-pasta-e-arquivo-passo-5-registrar-os-filesets-no-archetype-metadata-xml"></a>
#### PASSO 5 - Registrar os filesets no 'ARCHETYPE-METADATA.XML': <a href="#indice">↑ topo</a>

  * No mesmo arquivo do Passo 3, faca **tres** edicoes no `<fileSets>`
    (uma referencia para cada arquivo criado no Passo 4):
```xml
        &fileset-19-enable_s3_storage-config;
        &fileset-20-enable_s3_storage-src-main-java-config;
        &fileset-21-enable_s3_storage-docker-minio;
```
    - A ordem das referencias deve ser a mesma das entidades no `<!DOCTYPE>`.
    - A ordem e a **ordem numerica** (`19`, `20`, `21`) — o `NN` do nome do arquivo e a posicao no bloco.

---

<a id="acrescentando-novas-flags-exemplo-pratico-na-mao-flag-c-manipulacao-de-pasta-e-arquivo-passo-6-criar-o-variables-nn-s3-storage-groovy" name="acrescentando-novas-flags-exemplo-pratico-na-mao-flag-c-manipulacao-de-pasta-e-arquivo-passo-6-criar-o-variables-nn-s3-storage-groovy"></a>
#### PASSO 6 - Criar o 'VARIABLES-NN-S3-STORAGE.GROOVY': <a href="#indice">↑ topo</a>

  * Local: [_archetype_post_generate_variables_](src/main/resources/META-INF/archetype_post_generate_variables)
  * Nome do arquivo: `variables-NN-s3-storage.groovy` (`NN` = proximo numero livre)
    * Os `variables/` usam hoje `03`–`36` **com lacunas**: livres = `01`, `02`, `15`, `16`, `21`, `24`–`27`, `32`, `35`.
  * Objetivo: "ler" a flag e storing em uma variavel Groovy:
```groovy
// -----------------------------------------------------------------------------// VARIABLES-NN-S3-STORAGE.GROOVY -----------------------------------------------
def keep_s3_storage = "${properties?.enable_s3_storage}"
```
    - **CONVENCAO:** o nome da variavel costuma ser `keep_` + nome da flag.
    - O marcador `// VARIABLES-NN-S3-STORAGE.GROOVY` deve ser **exatamente o nome do arquivo em MAIUSCULAS** (o compositor valida no build).

---

<a id="acrescentando-novas-flags-exemplo-pratico-na-mao-flag-c-manipulacao-de-pasta-e-arquivo-passo-7-criar-o-snippet-nn-s3-storage-groovy-a-manobra-real" name="acrescentando-novas-flags-exemplo-pratico-na-mao-flag-c-manipulacao-de-pasta-e-arquivo-passo-7-criar-o-snippet-nn-s3-storage-groovy-a-manobra-real"></a>
#### PASSO 7 - Criar o 'SNIPPET-NN-S3-STORAGE.GROOVY' (a manobra real): <a href="#indice">↑ topo</a>

  * Local: [_archetype_post_generate_snippets_](src/main/resources/META-INF/archetype_post_generate_snippets)
  * Nome do arquivo: `snippet-NN-s3-storage.groovy` (`NN` = proximo numero livre)
  * Aqui esta a **logica condicional** (manipulacao de pasta/arquivo):

  * **CASO 1 - Apagar ARQUIVO:**
```groovy
// -----------------------------------------------------------------------------






// SNIPPET-NN-S3-STORAGE.GROOVY -------------------------------------------------
if (keep_s3_storage == 'false') {

    Path s3PropertiesPath =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'resources', 'config', 's3-storage.properties')

    log_text += "\nFILE-S3-STORAGE.PROPERTIES:\n $s3PropertiesPath\n"
    deleteFile(s3PropertiesPath.toFile())
}
```

  * **CASO 2 - Apagar PASTA INTEIRA:**
```groovy
// -----------------------------------------------------------------------------






// SNIPPET-NN-S3-STORAGE.GROOVY -------------------------------------------------
if (keep_s3_storage == 'false') {

    Path minioFolder =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'docker', 'minio')

    log_text += "\nFOLDER_MINIO:\n $minioFolder\n"
    deleteFolder(minioFolder.toFile())
}
```
    - **REGRA IMPORTANTE:** `import` **nao** e declarado no snippet. O compositor remove `import`/`//import` de todos os fragmentos e usa o preludio `archetype_post_generate_body/body-01-imports.groovy`, onde `Path`, `Paths` e `Files` ja estao ativos.
    - Se precisar de um import novo, adicione em `body-01-imports.groovy` como `//import ...` (o compositor remove as barras).

---

<a id="acrescentando-novas-flags-exemplo-pratico-na-mao-flag-c-manipulacao-de-pasta-e-arquivo-passo-8-adicionar-na-fixture-de-teste" name="acrescentando-novas-flags-exemplo-pratico-na-mao-flag-c-manipulacao-de-pasta-e-arquivo-passo-8-adicionar-na-fixture-de-teste"></a>
#### PASSO 8 - Adicionar na 'FIXTURE' de teste: <a href="#indice">↑ topo</a>

  * Local: [archetype.properties](src/test/resources/projects/basic/archetype.properties)
  * Adicionar:
```properties
enable_s3_storage=false
```
    - O nome tem que ser **IDENTICO** ao `<key>` do Passo 2.

---

<a id="acrescentando-novas-flags-exemplo-pratico-na-mao-flag-c-manipulacao-de-pasta-e-arquivo-passo-9-recriar-o-archetype-e-testar" name="acrescentando-novas-flags-exemplo-pratico-na-mao-flag-c-manipulacao-de-pasta-e-arquivo-passo-9-recriar-o-archetype-e-testar"></a>
#### PASSO 9 - Recriar o ARCHETYPE e TESTAR: <a href="#indice">↑ topo</a>

  * O arquivo `archetype-post-generate.groovy` e **GERADO**, nao editado.
  * O arquivo final fica em `target/classes/META-INF/` (fora do git e normalmente fora da arvore do IDE); `mvn clean` apaga e o proximo build recria.
  * O build monta ele a partir das pastas:
```bash
archetype_post_generate_body/          (15 arquivos, ordem fixa)
archetype_post_generate_variables/     (25 arquivos, ordem do NN)
archetype_post_generate_generators/    (4 arquivos, ordem do NN)
archetype_post_generate_snippets/      (20 arquivos, ordem do NN)
```
    - `variables-NN-s3-storage.groovy` entra na ordem do NN.
    - `snippet-NN-s3-storage.groovy` idem.

  * Rodar o build:
```bash
mvn clean install
```

  * Testar a geracao (flag LIGADA):
```bash
mvn archetype:generate \
  -DarchetypeGroupId=com.eda-archetype \
  -DarchetypeArtifactId=eda-archetype \
  -DarchetypeVersion=0.0.1-SNAPSHOT \
  -Denable_s3_storage=true
```
    - Se aparecer a pasta `docker/minio/` e o `StorageProperties.java` -> FUNCIONOU.

  * Testar a geracao (flag DESLIGADA):
```bash
mvn archetype:generate \
  -DarchetypeGroupId=com.eda-archetype \
  -DarchetypeArtifactId=eda-archetype \
  -DarchetypeVersion=0.0.1-SNAPSHOT \
  -Denable_s3_storage=false
```
    - Os arquivos devem SUMIR do projeto gerado.

---

<a id="acrescentando-novas-flags-exemplo-pratico-na-mao-flag-c-manipulacao-de-pasta-e-arquivo-resumo-9-arquivos-criados-3-editados-em-2-arquivos-existentes" name="acrescentando-novas-flags-exemplo-pratico-na-mao-flag-c-manipulacao-de-pasta-e-arquivo-resumo-9-arquivos-criados-3-editados-em-2-arquivos-existentes"></a>
#### RESUMO - 9 arquivos CRIADOS + 3 EDITADOS (em 2 arquivos existentes): <a href="#indice">↑ topo</a>

| #  | Arquivo | Acao |
|----|---------|------|
| 01 | `archetype-resources/src/main/resources/config/s3-storage.properties` | CRIAR |
| 02 | `archetype-resources/src/main/java/config/StorageProperties.java` | CRIAR |
| 03 | `archetype-resources/docker/minio/docker-compose.yml` | CRIAR |
| 04 | `archetype_metadata_required_properties/property-37-enable_s3_storage.xml` | CRIAR |
| 05 | `archetype-metadata.xml` | **EDITAR** (2 pontos: DOCTYPE + requiredProperties) |
| 06 | `archetype_metadata_filesets/fileset-19-enable_s3_storage-config.xml` | CRIAR |
| 07 | `archetype_metadata_filesets/fileset-20-enable_s3_storage-src-main-java-config.xml` | CRIAR |
| 08 | `archetype_metadata_filesets/fileset-21-enable_s3_storage-docker-minio.xml` | CRIAR |
| 09 | `archetype-metadata.xml` | **EDITAR** (1 ponto: fileSets, com 3 referencias) |
| 10 | `archetype_post_generate_variables/variables-NN-s3-storage.groovy` | CRIAR |
| 11 | `archetype_post_generate_snippets/snippet-NN-s3-storage.groovy` | CRIAR |
| 12 | `projects/basic/archetype.properties` | **EDITAR** |

---

<a id="acrescentando-novas-flags-exemplo-pratico-na-mao-flag-c-manipulacao-de-pasta-e-arquivo-o-que-nao-foi-feito-e-nao-precisa-ser" name="acrescentando-novas-flags-exemplo-pratico-na-mao-flag-c-manipulacao-de-pasta-e-arquivo-o-que-nao-foi-feito-e-nao-precisa-ser"></a>
#### O QUE NAO FOI FEITO (e nao precisa ser): <a href="#indice">↑ topo</a>

  * Nenhuma lista de flags adicionada em lugar nenhum
  * Nenhuma edicao no `ArchetypePostGenerateComposer.groovy` (ele le as pastas automaticamente)

  * **O procedimento nao precisa saber que `enable_s3_storage` existe.**
  * Ele le o catalogo em runtime.
  * O catalogo acabou de crescer -> a flag ja existe para o agente.

---

<a id="acrescentando-novas-flags-padronizacao" name="acrescentando-novas-flags-padronizacao"></a>
### 3) Padronização: <a href="#indice">↑ topo</a>

  * **FORMATO OBRIGATORIO** _(o mesmo dos 35 arquivos existentes)_:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!-- PROPERTY-12-ENABLE_DOCKER_SUPPORT.XML _____________________________ -->
<!-- FLAG: enable_docker_support -->
<!--   DESCRIPTION : Habilita o suporte a Docker (Dockerfile + docker-compose) no projeto gerado. -->
<!--   PROMPT      : Enable Docker Support -->
<!--   TYPE        : boolean -->
<!--   DEFAULT     : false -->
<!--   DOMAIN      : true | false -->
<!--   DEPENDS_ON  : nao aplicavel -->
<!--   VALIDATION  : ^(true|false)$ -->
<requiredProperty key="enable_docker_support">
    <defaultValue>false</defaultValue>
    <validationRegex>^(true|false)$</validationRegex>
</requiredProperty>
```
    - **NOMENCLATURA:** `property-<NN>-<flag>.xml` — **um arquivo por flag**.
      + `NN` = posicao na ordem canonica (`02` a `36`); o proximo numero livre seria `37`.
      + O trecho `<flag>` deve ser **identico** ao `<requiredProperty key="...">`.
    - **REGRA CRITICA**: o `<defaultValue>` **NUNCA** pode ser vazio.
    - **REGRA DE INDENTACAO**: **4 ESPACOS** _(nao 8)_. Veja os 35 arquivos
      existentes em [_archetype_metadata_required_properties_](src/main/resources/META-INF/maven/archetype_metadata_required_properties/)
      como referencia canonica.

  3. Use a _'REQUIRED-PROPERTY'_, c/ o **_VELOCITY_**, no _**'CODIGO'**_ SE _necessario_
  * **Local:** [_src/main/resources/archetype-resources_](src/main/resources/archetype-resources)

  4. Criar o **_'FILE-SET'_** _**(em caso de"MANIPULACAO DE PASTAS OU ARQUIVOS")**_:
  * Criar um 'arquivo.XML'
    * **Local:** [_src/main/resources/META-INF/maven/archetype_metadata_filesets_](src/main/resources/META-INF/maven/archetype_metadata_filesets)

  * Em sua nomenclatura, esse 'arquivo.XML' tera **_'obrigatoriamente'_**:
    * **'fileset-'** como prefixo
    * apos o prefixo, o **'NN'** (2 digitos) — a posicao na ordem canonica dos `<fileSet>`
    * apos o `NN`, o nome exato da _'SWITCH -D'_ criada
    * um sufixo descritivo quando a flag tiver **mais de um** `<fileSet>`
    * Exemplos:
      * fileset-01-enable_api_cpanel_elk_log_system.xml
      * fileset-07-enable_logback_support.xml
      * fileset-15-enable_jwt_authentication.xml
      * fileset-17-enable_folder_src_test_java-src-test-resources.xml

  * **FORMATO DO ARQUIVO** _(o mesmo dos 18 arquivos de filesets existentes)_:
    * **linha 1**: `<?xml version="1.0" encoding="UTF-8"?>`
    * **linha 2**: o comentario de identificacao — `<!-- FILESET-<NN>-<FLAG>.XML ______ -->`
      (1 espaco apos `<!--` e 1 antes do run de `_`, como nas properties)
    * **linha 3+**: o `<fileSet>` (4 ESPACOS de indentacao nos filhos)
    * **um unico** `<fileSet>` por arquivo

  * **REGRA DE DISTRIBUICAO**: **um arquivo = um `<fileSet>`**
    * Nunca agrupe varios `<fileSet>` no mesmo arquivo.
    * Se a flag precisar de varios `<fileSet>`, crie um arquivo para cada um,
      diferenciando pelo sufixo (ex.: `-src-test-resources`, `-src-test-java`).

  * **REGRA DE INDENTACAO**: **4 ESPACOS** para os filhos do `<fileSet>`; `<fileSet>` na coluna 1
    * Veja os 18 arquivos existentes em [_archetype_metadata_filesets_](src/main/resources/META-INF/maven/archetype_metadata_filesets)
    * como referencia canonica.

  * EXEMPLO 01 - **_'fileSet' de "PASTA"_** criado no arquivo 'fileset-01-enable_api_cpanel_elk_log_system.xml':
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!-- FILESET-01-ENABLE_API_CPANEL_ELK_LOG_SYSTEM.XML __________ -->
<!-- CPANEL / ELK SUPPORT -->
<fileSet filtered="true" encoding="UTF-8">
    <directory>ELK</directory>
    <includes>
        <include>**/*.*</include>
    </includes>
</fileSet>
```

* EXEMPLO 02 - _**'fileSet' de "PASTA"**_ criado no arquivo 'fileset-07-enable_logback_support.xml':
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!-- FILESET-07-ENABLE_LOGBACK_SUPPORT.XML __________________ -->
<!-- LOGBACK SUPPORT -->
<fileSet filtered="true" encoding="UTF-8">
    <directory>src/main/resources/logback-spring-config</directory>
    <includes>
        <include>**/*.xml</include>
    </includes>
</fileSet>
```

* EXEMPLO 03 - **_'fileSet' de "ARQUIVOS/PROPERTIES"_** criado no arquivo 'fileset-18-enable_folder_src_test_java-src-test-resources-properties.xml':
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!-- FILESET-18-ENABLE_FOLDER_SRC_TEST_JAVA-SRC_TEST_RESOURCES_PROPERTIES.XML  -->
<!-- SRC\TEST\RESOURCES\PROPERTIES -->
<fileSet filtered="true" encoding="UTF-8">
    <directory>src/test/resources/properties</directory>
    <includes>
        <include>**/*.yml</include>
        <include>**/*.properties</include>
    </includes>
</fileSet>
```
  * Quando a flag tem varios `<fileSet>`:
    * Um arquivo `fileset-<NN>-<flag>-<parte>.xml` para cada `<fileSet>`.
    * O sufixo `<parte>` identifica a pasta/arquivo coberto.

  5. **Se** o 'novo Criterio' _realizar **"MANIPULACAO DE PASTAS OU ARQUIVOS"**_:
  * Insira a 'pasta/arquivo' a ser manipulado, dentro do 'ARCHETYPE', EM:
    * **Local:** [_src\ "MAIN" \resources\ "ARCHETYPE-RESOURCES"_](src/main/resources/archetype-resources)

  6. Crie uma **pos generate _'VARIAVEL 'GROOVY' P/ MANIPULACAO'_** do 'arquivo/pasta' (se necessario)
  * Criar um 'arquivo.GROOVY'
    * **Local:** [_src/main/resources/META-INF/archetype_post_generate_variables_](src/main/resources/META-INF/archetype_post_generate_variables)

  * Em sua nomenclatura, esse 'arquivo.GROOVY' tera **_'obrigatoriamente'_**:
    * **'variables-NN-'**(PLURAL) como prefixo (`NN` = proximo numero livre)
    * apos o prefixo, um texto semelhante ao _'SWITCH -D'_ criado
    * Exemplo:
      * variables-NN-cpanel.groovy

  * _Dentro deste 'arquivo.GROOVY'_, a 'property' tera **nome semelhante** ao da _'SWITCH -D'_

  * EXEMPLO:

```groovy
// -----------------------------------------------------------------------------// VARIABLES-NN-LOGBACK.GROOVY --------------------------------------------------
def keep_logback_support = "${properties?.enable_logback_support}"
```
  * **CONVENCAO** _(nao obrigatoria)_: o nome da variavel costuma ser
    `keep_` + nome da flag, em `snake_case`.
    + `enable_logback_support` -> `keep_logback_support`
    + `enable_api_cpanel_elk_log_system` -> `keep_cpanel_elk_log_system`

  7. Crie uma **pos generate _'SNIPPET 'GROOVY' P/ MANIPULACAO'_** do 'arquivo/pasta' (se necessario)
  * Criar um 'arquivo.GROOVY'
    * **Local:** [_src/main/resources/META-INF/archetype_post_generate_snippets_](src/main/resources/META-INF/archetype_post_generate_snippets)

  * Em sua nomenclatura, esse 'arquivo.GROOVY' tera **_'obrigatoriamente'_**:
    * **'snippet-NN-'** como prefixo (`NN` = proximo numero livre)
    * apos o prefixo, um texto semelhante ao _'SWITCH -D'_ criado
    * Exemplo:
      * snippet-NN-cpanel.groovy

  * _Os imports_ não são declarados no fragmento: o compositor remove `import`/`//import` de todos os fragmentos e usa o prelúdio `archetype_post_generate_body/body-01-imports.groovy`. Se precisar de um import novo, adicione-o lá como `//import ...`.
  * Se o fragmento **GERA fontes Java**, use `generator-NN-<flag>.groovy` em `archetype_post_generate_generators/` (mesmas regras de marcador e `NN`).

  * EXEMPLO de remocao de **_"FILE"_**:
```groovy

// -----------------------------------------------------------------------------






// SNIPPET-NN-LOGBACK.GROOVY --------------------------------------------------------
if (keep_logback_support == 'true' && keep_cpanel_elk_log_system == 'false') {

    Path logstachPath =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'resources', 'logback-spring-config', 'appender-tcp-elk-logstash.xml')

    log_text += "\nAppenders-tcp-logstash.xml -> resources -> logback-spring-config:\n $logstachPath\n"

    deleteFile(logstachPath.toFile())

    deleteElkLogSystemStructure(log_text)
}
```

  * EXEMPLO de remocao de **_"FOLDER"_**:
```groovy

// -----------------------------------------------------------------------------






// SNIPPET-NN-BINDING-PROPERTIES.GROOVY --------------------------------------------
if (keep_binding_properties == 'false') {

    Path bindingPropertiesPath =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'src', 'main', 'java', entity_name, 'config', 'properties')

    log_text += "\nFOLDER_BINDING_PROPERTIES:\n $bindingPropertiesPath\n"

    deleteFolder(bindingPropertiesPath.toFile())
}
```













<a id="procedimento-para-especificacao-spec" name="procedimento-para-especificacao-spec"></a>
## Procedimento para Especificação/Spec <a href="#indice">↑ topo</a>

<a id="procedimento-para-especificacao-spec-como-usar-no-opencode-ou-no-github-spec-kit" name="procedimento-para-especificacao-spec-como-usar-no-opencode-ou-no-github-spec-kit"></a>
### Como usar no 'OpenCode' OU no 'GitHub Spec Kit' <a href="#indice">↑ topo</a>

* O procedimento completo **está empacotado no JAR** do archetype.
* O operador entrega a **spec**(usando, por ex.: 'GitHub Spec Kit') ao agente(por ex.: OpenCode)
  * A Spec devera obrigatoriamente conter (ORDEM OBRIGATORIA):
    * Preambulo `0. PROCEDIMENTO` — ponteiro para o `00-entry-point` dentro do JAR; nao e entrada de derivacao
    * Tipo de Modulo (Root/Principal ou Branch/Secundario)
    * PACKAGE BASE (sem o nome da entidade)
    * Descricao do USE-CASE desejado para o modulo (OBRIGATORIO)
    * O Contrato que sera fornecido pelo modulo a ser criado (OBRIGATORIO)
    * O Local de destino onde o projeto deve ser gerado (OBRIGATORIO)
  * Detalhes tecnicos adicionais:
    * se forem flags do catalogo, vao no contrato;
    * se forem contexto, vao no use-case;
    * instrucoes de execucao (PowerShell, `mvn clean verify`, IDE) sao opcionais e nao sao entradas de derivacao.
  * Nao fazem parte da Spec: `groupId` (= package base), `artifactId`, `version` do modulo, flags e defaults — o procedimento resolve.
* OBS1: Os procedimentos de Inferencia sao originariamente 'empacotados' na JAR.
* OBS2: O README não é fonte de runtime, sendo ele mera documentacao.

> ⚠️ **Advertência — premissa de entrada obrigatória.**
> Este modelo de prompt só é válido com o preâmbulo, **as _'duas partes'_, _'nesta ordem'_**, mais a **pasta de destino** e as demais entradas do spec (tipo de módulo e package base):
>
> 1. **use-case** (texto) — seção `3. USE-CASE` ou marcador `USE CASE`;
> 2. **contrato JSON** (objeto não vazio) — seção `4. CONTRATO JSON` ou marcador `CONTRATO`;
> 3. **pasta de destino** (caminho absoluto) — seção `5. PASTA DE DESTINO`.
>
> O procedimento Para e Reporta quando:
>
> - o use-case estiver **AUSENTE** ou em branco (**Gate A**);
> - o contrato estiver **AUSENTE**, em branco, for `{}` ou **'NAO FOR**' um **'OBJETO JSON'** (**Gate B**);
> - o contrato aparecer **ANTES** do use-case (**Gate C**);
> - a pasta de destino estiver **AUSENTE**, for **relativa/ambígua** ou estiver **dentro da árvore do archetype** (**Gate D**).
>
> O preâmbulo `0. PROCEDIMENTO` não é entrada de derivação: é a ordem de leitura do procedimento dentro do JAR.
> As 5 entradas continuam obrigatórias; os Gates A–D permanecem inalterados.
>
> Não reordene as seções, não omita o contrato, não use `none` como substituto e não presuma destino.
> A fonte normativa é `00-entry-point` §0, dentro do JAR.


<a id="exemplo-de-prompt-para-spec" name="exemplo-de-prompt-para-spec"></a>
#### Exemplo de Prompt p/ Spec <a href="#indice">↑ topo</a>

```text
# TAREFA: Gerar módulo com o Maven Archetype EDA

0. PROCEDIMENTO:
- Com o JAR com.eda-archetype:eda-archetype:0.0.1-SNAPSHOT instalado no
  repositório local, leia
  llm-maven-flags-inference/01-entrada-inicializacao/00-entry-point.md
  de dentro do JAR e siga-o antes de derivar.
- Este spec pode ser executado fora da pasta do repositório do archetype.

1. TIPO DE MÓDULO:
[X] RAIZ
[ ] SECUNDARIO

2. PACKAGE BASE (sem o nome da entidade):
org.clinica

3. USE-CASE:
- Cadastro de clientes de uma clínica
- O cadastro coleta: nome, data de nascimento, endereço, telefone e se o cliente está ativo.
- O sistema expõe uma API REST com autenticação JWT na porta 8080.

4. CONTRATO JSON do módulo:
{
  "id": 1024,
  "nome_cliente": "Maria Silva",
  "data_nascimento": "1990-05-14",
  "endereco": "Rua das Faias, 221",
  "telefone": "11987654321",
  "ativo": true,
  "enable_jwt_authentication": true,
  "api_port": 8080
}

5. PASTA DE DESTINO:
C:\Temp\eda-sandbox
```

* A ordem é normativa: 
  * Sequencia: 
    * **use-case primeiro**;
    * **contrato depois**;
    * **pasta de destino** em seguida;
  * Sem qualquer uma delas
    * com o contrato antes do use-case
    * ou com destino dentro da árvore do archetype
    * o procedimento para (Gates A, B, C e D de `00-entry-point` §0).
* O agente começa por `llm-maven-flags-inference/01-entrada-inicializacao/00-entry-point.md`
  * dentro do JAR empacotado/instalado (`com.eda-archetype:eda-archetype`).
* O `-Dpackage` é sempre o **pacote-base**: o archetype concatena `entity_package_name` automaticamente.
  - ✅ `-Dpackage=org.clinica` → `package org.clinica.cliente;`
  - ❌ `-Dpackage=org.clinica.cliente` → `package org.clinica.cliente.cliente;`
* `entity_package_name` não é entrada da SPEC; o procedimento deriva.
* A SPEC não informa `groupId`, `artifactId` nem `version` do módulo.
  * O procedimento deriva `groupId` do package base, `artifactId` do nome da entidade e aplica a `version` default.

<a id="procedimento-para-especificacao-spec-onde-estao-as-regras-de-formacao-do-contrato" name="procedimento-para-especificacao-spec-onde-estao-as-regras-de-formacao-do-contrato"></a>
### Onde estão as regras de formação do contrato <a href="#indice">↑ topo</a>

* O detalhamento do procedimento não vive mais no README. 
* Ele está empacotado no JAR, em `llm-maven-flags-inference/`, e é lido pelo Agente(ex.: Opencode) antes de qualquer derivação.

**Regras de formação do contrato** (`llm-maven-flags-inference/03-regras-de-formacao-do-contrato/`):

| Arquivo | Papel |
|---|---|
| `01-derivar-a-entidade-do-contrato.md` | nome da entidade, pacote da entidade, extração e sanitização dos atributos |
| `02-resolver-tipos-e-enums.md` | tipos `java.lang` × tipos do projeto × enums |
| `03-derivar-flags-de-tipos-aninhados.md` | POJOs aninhados, imports, enums e tipos aninhados |
| `04-aplicar-precedencia-e-validacao.md` | precedência das fontes e validação contra o catálogo |
| `05-materializar-e-emitir-o-contrato.md` | materialização do contrato e emissão de `entity_contract_file_path` |
| `06-relatar-proveniencia.md` | tabela de proveniência e resumo |

**Templates de contrato** (`llm-maven-flags-inference/04-referencia-templates-de-contrato/`):

| Arquivo | Papel |
|---|---|
| `01-schema-do-contrato.json` | schema permissivo do contrato |
| `02-exemplo-contrato-plano.json` | exemplo com campos de negócio no topo |
| `03-exemplo-contrato-com-objetos-aninhados.json` | exemplo com objetos aninhados |
| `04-exemplo-contrato-com-enums.json` | exemplo com enums |
| `05-exemplo-contrato-com-tipos-do-projeto.json` | exemplo com tipos do projeto |
| `06-exemplo-contrato-com-attributes-explicito.json` | exemplo com `attributes` explícito |

* Campos de negócio viram atributos da entidade; chaves do catálogo viram flags `-D`.
* O contrato pode trazer a coleção `attributes` explícita, que tem precedência sobre os campos avulsos.
* Cada atributo pode declarar o seu `type`; um tipo do projeto é preservado, nomeia o atributo
  e viaja em `custom_entity_attribute_imports` e/ou `contract_enum_definitions`.
* O contrato materializado é copiado para `src/test/resources/contracts/contract.json` no projeto
  gerado (quando `enable_folder_src_test_java = true`) — é a base dos testes de esquema do módulo.

<a id="procedimento-para-especificacao-spec-onde-usar-cada-abordagem" name="procedimento-para-especificacao-spec-onde-usar-cada-abordagem"></a>
### Onde usar cada abordagem <a href="#indice">↑ topo</a>

| Abordagem | Quando usar | Prós | Contras                        |
|---|---|---|--------------------------------|
| OpenCode | Testes rápidos, exploração, validação de ideias | Simples e imediato | Sem rastreabilidade; você cola a cada pedido |
| Spec Kit | Projeto que vai evoluir | Contrato vira artefato versionado; fases seguintes reutilizam | Burocrático                    |

**Em ambos os casos**, o seu modelo mental é o mesmo: descrição em cima,
JSON indentado embaixo. A parte técnica (`stdin`, flags, `-D` na saída)
fica a cargo do procedimento empacotado no JAR —
`llm-maven-flags-inference/01-entrada-inicializacao/00-entry-point.md` e as regras em
`llm-maven-flags-inference/03-regras-de-formacao-do-contrato/`.

















<a id="montagem-dos-arquivos-de-archetype" name="montagem-dos-arquivos-de-archetype"></a>
## Montagem dos Arquivos de Archetype 🏗️ <a href="#indice">↑ topo</a>

<a id="montagem-dos-arquivos-de-archetype-archetype-metadata-xml" name="montagem-dos-arquivos-de-archetype-archetype-metadata-xml"></a>
### 1) Archetype-Metadata.xml 🏗️ <a href="#indice">↑ topo</a>

* **OBJETIVO**
  * Manter 1 arquivo final [ARCHETYPE-METADATA.XML](src/main/resources/META-INF/maven/archetype-metadata.xml) consumido pelo (`maven-archetype-plugin`),
    mas organizado no source em modulos menores (**35 arquivos de propriedades + 18 de filesets**).

* **MECANISMO**
  * O XML "core" declara DOCTYPE com (`ENTITY property-<NN>-<flag> SYSTEM ".../property-<NN>-<flag>.xml"`) para cada snippet de propriedade
    e (`ENTITY fileset-<NN>-<flag> SYSTEM ".../fileset-<NN>-<flag>.xml"`) para cada snippet de fileset.
  * O total e **53 entidades** (35 + 18).
  * O corpo referencia as entities como (`&property-<NN>-<flag>;`) (em (`requiredProperties`)) e (`&fileset-<NN>-<flag>;`) (em (`fileSets`)).
  * Resultado: o arquivo-fonte continua sendo um unico XML valido, legivel, com (`&`) no lugar dos blocos.

* **PLUGIN** — [`xml-maven-plugin`](https://www.mojohaus.org/xml-maven-plugin/) 1.2.1
  * Fase: (`generate-resources`) (antes do (`maven-archetype-plugin`) ler o metadata).
  * Goal: (`transform`) — aplica XSLT em um input e grava o resultado no outputDir.
  * Input : (`src/main/resources/META-INF/maven/archetype-metadata.xml`)
  * Output: (`${project.build.outputDirectory}/META-INF/maven/archetype-metadata.xml`)
    (ou seja, (`target/classes/META-INF/maven/...`), dentro do JAR do arquetipo).
  * O (`<resources>`) do pom **exclui** o (`archetype-metadata.xml`) da copia convencional,
    garantindo que so a versao transformada va para o classpath.

* **ARCHETYPE-METADATA-IDENTITY.XSL**
  * XSLT 1.0, template identidade: (`<xsl:template match="@*|node()"><xsl:copy><xsl:apply-templates.../></xsl:copy></xsl:template>`).
  * Copia verbatim, forca reescrita do XML — efeito colateral desejado: o parser XSLT resolve as (`SYSTEM`) entities DOCTYPE
    e o output sai com os blocos inline (sem DOCTYPE/entities no XML final).

* **LAYOUT FISICO**
  * [`archetype_metadata_required_properties/`](src/main/resources/META-INF/maven/archetype_metadata_required_properties)
    => **35** (`property-<NN>-<flag>.xml`) (**1 por flag**, numerados `02` a `36`).
  * [`archetype_metadata_filesets/`](src/main/resources/META-INF/maven/archetype_metadata_filesets)
    => **18** (`fileset-<NN>-<flag>[-parte].xml`) (**1 `<fileSet>` por arquivo**, numerados `01` a `18`).
  * [`archetype-metadata-identity.xsl`](src/main/resources/META-INF/maven/archetype-metadata-identity.xsl) => folha de estilo XSLT.
  * [`archetype-metadata.xml`](src/main/resources/META-INF/maven/archetype-metadata.xml) => core (DOCTYPE com 53 entidades + (`&property-<NN>-<flag>;`)/(`&fileset-<NN>-<flag>;`)).

* **FLUXO BUILD**
  * source (`archetype-metadata.xml`) (com (`&entity;`))
    * => (`xml-maven-plugin:transform`) aplica (`archetype-metadata-identity.xsl`)
    * => (`target/classes/META-INF/maven/archetype-metadata.xml`) (XML completo, sem DOCTYPE)
    * => empacotado no JAR do arquetipo
    * => lido pelo (`maven-archetype-plugin`) 3.4.1 em (`mvn archetype:generate`).

<a id="montagem-dos-arquivos-de-archetype-archetype-post-generate-groovy" name="montagem-dos-arquivos-de-archetype-archetype-post-generate-groovy"></a>
### 2) Archetype-Post-Generate.groovy 🧬 <a href="#indice">↑ topo</a>

* **OBJETIVO**
  * Manter 1 arquivo final gerado em (`target/classes/META-INF/archetype-post-generate.groovy`) e consumido pelo (`maven-archetype-plugin`)
    durante o pos-processamento do projeto gerado,
    mas organizado no source em modulos menores (15 arquivos de (`body/`) + 25 de (`variables/`) + 4 de (`generators/`) + 20 de (`snippets/`)).

* **MECANISMO**
  * O arquivo final **nao existe como fonte unica** — ele e remontado a cada build a partir de 4 diretorios.
  * A [`List<String> SECTIONS`](src/main/groovy/ArchetypePostGenerateComposer.groovy) no topo do compositor e a **fonte unica da verdade da ordem**.
  * Resultado: o source continua sendo uma colecao de 64 arquivos `.groovy` granulares, e o arquivo final e remontado sob demanda.
  * Visibilidade: (`target/`) esta no (`.gitignore`) e o IDE normalmente o exclui — o arquivo **nao aparece** na arvore do projeto. Use `mvn clean process-resources` e abra (`target/classes/META-INF/archetype-post-generate.groovy`), ou `Ctrl+Shift+N` no IntelliJ.

* **PLUGIN** — [`groovy-maven-plugin`](https://groovy.github.io/gmaven/) 2.1.1
  * Fase: (`process-resources`) (imediatamente apos (`xml-maven-plugin:transform`) montar o metadata em (`generate-resources`), e antes do (`package`) e do (`integration-test`)).
  * Goal: (`execute`) — invoca o script Groovy declarado em (`<source>`).
  * Input : [`src/main/groovy/ArchetypePostGenerateComposer.groovy`](src/main/groovy/ArchetypePostGenerateComposer.groovy) (o compositor).
  * Output: (`${project.build.outputDirectory}/META-INF/archetype-post-generate.groovy`)
    (ou seja, (`target/classes/META-INF/...`), dentro do JAR do arquetipo).
  * O (`archetype-post-generate.groovy`) **nao existe** em (`src/main/resources/META-INF/`). O (`BuildArtifactsTest`) 1.4 falha se alguem recriar o monolito; assim o unico arquivo no classpath e o remontado.

* **COMPOSITOR** — [`ArchetypePostGenerateComposer.groovy`](src/main/groovy/ArchetypePostGenerateComposer.groovy)
  * Le os 15 arquivos de (`body/`) na ordem canonica da (`List<String> SECTIONS`); le os diretorios (`variables/`), (`generators/`) e (`snippets/`) em ordem do `NN` via (`listFiles().sort { it.name }`).
  * Aplica **consolidacao de imports** em 3 passos:
    1. Para cada arquivo concatenado, remove (`^import …`) e (`^//[[:space:]]*import`).
    2. Prepende o conteudo de (`archetype_post_generate_body/body-01-imports.groovy`) no topo, aplicando (`replaceAll('/', '')`) em cada linha.
    3. Concatena tudo e grava em (`target/classes/META-INF/archetype-post-generate.groovy`).
  * Sanity check: falha com (`IllegalStateException`) listando o arquivo faltando se qualquer arquivo obrigatorio de (`body/`) estiver ausente, ou se (`variables/`)/(`generators/`)/(`snippets/`) estiverem vazios.

* **LAYOUT FISICO**
  * [`archetype_post_generate_body/`](src/main/resources/META-INF/archetype_post_generate_body)
    => 15 arquivos canonicos (1 imports, 1 header, 1 logging, 1 global-vars, 5 path-vars, 1 binding-artifact-registry, 1 footer, 4 helpers).
  * [`archetype_post_generate_variables/`](src/main/resources/META-INF/archetype_post_generate_variables)
    => 25 (`variables-NN-*.groovy`) (1 por categoria de flag).
  * [`archetype_post_generate_generators/`](src/main/resources/META-INF/archetype_post_generate_generators)
    => 4 (`generator-NN-*.groovy`) (geram fontes Java: enums, tipos aninhados e contrato).
  * [`archetype_post_generate_snippets/`](src/main/resources/META-INF/archetype_post_generate_snippets)
    => 20 (`snippet-NN-*.groovy`) (1 por comportamento condicional).
  * [`ArchetypePostGenerateComposer.groovy`](src/main/groovy/ArchetypePostGenerateComposer.groovy) => o compositor Groovy de build-time.

* **FLUXO BUILD**
  * source 4 diretorios (`body/`)+(`variables/`)+(`generators/`)+(`snippets/`) + (`body-01-imports.groovy`)
    * => (`ArchetypePostGenerateComposer.groovy`) le na ordem da (`List<String> SECTIONS`), consolida imports
    * => (`target/classes/META-INF/archetype-post-generate.groovy`) (Groovy completo, sem duplicacao de imports)
    * => empacotado no JAR do arquetipo
    * => executado pelo (`maven-archetype-plugin`) 3.4.1 em (`mvn archetype:generate`), durante o pos-processamento do projeto gerado.

* **⚠️ ADVERTENCIAS** (particularidades deste mecanismo)
  * **1. Plugin estagnado**: o (`groovy-maven-plugin:2.1.1`) nao recebe release desde **2019-08-04** — risco latente se o (`groovy-bom`) do archetype-plugin migrar para Groovy 5/6.
  * **2. Override de Groovy 4.0.28 via (`<dependencies>`) nao pega**: o gmaven roda em classloader proprio com Groovy 2.4.9 default. O composer funciona com 2.4.9.















<a id="testabilidade" name="testabilidade"></a>
## Testabilidade 🧪 <a href="#indice">↑ topo</a>

<a id="testabilidade-onde-os-testes-vivem" name="testabilidade-onde-os-testes-vivem"></a>
### 1) ONDE OS TESTES VIVEM <a href="#indice">↑ topo</a>

  | Pasta | O que é | Executado por | Testa | Fase | Como rodar |
  |---|---|---|---|---|---|
  | `src/test/archetype-post-generate/` | suítes unitárias — scripts Groovy, um por assunto | **Groovy** | o pós-processador **e** o catálogo/templates do Maven | `test` | `mvn test` |
  | `src/test/groovy/` | o runner que executa as suítes | **Groovy** | — | `test` | `mvn test` (o `groovy-maven-plugin` dispara o runner) |
  | `src/test/resources/projects/` | fixtures de integração — **1 subpasta = 1 combinação de flags** | **Maven** | o plugin do Maven **e** o pós-processador (roda dentro do `generate`) | `integration-test` | `mvn clean integration-test` |
  | `archetype-resources/src/test/java/` | classes JUnit que **viajam no projeto gerado** | **Maven** (surefire) | o projeto gerado | `integration-test` (aninhado) | `mvn clean integration-test` (rodam dentro do `verify` de cada fixture) |

  > `mvn clean integration-test` já executa **as quatro linhas**: a fase `test` vem antes de `integration-test`, e o `verify` de cada fixture roda as classes JUnit do projeto gerado.
  > `mvn clean install` faz o mesmo e ainda instala o artefato em `~/.m2`.

<a id="testabilidade-o-que-cada-pasta-cobre" name="testabilidade-o-que-cada-pasta-cobre"></a>
### 2) O QUE CADA PASTA COBRE <a href="#indice">↑ topo</a>

  * **`src/test/archetype-post-generate/`** — é plana, sem subpastas; agrupada por assunto:
    * **Pós-processador** — helpers de arquivo/pasta, variáveis, geradores (enums, POJOs aninhados) e todos os snippets (JWT, binding, logback, docker, git, contrato, filetype).
    * **Lado Maven** — fragmentos do catálogo, catálogo montado, filesets × templates, wiring do build (3 slots, `llm-maven-flags-inference/` no JAR).
    * **Templates Velocity** — flags declaradas, guards `#if` vivos, `<include>`/imports resolvidos, paridade `.properties` × `.yml`.
    * **Procedimento e cobertura** — o `llm-maven-flags-inference/` empacotado e o gate que amarra suítes ↔ arquivos ↔ runner.
  * **`src/test/groovy/`** — **não cobre nada por si só.** 
    * Contém **apenas** o runner (`run-archetype-post-generate-tests.groovy`): é o *harness* que avalia cada suíte da 
     pasta acima no mesmo `GroovyShell` e converte um check falho em falha de build. 
    * Não tem asserção própria — a cobertura é a das suítes que ele executa. 
    * Sem ele, `mvn test` roda **zero** testes: as suítes são scripts Groovy, não `*Test.java`, então o `surefire` não as enxerga.
  * **`src/test/resources/projects/`** — uma subpasta por fixture; agrupadas por tema:
    * **Binding externalizado** — default / list / map / mutability / validation.
    * **Binding com testes no projeto gerado**.
    * **Base e defaults** — mínimo, básico, só defaults.
    * **Contrato e tipos do projeto** — imports FQN, enums, persistência do contrato.
    * **Tipos aninhados** — objetos aninhados e casos-limite.
    * **Logback** — console granular, sem ELK.
    * **Outros** — formato de config (yml), Java 21, tudo ligado, normalização do `id`.
  * **`archetype-resources/src/test/java/`** — **não cobre o archetype**: 
    * são as **14 classes JUnit fixas que viajam para dentro do projeto gerado**: 10 de binding (`ApplicationProperties*Test`, `ListProperties*Test`, `MapProperties*Test`) e 4 transversais de contrato (`ContractKeyDiscardTest`, `ContractKeyNormalizationTest`, `MalformedContractTest`, `TechnicalIdMappingTest`), além de 6 classes de apoio do contrato.
    * o `generator-04-contract_schema_tests` emite, em runtime, um teste por sujeito do contrato (campos simples, tipos aninhados e enums) quando o `contract.json` foi persistido.
    * Elas só sobrevivem lá quando `enable_folder_src_test_java=true` **e** `enable_binding_external_properties_tests=true` com pelo menos um sub-módulo (`default`/`list`/`map`) ligado; `mutability` e `validation` podam as variantes que não combinam. 
    * Como rodam dentro do `mvn verify` de cada fixture, uma falha nelas aparece como falha da fixture correspondente.

  > **Uma subpasta = um cenário = um vetor de flags.** A *assinatura* de cada fixture (as flags cujo valor **difere do default do catálogo**) fica declarada no cabeçalho `# FIXTURE SIGNATURE` do `archetype.properties` e é verificada por `FixturesMatrixTest.groovy`.
  > Matriz completa (cluster × flags sob teste × o que assere) → [src/test/resources/projects/README.md](src/test/resources/projects/README.md).

<a id="21-schema-test-entidade-contrato-gerado-na-aplicacao" name="21-schema-test-entidade-contrato-gerado-na-aplicacao"></a>
### 2.1) SCHEMA TEST ENTIDADE-CONTRATO (gerado na aplicacao) <a href="#indice">↑ topo</a>

  * **Premissa**: o schema test e um artefato DA APLICACAO GERADA. O archetype apenas o materializa/remove e verifica a entrega.
  * **Ativacao derivada** (sem flag nova): o pacote `src/test/java/<entity_package>/contract/` so existe quando `enable_folder_src_test_java=true` **e** `src/test/resources/contracts/contract.json` foi persistido. Caso contrario, `snippet-20-contract_schema_test.groovy` remove o pacote.
  * **Conteudo**: um arquivo de teste por sujeito - campos simples, cada tipo aninhado e cada enum - mais os arquivos das regras transversais (id tecnico, campos excedentes, round-trip, contrato salvo, contrato malformado, normalizacao de chaves, mapeamento de tipos e reconciliacao de aninhados/enums). Apoio: classes pequenas de leitura de campos, do contrato, de normalizacao de chaves e de navegacao.
  * **Execucao**: pelo ciclo de testes da propria aplicacao (`mvnw test`), para sempre. Cada divergencia produz uma falha propria, agrupada no arquivo de regra, em ordem deterministica, com atributo, tipo esperado/encontrado, direcao (contrato->entidade ou entidade->contrato) e caminho hierarquico.
  * **Regras espelhadas**: `llm-maven-flags-inference/03-regras-de-formacao-do-contrato/01..03` (formas, precedencia `attributes` > `fields` > topo, tipos/enums, aninhados ate 25 niveis) — e a regra do `id` em `01` §4.3, que espelha a descrição de `custom_entity_attributes_list` no catálogo (`id` → `<entidade>Id`, campo técnico, fora de `custom_entity_attributes_list`).
  * **JSON**: Spring Boot 4 usa Jackson 3 (`tools.jackson.databind`), disponivel via `spring-boot-starter-web`; nenhuma dependencia nova entra no `pom.xml` gerado.
  * **Prova no archetype (Frente 02)**: as fixtures `entity-contract`, `custom-nested-objects` e `custom-enums-from-payload` verificam a presenca do pacote, o relatorio surefire verde da aplicacao e a matriz de sabotagem da Frente 02.
  * **Sabotagem coberta**: contrato (chave removida, folha mais profunda renomeada, JSON corrompido), entidade (campo renomeado, campos clandestinos, tipo alterado), enums de topo e aninhado, e multiplas divergencias reportadas uma por falha, em ordem deterministica - mais a paridade Lombok on/off com o mesmo veredito. O contrato da fixture cobre 6 POJOs aninhados em cadeia (7 niveis), enums de topo/aninhado, tipo de projeto sobre array e dois campos do mesmo enum.

<a id="testabilidade-como-rodar-maven" name="testabilidade-como-rodar-maven"></a>
### 3) COMO RODAR (Maven) <a href="#indice">↑ topo</a>

  * **Todos os cenarios** (na raiz do projeto; **nao instala** o artefato em `~/.m2`):
    ```bash
    mvn clean integration-test
    ```
  * **Modo offline** (apos primeira execucao online):
    ```bash
    mvn -o integration-test
    ```
  * NAO use `mvn archetype:integration-test` — esse goal falha com *"Unable to get the archetypes' artifact which should have just been built: you probably launched 'mvn archetype:integration-test' instead of 'mvn integration-test'"*: fora do lifecycle nao existe `package`, logo `project.getArtifact().getFile()` e nulo e o plugin tenta resolver o archetype do repositorio (`~/.m2`).
  * O mesmo comando roda, **antes** das fixtures, as **suites unitarias** do pos-processador (a fase `test` vem antes de `integration-test` no lifecycle).
  * **CI (GitHub Actions) — `.github/workflows/ci.yml`:**
    * Roda em **dois comandos**, na ordem obrigatoria:
      ```bash
      mvn -B clean install "-Darchetype.test.skip=true"   # suites unitarias + instala o artefato
      mvn -B clean integration-test                       # fixtures contra o artefato instalado
      ```
    * A ordem importa: o `archetype:integration-test` resolve o archetype **do repositorio local** (`~/.m2`). Se o `install` rodar depois, as fixtures validam um artefato ausente ou obsoleto.
    * O `install` sozinho roda as suites unitarias; o `-Darchetype.test.skip=true` pula as fixtures para nao duplicar o trabalho.
  * **Knobs do `maven-archetype-plugin:3.4.1`** (user properties):

    | Property | Default efetivo | Para que serve |
    |---|---|---|
    | `archetype.test.skip` | `false` | pula as fixtures |
    | `archetype.test.projectsDirectory` | `target/test-classes/projects` | diretorio das fixtures; aponte para um diretorio contendo **uma so** para isolar |
    | `archetype.test.verifyScript` | `verify` | nome do hook pos-build (busca `.bsh` e `.groovy`) |
    | `archetype.test.localRepositoryPath` | `~/.m2/repository` | repositorio local usado nas invocacoes do projeto gerado |
    | `archetype.test.streamLogs` | `true` | espelha o log do build do projeto gerado no console |

    * **COMO RODAR (OpenCode)**
      * **Todos os cenarios:**
        ```text
           "Rode `mvn clean integration-test` e me diga quais fixtures passaram. Se alguma falhar, me mostre o trecho relevante do log."
        ```
      * **Em modo offline:**
        ```text
           "Rode `mvn -o integration-test` e me confirme que as fixtures passam sem rede."
        ```
      * **Sanity Check Completo:**
        O prompt canonico (passo a passo, formato das tabelas e criterios de falha) esta no fim desta secao.

<a id="testabilidade-verify-groovy-o-hook-pos-build" name="testabilidade-verify-groovy-o-hook-pos-build"></a>
### 4) verify.groovy — o hook pós-build 🔬 <a href="#indice">↑ topo</a>

  * Script Groovy executado **depois** do `mvn clean verify` da fixture. Atua como **assercao estrutural pos-build** sobre o projeto Spring Boot gerado (NAO e teste unitario) e e **auto-contido** — hardcoda a expectativa e nao depende do JSON copiado.
  * O `maven-archetype-plugin:3.4.1` dispara o hook pelo parametro `postBuildHookScript` (default **`verify`**, user property `archetype.test.verifyScript`): procura `verify.bsh` ou `verify.groovy` na raiz da fixture.
  * **Semantica de falha:** se o script lancar excecao **ou** retornar algo diferente de `null`/`true`, a fixture inteira e marcada como **falha**.
  * O plugin **gera** o projeto em `target/test-classes/projects/<fixture>/project/<artifactId>/` e e esse diretorio que chega ao hook como `basedir` (sem o binding, os scripts caem para `System.getProperty('basedir')`).

  > Detalhe do que cada `verify.groovy` assere → [specs/001-archetype-integration-tests/quickstart.md](specs/001-archetype-integration-tests/quickstart.md).

<a id="testabilidade-trabalhar-com-um-verify-groovy" name="testabilidade-trabalhar-com-um-verify-groovy"></a>
### 5) TRABALHAR COM UM `verify.groovy` <a href="#indice">↑ topo</a>

  * **Rodar uma fixture isolada (com verify automatico):**
    ```txt
    Isole a fixture `entity-contract` num diretorio temporario (copie `src/test/resources/projects/entity-contract` para `target/it-single/entity-contract`), rode `mvn clean integration-test -Darchetype.test.projectsDirectory=target/it-single` e me mostre a saida do `verify.groovy`.
    ```
    - O plugin 3.4.1 **nao** tem filtro por nome; `archetype.test.projectsDirectory` apontando para um diretorio com **uma so** fixture e o unico jeito de rodar apenas ela.
  * **Debug manual do verify (fora do Maven):**
    ```txt
    Debug: rode `groovy -Dbasedir=target/test-classes/projects/entity-contract/project/entity-contract src/test/resources/projects/entity-contract/verify.groovy` e me diga se passou.
    ```
    - `basedir` deve apontar para o **projeto gerado** (nao para `src/test/resources/...`); o script tambem aceita o binding `basedir` do invoker.
    - O `groovy` CLI nao e dependencia declarada do projeto — o Groovy vem do `groovy-maven-plugin`. Sem CLI instalado, rode via `mvn clean integration-test`.
  * **Criar verify para nova fixture:**
    ```txt
    Crie um `verify.groovy` para a fixture `minimal` que valide a entidade padrao: PK `private Long myEntityId;` (com `@Id`/`@GeneratedValue`), mais `private String username;` e `private String password;`, e nenhum import de Lombok.
    ```

<a id="testabilidade-sanity-check-completo-1-comando" name="testabilidade-sanity-check-completo-1-comando"></a>
### 6) SANITY CHECK COMPLETO (1 comando) <a href="#indice">↑ topo</a>

  * o prompt canonico (passo a passo, formato das tabelas e criterios de falha):
    ```txt
    Faca um sanity check completo do projeto archetype-v2-eda, na ordem:
    (1) Confirme os pre-requisitos: Java 17+ e Maven 3.9.x instalados. As fixtures pinam `java_version=17`; o default `java_version=21` do catalogo so vale para geracoes avulsas.
    (2) Rode `mvn clean integration-test` — um unico comando que executa, nesta ordem, as suites unitarias Groovy (fase `test`), as fixtures de `src/test/resources/projects/` e o `verify.groovy` das que o possuem. O `clean` ja recria `target/classes/META-INF/maven/archetype-metadata.xml`.
    (3) Me entregue um sumario final tabelado, com DOIS blocos:
    (3.1) UNITARIOS — todas as suites, todos os checks, quantas passaram e a lista de checks por suite;
    (3.2) INTEGRACAO — todas as fixtures, quantas passaram e qualquer `verify.groovy` que tenha falhado.
    (4) Quanto ao sumario final:
    (4.1) Qdo o item falhar, mostre o trecho do log e sugira a correcao.
    (4.2) Qdo o item passar, confirme com 'Tudo verde' e o tempo total gasto.
    (5) Formato obrigatorio do relatorio (100% em tabelas Markdown, nesta ordem):
    (5.1) Tabela de Setup [(1)-(2)]: colunas `Etapa | Comando / Verificacao | Resultado Obtido | Status | Tempo`.
    (5.2) Bloco (3.1) UNITARIOS: 
    - Tabela Resumo Geral: colunas `# | Suite | Total de Checks | Passaram | Falharam | Status` (com linha final de Total).
    - Uma tabela separada para CADA suite listando TODOS os checks individuais: colunas `ID | Check | Resultado`.         
    (5.3) Bloco (3.2) INTEGRACAO: tabela unica com todas as fixtures e linha final de Total: colunas `# | Fixture | Invoker Exit Code | Pos-Build verify.groovy | Verificacoes do verify.groovy | Status`.
    (5.4) Bloco (4) Sumario Final: tabela consolidada por bloco + linha `TOTAL GERAL`: colunas `Bloco | Escopo | Passaram | Falharam | Diagnostico / Correcao (4.1) | Confirmacao e Tempo (4.2)`.
    (5.5) Para testes que passaram use o emoji de 'checado' (verde); para testes que falharam use o emoji 'X' (vermelho).
    ```

<a id="testabilidade-prompts-para-homologacao-manual-geracao-via-opencode-04-cenarios" name="testabilidade-prompts-para-homologacao-manual-geracao-via-opencode-04-cenarios"></a>
### 7) PROMPTS PARA HOMOLOGAÇÃO MANUAL (Geração via OpenCode - 04 Cenários) 🧪 <a href="#indice">↑ topo</a>

  * Estes prompts **simulam o spec** que o operador entrega: 
    * use case + contrato JSON. 
    * Não fazem parte da suíte automatizada — eles exercitam o **agente**, não o build.
  * São portáveis: o procedimento vem do JAR, não do repositório do EDA.
  * O destino é sempre a `PASTA DE DESTINO`; a identidade do módulo não é informada no spec.
  * Servem para validar o **alinhamento da inferência**: 
    * se o agente derivar as flags erradas, 
    * o projeto gerado sai torto e você percebe antes de rodar `mvn archetype:generate`.

<a id="testabilidade-prompts-para-homologacao-manual-geracao-via-opencode-04-cenarios-cenario-a-contrato-plano-3-atributos" name="testabilidade-prompts-para-homologacao-manual-geracao-via-opencode-04-cenarios-cenario-a-contrato-plano-3-atributos"></a>
#### 7.1) Cenário A — contrato plano (3 atributos) <a href="#indice">↑ topo</a>

```text
# TAREFA: Gerar módulo com o Maven Archetype EDA

0. PROCEDIMENTO:
- Com o JAR com.eda-archetype:eda-archetype:0.0.1-SNAPSHOT instalado no
  repositório local, leia
  llm-maven-flags-inference/01-entrada-inicializacao/00-entry-point.md
  de dentro do JAR e siga-o antes de derivar.
- Este spec pode ser executado fora da pasta do repositório do archetype.

1. TIPO DE MÓDULO:
[X] RAIZ
[ ] SECUNDARIO

2. PACKAGE BASE (sem o nome da entidade):
br.com.loja

3. USE-CASE:
- Cadastro simplificado de produtos.
- O cadastro coleta nome do produto, preço e quantidade em estoque.
- O sistema expõe uma API REST na porta 8080.

4. CONTRATO JSON do módulo:
{
  "nome_produto": "Teclado Mecânico",
  "preco": 250.50,
  "quantidade_estoque": 15,
  "status": ["ATIVO", "CANCELADO"]
}

5. PASTA DE DESTINO:
C:\Temp\eda-005-destino-a

6. INSTRUÇÕES DE EXECUÇÃO (PARA O AGENTE):
- O destino de geração vem da marcação PASTA DE DESTINO acima; não presuma destino.
- No PowerShell, coloque cada -D entre aspas (a virgula da lista de
  atributos quebra o argumento).
- Depois de gerar, rode "mvn clean verify" na pasta do projeto gerado.
- Se o build passar, ABRA O PROJETO NO INTELLIJ automaticamente:
  "idea64 <pasta_do_projeto_gerado>".
- O projeto gerado TEM o proprio pom.xml; ele nao e um modulo do archetype.
- A API gerada deve ter amplo suporte a testabilidade.
```

  * **O que esperar:** 
    * entidade `Produto` no pacote `br.com.loja.produto`; 
    * `custom_entity_attributes_list=String:nome_produto,Double:preco,Integer:quantidade_estoque` (ordem do contrato, tipo estimado pelo valor);
    * `enable_custom_entity_attributes=true`; 
    * `entity_package_name=produto`; 
    * `api_port=8080`.

<a id="testabilidade-prompts-para-homologacao-manual-geracao-via-opencode-04-cenarios-cenario-b-array-enum-contrato-aninhado-filho-neto" name="testabilidade-prompts-para-homologacao-manual-geracao-via-opencode-04-cenarios-cenario-b-array-enum-contrato-aninhado-filho-neto"></a>
#### 7.2) Cenário B — array-enum + contrato aninhado (filho + neto) <a href="#indice">↑ topo</a>

```text
# TAREFA: Gerar módulo com o Maven Archetype EDA

0. PROCEDIMENTO:
- Com o JAR com.eda-archetype:eda-archetype:0.0.1-SNAPSHOT instalado no
  repositório local, leia
  llm-maven-flags-inference/01-entrada-inicializacao/00-entry-point.md
  de dentro do JAR e siga-o antes de derivar.
- Este spec pode ser executado fora da pasta do repositório do archetype.

1. TIPO DE MÓDULO:
[X] RAIZ
[ ] SECUNDARIO

2. PACKAGE BASE (sem o nome da entidade):
br.com.loja

3. USE-CASE:
- Cadastro de produtos com categoria e fabricante detalhados.
- O cadastro coleta nome do produto, preço, quantidade em estoque,
  os dados da categoria e os dados de origem do fabricante.
- O sistema expõe uma API REST na porta 8080.

4. CONTRATO JSON do módulo:
{
  "nome_produto": "Smartphone",
  "preco": 1500.0,
  "quantidade_estoque": 10,
  "status": ["ATIVO", "CANCELADO"],
  "detalhes_categoria": {
    "nome_categoria": "Eletrônicos",
    "codigo_setor": "E01",
    "fabricante_origem": {
      "nome_empresa": "Tech Corp",
      "cnpj_distribuidor": "12.345.678/0001-99",
      "tipo_origem": ["NACIONAL", "IMPORTADO"]
    }
  }
}

5. PASTA DE DESTINO:
C:\Temp\eda-005-destino-b

6. INSTRUÇÕES DE EXECUÇÃO (PARA O AGENTE):
- O destino de geração vem da marcação PASTA DE DESTINO acima; não presuma destino.
- No PowerShell, coloque cada -D entre aspas (a virgula da lista de
  atributos quebra o argumento).
- Depois de gerar, rode "mvn clean verify" na pasta do projeto gerado.
- Se o build passar, ABRA O PROJETO NO INTELLIJ automaticamente:
  "idea64 <pasta_do_projeto_gerado>".
- O projeto gerado TEM o proprio pom.xml; ele nao e um modulo do archetype.
- A API gerada deve ter amplo suporte a testabilidade.
```

  * **O que esperar:**
    * os mesmos 3 atributos do Cenário A **mais** o `status`, o objeto aninhado e o enum aninhado;
      * `custom_entity_attributes_list=String:nome_produto,Double:preco,Integer:quantidade_estoque,Status:status,DetalhesCategoria:detalhesCategoria`;
      * `contract_nested_type_definitions=DetalhesCategoria|nomeCategoria:String,codigoSetor:String,fabricanteOrigem:FabricanteOrigem;FabricanteOrigem|nomeEmpresa:String,cnpjDistribuidor:String,tipoOrigem:TipoOrigem`;
      * `contract_enum_definitions=Status:ATIVO|CANCELADO;nested.TipoOrigem:NACIONAL|IMPORTADO`.
    * Os POJOs nascem em `br.com.loja.produto.nested`, com campos em **camelCase** e o pai emitido antes do filho:
      * `DetalhesCategoria` (`nomeCategoria`, `codigoSetor`, `fabricanteOrigem`) e
      * `FabricanteOrigem` (`nomeEmpresa`, `cnpjDistribuidor`, `tipoOrigem`).
    * O enum aninhado `TipoOrigem` nasce em `br.com.loja.produto.nested` com `NACIONAL` e `IMPORTADO`; o enum de topo `Status` fica em `br.com.loja.produto` com `ATIVO` e `CANCELADO`.
    * Na entidade, o campo do objeto aninhado é referenciado por **FQN** e marcado com `@Transient`.
    * **Sinal de alerta:**
      * se o agente devolver `detalhes_categoria` como `String`, a profundidade não foi reconhecida;
      * se `tipo_origem` vier como `String` ou o agente não emitir `nested.TipoOrigem`, o enum aninhado não foi reconhecido;
      * o contrato tem 2 níveis (filho + neto) e a regra manda virar POJO.

<a id="testabilidade-prompts-para-homologacao-manual-geracao-via-opencode-04-cenarios-cenario-c-contrato-com-array-puro" name="testabilidade-prompts-para-homologacao-manual-geracao-via-opencode-04-cenarios-cenario-c-contrato-com-array-puro"></a>
#### 7.3) Cenário C — contrato com array puro <a href="#indice">↑ topo</a>

```text
# TAREFA: Gerar módulo com o Maven Archetype EDA

0. PROCEDIMENTO:
- Com o JAR com.eda-archetype:eda-archetype:0.0.1-SNAPSHOT instalado no
  repositório local, leia
  llm-maven-flags-inference/01-entrada-inicializacao/00-entry-point.md
  de dentro do JAR e siga-o antes de derivar.
- Este spec pode ser executado fora da pasta do repositório do archetype.

1. TIPO DE MÓDULO:
[X] RAIZ
[ ] SECUNDARIO

2. PACKAGE BASE (sem o nome da entidade):
br.com.loja

3. USE-CASE:
- Cadastro de produtos com status, etiquetas e variações.
- O cadastro coleta nome do produto, preço, quantidade em estoque,
  o status, as etiquetas e as variações disponíveis.
- O sistema expõe uma API REST na porta 8080.

4. CONTRATO JSON do módulo:
{
  "nome_produto": "Monitor 27\"",
  "preco": 1899.90,
  "quantidade_estoque": 12,
  "status": ["ATIVO", "CANCELADO"],
  "etiquetas": ["promocao", "novidade"],
  "variacoes": [
    { "cor": "Preto", "tamanho": "27" }
  ]
}

5. PASTA DE DESTINO:
C:\Temp\eda-005-destino-c

6. INSTRUÇÕES DE EXECUÇÃO (PARA O AGENTE):
- O destino de geração vem da marcação PASTA DE DESTINO acima; não presuma destino.
- No PowerShell, coloque cada -D entre aspas (a virgula da lista de
  atributos quebra o argumento).
- Depois de gerar, rode "mvn clean verify" na pasta do projeto gerado.
- Se o build passar, ABRA O PROJETO NO INTELLIJ automaticamente:
  "idea64 <pasta_do_projeto_gerado>".
- O projeto gerado TEM o proprio pom.xml; ele nao e um modulo do archetype.
- A API gerada deve ter amplo suporte a testabilidade.
```

  * **O que esperar:**
    * entidade `Produto` no pacote `br.com.loja.produto`;
    * `custom_entity_attributes_list=String:nome_produto,Double:preco,Integer:quantidade_estoque,Status:status,String:etiquetas,String:variacoes`;
    * `contract_enum_definitions=Status:ATIVO|CANCELADO` (array de MAIÚSCULAS vira enum no pacote da entidade);
    * `contract_nested_type_definitions` **não emitida** (default `none`);
    * `enable_custom_entity_attributes=true`, `entity_package_name=produto`, `api_port=8080`;
    * `etiquetas` (minúsculas) e `variacoes` (array de objetos) caem em `String` **com aviso**;
    * artefatos: `br.com.loja.produto.Status` apenas; **sem** pasta `nested/` e **sem** POJO para `variacoes`.
    * **Sinais de alerta:**
      * `status` como `String` → o array-enum não foi reconhecido;
      * `etiquetas`/`variacoes` virando enum, `List`/`Map` ou POJO → coleção inventada ou fallback ignorado;
      * aparecer `nested/` ou `contract_nested_type_definitions` → objeto fantasma.

<a id="testabilidade-prompts-para-homologacao-manual-geracao-via-opencode-04-cenarios-cenario-d-contrato-com-objeto-aninhado-filho-neto-bisneto" name="testabilidade-prompts-para-homologacao-manual-geracao-via-opencode-04-cenarios-cenario-d-contrato-com-objeto-aninhado-filho-neto-bisneto"></a>
#### 7.4) Cenário D — contrato com objeto aninhado puro (filho + neto + bisneto) <a href="#indice">↑ topo</a>

```text
# TAREFA: Gerar módulo com o Maven Archetype EDA

0. PROCEDIMENTO:
- Com o JAR com.eda-archetype:eda-archetype:0.0.1-SNAPSHOT instalado no
  repositório local, leia
  llm-maven-flags-inference/01-entrada-inicializacao/00-entry-point.md
  de dentro do JAR e siga-o antes de derivar.
- Este spec pode ser executado fora da pasta do repositório do archetype.

1. TIPO DE MÓDULO:
[X] RAIZ
[ ] SECUNDARIO

2. PACKAGE BASE (sem o nome da entidade):
br.com.loja

3. USE-CASE:
- Cadastro de produtos com dados de entrega completos.
- O cadastro coleta nome do produto, preço, quantidade em estoque,
  o endereço de entrega, a transportadora e o contato operacional.
- O sistema expõe uma API REST na porta 8080.

4. CONTRATO JSON do módulo:
{
  "nome_produto": "Mesa Escritório",
  "preco": 1250.0,
  "quantidade_estoque": 4,
  "dados_entrega": {
    "rua": "Rua das Acácias",
    "numero": 120,
    "transportadora": {
      "nome_fantasia": "Rápido Express",
      "contato_operacional": {
        "nome_responsavel": "João Souza",
        "telefone": "11999998888",
        "email": "joao@rapido.com"
      }
    }
  }
}

5. PASTA DE DESTINO:
C:\Temp\eda-005-destino-d

6. INSTRUÇÕES DE EXECUÇÃO (PARA O AGENTE):
- O destino de geração vem da marcação PASTA DE DESTINO acima; não presuma destino.
- No PowerShell, coloque cada -D entre aspas (a virgula da lista de
  atributos quebra o argumento).
- Depois de gerar, rode "mvn clean verify" na pasta do projeto gerado.
- Se o build passar, ABRA O PROJETO NO INTELLIJ automaticamente:
  "idea64 <pasta_do_projeto_gerado>".
- O projeto gerado TEM o proprio pom.xml; ele nao e um modulo do archetype.
- A API gerada deve ter amplo suporte a testabilidade.
```

  * **O que esperar:**
    * entidade `Produto` no pacote `br.com.loja.produto`;
    * `custom_entity_attributes_list=String:nome_produto,Double:preco,Integer:quantidade_estoque,DadosEntrega:dadosEntrega`;
    * `contract_nested_type_definitions=DadosEntrega|rua:String,numero:Integer,transportadora:Transportadora;Transportadora|nomeFantasia:String,contatoOperacional:ContatoOperacional;ContatoOperacional|nomeResponsavel:String,telefone:String,email:String`;
      * ordem obrigatória: **pai antes do filho** (DadosEntrega → Transportadora → ContatoOperacional);
    * `contract_enum_definitions` **não emitida** (`none`);
    * 3 POJOs em `br.com.loja.produto.nested`: `DadosEntrega`, `Transportadora`, `ContatoOperacional`, com campos em **camelCase**;
    * `custom_entity_attribute_imports` não emitida (a entidade usa o FQN inline);
    * na entidade: `private br.com.loja.produto.nested.DadosEntrega dadosEntrega;` marcado com `@Transient`;
    * nenhum enum gerado e nenhuma pasta fora de `nested/`.
    * **Sinais de alerta:**
      * qualquer nível virar `String` (ex.: `transportadora` como `String`) → cadeia de profundidade quebrada;
      * faltar `ContatoOperacional` ou ele nascer fora de `nested/` → bisneto/normalização de nível falhou;
      * ordem filho-antes-do-pai em `contract_nested_type_definitions` → regra de emissão violada;
      * `dados_entrega` virar `Map`/`List` → violação de "nunca invente coleção";
      * aparecer arquivo de enum sem array no contrato.
















<a id="flags-do-mvn-archetype-generate" name="flags-do-mvn-archetype-generate"></a>
## Flags do mvn archetype:generate 🤖 <a href="#indice">↑ topo</a>

* **NOTA**: as flags se dividem em *de pergunta* (aparecem no prompt interativo do archetype:generate) e *de
  resposta* (vão no archetype.properties do perfil de teste). A flag entity_package_name é **derivada** pelo
  agente e nunca chega ao usuário na command line gerada — mas, como toda `<requiredProperty>`, o
  `archetype:generate` **interativo** ainda a pergunta (com o default `myentity` pré-preenchido); só
  `--batch-mode` ou `-D` explícito a dispensam.

<a id="flags-do-mvn-archetype-generate-onde-cada-efeito-acontece" name="flags-do-mvn-archetype-generate-onde-cada-efeito-acontece"></a>
### Onde cada efeito acontece <a href="#indice">↑ topo</a>

| Camada | Onde age | Flags com efeito nesta camada |
|---|---|---|
| **Velocity** (`filtered="true"`) | conteúdo dos arquivos de `archetype-resources/` | `api_port`, `java_version`, `project_description`, `entity_name`, `entity_package_name`, `custom_entity_attributes_list`, `custom_entity_attribute_imports`, `contract_nested_type_definitions`, `enable_lombok_support`, `enable_jwt_authentication`, `enable_folder_src_test_java`, `enable_logback_support` + `enable_api_cpanel_elk_log_system`, `enable_binding_external_properties` + `enable_binding_external_properties_validation`, `git_repo_url`, `git_repo_branch` |
| **Pós-processador** (`archetype-post-generate.groovy`) | mantém / exclui / renomeia na árvore gerada | `enable_docker_support`, `enable_git_support`, `enable_logback_support`, `enable_api_cpanel_elk_log_system`, `enable_binding_external_properties*`, `config_file_format`, `enable_folder_src_test_java`, `enable_jwt_authentication`, `extra_pom_dependencies`, `entity_contract_file_path`, `contract_enum_definitions`, `contract_nested_type_definitions`, `show_archetype_post_generate_log` |
| **Catálogo** (`archetype-metadata.xml`) | resolve as flags `-D` (CLI > `defaultValue` > prompt) | todas as 35 |

> `enable_lombok_support` é consumida apenas pelo Velocity — não tem fragmento em `archetype_post_generate_variables/` nem snippet. O mesmo vale para `api_port`, `java_version`, `project_description`, `git_repo_url` e `git_repo_branch`; a flag Lombok é a que troca anotações em 6 templates.

<a id="flags-do-mvn-archetype-generate-property-built-in-fora-do-catalogo" name="flags-do-mvn-archetype-generate-property-built-in-fora-do-catalogo"></a>
### 0. Property built-in (fora do catálogo) <a href="#indice">↑ topo</a>

* **`package`**
  * Padrão: — (o `archetype:generate` exige o valor; não há `<requiredProperty>` correspondente)
  * Efeito: pacote-base de todos os pacotes Java gerados — o archetype **concatena** `entity_package_name` automaticamente (`<package>.<entity_package_name>`).
    * ✅ `-Dpackage=org.clinica` → `package org.clinica.cliente;`
    * ❌ `-Dpackage=org.clinica.cliente` → `package org.clinica.cliente.cliente;`
  * Sem dependência.

<a id="flags-do-mvn-archetype-generate-projeto" name="flags-do-mvn-archetype-generate-projeto"></a>
### 1. Projeto <a href="#indice">↑ topo</a>

<a id="flags-do-mvn-archetype-generate-projeto-metadados-do-projeto" name="flags-do-mvn-archetype-generate-projeto-metadados-do-projeto"></a>
#### 1.1 Metadados do projeto <a href="#indice">↑ topo</a>

* **`project_description`**
  * Padrão: `This is my Archetype`
  * Efeito: escreve a `<description>` do `pom.xml` e o texto do `README.md`.
  * Sem dependência.

* **`show_archetype_post_generate_log`**
  * Padrão: `false`
  * Efeito: quando `true`, imprime no console todo o relatório de deleção/renomeação acumulado no pós-processador.
  * Sem dependência.

<a id="flags-do-mvn-archetype-generate-projeto-ambiente-de-build" name="flags-do-mvn-archetype-generate-projeto-ambiente-de-build"></a>
#### 1.2 Ambiente de build <a href="#indice">↑ topo</a>

* **`java_version`**
  * Padrão: `21` (aceita `17` ou `21`)
  * Efeito: define `<java.version>` no `pom.xml`; o `maven.compiler.release` é derivado pelo parent do Spring Boot.
  * Sem dependência.

<a id="flags-do-mvn-archetype-generate-projeto-idioma-flags-reservadas" name="flags-do-mvn-archetype-generate-projeto-idioma-flags-reservadas"></a>
#### 1.3 Idioma — flags reservadas <a href="#indice">↑ topo</a>

* **`llm_prompt_language`**
  * Padrão: `pt`
  * Efeito: **reservada** — hoje nenhum componente lê a flag; o script externo recebe apenas a descrição e o contrato. Não altera arquivos nem o prompt.
  * Sem dependência.

* **`project_i18n_language`**
  * Padrão: `pt`
  * Efeito: **reservada para i18n futura** — hoje não há resources de i18n no template e a flag não altera o projeto gerado. O `MetadataContractTest` a mantém explicitamente como declarada e não consumida.
  * Sem dependência.

<a id="flags-do-mvn-archetype-generate-projeto-dependencias-externas" name="flags-do-mvn-archetype-generate-projeto-dependencias-externas"></a>
#### 1.4 Dependências externas <a href="#indice">↑ topo</a>

* **`extra_pom_dependencies`**
  * Padrão: `false`
  * Efeito: quando não é `false`, o pós-processador lê o `pom.xml` gerado linha a linha e insere cada `groupId:artifactId:version[:scope]` dentro da primeira tag `<dependencies>` encontrada. Sem `scope` → assume `compile`.
  * Sem dependência.

<a id="flags-do-mvn-archetype-generate-dominio" name="flags-do-mvn-archetype-generate-dominio"></a>
### 2. Domínio <a href="#indice">↑ topo</a>

* **`entity_name`**
  * Padrão: `MyEntity`
  * Efeito: nomeia as **classes** da entidade em `src/main/java/<package>/<entity_package_name>/` — `<entity_name>.java`, `<entity_name>Controller.java`, `<entity_name>Service.java` e `<entity_name>Repository.java`. A **pasta** é `entity_package_name`, **não** `entity_name`. Validação `^[A-Z][a-zA-Z0-9_]+$`.
  * Sem dependência.
  * *Derivação:* o agente (`llm-maven-flags-inference/03-regras-de-formacao-do-contrato/01` §2) resolve o nome
    **a partir do contrato** (campo `entityName`/`entity_name`/`entity`) e, na ausência dele,
    aceita a inferência do use-case — `entity_name` é a única flag da *allowlist* de
    inferência (`04` §6). O default do catálogo só entra como último recurso:
    quando o use-case **não trouxer nenhum nome** de entidade. Se trouxer mais de um
    candidato, o agente **para e pergunta** — nunca adota `MyEntity` em silêncio.

* **`entity_package_name`**
  * Padrão: `myentity` (literal do catálogo — o default nunca pode ser vazio)
  * Efeito: o archetype **usa o valor verbatim** como pacote (não há `${entity_name,,}` no
    template). Passar `-Dentity_package_name=<entity_name em minúsculas>` é o que faz o
    pacote acompanhar o nome da entidade.
  * *Derivação:* quando o contrato não traz `entity_package_name`, o agente o deriva do nome
    resolvido da entidade (em minúsculas) — fonte `CONTRACT_DERIVED` no relatório de
    proveniência (`01` §3).
  * **Depende de:** `entity_name`

* **`enable_custom_entity_attributes`**
  * Padrão: `false`
  * Efeito: habilita a inclusão de campos extras na entidade.
  * Sem dependência.
  * *Derivação:* o agente o **força** a `true` sempre que o contrato traz atributos — sem
    isso o template nem renderiza os campos (`01` §7).

* **`custom_entity_attributes_list`**
  * Padrão: `String:name`
  * Efeito: cada par `Tipo:nome` gera um campo na entidade.
  * O `id` e estrutural: o campo e `<entity_name em camelCase>Id` (ex.: `KitchenChef` -> `kitchenChefId`; `PET` -> `petId`). O `id` do contrato corresponde a esse campo técnico e **nao** entra na lista.
  * **Depende de:** `enable_custom_entity_attributes = true`
  * *Derivação:* o agente monta a lista **a partir do contrato**, no formato estrito
    `Tipo:nome,Tipo:nome` (sem espaços, na ordem do contrato). O **tipo é estimado pelo
    valor** do atributo (`Boolean`, `Integer`, `Long`, `Double`, `String`, e `null` → `String`);
    um `type`
    declarado no contrato tem precedência quando é um tipo de `java.lang` **ou um tipo do
    projeto** (fora de `java.lang`). Um tipo do projeto é referenciado pelo nome simples e
    **nomeia o atributo pelo próprio tipo** (`EnderecoCliente` → `enderecoCliente`). Um
    `type` fora dessas duas categorias (ex.: `java.time.Instant`, `List<String>`) é
    rejeitado com **aviso** e o valor volta a ser estimado pelo valor do atributo.
    Um **objeto JSON aninhado** não cai nessa estimativa: ele é derivado como POJO em
    `<package>.<entity_package_name>.nested` (ver `contract_nested_type_definitions`).

* **`custom_entity_attribute_imports`**
  * Padrão: `none`
  * Efeito: lista de FQNs dos **tipos do projeto** referenciados pela entidade — o template
    emite um `import` para cada um (o import do próprio pacote da entidade é omitido).
  * **Depende de:** `enable_custom_entity_attributes = true`
  * *Derivação:* preenchida pelo agente a partir do contrato: cada atributo com `type`
    fora de `java.lang` **e com pacote declarado** contribui com seu FQN.
  * Validação: `none` ou uma lista de FQNs separados por vírgula.

* **`contract_enum_definitions`**
  * Padrão: `none`
  * Efeito: os **enums** que o pós-processador gera no projeto gerado, no formato
    `Tipo:CONSTANTE|CONSTANTE` (vários enums separados por `;`).
  * **Depende de:** `enable_custom_entity_attributes = true`
  * *Derivação:* preenchida pelo agente a partir do contrato. Um enum é reconhecido
    quando: (a) um tipo do projeto tem **valor textual**; (b) um tipo do projeto tem
    `enumValues`/`values` explícitos; ou (c) um campo **sem tipo declarado** tem um
    array não vazio de strings **MAIÚSCULAS** — neste caso o nome do enum vem da chave
    do campo em PascalCase (`status` → `Status`). As constantes vêm de `enumValues`/`values`
    quando declaradas; caso contrário, do próprio valor observado (`"ATIVO"` → `ATIVO`).
    Um `Tipo` sem pacote é gerado no pacote da entidade; um `Tipo` com pacote é gerado no
    próprio pacote; o marcador relativo `nested.Tipo` gera o enum em `<entity_package>.nested`.
    Um arquivo de enum já existente **não é sobrescrito**.
  * Validação: `none` ou uma lista de definições no formato acima.

* **`contract_nested_type_definitions`**
  * Padrão: `none`
  * Efeito: os **POJOs** que o pós-processador gera para os objetos aninhados do contrato,
    no formato `Tipo|Campo:Tipo,Campo:Tipo` (vários separados por `;`), no pacote
    `<package>.<entity_package_name>.nested`.
  * **Depende de:** `enable_custom_entity_attributes = true`
  * *Derivação:* preenchida pelo agente a partir do contrato: cada objeto aninhado vira
    um POJO; a entidade o referencia pelo nome totalmente qualificado e marca o campo com
    `@Transient`. O **agente** só emite a definição de um tipo que aparece em `custom_entity_attributes_list` (`03` §1/§10) — o `generator-02` não faz essa checagem: o gate dele é apenas `contract_nested_type_definitions != none`.
  * Objeto vazio, nome inválido, nenhum campo válido ou profundidade acima de **25 gerações**
    → `String` com aviso. POJO já existente **não é sobrescrito**.
  * Validação: `none` ou uma lista de definições no formato acima.

* **`entity_contract_file_path`**
  * Padrão: `none`
  * Efeito: caminho do arquivo JSON do contrato do módulo — **absoluto** (é o que o agente
    emite, `05` §5) **ou relativo** (aceito pelo catálogo e resolvido pelo
    `generator-03` via `resolveExistingFileWalkingUp`). É preenchido pelo
    agente, que grava o contrato com nome determinístico (SHA-256 da forma canônica) em
    `%LOCALAPPDATA%\eda-archetype\contracts\<sha256>.json` (no POSIX: `$XDG_CACHE_HOME` ou
    `~/.cache`) e emite o caminho aqui (`05` §2-§5).
    Quando o arquivo existe **E** `enable_folder_src_test_java = true`, o pós-processador
    copia o contrato para `src/test/resources/contracts/contract.json` no projeto gerado —
    o contrato passa a ser a **base do módulo** e a referência dos testes de esquema futuros.
  * Validação: `none` ou um caminho (absoluto ou relativo) para arquivo `.json`.
  * **Depende de:** `enable_folder_src_test_java = true` (a pasta de contratos pertence à
    árvore de testes)

<a id="flags-do-mvn-archetype-generate-servidor-http" name="flags-do-mvn-archetype-generate-servidor-http"></a>
### 3. Servidor HTTP <a href="#indice">↑ topo</a>

* **`api_port`**
  * Padrão: `8080`
  * Efeito: define `server.port` no `application.properties`/`.yml`. Validação `^([1-9][0-9]{3,4})$`.
  * Sem dependência.

<a id="flags-do-mvn-archetype-generate-seguranca" name="flags-do-mvn-archetype-generate-seguranca"></a>
### 4. Segurança <a href="#indice">↑ topo</a>

* **`enable_jwt_authentication`**
  * Padrão: `false`
  * Efeito: quando `false`, o pós-processador **deleta** a pasta `authentication/` (dentro de `<Entity>/config/`) e os arquivos `security/appJwt.key` e `security/appJwt.pub` de `src/main/resources/security/`.
  * Efeito no `pom.xml`: quando `true`, o template **injeta** `spring-boot-starter-security`, `spring-security-test` (scope `test`) e `spring-boot-starter-oauth2-resource-server`.
  * Sem dependência.

<a id="flags-do-mvn-archetype-generate-containers" name="flags-do-mvn-archetype-generate-containers"></a>
### 5. Containers <a href="#indice">↑ topo</a>

* **`enable_docker_support`**
  * Padrão: `false`
  * Efeito: quando `false`, deleta a pasta `docker/` inteira — `docker-compose.yml`, `api_Dockerfile`, `api-compose.yml`, `db-mongo-compose.yml`, `nginx-compose.yml` e `elk-compose.yml`. Quando `true`, a pasta é mantida inteira.
  * Sem dependência.

<a id="flags-do-mvn-archetype-generate-repositorio-git" name="flags-do-mvn-archetype-generate-repositorio-git"></a>
### 6. Repositório Git <a href="#indice">↑ topo</a>

* **`enable_git_support`**
  * Padrão: `false`
  * Efeito: quando `true`, renomeia `git_temp/` → `.git/` e `gitignore` → `.gitignore`. O `git_temp/config` já vem com o remote `origin` e o branch preenchidos por Velocity (ver `git_repo_url` e `git_repo_branch`). Quando `false`, deleta ambos.
  * Sem dependência.

* **`git_repo_url`**
  * Padrão: `https://github.com/your-git-user/your-repo.git`
  * Efeito: **renderizado pelo Velocity** em dois lugares — `url = <url>` na seção `[remote "origin"]` do `.git/config` gerado e o link `Git Repo:` do `README.md` gerado. **Nenhum comando `git` é executado** pelo archetype.
  * **Depende de:** `enable_git_support = true`

* **`git_repo_branch`**
  * Padrão: `master`
  * Efeito: **renderizado pelo Velocity** no `.git/config` gerado — `[branch "<branch>"]` e `merge = refs/heads/<branch>`. **Nenhum comando `git` é executado** pelo archetype.
  * **Depende de:** `enable_git_support = true`

<a id="flags-do-mvn-archetype-generate-produtividade" name="flags-do-mvn-archetype-generate-produtividade"></a>
### 7. Produtividade <a href="#indice">↑ topo</a>

* **`enable_lombok_support`**
  * Padrão: `false`
  * Efeito: injeta `org.projectlombok:lombok` no `pom.xml` e exclui o Lombok do jar executável no `spring-boot-maven-plugin`. Não há `annotationProcessorPaths` explícito.
  * É consumida apenas pelo Velocity (não tem fragmento em `archetype_post_generate_variables/` nem snippet): 6 templates do `archetype-resources/` trocam anotações Lombok por getters/setters explícitos — a entidade, `<Entity>UserDetails`, `AdminCredentials`, `AuthenticationCredentials`, o `pom.xml` e o `README.md` gerado. `api_port`, `java_version`, `project_description`, `git_repo_url` e `git_repo_branch` também são consumidas só pelo Velocity, mas não trocam anotações.
  * Sem dependência.

<a id="flags-do-mvn-archetype-generate-observabilidade" name="flags-do-mvn-archetype-generate-observabilidade"></a>
### 8. Observabilidade <a href="#indice">↑ topo</a>

* **`enable_logback_support`**
  * Padrão: `false`
  * Efeito: quando `false`, deleta `logback-spring.xml`, `logback-test.xml`, a pasta `logback-spring-config/`, a pasta `ELK/` e `docker/elk-compose.yml`.
  * Efeito (segundo ramo, mesmo `snippet-17`): o conjunto acima **também** é removido quando `logback_console_type=1` **E** `enable_logback_appender_smtp=false` **E** `enable_logback_appender_file=false` **E** `enable_api_cpanel_elk_log_system=false` — Logback ligado, porém sem console custom e sem appender ⇒ a configuração inteira é apagada.
  * Sem dependência.

* **`logback_console_type`**
  * Padrão: `1`
  * Efeito: `1` = deleta os 3 consoles custom (`appender-console-tabular.xml`, `appender-console-granular.xml`, `appender-console-layouts-granular.xml`) **e** o `logback-test.xml` (fica o console default do Spring); `2` = mantém o console **tabular** (deleta o granular e o layout granular) **e** mantém o `logback-test.xml`; `3` = mantém o console **granular** (colorido, deleta o tabular) **e** mantém o `logback-test.xml`.
  * **Depende de:** `enable_logback_support = true`

* **`enable_logback_appender_smtp`**
  * Padrão: `false`
  * Efeito: quando `false`, deleta `appender-smtp-email.xml` de `logback-spring-config/`.
  * **Depende de:** `enable_logback_support = true`

* **`enable_logback_appender_file`**
  * Padrão: `false`
  * Efeito: quando `false`, deleta `appender-rolfile-file.xml` de `logback-spring-config/`.
  * **Depende de:** `enable_logback_support = true`

* **`enable_api_cpanel_elk_log_system`**
  * Padrão: `false`
  * Efeito: quando `true`, mantém `appender-tcp-elk-logstash.xml`, a pasta `ELK/` e `docker/elk-compose.yml`. Quando `false`, os três são removidos — a remoção por esta flag só ocorre com `enable_logback_support=true`; com Logback off quem remove é o `snippet-17`.
  * Efeito no `pom.xml`: quando `true` (e `enable_logback_support=true`), o template **injeta** `logstash-logback-encoder:8.0` e `janino:3.1.12`.
  * **Depende de:** `enable_logback_support = true`

<a id="flags-do-mvn-archetype-generate-externalizacao-de-propriedades" name="flags-do-mvn-archetype-generate-externalizacao-de-propriedades"></a>
### 9. Externalização de Propriedades <a href="#indice">↑ topo</a>

<a id="flags-do-mvn-archetype-generate-externalizacao-de-propriedades-formato-do-arquivo" name="flags-do-mvn-archetype-generate-externalizacao-de-propriedades-formato-do-arquivo"></a>
#### 9.1 Formato do arquivo <a href="#indice">↑ topo</a>

* **`config_file_format`**
  * Padrão: `1`
  * Efeito: `1` = deleta todos os `.yml` de `src/main/resources/` e `src/test/resources/` recursivamente; `2` = deleta todos os `.properties`. Apenas um formato sobrevive.
  * Sem dependência.

<a id="flags-do-mvn-archetype-generate-externalizacao-de-propriedades-sistema-de-binding" name="flags-do-mvn-archetype-generate-externalizacao-de-propriedades-sistema-de-binding"></a>
#### 9.2 Sistema de binding <a href="#indice">↑ topo</a>

* **`enable_binding_external_properties`**
  * Padrão: `false`
  * Efeito: liga o sistema inteiro. Quando `false` (ou quando nenhum sub-módulo está ligado), deleta `config/properties/`, `list_properties/`, `map_properties/` e os arquivos `application_list.*` / `application_map.*`.
  * Sem dependência.

* **`enable_default_binding_external_properties`**
  * Padrão: `false`
  * Efeito: mantém `ApplicationProperties.java` e `ApplicationPropertiesRecord.java` na pasta `config/properties/`. Quando `false`, ambos são deletados.
  * **Depende de:** `enable_binding_external_properties = true`

* **`enable_list_binding_external_properties`**
  * Padrão: `false`
  * Efeito: mantém a pasta `config/properties/list_properties/` com `ListObjects.java`, `ListProperties.java`, `ListPropertiesImmutable.java`, `ListPropertiesRecord.java` e os resources `application_list.*`.
  * **Depende de:** `enable_binding_external_properties = true`

* **`enable_map_binding_external_properties`**
  * Padrão: `false`
  * Efeito: mantém a pasta `config/properties/map_properties/` com `MapItem.java`, `MapProperties.java`, `MapPropertiesImmutable.java`, `MapPropertiesRecord.java` e os resources `application_map.*`.
  * **Depende de:** `enable_binding_external_properties = true`

* **`binding_external_properties_mutability`**
  * Padrão: `none`
  * Efeito: `immutable` = mantém apenas as variantes imutáveis (record/campos `final` + constructor binding); `mutable` = mantém apenas as variantes mutáveis (bean com setters); `none` = mantém todas as variantes. Age sobre as classes `.java` do main e do test, deletando as que não combinam com a escolha.
  * **Depende de:** `enable_binding_external_properties = true` **E** pelo menos um dos três sub-módulos ligado

* **`enable_binding_external_properties_validation`**
  * Padrão: `false`
  * Efeito: quando `false`, deleta `ApplicationPropertiesValidationTest.java` e `ApplicationPropertiesRecordValidationTest.java` do projeto de testes.
  * Efeito no `pom.xml`: quando `true` (com `enable_binding_external_properties=true`), o template **injeta** `spring-boot-starter-validation`.
  * **Depende de:** `enable_default_binding_external_properties = true`

<a id="flags-do-mvn-archetype-generate-externalizacao-de-propriedades-testes-do-binding" name="flags-do-mvn-archetype-generate-externalizacao-de-propriedades-testes-do-binding"></a>
#### 9.3 Testes do binding <a href="#indice">↑ topo</a>

* **`enable_binding_external_properties_tests`**
  * Padrão: `false`
  * Efeito: quando `false`, deleta `src/test/java/<Entity>/config/properties/` e `src/test/resources/properties/` inteiros.
  * **Depende de:** `enable_folder_src_test_java = true` **E** pelo menos um sub-módulo de binding ligado

<a id="flags-do-mvn-archetype-generate-estrutura-de-testes" name="flags-do-mvn-archetype-generate-estrutura-de-testes"></a>
### 10. Estrutura de testes <a href="#indice">↑ topo</a>

* **`enable_folder_src_test_java`**
  * Padrão: `false`
  * Efeito: quando `false`, deleta `src/test/java/` e `src/test/resources/` inteiros.
    Quando `true`, o `pom.xml` gerado recebe `h2` (scope `runtime`) e
    `spring-boot-starter-test` (scope `test`).
    Sem dependência.
  * **Consequência:** a pasta `src/test/resources/contracts/` — onde o contrato do
    módulo é persistido — **só existe quando esta flag é `true`**. Com a flag `false` não há
    pasta de contratos e o contrato não é persistido.

<a id="flags-do-mvn-archetype-generate-grafo-de-dependencias" name="flags-do-mvn-archetype-generate-grafo-de-dependencias"></a>
### Grafo de dependências <a href="#indice">↑ topo</a>

```text
enable_folder_src_test_java = true
  └─→ enable_binding_external_properties_tests

enable_binding_external_properties = true
  ├─→ enable_default_binding_external_properties = true
  │     └─→ enable_binding_external_properties_validation = true
  ├─→ enable_list_binding_external_properties = true
  ├─→ enable_map_binding_external_properties = true
  └─→ binding_external_properties_mutability (none | immutable | mutable)

enable_logback_support = true
  ├─→ logback_console_type
  ├─→ enable_logback_appender_smtp
  ├─→ enable_logback_appender_file
  └─→ enable_api_cpanel_elk_log_system

enable_git_support = true
  ├─→ git_repo_url
  └─→ git_repo_branch

enable_custom_entity_attributes = true
  ├─→ custom_entity_attributes_list
  ├─→ custom_entity_attribute_imports
  ├─→ contract_enum_definitions
  └─→ contract_nested_type_definitions

entity_name
  └─→ entity_package_name (derivada pelo agente a partir de entity_name)

enable_folder_src_test_java = true
  └─→ entity_contract_file_path (contrato copiado para src/test/resources/contracts/)
```
















<a id="funcionalidades" name="funcionalidades"></a>
## Funcionalidades 🛠️ <a href="#indice">↑ topo</a>

  <a id="funcionalidades-contratos-a-fonte-da-verdade-do-modulo-da-api" name="funcionalidades-contratos-a-fonte-da-verdade-do-modulo-da-api"></a>

### Contratos📜: a 'Fonte da Verdade' do Módulo da API <a href="#indice">↑ topo</a>

  * **1. Raciocínio central**:
    * o contrato nasce do desenho da tela (Design de Front-End) e é a fonte da verdade do módulo da API que alimenta essa tela.
    * a entidade é a classe que representa esse módulo: a expressão programática do contrato.
    * o ciclo é: desenho da tela → contrato → módulo da API → tela.
    * é um ciclo fechado: cada etapa depende da anterior.
    * se a entidade se separar do contrato, o ciclo se rompe.
    * a tela passa a receber dado errado, incompleto ou nenhum dado.
    * por isso a conferência protege o ciclo, e não apenas confere campos.

  ```text
        DESENHO DA TELA 
 (High/Low Definition Prototype)
               │
               │ 1. o Prototipo diz o que a tela/Front-End precisa
               │    um contrato JSON sera a fonte de dados da tela
               ▼
           EXTRACAO 
           CONTRATO   ◄── Contrato extraido a partir da HD/LD prototype
               │          O Contrato e a 'fonte da verdade'(provedor) do módulo
               │
               │ 2. o contrato diz o que o módulo entrega/serve/prove ao Front-End
               ▼
         MÓDULO DA API (Provedor de Dados ao Front-End)
               │
               │ 3. o módulo entrega os dados, obedecendo ao contrato,
               │    Servindo/Provendo ao Front-End(Client) os dados
               ▼
           FRONT-END
            (TELA)
               │
               │ 4. a tela cumpre sua funcao ao USER
               │    consumindo os dados providos pela API(Contrato)
               └──────────────► volta ao desenho: o ciclo recomeça
  ```

  * **2. O que faz**:
    * quando há contrato salvo e a pasta de testes habilitada, a aplicação nasce com um teste automático.
    * a cada execução, ele confere se a entidade continua fiel ao contrato.
    * se alguém mudar um dos dois, o teste falha.
    * a falha aponta o campo, o motivo e o sentido da diferença.

  * **3. Como funciona**:
    * o teste lê o contrato salvo no próprio projeto.
    * depois compara com a entidade, campo por campo.
    * vale nos dois sentidos: nada do contrato pode faltar; nada da entidade pode sobrar.
    * objetos dentro de objetos e listas de valores fixos também entram na conferência.
    * havendo vários problemas, todos aparecem juntos, em ordem previsível.

  * **4. Para quem serve**:
    * para quem usa o módulo gerado: entidade e contrato não se desencontram com o tempo.
    * para quem mantém o archetype: todo módulo novo já sai com a conferência funcionando.
    * para o front-end: os dados que chegam continuam sendo os combinados no desenho.

  * **5. Quando ele existe**:
    * apenas quando a pasta de testes está habilitada e o contrato foi salvo.
    * sem contrato, o teste não é criado.
    * nesse caso, o projeto segue compilando e passando normalmente.

  * **6. Detalhes técnicos**:
    * onde o teste vive, o que cobre e como a entrega é provada: <a href="#21-schema-test-entidade-contrato-gerado-na-aplicacao">Testabilidade → 2.1) SCHEMA TEST ENTIDADE-CONTRATO</a>.