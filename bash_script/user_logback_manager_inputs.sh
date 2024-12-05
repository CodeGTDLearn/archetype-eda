#!/bin/bash

# 1) PROMPT
ENABLE_LOGBACK_PROMPT="${ITEM_MARK}Enable 'Custom Logback'"
CONSOLE_OPTIONS_1="${OPTION_MARK}1)Spring\n${OPTION_MARK}2)Tabular\n${OPTION_MARK}3)Granular\n"
CONSOLE_OPT_PROMPT="${TOPIC_MARK}'Console Style':\n${CONSOLE_OPTIONS_1}"
CONSOLE_NUM_OPT_INTERVAL="1-3"
ENABLE_SMTP_PROMPT="${TOPIC_MARK}Enable SMTP/Email Appender"
ENABLE_FILE_PROMPT="${TOPIC_MARK}Enable FILE Appender"

# 2) VARIAVEIS DE SWITCHES DO MVN CLI + INPUTS
enable_logback=$(get_user_input_or_default_value "boleana" "false" "$ENABLE_LOGBACK_PROMPT")
logback_console_type=$(get_user_numeric_selection_or_default_value "$CONSOLE_OPT_PROMPT" "$CONSOLE_NUM_OPT_INTERVAL" "$enable_logback" "1")
enable_logback_appender_smtp=$(get_user_input_or_default_value "boleana" "false" "$ENABLE_SMTP_PROMPT" "$enable_logback")
enable_logback_appender_file=$(get_user_input_or_default_value "boleana" "false" "$ENABLE_FILE_PROMPT" "$enable_logback")

# 3) ADD PROMPT IN PROGRESS_BAR
add_apostrophe_item_in_progress_bar "${ENABLE_LOGBACK_PROMPT}" "${enable_logback}"