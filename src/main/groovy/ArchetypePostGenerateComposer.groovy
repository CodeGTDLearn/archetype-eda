/**
 * ArchetypePostGenerateComposer -- Compositor Nativo Groovy para archetype-post-generate.groovy.
 *
 * Substitui o bash pre-processor bash_script/create_archetype_post_generate.sh
 * (que dependia de sed -i, tr, ls -1, e travava com read -n 1 -s) por
 * composicao controlada em Groovy puro, executada em build-time na fase
 * process-resources via org.codehaus.gmaven:groovy-maven-plugin:2.1.1.
 *
 * Estrategia (Opcao B do concept.md):
 *   1. Declara uma SECTIONS (fonte unica da verdade da ordem) no topo do script.
 *      Cada entrada tem comentario explicativo.
 *   2. Le os arquivos de body/ na ordem canonica; le variables/, generators/ e
 *      snippets/ em ordem alfabetica (os nomes carregam o NN da ordem).
 *   3. Aplica dois regexes de imports (espaco-inicio-import e
 *      espaco-inicio-barra-barra-espaco-import) para consolidar.
 *   4. Prepende archetype_post_generate_body/body-01-imports.groovy
 *      (com replaceAll barra barra vazio em cada linha, espelhando o
 *      tr -d barra do bash legado) no topo do arquivo final.
 *   5. Concatena tudo e escreve em
 *      target/classes/META-INF/archetype-post-generate.groovy.
 *
 * Garantia historica: o output deste script foi validado como identico
 * byte-a-byte a saida do bash_script/create_archetype_post_generate.sh
 * (script que nao existe mais no repositorio). O criterio atual de regressao
 * e o ComposerTest. Ver specs/002-native-groovy-composer/quickstart.md.
 *
 * Origem: .specify/assessments/groovy-snippet-injection/.
 *
 * @author edufi
 */

import java.util.regex.Pattern

// ---------------------------------------------------------------------------
// SECTIONS -- Fonte da verdade da ordem (FR-006; data-model.md "Composer Manifest")
// ---------------------------------------------------------------------------
// Cada entrada referencia um arquivo de body/, ou um marcador especial
// (VARIABLES / GENERATORS / SNIPPETS) que expande para todos os arquivos da
// pasta correspondente em ordem alfabética (os nomes carregam o NN da ordem).
//
// Esta é a única estrutura de dados que define a ordem do output. Editar aqui
// para reorganizar o arquivo gerado. Cada entrada tem um comentário curto
// explicando o papel.
//
// Para adicionar uma nova flag + snippet:
//   1. Criar `property-NN-xxx.xml` em `archetype_metadata_required_properties/`.
//   2. Criar `variables-NN-xxx.groovy` em `archetype_post_generate_variables/`.
//   3. Criar `snippet-NN-xxx.groovy` em `archetype_post_generate_snippets/`
//      (ou `generator-NN-xxx.groovy` em `..._generators/` quando ele GERA fontes)
//      com o marcador `// <ARQUIVO-GROOVY>` no topo.
//   4. Atualizar este SECTIONS se a ordem numérica não for suficiente.
//
// Os arquivos de body/ carregam o NN da MESMA forma (body-NN-<papel>), mas
// aqui o NN é DOCUMENTAL: a autoridade da ordem é esta lista. Renomear um
// arquivo sem atualizar o SECTIONS não passa despercebido — ComposerTest 7.x
// falha se o diretório e a lista divergirem, se a ordem não for crescente pelo
// NN ou se faltar o marcador. `body-01-imports.groovy` é o prelúdio
// prependido (com `tr -d '/'`), não tem marcador e NÃO está nesta lista: por
// isso a sequência abaixo começa em body-02.
// ---------------------------------------------------------------------------
final List<String> SECTIONS = [
    "body-02-header.groovy",                       // 02: cabeçalho inicial (Properties properties = request.properties)
    "body-03-logging-variables.groovy",            // 03: infraestrutura do log (log_text/header/footer)
    "body-04-global-variables.groovy",             // 04: constantes de domínio (project_package, folders)
    "body-05-path-root-variables.groovy",          // 05: Path do projeto (projectRootFolder + ELK/git/docker)
    "body-06-path-src-main-variables.groovy",      // 06: Paths de src/main/java e src/main/resources
    "body-07-path-src-test-variables.groovy",      // 07: Paths de src/test/java e src/test/resources
    "body-08-path-logback-variables.groovy",       // 08: Paths de logback-spring-config (appenders)
    "body-09-path-security-variables.groovy",      // 09: Paths de security (appJwt.key/pub, folderJwtPath)
    "body-10-binding-artifact-registry.groovy",    // 10: inventário dos artefatos de binding (dados)
    "VARIABLES",                                        // (dinâmico) arquivos variables-*.groovy (ordem numérica)
    "GENERATORS",                                       // (dinâmico) arquivos generator-*.groovy (criam fontes Java)
    "SNIPPETS",                                         // (dinâmico) arquivos snippet-*.groovy (podam/renomeiam)
    "body-11-footer.groovy",                       // 11: log de fim do pos-processor
    "body-12-file-functions.groovy",               // 12: helpers (deleteFile, renameFile, copyFile)
    "body-13-folder-functions.groovy",             // 13: helpers (deleteFolder, renameFolder, etc.)
    "body-14-java-source-functions.groovy",        // 14: helpers de geração de fontes Java
    "body-15-elk-functions.groovy"                 // 15: helper único dos artefatos ELK
]

