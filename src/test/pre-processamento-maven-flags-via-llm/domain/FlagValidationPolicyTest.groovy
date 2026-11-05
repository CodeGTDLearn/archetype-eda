def moduleRoot = new File("src/main/pre-processamento-maven-flags-via-llm")
def index = [:]
moduleRoot.eachFileRecurse { File f -> if (f.isFile() && f.name.endsWith('.groovy')) index[f.name] = f }
def gcl = new GroovyClassLoader(this.class.classLoader)
gcl.setResourceLoader({ String cn ->
    def src = index[cn.substring(cn.lastIndexOf('.') + 1) + '.groovy']
    return src == null ? null : src.toURI().toURL()
} as groovy.lang.GroovyResourceLoader)
def entryClass = gcl.loadClass('eda.postgenerate.pre_processamento_maven_flags_via_llm.catalog.CatalogEntry')
def policy = gcl.loadClass('eda.postgenerate.pre_processamento_maven_flags_via_llm.domain.FlagValidationPolicy').newInstance()
def entry = entryClass.newInstance('api_port', '8080', '^[0-9]+$', '')

int failures = 0
def check = { String n, boolean c -> if (c) { println "PASS: ${n}" } else { println "FAIL: ${n}"; failures++ } }

def valid = policy.validate(entry, '9090')
check("1.1 a valid value is accepted", !valid.rejected && valid.value == '9090')
def invalid = policy.validate(entry, 'abc')
check("1.2 an invalid value falls back to the default", invalid.rejected && invalid.value == '8080')
check("1.3 the note mentions FR-006", invalid.note.contains('FR-006'))
check("1.4 the error names the flag and the regex",
        invalid.error.contains("'api_port'") && invalid.error.contains("'abc'"))
check("1.5 a null value is accepted", !policy.validate(entry, null).rejected)
def derived = policy.validateDerived(entry, 'xyz')
check("1.6 the derived rejection carries a derived note",
        derived.rejected && derived.note.contains('derivado'))

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
