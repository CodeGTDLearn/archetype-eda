#!/bin/bash

# PROMPT
ENABLE_LOGBACK_PROMPT="Habilitar custom Logback"

# VARIAVEIS DE SWITCHES DO MVN CLI + INPUTS
enable_logback=$(user_input_or_default_value "boleana" "false" "$ENABLE_LOGBACK_PROMPT")