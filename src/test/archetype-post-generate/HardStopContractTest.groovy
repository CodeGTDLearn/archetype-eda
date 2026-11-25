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

def der02Matcher = (text =~ /(?s)> ## PARADA DURA — STOP-DER-02.*?> \*\*Dicionário:\*\* `prompt-00` §0\.4\./)
def der02 = der02Matcher.find() ? der02Matcher.group() : ""
def der02Options = (der02 =~ /(?m)^> \d+\. /).count

check("2.1 STOP-DER-02 carries exactly 3 option texts: ${der02Options}", der02Options == 3 &&
        der02.contains("Corrija o campo") &&
        der02.contains("Remova o campo") &&
        der02.contains("Escreva a lista completa"))
check("2.2 STOP-DER-02 forbids the String:name fallback",
        der02.contains('O valor padrão "String:name" não serve') &&
                der02.contains('não use "String:name"'))
check("2.3 STOP-DER-02 emits no Maven command",
        der02.contains("o comando do Maven não foi montado") &&
                der02.contains("não rode o Maven"))

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
