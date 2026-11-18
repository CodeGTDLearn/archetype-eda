/**
 * Unit test for generator-04-contract_schema_tests.groovy.
 *
 * Responsibility: prove that the set of emitted test files is derived from the
 * flags (no orphan, no empty block) and that a duplicate type fails explicitly.
 *
 * Run from repo root:
 *   groovy src/test/archetype-post-generate/ContractSchemaTestGeneratorTest.groovy
 */

import java.nio.file.Path

def generatorFile = new File("src/main/resources/META-INF/archetype_post_generate_generators/generator-04-contract_schema_tests.groovy")
def harnessFile = new File("src/test/archetype-post-generate/PostProcessorHarness.groovy")

int failures = 0
def check = { String name, boolean cond ->
    if (cond) { println "PASS: ${name}" } else { println "FAIL: ${name}"; failures++ }
}

check("1.0 the generator source exists", generatorFile.isFile())
check("1.1 the harness exists", harnessFile.isFile())

def root = { String prefix ->
    File dir = File.createTempDir(prefix, "")
    dir.deleteOnExit()
    File contracts = new File(dir, "src/test/resources/contracts")
    contracts.mkdirs()
    new File(contracts, "contract.json").text = "{}"
    return dir
}

def runGenerator = { File dir, Map props ->
    def harness = new GroovyClassLoader(this.class.classLoader)
            .parseClass(harnessFile).newInstance()
    return harness.runGenerator(dir, props, "generator-04-contract_schema_tests.groovy")
}

def emitted = { File dir ->
    File folder = new File(dir, "src/test/java/it/pkg/contractentity/contract")
    if (!folder.isDirectory()) return [] as Set
    return folder.listFiles().findAll { it.isFile() && it.name.endsWith(".java") }*.name as Set
}

def base = {
    return [
            "package": "it.pkg",
            entity_package_name: "contractentity",
            entity_name: "ContractEntity",
            enable_custom_entity_attributes: "true",
            contract_nested_type_definitions: "none",
            contract_enum_definitions: "none"
    ]
}

// ---------------------------------------------------------------------------
// 2) full shape: simple + nested + enums
// ---------------------------------------------------------------------------
def full = base()
full.custom_entity_attributes_list = "Long:id,String:name,Boolean:active,Endereco:endereco,Status:status,String:tags,Status:previousStatus"
full.contract_nested_type_definitions = "Endereco|logradouro:String,numero:Integer,uf:Uf,complemento:Complemento;Complemento|descricao:String,pontoReferencia:PontoReferencia;PontoReferencia|descricao:String,distanciaMetros:Integer,referencia:Referencia;Referencia|nome:String,origem:Origem;Origem|bairro:String,detalhe:Detalhe;Detalhe|codigo:String,observacao:String"
full.contract_enum_definitions = "Status:ATIVO|CANCELADO;nested.Uf:SP|RJ"
File fullRoot = root("contract-generator-full-")
runGenerator(fullRoot, full)
def fullFiles = emitted(fullRoot)
def expectedFull = [
        "SimpleAttributeFieldsTest.java", "NestedAttributeReferenceTest.java",
        "EnumAttributeReferenceTest.java", "NoExtraEntityFieldTest.java",
        "ContractRoundTripTest.java", "SavedContractMatchesGeneratedModelTest.java",
        "ContractTypeMappingTest.java", "EnderecoFieldsTest.java",
        "ComplementoFieldsTest.java", "PontoReferenciaFieldsTest.java",
        "ReferenciaFieldsTest.java", "OrigemFieldsTest.java", "DetalheFieldsTest.java",
        "StatusEnumTest.java", "UfEnumTest.java",
        "SavedContractNestedKeysTest.java", "SavedContractEnumsMatchTest.java"
] as Set
check("2.1 the full shape emits exactly 17 files", fullFiles.size() == 17)
check("2.2 the full shape emits the expected set: ${fullFiles - expectedFull} / ${expectedFull - fullFiles}",
        fullFiles == expectedFull)

// ---------------------------------------------------------------------------
// 3) simple shape: no nested, no enums
// ---------------------------------------------------------------------------
def simple = base()
simple.custom_entity_attributes_list = "Long:id,String:name"
File simpleRoot = root("contract-generator-simple-")
runGenerator(simpleRoot, simple)
def simpleFiles = emitted(simpleRoot)
def expectedSimple = [
        "SimpleAttributeFieldsTest.java", "NoExtraEntityFieldTest.java",
        "ContractRoundTripTest.java", "SavedContractMatchesGeneratedModelTest.java",
        "ContractTypeMappingTest.java"
] as Set
check("3.1 the simple shape emits exactly 5 files", simpleFiles.size() == 5)
check("3.2 the simple shape emits the expected set: ${simpleFiles - expectedSimple} / ${expectedSimple - simpleFiles}",
        simpleFiles == expectedSimple)

// ---------------------------------------------------------------------------
// 4) scaffold shape: no custom attributes
// ---------------------------------------------------------------------------
def scaffold = base()
scaffold.custom_entity_attributes_list = "none"
scaffold.enable_custom_entity_attributes = "false"
File scaffoldRoot = root("contract-generator-scaffold-")
runGenerator(scaffoldRoot, scaffold)
def scaffoldFiles = emitted(scaffoldRoot)
def expectedScaffold = [
        "ScaffoldFieldsTest.java", "NoExtraEntityFieldTest.java",
        "ContractRoundTripTest.java", "SavedContractMatchesGeneratedModelTest.java",
        "ContractTypeMappingTest.java"
] as Set
check("4.1 the scaffold shape emits exactly 5 files", scaffoldFiles.size() == 5)
check("4.2 the scaffold shape emits the expected set: ${scaffoldFiles - expectedScaffold} / ${expectedScaffold - scaffoldFiles}",
        scaffoldFiles == expectedScaffold)

// ---------------------------------------------------------------------------
// 5) duplicate type: explicit failure, never overwrite
// ---------------------------------------------------------------------------
def collision = base()
collision.custom_entity_attributes_list = "Long:id,String:name,Endereco:endereco,Endereco:outroEndereco"
collision.contract_nested_type_definitions = "Endereco|logradouro:String;Endereco|numero:Integer"
File collisionRoot = root("contract-generator-collision-")
String message = ""
try {
    runGenerator(collisionRoot, collision)
} catch (Throwable t) {
    message = t.message == null ? t.toString() : t.message
}
check("5.1 a duplicate type fails explicitly", message.contains("duplicate test file 'EnderecoFieldsTest'"))

println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
