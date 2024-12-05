#!/bin/bash

# PROMPT
ENABLE_LOGBACK_PROMPT="Habilitar custom Logback"
DEFAULT_OPTION="1"
OPTIONS_PROMPT="Selecione o 'Console':\n1) Default, 2) Tabular ou 3) Granular"
NUMERIC_INTERVAL_ALLOWED="1-3"

# VARIAVEIS DE SWITCHES DO MVN CLI + INPUTS
enable_logback=$(user_input_or_default_value "boleana" "false" "$ENABLE_LOGBACK_PROMPT")
logback_console_type=$(user_numeric_options_or_default_value "$enable_logback" "$DEFAULT_OPTION" "$OPTIONS_PROMPT" "$NUMERIC_INTERVAL_ALLOWED")