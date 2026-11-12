/**
 * The post-processor as the PRODUCTION runs it.
 *
 * The other suites evaluate ONE asset at a time against a synthetic
 * binding. The real run is different:
 *   - the script that runs is the COMPOSED artifact (body + variables +
 *     generators + snippets + footer + functions, in that order);
 *   - the binding exposes EVERY property as a direct variable - that is how
 *     the archetype plugin binds the requiredProperties;
 *   - `properties` is request.properties (body-02), not a value injected by
 *     the test.
 *
 * This suite runs the composed artifact with that binding, so the
 * interactions between the stages are covered at last.
 */
def h = new GroovyClassLoader(this.class.classLoader)
        .parseClass(new File("src/test/archetype-post-generate/PostProcessorHarness.groovy")).newInstance()

int failures = 0
def check = { String n, boolean c -> if (c) { println "PASS: ${n}" } else { println "FAIL: ${n}"; failures++ } }

def composed = new File("target/classes/META-INF/archetype-post-generate.groovy")
check("1.1 the composed post-processor exists (process-resources ran)", composed.isFile())

def baseFlags = [
        enable_jwt_authentication: "false",
        enable_binding_external_properties: "false",
        enable_binding_external_properties_tests: "false",
        enable_default_binding_external_properties: "false",
        enable_list_binding_external_properties: "false",
        enable_map_binding_external_properties: "false",
        enable_binding_external_properties_validation: "false",
        binding_external_properties_mutability: "none",
        enable_api_cpanel_elk_log_system: "false",
        enable_docker_support: "false",
        enable_custom_entity_attributes: "false",
        custom_entity_attributes_list: "false",
        custom_entity_attribute_imports: "none",
        contract_enum_definitions: "none",
        contract_nested_type_definitions: "none",
        entity_contract_file_path: "none",
        enable_git_support: "false",
        enable_logback_support: "false",
        logback_console_type: "1",
        enable_logback_appender_smtp: "false",
        enable_logback_appender_file: "false",
        enable_lombok_support: "false",
        extra_pom_dependencies: "false",
        config_file_format: "1",
        show_archetype_post_generate_log: "false",
        enable_folder_src_test_java: "false"
]

// a contract on disk, outside the project
File contractSource = File.createTempFile("contract-", ".json")
contractSource.deleteOnExit()
contractSource.setText('{"entity_name":"Cliente","nome":"Maria Silva"}', "UTF-8")

// ---------------------------------------------------------------------------
// A. the whole pipeline runs: the GENERATOR writes, the SNIPPET prunes and
//    the FOOTER prints the report
// ---------------------------------------------------------------------------
File rootA = File.createTempDir("pipeline-full-", "")
rootA.deleteOnExit()
h.seed(rootA, "src/main/java/it/pkg/myentity/config/authentication/MyEntitySecurityConfig.java", "class X {}")
h.seed(rootA, "src/main/resources/security/appJwt.key", "PRIVATE")

Map flagsA = new LinkedHashMap<String, String>(baseFlags)
flagsA.put("enable_folder_src_test_java", "true")
flagsA.put("entity_contract_file_path", contractSource.absolutePath)
flagsA.put("contract_enum_definitions", "Status:ATIVO|CANCELADO")
flagsA.put("show_archetype_post_generate_log", "true")

String outA = h.runFull(rootA, flagsA)

check("2.1 the pipeline does not break", outA != null)
check("2.2 the GENERATOR persisted the contract",
        new File(rootA, "src/test/resources/contracts/contract.json").isFile())
check("2.3 the GENERATOR wrote the enum",
        new File(rootA, "src/main/java/it/pkg/myentity/Status.java").isFile())
check("2.4 the SNIPPET pruned the JWT artifacts",
        !new File(rootA, "src/main/resources/security/appJwt.key").exists() &&
                !new File(rootA, "src/main/java/it/pkg/myentity/config/authentication").exists())
check("2.5 the FOOTER printed the report (the log flag reaches the binding)",
        outA.contains("POS-PROCESSOR") && outA.contains("ENDING"))
check("2.6 the report mentions the generated enum",
        outA.contains("Status.java") || outA.contains("Status"))

// ---------------------------------------------------------------------------
// B. with the src/test tree off, the contract is NOT persisted
// ---------------------------------------------------------------------------
File rootB = File.createTempDir("pipeline-notest-", "")
rootB.deleteOnExit()
Map flagsB = new LinkedHashMap<String, String>(baseFlags)
flagsB.put("entity_contract_file_path", contractSource.absolutePath)
flagsB.put("show_archetype_post_generate_log", "false")

String outB = h.runFull(rootB, flagsB)

check("3.1 the pipeline does not break with the src/test tree off", outB != null)
check("3.2 the contract is NOT persisted with the src/test tree off",
        !new File(rootB, "src/test/resources/contracts/contract.json").exists())
check("3.3 the report is NOT printed when the log flag is false",
        !outB.contains("POS-PROCESSOR"))

// ---------------------------------------------------------------------------
// C. body-02 binds request.properties: the run only succeeds because the
//    coercion holds
// ---------------------------------------------------------------------------
check("4.1 the composed artifact binds request.properties",
        composed.getText("UTF-8").contains("Properties properties = request.properties"))
check("4.2 the composed artifact carries the footer that prints the report",
        composed.getText("UTF-8").contains("BODY-11-FOOTER.GROOVY"))

// ---------------------------------------------------------------------------
// D. the production ORDER: the generators run before the snippets, and the
//    footer before the helper functions
// ---------------------------------------------------------------------------
String composedSource = composed.getText("UTF-8")
int generatorPos = composedSource.indexOf("// GENERATOR-01-CONTRACT_ENUM_DEFINITIONS.GROOVY")
int snippetPos = composedSource.indexOf("// SNIPPET-01-ENABLE_JWT_AUTHENTICATION.GROOVY")
int footerPos = composedSource.indexOf("// BODY-11-FOOTER.GROOVY")
int functionsPos = composedSource.indexOf("// BODY-12-FILE-FUNCTIONS.GROOVY")
check("5.1 the composed artifact runs the generators before the snippets",
        generatorPos >= 0 && snippetPos > generatorPos)
check("5.2 the composed artifact runs the footer before the helper functions",
        footerPos >= 0 && functionsPos > footerPos)

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
