/**
 * verify.groovy - config_file_format=2 (the YAML flavour).
 *
 * The post-processor prunes the extension that the chosen format does NOT
 * use. With config_file_format=2 the .properties are pruned and the .yml
 * twins must survive - otherwise the generated project starts without a
 * configuration.
 */
def _basedir = null
try { _basedir = basedir } catch (Throwable t) { _basedir = null }
if (_basedir == null) _basedir = System.getProperty('basedir')
if (_basedir == null) throw new IllegalStateException("verify.groovy: basedir not bound")

def expectedArtifactId = 'config-yml'
def basedirFile = new File(_basedir as String)
def projectRoot = new File(basedirFile, "project/${expectedArtifactId}")
if (!projectRoot.exists()) projectRoot = basedirFile

def mustExist = [
        'src/main/resources/application.yml',
        'src/test/resources/application.yml',
        'src/main/resources/protected_properties/admin_credentials.yml',
        'src/main/resources/protected_properties/application_keys.yml',
        'src/main/resources/protected_properties/authentication_credentials.yml'
]
def mustBeGone = [
        'src/main/resources/application.properties',
        'src/test/resources/application.properties',
        'src/main/resources/protected_properties/admin_credentials.properties',
        'src/main/resources/protected_properties/application_keys.properties',
        'src/main/resources/protected_properties/authentication_credentials.properties'
]

mustExist.each { String rel ->
    File f = new File(projectRoot, rel)
    if (!f.isFile()) {
        throw new IllegalStateException(
                "verify.groovy: the YAML flavour must keep '${rel}'. Project: ${projectRoot.path}")
    }
}
mustBeGone.each { String rel ->
    File f = new File(projectRoot, rel)
    if (f.exists()) {
        throw new IllegalStateException(
                "verify.groovy: the YAML flavour must prune '${rel}'. Project: ${projectRoot.path}")
    }
}

// the binding module is OFF in this profile: the properties folder is pruned
// by the binding rule, not by the file format rule
File propertiesFolder = new File(projectRoot, 'src/main/resources/properties')
if (propertiesFolder.exists()) {
    throw new IllegalStateException(
            "verify.groovy: the properties folder must be pruned with the binding off: ${propertiesFolder.path}")
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
