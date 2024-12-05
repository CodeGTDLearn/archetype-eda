#!/bin/bash

inject_script() {

  local FOLDER="$1"
  local FILE="$2"
  local SHOW_DEBUG_LOG="$3"

  local ENABLE_SPACER_AFTER_FUNCTION="true"

  local divider="\n--------------------------------------------------------------------->\n"

  if [ "$SHOW_DEBUG_LOG" = "true" ]; then
    echo -e "$divider"
    echo -e "\n-----> INJECTING: $FILE ------ START\n"
  fi

  local FOLDER_NOT_FOUND_MESSAGE="Script Folder \'$FOLDER\' NOT FOUND.\n"
  local SCRIPT_NOT_FOUND_MESSAGE="Script \'$FILE\' NOT FOUND in $FOLDER.\n"

  if [ ! -d "$FOLDER" ]; then
    echo -e "$FOLDER_NOT_FOUND_MESSAGE"
    return 1
  fi

  if [ ! -f "$FOLDER/$FILE" ]; then
    echo -e "$SCRIPT_NOT_FOUND_MESSAGE"
    return 1
  fi

  source "${FOLDER}\\${FILE}"

  if [ "$ENABLE_SPACER_AFTER_FUNCTION" = "true" ]; then
    echo -e "\n"
  fi

    if [ "$SHOW_DEBUG_LOG" = "true" ]; then
      echo -e "\n-----> INJECTING: $FILE ------ END\n"
      echo -e "$divider"
    fi
}