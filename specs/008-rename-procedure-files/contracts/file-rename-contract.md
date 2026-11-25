# Contract: Renomeação de Arquivos das Subpastas 01 e 02

**Feature**: `008-rename-procedure-files` | **Date**: 2026-11-25 | **Spec**: [../spec.md](../spec.md)

## Escopo

- **Dentro**: sete renomes de arquivos, dois na subpasta 01 e cinco na subpasta 02, sob `src/main/resources/llm-maven-flags-inference/`.
- **Fora**: conteúdo dos arquivos nas Fases 1 e 2; README; índice como conteúdo; fixtures; suítes; redirect em `src/main/resources/utils/`; aliases; cópias; renomear arquivos das subpastas 03 e 04; JAR instalado.
- **Fase 3**: as referências são tratadas no contrato [reference-update-contract.md](./reference-update-contract.md).

## Mapa canônico dos renomes

Caminhos relativos à raiz do repositório.

| Fase | Origem | Destino |
|---|---|---|
| 1 | `src/main/resources/llm-maven-flags-inference/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md` | `src/main/resources/llm-maven-flags-inference/01-entrada-inicializacao/01-entryPoint.md` |
| 1 | `src/main/resources/llm-maven-flags-inference/01-entrada-inicializacao/indice-01-mapa-dos-arquivos-e-ordem-de-leitura.md` | `src/main/resources/llm-maven-flags-inference/01-entrada-inicializacao/02-mapa-dos-arquivos-e-ordem-de-leitura.md` |
| 2 | `src/main/resources/llm-maven-flags-inference/02-operacao-procedimento-do-agente/prompt-01-papel-e-restricoes-do-agente.md` | `src/main/resources/llm-maven-flags-inference/02-operacao-procedimento-do-agente/01-papel-e-restricoes-do-agente.md` |
| 2 | `src/main/resources/llm-maven-flags-inference/02-operacao-procedimento-do-agente/prompt-02-como-localizar-e-ler-o-archetype.md` | `src/main/resources/llm-maven-flags-inference/02-operacao-procedimento-do-agente/02-como-localizar-e-ler-o-archetype.md` |
| 2 | `src/main/resources/llm-maven-flags-inference/02-operacao-procedimento-do-agente/prompt-03-regras-obrigatorias-de-geracao.md` | `src/main/resources/llm-maven-flags-inference/02-operacao-procedimento-do-agente/03-regras-obrigatorias-de-geracao.md` |
| 2 | `src/main/resources/llm-maven-flags-inference/02-operacao-procedimento-do-agente/prompt-04-fluxo-de-execucao-e-verificacao.md` | `src/main/resources/llm-maven-flags-inference/02-operacao-procedimento-do-agente/04-fluxo-de-execucao-e-verificacao.md` |
| 2 | `src/main/resources/llm-maven-flags-inference/02-operacao-procedimento-do-agente/prompt-05-entregaveis-e-relatorios.md` | `src/main/resources/llm-maven-flags-inference/02-operacao-procedimento-do-agente/05-entregaveis-e-relatorios.md` |

## Invariantes

- **C-001**: O procedimento mantém 19 arquivos e 4 subpastas.
- **C-002**: Cada rename mantém o arquivo na mesma subpasta.
- **C-003**: O conteúdo dos 7 arquivos permanece byte a byte idêntico nas Fases 1 e 2.
- **C-004**: Nenhum nome de origem sobrevive como arquivo, alias, cópia ou redirect dentro de `llm-maven-flags-inference/`.
- **C-005**: Após a Fase 1, a subpasta 01 tem exatamente `01-entryPoint.md` e `02-mapa-dos-arquivos-e-ordem-de-leitura.md`.
- **C-006**: Após a Fase 2, a subpasta 02 tem exatamente cinco arquivos, nenhum com prefixo `prompt-`, numerados de 01 a 05.
- **C-007**: As subpastas 03 e 04 não são renomeadas; nenhum arquivo delas é adicionado ou removido.
- **C-008**: README, fixture, suítes e `utils/` não são editados nas Fases 1 e 2.
- **C-009**: Nenhuma dependência, plugin ou arquivo em `src/main/java` é criado ou alterado.
- **C-010**: O nome `01-entryPoint.md` é literal, com `E` maiúsculo.

## Pré-condições por fase

### Fase 1

1. Os dois arquivos de origem existem na subpasta 01.
2. Os dois destinos não existem.
3. O manifesto SHA-256 dos dois arquivos e o snapshot das subpastas 02, 03 e 04 foram capturados fora do repositório.

### Fase 2

1. Os cinco arquivos de origem existem na subpasta 02.
2. Os cinco destinos não existem.
3. O manifesto SHA-256 dos cinco arquivos e o snapshot das subpastas 01, 03 e 04 foram capturados fora do repositório.

## Pós-condições por fase

### Fase 1

- C-002, C-003, C-005 satisfeitos.
- C-007 e C-008 satisfeitos.
- As suítes que leem os caminhos antigos da subpasta 01 ficam vermelhas, conforme o research.

### Fase 2

- C-002, C-003, C-006 satisfeitos.
- C-007 e C-008 satisfeitos.
- As seis suítes ficam vermelhas, conforme o research.

## Evidências exigidas

1. Listagem nominal das subpastas 01 e 02 após cada fase.
2. Comparação de SHA-256 por arquivo renomeado: 0 divergências.
3. Comparação do snapshot das subpastas de controle: 0 arquivos adicionados, removidos ou alterados.
4. Contagem total do procedimento: 19 arquivos.
5. Registro nominal das suítes que falham por citar os nomes antigos.

## Critérios de rejeição

- Destino já existente antes do rename.
- Arquivo de origem ausente.
- Hash divergente depois do rename.
- Qualquer arquivo das subpastas de controle alterado.
- Qualquer edição em README, suítes, fixtures, índice (conteúdo) ou redirect nas Fases 1 e 2.
- Criação de alias, cópia ou redirect com nome antigo.
- Contagem diferente de 19 no procedimento.
- Renomear arquivo das subpastas 03 ou 04.
