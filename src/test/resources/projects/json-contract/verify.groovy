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

def expectedEntityName = 'ContractEntity'
def expectedArtifactId = 'json-contract'
def packagePath = 'it/pkg/contractentity'
def expectedAttributes = [
    [name: 'id',        type: 'Long'],
    [name: 'name',      type: 'String'],
    [name: 'createdAt', type: 'String'],
]

def basedirFile = new File(_basedir as String)
def projectRoot = new File(basedirFile, "project/${expectedArtifactId}")
if (!projectRoot.exists()) {
    projectRoot = basedirFile
}

def candidatePaths = []
candidatePaths << new File(projectRoot, "src/main/java/${packagePath}/${expectedEntityName}.java")
candidatePaths << new File(projectRoot, "src/main/java/${packagePath}/entity/${expectedEntityName}.java")
candidatePaths << new File(projectRoot, "src/main/java/${packagePath}/model/${expectedEntityName}.java")
candidatePaths << new File(projectRoot, "src/main/java/${expectedEntityName}.java")
candidatePaths << new File(basedirFile, "src/main/java/${packagePath}/${expectedEntityName}.java")

def entityFile = candidatePaths.find { it.exists() }
if (entityFile == null) {
    throw new IllegalStateException(
        "verify.groovy: generated entity file not found. " +
        "Expected entity name: ${expectedEntityName}. " +
        "Tried paths: ${candidatePaths*.path.join(' | ')}"
    )
}

def entitySource = entityFile.text

expectedAttributes.each { attr ->
    def attrName = attr.name as String
    def attrType = attr.type as String
    def pattern = ~/private\s+${attrType}\s+${attrName}\s*[;=]/
    if (!(pattern.matcher(entitySource).find())) {
        throw new IllegalStateException(
            "verify.groovy: expected attribute '${attrName}' of type '${attrType}' " +
            "not found in generated entity ${entityFile.path}"
        )
    }
}

return null
