/**
 * Minimal red skeleton for the HARD-STOP CONTRACT (T002).
 *
 * Detailed checks (10 fields, options, dictionary reference, sites and
 * language rules) belong to T026-T028. This skeleton only proves that the
 * packaged prompt-00 carries the 18 registered codes and their messages.
 */
def prompt = new File("target/classes/llm-maven-flags-inference/01-entrada-inicializacao/prompt-00-leia-este-arquivo-primeiro.md")
def text = prompt.isFile() ? prompt.getText("UTF-8") : ""

int failures = 0
def check = { String name, boolean cond ->
    if (cond) { println "PASS: ${name}" } else { println "FAIL: ${name}"; failures++ }
}

def codes = (text =~ /STOP-(?:IN|ENV|DER|EXEC|RULE|CONF)-\d{2}/).collect { it }.unique()
def messages = (text =~ /(?m)^> ## PARADA DURA — STOP-(?:IN|ENV|DER|EXEC|RULE|CONF)-\d{2}/).count

check("1.1 the procedure declares 18 unique stop codes: ${codes.size()}", codes.size() == 18)
check("1.2 the procedure carries 18 hard-stop messages: ${messages}", messages == 18)

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
