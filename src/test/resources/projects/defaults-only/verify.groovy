/**
 * verify.groovy - the defaults-only profile.
 *
 * The profile omits 30 flags: every one of them falls back to the catalog
 * default (false). It proves that the ABSENT flags really behave as "off" -
 * the same shape the minimal profile produces, but reached through the
 * catalog defaults instead of through an explicit false.
 */
def _basedir = null
try { _basedir = basedir } catch (Throwable t) { _basedir = null }
if (_basedir == null) _basedir = System.getProperty('basedir')
if (_basedir == null) throw new IllegalStateException("verify.groovy: basedir not bound")

def expectedArtifactId = 'defaults-only'
def basedirFile = new File(_basedir as String)
def projectRoot = new File(basedirFile, "project/${expectedArtifactId}")
if (!projectRoot.exists()) projectRoot = basedirFile

// the flags that were omitted fall back to the catalog default (false)
[
        'src/test/java',
        'docker',
        'ELK',
        'src/main/resources/logback-spring-config',
        'src/main/resources/logback-spring.xml',
        'src/main/java/it/pkg/myentity/config/authentication',
        'src/main/java/it/pkg/myentity/config/properties/ApplicationProperties.java',
        'src/main/java/it/pkg/myentity/config/properties/ApplicationPropertiesRecord.java',
        'src/main/java/it/pkg/myentity/config/properties/list_properties',
        'src/main/java/it/pkg/myentity/config/properties/map_properties',
        'src/main/resources/properties'
].each { String rel ->
    File f = new File(projectRoot, rel)
    if (f.exists()) {
        throw new IllegalStateException(
                "verify.groovy: an omitted flag must fall back to the default (off): '${rel}'. " +
                        "Project: ${projectRoot.path}")
    }
}

// the core survives
File entity = new File(projectRoot, 'src/main/java/it/pkg/myentity/MyEntity.java')
if (!entity.isFile()) {
    throw new IllegalStateException(
            "verify.groovy: the entity was not generated: ${entity.path}")
}

// the pom carries the java version of the fixture
File pom = new File(projectRoot, 'pom.xml')
if (!pom.isFile()) {
    throw new IllegalStateException("verify.groovy: the pom was not generated: ${pom.path}")
}
if (!pom.getText("UTF-8").contains("<java.version>17</java.version>")) {
    throw new IllegalStateException(
            "verify.groovy: the pom must declare <java.version>17</java.version>. Pom: ${pom.path}")
}


// ---------------------------------------------------------------------------
// ---------------------------------------------------------------------------
// Contract schema test: this profile does not persist a contract, so the
// generated application must NOT carry the orphan test package.
// ---------------------------------------------------------------------------
def _contractGateBase = null
try { _contractGateBase = basedir } catch (Throwable ignored) { _contractGateBase = System.getProperty('basedir') }
def _contractGateRoot = new File(_contractGateBase as String)
def _contractGateProjects = []
if (new File(_contractGateRoot, 'project').isDirectory()) {
    new File(_contractGateRoot, 'project').eachDir { _cgProject -> _contractGateProjects << _cgProject }
} else {
    _contractGateProjects << _contractGateRoot
}
_contractGateProjects.each { _cgProject ->
    def _cgTestJava = new File(_cgProject, 'src/test/java')
    if (!_cgTestJava.isDirectory()) return
    def _cgOrphanDirs = []
    _cgTestJava.eachFileRecurse { _cgFile ->
        if (_cgFile.isDirectory() && _cgFile.name == 'contract'
                && _cgFile.path.replace('\\', '/').contains('/src/test/java/')) {
            _cgOrphanDirs << _cgFile
        }
    }
    if (!_cgOrphanDirs.isEmpty()) {
        throw new IllegalStateException(
            "verify.groovy: orphan contract schema test package in ${_cgProject.path}: ${_cgOrphanDirs*.path}")
    }
}

return null
