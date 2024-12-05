#!/bin/bash

# PROMPT
ENABLE_BINDING_PROPERTIES_PROMPT="${ITEM_MARK}Enable 'Type Safe Property Binding'"
DEFAULT_BIND_PROPS_PROMPT="${TOPIC_MARK}Default Binding"
LIST_BIND_PROPS_PROMPT="${TOPIC_MARK}List Binding"
MAP_BIND_PROPS_PROMPT="${TOPIC_MARK}Map Binding"
MUTABLE_OPT_1="${OPTION_MARK}1)Immutable\n${OPTION_MARK}2)Mutable\n${OPTION_MARK}3)Both\n"
MUTABLE_BIND_PROPS_PROMPT="${TOPIC_MARK}Mutability:\n${MUTABLE_OPT_1}"
MUTABLE_NUM_OPT_INTERVAL="1-3"
VALIDATED_BIND_PROPS_PROMPT="${TOPIC_MARK}Enable 'Properties Validation'"
BINDING_PROPERTIES_TEST_PROMPT="${TOPIC_MARK}Properties 'Test Support'"


# VARIAVEIS DE SWITCHES DO MVN CLI + INPUTS
enable_binding_properties=$(user_input_or_default_value "boleana" "true" "$ENABLE_BINDING_PROPERTIES_PROMPT")
default_binding_properties=$(user_input_or_default_value "boleana" "true" "$DEFAULT_BIND_PROPS_PROMPT" "$enable_binding_properties")
list_binding_properties=$(user_input_or_default_value "boleana" "false" "$LIST_BIND_PROPS_PROMPT" "$enable_binding_properties")
map_binding_properties=$(user_input_or_default_value "boleana" "false" "$MAP_BIND_PROPS_PROMPT" "$enable_binding_properties")

if [[ "$default_binding_properties" = "true" ||
      "$list_binding_properties" = "true" ||
      "$map_binding_properties" = "true"
   ]]; then
          binding_properties_styles_enabled=1
       else
          binding_properties_styles_enabled=0
fi

mutable_binding_properties=false
if [[ "$binding_properties_styles_enabled" -eq 1 ]]; then
mutable_binding_properties=$(user_numeric_selection_or_default_value "$MUTABLE_BIND_PROPS_PROMPT" "$MUTABLE_NUM_OPT_INTERVAL" "$enable_binding_properties" "${OPTION_MARK}Default")
fi

validated_binding_properties="false"
if [[ "$default_binding_properties" = "true" ]]; then
validated_binding_properties=$(user_input_or_default_value "boleana" "false" "$VALIDATED_BIND_PROPS_PROMPT" "$enable_binding_properties")
fi

enable_binding_properties_tests="false"
if [[ "$enable_folder_src_test_java" = "true" && "$binding_properties_styles_enabled" -eq 1 ]]; then
enable_binding_properties_tests=$(user_input_or_default_value "boleana" "false" "$BINDING_PROPERTIES_TEST_PROMPT" "$enable_binding_properties")
fi