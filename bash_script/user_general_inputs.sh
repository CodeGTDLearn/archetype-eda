#!/bin/bash

# 1) PROMPT
GROUPID_PROMPT="${ITEM_MARK}Digite o 'GroupId'"
PROJECT_DESCRIPTION_PROMPT="${ITEM_MARK}Project 'Description'"
PROJECT_VERSION_PROMPT="${ITEM_MARK}Digite a 'Project Version'"
API_PORT_PROMPT="${ITEM_MARK}Define 'API Port'"


# 2) VARIAVEIS DE SWITCHES DO MVN CLI + INPUTS
# OBSERVACAO
# groupid: (https://maven.apache.org/pom.html#Maven_Coordinates)
# * Company-Domain Invertido "WITHOUT" the 'PROJECT-NAME' + '/' + 'ARTIFACT_ID'
groupId=$(get_user_input_or_default_value "groupid" "com.domain" "$GROUPID_PROMPT")
project_description=$(get_user_input_or_default_value "texto" "This is my description" "$PROJECT_DESCRIPTION_PROMPT")
version=$(get_user_input_or_default_value "numero" "1.17.1" "$PROJECT_VERSION_PROMPT")
api_port=$(get_user_input_or_default_value "numero" "8080" "$API_PORT_PROMPT")