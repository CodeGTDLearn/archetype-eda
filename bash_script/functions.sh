#!/bin/bash

execute_maven_command_line() {

  local show_basic_log=$1
  local show_debug_log=$2
  local maven_command_line_param=$3

  local show_progress_bar=$ACTIVE_PROGRESS_BAR
  local maven_output
  local debug_command_line

  local output_header="\n\n\n\n"
       output_header+="\n-------------------------------------------------------------------------------\n"
       output_header+="\n------------------------- LOG/DEBUG PROCESS - STARTING ------------------------\n"
       output_header+="\n-------------------------------------------------------------------------------\n"

  local output_footer="\n\n-------------------------------------------------------------------------------\n"
         output_footer+="\n--------------------------- LOG/DEBUG PROCESS - ENDING ------------------------\n"
         output_footer+="\n-------------------------------------------------------------------------------\n"
         output_footer+="\n\n\n\n\n"

  local output=""

  echo -e "$output_header"

# debug_log: implicitamente "POSSUI" o "BASIC_LOG"
  if [ "$show_debug_log" = "true" ]; then

    output+="$SHOW_MAVEN_DEBUG_LOG_MESSAGE"
    echo -e "\n$output\n"

    mvn_cli_switches="--batch-mode --debug"
    before_batch_switch=${maven_command_line_param%%--batch-mode*}
    after_batch_switch=${maven_command_line_param#*--batch-mode}
    debug_command_line="$before_batch_switch $mvn_cli_switches $after_batch_switch"

    maven_output=$(eval "$debug_command_line" | tee /dev/tty)

    echo -e "\n\n\n\nMVN-COMMAND PROCESSED:\n---> $debug_command_line\n"

    echo -e "$output"
    echo -e "$output_footer"
  fi

# basic_log: "NAO POSSUI" o "DEBUG_LOG" implicitamente
  if [ "$show_basic_log" = "true" ] && [ "$show_debug_log" = "false" ]; then
    #output+="-------------------------------------------------------------------------------\n\n"
    output+="\n\n$SHOW_MAVEN_BASIC_LOG_MESSAGE"
    echo -e "\n\n$output\n\n"

    maven_output=$(eval "$maven_command_line_param" | tee /dev/tty)

    echo -e "\n\n\n\nMVN-COMMAND PROCESSED:\n---> $maven_command_line_param\n"

    echo -e "$output"
    echo -e "$output_footer"
  fi

  if [ "$show_basic_log" = "false" ] && [ "$show_debug_log" = "false" ]; then

    # Progression_Bar Parameters:
    # a) Symbol types options: ▮ █ ■ ▪ * + -
    # b) ProgressBar types:   percentual_mult_layer,    percentual,    random
    maven_output=$(eval "$maven_command_line_param") &
    progression_bar $! '■' '55' "percentual_mult_layer" $show_progress_bar
    wait $!

    check_maven_build_result "$maven_output"

  fi
}

check_maven_build_result() {

  local output_to_be_checked=$1

  if echo "$output_to_be_checked" | grep -q "$ERROR_KEYWORD"; then
    echo -e "\n$BUILD_FAILURE_MESSAGE"

  elif echo "$output_to_be_checked" | grep -q "$SUCCESS_KEYWORD"; then
    echo -e "\n$BUILD_SUCCESS_MESSAGE"

  fi
}

match_input_pattern_with_regex() {

  local input_pattern=$1
  local value_to_be_matched=$2

  local input_patterns
  local return_result="false"

  case $input_pattern in

    #numero: Inteiros/decimais (ex: 123 ou 123.45)
    numero) input_patterns="^[0-9]+(\.[0-9]+)?$";;

    #texto: Qquer caracter + espaços/acentos (ex: Olá, mundo! ou ``)
    texto) input_patterns="^[a-zA-Z].*$";;

    #entity_list_attributes: Long:id, MyId:id, My_I8d:my_i9d, My_Id:my_id, Invalid:ID, notValid:id
    entity_list_attributes) input_patterns="^[A-Z][a-zA-Z0-9_]*:[a-z][a-zA-Z0-9_]*$";;

    #artifact: (https://maven.apache.org/pom.html#Maven_Coordinates)
    # - Letra maisc minusc/número/caracteres(_) sem espaços(ex: tes_123n)
    # - Segundo a documentacao do Maven deve ser o PROJECT_NAME:
    #   * "generally the name that the project is known by."
    # - descricao de Artefato no MAVEN, na tag:
    #   * <artifactId>MyArtifact</artifactId>
    artifact) input_patterns="^[a-zA-Z][a-zA-Z0-9_]+$";;

    #model/entity: Letra/número/caracteres(_.) sem espaços(ex: tes_123.domain)
    entity_name) input_patterns="^[A-Z][a-zA-Z0-9_]+$";;

    #package:
    # - Letra minuscula/número/caracteres(_.) sem espaços(ex: tes_123.domain)
    # - 'Arvores de Pastas'(Pastas separadas por ".") onde a classe(s) se encontra(m)
    #package) input_patterns="^[a-z][a-z0-9_.]+$";;

    #groupid:
    # - Letras minuscula|números|pontos|hífens (ex: com.example.meu_pacote)
    # - Company-Domain 'invertido' "WITHOUT" the 'PROJECT-NAME'/'ARTIFACT_ID'
    groupid) input_patterns="^[a-z][a-z0-9]*(\.[a-z0-9]+)+$";;

    #dependency: Letra/número/caracteres(,:._-) sem espaços(ex: org.slf4j:slf4j-api:1.7.1:runtime)
    dependency) input_patterns="^[a-zA-Z][a-zA-Z0-9,:._-]+$";;

    #path: Path de arquivo/pasta (ex: /home/arquivo.txt ou c:\home\arquivo.txt)
    path) input_patterns="^([a-zA-Z]:)?[\/\\][a-zA-Z0-9._-\/\\]+\/?$";;

    #boleana: Palavras "true"|"false" em diversos formatos (ex: true|False|TRUE)
    boleana) input_patterns="^(true|false|t|f|T|F|TRUE|FALSE|True|False|tRUE|fALSE|tRuE|fALsE)$";;

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

  if [ "$input_pattern" != "entity_list_attributes" ] &&
     [[ $value_to_be_matched =~ $input_patterns ]]; then
     return_result="true"
  fi

  if [ "$input_pattern" == "entity_list_attributes" ]; then

      # Limpa espacos e splita em array usando ',' e '\n'
      IFS=$',\n' read -r -a attribute_list <<< "${value_to_be_matched// /}"

      for attribute in "${attribute_list[@]}"; do
          if [[ $attribute =~ $input_patterns ]]; then
              return_result="true"
          else
              return_result="false"
              echo $return_result
          fi
      done
  fi

  echo $return_result
}

