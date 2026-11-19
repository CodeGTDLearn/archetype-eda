/**
 * Behaviour of the snippets when their flag is ABSENT.
 *
 * An absent flag becomes the string 'null' (the convention the variables
 * layer guarantees). The snippets compare with 'false' or with 'true', so the
 * behaviour of an absent flag was an IMPLICIT decision that no suite
 * asserted: a snippet that removed the artifact on 'false' kept it, while a
 * snippet that removed it on '!= true' removed it.
 *
 * This suite pins the decision: with the flag absent, every snippet is
 * FAIL-OPEN (it keeps the artifact) - the only exception is the config file
 * format, whose fallback is the catalog default (1 = properties).
 */
def h = new GroovyClassLoader(this.class.classLoader)
        .parseClass(new File("src/test/archetype-post-generate/PostProcessorHarness.groovy")).newInstance()

int failures = 0
def check = { String n, boolean c -> if (c) { println "PASS: ${n}" } else { println "FAIL: ${n}"; failures++ } }

def snippetDir = new File("src/main/resources/META-INF/archetype_post_generate_snippets")
def snippets = snippetDir.listFiles().findAll { it.isFile() && it.name.endsWith(".groovy") }.sort { it.name }*.name
check("1.1 the post-processor has 20 snippets", snippets.size() == 20)

// Every artifact a snippet may touch.
def artifacts = [
        "src/main/java/it/pkg/myentity/config/authentication/MyEntitySecurityConfig.java",
        "src/main/resources/security/appJwt.key",
        "src/main/resources/security/appJwt.pub",
        "src/main/java/it/pkg/myentity/config/properties/ApplicationProperties.java",
        "src/main/java/it/pkg/myentity/config/properties/ApplicationPropertiesRecord.java",
        "src/main/java/it/pkg/myentity/config/properties/list_properties/ListObjects.java",
        "src/main/java/it/pkg/myentity/config/properties/list_properties/ListProperties.java",
        "src/main/java/it/pkg/myentity/config/properties/list_properties/ListPropertiesImmutable.java",
        "src/main/java/it/pkg/myentity/config/properties/list_properties/ListPropertiesRecord.java",
        "src/main/java/it/pkg/myentity/config/properties/map_properties/MapItem.java",
        "src/main/java/it/pkg/myentity/config/properties/map_properties/MapProperties.java",
        "src/main/java/it/pkg/myentity/config/properties/map_properties/MapPropertiesImmutable.java",
        "src/main/java/it/pkg/myentity/config/properties/map_properties/MapPropertiesRecord.java",
        "src/main/resources/properties/application_list.properties",
        "src/main/resources/properties/application_list.yml",
        "src/main/resources/properties/application_map.properties",
        "src/main/resources/properties/application_map.yml",
        "src/test/java/it/pkg/myentity/config/properties/ApplicationPropertiesRecordTest.java",
        "src/test/java/it/pkg/myentity/config/properties/ApplicationPropertiesRecordValidationTest.java",
        "src/test/java/it/pkg/myentity/config/properties/ApplicationPropertiesTest.java",
        "src/test/java/it/pkg/myentity/config/properties/ApplicationPropertiesValidationTest.java",
        "src/test/java/it/pkg/myentity/config/properties/list_properties/ListPropertiesTest.java",
        "src/test/java/it/pkg/myentity/config/properties/list_properties/ListPropertiesImmutableTest.java",
        "src/test/java/it/pkg/myentity/config/properties/list_properties/ListPropertiesRecordTest.java",
        "src/test/java/it/pkg/myentity/config/properties/map_properties/MapPropertiesTest.java",
        "src/test/java/it/pkg/myentity/config/properties/map_properties/MapPropertiesImmutableTest.java",
        "src/test/java/it/pkg/myentity/config/properties/map_properties/MapPropertiesRecordTest.java",
        "src/test/resources/properties/application_list_test.properties",
        "src/test/resources/properties/application_list_test.yml",
        "src/test/resources/properties/application_map_test.properties",
        "src/test/resources/properties/application_map_test.yml",
        "src/main/resources/logback-spring.xml",
        "src/main/resources/logback-spring-config/appender-console-tabular.xml",
        "src/main/resources/logback-spring-config/appender-console-layouts-granular.xml",
        "src/main/resources/logback-spring-config/appender-console-granular.xml",
        "src/main/resources/logback-spring-config/appender-rolfile-file.xml",
        "src/main/resources/logback-spring-config/appender-smtp-email.xml",
        "src/main/resources/logback-spring-config/appender-tcp-elk-logstash.xml",
        "src/test/resources/logback-test.xml",
        "ELK/elk.conf",
        "docker/elk-compose.yml",
        "docker/docker-compose.yml",
        "git_temp/config",
        "gitignore",
        "src/test/java/it/pkg/myentity/SomeTest.java",
        "src/test/resources/some.properties",
        "src/main/resources/application.properties",
        "src/main/resources/application.yml"
]

// The ONLY snippet whose absent flag does not keep everything: the config
// file format falls back to the catalog default (1 = properties), so the
// .yml files are pruned and the .properties are kept.
def expectedRemoval = [
        "snippet-18-config_file_format.groovy": [
                "src/main/resources/application.yml",
                "src/main/resources/properties/application_list.yml",
                "src/main/resources/properties/application_map.yml",
                "src/test/resources/properties/application_list_test.yml",
                "src/test/resources/properties/application_map_test.yml"
        ]
]

snippets.each { String name ->
    File root = File.createTempDir("snippet-absent-", "")
    root.deleteOnExit()
    artifacts.each { h.seed(root, it, "x") }

    Throwable thrown = null
    try {
        h.run(root, [:], name)
    } catch (Throwable t) {
        thrown = t
    }
    check("2.${snippets.indexOf(name) + 1} ${name} does not break with the flag absent",
            thrown == null)

    if (thrown != null) {
        println "  ${thrown.getClass().getSimpleName()}: ${thrown.message}"
        return
    }

    def expected = expectedRemoval[name] ?: []
    def removed = artifacts.findAll { !new File(root, it).exists() }
    check("3.${snippets.indexOf(name) + 1} ${name} removes only the expected artifacts with the flag absent: ${removed - expected}",
            (removed - expected).isEmpty())
    check("4.${snippets.indexOf(name) + 1} ${name} removes the expected artifacts with the flag absent: ${expected - removed}",
            (expected - removed).isEmpty())
}

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
