#!/bin/bash

# PROMPT
ENABLE_API_CONTROL_PANEL_PROMPT="${ITEM_MARK}Enable 'API Control Panel'"
ENABLE_CPANEL_ELK_OPTION_PROMPT="${TOPIC_MARK}Enable Elk(Elastic+Logstash+Kibana)"
ENABLE_ELK_OPTION="false"

# VARIAVEIS DE SWITCHES DO MVN CLI + INPUTS
enable_api_cpanel=$(user_input_or_default_value "boleana" "false" "$ENABLE_API_CONTROL_PANEL_PROMPT")

# a OPCAO DE CPANEL 'ELK', SO EXISTE SE HOUVER 'CPANEL HABILITADO'+'LOGBACK HABILITADO'
if [[ "$enable_logback" = "true" && "$enable_api_cpanel" = "true" ]]; then ENABLE_ELK_OPTION="true"; fi
enable_api_cpanel_elk_log_system=$(user_input_or_default_value "boleana" "false" "$ENABLE_CPANEL_ELK_OPTION_PROMPT" "$ENABLE_ELK_OPTION")