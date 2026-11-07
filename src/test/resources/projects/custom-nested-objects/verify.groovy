/**
 * verify.groovy - NESTED contract objects (FR-021) for the
 * `custom-nested-objects` profile.
 *
 * Asserts that the generated project:
 *   1. imports jakarta.persistence.Transient and references the nested POJO
 *      by its FULLY QUALIFIED name (no import);
 *   2. marks the nested field @Transient;
 *   3. generates every POJO in the 'nested' subpackage of the entity;
 *   4. keeps the contract keys as the POJO field names;
 *   5. emits plain POJOs (no Lombok, no JPA).
 */
def _basedir = null
def _scriptdir = null
try { _basedir = basedir } catch (Throwable t) { _basedir = null }
try { _scriptdir = scriptdir } catch (Throwable t) { _scriptdir = null }

if (_basedir == null) {
    _basedir = System.getProperty('basedir')
}
if (_basedir == null) {
    throw new IllegalStateException("verify.groovy: basedir not bound")
}

def expectedArtifactId = 'custom-nested-objects'
def expectedEntityName = 'Cliente'
def expectedPackage = 'it.pkg.cliente'
def packagePath = 'it/pkg/cliente'
def nestedPath = "${packagePath}/nested"

def basedirFile = new File(_basedir as String)
def projectRoot = new File(basedirFile, "project/${expectedArtifactId}")
if (!projectRoot.exists()) {
    projectRoot = basedirFile
}

def fail = { String message -> throw new IllegalStateException("verify.groovy: " + message) }

// ---------------------------------------------------------------------------
// 1) The entity references the nested POJO by FQN and marks it @Transient
// ---------------------------------------------------------------------------
def entityFile = new File(projectRoot, "src/main/java/${packagePath}/${expectedEntityName}.java")
if (!entityFile.isFile()) fail("generated entity not found: ${entityFile.path}")
def entity = entityFile.getText("UTF-8")

if (!entity.contains("import jakarta.persistence.Transient;")) {
    fail("the entity does not import jakarta.persistence.Transient")
}
if (entity.contains("import ${expectedPackage}.nested.")) {
    fail("the entity must NOT import a nested type (it uses the FQN)")
}
def packageRegex = expectedPackage.replace('.', '\\.')
if (!(entity =~ /(?s)@Transient\s+private\s+${packageRegex}\.nested\.Endereco\s+endereco\s*;/).find()) {
    fail("the entity does not mark the nested field 'endereco' @Transient with the FQN")
}
if (entity.contains("@JsonIgnore")) {
    fail("the entity must NOT use @JsonIgnore (the nested object stays in the API JSON)")
}
if (entity =~ /(?s)@Transient\s+private\s+String\s+nome\s*;/) {
    fail("only nested fields may be @Transient; 'nome' is a regular attribute")
}

// ---------------------------------------------------------------------------
// 2) The nested POJOs exist in the 'nested' subpackage
// ---------------------------------------------------------------------------
def enderecoFile = new File(projectRoot, "src/main/java/${nestedPath}/Endereco.java")
def complementoFile = new File(projectRoot, "src/main/java/${nestedPath}/Complemento.java")
if (!enderecoFile.isFile()) fail("nested POJO not found: ${enderecoFile.path}")
if (!complementoFile.isFile()) fail("nested POJO not found: ${complementoFile.path}")

def endereco = enderecoFile.getText("UTF-8")
def complemento = complementoFile.getText("UTF-8")

if (!endereco.contains("package ${packagePath.replace('/', '.')}.nested;")) {
    fail("Endereco does not declare the nested package")
}
if (!(endereco =~ /private\s+String\s+logradouro\s*;/).find()) {
    fail("Endereco lost the contract key 'logradouro'")
}
if (!(endereco =~ /private\s+Integer\s+numero\s*;/).find()) {
    fail("Endereco lost the contract key 'numero'")
}
if (!(endereco =~ /private\s+Complemento\s+complemento\s*;/).find()) {
    fail("Endereco does not reference the child POJO Complemento")
}
if (!endereco.contains("public String getLogradouro()")) {
    fail("Endereco has no explicit getter for logradouro")
}
if (!endereco.contains("public void setNumero(Integer numero)")) {
    fail("Endereco has no explicit setter for numero")
}
if (!(complemento =~ /private\s+String\s+descricao\s*;/).find()) {
    fail("Complemento lost the contract key 'descricao'")
}

// ---------------------------------------------------------------------------
// 3) The POJOs are plain: no Lombok, no JPA
// ---------------------------------------------------------------------------
[enderecoFile, complementoFile].each { File pojo ->
    def source = pojo.getText("UTF-8")
    if (source.contains("lombok")) fail("${pojo.name} uses Lombok")
    if (source.contains("jakarta.persistence")) fail("${pojo.name} uses JPA")
}

// ---------------------------------------------------------------------------
// 4) The entity keeps its regular attribute
// ---------------------------------------------------------------------------
if (!(entity =~ /private\s+String\s+nome\s*;/).find()) {
    fail("the entity lost the regular attribute 'nome'")
}

return null
