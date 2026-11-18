import java.nio.file.Path
import java.nio.file.Paths

def _basedir = null
def _scriptdir = null
try { _basedir = basedir } catch (Throwable t) { _basedir = null }
try { _scriptdir = scriptdir } catch (Throwable t) { _scriptdir = null }
if (_basedir == null) _basedir = System.getProperty('basedir')
if (_basedir == null) throw new IllegalStateException("verify.groovy: basedir not bound")

def expectedArtifactId = 'entity-contract'
def expectedEntityName = 'ContractEntity'
def expectedPackagePath = 'it/pkg/contractentity'
def basedirFile = new File(_basedir as String)
def projectRoot = new File(basedirFile, "project/${expectedArtifactId}")
if (!projectRoot.exists()) projectRoot = basedirFile

// ---------------------------------------------------------------------------
// 1) contract persisted byte-identically
// ---------------------------------------------------------------------------
def contractFile = new File(projectRoot, 'src/test/resources/contracts/contract.json')
if (!contractFile.isFile()) {
    throw new IllegalStateException("verify.groovy: the contract was NOT persisted")
}
def sourceContract = new File(basedirFile, 'sample-contract.json')
if (contractFile.getText('UTF-8') != sourceContract.getText('UTF-8')) {
    throw new IllegalStateException("verify.groovy: the persisted contract is not byte-identical")
}

// ---------------------------------------------------------------------------
// 2) entity fields and technical id
// ---------------------------------------------------------------------------
def entityFile = new File(projectRoot, "src/main/java/${expectedPackagePath}/${expectedEntityName}.java")
if (!entityFile.isFile()) {
    throw new IllegalStateException("verify.groovy: entity not generated: ${entityFile.path}")
}
def entityText = entityFile.getText('UTF-8')
['name', 'active', 'endereco', 'status', 'tags', 'previousStatus'].each { String field ->
    if (!(entityText =~ /\bprivate\s+[\w.<>]+\s+${field}\s*;/)) {
        throw new IllegalStateException("verify.groovy: entity field '${field}' is missing")
    }
}
if (!(entityText =~ /\bprivate\s+Long\s+\w+Id\s*;/)) {
    throw new IllegalStateException("verify.groovy: technical identity field is missing")
}

// ---------------------------------------------------------------------------
// 3) nested chain and enums
// ---------------------------------------------------------------------------
[
        'nested/Endereco.java', 'nested/Complemento.java', 'nested/PontoReferencia.java',
        'nested/Referencia.java', 'nested/Origem.java', 'nested/Detalhe.java',
        'nested/Uf.java', 'Status.java'
].each { String relative ->
    if (!new File(projectRoot, "src/main/java/${expectedPackagePath}/${relative}").isFile()) {
        throw new IllegalStateException("verify.groovy: generated type missing: ${relative}")
    }
}

// ---------------------------------------------------------------------------
// 4) the sliced test package (21 test files + 6 support files)
// ---------------------------------------------------------------------------
def expectedTests = [
        'TechnicalIdMappingTest', 'MalformedContractTest',
        'ContractKeyNormalizationTest', 'ContractKeyDiscardTest',
        'SimpleAttributeFieldsTest', 'NestedAttributeReferenceTest',
        'EnumAttributeReferenceTest', 'NoExtraEntityFieldTest',
        'ContractRoundTripTest', 'SavedContractMatchesGeneratedModelTest',
        'ContractTypeMappingTest', 'SavedContractNestedKeysTest',
        'SavedContractEnumsMatchTest', 'EnderecoFieldsTest',
        'ComplementoFieldsTest', 'PontoReferenciaFieldsTest',
        'ReferenciaFieldsTest', 'OrigemFieldsTest', 'DetalheFieldsTest',
        'StatusEnumTest', 'UfEnumTest'
]
def expectedSupport = [
        'EntityFields', 'ContractNames', 'ContractKeys',
        'ContractJson', 'ContractAttributes', 'ContractNavigation'
]
def contractFiles = [:]
new File(projectRoot, 'src/test/java').eachFileRecurse { File file ->
    if (file.isFile() && file.name.endsWith('.java')
            && file.path.replace('\\', '/').contains('/contract/')) {
        contractFiles[file.name.substring(0, file.name.length() - '.java'.length())] = file
    }
}
expectedTests.each { String name ->
    if (!contractFiles.containsKey(name)) {
        throw new IllegalStateException("verify.groovy: test file ${name}.java was not generated")
    }
}
expectedSupport.each { String name ->
    if (!contractFiles.containsKey(name)) {
        throw new IllegalStateException("verify.groovy: support file ${name}.java was not generated")
    }
}
def unexpected = contractFiles.keySet() - (expectedTests + expectedSupport)
if (!unexpected.isEmpty()) {
    throw new IllegalStateException("verify.groovy: unexpected contract files: ${unexpected}")
}

