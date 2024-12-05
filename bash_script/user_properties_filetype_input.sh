#!/bin/bash

# 1) PROMPT
FILETYPE_OPTIONS="${TOPIC_MARK}1)properties\n${TOPIC_MARK}2)yml\n"
FTYPE_OPTS_PROMPT="${ITEM_MARK}'Properties Filetype':\n${FILETYPE_OPTIONS}"
FTYPE_NUM_OPT_INTERVAL="1-2"

# 2) VARIAVEIS DE SWITCHES DO MVN CLI + INPUTS
properties_filetype=$(get_user_numeric_selection_or_default_value "$FTYPE_OPTS_PROMPT" "$FTYPE_NUM_OPT_INTERVAL" "true" "1")

# 3) ADD PROMPT IN PROGRESS_BAR
add_apostrophe_item_in_progress_bar "${FTYPE_OPTS_PROMPT}" "true"