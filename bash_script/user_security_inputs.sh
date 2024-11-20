#!/bin/bash

# PROMPT
ENABLE_SECURITY_AUTHENTICATION_SUPPORT_PROMPT="Habilitar Autenticacao JWT"

# INPUTS
enable_security_authentic_support=$(user_input_or_default_value "boleana" "false" "$ENABLE_SECURITY_AUTHENTICATION_SUPPORT_PROMPT")