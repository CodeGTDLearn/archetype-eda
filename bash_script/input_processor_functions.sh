#!/bin/bash

match_input_pattern_with_regex() {

  local matching_pattern=$1
  local value_to_be_matched=$2

  local input_patterns
  local return_result="false"

  case $matching_pattern in

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

    #dependency: Letra/número/caracteres(,:._-) sem espaços
    # (ex: org.aa:aa-api:1.7:test, org.bb:bb-api:1.8:runtime)
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

    #url do git: URL do git (ex: https://github.com/username/repo.git)
    git_url) input_patterns="^(https?:\/\/)?(www\.)?[-a-zA-Z0-9@:%._\+~#=]{1,256}\.[a-zA-Z0-9()]{1,6}\b([-a-zA-Z0-9()@:%_\+.~#?&//=]*)\.git$";;

  esac

  if [ "$matching_pattern" != "entity_list_attributes" ] &&
     [[ $value_to_be_matched =~ $input_patterns ]]; then
     return_result="true"
  fi

  if [ "$matching_pattern" == "entity_list_attributes" ]; then

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

  local matching_pattern=$1
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
  if [ "$matching_pattern" == "boleana" ]; then
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

get_user_input_or_default_value() {

  local matching_pattern=$1
  local defaultValue_onEnterKey_press=$2
  local input_prompt=$3
  local proceed_with_function=${4:-true}

  local input_value
  local pattern_not_matched_message="$INVALID_INPUT_MESSAGE"

  if [ "$proceed_with_function" = "false" ]; then
    echo "false"
    return
  fi

  while true; do

    local prompt="$input_prompt [$defaultValue_onEnterKey_press]: "
    local highlighted_prompt=$(colorize_delimited_text "$prompt" false "green")
    read -r -p "$highlighted_prompt" input_value

    # Checa se o valor é vazio (vazio significa que o 'enter' foi pressionado),
    # Se sim, finaliza o loop e atribui o valor default
    # valor default(presumidamente no pattern correto + Nao sendo palavra reservada)
    if [ -z "$input_value" ]; then
      input_value=$defaultValue_onEnterKey_press
      break
    fi

    # Checar se:
    # - o 'pattern-regex' da 'match' E
    # - se o 'input_value' é uma palavra reservada do Java
    if [ "$(match_input_pattern_with_regex "$matching_pattern" "$input_value")" = "true" ] &&
       [ "$(is_java_reserved_word "$matching_pattern" "$input_value")" = "false" ]; then
      break
    else
      echo -e "$pattern_not_matched_message\n" >&2
      sync
    fi
  done

  # Checa input_value p/:
  # - 'true/TRUE/True/T/t' ou 'false/FALSE/False/F/f' e
  # - converte p/ lowerCase
  case ${input_value,,} in
    true|t) echo "true";;
    false|f) echo "false";;
    *) echo "$input_value";;
  esac
}

get_user_numeric_selection_or_default_value() {

  local numeric_option_prompt=$1
  local numeric_option_interval_from_min_to_max=$2
  local proceed_with_function=${3:-true}
  local defaultValue_onEnterKey_press=$4

  local matching_pattern="numero"
  local chosen_option
  local is_numeric_option_allowed
  local intervalMin
  local intervalMax
  local FUNCTION_INTERNAL_DEBUG="false"

  intervalMin=$(echo "$numeric_option_interval_from_min_to_max" | cut -d'-' -f1)
  intervalMax=$(echo "$numeric_option_interval_from_min_to_max" | cut -d'-' -f2)

  if [ "$proceed_with_function" = "false" ]; then
    echo "$defaultValue_onEnterKey_press"
    exit 1
  fi

  if [[ "${FUNCTION_INTERNAL_DEBUG,,}" = "true" ]]; then
    echo -e "\n\n\n-----> FUNCTION INTERNAL DEBUG <-----" >&2
    echo -e "\nFunction: user_numeric_options_or_default_value" >&2
    echo -e "IntervalMin: $intervalMin" >&2
    echo -e "IntervalMax: $intervalMax" >&2
    echo -e "Active: $proceed_with_function" >&2
    echo -e "\n-------------------------------------\n" >&2
  fi

  while true; do

    local prompt="${numeric_option_prompt}"
    local highlighted_prompt=$(colorize_delimited_text "$prompt" false "magenta")
    read -r -p "$highlighted_prompt" chosen_option

    # Checa se o valor é vazio ('enter' foi pressionado),
    # Se sim, finaliza o loop e atribui o valor default
    if [ -z "$chosen_option" ]; then
      chosen_option=$defaultValue_onEnterKey_press
    fi

    if (( chosen_option >= intervalMin && chosen_option <= intervalMax )); then
      is_numeric_option_allowed="true"
    else
      echo -e "\n$$INVALID_INPUT_OPTIONS_INTERVAL_MESSAGE"
    fi

    # Checar se o 'pattern-regex' da 'match' E checar é uma palavra reservada do Java
    if [ "$is_numeric_option_allowed" = "true" ] &&
       [ "$(match_input_pattern_with_regex "$matching_pattern" "$chosen_option")" = "true" ] &&
       [ "$(is_java_reserved_word "$matching_pattern" "$chosen_option")" = "false" ]; then
         echo "$chosen_option"
         break
    else
         echo -e "$INVALID_INPUT_MESSAGE\n" >&2
         sync
    fi
  done

}