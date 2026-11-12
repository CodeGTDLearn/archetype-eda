/**
 * verify.groovy - the binding profile with the LIST sub-module only.
 *
 * Proves that the list sub-module survives (its sources AND its resource)
 * while the default and map sub-modules are pruned.
 */
def _basedir = null
try { _basedir = basedir } catch (Throwable t) { _basedir = null }
if (_basedir == null) _basedir = System.getProperty('basedir')
if (_basedir == null) throw new IllegalStateException("verify.groovy: basedir not bound")

def expectedArtifactId = 'external-binding-list-only'
def basedirFile = new File(_basedir as String)
def projectRoot = new File(basedirFile, "project/${expectedArtifactId}")
if (!projectRoot.exists()) projectRoot = basedirFile

def mainPkg = 'src/main/java/it/pkg/myentity/config/properties'

// the list sub-module survives
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

// its resource survives too
File listResource = new File(projectRoot, 'src/main/resources/properties/application_list.properties')
if (!listResource.isFile()) {
    throw new IllegalStateException(
            "verify.groovy: the list resource must survive: ${listResource.path}")
}

// the default and map sub-modules are pruned
[
        "${mainPkg}/ApplicationProperties.java",
        "${mainPkg}/ApplicationPropertiesRecord.java",
        "${mainPkg}/map_properties/MapProperties.java",
        "${mainPkg}/map_properties/MapItem.java",
        'src/main/resources/properties/application_map.properties'
].each { String rel ->
    File f = new File(projectRoot, rel)
    if (f.exists()) {
        throw new IllegalStateException(
                "verify.groovy: the default/map sub-modules must be pruned: '${rel}'. Project: ${projectRoot.path}")
    }
}

// the binding tests are OFF
File testJava = new File(projectRoot, 'src/test/java')
if (testJava.exists()) {
    throw new IllegalStateException(
            "verify.groovy: the test tree must be pruned with the binding tests off: ${testJava.path}")
}

return null
