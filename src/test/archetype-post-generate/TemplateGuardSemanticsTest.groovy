/**
 * SEMANTIC test for the Velocity GUARDS of the archetype-resources templates.
 *
 * The existing TemplateFlagConsistencyTest proves that every `enable_*` token
 * of a template is a declared catalog flag. It does NOT prove that the
 * `#if` guard of a template is LIVE: a guard whose condition references an
 * unknown flag is permanently false and the feature silently disappears from
 * the generated project - the project still compiles and the whole suite
 * stays green.
 *
 * This suite renders every template twice - once with the guard satisfied,
 * once with it violated - and asserts that the guard really changes the
 * rendered output.
 *
 * The guard flags are derived from the CONDITION TEXT itself, so a new
 * template or a new guard is covered without touching this suite.
 */
def resources = new File("src/main/resources/archetype-resources")
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

def catalogFlags = new HashSet<String>()
(catalogFile.getText("UTF-8") =~ /<requiredProperty\s+key="([^"]+)"/).each { m -> catalogFlags << m[1] }
def defaults = renderer.catalogDefaults()
def builtins = [package: "it.pkg", groupId: "archetype.it", artifactId: "full", version: "0.1-SNAPSHOT"]

// The sentinel is a value that differs from every literal used by the
// conditions of the templates ("none", "false", "true", "1", "2", "3"...).
def SENTINEL = "Sentinel"

// ---------------------------------------------------------------------------
// Condition parsing
// ---------------------------------------------------------------------------

// One term: the flag it references and the literal it compares against.
def parseTerm = { String term ->
    def m = term =~ /\$\{([A-Za-z_][A-Za-z0-9_]*)\}\s*(==|!=)\s*"([^"]*)"/
    if (!m.matches()) return null
    return [flag: m[0][1], op: m[0][2], value: m[0][3]]
}

// The terms of a condition, split on the top level && and ||.
def parseCondition = { String condition ->
    def orGroups = condition.split("\\|\\|").collect { String group ->
        group.split("&&").collect { String term ->
            parseTerm(term.trim())
        }.findAll { it != null }
    }.findAll { !it.isEmpty() }
    return orGroups
}

// The terms of one flag, grouped by the OR group they belong to.
def termsByFlag = { List orGroups ->
    Map perFlag = [:]
    orGroups.eachWithIndex { List group, int groupIndex ->
        group.each { Map term ->
            List all = (perFlag[term.flag] ?: [])
            all << [term: term, group: groupIndex]
            perFlag[term.flag] = all
        }
    }
    return perFlag
}

// The value that SATISFIES every given term of one flag (null = impossible).
def valueSatisfying = { String flag, List terms ->
    String required = null
    List forbidden = []
    terms.each { Map entry ->
        Map term = entry.term
        if (term.op == "==") {
            if (required == null) required = term.value
            else if (required != term.value) return null
        } else {
            forbidden << term.value
        }
    }
    if (required != null) {
        return forbidden.contains(required) ? null : required
    }
    // only "!=" constraints: a value different from all of them
    String current = defaults[flag]
    if (current != null && !forbidden.contains(current)) return current
    return SENTINEL
}

// The value that VIOLATES every given term of one flag (null = impossible).
def valueViolating = { String flag, List terms ->
    String required = null
    List forbidden = []
    terms.each { Map entry ->
        Map term = entry.term
        if (term.op == "!=") {
            if (required == null) required = term.value
            else if (required != term.value) return null
        } else {
            forbidden << term.value
        }
    }
    if (required != null) {
        return forbidden.contains(required) ? null : required
    }
    // only "==" constraints: a value different from all of them
    String current = defaults[flag]
    if (current != null && !forbidden.contains(current)) return current
    return SENTINEL
}

// The flag set that satisfies the whole condition: the FIRST OR group is
// satisfied, the other groups are violated.
def satisfyingFlags = { List orGroups ->
    Map perFlag = termsByFlag(orGroups)
    Map flags = [:]
    perFlag.each { String flag, List entries ->
        List firstGroupEntries = entries.findAll { it.group == 0 }
        List relevant = firstGroupEntries.isEmpty() ? entries : firstGroupEntries
        String value = valueSatisfying(flag, relevant)
        flags[flag] = value
    }
    return flags
}
// The flag set that violates the whole condition: every OR group is violated.
def violatingFlags = { List orGroups ->
    Map perFlag = termsByFlag(orGroups)
    Map flags = [:]
    perFlag.each { String flag, List entries ->
        flags[flag] = valueViolating(flag, entries)
    }
    return flags
}

// ---------------------------------------------------------------------------
// The guards of every template
// ---------------------------------------------------------------------------
def templates = []
resources.eachFileRecurse { File f ->
    if (!f.isFile()) return
    String text = f.getText("UTF-8")
    if (!text.contains("#if(")) return
    templates << [file: f, text: text]
}

