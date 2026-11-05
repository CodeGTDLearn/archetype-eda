/**
 * Unit test for CliOutput: stdout/stderr routing of the command-line surface.
 *
 * Run from repo root:
 *   groovy src/test/pre-processamento-maven-flags-via-llm/presentation/CliOutputTest.groovy
 * (also executed by the build - see src/test/groovy/run-maven-flags-via-llm-tests.groovy)
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

def cliOutputClass = gcl.loadClass("eda.postgenerate.pre_processamento_maven_flags_via_llm.presentation.CliOutput")

int failures = 0
def check = { String name, boolean cond ->
    if (cond) { println "PASS: ${name}" } else { println "FAIL: ${name}"; failures++ }
}

def newOutput = {
    def outBytes = new ByteArrayOutputStream()
    def errBytes = new ByteArrayOutputStream()
    def out = new PrintStream(outBytes, true, "UTF-8")
    def err = new PrintStream(errBytes, true, "UTF-8")
    def cli = cliOutputClass.newInstance(out, err)
    return [cli: cli, outBytes: outBytes, errBytes: errBytes]
}

def text = { ByteArrayOutputStream bytes -> new String(bytes.toByteArray(), "UTF-8") }
String nl = System.lineSeparator()

// ----------------------------------------------------------------------
// 1) stdout / stderr routing
// ----------------------------------------------------------------------
def a = newOutput()
a.cli.commandLine("mvn archetype:generate --batch-mode -Dapi_port=8080")
String aOut = text(a.outBytes)
String aErr = text(a.errBytes)
check("1.1 commandLine writes to stdout", aOut.contains("mvn archetype:generate"))
check("1.2 commandLine does not write to stderr", !aErr.contains("mvn archetype:generate"))
check("1.3 commandLine ends with a newline", aOut.endsWith(nl))

def b = newOutput()
b.cli.reportHeader(true)
String bErr = text(b.errBytes)
String bOut = text(b.outBytes)
check("1.4 reportHeader(HIT) writes the HIT header to stderr",
        bErr.contains("## Relatorio de Proveniencia - cache: HIT"))
check("1.5 reportHeader leaves a blank line after the header", bErr.endsWith(nl + nl))
check("1.6 reportHeader does not write to stdout", bOut.isEmpty())

def c = newOutput()
c.cli.reportHeader(false)
check("1.7 reportHeader(MISS) writes the MISS header to stderr",
        text(c.errBytes).contains("## Relatorio de Proveniencia - cache: MISS"))

def d = newOutput()
d.cli.reportBody("| flag | valor final | fonte | notas |")
check("1.8 reportBody writes to stderr only",
        text(d.errBytes).contains("| flag | valor final | fonte | notas |") &&
        text(d.outBytes).isEmpty())

def e = newOutput()
e.cli.warning("WARNING: test")
check("1.9 warning writes to stderr only",
        text(e.errBytes).contains("WARNING: test") && text(e.outBytes).isEmpty())

def f = newOutput()
f.cli.error("ERROR: test")
check("1.10 error writes to stderr only",
        text(f.errBytes).contains("ERROR: test") && text(f.outBytes).isEmpty())

def g = newOutput()
g.cli.info("Catalog: 35 flags")
check("1.11 info writes to stdout only",
        text(g.outBytes).contains("Catalog: 35 flags") && text(g.errBytes).isEmpty())

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
