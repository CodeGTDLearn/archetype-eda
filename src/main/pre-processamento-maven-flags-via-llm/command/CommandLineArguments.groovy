package eda.postgenerate.pre_processamento_maven_flags_via_llm.command

/**
 * Parses the operator-facing CLI arguments.
 *
 * Responsibility: argument surface only. It does not validate business
 * preconditions (the application gates do that) and does not print.
 */
class CommandLineArguments {

    Map<String, String> parse(String[] args) {
        Map<String, String> out = new LinkedHashMap<>()
        for (int i = 0; i < args.length; i++) {
            String a = args[i]
            switch (a) {
                case "--use-case-description":
                    // If --use-case-description is the last argument, OR the next
                    // token starts with "--" (i.e. it is another flag, not a value),
                    // do NOT advance i. The caller detects the missing value via the
                    // description gate and emits the FR-001 error.
                    if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                        out.put("description", args[++i])
                    }
                    break
                case "--use-case-contract":          out.put("use-case-contract", "true"); break
                case "--cache-dir":                  out.put("cache-dir", args[++i]); break
                case "--contract-dir":               out.put("contract-dir", args[++i]); break
                case "--inference-bin":              out.put("inference-bin", args[++i]); break
                default:
                    if (a.startsWith("--")) {
                        throw new IllegalArgumentException("Unknown argument: " + a)
                    }
                    break
            }
        }
        // stdin is intentionally NOT read here. The application reads stdin
        // ONLY after the description gate passes (see FR-001).
        return out
    }
}
