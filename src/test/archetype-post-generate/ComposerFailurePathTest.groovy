/**
 * Failure paths of the COMPOSITOR (ArchetypePostGenerateComposer.groovy).
 *
 * The composer guards its inputs with IllegalStateException: a missing body
 * file, an import that is not declared in body-01-imports, an asset without
 * its marker, an empty asset folder. Those guards are the only thing that
 * stops a silently broken post-processor from reaching the JAR - and no
 * suite exercised them.
 *
 * The composer resolves its paths RELATIVE to the working directory, so the
 * suite runs it in a FORKED JVM whose working directory is a throw-away copy
 * of the asset tree. The composer that runs is the real one, from
 * src/main/groovy - not a copy of its logic.
 */
def composer = new File("src/main/groovy/ArchetypePostGenerateComposer.groovy").canonicalFile
def metaDir = new File("src/main/resources/META-INF")

int failures = 0
def check = { String n, boolean c -> if (c) { println "PASS: ${n}" } else { println "FAIL: ${n}"; failures++ } }

// The classpath of the suite, so the forked JVM can run the composer.
def classpath = {
    def urls = []
    def cl = this.class.classLoader
    while (cl != null) {
        try {
            if (cl instanceof URLClassLoader) urls.addAll(((URLClassLoader) cl).getURLs())
        } catch (Throwable ignored) {
        }
        cl = cl.parent
    }
    return urls.collect { new File(it.toURI()).absolutePath }.join(File.pathSeparator)
}()

def javaBin = new File(System.getProperty("java.home"), "bin/java.exe")
if (!javaBin.isFile()) javaBin = new File(System.getProperty("java.home"), "bin/java")

// A throw-away copy of the asset tree.
def copyTree = { File source, File target ->
    target.mkdirs()
    source.eachFile { File f ->
        if (f.isDirectory()) {
            copyTree(f, new File(target, f.name))
        } else {
            new File(target, f.name).bytes = f.bytes
        }
    }
}
def freshWorkspace = {
    File root = File.createTempDir("composer-fail-", "")
    root.deleteOnExit()
    File meta = new File(root, "src/main/resources/META-INF")
    ["archetype_post_generate_body", "archetype_post_generate_variables",
     "archetype_post_generate_generators", "archetype_post_generate_snippets"].each { String dir ->
        copyTree(new File(metaDir, dir), new File(meta, dir))
    }
    return root
}
def runComposer = { File workingDir ->
    def cmd = [javaBin.absolutePath, "-cp", classpath, "groovy.ui.GroovyMain", composer.absolutePath]
    def pb = new ProcessBuilder(cmd)
    pb.directory(workingDir)
    pb.redirectErrorStream(true)
    def proc = pb.start()
    def out = proc.inputStream.getText("UTF-8")
    proc.waitFor()
    return [exit: proc.exitValue(), out: out]
}

check("1.0 the composer source exists", composer.isFile())
check("1.1 the classpath of the suite is not empty", classpath.length() > 0)

// ---------------------------------------------------------------------------
// The happy path: the composer writes the artifact in the forked workspace
// ---------------------------------------------------------------------------
File happy = freshWorkspace()
def happyResult = runComposer(happy)
File composed = new File(happy, "target/classes/META-INF/archetype-post-generate.groovy")
check("2.1 the composer succeeds on a healthy tree", happyResult.exit == 0)
check("2.2 the composer writes the composed artifact", composed.isFile())
check("2.3 the composed artifact carries the canonical sections",
        composed.isFile() && composed.getText("UTF-8").contains("// BODY-03-LOGGING-VARIABLES.GROOVY"))

// ---------------------------------------------------------------------------
// The failure paths
// ---------------------------------------------------------------------------

// 3) a body file declared in SECTIONS is missing
File missingBody = freshWorkspace()
new File(missingBody, "src/main/resources/META-INF/archetype_post_generate_body/body-13-folder-functions.groovy").delete()
def missingBodyResult = runComposer(missingBody)
check("3.1 the composer fails when a body file of SECTIONS is missing", missingBodyResult.exit != 0)
check("3.2 the composer names the missing body file",
        missingBodyResult.out.contains("Required body file missing") &&
                missingBodyResult.out.contains("body-13-folder-functions.groovy"))

// 4) the imports prelude is missing
File missingImports = freshWorkspace()
new File(missingImports, "src/main/resources/META-INF/archetype_post_generate_body/body-01-imports.groovy").delete()
def missingImportsResult = runComposer(missingImports)
check("4.1 the composer fails when the imports prelude is missing", missingImportsResult.exit != 0)
check("4.2 the composer names the missing imports prelude",
        missingImportsResult.out.contains("Required body file missing") &&
                missingImportsResult.out.contains("body-01-imports.groovy"))

// 5) an import that is not declared in body-01-imports
File undeclaredImport = freshWorkspace()
new File(undeclaredImport,
        "src/main/resources/META-INF/archetype_post_generate_snippets/snippet-14-enable_docker_support.groovy")
        .setText("import java.util.UUID\n// SNIPPET-14-ENABLE_DOCKER_SUPPORT.GROOVY\n", "UTF-8")
def undeclaredImportResult = runComposer(undeclaredImport)
check("5.1 the composer fails on an import that is not declared", undeclaredImportResult.exit != 0)
check("5.2 the composer names the undeclared import",
        undeclaredImportResult.out.contains("is not declared in") &&
                undeclaredImportResult.out.contains("snippet-14-enable_docker_support.groovy"))

// 6) an asset without the composer marker
File missingMarker = freshWorkspace()
new File(missingMarker,
        "src/main/resources/META-INF/archetype_post_generate_snippets/snippet-14-enable_docker_support.groovy")
        .setText("// SNIPPET-14-DISABLED\n", "UTF-8")
def missingMarkerResult = runComposer(missingMarker)
check("6.1 the composer fails when an asset has no marker", missingMarkerResult.exit != 0)
check("6.2 the composer names the asset without a marker",
        missingMarkerResult.out.contains("is missing its composer marker") &&
                missingMarkerResult.out.contains("snippet-14-enable_docker_support.groovy"))

// 7) an empty asset folder
File emptyFolder = freshWorkspace()
new File(emptyFolder, "src/main/resources/META-INF/archetype_post_generate_generators")
        .listFiles().each { it.delete() }
def emptyFolderResult = runComposer(emptyFolder)
check("7.1 the composer fails when an asset folder is empty", emptyFolderResult.exit != 0)
check("7.2 the composer names the empty folder",
        emptyFolderResult.out.contains("Generators directory is empty"))

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
