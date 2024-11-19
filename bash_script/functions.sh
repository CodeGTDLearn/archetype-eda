#!/bin/bash

progression_bar() {

  local pid=$1
  local symbol=$2
  local size_bar=$3
  local progression_bar_type=$4
  local active=$5

  echo -e "\n$PROCESS_ON_GOING_MESSAGE\n"

  printf "\n"

  _percentual_mult_layer_progression_bar() {

    local model_label="Model:     "
    local view_label="View:      "
    local controller_label="Controller:"
    local delay=0.01

    # Esgotar as barras de progresso uma após a outra
    for component in model view controller; do
      for ((i=1; i<=size_bar; i++)); do
        # Atualizar a barra de progresso
        case $component in
          model) printf "\r${model_label} %-${size_bar}s%4d%%" "$(printf "%*s" $i | tr ' ' "$symbol")" $((i * 100 / size_bar));;
          view) printf "\r${view_label} %-${size_bar}s%4d%%" "$(printf "%*s" $i | tr ' ' "$symbol")" $((i * 100 / size_bar));;
          controller) printf "\r${controller_label} %-${size_bar}s%4d%%" "$(printf "%*s" $i | tr ' ' "$symbol")" $((i * 100 / size_bar));;
        esac
      done
      printf "\n"
    done
  }

  _percentual_progression_bar() {
    # código para quando $active == true e $percent_progression == true
    progress=0
    while [ -d /proc/$pid ]; do
      #printf "\r[%-${size_bar}s]%3d%%" "$(printf "%*s" $progress | tr ' ' "$symbol")" $((progress * 100 / size_bar))
      printf "\r%-${size_bar}s%3d%%" "$(printf "%*s" $progress | tr ' ' "$symbol")" $((progress * 100 / size_bar))
      printf "\033[0G"  # move o cursor para a posição 0 da linha atual
      sleep 0.1

      ((progress++))
      if [ $progress -gt $size_bar ]; then
        progress=$size_bar
      fi

    done

       #printf "\r[%-${size_bar}s]100%%" "$(printf "%*s" "${size_bar}" | tr ' ' "$symbol")"
       printf "\r%-${size_bar}s 100%%" "$(printf "%*s" "${size_bar}" | tr ' ' "$symbol")"
  }

  _random_progression_bar() {

      while [ -d /proc/$pid ]; do
        #printf "\r[%-${size_bar}s]" "$(printf "%*s" $(( RANDOM % size_bar )) | tr ' ' "$symbol")"
        printf "\r%-${size_bar}s" "$(printf "%*s" $(( RANDOM % size_bar )) | tr ' ' "$symbol")"
        printf "\033[0G"
        sleep 0.1
      done
  }

  if $active == true; then
    case $progression_bar_type in
      "percentual_mult_layer") _percentual_mult_layer_progression_bar ;;
      "percentual") _percentual_progression_bar ;;
      "random") _random_progression_bar ;;
    esac
  fi
}

execute_maven_command_line() {

  local show_basic_log=$1
  local show_debug_log=$2
  local maven_command_line_param=$3

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
    maven_output=$(eval "$debug_command_line ") &
    progression_bar $! '■' '55' "percentual" true
    wait $!
    # Symbol types options: ▮ █ ■ ▪ * + -

    echo "$debug_command_line "
  else
    maven_output=$(eval "$maven_command_line_param") &
    progression_bar $! '■' '55' "percentual" true
    wait $!
  fi

#  MONITORA O RESULTA DO BUILD DO MAVEN
  if echo "$maven_output" | grep -q "$ERROR_KEYWORD"; then
     echo -e "\n$BUILD_FAILURE_MESSAGE"

  elif echo "$maven_output" | grep -q "$SUCESS_KEYWORD"; then
     echo -e "\n$BUILD_SUCCESS_MESSAGE"
  fi

}

match_input_pattern() {
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

user_input_or_default_value() {

  local input_pattern=$1
  local input_default_value=$2
  local input_prompt=$3

  local input_value
  local pattern_not_matched_message="$INVALID_INPUT_MESSAGE"

  while true; do

    read -r -p "$input_prompt (ex: $input_default_value): " input_value

    # Checa se o valor é vazio ('enter' foi pressionado),
    # se sim, finaliza o loop e atribui o valor default
    if [ -z "$input_value" ]; then
      input_value=$input_default_value
      break
    fi

    if [ "$(match_input_pattern "$input_pattern" "$input_value")" = "true" ]; then
      break
    else
      echo -e "$pattern_not_matched_message\n" >&2
      sync
    fi

  done

  # Checa input_value p/ 'true/TRUE/True/T/t' ou
  # 'false/FALSE/False/F/f' e
  # converte p/ lowerCase
  case ${input_value,,} in
    true|t) echo "true";;
    false|f) echo "false";;
    *) echo "$input_value";;
  esac
}

