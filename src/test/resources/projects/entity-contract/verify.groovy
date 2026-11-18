/**
 * verify.groovy - FR-023 (contract persistence) + Frente 01/Frente 02 for the
 * `entity-contract` profile.
 *
 * Asserts:
 *   1. src/test/resources/contracts/contract.json EXISTS and is byte-identical
 *      to the source contract;
 *   2. the generated entity matches the contract (id + 6 nested POJOs chain +
 *      top-level and nested enums);
 *   3. the schema test package src/test/java/it/pkg/contractentity/contract/
 *      EXISTS with the 6 generated test classes;
 *   4. the application's own surefire report registers
 *      ContractEntityContractTest with zero failures/errors;
 *   5. SABOTAGE (contract): tampering the persisted contract (drop a key /
 *      rename the deepest nested key / corrupt the JSON) makes the
 *      APPLICATION'S test run fail with the diagnostic message;
 *   6. SABOTAGE (entity): removing/renaming a field, adding a clandestine
 *      field and changing a field type also fail the application's own test
 *      run with the direction and the expected/found types;
 *   7. SABOTAGE (enums): renaming a top-level and a nested enum constant in
 *      the contract fails with ENUM_CONSTANT_MISMATCH and the full path;
 *   8. SABOTAGE (aggregation): two simultaneous divergences are reported in a
 *      SINGLE deterministic failure with every item;
 *   9. SC-006: the generated pom.xml carries exactly the entity-contract
 *      dependency set, a subset of the archetype template (zero new deps);
 *      the proof is saved to <repo>/target/baseline-pom-entity-contract.xml;
 *  10. SC-005: with the same declared fields and Lombok generating the
 *      boilerplate, the gate stays green and the same sabotage yields the
 *      same diagnostic set (no 23rd fixture).
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
    [name: 'contractEntityId', type: 'Long'],
    [name: 'name',   type: 'String'],
    [name: 'active', type: 'Boolean'],
    [name: 'tags',   type: 'String'],
]

def testPackagePath = 'contractentity'

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
if (!(entitySource =~ /(?s)@Transient\s+private\s+it\.pkg\.contractentity\.nested\.Endereco\s+endereco\s*;/).find()) {
    throw new IllegalStateException(
        "verify.groovy: the entity does not reference the nested Endereco with @Transient (7 levels chain)"
    )
}
if (!(entitySource =~ /private\s+Status\s+status\s*;/).find()) {
    throw new IllegalStateException(
        "verify.groovy: the entity does not declare the top-level enum field 'status'"
    )
}
if (!(entitySource =~ /private\s+Status\s+previousStatus\s*;/).find()) {
    throw new IllegalStateException(
        "verify.groovy: the entity does not declare the second field of the aggregated enum 'previousStatus'"
    )
}

// ---------------------------------------------------------------------------
// 4) The nested POJO chain and the enums MUST exist (7 levels, 6 POJOs)
// ---------------------------------------------------------------------------
def nestedPath = "src/main/java/${expectedPackagePath}/nested"
['Endereco', 'Complemento', 'PontoReferencia', 'Referencia', 'Origem', 'Detalhe'].each { type ->
    def pojo = new File(projectRoot, "${nestedPath}/${type}.java")
    if (!pojo.isFile()) {
        throw new IllegalStateException("verify.groovy: nested POJO not found: ${pojo.path}")
    }
}
def enderecoSource = new File(projectRoot, "${nestedPath}/Endereco.java").text
if (!(enderecoSource =~ /private\s+Uf\s+uf\s*;/).find()) {
    throw new IllegalStateException("verify.groovy: nested Endereco lost the nested enum field 'uf'")
}
if (!new File(projectRoot, "src/main/java/${expectedPackagePath}/Status.java").isFile()) {
    throw new IllegalStateException("verify.groovy: top-level enum Status was not generated")
}
if (!new File(projectRoot, "${nestedPath}/Uf.java").isFile()) {
    throw new IllegalStateException("verify.groovy: nested enum Uf was not generated")
}

// ---------------------------------------------------------------------------
// 5) The schema test package MUST be present in the generated application
// ---------------------------------------------------------------------------
def schemaTestPackage = new File(projectRoot, "src/test/java/${testPackagePath}/contract")
if (!schemaTestPackage.isDirectory()) {
    throw new IllegalStateException(
        "verify.groovy: the contract schema test package was not generated: ${schemaTestPackage.path}"
    )
}
[
        "${expectedEntityName}ContractTest.java",
        "${expectedEntityName}ContractConformanceTest.java",
        "${expectedEntityName}ContractCoverageTest.java",
        'ContractNestedTypesTest.java',
        'ContractEnumsTest.java',
        'ContractModel.java',
        'ContractDiagnostics.java'
].each { name ->
    if (!new File(schemaTestPackage, name).isFile()) {
        throw new IllegalStateException(
            "verify.groovy: schema test file not generated: ${new File(schemaTestPackage, name).path}"
        )
    }
}

// ---------------------------------------------------------------------------
// 6) The generated application ran the schema test GREEN (its own surefire)
// ---------------------------------------------------------------------------
def surefireDir = new File(projectRoot, 'target/surefire-reports')
if (!surefireDir.isDirectory()) {
    throw new IllegalStateException(
        "verify.groovy: the generated application has no surefire reports: ${surefireDir.path}"
    )
}
def schemaReports = surefireDir.listFiles().findAll {
    it.isFile() && it.name.endsWith('.txt') && it.name.contains("${expectedEntityName}ContractTest")
}
if (schemaReports.isEmpty()) {
    throw new IllegalStateException(
        "verify.groovy: no surefire report for ${expectedEntityName}ContractTest in ${surefireDir.path}"
    )
}
schemaReports.each { report ->
    String text = report.getText('UTF-8')
    if (!(text =~ /Tests run: \d+, Failures: 0, Errors: 0/)) {
        throw new IllegalStateException(
            "verify.groovy: the schema test did not run green in the generated application: ${report.path}\n${text}"
        )
    }
}

// ---------------------------------------------------------------------------
// 7) SABOTAGE: the application's OWN test run must fail, with a diagnostic
// ---------------------------------------------------------------------------
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

def assertSabotageFails = { String scenario, Closure mutate, List<String> expectedFragments ->
    resetSabotage()
    mutate()
    def result = runApplicationTests(sabotageRoot)
    if (result.exit == 0) {
        throw new IllegalStateException(
            "verify.groovy: sabotage '${scenario}' did NOT fail the generated application's tests"
        )
    }
    expectedFragments.each { String fragment ->
        if (!result.output.contains(fragment)) {
            throw new IllegalStateException(
                "verify.groovy: sabotage '${scenario}' failed without the expected diagnostic '${fragment}'.\n" +
                "Output (tail):\n" + result.output.takeRight(4000)
            )
        }
    }
    return result.output
}

def dropNameAttribute = { String contractText ->
    contractText.replaceAll(/\{\s*"name":\s*"name",\s*"value":\s*"Acme Corp"\s*\},\s*/, '')
}

