#!/bin/bash

# A.1) ARCHETYPE UPDATE - "PRE-PROCESSOR" --------------------------------------
# A.1.1) RECRIA o "ARCHETYPE-METADATA.XML" e "ARCHETYPE-POST-GENERATE.GROOVY"
RECREATE_ARCHETYPE=false

# A.1.2) LOG|DEBUG - ARCHETYPE RECREATION
SHOW_RECREATE_ARCHETYPE_LOG=false

# A.2) LOG|DEBUG MANAGEMENT "PROCESSOR"
SHOW_INJECTION_LOG=false
SHOW_MAVEN_BASIC_LOG=false
SHOW_MAVEN_DEBUG_LOG=false
SHOW_ARCHETYPE_POS_PROCESSOR_LOG=false

# A.3) PHASES EXECUTED AFTER SCRIPT EXECUTION
IDE_EXEC_FILE_FOLDER='C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2024.3\bin\'
IDE_EXEC_FILE='idea64.exe'
USE_IDE_ACTIVATION_MANAGER=false
OPEN_IDE_DIRECTLY=true
EXECUTE_API_TESTS=false

# A.4) ENABLING PROGRESS_BAR
# Progress_bar run a inside the function 'execute_maven_command_line'(maven_functions.sh)
ACTIVE_PROGRESS_BAR=false

# A.5) 'ARCHETYPE FILES' GENERATION:
# A.5.1) 'ARCHETYPE FILES' PATH
PROJECT_FOLDER_WIN="c:\\WORKBASE\\DEV\\SCRIPTS\\archetype-v2-eda"
PROJECT_FOLDER_LINUX="/c/WORKBASE/DEV/SCRIPTS/archetype-v2-eda"
META_INF_FOLDER_WIN="${PROJECT_FOLDER_WIN}\\src\\main\\resources\\META-INF"
MAVEN_FOLDER_WIN="${META_INF_FOLDER_WIN}\\maven"
SCRIPT_FOLDER_WIN="${PROJECT_FOLDER_WIN}\\bash_script"
source "${SCRIPT_FOLDER_WIN}\\maven_functions.sh"

# A.5.2) RE-CREATING ARCHETYPE-METADATA.XML
source "${SCRIPT_FOLDER_WIN}\\create_archetype_metadata.sh"
create_archetype_metadata_xml "${RECREATE_ARCHETYPE}" "${MAVEN_FOLDER_WIN}" "archetype-metadata.xml" "$SHOW_RECREATE_ARCHETYPE_LOG"

# A.5.3) RE-CREATING ARCHETYPE-POST-GENERATE.GROOVY
source "${SCRIPT_FOLDER_WIN}\\create_archetype_post_generate.sh"
create_archetype_post_generate_groovy "${RECREATE_ARCHETYPE}" "${META_INF_FOLDER_WIN}" "archetype-post-generate.groovy" "$SHOW_RECREATE_ARCHETYPE_LOG"

# A.5.4) INSTALLING THE ARCHETYPE BASED ON THE NEWLY FILES CREATED ABOVE:
# - ARCHETYPE-POST-GENERATE.GROOVY
# - ARCHETYPE-METADATA.XML
execute_mvn_clean_install "${PROJECT_FOLDER_LINUX}" "${RECREATE_ARCHETYPE}" "${SHOW_RECREATE_ARCHETYPE_LOG}"

# A.6) CHECKING PROJECT FOLDER VIABILITY
source "${SCRIPT_FOLDER_WIN}\\project_viability_checker.sh"
check_project_folder_viability "true"

# A.7) SCRIPT INITIALIZATION
source "${SCRIPT_FOLDER_WIN}\\script_injection.sh"


# B) INJECOES DE FUNCOES -------------------------------------------------------
inject_script "${SCRIPT_FOLDER_WIN}" "progress_bar_functions.sh" $SHOW_INJECTION_LOG
inject_script "${SCRIPT_FOLDER_WIN}" "general_functions.sh" $SHOW_INJECTION_LOG
inject_script "${SCRIPT_FOLDER_WIN}" "general_functions_for_inputs.sh" $SHOW_INJECTION_LOG
inject_script "${SCRIPT_FOLDER_WIN}" "input_processor_functions.sh" $SHOW_INJECTION_LOG
inject_script "${SCRIPT_FOLDER_WIN}" "config_constants.sh" $SHOW_INJECTION_LOG
inject_script "${SCRIPT_FOLDER_WIN}" "general_messages_constants.sh" $SHOW_INJECTION_LOG


