/**
 * verify.groovy - binding tests ON with the list sub-module only.
 *
 * Covers the branch of snippet-09 (tests on + list off would prune, but
 * here the list is ON): the list sources survive, the list TEST sources are
 * pruned by the default sub-module rule, and the default/map sub-modules are
 * gone.
 */
def _basedir = null
try { _basedir = basedir } catch (Throwable t) { _basedir = null }
if (_basedir == null) _basedir = System.getProperty('basedir')
if (_basedir == null) throw new IllegalStateException("verify.groovy: basedir not bound")

def expectedArtifactId = 'binding-tests-list-only'
def basedirFile = new File(_basedir as String)
def projectRoot = new File(basedirFile, "project/${expectedArtifactId}")
if (!projectRoot.exists()) projectRoot = basedirFile

def mainPkg = 'src/main/java/it/pkg/myentity/config/properties'
def testPkg = 'src/test/java/myentity/config/properties'

// the list sub-module is ON: its sources survive
[
        "${mainPkg}/list_properties/ListProperties.java",
        "${mainPkg}/list_properties/ListObjects.java",
        "${mainPkg}/list_properties/ListPropertiesImmutable.java",
        "${mainPkg}/list_properties/ListPropertiesRecord.java"
].each { String rel ->
    File f = new File(projectRoot, rel)
    if (!f.isFile()) {
        throw new IllegalStateException(
                "verify.groovy: the list sub-module must survive: '${rel}'. Project: ${projectRoot.path}")
    }
}

// the default sub-module is OFF
[
        "${mainPkg}/ApplicationProperties.java",
        "${mainPkg}/ApplicationPropertiesRecord.java"
].each { String rel ->
    File f = new File(projectRoot, rel)
    if (f.exists()) {
        throw new IllegalStateException(
                "verify.groovy: the default sub-module must be pruned: '${rel}'. Project: ${projectRoot.path}")
    }
}

// the map sub-module is OFF
[
        "${mainPkg}/map_properties/MapProperties.java",
        "${mainPkg}/map_properties/MapItem.java"
].each { String rel ->
    File f = new File(projectRoot, rel)
    if (f.exists()) {
        throw new IllegalStateException(
                "verify.groovy: the map sub-module must be pruned: '${rel}'. Project: ${projectRoot.path}")
    }
}

// the list TEST sources survive: the mutability is 'none', so neither the
// immutable nor the mutable rule prunes them
[
        "${testPkg}/list_properties/ListPropertiesTest.java",
        "${testPkg}/list_properties/ListPropertiesImmutableTest.java",
        "${testPkg}/list_properties/ListPropertiesRecordTest.java"
].each { String rel ->
    File f = new File(projectRoot, rel)
    if (!f.isFile()) {
        throw new IllegalStateException(
                "verify.groovy: the list test sources must survive: '${rel}'. Project: ${projectRoot.path}")
    }
}

// the DEFAULT test sources are pruned (the tests are on and the default
// sub-module is off)
[
        "${testPkg}/ApplicationPropertiesTest.java",
        "${testPkg}/ApplicationPropertiesRecordTest.java",
        "${testPkg}/ApplicationPropertiesValidationTest.java",
        "${testPkg}/ApplicationPropertiesRecordValidationTest.java"
].each { String rel ->
    File f = new File(projectRoot, rel)
    if (f.exists()) {
        throw new IllegalStateException(
                "verify.groovy: the default test sources must be pruned: '${rel}'. Project: ${projectRoot.path}")
    }
}

// the MAP test resources are pruned (the tests are on and the map
// sub-module is off)
File mapTestResource = new File(projectRoot, 'src/test/resources/properties/application_map_test.properties')
if (mapTestResource.exists()) {
    throw new IllegalStateException(
            "verify.groovy: the map test resources must be pruned: ${mapTestResource.path}")
}

// the LIST test resources survive (the list sub-module is on)
File listTestResource = new File(projectRoot, 'src/test/resources/properties/application_list_test.properties')
if (!listTestResource.isFile()) {
    throw new IllegalStateException(
            "verify.groovy: the list test resources must survive: ${listTestResource.path}")
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
