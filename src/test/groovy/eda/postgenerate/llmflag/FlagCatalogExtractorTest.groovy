/**
 * Unit test for the canonical flag catalog (FR-005, FR-013, FR-018).
 *
 * Guards the regression fixed in
 * .specify/bugs/flag-contrato-absoluto-obsoleta/assessment.md:
 *
 *   - the JSON-contract flow flags were removed from the catalog
 *     (absolute_path_jsonfile_contract, enable_json_entity_contracts);
 *   - every <requiredProperty> MUST have a non-empty <defaultValue> (FR-018).
 *     The two empty defaults used to abort the Orchestrator with exit code 4
 *     BEFORE any inference ran - the mechanism was 100% out of operation;
 *   - entity_name_package has a usable, format-valid default ("myentity");
 *   - the catalog exposes exactly 33 flags (32 + entity_contract_path, the
 *     FR-021 contract persistence channel added by
 *     .specify/bugs/contrato-json-inferido-inoperante/).
 *
 * The catalog is read from the REAL property-*.xml fragments, so this test
 * needs no prior build. It also exercises the REAL FlagCatalogExtractor and
 * the REAL Orchestrator catalog loader against the built metadata when
 * target/classes is present.
 *
 * No JUnit infra in this repo; runnable directly:
 *   groovy src/test/groovy/eda/postgenerate/llmflag/FlagCatalogExtractorTest.groovy
 * Exits 0 when every check passes, 1 otherwise.
 */

import groovy.xml.XmlSlurper

def gcl = new GroovyClassLoader(this.class.classLoader)
def catalogEntryClass = gcl.parseClass(new File("src/main/groovy/eda/postgenerate/llmflag/CatalogEntry.groovy"))
def extractorClass = gcl.parseClass(new File("src/main/groovy/eda/postgenerate/llmflag/FlagCatalogExtractor.groovy"))
def orchestratorClass = gcl.parseClass(new File("src/main/groovy/eda/postgenerate/llmflag/Orchestrator.groovy"))

int failures = 0
def check = { String name, boolean cond ->
    if (cond) { println "PASS: ${name}" } else { println "FAIL: ${name}"; failures++ }
}
def skip = { String name, String reason -> println "SKIP: ${name} (${reason})" }

// ----------------------------------------------------------------------
// 1) The REAL catalog, read from the property-*.xml fragments
// ----------------------------------------------------------------------
def propsDir = new File("src/main/resources/META-INF/maven/archetype_metadata_properties")
def slurper = new XmlSlurper()
def realCatalog = new LinkedHashMap()
def emptyDefaults = []

propsDir.listFiles().sort { it.name }.each { File f ->
    if (!f.name.endsWith(".xml")) return
    // The property-*.xml files are XML FRAGMENTS (several <requiredProperty>
    // roots, included by archetype-metadata.xml through XML entities).
    // Strip the BOM + XML declaration and wrap them in a synthetic root, so
    // XmlSlurper can read them standalone.
    def fragment = f.getText("UTF-8").replaceAll(/(?s)^.*?\?>/, "").trim()
    def parsed = slurper.parseText("<catalog>" + fragment + "</catalog>")
    parsed.requiredProperty.each { node ->
        String key = node.@key.text()
        String dflt = node.defaultValue.text()
        String regex = node.validationRegex.text()
        if (dflt == null || dflt.trim().isEmpty()) emptyDefaults << key
        realCatalog.put(key, [defaultValue: dflt, validationRegex: regex, file: f.name])
    }
}

check("1.1 catalog has the 33 flags", realCatalog.size() == 33)
check("1.2 FR-018: no flag has an empty defaultValue", emptyDefaults.isEmpty())
if (!emptyDefaults.isEmpty()) {
    println "       empty defaultValue: " + emptyDefaults.join(", ")
}
check("1.3 absolute_path_jsonfile_contract is gone",
    !realCatalog.containsKey("absolute_path_jsonfile_contract"))
check("1.4 enable_json_entity_contracts is gone",
    !realCatalog.containsKey("enable_json_entity_contracts"))

