import java.nio.file.Path

/**
 * Unit test for snippet-authentication.groovy.
 * JWTs off -> authentication folder and the key pair are deleted.
 * JWTs on  -> nothing is touched.
 */
def h = new GroovyClassLoader(this.class.classLoader)
        .parseClass(new File("src/test/archetype-post-generate/PostProcessorHarness.groovy")).newInstance()

int failures = 0
def check = { String n, boolean c -> if (c) { println "PASS: ${n}" } else { println "FAIL: ${n}"; failures++ } }

def root = File.createTempDir("snippet-auth-", "")
root.deleteOnExit()
h.seed(root, "src/main/java/it/pkg/myentity/config/authentication/MyEntitySecurityConfig.java", "class X {}")
def key = h.seed(root, "src/main/resources/security/appJwt.key", "PRIVATE")
def pub = h.seed(root, "src/main/resources/security/appJwt.pub", "PUBLIC")

h.run(root, [enable_jwt_authentication: "false"], "snippet-authentication.groovy")
check("1.1 jwt=false deletes the authentication folder",
        !new File(root, "src/main/java/it/pkg/myentity/config/authentication").exists())
check("1.2 jwt=false deletes appJwt.key", !key.exists())
check("1.3 jwt=false deletes appJwt.pub", !pub.exists())

def root2 = File.createTempDir("snippet-auth-on-", "")
root2.deleteOnExit()
h.seed(root2, "src/main/java/it/pkg/myentity/config/authentication/MyEntitySecurityConfig.java", "class X {}")
def key2 = h.seed(root2, "src/main/resources/security/appJwt.key", "PRIVATE")
def pub2 = h.seed(root2, "src/main/resources/security/appJwt.pub", "PUBLIC")
h.run(root2, [enable_jwt_authentication: "true"], "snippet-authentication.groovy")
check("2.1 jwt=true keeps the authentication folder",
        new File(root2, "src/main/java/it/pkg/myentity/config/authentication/MyEntitySecurityConfig.java").isFile())
check("2.2 jwt=true keeps appJwt.key", key2.isFile())
check("2.3 jwt=true keeps appJwt.pub", pub2.isFile())

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
