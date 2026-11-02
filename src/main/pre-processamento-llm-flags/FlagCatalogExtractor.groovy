package eda.postgenerate.llmflag

import groovy.xml.XmlSlurper

/**
 * Reads the canonical archetype metadata and produces a Map<String, CatalogEntry>.
 * See spec.md FR-005, FR-013, FR-018; data-model.md Entity 3.
 *
 * Path: reads target/classes/META-INF/maven/archetype-metadata.xml (already XInclude-merged
 * by xml-maven-plugin:1.2.1 + identity.xsl in phase generate-resources).
 *
 * Enforces FR-018: every <requiredProperty> MUST have a non-empty <defaultValue>.
 */
class FlagCatalogExtractor {

    Map<String, CatalogEntry> extract(java.io.File metadataFile) {
        if (!metadataFile.exists()) {
            throw new IllegalStateException("Flag catalog not found: ${metadataFile.absolutePath}")
        }
        def parsed = new XmlSlurper().parse(metadataFile)
        Map<String, CatalogEntry> catalog = new LinkedHashMap<>()
        parsed.requiredProperties.requiredProperty.each { node ->
            String key = node.@key.text()
            String defaultValue = node.defaultValue.text()
            String validationRegex = node.validationRegex.text()
            String description = node.description.text()

            if (defaultValue == null || defaultValue.isEmpty()) {
                throw new IllegalStateException(
                    "Flag '${key}' has empty defaultValue. Catalog is invalid per FR-018. " +
                    "Every <requiredProperty> MUST have a non-empty <defaultValue>."
                )
            }

            catalog.put(key, new CatalogEntry(key, defaultValue, validationRegex, description))
        }
        return catalog
    }
}
