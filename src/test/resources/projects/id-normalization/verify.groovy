/**
 * verify.groovy - structural id (case normalization).
 *
 * The entity template MUST emit exactly ONE primary key field,
 * "private Long <entityName>Id", and MUST drop every spelling of the
 * contract id (id, Id, iD, ID) from the custom attribute list.
 *
 * This fixture hands the archetype the hand-written list
 * "Long:id,Integer:iD,String:name" (both id spellings are valid per the
 * catalog regex): without the case-insensitive filter in the template the
 * generated entity would carry a SECOND field "private Integer iD;".
 */
def _basedir = null
try { _basedir = basedir } catch (Throwable t) { _basedir = null }
if (_basedir == null) _basedir = System.getProperty('basedir')
if (_basedir == null) throw new IllegalStateException("verify.groovy: basedir not bound")

def expectedArtifactId = 'id-normalization'
def expectedEntityName = 'ContractEntity'
def packagePath = 'it/pkg/contractentity'

def basedirFile = new File(_basedir as String)
def projectRoot = new File(basedirFile, "project/${expectedArtifactId}")
if (!projectRoot.exists()) projectRoot = basedirFile

def candidatePaths = []
candidatePaths << new File(projectRoot, "src/main/java/${packagePath}/${expectedEntityName}.java")
candidatePaths << new File(projectRoot, "src/main/java/${packagePath}/entity/${expectedEntityName}.java")
candidatePaths << new File(projectRoot, "src/main/java/${expectedEntityName}.java")

def entityFile = candidatePaths.find { it.isFile() }
if (entityFile == null) {
    throw new IllegalStateException(
        "verify.groovy: generated entity file not found. Expected entity name: ${expectedEntityName}. " +
        "Tried paths: ${candidatePaths*.path.join(' | ')}"
    )
}

def entitySource = entityFile.getText("UTF-8")

// The primary key is structural: exactly ONE declaration.
def idDeclarations = entitySource.findAll(/(?m)^\s*private\s+Long\s+contractEntityId\s*;/).size()
if (idDeclarations != 1) {
    throw new IllegalStateException(
        "verify.groovy: expected exactly ONE 'private Long contractEntityId;' declaration, " +
        "found ${idDeclarations} in ${entityFile.path}"
    )
}

// No id spelling may leak as a regular attribute.
['iD', 'ID', 'Id'].each { String leaked ->
    def matches = entitySource.findAll(
        /(?m)^\s*private\s+[A-Za-z0-9_.<>\[\]]+\s+${leaked}\s*[;=]/
    )
    if (!matches.isEmpty()) {
        throw new IllegalStateException(
            "verify.groovy: the id spelling '${leaked}' leaked as a regular attribute " +
            "in ${entityFile.path}: ${matches.join(' | ')}"
        )
    }
}

// The non-id attribute of the list MUST still be rendered.
if (!entitySource.findAll(/(?m)^\s*private\s+String\s+name\s*;/)) {
    throw new IllegalStateException(
        "verify.groovy: the non-id attribute 'name' was not rendered in ${entityFile.path}"
    )
}

return null
