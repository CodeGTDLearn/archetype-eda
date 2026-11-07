/**
 * verify.groovy - ENUMS derived from a PURE PAYLOAD (FR-021) for the
 * `custom-enums-from-payload` profile.
 *
 * Asserts:
 *   1. a top-level UPPERCASE array became an enum (Status, entity package);
 *   2. a nested field UPPERCASE array became an enum (Uf, nested package);
 *   3. the entity references the enum by simple name (same package) and the
 *      nested POJO by FQN, marking only the nested field @Transient;
 *   4. both enums carry the payload constants;
 *   5. the nested POJO references the nested enum by its simple name;
 *   6. no @JsonIgnore, no import for the enums.
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

def expectedArtifactId = 'custom-enums-from-payload'
def expectedEntityName = 'Pedido'
def expectedPackage = 'it.pkg.pedido'
def packagePath = 'it/pkg/pedido'
def nestedPath = "${packagePath}/nested"

def basedirFile = new File(_basedir as String)
def projectRoot = new File(basedirFile, "project/${expectedArtifactId}")
if (!projectRoot.exists()) {
    projectRoot = basedirFile
}

def fail = { String message -> throw new IllegalStateException("verify.groovy: " + message) }

// ---------------------------------------------------------------------------
// 1) The entity: top-level enum by simple name; nested POJO by FQN
// ---------------------------------------------------------------------------
def entityFile = new File(projectRoot, "src/main/java/${packagePath}/${expectedEntityName}.java")
if (!entityFile.isFile()) fail("generated entity not found: ${entityFile.path}")
def entity = entityFile.getText("UTF-8")

if (!(entity =~ /private\s+Status\s+status\s*;/).find()) {
    fail("the entity lost the top-level enum field 'status'")
}
if (!(entity =~ /(?s)@Transient\s+private\s+it\.pkg\.pedido\.nested\.Endereco\s+endereco\s*;/).find()) {
    fail("the entity does not reference the nested POJO by FQN with @Transient")
}
if (entity.contains("import ${expectedPackage}.Status;")) {
    fail("the entity must NOT import Status (same package)")
}
if (entity.contains("import ${expectedPackage}.nested.")) {
    fail("the entity must NOT import a nested type (it uses the FQN)")
}
if (entity.contains("@JsonIgnore")) {
    fail("the entity must NOT use @JsonIgnore")
}
if (entity =~ /(?s)@Transient\s+private\s+Status\s+status\s*;/) {
    fail("a top-level enum field MUST NOT be @Transient")
}
if (entity =~ /(?s)@Transient\s+private\s+String\s+name\s*;/) {
    fail("only nested fields may be @Transient; 'name' is a regular attribute")
}

// ---------------------------------------------------------------------------
// 2) The top-level enum lives in the entity package
// ---------------------------------------------------------------------------
def statusFile = new File(projectRoot, "src/main/java/${packagePath}/Status.java")
if (!statusFile.isFile()) fail("top-level enum not found: ${statusFile.path}")
def status = statusFile.getText("UTF-8")
if (!status.contains("package it.pkg.pedido;")) fail("Status does not declare the entity package")
if (!status.contains("public enum Status")) fail("Status is not a public enum")
['ATIVO', 'CANCELADO'].each { String constant ->
    if (!status.contains(constant)) fail("Status lost the constant '${constant}'")
}

// ---------------------------------------------------------------------------
// 3) The nested enum lives in the nested package
// ---------------------------------------------------------------------------
def ufFile = new File(projectRoot, "src/main/java/${nestedPath}/Uf.java")
if (!ufFile.isFile()) fail("nested enum not found: ${ufFile.path}")
def uf = ufFile.getText("UTF-8")
if (!uf.contains("package it.pkg.pedido.nested;")) fail("Uf does not declare the nested package")
if (!uf.contains("public enum Uf")) fail("Uf is not a public enum")
['SP', 'RJ'].each { String constant ->
    if (!uf.contains(constant)) fail("Uf lost the constant '${constant}'")
}

// ---------------------------------------------------------------------------
// 4) The nested POJO references the nested enum by simple name
// ---------------------------------------------------------------------------
def enderecoFile = new File(projectRoot, "src/main/java/${nestedPath}/Endereco.java")
if (!enderecoFile.isFile()) fail("nested POJO not found: ${enderecoFile.path}")
def endereco = enderecoFile.getText("UTF-8")
if (!(endereco =~ /private\s+Uf\s+uf\s*;/).find()) {
    fail("Endereco lost the nested enum field 'uf'")
}
if (!endereco.contains("public Uf getUf()")) fail("Endereco has no explicit getter for the nested enum")
if (!endereco.contains("public void setUf(Uf uf)")) fail("Endereco has no explicit setter for the nested enum")
if (endereco.contains("lombok")) fail("Endereco uses Lombok")
if (endereco.contains("jakarta.persistence")) fail("Endereco uses JPA")
if (endereco.contains("@Transient")) fail("Endereco must not use @Transient (plain POJO)")

return null
