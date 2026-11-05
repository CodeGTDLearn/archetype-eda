def h = new GroovyClassLoader(this.class.classLoader).parseClass(new File("src/test/archetype-post-generate/PostProcessorHarness.groovy")).newInstance()
int failures = 0
def check = { String n, boolean c -> if (c) { println "PASS: ${n}" } else { println "FAIL: ${n}"; failures++ } }

def root = File.createTempDir("github-off-", ""); root.deleteOnExit()
h.seed(root, "git_temp/config", "x")
h.seed(root, "gitignore", "target/")
h.run(root, [enable_git_support: "false"], "snippet-github.groovy")
check("1.1 git=false removes git_temp", !new File(root, "git_temp").exists())
check("1.2 git=false removes gitignore", !new File(root, "gitignore").exists())

def root2 = File.createTempDir("github-on-", ""); root2.deleteOnExit()
h.seed(root2, "git_temp/config", "x")
h.seed(root2, "gitignore", "target/")
h.run(root2, [enable_git_support: "true"], "snippet-github.groovy")
check("2.1 git=true renames git_temp to .git", new File(root2, ".git/config").isFile() && !new File(root2, "git_temp").exists())
check("2.2 git=true renames gitignore to .gitignore",
        new File(root2, ".gitignore").isFile() && !new File(root2, "gitignore").exists())
check("2.3 the .gitignore content is preserved", new File(root2, ".gitignore").getText("UTF-8") == "target/")

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
