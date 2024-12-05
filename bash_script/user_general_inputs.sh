#!/bin/bash

# PROMPT
GROUPID_PROMPT="Digite o 'GroupId'"
VERSION_PROMPT="Digite a 'Project Version'"
PROJECT_DESCRIPTION_PROMPT="Project 'Description'"
JAVA_VERSION_OPT_PROMPT="Java Version:\n •1)'17'\n •2)'21'\n •Default"
VERSION_NUMERIC_OPT_INTERVAL="1-2"

# VARIAVEIS DE SWITCHES DO MVN CLI + INPUTS

# OBSERVACAO
# groupid: (https://maven.apache.org/pom.html#Maven_Coordinates)
# * Company-Domain Invertido "WITHOUT" the 'PROJECT-NAME'/'ARTIFACT_ID'
groupid=$(user_input_or_default_value "groupid" "com.domain" "$GROUPID_PROMPT")
project_description=$(user_input_or_default_value "texto" "This is my description" "$PROJECT_DESCRIPTION_PROMPT")
version=$(user_input_or_default_value "numero" "1.17.1" "$VERSION_PROMPT")
java_choice=$(user_numeric_selection_or_default_value "$JAVA_VERSION_OPT_PROMPT" "$VERSION_NUMERIC_OPT_INTERVAL" "true")
case $java_choice in
  1) java_version=17 ;;
  2) java_version=21 ;;
esac