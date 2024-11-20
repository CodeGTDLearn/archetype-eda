#!/bin/bash

# PROMPT
ENABLE_CUSTOM_ATTRIBUTES_PROMPT="Habilitar atributos customizados"
CUSTOM_ATTRIBUTES_LIST_PROMPT="Defina os atributos do Model"

# INPUTS
enable_custom_attributes=$(user_input_or_default_value "boleana" "false" "$ENABLE_CUSTOM_ATTRIBUTES_PROMPT")

custom_attributes_list_input