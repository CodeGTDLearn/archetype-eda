def moduleRoot = new File("src/main/pre-processamento-maven-flags-via-llm")
def index = [:]
moduleRoot.eachFileRecurse { File f -> if (f.isFile() && f.name.endsWith('.groovy')) index[f.name] = f }
def gcl = new GroovyClassLoader(this.class.classLoader)
gcl.setResourceLoader({ String cn ->
    def src = index[cn.substring(cn.lastIndexOf('.') + 1) + '.groovy']
    return src == null ? null : src.toURI().toURL()
} as groovy.lang.GroovyResourceLoader)
def resolver = gcl.loadClass('eda.postgenerate.pre_processamento_maven_flags_via_llm.domain.ContractEntityNameResolver').newInstance()

int failures = 0
def check = { String n, boolean c -> if (c) { println "PASS: ${n}" } else { println "FAIL: ${n}"; failures++ } }

check("1.1 entity_name wins", resolver.resolve([entity_name: 'A'], [:]).entityName == 'A' &&
        resolver.resolve([entity_name: 'A'], [:]).entityNameSource == 'CONTRACT')
check("1.2 entityName is accepted", resolver.resolve([entityName: 'B'], [:]).entityName == 'B')
check("1.3 entity is accepted", resolver.resolve([entity: 'C'], [:]).entityName == 'C')
check("1.4 a blank entity_name falls through to entityName",
        resolver.resolve([entity_name: '  ', entityName: 'D'], [:]).entityName == 'D')
check("1.5 the LLM proposal is the fallback",
        resolver.resolve(null, [entity_name: 'E']).entityName == 'E' &&
                resolver.resolve(null, [entity_name: 'E']).entityNameSource == 'INFERENCE')
check("1.6 no source leaves the name null",
        resolver.resolve(null, null).entityName == null && resolver.resolve([:], [:]).entityName == null)

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
