/**
 * T036 / SC-005 / US1/AC4: the entity<->contract gate is independent of the
 * generated boilerplate.
 *
 * The generated gate compares DECLARED FIELDS by reflection and does a strict
 * Jackson round-trip; it never calls getters/setters and never imports Lombok.
 * Therefore the Lombok-on and Lombok-off builds must reach the same verdict.
 *
 * This suite proves that WITHOUT adding a 23rd fixture to the matrix:
 *   - renders the entity template twice with the entity-contract flags, once
 *     with enable_lombok_support=false and once with true;
 *   - asserts both renders declare the SAME field list, in the same order;
 *   - asserts Lombok-on removes the explicit accessors/constructor and adds
 *     @Data/@NoArgsConstructor/@AllArgsConstructor;
 *   - asserts the generated contract gate templates never depend on Lombok or
 *     on the entity accessors.
 *
 * The entity-contract IT (22/22 matrix) executes the Lombok-off side; this
 * suite proves the Lombok-on side sees the same fields and the same gate.
 */
def resources = new File("src/main/resources/archetype-resources")
def fixtureDir = new File("src/test/resources/projects/entity-contract")
def fixturePropertiesFile = new File(fixtureDir, "archetype.properties")
def catalogFile = new File("target/classes/META-INF/maven/archetype-metadata.xml")

def renderer = new GroovyClassLoader(this.class.classLoader)
        .parseClass(new File("src/test/archetype-post-generate/TemplateRenderer.groovy"))

int failures = 0
def check = { String name, boolean cond ->
    if (cond) {
        println "PASS: ${name}"
    } else {
        println "FAIL: ${name}"
        failures++
    }
}

check("1.0 the built catalog exists", catalogFile.isFile())
check("1.1 the entity-contract fixture exists", fixturePropertiesFile.isFile() && fixtureDir.isDirectory())
if (!catalogFile.isFile() || !fixturePropertiesFile.isFile()) {
    println ""
    println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
    if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
    return
}

// ---------------------------------------------------------------------------
// A. Fixture flags (the same contract, the same entity, only Lombok flips)
// ---------------------------------------------------------------------------
def fixtureProperties = [:]
fixturePropertiesFile.getText("UTF-8").readLines().each { String line ->
    String trimmed = line.trim()
    if (trimmed.isEmpty() || trimmed.startsWith("#")) {
        return
    }
    int equals = trimmed.indexOf("=")
    if (equals > 0) {
        fixtureProperties[trimmed.substring(0, equals).trim()] = trimmed.substring(equals + 1).trim()
    }
}

check("2.1 the IT fixture runs Lombok OFF", fixtureProperties["enable_lombok_support"] == "false")
check("2.2 the IT fixture keeps the test tree and the persisted contract",
        fixtureProperties["enable_folder_src_test_java"] == "true" &&
                fixtureProperties["entity_contract_file_path"] != null &&
                fixtureProperties["entity_contract_file_path"] != "none")

def fixtureDirectories = new File("src/test/resources/projects").listFiles()
        .findAll { File entry -> entry.isDirectory() }
check("2.3 the fixture matrix stays at 22 (no Lombok fixture was added)",
        fixtureDirectories.size() == 22)

// ---------------------------------------------------------------------------
// B. Render the entity with Lombok OFF and ON
// ---------------------------------------------------------------------------
def builtins = [
        package           : "it.pkg",
        packageInPathFormat: "it/pkg",
        groupId           : "archetype.it",
        artifactId        : "entity-contract",
        version           : "0.1-SNAPSHOT"
]
def entityTemplate = new File(resources,
        "src/main/java/__entity_package_name__/__entity_name__.java")
def offFlags = new LinkedHashMap<String, String>(fixtureProperties)
offFlags["enable_lombok_support"] = "false"
def onFlags = new LinkedHashMap<String, String>(fixtureProperties)
onFlags["enable_lombok_support"] = "true"

def offSource = null
def onSource = null
try {
    offSource = renderer.render(entityTemplate, builtins, renderer.flags(offFlags))
    onSource = renderer.render(entityTemplate, builtins, renderer.flags(onFlags))
} catch (Throwable t) {
    check("3.0 the entity template renders in both Lombok modes: ${t.message}", false)
}
if (offSource == null || onSource == null) {
    println ""
    println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
    throw new RuntimeException("${failures} CHECK(S) FAILED")
}

