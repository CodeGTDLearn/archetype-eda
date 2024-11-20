#!/bin/bash

# PROMPT
#PACKAGE_PROMPT="Digite o package"
GROUPID_PROMPT="Digite o GroupId"
VERSION_PROMPT="Digite a Version"
PROJECT_NAME_PROMPT="Digite o nome do projeto"
PROJECT_DESCRIPTION_PROMPT="Descricao do projeto"

# INPUTS
#PACKAGE
groupid=$(user_input_or_default_value "groupid" "com.b1r" "$GROUPID_PROMPT")
version=$(user_input_or_default_value "numero" "1.17.1" "$VERSION_PROMPT")
project_name=$(user_input_or_default_value "model_entity" "NoName" "$PROJECT_NAME_PROMPT")
project_description=$(user_input_or_default_value "texto" "This is my description" "$PROJECT_DESCRIPTION_PROMPT")