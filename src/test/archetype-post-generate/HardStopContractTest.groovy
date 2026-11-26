/**
 * Contract test for the HARD-STOP CONTRACT (T026-T028).
 *
 * Covers the registry and messages, the 10 fields, the 20-word rule, the
 * per-option examples, the exact confirmation options, the dictionary, the
 * prohibition, the site scan, the file counts and the opt-in installed JAR.
 */
def procedure = new File("target/classes/llm-maven-flags-inference")
def prompt = new File(procedure, "01-entrada-inicializacao/00-entry-point.md")
def text = prompt.isFile() ? prompt.getText("UTF-8") : ""

int failures = 0
def check = { String name, boolean cond ->
    if (cond) { println "PASS: ${name}" } else { println "FAIL: ${name}"; failures++ }
}

def mdFiles = []
if (procedure.isDirectory()) procedure.eachFileRecurse { File f -> if (f.isFile() && f.name.endsWith(".md")) mdFiles << f }
def corpus = mdFiles.collect { it.getText("UTF-8") }.join("\n")

def codePattern = /STOP-(?:IN|ENV|DER|EXEC|RULE|CONF)-\d{2}/
def allCodes = (text =~ codePattern).collect { it }.unique().sort()
def lines = text.readLines()
def rs = lines.findIndexOf { it ==~ /^### 0\.5 .*/ }
def re = rs < 0 ? -1 : lines.findIndexOf(rs + 1) { it ==~ /^## .*/ }
def registry = []
if (rs >= 0 && re > rs) {
    lines.subList(rs, re).each { line ->
        def m = line =~ /^\| `(STOP-(?:IN|ENV|DER|EXEC|RULE|CONF)-\d{2})` \|/
        if (m.find()) registry << m.group(1)
    }
}
registry = registry.unique().sort()
check("1.1 the registry declares 18 unique stop codes: ${registry.size()}", registry.size() == 18)
check("1.2 every code cited in 00-entry-point is registered: ${allCodes - registry}", (allCodes - registry).isEmpty())
check("1.3 every registered code is cited in 00-entry-point: ${registry - allCodes}", (registry - allCodes).isEmpty())

def blockPattern = ~/(?s)```text\r?\n---\r?\n> ## PARADA DURA — (STOP-(?:IN|ENV|DER|EXEC|RULE|CONF)-\d{2})\r?\n(.*?)\r?\n---\r?\n```/
def blocks = [:]
def bm = blockPattern.matcher(text)
while (bm.find()) blocks[bm.group(1)] = bm.group(2)
check("1.4 the procedure carries 18 hard-stop messages: ${blocks.size()}", blocks.size() == 18)
check("1.5 every registered code has exactly one message: ${registry - blocks.keySet()}",
        (registry - blocks.keySet()).isEmpty() && blocks.keySet() == registry.toSet())

def labels = ["O que aconteceu", "Onde está", "Por que parou", "O que não foi executado",
              "Opções de correção", "Sugestão", "Como retomar", "O que não fazer", "Segurança"]
def missingFields = []
def optionsWithoutExample = []
def tooLong = []
def badPare = []
def badParada = []
def sentences = { String block ->
    def raw = block.readLines().findAll { it ==~ /^> (\*\*.*|\d+\. .*)$/ }
    def prose = raw.collect {
        it.replaceFirst(/^> /, "")
          .replaceFirst(/^\*\*[^*]+:\*\*\s*/, "")
          .replaceFirst(/^\d+\.\s*/, "")
    }.join(" ")
    def cleaned = prose.replaceAll(/`[^`]*`/, " ")
        .replaceAll(/https?:\/\/\S+/, " ")
        .replaceAll(/\S*[\/\\]\S*/, " ")
    cleaned.split(/(?<=[.!?])\s+/).collect { it.trim() }.findAll { it }
}
blocks.each { String code, String block ->
    def missing = labels.findAll { !block.contains(it) }
    if (!missing.isEmpty()) missingFields << "${code}: ${missing}"
    def opts = block.readLines().findAll { it ==~ /^> \d+\. .*/ }
    if (opts.size() < 2) missingFields << "${code}: options=${opts.size()}"
    opts.each { if (!(it =~ /Exemplo:|por exemplo/)) optionsWithoutExample << "${code}: ${it}" }
    if (!block.contains("Dicionário:")) missingFields << "${code}: dictionary"
    sentences(block).each { s ->
        def words = s.split(/\s+/).findAll { it }.size()
        if (words > 20) tooLong << "${code}: ${words} words: ${s.take(60)}"
    }
}
check("1.6 every message has the 10 fields: ${missingFields}", missingFields.isEmpty())
check("1.7 every option has a concrete example: ${optionsWithoutExample}", optionsWithoutExample.isEmpty())
check("1.8 every prose sentence has at most 20 words: ${tooLong}", tooLong.isEmpty())
check("1.9 STOP-DER-02 has exactly 3 option texts",
        (blocks['STOP-DER-02']?.readLines()?.count { it ==~ /^> \d+\. .*/ }) == 3)
check("1.10 STOP-CONF-01 carries the exact options",
        ["Criar a pasta", "Escolher outra pasta", "Cancelar"].every { blocks['STOP-CONF-01']?.contains(it) })
check("1.11 STOP-CONF-02 carries the exact options",
        ["Sobrescrever o módulo", "Manter o existente e encerrar", "Escolher outro destino"].every { blocks['STOP-CONF-02']?.contains(it) })

mdFiles.each { File f ->
    f.readLines().eachWithIndex { String line, int i ->
        if (line ==~ /(?i).*\bpare\b.*/ && !(line =~ codePattern)) badPare << "${f.name}:${i + 1}: ${line}"
        if (line.contains("PARADA DURA") && !(line =~ codePattern) && !(line =~ /STOP-XXX/)) badParada << "${f.name}:${i + 1}: ${line}"
    }
}
check("1.12 every line with pare cites a registered code: ${badPare}", badPare.isEmpty())
check("1.13 every PARADA DURA block cites a code: ${badParada}", badParada.isEmpty())

def dictionary = []
def ds = lines.findIndexOf { it ==~ /^### 0\.4 .*/ }
def de = lines.findIndexOf { it ==~ /^### 0\.5 .*/ }
if (ds >= 0 && de > ds) {
    lines.subList(ds, de).each { line ->
        def m = line =~ /^\| ([^|]+) \| ([^|]+) \|$/
        if (m.find() && !m.group(1).trim().equalsIgnoreCase("Termo técnico") && !m.group(1).trim().startsWith("---")) {
            dictionary << [term: m.group(1).trim(), translation: m.group(2).trim()]
        }
    }
}
check("2.1 the dictionary carries 20 terms: ${dictionary.size()}", dictionary.size() == 20)
check("2.2 every dictionary term has a translation", dictionary.every { it.translation && it.translation != "-" })
check("2.3 the procedure carries no 'informe apenas'", !corpus.contains("informe apenas"))
def regras01 = new File(procedure, "03-regras-de-formacao-do-contrato/01-derivar-a-entidade-do-contrato.md").getText("UTF-8")
check("2.4 the invalid list stops with STOP-DER-02 and has no fallback",
        regras01.contains("pare com STOP-DER-02") && !regras01.contains("O desfecho é o de"))
def secretHits = []
["(?i)(password|passwd|secret|token|api[_-]?key)\\s*[:=]", "AKIA[0-9A-Z]{16}", "sk-[A-Za-z0-9]{20,}", "-----BEGIN [A-Z ]*PRIVATE KEY-----"].each { regex ->
    def m = (corpus =~ regex)
    while (m.find()) secretHits << m.group()
}
check("2.5 the procedure carries no secret pattern: ${secretHits}", secretHits.isEmpty())

def files = []
if (procedure.isDirectory()) procedure.eachFileRecurse { if (it.isFile()) files << it }
check("2.6 the procedure carries 19 files: ${files.size()}", files.size() == 19)
check("2.7 the procedure carries 13 Markdown and 6 JSON",
        files.count { it.name.endsWith(".md") } == 13 && files.count { it.name.endsWith(".json") } == 6)

if (System.getProperty("eda.verify.installed.jar") == "true") {
    def jar = new File(System.getProperty("user.home"),
            ".m2/repository/com/eda-archetype/eda-archetype/0.0.1-SNAPSHOT/eda-archetype-0.0.1-SNAPSHOT.jar")
    check("3.1 the installed artifact exists", jar.isFile())
    if (jar.isFile()) {
        def zip = new java.util.zip.ZipFile(jar)
        def entry = zip.getEntry("llm-maven-flags-inference/01-entrada-inicializacao/00-entry-point.md")
        def installed = entry == null ? "" : zip.getInputStream(entry).getText("UTF-8")
        zip.close()
        check("3.2 the installed artifact carries 18 stop codes",
                (installed =~ codePattern).collect { it }.unique().size() == 18)
        check("3.3 the installed artifact carries STOP-DER-02", installed.contains("STOP-DER-02"))
        check("3.4 the installed artifact carries the dictionary",
                installed.contains("command line") && installed.contains("## 0.4 Dicionário de linguagem simples"))
    }
}

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
