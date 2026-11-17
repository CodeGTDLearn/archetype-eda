/**
 * verify.groovy - the binding profile with the DEFAULT sub-module only.
 *
 * Proves that the default sub-module survives while the list and map
 * sub-modules are pruned - and that the resources of the pruned
 * sub-modules are gone as well.
 */
def _basedir = null
try { _basedir = basedir } catch (Throwable t) { _basedir = null }
if (_basedir == null) _basedir = System.getProperty('basedir')
if (_basedir == null) throw new IllegalStateException("verify.groovy: basedir not bound")

def expectedArtifactId = 'external-binding-default-only'
def basedirFile = new File(_basedir as String)
def projectRoot = new File(basedirFile, "project/${expectedArtifactId}")
if (!projectRoot.exists()) projectRoot = basedirFile

def mainPkg = 'src/main/java/it/pkg/myentity/config/properties'

// the default sub-module survives
[
        "${mainPkg}/ApplicationProperties.java",
        "${mainPkg}/ApplicationPropertiesRecord.java"
].each { String rel ->
    File f = new File(projectRoot, rel)
    if (!f.isFile()) {
        throw new IllegalStateException(
                "verify.groovy: the default sub-module must survive: '${rel}'. Project: ${projectRoot.path}")
    }
}

// the list and map sub-modules are pruned
[
        "${mainPkg}/list_properties/ListProperties.java",
        "${mainPkg}/list_properties/ListObjects.java",
        "${mainPkg}/map_properties/MapProperties.java",
        "${mainPkg}/map_properties/MapItem.java"
].each { String rel ->
    File f = new File(projectRoot, rel)
    if (f.exists()) {
        throw new IllegalStateException(
                "verify.groovy: the list/map sub-modules must be pruned: '${rel}'. Project: ${projectRoot.path}")
    }
}

// the resources of the pruned sub-modules are gone
[
        'src/main/resources/properties/application_list.properties',
        'src/main/resources/properties/application_map.properties'
].each { String rel ->
    File f = new File(projectRoot, rel)
    if (f.exists()) {
        throw new IllegalStateException(
                "verify.groovy: the list/map resources must be pruned: '${rel}'. Project: ${projectRoot.path}")
    }
}

// the binding tests are OFF: no test source at all
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
    def _cgOrphans = []
    _cgTestJava.eachFileRecurse { _cgFile ->
        if (_cgFile.isFile() && _cgFile.name.endsWith('ContractTest.java')
                && _cgFile.path.replace('\\', '/').contains('/contract/')) {
            _cgOrphans << _cgFile
        }
    }
    if (!_cgOrphans.isEmpty()) {
        throw new IllegalStateException(
            "verify.groovy: orphan contract schema test in ${_cgProject.path}: ${_cgOrphans*.name}")
    }
}

return null