// Every #if condition of every template.
def guards = []
templates.each { entry ->
    (entry.text =~ /#if\s*\(([^)]*)\)/).each { m ->
        String condition = m[1].trim()
        guards << [file: entry.file, condition: condition]
    }
}

check("2.1 the templates carry guards", !guards.isEmpty())

// ---------------------------------------------------------------------------
// A. every flag referenced by a guard is a declared catalog flag
// ---------------------------------------------------------------------------
def unknownGuardFlags = []
guards.each { Map guard ->
    (guard.condition =~ /\$\{([A-Za-z_][A-Za-z0-9_]*)\}/).each { m ->
        String name = m[1]
        if (!catalogFlags.contains(name)) {
            unknownGuardFlags << "${guard.file.name}: \${${name}}"
        }
    }
}
check("2.2 every flag referenced by a guard is a declared catalog flag: ${unknownGuardFlags}",
        unknownGuardFlags.isEmpty())

// ---------------------------------------------------------------------------
// B. every guard is LIVE: flipping its condition changes the rendered output
// ---------------------------------------------------------------------------
def deadGuards = []
int liveGuards = 0
guards.each { Map guard ->
    List orGroups = parseCondition(guard.condition)
    if (orGroups.isEmpty()) return   // a guard over a Velocity local: covered by the alias check
    Map on = satisfyingFlags(orGroups)
    Map off = violatingFlags(orGroups)
    if (on.values().any { it == null } || off.values().any { it == null }) {
        // the condition cannot be satisfied (or cannot be violated) by any
        // flag value: there is nothing to sweep
        return
    }
    String onText
    String offText
    try {
        onText = renderer.render(guard.file, builtins, renderer.flags(on))
        offText = renderer.render(guard.file, builtins, renderer.flags(off))
    } catch (Throwable t) {
        deadGuards << "${guard.file.name}: ${guard.condition} (render falhou: ${t.message})"
        return
    }
    def onLines = onText.readLines() as Set
    def offLines = offText.readLines() as Set
    def added = onLines - offLines
    if (added.isEmpty()) {
        deadGuards << "${guard.file.name}: ${guard.condition}"
    } else {
        liveGuards++
    }
}
check("2.3 every guard changes the rendered output when it is flipped: ${deadGuards}", deadGuards.isEmpty())
check("2.4 the guard sweep covered the guards of the templates (${liveGuards} live)", liveGuards > 0)

// ---------------------------------------------------------------------------
// C. the guards of the critical templates carry the expected content
// ---------------------------------------------------------------------------
def contentExpectations = [
        [file: new File(resources, "src/main/resources/logback-spring.xml"),
         on: [logback_console_type: "2"], marker: "appender-console-tabular.xml"],
        [file: new File(resources, "src/main/resources/logback-spring.xml"),
         on: [logback_console_type: "3"], marker: "appender-console-granular.xml"],
        [file: new File(resources, "src/main/resources/logback-spring.xml"),
         on: [enable_api_cpanel_elk_log_system: "true"], marker: "appender-tcp-elk-logstash.xml"],
        [file: new File(resources, "src/main/resources/logback-spring.xml"),
         on: [enable_logback_appender_file: "true"], marker: "appender-rolfile-file.xml"],
        [file: new File(resources, "src/main/resources/logback-spring.xml"),
         on: [enable_logback_appender_smtp: "true"], marker: "appender-smtp-email.xml"],
        [file: new File(resources, "src/main/resources/application.properties"),
         on: [enable_jwt_authentication: "true"], marker: "jwt.private.key"],
        [file: new File(resources, "src/main/resources/application.properties"),
         on: [enable_folder_src_test_java: "true"], marker: "spring.datasource.url"],
        [file: new File(resources, "src/main/resources/application.yml"),
         on: [enable_jwt_authentication: "true"], marker: "jwt.private.key"],
        [file: new File(resources, "pom.xml"),
         on: [enable_lombok_support: "true"], marker: "lombok"],
        [file: new File(resources, "pom.xml"),
         on: [enable_jwt_authentication: "true"], marker: "oauth2-resource-server"],
        [file: new File(resources, "pom.xml"),
         on: [enable_api_cpanel_elk_log_system: "true", enable_logback_support: "true"],
         marker: "logstash-logback-encoder"],
        [file: new File(resources, "README.md"),
         on: [enable_docker_support: "true"], marker: "Docker Support"]
]
contentExpectations.eachWithIndex { Map expectation, int index ->
    File file = expectation.file
    String marker = expectation.marker
    Map on = expectation.on
    Map off = [:]
    on.each { k, v -> off[k] = (v == "true") ? "false" : "true" }
    String onText = renderer.render(file, builtins, renderer.flags(on))
    String offText = renderer.render(file, builtins, renderer.flags(off))
    check("2.${5 + index} ${file.name} emits '${marker}' only when the guard is satisfied",
            onText.contains(marker) && !offText.contains(marker))
}

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
