/**
 * Unit test for every post-processor VARIABLES file.
 *
 * The suite is DERIVED from the directory: every `variables-NN-<flag>.groovy`
 * must bind the catalog flag of the same name to the local of the same name,
 * and a missing property must become the string "null" (the convention the
 * snippets rely on). There is no hard-coded count: adding a flag only requires
 * adding the fragment.
 *
 * Run from repo root:
 *   groovy src/test/archetype-post-generate/VariablesTest.groovy
 * (also executed by the build - see src/test/groovy/run-archetype-post-generate-tests.groovy)
 */
def dir = new File("src/main/resources/META-INF/archetype_post_generate_variables")
def metadata = new File("target/classes/META-INF/maven/archetype-metadata.xml")

int failures = 0
def check = { String n, boolean c -> if (c) { println "PASS: ${n}" } else { println "FAIL: ${n}"; failures++ } }

def catalog = new HashSet<String>()
if (metadata.isFile()) {
    (metadata.getText("UTF-8") =~ /<requiredProperty\s+key="([^"]+)"/).each { m -> catalog << m[1] }
}
check("0.1 the built catalog exists", metadata.isFile())

def files = dir.listFiles().findAll { it.isFile() && it.name.endsWith(".groovy") }.sort { it.name }
check("0.2 there is at least one variable fragment", !files.isEmpty())

def NAMING = ~/^variables-(\d{2})-([a-z][a-z0-9_]*)\.groovy$/
def flagOf = { File f ->
    def m = NAMING.matcher(f.name)
    return m.matches() ? m.group(2) : null
}

check("0.3 every variable file follows variables-NN-<flag>.groovy: " +
        files.findAll { flagOf(it) == null }*.name,
        files.every { flagOf(it) != null })

def notInCatalog = files.findAll { flagOf(it) != null && !catalog.contains(flagOf(it)) }
check("0.4 every variable flag is declared in the catalog: ${notInCatalog*.name}", notInCatalog.isEmpty())

// The file must bind the local of the SAME name and read properties?.<flag>.
def badBinding = files.findAll { File f ->
    String flag = flagOf(f)
    if (flag == null) return false
    String text = f.getText("UTF-8")
    return !text.contains("def ${flag} = \"\${properties?.${flag}}\"")
}
check("0.5 every variable binds its flag to the local of the same name: ${badBinding*.name}",
        badBinding.isEmpty())

// ---------------------------------------------------------------------------
// 1) Values are mapped, and a missing property becomes the string "null"
// ---------------------------------------------------------------------------
def evaluate = { File f, String flag, Map<String, String> props ->
    def properties = new Properties()
    props.each { k, v -> properties.put(k, v) }
    def binding = new Binding()
    binding.setVariable("properties", properties)
    def captureScript = "captured = [${flag}: ${flag}]"
    new GroovyShell(binding).evaluate(f.getText("UTF-8") + "\n" + captureScript)
    return (Map) binding.getVariable("captured")
}

def unmapped = []
def notNulled = []
files.each { File f ->
    String flag = flagOf(f)
    if (flag == null) return

    def mapped = evaluate(f, flag, [(flag): "sample-${flag}"])
    if (mapped[flag] != "sample-${flag}") unmapped << f.name

    def missing = evaluate(f, flag, [:])
    if (missing[flag] != "null") notNulled << f.name
}
check("1.1 every variable maps its flag value: ${unmapped}", unmapped.isEmpty())
check("1.2 a missing property becomes the string 'null': ${notNulled}", notNulled.isEmpty())

// ---------------------------------------------------------------------------
// 2) One fragment per flag: no duplicates, no gaps in the sequence
// ---------------------------------------------------------------------------
def flags = files.collect { flagOf(it) }.findAll { it != null }
check("2.1 no duplicated flag across variable fragments", flags.size() == flags.toSet().size())

def numbers = files.findAll { flagOf(it) != null }.collect { (it.name =~ NAMING)[0][1] as int }.sort()
check("2.2 the NN sequence is strictly increasing and unique",
        numbers == numbers.toSet().sort() && numbers.every { it >= 2 && it <= 36 })

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