// A developer renaming a field renames its accessors too. Without this the
// Lombok-off entity keeps getName()/setName() over the renamed field, Jackson
// still sees the old property and the round-trip hides the drift.
def renameEntityField = { String source, String from, String to ->
    String capitalizedFrom = from.substring(0, 1).toUpperCase() + from.substring(1)
    String capitalizedTo = to.substring(0, 1).toUpperCase() + to.substring(1)
    return source
            .replaceAll("\\b${from}\\b", to)
            .replace("get${capitalizedFrom}", "get${capitalizedTo}")
            .replace("set${capitalizedFrom}", "set${capitalizedTo}")
}

def aggregateBlock = { String output, String testName ->
    def block = output.split('CONTRACT DRIFT DETECTED').find {
        it.contains("test    : ${testName}")
    }
    if (block == null) {
        throw new IllegalStateException(
            "verify.groovy: the aggregated failure of '${testName}' was not found in the test output")
    }
    return block
}

// 7.1 drop a top-level contract key -> entity->contract drift
assertSabotageFails(
        'drop the name attribute',
        { sabotageContract.setText(dropNameAttribute(originalContract), 'UTF-8') },
        ['[ENTITY_TO_CONTRACT] name']
)

// 7.2 rename the deepest nested key -> contract->entity drift at level 7
assertSabotageFails(
        'rename the deepest nested key',
        { sabotageContract.setText(originalContract.replace('"observacao"', '"observacaoRenomeada"'), 'UTF-8') },
        ['endereco.complemento.pontoReferencia.referencia.origem.detalhe.observacaoRenomeada']
)

// 7.3 corrupt the contract JSON -> structural failure
assertSabotageFails(
        'corrupt the contract JSON',
        { sabotageContract.setText('{ this is not valid json', 'UTF-8') },
        ['CONTRACT STRUCTURAL FAILURE']
)

