#!/bin/bash

# A) GENERAL OPTIONS------------------------------------------------------------
# A.1) ATUALIZA o ARCHETYPE-METADATA 'PROCESSOR'
# qdo o 'ARCHETYPE_METADATA.XML' for alterado
RECREATE_ARCHETYPE=false
SHOW_RECREATE_ARCHETYPE_LOG=false

# A.2) LOG|DEBUG MANAGEMENT
SHOW_MAVEN_BASIC_LOG=false
SHOW_MAVEN_DEBUG_LOG=false
SHOW_ARCHETYPE_POS_PROCESSOR_LOG=false
SHOW_INJECTION_LOG=false

# A.3) IDE EXECUTION AFTER SCRIPT EXECUTION
IDE_EXEC_FILE_FOLDER='C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2024.3\bin\'
IDE_EXEC_FILE='idea64.exe'
USE_IDE_ACTIVATION_MANAGER=true
OPEN_IDE_DIRECTLY=false

# A.4) ENABLING PROGRESS_BAR
ACTIVE_PROGRESS_BAR=false

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
project_folder_viability_checking

# A.7) SCRIPT INITIALIZATION
source "${SCRIPT_FOLDER}\\script_injection.sh"

# B) INJECOES DE FUNCOES -------------------------------------------------------
script_injection "${SCRIPT_FOLDER}" "progress_bar.sh" $SHOW_INJECTION_LOG
script_injection "${SCRIPT_FOLDER}" "general_functions.sh" $SHOW_INJECTION_LOG
script_injection "${SCRIPT_FOLDER}" "user_input_processor.sh" $SHOW_INJECTION_LOG
script_injection "${SCRIPT_FOLDER}" "config_constants.sh" $SHOW_INJECTION_LOG
script_injection "${SCRIPT_FOLDER}" "general_messages.sh" $SHOW_INJECTION_LOG

#-------------------------------------------------------------------------------
#            C) PASSOS P/ ADICAO DE 'NOVOS CRITERIOS', configurando:
#           * Pre-Processador: BASH_SCRIPTs (folder: "bash_script")
#                 * Processador: "ARCHETYPE-METADATA".XML
#              * Processador-Test: "ARCHETYPE".PROPERTIES(test)
#             * Pos-Processador: "ARCHETYPE-POS-GENERATE".GROOVY
#-------------------------------------------------------------------------------
# C.1) INJECOES DE 'SCRIPTS DE INPUT'/coleta de 'VARIAVEIS p/ os SWITCHES DO MVN-CLI'
script_injection "${SCRIPT_FOLDER}" "user_general_inputs.sh" $SHOW_INJECTION_LOG
script_injection "${SCRIPT_FOLDER}" "user_attributes_input.sh" $SHOW_INJECTION_LOG
script_injection "${SCRIPT_FOLDER}" "user_docker_input.sh" $SHOW_INJECTION_LOG
script_injection "${SCRIPT_FOLDER}" "user_lombok_input.sh" $SHOW_INJECTION_LOG
script_injection "${SCRIPT_FOLDER}" "user_logback_manager_input.sh" $SHOW_INJECTION_LOG
script_injection "${SCRIPT_FOLDER}" "user_cpanel_input.sh" $SHOW_INJECTION_LOG
script_injection "${SCRIPT_FOLDER}" "user_jwt_authentication_inputs.sh" $SHOW_INJECTION_LOG
script_injection "${SCRIPT_FOLDER}" "user_extra_pom_dependencies_inputs.sh" $SHOW_INJECTION_LOG
script_injection "${SCRIPT_FOLDER}" "user_test_inputs.sh" $SHOW_INJECTION_LOG
script_injection "${SCRIPT_FOLDER}" "user_artifactid_inputs.sh" $SHOW_INJECTION_LOG

# C.2) Packages: 'Arvores de Pastas separadas por ".", onde a(s) classe(s) se encontra(m)
project_package=$groupid

# C.3) MONTAGEM das 'SWITCHES -D' para o MVN-CLI
script_injection "${SCRIPT_FOLDER}" "mvn_command_line.sh" $SHOW_INJECTION_LOG

# C.4) 'REQUIRED-PROPERTY':
# C.5) 'PROPERTY DE TESTE' do archetype
# C.6) "MANIPULACAO DE PASTAS OU ARQUIVOS" (se necessario)
# C.7) 'FILE-SET':
# C.8) VARIAVEL P/ "MANIPULACAO DE PASTAS OU ARQUIVOS" (se necessario)
# C.9) FUNCAO P/ "MANIPULACAO DE PASTAS OU ARQUIVOS" (se necessario)
#-------------------------------------------------------------------------------

execute_maven_command_line $SHOW_MAVEN_BASIC_LOG $SHOW_MAVEN_DEBUG_LOG "$maven_command_line"

open_ide_manager $USE_IDE_ACTIVATION_MANAGER $OPEN_IDE_DIRECTLY "$IDE_EXEC_FILE_FOLDER" "$IDE_EXEC_FILE" $SHOW_INJECTION_LOG