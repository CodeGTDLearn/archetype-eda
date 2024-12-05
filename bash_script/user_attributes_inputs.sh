#!/bin/bash

# PROMPT
ENABLE_CUSTOM_ATTRIBUTES_PROMPT="-> Add 'Custom Attributes'"
CUSTOM_ATTRIBUTES_LIST_PROMPT="  -> Provide the 'Attributes'"

# VARIAVEIS DE SWITCHES DO MVN CLI + INPUTS
enable_custom_attributes=$(user_input_or_default_value "boleana" "false" "$ENABLE_CUSTOM_ATTRIBUTES_PROMPT")

custom_attributes_list_input