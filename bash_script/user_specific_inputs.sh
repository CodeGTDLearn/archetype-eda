#!/bin/bash

# PROMPT
ENABLE_LOMBOK_PROMPT="Habilitar Lombok"
ENABLE_LOGBACK_PROMPT="Habilitar custom Logback"

# INPUTS
enable_lombok=$(user_input_or_default_value "boleana" "false" "$ENABLE_LOMBOK_PROMPT")
enable_logback=$(user_input_or_default_value "boleana" "false" "$ENABLE_LOGBACK_PROMPT")