#!/bin/bash

# PROMPT
ENABLE_LOGBACK_PROMPT="Habilitar custom Logback"
ENABLE_LOGSTASH_PROMPT="Habilitar Logstash Appender? (Default: false)"
ENABLE_SMTP_PROMPT="Habilitar SMTP/Email Appender? (Default: false)"
ENABLE_FILE_PROMPT="Habilitar SMTP/Email Appender? (Default: false)"
OPTIONS_PROMPT="\nSelecione o 'Logback Console Style':\n1)Default; 2)Tabular; 3)Granular"
NUMERIC_INTERVAL_ALLOWED="1-3"

# VARIAVEIS DE SWITCHES DO MVN CLI + INPUTS
enable_logback=$(user_input_or_default_value "boleana" "false" "$ENABLE_LOGBACK_PROMPT")
logback_console_type=$(user_numeric_options_or_default_value "$enable_logback" "$OPTIONS_PROMPT" "$NUMERIC_INTERVAL_ALLOWED")
enable_logback_appender_logstash=$(user_input_or_default_value "boleana" "false" "$ENABLE_LOGSTASH_PROMPT")
enable_logback_appender_smtp=$(user_input_or_default_value "boleana" "false" "$ENABLE_SMTP_PROMPT")
enable_logback_appender_file=$(user_input_or_default_value "boleana" "false" "$ENABLE_FILE_PROMPT")