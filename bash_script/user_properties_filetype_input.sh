#!/bin/bash

# PROMPT
FILETYPE_OPTIONS="${TOPIC_MARK}1)properties\n${TOPIC_MARK}2)yml\n"
FTYPE_OPTS_PROMPT="${ITEM_MARK}Properties 'Filetype'[1]:\n${FILETYPE_OPTIONS}"
FTYPE_NUM_OPT_INTERVAL="1-2"

# VARIAVEIS DE SWITCHES DO MVN CLI + INPUTS
properties_filetype=$(user_numeric_selection_or_default_value "$FTYPE_OPTS_PROMPT" "$FTYPE_NUM_OPT_INTERVAL" "true")

# ADD PROMPT IN PROGRESS_BAR
add_item_in_progress_bar "${FTYPE_OPTS_PROMPT}" "true"