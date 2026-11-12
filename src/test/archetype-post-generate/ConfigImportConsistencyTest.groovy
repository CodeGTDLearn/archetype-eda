/**
 * Contract test for the CONFIG TEMPLATES (imports + classpath keys).
 *
 * Catches the class of bug where a template imports a config file that does
 * not exist in the generated tree:
 *   - application.yml / application.yml(test) importing
 *     admin_credentials.yml / authentication_credentials.yml that were never
 *     created (the JWT + YAML startup failure);
 *   - application_keys.* pointing at app.key/app.pub instead of the real
 *     appJwt.key/appJwt.pub (and at backslash paths, which YAML eats as
 *     escapes);
 *   - a .yml/.properties pair drifting apart (different keys).
 *
 * Direction A: every spring.config.import entry of the four config templates
 *              (main/test x yml/properties), in EVERY Velocity branch, must
 *              resolve to a real file in archetype-resources.
 * Direction B: every classpath: reference in the templates and in the
 *              protected_properties files must resolve to a real file.
 * Direction C: a .yml file must mirror the keys of its .properties twin.
 *
 * Run from repo root:
 *   groovy src/test/archetype-post-generate/ConfigImportConsistencyTest.groovy
 * (also executed by the build - see src/test/groovy/run-archetype-post-generate-tests.groovy)
 */

def mainRoot = new File("src/main/resources/archetype-resources/src/main/resources")
def testRoot = new File("src/main/resources/archetype-resources/src/test/resources")

int failures = 0
def check = { String name, boolean cond ->
    if (cond) { println "PASS: ${name}" } else { println "FAIL: ${name}"; failures++ }
}

// ---------------------------------------------------------------------------
// Template inventory: folder placeholder -> concrete directory
// ---------------------------------------------------------------------------
// The test templates import protected_properties from the MAIN resources
// (the generated project's runtime classpath carries main before test).
def folderFor = { boolean testTemplate, String placeholder ->
    if (placeholder == "properties_folder") {
        return testTemplate ? new File(testRoot, "properties") : new File(mainRoot, "properties")
    }
    return new File(mainRoot, "protected_properties")
}

def templates = [
        [file: new File(mainRoot, "application.yml"),       test: false, yml: true],
        [file: new File(mainRoot, "application.properties"), test: false, yml: false],
        [file: new File(testRoot, "application.yml"),        test: true,  yml: true],
        [file: new File(testRoot, "application.properties"), test: true,  yml: false],
]

// ---------------------------------------------------------------------------
// Direction A - every spring.config.import entry resolves to a real file
// ---------------------------------------------------------------------------
def brokenImports = []
templates.each { t ->
    def text = t.file.isFile() ? t.file.getText("UTF-8") : null
    if (text == null) {
        brokenImports << t.file.path + " (template ausente)"
        return
    }
    def regex = t.yml
            ? /^\s*-\s+\$\{(properties_folder|protected_properties_folder)\}\/(.+?)\s*$/
            : /^spring\.config\.import=\$\{(properties_folder|protected_properties_folder)\}\/(.+?)\s*$/
    (text.readLines() =~ regex).each { m ->
        String placeholder = m[0][1]
        String name = m[0][2].trim()
        File dir = folderFor(t.test, placeholder)
        File target = new File(dir, name)
        if (!target.isFile()) {
            brokenImports << t.file.name + " -> " + placeholder + "/" + name
            return
        }
        // The import must match the format of the template (yml -> yml).
        if (t.yml && name.endsWith(".properties")) {
            brokenImports << t.file.name + " (yml importa .properties) -> " + name
        }
        if (!t.yml && name.endsWith(".yml")) {
            brokenImports << t.file.name + " (properties importa .yml) -> " + name
        }
    }
}
check("1.1 every spring.config.import of the 4 templates resolves to a real file: ${brokenImports}",
        brokenImports.isEmpty())

// ---------------------------------------------------------------------------
// Direction B - every classpath: reference resolves to a real file
// ---------------------------------------------------------------------------
def classpathFiles = []
mainRoot.eachFileRecurse { File f -> if (f.isFile()) classpathFiles << f }
testRoot.eachFileRecurse { File f -> if (f.isFile()) classpathFiles << f }

