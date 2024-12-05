#!/bin/bash

# PROMPT
ENABLE_LOMBOK_PROMPT="Enable 'Lombok'"

# VARIAVEIS DE SWITCHES DO MVN CLI + INPUTS
enable_lombok=$(user_input_or_default_value "boleana" "true" "$ENABLE_LOMBOK_PROMPT")