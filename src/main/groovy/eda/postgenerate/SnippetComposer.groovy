/**
 * SnippetComposer -- Compositor Nativo Groovy para archetype-post-generate.groovy.
 *
 * Substitui o bash pre-processor bash_script/create_archetype_post_generate.sh
 * (que dependia de sed -i, tr, ls -1, e travava com read -n 1 -s) por
 * composicao controlada em Groovy puro, executada em build-time na fase
 * process-resources via org.codehaus.gmaven:groovy-maven-plugin:2.1.1.
 *
 * Estrategia (Opcao B do concept.md):
 *   1. Declara uma SECTIONS (fonte unica da verdade da ordem) no topo do script.
 *      Cada entrada tem comentario explicativo.
 *   2. Le os 11 arquivos de body/ na ordem canonica; le variables/ e snippets/
 *      em ordem alfabetica (equivalente a ls -1).
 *   3. Aplica dois regexes de imports (espaco-inicio-import e
 *      espaco-inicio-barra-barra-espaco-import) para consolidar.
 *   4. Prepende archetype_post_generate_body/archetype-imports.groovy
 *      (com replaceAll barra barra vazio em cada linha, espelhando o
 *      tr -d barra do bash legado) no topo do arquivo final.
 *   5. Concatena tudo e escreve em
 *      target/classes/META-INF/archetype-post-generate.groovy.
 *
 * Garantia de byte-equivalencia: o output deste script deve ser identico
 * byte-a-byte a saida do bash_script/create_archetype_post_generate.sh.
 * Validar com diff ou sha256sum. Ver
 * specs/002-native-groovy-composer/quickstart.md para o cenario de validacao.
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
// (VARIABLES / SNIPPETS) que expande para todos os arquivos da pasta
// correspondente em ordem alfabética (equivalente a `ls -1` no bash legado).
//
// Esta é a única estrutura de dados que define a ordem do output. Editar aqui
// para reorganizar o arquivo gerado. Cada entrada tem um comentário curto
// explicando o papel.
//
// Para adicionar uma nova flag + snippet:
//   1. Criar `property-xxx.xml` em `archetype_metadata_properties/`.
//   2. Criar `variables-xxx.groovy` em `archetype_post_generate_variables/`.
//   3. Criar `snippet-xxx.groovy` em `archetype_post_generate_snippets/`
//      (com bloco de comentário `// FLAG: enable_xxx` no topo).
//   4. Atualizar este SECTIONS se a ordem alfabética não for suficiente.
// ---------------------------------------------------------------------------
final List<String> SECTIONS = [
    "archetype-header.groovy",                          //  0: cabeçalho inicial (Properties properties = request.properties)
    "archetype-global-variables.groovy",                //  1: defs globais (project_package, entity_name, etc.)
    "archetype-path-root-variables.groovy",             //  2: Path do projeto (projectRootFolder + ELK/git/docker)
    "archetype-path-src-main-variables.groovy",         //  3: Paths de src/main/java e src/main/resources
    "archetype-path-src-test-variables.groovy",         //  4: Paths de src/test/java e src/test/resources
    "archetype-path-logback-variables.groovy",          //  5: Paths de logback-spring-config (appenders)
    "archetype-path-security-variables.groovy",         //  6: Paths de security (appJwt.key/pub, folderJwtPath)
    "VARIABLES",                                        //  7: 10 arquivos variables-*.groovy (alfabético)
    "SNIPPETS",                                         //  8: 11 arquivos snippet-*.groovy (alfabético)
    "archetype-footer.groovy",                          //  9: log de fim do pos-processor
    "archetype-file-functions.groovy",                  // 10: helpers (deleteFile, renameFile, copyFile)
    "archetype-folder-functions.groovy"                 // 11: helpers (deleteFolder, renameFolder, etc.)
]

// ---------------------------------------------------------------------------
// Paths
// ---------------------------------------------------------------------------
def baseDir = new File("src/main/resources/META-INF")
def bodyDir = new File(baseDir, "archetype_post_generate_body")
def variablesDir = new File(baseDir, "archetype_post_generate_variables")
def snippetsDir = new File(baseDir, "archetype_post_generate_snippets")
def importsFile = new File(bodyDir, "archetype-imports.groovy")
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
def requiredBodyFiles = [
    "archetype-imports.groovy",
    "archetype-header.groovy",
    "archetype-global-variables.groovy",
    "archetype-path-root-variables.groovy",
    "archetype-path-src-main-variables.groovy",
    "archetype-path-src-test-variables.groovy",
    "archetype-path-logback-variables.groovy",
    "archetype-path-security-variables.groovy",
    "archetype-footer.groovy",
    "archetype-file-functions.groovy",
    "archetype-folder-functions.groovy"
]
requiredBodyFiles.each { name ->
    def f = new File(bodyDir, name)
    if (!f.exists()) {
        throw new IllegalStateException("Required body file missing: ${f.path}")
    }
}
if (variablesDir.listFiles() == null || variablesDir.listFiles().length == 0) {
    throw new IllegalStateException("Variables directory is empty: ${variablesDir.path}")
}
if (snippetsDir.listFiles() == null || snippetsDir.listFiles().length == 0) {
    throw new IllegalStateException("Snippets directory is empty: ${snippetsDir.path}")
}

// ---------------------------------------------------------------------------
// Compose
// ---------------------------------------------------------------------------
def sb = new StringBuilder()

// 1. Prepend imports (espelha o tr -d '/' aplicado a archetype-imports.groovy
//    no bash legado -- cada linha tem seus / removidos).
importsFile.eachLine { line ->
    sb.append(line.replaceAll("/", "")).append("\n")
}

// 2. Concatenar secoes em ordem (SECTIONS). Sem cabecalhos extras para
//    preservar byte-equivalencia com a saida do bash legado.
SECTIONS.each { section ->
    if (section == "VARIABLES" || section == "SNIPPETS") {
        // Pasta dinamica: variables/ ou snippets/, ordem alfabetica.
        def dir = (section == "VARIABLES") ? variablesDir : snippetsDir
        def files = dir.listFiles().sort { it.name }
        files.each { f ->
            sb.append(removeImportLines(f.text))
        }
    } else {
        // Arquivo unico de body/ na ordem canonica.
        def f = new File(bodyDir, section)
        if (!f.exists()) {
            throw new IllegalStateException("Body file declared in SECTIONS does not exist: ${f.path}")
        }
        sb.append(removeImportLines(f.text))
    }
}

// ---------------------------------------------------------------------------
// Write
// ---------------------------------------------------------------------------
outputFile.parentFile.mkdirs()
outputFile.text = sb.toString()

println "[SnippetComposer] Wrote ${outputFile.path} (${sb.toString().length()} chars)"
println "[SnippetComposer] Body files: ${requiredBodyFiles.size()}, Variables: ${variablesDir.listFiles().length}, Snippets: ${snippetsDir.listFiles().length}"
