// SNIPPET-CUSTOM-NESTED-TYPES.GROOVY ---------------------------------------
// FR-021 (contract types): the NESTED objects of the contract are generated
// as plain POJOs in the 'nested' subpackage of the entity:
//
//     <package>.<entity_name_package>.nested.<Tipo>
//
// The flag custom_nested_type_definitions carries one definition per POJO:
//
//     "Tipo|Campo:Tipo,Campo:Tipo"     (definitions separated by ';')
//
// The POJOs are intentionally plain: no Lombok, no JPA, explicit getters and
// setters. A field type that the flag does not declare is emitted verbatim:
// if it is not a java.lang or an existing project type, the Java compilation
// of the generated project fails loudly instead of silently dropping it.
//
// A POJO file that already exists is NEVER overwritten: the generation is
// skipped with a warning, so an existing source is preserved.

if (custom_nested_type_definitions_value != null && !custom_nested_type_definitions_value.trim().isEmpty()
        && custom_nested_type_definitions_value.trim() != 'none'
        && custom_nested_type_definitions_value.trim() != 'null') {

    def base_package = "${properties?.package}".trim()
    def entity_package = base_package
    if (entity_name_package_value != null && !entity_name_package_value.trim().isEmpty()) {
        entity_package = entity_package + "." + entity_name_package_value.trim()
    }

    if (base_package.isEmpty() || base_package == "null") {
        println "WARNING: the package of the project is unknown - the nested POJOs were NOT generated."
        log_text += "\nCUSTOM_NESTED_TYPE: package unknown - the nested POJOs were NOT generated\n"
    } else {

        String nested_package = entity_package + ".nested"
        Path nested_folder = srcMainJava.resolve(nested_package.replace('.', '/'))
        nested_folder.toFile().mkdirs()
        StringBuilder nested_log = new StringBuilder()

        custom_nested_type_definitions_value.split(';').each { String definition ->
            String entry = definition.trim()
            if (entry.isEmpty()) return

            int separator = entry.indexOf('|')
            if (separator <= 0) {
                println "WARNING: custom_nested_type_definitions entry '" + entry + "' is invalid - skipped."
                nested_log.append("\nCUSTOM_NESTED_TYPE: ").append(entry)
                        .append(" - invalid definition - skipped\n")
                return
            }

            String type_name = entry.substring(0, separator).trim()
            String field_list = entry.substring(separator + 1).trim()
            if (type_name.isEmpty() || field_list.isEmpty()) {
                println "WARNING: custom_nested_type_definitions entry '" + entry + "' has no field - skipped."
                nested_log.append("\nCUSTOM_NESTED_TYPE: ").append(entry)
                        .append(" - no field - skipped\n")
                return
            }

            List<Map<String, String>> fields = []
            for (String raw_field : field_list.split(',')) {
                String field_entry = raw_field.trim()
                if (field_entry.isEmpty()) continue
                int field_separator = field_entry.indexOf(':')
                if (field_separator <= 0) {
                    println "WARNING: custom_nested_type_definitions field '" + field_entry +
                            "' of '" + type_name + "' is invalid - skipped."
                    nested_log.append("\nCUSTOM_NESTED_TYPE: ").append(field_entry)
                            .append(" - invalid field - skipped\n")
                    continue
                }
                String field_name = field_entry.substring(0, field_separator).trim()
                String field_type = field_entry.substring(field_separator + 1).trim()
                if (field_name.isEmpty() || field_type.isEmpty()) {
                    println "WARNING: custom_nested_type_definitions field '" + field_entry +
                            "' of '" + type_name + "' has an empty name or type - skipped."
                    nested_log.append("\nCUSTOM_NESTED_TYPE: ").append(field_entry)
                            .append(" - empty name or type - skipped\n")
                    continue
                }
                fields.add([name: field_name, type: field_type])
            }

            if (fields.isEmpty()) {
                println "WARNING: custom_nested_type_definitions entry '" + entry + "' has no valid field - skipped."
                nested_log.append("\nCUSTOM_NESTED_TYPE: ").append(entry)
                        .append(" - no valid field - skipped\n")
                return
            }

            Path nested_file = nested_folder.resolve(type_name + '.java')
            if (nested_file.toFile().exists()) {
                println "WARNING: nested type '" + type_name + "' already exists - NOT overwritten."
                nested_log.append("\nCUSTOM_NESTED_TYPE: ").append(type_name)
                        .append(" already exists - NOT overwritten\n")
                return
            }

            StringBuilder source = new StringBuilder()
            source.append("package ").append(nested_package).append(";\n\n")
            source.append("/**\n")
            source.append(" * ").append(type_name)
                    .append(" - POJO gerado pelo archetype a partir de um objeto aninhado do contrato JSON.\n")
            source.append(" */\n")
            source.append("public class ").append(type_name).append(" {\n\n")

            for (Map<String, String> field : fields) {
                source.append("    private ").append(field.type).append(" ")
                        .append(field.name).append(";\n")
            }
            source.append("\n")

            for (Map<String, String> field : fields) {
                String cap = field.name.substring(0, 1).toUpperCase() + field.name.substring(1)
                source.append("    public ").append(field.type).append(" get").append(cap).append("() {\n")
                source.append("        return this.").append(field.name).append(";\n")
                source.append("    }\n\n")
                source.append("    public void set").append(cap).append("(")
                        .append(field.type).append(" ").append(field.name).append(") {\n")
                source.append("        this.").append(field.name).append(" = ").append(field.name).append(";\n")
                source.append("    }\n\n")
            }

            source.append("}\n")
            nested_file.toFile().text = source.toString()

            nested_log.append("\nCUSTOM_NESTED_TYPE: ").append(type_name)
                    .append("\n            -> ").append(nested_file).append("\n")
            println "Nested type generated: " + type_name + " -> " + nested_file
        }

        log_text += nested_log.toString()
    }
}
