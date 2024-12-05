#!/bin/bash

# PROMPTS
ENABLE_DEPENDENCIES_ADD_PROMPT="Habilitar POM-Dependencies customizadas"
EXTRA_DEPENDENCIES_PROMPT="Add POM-Dependencies [Delimiters , : ]"

# VARIAVEIS DE SWITCHES DO MVN CLI + INPUTS
enable_dependencies_addition=$(user_input_or_default_value "boleana" "false" "$ENABLE_DEPENDENCIES_ADD_PROMPT")
custom_dependencies_addition_input