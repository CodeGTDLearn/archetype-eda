/**
 * Contract test for the PACKAGED PROCEDURE (llm-maven-flags-inference/).
 *
 * The procedure is what the AGENT reads from the JAR to derive the contract
 * and the command line of the archetype: after the inference engine was
 * removed, it IS the mechanism. The existing BuildArtifactsTest only proves
 * that the 19 files EXIST and that the index lists them.
 *
 * This suite proves the CONTENT of the procedure:
 *
 *   A. the 6 JSON files of 04-referencia-templates-de-contrato/ are valid JSON;
 *   B. the 5 examples validate against 01-schema-do-contrato.json
 *      (a hand-rolled validator for the draft-07 subset the schema uses);
 *   C. every flag-like term the procedure mentions is a declared catalog flag;
 *   D. every path the procedure points at exists in the JAR layout;
 *   E. the rules of the nested types agree with what the entity template
 *      really renders (the rules used to contradict the template);
 *   F. the flags the procedure does NOT cite are an explicit allowlist - a new
 *      uncited flag fails the suite.
 */
import groovy.json.JsonSlurper

def procedure = new File("src/main/resources/llm-maven-flags-inference")
def resources = new File("src/main/resources")
def classes = new File("target/classes")
def catalogFile = new File(classes, "META-INF/maven/archetype-metadata.xml")

int failures = 0
def check = { String name, boolean cond ->
    if (cond) { println "PASS: ${name}" } else { println "FAIL: ${name}"; failures++ }
}

check("1.0 the built catalog exists", catalogFile.isFile())
if (!catalogFile.isFile()) {
    println ""
    println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
    if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
    return
}

def catalogFlags = new HashSet<String>()
(catalogFile.getText("UTF-8") =~ /<requiredProperty\s+key="([^"]+)"/).each { m -> catalogFlags << m[1] }

def slurper = new JsonSlurper()

// ---------------------------------------------------------------------------
// A. the JSON files of the procedure are valid JSON
// ---------------------------------------------------------------------------
def contractTemplates = new File(procedure, "04-referencia-templates-de-contrato")
def jsonFiles = contractTemplates.listFiles().findAll { it.isFile() && it.name.endsWith(".json") }.sort { it.name }
def invalidJson = []
def parsed = [:]
jsonFiles.each { File f ->
    try {
        parsed[f.name] = slurper.parse(f)
    } catch (Throwable t) {
        invalidJson << "${f.name}: ${t.message}"
    }
}
check("1.1 every JSON file of 04-referencia-templates-de-contrato/ is valid JSON: ${invalidJson}", invalidJson.isEmpty())
check("1.2 the procedure carries the 6 contract templates", jsonFiles.size() == 6)

// ---------------------------------------------------------------------------
// B. the examples validate against the schema
//
// A hand-rolled validator for the draft-07 subset the schema uses: type,
// properties, pattern, additionalProperties, oneOf, $ref and items.
// It is a METHOD (not a closure) so the recursion is straightforward.
// ---------------------------------------------------------------------------
def validateSchema(Object schema, Object instance, Object rootSchema, String path) {
    List<String> errors = []
    if (!(schema instanceof Map)) return errors
    if (schema.containsKey('$ref')) {
        String ref = schema['$ref']
        if (!ref.startsWith("#/")) {
            errors << "${path}: $ref nao suportado"
            return errors
        }
        def target = rootSchema
        ref.substring(2).split("/").each { String part ->
            target = target instanceof Map ? target[part] : null
        }
        if (target == null) {
            errors << "${path}: $ref nao resolve"
            return errors
        }
        return validateSchema(target, instance, rootSchema, path)
    }
    if (schema.containsKey('oneOf')) {
        int valid = schema.oneOf.count { validateSchema(it, instance, rootSchema, path).isEmpty() }
        if (valid != 1) errors << "${path}: oneOf nao satisfeito (${valid} ramos validos)"
        return errors
    }
    String type = schema.type
    if (type == "object") {
        if (!(instance instanceof Map)) {
            errors << "${path}: esperado object, veio ${instance?.getClass()?.simpleName}"
            return errors
        }
        if (schema.properties instanceof Map) {
            schema.properties.each { String key, Object sub ->
                if (instance.containsKey(key)) {
                    errors.addAll(validateSchema(sub, instance[key], rootSchema, "${path}.${key}"))
                }
            }
        }
        if (schema.additionalProperties == false && schema.properties instanceof Map) {
            instance.keySet().each { String key ->
                if (!schema.properties.containsKey(key)) errors << "${path}.${key}: propriedade nao permitida"
            }
        }
        return errors
    }
    if (type == "array") {
        if (!(instance instanceof List)) {
            errors << "${path}: esperado array"
            return errors
        }
        if (schema.items != null) {
            instance.eachWithIndex { Object item, int i ->
                errors.addAll(validateSchema(schema.items, item, rootSchema, "${path}[${i}]"))
            }
        }
        return errors
    }
    if (type == "string") {
        if (!(instance instanceof String)) {
            errors << "${path}: esperado string"
            return errors
        }
        if (schema.pattern != null && !(instance ==~ schema.pattern)) {
            errors << "${path}: '${instance}' nao casa com ${schema.pattern}"
        }
        return errors
    }
    return errors
}

