#!/bin/bash

# 1) PROMPT
ENTITY_NAME_PROMPT="${ITEM_MARK}Nome da 'Entity'"
ENABLE_CUSTOM_ATTRIBUTES_PROMPT="${TOPIC_MARK}Add 'Custom Attributes'"
CUSTOM_ATTRIBUTES_LIST_PROMPT="${OPTION_MARK}Provide the 'Attributes'"

# 2) VARIAVEIS DE SWITCHES DO MVN CLI + INPUTS
entity_name=$(get_user_input_or_default_value "entity_name" "MyEntity" "$ENTITY_NAME_PROMPT")
enable_custom_attributes=$(get_user_input_or_default_value "boleana" "false" "$ENABLE_CUSTOM_ATTRIBUTES_PROMPT")

get_custom_attributes_list_input

#carrega 'entity_name_package' com um versao 'lowercase' de 'entity_name'
#Pois, por convencao os packages, nao possuem letras maiusculas
entity_name_package=${entity_name,,}

# 3) ADD PROMPT IN PROGRESS_BAR
add_item_in_progress_bar "${ENTITY_NAME_PROMPT}" "true"