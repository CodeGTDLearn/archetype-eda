/**
 * Contract test for the FLAG CATALOG (Maven/metadata side).
 *
 * Asserts the invariants every flag must satisfy and that no catalog flag is
 * silently dead. `project_language` is the single declared-not-consumed flag
 * (documented for future i18n use) and is explicitly allowlisted here.
 */
def metadata = new File("target/classes/META-INF/maven/archetype-metadata.xml")
def fragmentsDir = new File("src/main/resources/META-INF/maven/archetype_metadata_properties")

int failures = 0
def check = { String name, boolean cond ->
    if (cond) { println "PASS: ${name}" } else { println "FAIL: ${name}"; failures++ }
}

check("1.1 the built catalog exists", metadata.isFile())
if (!metadata.isFile()) {
    println ""
    println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
    if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
    return
}

def text = metadata.getText("UTF-8")
def blocks = [:]
def matcher = (text =~ /(?s)<requiredProperty\s+key="([^"]+)">\s*<defaultValue>(.*?)<\/defaultValue>\s*<validationRegex>(.*?)<\/validationRegex>/)
matcher.each { m -> blocks[m[1]] = [defaultValue: m[2].trim(), regex: m[3].trim()] }

check("1.2 the catalog has 35 flags", blocks.size() == 35)
check("1.3 every flag has a default value", blocks.every { it.value.defaultValue != null })
check("1.4 every flag has a validation regex", blocks.every { it.value.regex != null })

def regexFailures = []
blocks.each { key, value ->
    try {
        if (!(value.defaultValue ==~ value.regex)) regexFailures << key
    } catch (Throwable t) {
        regexFailures << "${key} (regex invalido: ${t.message})"
    }
}
check("1.5 every default value satisfies its own regex: ${regexFailures}", regexFailures.isEmpty())

check("1.6 the 17 metadata fragments exist",
        fragmentsDir.listFiles().findAll { it.name.endsWith(".xml") }.size() == 17)

// ---------------------------------------------------------------------------
// 2) No silently dead flag (declared but referenced nowhere)
// ---------------------------------------------------------------------------
def corpus = new StringBuilder()
[new File("src/main/resources/archetype-resources"),
 new File("src/main/resources/META-INF/archetype_post_generate_body"),
 new File("src/main/resources/META-INF/archetype_post_generate_snippets"),
 new File("src/main/resources/META-INF/archetype_post_generate_variables")].each { dir ->
    dir.eachFileRecurse { File f ->
        if (f.isFile() && f.length() < 300000 && !(f.name ==~ /.*\.(?:key|pub|jar|png|jpg|gif|ico)$/)) {
            try { corpus.append(f.getText("UTF-8")).append("\n") } catch (Throwable ignored) { }
        }
    }
}
def allowlist = ["project_language"]
def unreferenced = blocks.keySet().findAll { key -> !corpus.contains(key) && !allowlist.contains(key) }
check("2.1 no unexpected dead flag: ${unreferenced}", unreferenced.isEmpty())
check("2.2 project_language is still declared (future i18n use)", blocks.containsKey("project_language"))
check("2.3 project_language keeps its documented domain",
        blocks['project_language'].regex == '^(pt|en)$' && blocks['project_language'].defaultValue == 'pt')

// ---------------------------------------------------------------------------
// 3) Every `properties?.X` read by the post-processor is a catalog flag
//    (or the archetype built-in `package`).
// ---------------------------------------------------------------------------
def builtins = ["package"]
def propsRefs = new HashSet<String>()
[new File("src/main/resources/META-INF/archetype_post_generate_body"),
 new File("src/main/resources/META-INF/archetype_post_generate_snippets"),
 new File("src/main/resources/META-INF/archetype_post_generate_variables")].each { dir ->
    dir.eachFileRecurse { File f ->
        if (f.isFile()) {
            (f.getText("UTF-8") =~ /properties\?\.([A-Za-z_][A-Za-z0-9_]*)/).each { m -> propsRefs << m[1] }
        }
    }
}
def unknownProps = propsRefs.findAll { !blocks.containsKey(it) && !builtins.contains(it) }
check("3.1 every properties?.X is a catalog flag: ${unknownProps}", unknownProps.isEmpty())
check("3.2 the post-processor reads at least 20 flags", propsRefs.size() >= 20)

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
