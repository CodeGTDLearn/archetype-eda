package eda.postgenerate.pre_processamento_maven_flags_via_llm.inference

/**
 * Resolves the runtime configuration (paths, defaults and debug flag) from the
 * CLI arguments and the environment.
 *
 * Responsibility: configuration only. No I/O beyond reading environment
 * variables and system properties.
 */
class InferenceConfig {

    final String cacheDir
    final String contractDir
    final String inferenceBin
    final String catalogPath
    final boolean debug

    private InferenceConfig(String cacheDir, String contractDir, String inferenceBin,
                            String catalogPath, boolean debug) {
        this.cacheDir = cacheDir
        this.contractDir = contractDir
        this.inferenceBin = inferenceBin
        this.catalogPath = catalogPath
        this.debug = debug
    }

    static InferenceConfig from(Map<String, String> cliArgs) {
        String cacheDir = cliArgs.getOrDefault("cache-dir", defaultCacheDir())
        String contractDir = cliArgs.getOrDefault("contract-dir",
                new File(cacheDir, "contracts").absolutePath)
        String inferenceBin = cliArgs.getOrDefault("inference-bin",
                System.getenv("EDA_LLM_INFERENCE_BIN"))
        String catalogPath = "target/classes/META-INF/maven/archetype-metadata.xml"
        boolean debug = Boolean.parseBoolean(System.getProperty("eda.llm.debug", "false"))
        return new InferenceConfig(cacheDir, contractDir, inferenceBin, catalogPath, debug)
    }

    static String defaultCacheDir() {
        String xdg = System.getenv("XDG_CACHE_HOME")
        if (xdg != null && !xdg.isEmpty()) {
            return new File(xdg, "eda-archetype/llm-flag-inference").absolutePath
        }
        String localAppData = System.getenv("LOCALAPPDATA")
        if (localAppData != null && !localAppData.isEmpty()) {
            return new File(localAppData, "eda-archetype/llm-flag-inference").absolutePath
        }
        // Last-resort fallback: ~/.cache (POSIX convention)
        String userHome = System.getProperty("user.home")
        return new File(userHome, ".cache/eda-archetype/llm-flag-inference").absolutePath
    }

    File catalogFile() {
        return new File(catalogPath)
    }
}
