import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.runtime.RuntimeConstants

/**
 * Renders an archetype-resources template the way the maven-archetype-plugin
 * does, so the suites can assert the MEANING of a `#if` guard instead of the
 * mere presence of a flag token.
 *
 * The context mirrors the plugin:
 *   - the generation builtins (package, packageInPathFormat, groupId,
 *     artifactId, version);
 *   - the resolved requiredProperties (the flag values), catalog default
 *     included.
 *
 * Undefined references render LITERALLY - the same as the plugin - which is
 * what keeps the `${SECRET_*}` placeholders of the credentials files intact
 * for Spring to resolve at runtime.
 */
class TemplateRenderer {

    /** The generation builtins the maven-archetype-plugin exposes. */
    static final List<String> BUILTINS = ["package", "packageInPathFormat", "groupId", "artifactId", "version"]

    /** The catalog of the built archetype: target/classes/META-INF/maven/archetype-metadata.xml */
    static File catalogFile() {
        return new File("target/classes/META-INF/maven/archetype-metadata.xml")
    }

    /** Every catalog flag with its DEFAULT value (the value the plugin applies when the flag is absent). */
    static Map<String, String> catalogDefaults() {
        Map<String, String> defaults = [:]
        File catalog = catalogFile()
        if (!catalog.isFile()) return defaults
        (catalog.getText("UTF-8") =~ /<requiredProperty\s+key="([^"]+)"\s*>\s*<defaultValue>(.*?)<\/defaultValue>/).each { m ->
            defaults[m[1]] = m[2]
        }
        return defaults
    }

    /** The catalog defaults with the given overrides applied. */
    static Map<String, String> flags(Map<String, String> overrides = [:]) {
        Map<String, String> merged = new LinkedHashMap<String, String>(catalogDefaults())
        overrides.each { k, v -> merged[k] = v }
        return merged
    }

    /** Builds the plugin context: builtins + flags (the flags win). */
    static Map<String, Object> context(Map<String, String> builtins, Map<String, String> flags) {
        Map<String, Object> ctx = [:]
        builtins.each { k, v -> ctx[k] = v }
        flags.each { k, v -> ctx[k] = v }
        return ctx
    }

    /** Renders one template file and returns the result. */
    static String render(File template, Map<String, Object> context) {
        if (!template.isFile()) {
            throw new IllegalStateException("Template not found: ${template.path}")
        }
        VelocityEngine engine = new VelocityEngine()
        engine.setProperty(RuntimeConstants.RUNTIME_LOG_INSTANCE, org.slf4j.helpers.NOPLogger.NOP_LOGGER)

        VelocityContext velocityContext = new VelocityContext()
        context.each { k, v -> velocityContext.put(k, v) }

        StringWriter writer = new StringWriter()
        engine.evaluate(velocityContext, writer, template.name, template.getText("UTF-8"))
        return writer.toString()
    }

    /** Renders one template with the given builtins and flags. */
    static String render(File template, Map<String, String> builtins, Map<String, String> flags) {
        return render(template, context(builtins, flags))
    }
}
