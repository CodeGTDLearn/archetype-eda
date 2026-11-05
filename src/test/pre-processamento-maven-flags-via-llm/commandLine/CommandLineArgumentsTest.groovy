def moduleRoot = new File("src/main/pre-processamento-maven-flags-via-llm")
def index = [:]
moduleRoot.eachFileRecurse { File f -> if (f.isFile() && f.name.endsWith('.groovy')) index[f.name] = f }
def gcl = new GroovyClassLoader(this.class.classLoader)
gcl.setResourceLoader({ String cn ->
    def src = index[cn.substring(cn.lastIndexOf('.') + 1) + '.groovy']
    return src == null ? null : src.toURI().toURL()
} as groovy.lang.GroovyResourceLoader)
def parse = gcl.loadClass('eda.postgenerate.pre_processamento_maven_flags_via_llm.commandLine.CommandLineArguments').newInstance().&parse

int failures = 0
def check = { String n, boolean c -> if (c) { println "PASS: ${n}" } else { println "FAIL: ${n}"; failures++ } }

check("1.1 an empty command line parses to an empty map", parse([] as String[]).isEmpty())
check("1.2 the description is captured",
        parse(['--use-case-description', 'create an order'] as String[]).description == 'create an order')
check("1.3 a trailing --use-case-description has no value",
        !parse(['--use-case-description'] as String[]).containsKey('description'))
check("1.4 a following -- token is not swallowed as the description",
        !parse(['--use-case-description', '--use-case-contract'] as String[]).containsKey('description'))
check("1.5 --use-case-contract is a boolean switch",
        parse(['--use-case-contract'] as String[]).get('use-case-contract') == 'true')
check("1.6 the path options are captured",
        parse(['--cache-dir', 'c', '--contract-dir', 'd', '--inference-bin', 'i'] as String[]).subMap(['cache-dir', 'contract-dir', 'inference-bin']) ==
                [ 'cache-dir': 'c', 'contract-dir': 'd', 'inference-bin': 'i' ])
check("1.7 a non-flag token is ignored", parse(['loose'] as String[]).isEmpty())
def unknown = false
try { parse(['--nope'] as String[]) } catch (IllegalArgumentException e) { unknown = e.message.contains('--nope') }
check("1.8 an unknown -- argument is rejected", unknown)

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
