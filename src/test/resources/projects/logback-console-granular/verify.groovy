/**
 * verify.groovy - Logback ON with the GRANULAR console (type 3).
 *
 * Covers the branch of snippet-17 (console type 3): the granular consoles
 * survive and the tabular one is pruned. The logback-spring.xml survives
 * because a console is still configured.
 */
def _basedir = null
try { _basedir = basedir } catch (Throwable t) { _basedir = null }
if (_basedir == null) _basedir = System.getProperty('basedir')
if (_basedir == null) throw new IllegalStateException("verify.groovy: basedir not bound")

def expectedArtifactId = 'logback-console-granular'
def basedirFile = new File(_basedir as String)
def projectRoot = new File(basedirFile, "project/${expectedArtifactId}")
if (!projectRoot.exists()) projectRoot = basedirFile

def cfg = 'src/main/resources/logback-spring-config'

// the granular consoles survive
[
        "${cfg}/appender-console-granular.xml",
        "${cfg}/appender-console-layouts-granular.xml"
].each { String rel ->
    File f = new File(projectRoot, rel)
    if (!f.isFile()) {
        throw new IllegalStateException(
                "verify.groovy: the granular consoles must survive: '${rel}'. Project: ${projectRoot.path}")
    }
}

// the tabular console is pruned
File tabular = new File(projectRoot, "${cfg}/appender-console-tabular.xml")
if (tabular.exists()) {
    throw new IllegalStateException(
            "verify.groovy: the tabular console must be pruned: ${tabular.path}")
}

// a console is configured: the spring configuration survives
File spring = new File(projectRoot, 'src/main/resources/logback-spring.xml')
if (!spring.isFile()) {
    throw new IllegalStateException(
            "verify.groovy: the logback-spring.xml must survive: ${spring.path}")
}
if (!spring.getText("UTF-8").contains("appender-console-granular.xml")) {
    throw new IllegalStateException(
            "verify.groovy: the logback-spring.xml must include the granular console: ${spring.path}")
}

// no ELK
File elk = new File(projectRoot, 'ELK')
if (elk.exists()) {
    throw new IllegalStateException(
            "verify.groovy: the ELK folder must be pruned: ${elk.path}")
}

return null