def schemaFile = new File(contractTemplates, "01-schema-do-contrato.json")
def schema = parsed[schemaFile.name]
def schemaErrors = [:]
if (schema != null) {
    jsonFiles.findAll { it.name != schemaFile.name }.each { File f ->
        def errors = validateSchema(schema, parsed[f.name], schema, f.name)
        if (!errors.isEmpty()) schemaErrors[f.name] = errors
    }
}
check("1.3 every contract example validates against 01-schema-do-contrato.json: ${schemaErrors}",
        schemaErrors.isEmpty())

// ---------------------------------------------------------------------------
// C. every flag-like term of the procedure is a declared catalog flag
// ---------------------------------------------------------------------------
def mdFiles = []
procedure.eachFileRecurse { File f -> if (f.isFile() && f.name.endsWith(".md")) mdFiles << f }
// terms that look like a flag but are not one (metadata keywords, env vars)
def nonFlagTerms = ["CONTRACT_DERIVED", "DEPENDS_ON", "XDG_CACHE_HOME"]
def unknownTerms = []
mdFiles.each { File f ->
    (f.getText("UTF-8") =~ /`([^`]+)`/).each { m ->
        String token = m[1]
        if (!(token ==~ /^[a-z][a-z0-9_]*_[a-z0-9_]+$/)) return
        if (!catalogFlags.contains(token) && !nonFlagTerms.contains(token)) {
            unknownTerms << "${f.name}: ${token}"
        }
    }
}
check("1.4 every flag-like term of the procedure is a declared catalog flag: ${unknownTerms}",
        unknownTerms.isEmpty())

// ---------------------------------------------------------------------------
// D. every path the procedure points at exists in the JAR layout
// ---------------------------------------------------------------------------
def brokenPaths = []
mdFiles.each { File f ->
    (f.getText("UTF-8") =~ /`([^`]*\/[^`\s]*)`/).each { m ->
        String path = m[1]
        // only the paths that live INSIDE the JAR are validated
        if (!(path.startsWith("llm-maven-flags-inference/") || path.startsWith("META-INF/") || path.startsWith("archetype-resources/"))) return
        // a glob is not a path
        if (path.contains("*")) return
        boolean found = new File(resources, path).exists() ||
                new File(classes, path).exists() ||
                new File(resources, path + ".md").exists() ||
                new File(resources, path + ".json").exists() ||
                new File(resources, path + ".xml").exists()
        if (!found) brokenPaths << "${f.name}: ${path}"
    }
}
check("1.5 every path the procedure points at exists in the JAR layout: ${brokenPaths}", brokenPaths.isEmpty())

// ---------------------------------------------------------------------------
// E. the rules of the nested types agree with the entity template
// ---------------------------------------------------------------------------
def entityTemplate = new File(resources,
        "archetype-resources/src/main/java/__entity_package_name__/__entity_name__.java")
def rulesFile = new File(procedure,
        "03-derivacao-regras-de-formacao-do-contrato/03-como-derivar-flags-de-tipos-aninhados.md")
def exampleFile = new File(contractTemplates, "03-exemplo-contrato-com-objetos-aninhados.json")

def templateUsesFqn = entityTemplate.getText("UTF-8").contains("\${nested_package_full}.\${attrib.type}")
def rulesText = rulesFile.getText("UTF-8")
def exampleText = exampleFile.getText("UTF-8")
check("1.6 the entity template renders the nested type fully qualified", templateUsesFqn)
check("1.7 the nested-type rules do not claim a simple-name reference",
        !(templateUsesFqn && rulesText.contains("pelo nome simples")))
check("1.8 the nested-type example does not claim a simple-name reference",
        !(templateUsesFqn && exampleText.contains("pelo nome simples")))

// ---------------------------------------------------------------------------
// F. the flags the procedure does NOT cite are an explicit allowlist
// ---------------------------------------------------------------------------
def corpusParts = []
procedure.eachFileRecurse { File f -> if (f.isFile()) corpusParts << f.getText("UTF-8") }
def corpus = corpusParts.join("\n")

// The procedure MUST derive these flags from the contract.
def contractFlags = ["entity_name", "entity_package_name", "enable_custom_entity_attributes",
                     "custom_entity_attributes_list", "custom_entity_attribute_imports",
                     "contract_enum_definitions", "contract_nested_type_definitions",
                     "entity_contract_file_path", "enable_folder_src_test_java"]
def uncitedContract = contractFlags.findAll { !corpus.contains(it) }
check("1.9 the procedure cites every contract-derivation flag: ${uncitedContract}", uncitedContract.isEmpty())

// The flags the procedure does NOT derive: they are chosen by the operator on
// the command line. A NEW uncited flag must be added here on purpose.
def allowedUncited = [
        "api_port", "binding_external_properties_mutability", "config_file_format",
        "enable_api_cpanel_elk_log_system", "enable_binding_external_properties",
        "enable_binding_external_properties_tests", "enable_binding_external_properties_validation",
        "enable_default_binding_external_properties", "enable_docker_support",
        "enable_git_support", "enable_list_binding_external_properties",
        "enable_logback_appender_file", "enable_logback_appender_smtp", "enable_logback_support",
        "enable_lombok_support", "enable_map_binding_external_properties", "extra_pom_dependencies",
        "git_repo_branch", "git_repo_url", "java_version", "llm_prompt_language",
        "logback_console_type", "project_description", "project_i18n_language",
        "show_archetype_post_generate_log"
]
def uncited = catalogFlags.findAll { !corpus.contains(it) } as List
def unexpectedUncited = (uncited - allowedUncited).sort()
check("1.10 every flag NOT cited by the procedure is an explicit allowlist entry: ${unexpectedUncited}",
        unexpectedUncited.isEmpty())

// ---------------------------------------------------------------------------
// G. the destination of generation is native, not a catalog flag
//
// Feature 005 made the destination an explicit spec entry. It travels in the
// native `outputDirectory` property of the generation goal, so it MUST NOT
// become a catalog flag, and the contract meta-keys MUST stay untouched.
// ---------------------------------------------------------------------------
def regras01 = new File(procedure,
        "03-derivacao-regras-de-formacao-do-contrato/01-como-derivar-a-entidade-do-contrato.md")
        .getText("UTF-8")

check("1.11 the destination property outputDirectory is NOT a catalog flag",
        !catalogFlags.contains("outputDirectory"))

def metaKeyLine = regras01.readLines().find { it.contains("meta-chaves (`entityName`") }
def originalMetaKeys = ["entityName", "entity", "package", "description", "attributes", "fields"]
check("1.12 01 keeps exactly the six contract meta-keys",
        metaKeyLine != null && originalMetaKeys.every { metaKeyLine.contains(it) })
check("1.13 the destination marker is not one of the contract meta-keys",
        metaKeyLine != null && !metaKeyLine.contains("PASTA DE DESTINO"))

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
