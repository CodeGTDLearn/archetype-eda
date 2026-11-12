import java.nio.file.Path
import java.nio.file.Paths

/**
 * Shared harness for the post-processor suites.
 *
 * Composes the SAME pieces the real build composes (body + variables +
 * functions + one snippet/generator) and evaluates them in a throw-away
 * project root. It is a test helper, not a suite (it is not listed in the
 * runners).
 */
class PostProcessorHarness {

    private static final String BASE = "src/main/resources/META-INF"

    /**
     * Body pieces that must be evaluated BEFORE the variables and the
     * snippet: logging buffer, domain constants, paths and the binding
     * artifact registry (data read by the binding snippets).
     */
    private static final List<String> BODY_FILES = [
            "body-03-logging-variables.groovy",
            "body-04-global-variables.groovy",
            "body-05-path-root-variables.groovy",
            "body-06-path-src-main-variables.groovy",
            "body-07-path-src-test-variables.groovy",
            "body-08-path-logback-variables.groovy",
            "body-09-path-security-variables.groovy",
            "body-10-binding-artifact-registry.groovy"
    ]

    /** Helper functions (resolved at runtime, so the position is irrelevant). */
    private static final List<String> FUNCTION_FILES = [
            "body-12-file-functions.groovy",
            "body-13-folder-functions.groovy",
            "body-14-java-source-functions.groovy",
            "body-15-elk-functions.groovy"
    ]

    static File baseDir() { new File(BASE) }

    static File snippet(String name) { new File(BASE, "archetype_post_generate_snippets/" + name) }

    static File generator(String name) { new File(BASE, "archetype_post_generate_generators/" + name) }

    static File variable(String name) { new File(BASE, "archetype_post_generate_variables/" + name) }

    /**
     * Runs one snippet against `root` (the generated project root).
     * `props` are the archetype properties exposed to the snippet.
     * Returns the value of `log_text` after the run.
     */
    static String run(File root, Map<String, String> props, String snippetName) {
        return runAsset(root, props, snippet(snippetName))
    }

    /** Runs one generator against `root`. */
    static String runGenerator(File root, Map<String, String> props, String generatorName) {
        return runAsset(root, props, generator(generatorName))
    }

    private static String runAsset(File root, Map<String, String> props, File asset) {
        def effective = new LinkedHashMap<String, String>()
        effective.put("package", "it.pkg")
        effective.put("entity_name", "MyEntity")
        effective.put("entity_package_name", "myentity")
        effective.putAll(props)
        props = effective
        def sb = new StringBuilder()
        sb.append("import java.nio.file.Path\n")
        sb.append("import java.nio.file.Paths\n")
        sb.append("import java.nio.file.Files\n\n")
        BODY_FILES.each { name -> sb.append(new File(BASE, "archetype_post_generate_body/" + name).getText("UTF-8")).append("\n") }
        new File(BASE, "archetype_post_generate_variables").listFiles().sort { it.name }.each { f ->
            if (f.isFile() && f.name.endsWith(".groovy")) {
                sb.append(f.getText("UTF-8")).append("\n")
            }
        }
        FUNCTION_FILES.each { name ->
            sb.append(new File(BASE, "archetype_post_generate_body/" + name).getText("UTF-8")).append("\n")
        }
        sb.append(asset.getText("UTF-8")).append("\n")
        sb.append("\nreturn_log = log_text\n")

        def properties = new Properties()
        props.each { k, v -> properties.put(k, v) }

        def binding = new Binding()
        binding.setVariable("properties", properties)
        binding.setVariable("request", [outputDirectory: root.parentFile.absolutePath, artifactId: root.name])
        binding.setVariable("log_text", "")

        new GroovyShell(PostProcessorHarness.class.classLoader, binding).evaluate(sb.toString())
        return binding.getVariable("return_log") == null ? "" : binding.getVariable("return_log").toString()
    }

    /** Runs a raw snippet text against `root` (used by the body variables tests). */
    static Map<String, Object> runRaw(File root, Map<String, String> props, String source) {
        def properties = new Properties()
        props.each { k, v -> properties.put(k, v) }
        def binding = new Binding()
        binding.setVariable("properties", properties)
        binding.setVariable("request", [outputDirectory: root.parentFile.absolutePath, artifactId: root.name])
        binding.setVariable("log_text", "")
        new GroovyShell(PostProcessorHarness.class.classLoader, binding).evaluate(source)
        return binding.getVariables()
    }

    /**
     * Runs the COMPOSED post-processor (the real artifact produced by the
     * build) against `root`, with the binding the maven-archetype-plugin
     * builds: the `request` plus EVERY property key as a direct binding
     * variable.
     *
     * This is the only path that reproduces the production semantics: the
     * per-asset `run` above binds `properties` only, so the helpers that read
     * the flag from the BINDING (body-03) can never see it there.
     *
     * Returns what the post-processor printed to stdout: the report is
     * printed by body-11-footer, not returned through the binding.
     */
    static String runFull(File root, Map<String, String> props) {
        File composed = new File("target/classes/META-INF/archetype-post-generate.groovy")
        if (!composed.isFile()) {
            throw new IllegalStateException("Composed post-processor not found: ${composed.path}")
        }

        def effective = new LinkedHashMap<String, String>()
        effective.put("package", "it.pkg")
        effective.put("groupId", "archetype.it")
        effective.put("version", "0.1-SNAPSHOT")
        effective.put("entity_name", "MyEntity")
        effective.put("entity_package_name", "myentity")
        effective.putAll(props)

        def properties = new Properties()
        effective.each { k, v -> properties.put(k, v) }

        def binding = new Binding()
        binding.setVariable("request", [outputDirectory: root.parentFile.absolutePath,
                                       artifactId: root.name,
                                       properties: properties])
        binding.setVariable("properties", properties)
        binding.setVariable("log_text", "")
        // The archetype plugin exposes EVERY requiredProperty as a direct
        // binding variable - body-03 reads the log flag from there.
        effective.each { k, v -> binding.setVariable(k, v) }

        def buffer = new ByteArrayOutputStream()
        def originalOut = System.out
        try {
            System.setOut(new PrintStream(buffer, true, "UTF-8"))
            new GroovyShell(PostProcessorHarness.class.classLoader, binding)
                    .evaluate(composed.getText("UTF-8"))
        } finally {
            System.setOut(originalOut)
        }
        return buffer.toString("UTF-8")
    }

    /** Seeds a file under root. */
    static File seed(File root, String relative, String content) {
        File f = new File(root, relative)
        f.parentFile.mkdirs()
        f.setText(content, "UTF-8")
        return f
    }
}
