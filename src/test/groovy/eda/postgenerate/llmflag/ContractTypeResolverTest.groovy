/**
 * Unit test for ContractTypeResolver - the TYPE INFERENCE component (single
 * responsibility: resolve the Java type of ONE entity attribute).
 *
 * The component under test is the CANONICAL module:
 *   src/main/groovy/eda/postgenerate/llmflag/ContractTypeResolver.groovy
 * (the Orchestrator carries an inline runtime mirror of it; the parity between
 * the mirror and this module is asserted by ContractDerivationTest.groovy).
 *
 * No JUnit infra in this repo; runnable directly:
 *   groovy src/test/groovy/eda/postgenerate/llmflag/ContractTypeResolverTest.groovy
 * Exits 0 when every check passes, 1 otherwise.
 */

def gcl = new GroovyClassLoader(this.class.classLoader)
def resolverClass = gcl.parseClass(new File("src/main/groovy/eda/postgenerate/llmflag/ContractTypeResolver.groovy"))
def resolver = resolverClass.newInstance()

int failures = 0
def check = { String name, boolean cond ->
    if (cond) { println "PASS: ${name}" } else { println "FAIL: ${name}"; failures++ }
}

// ---------------------------------------------------------------------------
// 1) A DECLARED java.lang type wins over the value estimate
// ---------------------------------------------------------------------------
def r1 = resolver.resolve("id", "Long", 1)
check("1.1 a declared java.lang type wins over the value estimate", r1.type == "Long")
check("1.2 ...and no warning is raised", r1.warning == null)

// ---------------------------------------------------------------------------
// 2) A declared FQN java.lang type is normalized to the simple name
// ---------------------------------------------------------------------------
def r2 = resolver.resolve("label", "java.lang.String", "x")
check("2.1 a declared FQN java.lang type is normalized to the simple name", r2.type == "String")
check("2.2 ...and no warning is raised", r2.warning == null)

// ---------------------------------------------------------------------------
// 3) A type OUTSIDE java.lang is rejected: warning + estimate from the value
// ---------------------------------------------------------------------------
def r3 = resolver.resolve("createdAt", "java.time.Instant", 1)
check("3.1 a type outside java.lang falls back to the value estimate", r3.type == "Integer")
check("3.2 ...the rejection is reported as a loud warning", (r3.warning ?: "").contains("java.time.Instant"))
check("3.3 ...the warning names the offending attribute", (r3.warning ?: "").contains("createdAt"))

// ---------------------------------------------------------------------------
// 4) A PROJECT type (outside java.lang) is KEPT: it is a valid domain type
// ---------------------------------------------------------------------------
def r4 = resolver.resolve("cliente", "ClienteEntity", null)
check("4.1 a project-own type outside java.lang is kept", r4.type == "ClienteEntity")
check("4.2 ...and no warning is raised", r4.warning == null)
check("4.3 ...and the resolution is flagged as a project type", r4.projectType == true)

def r4b = resolver.resolve("endereco", "br.com.projeto.EnderecoCliente", [rua: "X"])
check("4.4 a project type declared with a FQN is normalized to the simple name",
        r4b.type == "EnderecoCliente")
check("4.5 ...the qualified type is preserved for the import",
        r4b.qualifiedType == "br.com.projeto.EnderecoCliente")
check("4.6 ...and no warning is raised", r4b.warning == null)

// ---------------------------------------------------------------------------
// 5) Generics are dropped BEFORE the java.lang check (List<String> -> List)
// ---------------------------------------------------------------------------
def r5 = resolver.resolve("tags", "List<String>", ["a", "b"])
check("5.1 a generic declared type is rejected after the generics are dropped", r5.type == "String")
check("5.2 ...with a loud warning naming the rejected type", (r5.warning ?: "").contains("List<String>"))

// ---------------------------------------------------------------------------
// 6) The estimate maps the runtime class of the VALUE to a java.lang type
// ---------------------------------------------------------------------------
check("6.1 an Integer value estimates Integer", resolver.resolve("id", null, 1024).type == "Integer")
check("6.2 a Long value estimates Long", resolver.resolve("big", null, 5000000000L).type == "Long")
check("6.3 a decimal value estimates Double", resolver.resolve("score", null, new BigDecimal("9.5")).type == "Double")
check("6.4 a boolean value estimates Boolean", resolver.resolve("active", null, true).type == "Boolean")
check("6.5 a string value estimates String", resolver.resolve("name", null, "Acme").type == "String")
check("6.6 a null value estimates String", resolver.resolve("x", null, null).type == "String")
check("6.7 a nested object (Map) estimates String", resolver.resolve("addr", null, [rua: "X"]).type == "String")
check("6.8 an array (List) estimates String", resolver.resolve("items", null, ["a"]).type == "String")