// 7.4 ENTITY sabotage: remove/rename a declared field -> contract->entity missing
def lombokOffRenameOutput = assertSabotageFails(
        'rename the entity field name to nome',
        { sabotageEntity.setText(renameEntityField(originalEntity, 'name', 'nome'), 'UTF-8') },
        ['[CONTRACT_TO_ENTITY] name | expected: String | found: absent | kind: MISSING_FIELD',
         '[ENTITY_TO_CONTRACT] nome']
)

// 7.5 ENTITY sabotage: clandestine fields fail entity->contract, sorted by name
def clandestineOutput = assertSabotageFails(
        'add two clandestine entity fields in reverse alphabetical order',
        {
            sabotageEntity.setText(originalEntity.replace(
                    "public class ${expectedEntityName} {",
                    "public class ${expectedEntityName} {\n  private String zeta;\n  private String alfa;"),
                    'UTF-8')
        },
        ['[ENTITY_TO_CONTRACT] alfa | expected: absent | found: String | kind: EXTRA_FIELD',
         '[ENTITY_TO_CONTRACT] zeta | expected: absent | found: String | kind: EXTRA_FIELD']
)
def clandestineAggregate = aggregateBlock(
        clandestineOutput, "${expectedEntityName}ContractTest.entityAndContractAreFullyAligned")
if (clandestineAggregate.indexOf('[ENTITY_TO_CONTRACT] alfa') >
        clandestineAggregate.indexOf('[ENTITY_TO_CONTRACT] zeta')) {
    throw new IllegalStateException(
        "verify.groovy: entity->contract extras are not sorted by name:\n" +
        clandestineAggregate.take(4000))
}

// 7.6 ENTITY sabotage: change a field type -> expected/found type mismatch
assertSabotageFails(
        'change the entity field type Boolean to String',
        { sabotageEntity.setText(originalEntity.replaceAll('\\bBoolean\\b', 'String'), 'UTF-8') },
        ['[CONTRACT_TO_ENTITY] active | expected: Boolean | found: String | kind: TYPE_MISMATCH']
)

// 7.7 ENUM sabotage: rename the top-level enum constant in the contract
assertSabotageFails(
        'rename the top-level enum constant ATIVO to SUSPENSO',
        { sabotageContract.setText(originalContract.replace('"ATIVO"', '"SUSPENSO"'), 'UTF-8') },
        ['[CONTRACT_TO_ENTITY] status',
         'kind: ENUM_CONSTANT_MISMATCH']
)

// 7.8 ENUM sabotage: rename the nested enum constant in the contract
assertSabotageFails(
        'rename the nested enum constant SP to MG',
        { sabotageContract.setText(originalContract.replace('"SP"', '"MG"'), 'UTF-8') },
        ['[CONTRACT_TO_ENTITY] endereco.uf',
         'kind: ENUM_CONSTANT_MISMATCH']
)

// 7.9 AGGREGATION: two simultaneous divergences in ONE deterministic failure
def multipleContract = dropNameAttribute(originalContract)
        .replace('"observacao"', '"observacaoRenomeada"')
def multipleOutput = assertSabotageFails(
        'drop a key and rename the deepest leaf at the same time',
        { sabotageContract.setText(multipleContract, 'UTF-8') },
        ['[ENTITY_TO_CONTRACT] name',
         'endereco.complemento.pontoReferencia.referencia.origem.detalhe.observacaoRenomeada',
         'total   : 3 divergence(s)']
)
def multipleAggregate = aggregateBlock(
        multipleOutput, "${expectedEntityName}ContractTest.entityAndContractAreFullyAligned")
if (!multipleAggregate.contains('[ENTITY_TO_CONTRACT] name |') ||
        !multipleAggregate.contains('endereco.complemento.pontoReferencia.referencia.origem.detalhe.observacaoRenomeada') ||
        !multipleAggregate.contains('total   : 3 divergence(s)')) {
    throw new IllegalStateException(
        "verify.groovy: two simultaneous divergences were not aggregated into a single failure:\n" +
        multipleAggregate.take(4000))
}

// ---------------------------------------------------------------------------
// 8) SC-006/T002: the generated pom.xml carries ZERO new dependencies
// ---------------------------------------------------------------------------
def parseDependencies = { File pomFile ->
    def pom = new groovy.xml.XmlSlurper(false, false).parse(pomFile)
    return pom.dependencies.dependency.collect { dep ->
        [
                groupId   : dep.groupId.text().trim(),
                artifactId: dep.artifactId.text().trim(),
                scope     : dep.scope.text().trim()
        ]
    }
}

def generatedPom = new File(projectRoot, 'pom.xml')
if (!generatedPom.isFile()) {
    throw new IllegalStateException(
        "verify.groovy: generated pom.xml not found: ${generatedPom.path}")
}

