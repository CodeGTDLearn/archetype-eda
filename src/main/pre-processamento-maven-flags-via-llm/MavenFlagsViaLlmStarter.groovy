package eda.postgenerate.pre_processamento_maven_flags_via_llm

import groovy.lang.GroovyClassLoader
import groovy.lang.GroovyResourceLoader

/**
 * CLI entry point.
 *
 * Responsibility: bootstrap only. The application flow lives in
 * `InferenceApplication`; every other responsibility lives in its own package
 * (catalog, contract, cache, command, domain, inference, presentation) or in
 * the module root when its folder would contain a single file.
 *
 * Why the dynamic loader: the operator invokes this file directly
 * (`groovy MavenFlagsViaLlmStarter.groovy ...`) and `groovy.ui.GroovyMain` compiles a
 * single source file without resolving sibling sources. The loader below
 * indexes the module's `.groovy` files and resolves each collaborator on
 * demand, preserving the single-entry-point invocation while allowing the
 * implementation to be split across files and folders.
 */
class MavenFlagsViaLlmStarter {

    static void main(String[] args) {
        File baseDir = moduleDirectory()
        Map<String, File> sources = indexSources(baseDir)
        GroovyClassLoader loader = new GroovyClassLoader(MavenFlagsViaLlmStarter.class.classLoader)
        loader.setResourceLoader({ String className ->
            String simple = className.substring(className.lastIndexOf('.') + 1)
            File source = sources.get(simple + '.groovy')
            return source == null ? null : source.toURI().toURL()
        } as GroovyResourceLoader)

        Class applicationClass = loader.loadClass(
                'eda.postgenerate.pre_processamento_maven_flags_via_llm.inference.InferenceApplication')
        Object application = applicationClass.newInstance()
        int exitCode = (int) application.run(args)
        System.exit(exitCode)
    }

    private static File moduleDirectory() {
        try {
            def location = MavenFlagsViaLlmStarter.class.protectionDomain?.codeSource?.location
            if (location != null) {
                File candidate = new File(location.toURI())
                return candidate.isDirectory() ? candidate : candidate.parentFile
            }
        } catch (Exception ignored) {
            // Fall through to the repository-relative location.
        }
        return new File(System.getProperty("user.dir"), "src/main/pre-processamento-maven-flags-via-llm")
    }

    private static Map<String, File> indexSources(File baseDir) {
        Map<String, File> sources = new LinkedHashMap<>()
        baseDir.eachFileRecurse { File file ->
            if (file.isFile() && file.name.endsWith('.groovy')) {
                sources.put(file.name, file)
            }
        }
        return sources
    }
}
