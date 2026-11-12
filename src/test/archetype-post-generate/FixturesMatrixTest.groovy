/**
 * Contract test for the INTEGRATION FIXTURE MATRIX.
 *
 * A fixture is one scenario = one point in the flag hypercube. Each fixture
 * declares ~29-32 flags, but only the ones that DIFFER from the catalog default
 * identify the scenario: that set is the fixture SIGNATURE.
 *
 * The signature is declared in the `# FIXTURE SIGNATURE` header of the
 * `archetype.properties` and is read from there; this suite recomputes it from
 * the file + the catalog and fails when the two drift apart. It also proves
 * that every catalog flag is exercised by at least one fixture (or is on the
 * explicit identity/metadata allowlist) and that the index lists every fixture.
 *
 * Without this guard the header is just documentation - and documentation
 * ages (the five pre-existing headers already cited a removed engine).
 */

def catalogFile = new File("target/classes/META-INF/maven/archetype-metadata.xml")
def fixturesDir = new File("src/test/resources/projects")
def indexFile = new File(fixturesDir, "README.md")

// Flags that are NOT part of a fixture signature: they carry identity, free
// text or an environment pin, so they differ from the default without
// describing the scenario.
def EXCLUDED = [
        "project_description",
        "entity_name",
        "entity_package_name",
        "java_version",
        "show_archetype_post_generate_log",
        "api_port"
] as Set

def CLUSTERS = ["core", "binding", "logback", "contract", "nested", "multi"] as Set

// Catalog flags that NO fixture exercises, each with the explicit reason.
// Adding a catalog flag without a fixture (and without an entry here) fails
// check 4.1 on purpose: "not tested" must be a decision, never an accident.
def NOT_EXERCISED = [
        "git_repo_url"         : "remote URL is inert when enable_git_support=false; no verify.groovy reads it",
        "llm_prompt_language"  : "reserved flag: declared and intentionally not consumed (see MetadataContractTest)",
        "project_i18n_language": "reserved flag: declared and intentionally not consumed (see MetadataContractTest)"
]

int failures = 0
def check = { String name, boolean cond ->
    if (cond) { println "PASS: ${name}" } else { println "FAIL: ${name}"; failures++ }
}

check("1.1 the built catalog exists", catalogFile.isFile())
if (!catalogFile.isFile()) {
    println ""
    println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
    if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
    return
}

// ---------------------------------------------------------------------------
// 1) Load the catalog (flag -> defaultValue)
// ---------------------------------------------------------------------------
def defaults = [:]
(catalogFile.getText("UTF-8") =~ /(?s)<requiredProperty\s+key="([^"]+)">\s*<defaultValue>(.*?)<\/defaultValue>/).each { m ->
    defaults[m[1]] = m[2].trim()
}
check("1.2 the catalog declares flags", defaults.size() >= 30)

// ---------------------------------------------------------------------------
// 2) Parse each fixture: properties, signature, cluster
// ---------------------------------------------------------------------------
def fixtures = fixturesDir.listFiles()
        .findAll { it.isDirectory() }
        .sort { it.name }

def parseProperties = { File f ->
    def props = [:]
    f.readLines("UTF-8").each { String line ->
        String t = line.trim()
        if (!t || t.startsWith("#") || !t.contains("=")) { return }
        def kv = t.split("=", 2)
        props[kv[0].trim()] = kv[1].trim()
    }
    return props
}

