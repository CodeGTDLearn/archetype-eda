def h = new GroovyClassLoader(this.class.classLoader).parseClass(new File("src/test/archetype-post-generate/PostProcessorHarness.groovy")).newInstance()
int failures = 0
def check = { String n, boolean c -> if (c) { println "PASS: ${n}" } else { println "FAIL: ${n}"; failures++ } }

def root = File.createTempDir("docker-off-", ""); root.deleteOnExit()
h.seed(root, "docker/docker-compose.yml", "x")
h.seed(root, "docker/api_Dockerfile", "x")
h.run(root, [enable_docker: "false"], "snippet-docker.groovy")
check("1.1 docker=false removes the docker folder", !new File(root, "docker").exists())

def root2 = File.createTempDir("docker-on-", ""); root2.deleteOnExit()
h.seed(root2, "docker/docker-compose.yml", "x")
h.run(root2, [enable_docker: "true"], "snippet-docker.groovy")
check("2.1 docker=true keeps the docker folder", new File(root2, "docker/docker-compose.yml").isFile())

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
