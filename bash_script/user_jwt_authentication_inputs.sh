#!/bin/bash

# PROMPT
ENABLE_SECURITY_AUTHENTICATION_SUPPORT_PROMPT="Habilitar Autenticacao JWT"

# VARIAVEIS DE SWITCHES DO MVN CLI + INPUTS
enable_jwt_authentication=$(user_input_or_default_value "boleana" "false" "$ENABLE_SECURITY_AUTHENTICATION_SUPPORT_PROMPT")