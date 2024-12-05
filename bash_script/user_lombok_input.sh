#!/bin/bash

# 1) PROMPT
ENABLE_LOMBOK_PROMPT="${ITEM_MARK}Enable 'Lombok'"

# 2) VARIAVEIS DE SWITCHES DO MVN CLI + INPUTS
enable_lombok=$(get_user_input_or_default_value "boleana" "true" "$ENABLE_LOMBOK_PROMPT")

# 3) ADD PROMPT IN PROGRESS_BAR
add_apostrophe_item_in_progress_bar "${ENABLE_LOMBOK_PROMPT}" "${enable_lombok}"