// ---------------------------------------------------------------------------
// Paths
// ---------------------------------------------------------------------------
def baseDir = new File("src/main/resources/META-INF")
def bodyDir = new File(baseDir, "archetype_post_generate_body")
def variablesDir = new File(baseDir, "archetype_post_generate_variables")
def snippetsDir = new File(baseDir, "archetype_post_generate_snippets")
def generatorsDir = new File(baseDir, "archetype_post_generate_generators")
def importsFile = new File(bodyDir, "body-01-imports.groovy")
def outputFile = new File("target/classes/META-INF/archetype-post-generate.groovy")

// ---------------------------------------------------------------------------
// Regex de consolidacao de imports (research.md Decisao 3)
// ---------------------------------------------------------------------------
// Remove linhas que comecam com `import` (espaco opcional) ou
// `//import` / `// import` (com espaco opcional). Equivalente ao
// remove_lines_with_import_keyword do bash legado.
//
// Estrategia: quebrar em linhas, filtrar, juntar. Isso preserva
// o conteudo das linhas que NAO sao imports, e remove por completo
// as linhas que sao.
def removeImportLines(content) {
    def lines = content.readLines()
    def filtered = lines.findAll { line ->
        !(line ==~ /^\s*import.*/ || line ==~ /^\s*\/\/\s*import.*/)
    }
    return filtered.join("\n") + "\n"
}

// ---------------------------------------------------------------------------
// Sanity check: arquivos obrigatórios de body/ devem existir
// ---------------------------------------------------------------------------
// A lista é DERIVADA do SECTIONS: os marcadores VARIABLES/GENERATORS/SNIPPETS
// são pastas dinâmicas, não arquivos de body/. Assim não há lista duplicada
// para sair de sincronia com a ordem canônica.
final List<String> DYNAMIC_SECTIONS = ["VARIABLES", "GENERATORS", "SNIPPETS"]
def requiredBodyFiles = SECTIONS.findAll { !DYNAMIC_SECTIONS.contains(it) }
requiredBodyFiles.each { name ->
    def f = new File(bodyDir, name)
    if (!f.exists()) {
        throw new IllegalStateException("Required body file missing: ${f.path}")
    }
}
// O prelúdio de imports NÃO está no SECTIONS (é prependido separadamente), por
// isso a existência dele é checada aqui de forma explícita.
if (!importsFile.isFile()) {
    throw new IllegalStateException("Required body file missing: ${importsFile.path}")
}

// ---------------------------------------------------------------------------
// Imports: o preludio e a fonte unica dos imports ativos
// ---------------------------------------------------------------------------
// removeImportLines() descarta qualquer import dos fragmentos. Para evitar que
// um import novo seja descartado em silencio, todo import ativo encontrado nos
// fragmentos precisa estar declarado em body-01-imports.groovy.
def declaredImports = importsFile.readLines("UTF-8")
        .collect { it.replaceAll("/", "").trim() }
        .findAll { it ==~ /^import\s+\S.*$/ }
        .toSet()
def validateImports = { File f ->
    f.readLines("UTF-8")
            .findAll { it ==~ /^\s*import\s+\S.*$/ }
            .each { String line ->
                String normalized = line.trim()
                if (!declaredImports.contains(normalized)) {
                    throw new IllegalStateException(
                            "Import '${normalized}' in '${f.name}' is not declared in ${importsFile.name}")
                }
            }
}

