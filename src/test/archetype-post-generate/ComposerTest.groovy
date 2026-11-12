/**
 * Unit test for the build-time composer: ArchetypePostGenerateComposer.groovy.
 *
 * The composer is the Maven-side piece of the archetype: it merges the body
 * files, the variables, the generators, the snippets and the imports block into
 * target/classes/META-INF/archetype-post-generate.groovy.
 *
 * This suite asserts the STRUCTURE and the IDEMPOTENCE (no golden master):
 *   - the canonical order of the body sections;
 *   - every variable/generator/snippet is present;
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
def variablesDir = new File("src/main/resources/META-INF/archetype_post_generate_variables")
def generatorsDir = new File("src/main/resources/META-INF/archetype_post_generate_generators")
def snippetsDir = new File("src/main/resources/META-INF/archetype_post_generate_snippets")

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
// 4) Canonical section order (body + one marker per dynamic category)
// ---------------------------------------------------------------------------
def markers = [
        "// BODY-03-LOGGING-VARIABLES.GROOVY",
        "// BODY-04-GLOBAL-VARIABLES.GROOVY",
        "// BODY-05-PATH-ROOT-VARIABLES.GROOVY",
        "// BODY-06-PATH-SRC-MAIN-VARIABLES.GROOVY",
        "// BODY-07-PATH-SRC-TEST-VARIABLES.GROOVY",
        "// BODY-08-PATH-LOGBACK-VARIABLES.GROOVY",
        "// BODY-09-PATH-SECURITY-VARIABLES.GROOVY",
        "// BODY-10-BINDING-ARTIFACT-REGISTRY.GROOVY",
        "// VARIABLES-03-ENABLE_JWT_AUTHENTICATION.GROOVY",
        "// GENERATOR-01-CONTRACT_ENUM_DEFINITIONS.GROOVY",
        "// SNIPPET-01-ENABLE_JWT_AUTHENTICATION.GROOVY",
        "// BODY-11-FOOTER.GROOVY",
        "// BODY-12-FILE-FUNCTIONS.GROOVY",
        "// BODY-13-FOLDER-FUNCTIONS.GROOVY",
        "// BODY-14-JAVA-SOURCE-FUNCTIONS.GROOVY",
        "// BODY-15-ELK-FUNCTIONS.GROOVY"
]
def positions = markers.collect { after.indexOf(it) }
check("4.1 every canonical section marker is present", positions.every { it >= 0 })
check("4.2 the sections are in the canonical order",
        positions.every { it >= 0 } && (0..<positions.size() - 1).every { positions[it] < positions[it + 1] })

// ---------------------------------------------------------------------------
// 5) Every variable, generator and snippet is composed
//    (derived from the directories, no hard-coded count)
// ---------------------------------------------------------------------------
def assets = []
[variablesDir, generatorsDir, snippetsDir].each { dir ->
    dir.listFiles()
            .findAll { it.isFile() && it.name.endsWith(".groovy") }
            .each { assets << it }
}
check("5.1 the post-processor has assets in all three categories",
        !variablesDir.listFiles().findAll { it.name.endsWith(".groovy") }.isEmpty() &&
                !generatorsDir.listFiles().findAll { it.name.endsWith(".groovy") }.isEmpty() &&
                !snippetsDir.listFiles().findAll { it.name.endsWith(".groovy") }.isEmpty())

def missingMarkers = assets.findAll { File f -> !after.contains("// " + f.name.toUpperCase()) }
check("5.2 every variable/generator/snippet carries its marker: ${missingMarkers*.name}",
        missingMarkers.isEmpty())

def MARKER = ~/(?m)^\/\/ ((?:VARIABLES|GENERATOR|SNIPPET)-[A-Z0-9_-]+\.GROOVY)/
def markerOf = { File f ->
    def m = (f.getText("UTF-8") =~ MARKER)
    return m.find() ? m.group(1) : null
}
check("5.3 every asset declares a valid marker line: " +
        assets.findAll { markerOf(it) == null }*.name,
        assets.every { markerOf(it) != null })

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
check("6.4 it declares the java-source helpers",
        after.contains("def String resolveEntityPackage(") && after.contains("def Path writeJavaSourceIfAbsent(") &&
                after.contains("def File resolveExistingFileWalkingUp("))
check("6.5 it declares the single ELK owner",
        after.contains("def String deleteElkArtifacts("))
check("6.6 no Velocity directive leaked into the Groovy output",
        !(after =~ /(?m)^\s*#(?:if|set|end)\b/))

// ---------------------------------------------------------------------------
// 7) BODY drift guard: the numbered directory and the composed output agree.
//    The NN of the body files is DOCUMENTAL (SECTIONS is the authority), so
//    these checks are what keep the two from drifting apart.
// ---------------------------------------------------------------------------
def bodyDir = new File("src/main/resources/META-INF/archetype_post_generate_body")
def bodyFiles = bodyDir.listFiles().findAll { it.isFile() && it.name.endsWith(".groovy") }
def IMPORTS_FILE = "body-01-imports.groovy"

check("7.1 every body file is numbered body-NN-<role>.groovy: " +
        bodyFiles.findAll { !(it.name ==~ /^body-\d{2}-[a-z0-9-]+\.groovy$/) }*.name,
        bodyFiles.every { it.name ==~ /^body-\d{2}-[a-z0-9-]+\.groovy$/ })

def declaredBodyMarkers = bodyFiles
        .findAll { it.name != IMPORTS_FILE }
        .collect { it.name.toUpperCase() }
check("7.2 every declared body file is composed: " +
        declaredBodyMarkers.findAll { !after.contains("// " + it) },
        declaredBodyMarkers.every { after.contains("// " + it) })

def bodyMarkersInOutput = []
(after =~ /(?m)^\/\/ (BODY-(\d{2})-[A-Z0-9-]+\.GROOVY)/).each { m ->
    bodyMarkersInOutput << [marker: m[1], nn: m[2] as int]
}
check("7.3 the composed output carries body markers", !bodyMarkersInOutput.isEmpty())

def orphanBodyMarkers = bodyMarkersInOutput.findAll { entry ->
    !new File(bodyDir, entry.marker.toLowerCase()).isFile()
}
check("7.4 no body marker without a matching file: ${orphanBodyMarkers*.marker}", orphanBodyMarkers.isEmpty())

def bodyOrder = bodyMarkersInOutput*.nn
check("7.5 the body NN sequence is strictly increasing in the output: ${bodyOrder}",
        bodyOrder == bodyOrder.sort() && bodyOrder.toSet().size() == bodyOrder.size())

check("7.6 the prepended imports prelude exists and carries NO marker",
        new File(bodyDir, IMPORTS_FILE).isFile() && !after.contains("BODY-01-IMPORTS"))

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