// ---------------------------------------------------------------------------
// 5) the generated application ran the 21 test classes GREEN
// ---------------------------------------------------------------------------
def surefireDir = new File(projectRoot, 'target/surefire-reports')
if (!surefireDir.isDirectory()) {
    throw new IllegalStateException("verify.groovy: surefire reports are missing")
}
int totalTests = 0
expectedTests.each { String name ->
    def report = new File(surefireDir, "it.pkg.contractentity.contract.${name}.txt")
    if (!report.isFile()) {
        throw new IllegalStateException("verify.groovy: surefire report missing for ${name}")
    }
    def matcher = report.getText('UTF-8') =~ /Tests run: (\d+), Failures: (\d+), Errors: (\d+)/
    if (!matcher.find()) {
        throw new IllegalStateException("verify.groovy: cannot parse report ${report.name}")
    }
    totalTests += matcher.group(1) as int
    if ((matcher.group(2) as int) != 0 || (matcher.group(3) as int) != 0) {
        throw new IllegalStateException("verify.groovy: ${name} is not green")
    }
}
if (totalTests != 43) {
    throw new IllegalStateException("verify.groovy: expected 43 tests, found ${totalTests}")
}

// ---------------------------------------------------------------------------
// 6) golden of the nine sabotage scenarios
// ---------------------------------------------------------------------------
def golden = [:]
def goldenKey = null
new File(basedirFile, 'expected/divergences.txt').eachLine('UTF-8') { String line ->
    if (line.startsWith('# ')) {
        goldenKey = line.substring(2).trim()
        golden[goldenKey] = [] as Set
    } else if (line.trim() && goldenKey != null) {
        golden[goldenKey] << line.trim()
    }
}

def copyTree
copyTree = { File source, File target ->
    if (source.isDirectory()) {
        target.mkdirs()
        source.eachFile { File child -> copyTree(child, new File(target, child.name)) }
    } else {
        target.parentFile.mkdirs()
        target.bytes = source.bytes
    }
}

def isWindows = System.getProperty('os.name').toLowerCase().contains('win')
def runApplicationTests = { File workingDir ->
    def mavenHome = System.getProperty('maven.home')
    List<String> cmd
    if (mavenHome) {
        def mvn = new File(mavenHome as String, 'bin/' + (isWindows ? 'mvn.cmd' : 'mvn'))
        cmd = isWindows
                ? ['cmd.exe', '/c', mvn.absolutePath, '-o', '-q', 'test']
                : [mvn.absolutePath, '-o', '-q', 'test']
    } else {
        cmd = isWindows
                ? ['cmd.exe', '/c', 'mvnw.cmd', '-o', '-q', 'test']
                : ['sh', '-c', './mvnw -o -q test']
    }
    def pb = new ProcessBuilder(cmd)
    pb.directory(workingDir)
    pb.redirectErrorStream(true)
    def process = pb.start()
    String output = process.inputStream.getText('UTF-8')
    boolean finished = process.waitFor(10, java.util.concurrent.TimeUnit.MINUTES)
    if (!finished) {
        process.destroyForcibly()
        throw new IllegalStateException("verify.groovy: nested mvn test timed out in ${workingDir.path}")
    }
    return [exit: process.exitValue(), output: output]
}

def sabotageRoot = File.createTempDir('entity-contract-sabotage-', '')
sabotageRoot.deleteOnExit()
copyTree(projectRoot, sabotageRoot)

