import java.nio.file.Path
import java.nio.file.Paths

/**
 * Shared harness for the post-processor suites.
 *
 * Composes the SAME pieces the real build composes (body + variables +
 * functions + one snippet) and evaluates them in a throw-away project root.
 * It is a test helper, not a suite (it is not listed in the runners).
 */
class PostProcessorHarness {

    private static final String BASE = "src/main/resources/META-INF"
    private static final List<String> PATH_FILES = [
            "archetype-global-variables.groovy",
            "archetype-path-root-variables.groovy",
            "archetype-path-src-main-variables.groovy",
            "archetype-path-src-test-variables.groovy",
            "archetype-path-logback-variables.groovy",
            "archetype-path-security-variables.groovy"
    ]

    static File baseDir() { new File(BASE) }

    static File snippet(String name) { new File(BASE, "archetype_post_generate_snippets/" + name) }

    static File variable(String name) { new File(BASE, "archetype_post_generate_variables/" + name) }

    /**
     * Runs one snippet against `root` (the generated project root).
     * `props` are the archetype properties exposed to the snippet.
     * Returns the value of `log_text` after the run.
     */
    static String run(File root, Map<String, String> props, String snippetName) {
        def effective = new LinkedHashMap<String, String>()
        effective.put("package", "it.pkg")
        effective.put("entity_name", "MyEntity")
        effective.put("entity_name_package", "myentity")
        effective.putAll(props)
        props = effective
        def sb = new StringBuilder()
        sb.append("import java.nio.file.Path\n")
        sb.append("import java.nio.file.Paths\n")
        sb.append("import java.nio.file.Files\n\n")
        PATH_FILES.each { name -> sb.append(new File(BASE, "archetype_post_generate_body/" + name).getText("UTF-8")).append("\n") }
        new File(BASE, "archetype_post_generate_variables").listFiles().sort { it.name }.each { f ->
            sb.append(f.getText("UTF-8")).append("\n")
        }
        sb.append(new File(BASE, "archetype_post_generate_body/archetype-file-functions.groovy").getText("UTF-8")).append("\n")
        sb.append(new File(BASE, "archetype_post_generate_body/archetype-folder-functions.groovy").getText("UTF-8")).append("\n")
        sb.append(new File(BASE, "archetype_post_generate_snippets/" + snippetName).getText("UTF-8")).append("\n")
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

    /** Seeds a file under root. */
    static File seed(File root, String relative, String content) {
        File f = new File(root, relative)
        f.parentFile.mkdirs()
        f.setText(content, "UTF-8")
        return f
    }
}
