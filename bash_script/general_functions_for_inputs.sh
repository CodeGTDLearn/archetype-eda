#!/bin/bash

is_compound_input_valid() {
  local input_value=$1
  local separator=$2

  local terms=(${input_value//$separator/ })

  for term in "${terms[@]}"; do
    if [ "$(is_java_reserved_word "$term")" = "true" ]; then
      echo "false"
      return
    fi
  done

  echo "true"
}

get_artifactId_input() {

  local artifactId_text='O artifactId[Nome do Projeto]'

  while true; do

    artifactId=$(get_user_input_or_default_value "artifact" "my_archetype" "$ARTIFACT_ID_PROMPT")

    if [ -d "$artifactId" ]; then
       echo -e "\n$artifactId_text \"$artifactId\" $ARTIFACT_ID_EXISTS_MESSAGE \n\n$FOLDER_ALREADY_EXISTENT_MESSAGE\n"
       pwd

       ls -l
       echo
    else
       break
    fi

  done
}

get_custom_attributes_list_input() {

  if [[ "${enable_custom_attributes,,}" =~ ^(true|t|T|TRUE|True)$ ]]; then
     custom_attributes_list=$(get_user_input_or_default_value "entity_list_attributes" "Long:id, MyId:id, My_Id:my_id" "$CUSTOM_ATTRIBUTES_LIST_PROMPT")

  else
     custom_attributes_list="false"

  fi
}