os_path_provider() {

  local pwd=$(pwd)
   #echo -e "DEBUG LINE - Initial pwd $pwd"

  local converted_path

  local first_pwd_character=${pwd:0:1}
   #echo -e "DEBUG LINE - first_pwd_character $first_pwd_character\n"

  local second_pwd_character=${pwd:1:1}
   #echo -e "DEBUG LINE - second_pwd_character $second_pwd_character\n"

  # Detect Windows - checking a Letter as PATH begin
  # ex: c:\WORKBASE\DEV\bash_script
  # convert to Linux-Standard Path
  if [[ "$first_pwd_character" =~ [a-zA-Z] ]]; then
    converted_path=${pwd/#\//$drive_letter:\\}
    converted_path=${converted_path//\//\\}
    #echo -e "DEBUG LINE - converted_path Linux $converted_path\n"
  fi

  # Detect Linux - checking a '/' as PATH begin
  # ex: /c/WORKBASE/DEV/bash_script
  # convert to Windows-Standard Path
  if [ "$first_pwd_character" == "/" ]; then
    converted_path=${pwd/#\/$second_pwd_character/${second_pwd_character}:}
    converted_path=${converted_path//\//\\}
    #echo -e "DEBUG LINE - converted_path Windows $converted_path\n"
  fi

  echo "$converted_path"
}

open_ide_choice_input() {

  local active=$1
  local ide_execution_folder=$2
  local ide_execution_file=$3

  local ide_full_path=$ide_execution_folder$ide_execution_file

#  echo -e "DEBUG LINE - IDE_EXEC_FILE_FOLDER $ide_execution_folder\n"
#  echo -e "DEBUG LINE - IDE_EXEC_FILE $ide_execution_file\n"
#  echo -e "DEBUG LINE - IDE_FULL_PATH $ide_full_path\n"

  if [ "$active" = "false" ]; then
    read -p "$(echo -e "\n$FINISH_PROMPT_MESSAGE")"
    exit 1
  fi

  local options_question='\n\nDo you want to:'
  local options_alternatives=' 1. Open project in IDE\n 2. Exit\n'
  local options_prompt='Choose an option (1/2): '
  local invalid_option_message='Invalid option. Please try again.'

  local current_path=$(os_path_provider)
#  echo -e "DEBUG LINE - current_path $current_path\n"

  local OPEN_IDE_CMD

  if [ -f "$ide_full_path" ]; then
#       echo -e "DEBUG LINE - ide_Exec_path+file ${ide_execution_folder}${ide_execution_file}\n"
      # read -p "Pressione Enter para continuar..."

      OPEN_IDE_CMD="$ide_execution_file \"$current_path\\$artifactId\" ; exit"
#      echo -e "DEBUG LINE - OPEN_IDE_CMD $OPEN_IDE_CMD\n"
      #read -p "Pressione Enter para continuar..."

  else
      echo -e "\n\nERROR in ${ide_execution_folder}${ide_execution_file}, \nplease check it out. IDE execution aborted
      .\n"
      read -p "$FINISH_PROMPT_MESSAGE"
      exit 1
  fi

  while true; do
    echo -e "$options_question\n\n$options_alternatives"
    read -p "$options_prompt" option

    case $option in
       1) eval "$OPEN_IDE_CMD";;
       2) break ;;
       *) echo "$invalid_option_message" ;;
    esac
  done

  # read -p "$(echo -e "\n$FINISH_PROMPT_MESSAGE")"
}

artifactId_input() {

  local artifactId_text='O artifactId'

  while true; do

    artifactId=$(user_input_or_default_value "artifact_model" "my-archetype" "$ARTIFACT_ID_PROMPT")

    if [ -d "$artifactId" ]; then
       echo -e "\n$artifactId_text \"$artifactId\" $ARTIFACT_ID_EXISTS_MESSAGE \n\n$FOLDER_ALREADY_EXISTENT_MESSAGE\n"
       ls -l
       echo
    else
       break
    fi

  done
}

custom_attributes_list_input() {

  if [[ "${enable_custom_attributes,,}" =~ ^(true|t|T|TRUE|True)$ ]]; then
     custom_attributes_list=$(user_input_or_default_value "texto" "Long:Id, String:name" "$CUSTOM_ATTRIBUTES_LIST_PROMPT")

  else
     custom_attributes_list="false"

  fi
}

custom_dependencies_addition_input() {

  if [[ "${enable_dependencies_addition,,}" =~ ^(true|t|T|TRUE|True)$ ]]; then
     extra_dependencies=$(user_input_or_default_value "dependency" "org.slf4j:slf4j-api:1.7.1:runtime" "$EXTRA_DEPENDENCIES_PROMPT")
  else
     extra_dependencies="false"

  fi
}