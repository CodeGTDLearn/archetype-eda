def h = new GroovyClassLoader(this.class.classLoader).parseClass(new File("src/test/archetype-post-generate/PostProcessorHarness.groovy")).newInstance()
int failures = 0
def check = { String n, boolean c -> if (c) { println "PASS: ${n}" } else { println "FAIL: ${n}"; failures++ } }

def pom = '''<project>
  <modelVersion>4.0.0</modelVersion>
  <dependencies>
  </dependencies>
</project>
'''

def withPom = { String prefix ->
    def r = File.createTempDir(prefix, ""); r.deleteOnExit(); h.seed(r, "pom.xml", pom); r
}

def r1 = withPom("pom-runtime-")
h.run(r1, [extra_POM_dependencies: "org.slf4j:slf4j-api:1.7.31:runtime"], "snippet-extra-pom-dependencies.groovy")
def p1 = new File(r1, "pom.xml").getText("UTF-8")
check("1.1 the dependency coordinates are inserted",
        p1.contains("<groupId>org.slf4j</groupId>") && p1.contains("<artifactId>slf4j-api</artifactId>") &&
                p1.contains("<version>1.7.31</version>"))
check("1.2 the explicit scope is used", p1.contains("<scope>runtime</scope>"))

def r2 = withPom("pom-compile-")
h.run(r2, [extra_POM_dependencies: "com.acme:lib:2.0"], "snippet-extra-pom-dependencies.groovy")
check("2.1 a missing scope defaults to compile",
        new File(r2, "pom.xml").getText("UTF-8").contains("<scope>compile</scope>"))

def r3 = withPom("pom-off-")
def before = new File(r3, "pom.xml").getText("UTF-8")
h.run(r3, [extra_POM_dependencies: "false"], "snippet-extra-pom-dependencies.groovy")
check("3.1 false leaves the pom untouched", new File(r3, "pom.xml").getText("UTF-8") == before)

def r4 = File.createTempDir("pom-missing-", ""); r4.deleteOnExit()
h.run(r4, [extra_POM_dependencies: "com.acme:lib:2.0"], "snippet-extra-pom-dependencies.groovy")
check("4.1 a missing pom does not crash", true)

def r5 = withPom("pom-multi-")
h.run(r5, [extra_POM_dependencies: "a.b:c:1.0,b.c:d:2.0:test"], "snippet-extra-pom-dependencies.groovy")
def p5 = new File(r5, "pom.xml").getText("UTF-8")
check("5.1 two dependencies are inserted", p5.contains("<artifactId>c</artifactId>") && p5.contains("<artifactId>d</artifactId>"))
check("5.2 the second dependency keeps its own scope", p5.contains("<scope>test</scope>"))

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
