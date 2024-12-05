#!/bin/bash

project_folder_viability_checking() {

# ATENCAO!!!
# ESSA CONSTANTES DEVE FICAR NESTE LOCAL
# POIS ELAS SAO USADAS 'ANTES' DA INJECAO DAS 'GENERAL_MESSAGES_CONSTANTS.SH'
PRESS_KEY_MESSAGE="Pressione qualquer tecla para continuar..."
PROJECT_FOLDER_NOT_ALLOWED_MESSAGE="Project creation FOLDER not allowed. Try elsewhere.\n"

CURRENT_DIR=$(pwd)

  if [ -d "$CURRENT_DIR/src" ] ||
     [ -f "$CURRENT_DIR/pom.xml" ] ||
     [ -n "$(ls -A | grep -v '/')" ]; then
    echo -e "$PROJECT_FOLDER_NOT_ALLOWED_MESSAGE"
    read -p "$PRESS_KEY_MESSAGE" -n 1 -s
    exit 1
  fi
}