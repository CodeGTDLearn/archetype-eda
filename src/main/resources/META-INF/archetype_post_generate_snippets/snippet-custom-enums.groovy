// SNIPPET-CUSTOM-ENUMS.GROOVY ----------------------------------------------
// FR-021 (contract types): the ENUMS derived from the contract are generated
// as Java sources in the generated project.
//
// The flag custom_enum_definitions carries one definition per enum:
//
//     "Tipo:CONSTANTE|CONSTANTE"     (definitions separated by ';')
//
// A bare "Tipo" is resolved against the package of the entity
// (${package}.${entity_name_package}); a qualified "br.com.projeto.Tipo" is
// generated in its own package.
//
// An enum file that already exists is NEVER overwritten: the generation is
// skipped with a warning, so an existing source is preserved.

if (custom_enum_definitions_value != null && !custom_enum_definitions_value.trim().isEmpty()
        && custom_enum_definitions_value.trim() != 'none'
        && custom_enum_definitions_value.trim() != 'null') {

    def base_package = "${properties?.package}".trim()
    def entity_package = base_package
    if (entity_name_package_value != null && !entity_name_package_value.trim().isEmpty()) {
        entity_package = entity_package + "." + entity_name_package_value.trim()
    }

    if (base_package.isEmpty() || base_package == "null") {
        println "WARNING: the package of the project is unknown - the enums were NOT generated."
        log_text += "\nCUSTOM_ENUM: package unknown - the enums were NOT generated\n"
    } else {

        StringBuilder enum_log = new StringBuilder()

        custom_enum_definitions_value.split(';').each { String definition ->
            String entry = definition.trim()
            if (entry.isEmpty()) return

            int separator = entry.indexOf(':')
            if (separator <= 0) {
                println "WARNING: custom_enum_definitions entry '" + entry + "' is invalid - skipped."
                enum_log.append("\nCUSTOM_ENUM: ").append(entry).append(" - invalid definition - skipped\n")
                return
            }

            String type_name = entry.substring(0, separator).trim()
            String constants = entry.substring(separator + 1).trim()

            String enum_package = entity_package
            String simple_name = type_name
            if (type_name.contains('.')) {
                enum_package = type_name.substring(0, type_name.lastIndexOf('.')).trim()
                simple_name = type_name.substring(type_name.lastIndexOf('.') + 1).trim()
            }

            List<String> enum_constants = constants.split('\\|').collect { it.trim() }.findAll { !it.isEmpty() }
            if (simple_name.isEmpty() || enum_constants.isEmpty()) {
                println "WARNING: custom_enum_definitions entry '" + entry + "' has no constant - skipped."
                enum_log.append("\nCUSTOM_ENUM: ").append(entry).append(" - no constant - skipped\n")
                return
            }

            Path enum_folder = srcMainJava
            if (enum_package != null && !enum_package.isEmpty()) {
                enum_folder = srcMainJava.resolve(enum_package.replace('.', '/'))
            }
            enum_folder.toFile().mkdirs()
            Path enum_file = enum_folder.resolve(simple_name + '.java')

            if (enum_file.toFile().exists()) {
                println "WARNING: enum '" + type_name + "' already exists - NOT overwritten."
                enum_log.append("\nCUSTOM_ENUM: ").append(type_name)
                        .append(" already exists - NOT overwritten\n")
                return
            }

            StringBuilder enum_source = new StringBuilder()
            enum_source.append("package ").append(enum_package).append(";\n\n")
            enum_source.append("/**\n")
            enum_source.append(" * ").append(simple_name).append(" - enum gerado pelo archetype a partir do contrato JSON.\n")
            enum_source.append(" */\n")
            enum_source.append("public enum ").append(simple_name).append(" {\n")
            enum_source.append(enum_constants.collect { "    " + it }.join(",\n"))
            enum_source.append("\n}\n")
            enum_file.toFile().text = enum_source.toString()

            enum_log.append("\nCUSTOM_ENUM: ").append(type_name)
                    .append("\n            -> ").append(enum_file).append("\n")
            println "Enum generated: " + type_name + " -> " + enum_file
        }

        log_text += enum_log.toString()
    }
}
