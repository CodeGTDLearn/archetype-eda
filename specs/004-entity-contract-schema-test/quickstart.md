# Quickstart: Gate de Conformidade Entidade–Contrato (replan)

**Feature**: `004-entity-contract-schema-test` | **Date**: 2026-11-13

Pré-requisitos: JDK 17, Maven 3.9.16, raiz do repo, branch `004-entity-contract-schema-test`.

## 1. Recompõe o pós-processador e roda tudo

```powershell
mvn clean install
```

**Esperado**: `target/classes/META-INF/archetype-post-generate.groovy` recomposto (inclui `SNIPPET-20-CONTRACT_SCHEMA_TEST`); suites Groovy verdes (inclui `SnippetContractSchemaTestTest`); 22/22 fixtures verdes.

## 2. Só a fixture entity-contract

```powershell
Copy-Item -Recurse src/test/resources/projects/entity-contract target/it-single/entity-contract -Force
mvn clean integration-test -Darchetype.test.projectsDirectory=target/it-single
```

**Esperado** no `verify.groovy`:
1. pacote `contract/` presente na aplicação gerada;
2. `target/surefire-reports` da aplicação contém 21 relatórios e 43 testes, com 0 falhas/erros;
3. cópia sabotada falha `mvnw test` com uma falha por divergência, nomeando atributo e sentido.

## 3. Frente 01 manual

```powershell
mvn archetype:generate -DarchetypeGroupId=com.eda-archetype -DarchetypeArtifactId=eda-archetype -DarchetypeVersion=0.0.1-SNAPSHOT `
  -DgroupId=archetype.it -DartifactId=demo-contract -Dversion=0.1-SNAPSHOT -Dpackage=it.pkg `
  -Dentity_contract_file_path=src/test/resources/projects/entity-contract/sample-contract.json `
  -Denable_folder_src_test_java=true -Denable_custom_entity_attributes=true `
  -Dcustom_entity_attributes_list=Long:id,String:name,Boolean:active,Endereco:endereco,Status:status,String:tags,Status:previousStatus `
  -Dcontract_nested_type_definitions="Endereco|logradouro:String,numero:Integer,uf:Uf,complemento:Complemento;Complemento|descricao:String,pontoReferencia:PontoReferencia;PontoReferencia|descricao:String,distanciaMetros:Integer,referencia:Referencia;Referencia|nome:String,origem:Origem;Origem|bairro:String,detalhe:Detalhe;Detalhe|codigo:String,observacao:String" `
  -Dcontract_enum_definitions="Status:ATIVO|CANCELADO;nested.Uf:SP|RJ" `
  -Dentity_name=ContractEntity -Dentity_package_name=contractentity -DinteractiveMode=false
```

No módulo:

```powershell
cd demo-contract; ./mvnw test        # 1) verde: 21 arquivos / 43 testes do pacote contract executados
# renomeie/remova um campo da entidade OU edite contracts/contract.json
./mvnw test                          # 2) vermelho: uma falha por divergência, com atributo/tipo/sentido
./mvnw test                          # 3) verde novamente após corrigir
```

## 4. Negativo

Gerar sem `entity_contract_file_path`/`none`, ou com `enable_folder_src_test_java=false`:

**Esperado**: pacote `contract/` ausente; `mvn test` da aplicação verde. (Também assegurado pelos 19 `verify.groovy` que verificam a ausência pelo diretório `contract/`.)

## 5. Lombok on/off

Repetir o cenário 3 com `enable_lombok_support=true|false`: veredito idêntico.

## 6. Documentação da feature no README

Revisão manual do README (não há comando de build envolvido):

1. Abrir `README.md` e expandir **Funcionalidades** → deve existir o subtópico **Contratos**, em `<details>`/`<summary>`.
2. O `<summary>` deve exibir `Contratos📜: a 'Fonte da Verdade' do Módulo da API`, sem título interno repetindo o nome.
3. O conteúdo deve ter seis itens numerados, nesta ordem: 1) Raciocínio central; 2) O que faz; 3) Como funciona; 4) Para quem serve; 5) Quando ele existe; 6) Detalhes técnicos.
4. O item 1 deve abrir com o ciclo fechado (desenho da tela → contrato → módulo da API → tela) e conter o diagrama em bloco `text`, com a seta de retorno da TELA ao DESENHO.
5. O link de **Detalhes técnicos** para *Testabilidade → 2.1) SCHEMA TEST ENTIDADE-CONTRATO* deve abrir a seção existente, que preserva título, âncora, premissa, ativação, regras espelhadas e nota de JSON; apenas os 4 bullets técnicos (contagem/nomes, forma da falha, perfis de prova e agregação) refletem o desenho novo.
6. Nenhuma menção duplicada do mecanismo de contrato (inferência/formato/flags) dentro do subtópico.

**Esperado**: os seis itens conferem; nenhuma outra seção do README alterada.

## Referências

- Regras espelhadas: `src/main/resources/utils/03-derivacao-regras-de-formacao-do-contrato/regras-01..03`
- Contrato: `contracts/schema-test-contract.md` · Modelo: `data-model.md`