def brokenClasspath = []
def scanClasspath = { File f ->
    def text = f.getText("UTF-8")
    (text =~ /classpath:([^"'\s\\]+)/).each { m ->
        String rel = m[1].replace('\\', '/')
        // Folder declarations (properties_folder: classpath:properties) are
        // not file references: no "/" and no "." in the value.
        if (!rel.contains("/") && !rel.contains(".")) return
        boolean exists = classpathFiles.any { File cf ->
            cf.absolutePath.replace('\\', '/').endsWith("/resources/" + rel)
        }
        if (!exists) brokenClasspath << f.name + " -> classpath:" + rel
    }
    if (text.contains("classpath:") && text.contains("\\")) {
        // A backslash inside a classpath: value is either a broken path or a
        // YAML escape; both are rejected.
        (text.readLines().findAll { it.contains("classpath:") && it.contains("\\") }).each { line ->
            brokenClasspath << f.name + " (barra invertida) -> " + line.trim()
        }
    }
}
templates.each { if (it.file.isFile()) scanClasspath(it.file) }
new File(mainRoot, "protected_properties").listFiles().findAll { it.isFile() }.each { scanClasspath(it) }
check("1.2 every classpath: reference resolves to a real file (and never uses \\): ${brokenClasspath}",
        brokenClasspath.isEmpty())

// ---------------------------------------------------------------------------
// Direction C - the .yml mirrors the keys of its .properties twin
// ---------------------------------------------------------------------------
// Minimal flattening: nested "key:" maps become dotted keys; list items are
// indexed as parent[0], parent[1] (the .properties twin uses objects[0].name
// style keys). Two list shapes exist:
//   - mapping item ("- name: Brazil"): the key is a LEAF, so the sibling keys
//     that follow attach to parent[n] (objects[0].currency, NOT
//     objects[0].name.currency);
//   - scalar item ("- Brazil"): the value IS the entry (parent[0]).
// A colon only opens a mapping when a space follows it, so "- http://host" is
// a scalar item, not a "http" key.
// No YAML dependency is added to the build.
// NOTE: Groovy's List.pop() removes the FIRST element, so the stack uses
// explicit remove(size-1).
def flattenYaml = { File f ->
    def out = []
    def stack = []   // [indent, key]
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
            // YAML only sees a mapping when the colon is followed by a space:
            // "- http://host" is a SCALAR item, not a "http" key.
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
            // The topmost entry is the parent LIST property: index it.
            def parent = stack.remove(stack.size() - 1)
            String parentKey = parent[1]
            String base = parentKey.contains("[")
                    ? parentKey.substring(0, parentKey.indexOf("["))
                    : parentKey
            int nextIndex = parentKey.contains("[")
                    ? (parentKey =~ /\[(\d+)\]/)[0][1] as int + 1
                    : 0
            stack << [parent[0], base + "[" + nextIndex + "]"]
        }
        if (isMapping) {
            stack << [indent, key]
            if (!val.isEmpty()) out << stack.collect { it[1] }.join(".")
            // A list item with a SCALAR value ("- name: Brazil") is a LEAF: it must
            // not become the parent of the sibling keys that follow it. Spring
            // flattens the list as objects[0].name + objects[0].currency, not
            // objects[0].name.currency.
            if (isListItem && !val.isEmpty()) stack.remove(stack.size() - 1)
        } else {
            // A SCALAR list item ("- Brazil"): the indexed parent IS the key.
            if (!val.isEmpty()) out << stack.collect { it[1] }.join(".")
        }
    }
    return out.sort()
}

def propsKeys = { File f ->
    f.readLines("UTF-8")
            .findAll { String l -> def t = l.trim(); t && !t.startsWith("#") && t.contains("=") }
            .collect { it.split("=", 2)[0].trim() }
            .sort()
}

def drifted = []
def checkPairs = { File dir ->
    if (!dir.isDirectory()) return
    dir.listFiles().findAll { it.isFile() && it.name.endsWith(".properties") }.each { File props ->
        File yml = new File(dir, props.name.replace(".properties", ".yml"))
        if (!yml.isFile()) return   // not every .properties has a .yml twin
        def a = propsKeys(props)
        def b = flattenYaml(yml)
        if (a != b) {
            drifted << dir.name + "/" + props.name.replace(".properties", "") +
                    " (só em .properties: " + (a - b) + " / só em .yml: " + (b - a) + ")"
        }
    }
}
checkPairs(new File(mainRoot, "protected_properties"))
checkPairs(new File(mainRoot, "properties"))
checkPairs(new File(testRoot, "properties"))
check("1.3 every .yml mirror has the SAME keys as its .properties twin: ${drifted}",
        drifted.isEmpty())

// ---------------------------------------------------------------------------
// 2) The YAML JWT credentials files EXIST (the regression of the missing
//    admin_credentials.yml / authentication_credentials.yml imports)
// ---------------------------------------------------------------------------
def protectedDir = new File(mainRoot, "protected_properties")
check("2.1 admin_credentials.yml exists",
        new File(protectedDir, "admin_credentials.yml").isFile())
check("2.2 authentication_credentials.yml exists",
        new File(protectedDir, "authentication_credentials.yml").isFile())
check("2.3 application_keys.yml points at the REAL key files (appJwt.pub/appJwt.key, forward slashes)",
        new File(protectedDir, "application_keys.yml").getText("UTF-8").contains("classpath:security/appJwt.pub") &&
                new File(protectedDir, "application_keys.yml").getText("UTF-8").contains("classpath:security/appJwt.key"))

println ""
if (failures == 0) { println "ALL CHECKS PASSED" } else { println "${failures} CHECK(S) FAILED" }
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
