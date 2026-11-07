/**
 * Unit test for ContractEnumDeriver - the payload convention that turns an
 * array of UPPERCASE strings into an enum.
 *
 * The component under test is the production module:
 *   src/main/pre-processamento-maven-flags-via-llm/domain/ContractEnumDeriver.groovy
 *
 * No JUnit infra in this repo; runnable directly:
 *   groovy src/test/pre-processamento-maven-flags-via-llm/domain/ContractEnumDeriverTest.groovy
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
def deriverClass = gcl.loadClass("eda.postgenerate.pre_processamento_maven_flags_via_llm.domain.ContractEnumDeriver")

int failures = 0
def check = { String name, boolean cond ->
    if (cond) { println "PASS: ${name}" } else { println "FAIL: ${name}"; failures++ }
}

def newDeriver = { String entityName ->
    def resolver = resolverClass.newInstance()
    def registry = registryClass.newInstance(resolver, entityName)
    deriverClass.newInstance(resolver, registry)
}

check("1.1 a null resolver is rejected",
        { try { deriverClass.newInstance(null, null); false } catch (IllegalArgumentException e) { true } }())

def d2 = newDeriver("Cliente")
String status = d2.deriveEntityEnum("status", ["ATIVO", "CANCELADO"])
check("2.1 an entity UPPERCASE array becomes an enum", status == "Status")
check("2.2 ...with the contract constants", d2.enumTypes.size() == 1 &&
        d2.enumTypes[0].constants == ["ATIVO", "CANCELADO"])
check("2.3 ...and scope 'entity'", d2.enumTypes[0].scope == "entity")

def d3 = newDeriver("Cliente")
check("3.1 a lowercase array is not an enum",
        d3.deriveEntityEnum("status", ["ativo"]) == null)
check("3.2 ...and no enum is recorded", d3.enumTypes.isEmpty())

def d4 = newDeriver("Cliente")
check("4.1 an empty array is not an enum", d4.deriveEntityEnum("status", []) == null)
check("4.2 a mixed array is not an enum", d4.deriveEntityEnum("status", ["ATIVO", 1]) == null)

def d5 = newDeriver("Cliente")
String uf = d5.deriveNestedEnum("uf", ["SP", "RJ"], "Endereco")
check("5.1 a nested UPPERCASE array becomes an enum", uf == "Uf")
check("5.2 ...with scope 'nested'", d5.enumTypes[0].scope == "nested")

def d6 = newDeriver("Cliente")
check("6.1 a nested enum takes the base name first",
        d6.deriveNestedEnum("uf", ["SP"], "Endereco") == "Uf")
check("6.2 ...a second collision is prefixed with its parent",
        d6.deriveNestedEnum("uf", ["RJ"], "Entrega") == "EntregaUf")

def d7 = newDeriver("Status")
check("7.1 the entity name is reserved for the enum", d7.deriveEntityEnum("status", ["ATIVO"]) == "StatusType")

def d8 = newDeriver("Cliente")
check("8.1 duplicate constants are deduplicated",
        d8.deriveEntityEnum("uf", ["SP", "SP", "RJ"]) == "Uf" &&
                d8.enumTypes[0].constants == ["SP", "RJ"])

def d9 = newDeriver("Cliente")
check("9.1 a scalar uppercase string is not an enum",
        d9.deriveEntityEnum("status", "ATIVO") == null)

def d10 = newDeriver("Cliente")
check("10.1 a Java array of uppercase strings is an enum",
        d10.deriveEntityEnum("uf", new String[]{"SP", "RJ"}) == "Uf")

def d11 = newDeriver("Cliente")
check("11.1 an invalid key has no valid enum type name",
        d11.deriveEntityEnum("###", ["ATIVO"]) == null)

def d12 = newDeriver("Cliente")
d12.deriveEntityEnum("status", ["ATIVO"])
d12.deriveNestedEnum("uf", ["SP"], "Endereco")
check("12.1 the derived enums keep the derivation order",
        d12.enumTypes*.name == ["Status", "Uf"])

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
