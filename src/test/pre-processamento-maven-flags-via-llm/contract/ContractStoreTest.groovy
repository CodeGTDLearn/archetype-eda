/**
 * Unit test for ContractStore (FR-023): deterministic materialization, UTF-8
 * content, overwrite behavior and failure handling.
 *
 * Run from repo root:
 *   groovy src/test/pre-processamento-maven-flags-via-llm/contract/ContractStoreTest.groovy
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

def contractStoreClass = gcl.loadClass("eda.postgenerate.pre_processamento_maven_flags_via_llm.contract.ContractStore")
def store = contractStoreClass.newInstance()

int failures = 0
def check = { String name, boolean cond ->
    if (cond) { println "PASS: ${name}" } else { println "FAIL: ${name}"; failures++ }
}

def tmpRoot = new File(System.getProperty("java.io.tmpdir"), "contract-store-test-" + System.nanoTime())
tmpRoot.mkdirs()

try {
    def dir = new File(tmpRoot, "contracts")
    String canonical = '{"a":1}'
    String delivered = '{"a": 1}'

    // ------------------------------------------------------------------
    // 1) Deterministic materialization
    // ------------------------------------------------------------------
    def first = store.materialize(dir.absolutePath, canonical, delivered)
    check("1.1 materialization returns a path", first.path != null)
    check("1.2 materialization has no warning", first.warning == null)
    check("1.3 the materialized file exists", new File(first.path).isFile())
    check("1.4 the content is the contract as delivered",
            new File(first.path).getText("UTF-8") == delivered)
    check("1.5 the path uses forward slashes only", !first.path.contains("\\"))

    def second = store.materialize(dir.absolutePath, canonical, delivered)
    check("1.6 the path is deterministic for the same canonical contract",
            second.path == first.path)

    // ------------------------------------------------------------------
    // 2) Same canonical contract -> same file, content refreshed
    // ------------------------------------------------------------------
    String updated = '{"a":  1}'
    def third = store.materialize(dir.absolutePath, canonical, updated)
    check("2.1 the same canonical contract keeps the same path", third.path == first.path)
    check("2.2 changed delivered content overwrites the file",
            new File(third.path).getText("UTF-8") == updated)

    // ------------------------------------------------------------------
    // 3) Different canonical contract -> different file
    // ------------------------------------------------------------------
    def other = store.materialize(dir.absolutePath, '{"b":2}', '{"b":2}')
    check("3.1 a different canonical contract produces a different path",
            other.path != first.path)

    // ------------------------------------------------------------------
    // 4) Nested target directory is created
    // ------------------------------------------------------------------
    def nestedDir = new File(tmpRoot, "nested/deep/contracts")
    def nested = store.materialize(nestedDir.absolutePath, '{"c":3}', '{"c":3}')
    check("4.1 a nested contract directory is created",
            nested.path != null && new File(nested.path).isFile())

    // ------------------------------------------------------------------
    // 5) Failure is reported, never thrown
    // ------------------------------------------------------------------
    File blocker = new File(tmpRoot, "blocker")
    blocker.text = "not a directory"
    def failed = store.materialize(blocker.absolutePath, '{"d":4}', '{"d":4}')
    check("5.1 a failed materialization returns a null path", failed.path == null)
    check("5.2 ...and a warning instead of an exception",
            failed.warning != null && failed.warning.contains("could not be materialized"))
} finally {
    tmpRoot.deleteDir()
}

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
