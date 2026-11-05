def moduleRoot = new File("src/main/pre-processamento-maven-flags-via-llm")
def index = [:]
moduleRoot.eachFileRecurse { File f -> if (f.isFile() && f.name.endsWith('.groovy')) index[f.name] = f }
def gcl = new GroovyClassLoader(this.class.classLoader)
gcl.setResourceLoader({ String cn ->
    def src = index[cn.substring(cn.lastIndexOf('.') + 1) + '.groovy']
    return src == null ? null : src.toURI().toURL()
} as groovy.lang.GroovyResourceLoader)
def reader = gcl.loadClass('eda.postgenerate.pre_processamento_maven_flags_via_llm.domain.ContractValueReader').newInstance()

int failures = 0
def check = { String n, boolean c -> if (c) { println "PASS: ${n}" } else { println "FAIL: ${n}"; failures++ } }

check("1.1 a string value is returned verbatim", reader.pick('k', [k: 'v']) == 'v')
check("1.2 a number is serialized", reader.pick('k', [k: 42]) == '42')
check("1.3 a boolean is serialized", reader.pick('k', [k: true]) == 'true')
check("1.4 a missing key returns null", reader.pick('k', [:]) == null)
check("1.5 a null contract returns null", reader.pick('k', null) == null)
check("1.6 a map is serialized as JSON", reader.pick('k', [k: [a: 1]]) == '{"a":1}')
check("1.7 a list is serialized as JSON", reader.pick('k', [k: [1, 2]]) == '[1,2]')

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
