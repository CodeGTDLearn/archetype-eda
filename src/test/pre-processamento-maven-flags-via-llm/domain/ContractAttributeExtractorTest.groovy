def moduleRoot = new File("src/main/pre-processamento-maven-flags-via-llm")
def index = [:]
moduleRoot.eachFileRecurse { File f -> if (f.isFile() && f.name.endsWith('.groovy')) index[f.name] = f }
def gcl = new GroovyClassLoader(this.class.classLoader)
gcl.setResourceLoader({ String cn ->
    def src = index[cn.substring(cn.lastIndexOf('.') + 1) + '.groovy']
    return src == null ? null : src.toURI().toURL()
} as groovy.lang.GroovyResourceLoader)
def extractor = gcl.loadClass('eda.postgenerate.pre_processamento_maven_flags_via_llm.domain.ContractAttributeExtractor').newInstance()

int failures = 0
def check = { String n, boolean c -> if (c) { println "PASS: ${n}" } else { println "FAIL: ${n}"; failures++ } }

def fromMap = extractor.extract([attributes: [id: 1, name: 'x']], [] as Set)
check("1.1 a MAP of attributes is extracted",
        fromMap.size() == 2 && fromMap[0].name == 'id' && fromMap[0].value == 1)

def fromList = extractor.extract([attributes: [
        [name: 'id', value: 1, type: 'Long', enumValues: ['A']],
        [attribute: 'name', default: 'x'],
        [field: 'active']
]], [] as Set)
check("1.2 LIST entries accept name/attribute/field",
        fromList*.name == ['id', 'name', 'active'])
check("1.3 LIST entries capture value/default/type/enumValues",
        fromList[0].value == 1 && fromList[0].type == 'Long' && fromList[0].enumValues == ['A'] &&
                fromList[1].value == 'x')

def fromStrings = extractor.extract([attributes: ['id', 'name']], [] as Set)
check("1.4 a LIST of strings becomes name-only attributes",
        fromStrings*.name == ['id', 'name'] && fromStrings.every { it.value == null && it.type == null })

def flat = extractor.extract([id: 1, name: 'x', api_port: 8080, entityName: 'E'], ['api_port'] as Set)
check("1.5 a FLAT contract excludes catalog flags and meta keys",
        flat*.name == ['id', 'name'])

check("1.6 a null contract extracts nothing", extractor.extract(null, [] as Set).isEmpty())
check("1.7 the fields[] alias is accepted",
        extractor.extract([fields: [[name: 'id']]], [] as Set).size() == 1)

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
