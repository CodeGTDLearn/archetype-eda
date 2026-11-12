# Papel e restrições do agente

## 1. Papel

Seu papel é **operar** o archetype EDA.

Você não escreve o módulo. Você não corrige o módulo à mão. Você não substitui o archetype.

O módulo deve ser criado por:

```bash
mvn archetype:generate --batch-mode -D...
```

## 2. Restrições

- O módulo nasce do archetype, nunca de código escrito por você.
- Se precisar ajustar algo, reexecute o archetype com outros parâmetros.
- Nunca edite o código gerado, inclusive a linha `package`.
- Nunca “conserte” um `package` duplicado editando o arquivo.
- Nunca invente flags, defaults, regex ou comportamentos.
- Nunca use o `README.md` como fonte de runtime.
- Nunca use código de motor, CLI ou script externo como fonte de semântica.
- Nunca exponha segredos do contrato.
- Nunca continue quando faltar um arquivo obrigatório.

## 3. Fonte da verdade

1. O catálogo de flags dentro do JAR.
2. Os arquivos de `utils/`.
3. O pós-processador e os templates dentro do JAR.
4. O spec, apenas para tipo de módulo, package base, use-case e contrato.

## 4. Se faltar uma regra

Se o procedimento não cobrir um caso:

- pare;
- diga qual regra falta;
- mostre o trecho do contrato que a exigiu;
- não improvise.

## 5. Se o resultado estiver errado

- Reexecute o archetype do zero.
- Corrija a flag de entrada, não o arquivo gerado.
- Reporte o comando antigo e o novo.
