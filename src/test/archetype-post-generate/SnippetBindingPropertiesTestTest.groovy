def h = new GroovyClassLoader(this.class.classLoader).parseClass(new File("src/test/archetype-post-generate/PostProcessorHarness.groovy")).newInstance()
int failures = 0
def check = { String n, boolean c -> if (c) { println "PASS: ${n}" } else { println "FAIL: ${n}"; failures++ } }

def base = "src/test/java/myentity/config/properties"
def seed = { File root ->
    ["ApplicationPropertiesRecordTest.java", "ApplicationPropertiesRecordValidationTest.java",
     "ApplicationPropertiesTest.java", "ApplicationPropertiesValidationTest.java"].each { h.seed(root, "${base}/${it}", "x") }
    ["ListPropertiesTest.java", "ListPropertiesImmutableTest.java", "ListPropertiesRecordTest.java"].each { h.seed(root, "${base}/list_properties/${it}", "x") }
    ["MapPropertiesTest.java", "MapPropertiesImmutableTest.java", "MapPropertiesRecordTest.java"].each { h.seed(root, "${base}/map_properties/${it}", "x") }
    ["application_list_test.properties", "application_list_test.yml", "application_map_test.properties", "application_map_test.yml"].each { h.seed(root, "src/test/resources/properties/${it}", "x") }
}
def fresh = { String p -> def r = File.createTempDir(p, ""); r.deleteOnExit(); seed(r); r }

def r1 = fresh("bpt-off-")
h.run(r1, [enable_binding_properties_tests: "false", enable_binding_properties: "true",
           default_binding_properties: "true", list_binding_properties: "true", map_binding_properties: "true"], "snippet-binding-properties-test.groovy")
check("1.1 tests=false removes the test properties folder", !new File(r1, base).exists())
check("1.2 tests=false removes the test resources folder", !new File(r1, "src/test/resources/properties").exists())

def r2 = fresh("bpt-nodefault-")
h.run(r2, [enable_binding_properties_tests: "true", enable_binding_properties: "true",
           default_binding_properties: "false", list_binding_properties: "true", map_binding_properties: "true"], "snippet-binding-properties-test.groovy")
check("2.1 default=false removes ApplicationPropertiesTest", !new File(r2, "${base}/ApplicationPropertiesTest.java").exists())
check("2.2 default=false removes ApplicationPropertiesValidationTest", !new File(r2, "${base}/ApplicationPropertiesValidationTest.java").exists())
check("2.3 default=false also removes the record test",
        !new File(r2, "${base}/ApplicationPropertiesRecordTest.java").exists())

def r3 = fresh("bpt-nolist-")
h.run(r3, [enable_binding_properties_tests: "true", enable_binding_properties: "true",
           default_binding_properties: "true", list_binding_properties: "false", map_binding_properties: "true",
           validated_binding_properties: "true"], "snippet-binding-properties-test.groovy")
check("3.1 list=false removes the list test folder", !new File(r3, "${base}/list_properties").exists())
check("3.2 list=false removes the list test resources", !new File(r3, "src/test/resources/properties/application_list_test.properties").exists())
check("3.3 list=false keeps the map test resources", new File(r3, "src/test/resources/properties/application_map_test.properties").isFile())

def r4 = fresh("bpt-nomap-")
h.run(r4, [enable_binding_properties_tests: "true", enable_binding_properties: "true",
           default_binding_properties: "true", list_binding_properties: "true", map_binding_properties: "false",
           validated_binding_properties: "true"], "snippet-binding-properties-test.groovy")
check("4.1 map=false removes the map test folder", !new File(r4, "${base}/map_properties").exists())
check("4.2 map=false removes the map test resources", !new File(r4, "src/test/resources/properties/application_map_test.properties").exists())
check("4.3 map=false keeps the list test resources", new File(r4, "src/test/resources/properties/application_list_test.properties").isFile())

def r5 = fresh("bpt-mutable1-")
h.run(r5, [enable_binding_properties_tests: "true", enable_binding_properties: "true",
           default_binding_properties: "true", list_binding_properties: "true", map_binding_properties: "true",
           mutable_binding_properties: "1", validated_binding_properties: "true"], "snippet-binding-properties-test.groovy")
check("5.1 mutable=1 removes the mutable test classes",
        !new File(r5, "${base}/ApplicationPropertiesTest.java").exists() &&
                !new File(r5, "${base}/list_properties/ListPropertiesTest.java").exists() &&
                !new File(r5, "${base}/map_properties/MapPropertiesTest.java").exists())
check("5.2 mutable=1 keeps the record test class", new File(r5, "${base}/ApplicationPropertiesRecordTest.java").isFile())

def r6 = fresh("bpt-mutable2-")
h.run(r6, [enable_binding_properties_tests: "true", enable_binding_properties: "true",
           default_binding_properties: "true", list_binding_properties: "true", map_binding_properties: "true",
           mutable_binding_properties: "2", validated_binding_properties: "true"], "snippet-binding-properties-test.groovy")
check("6.1 mutable=2 removes the immutable test classes",
        !new File(r6, "${base}/ApplicationPropertiesRecordTest.java").exists() &&
                !new File(r6, "${base}/list_properties/ListPropertiesImmutableTest.java").exists() &&
                !new File(r6, "${base}/map_properties/MapPropertiesImmutableTest.java").exists())
check("6.2 mutable=2 keeps the mutable test class", new File(r6, "${base}/ApplicationPropertiesTest.java").isFile())

def r7 = fresh("bpt-novalidated-")
h.run(r7, [enable_binding_properties_tests: "true", enable_binding_properties: "true",
           default_binding_properties: "true", list_binding_properties: "true", map_binding_properties: "true",
           validated_binding_properties: "false"], "snippet-binding-properties-test.groovy")
check("7.1 validated=false removes the validation test classes",
        !new File(r7, "${base}/ApplicationPropertiesRecordValidationTest.java").exists() &&
                !new File(r7, "${base}/ApplicationPropertiesValidationTest.java").exists())
check("7.2 validated=false keeps the non-validation tests", new File(r7, "${base}/ApplicationPropertiesRecordTest.java").isFile())

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
