#!/bin/bash

# A) EXIBICAO DE LOGS: Exibe logs do 'Maven' e do 'Archetype Pos Processor'
show_maven_basic_log=false
show_maven_debug_log=false
show_archetype_pos_processor_log=false


# B) DEFINICAO DE CONSTANTES
# B.1) Keywords RELATIVAS/RASTREADAS do Maven
ERROR_KEYWORD="BUILD FAILURE"
SUCESS_KEYWORD="BUILD SUCCESS"


# B.2) Configurar "CONFORME" o Archetype "INSTALADO" no 'REPO-LOCAL DO MAVEN'
# 'REPO-LOCAL DO MAVEN': ~\.m2\repository\archetype-catalog.xml
ARCHETYPE_GROUP_ID="com.eda-archetype"
ARCHETYPE_ARTIFACT_ID="eda-archetype"
ARCHETYPE_VERSION="0.0.1-SNAPSHOT"

# B.3) CONSTANTES relativas aos Prompts "do Script"
#CONFIRMATION_PROMPT="Deseja continuar? (s/n): "
#OPERATION_CANCELLED_MESSAGE="Operação cancelada pelo usuário."
#ABSENCE_OF_ANSWER_WARNING="Insira um valor válido."
#FINISH_PROMPT="Pressione Enter para fechar a tela..."
BUILD_FAILURE_MESSAGE="Ocorreu um erro. Veja o log para mais detalhes."
BUILD_SUCCESS_MESSAGE="Solicitação processada com sucesso."
ARTIFACT_ID_EXISTS_WARNING="já existe. Escolha outro nome."
PROCESS_ON_GOING="Processando a solicitacao (Maven)...................."
SHOW_MAVEN_BASIC_LOG_MESSAGE="Showing Maven-BASIC-Output ...................."
SHOW_MAVEN_DEBUG_LOG_MESSAGE="Showing Maven-DEBUG-Output ...................."
SHOW_MAVEN_COMMANDO_LINE_LOG_MESSAGE="Showing Maven-MVN-Command-Line-Output ...................."
FOLDER_ALREADY_EXISTENT_MESSAGE='Folders ja existentes:'
FOLDER_LIST_MESSAGE='Folders criadas na pasta:'


# C) METODOS/FUNCOES OPERACIONAIS DO SCRIPT

scan_user_input_line() {

    if [ -z "$BASH_VERSION" ]; then
       read -p "$1" value_to_be_matched

    else
       read -e -p "$1" value_to_be_matched

    fi

    echo "$value_to_be_matched"
}

artifactId_input() {

  local artifactId_text='O artifactId'

  while true; do

    artifactId=$(default_value_used_when_return_key_typed_in_input "artifact_model" "my-archetype" "$ARTIFACT_ID_PROMPT")

    if [ -d "$artifactId" ]; then
       echo -e "\n$artifactId_text \"$artifactId\" $ARTIFACT_ID_EXISTS_WARNING \n\n$FOLDER_ALREADY_EXISTENT_MESSAGE\n"
       ls -l
       echo
    else
       break
    fi

  done
}

