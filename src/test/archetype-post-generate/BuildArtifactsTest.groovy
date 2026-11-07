/**
 * Unit test for the MAVEN side of the archetype build.
 *
 * Asserts the artifacts produced by the build lifecycle (generate-resources /
 * process-resources) and the pom wiring that produces them. Runs in the
 * `test` phase, after process-resources.
 */
def metadata = new File("target/classes/META-INF/maven/archetype-metadata.xml")
def composed = new File("target/classes/META-INF/archetype-post-generate.groovy")
def pom = new File("pom.xml")

int failures = 0
def check = { String name, boolean cond ->
    if (cond) { println "PASS: ${name}" } else { println "FAIL: ${name}"; failures++ }
}

check("1.1 archetype-metadata.xml exists", metadata.isFile())
check("1.2 archetype-post-generate.groovy exists", composed.isFile())
check("1.3 pom.xml exists", pom.isFile())

if (!metadata.isFile() || !composed.isFile() || !pom.isFile()) {
    println ""
    println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
    if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
    return
}

def metadataText = metadata.getText("UTF-8")
def pomText = pom.getText("UTF-8")

// ---------------------------------------------------------------------------
// 2) Catalog artifact
// ---------------------------------------------------------------------------
check("2.1 the descriptor root is archetype-descriptor",
        metadataText.contains("<archetype-descriptor"))
check("2.2 the identity transform removed the DOCTYPE", !metadataText.contains("<!DOCTYPE"))
check("2.3 the catalog has 36 required properties",
        (metadataText =~ /<requiredProperty\s+key=/).count == 36)
check("2.4 the catalog carries 36 default values",
        (metadataText =~ /<defaultValue>/).count == 36)

// ---------------------------------------------------------------------------
// 3) Composed post-processor artifact
// ---------------------------------------------------------------------------
check("3.1 the composed script is non-trivial", composed.length() > 40000)
def composes = true
try { new GroovyShell().parse(composed.getText("UTF-8")) } catch (Throwable t) { composes = false }
check("3.2 the composed script is valid Groovy", composes)
check("3.3 the composed script is Groovy (no Velocity directive)",
        !(composed.getText("UTF-8") =~ /(?m)^\s*#(?:if|set|end)\b/))

// ---------------------------------------------------------------------------
// 4) pom wiring (the Maven pipeline that produces the artifacts above)
// ---------------------------------------------------------------------------
check("4.1 the archetype packaging extension is declared",
        pomText.contains("archetype-packaging"))
check("4.2 the xml-maven-plugin transform is wired", pomText.contains("xml-maven-plugin") &&
        pomText.contains("<goal>transform</goal>"))
check("4.3 the composer execution is wired (process-resources)",
        pomText.contains("archetype-post-generate-composer-execute"))
check("4.4 the pre-processing test runner is wired (test)",
        pomText.contains("maven-flags-via-llm-test-runner"))
check("4.5 the post-generate test runner is wired (test)",
        pomText.contains("archetype-post-generate-test-runner"))
check("4.6 surefire is restricted to **/*Test.java", pomText.contains("**/*Test.java"))

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
