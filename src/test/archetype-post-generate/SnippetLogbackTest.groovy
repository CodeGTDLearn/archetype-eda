def h = new GroovyClassLoader(this.class.classLoader).parseClass(new File("src/test/archetype-post-generate/PostProcessorHarness.groovy")).newInstance()
int failures = 0
def check = { String n, boolean c -> if (c) { println "PASS: ${n}" } else { println "FAIL: ${n}"; failures++ } }

def cfg = "src/main/resources/logback-spring-config"
def files = ["appender-console-granular.xml", "appender-console-layouts-granular.xml",
             "appender-console-tabular.xml", "appender-rolfile-file.xml",
             "appender-smtp-email.xml", "appender-tcp-elk-logstash.xml"]
def seed = { File root ->
    h.seed(root, "src/main/resources/logback-spring.xml", "x")
    h.seed(root, "src/test/resources/logback-test.xml", "x")
    files.each { h.seed(root, "${cfg}/${it}", "x") }
    h.seed(root, "ELK/x", "x")
    h.seed(root, "docker/elk-compose.yml", "x")
}
def fresh = { String p -> def r = File.createTempDir(p, ""); r.deleteOnExit(); seed(r); r }
def exists = { File r, String rel -> new File(r, rel).exists() }

def r1 = fresh("logback-off-")
h.run(r1, [enable_logback: "false"], "snippet-logback.groovy")
check("1.1 logback=false removes the spring config folder", !exists(r1, cfg))
check("1.2 logback=false removes logback-spring.xml", !exists(r1, "src/main/resources/logback-spring.xml"))
check("1.3 logback=false removes logback-test.xml", !exists(r1, "src/test/resources/logback-test.xml"))
check("1.4 logback=false removes ELK and elk-compose", !exists(r1, "ELK") && !exists(r1, "docker/elk-compose.yml"))

def r2 = fresh("logback-console1-")
h.run(r2, [enable_logback: "true", logback_console_type: "1",
           enable_logback_appender_smtp: "false", enable_logback_appender_file: "false",
           enable_api_cpanel_elk_log_system: "false"], "snippet-logback.groovy")
check("2.1 console=1 removes the granular/tabular consoles",
        !exists(r2, "${cfg}/appender-console-granular.xml") &&
                !exists(r2, "${cfg}/appender-console-layouts-granular.xml") &&
                !exists(r2, "${cfg}/appender-console-tabular.xml"))
check("2.2 console=1 removes the test logback", !exists(r2, "src/test/resources/logback-test.xml"))
check("2.3 console=1 without appenders/ELK removes logback-spring.xml",
        !exists(r2, "src/main/resources/logback-spring.xml"))

def r3 = fresh("logback-console2-")
h.run(r3, [enable_logback: "true", logback_console_type: "2",
           enable_logback_appender_smtp: "true", enable_logback_appender_file: "true",
           enable_api_cpanel_elk_log_system: "true"], "snippet-logback.groovy")
check("3.1 console=2 removes the granular consoles",
        !exists(r3, "${cfg}/appender-console-granular.xml") &&
                !exists(r3, "${cfg}/appender-console-layouts-granular.xml"))
check("3.2 console=2 keeps the tabular console and the appenders",
        exists(r3, "${cfg}/appender-console-tabular.xml") &&
                exists(r3, "${cfg}/appender-smtp-email.xml") && exists(r3, "${cfg}/appender-rolfile-file.xml"))
check("3.3 console=2 keeps ELK and elk-compose", exists(r3, "ELK") && exists(r3, "docker/elk-compose.yml"))

def r4 = fresh("logback-console3-")
h.run(r4, [enable_logback: "true", logback_console_type: "3",
           enable_logback_appender_smtp: "true", enable_logback_appender_file: "true",
           enable_api_cpanel_elk_log_system: "true"], "snippet-logback.groovy")
check("4.1 console=3 removes only the tabular console", !exists(r4, "${cfg}/appender-console-tabular.xml"))
check("4.2 console=3 keeps the granular consoles",
        exists(r4, "${cfg}/appender-console-granular.xml") && exists(r4, "${cfg}/appender-console-layouts-granular.xml"))

def r5 = fresh("logback-appenders-off-")
h.run(r5, [enable_logback: "true", logback_console_type: "2",
           enable_logback_appender_smtp: "false", enable_logback_appender_file: "false",
           enable_api_cpanel_elk_log_system: "true"], "snippet-logback.groovy")
check("5.1 smtp=false removes the smtp appender", !exists(r5, "${cfg}/appender-smtp-email.xml"))
check("5.2 file=false removes the rolling file appender", !exists(r5, "${cfg}/appender-rolfile-file.xml"))
check("5.3 ELK is kept when it is enabled", exists(r5, "ELK"))

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
