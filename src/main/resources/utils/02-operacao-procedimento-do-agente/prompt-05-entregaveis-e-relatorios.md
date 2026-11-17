# Entregáveis e relatórios

## 1. Classificação do módulo

Informe se o módulo é **central** ou **secundário**.

A classificação é inferida; o JAR não define esses termos.

### Central

- Concentra a entidade principal do domínio.
- Outros módulos dependem dele.
- Expõe ou publica eventos que outros consomem.

### Secundário

- Apoia o domínio.
- Depende de outros módulos.
- Não é referência para eles.

Justifique em 2 ou 3 linhas, com base no use-case.

## 2. Relatório de tokens

Tabela:

| Etapa | Tokens de entrada | Tokens de saída | Total |
|---|---:|---:|---:|
| Leitura do JAR | | | |
| Derivação | | | |
| Execução | | | |
| Verificação | | | |
| **Total geral** | | | |

Se não for possível medir com precisão, marque como **estimativa**.

## 3. Economia

- Liste os arquivos gerados.
- Estime os tokens que o modelo gastaria escrevendo esse boilerplate à mão.
- Mostre a economia em valor absoluto.
- Mostre a economia em percentual.

## 4. Resumo final

Inclua:

- status dos Gates A, B e C da premissa de entrada;
- command line executada;
- resultado da validação do `package`;
- resultado da cópia do contrato;
- resultado do `mvn clean compile`;
- classificação do módulo;
- proveniência resumida;
- avisos e pendências.
