/**
 * verify.groovy - FR-021 (contract persistence) for the `entity-contract` profile.
 *
 * Asserts that the generated project carries the contract it was built from:
 *   1. src/test/resources/contracts/contract.json EXISTS;
 *   2. its content is byte-identical to the contract handed to the archetype
 *      (the contract IS the base of the module);
 *   3. the generated entity still matches the contract (name, package and the
 *      attributes with their estimated types) - the "schema test" the module
 *      will keep running against this file.
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

def expectedArtifactId = 'entity-contract'
def expectedEntityName = 'ContractEntity'
def expectedPackagePath = 'it/pkg/contractentity'
def expectedAttributes = [
    [name: 'id',     type: 'Integer'],
    [name: 'name',   type: 'String'],
    [name: 'active', type: 'Boolean'],
]

def basedirFile = new File(_basedir as String)
def projectRoot = new File(basedirFile, "project/${expectedArtifactId}")
if (!projectRoot.exists()) {
    projectRoot = basedirFile
}

// ---------------------------------------------------------------------------
// 1) The contract MUST be present in the generated project
// ---------------------------------------------------------------------------
def contractFile = new File(projectRoot, 'src/test/resources/contracts/contract.json')
if (!contractFile.exists()) {
    throw new IllegalStateException(
        "verify.groovy: the contract was NOT persisted. " +
        "Expected: ${contractFile.path}. " +
        "The post-processor copies it only when enable_folder_src_test_java=true."
    )
}

// ---------------------------------------------------------------------------
// 2) The persisted contract MUST be byte-identical to the source contract
// ---------------------------------------------------------------------------
def fixtureRelative = 'src/test/resources/projects/entity-contract/sample-contract.json'
def fixtureCandidates = [new File(fixtureRelative)]
def walker = basedirFile
for (int level = 0; level < 6 && walker != null; level++) {
    fixtureCandidates << new File(walker, fixtureRelative)
    walker = walker.parentFile
}
def fixture = fixtureCandidates.find { it.isFile() }
if (fixture == null) {
    throw new IllegalStateException(
        "verify.groovy: source contract not found. Tried: " +
        fixtureCandidates*.path.join(' | ')
    )
}
byte[] persisted = contractFile.bytes
byte[] expected = fixture.bytes
if (persisted.length != expected.length || !Arrays.equals(persisted, expected)) {
    throw new IllegalStateException(
        "verify.groovy: the persisted contract differs from the source contract.\n" +
        "  source    : ${fixture.path} (${expected.length} bytes)\n" +
        "  persisted : ${contractFile.path} (${persisted.length} bytes)"
    )
}

// ---------------------------------------------------------------------------
// 3) The generated entity MUST match the contract
// ---------------------------------------------------------------------------
def entityFile = [
        new File(projectRoot, "src/main/java/${expectedPackagePath}/${expectedEntityName}.java"),
        new File(projectRoot, "src/main/java/${expectedPackagePath}/entity/${expectedEntityName}.java"),
        new File(projectRoot, "src/main/java/${expectedPackagePath}/model/${expectedEntityName}.java"),
        new File(projectRoot, "src/main/java/${expectedEntityName}.java")
].find { it.exists() }

if (entityFile == null) {
    throw new IllegalStateException(
        "verify.groovy: generated entity file not found. Expected: ${expectedEntityName}"
    )
}

def entitySource = entityFile.text
if (!entitySource.contains("class ${expectedEntityName} ")) {
    throw new IllegalStateException(
        "verify.groovy: the generated entity does not declare the contract entity name: ${entityFile.path}"
    )
}
expectedAttributes.each { attr ->
    def pattern = ~/private\s+${attr.type}\s+${attr.name}\s*[;=]/
    if (!(pattern.matcher(entitySource).find())) {
        throw new IllegalStateException(
            "verify.groovy: the generated entity does not match the contract: " +
            "attribute '${attr.name}' of type '${attr.type}' not found in ${entityFile.path}"
        )
    }
}

return null
