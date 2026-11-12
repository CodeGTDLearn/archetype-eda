/**
 * verify.groovy - Logback ON, console type 1, no ELK, no appenders.
 *
 * Covers the branch of snippet-13 (logback on + ELK off) and of snippet-17
 * (logback on + console 1 + no appender + no ELK): the ELK artifacts are
 * pruned and, with no appender at all, the whole logback configuration goes
 * away - the application then falls back to the Spring Boot default.
 */
def _basedir = null
try { _basedir = basedir } catch (Throwable t) { _basedir = null }
if (_basedir == null) _basedir = System.getProperty('basedir')
if (_basedir == null) throw new IllegalStateException("verify.groovy: basedir not bound")

def expectedArtifactId = 'logback-no-elk'
def basedirFile = new File(_basedir as String)
def projectRoot = new File(basedirFile, "project/${expectedArtifactId}")
if (!projectRoot.exists()) projectRoot = basedirFile

// the ELK artifacts are pruned (snippet-13)
[
        'ELK',
        'docker/elk-compose.yml'
].each { String rel ->
    File f = new File(projectRoot, rel)
    if (f.exists()) {
        throw new IllegalStateException(
                "verify.groovy: the ELK artifacts must be pruned: '${rel}'. Project: ${projectRoot.path}")
    }
}

// no appender at all: the logback configuration is pruned (snippet-17)
[
        'src/main/resources/logback-spring.xml',
        'src/main/resources/logback-spring-config',
        'src/test/resources/logback-test.xml'
].each { String rel ->
    File f = new File(projectRoot, rel)
    if (f.exists()) {
        throw new IllegalStateException(
                "verify.groovy: the logback configuration must be pruned: '${rel}'. Project: ${projectRoot.path}")
    }
}

return null
