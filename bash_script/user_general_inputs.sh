#!/bin/bash

# PROMPT
GROUPID_PROMPT="Digite o 'GroupId'"
VERSION_PROMPT="Digite a 'Version'"
ENTITY_NAME_PROMPT="Digite o nome de 'Entity/Model'"
PROJECT_DESCRIPTION_PROMPT="Project 'Description'"

#VARIAVEIS DE SWITCHES DO MVN CLI + INPUTS

# OBSERVACAO 1
# groupid: (https://maven.apache.org/pom.html#Maven_Coordinates)
# * Company-Domain Invertido "WITHOUT" the 'PROJECT-NAME'/'ARTIFACT_ID'
groupid=$(user_input_or_default_value "groupid" "com.domain" "$GROUPID_PROMPT")
project_description=$(user_input_or_default_value "texto" "This is my description" "$PROJECT_DESCRIPTION_PROMPT")
version=$(user_input_or_default_value "numero" "1.17.1" "$VERSION_PROMPT")

# OBSERVACAO 2
entity_name=$(user_input_or_default_value "entity_name" "EntityName" "$ENTITY_NAME_PROMPT")