if (variablesDir.listFiles() == null || variablesDir.listFiles().length == 0) {
    throw new IllegalStateException("Variables directory is empty: ${variablesDir.path}")
}
if (generatorsDir.listFiles() == null || generatorsDir.listFiles().length == 0) {
    throw new IllegalStateException("Generators directory is empty: ${generatorsDir.path}")
}
if (snippetsDir.listFiles() == null || snippetsDir.listFiles().length == 0) {
    throw new IllegalStateException("Snippets directory is empty: ${snippetsDir.path}")
}

// Cada asset declara o próprio marcador `// <ARQUIVO-GROOVY>` (mesmo contrato
// que o ComposerTest verifica no output). Falha cedo, no build, se faltar.
def validateAssetMarkers = { File dir, String label ->
    dir.listFiles()
            .findAll { it.isFile() && it.name.endsWith(".groovy") }
            .each { File f ->
                String expected = "// " + f.name.toUpperCase(Locale.ROOT)
                if (!f.getText("UTF-8").contains(expected)) {
                    throw new IllegalStateException(
                            "${label} file '${f.name}' is missing its composer marker: ${expected}")
                }
            }
}
validateAssetMarkers(variablesDir, "Variables")
validateAssetMarkers(generatorsDir, "Generators")
validateAssetMarkers(snippetsDir, "Snippets")

// O body segue o mesmo contrato de marcador, com uma exceção deliberada:
// body-01-imports.groovy é o prelúdio prependido e não pode ter marcador
// (ele seria a primeira linha do arquivo composto e quebraria o contrato de
// ComposerTest 3.1/3.2: imports nas primeiras linhas).
SECTIONS.findAll { !DYNAMIC_SECTIONS.contains(it) }.each { String name ->
    File f = new File(bodyDir, name)
    String expected = "// " + f.name.toUpperCase(Locale.ROOT)
    if (!f.getText("UTF-8").contains(expected)) {
        throw new IllegalStateException(
                "Body file '${f.name}' is missing its composer marker: ${expected}")
    }
}

// ---------------------------------------------------------------------------
// Compose
// ---------------------------------------------------------------------------
def sb = new StringBuilder()

// 1. Prepend imports (espelha o tr -d '/' aplicado a body-01-imports.groovy
//    no bash legado -- cada linha tem seus / removidos).
importsFile.eachLine("UTF-8") { line ->
    sb.append(line.replaceAll("/", "")).append("\n")
}

// 2. Concatenar secoes em ordem (SECTIONS). Sem cabecalhos extras para
//    preservar o contrato do ComposerTest.
SECTIONS.each { section ->
    if (DYNAMIC_SECTIONS.contains(section)) {
        // Pasta dinâmica: variables/, generators/ ou snippets/, ordem alfabética
        // -- os nomes carregam o NN, então a ordem alfabética é a ordem pretendida.
        def dir = (section == "VARIABLES") ? variablesDir : ((section == "GENERATORS") ? generatorsDir : snippetsDir)
        def files = dir.listFiles().findAll { it.isFile() && it.name.endsWith(".groovy") }.sort { it.name }
        files.each { f ->
            validateImports(f)
            sb.append(removeImportLines(f.getText("UTF-8")))
        }
    } else {
        // Arquivo unico de body/ na ordem canonica.
        def f = new File(bodyDir, section)
        if (!f.exists()) {
            throw new IllegalStateException("Body file declared in SECTIONS does not exist: ${f.path}")
        }
        validateImports(f)
        sb.append(removeImportLines(f.getText("UTF-8")))
    }
}

// ---------------------------------------------------------------------------
// Write
// ---------------------------------------------------------------------------
outputFile.parentFile.mkdirs()
outputFile.setText(sb.toString(), "UTF-8")

def groovyCount = { File dir ->
    dir.listFiles().findAll { it.isFile() && it.name.endsWith(".groovy") }.size()
}
println "[ArchetypePostGenerateComposer] Wrote ${outputFile.path} (${sb.toString().length()} chars)"
println "[ArchetypePostGenerateComposer] Body files: ${requiredBodyFiles.size()}, Variables: ${groovyCount(variablesDir)}, Generators: ${groovyCount(generatorsDir)}, Snippets: ${groovyCount(snippetsDir)}"
