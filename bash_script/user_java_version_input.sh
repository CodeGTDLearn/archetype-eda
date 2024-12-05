#!/bin/bash

# 1) PROMPT
JAVA_VERSION_OPTS="${TOPIC_MARK}1)'17'\n${TOPIC_MARK}2)'21'\n"
JAVA_VERSION_PROMPT="${ITEM_MARK}Java Version:\n${JAVA_VERSION_OPTS}"
JAVA_VERSION_NUM_OPT_INTERVAL="1-2"


# 2) VARIAVEIS DE SWITCHES DO MVN CLI + INPUTS
java_choice=$(get_user_numeric_selection_or_default_value "$JAVA_VERSION_PROMPT" "$JAVA_VERSION_NUM_OPT_INTERVAL" "true" "1")
case $java_choice in
  1) java_version=17 ;;
  2) java_version=21 ;;
esac