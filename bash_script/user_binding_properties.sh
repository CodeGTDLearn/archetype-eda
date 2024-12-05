#!/bin/bash

# PROMPT
ENABLE_BINDING_PROPERTIES_PROMPT="Habilitar Docker support"

# VARIAVEIS DE SWITCHES DO MVN CLI + INPUTS
enable_binding_properties=$(user_input_or_default_value "boleana" "true" "$ENABLE_BINDING_PROPERTIES_PROMPT")