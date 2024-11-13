#!/bin/bash

execute_maven_command_line() {

  local show_basic_log=$1
  local show_debug_log=$2
  local maven_command_line_param=$3

  echo -e "\n$PROCESS_ON_GOING\n"

  local output=""
  local maven_output
  local debug_command_line

  if [ "$show_basic_log" = true ]; then
    output="$SHOW_MAVEN_BASIC_LOG_MESSAGE"
  fi

  if [ "$show_debug_log" = true ]; then
    output+="\n\n$SHOW_MAVEN_DEBUG_LOG_MESSAGE"

    mvn_cli_switches="--batch-mode --debug"
    before_batch_switch=${maven_command_line_param%%--batch-mode*}
    after_batch_switch=${maven_command_line_param#*--batch-mode}

    debug_command_line="$before_batch_switch $mvn_cli_switches $after_batch_switch"

    echo -e "$SHOW_MAVEN_COMMANDO_LINE_LOG_MESSAGE \n\n$debug_command_line \n"
  fi

  if [ "$show_basic_log" = true ] || [ "$show_debug_log" = true ]; then
    echo -e "\n$output\n"
    maven_output=$(eval "$debug_command_line ")
    echo "$debug_command_line "
  else
    maven_output=$(eval "$maven_command_line_param")
  fi

#  MONITORA O RESULTA DO BUILD DO MAVEN
  if echo "$maven_output" | grep -q "$ERROR_KEYWORD"; then
     echo -e "\n$BUILD_FAILURE_MESSAGE"
  elif echo "$maven_output" | grep -q "$SUCESS_KEYWORD"; then
     echo -e "\n$BUILD_SUCCESS_MESSAGE"
  fi

}

match_input_with_pattern() {
  local input_pattern=$1
  local value_to_be_matched=$2

  local input_patterns

  case $input_pattern in

    #numero: Inteiros/decimais (ex: 123 ou 123.45)
    numero) input_patterns="^[0-9]+(\.[0-9]+)?$";;

    #texto: Qquer caracter + espaços/acentos (ex: Olá, mundo! ou ``)
    texto) input_patterns="^[a-zA-Z].*$";;

    #artifact_model: Letra/número/caracteres(_-) sem espaços(ex: teste_123 / nome-dominio)
    artifact_model) input_patterns="^[a-zA-Z][a-zA-Z0-9_-]+$";;

    #dependency: Letra/número/caracteres(,:._-) sem espaços(ex: org.slf4j:slf4j-api:1.7.1:runtime)
    dependency) input_patterns="^[a-zA-Z][a-zA-Z0-9,:._-]+$";;

    #groupid: Sequência de letras|números|pontos|underscore|hífens (ex: com.example.meu_pacote)
    groupid) input_patterns="^[a-zA-Z][a-zA-Z0-9]*(\.[a-zA-Z0-9]+)+$";;

    #path: Path de arquivo/pasta (ex: /home/arquivo.txt ou c:\home\arquivo.txt)
    path) input_patterns="^([a-zA-Z]:)?[\/\\][a-zA-Z0-9._-\/\\]+\/?$";;

    #boleana: Palavras "true"|"false" em diversos formatos (ex: true|False|TRUE)
    boleana) input_patterns="^(true|false|t|f|T|F|TRUE|FALSE|True|False|tRUE|fALSE)$";;

    #data: Data no formato "dd/mm/aaaa" (ex: 12/03/2023 ou 1/1/23)
    data) input_patterns="^[0-9]{1,2}\/[0-9]{1,2}\/[0-9]{2,4}$";;

    #hora: Hora no formato "HH" (ex: 12, 23, 07)
    hora) input_patterns="^(2[0-3]|[01][0-9])$";;

    #minuto: Minuto no formato "MM" (ex: 59, 30, 01)
    minuto) input_patterns="^[0-5][0-9]$";;

    #hora_minuto: Hora e minuto no formato "HH:MM" (ex: 12:30, 07:01)
    hora_minuto) input_patterns="^(2[0-3]|[01][0-9]):[0-5][0-9]$";;

    #hora_minuto_segundo: Hora, minuto e segundo no formato "HH:MM:SS" (ex: 12:30:45)
    hora_minuto_segundo) input_patterns="^(2[0-3]|[01][0-9]):[0-5][0-9]:[0-5][0-9]$";;

  esac

  if [[ $value_to_be_matched =~ $input_patterns ]]; then
    echo "true"
  else
    echo "false"
  fi
}