#-------------------------------------------------------------------------------
#            C) PASSOS P/ ADICAO DE 'NOVOS CRITERIOS', configurando:
#           * Pre-Processador: BASH_SCRIPTs (folder: "bash_script")
#                 * Processador: "ARCHETYPE-METADATA".XML
#              * Processador-Test: "ARCHETYPE".PROPERTIES(test)
#             * Pos-Processador: "ARCHETYPE-POS-GENERATE".GROOVY
#-------------------------------------------------------------------------------
# C.1) INJECOES DE 'SCRIPTS DE INPUT'/coleta de 'VARIAVEIS p/ os SWITCHES DO MVN-CLI'
inject_script "${SCRIPT_FOLDER_WIN}" "user_general_inputs.sh" $SHOW_INJECTION_LOG
inject_script "${SCRIPT_FOLDER_WIN}" "user_java_version_input.sh" $SHOW_INJECTION_LOG
inject_script "${SCRIPT_FOLDER_WIN}" "user_entity_attributes_inputs.sh" $SHOW_INJECTION_LOG
inject_script "${SCRIPT_FOLDER_WIN}" "user_github_inputs.sh" $SHOW_INJECTION_LOG
inject_script "${SCRIPT_FOLDER_WIN}" "user_extra_pom_dependencies_inputs.sh" $SHOW_INJECTION_LOG
inject_script "${SCRIPT_FOLDER_WIN}" "user_properties_filetype_input.sh" $SHOW_INJECTION_LOG
inject_script "${SCRIPT_FOLDER_WIN}" "user_api_tests_input.sh" $SHOW_INJECTION_LOG
inject_script "${SCRIPT_FOLDER_WIN}" "user_binding_properties_inputs.sh" $SHOW_INJECTION_LOG
inject_script "${SCRIPT_FOLDER_WIN}" "user_logback_manager_inputs.sh" $SHOW_INJECTION_LOG
inject_script "${SCRIPT_FOLDER_WIN}" "user_docker_input.sh" $SHOW_INJECTION_LOG
inject_script "${SCRIPT_FOLDER_WIN}" "user_lombok_input.sh" $SHOW_INJECTION_LOG
inject_script "${SCRIPT_FOLDER_WIN}" "user_cpanel_input.sh" $SHOW_INJECTION_LOG
inject_script "${SCRIPT_FOLDER_WIN}" "user_jwt_authentication_input.sh" $SHOW_INJECTION_LOG
inject_script "${SCRIPT_FOLDER_WIN}" "user_artifactid_inputs.sh" $SHOW_INJECTION_LOG

# C.2) Packages: 'Arvores de Pastas separadas por ".", onde a(s) classe(s) se encontra(m)
project_package=$groupId.$artifactId

# C.3) MONTAGEM das 'SWITCHES -D' para o MVN-CLI
inject_script "${SCRIPT_FOLDER_WIN}" "mvn_command_line_archetype_generate.sh" $SHOW_INJECTION_LOG

# C.4) 'REQUIRED-PROPERTY':
# C.5) 'PROPERTY DE TESTE' do archetype
# C.6) "MANIPULACAO DE PASTAS OU ARQUIVOS" (se necessario)
# C.7) 'FILE-SET':
# C.8) VARIAVEL P/ "MANIPULACAO DE PASTAS OU ARQUIVOS" (se necessario)
# C.9) FUNCAO P/ "MANIPULACAO DE PASTAS OU ARQUIVOS" (se necessario)
#-------------------------------------------------------------------------------

# D) Progression_Bar is executed inside the function 'execute_maven_command_line'
# Progression_Bar is executed inside the function 'execute_maven_command_line'
# which is found inside the script: maven_functions.sh
execute_maven_command_line $SHOW_MAVEN_BASIC_LOG $SHOW_MAVEN_DEBUG_LOG "$maven_command_line_arch_generate_var"

execute_api_tests_using_maven_command_line $EXECUTE_API_TESTS $(provide_os_path) "$artifactId"

open_ide_manager $USE_IDE_ACTIVATION_MANAGER $OPEN_IDE_DIRECTLY "$IDE_EXEC_FILE_FOLDER" "$IDE_EXEC_FILE" $SHOW_INJECTION_LOG