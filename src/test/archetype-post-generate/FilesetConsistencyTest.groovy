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
def filesetFragmentsDir = new File("src/main/resources/META-INF/maven/archetype_metadata_filesets")

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
    def excs = fs.excludes.exclude.collect { it.text() }
    fileSets << [dir: dir, includes: incs, excludes: excs]
}

check("1.2 the catalog declares 18 fileSets", fileSets.size() == 18)

def files = []
resourcesRoot.eachFileRecurse { File f ->
    if (f.isFile()) files << f.absolutePath.substring(resourcesRoot.absolutePath.length() + 1).replace('\\', '/')
}
check("1.3 the template tree has 127 files", files.size() == 127)
check("1.4 the 18 fileset fragments exist (one per <fileSet>)",
        filesetFragmentsDir.isDirectory() &&
                filesetFragmentsDir.listFiles().findAll { it.name.endsWith(".xml") }.size() == 18)

// ---------------------------------------------------------------------------
// 1b) The fragments are NUMBERED: fileset-<NN>-<flag>[-<parte>].xml
//
//     The NN is the canonical order of the <fileSet> block, versioned in the
//     file name (same convention as property-<NN>-<flag>.xml). The
//     aggregator is the source of truth for that order: the DOCTYPE entities
//     and the <fileSets> references must follow the ascending NN.
//
//     Every assertion below is derived from the directories/files on disk.
// ---------------------------------------------------------------------------
def fragments = filesetFragmentsDir.listFiles()
        .findAll { it.name.endsWith(".xml") }
        .sort { it.name }

check("1.5 every fileset fragment is named fileset-<NN>-<flag>[-<parte>].xml",
        fragments.every { it.name ==~ /^fileset-\d{2}-[a-z][a-z0-9_]*(-[a-z0-9_-]+)?\.xml$/ })

def fragmentNNs = fragments.collect { it.name =~ /^fileset-(\d{2})-/ ? (it.name =~ /^fileset-(\d{2})-/)[0][1] : null }
check("1.6 the fileset NN are contiguous and duplicate-free: ${fragmentNNs}",
        fragmentNNs == fragmentNNs.unique().sort() &&
                fragmentNNs == (1..fragmentNNs.size()).collect { String.format("%02d", it) })

def aggregator = new File("src/main/resources/META-INF/maven/archetype-metadata.xml")
def aggregatorText = aggregator.isFile() ? aggregator.getText("UTF-8") : ""

// The DOCTYPE entities of the filesets, in declaration order.
def declaredEntities = []
(aggregatorText =~ /<!ENTITY\s+(fileset-[A-Za-z0-9_.-]+)\s+SYSTEM\s+"([^"]+)">/).each { m ->
    declaredEntities << [name: m[1], path: m[2]]
}
check("1.7 the aggregator declares 18 fileset entities", declaredEntities.size() == 18)

// The <fileSets> references, in document order.
def referencedEntities = []
(aggregatorText =~ /&(fileset-[A-Za-z0-9_.-]+);/).each { m ->
    referencedEntities << m[1]
}
check("1.8 the <fileSets> block references 18 fileset entities", referencedEntities.size() == 18)

def nnOf = { String name ->
    def m = name =~ /^fileset-(\d{2})-/ 
    return m ? m[0][1] : null
}

check("1.9 the DOCTYPE entities are declared in ascending NN order: ${declaredEntities*.name}",
        declaredEntities*.name.collect { nnOf(it) } == declaredEntities*.name.collect { nnOf(it) }.sort())

check("1.10 the <fileSets> references are in ascending NN order: ${referencedEntities}",
        referencedEntities.collect { nnOf(it) } == referencedEntities.collect { nnOf(it) }.sort())

check("1.11 the DOCTYPE and the <fileSets> block declare the filesets in the same order",
        declaredEntities*.name == referencedEntities)

// Each entity must point to the fragment that carries its own name.
def mismatchedTargets = declaredEntities.findAll { e ->
    def target = e.path.split("/").last().replace(".xml", "")
    return target != e.name
}
check("1.12 every fileset entity points to the fragment named after it: ${mismatchedTargets*.name}",
        mismatchedTargets.isEmpty())

// And the fragments on disk must be exactly the ones the aggregator declares.
def onDisk = fragments.collect { it.name.replace(".xml", "") }
def declared = declaredEntities*.name
check("1.13 the fragments on disk are exactly the declared entities: ${(onDisk - declared)} / ${(declared - onDisk)}",
        onDisk.sort() == declared.sort())

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
