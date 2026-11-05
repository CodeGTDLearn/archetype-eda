package eda.postgenerate.pre_processamento_maven_flags_via_llm.contract

/**
 * Detects secret-like keys in the contract so the application can warn the
 * operator before the contract is versioned inside the generated module.
 *
 * Responsibility: detection only. Rendering the warning is a presentation
 * concern owned by the application.
 */
class ContractSecretScanner {

    List<String> scan(Object node) {
        List<String> suspicious = new ArrayList<>()
        collectSecretLikeKeys(node, "", suspicious)
        return suspicious
    }

    private void collectSecretLikeKeys(Object node, String path, List<String> out) {
        if (node instanceof Map) {
            for (Map.Entry<?, ?> e : ((Map<?, ?>) node).entrySet()) {
                String key = e.key?.toString() ?: ""
                String childPath = path.isEmpty() ? key : (path + "." + key)
                if (key.toLowerCase(Locale.ROOT) ==~ /.*(password|senha|secret|token|credential|api[_-]?key|private[_-]?key).*/) {
                    if (!out.contains(childPath)) out.add(childPath)
                }
                collectSecretLikeKeys(e.value, childPath, out)
            }
        } else if (node instanceof List) {
            List<?> list = (List<?>) node
            for (int i = 0; i < list.size(); i++) {
                collectSecretLikeKeys(list.get(i), path + "[" + i + "]", out)
            }
        }
    }
}
