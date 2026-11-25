# Como relatar proveniência

Esta regra define a tabela de proveniência mostrada antes da execução.

## 1. Quando mostrar

Mostre a tabela:

- antes de executar o `mvn archetype:generate`;
- novamente no relatório final, de forma resumida.

A tabela não substitui a command line. As duas são obrigatórias.

## 2. Quais flags listar

Liste:

1. todas as flags que entram na command line;
2. todas as flags derivadas do contrato;
3. todas as flags rejeitadas pela regex.

Não é necessário listar todas as 35 flags do catálogo. Flags que ficaram no default e não afetam a geração podem ficar de fora da tabela principal.

A pasta de destino da geração não é flag. Ela não aparece nesta tabela e não recebe fonte de proveniência. É relatada em campo próprio, junto da command line executada.

Se uma flag rejeitada caiu no default, ela aparece com a nota da rejeição.

Não omita `contract_nested_type_definitions` quando o valor for `none`: registre a linha com fonte `DEFAULT` e nota `sem objetos aninhados`.

## 3. Colunas

| Coluna | Conteúdo |
|---|---|
| `flag` | nome da flag |
| `valor final` | valor que será usado |
| `fonte` | `CONTRACT`, `CONTRACT_DERIVED`, `INFERENCE` ou `DEFAULT` |
| `notas` | rejeição, derivação, aviso ou justificativa curta |

## 4. Fontes

| Fonte | Significado |
|---|---|
| `CONTRACT` | valor literal do contrato |
| `CONTRACT_DERIVED` | valor derivado do contrato pelo processo |
| `INFERENCE` | valor inferido a partir do use-case |
| `DEFAULT` | valor do catálogo |

Uma flag derivada que violou a regex e caiu no default aparece como `DEFAULT`, com a nota da rejeição.

## 5. Notas

Inclua nota quando:

- o valor foi derivado;
- o valor foi rejeitado pela regex;
- o valor foi inferido;
- o valor explícito venceu uma derivação;
- um tipo foi rejeitado;
- um atributo foi descartado;
- um POJO ou enum não pôde ser gerado;
- o atributo `id` do contrato foi mapeado no campo técnico `<entidade>Id` (ver `regras-01` §4.3). Nesse caso, a nota diz `id` → `<entidade>Id` (campo técnico) e o atributo **não** é listado como atributo derivado comum.
- a lista de atributos derivada foi rejeitada e a parada STOP-DER-02 foi acionada.

Notas devem ser curtas e factuais.

## 6. Descartadas

Liste separadamente as propostas descartadas:

| flag | valor proposto |
|---|---|
| ... | ... |

Uma proposta é descartada quando:

- a flag não existe no catálogo;
- a flag não é elegível para inferência;
- o valor não veio do contrato nem da allowlist.

Uma lista de atributos inválida não é uma proposta descartada: ela para com STOP-DER-02 e não entra nesta tabela.

## 7. Resumo

Termine com a linha exata:

```text
**Resumo**: <c> CONTRACT / <d> CONTRACT_DERIVED / <i> INFERENCE / <x> DEFAULT / <n> descartada(s)
```

Exemplo:

```text
**Resumo**: 8 CONTRACT / 4 CONTRACT_DERIVED / 1 INFERENCE / 22 DEFAULT / 2 descartada(s)
```

## 8. O que não fazer

- Não esconder rejeições.
- Não listar campos de negócio como flags.
- Não listar a pasta de destino como flag, nem inventar fonte para ela.
- Não inventar uma fonte.
- Não mostrar segredos.
- Não usar a tabela para substituir a validação real.