execute_mvn_maven_command_line() {
  local show_basic_log=$1
  local show_debug_log=$2

  echo -e "\n$PROCESS_ON_GOING\n"

  local output=""
  local maven_output

  if [ "$show_basic_log" = true ]; then
    output="$SHOW_MAVEN_BASIC_LOG_MESSAGE"
  fi

  if [ "$show_debug_log" = true ]; then
    output+="\n\n$SHOW_MAVEN_DEBUG_LOG_MESSAGE"

    mvn_cli_switches="--batch-mode --debug"
    before_batch_switch=${maven_command_with_configured_params%%--batch-mode*}
    after_batch_switch=${maven_command_with_configured_params#*--batch-mode}

    maven_command_with_configured_params="$before_batch_switch $mvn_cli_switches $after_batch_switch"

    echo -e "$SHOW_MAVEN_COMMANDO_LINE_LOG_MESSAGE \n\n$maven_command_with_configured_params\n"
  fi

  if [ "$show_basic_log" = true ] || [ "$show_debug_log" = true ]; then
    echo -e "\n$output\n"
    maven_output=$(eval "$maven_command_with_configured_params")
    echo "$maven_output"
  else
    maven_output=$(eval "$maven_command_with_configured_params")
  fi

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

    #package: Sequência de letras|números|pontos|underscore|hífens (ex: com.example.meu_pacote)
    package) input_patterns="^[a-zA-Z][a-zA-Z0-9]*(\.[a-zA-Z0-9]+)+$";;

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

default_value_used_when_return_key_typed_in_input() {

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

custom_attributes_list_input() {

  if [[ "${enable_custom_attributes,,}" =~ ^(true|t|T|TRUE|True)$ ]]; then
     custom_attributes_list=$(default_value_used_when_return_key_typed_in_input "texto" "Long:Id, String:name" "$CUSTOM_ATTRIBUTES_LIST_PROMPT")

  else
     custom_attributes_list="false"

  fi
}

package_input() {

    value_to_be_matched=$(scan_user_input_line "$PACKAGE_PROMPT ($groupid.$artifactId): ")

    if [ -z "$value_to_be_matched" ]; then
       echo "$groupid.$artifactId"

    else
       echo "$value_to_be_matched"

    fi
}

open_ide_final_option_input() {

  local user_options_text='\nDo you want to:\n\n 1. Open project in IDE\n 2. Exit\n'
  local user_options_input_patterns_from_prompt='Choose an option (1/2): '
  local invalid_user_option='Invalid option. Please try again.'
  local ide_path_windows='C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2024.2.2\bin\'
  local ide_path_unix='/mnt/c/Program Files/JetBrains/IntelliJ IDEA Community Edition 2024.2.2/bin/'
  local ide_path_wsl2_windows="'${ide_path_unix}idea64.exe'"

  if [ -f "${ide_path_wsl2_windows}idea64.exe" ]; then
      OPEN_IDE_CMD="cd $artifactId && "${ide_path_wsl2_windows}" . & ; break ;"
  else
      echo -e "\n\nErro: idea64.exe não encontrado em ${ide_path_wsl2_windows}"
  fi

  echo -e "\n$FOLDER_LIST_MESSAGE\n"
  ls -l

#  echo -e "\n$FINISH_PROMPT\n"

  while true; do

    echo -e "$user_options_text"
    read -p "$user_options_input_patterns_from_prompt" option

    case $option in
#      1) cd $artifactId && "${ide_path_wsl2_windows}" . & ; break ;;
      1) eval "$OPEN_IDE_CMD" ;;
      2) break ;;
      *) echo "$invalid_user_option" ;;
    esac

  done
#  read -p "$(echo -e "\n$FINISH_PROMPT")"
}

#-------------------------------------------------------------------------------
#    D) 4 PASSOS P/ ADICAO DE CRITERIOS ADVINDOS DO ARCHETYPE-METADATA.XML
#-------------------------------------------------------------------------------

# D.1) CONSTANTES relativas aos "PROMPTS RESPONDIDOS PELO USUARIO"
PACKAGE_PROMPT="Digite o package"
GROUPID_PROMPT="Digite o GroupId"
VERSION_PROMPT="Digite a Version"
PROJECT_NAME_PROMPT="Digite o nome do projeto"
ARTIFACT_ID_PROMPT="Digite o ArtifactId"
ENABLE_FOLDER_SRC_TEST_JAVA_PROMPT="Habilitar pasta src/test/java"
ENABLE_FOLDER_SRC_TEST_RESOURCES_PROMPT="Habilitar pasta src/test/resources"
ENABLE_LOMBOK_PROMPT="Habilitar Lombok"
ENABLE_CUSTOM_ATTRIBUTES_PROMPT="Habilitar atributos customizados"
ENABLE_YML_FILE_IN_MAIN_RESOURCES_PROMPT="Criar application.yml no Resources"
CUSTOM_ATTRIBUTES_LIST_PROMPT="Defina os atributos do Model"
PROJECT_DESCRIPTION_PROMPT="Descricao do projeto"
ENABLE_SECURITY_AUTHENTICATION_SUPPORT_PROMPT="Habilitar Autenticacao JWT"
EXTRA_DEPENDENCIES_PROMPT="Add POM-Dependencies [Delimiters , : ]"


