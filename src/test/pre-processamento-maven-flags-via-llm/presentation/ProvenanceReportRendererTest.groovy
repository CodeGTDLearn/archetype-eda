/**
 * Unit test for ProvenanceReportRenderer (FR-020): merge provenance and
 * Markdown report rendering.
 *
 * Run from repo root:
 *   groovy src/test/pre-processamento-maven-flags-via-llm/presentation/ProvenanceReportRendererTest.groovy
 * (also executed by the build - see src/test/groovy/run-maven-flags-via-llm-tests.groovy)
 */

def moduleRoot = new File("src/main/pre-processamento-maven-flags-via-llm")
def sourceIndex = [:]
moduleRoot.eachFileRecurse { File f ->
    if (f.isFile() && f.name.endsWith(".groovy")) sourceIndex[f.name] = f
}
def gcl = new GroovyClassLoader(this.class.classLoader)
gcl.setResourceLoader({ String className ->
    String simple = className.substring(className.lastIndexOf(".") + 1)
    File source = sourceIndex[simple + ".groovy"]
    return source == null ? null : source.toURI().toURL()
} as groovy.lang.GroovyResourceLoader)

def catalogEntryClass = gcl.loadClass("eda.postgenerate.pre_processamento_maven_flags_via_llm.catalog.CatalogEntry")
def resolverClass = gcl.loadClass("eda.postgenerate.pre_processamento_maven_flags_via_llm.domain.ContractTypeResolver")
def deriverClass = gcl.loadClass("eda.postgenerate.pre_processamento_maven_flags_via_llm.domain.ContractEntityDeriver")
def mergerClass = gcl.loadClass("eda.postgenerate.pre_processamento_maven_flags_via_llm.domain.FlagProposalMerger")
def rendererClass = gcl.loadClass("eda.postgenerate.pre_processamento_maven_flags_via_llm.presentation.ProvenanceReportRenderer")

int failures = 0
def check = { String name, boolean cond ->
    if (cond) { println "PASS: ${name}" } else { println "FAIL: ${name}"; failures++ }
}

def mkEntry = { String k, String d, String r ->
    catalogEntryClass.newInstance(k, d, r, "")
}

def merger = mergerClass.newInstance(deriverClass.newInstance(resolverClass.newInstance()))
def renderer = rendererClass.newInstance()

// ----------------------------------------------------------------------
// 1) merge() provenance + report rendering
// ----------------------------------------------------------------------
def catalog = new LinkedHashMap()
catalog.put("api_port", mkEntry("api_port", "8080", "\\d{1,5}"))
catalog.put("log_level", mkEntry("log_level", "INFO", null))
catalog.put("pipe_flag", mkEntry("pipe_flag", "none", null))

def contract = new LinkedHashMap()
contract.put("api_port", "999999")        // fails regex -> FR-006
contract.put("log_level", "DEBUG")        // CONTRACT
contract.put("secret_field", "shhh")      // not in catalog -> must never leak (FR-017)

def inference = new LinkedHashMap()
inference.put("api_port", "7070")         // in contract -> eligible, but contract wins
inference.put("pipe_flag", "a|b")         // in contract? no -> discarded; pipe tests mdCell
inference.put("unknown_llm", "zz")        // discarded

def merged = merger.mergeWithProvenance(inference, contract, catalog)
String report = (String) renderer.render(merged)

check("1.1 report has main table header", report.contains("| flag | valor final | fonte | notas |"))
check("1.2 FR-006 fallback row (api_port=8080 DEFAULT)", report.contains("| api_port | 8080 | DEFAULT |"))
check("1.3 FR-006 note mentions regex rejection", report.contains("rejeitado pela regex"))
check("1.4 CONTRACT row (log_level=DEBUG)", report.contains("| log_level | DEBUG | CONTRACT |"))
check("1.5 DEFAULT row (pipe_flag=none)", report.contains("| pipe_flag | none | DEFAULT |"))
check("1.6 discarded section present", report.contains("Propostas do LLM descartadas"))
check("1.7 discarded row (unknown_llm)", report.contains("| unknown_llm | zz |"))
check("1.8 mdCell escapes pipe in discarded value", report.contains("a\\|b"))
check("1.9 summary line counts", report.contains("**Resumo**: 1 CONTRACT / 0 CONTRACT_DERIVED / 0 INFERENCE / 2 DEFAULT / 2 descartada(s)"))
check("1.10 FR-017: secret_field absent from report", !report.contains("secret_field") && !report.contains("shhh"))

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
