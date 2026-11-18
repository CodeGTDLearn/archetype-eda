/**
 * verify.groovy - binding tests ON with the map sub-module only.
 *
 * Covers the branch of snippet-10 (tests on + map off): the map sources
 * survive, the map TEST sources are pruned, and the default/list
 * sub-modules are gone.
 */
def _basedir = null
try { _basedir = basedir } catch (Throwable t) { _basedir = null }
if (_basedir == null) _basedir = System.getProperty('basedir')
if (_basedir == null) throw new IllegalStateException("verify.groovy: basedir not bound")

def expectedArtifactId = 'binding-tests-map-only'
def basedirFile = new File(_basedir as String)
def projectRoot = new File(basedirFile, "project/${expectedArtifactId}")
if (!projectRoot.exists()) projectRoot = basedirFile

def mainPkg = 'src/main/java/it/pkg/myentity/config/properties'
def testPkg = 'src/test/java/myentity/config/properties'

// the map sub-module is ON: its sources survive
[
        "${mainPkg}/map_properties/MapProperties.java",
        "${mainPkg}/map_properties/MapItem.java",
        "${mainPkg}/map_properties/MapPropertiesImmutable.java",
        "${mainPkg}/map_properties/MapPropertiesRecord.java"
].each { String rel ->
    File f = new File(projectRoot, rel)
    if (!f.isFile()) {
        throw new IllegalStateException(
                "verify.groovy: the map sub-module must survive: '${rel}'. Project: ${projectRoot.path}")
    }
}

// the default and list sub-modules are OFF
[
        "${mainPkg}/ApplicationProperties.java",
        "${mainPkg}/ApplicationPropertiesRecord.java",
        "${mainPkg}/list_properties/ListProperties.java",
        "${mainPkg}/list_properties/ListObjects.java"
].each { String rel ->
    File f = new File(projectRoot, rel)
    if (f.exists()) {
        throw new IllegalStateException(
                "verify.groovy: the default/list sub-modules must be pruned: '${rel}'. Project: ${projectRoot.path}")
    }
}

// the map TEST sources survive: the mutability is 'none', so neither the
// immutable nor the mutable rule prunes them
[
        "${testPkg}/map_properties/MapPropertiesTest.java",
        "${testPkg}/map_properties/MapPropertiesImmutableTest.java",
        "${testPkg}/map_properties/MapPropertiesRecordTest.java"
].each { String rel ->
    File f = new File(projectRoot, rel)
    if (!f.isFile()) {
        throw new IllegalStateException(
                "verify.groovy: the map test sources must survive: '${rel}'. Project: ${projectRoot.path}")
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

// the LIST test resources are pruned (the tests are on and the list
// sub-module is off)
File listTestResource = new File(projectRoot, 'src/test/resources/properties/application_list_test.properties')
if (listTestResource.exists()) {
    throw new IllegalStateException(
            "verify.groovy: the list test resources must be pruned: ${listTestResource.path}")
}

// the MAP test resources survive (the map sub-module is on)
File mapTestResource = new File(projectRoot, 'src/test/resources/properties/application_map_test.properties')
if (!mapTestResource.isFile()) {
    throw new IllegalStateException(
            "verify.groovy: the map test resources must survive: ${mapTestResource.path}")
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
