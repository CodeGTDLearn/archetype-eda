#!/bin/bash

# PROMPT
ENABLE_LOGBACK_PROMPT="Enable 'Custom Logback'"
CONSOLE_OPT_PROMPT=" •Select 'Console Style':\n  ▪1)Spring\n  ▪2)Tabular\n  ▪3)Granular\n  ▪Default"
CONSOLE_NUMERIC_OPTIONS_INTERVAL="1-3"
ENABLE_SMTP_PROMPT=" •Enable SMTP/Email Appender"
ENABLE_FILE_PROMPT=" •Enable FILE Appender"

# VARIAVEIS DE SWITCHES DO MVN CLI + INPUTS
enable_logback=$(user_input_or_default_value "boleana" "false" "$ENABLE_LOGBACK_PROMPT")
logback_console_type=$(user_numeric_selection_or_default_value "$CONSOLE_OPT_PROMPT" "$CONSOLE_NUMERIC_OPTIONS_INTERVAL" "$enable_logback")
enable_logback_appender_smtp=$(user_input_or_default_value "boleana" "false" "$ENABLE_SMTP_PROMPT" "$enable_logback")
enable_logback_appender_file=$(user_input_or_default_value "boleana" "false" "$ENABLE_FILE_PROMPT" "$enable_logback")