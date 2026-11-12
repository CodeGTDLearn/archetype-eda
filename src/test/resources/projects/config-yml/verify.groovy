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

return null
