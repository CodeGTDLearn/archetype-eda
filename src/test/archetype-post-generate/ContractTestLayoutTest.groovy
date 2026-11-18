/**
 * Layout gate for the generated contract schema test package: file, method and
 * support limits plus the shape rules (no loop in @Test, no mutable static
 * field, no "And" in a test name, no test calling another test).
 *
 * Run from repo root:
 *   groovy src/test/archetype-post-generate/ContractTestLayoutTest.groovy
 */

int failures = 0
def check = { String name, boolean cond ->
    if (cond) { println "PASS: ${name}" } else { println "FAIL: ${name}"; failures++ }
}

def codeLines = { String source ->
    return source.readLines().findAll { String line ->
        String t = line.trim()
        return t && !t.startsWith('//') && !t.startsWith('*') && !t.startsWith('/*')
                && !t.startsWith('*/') && !t.startsWith('import ') && !t.startsWith('package ')
    }.size()
}

def testMethods = { String source ->
    def lines = source.readLines()
    def methods = []
    for (int i = 0; i < lines.size(); i++) {
        if (lines[i].trim() != '@Test') continue
        int j = i + 1
        while (j < lines.size() && !lines[j].contains('(')) j++
        int start = j
        int depth = 0
        boolean opened = false
        while (j < lines.size()) {
            depth += lines[j].count('{') - lines[j].count('}')
            if (lines[j].contains('{')) opened = true
            if (opened && depth == 0) break
            j++
        }
        String signature = lines[start].trim()
        String name = (signature =~ /void\s+(\w+)\s*\(/).with { it.find() ? it.group(1) : signature }
        methods << [name: name, lines: lines.subList(start, Math.min(j + 1, lines.size()))]
        i = j
    }
    return methods
}

def checkSource = { String label, String source, int fileLimit, int supportLimit ->
    int code = codeLines(source)
    int limit = label.endsWith('Test.java') ? fileLimit : supportLimit
    check("${label}: ${code} code lines <= ${limit}", code <= limit)
    def methods = testMethods(source)
    def longMethods = methods.findAll { it.lines.size() > 20 }
    check("${label}: every @Test has <= 20 lines ${longMethods*.name}", longMethods.isEmpty())
    def loops = methods.findAll { m -> m.lines.any { it =~ /\b(for|while)\s*\(/ } }
    check("${label}: no loop inside @Test ${loops*.name}", loops.isEmpty())
    def mutableStatic = source.readLines().findAll {
        it =~ /^\s*(private|public|protected)?\s*static\s+(?!final\b)(?!void\b)[\w<>\[\]]+\s+\w+\s*[=;]/
    }
    check("${label}: no mutable static field ${mutableStatic}", mutableStatic.isEmpty())
    def andNames = methods.findAll { it.name ==~ /.*And.*/ }
    check("${label}: no 'And' in a test name ${andNames*.name}", andNames.isEmpty())
    def names = methods*.name as Set
    def calls = methods.findAll { m ->
        def body = m.lines.drop(1).join('\n')
        names.any { other -> other != m.name && body.contains(other + '(') }
    }
    check("${label}: no test calls another test ${calls*.name}", calls.isEmpty())
}

// ---------------------------------------------------------------------------
// 1) fixed templates
// ---------------------------------------------------------------------------
def fixedDir = new File("src/main/resources/archetype-resources/src/test/java/__entity_package_name__/contract")
def fixed = [
        "EntityFields.java", "ContractNames.java", "ContractKeys.java",
        "ContractJson.java", "ContractAttributes.java",
        "TechnicalIdMappingTest.java", "MalformedContractTest.java",
        "ContractKeyNormalizationTest.java", "ContractKeyDiscardTest.java"
]
fixed.each { String name ->
    File file = new File(fixedDir, name)
    check("1.0 ${name} exists", file.isFile())
    if (file.isFile()) {
        checkSource(name, file.getText("UTF-8"), 120, 60)
    }
}

// ---------------------------------------------------------------------------
// 2) generated output (full shape)
// ---------------------------------------------------------------------------
def harnessFile = new File("src/test/archetype-post-generate/PostProcessorHarness.groovy")
def harness = new GroovyClassLoader(this.class.classLoader).parseClass(harnessFile).newInstance()
File root = File.createTempDir("contract-layout-", ""); root.deleteOnExit()
File contracts = new File(root, "src/test/resources/contracts"); contracts.mkdirs()
new File(contracts, "contract.json").text = "{}"
def props = [
        "package": "it.pkg",
        entity_package_name: "contractentity",
        entity_name: "ContractEntity",
        enable_custom_entity_attributes: "true",
        custom_entity_attributes_list: "Long:id,String:name,Boolean:active,Endereco:endereco,Status:status,String:tags,Status:previousStatus",
        contract_nested_type_definitions: "Endereco|logradouro:String,numero:Integer,uf:Uf,complemento:Complemento;Complemento|descricao:String,pontoReferencia:PontoReferencia;PontoReferencia|descricao:String,distanciaMetros:Integer,referencia:Referencia;Referencia|nome:String,origem:Origem;Origem|bairro:String,detalhe:Detalhe;Detalhe|codigo:String,observacao:String",
        contract_enum_definitions: "Status:ATIVO|CANCELADO;nested.Uf:SP|RJ"
]
harness.runGenerator(root, props, "generator-04-contract_schema_tests.groovy")
File generatedDir = new File(root, "src/test/java/it/pkg/contractentity/contract")
def generated = generatedDir.listFiles().findAll { it.isFile() && it.name.endsWith(".java") }
check("2.1 the generator emitted 15 files", generated.size() == 15)
generated.each { File file -> checkSource(file.name, file.getText("UTF-8"), 120, 60) }

println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
