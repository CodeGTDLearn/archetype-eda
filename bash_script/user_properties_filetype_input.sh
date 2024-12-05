#!/bin/bash

# PROMPT
FILETYPE_OPTIONS_PROMPT="Select 'Properties Filetype':\n •1).properties\n •2).yml\n •Default"
FILETYPE_NUMERIC_OPTIONS_INTERVAL="1-2"


# VARIAVEIS DE SWITCHES DO MVN CLI + INPUTS
properties_filetype=$(user_numeric_selection_or_default_value "$FILETYPE_OPTIONS_PROMPT" "$FILETYPE_NUMERIC_OPTIONS_INTERVAL" "true")