is_java_reserved_word() {

  local input_pattern=$1
  local word_to_be_checked=$2

  # PROBLEMA:
  #  'true' e 'false' sao reservadas, mas nao podem ser bloqueadas
  #  pois sao usadas pelo sistema, em certos 'prompts'
  #  Caso 'true' ou 'false' fossem bloqueadas, o sistema nao conseguira averigua-los

  # SOLUCAO:
  # * Onde o 'input do usuario'(pattern de input) FOR BOOLEANO (ex: 'true|false'):
  #   - 'true|false' NAO SERAO CHECADOS(essa funcao de palavras reservadas SKIPA)
  #   - pois 'true|false' nesses casos, sao os unicos inputs aceitos

  # * Onde o 'input do usuario'(pattern de input) NAO FOR BOOLEANO (ex: texto):
  #   - 'true|false' SERAO CHECADOS(essa funcao de palavras reservas NAO SKIPA)
  #   - pois 'true|false' nesses casos poderiam ser 'acidentalmente' aceitos
  if [ "$input_pattern" == "boleana" ]; then
    echo "false"
    return
  fi

  local java_reserved_words=(
    "abstract" "assert" "boolean" "break" "byte" "case" "catch" "char" "class" "const" "continue"
    "default" "do" "double" "else" "enum" "extends" "final" "finally" "float" "for" "goto"
    "if" "implements" "import" "instanceof" "int" "interface" "long" "native" "new" "null"
    "package" "private" "protected" "public" "record" "return" "sealed" "short" "static" "strictfp"
    "super" "switch" "synchronized" "this" "throw" "throws" "transient" "true" "false" "try" "var"
    "void" "volatile" "while" "yield" "non-sealed" "permits"
  )

  for palavra_reservada in "${java_reserved_words[@]}"; do
    if [ "${word_to_be_checked,,}" = "${palavra_reservada,,}" ]; then
      echo "true"
      return
    fi
  done
  echo "false"
}

is_compound_input_valid() {
  local input_value=$1
  local separator=$2

  local terms=(${input_value//$separator/ })

  for term in "${terms[@]}"; do
    if [ "$(is_java_reserved_word "$term")" = "true" ]; then
      echo "false"
      return
    fi
  done

  echo "true"
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
    # Se sim, finaliza o loop e atribui o valor default
    if [ -z "$input_value" ]; then
      input_value=$input_default_value
      break
    fi

    # Checar se o 'pattern-regex' da 'match' E checar é uma palavra reservada do Java
    # if [ "$(match_input_pattern_with_regex "$input_pattern" "$input_value")" = "true" ]; then
    if [ "$(match_input_pattern_with_regex "$input_pattern" "$input_value")" = "true" ] &&
       [ "$(is_java_reserved_word "$input_pattern" "$input_value")" = "false" ]; then
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

open_ide_manager() {

  local active=$1
  local open_ide_directly_no_option=$2
  local ide_execution_folder=$3
  local ide_execution_file=$4

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

  if [ "$open_ide_directly_no_option" = "true" ]; then
    eval "$OPEN_IDE_CMD"
    #read -p "$(echo -e "\n$FINISH_PROMPT_MESSAGE")"
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

  local artifactId_text='O artifactId[Nome do Projeto]'

  while true; do

    artifactId=$(user_input_or_default_value "artifact" "my_archetype" "$ARTIFACT_ID_PROMPT")

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
     custom_attributes_list=$(user_input_or_default_value "entity_list_attributes" "Long:id, MyId:id, My_Id:my_id" "$CUSTOM_ATTRIBUTES_LIST_PROMPT")

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