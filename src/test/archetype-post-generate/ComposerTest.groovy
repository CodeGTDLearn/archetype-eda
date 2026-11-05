/**
 * Unit test for the build-time composer: ArchetypePostGenerateComposer.groovy.
 *
 * The composer is the Maven-side piece of the archetype: it merges the 11
 * body files, the 11 variables, the 12 snippets and the imports block into
 * target/classes/META-INF/archetype-post-generate.groovy.
 *
 * This suite asserts the STRUCTURE and the IDEMPOTENCE (no golden master):
 *   - the canonical order of the body sections;
 *   - every variable/snippet is present;
 *   - the imports block is consolidated at the top;
 *   - re-running the composer produces byte-identical output;
 *   - the composed script is valid Groovy.
 *
 * Run from repo root:
 *   groovy src/test/archetype-post-generate/ComposerTest.groovy
 */
import java.security.MessageDigest

def composerFile = new File("src/main/groovy/ArchetypePostGenerateComposer.groovy")
def outputFile = new File("target/classes/META-INF/archetype-post-generate.groovy")

int failures = 0
def check = { String name, boolean cond ->
    if (cond) { println "PASS: ${name}" } else { println "FAIL: ${name}"; failures++ }
}
def sha256 = { String text ->
    MessageDigest.getInstance("SHA-256").digest(text.getBytes("UTF-8")).encodeHex().toString()
}

check("1.1 the composer source exists", composerFile.isFile())
check("1.2 the composed output exists (process-resources ran)", outputFile.isFile())

if (!composerFile.isFile() || !outputFile.isFile()) {
    println ""
    println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
    if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
    return
}

def before = outputFile.getText("UTF-8")
def beforeHash = sha256(before)

// ---------------------------------------------------------------------------
// 2) Idempotence: re-running the composer must not change the bytes
// ---------------------------------------------------------------------------
new GroovyShell(this.class.classLoader).evaluate(composerFile)
def after = outputFile.getText("UTF-8")

check("2.1 the composer is idempotent (byte-identical on re-run)", sha256(after) == beforeHash)
check("2.2 the composed output is non-trivial", after.length() > 40000)

// ---------------------------------------------------------------------------
// 3) Imports block: consolidated at the top, no import elsewhere
// ---------------------------------------------------------------------------
def lines = after.readLines()
check("3.1 the output starts with the imports block",
        lines[0] == "import java.nio.file.Path" && lines[1] == "import java.nio.file.Paths")
def importLines = after.findAll(/(?m)^\s*(?:\/\/\s*)?import\s+.*$/)
check("3.2 exactly three import lines survive (one per imports entry)", importLines.size() == 3)
check("3.3 no commented import line survives", !(after =~ /(?m)^\s*\/\/\s*import\s+/))

// ---------------------------------------------------------------------------
// 4) Canonical section order
// ---------------------------------------------------------------------------
def markers = [
        "// ARCHETYPE-GLOBAL-VARIABLES.GROOVY",
        "// ARCHETYPE-PATH-ROOT-VARIABLES.GROOVY",
        "// ARCHETYPE-PATH-SRC-MAIN-VARIABLES.GROOVY",
        "// ARCHETYPE-PATH-SRC-TEST-VARIABLES.GROOVY",
        "// ARCHETYPE-PATH-LOGBACK-VARIABLES.GROOVY",
        "// ARCHETYPE-PATH-SECURITY-VARIABLES.GROOVY",
        "// VARIABLES-AUTHENTICATION.GROOVY",
        "// SNIPPET-AUTHENTICATION.GROOVY",
        "// ARCHETYPE-FOOTER.GROOVY",
        "// ARCHETYPE-FILE-FUNCTIONS.GROOVY",
        "// ARCHETYPE-FOLDER-FUNCTIONS.GROOVY"
]
def positions = markers.collect { after.indexOf(it) }
check("4.1 every canonical section marker is present", positions.every { it >= 0 })
check("4.2 the sections are in the canonical order",
        positions.every { it >= 0 } && (0..<positions.size() - 1).every { positions[it] < positions[it + 1] })

// ---------------------------------------------------------------------------
// 5) Every variable and snippet is composed
// ---------------------------------------------------------------------------
def variables = new File("src/main/resources/META-INF/archetype_post_generate_variables").listFiles().findAll { it.isFile() }
def snippets = new File("src/main/resources/META-INF/archetype_post_generate_snippets").listFiles().findAll { it.isFile() }
check("5.1 the archetype has 11 variables", variables.size() == 11)
check("5.2 the archetype has 12 snippets", snippets.size() == 12)
def markerOf = { File f ->
    def m = (f.getText("UTF-8") =~ /(?m)^\/\/ ((?:VARIABLES|SNIPPET)-[A-Z0-9-]+\.GROOVY)/)
    return m.find() ? m.group(1) : null
}
check("5.3 every variable file is composed",
        variables.every { f -> def marker = markerOf(f); marker != null && after.contains("// " + marker) })
check("5.4 every snippet file is composed",
        snippets.every { f -> def marker = markerOf(f); marker != null && after.contains("// " + marker) })

// ---------------------------------------------------------------------------
// 6) The composed script is valid Groovy and carries the helper functions
// ---------------------------------------------------------------------------
def parses = true
try { new GroovyShell().parse(after) } catch (Throwable t) { parses = false; println "  parse error: ${t.message}" }
check("6.1 the composed script is valid Groovy", parses)
check("6.2 it declares the file helpers",
        after.contains("def deleteFile(") && after.contains("def renameFile(") && after.contains("def copyFile("))
check("6.3 it declares the folder helpers",
        after.contains("def deleteFolder(") && after.contains("def deleteFilesFromListFromFolder(") &&
                after.contains("def deleteFilesByExtensionRecursivelyFromFolder("))
check("6.4 no Velocity directive leaked into the Groovy output",
        !(after =~ /(?m)^\s*#(?:if|set|end)\b/))

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
