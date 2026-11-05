def moduleRoot = new File("src/main/pre-processamento-maven-flags-via-llm")
def index = [:]
moduleRoot.eachFileRecurse { File f -> if (f.isFile() && f.name.endsWith('.groovy')) index[f.name] = f }
def gcl = new GroovyClassLoader(this.class.classLoader)
gcl.setResourceLoader({ String cn ->
    def src = index[cn.substring(cn.lastIndexOf('.') + 1) + '.groovy']
    return src == null ? null : src.toURI().toURL()
} as groovy.lang.GroovyResourceLoader)
def resolver = gcl.loadClass('eda.postgenerate.pre_processamento_maven_flags_via_llm.domain.ContractTypeResolver').newInstance()
def assemblerClass = gcl.loadClass('eda.postgenerate.pre_processamento_maven_flags_via_llm.domain.ContractAttributeAssembler')
def assembler = assemblerClass.newInstance(resolver)

int failures = 0
def check = { String n, boolean c -> if (c) { println "PASS: ${n}" } else { println "FAIL: ${n}"; failures++ } }

check("1.1 a null resolver is rejected",
        { try { assemblerClass.newInstance(null); false } catch (IllegalArgumentException e) { true } }())

def invalid = assembler.assemble([[name: '!!!']])
check("1.2 an invalid name is discarded with a warning",
        invalid.attributes.isEmpty() && invalid.warnings.any { it.contains('nome invalido') })

def duplicate = assembler.assemble([[name: 'id', value: 1], [name: 'id', value: 2]])
check("1.3 a duplicate name is discarded with a warning",
        duplicate.attributes.size() == 1 && duplicate.warnings.any { it.contains("nome duplicado 'id'") })

def project = assembler.assemble([[name: 'x', type: 'br.com.StatusPedido', value: 'ATIVO', enumValues: ['ATIVO']]])
check("1.4 a PROJECT type names the attribute after the type",
        project.attributes[0].name == 'statusPedido' && project.attributes[0].type == 'StatusPedido')
check("1.5 the project details carry the enum metadata",
        project.attributeDetails[0].projectType == true && project.attributeDetails[0].enumType == true &&
                project.attributeDetails[0].enumConstants == ['ATIVO'])

def clash = assembler.assemble([
        [name: 'a', type: 'br.com.StatusPedido', value: 'A'],
        [name: 'b', type: 'br.com.StatusPedido', value: 'B']
])
check("1.6 a repeated project type keeps the contract name of the second attribute",
        clash.attributes[0].name == 'statusPedido' && clash.attributes[1].name == 'b')

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
