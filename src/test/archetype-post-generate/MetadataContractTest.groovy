/**
 * Contract test for the FLAG CATALOG (Maven/metadata side).
 *
 * Asserts the invariants every flag must satisfy and that no catalog flag is
 * silently dead. `project_i18n_language` and `llm_prompt_language` are the
 * declared-not-consumed flags (documented for future use) and are explicitly
 * allowlisted here.
 */
def metadata = new File("target/classes/META-INF/maven/archetype-metadata.xml")
def fragmentsDir = new File("src/main/resources/META-INF/maven/archetype_metadata_required_properties")

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

check("1.6 the 35 metadata fragments exist (one per flag)",
        fragmentsDir.listFiles().findAll { it.name.endsWith(".xml") }.size() == 35)

// ---------------------------------------------------------------------------
// 1b) The AGGREGATOR resolves: every DOCTYPE entity points to a real fragment
//     and is actually expanded. Guards the rename/prune wave of
//     src/main/resources/META-INF/maven (the catalog used to build is the
//     transformed one; a dangling SYSTEM target would fail the transformation
//     with an obscure parser error).
// ---------------------------------------------------------------------------
def aggregator = new File("src/main/resources/META-INF/maven/archetype-metadata.xml")
check("1.7 the aggregator exists", aggregator.isFile())

def entityTargets = [:]
if (aggregator.isFile()) {
    (aggregator.getText("UTF-8") =~ /<!ENTITY\s+([A-Za-z0-9_.-]+)\s+SYSTEM\s+"([^"]+)">/).each { m ->
        entityTargets[m[1]] = m[2]
    }
}
check("1.8 the aggregator declares entities", !entityTargets.isEmpty())

def missingFragments = entityTargets.findAll { name, path ->
    !new File(aggregator.parentFile, path).isFile()
}.collect { name, path -> path }
check("1.9 every entity target exists on disk: ${missingFragments}", missingFragments.isEmpty())

def aggregatorText = aggregator.isFile() ? aggregator.getText("UTF-8") : ""
def unusedEntities = entityTargets.keySet().findAll { name ->
    !(aggregatorText =~ ("&" + name + ";")).find()
}
check("1.10 every declared entity is expanded: ${unusedEntities}", unusedEntities.isEmpty())

// ---------------------------------------------------------------------------
// 2) No silently dead flag (declared but referenced nowhere)
// ---------------------------------------------------------------------------
// NOTE: archetype_post_generate_variables/ is intentionally NOT part of the
// corpus: that folder IS the binding layer (one file per consumed flag), so it
// would make every flag look "referenced". CoverageRegistryTest 4.5/4.6 hold
// the real 1:1 relation between readers and bindings.
def corpus = new StringBuilder()
[new File("src/main/resources/archetype-resources"),
 new File("src/main/resources/META-INF/archetype_post_generate_body"),
 new File("src/main/resources/META-INF/archetype_post_generate_snippets"),
 new File("src/main/resources/META-INF/archetype_post_generate_generators")].each { dir ->
    dir.eachFileRecurse { File f ->
        if (f.isFile() && f.length() < 300000 && !(f.name ==~ /.*\.(?:key|pub|jar|png|jpg|gif|ico)$/)) {
            try { corpus.append(f.getText("UTF-8")).append("\n") } catch (Throwable ignored) { }
        }
    }
}
def allowlist = ["project_i18n_language", "llm_prompt_language"]
def unreferenced = blocks.keySet().findAll { key -> !corpus.contains(key) && !allowlist.contains(key) }
check("2.1 no unexpected dead flag: ${unreferenced}", unreferenced.isEmpty())
check("2.2 project_i18n_language is still declared (future i18n use)", blocks.containsKey("project_i18n_language"))
check("2.3 project_i18n_language keeps its documented domain",
        blocks['project_i18n_language'].regex == '^(pt|en)$' && blocks['project_i18n_language'].defaultValue == 'pt')

// ---------------------------------------------------------------------------
// 3) Every `properties?.X` read by the post-processor is a catalog flag
//    (or the archetype built-in `package`).
// ---------------------------------------------------------------------------
def builtins = ["package"]
def propsRefs = new HashSet<String>()
[new File("src/main/resources/META-INF/archetype_post_generate_body"),
 new File("src/main/resources/META-INF/archetype_post_generate_snippets"),
 new File("src/main/resources/META-INF/archetype_post_generate_variables"),
 new File("src/main/resources/META-INF/archetype_post_generate_generators")].each { dir ->
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
