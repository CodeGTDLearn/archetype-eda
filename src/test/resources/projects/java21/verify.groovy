/**
 * verify.groovy - java_version 21 (the catalog default).
 *
 * The catalog declares java_version=21; the other fixtures pin 17. This one
 * proves that the default reaches the pom of the generated project.
 */
def _basedir = null
try { _basedir = basedir } catch (Throwable t) { _basedir = null }
if (_basedir == null) _basedir = System.getProperty('basedir')
if (_basedir == null) throw new IllegalStateException("verify.groovy: basedir not bound")

def expectedArtifactId = 'java21'
def basedirFile = new File(_basedir as String)
def projectRoot = new File(basedirFile, "project/${expectedArtifactId}")
if (!projectRoot.exists()) projectRoot = basedirFile

File pom = new File(projectRoot, 'pom.xml')
if (!pom.isFile()) {
    throw new IllegalStateException("verify.groovy: the pom was not generated: ${pom.path}")
}
String pomSource = pom.getText("UTF-8")
if (!pomSource.contains("<java.version>21</java.version>")) {
    throw new IllegalStateException(
            "verify.groovy: the pom must declare <java.version>21</java.version>. Pom: ${pom.path}")
}

return null
