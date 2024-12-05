#!/bin/bash

# PROMPT
ENABLE_BINDING_PROPERTIES_PROMPT="Enable 'Type Safe Property Binding'"
DEFAULT_BINDING_PROPERTIES_PROMPT="-> Default Binding"
LIST_BINDING_PROPERTIES_PROMPT="-> List Binding"
MAP_BINDING_PROPERTIES_PROMPT="-> Map Binding"
BINDING_PROPERTIES_TEST_PROMPT="-> Enable 'Test Support'"
MUTABLE_BINDING_PROPERTIES_PROMPT="Enable 'Mutable Properties'"
VALIDATED_BINDING_PROPERTIES_PROMPT="Enable 'Validated Binding Properties'"


# VARIAVEIS DE SWITCHES DO MVN CLI + INPUTS
enable_binding_properties=$(user_input_or_default_value "boleana" "true" "$ENABLE_BINDING_PROPERTIES_PROMPT")
default_binding_properties=$(user_input_or_default_value "boleana" "true" "$DEFAULT_BINDING_PROPERTIES_PROMPT" "$enable_binding_properties")
list_binding_properties=$(user_input_or_default_value "boleana" "false" "$LIST_BINDING_PROPERTIES_PROMPT" "$enable_binding_properties")
map_binding_properties=$(user_input_or_default_value "boleana" "false" "$MAP_BINDING_PROPERTIES_PROMPT" "$enable_binding_properties")
enable_binding_properties_tests=$(user_input_or_default_value "boleana" "false" "$BINDING_PROPERTIES_TEST_PROMPT" "$enable_binding_properties")
mutable_binding_properties=$(user_input_or_default_value "boleana" "false" "$MUTABLE_BINDING_PROPERTIES_PROMPT" "$enable_binding_properties")
validated_binding_properties=$(user_input_or_default_value "boleana" "false" "$VALIDATED_BINDING_PROPERTIES_PROMPT" "$enable_binding_properties")