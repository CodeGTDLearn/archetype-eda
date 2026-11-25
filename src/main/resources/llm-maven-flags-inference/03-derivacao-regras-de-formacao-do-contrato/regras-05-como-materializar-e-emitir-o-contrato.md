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

- Grave **a forma canônica de §1** no arquivo, em UTF-8.
- O arquivo gravado é byte a byte a forma canônica. Sem espaços, sem quebras de linha e com as chaves ordenadas recursivamente.
- A forma canônica é o que vai para o disco e o que é copiado para o projeto gerado. O SHA-256 de §2 é calculado sobre exatamente estes mesmos bytes.
- Não acrescente comentários, campos extras ou formatação posterior.
- O arquivo gravado é a fonte da cópia do pós-processador.

> **Por que "não reordene" não se aplica aqui:** a reordenação recursiva de chaves **é** a forma canônica. O que é proibido é reordenar de novo, reformatar ou reintroduzir espaçamento fora do §1. Gogar o contrato como o agente o recebeu produziria bytes diferentes do hash que dá nome ao arquivo.

## 5. Caminho emitido

- `entity_contract_file_path` recebe o caminho absoluto do arquivo.
- Emita com barras normais (`/`), nunca com barras invertidas.
- A fonte é `CONTRACT_DERIVED`.
- Se a materialização falhar, pare com STOP-DER-04 e reporte: não use `none`.
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
- O caminho precisa ser **absoluto**: um caminho relativo é recusado com aviso, mesmo que case com a regex do catálogo. Pare com STOP-DER-04.
- O arquivo precisa ser JSON válido com raiz objeto.
- O SHA-256 do §2 é calculado sobre a forma canônica; o arquivo gravado tem esses mesmos bytes. Se os bytes do disco divergirem da forma canônica, regrave antes de emitir o caminho.

## 8. Segredos

- O contrato é persistido e versionado.
- Não coloque credenciais no contrato.
- Se houver chaves com aparência de segredo, avise antes de gravar.
- Nunca imprima o conteúdo integral do contrato no relatório.
