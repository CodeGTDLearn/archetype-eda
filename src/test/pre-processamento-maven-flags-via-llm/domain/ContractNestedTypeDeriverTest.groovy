/**
 * Unit test for ContractNestedTypeDeriver - the nested-object POJO deriver.
 *
 * The component under test is the production module:
 *   src/main/pre-processamento-maven-flags-via-llm/domain/ContractNestedTypeDeriver.groovy
 *
 * No JUnit infra in this repo; runnable directly:
 *   groovy src/test/pre-processamento-maven-flags-via-llm/domain/ContractNestedTypeDeriverTest.groovy
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
def deriverClass = gcl.loadClass("eda.postgenerate.pre_processamento_maven_flags_via_llm.domain.ContractNestedTypeDeriver")

int failures = 0
def check = { String name, boolean cond ->
    if (cond) { println "PASS: ${name}" } else { println "FAIL: ${name}"; failures++ }
}

def mapOf = { Object... pairs ->
    Map<Object, Object> out = new LinkedHashMap<>()
    for (int i = 0; i < pairs.length; i += 2) out.put(pairs[i], pairs[i + 1])
    return out
}
def newDeriver = { String entityName -> deriverClass.newInstance(resolverClass.newInstance(), entityName) }
def typeNames = { deriver -> deriver.getNestedTypes().collect { it.get("name") } }
def fieldNames = { Map node -> node.get("fields").collect { it.get("name") } }
def fieldTypes = { Map node ->
    Map<String, String> out = new LinkedHashMap<>()
    node.get("fields").each { out.put(it.get("name") as String, it.get("type") as String) }
    return out
}

// ---------------------------------------------------------------------------
// 1) A top-level nested object becomes a POJO
// ---------------------------------------------------------------------------
def d1 = newDeriver("Cliente")
def t1 = d1.derive("endereco", mapOf("logradouro", "Rua A", "numero", 10))
check("1.1 the nested object becomes a POJO", t1 == "Endereco")
check("1.2 ...the deriver knows the type", d1.isNestedType("Endereco"))
check("1.3 ...the fields keep the contract keys",
        fieldNames(d1.getNestedTypes()[0]) == ["logradouro", "numero"])
check("1.4 ...the field types are estimated from the values",
        fieldTypes(d1.getNestedTypes()[0]) == [logradouro: "String", numero: "Integer"])

// ---------------------------------------------------------------------------
// 2) Output order is deterministic: contract order, parent before child
// ---------------------------------------------------------------------------
def d2 = newDeriver("Cliente")
d2.derive("endereco", mapOf("logradouro", "Rua A",
        "complemento", mapOf("numero", 1, "bairro", "Centro")))
d2.derive("contato", mapOf("email", "a@b.com"))
check("2.1 the types are emitted in contract order, parent before child",
        typeNames(d2) == ["Endereco", "Complemento", "Contato"])

// ---------------------------------------------------------------------------
// 3) Empty objects and arrays fall back to String with a warning
// ---------------------------------------------------------------------------
def d3 = newDeriver("Cliente")
def t3 = d3.derive("vazio", [:])
check("3.1 an empty object becomes String", t3 == "String")
check("3.2 ...and is reported", d3.getWarnings().any { it.contains("vazio") })

def d3b = newDeriver("Cliente")
d3b.derive("endereco", mapOf("tags", ["a", "b"]))
check("3.3 an array field becomes String", fieldTypes(d3b.getNestedTypes()[0]).get("tags") == "String")
check("3.4 ...and is reported", d3b.getWarnings().any { it.contains("tags") })

// ---------------------------------------------------------------------------
// 4) The generation limit: 25 is accepted, 26 falls back to String
// ---------------------------------------------------------------------------
def nest = { Map inner, int count ->
    Map value = inner
    for (int i = 0; i < count; i++) {
        Map wrapper = new LinkedHashMap()
        wrapper.put("nivel" + i, value)
        value = wrapper
    }
    return value
}

// 23 wrappers + the root = 24 levels in the success case (root is generation
// 2, so the deepest object is generation 25).
def d4 = newDeriver("Cliente")
String t4 = d4.derive("raiz", nest([leaf: "x"], 23))
check("4.1 a chain that reaches generation 25 is fully materialized", t4 == "Raiz")
check("4.2 ...the contract produced 24 POJOs", typeNames(d4).size() == 24)
check("4.3 ...and the deepest POJO (generation 25) exists", typeNames(d4).contains("Nivel0"))
check("4.4 ...its leaf field is a String",
        fieldTypes(d4.getNestedTypes().last()).get("leaf") == "String")
check("4.5 ...with no generation warning",
        !d4.getWarnings().any { it.contains("limite") })

// 24 wrappers = the deepest object would be generation 26.
def d4b = newDeriver("Cliente")
String t4b = d4b.derive("raiz", nest([leaf: "x"], 24))
check("4.6 a chain that would exceed generation 25 stops at String", t4b == "Raiz")
check("4.7 ...the object at generation 26 is NOT a POJO",
        !typeNames(d4b).contains("Nivel0"))
check("4.8 ...the parent field at the cut is String",
        fieldTypes(d4b.getNestedTypes().last()).get("nivel0") == "String")
check("4.9 ...and the limit is reported", d4b.getWarnings().any { it.contains("limite") })

// ---------------------------------------------------------------------------
// 5) Type-name collisions
// ---------------------------------------------------------------------------
def d5 = newDeriver("Cliente")
check("5.1 a java.lang type name receives the 'Type' suffix",
        d5.derive("string", mapOf("valor", "x")) == "StringType")

def d5b = newDeriver("Cliente")
check("5.2 the entity name is reserved", d5b.derive("cliente", mapOf("nome", "x")) == "ClienteType")

def d5c = newDeriver("Cliente")
check("5.3 a keyword type name receives the 'Type' suffix",
        d5c.derive("class", mapOf("valor", "x")) == "ClassType")

def d5d = newDeriver("Cliente")
d5d.derive("a", mapOf("dados", mapOf("x", 1)))
d5d.derive("b", mapOf("dados", mapOf("y", 2)))
check("5.4 a collision is prefixed with the immediate parent",
        typeNames(d5d) == ["A", "Dados", "B", "BDados"])

def d5e = newDeriver("Cliente")
d5e.derive("a", mapOf("dados", mapOf("x", 1)))
d5e.derive("b", mapOf("dados", mapOf("y", 2)))
d5e.derive("c", mapOf("dados", mapOf("z", 3)))
check("5.5 a repeated collision keeps the parent prefix",
        typeNames(d5e) == ["A", "Dados", "B", "BDados", "C", "CDados"])

def d5f = newDeriver("Cliente")
d5f.derive("endereco", mapOf("x", 1))
d5f.derive("cliente_endereco", mapOf("x", 2))
d5f.derive("endereco", mapOf("x", 3))
d5f.derive("endereco", mapOf("x", 4))
check("5.6 a used BaseType falls back to BaseType and then BaseType2",
        typeNames(d5f) == ["Endereco", "ClienteEndereco", "EnderecoType", "EnderecoType2"])

// ---------------------------------------------------------------------------
// 6) Field-name sanitization and collisions
// ---------------------------------------------------------------------------
def d6 = newDeriver("Cliente")
d6.derive("endereco", mapOf("nome-x", "a", "nome_x", "b", "nome.x", "c"))
check("6.1 the sanitized field keeps the first camelCase spelling",
        fieldNames(d6.getNestedTypes()[0]) == ["nomeX", "nomeX_2", "nomeX_3"])

def d6b = newDeriver("Cliente")
d6b.derive("endereco", mapOf("class", "x", "opção", "y", "NOME_PRINCIPAL", "z"))
check("6.2 a keyword field receives the '_' suffix",
        fieldTypes(d6b.getNestedTypes()[0]).containsKey("class_"))
check("6.3 a non-ASCII field key is ASCII-normalized",
        fieldTypes(d6b.getNestedTypes()[0]).containsKey("opcao"))
check("6.4 an ALL_CAPS field key becomes camelCase",
        fieldTypes(d6b.getNestedTypes()[0]).containsKey("nomePrincipal"))

// ---------------------------------------------------------------------------
// 7) Type-name normalization
// ---------------------------------------------------------------------------
def d7 = newDeriver("Cliente")
check("7.1 an accented key is normalized to ASCII",
        d7.derive("endereço", mapOf("logradouro", "x")) == "Endereco")

def d7b = newDeriver("Cliente")
check("7.2 an underscore key becomes PascalCase",
        d7b.derive("endereco_complemento", mapOf("numero", 1)) == "EnderecoComplemento")

def d7c = newDeriver("Cliente")
check("7.3 a key starting with a digit is prefixed",
        d7c.derive("1endereco", mapOf("numero", 1)) == "Type1endereco")

def d7d = newDeriver("Cliente")
check("7.4 an ALL_CAPS type key becomes PascalCase",
        d7d.derive("ENDERECO", mapOf("numero", 1)) == "Endereco")

def d7e = newDeriver("Cliente")
check("7.5 an ALL_CAPS snake_case type key becomes PascalCase",
        d7e.derive("NOME_PRINCIPAL", mapOf("numero", 1)) == "NomePrincipal")

// ---------------------------------------------------------------------------
// 8) Non-object values are not derived
// ---------------------------------------------------------------------------
def d8 = newDeriver("Cliente")
check("8.1 a non-object value is not derived", d8.derive("endereco", "Rua A") == "String")
check("8.2 ...and no POJO is emitted", d8.getNestedTypes().isEmpty())

def d8b = newDeriver("Cliente")
d8b.derive("endereco", mapOf("vazio", [:]))
check("8.3 an empty object as a field becomes String with a warning",
        fieldTypes(d8b.getNestedTypes()[0]).get("vazio") == "String" &&
                d8b.getWarnings().any { it.contains("vazio") })

def d8c = newDeriver("Cliente")
check("8.4 an object with only invalid field names becomes String",
        d8c.derive("endereco", mapOf("###", "x")) == "String")
check("8.5 ...and no POJO is emitted", d8c.getNestedTypes().isEmpty())
check("8.6 ...and the fallback is reported",
        d8c.getWarnings().any { it.contains("nenhum campo valido") })

def d8d = newDeriver("Cliente")
check("8.7 an invalid type key becomes String",
        d8d.derive("###", mapOf("a", 1)) == "String")
check("8.8 ...and no POJO is emitted", d8d.getNestedTypes().isEmpty())
check("8.9 ...and the invalid name is reported",
        d8d.getWarnings().any { it.contains("nome de tipo invalido") })

def d8e = newDeriver("Cliente")
check("8.10 a null value becomes String without a POJO",
        d8e.derive("endereco", null) == "String" && d8e.getNestedTypes().isEmpty())

// ---------------------------------------------------------------------------
// 9) Reserved project types never collide with a derived POJO
// ---------------------------------------------------------------------------
def d9 = newDeriver("Cliente")
d9.reserveType("br.com.projeto.Endereco")
check("9.1 a reserved project type is not reused",
        d9.derive("endereco", mapOf("logradouro", "x")) == "EnderecoType")

// ---------------------------------------------------------------------------
// 10) Type names are limited to 120 chars and the collision loop terminates
// ---------------------------------------------------------------------------
def d10 = newDeriver("Cliente")
String longKey = "a" * 130
String firstLong = d10.derive(longKey, mapOf("x", 1))
String secondLong = d10.derive(longKey + "b", mapOf("y", 1))
check("10.1 a 130-char key is truncated to 120 chars", firstLong.length() == 120)
check("10.2 ...a colliding long key terminates with a distinct name",
        secondLong != firstLong && secondLong.length() == 120)
check("10.3 ...and both POJOs exist", d10.getNestedTypes().size() == 2)

def d10b = newDeriver("Cliente")
String longBase = "A" + ("a" * 119)
d10b.reserveType("Cliente" + longBase.substring(0, 113))
String firstReserved = d10b.derive("a" * 120, mapOf("x", 1))
String secondReserved = d10b.derive("a" * 120, mapOf("y", 1))
check("10.4 ...the reserved parent-prefix fallback is skipped", firstReserved == longBase)
check("10.5 ...and a reserved parent prefix still terminates",
        secondReserved != firstReserved && secondReserved.length() == 120)
check("10.6 ...and both POJOs exist", d10b.getNestedTypes().size() == 2)

// ---------------------------------------------------------------------------
// 11) A nested UPPERCASE array becomes a nested enum
// ---------------------------------------------------------------------------
def d11 = newDeriver("Cliente")
d11.derive("endereco", mapOf("uf", ["SP", "RJ"]))
check("11.1 a nested UPPERCASE array becomes an enum field",
        fieldTypes(d11.getNestedTypes()[0]).get("uf") == "Uf")
check("11.2 ...and no array warning is raised",
        !d11.getWarnings().any { it.contains("uf") })

def d11b = newDeriver("Cliente")
d11b.derive("endereco", mapOf("uf", ["SP"]))
d11b.derive("entrega", mapOf("uf", ["RJ"]))
check("11.3 a nested enum collision uses the parent prefix",
        fieldTypes(d11b.getNestedTypes()[1]).get("uf") == "EntregaUf")

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
