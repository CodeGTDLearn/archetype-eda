#!/bin/bash

# PROMPTS
ENABLE_EXTRA_POM_DEPENDENCIES_PROMPT="Adicionar 'Extra POM-Dependencies'"
EXTRA_POM_DEPENDENCIES_PROMPT=" •Dependencies to add"

# VARIAVEIS DE SWITCHES DO MVN CLI + INPUTS
enable_extra_pom_dependencies_addition=$(user_input_or_default_value "boleana" "false" "$ENABLE_EXTRA_POM_DEPENDENCIES_PROMPT")
extra_pom_dependencies_addition_input