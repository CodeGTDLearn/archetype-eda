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

def nested = assembler.assemble([[name: 'endereco', value: [logradouro: 'Rua A']]], 'Cliente')
check("1.7 a nested object becomes a POJO",
        nested.attributes[0].type == 'Endereco' && nested.nestedTypes.size() == 1)
check("1.8 ...and the POJO is listed in nestedTypes",
        nested.nestedTypes[0].name == 'Endereco')

def declaredLang = assembler.assemble([[name: 'payload', type: 'String', value: [a: 1]]])
check("1.9 a declared java.lang type wins over the object value",
        declaredLang.attributes[0].type == 'String' && declaredLang.nestedTypes.isEmpty())

def reservedType = assembler.assemble([
        [name: 'enderecoCliente', type: 'br.com.projeto.EnderecoCliente'],
        [name: 'endereco_cliente', value: [logradouro: 'Rua A']]
], 'Cliente')
check("1.10 a project-type name collision drops the nested attribute before deriving it",
        reservedType.attributes.size() == 1 && reservedType.nestedTypes.isEmpty() &&
                reservedType.warnings.any { it.contains("nome duplicado 'enderecoCliente'") })

def entityCollision = assembler.assemble([[name: 'cliente', value: [nome: 'x']]], 'Cliente')
check("1.11 the entity name is reserved for the nested POJO",
        entityCollision.attributes[0].type == 'ClienteType')

def enumArray = assembler.assemble([
        [name: 'status', type: 'br.com.projeto.StatusPedido', value: ['ATIVO', 'CANCELADO']]
])
check("1.12 a project type + UPPERCASE array is an enum",
        enumArray.attributes[0].type == 'StatusPedido' &&
                enumArray.attributeDetails[0].enumType == true &&
                enumArray.attributeDetails[0].enumConstants == ['ATIVO', 'CANCELADO'])
check("1.13 ...and the attribute is named after the type",
        enumArray.attributes[0].name == 'statusPedido')

def lowerArray = assembler.assemble([
        [name: 'status', type: 'br.com.projeto.StatusPedido', value: ['ativo']]
])
check("1.14 a project type + lowercase array becomes String",
        lowerArray.attributes[0].type == 'String' &&
                lowerArray.warnings.any { it.contains('array') })

def langArray = assembler.assemble([[name: 'codigos', type: 'Integer', value: [1, 2]]])
check("1.15 java.lang + array keeps the type and warns",
        langArray.attributes[0].type == 'Integer' &&
                langArray.warnings.any { it.contains('array') })

def payloadEnum = assembler.assemble([[name: 'status', value: ['ATIVO', 'CANCELADO']]])
check("1.16 a payload UPPERCASE array becomes an enum",
        payloadEnum.attributes[0].type == 'Status' &&
                payloadEnum.attributeDetails[0].enumType == true &&
                payloadEnum.attributeDetails[0].projectType == false)
check("1.17 ...and the enum is recorded",
        payloadEnum.enumTypes.size() == 1 &&
                payloadEnum.enumTypes[0].name == 'Status' &&
                payloadEnum.enumTypes[0].scope == 'entity')

def nestedEnum = assembler.assemble([[name: 'endereco', value: [uf: ['SP', 'RJ']]]], 'Cliente')
check("1.18 a nested UPPERCASE array becomes an enum field",
        nestedEnum.nestedTypes[0].fields[0].type == 'Uf' &&
                nestedEnum.enumTypes.size() == 1 &&
                nestedEnum.enumTypes[0].scope == 'nested')

def lowerPayloadEnum = assembler.assemble([[name: 'status', value: ['ativo']]])
check("1.19 a lowercase payload array stays String",
        lowerPayloadEnum.attributes[0].type == 'String' && lowerPayloadEnum.enumTypes.isEmpty())

def duplicateEnum = assembler.assemble([
        [name: 'status', value: 'ATIVO'],
        [name: 'status', value: ['ATIVO', 'CANCELADO']]
])
check("1.20 a duplicate enum attribute is dropped before deriving the enum",
        duplicateEnum.attributes.size() == 1 && duplicateEnum.enumTypes.isEmpty() &&
                duplicateEnum.warnings.any { it.contains("nome duplicado 'status'") })

def duplicateNested = assembler.assemble([
        [name: 'endereco', value: 'Rua A'],
        [name: 'endereco', value: [logradouro: 'B']]
])
check("1.21 a duplicate nested attribute is dropped before deriving the POJO",
        duplicateNested.attributes.size() == 1 && duplicateNested.nestedTypes.isEmpty())

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
