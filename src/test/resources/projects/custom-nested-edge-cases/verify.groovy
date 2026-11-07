/**
 * verify.groovy - NESTED contract objects - edge cases (FR-021) for the
 * `custom-nested-edge-cases` profile.
 *
 * Asserts:
 *   1. the entity name is reserved: the nested object 'pedido' becomes
 *      PedidoType;
 *   2. a Java keyword field keeps the '_' suffix (class_);
 *   3. a non-ASCII key is ASCII-normalized (opcao);
 *   4. an array value is emitted as String (tags);
 *   5. a nested POJO references another nested POJO (PedidoAnterior);
 *   6. Lombok is enabled on the entity, but the nested POJOs stay plain.
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

def expectedArtifactId = 'custom-nested-edge-cases'
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

def entityFile = new File(projectRoot, "src/main/java/${packagePath}/${expectedEntityName}.java")
if (!entityFile.isFile()) fail("generated entity not found: ${entityFile.path}")
def entity = entityFile.getText("UTF-8")

// ---------------------------------------------------------------------------
// 1) The entity references BOTH nested POJOs by FQN and marks them @Transient
// ---------------------------------------------------------------------------
if (!entity.contains("import jakarta.persistence.Transient;")) {
    fail("the entity does not import jakarta.persistence.Transient")
}
if (entity.contains("import ${expectedPackage}.nested.")) {
    fail("the entity must NOT import a nested type (it uses the FQN)")
}
def packageRegex = expectedPackage.replace('.', '\\.')
if (!(entity =~ /(?s)@Transient\s+private\s+${packageRegex}\.nested\.PedidoType\s+pedido\s*;/).find()) {
    fail("the entity does not mark 'pedido' @Transient with the FQN")
}
if (!(entity =~ /(?s)@Transient\s+private\s+${packageRegex}\.nested\.Endereco\s+endereco\s*;/).find()) {
    fail("the entity does not mark the accented nested field @Transient with the FQN")
}
if (entity.contains("@JsonIgnore")) {
    fail("the entity must NOT use @JsonIgnore (the nested object stays in the API JSON)")
}
if (entity =~ /(?s)@Transient\s+private\s+String\s+nome\s*;/) {
    fail("only nested fields may be @Transient; 'nome' is a regular attribute")
}

// ---------------------------------------------------------------------------
// 2) The reserved entity name produced PedidoType
// ---------------------------------------------------------------------------
def pedidoTypeFile = new File(projectRoot, "src/main/java/${nestedPath}/PedidoType.java")
def enderecoFile = new File(projectRoot, "src/main/java/${nestedPath}/Endereco.java")
def pedidoAnteriorFile = new File(projectRoot, "src/main/java/${nestedPath}/PedidoAnterior.java")
if (!pedidoTypeFile.isFile()) fail("nested POJO not found: ${pedidoTypeFile.path}")
if (!enderecoFile.isFile()) fail("nested POJO not found: ${enderecoFile.path}")
if (!pedidoAnteriorFile.isFile()) fail("nested POJO not found: ${pedidoAnteriorFile.path}")

def pedidoType = pedidoTypeFile.getText("UTF-8")
if (!(pedidoType =~ /private\s+String\s+uf\s*;/).find()) {
    fail("PedidoType lost the contract key 'uf'")
}
def pedidoAnterior = pedidoAnteriorFile.getText("UTF-8")
if (!(pedidoAnterior =~ /private\s+String\s+uf\s*;/).find()) {
    fail("PedidoAnterior lost the contract key 'uf'")
}

// ---------------------------------------------------------------------------
// 3) Keyword and non-ASCII field names, array-as-String, nested reference
// ---------------------------------------------------------------------------
def endereco = enderecoFile.getText("UTF-8")
if (!(endereco =~ /private\s+String\s+class_\s*;/).find()) {
    fail("Endereco lost the keyword field 'class_'")
}
if (!(endereco =~ /private\s+String\s+opcao\s*;/).find()) {
    fail("Endereco lost the ASCII-normalized field 'opcao'")
}
if (!(endereco =~ /private\s+String\s+tags\s*;/).find()) {
    fail("Endereco did not map the array field 'tags' to String")
}
if (!(endereco =~ /private\s+PedidoAnterior\s+pedidoAnterior\s*;/).find()) {
    fail("Endereco does not reference the nested POJO PedidoAnterior")
}
if (!endereco.contains("public String getClass_()")) {
    fail("Endereco has no explicit getter for the keyword field")
}
if (!endereco.contains("public String getOpcao()")) {
    fail("Endereco has no explicit getter for the accented field")
}
if (!endereco.contains("public void setTags(String tags)")) {
    fail("Endereco has no explicit setter for tags")
}

// ---------------------------------------------------------------------------
// 4) The nested POJOs stay plain even with Lombok enabled on the entity
// ---------------------------------------------------------------------------
[enderecoFile, pedidoTypeFile, pedidoAnteriorFile].each { File pojo ->
    def source = pojo.getText("UTF-8")
    if (source.contains("lombok")) fail("${pojo.name} uses Lombok")
    if (source.contains("jakarta.persistence")) fail("${pojo.name} uses JPA")
}

return null
