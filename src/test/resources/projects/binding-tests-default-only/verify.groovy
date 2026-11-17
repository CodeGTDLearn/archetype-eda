/**
 * verify.groovy - binding tests ON with the default sub-module OFF.
 *
 * Covers the branches of snippet-08 (tests on + default off) and of
 * snippet-12 (tests on + validation off): the default sub-module sources and
 * the default sub-module test sources must be pruned, while the binding
 * module itself stays.
 */
def _basedir = null
try { _basedir = basedir } catch (Throwable t) { _basedir = null }
if (_basedir == null) _basedir = System.getProperty('basedir')
if (_basedir == null) throw new IllegalStateException("verify.groovy: basedir not bound")

def expectedArtifactId = 'binding-tests-default-only'
def basedirFile = new File(_basedir as String)
def projectRoot = new File(basedirFile, "project/${expectedArtifactId}")
if (!projectRoot.exists()) projectRoot = basedirFile

def mainPkg = 'src/main/java/it/pkg/myentity/config/properties'
def testPkg = 'src/test/java/myentity/config/properties'

// the default sub-module is OFF: its sources are pruned
[
        "${mainPkg}/ApplicationProperties.java",
        "${mainPkg}/ApplicationPropertiesRecord.java",
        "${testPkg}/ApplicationPropertiesTest.java",
        "${testPkg}/ApplicationPropertiesRecordTest.java",
        "${testPkg}/ApplicationPropertiesValidationTest.java",
        "${testPkg}/ApplicationPropertiesRecordValidationTest.java"
].each { String rel ->
    File f = new File(projectRoot, rel)
    if (f.exists()) {
        throw new IllegalStateException(
                "verify.groovy: the default sub-module must be pruned: '${rel}'. Project: ${projectRoot.path}")
    }
}

// the list and map sub-modules are OFF as well
[
        "${mainPkg}/list_properties/ListProperties.java",
        "${mainPkg}/map_properties/MapProperties.java",
        "${mainPkg}/map_properties/MapItem.java"
].each { String rel ->
    File f = new File(projectRoot, rel)
    if (f.exists()) {
        throw new IllegalStateException(
                "verify.groovy: the list/map sub-modules must be pruned: '${rel}'. Project: ${projectRoot.path}")
    }
}

// the binding module itself is ON: the starter carries the scan
def starter = new File(projectRoot, 'src/main/java/it/pkg/myentity/ApiStarter.java')
if (!starter.isFile()) {
    throw new IllegalStateException("verify.groovy: the starter was not generated: ${starter.path}")
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
