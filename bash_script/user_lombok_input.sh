#!/bin/bash

# PROMPT
ENABLE_LOMBOK_PROMPT="Habilitar Lombok"

# VARIAVEIS DE SWITCHES DO MVN CLI + INPUTS
enable_lombok=$(user_input_or_default_value "boleana" "false" "$ENABLE_LOMBOK_PROMPT")