/**
 * Unit test for ContractReader: stdin ingestion and JSON root validation.
 *
 * Run from repo root:
 *   groovy src/test/pre-processamento-maven-flags-via-llm/contract/ContractReaderTest.groovy
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

def contractReaderClass = gcl.loadClass("eda.postgenerate.pre_processamento_maven_flags_via_llm.contract.ContractReader")
def formatExceptionClass = gcl.loadClass("eda.postgenerate.pre_processamento_maven_flags_via_llm.contract.ContractFormatException")
def reader = contractReaderClass.newInstance()

int failures = 0
def check = { String name, boolean cond ->
    if (cond) { println "PASS: ${name}" } else { println "FAIL: ${name}"; failures++ }
}

// ----------------------------------------------------------------------
// 1) JSON parsing and root validation
// ----------------------------------------------------------------------
def valid = reader.parse('{\n  "entityName": "X"\n}')
check("1.1 a valid object returns a Map",
        valid instanceof Map && valid.get("entityName") == "X")

Throwable rootArray = null
try {
    reader.parse('[1,2,3]')
} catch (Throwable t) {
    rootArray = t
}
check("1.2 an array root throws ContractFormatException",
        formatExceptionClass.isInstance(rootArray))
check("1.3 ...with the explicit message",
        rootArray != null && rootArray.message == "Contract root MUST be a JSON object.")

Throwable rootScalar = null
try {
    reader.parse('"just-a-string"')
} catch (Throwable t) {
    rootScalar = t
}
check("1.4 a scalar root throws ContractFormatException",
        formatExceptionClass.isInstance(rootScalar))

Throwable invalidJson = null
try {
    reader.parse('{invalid')
} catch (Throwable t) {
    invalidJson = t
}
check("1.5 invalid JSON throws a parser exception",
        invalidJson != null && !formatExceptionClass.isInstance(invalidJson))

// ----------------------------------------------------------------------
// 2) stdin ingestion
// ----------------------------------------------------------------------
InputStream originalIn = System.in
try {
    String input = '{\n  "a": 1\n}'
    System.setIn(new ByteArrayInputStream(input.getBytes("UTF-8")))
    String text = reader.readFromStdin()
    String expected = '{' + System.lineSeparator() +
            '  "a": 1' + System.lineSeparator() + '}'
    check("2.1 multiple stdin lines are read and joined", text == expected)

    System.setIn(new ByteArrayInputStream(new byte[0]))
    check("2.2 empty stdin returns an empty string", reader.readFromStdin() == "")
} finally {
    System.setIn(originalIn)
}

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