def parseHeader = { File f ->
    def declared = [:]
    String cluster = null
    boolean hasHeader = false
    boolean inSignature = false
    f.readLines("UTF-8").each { String line ->
        if (line.contains("FIXTURE SIGNATURE")) { hasHeader = true }
        def cm = (line =~ /^# CLUSTER\s*:\s*(\S+)\s*$/)
        if (cm.matches()) { cluster = cm[0][1] }
        if (line.contains("FLAGS UNDER TEST")) { inSignature = true; return }
        if (line.startsWith("# ASSERTS")) { inSignature = false; return }
        if (inSignature) {
            def sm = (line =~ /^#\s{3}([a-z][a-z0-9_]*)=(.*)$/)
            if (sm.matches()) { declared[sm[0][1]] = sm[0][2].trim() }
        }
    }
    return [declared: declared, cluster: cluster, hasHeader: hasHeader]
}

def derivedSignature = { Map props, Map catalog ->
    def sig = [:]
    props.each { String k, String v ->
        if (catalog.containsKey(k) && !EXCLUDED.contains(k) && v != catalog[k]) { sig[k] = v }
    }
    return sig
}

def noProperties = fixtures.findAll { !new File(it, "archetype.properties").isFile() }*.name
check("2.1 every fixture has archetype.properties: ${noProperties}", noProperties.isEmpty())

def noHeader = fixtures.findAll { !parseHeader(new File(it, "archetype.properties")).hasHeader }*.name
check("2.2 every fixture declares the FIXTURE SIGNATURE header: ${noHeader}", noHeader.isEmpty())

def badCluster = fixtures.findAll { File d ->
    def c = parseHeader(new File(d, "archetype.properties")).cluster
    c == null || !CLUSTERS.contains(c)
}.collect { File d -> "${d.name} -> ${parseHeader(new File(d, "archetype.properties")).cluster}" }
check("2.3 every fixture declares a known CLUSTER: ${badCluster}", badCluster.isEmpty())

// ---------------------------------------------------------------------------
// 3) The declared signature MUST equal the derived one
// ---------------------------------------------------------------------------
def drift = []
def exercised = new HashSet<String>()
fixtures.each { File d ->
    def props = parseProperties(new File(d, "archetype.properties"))
    def header = parseHeader(new File(d, "archetype.properties"))
    def derived = derivedSignature(props, defaults)
    exercised.addAll(derived.keySet())
    if (derived != header.declared) {
        def missing = (derived.keySet() - header.declared.keySet()).sort()
        def extra = (header.declared.keySet() - derived.keySet()).sort()
        def valueDrift = derived.findAll { k, v -> header.declared.containsKey(k) && header.declared[k] != v }.collect { k, v -> "${k} declared=${header.declared[k]} derived=${v}" }
        drift << "${d.name} (missing: ${missing} / extra: ${extra} / value: ${valueDrift})"
    }
}
check("3.1 every declared signature matches the derived one: ${drift}", drift.isEmpty())

// ---------------------------------------------------------------------------
// 4) Coverage: every catalog flag is exercised or explicitly excluded
// ---------------------------------------------------------------------------
def unexercised = defaults.keySet().findAll { String k ->
    !exercised.contains(k) && !EXCLUDED.contains(k) && !NOT_EXERCISED.containsKey(k)
}.sort()
check("4.1 every catalog flag is exercised by a fixture or documented as not exercised: ${unexercised}", unexercised.isEmpty())
check("4.2 at least one fixture carries a non-empty signature",
        fixtures.any { File d -> !derivedSignature(parseProperties(new File(d, "archetype.properties")), defaults).isEmpty() })
def staleAllowlist = NOT_EXERCISED.keySet().findAll { String k -> !defaults.containsKey(k) || exercised.contains(k) }.sort()
check("4.3 the NOT_EXERCISED allowlist has no stale entry: ${staleAllowlist}", staleAllowlist.isEmpty())

// ---------------------------------------------------------------------------
// 5) The index lists every fixture
// ---------------------------------------------------------------------------
check("5.1 the index exists: ${indexFile}", indexFile.isFile())
if (indexFile.isFile()) {
    String index = indexFile.getText("UTF-8")
    def missingInIndex = fixtures.findAll { !index.contains("`" + it.name + "/`") }*.name
    check("5.2 the index lists every fixture: ${missingInIndex}", missingInIndex.isEmpty())
    def orphans = CLUSTERS.findAll { c -> !index.contains("### `" + c + "`") }
    check("5.3 the index declares every cluster section: ${orphans}", orphans.isEmpty())
}

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
