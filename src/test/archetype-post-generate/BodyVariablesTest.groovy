/**
 * Unit test for the post-processor PATH/GLOBAL variables (body).
 * Asserts the path tree derived from request.outputDirectory/artifactId.
 */
import java.nio.file.Path

def body = new File("src/main/resources/META-INF/archetype_post_generate_body")
def files = ["archetype-global-variables.groovy", "archetype-path-root-variables.groovy",
             "archetype-path-src-main-variables.groovy", "archetype-path-src-test-variables.groovy",
             "archetype-path-logback-variables.groovy", "archetype-path-security-variables.groovy"]
def source = files.collect { new File(body, it).getText("UTF-8") }.join("\n")

int failures = 0
def check = { String n, boolean c -> if (c) { println "PASS: ${n}" } else { println "FAIL: ${n}"; failures++ } }

def root = File.createTempDir("body-vars-", ""); root.deleteOnExit()
def properties = new Properties()
properties.put("package", "it.pkg")
properties.put("entity_name", "MyEntity")
properties.put("entity_name_package", "myentity")

def binding = new Binding()
binding.setVariable("request", [outputDirectory: root.parentFile.absolutePath, artifactId: root.name])
binding.setVariable("properties", properties)
def names = ["projectRootFolder", "srcMainJava", "srcMainJavaEntityFolder", "srcMainResources",
             "srcMainResourcesProperties", "srcTestJava", "srcTestResources",
             "srcTestJavaEntityConfigPropertiesFolder", "filePathAppKey", "filePathAppPub",
             "folderJwtPath", "logbackAppenderSmtpPathFile", "logbackAppenderRolFilePathFile",
             "rootGitIgnoreFile", "rootGitFolder", "rootDockerFolder", "rootELKFolder"]
def capture = "captured = [" + names.collect { "${it}: ${it}" }.join(", ") + "]"
new GroovyShell(binding).evaluate(source + "\n" + capture)
def p = (Map) binding.getVariable("captured")

def eq = { String name, String expected -> p[name]?.toString()?.replace('\\', '/') == expected.replace('\\', '/') }

check("1.1 projectRootFolder points to the generated project", eq("projectRootFolder", root.absolutePath))
check("1.2 srcMainJava", eq("srcMainJava", "${root.absolutePath}/src/main/java"))
check("1.3 srcMainJavaEntityFolder", eq("srcMainJavaEntityFolder", "${root.absolutePath}/src/main/java/it/pkg/myentity"))
check("1.4 srcMainResources", eq("srcMainResources", "${root.absolutePath}/src/main/resources"))
check("1.5 srcMainResourcesProperties", eq("srcMainResourcesProperties", "${root.absolutePath}/src/main/resources/properties"))
check("1.6 srcTestJava", eq("srcTestJava", "${root.absolutePath}/src/test/java"))
check("1.7 srcTestResources", eq("srcTestResources", "${root.absolutePath}/src/test/resources"))
check("1.8 srcTestJavaEntityConfigPropertiesFolder",
        eq("srcTestJavaEntityConfigPropertiesFolder", "${root.absolutePath}/src/test/java/myentity/config/properties"))
check("1.9 the JWT key path lives under security/", eq("filePathAppKey", "${root.absolutePath}/src/main/resources/security/appJwt.key"))
check("1.10 the JWT pub path lives under security/", eq("filePathAppPub", "${root.absolutePath}/src/main/resources/security/appJwt.pub"))
check("1.11 folderJwtPath", eq("folderJwtPath", "${root.absolutePath}/src/main/java/it/pkg/myentity/config/authentication"))
check("1.12 logback smtp appender path", eq("logbackAppenderSmtpPathFile", "${root.absolutePath}/src/main/resources/logback-spring-config/appender-smtp-email.xml"))
check("1.13 logback rolling-file appender path", eq("logbackAppenderRolFilePathFile", "${root.absolutePath}/src/main/resources/logback-spring-config/appender-rolfile-file.xml"))
check("1.14 rootGitIgnoreFile points to the generated 'gitignore'", eq("rootGitIgnoreFile", "${root.absolutePath}/gitignore"))
check("1.15 rootGitFolder", eq("rootGitFolder", "${root.absolutePath}/git_temp"))
check("1.16 rootDockerFolder", eq("rootDockerFolder", "${root.absolutePath}/docker"))
check("1.17 rootELKFolder", eq("rootELKFolder", "${root.absolutePath}/ELK"))

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
