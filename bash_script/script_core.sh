#!/bin/bash

# A.1) ARCHETYPE UPDATE - "PRE-PROCESSOR" --------------------------------------
# A.1.1) RECRIA o "ARCHETYPE-METADATA.XML" e "ARCHETYPE-POST-GENERATE.GROOVY"
RECREATE_ARCHETYPE=true

# A.1.2) LOG|DEBUG - ARCHETYPE RECREATION
SHOW_RECREATE_ARCHETYPE_LOG=false

# A.2) LOG|DEBUG MANAGEMENT "PROCESSOR"
SHOW_INJECTION_LOG=false
SHOW_MAVEN_BASIC_LOG=false
SHOW_MAVEN_DEBUG_LOG=false
SHOW_ARCHETYPE_POS_PROCESSOR_LOG=false

# A.3) IDE EXECUTION AFTER SCRIPT EXECUTION
IDE_EXEC_FILE_FOLDER='C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2024.3\bin\'
IDE_EXEC_FILE='idea64.exe'
USE_IDE_ACTIVATION_MANAGER=true
OPEN_IDE_DIRECTLY=true

# A.4) ENABLING PROGRESS_BAR
# Progress_bar runa inside the function 'execute_maven_command_line'(maven_functions.sh)
ACTIVE_PROGRESS_BAR=true

# A.5) ARCHETYPE FILES GENERATION:
# A.5.1) PATH DEFINITIONS
PROJECT_FOLDER="c:\\WORKBASE\\DEV\\SCRIPTS\\archetype-v2-eda"
PROJECT_FOLDER_LINUX="/c/WORKBASE/DEV/SCRIPTS/archetype-v2-eda"
META_INF_FOLDER="${PROJECT_FOLDER}\\src\\main\\resources\\META-INF"
MAVEN_FOLDER="${META_INF_FOLDER}\\maven"
SCRIPT_FOLDER="${PROJECT_FOLDER}\\bash_script"
source "${SCRIPT_FOLDER}\\maven_functions.sh"

# A.5.2) ARCHETYPE FILES GENERATION ('archetype-metadata.xml', 'archetype-post-generate.groovy')
source "${SCRIPT_FOLDER}\\create_archetype_metadata.sh"
create_archetype_metadata_xml "${RECREATE_ARCHETYPE}" "${MAVEN_FOLDER}" "archetype-metadata.xml" "$SHOW_RECREATE_ARCHETYPE_LOG"

# A.5.3) ARCHETYPE-METADATA.XML
source "${SCRIPT_FOLDER}\\create_archetype_post_generate.sh"
create_archetype_post_generate_groovy "${RECREATE_ARCHETYPE}" "${META_INF_FOLDER}" "archetype-post-generate.groovy" "$SHOW_RECREATE_ARCHETYPE_LOG"

# A.5.4) ARCHETYPE-POST-GENERATE.GROOVY
execute_mvn_clean_install "${PROJECT_FOLDER_LINUX}" "${RECREATE_ARCHETYPE}" "${SHOW_RECREATE_ARCHETYPE_LOG}"

# A.6) CHECKING PROJECT FOLDER VIABILITY
source "${SCRIPT_FOLDER}\\project_viability_checker.sh"
check_project_folder_viability "true"

# A.7) SCRIPT INITIALIZATION
source "${SCRIPT_FOLDER}\\script_injection.sh"


# B) INJECOES DE FUNCOES -------------------------------------------------------
inject_script "${SCRIPT_FOLDER}" "progress_bar_functions.sh" $SHOW_INJECTION_LOG
inject_script "${SCRIPT_FOLDER}" "general_functions.sh" $SHOW_INJECTION_LOG
inject_script "${SCRIPT_FOLDER}" "input_processor_functions.sh" $SHOW_INJECTION_LOG
inject_script "${SCRIPT_FOLDER}" "config_constants.sh" $SHOW_INJECTION_LOG
inject_script "${SCRIPT_FOLDER}" "general_messages_constants.sh" $SHOW_INJECTION_LOG


#-------------------------------------------------------------------------------
#            C) PASSOS P/ ADICAO DE 'NOVOS CRITERIOS', configurando:
#           * Pre-Processador: BASH_SCRIPTs (folder: "bash_script")
#                 * Processador: "ARCHETYPE-METADATA".XML
#              * Processador-Test: "ARCHETYPE".PROPERTIES(test)
#             * Pos-Processador: "ARCHETYPE-POS-GENERATE".GROOVY
#-------------------------------------------------------------------------------
# C.1) INJECOES DE 'SCRIPTS DE INPUT'/coleta de 'VARIAVEIS p/ os SWITCHES DO MVN-CLI'
inject_script "${SCRIPT_FOLDER}" "user_general_inputs.sh" $SHOW_INJECTION_LOG
inject_script "${SCRIPT_FOLDER}" "user_entity_attributes_inputs.sh" $SHOW_INJECTION_LOG
inject_script "${SCRIPT_FOLDER}" "user_api_general_inputs.sh" $SHOW_INJECTION_LOG
inject_script "${SCRIPT_FOLDER}" "user_extra_pom_dependencies_inputs.sh" $SHOW_INJECTION_LOG
inject_script "${SCRIPT_FOLDER}" "user_properties_filetype_input.sh" $SHOW_INJECTION_LOG
inject_script "${SCRIPT_FOLDER}" "user_api_tests_input.sh" $SHOW_INJECTION_LOG
inject_script "${SCRIPT_FOLDER}" "user_binding_properties_inputs.sh" $SHOW_INJECTION_LOG
inject_script "${SCRIPT_FOLDER}" "user_logback_manager_inputs.sh" $SHOW_INJECTION_LOG
inject_script "${SCRIPT_FOLDER}" "user_docker_input.sh" $SHOW_INJECTION_LOG
inject_script "${SCRIPT_FOLDER}" "user_lombok_input.sh" $SHOW_INJECTION_LOG
inject_script "${SCRIPT_FOLDER}" "user_cpanel_input.sh" $SHOW_INJECTION_LOG
inject_script "${SCRIPT_FOLDER}" "user_jwt_authentication_input.sh" $SHOW_INJECTION_LOG
inject_script "${SCRIPT_FOLDER}" "user_artifactid_inputs.sh" $SHOW_INJECTION_LOG

# C.2) Packages: 'Arvores de Pastas separadas por ".", onde a(s) classe(s) se encontra(m)
project_package=$groupid

# C.3) MONTAGEM das 'SWITCHES -D' para o MVN-CLI
inject_script "${SCRIPT_FOLDER}" "mvn_command_line.sh" $SHOW_INJECTION_LOG

# C.4) 'REQUIRED-PROPERTY':
# C.5) 'PROPERTY DE TESTE' do archetype
# C.6) "MANIPULACAO DE PASTAS OU ARQUIVOS" (se necessario)
# C.7) 'FILE-SET':
# C.8) VARIAVEL P/ "MANIPULACAO DE PASTAS OU ARQUIVOS" (se necessario)
# C.9) FUNCAO P/ "MANIPULACAO DE PASTAS OU ARQUIVOS" (se necessario)
#-------------------------------------------------------------------------------

# Progression_Bar is executed inside the function 'execute_maven_command_line'
# which is found inside the script: maven_functions.sh
execute_maven_command_line $SHOW_MAVEN_BASIC_LOG $SHOW_MAVEN_DEBUG_LOG "$maven_command_line"

open_ide_manager $USE_IDE_ACTIVATION_MANAGER $OPEN_IDE_DIRECTLY "$IDE_EXEC_FILE_FOLDER" "$IDE_EXEC_FILE" $SHOW_INJECTION_LOG