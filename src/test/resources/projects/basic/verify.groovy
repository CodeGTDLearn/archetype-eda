/**
 * verify.groovy - the basic profile.
 *
 * The only profile that carries a REAL extra pom dependency and the src/test
 * tree. It proves that the post-processor injected the dependency in the
 * generated pom and that the optional modules were pruned.
 */
def _basedir = null
try { _basedir = basedir } catch (Throwable t) { _basedir = null }
if (_basedir == null) _basedir = System.getProperty('basedir')
if (_basedir == null) throw new IllegalStateException("verify.groovy: basedir not bound")

def expectedArtifactId = 'basic'
def basedirFile = new File(_basedir as String)
def projectRoot = new File(basedirFile, "project/${expectedArtifactId}")
if (!projectRoot.exists()) projectRoot = basedirFile

// the extra dependency was injected in the generated pom
File pom = new File(projectRoot, 'pom.xml')
if (!pom.isFile()) {
    throw new IllegalStateException("verify.groovy: the pom was not generated: ${pom.path}")
}
String pomSource = pom.getText("UTF-8")
if (!pomSource.contains("<artifactId>slf4j-api</artifactId>")) {
    throw new IllegalStateException(
            "verify.groovy: the extra dependency was not injected in the pom. Pom: ${pom.path}")
}
if (!pomSource.contains("<scope>runtime</scope>")) {
    throw new IllegalStateException(
            "verify.groovy: the scope of the extra dependency was not kept. Pom: ${pom.path}")
}

// enable_folder_src_test_java=true: the test tree exists
File testJava = new File(projectRoot, 'src/test/java')
if (!testJava.isDirectory()) {
    throw new IllegalStateException(
            "verify.groovy: the test tree must exist: ${testJava.path}")
}

// the optional modules are OFF
[
        'docker',
        'src/main/resources/logback-spring-config',
        'src/main/java/it/pkg/myentity/config/authentication',
        'src/main/resources/properties'
].each { String rel ->
    File f = new File(projectRoot, rel)
    if (f.exists()) {
        throw new IllegalStateException(
                "verify.groovy: the optional module must be pruned: '${rel}'. Project: ${projectRoot.path}")
    }
}

// the entity was generated (entity_package_name=entitynamepackage)
File entity = new File(projectRoot, 'src/main/java/it/pkg/entitynamepackage/MyEntityName.java')
if (!entity.isFile()) {
    throw new IllegalStateException(
            "verify.groovy: the entity was not generated: ${entity.path}")
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