// ---------------------------------------------------------------------------
// 7) sanitizeAttributeName: a valid Java identifier that satisfies the
//    catalog regex ^[a-z][a-zA-Z0-9_]*$
// ---------------------------------------------------------------------------
check("7.1 special characters become an underscore", resolver.sanitizeAttributeName("nome-cachorro") == "nome_cachorro")
check("7.2 a leading digit gets the field_ prefix", resolver.sanitizeAttributeName("9abc") == "field_9abc")
check("7.3 a leading uppercase letter is lowercased", resolver.sanitizeAttributeName("Nome") == "nome")
check("7.4 a name with no valid character is rejected", resolver.sanitizeAttributeName("!!!") == null)
check("7.5 an already valid name passes through", resolver.sanitizeAttributeName("nome_ok") == "nome_ok")

// ---------------------------------------------------------------------------
// 8) normalizeType edge cases
// ---------------------------------------------------------------------------
check("8.1 a null declared type is rejected", resolver.normalizeType(null) == null)
check("8.2 a blank declared type is rejected", resolver.normalizeType("   ") == null)
check("8.3 a java.lang type passes the normalization", resolver.normalizeType("Boolean") == "Boolean")

// ---------------------------------------------------------------------------
// 9) isProjectType: what is a PROJECT type and what is not
// ---------------------------------------------------------------------------
check("9.1 a domain type is a project type", resolver.isProjectType("ClienteEntity"))
check("9.2 a domain type with a FQN is a project type",
        resolver.isProjectType("br.com.projeto.StatusPedido"))
check("9.3 a java.lang type is NOT a project type", !resolver.isProjectType("String"))
check("9.4 a JDK type outside java.lang is NOT a project type",
        !resolver.isProjectType("java.time.Instant"))
check("9.5 a generic type is NOT a project type", !resolver.isProjectType("List<String>"))
check("9.6 a bare JDK type name is NOT a project type", !resolver.isProjectType("List"))
check("9.7 a name that is not a class-like name is NOT a project type",
        !resolver.isProjectType("statusPedido"))
check("9.8 a blank type is NOT a project type", !resolver.isProjectType("   "))

// ---------------------------------------------------------------------------
// 10) the enum detection: a PROJECT type with a textual value is an enum
// ---------------------------------------------------------------------------
def r10 = resolver.resolve("status", "StatusPedido", "ATIVO")
check("10.1 a project type with a textual value is an enum", r10.enumType == true)
check("10.2 ...the observed value becomes the constant", r10.enumConstants == ["ATIVO"])

def r10b = resolver.resolve("status", "StatusPedido", "ATIVO", ["ATIVO", "em processamento"])
check("10.3 the explicit constants win over the observed value",
        r10b.enumConstants == ["ATIVO", "EM_PROCESSAMENTO"])
check("10.4 ...and the type is still an enum", r10b.enumType == true)

def r10c = resolver.resolve("endereco", "EnderecoCliente", [rua: "X"])
check("10.5 a project type with a nested object is NOT an enum", r10c.enumType == false)
check("10.6 ...and it carries no constant", r10c.enumConstants == [])

def r10d = resolver.resolve("status", "StatusPedido", null)
check("10.7 a project type without a textual value is NOT an enum", r10d.enumType == false)

// ---------------------------------------------------------------------------
// 11) fieldNameForType: the attribute name comes from the TYPE
// ---------------------------------------------------------------------------
check("11.1 a domain class type names the attribute after itself",
        resolver.fieldNameForType("EnderecoCliente") == "enderecoCliente")
check("11.2 an enum type names the attribute after itself",
        resolver.fieldNameForType("StatusPedido") == "statusPedido")
check("11.3 a name that is not a valid identifier is rejected",
        resolver.fieldNameForType("Status Pedido") == null)
check("11.4 a null type name is rejected", resolver.fieldNameForType(null) == null)

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
