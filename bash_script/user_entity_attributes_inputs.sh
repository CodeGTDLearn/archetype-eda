#!/bin/bash

# PROMPT
ENTITY_NAME_PROMPT="Digite o nome de 'Entity/Model'"
ENABLE_CUSTOM_ATTRIBUTES_PROMPT=" •Add 'Custom Attributes'"
CUSTOM_ATTRIBUTES_LIST_PROMPT="  ▪Provide the 'Attributes'"

# VARIAVEIS DE SWITCHES DO MVN CLI + INPUTS
entity_name=$(user_input_or_default_value "entity_name" "EntityName" "$ENTITY_NAME_PROMPT")
enable_custom_attributes=$(user_input_or_default_value "boleana" "false" "$ENABLE_CUSTOM_ATTRIBUTES_PROMPT")

custom_attributes_list_input