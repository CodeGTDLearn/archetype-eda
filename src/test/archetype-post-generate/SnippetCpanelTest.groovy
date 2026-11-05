def h = new GroovyClassLoader(this.class.classLoader).parseClass(new File("src/test/archetype-post-generate/PostProcessorHarness.groovy")).newInstance()
int failures = 0
def check = { String n, boolean c -> if (c) { println "PASS: ${n}" } else { println "FAIL: ${n}"; failures++ } }

def seedAll = { File root ->
    h.seed(root, "src/main/resources/logback-spring-config/appender-tcp-elk-logstash.xml", "x")
    h.seed(root, "ELK/logstash_config/pipeline/x.conf", "x")
    h.seed(root, "docker/elk-compose.yml", "x")
}

def root = File.createTempDir("cpanel-on-", ""); root.deleteOnExit(); seedAll(root)
h.run(root, [enable_logback: "true", enable_api_cpanel_elk_log_system: "false"], "snippet-cpanel.groovy")
check("1.1 logstash appender removed when ELK is off", !new File(root, "src/main/resources/logback-spring-config/appender-tcp-elk-logstash.xml").exists())
check("1.2 ELK folder removed", !new File(root, "ELK").exists())
check("1.3 docker/elk-compose.yml removed", !new File(root, "docker/elk-compose.yml").exists())

def root2 = File.createTempDir("cpanel-keep-", ""); root2.deleteOnExit(); seedAll(root2)
h.run(root2, [enable_logback: "true", enable_api_cpanel_elk_log_system: "true"], "snippet-cpanel.groovy")
check("2.1 ELK kept when enabled", new File(root2, "ELK").isDirectory())
check("2.2 logstash appender kept when enabled", new File(root2, "src/main/resources/logback-spring-config/appender-tcp-elk-logstash.xml").isFile())

def root3 = File.createTempDir("cpanel-logback-off-", ""); root3.deleteOnExit(); seedAll(root3)
h.run(root3, [enable_logback: "false", enable_api_cpanel_elk_log_system: "false"], "snippet-cpanel.groovy")
check("3.1 nothing is touched when logback is off", new File(root3, "ELK").isDirectory())

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
