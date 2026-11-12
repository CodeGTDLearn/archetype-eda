/**
 * Behaviour of the snippets with a MALFORMED flag value.
 *
 * The catalog constrains the value with a validationRegex, but the
 * post-processor must not BREAK when the value is outside the domain (an
 * operator typing a coordinate by hand, a flag added without a default, a
 * typo). A snippet that throws aborts the whole post-generation - the
 * project is left half processed.
 *
 * This suite pins:
 *   A. snippet-15 (extra dependencies) survives an absent, 'false', 'null'
 *      or incomplete coordinate, and reports the malformed one;
 *   B. snippet-15 preserves the bytes of the pom it rewrites (UTF-8);
 *   C. snippet-18 (config file format) falls back to the catalog default
 *      for a value outside the domain - the .properties are never lost;
 *   D. snippet-16 (git) logs the truth: no literal '\n' and no false
 *      "renamed" claim.
 */
def h = new GroovyClassLoader(this.class.classLoader)
        .parseClass(new File("src/test/archetype-post-generate/PostProcessorHarness.groovy")).newInstance()

int failures = 0
def check = { String n, boolean c -> if (c) { println "PASS: ${n}" } else { println "FAIL: ${n}"; failures++ } }

def pomOf = { String description ->
    return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<project>\n" +
            "  <modelVersion>4.0.0</modelVersion>\n" +
            "  <groupId>archetype.it</groupId>\n" +
            "  <artifactId>malformed</artifactId>\n" +
            "  <version>0.1-SNAPSHOT</version>\n" +
            "  <description>${description}</description>\n" +
            "  <dependencies>\n" +
            "    <dependency>\n" +
            "      <groupId>org.springframework.boot</groupId>\n" +
            "      <artifactId>spring-boot-starter</artifactId>\n" +
            "    </dependency>\n" +
            "  </dependencies>\n" +
            "</project>\n"
}
def freshPom = { String description ->
    File root = File.createTempDir("snippet-malformed-", "")
    root.deleteOnExit()
    h.seed(root, "pom.xml", pomOf(description))
    return root
}

// ---------------------------------------------------------------------------
// A. snippet-15 survives a malformed coordinate
// ---------------------------------------------------------------------------
["foo", "g:a", "g:a:1", "false", "null", ""].eachWithIndex { String value, int index ->
    File root = freshPom("malformed")
    Throwable thrown = null
    String log = ""
    try {
        log = h.run(root, [extra_pom_dependencies: value], "snippet-15-extra_pom_dependencies.groovy")
    } catch (Throwable t) {
        thrown = t
    }
    check("1.${index + 1} snippet-15 survives the value '${value}'",
            thrown == null)
    if (thrown != null) {
        println "  ${thrown.getClass().getSimpleName()}: ${thrown.message}"
        return
    }
    check("2.${index + 1} snippet-15 does not inject a dependency for the value '${value}'",
            !new File(root, "pom.xml").getText("UTF-8").contains("<groupId>foo</groupId>"))
    check("3.${index + 1} snippet-15 keeps the pom readable for the value '${value}'",
            new File(root, "pom.xml").getText("UTF-8").contains("</project>"))
}

File rootMalformed = freshPom("malformed")
String logMalformed = h.run(rootMalformed, [extra_pom_dependencies: "foo:bar"],
        "snippet-15-extra_pom_dependencies.groovy")
check("4.1 snippet-15 reports the malformed dependency",
        logMalformed.contains("Ignored malformed dependency") && logMalformed.contains("foo:bar"))

// a valid dependency is still injected
File rootValid = freshPom("valid")
h.run(rootValid, [extra_pom_dependencies: "org.slf4j:slf4j-api:1.7.31:runtime"],
        "snippet-15-extra_pom_dependencies.groovy")
check("4.2 snippet-15 still injects a valid dependency",
        new File(rootValid, "pom.xml").getText("UTF-8").contains("<artifactId>slf4j-api</artifactId>"))

// ---------------------------------------------------------------------------
// B. snippet-15 preserves the bytes of the pom it rewrites
// ---------------------------------------------------------------------------
String accented = "Descricao com acentuacao: acao, Relatorio, imoveis"
File rootAccent = freshPom(accented)
h.run(rootAccent, [extra_pom_dependencies: "org.slf4j:slf4j-api:1.7.31:runtime"],
        "snippet-15-extra_pom_dependencies.groovy")
check("5.1 snippet-15 preserves the non-ASCII content of the pom",
        new File(rootAccent, "pom.xml").getText("UTF-8").contains(accented))

// ---------------------------------------------------------------------------
// C. snippet-18 falls back to the catalog default for a value outside the
//    domain: the .properties are never lost.
// ---------------------------------------------------------------------------
["3", "", "null", "properties"].eachWithIndex { String value, int index ->
    File root = File.createTempDir("snippet-filetype-", "")
    root.deleteOnExit()
    h.seed(root, "src/main/resources/application.properties", "a=1")
    h.seed(root, "src/main/resources/application.yml", "a: 1")
    h.run(root, [config_file_format: value], "snippet-18-config_file_format.groovy")
    check("6.${index + 1} snippet-18 keeps the .properties for the value '${value}'",
            new File(root, "src/main/resources/application.properties").isFile())
    check("7.${index + 1} snippet-18 prunes the .yml for the value '${value}'",
            !new File(root, "src/main/resources/application.yml").isFile())
}

// ---------------------------------------------------------------------------
// D. snippet-16 logs the truth
// ---------------------------------------------------------------------------
File rootGit = File.createTempDir("snippet-git-", "")
rootGit.deleteOnExit()
h.seed(rootGit, "git_temp/config", "x")
h.seed(rootGit, "gitignore", "x")
String logGit = h.run(rootGit, [enable_git_support: "true"], "snippet-16-enable_git_support.groovy")
check("8.1 snippet-16 does not write a literal backslash-n in the report",
        !logGit.contains("\\n"))
check("8.2 snippet-16 renames the folder when it can",
        new File(rootGit, ".git").isDirectory() && !new File(rootGit, "git_temp").exists())
check("8.3 snippet-16 renames the gitignore when it can",
        new File(rootGit, ".gitignore").isFile() && !new File(rootGit, "gitignore").exists())

File rootGit2 = File.createTempDir("snippet-git2-", "")
rootGit2.deleteOnExit()
h.seed(rootGit2, "git_temp/config", "x")
h.seed(rootGit2, ".git/config", "x")
h.seed(rootGit2, "gitignore", "x")
String logGit2 = h.run(rootGit2, [enable_git_support: "true"], "snippet-16-enable_git_support.groovy")
check("8.4 snippet-16 reports that an existing .git was NOT renamed",
        logGit2.contains("NOT renamed"))
check("8.5 snippet-16 keeps the git_temp folder when the rename did not happen",
        new File(rootGit2, "git_temp").isDirectory())

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
