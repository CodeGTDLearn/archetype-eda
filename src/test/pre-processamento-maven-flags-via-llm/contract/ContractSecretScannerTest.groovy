def moduleRoot = new File("src/main/pre-processamento-maven-flags-via-llm")
def index = [:]
moduleRoot.eachFileRecurse { File f -> if (f.isFile() && f.name.endsWith('.groovy')) index[f.name] = f }
def gcl = new GroovyClassLoader(this.class.classLoader)
gcl.setResourceLoader({ String cn ->
    def src = index[cn.substring(cn.lastIndexOf('.') + 1) + '.groovy']
    return src == null ? null : src.toURI().toURL()
} as groovy.lang.GroovyResourceLoader)
def scanner = gcl.loadClass('eda.postgenerate.pre_processamento_maven_flags_via_llm.contract.ContractSecretScanner').newInstance()

int failures = 0
def check = { String n, boolean c -> if (c) { println "PASS: ${n}" } else { println "FAIL: ${n}"; failures++ } }

check("1.1 a clean contract has no suspicious key",
        scanner.scan([entityName: 'X', attributes: [[name: 'id', value: 1]]]).isEmpty())
check("1.2 a password key is detected",
        scanner.scan([password: 'x']).contains('password'))
check("1.3 the detection is case-insensitive",
        scanner.scan([API_KEY: 'x']).contains('API_KEY'))
check("1.4 nested keys use a dotted path",
        scanner.scan([credentials: [user: 'u', password: 'p']]).contains('credentials.password'))
check("1.5 list elements use an indexed path",
        scanner.scan([login: [[token: 't']]]).contains('login[0].token'))
check("1.6 a secret is reported once per path",
        scanner.scan([secret: 'a', other: [secret: 'b']]).size() == 2)
check("1.7 a scalar root is tolerated", scanner.scan('plain').isEmpty())

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
