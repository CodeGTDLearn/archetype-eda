#!/bin/bash

# A) GENERAL OPTIONS
# A.1) LOGS
SHOW_MAVEN_BASIC_LOG=false
SHOW_MAVEN_DEBUG_LOG=false
SHOW_POS_PROCESSOR_LOG=false

# A.2) IDE EXECUTION AFTER SCRIPT EXECUTION
IDE_EXEC_FILE_FOLDER='C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2024.3\bin\'
IDE_EXEC_FILE='idea64.exe'
IDE_EXEC_CHOICE=true

# A.3) SCRIPT BOOTSTRAP STARTING
SCRIPT_FOLDER="c:\\WORKBASE\\DEV\\SCRIPTS\\archetype-v2-eda\\bash_script"
source "${SCRIPT_FOLDER}\\script_initializer.sh"
project_folder_viability_checking

# B) INJECOES DE SCRIPTS
# B.1) FUNCOES
script_injection "${SCRIPT_FOLDER}" "functions.sh"

# B.2) CONFIGURACOES
script_injection "${SCRIPT_FOLDER}" "config.sh"

# B.3) MENSAGENS
script_injection "${SCRIPT_FOLDER}" "general_messages.sh"

#-------------------------------------------------------------------------------
# D) 4 PASSOS P/ ADICAO DE NOVOS CRITERIOS DO ARCHETYPE-METADATA.XML
#-------------------------------------------------------------------------------
# D.1) INJECAO: 'prompts ao usuario'
script_injection "${SCRIPT_FOLDER}" "user_prompts.sh"

# D.2) 'PROMPT-ANSWERS' CHECADAS/COLETADAS
# D.2.1) Single Inputs
groupid=$(user_input_or_default_value "groupid" "com.b1r" "$GROUPID_PROMPT")
version=$(user_input_or_default_value "numero" "1.17.1" "$VERSION_PROMPT")
project_name=$(user_input_or_default_value "artifact_model" "NoName" "$PROJECT_NAME_PROMPT")
project_description=$(user_input_or_default_value "texto" "This is my description" "$PROJECT_DESCRIPTION_PROMPT")

enable_folder_src_test_java=$(user_input_or_default_value "boleana" "false" "$ENABLE_FOLDER_SRC_TEST_JAVA_PROMPT")
enable_folder_src_test_resources=$(user_input_or_default_value "boleana" "false" "$ENABLE_FOLDER_SRC_TEST_RESOURCES_PROMPT")
enable_lombok=$(user_input_or_default_value "boleana" "false" "$ENABLE_LOMBOK_PROMPT")
enable_yml_file_in_main_resources=$(user_input_or_default_value "boleana" "false" "$ENABLE_YML_FILE_IN_MAIN_RESOURCES_PROMPT")
enable_security_authentic_support=$(user_input_or_default_value "boleana" "false" "$ENABLE_SECURITY_AUTHENTICATION_SUPPORT_PROMPT")

# D.2.2) Compound Inputs
enable_custom_attributes=$(user_input_or_default_value "boleana" "false" "$ENABLE_CUSTOM_ATTRIBUTES_PROMPT")
custom_attributes_list_input
enable_dependencies_addition=$(user_input_or_default_value "boleana" "false" "$ENABLE_DEPENDENCIES_ADD_PROMPT")
custom_dependencies_addition_input

# D.2.3) Input Methods ('Variable' loaded by the methods)
artifactId_input

# D.3) INJECAO: 'mvn command line'
# Montagem de 'Switches -D' do 'Maven/Mvn command line'
# usando-se as 'PROMPT-ANSWERS' acima
script_injection "${SCRIPT_FOLDER}" "mvn_command_line.sh"

# D.4) Criar um criterio identico ao 'Switch -D da Maven/Mvn command line' em:
# D.4.1) src\main\resources\ "META-INF" \ "MAVEN" \archetype-metadata.XML
# D.4.2) src\test\resources\ "PROJECTS" \ "BASIC" \archetype.PROPERTIES

#-------------------------------------------------------------------------------
#-------------------------------------------------------------------------------

execute_maven_command_line $SHOW_MAVEN_BASIC_LOG $SHOW_MAVEN_DEBUG_LOG "$maven_command_line"

open_ide_choice_input $IDE_EXEC_CHOICE "$IDE_EXEC_FILE_FOLDER" "$IDE_EXEC_FILE"