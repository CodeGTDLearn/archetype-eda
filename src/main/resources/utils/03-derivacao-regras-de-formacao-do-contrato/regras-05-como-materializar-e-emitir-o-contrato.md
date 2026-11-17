# Como materializar e emitir o contrato

Esta regra grava o contrato em disco e emite o caminho em `entity_contract_file_path`.

## 1. Forma canônica

A forma canônica é um JSON compacto:

- sem espaços e sem quebras de linha;
- chaves de objeto ordenadas recursivamente;
- arrays preservam a ordem;
- strings escapadas conforme JSON;
- números e booleanos literais;
- `null` literal.

Exemplo:

```json
{"attributes":{"ativo":true,"nome":"Maria"},"entity_name":"Cliente"}
```

## 2. Nome do arquivo

1. Calcule o SHA-256 da forma canônica em UTF-8.
2. Use o hexadecimal em minúsculas.
3. O nome do arquivo é:

```text
<sha256>.json
```

O nome é determinístico: o mesmo contrato produz o mesmo arquivo.

## 3. Diretório

Use, nesta ordem:

1. o diretório indicado pelo usuário;
2. o diretório de contratos do archetype:
   - Windows: `%LOCALAPPDATA%\eda-archetype\contracts`;
   - POSIX com `XDG_CACHE_HOME`: `$XDG_CACHE_HOME/eda-archetype/contracts`;
   - POSIX sem `XDG_CACHE_HOME`: `~/.cache/eda-archetype/contracts`.

Crie o diretório se ele não existir.

## 4. Conteúdo do arquivo

- Grave o JSON do contrato em UTF-8.
- Remova espaços em branco nas extremidades.
- Não reordene, não reformate e não acrescente comentários.
- O arquivo gravado é a fonte da cópia do pós-processador.

## 5. Caminho emitido

- `entity_contract_file_path` recebe o caminho absoluto do arquivo.
- Emita com barras normais (`/`), nunca com barras invertidas.
- A fonte é `CONTRACT_DERIVED`.
- Se a materialização falhar, pare e reporte: não use `none`.
- O caminho é parte da identidade da geração: mudar o diretório muda o caminho emitido.

## 6. Cópia no projeto gerado

O pós-processador copia o contrato para:

```text
src/test/resources/contracts/contract.json
```

Somente quando:

- `entity_contract_file_path` existe e não é `none`; e
- `enable_folder_src_test_java=true`.

A cópia é byte a byte igual ao arquivo materializado.

Se o arquivo de origem não existir:

- o pós-processador avisa;
- a geração continua;
- o relatório registra a ausência.

## 7. Validação

- `entity_contract_file_path` deve satisfazer `^[^\r\n]*\.json$`: `none` é valor do catálogo, fora deste procedimento.
- O caminho não pode conter quebra de linha.
- O caminho não pode apontar para diretório.
- O arquivo precisa ser JSON válido com raiz objeto.

## 8. Segredos

- O contrato é persistido e versionado.
- Não coloque credenciais no contrato.
- Se houver chaves com aparência de segredo, avise antes de gravar.
- Nunca imprima o conteúdo integral do contrato no relatório.
