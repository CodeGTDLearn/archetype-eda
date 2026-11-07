/**
 * Unit test for ContractTypeResolver - the TYPE INFERENCE component (single
 * responsibility: resolve the Java type of ONE entity attribute).
 *
 * The component under test is the production module:
 *   src/main/pre-processamento-maven-flags-via-llm/domain/ContractTypeResolver.groovy
 *
 * No JUnit infra in this repo; runnable directly:
 *   groovy src/test/pre-processamento-maven-flags-via-llm/domain/ContractTypeResolverTest.groovy
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
check("4.4 a project type declared for an OBJECT value becomes String",
        r4b.type == "String")
check("4.5 ...the qualified type is dropped (no import/POJO)",
        r4b.qualifiedType == null && r4b.projectType == false)
check("4.6 ...and a warning explains the fallback",
        (r4b.warning ?: "").contains("EnderecoCliente") && (r4b.warning ?: "").contains("objeto"))

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
check("6.9 ...and the array fallback is loud",
        (resolver.resolve("items", null, ["a"]).warning ?: "").contains("array"))

// ---------------------------------------------------------------------------
// 7) sanitizeAttributeName: a valid Java identifier that satisfies the
//    catalog regex ^[a-z][a-zA-Z0-9_]*$
// ---------------------------------------------------------------------------
check("7.1 special characters are word boundaries (camelCase)", resolver.sanitizeAttributeName("nome-cachorro") == "nomeCachorro")
check("7.2 a leading digit gets the field_ prefix", resolver.sanitizeAttributeName("9abc") == "field_9abc")
check("7.3 a leading uppercase letter is lowercased", resolver.sanitizeAttributeName("Nome") == "nome")
check("7.4 a name with no valid character is rejected", resolver.sanitizeAttributeName("!!!") == null)
check("7.5 a snake_case name becomes camelCase", resolver.sanitizeAttributeName("nome_ok") == "nomeOk")
check("7.6 an all-uppercase 'ID' collapses to the structural id",
        resolver.sanitizeAttributeName("ID") == "id")
check("7.7 a mixed-case 'iD' collapses to the structural id",
        resolver.sanitizeAttributeName("iD") == "id")
check("7.8 a capitalized 'Id' collapses to the structural id",
        resolver.sanitizeAttributeName("Id") == "id")
check("7.9 a name that only starts with 'id' is untouched",
        resolver.sanitizeAttributeName("identity") == "identity")
check("7.10 a plural name is untouched",
        resolver.sanitizeAttributeName("ids") == "ids")
check("7.11 an ALL_CAPS name becomes camelCase",
        resolver.sanitizeAttributeName("NOME_PRINCIPAL") == "nomePrincipal")
check("7.12 a snake_case name becomes camelCase",
        resolver.sanitizeAttributeName("nome_principal") == "nomePrincipal")
check("7.13 an ALL_CAPS single word is lowercased",
        resolver.sanitizeAttributeName("ENDERECO") == "endereco")
check("7.14 an accented name is ASCII-normalized",
        resolver.sanitizeAttributeName("endereço") == "endereco")
check("7.15 an accented name with two marks is ASCII-normalized",
        resolver.sanitizeAttributeName("opção") == "opcao")
check("7.16 a Java keyword receives the '_' suffix",
        resolver.sanitizeAttributeName("class") == "class_")

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
check("10.5 a project type with a nested object becomes String", r10c.type == "String")
check("10.6 ...is NOT an enum and carries no constant",
        r10c.enumType == false && r10c.enumConstants == [])

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

// ---------------------------------------------------------------------------
// 12) typeNameFor: PascalCase of the key, ASCII-normalized, max 120 chars
// ---------------------------------------------------------------------------
check("12.1 an ALL_CAPS key becomes PascalCase",
        resolver.typeNameFor("ENDERECO") == "Endereco")
check("12.2 an ALL_CAPS snake_case key becomes PascalCase",
        resolver.typeNameFor("NOME_PRINCIPAL") == "NomePrincipal")
check("12.3 an accented key is ASCII-normalized",
        resolver.typeNameFor("endereço") == "Endereco")
check("12.4 an underscore key becomes PascalCase",
        resolver.typeNameFor("endereco_complemento") == "EnderecoComplemento")
check("12.5 an already camelCase key keeps its words",
        resolver.typeNameFor("enderecoCliente") == "EnderecoCliente")
check("12.6 a key starting with a digit is prefixed",
        resolver.typeNameFor("1endereco") == "Type1endereco")
check("12.7 a keyword key receives the 'Type' suffix",
        resolver.typeNameFor("class") == "ClassType")
check("12.8 a key with no valid character is rejected",
        resolver.typeNameFor("###") == null)
check("12.9 the name is limited to 120 chars",
        resolver.typeNameFor("a" * 130).length() == 120)

// ---------------------------------------------------------------------------
// 13) isJavaLangType / isArrayValue / isUppercaseEnumArray
// ---------------------------------------------------------------------------
check("13.1 'String' is a java.lang type", resolver.isJavaLangType("String"))
check("13.2 'java.lang.Integer' is a java.lang type", resolver.isJavaLangType("java.lang.Integer"))
check("13.3 'StatusPedido' is NOT a java.lang type", !resolver.isJavaLangType("StatusPedido"))
check("13.4 a List is an array value", resolver.isArrayValue(["a"]))
check("13.5 a Java array is an array value", resolver.isArrayValue(new String[]{"a"}))
check("13.6 a Map is NOT an array value", !resolver.isArrayValue([a: 1]))
check("13.7 an UPPERCASE array is an enum candidate",
        resolver.isUppercaseEnumArray(["ATIVO", "CANCELADO"]))
check("13.8 a lowercase array is NOT an enum candidate",
        !resolver.isUppercaseEnumArray(["ativo"]))
check("13.9 an empty array is NOT an enum candidate",
        !resolver.isUppercaseEnumArray([]))
check("13.10 a mixed array is NOT an enum candidate",
        !resolver.isUppercaseEnumArray(["ATIVO", 1]))
check("13.11 a single uppercase element is an enum candidate",
        resolver.isUppercaseEnumArray(["SP"]))
check("13.12 a Java array of uppercase strings is an enum candidate",
        resolver.isUppercaseEnumArray(new String[]{"SP", "RJ"}))

// ---------------------------------------------------------------------------
// 14) array + enum in the resolver
// ---------------------------------------------------------------------------
def r14 = resolver.resolve("status", "StatusPedido", ["ATIVO", "CANCELADO"])
check("14.1 a project type + UPPERCASE array is an enum",
        r14.type == "StatusPedido" && r14.enumType == true)
check("14.2 ...the array elements become the constants",
        r14.enumConstants == ["ATIVO", "CANCELADO"])
check("14.3 ...the project type is kept and no warning is raised",
        r14.projectType == true && r14.warning == null)

def r14b = resolver.resolve("status", "StatusPedido", ["ativo", "cancelado"])
check("14.4 a project type + lowercase array becomes String",
        r14b.type == "String" && r14b.projectType == false)
check("14.5 ...and the fallback is loud", (r14b.warning ?: "").contains("array"))

def r14c = resolver.resolve("status", "StatusPedido", ["ativo"], ["ATIVO", "CANCELADO"])
check("14.6 the explicit enumValues win over the array case",
        r14c.type == "StatusPedido" && r14c.enumConstants == ["ATIVO", "CANCELADO"])

def r14d = resolver.resolve("codigos", "Integer", [1, 2])
check("14.7 java.lang + array keeps the declared type",
        r14d.type == "Integer" && r14d.projectType == false)
check("14.8 ...and the array fallback is loud", (r14d.warning ?: "").contains("array"))

def r14e = resolver.resolve("tags", null, ["a", "b"])
check("14.9 no declared type + array becomes String",
        r14e.type == "String" && r14e.warning != null)

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