def declaredFields = { String source ->
    source.readLines()
            .collect { String line -> line.trim() }
            .findAll { String line ->
                line ==~ /private\s+[A-Za-z0-9_.$]+\s+[A-Za-z_$][A-Za-z0-9_$]*\s*;/
            }
            .collect { String line ->
                def m = line =~ /^private\s+([A-Za-z0-9_.$]+)\s+([A-Za-z_$][A-Za-z0-9_$]*)\s*;$/
                "${m[0][1]} ${m[0][2]}".toString()
            }
}
def offFields = declaredFields(offSource)
def onFields = declaredFields(onSource)

check("3.1 both renders declare the same fields in the same order", offFields == onFields)
check("3.2 the shared field list is the contract entity", offFields == [
        "Long contractEntityId",
        "String name",
        "Boolean active",
        "it.pkg.contractentity.nested.Endereco endereco",
        "Status status",
        "String tags",
        "Status previousStatus"
])

// ---------------------------------------------------------------------------
// C. Lombok ON removes the boilerplate; Lombok OFF keeps it
// ---------------------------------------------------------------------------
check("4.1 Lombok ON imports and applies @Data/@NoArgsConstructor/@AllArgsConstructor",
        onSource.contains("import lombok.Data;") &&
                onSource.contains("import lombok.NoArgsConstructor;") &&
                onSource.contains("import lombok.AllArgsConstructor;") &&
                onSource.contains("@Data") &&
                onSource.contains("@NoArgsConstructor") &&
                onSource.contains("@AllArgsConstructor"))
check("4.2 Lombok ON has no explicit accessors or constructor",
        !onSource.contains("return this.name;") &&
                !onSource.contains("public ContractEntity(") &&
                !onSource.contains("public String getName()"))
check("4.3 Lombok OFF keeps the explicit accessors and constructors",
        offSource.contains("public String getName()") &&
                offSource.contains("public void setActive(Boolean active)") &&
                offSource.contains("public ContractEntity(") &&
                !offSource.contains("import lombok."))

// ---------------------------------------------------------------------------
// D. The generated gate never depends on Lombok or on entity accessors
// ---------------------------------------------------------------------------
def gateDir = new File(resources,
        "src/test/java/__entity_package_name__/contract")
def gateFiles = gateDir.listFiles().findAll { File entry -> entry.isFile() }
check("5.0 the contract gate templates exist", gateFiles.size() == 8)

def gateSources = gateFiles.collectEntries { File entry -> [entry.name, entry.getText("UTF-8")] }
def lombokUses = gateSources.findAll { String name, String text ->
    text.toLowerCase().contains("lombok")
}.keySet()
check("5.1 no contract gate template uses Lombok: ${lombokUses}", lombokUses.isEmpty())

def accessorPattern = ~/\b(entity|actual)\s*\.\s*(get|set|is)[A-Z][A-Za-z0-9_]*\s*\(/
def accessorUses = gateSources.findAll { String name, String text ->
    accessorPattern.matcher(text).find()
}.keySet()
check("5.2 no contract gate template calls entity accessors: ${accessorUses}", accessorUses.isEmpty())

check("5.3 the contract model reads attributes/fields with the documented precedence",
        gateSources["ContractModel.java"] != null &&
                gateSources["ContractModel.java"].contains('"attributes"') &&
                gateSources["ContractModel.java"].contains('"fields"'))
check("5.4 the entity coverage test reflects on declared fields",
        gateSources["__entity_name__ContractCoverageTest.java"] != null &&
                gateSources["__entity_name__ContractCoverageTest.java"].contains("getDeclaredFields()"))
check("5.5 the conformance test reflects on declared fields",
        gateSources["__entity_name__ContractConformanceTest.java"] != null &&
                gateSources["__entity_name__ContractConformanceTest.java"].contains("getDeclaredFields()"))
check("5.6 the round-trip test is strict Jackson",
        gateSources["ContractRoundTripTest.java"] != null &&
                gateSources["ContractRoundTripTest.java"].contains("FAIL_ON_UNKNOWN_PROPERTIES"))

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) {
    throw new RuntimeException("${failures} CHECK(S) FAILED")
}
