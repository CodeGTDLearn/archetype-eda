/**
 * Contract test for the TEMPLATE RESOURCES (archetype-resources).
 *
 * The unit suites of the post-processor prove what the Groovy layer does.
 * This suite proves what the TEMPLATE layer does - and that is where the
 * defects live:
 *
 *   A. every <include resource="..."> of a Logback template resolves to a
 *      real file of the generated project (a typo in the file name makes
 *      Logback skip the appender SILENTLY - the application then starts with
 *      no console appender at all);
 *   B. every ${...} of the Docker files and of the root templates is a
 *      declared catalog flag (an undefined placeholder is rendered LITERALLY
 *      by Velocity and reaches the Dockerfile);
 *   C. every spring.config.import of the four config templates resolves to a
 *      real file - including the multi-line (continuation) form;
 *   D. the .properties <-> .yml parity also covers the application.* pair,
 *      the most critical one;
 *   E. the renderer used by the guard suites is calibrated: it reproduces the
 *      values the archetype plugin renders.
 */
def resources = new File("src/main/resources/archetype-resources")
def mainRoot = new File(resources, "src/main/resources")
def testRoot = new File(resources, "src/test/resources")
def catalogFile = new File("target/classes/META-INF/maven/archetype-metadata.xml")

def renderer = new GroovyClassLoader(this.class.classLoader)
        .parseClass(new File("src/test/archetype-post-generate/TemplateRenderer.groovy"))

int failures = 0
def check = { String name, boolean cond ->
    if (cond) { println "PASS: ${name}" } else { println "FAIL: ${name}"; failures++ }
}

check("1.0 the built catalog exists", catalogFile.isFile())
if (!catalogFile.isFile()) {
    println ""
    println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
    if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
    return
}

// The catalog flags and the generation builtins are the only names the
// templates are allowed to interpolate.
def catalogFlags = new HashSet<String>()
(catalogFile.getText("UTF-8") =~ /<requiredProperty\s+key="([^"]+)"/).each { m -> catalogFlags << m[1] }
def builtins = ["package", "packageInPathFormat", "groupId", "artifactId", "version"]

// ---------------------------------------------------------------------------
// A. every <include resource> resolves to a real file
// ---------------------------------------------------------------------------
def brokenIncludes = []
[mainRoot, testRoot].each { File root ->
    root.eachFileRecurse { File f ->
        if (!f.isFile() || !f.name.endsWith(".xml")) return
        (f.getText("UTF-8") =~ /<include\s+resource="([^"]+)"/).each { m ->
            String resource = m[1]
            boolean found = [mainRoot, testRoot].any { new File(it, resource).isFile() }
            if (!found) brokenIncludes << "${f.name} -> ${resource}"
        }
    }
}
check("1.1 every <include resource> resolves to a real file: ${brokenIncludes}", brokenIncludes.isEmpty())

// ---------------------------------------------------------------------------
// B. every placeholder of the Docker files and of the root templates is a
//    catalog flag (case-sensitive - Velocity does not fold the case) or a
//    generation builtin.
//    Commented lines of the Docker files are skipped: a commented command is
//    never executed, so an undefined placeholder there is documentation.
// ---------------------------------------------------------------------------
def invalidPlaceholders = []
def scanPlaceholders = { File f, boolean skipComments ->
    (f.getText("UTF-8") =~ /\$\{([A-Za-z_][A-Za-z0-9_]*)\}/).each { m ->
        String name = m[1]
        if (!catalogFlags.contains(name) && !builtins.contains(name)) {
            invalidPlaceholders << "${f.name} -> \${${name}}"
        }
    }
}
def dockerDir = new File(resources, "docker")
dockerDir.eachFileRecurse { File f ->
    if (!f.isFile()) return
    // only the ACTIVE lines: a commented line is not executed
    f.readLines("UTF-8").each { String line ->
        String t = line.trim()
        if (t.isEmpty() || t.startsWith("#")) return
        (line =~ /\$\{([A-Za-z_][A-Za-z0-9_]*)\}/).each { m ->
            String name = m[1]
            if (!catalogFlags.contains(name) && !builtins.contains(name)) {
                invalidPlaceholders << "${f.name} -> \${${name}}"
            }
        }
    }
}
scanPlaceholders(new File(resources, "pom.xml"), false)
scanPlaceholders(new File(resources, "README.md"), false)
check("1.2 every placeholder of docker/ and of the root templates is a declared flag: ${invalidPlaceholders}",
        invalidPlaceholders.isEmpty())

