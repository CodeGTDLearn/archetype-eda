def h = new GroovyClassLoader(this.class.classLoader).parseClass(new File("src/test/archetype-post-generate/PostProcessorHarness.groovy")).newInstance()
int failures = 0
def check = { String n, boolean c -> if (c) { println "PASS: ${n}" } else { println "FAIL: ${n}"; failures++ } }

def seed = { File root ->
    def b = "src/main/java/it/pkg/myentity/config/properties"
    ["ApplicationProperties.java", "ApplicationPropertiesRecord.java"].each { h.seed(root, "${b}/${it}", "x") }
    ["ListObjects.java", "ListProperties.java", "ListPropertiesImmutable.java", "ListPropertiesRecord.java"].each { h.seed(root, "${b}/list_properties/${it}", "x") }
    ["MapItem.java", "MapProperties.java", "MapPropertiesImmutable.java", "MapPropertiesRecord.java"].each { h.seed(root, "${b}/map_properties/${it}", "x") }
    ["application_list.properties", "application_list.yml", "application_map.properties", "application_map.yml"].each { h.seed(root, "src/main/resources/properties/${it}", "x") }
}
def fresh = { String prefix -> def r = File.createTempDir(prefix, ""); r.deleteOnExit(); seed(r); r }
def base = "src/main/java/it/pkg/myentity/config/properties"

def r1 = fresh("bp-off-")
h.run(r1, [enable_binding_properties: "false"], "snippet-binding-properties.groovy")
check("1.1 binding=false removes the list folder", !new File(r1, "${base}/list_properties").exists())
check("1.2 binding=false removes the map folder", !new File(r1, "${base}/map_properties").exists())
check("1.3 binding=false removes the properties resources folder", !new File(r1, "src/main/resources/properties").exists())
check("1.4 binding=false removes ApplicationProperties.java", !new File(r1, "${base}/ApplicationProperties.java").exists())

def r2 = fresh("bp-nodefault-")
h.run(r2, [enable_binding_properties: "true", default_binding_properties: "false",
           list_binding_properties: "true", map_binding_properties: "true"], "snippet-binding-properties.groovy")
check("2.1 default=false removes the record", !new File(r2, "${base}/ApplicationPropertiesRecord.java").exists())
check("2.2 default=false keeps list/map folders", new File(r2, "${base}/list_properties").isDirectory() &&
        new File(r2, "${base}/map_properties").isDirectory())

def r3 = fresh("bp-nolist-")
h.run(r3, [enable_binding_properties: "true", default_binding_properties: "true",
           list_binding_properties: "false", map_binding_properties: "true"], "snippet-binding-properties.groovy")
check("3.1 list=false removes the list folder", !new File(r3, "${base}/list_properties").exists())
check("3.2 list=false removes the list resources", !new File(r3, "src/main/resources/properties/application_list.properties").exists())
check("3.3 list=false keeps the map resources", new File(r3, "src/main/resources/properties/application_map.properties").isFile())

def r4 = fresh("bp-nomap-")
h.run(r4, [enable_binding_properties: "true", default_binding_properties: "true",
           list_binding_properties: "true", map_binding_properties: "false"], "snippet-binding-properties.groovy")
check("4.1 map=false removes the map folder", !new File(r4, "${base}/map_properties").exists())
check("4.2 map=false removes the map resources", !new File(r4, "src/main/resources/properties/application_map.properties").exists())
check("4.3 map=false keeps the list resources", new File(r4, "src/main/resources/properties/application_list.properties").isFile())

def r5 = fresh("bp-mutable1-")
h.run(r5, [enable_binding_properties: "true", default_binding_properties: "true",
           list_binding_properties: "true", map_binding_properties: "true",
           mutable_binding_properties: "1"], "snippet-binding-properties.groovy")
check("5.1 mutable=1 removes the mutable sources",
        !new File(r5, "${base}/ApplicationProperties.java").exists() &&
                !new File(r5, "${base}/list_properties/ListProperties.java").exists() &&
                !new File(r5, "${base}/map_properties/MapProperties.java").exists())
check("5.2 mutable=1 keeps the immutable sources",
        new File(r5, "${base}/ApplicationPropertiesRecord.java").isFile() &&
                new File(r5, "${base}/list_properties/ListPropertiesImmutable.java").isFile())

def r6 = fresh("bp-mutable2-")
h.run(r6, [enable_binding_properties: "true", default_binding_properties: "true",
           list_binding_properties: "true", map_binding_properties: "true",
           mutable_binding_properties: "2"], "snippet-binding-properties.groovy")
check("6.1 mutable=2 removes the immutable sources",
        !new File(r6, "${base}/ApplicationPropertiesRecord.java").exists() &&
                !new File(r6, "${base}/list_properties/ListPropertiesImmutable.java").exists() &&
                !new File(r6, "${base}/map_properties/MapPropertiesImmutable.java").exists())
check("6.2 mutable=2 keeps the mutable sources",
        new File(r6, "${base}/ApplicationProperties.java").isFile() &&
                new File(r6, "${base}/list_properties/ListProperties.java").isFile())

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