# D.2) 'RESPOSTAS CHECADAS' COLETADAS EM VARIAVEIS
# D.2.1) general_inputs
groupid=$(default_value_used_when_return_key_typed_in_input "package" "com.b1r" "$GROUPID_PROMPT")
version=$(default_value_used_when_return_key_typed_in_input "numero" "1.17.1" "$VERSION_PROMPT")
project_name=$(default_value_used_when_return_key_typed_in_input "artifact_model" "NoName" "$PROJECT_NAME_PROMPT")
project_description=$(default_value_used_when_return_key_typed_in_input "texto" "This is my description" "$PROJECT_DESCRIPTION_PROMPT")

enable_folder_src_test_java=$(default_value_used_when_return_key_typed_in_input "boleana" "false" "$ENABLE_FOLDER_SRC_TEST_JAVA_PROMPT")
enable_folder_src_test_resources=$(default_value_used_when_return_key_typed_in_input "boleana" "false" "$ENABLE_FOLDER_SRC_TEST_RESOURCES_PROMPT")
enable_lombok=$(default_value_used_when_return_key_typed_in_input "boleana" "false" "$ENABLE_LOMBOK_PROMPT")
enable_yml_file_in_main_resources=$(default_value_used_when_return_key_typed_in_input "boleana" "false" "$ENABLE_YML_FILE_IN_MAIN_RESOURCES_PROMPT")
enable_security_authentic_support=$(default_value_used_when_return_key_typed_in_input "boleana" "false" "$ENABLE_SECURITY_AUTHENTICATION_SUPPORT_PROMPT")
enable_custom_attributes=$(default_value_used_when_return_key_typed_in_input "boleana" "false" "$ENABLE_CUSTOM_ATTRIBUTES_PROMPT")

extra_dependencies=$(default_value_used_when_return_key_typed_in_input "dependency" "org.slf4j:slf4j-api:1.7.1:runtime" "$EXTRA_DEPENDENCIES_PROMPT")

# D.2.2) Variaveis coletadas dentro de Metodos
custom_attributes_list_input
artifactId_input

# D.2.3) package_input
package=$(package_input)


# D.3) Montagem do 'Parametro/Switch -D' do Maven,
# com a Variavel das respostas coletadas
maven_command_with_configured_params="mvn archetype:generate --batch-mode \
-DarchetypeGroupId=\"$ARCHETYPE_GROUP_ID\" \
\
-DarchetypeArtifactId=\"$ARCHETYPE_ARTIFACT_ID\" \
-DarchetypeVersion=\"$ARCHETYPE_VERSION\" \
\
-Dpackage=\"$package\" \
-DgroupId=\"$groupid\" \
-DartifactId=\"$artifactId\" \
-Dversion=\"$version\" \
-Dproject_name=\"$project_name\" \
-Dproject_description=\"$project_description\" \
\
-Denable_folder_src_test_java=\"$enable_folder_src_test_java\" \
-Denable_folder_src_test_resources=\"$enable_folder_src_test_resources\" \
-Denable_lombok=\"$enable_lombok\" \
-Denable_yml_file_in_main_resources=\"$enable_yml_file_in_main_resources\" \
-Denable_security_authentication_support=\"$enable_security_authentic_support\" \
-Denable_custom_attributes=\"$enable_custom_attributes\" \
\
-Dcustom_attributes_list=\"$custom_attributes_list\" \
\
-Dextra_POM_dependencies=\"$extra_dependencies\" \
\
-Dshow_archetype_posprocessor_log=\"$show_archetype_pos_processor_log\""

# D.4) Criar um criterio identico ao 'Parametro -D'(variavel) nos arquivos:
# D.4.1) src\main\resources\ "META-INF" \ "MAVEN" \archetype-metadata.XML
# D.4.2) src\test\resources\ "PROJECTS" \ "BASIC" \archetype.PROPERTIES

#-------------------------------------------------------------------------------
#-------------------------------------------------------------------------------
#-------------------------------------------------------------------------------

execute_mvn_maven_command_line $show_maven_basic_log $show_maven_debug_log

open_ide_final_option_input