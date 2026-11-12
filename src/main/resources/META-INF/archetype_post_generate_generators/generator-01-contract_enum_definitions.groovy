// -----------------------------------------------------------------------------

// GENERATOR-01-CONTRACT_ENUM_DEFINITIONS.GROOVY -------------------------------
// FR-021 (contract types): the ENUMS derived from the contract are generated
// as Java sources in the generated project.
//
// The flag contract_enum_definitions carries one definition per enum:
//
//     "Tipo:CONSTANTE|CONSTANTE"     (definitions separated by ';')
//
// A bare "Tipo" is resolved against the package of the entity
// (${package}.${entity_package_name}); a qualified "br.com.projeto.Tipo" is
// generated in its own package. The relative marker "nested.Tipo" (used by the
// payload-derived nested enums) is resolved against the entity's 'nested'
// subpackage.
//
// NOTE: "nested." is a RESERVED marker. A real top-level package literally
// named "nested" cannot be expressed in this flag: the marker always resolves
// to the entity's nested subpackage. Any other layout must use a qualified
// package name.
//
// An enum file that already exists is NEVER overwritten: the generation is
// skipped with a warning, so an existing source is preserved.

if (contract_enum_definitions != null && !contract_enum_definitions.trim().isEmpty()
        && contract_enum_definitions.trim() != 'none'
        && contract_enum_definitions.trim() != 'null') {

    def base_package = "${properties?.package}".trim()

    if (!isPackageKnown(base_package)) {
        println "WARNING: the package of the project is unknown - the enums were NOT generated."
        log_text += "\nCUSTOM_ENUM: package unknown - the enums were NOT generated\n"
    } else {

        def entity_package = resolveEntityPackage(base_package, entity_package_name)
        StringBuilder enum_log = new StringBuilder()

        contract_enum_definitions.split(';').each { String definition ->
            String entry = definition.trim()
            if (entry.isEmpty()) return

            int separator = entry.indexOf(':')
            if (separator <= 0) {
                println "WARNING: contract_enum_definitions entry '" + entry + "' is invalid - skipped."
                enum_log.append("\nCUSTOM_ENUM: ").append(entry).append(" - invalid definition - skipped\n")
                return
            }

            String type_name = entry.substring(0, separator).trim()
            String constants = entry.substring(separator + 1).trim()

            String enum_package = entity_package
            String simple_name = type_name
            if (type_name.startsWith('nested.')) {
                // Relative marker used by the payload-derived nested enums:
                // the enum lives in the entity's 'nested' subpackage.
                enum_package = entity_package + ".nested"
                simple_name = type_name.substring('nested.'.length()).trim()
            } else if (type_name.contains('.')) {
                enum_package = type_name.substring(0, type_name.lastIndexOf('.')).trim()
                simple_name = type_name.substring(type_name.lastIndexOf('.') + 1).trim()
            }

            List<String> enum_constants = constants.split('\\|').collect { it.trim() }.findAll { !it.isEmpty() }
            if (simple_name.isEmpty() || enum_constants.isEmpty()) {
                println "WARNING: contract_enum_definitions entry '" + entry + "' has no constant - skipped."
                enum_log.append("\nCUSTOM_ENUM: ").append(entry).append(" - no constant - skipped\n")
                return
            }

            Path enum_folder = srcMainJava
            if (enum_package != null && !enum_package.isEmpty()) {
                enum_folder = srcMainJava.resolve(enum_package.replace('.', '/'))
            }

            StringBuilder enum_source = new StringBuilder()
            enum_source.append("package ").append(enum_package).append(";\n\n")
            enum_source.append("/**\n")
            enum_source.append(" * ").append(simple_name).append(" - enum gerado pelo archetype a partir do contrato JSON.\n")
            enum_source.append(" */\n")
            enum_source.append("public enum ").append(simple_name).append(" {\n")
            enum_source.append(enum_constants.collect { "    " + it }.join(",\n"))
            enum_source.append("\n}\n")

            Path enum_file = writeJavaSourceIfAbsent(enum_folder, simple_name, enum_source.toString())

            if (enum_file == null) {
                println "WARNING: enum '" + type_name + "' already exists - NOT overwritten."
                enum_log.append("\nCUSTOM_ENUM: ").append(type_name)
                        .append(" already exists - NOT overwritten\n")
                return
            }

            enum_log.append("\nCUSTOM_ENUM: ").append(type_name)
                    .append("\n            -> ").append(enum_file).append("\n")
            println "Enum generated: " + type_name + " -> " + enum_file
        }

        log_text += enum_log.toString()
    }
}