#!/bin/bash

# PROMPT
ENABLE_SECURITY_AUTHENTICATION_SUPPORT_PROMPT="${ITEM_MARK}Enable Security 'JWT Auth'"

# VARIAVEIS DE SWITCHES DO MVN CLI + INPUTS
enable_jwt_authentication=$(user_input_or_default_value "boleana" "false" "$ENABLE_SECURITY_AUTHENTICATION_SUPPORT_PROMPT")

# ADD PROMPT IN PROGRESS_BAR
add_item_in_progress_bar "${ENABLE_SECURITY_AUTHENTICATION_SUPPORT_PROMPT}" "${enable_jwt_authentication}"