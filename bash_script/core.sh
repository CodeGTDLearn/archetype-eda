#!/bin/bash

# A) GENERAL OPTIONS
# A.1) LOG+DEBUG MANAGEMENT
SHOW_MAVEN_BASIC_LOG=true
SHOW_MAVEN_DEBUG_LOG=true
SHOW_POS_PROCESSOR_LOG=true

# A.2) IDE EXECUTION AFTER SCRIPT EXECUTION
IDE_EXEC_FILE_FOLDER='C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2024.3\bin\'
IDE_EXEC_FILE='idea64.exe'
USE_IDE_ACTIVE_MANAGER=true
OPEN_IDE_DIRECTLY=true

# A.3) ENABLING PROGRESS_BAR
ACTIVE_PROGRESS_BAR=false

# A.4) SCRIPT INITIALIZATION
SCRIPT_FOLDER="c:\\WORKBASE\\DEV\\SCRIPTS\\archetype-v2-eda\\bash_script"
source "${SCRIPT_FOLDER}\\script_injection_tool.sh"
source "${SCRIPT_FOLDER}\\project_viability_checker.sh"
project_folder_viability_checking

# B) INJECOES DE SCRIPTS
# B.1) FUNCOES ESPECIAIS DO SCRIPT
script_injection "${SCRIPT_FOLDER}" "progress_bar.sh"

# B.2) FUNCOES GERAIS DO SCRIPT
script_injection "${SCRIPT_FOLDER}" "functions.sh"

# B.3) CONFIGURACOES GENERICAS DO SCRIPT
script_injection "${SCRIPT_FOLDER}" "config.sh"

# B.4) MENSAGENS DE USO GERAL
script_injection "${SCRIPT_FOLDER}" "general_messages.sh"

#-------------------------------------------------------------------------------
#           D) 05 PASSOS P/ ADICAO DE 'NOVOS CRITERIOS', configurando:
#                          ARCHETYPE-METADATA.XML
#                       ARCHETYPE-POS-GENERATE.GROOVY
#-------------------------------------------------------------------------------
# D.1) INJECAO: 'prompts ao usuario'
script_injection "${SCRIPT_FOLDER}" "user_prompts.sh"

# D.2) 'PROMPT-ANSWERS' CHECADAS/COLETADAS
# D.2.1) Single Inputs
groupid=$(user_input_or_default_value "groupid" "com.b1r" "$GROUPID_PROMPT")
version=$(user_input_or_default_value "numero" "1.17.1" "$VERSION_PROMPT")
project_name=$(user_input_or_default_value "model_entity" "NoName" "$PROJECT_NAME_PROMPT")
project_description=$(user_input_or_default_value "texto" "This is my description" "$PROJECT_DESCRIPTION_PROMPT")

enable_folder_src_test_java=$(user_input_or_default_value "boleana" "false" "$ENABLE_FOLDER_SRC_TEST_JAVA_PROMPT")
enable_folder_src_test_resources=$(user_input_or_default_value "boleana" "false" "$ENABLE_FOLDER_SRC_TEST_RESOURCES_PROMPT")
enable_lombok=$(user_input_or_default_value "boleana" "false" "$ENABLE_LOMBOK_PROMPT")
enable_logback=$(user_input_or_default_value "boleana" "false" "$ENABLE_LOGBACK_PROMPT")
enable_security_authentic_support=$(user_input_or_default_value "boleana" "false" "$ENABLE_SECURITY_AUTHENTICATION_SUPPORT_PROMPT")

# D.2.2) Compound Inputs
enable_custom_attributes=$(user_input_or_default_value "boleana" "false" "$ENABLE_CUSTOM_ATTRIBUTES_PROMPT")
custom_attributes_list_input
enable_dependencies_addition=$(user_input_or_default_value "boleana" "false" "$ENABLE_DEPENDENCIES_ADD_PROMPT")
custom_dependencies_addition_input

# D.2.3) Input Methods ('Variable' loaded by the methods)
artifactId_input

# D.2.4) Input Combinated Variables
package=$groupid.$artifactId

# D.3) INJECAO: 'mvn command line'
# Montagem de 'Switches -D' do 'Maven/Mvn command line'
# usando-se as 'PROMPT-ANSWERS' acima
script_injection "${SCRIPT_FOLDER}" "mvn_command_line.sh"

# D.4) Criar uma 'REQUIRED-PROPERTY' identica ao 'Switch -D DA Mvn command line', EM:
# D.4.1) src\"MAIN"\resources\ "META-INF" \ "MAVEN" \ "archetype-metadata".XML
# D.4.2) src\"TEST"\resources\ "PROJECTS" \ "BASIC" \ "archetype".PROPERTIES

# D.5) Se 'novo Criterio' realizar "MANIPULACAO PASTAS OU ARQUIVOS":
#
# D.5.1) Crie/Insira a 'pasta ou arquivo' dentro do 'ARCHETYPE', EM:
#   - src\"MAIN"\resources\ "ARCHETYPE-RESOURCES" \ src
#
# D.5.2) Especifique a 'pasta ou arquivo' no area "FILE-SET", EM:
#   - src\"MAIN"\resources\ "META-INF" \ "MAVEN" \ "archetype-metadata".XML
#
# D.5.3) Crie a 'FUNCAO PARA MANIPULACAO' da 'pasta ou arquivo', EM:
#   -  src\"MAIN"\resources\ "META-INF" \ "archetype-post-generate".groovy
#-------------------------------------------------------------------------------
#-------------------------------------------------------------------------------

execute_maven_command_line $SHOW_MAVEN_BASIC_LOG $SHOW_MAVEN_DEBUG_LOG "$maven_command_line"

open_ide_manager $USE_IDE_ACTIVE_MANAGER $OPEN_IDE_DIRECTLY "$IDE_EXEC_FILE_FOLDER" "$IDE_EXEC_FILE"