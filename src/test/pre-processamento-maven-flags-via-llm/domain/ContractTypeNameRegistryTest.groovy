/**
 * Unit test for ContractTypeNameRegistry - the shared type-name allocator of
 * the nested POJOs and the payload-derived enums.
 *
 * The component under test is the production module:
 *   src/main/pre-processamento-maven-flags-via-llm/domain/ContractTypeNameRegistry.groovy
 *
 * No JUnit infra in this repo; runnable directly:
 *   groovy src/test/pre-processamento-maven-flags-via-llm/domain/ContractTypeNameRegistryTest.groovy
 * Exits 0 when every check passes, 1 otherwise.
 */

def moduleRoot = new File("src/main/pre-processamento-maven-flags-via-llm")
def sourceIndex = [:]
moduleRoot.eachFileRecurse { File f ->
    if (f.isFile() && f.name.endsWith(".groovy")) sourceIndex[f.name] = f
}
def gcl = new GroovyClassLoader(this.class.classLoader)
gcl.setResourceLoader({ String className ->
    String simple = className.substring(className.lastIndexOf(".") + 1)
    File source = sourceIndex[simple + ".groovy"]
    return source == null ? null : source.toURI().toURL()
} as groovy.lang.GroovyResourceLoader)
def resolverClass = gcl.loadClass("eda.postgenerate.pre_processamento_maven_flags_via_llm.domain.ContractTypeResolver")
def registryClass = gcl.loadClass("eda.postgenerate.pre_processamento_maven_flags_via_llm.domain.ContractTypeNameRegistry")

int failures = 0
def check = { String name, boolean cond ->
    if (cond) { println "PASS: ${name}" } else { println "FAIL: ${name}"; failures++ }
}

def newRegistry = { String entityName -> registryClass.newInstance(resolverClass.newInstance(), entityName) }

check("1.1 a null resolver is rejected",
        { try { registryClass.newInstance(null, "Cliente"); false } catch (IllegalArgumentException e) { true } }())

def r2 = newRegistry("Cliente")
check("2.1 a java.lang name receives the 'Type' suffix", r2.uniqueTypeName("String", null) == "StringType")
check("2.2 the entity name is reserved", r2.uniqueTypeName("Cliente", null) == "ClienteType")

def r3 = newRegistry("Cliente")
r3.reserveType("br.com.projeto.Endereco")
check("3.1 a reserved project type is not reused", r3.uniqueTypeName("Endereco", null) == "EnderecoType")

def r4 = newRegistry("Cliente")
check("4.1 a free name is used as is", r4.uniqueTypeName("Endereco", null) == "Endereco")
check("4.2 a colliding name is prefixed with the immediate parent",
        r4.uniqueTypeName("Endereco", "Cliente") == "ClienteEndereco")

def r5 = newRegistry("Cliente")
check("5.1 the base is allocated first", r5.uniqueTypeName("Endereco", null) == "Endereco")
check("5.2 the parent prefix is used before the Type suffix",
        r5.uniqueTypeName("ClienteEndereco", null) == "ClienteEndereco")
check("5.3 ...then the Type suffix", r5.uniqueTypeName("Endereco", "Cliente") == "EnderecoType")
check("5.4 ...then the Type suffix with a sequence", r5.uniqueTypeName("Endereco", "Cliente") == "EnderecoType2")

def r6 = newRegistry("Cliente")
check("6.1 a released name can be reused", r6.uniqueTypeName("Endereco", null) == "Endereco")
r6.releaseType("Endereco")
check("6.2 ...after releaseType", r6.uniqueTypeName("Endereco", null) == "Endereco")

def r7 = newRegistry("Cliente")
check("7.1 a long name is truncated to 120 chars",
        r7.uniqueTypeName("a" * 130, null).length() == 120)

def r8 = newRegistry("Cliente")
String longBase = "A" + ("a" * 119)
r8.reserveType("Cliente" + longBase.substring(0, 113))
String first = r8.uniqueTypeName(longBase, null)
String second = r8.uniqueTypeName(longBase, null)
check("8.1 the base is allocated first", first == longBase)
check("8.2 a reserved parent prefix still terminates with a distinct name",
        second != first && second.length() == 120)

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
