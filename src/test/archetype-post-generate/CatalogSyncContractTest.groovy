/**
 * Contract test for the CRITICAL TABLE / CATALOG SYNC (T033).
 *
 * Covers the 3-column parsing, the 9 defaults, the 3 literal regexes
 * (with placeholder normalization for custom_entity_attributes_list), the
 * 6 descriptive examples, the 35 properties / 18 filesets and the pinned
 * catalog values that T038 must not change.
 */
def rules = new File("target/classes/llm-maven-flags-inference/03-derivacao-regras-de-formacao-do-contrato/regras-04-como-aplicar-precedencia-e-validacao.md")
def catalog = new File("target/classes/META-INF/maven/archetype-metadata.xml")
def text = rules.isFile() ? rules.getText("UTF-8") : ""
def catalogText = catalog.isFile() ? catalog.getText("UTF-8") : ""

int failures = 0
def check = { String name, boolean cond ->
    if (cond) { println "PASS: ${name}" } else { println "FAIL: ${name}"; failures++ }
}

def lines = text.readLines()
def start = lines.findIndexOf { it ==~ /^### 3\.1 .*/ }
def end = start < 0 ? -1 : lines.findIndexOf(start + 1) { it ==~ /^## .*/ }
def table = start < 0 || end < 0 ? [] : lines.subList(start, end)

def splitRow = { String line ->
    def cells = line.split(/(?<!\\)\|/, -1).collect { it.trim() }
    if (cells.size() > 0 && cells[0].isEmpty()) cells.remove(0)
    if (cells.size() > 0 && cells[-1].isEmpty()) cells.remove(cells.size() - 1)
    cells
}

def header = table.find { it ==~ /^\|.*\|$/ }
def rows = table.findAll { it ==~ /^\| `.*\|$/ }
def badRows = rows.findAll { splitRow(it).size() != 3 }

check("1.1 the critical table header has exactly 3 columns", header != null && splitRow(header).size() == 3)
check("1.2 the critical table carries 9 flag rows", rows.size() == 9)
check("1.3 every critical table row has exactly 3 columns: ${badRows.size()} bad", badRows.isEmpty())

def firstBacktick = { String cell ->
    def m = cell =~ /`([^`]*)`/
    m.find() ? m.group(1) : null
}
def unescapePipes = { String value -> value == null ? null : value.replace('\\|', '|') }

def tableRows = splitRow(rows[0]).size() == 3 ? rows.collect { splitRow(it) } : []
def tableByFlag = [:]
tableRows.each { row ->
    tableByFlag[firstBacktick(row[0])] = [defaultValue: firstBacktick(row[1]), regex: row[2]]
}

def catalogDefault = { String flag ->
    def m = catalogText =~ /(?s)<requiredProperty key="${flag}">\s*<defaultValue>(.*?)<\/defaultValue>/
    m.find() ? m.group(1) : null
}
def catalogRegex = { String flag ->
    def m = catalogText =~ /(?s)<requiredProperty key="${flag}">\s*<defaultValue>.*?<\/defaultValue>\s*<validationRegex>(.*?)<\/validationRegex>/
    m.find() ? m.group(1) : null
}

def flags = ["entity_name", "entity_package_name", "custom_entity_attributes_list",
             "custom_entity_attribute_imports", "contract_enum_definitions",
             "contract_nested_type_definitions", "entity_contract_file_path",
             "enable_custom_entity_attributes", "enable_folder_src_test_java"]
def missingRows = flags.findAll { !tableByFlag.containsKey(it) }
check("2.1 the table lists the 9 critical flags: ${missingRows}", missingRows.isEmpty())

def defaultMismatch = flags.findAll { tableByFlag[it]?.defaultValue != catalogDefault(it) }
        .collect { "${it}: table=${tableByFlag[it]?.defaultValue} catalog=${catalogDefault(it)}" }
check("2.2 every table default matches the catalog: ${defaultMismatch}", defaultMismatch.isEmpty())

def literalFlags = flags.findAll { tableByFlag[it]?.regex?.contains('`^') }
check("2.3 the table carries exactly 3 literal regexes: ${literalFlags}", literalFlags.size() == 3)
check("2.4 the literal regexes are the expected 3 flags",
        literalFlags.toSet() == ["entity_name", "entity_package_name", "custom_entity_attributes_list"].toSet())

def normalizeList = { String value ->
    value == null ? null : value.replace("Tipo:nome", "[A-Z][a-zA-Z0-9_]*:[a-z][a-zA-Z0-9_]*")
}
def regexMismatch = literalFlags.findAll { flag ->
    def tableRegex = unescapePipes(firstBacktick(tableByFlag[flag].regex))
    def expected = flag == "custom_entity_attributes_list" ? normalizeList(tableRegex) : tableRegex
    expected != catalogRegex(flag)
}.collect { "${it}: table=${unescapePipes(firstBacktick(tableByFlag[it].regex))} catalog=${catalogRegex(it)}" }
check("2.5 every literal regex matches the catalog: ${regexMismatch}", regexMismatch.isEmpty())

def examples = [
        "entity_name"                   : [ok: "Cliente", bad: "cliente"],
        "entity_package_name"           : [ok: "br.com.cliente", bad: "Cliente"],
        "custom_entity_attributes_list" : [ok: "String:nome,Integer:idade", bad: "nome"],
]
def exampleMismatch = literalFlags.findAll { flag ->
    def raw = unescapePipes(firstBacktick(tableByFlag[flag].regex))
    def value = flag == "custom_entity_attributes_list" ? normalizeList(raw) : raw
    def re = value == null ? null : java.util.regex.Pattern.compile(value)
    re != null && (re.matcher(examples[flag].ok).matches() == false ||
            re.matcher(examples[flag].bad).matches() == true)
}.collect { "${it}: ${unescapePipes(firstBacktick(tableByFlag[it].regex))}" }
check("2.6 every literal regex accepts a valid example and rejects an invalid one: ${exampleMismatch}",
        exampleMismatch.isEmpty())

def descriptiveTokens = [
        "custom_entity_attribute_imports"    : ["FQNs"],
        "contract_enum_definitions"          : ["Tipo:CONST|CONST"],
        "contract_nested_type_definitions"   : ["Tipo|Campo:Tipo"],
        "entity_contract_file_path"          : ["none", ".json"],
        "enable_custom_entity_attributes"    : ["true", "false"],
        "enable_folder_src_test_java"        : ["true", "false"],
]
def descriptiveMismatch = descriptiveTokens.findAll { flag, tokens ->
    def cell = unescapePipes(tableByFlag[flag]?.regex ?: "")
    !tokens.every { cell.contains(it) }
}.collect { it.key }
check("2.7 the 6 descriptive rows carry their examples: ${descriptiveMismatch}", descriptiveMismatch.isEmpty())

def pinnedDefaults = [
        "entity_name"                     : "MyEntity",
        "entity_package_name"             : "myentity",
        "custom_entity_attributes_list"   : "String:name",
        "custom_entity_attribute_imports" : "none",
        "contract_enum_definitions"       : "none",
        "contract_nested_type_definitions": "none",
        "entity_contract_file_path"       : "none",
        "enable_custom_entity_attributes" : "false",
        "enable_folder_src_test_java"     : "false",
]
def pinnedRegexes = [
        "entity_name"                     : '^[A-Z][a-zA-Z0-9_]+$',
        "entity_package_name"             : '^[a-z][a-z0-9_.]+$',
        "custom_entity_attributes_list"   : '^(false|[A-Z][a-zA-Z0-9_]*:[a-z][a-zA-Z0-9_]*(,[A-Z][a-zA-Z0-9_]*:[a-z][a-zA-Z0-9_]*)*)$',
        "custom_entity_attribute_imports" : '^(none|([A-Za-z_][A-Za-z0-9_]*\\.)+[A-Za-z_][A-Za-z0-9_]*(,([A-Za-z_][A-Za-z0-9_]*\\.)+[A-Za-z_][A-Za-z0-9_]*)*)$',
        "contract_enum_definitions"       : '^(none|([A-Za-z_][A-Za-z0-9_]*\\.)*[A-Za-z_][A-Za-z0-9_]*:[A-Z][A-Z0-9_]*(\\|[A-Z][A-Z0-9_]*)*(;([A-Za-z_][A-Za-z0-9_]*\\.)*[A-Za-z_][A-Za-z0-9_]*:[A-Z][A-Z0-9_]*(\\|[A-Z][A-Z0-9_]*)*)*)$',
        "contract_nested_type_definitions": '^(none|[A-Z][A-Za-z0-9_]*\\|[a-z][a-zA-Z0-9_]*:[A-Za-z_][A-Za-z0-9_.]*(,[a-z][a-zA-Z0-9_]*:[A-Za-z_][A-Za-z0-9_.]*)*)(;[A-Z][A-Za-z0-9_]*\\|[a-z][a-zA-Z0-9_]*:[A-Za-z_][A-Za-z0-9_.]*(,[a-z][a-zA-Z0-9_]*:[A-Za-z_][A-Za-z0-9_.]*)*)*$',
        "entity_contract_file_path"       : '^(none|[^\\r\\n]*\\.json)$',
        "enable_custom_entity_attributes" : '^(true|false)$',
        "enable_folder_src_test_java"     : '^(true|false)$',
]
def catalogDefaultMismatch = flags.findAll { catalogDefault(it) != pinnedDefaults[it] }
        .collect { "${it}: ${catalogDefault(it)}" }
def catalogRegexMismatch = flags.findAll { catalogRegex(it) != pinnedRegexes[it] }
        .collect { "${it}: ${catalogRegex(it)}" }

check("3.1 the catalog carries 35 properties", (catalogText =~ /<requiredProperty /).count == 35)
check("3.2 the catalog carries 18 filesets", (catalogText =~ /<fileSet /).count == 18)
check("3.3 no catalog default changed: ${catalogDefaultMismatch}", catalogDefaultMismatch.isEmpty())
check("3.4 no catalog validation regex changed: ${catalogRegexMismatch}", catalogRegexMismatch.isEmpty())

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
