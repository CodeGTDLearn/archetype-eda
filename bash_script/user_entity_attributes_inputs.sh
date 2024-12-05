#!/bin/bash

# 1) PROMPT
# 1.1) ENTITY CREATION OPTIONS
ENTITY_OPTIONS="${TOPIC_MARK}1)Manual\n${TOPIC_MARK}2)JSON Converter\n"
ENTITY_CREATION_OPTIONS_PROMPT="${ITEM_MARK}'Entity Creation':\n${ENTITY_OPTIONS}"
ENTITY_OPT_INTERVAL="1-2"

# 1.2) MAIN ENTITY
ENTITY_NAME_PROMPT="${TOPIC_MARK}Digite o nome do 'Entity'"
ENABLE_CUSTOM_ATTRIBUTES_PROMPT="${TOPIC_MARK}Add 'Custom Attributes'"
CUSTOM_ATTRIBUTES_LIST_PROMPT="${OPTION_MARK}Provide the 'Attributes'"

# 1.3) JSON CONVERTER ENTITY
JSON_PATH_PROMPT="\n${TOPIC_MARK}Enter 'JSON' file location"
ENABLE_DENORMALIZATION_PROMPT="${TOPIC_MARK}Adicionar entidades colecionadas/aninhadas ('Denormalizar')"

# 2) VARIAVEIS DE SWITCHES DO MVN CLI + INPUTS
# 2.1) CREATION SPECIFIC VARIABLES
custom_attributes_list="false"
entity_creation_option="1"
entity_name=""
enable_custom_attributes=""
json_file_path=""
enable_denormalization="false"
absolute_path_jsonfile=""
RESULTADO="false"


# 2.2) ENTITY CREATION OPTIONS
entity_creation_option=$(get_user_numeric_selection_or_default_value "$ENTITY_CREATION_OPTIONS_PROMPT" "$ENTITY_OPT_INTERVAL" "true" "1")

# 2.3) MAIN ENTITY
if [[ "$entity_creation_option" -eq 1 ]]; then
  entity_name=$(get_user_input_or_default_value "entity_name" "MyEntity" "$ENTITY_NAME_PROMPT")
  enable_custom_attributes=$(get_user_input_or_default_value "boleana" "false" "$ENABLE_CUSTOM_ATTRIBUTES_PROMPT")

  get_custom_attributes_list_input() {
  if [[ "${enable_custom_attributes,,}" =~ ^(true|t|T|TRUE|True)$ ]]; then
  custom_attributes_list=$(get_user_input_or_default_value "entity_list_attributes" "Long:id, MyId:id, My_Id:my_id" "$CUSTOM_ATTRIBUTES_LIST_PROMPT")
  fi
 }
  get_custom_attributes_list_input

# 2.3.1) ADD PROMPT IN PROGRESS_BAR
  add_apostrophe_item_in_progress_bar "${ENTITY_NAME_PROMPT}" "true"
fi

# 2.4) JSON CONVERTER ENTITY
if [[ "$entity_creation_option" -eq 2 ]]; then

  while [ "$RESULTADO" = "false" ]; do
    json_file_path=$(get_user_input_or_default_value "path_linux_json" "/json/model.json" "$JSON_PATH_PROMPT")
    absolute_path_jsonfile=$(pwd)$json_file_path
    RESULTADO=$(check_file_existence "$absolute_path_jsonfile" "Invalid Path or File not found. Try again\n")
  done

  validar_json "$absolute_path_jsonfile"
  entity_name=$(get_filename_checking_path "$absolute_path_jsonfile")
  echo -e "  ${OPTION_MARK}Entity Name extracted: ${entity_name}"

#  if [[ $(validar_json "$absolute_path_jsonfile") -eq 0 ]]; then
#    echo -e "JSON-File is invalid.\n"
#    exit 1
#  fi

#  custom_attributes_list=$(get_json_properties_and_types "$absolute_path_jsonfile" "false")
#  echo -e "  ${OPTION_MARK}Entity Attributes extracted: ${custom_attributes_list}\n"

  enable_denormalization=$(get_user_input_or_default_value "boleana" "false" "$ENABLE_DENORMALIZATION_PROMPT")

  # 2.4.1)ADD PROMPT IN PROGRESS_BAR
  add_apostrophe_item_in_progress_bar "${DENORMALIZE_EXPLANATION_PROMPT}" "true"
fi



#carrega 'entity_name_package' com um versao 'lowercase' de 'entity_name'
#Pois, por convencao os packages, nao possuem letras maiusculas
entity_name_package=${entity_name,,}