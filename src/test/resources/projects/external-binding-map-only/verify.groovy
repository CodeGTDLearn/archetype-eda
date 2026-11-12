/**
 * verify.groovy - the binding profile with the MAP sub-module only.
 *
 * Proves that the map sub-module survives (its sources AND its resource)
 * while the default and list sub-modules are pruned.
 */
def _basedir = null
try { _basedir = basedir } catch (Throwable t) { _basedir = null }
if (_basedir == null) _basedir = System.getProperty('basedir')
if (_basedir == null) throw new IllegalStateException("verify.groovy: basedir not bound")

def expectedArtifactId = 'external-binding-map-only'
def basedirFile = new File(_basedir as String)
def projectRoot = new File(basedirFile, "project/${expectedArtifactId}")
if (!projectRoot.exists()) projectRoot = basedirFile

def mainPkg = 'src/main/java/it/pkg/myentity/config/properties'

// the map sub-module survives
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

// its resource survives too
File mapResource = new File(projectRoot, 'src/main/resources/properties/application_map.properties')
if (!mapResource.isFile()) {
    throw new IllegalStateException(
            "verify.groovy: the map resource must survive: ${mapResource.path}")
}

// the default and list sub-modules are pruned
[
        "${mainPkg}/ApplicationProperties.java",
        "${mainPkg}/ApplicationPropertiesRecord.java",
        "${mainPkg}/list_properties/ListProperties.java",
        "${mainPkg}/list_properties/ListObjects.java",
        'src/main/resources/properties/application_list.properties'
].each { String rel ->
    File f = new File(projectRoot, rel)
    if (f.exists()) {
        throw new IllegalStateException(
                "verify.groovy: the default/list sub-modules must be pruned: '${rel}'. Project: ${projectRoot.path}")
    }
}

// the binding tests are OFF
File testJava = new File(projectRoot, 'src/test/java')
if (testJava.exists()) {
    throw new IllegalStateException(
            "verify.groovy: the test tree must be pruned with the binding tests off: ${testJava.path}")
}

return null
