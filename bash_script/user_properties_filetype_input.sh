#!/bin/bash

# PROMPT
OPTIONS_PROMPT="Select 'Properties Filetype':  1).properties   2).yml"
NUMERIC_OPTIONS_INTERVAL="1-2"


# VARIAVEIS DE SWITCHES DO MVN CLI + INPUTS
properties_filetype=$(user_numeric_selection_or_default_value "$OPTIONS_PROMPT" "$NUMERIC_OPTIONS_INTERVAL" "true")