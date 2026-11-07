/**
 * Contract test between the FLAG CATALOG and the VELOCITY TEMPLATES.
 *
 * Catches the class of bug where a template gates a feature on a flag that is
 * not declared in the catalog (e.g. the old
 * `enable_security_authentication_support` in application.yml).
 *
 * Also asserts the JWT feature is gated by the SAME flag in the four config
 * templates (main/test .properties and .yml).
 */
def metadata = new File("target/classes/META-INF/maven/archetype-metadata.xml")
def root = new File("src/main/resources/archetype-resources")
def postGenDirs = [
        new File("src/main/resources/META-INF/archetype_post_generate_body"),
        new File("src/main/resources/META-INF/archetype_post_generate_snippets"),
        new File("src/main/resources/META-INF/archetype_post_generate_variables")
]

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

def catalog = new HashSet<String>()
(metadata.getText("UTF-8") =~ /<requiredProperty\s+key="([^"]+)"/).each { m -> catalog << m[1] }
def builtins = ["package", "groupId", "artifactId", "version", "name", "description"]

def templateFiles = []
root.eachFileRecurse { File f -> if (f.isFile()) templateFiles << f }
postGenDirs.each { dir -> dir.eachFileRecurse { File f -> if (f.isFile()) templateFiles << f } }

// ---------------------------------------------------------------------------
// 2) No reference to the dead flag anywhere
// ---------------------------------------------------------------------------
def deadFlagRefs = templateFiles.findAll { File f -> f.getText("UTF-8").contains("enable_security_authentication_support") }
check("2.1 no template references enable_security_authentication_support: ${deadFlagRefs*.name}",
        deadFlagRefs.isEmpty())

// ---------------------------------------------------------------------------
// 3) Every `enable_*` token is a declared catalog flag
// ---------------------------------------------------------------------------
def unknownEnable = new TreeSet<String>()
templateFiles.each { File f ->
    (f.getText("UTF-8") =~ /\b(enable_[a-z0-9_]+)\b/).each { m ->
        if (!catalog.contains(m[1])) unknownEnable << "${f.name}:${m[1]}"
    }
}
check("3.1 every enable_* token is in the catalog: ${unknownEnable}", unknownEnable.isEmpty())

// ---------------------------------------------------------------------------
// 4) Every Velocity alias `#set($x = "${flag}")` points to a catalog flag
// ---------------------------------------------------------------------------
def badAliases = new TreeSet<String>()
templateFiles.each { File f ->
    (f.getText("UTF-8") =~ /#set\(\s*\$([A-Za-z_][A-Za-z0-9_]*)\s*=\s*"\$\{([A-Za-z_][A-Za-z0-9_]*)\}"\s*\)/).each { m ->
        def source = m[2]
        if (!catalog.contains(source) && !builtins.contains(source) && !source.startsWith("symbol_")) {
            badAliases << "${f.name}:${m[1]}=${source}"
        }
    }
}
check("4.1 every Velocity alias reads a catalog flag: ${badAliases}", badAliases.isEmpty())

// ---------------------------------------------------------------------------
// 4b) The nested-type flag is declared and actually used by the entity
// ---------------------------------------------------------------------------
def entityTemplate = new File(root, "src/main/java/__entity_name_package__/__entity_name__.java")
check("4.2 the nested flag is declared in the catalog",
        catalog.contains("custom_nested_type_definitions"))
check("4.3 the entity template uses the nested flag",
        entityTemplate.isFile() &&
                entityTemplate.getText("UTF-8").contains("custom_nested_type_definitions"))

// ---------------------------------------------------------------------------
// 5) JWT is gated by enable_jwt_authentication in all four config templates
// ---------------------------------------------------------------------------
def configTemplates = [
        "src/main/resources/application.properties",
        "src/main/resources/application.yml",
        "src/test/resources/application.properties",
        "src/test/resources/application.yml"
]
configTemplates.eachWithIndex { String relative, int index ->
    def file = new File(root, relative)
    check("5.${index + 1} ${relative} exists and gates JWT on enable_jwt_authentication",
            file.isFile() && file.getText("UTF-8").contains("enable_jwt_authentication"))
}

// ---------------------------------------------------------------------------
// 6) The YAML block is not orphaned: it declares the same JWT guard as the
//    properties template (parity of the feature gate).
// ---------------------------------------------------------------------------
def mainProps = new File(root, "src/main/resources/application.properties").getText("UTF-8")
def mainYml = new File(root, "src/main/resources/application.yml").getText("UTF-8")
def jwtGuard = "enable_jwt_authentication"
check("6.1 properties and yml use the same JWT guard flag",
        mainProps.contains(jwtGuard) && mainYml.contains(jwtGuard))
check("6.2 the yml emits the jwt.* keys when the guard is true",
        mainYml.contains("jwt.private.key") && mainYml.contains("jwt.public.key"))
check("6.3 the test yml emits the jwt.* keys when the guard is true",
        new File(root, "src/test/resources/application.yml").getText("UTF-8").contains("jwt.public.key"))

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
