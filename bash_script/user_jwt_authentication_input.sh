#!/bin/bash

# 1) PROMPT
ENABLE_SECURITY_AUTHENTICATION_SUPPORT_PROMPT="${ITEM_MARK}Enable Security 'JWT Auth'"

# 2) VARIAVEIS DE SWITCHES DO MVN CLI + INPUTS
enable_jwt_authentication=$(get_user_input_or_default_value "boleana" "false" "$ENABLE_SECURITY_AUTHENTICATION_SUPPORT_PROMPT")

# 3) ADD PROMPT IN PROGRESS_BAR
add_apostrophe_item_in_progress_bar "${ENABLE_SECURITY_AUTHENTICATION_SUPPORT_PROMPT}" "${enable_jwt_authentication}"