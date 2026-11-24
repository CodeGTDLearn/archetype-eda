/**
 * Minimal red skeleton for the CRITICAL TABLE / CATALOG SYNC (T003).
 *
 * Detailed checks (9 defaults, 3 literal regexes and 6 descriptive formats)
 * belong to T033. This skeleton only proves that the critical table has
 * exactly three columns and nine flag rows. It must fail while any internal
 * pipe is unescaped.
 */
def rules = new File("target/classes/llm-maven-flags-inference/03-derivacao-regras-de-formacao-do-contrato/regras-04-como-aplicar-precedencia-e-validacao.md")
def text = rules.isFile() ? rules.getText("UTF-8") : ""

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

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
