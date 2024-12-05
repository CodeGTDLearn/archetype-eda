#!/bin/bash

# 1) PROMPT
ENTITY_NAME_PROMPT="${ITEM_MARK}Nome do 'Modulo'"
ENABLE_CUSTOM_ATTRIBUTES_PROMPT="${TOPIC_MARK}Add 'Custom Attributes'"
CUSTOM_ATTRIBUTES_LIST_PROMPT="${OPTION_MARK}Provide the 'Attributes'"

# 2) VARIAVEIS DE SWITCHES DO MVN CLI + INPUTS
entity_name=$(get_user_input_or_default_value "entity_name" "MyEntity" "$ENTITY_NAME_PROMPT")
enable_custom_attributes=$(get_user_input_or_default_value "boleana" "false" "$ENABLE_CUSTOM_ATTRIBUTES_PROMPT")

get_custom_attributes_list_input() {

  if [[ "${enable_custom_attributes,,}" =~ ^(true|t|T|TRUE|True)$ ]]; then
     custom_attributes_list=$(get_user_input_or_default_value "entity_list_attributes" "Long:id, MyId:id, My_Id:my_id" "$CUSTOM_ATTRIBUTES_LIST_PROMPT")

  else
     custom_attributes_list="false"

  fi
}
get_custom_attributes_list_input

#carrega 'entity_name_package' com um versao 'lowercase' de 'entity_name'
#Pois, por convencao os packages, nao possuem letras maiusculas
entity_name_package=${entity_name,,}

# 3) ADD PROMPT IN PROGRESS_BAR
add_item_in_progress_bar "${ENTITY_NAME_PROMPT}" "true"