def repoRoot = basedirFile
while (repoRoot != null
        && !new File(repoRoot, 'src/main/resources/archetype-resources/pom.xml').isFile()) {
    repoRoot = repoRoot.parentFile
}
if (repoRoot == null) {
    throw new IllegalStateException(
        "verify.groovy: archetype root not found walking up from ${basedirFile.path}")
}
def templatePom = new File(repoRoot, 'src/main/resources/archetype-resources/pom.xml')

def expectedDependencies = [
        [groupId: 'org.springframework.boot', artifactId: 'spring-boot-starter-data-jpa', scope: ''],
        [groupId: 'org.springframework.boot', artifactId: 'spring-boot-starter-web', scope: ''],
        [groupId: 'com.h2database', artifactId: 'h2', scope: 'runtime'],
        [groupId: 'org.springframework.boot', artifactId: 'spring-boot-starter-test', scope: 'test']
]
def generatedDependencies = parseDependencies(generatedPom)
def templateDependencies = parseDependencies(templatePom)

def coordinates = { List deps ->
    deps.collect { "${it.groupId}:${it.artifactId}:${it.scope}".toString() }.sort()
}
def generatedCoordinates = coordinates(generatedDependencies)
def expectedCoordinates = coordinates(expectedDependencies)
if (generatedCoordinates != expectedCoordinates) {
    throw new IllegalStateException(
        "verify.groovy: the generated pom.xml dependency set drifted from the entity-contract profile.\n" +
        "  expected : ${expectedCoordinates}\n" +
        "  generated: ${generatedCoordinates}"
    )
}
def templateCoordinates = coordinates(templateDependencies) as Set
def newDependencies = generatedCoordinates.findAll { !templateCoordinates.contains(it) }
if (!newDependencies.isEmpty()) {
    throw new IllegalStateException(
        "verify.groovy: the generated pom.xml carries dependencies absent from the template: ${newDependencies}")
}

def baselineFile = new File(new File(repoRoot, 'target'), 'baseline-pom-entity-contract.xml')
baselineFile.parentFile.mkdirs()
def sha256 = { File file ->
    def digest = java.security.MessageDigest.getInstance('SHA-256')
    digest.digest(file.bytes).collect { byte b -> String.format('%02x', b & 0xff) }.join()
}
def baseline = new StringBuilder()
baseline.append('<?xml version="1.0" encoding="UTF-8"?>\n')
baseline.append('<baseline fixture="entity-contract" verdict="zero-new-dependencies">\n')
baseline.append('  <template path="src/main/resources/archetype-resources/pom.xml" sha256="')
        .append(sha256(templatePom)).append('"/>\n')
baseline.append('  <generated path="').append(generatedPom.path.replace('\\', '/')).append('"/>\n')
baseline.append('  <dependencies>\n')
generatedDependencies.each { dep ->
    baseline.append('    <dependency groupId="').append(dep.groupId)
            .append('" artifactId="').append(dep.artifactId)
            .append('" scope="').append(dep.scope).append('"/>\n')
}
baseline.append('  </dependencies>\n')
baseline.append('  <assertion>generated == entity-contract profile set; generated subset of the template</assertion>\n')
baseline.append('</baseline>\n')
baselineFile.setText(baseline.toString(), 'UTF-8')

