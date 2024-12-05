#!/bin/bash

# PROMPT
ENABLE_BINDING_PROPERTIES_PROMPT="Enable 'Type Safe Property Binding'"
OPTIONS_PROMPT="-> Select 'Properties filetype':\n   1)properties;  2)yml;"
NUMERIC_OPTIONS_INTERVAL="1-2"

# VARIAVEIS DE SWITCHES DO MVN CLI + INPUTS
enable_binding_properties=$(user_input_or_default_value "boleana" "true" "$ENABLE_BINDING_PROPERTIES_PROMPT")
properties_filetype=$(user_numeric_selection_or_default_value "$OPTIONS_PROMPT" "$NUMERIC_OPTIONS_INTERVAL" "$enable_binding_properties")