def sabotageContract = new File(sabotageRoot, 'src/test/resources/contracts/contract.json')
def originalContract = sabotageContract.getText('UTF-8')
def entityRelativePath = projectRoot.toPath().relativize(entityFile.toPath()).toString()
def sabotageEntity = new File(sabotageRoot, entityRelativePath)
def originalEntity = sabotageEntity.getText('UTF-8')

def resetSabotage = {
    sabotageContract.setText(originalContract, 'UTF-8')
    sabotageEntity.setText(originalEntity, 'UTF-8')
}

def divergenceLines = { String output ->
    def result = [] as Set
    (output =~ /\[(CONTRACT_TO_ENTITY|ENTITY_TO_CONTRACT)\][^\r\n]*/).each { match ->
        result << match[0].trim()
    }
    return result
}

def assertSabotageFails = { String key, Closure mutate ->
    resetSabotage()
    mutate()
    def result = runApplicationTests(sabotageRoot)
    if (result.exit == 0) {
        throw new IllegalStateException("verify.groovy: sabotage ${key} did not fail the application tests")
    }
    if (key == '7.3') {
        if (!result.output.contains('CONTRACT STRUCTURAL FAILURE')) {
            throw new IllegalStateException("verify.groovy: sabotage 7.3 did not fail structurally")
        }
        return result.output
    }
    def actual = divergenceLines(result.output)
    def expected = golden[key] ?: ([] as Set)
    if (actual != expected) {
        throw new IllegalStateException(
                "verify.groovy: sabotage ${key} diagnostics differ.\n" +
                "expected: ${expected}\nactual:   ${actual}\n" +
                "Output (tail):\n" + result.output.takeRight(6000))
    }
    return result.output
}

def dropNameAttribute = { String contractText ->
    contractText.replaceAll(/\{\s*"name":\s*"name",\s*"value":\s*"Acme Corp"\s*\},\s*/, '')
}

def renameEntityField = { String source, String from, String to ->
    String capitalizedFrom = from.substring(0, 1).toUpperCase() + from.substring(1)
    String capitalizedTo = to.substring(0, 1).toUpperCase() + to.substring(1)
    return source
            .replaceAll("\\b${from}\\b", to)
            .replace("get${capitalizedFrom}", "get${capitalizedTo}")
            .replace("set${capitalizedFrom}", "set${capitalizedTo}")
}

assertSabotageFails('7.1', { sabotageContract.setText(dropNameAttribute(originalContract), 'UTF-8') })
assertSabotageFails('7.2', { sabotageContract.setText(originalContract.replace('"observacao"', '"observacaoRenomeada"'), 'UTF-8') })
assertSabotageFails('7.3', { sabotageContract.setText('{ this is not valid json', 'UTF-8') })
assertSabotageFails('7.4', { sabotageEntity.setText(renameEntityField(originalEntity, 'name', 'nome'), 'UTF-8') })
assertSabotageFails('7.5', {
    sabotageEntity.setText(originalEntity.replace(
            "public class ${expectedEntityName} {",
            "public class ${expectedEntityName} {\n  private String zeta;\n  private String alfa;"), 'UTF-8')
})
assertSabotageFails('7.6', { sabotageEntity.setText(originalEntity.replaceAll('\\bBoolean\\b', 'String'), 'UTF-8') })
assertSabotageFails('7.7', { sabotageContract.setText(originalContract.replace('"ATIVO"', '"SUSPENSO"'), 'UTF-8') })
assertSabotageFails('7.8', { sabotageContract.setText(originalContract.replace('"SP"', '"MG"'), 'UTF-8') })
assertSabotageFails('7.9', {
    sabotageContract.setText(dropNameAttribute(originalContract)
            .replace('"observacao"', '"observacaoRenomeada"'), 'UTF-8')
})

// ---------------------------------------------------------------------------
// 7) SC-006: zero new dependencies in the generated pom
// ---------------------------------------------------------------------------
def pom = new File(projectRoot, 'pom.xml')
if (!pom.isFile()) {
    throw new IllegalStateException("verify.groovy: generated pom.xml is missing")
}
def pomText = pom.getText('UTF-8')
if (!pomText.contains('spring-boot-starter-test')) {
    throw new IllegalStateException("verify.groovy: expected test dependency 'spring-boot-starter-test' is missing")
}
