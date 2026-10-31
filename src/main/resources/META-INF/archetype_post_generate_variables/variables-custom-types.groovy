// VARIABLES-CUSTOM-TYPES.GROOVY ---------------------------------------------
// The metadata of the PROJECT types (types outside java.lang) derived from
// the JSON contract by the inference mechanism:
//
//   - custom_attribute_imports : the FQNs of the referenced types; the entity
//                                template imports them.
//   - custom_enum_definitions  : the enums the post-processor must generate,
//                                one definition per enum:
//                                "Tipo:CONSTANTE|CONSTANTE" (separated by ';').
//
// Both default to 'none' (the project carries no custom type).

def custom_attribute_imports_value = "${properties?.custom_attribute_imports}"
def custom_enum_definitions_value = "${properties?.custom_enum_definitions}"
def entity_name_package_value = "${properties?.entity_name_package}"
