#!/bin/bash

provide_os_path() {

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

open_ide_manager() {

  local proceed_with_function=$1
  local open_ide_directly_no_option=$2
  local ide_execution_folder=$3
  local ide_execution_file=$4
  local show_basic_log=$5

  local ide_full_path=$ide_execution_folder$ide_execution_file

  local divider="\n--------------------------------------------------------------------->\n"

  if [ "$show_basic_log" = "true" ]; then
    echo -e "$divider"
    echo -e "\n-----> EXECUTING: 'open_ide_manager function' - START\n"
    echo -e "DEBUG - IDE_EXEC_FILE_FOLDER: $ide_execution_folder"
    echo -e "DEBUG - IDE_EXEC_FILE: $ide_execution_file"
    echo -e "DEBUG - IDE_FULL_PATH: $ide_full_path \n"
  fi


  if [ "$proceed_with_function" = "false" ]; then
    read -p "$(echo -e "\n$FINISH_PROMPT_MESSAGE")"
    exit 1
  fi

  local options_question='\n\nDo you want to:'
  local options_alternatives=' 1. Open IDE\n 2. Exit\n'
  local options_prompt='Choose an option (1/2): '
  local invalid_option_message='Invalid option. Please try again.'

  local current_path=$(provide_os_path)
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

  if [ "$open_ide_directly_no_option" = "true" ]; then
    eval "$OPEN_IDE_CMD"
    # read -p "$(echo -e "\n$FINISH_PROMPT_MESSAGE")"
    exit 1
  fi

  if [ "$show_basic_log" = "true" ]; then
    echo -e "\n-----> EXECUTING: 'open_ide_manager function' - END\n"
    echo -e "$divider"
  fi

  while true; do
    echo -e "$options_question\n\n$options_alternatives"
    read -p "$options_prompt" option

    case $option in
       1) eval "$OPEN_IDE_CMD";;
       2) exit 0 ;;
       *) echo "$invalid_option_message" ;;
    esac
  done

  # read -p "$(echo -e "\n$FINISH_PROMPT_MESSAGE")"
}

colorize_delimited_text() {

    # Parameters:
    # $1 - Input string
    # $2 - Use parentheses (true) or brackets (false)
    # $3 - Color name
    local input_string="$1"
    local apply_color_in_sequence_marker="$2"
    local color_name_sequence_marker="${3:-green}"
    local color_name_default="${4:-cyan}"

    local COLOR_DEFAULT
    local COLOR_SEQUENCE_MARKER
    local result

    # Define colors with \001 and \002 to work with readline
    RED=$'\001\033[0;31m\002'
    GREEN=$'\001\033[0;32m\002'
    YELLOW=$'\001\033[0;33m\002'
    MAGENTA=$'\001\033[0;35m\002'
    CYAN=$'\001\033[0;36m\002'
    RESET=$'\001\033[0m\002'

    case "${color_name_default,,}" in
        "red") COLOR_DEFAULT=$RED ;;
        "green") COLOR_DEFAULT=$GREEN ;;
        "yellow") COLOR_DEFAULT=$YELLOW ;;
        "magenta") COLOR_DEFAULT=$MAGENTA ;;
        "cyan") COLOR_DEFAULT=$CYAN ;;
        *) COLOR_DEFAULT=$CYAN ;;
    esac

    case "${color_name_sequence_marker,,}" in
        "red") COLOR_SEQUENCE_MARKER=$RED ;;
        "green") COLOR_SEQUENCE_MARKER=$GREEN ;;
        "yellow") COLOR_SEQUENCE_MARKER=$YELLOW ;;
        "magenta") COLOR_SEQUENCE_MARKER=$MAGENTA ;;
        "cyan") COLOR_SEQUENCE_MARKER=$CYAN ;;
        *) COLOR_SEQUENCE_MARKER=$CYAN ;;
    esac

    result=$(echo -e "$input_string" | sed "s/\[\([^]]*\)\]/$COLOR_DEFAULT\0$RESET/g")

    local colorize_seq_marker_limit=1
    if [ "$apply_color_in_sequence_marker" = true ]; then
        result="${COLOR_SEQUENCE_MARKER}${result:0:${colorize_seq_marker_limit}}${RESET}${result:1}"
    fi

    printf "%s" "$result"
}