/**
 * verify.groovy - the minimal profile: every optional flag is OFF.
 *
 * Proves that the post-processor prunes EVERY optional module of the
 * generated project - the shape the operator gets when no feature is asked
 * for.
 */
def _basedir = null
try { _basedir = basedir } catch (Throwable t) { _basedir = null }
if (_basedir == null) _basedir = System.getProperty('basedir')
if (_basedir == null) throw new IllegalStateException("verify.groovy: basedir not bound")

def expectedArtifactId = 'minimal'
def basedirFile = new File(_basedir as String)
def projectRoot = new File(basedirFile, "project/${expectedArtifactId}")
if (!projectRoot.exists()) projectRoot = basedirFile

// every optional module is pruned
// (the credentials sub-folder always survives: it is not part of the binding
// module)
[
        'src/test/java',
        'src/test/resources',
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
                "verify.groovy: the optional module must be pruned: '${rel}'. Project: ${projectRoot.path}")
    }
}

// the entity and the starter survive
[
        'src/main/java/it/pkg/myentity/MyEntity.java',
        'src/main/java/it/pkg/myentity/ApiStarter.java'
].each { String rel ->
    File f = new File(projectRoot, rel)
    if (!f.isFile()) {
        throw new IllegalStateException(
                "verify.groovy: the core source must survive: '${rel}'. Project: ${projectRoot.path}")
    }
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
