#!/bin/bash

# A) GENERAL OPTIONS------------------------------------------------------------
# A.1) LOG+DEBUG MANAGEMENT
SHOW_MAVEN_BASIC_LOG=true
SHOW_MAVEN_DEBUG_LOG=true
SHOW_POS_PROCESSOR_LOG=true
SHOW_INJECTOR_TOOL_LOG=true

# A.2) IDE EXECUTION AFTER SCRIPT EXECUTION
IDE_EXEC_FILE_FOLDER='C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2024.3\bin\'
IDE_EXEC_FILE='idea64.exe'
USE_IDE_ACTIVATION_MANAGER=true
OPEN_IDE_DIRECTLY=false

# A.3) ENABLING PROGRESS_BAR
ACTIVE_PROGRESS_BAR=false

# A.4) SCRIPT INITIALIZATION
SCRIPT_FOLDER="c:\\WORKBASE\\DEV\\SCRIPTS\\archetype-v2-eda\\bash_script"
source "${SCRIPT_FOLDER}\\script_injection_tool.sh"
source "${SCRIPT_FOLDER}\\project_viability_checker.sh"
project_folder_viability_checking

# B) INJECOES DE FUNCOES/SCRIPTS OPERACIONAIS-------------------------------------------
# B.1) FUNCOES ESPECIAIS
script_injection "${SCRIPT_FOLDER}" "progress_bar.sh" $SHOW_INJECTOR_TOOL_LOG

# B.2) FUNCOES GERAIS
script_injection "${SCRIPT_FOLDER}" "functions.sh" $SHOW_INJECTOR_TOOL_LOG

# B.3) FUNCAO DE CONFIGURCAO GERAL
script_injection "${SCRIPT_FOLDER}" "config.sh" $SHOW_INJECTOR_TOOL_LOG

# B.4) FUNCAO DE MENSAGENS GERAIS
script_injection "${SCRIPT_FOLDER}" "general_messages.sh" $SHOW_INJECTOR_TOOL_LOG

#-------------------------------------------------------------------------------
#           C) PASSOS P/ ADICAO DE 'NOVOS CRITERIOS', configurando:
#                          ARCHETYPE-METADATA.XML
#                       ARCHETYPE-POS-GENERATE.GROOVY
#-------------------------------------------------------------------------------
# C.1) INJECOES/EXECUCAO DE SCRIPTS DE INPUT/coleta de 'VARIAVEIS DE SWITCHES DO MVN CLI'
script_injection "${SCRIPT_FOLDER}" "user_general_inputs.sh" $SHOW_INJECTOR_TOOL_LOG
script_injection "${SCRIPT_FOLDER}" "user_docker_input.sh" $SHOW_INJECTOR_TOOL_LOG
script_injection "${SCRIPT_FOLDER}" "user_lombok_input.sh" $SHOW_INJECTOR_TOOL_LOG
script_injection "${SCRIPT_FOLDER}" "user_attributes_input.sh" $SHOW_INJECTOR_TOOL_LOG
script_injection "${SCRIPT_FOLDER}" "user_logback_manager_input.sh" $SHOW_INJECTOR_TOOL_LOG
script_injection "${SCRIPT_FOLDER}" "user_jwt_authentication_inputs.sh" $SHOW_INJECTOR_TOOL_LOG
script_injection "${SCRIPT_FOLDER}" "user_dependencies_inputs.sh" $SHOW_INJECTOR_TOOL_LOG
script_injection "${SCRIPT_FOLDER}" "user_test_inputs.sh" $SHOW_INJECTOR_TOOL_LOG
script_injection "${SCRIPT_FOLDER}" "user_artifactid_inputs.sh" $SHOW_INJECTOR_TOOL_LOG

# C.2) Packages:
# * 'Arvores de Pastas separadas por ".", onde a(s) classe(s) se encontra(m)
project_package=$groupid

# C.3) MONTAGEM da 'MVN COMMAND LINE' com SWITCHES DO MVN CLI
# Montagem de 'Switches -D' do 'mvn cli' usando-se as 'PROMPT-ANSWERS' INPUTADAS
# coletoras das 'VARIAVEIS DE SWITCHES DO MVN CLI' encontradas no script acima
script_injection "${SCRIPT_FOLDER}" "mvn_command_line.sh" $SHOW_INJECTOR_TOOL_LOG

# C.4) Criar uma 'REQUIRED-PROPERTY' identica ao 'Switch -D DA Mvn command line', EM:
# C.4.1) src\"MAIN"\resources\ "META-INF" \ "MAVEN" \ "archetype-metadata".XML
# C.4.2) src\"TEST"\resources\ "PROJECTS" \ "BASIC" \ "archetype".PROPERTIES
# C.4.3) Use a 'REQUIRED-PROPERTY' criada, com o 'VELOCITY' no 'arquivo' de codigo, se necessario

# C.5) Se 'novo Criterio' realizar "MANIPULACAO PASTAS OU ARQUIVOS":
# C.5.1) Crie/Insira a 'pasta/arquivo' dentro do 'ARCHETYPE', no local correto, EM:
#   - src\"MAIN"\resources\ "ARCHETYPE-RESOURCES" \ src
#
# C.5.2) Especifique a 'pasta/arquivo' no area "FILE-SET", EM:
#   - src\"MAIN"\resources\ "META-INF" \ "MAVEN" \ "archetype-metadata".XML
#
# C.5.3) Crie a 'FUNCAO PARA MANIPULACAO' da 'pasta/arquivo', EM:
#   -  src\"MAIN"\resources\ "META-INF" \ "archetype-post-generate".groovy
#-------------------------------------------------------------------------------

execute_maven_command_line $SHOW_MAVEN_BASIC_LOG $SHOW_MAVEN_DEBUG_LOG "$maven_command_line"

open_ide_manager $USE_IDE_ACTIVATION_MANAGER $OPEN_IDE_DIRECTLY "$IDE_EXEC_FILE_FOLDER" "$IDE_EXEC_FILE"