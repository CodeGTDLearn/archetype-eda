/**
 * verify.groovy - the binding profile with the VALIDATION sub-module.
 *
 * With enable_binding_external_properties_validation=true the generated
 * properties class carries the Jakarta Validation annotations and the
 * @Validated stereotype - the guard of the template. Without the flag the
 * class is a plain bean.
 */
def _basedir = null
try { _basedir = basedir } catch (Throwable t) { _basedir = null }
if (_basedir == null) _basedir = System.getProperty('basedir')
if (_basedir == null) throw new IllegalStateException("verify.groovy: basedir not bound")

def expectedArtifactId = 'external-binding-validated-only'
def basedirFile = new File(_basedir as String)
def projectRoot = new File(basedirFile, "project/${expectedArtifactId}")
if (!projectRoot.exists()) projectRoot = basedirFile

def mainPkg = 'src/main/java/it/pkg/myentity/config/properties'

// the default sub-module survives (the mutability is 'none': both flavours)
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

// the validation guard of the template is applied
File properties = new File(projectRoot, "${mainPkg}/ApplicationProperties.java")
String source = properties.getText("UTF-8")
if (!source.contains("@Validated")) {
    throw new IllegalStateException(
            "verify.groovy: the validation stereotype is missing: ${properties.path}")
}
if (!source.contains("import org.springframework.validation.annotation.Validated;")) {
    throw new IllegalStateException(
            "verify.groovy: the Validated import is missing: ${properties.path}")
}
if (!source.contains("jakarta.validation.constraints.NotEmpty")) {
    throw new IllegalStateException(
            "verify.groovy: the Jakarta Validation annotations are missing: ${properties.path}")
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

return null
