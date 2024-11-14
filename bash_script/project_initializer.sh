#!/bin/bash

script_injection() {

  local FOLDER="$1"
  local FILE="$2"

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
}

project_folder_viability_checking() {

  PRESS_KEY_MESSAGE="Pressione qualquer tecla para continuar..."
  PROJECT_EXISTS_MESSAGE="Project creation FOLDER not allowed. Try elsewhere.\n"

  CURRENT_DIR=$(pwd)

  if [ -d "$CURRENT_DIR/src" ] ||
     [ -f "$CURRENT_DIR/pom.xml" ] ||
     [ -n "$(ls -A | grep -v '/')" ]; then
    echo -e "$PROJECT_EXISTS_MESSAGE"
    read -p "$PRESS_KEY_MESSAGE" -n 1 -s
    exit 1
  fi
}