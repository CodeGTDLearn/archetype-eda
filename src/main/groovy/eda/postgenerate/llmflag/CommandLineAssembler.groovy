package eda.postgenerate.llmflag

/**
 * Assembles the final mvn archetype:generate command line.
 * See spec.md FR-010 and data-model.md Entity 5.
 *
 * Escape rule (per data-model.md Entity 5):
 *   Wrap a value in double quotes iff the value contains any of:
 *     whitespace, $, `, \, ", &, |, <, >, ;, (, ), *, ?, [, ], {, }
 *   Escape embedded " as \"
 *   Empty values emitted as -D<key>= (no quotes, empty RHS)
 *   Single -D<key>=<value> per catalog key (no duplicates)
 */
class CommandLineAssembler {

    private static final String[] META_CHARS = [
        ' ', '\t', '$', '`', '\\', '"', '&', '|', '<', '>', ';',
        '(', ')', '*', '?', '[', ']', '{', '}'
    ] as String[]

    String assemble(Map<String, String> validatedProposal) {
        if (validatedProposal == null) {
            throw new IllegalArgumentException("CommandLineAssembler: validatedProposal is null")
        }
        StringBuilder sb = new StringBuilder("mvn archetype:generate --batch-mode")
        for (Map.Entry<String, String> entry : validatedProposal.entrySet()) {
            sb.append(' ')
            sb.append("-D").append(entry.key).append('=')
            sb.append(escapeValue(entry.value))
        }
        return sb.toString()
    }

    private String escapeValue(String value) {
        if (value == null || value.isEmpty()) {
            return ""
        }
        boolean needsQuotes = false
        for (String m : META_CHARS) {
            if (value.contains(m)) { needsQuotes = true; break }
        }
        if (!needsQuotes) return value
        StringBuilder sb = new StringBuilder("\"")
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i)
            if (c == '"') sb.append("\\\"")
            else if (c == '\\') sb.append("\\\\")
            else sb.append(c)
        }
        sb.append("\"")
        return sb.toString()
    }
}
