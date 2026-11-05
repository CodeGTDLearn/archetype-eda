def moduleRoot = new File("src/main/pre-processamento-maven-flags-via-llm")
def index = [:]
moduleRoot.eachFileRecurse { File f -> if (f.isFile() && f.name.endsWith('.groovy')) index[f.name] = f }
def gcl = new GroovyClassLoader(this.class.classLoader)
gcl.setResourceLoader({ String cn ->
    def src = index[cn.substring(cn.lastIndexOf('.') + 1) + '.groovy']
    return src == null ? null : src.toURI().toURL()
} as groovy.lang.GroovyResourceLoader)
def policy = gcl.loadClass('eda.postgenerate.pre_processamento_maven_flags_via_llm.domain.InferenceEligibilityPolicy').newInstance()

int failures = 0
def check = { String n, boolean c -> if (c) { println "PASS: ${n}" } else { println "FAIL: ${n}"; failures++ } }

check("1.1 entity_name is allowlisted", policy.isEligible('entity_name', null) && policy.isAllowlisted('entity_name'))
check("1.2 a non-allowlisted flag present in the contract is eligible",
        policy.isEligible('api_port', [api_port: '9090']))
check("1.3 a non-allowlisted flag absent from the contract is NOT eligible",
        !policy.isEligible('mutable_binding_properties', [api_port: '9090']))
check("1.4 a flag absent from the contract is not eligible",
        !policy.isEligible('log_level', [api_port: '9090']))
check("1.5 a null contract only allows the allowlist",
        !policy.isEligible('log_level', null) && policy.isEligible('entity_name', null))
check("1.6 isAllowlisted is false for other flags", !policy.isAllowlisted('api_port'))

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
