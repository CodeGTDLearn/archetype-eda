/**
 * Unit test for every post-processor VARIABLES file (11/11).
 * Each variable file maps archetype properties to the locals the snippets
 * consume; a missing property must become the string "null".
 */
def dir = new File("src/main/resources/META-INF/archetype_post_generate_variables")

int failures = 0
def check = { String n, boolean c -> if (c) { println "PASS: ${n}" } else { println "FAIL: ${n}"; failures++ } }

def capture = { String fileName, Map<String, String> props, List<String> names ->
    def source = new File(dir, fileName).getText("UTF-8")
    def properties = new Properties()
    props.each { k, v -> properties.put(k, v) }
    def binding = new Binding()
    binding.setVariable("properties", properties)
    def captureScript = "captured = [" + names.collect { "${it}: ${it}" }.join(", ") + "]"
    new GroovyShell(binding).evaluate(source + "\n" + captureScript)
    return (Map) binding.getVariable("captured")
}

check("0.1 all 11 variable files exist",
        dir.listFiles().findAll { it.name.endsWith(".groovy") }.size() == 11)

// 1) authentication
def auth = capture("variables-authentication.groovy", [enable_jwt_authentication: "true"], ["keep_jwt_authentication"])
check("1.1 authentication maps enable_jwt_authentication", auth.keep_jwt_authentication == "true")
check("1.2 authentication missing property -> 'null'",
        capture("variables-authentication.groovy", [:], ["keep_jwt_authentication"]).keep_jwt_authentication == "null")

// 2) binding properties
def binding = capture("variables-binding-properties.groovy",
        [enable_binding_properties: "true", map_binding_properties: "true", list_binding_properties: "false",
         default_binding_properties: "true", mutable_binding_properties: "2", validated_binding_properties: "true"],
        ["keep_binding_properties", "keep_map_binding_properties", "keep_list_binding_properties",
         "keep_default_binding_properties", "mutable_binding_properties_choice", "keep_validated_binding_properties"])
check("2.1 binding maps every sub-flag",
        binding.keep_binding_properties == "true" && binding.keep_map_binding_properties == "true" &&
                binding.keep_list_binding_properties == "false" && binding.keep_default_binding_properties == "true" &&
                binding.mutable_binding_properties_choice == "2" && binding.keep_validated_binding_properties == "true")

// 3) binding properties tests
def bindingTests = capture("variables-binding-properties-test.groovy",
        [enable_binding_properties_tests: "true"], ["keep_binding_properties_tests"])
check("3.1 binding-tests maps enable_binding_properties_tests", bindingTests.keep_binding_properties_tests == "true")

// 4) cpanel
def cpanel = capture("variables-cpanel.groovy", [enable_api_cpanel_elk_log_system: "true"], ["keep_cpanel_elk_log_system"])
check("4.1 cpanel maps enable_api_cpanel_elk_log_system", cpanel.keep_cpanel_elk_log_system == "true")
check("4.2 cpanel missing property -> 'null'",
        capture("variables-cpanel.groovy", [:], ["keep_cpanel_elk_log_system"]).keep_cpanel_elk_log_system == "null")

// 5) custom types (already covered by VariablesCustomTypesTest; kept here for 11/11)
def custom = capture("variables-custom-types.groovy",
        [custom_attribute_imports: "a.B", custom_enum_definitions: "T:X", entity_name_package: "pkg"],
        ["custom_attribute_imports_value", "custom_enum_definitions_value", "entity_name_package_value"])
check("5.1 custom types maps the three contract channels",
        custom.custom_attribute_imports_value == "a.B" && custom.custom_enum_definitions_value == "T:X" &&
                custom.entity_name_package_value == "pkg")

// 6) docker
def docker = capture("variables-docker.groovy", [enable_docker: "true"], ["keep_docker"])
check("6.1 docker maps enable_docker", docker.keep_docker == "true")

// 7) extra pom dependencies
def extra = capture("variables-extra-pom-dependencies.groovy", [extra_POM_dependencies: "a:b:1"], ["extra_POM_dependencies"])
check("7.1 extra pom dependencies are passed through", extra.extra_POM_dependencies == "a:b:1")

// 8) github
def github = capture("variables-github.groovy", [enable_git_support: "true"], ["keep_git_support"])
check("8.1 github maps enable_git_support", github.keep_git_support == "true")

// 9) logback
def logback = capture("variables-logback.groovy",
        [enable_logback: "true", logback_console_type: "3",
         enable_logback_appender_smtp: "true", enable_logback_appender_file: "false"],
        ["keep_logback", "console_type", "keep_appender_smtp", "keep_appender_file"])
check("9.1 logback maps the four sub-flags",
        logback.keep_logback == "true" && logback.console_type == "3" &&
                logback.keep_appender_smtp == "true" && logback.keep_appender_file == "false")

// 10) properties filetype
def filetype = capture("variables-properties-filetype.groovy", [properties_filetype: "2"], ["properties_filetype_choice"])
check("10.1 filetype maps properties_filetype", filetype.properties_filetype_choice == "2")

// 11) src test java
def srcTest = capture("variables-src-test-java.groovy", [enable_folder_src_test_java: "true"], ["keep_src_test_java"])
check("11.1 src-test-java maps enable_folder_src_test_java", srcTest.keep_src_test_java == "true")
check("11.2 src-test-java missing property -> 'null'",
        capture("variables-src-test-java.groovy", [:], ["keep_src_test_java"]).keep_src_test_java == "null")

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