// ---------------------------------------------------------------------------
// C. every spring.config.import resolves to a real file (single line AND the
//    continuation form).
// ---------------------------------------------------------------------------
def folderOf = { String placeholder, boolean testScope ->
    if (placeholder == "properties_folder") {
        return testScope ? new File(testRoot, "properties") : new File(mainRoot, "properties")
    }
    return new File(mainRoot, "protected_properties")
}
def logicalLines = { File f ->
    def logical = []
    StringBuffer buffer = new StringBuffer()
    f.readLines("UTF-8").each { String line ->
        String t = line.trim()
        if (t.endsWith("\\")) {
            buffer.append(t[0..-2].trim()).append(" ")
        } else {
            buffer.append(t)
            logical << buffer.toString()
            buffer = new StringBuffer()
        }
    }
    if (buffer.length() > 0) logical << buffer.toString()
    return logical
}
def brokenImports = []
[[mainRoot, false], [testRoot, true]].each { pair ->
    File root = pair[0]
    boolean testScope = pair[1]
    ["application.properties", "application.yml"].each { String name ->
        File template = new File(root, name)
        if (!template.isFile()) return
        logicalLines(template).each { String line ->
            String t = line.trim()
            if (!t.startsWith("spring.config.import")) return
            String value = t.contains("=") ? t.substring(t.indexOf("=") + 1) : ""
            value.split(",").collect { it.trim() }.findAll { it }.each { String entry ->
                def m = entry =~ /\$\{(properties_folder|protected_properties_folder)\}\/(.+)/
                if (!m.matches()) {
                    brokenImports << "${name} -> entrada nao resolvida: ${entry}"
                    return
                }
                File target = new File(folderOf(m[0][1], testScope), m[0][2].trim())
                if (!target.isFile()) {
                    brokenImports << "${name} -> ${entry}"
                }
            }
        }
    }
}
check("1.3 every spring.config.import resolves to a real file: ${brokenImports}", brokenImports.isEmpty())

