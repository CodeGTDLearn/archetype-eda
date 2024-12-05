<!--
Sync Impact Report
Version change: (none) → 1.0.0 (initial ratification — MAJOR)
Modified principles: N/A (initial version)
Added sections:
  - Preamble
  - Core Principles (I–VI)
  - Technology Stack & Constraints
  - Governance
Removed sections: N/A
Follow-up TODOs:
  - TODO(PROCESSO_DE_APROVACAO_DE_EMENDAS): nenhum processo formal de múltiplos aprovadores
    foi encontrado neste repositório (sem CODEOWNERS, sem template de PR, sem branch protection
    visível no repositório). Assumido mantenedor único até confirmação explícita.
-->

## Constitution do EDA Archetype

Este projeto é um Maven Archetype que gera projetos Spring Boot através de uma camada de pré-processamento em bash, uma camada de processamento via metadata XML e Velocity, e uma camada de pós-processamento em Groovy. Os princípios abaixo são derivados dos padrões que o código já impõe na prática. Eles são vinculantes para qualquer mudança no scaffolding do archetype, no seu pré-processador bash, no seu pós-processador Groovy e nos templates Java/config que ele gera.

## Core Principles

### I. Modularidade por Chave de Ativação (NON-NEGOTIABLE)

Toda capacidade opcional deve ser controlada por uma flag, respeitada de forma consistente na camada de input, na camada de metadata e template, e na camada de pós-geração. Nada que dependa de uma escolha do usuário pode ser adicionado de forma incondicional.

**Racional**: o propósito do archetype é gerar variações a partir de um único template; qualquer adição incondicional reintroduz silenciosamente uma funcionalidade que o usuário desativou.

### II. Convenção Fixa de Nomenclatura para Artefatos de Extensão (NON-NEGOTIABLE)

Arquivos que estendem o archetype devem seguir o prefixo e sufixo fixo da sua categoria.

**Racional**: o mecanismo de montagem seleciona arquivos pelo padrão de nome; um arquivo mal nomeado é descartado silenciosamente, produzindo um defeito invisível que ninguém percebe até o archetype gerar algo incompleto.

### III. Pacote por Funcionalidade no Código Gerado

O código Java gerado deve ser organizado por funcionalidade de domínio, nunca por camada técnica.

**Racional**: escolha deliberada por coesão alta, baixo acoplamento e facilidade de extração futura para microsserviços.

### IV. Binding Duplo de Configuração Externalizada

Toda propriedade de configuração externalizada deve ser oferecida em uma variante imutável e uma variante mutável, cada uma testada de forma independente.

**Racional**: esse é o padrão já estabelecido e repetido do projeto para configuração type-safe, não é duplicação incidental.

### V. Credenciais Nunca Entram no Repositório

Nenhuma credencial ou segredo pode ser commitado. Valores sensíveis devem vir de fora do repositório através de variáveis de ambiente, com uma regra de exclusão obrigatória.

**Racional**: decisão de segurança deliberada e avaliada, em que a alternativa de usar ferramentas de scanning automático foi considerada e rejeitada como insuficiente.

### VI. Validar Antes de Culpar o Código

Antes de concluir que uma mudança no scaffolding do archetype "não teve efeito", uma reinstalação limpa e completa deve ser confirmada primeiro.

**Racional**: um modo de falha recorrente e já conhecido neste projeto é um archetype em cache desatualizado, não o código que foi editado; seguir essa ordem de investigação evita depuração desperdiçada.

## Technology Stack & Constraints

O projeto usa Java 17, Maven 3.9.16, Spring Boot 4.0.0 nos projetos gerados, Logback com stack ELK opcional, Spring Security com OAuth2 Resource Server para autenticação JWT, Jakarta Validation, e JUnit 5 como framework de testes. Alterar qualquer um desses itens exige uma emenda formal a esta constitution, não uma edição pontual e informal.

## Governance

Esta constitution substitui qualquer convenção informal que exista hoje no projeto.

Emendas exigem incremento de versão seguindo versionamento semântico:
- Mudança de versão principal significa remoção ou redefinição de um princípio.
- Mudança de versão menor significa um princípio novo ou expandido.
- Mudança de versão de correção significa apenas ajuste de redação.

Toda emenda deve ser registrada em um relatório de impacto de sincronização no topo do próprio arquivo.

Compliance review: qualquer mudança em bash_script/, em META-INF/maven/archetype-metadata.xml, em META-INF/archetype-post-generate.groovy, ou em archetype-resources/ deve ser verificada contra os Princípios I a VI antes de ser aceita.

Tratamento de conflito futuro: se uma feature trabalhada através dos comandos de especificação, planejamento ou análise revelar um conflito com um princípio existente, ou revelar uma lacuna que esta constitution não cobre, o agente não deve alterar este documento automaticamente. Ele deve sinalizar o conflito encontrado e propor duas alternativas: ajustar o escopo da feature para respeitar a constitution como ela está escrita hoje, ou rodar o comando de constitution novamente para emendá-la de forma deliberada, aguardando aprovação explícita antes de prosseguir com qualquer implementação.

TODO(PROCESSO_DE_APROVACAO_DE_EMENDAS): não foi encontrado neste repositório nenhum processo formal de aprovação de emendas por múltiplas pessoas, então assume-se, até que isso seja definido, que o mantenedor único do projeto é quem aprova.

**Version**: 1.0.0 | **Ratified**: 2026-09-23 | **Last Amended**: 2026-09-23
