/**
 * Consistency test between the CONTRACT and the archetype integration
 * profiles that carry NESTED objects (FR-021).
 *
 * The mechanism (FlagProposalMerger) turns the operator's pure payload into
 * the flags the archetype consumes. Each profile declares those flags BY HAND
 * (archetype.properties) and carries the contract that produced them
 * (sample-contract.json). This suite closes the loop: the flags derived from
 * the contract MUST be exactly the flags the profile declares.
 *
 * It also validates the derived values against the REAL catalog regex, so a
 * divergence in the metadata fragment (property-entity.xml) breaks here
 * instead of silently falling back to 'none' at generation time.
 *
 * No JUnit infra in this repo; runnable directly:
 *   groovy src/test/pre-processamento-maven-flags-via-llm/domain/NestedProfilesConsistencyTest.groovy
 * (also executed by the build - see src/test/groovy/run-maven-flags-via-llm-tests.groovy)
 */

import groovy.json.JsonSlurper
import groovy.xml.XmlSlurper

def profiles = ["custom-nested-objects", "custom-nested-edge-cases", "custom-enums-from-payload"]

def moduleRoot = new File("src/main/pre-processamento-maven-flags-via-llm")
def sourceIndex = [:]
moduleRoot.eachFileRecurse { File f ->
    if (f.isFile() && f.name.endsWith(".groovy")) sourceIndex[f.name] = f
}
def gcl = new GroovyClassLoader(this.class.classLoader)
gcl.setResourceLoader({ String className ->
    String simple = className.substring(className.lastIndexOf(".") + 1)
    File source = sourceIndex[simple + ".groovy"]
    return source == null ? null : source.toURI().toURL()
} as groovy.lang.GroovyResourceLoader)
def catalogEntryClass = gcl.loadClass("eda.postgenerate.pre_processamento_maven_flags_via_llm.catalog.CatalogEntry")
def resolverClass = gcl.loadClass("eda.postgenerate.pre_processamento_maven_flags_via_llm.domain.ContractTypeResolver")
def deriverClass = gcl.loadClass("eda.postgenerate.pre_processamento_maven_flags_via_llm.domain.ContractEntityDeriver")
def mergerClass = gcl.loadClass("eda.postgenerate.pre_processamento_maven_flags_via_llm.domain.FlagProposalMerger")

int failures = 0
def check = { String name, boolean cond ->
    if (cond) { println "PASS: ${name}" } else { println "FAIL: ${name}"; failures++ }
}

def mkEntry = { String k, String d, String r -> catalogEntryClass.newInstance(k, d, r, "") }

// ---------------------------------------------------------------------------
// 1) The REAL catalog (built metadata when available, fragments otherwise)
// ---------------------------------------------------------------------------
def catalog = new LinkedHashMap<String, Object>()
def builtMetadata = new File("target/classes/META-INF/maven/archetype-metadata.xml")
if (builtMetadata.exists()) {
    def parsed = new XmlSlurper().parse(builtMetadata)
    parsed.requiredProperties.requiredProperty.each { node ->
        String key = node.@key.text().toString()
        catalog.put(key, mkEntry(key, node.defaultValue.text().toString(),
                node.validationRegex.text().toString()))
    }
} else {
    def propsDir = new File("src/main/resources/META-INF/maven/archetype_metadata_required_properties")
    def slurper = new XmlSlurper()
    propsDir.listFiles().sort { it.name }.each { File f ->
        if (!f.name.endsWith(".xml")) return
        def fragment = f.getText("UTF-8").replaceAll(/(?s)^.*?\?>/, "").trim()
        def parsed = slurper.parseText("<catalog>" + fragment + "</catalog>")
        parsed.requiredProperty.each { node ->
            String key = node.@key.text().toString()
            catalog.put(key, mkEntry(key, node.defaultValue.text().toString(),
                    node.validationRegex.text().toString()))
        }
    }
}
check("1.1 the catalog was loaded", catalog != null && catalog.size() >= 35)

// ---------------------------------------------------------------------------
// 2) Every profile: contract -> merger -> flags == archetype.properties
// ---------------------------------------------------------------------------
def flags = ["entity_name", "custom_attributes_list", "custom_nested_type_definitions",
             "custom_attribute_imports", "custom_enum_definitions"]

profiles.eachWithIndex { String profile, int index ->
    def profileDir = new File("src/test/resources/projects/${profile}")
    def contractFile = new File(profileDir, "sample-contract.json")
    def propertiesFile = new File(profileDir, "archetype.properties")
    check("2.${index + 1}.1 the contract of '${profile}' exists", contractFile.isFile())
    check("2.${index + 1}.2 the properties of '${profile}' exist", propertiesFile.isFile())
    if (!contractFile.isFile() || !propertiesFile.isFile()) return

    def declared = new LinkedHashMap<String, String>()
    propertiesFile.eachLine { String line ->
        def trimmed = line.trim()
        if (trimmed.isEmpty() || trimmed.startsWith("#")) return
        int separator = trimmed.indexOf("=")
        if (separator <= 0) return
        declared.put(trimmed.substring(0, separator).trim(), trimmed.substring(separator + 1).trim())
    }
    def contract = (Map<String, Object>) new JsonSlurper().parse(contractFile)
    def merger = mergerClass.newInstance(deriverClass.newInstance(resolverClass.newInstance()))
    def merged = (Map) merger.mergeWithProvenance([:], contract, catalog)
    def values = (Map<String, String>) merged.get("values")
    def sources = (Map<String, String>) merged.get("sources")

    flags.each { String flag ->
        check("3.${index + 1}.${flags.indexOf(flag) + 1} '${profile}' declares '${flag}'",
                declared.containsKey(flag))
        check("3.${index + 1}.${flags.indexOf(flag) + 1}b the derived '${flag}' matches the profile",
                values.get(flag) == declared.get(flag))
    }

    check("4.${index + 1} the entity name comes from the contract",
            "CONTRACT".equals(sources.get("entity_name")))
    ["custom_attributes_list", "custom_nested_type_definitions",
     "custom_attribute_imports", "custom_enum_definitions"].eachWithIndex { String flag, int flagIndex ->
        check("4.${index + 1}.${flagIndex + 1} '${flag}' of '${profile}' is CONTRACT_DERIVED",
                "CONTRACT_DERIVED".equals(sources.get(flag)))
    }

    flags.eachWithIndex { String flag, int flagIndex ->
        def entry = catalog.get(flag)
        check("5.${index + 1}.${flagIndex + 1} '${flag}' of '${profile}' satisfies the real regex",
                entry != null && values.get(flag) ==~ entry.validationRegex)
    }
}

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