user_input_or_return_for_default_value() {

  local input_pattern=$1
  local input_default_value=$2
  local input_prompt=$3

  local input_value

  while true; do

    read -r -p "$input_prompt (ex: $input_default_value): " input_value

    # Checa se o valor é vazio ('enter' foi pressionado),
    # se sim, finaliza o loop e atribui o valor default
    if [ -z "$input_value" ]; then
      input_value=$input_default_value
      break
    fi

    if [ "$(match_input_with_pattern "$input_pattern" "$input_value")" = "true" ]; then
      break
    fi

  done

  # Checa input_value p/ 'true/TRUE/True/T/t' ou 'false/FALSE/False/F/f' e converte p/ lowerCase
  case ${input_value,,} in
    true|t) echo "true";;
    false|f) echo "false";;
    *) echo "$input_value";;
  esac
}

converte_pwd_para_windows() {

  local pwd=$(pwd)
  # echo "Initial pwd: $pwd"

  local drive_letter=${pwd:1:1}
  # echo "Drive letter: $drive_letter"

  local windows_path=${pwd/#\/$drive_letter/${drive_letter}:}
  windows_path=${windows_path//\//\\}
  # echo "Windows_path converted: $windows_path"

  echo "$windows_path"
}

open_ide_final_option_input() {

  local user_options_text='\nDo you want to:\n\n 1. Open project in IDE\n 2. Exit\n'
  local user_options_prompt='Choose an option (1/2): '
  local invalid_user_option='Invalid option. Please try again.'
  local ide_exec='C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2024.3\bin\idea64.exe'

  local my_path=$(converte_pwd_para_windows)
  # echo "my_path $my_path"

  local OPEN_IDE_CMD

  if [ -f "${ide_exec}" ]; then
      # echo "ide_Exec_path ${ide_exec}"
      # read -p "Pressione Enter para continuar..."

      OPEN_IDE_CMD="idea64 \"$my_path\\$artifactId\""
      # echo "Linha de execucao OPEN_IDE_CMD: $OPEN_IDE_CMD"

      #read -p "Pressione Enter para continuar..."
  else
      echo -e "\n\nERRO: ${ide_exec} not found.\n"
      read -p "Pressione Enter para continuar..."
      exit 1
  fi

  while true; do
    echo -e "$user_options_text"
    read -p "$user_options_prompt" option

    case $option in
       1) eval $OPEN_IDE_CMD;;
#       1) $OPEN_IDE_CMD;;
#       1) idea64 \"$my_path\\$artifactId\";;
       2) break ;;
       *) echo "$invalid_user_option" ;;
    esac
  done

  read -p "$(echo -e "\n$FINISH_PROMPT")"
}

artifactId_input() {

  local artifactId_text='O artifactId'

  while true; do

    artifactId=$(user_input_or_return_for_default_value "artifact_model" "my-archetype" "$ARTIFACT_ID_PROMPT")

    if [ -d "$artifactId" ]; then
       echo -e "\n$artifactId_text \"$artifactId\" $ARTIFACT_ID_EXISTS_WARNING \n\n$FOLDER_ALREADY_EXISTENT_MESSAGE\n"
       ls -l
       echo
    else
       break
    fi

  done
}

custom_attributes_list_input() {

  if [[ "${enable_custom_attributes,,}" =~ ^(true|t|T|TRUE|True)$ ]]; then
     custom_attributes_list=$(user_input_or_return_for_default_value "texto" "Long:Id, String:name" "$CUSTOM_ATTRIBUTES_LIST_PROMPT")

  else
     custom_attributes_list="false"

  fi
}

custom_dependencies_addition_input() {

  if [[ "${enable_dependencies_addition,,}" =~ ^(true|t|T|TRUE|True)$ ]]; then
     extra_dependencies=$(user_input_or_return_for_default_value "dependency" "org.slf4j:slf4j-api:1.7.1:runtime" "$EXTRA_DEPENDENCIES_PROMPT")
  else
     extra_dependencies="false"

  fi
}