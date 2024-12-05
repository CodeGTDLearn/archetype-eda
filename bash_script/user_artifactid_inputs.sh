#!/bin/bash


# 1) PROMPT
ARTIFACT_ID_PROMPT="${ITEM_MARK}Digite o 'ArtifactId'"

# 2) VARIAVEIS DE SWITCHES DO MVN CLI + INPUTS
get_artifactId_input() {

  local artifactId_text='O artifactId'

  while true; do

    artifactId=$(get_user_input_or_default_value "artifact" "api_v1" "$ARTIFACT_ID_PROMPT")

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
get_artifactId_input