#!/bin/bash

# 1) PROMPT
ENABLE_FOLDER_DOCKER_PROMPT="${ITEM_MARK}Enable 'Docker support'"

# 2) VARIAVEIS DE SWITCHES DO MVN CLI + INPUTS
enable_docker=$(get_user_input_or_default_value "boleana" "true" "$ENABLE_FOLDER_DOCKER_PROMPT")

# 3) ADD PROMPT IN PROGRESS_BAR
add_apostrophe_item_in_progress_bar "${ENABLE_FOLDER_DOCKER_PROMPT}" "${enable_docker}"