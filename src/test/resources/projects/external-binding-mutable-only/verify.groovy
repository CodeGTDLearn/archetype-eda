/**
 * verify.groovy - the binding profile with the MUTABLE flavour.
 *
 * With mutability=mutable the mutable bean survives and the immutable
 * record is pruned - the two flavours are never kept together.
 */
def _basedir = null
try { _basedir = basedir } catch (Throwable t) { _basedir = null }
if (_basedir == null) _basedir = System.getProperty('basedir')
if (_basedir == null) throw new IllegalStateException("verify.groovy: basedir not bound")

def expectedArtifactId = 'external-binding-mutable-only'
def basedirFile = new File(_basedir as String)
def projectRoot = new File(basedirFile, "project/${expectedArtifactId}")
if (!projectRoot.exists()) projectRoot = basedirFile

def mainPkg = 'src/main/java/it/pkg/myentity/config/properties'

// the mutable bean survives
File mutable = new File(projectRoot, "${mainPkg}/ApplicationProperties.java")
if (!mutable.isFile()) {
    throw new IllegalStateException(
            "verify.groovy: the mutable flavour must survive: ${mutable.path}")
}

// the immutable record is pruned
File immutable = new File(projectRoot, "${mainPkg}/ApplicationPropertiesRecord.java")
if (immutable.exists()) {
    throw new IllegalStateException(
            "verify.groovy: the immutable flavour must be pruned: ${immutable.path}")
}

// the list and map sub-modules are OFF
[
        "${mainPkg}/list_properties/ListProperties.java",
        "${mainPkg}/map_properties/MapProperties.java"
].each { String rel ->
    File f = new File(projectRoot, rel)
    if (f.exists()) {
        throw new IllegalStateException(
                "verify.groovy: the list/map sub-modules must be pruned: '${rel}'. Project: ${projectRoot.path}")
    }
}

// the binding tests are OFF
File testJava = new File(projectRoot, 'src/test/java')
if (testJava.exists()) {
    throw new IllegalStateException(
            "verify.groovy: the test tree must be pruned with the binding tests off: ${testJava.path}")
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
