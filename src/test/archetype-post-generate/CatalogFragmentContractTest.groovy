/**
 * Contract test for the CATALOG FRAGMENTS (property-<NN>-<flag>.xml).
 *
 * The existing MetadataContractTest reads the BUILT catalog (the XML the
 * archetype plugin consumes) and counts the fragments. It never opens a
 * fragment: a fragment whose key does not match its file name, a fragment
 * that declares no default, or a DEPENDS_ON that no fixture respects would
 * pass the whole build.
 *
 * This suite opens every fragment and asserts:
 *   A. it parses and declares key + defaultValue + validationRegex;
 *   B. its NN is contiguous and matches the key inside;
 *   C. it has an entity in the aggregator (1:1, in both directions);
 *   D. the DEPENDS_ON it declares is respected by the integration fixtures;
 *   E. the flags that no fragment reads are an explicit allowlist;
 *   F. the catalog default crosses the binding layer unchanged.
 */
def fragmentDir = new File("src/main/resources/META-INF/maven/archetype_metadata_required_properties")
def aggregator = new File("src/main/resources/META-INF/maven/archetype-metadata.xml")
def builtCatalog = new File("target/classes/META-INF/maven/archetype-metadata.xml")
def variablesDir = new File("src/main/resources/META-INF/archetype_post_generate_variables")
def fixturesDir = new File("src/test/resources/projects")

int failures = 0
def check = { String name, boolean cond ->
    if (cond) { println "PASS: ${name}" } else { println "FAIL: ${name}"; failures++ }
}

check("1.0 the built catalog exists", builtCatalog.isFile())
if (!builtCatalog.isFile()) {
    println ""
    println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
    if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
    return
}

def catalogDefaults = [:]
(builtCatalog.getText("UTF-8") =~ /<requiredProperty\s+key="([^"]+)"\s*>\s*<defaultValue>(.*?)<\/defaultValue>/).each { m ->
    catalogDefaults[m[1]] = m[2]
}
def catalogFlags = new HashSet<String>(catalogDefaults.keySet())

// ---------------------------------------------------------------------------
// A. every fragment parses and declares the three contract fields
// ---------------------------------------------------------------------------
def fragments = fragmentDir.listFiles().findAll { it.isFile() && it.name.endsWith(".xml") }.sort { it.name }
def parsed = [:]
def malformed = []
fragments.each { File f ->
    try {
        def xml = new XmlSlurper().parse(f)
        String key = xml.'@key'.toString()
        String defaultValue = xml.defaultValue.text()
        String regex = xml.validationRegex.text()
        parsed[f.name] = [key: key, default: defaultValue, regex: regex]
        if (!key || defaultValue == null || defaultValue.isEmpty() || !regex) {
            malformed << "${f.name} (key='${key}', default='${defaultValue}', regex='${regex}')"
        }
    } catch (Throwable t) {
        malformed << "${f.name}: ${t.message}"
    }
}
check("1.1 every fragment parses and declares key + defaultValue + validationRegex: ${malformed}",
        malformed.isEmpty())
check("1.2 the catalog declares 35 fragments", fragments.size() == 35)

// ---------------------------------------------------------------------------
// B. the NN is contiguous and matches the key inside the fragment
// ---------------------------------------------------------------------------
def NAMING = ~/^property-(\d{2})-([a-z][a-z0-9_]*)\.xml$/
def keyMismatch = fragments.findAll { File f ->
    def m = NAMING.matcher(f.name)
    !m.matches() || parsed[f.name]?.key != m.group(2)
}
check("1.3 every fragment is named property-<NN>-<key>.xml and the key matches: ${keyMismatch*.name}",
        keyMismatch.isEmpty())
def numbers = fragments.collect { (it.name =~ NAMING)[0][1] as int }.sort()
check("1.4 the NN sequence is contiguous and duplicate-free: ${numbers}",
        numbers == numbers.toSet().sort() && numbers == (numbers.first()..numbers.last()).toList())

// ---------------------------------------------------------------------------
// C. every fragment has an entity in the aggregator (1:1, both directions)
// ---------------------------------------------------------------------------
def aggregatorText = aggregator.getText("UTF-8")
def declaredEntities = []
(aggregatorText =~ /<!ENTITY\s+(property-[A-Za-z0-9_.-]+)\s+SYSTEM\s+"([^"]+)"/).each { m ->
    declaredEntities << [name: m[1], path: m[2]]
}
check("1.5 the aggregator declares one entity per fragment", declaredEntities.size() == fragments.size())
def entityWithoutFragment = declaredEntities.findAll { e ->
    !new File(aggregator.parentFile, e.path).isFile()
}
check("1.6 every declared entity points at a real fragment: ${entityWithoutFragment*.name}",
        entityWithoutFragment.isEmpty())
def fragmentWithoutEntity = fragments.findAll { File f ->
    !declaredEntities.any { it.path.endsWith("/" + f.name) }
}
check("1.7 every fragment on disk has an entity in the aggregator: ${fragmentWithoutEntity*.name}",
        fragmentWithoutEntity.isEmpty())

