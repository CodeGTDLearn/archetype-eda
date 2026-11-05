package eda.postgenerate.pre_processamento_maven_flags_via_llm.presentation

/**
 * Renders the FR-020 provenance report as Markdown.
 *
 * Responsibility: presentation only. It reads the merge result and returns the
 * report body; it does not print and does not persist.
 */
class ProvenanceReportRenderer {

    String render(Map<String, Object> merged) {
        Map<String, String> values = (Map<String, String>) merged.get("values")
        Map<String, String> sources = (Map<String, String>) merged.get("sources")
        Map<String, String> notes = (Map<String, String>) merged.get("notes")
        Map<String, String> discarded = (Map<String, String>) merged.get("discarded")

        String nl = System.lineSeparator()
        StringBuilder sb = new StringBuilder()
        sb.append("| flag | valor final | fonte | notas |").append(nl)
        sb.append("|---|---|---|---|").append(nl)
        int cContract = 0
        int cDerived = 0
        int cInference = 0
        int cDefault = 0
        for (Map.Entry<String, String> e : values.entrySet()) {
            String src = sources.get(e.key)
            if (src == "CONTRACT") cContract++
            else if (src == "CONTRACT_DERIVED") cDerived++
            else if (src == "INFERENCE") cInference++
            else cDefault++
            String note = notes.containsKey(e.key) ? notes.get(e.key) : ""
            sb.append("| ").append(mdCell(e.key))
              .append(" | ").append(mdCell(e.value))
              .append(" | ").append(src)
              .append(" | ").append(mdCell(note))
              .append(" |").append(nl)
        }
        if (discarded != null && !discarded.isEmpty()) {
            sb.append(nl)
            sb.append("### Propostas do LLM descartadas (ausentes do contrato - FR-009)").append(nl)
            sb.append(nl)
            sb.append("| flag | valor proposto |").append(nl)
            sb.append("|---|---|").append(nl)
            for (Map.Entry<String, String> e : discarded.entrySet()) {
                sb.append("| ").append(mdCell(e.key))
                  .append(" | ").append(mdCell(e.value))
                  .append(" |").append(nl)
            }
        }
        sb.append(nl)
        sb.append("**Resumo**: ").append(cContract).append(" CONTRACT / ")
          .append(cDerived).append(" CONTRACT_DERIVED / ")
          .append(cInference).append(" INFERENCE / ")
          .append(cDefault).append(" DEFAULT / ")
          .append(discarded == null ? 0 : discarded.size()).append(" descartada(s)")
        return sb.toString()
    }

    private String mdCell(String s) {
        if (s == null) return ""
        return s.replace("|", "\\|")
    }
}
