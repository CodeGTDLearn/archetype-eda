#!/bin/bash

# PROMPT
ENABLE_FOLDER_DOCKER_PROMPT="Habilitar Docker support"

# VARIAVEIS DE SWITCHES DO MVN CLI + INPUTS
enable_docker=$(user_input_or_default_value "boleana" "true" "$ENABLE_FOLDER_DOCKER_PROMPT")