// FR-021: the contract persistence channel. Its default MUST be non-empty
// (FR-018) and MUST satisfy its own validationRegex.
def contractPath = realCatalog.get("entity_contract_path")
check("1.5 entity_contract_path is present", contractPath != null)
if (contractPath != null) {
    check("1.6 entity_contract_path default is 'none'", contractPath.defaultValue == "none")
    check("1.7 entity_contract_path default matches its own validationRegex",
        contractPath.defaultValue ==~ contractPath.validationRegex)
    check("1.8 entity_contract_path accepts an absolute or relative .json path",
        "C:/tmp/contracts/abc.json" ==~ contractPath.validationRegex &&
        "src/test/resources/projects/x/contract.json" ==~ contractPath.validationRegex)
    check("1.9 entity_contract_path rejects a value that is not a .json path",
        !("C:/tmp/contracts/abc.txt" ==~ contractPath.validationRegex))
}

def pkg = realCatalog.get("entity_name_package")
check("1.5 entity_name_package is present", pkg != null)
if (pkg != null) {
    check("1.6 entity_name_package default is 'myentity'", pkg.defaultValue == "myentity")
    check("1.7 entity_name_package default matches its own validationRegex",
        pkg.defaultValue ==~ pkg.validationRegex)
}

// Every remaining default must satisfy its own validationRegex, otherwise the
// archetype would reject the value the mechanism emits on the command line.
def selfViolating = []
realCatalog.each { k, v ->
    if (v.validationRegex && !(v.defaultValue ==~ v.validationRegex)) selfViolating << k
}
check("1.8 every defaultValue satisfies its own validationRegex", selfViolating.isEmpty())
if (!selfViolating.isEmpty()) {
    println "       self-violating: " + selfViolating.join(", ")
}

// ----------------------------------------------------------------------
// 2) The REAL FlagCatalogExtractor against the REAL fragments
// ----------------------------------------------------------------------
def extractor = extractorClass.newInstance()
def builtMetadata = new File("target/classes/META-INF/maven/archetype-metadata.xml")
if (builtMetadata.exists()) {
    def extracted = extractor.extract(builtMetadata)
    check("2.1 extractor reads the built catalog", extracted != null && extracted.size() == 33)
    check("2.2 extracted catalog has no contract flags",
        !extracted.containsKey("absolute_path_jsonfile_contract") &&
        !extracted.containsKey("enable_json_entity_contracts"))
    check("2.3 extracted entity_name_package default is 'myentity'",
        extracted.get("entity_name_package")?.defaultValue == "myentity")
} else {
    skip("2.1-2.3 extractor against the built catalog",
        "target/classes not built - run 'mvn process-resources' first")
}

// ----------------------------------------------------------------------
// 3) The REAL Orchestrator catalog loader must NOT abort with exit code 4
// ----------------------------------------------------------------------
if (builtMetadata.exists()) {
    def orchestrator = orchestratorClass.newInstance()
    def m = orchestratorClass.getDeclaredMethod("extractCatalog", File)
    m.accessible = true
    def loaded = null
    String thrown = null
    try {
        loaded = m.invoke(orchestrator, builtMetadata)
    } catch (Throwable t) {
        thrown = (t.cause?.message ?: t.message)
    }
    check("3.1 Orchestrator loads the catalog without throwing (no exit code 4)", thrown == null)
    if (thrown != null) {
        println "       thrown: " + thrown
    } else {
        check("3.2 Orchestrator catalog has the 33 flags", loaded?.size() == 33)
    }
} else {
    skip("3.1-3.2 Orchestrator catalog loader", "target/classes not built")
}

// ----------------------------------------------------------------------
// 4) FR-018 negative case: an empty defaultValue MUST be rejected
// ----------------------------------------------------------------------
def tmp = File.createTempFile("catalog-negative-", ".xml")
tmp.deleteOnExit()
tmp.text = '''<?xml version="1.0" encoding="UTF-8"?>
<archetype-descriptor>
  <requiredProperties>
    <requiredProperty key="enable_test_xyz">
      <defaultValue></defaultValue>
      <validationRegex>^(true|false)$</validationRegex>
    </requiredProperty>
  </requiredProperties>
</archetype-descriptor>
'''
String negativeError = null
try {
    extractor.extract(tmp)
} catch (Throwable t) {
    negativeError = (t.cause?.message ?: t.message)
}
check("4.1 FR-018: empty defaultValue is rejected", negativeError != null)
check("4.2 FR-018: the error names the offending flag",
    (negativeError ?: "").contains("enable_test_xyz"))

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