// ---------------------------------------------------------------------------
// D. the DEPENDS_ON of every fragment is respected by the fixtures
// ---------------------------------------------------------------------------
def dependsOn = [:]
fragments.each { File f ->
    String text = f.getText("UTF-8")
    def m = text =~ /DEPENDS_ON\s*:\s*(.+)/
    dependsOn[f.name] = m ? m[0][1].trim() : null
}

// The fixtures that DELIBERATELY exercise the negative case of a DEPENDS_ON:
// the child flag is requested on purpose to prove that the post-processor
// does NOT apply it (the fixture's verify.groovy asserts the absence).
def allowedNegative = ["custom-attributes": ["entity_contract_file_path"]]

def violations = []
fixturesDir.listFiles().findAll { it.isDirectory() }.each { File dir ->
    File propsFile = new File(dir, "archetype.properties")
    if (!propsFile.isFile()) return
    Map values = [:]
    propsFile.readLines("UTF-8").each { String l ->
        String t = l.trim()
        if (!t || t.startsWith("#") || !t.contains("=")) return
        String[] parts = t.split("=", 2)
        values[parts[0].trim()] = parts[1].trim()
    }
    // the catalog default applies to the absent flags
    Map effective = new LinkedHashMap(catalogDefaults)
    values.each { k, v -> effective[k] = v }

    dependsOn.each { String fragment, String constraint ->
        if (!constraint) return
        def m = constraint =~ /([a-z_][a-z0-9_]*)\s*=\s*(true|false)/
        if (!m.find()) return          // a prose constraint is not machine-readable
        String parent = m[0][1]
        String required = m[0][2]
        String child = parsed[fragment]?.key
        if (child == null) return
        String childValue = effective[child]
        // The constraint binds only when the operator asked for something
        // OTHER than the catalog default: the default value is inert by
        // definition (it is the value the archetype applies when the feature
        // is not requested).
        if (childValue == null) return
        if (childValue == catalogDefaults[child]) return
        // the catalog also accepts these values as "nothing requested"
        if (childValue.isEmpty() || childValue == "none" || childValue == "false") return
        if ((allowedNegative[dir.name] ?: []).contains(child)) return
        if (effective[parent] != required) {
            violations << "${dir.name}: ${child} exige ${parent}=${required} (encontrado ${effective[parent]})"
        }
    }
}
check("1.8 no fixture violates the DEPENDS_ON of a flag: ${violations}", violations.isEmpty())

// ---------------------------------------------------------------------------
// E. the flags that no fragment reads are an explicit allowlist
// ---------------------------------------------------------------------------
def readers = []
["archetype_post_generate_body", "archetype_post_generate_snippets",
 "archetype_post_generate_generators", "archetype_post_generate_variables"].each { String dir ->
    new File("src/main/resources/META-INF", dir).eachFileRecurse { File f ->
        if (f.isFile() && f.name.endsWith(".groovy")) readers << f
    }
}
def readFlags = new TreeSet<String>()
readers.each { File f ->
    (f.getText("UTF-8") =~ /properties\?\.([A-Za-z_][A-Za-z0-9_]*)/).each { m -> readFlags << m[1] }
}
def deadFlags = (catalogFlags - readFlags).sort()
// the flags the TEMPLATES consume (Velocity), not the post-processor
def templateFlags = new TreeSet<String>()
new File("src/main/resources/archetype-resources").eachFileRecurse { File f ->
    if (!f.isFile()) return
    (f.getText("UTF-8") =~ /\$\{([a-z_][a-z0-9_]*)\}/).each { m -> templateFlags << m[1] }
}
def allowedDead = ["llm_prompt_language", "project_i18n_language"]
def unexpectedDead = (deadFlags - templateFlags - allowedDead).sort()
check("1.9 every flag that neither the post-processor nor the templates read is an explicit allowlist entry: ${unexpectedDead}",
        unexpectedDead.isEmpty())

// ---------------------------------------------------------------------------
// F. the catalog default crosses the binding layer unchanged
// ---------------------------------------------------------------------------
def evaluateVariable = { File f, String flag, Map<String, String> props ->
    def properties = new Properties()
    props.each { k, v -> properties.put(k, v) }
    def binding = new Binding()
    binding.setVariable("properties", properties)
    def captureScript = "captured = [${flag}: ${flag}]"
    new GroovyShell(binding).evaluate(f.getText("UTF-8") + "\n" + captureScript)
    return (Map) binding.getVariable("captured")
}
def driftedDefaults = []
variablesDir.listFiles().findAll { it.isFile() && it.name.endsWith(".groovy") }.each { File f ->
    def m = (f.name =~ /^variables-\d{2}-([a-z][a-z0-9_]*)\.groovy$/)
    if (!m.matches()) return
    String flag = m[0][1]
    String expected = catalogDefaults[flag]
    if (expected == null) return
    Map captured = evaluateVariable(f, flag, catalogDefaults)
    if (captured[flag] != expected) {
        driftedDefaults << "${f.name}: '${captured[flag]}' != default '${expected}'"
    }
}
check("1.10 the catalog default reaches the post-processor unchanged: ${driftedDefaults}", driftedDefaults.isEmpty())

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
