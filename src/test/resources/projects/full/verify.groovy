/**
 * verify.groovy - FR: the JWT block is emitted and the key pair is copied.
 *
 * The `full` profile is the only IT with enable_jwt_authentication=true, so it
 * is the only generated project that KEEPS the SecurityConfig and needs
 * `jwt.public.key` / `jwt.private.key` to load the ApplicationContext.
 *
 * This hook asserts the GENERATION side (not only the generated project build):
 *   1. src/main/resources/security/appJwt.key exists;
 *   2. src/main/resources/security/appJwt.pub exists;
 *   3. application.properties carries both jwt.* keys pointing at them;
 *   4. the SecurityConfig was kept (the post-processor must not delete it).
 */
def _basedir = null
try { _basedir = basedir } catch (Throwable t) { _basedir = null }
if (_basedir == null) _basedir = System.getProperty('basedir')
if (_basedir == null) throw new IllegalStateException("verify.groovy: basedir not bound")

def expectedArtifactId = 'full'
def basedirFile = new File(_basedir as String)
def projectRoot = new File(basedirFile, "project/${expectedArtifactId}")
if (!projectRoot.exists()) projectRoot = basedirFile

def resourcesDir = new File(projectRoot, 'src/main/resources')
def keyFile = new File(resourcesDir, 'security/appJwt.key')
def pubFile = new File(resourcesDir, 'security/appJwt.pub')

if (!keyFile.isFile()) {
    throw new IllegalStateException(
        "verify.groovy: the JWT private key was NOT copied. Expected: ${keyFile.path}. " +
        "Check the 'src/main/resources/security' fileset includes."
    )
}
if (!pubFile.isFile()) {
    throw new IllegalStateException(
        "verify.groovy: the JWT public key was NOT copied. Expected: ${pubFile.path}. " +
        "Check the 'src/main/resources/security' fileset includes."
    )
}

def appProperties = new File(resourcesDir, 'application.properties')
if (!appProperties.isFile()) {
    throw new IllegalStateException(
        "verify.groovy: application.properties not found: ${appProperties.path}"
    )
}

def appPropertiesSource = appProperties.getText("UTF-8")
if (!appPropertiesSource.contains('jwt.private.key=classpath:security/appJwt.key')) {
    throw new IllegalStateException(
        "verify.groovy: application.properties does not declare " +
        "'jwt.private.key=classpath:security/appJwt.key'. " +
        "The JWT block is gated by enable_jwt_authentication; check the Velocity conditional."
    )
}
if (!appPropertiesSource.contains('jwt.public.key=classpath:security/appJwt.pub')) {
    throw new IllegalStateException(
        "verify.groovy: application.properties does not declare " +
        "'jwt.public.key=classpath:security/appJwt.pub'."
    )
}

def securityConfig = new File(projectRoot,
        'src/main/java/it/pkg/myentity/config/authentication/MyEntitySecurityConfig.java')
if (!securityConfig.isFile()) {
    throw new IllegalStateException(
        "verify.groovy: the SecurityConfig was NOT kept for enable_jwt_authentication=true. " +
        "Expected: ${securityConfig.path}"
    )
}

return null