// ---------------------------------------------------------------------------
// 9) SC-005: Lombok on/off parity of the schema test verdict
//
// No 23rd fixture: copy the SAME generated application, rebuild its entity in
// the Lombok shape (same declared fields, boilerplate moved to @Data /
// @NoArgsConstructor / @AllArgsConstructor), add Lombok to the COPY ONLY, and
// rerun the gate. The verdict (green, and the drift of the same sabotage) must
// be identical to the non-Lombok run above.
// ---------------------------------------------------------------------------
def toLombokSource = { String source, String entityName ->
    List<String> lines = source.readLines()
    String packageLine = lines.find { String l -> l.trim().startsWith('package ') }
    if (packageLine == null) {
        throw new IllegalStateException("verify.groovy: entity source has no package declaration")
    }
    List<String> imports = lines.findAll { String l -> l.trim().startsWith('import ') }
            .collect { String l -> l.trim() }
    ['import lombok.Data;', 'import lombok.NoArgsConstructor;',
     'import lombok.AllArgsConstructor;'].each { String wanted ->
        if (!imports.contains(wanted)) {
            imports << wanted
        }
    }
    int classLine = -1
    for (int i = 0; i < lines.size(); i++) {
        if (lines[i].trim().startsWith("public class ${entityName}")) {
            classLine = i
            break
        }
    }
    if (classLine < 0) {
        throw new IllegalStateException("verify.groovy: entity class declaration not found")
    }
    List<String> classAnnotations = []
    for (int i = classLine - 1; i >= 0; i--) {
        String trimmed = lines[i].trim()
        if (trimmed.isEmpty()) {
            continue
        }
        if (trimmed.startsWith('@')) {
            classAnnotations.add(0, trimmed)
            continue
        }
        break
    }
    List<String> fields = []
    List<String> pendingAnnotations = []
    lines.each { String line ->
        String trimmed = line.trim()
        if (trimmed.startsWith('@')) {
            pendingAnnotations.add(trimmed)
            return
        }
        if (trimmed ==~ /private\s+[A-Za-z0-9_.$]+\s+[A-Za-z_$][A-Za-z0-9_$]*\s*;/) {
            fields.addAll(pendingAnnotations)
            pendingAnnotations.clear()
            fields.add(trimmed)
            return
        }
        if (!trimmed.isEmpty()) {
            pendingAnnotations.clear()
        }
    }
    if (fields.isEmpty()) {
        throw new IllegalStateException("verify.groovy: no declared field found in the entity source")
    }
    StringBuilder out = new StringBuilder()
    out.append(packageLine.trim()).append('\n\n')
    imports.each { String line -> out.append(line).append('\n') }
    out.append('\n')
    classAnnotations.each { String line -> out.append(line).append('\n') }
    out.append('@Data\n@NoArgsConstructor\n@AllArgsConstructor\n')
    out.append('public class ').append(entityName).append(' {\n\n')
    fields.each { String line -> out.append('  ').append(line).append('\n') }
    out.append('}\n')
    return out.toString()
}

def lombokRoot = File.createTempDir('entity-contract-lombok-', '')
lombokRoot.deleteOnExit()
copyTree(projectRoot, lombokRoot)
new File(lombokRoot, 'target').deleteDir()

def lombokEntityFile = new File(lombokRoot, entityRelativePath)
String lombokEntitySource = toLombokSource(originalEntity, expectedEntityName)
lombokEntityFile.setText(lombokEntitySource, 'UTF-8')

def lombokPomFile = new File(lombokRoot, 'pom.xml')
String lombokPomText = lombokPomFile.getText('UTF-8')
if (!lombokPomText.contains('<artifactId>lombok</artifactId>')) {
    String lombokDependency = '        <dependency>\n' +
            '            <groupId>org.projectlombok</groupId>\n' +
            '            <artifactId>lombok</artifactId>\n' +
            '            <optional>true</optional>\n' +
            '        </dependency>\n'
    lombokPomText = lombokPomText.replace('</dependencies>', lombokDependency + '    </dependencies>')
    lombokPomFile.setText(lombokPomText, 'UTF-8')
}

def lombokGreen = runApplicationTests(lombokRoot)
if (lombokGreen.exit != 0) {
    throw new IllegalStateException(
        "verify.groovy: the schema test did NOT stay green with Lombok enabled (SC-005).\n" +
        "Output (tail):\n" + lombokGreen.output.takeRight(6000))
}

lombokEntityFile.setText(renameEntityField(lombokEntitySource, 'name', 'nome'), 'UTF-8')
def lombokRename = runApplicationTests(lombokRoot)
if (lombokRename.exit == 0) {
    throw new IllegalStateException(
        "verify.groovy: sabotage with Lombok enabled did NOT fail the generated application's tests")
}

def normalizeKnownProperties = { String line ->
    def matcher = line =~ /known properties: (.*)\)/
    if (!matcher.find()) {
        return line
    }
    String sortedNames = matcher.group(1).split(', ')
            .collect { String name -> name.trim() }
            .sort()
            .join(', ')
    return line.substring(0, matcher.start(1)) + sortedNames + line.substring(matcher.end(1))
}
def driftItems = { String output ->
    def block = aggregateBlock(output, "${expectedEntityName}ContractTest.entityAndContractAreFullyAligned")
    return block.readLines()
            .findAll { String line -> line.trim().startsWith('- [') }
            .collect { String line -> normalizeKnownProperties(line.trim()) }
}
def lombokOffItems = driftItems(lombokOffRenameOutput)
def lombokOnItems = driftItems(lombokRename.output)
if (lombokOnItems != lombokOffItems) {
    throw new IllegalStateException(
        "verify.groovy: the schema test verdict drifted between Lombok off and Lombok on (SC-005).\n" +
        "  lombok off: ${lombokOffItems}\n" +
        "  lombok on : ${lombokOnItems}")
}

return null