// ---------------------------------------------------------------------------
// D. the .properties <-> .yml parity ALSO covers the application.* pair.
//
//    The strict list comparison of the existing checkPairs cannot be reused
//    here: a .properties file declares a repeated key while the .yml twin
//    declares an indexed list, and the properties template escapes its
//    comment lines with ${symbol_pound}. The comparison is therefore done on
//    the SET of the leaf names, with the list index stripped.
// ---------------------------------------------------------------------------
def propsLeafKeys = { File f ->
    def keys = new HashSet<String>()
    f.readLines("UTF-8").each { String l ->
        String t = l.trim()
        if (!t || t.startsWith("#")) return
        if (!t.contains("=")) return
        String key = t.split("=", 2)[0].trim()
        // an escaped comment line is not a property
        if (key.startsWith("\${symbol_pound}")) return
        keys << key
    }
    return keys
}
def ymlLeafKeys = { File f ->
    def keys = new HashSet<String>()
    def stack = []
    f.eachLine("UTF-8") { String line ->
        String t = line.replaceAll("\t", "    ")
        String trimmed = t.trim()
        if (trimmed.isEmpty() || trimmed.startsWith("#")) return
        def item = t =~ /^(\s*)-\s+(.*)$/
        boolean isListItem = item.matches()
        String itemIndent = isListItem ? item[0][1].length() : -1
        if (isListItem) t = item[0][1] + item[0][2]
        def m = t =~ /^(\s*)([^:#]+):\s*(.*)$/
        boolean isMapping = m.matches()
        if (isListItem && isMapping) {
            isMapping = (t =~ /^(\s*)([^:#]+):(\s.*)?$/).matches()
        }
        if (!isMapping && !isListItem) return
        int indent = isMapping ? m[0][1].length() : itemIndent
        String key = isMapping ? m[0][2].trim() : ""
        String val = isMapping ? m[0][3].trim() : t.trim()
        while (!stack.isEmpty() && ((int) stack.last()[0]) >= indent) {
            stack.remove(stack.size() - 1)
        }
        if (isListItem && !stack.isEmpty()) {
            def parent = stack.remove(stack.size() - 1)
            String parentKey = parent[1]
            String base = parentKey.contains("[") ? parentKey.substring(0, parentKey.indexOf("[")) : parentKey
            int nextIndex = parentKey.contains("[") ? (parentKey =~ /\[(\d+)\]/)[0][1] as int + 1 : 0
            stack << [parent[0], base + "[" + nextIndex + "]"]
        }
        if (isMapping) {
            stack << [indent, key]
            if (!val.isEmpty()) keys << stack.collect { it[1] }.join(".")
            if (isListItem && !val.isEmpty()) stack.remove(stack.size() - 1)
        } else {
            if (!val.isEmpty()) keys << stack.collect { it[1] }.join(".")
        }
    }
    // the list index is not part of the property NAME
    return keys.collect { it.replaceAll(/\[\d+\]$/, "") } as Set
}
def parityDrift = []
[[mainRoot, "main"], [testRoot, "test"]].each { pair ->
    File root = pair[0]
    String scope = pair[1]
    File props = new File(root, "application.properties")
    File yml = new File(root, "application.yml")
    if (!props.isFile() || !yml.isFile()) {
        parityDrift << "${scope}/application (template ausente)"
        return
    }
    def a = propsLeafKeys(props)
    def b = ymlLeafKeys(yml)
    if (a != b) {
        parityDrift << "${scope}/application (so em .properties: ${(a - b).sort()} / so em .yml: ${(b - a).sort()})"
    }
}
check("1.4 the application.* .properties <-> .yml parity holds: ${parityDrift}", parityDrift.isEmpty())

// ---------------------------------------------------------------------------
// E. the renderer is calibrated against what the plugin renders.
// ---------------------------------------------------------------------------
def generationBuiltins = [package: "it.pkg", groupId: "archetype.it", artifactId: "full", version: "0.1-SNAPSHOT"]
def template = new File(mainRoot, "application.properties")
def renderedOn = renderer.render(template, generationBuiltins,
        renderer.flags([enable_jwt_authentication: "true", api_port: "9090"]))
def renderedOff = renderer.render(template, generationBuiltins,
        renderer.flags([enable_jwt_authentication: "false"]))

check("1.5 the renderer applies the catalog values and the generation builtins",
        renderedOn.contains("server.port=9090") &&
                renderedOn.contains("spring.application.name=full") &&
                renderedOn.contains("spring.application.version=0.1-SNAPSHOT"))
check("1.6 the renderer emits the JWT block when the flag is true",
        renderedOn.contains("jwt.private.key=classpath:security/appJwt.key") &&
                renderedOn.contains("jwt.public.key=classpath:security/appJwt.pub"))
check("1.7 the renderer omits the JWT block when the flag is false",
        !renderedOff.contains("jwt.private.key") && !renderedOff.contains("jwt.public.key"))
def credentials = new File(mainRoot, "protected_properties/admin_credentials.properties")
def renderedCredentials = renderer.render(credentials, generationBuiltins, renderer.flags([:]))
check("1.8 the renderer keeps an undefined placeholder literal (the SECRET contract)",
        renderedCredentials.contains("\${SECRET_ADMIN_PASSWORD}"))

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
