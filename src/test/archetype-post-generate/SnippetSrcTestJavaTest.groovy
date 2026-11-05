def h = new GroovyClassLoader(this.class.classLoader).parseClass(new File("src/test/archetype-post-generate/PostProcessorHarness.groovy")).newInstance()
int failures = 0
def check = { String n, boolean c -> if (c) { println "PASS: ${n}" } else { println "FAIL: ${n}"; failures++ } }

def root = File.createTempDir("srctest-off-", ""); root.deleteOnExit()
h.seed(root, "src/test/java/X.java", "x")
h.seed(root, "src/test/resources/y.properties", "x")
h.run(root, [enable_folder_src_test_java: "false"], "snippet-src-test-java.groovy")
check("1.1 src_test=false removes src/test/java", !new File(root, "src/test/java").exists())
check("1.2 src_test=false removes src/test/resources", !new File(root, "src/test/resources").exists())

def root2 = File.createTempDir("srctest-on-", ""); root2.deleteOnExit()
h.seed(root2, "src/test/java/X.java", "x")
h.seed(root2, "src/test/resources/y.properties", "x")
h.run(root2, [enable_folder_src_test_java: "true"], "snippet-src-test-java.groovy")
check("2.1 src_test=true keeps src/test/java", new File(root2, "src/test/java/X.java").isFile())
check("2.2 src_test=true keeps src/test/resources", new File(root2, "src/test/resources/y.properties").isFile())

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
