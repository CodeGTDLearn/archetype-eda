/**
 * Unit test for the post-processor FILE and FOLDER helpers.
 * Covers the 12 branches of archetype-file-functions.groovy and
 * archetype-folder-functions.groovy, including the same-path rename no-op.
 */
def fileFns = new File("src/main/resources/META-INF/archetype_post_generate_body/archetype-file-functions.groovy").getText("UTF-8")
def folderFns = new File("src/main/resources/META-INF/archetype_post_generate_body/archetype-folder-functions.groovy").getText("UTF-8")
def functions = "import java.nio.file.Path\nimport java.nio.file.Paths\n" + fileFns + "\n" + folderFns

int failures = 0
def check = { String n, boolean c -> if (c) { println "PASS: ${n}" } else { println "FAIL: ${n}"; failures++ } }

def run = { Object target, String call ->
    def binding = new Binding()
    binding.setVariable("target", target)
    binding.setVariable("listTarget", target)
    new GroovyShell(this.class.classLoader, binding).evaluate(functions + "\n" + call)
}

// ---------------------------------------------------------------------------
// 1) deleteFile
// ---------------------------------------------------------------------------
def f1 = File.createTempFile("fn-delete-", ".txt"); f1.deleteOnExit()
run(f1, "deleteFile(target)")
check("1.1 deleteFile removes an existing file", !f1.exists())

def missing = new File(f1.parentFile, "does-not-exist.txt")
def survived = true
try { run(missing, "deleteFile(target)") } catch (Throwable t) { survived = false }
check("1.2 deleteFile on a missing file is a no-op", survived && !missing.exists())

// ---------------------------------------------------------------------------
// 2) renameFile
// ---------------------------------------------------------------------------
def dir2 = File.createTempDir("fn-rename-", ""); dir2.deleteOnExit()
def f2 = new File(dir2, "old.txt"); f2.setText("x", "UTF-8")
run(f2, "renameFile(target, 'new.txt')")
check("2.1 renameFile renames an existing file", new File(dir2, "new.txt").isFile() && !f2.exists())

def f3 = new File(dir2, "same.txt"); f3.setText("x", "UTF-8")
def ok3 = true
try { run(f3, "renameFile(target, 'same.txt')") } catch (Throwable t) { ok3 = false }
check("2.2 renameFile with the same name is a no-op", ok3 && f3.isFile())

def f4 = new File(dir2, "a.txt"); f4.setText("x", "UTF-8")
def f5 = new File(dir2, "b.txt"); f5.setText("x", "UTF-8")
run(f4, "renameFile(target, 'b.txt')")
check("2.3 renameFile never overwrites an existing target", f4.isFile() && f5.isFile())

// ---------------------------------------------------------------------------
// 3) copyFile
// ---------------------------------------------------------------------------
def dir3 = File.createTempDir("fn-copy-", ""); dir3.deleteOnExit()
def src = new File(dir3, "source.txt"); src.setText("content", "UTF-8")
def destination = new File(dir3, "destination")
run(src, "copyFile(target, new File('${destination.absolutePath.replace('\\', '/')}'))")
check("3.1 copyFile copies the bytes", new File(destination, "source.txt").getText("UTF-8") == "content")

def ok4 = true
try { run(missing, "copyFile(target, new File('${destination.absolutePath.replace('\\', '/')}'))") } catch (Throwable t) { ok4 = false }
check("3.2 copyFile on a missing source is a no-op", ok4)

// ---------------------------------------------------------------------------
// 4) deleteFolder / renameFolder
// ---------------------------------------------------------------------------
def dir4 = File.createTempDir("fn-folder-", ""); dir4.deleteOnExit()
new File(dir4, "a/b").mkdirs(); new File(dir4, "a/b/c.txt").setText("x", "UTF-8")
run(dir4, "deleteFolder(target)")
check("4.1 deleteFolder removes a nested tree", !dir4.exists())

def dir5 = File.createTempDir("fn-rename-folder-", ""); dir5.deleteOnExit()
def child = new File(dir5, "child"); child.mkdirs(); new File(child, "x.txt").setText("x", "UTF-8")
run(child, "renameFolder(target, 'renamed')")
check("4.2 renameFolder renames a directory", new File(dir5, "renamed/x.txt").isFile())

def dir6 = File.createTempDir("fn-rename-folder-busy-", ""); dir6.deleteOnExit()
new File(dir6, "a").mkdirs(); new File(dir6, "b").mkdirs()
run(new File(dir6, "a"), "renameFolder(target, 'b')")
check("4.3 renameFolder never overwrites an existing folder", new File(dir6, "a").isDirectory() && new File(dir6, "b").isDirectory())

// ---------------------------------------------------------------------------
// 5) deleteFilesByExtensionRecursivelyFromFolder / deleteFilesFromListFromFolder
// ---------------------------------------------------------------------------
def dir7 = File.createTempDir("fn-ext-", ""); dir7.deleteOnExit()
new File(dir7, "sub").mkdirs()
new File(dir7, "a.yml").setText("x", "UTF-8")
new File(dir7, "a.properties").setText("x", "UTF-8")
new File(dir7, "sub/b.yml").setText("x", "UTF-8")
run(dir7.toPath(), "deleteFilesByExtensionRecursivelyFromFolder(target, '.yml')")
check("5.1 deleteFilesByExtension deletes recursively", !new File(dir7, "a.yml").exists() && !new File(dir7, "sub/b.yml").exists())
check("5.2 deleteFilesByExtension keeps other extensions", new File(dir7, "a.properties").isFile())

def dir8 = File.createTempDir("fn-list-", ""); dir8.deleteOnExit()
new File(dir8, "a.txt").setText("x", "UTF-8"); new File(dir8, "b.txt").setText("x", "UTF-8")
new File(dir8, "keep.txt").setText("x", "UTF-8")
run(dir8.toPath(), "deleteFilesFromListFromFolder(target, ['a.txt', 'b.txt', null, '  '])")
check("5.3 deleteFilesFromListFromFolder deletes only the listed files",
        !new File(dir8, "a.txt").exists() && !new File(dir8, "b.txt").exists() && new File(dir8, "keep.txt").isFile())
check("5.4 deleteFilesFromListFromFolder no-ops on a missing directory",
        { run(new File(dir8, "missing").toPath(), "deleteFilesFromListFromFolder(target, ['x.txt'])"); true }())

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
