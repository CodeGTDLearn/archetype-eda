def moduleRoot = new File("src/main/pre-processamento-maven-flags-via-llm")
def index = [:]
moduleRoot.eachFileRecurse { File f -> if (f.isFile() && f.name.endsWith('.groovy')) index[f.name] = f }
def gcl = new GroovyClassLoader(this.class.classLoader)
gcl.setResourceLoader({ String cn ->
    def src = index[cn.substring(cn.lastIndexOf('.') + 1) + '.groovy']
    return src == null ? null : src.toURI().toURL()
} as groovy.lang.GroovyResourceLoader)
def formatter = gcl.loadClass('eda.postgenerate.pre_processamento_maven_flags_via_llm.domain.TypeNameListFormatter').newInstance()

int failures = 0
def check = { String n, boolean c -> if (c) { println "PASS: ${n}" } else { println "FAIL: ${n}"; failures++ } }

check("1.1 an empty list formats to an empty string", formatter.format([]) == '')
check("1.2 one attribute formats as Type:name",
        formatter.format([[type: 'String', name: 'name']]) == 'String:name')
check("1.3 several attributes are comma-separated",
        formatter.format([[type: 'Long', name: 'id'], [type: 'String', name: 'name']]) == 'Long:id,String:name')

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
