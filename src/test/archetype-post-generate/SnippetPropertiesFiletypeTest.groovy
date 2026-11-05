def h = new GroovyClassLoader(this.class.classLoader).parseClass(new File("src/test/archetype-post-generate/PostProcessorHarness.groovy")).newInstance()
int failures = 0
def check = { String n, boolean c -> if (c) { println "PASS: ${n}" } else { println "FAIL: ${n}"; failures++ } }

def seed = { File root ->
    ["src/main/resources/a.properties", "src/main/resources/a.yml",
     "src/main/resources/sub/b.properties", "src/main/resources/sub/b.yml",
     "src/test/resources/c.properties", "src/test/resources/c.yml"].each { h.seed(root, it, "x") }
}

def root = File.createTempDir("filetype-1-", ""); root.deleteOnExit(); seed(root)
h.run(root, [properties_filetype: "1"], "snippet-properties-filetype.groovy")
check("1.1 filetype=1 deletes every .yml under main", !new File(root, "src/main/resources/a.yml").exists() &&
        !new File(root, "src/main/resources/sub/b.yml").exists())
check("1.2 filetype=1 keeps .properties under main", new File(root, "src/main/resources/b.properties").exists() == false &&
        new File(root, "src/main/resources/a.properties").isFile())
check("1.3 filetype=1 deletes every .yml under test", !new File(root, "src/test/resources/c.yml").exists())
check("1.4 filetype=1 keeps .properties under test", new File(root, "src/test/resources/c.properties").isFile())

def root2 = File.createTempDir("filetype-2-", ""); root2.deleteOnExit(); seed(root2)
h.run(root2, [properties_filetype: "2"], "snippet-properties-filetype.groovy")
check("2.1 filetype=2 deletes every .properties under main", !new File(root2, "src/main/resources/a.properties").exists() &&
        !new File(root2, "src/main/resources/sub/b.properties").exists())
check("2.2 filetype=2 keeps .yml under main", new File(root2, "src/main/resources/a.yml").isFile())
check("2.3 filetype=2 deletes every .properties under test", !new File(root2, "src/test/resources/c.properties").exists())
check("2.4 filetype=2 keeps .yml under test", new File(root2, "src/test/resources/c.yml").isFile())

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
