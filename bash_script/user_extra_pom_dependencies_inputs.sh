#!/bin/bash

# 1) PROMPTS
ENABLE_EXTRA_POM_DEPENDENCIES_PROMPT="${ITEM_MARK}Adicionar Extra 'POM-Dependencies'"
EXTRA_POM_DEPENDENCIES_PROMPT="${TOPIC_MARK}Dependencies to add"

# 2) VARIAVEIS DE SWITCHES DO MVN CLI + INPUTS
enable_extra_pom_dependencies_addition=$(get_user_input_or_default_value "boleana" "false" "$ENABLE_EXTRA_POM_DEPENDENCIES_PROMPT")
get_extra_pom_dependencies_addition_input

# 3) ADD PROMPT IN PROGRESS_BAR
add_item_in_progress_bar "${ENABLE_EXTRA_POM_DEPENDENCIES_PROMPT}" "${enable_extra_pom_dependencies_addition}"