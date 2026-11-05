/**
 * Contract test for the archetype FILESETS (Maven side).
 *
 * Two directions:
 *   A. every template file is matched by at least one fileSet (nothing is
 *      silently left out of the generated project);
 *   B. every include matches at least one real file (no dead include).
 *
 * The second direction is the regression test for the app.key/app.pub vs
 * appJwt.key/appJwt.pub mismatch.
 */
import java.util.regex.Pattern

def metadata = new File("target/classes/META-INF/maven/archetype-metadata.xml")
def resourcesRoot = new File("src/main/resources/archetype-resources")

int failures = 0
def check = { String name, boolean cond ->
    if (cond) { println "PASS: ${name}" } else { println "FAIL: ${name}"; failures++ }
}

check("1.1 the built catalog exists", metadata.isFile())
if (!metadata.isFile()) {
    println ""
    println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
    if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
    return
}

def antToRegex = { String pattern ->
    def sb = new StringBuilder("^")
    for (int i = 0; i < pattern.length(); i++) {
        char c = pattern.charAt(i)
        if (c == '*') {
            if (i + 1 < pattern.length() && pattern.charAt(i + 1) == '*') {
                i++
                if (i + 1 < pattern.length() && pattern.charAt(i + 1) == '/') {
                    i++
                    sb.append("(?:.*/)?")
                } else {
                    sb.append(".*")
                }
            } else {
                sb.append("[^/]*")
            }
        } else if (c == '?') {
            sb.append("[^/]")
        } else {
            sb.append(Pattern.quote(c.toString()))
        }
    }
    sb.append('$')
    return sb.toString()
}

def descriptor = new XmlSlurper().parse(metadata)
def fileSets = []
descriptor.fileSets.fileSet.each { fs ->
    def dir = fs.directory.text()
    def incs = fs.includes.include.collect { it.text() }
    fileSets << [dir: dir, includes: incs]
}

check("1.2 the catalog declares 18 fileSets", fileSets.size() == 18)

def files = []
resourcesRoot.eachFileRecurse { File f ->
    if (f.isFile()) files << f.absolutePath.substring(resourcesRoot.absolutePath.length() + 1).replace('\\', '/')
}
check("1.3 the template tree has 110 files", files.size() == 110)

def subPath = { String file, String dir ->
    if (!dir) return file
    if (!file.startsWith(dir + "/")) return null
    return file.substring(dir.length() + 1)
}

// ---------------------------------------------------------------------------
// 2) Every template file is covered by at least one fileSet
// ---------------------------------------------------------------------------
def uncovered = files.findAll { String file ->
    !fileSets.any { fs ->
        def sub = subPath(file, fs.dir)
        if (sub == null) return false
        if (fs.includes.isEmpty()) return true
        return fs.includes.any { inc -> sub ==~ antToRegex(inc) }
    }
}
check("2.1 every template file is covered by a fileSet: ${uncovered}", uncovered.isEmpty())

// ---------------------------------------------------------------------------
// 3) Every include matches at least one real file
// ---------------------------------------------------------------------------
def deadIncludes = []
fileSets.each { fs ->
    fs.includes.each { inc ->
        def rx = antToRegex(inc)
        def matched = files.any { String file ->
            def sub = subPath(file, fs.dir)
            sub != null && (sub ==~ rx)
        }
        if (!matched) deadIncludes << "${fs.dir}:${inc}"
    }
}
check("3.1 no dead fileSet include: ${deadIncludes}", deadIncludes.isEmpty())

// ---------------------------------------------------------------------------
// 4) JWT regression: the security fileSet includes the real key names
// ---------------------------------------------------------------------------
def securitySets = fileSets.findAll { it.dir == "src/main/resources/security" }
check("4.1 the security directory is declared", securitySets.size() == 1)
check("4.2 the security fileSet includes appJwt.key",
        securitySets.size() == 1 && securitySets[0].includes.contains("appJwt.key"))
check("4.3 the security fileSet includes appJwt.pub",
        securitySets.size() == 1 && securitySets[0].includes.contains("appJwt.pub"))

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
