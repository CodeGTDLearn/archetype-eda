#!/bin/bash

# A) GENERAL OPTIONS------------------------------------------------------------
# A.1) ATUALIZA o ARCHETYPE-METADATA 'PROCESSOR'
# qdo o 'ARCHETYPE_METADATA.XML' for alterado
RECREATE_ARCHETYPE=true
SHOW_RECREATE_ARCHETYPE_LOG=true

# A.2) LOG|DEBUG MANAGEMENT
SHOW_MAVEN_BASIC_LOG=true
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

# A.5) SCRIPT INITIALIZATION
PROJECT_FOLDER="c:\\WORKBASE\\DEV\\SCRIPTS\\archetype-v2-eda"
META_INF_MAVEN_FOLDER="${PROJECT_FOLDER}\\src\\main\\resources\\META-INF\\maven"
SCRIPT_FOLDER="${PROJECT_FOLDER}\\bash_script"
source "${SCRIPT_FOLDER}\\generate_archetype_metadata.sh"
create_archetype_metadata "${RECREATE_ARCHETYPE}" "${META_INF_MAVEN_FOLDER}" "archetype-metadata.xml" "$SHOW_RECREATE_ARCHETYPE_LOG"

source "${SCRIPT_FOLDER}\\project_viability_checker.sh"
project_folder_viability_checking

source "${SCRIPT_FOLDER}\\script_injection.sh"

# B) INJECOES DE FUNCOES/SCRIPTS OPERACIONAIS-----------------------------------
# B.1) FUNCOES ESPECIAIS
script_injection "${SCRIPT_FOLDER}" "progress_bar.sh" $SHOW_INJECTION_LOG

# B.2) FUNCOES GERAIS
script_injection "${SCRIPT_FOLDER}" "general_functions.sh" $SHOW_INJECTION_LOG

# B.3) FUNCOES DE USER-INPUT-DATA
script_injection "${SCRIPT_FOLDER}" "input_processor.sh" $SHOW_INJECTION_LOG

# B.4) FUNCAO DE CONFIGURACAO GERAL
script_injection "${SCRIPT_FOLDER}" "config.sh" $SHOW_INJECTION_LOG

# B.5) FUNCAO DE MENSAGENS GERAIS
script_injection "${SCRIPT_FOLDER}" "general_messages.sh" $SHOW_INJECTION_LOG

#-------------------------------------------------------------------------------
#            C) PASSOS P/ ADICAO DE 'NOVOS CRITERIOS', configurando:
#           * Pre-Processador: BASH_SCRIPTs (folder: "bash_script")
#    * Processador: "ARCHETYPE-METADATA".XML + "ARCHETYPE".PROPERTIES(test)
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
# C.3.1) Montagem de 'Switches -D' usando-se as 'PROMPT-ANSWERS' INPUTADAS
#        e coletadas nas 'VARIAVEIS' processadas no 'script injetado' acima
#
# C.3.2) A 'Switch -D' tera MESMO NOME da 'variavel coletada' no 'SCRIPT DE INPUT' acima
#
# C.3.3) INSERIR/CRIAR A 'SWITCH -D' no 'mvn_command_line.sh'
script_injection "${SCRIPT_FOLDER}" "mvn_command_line.sh" $SHOW_INJECTION_LOG

# C.4) Criar uma 'REQUIRED-PROPERTY':
# C.4.1) Nomeia-a de forma identica(mesmo nome) ao 'SWITCH -D', EM:
#
# C.4.2) Coloque o 'XML' criado, em 'src/main/resources/META-INF/maven/archetype_descriptor_properties'
#
# C.4.3) EXEMPLO:
#        - ex: property-cpanel.xml, contendo:
#              <requiredProperty key="enable_api_cpanel">
#                  <defaultValue></defaultValue>
#              </requiredProperty>
#
# C.4.4) RECRIAR o 'archetype-metadata.xml' setando a constante 'RECREATE_ARCHETYPE=true'
#        - Setar com 'RECREATE_ARCHETYPE=false', logo apos a 'recriacao'
#
# C.4.5) Use a 'REQUIRED-PROPERTY' c/ o VELOCITY, no 'CODIGO' (onde necessario)

# C.5) Criar a 'PROPERTY DE TESTE' do archetype, identica(mesmo nome) ao 'REQUIRED-PROPERTY', EM:
# C.5.1) src\"TEST"\resources\ "PROJECTS" \ "BASIC" \ "archetype".PROPERTIES

# C.6) Se o 'novo Criterio' realizar "MANIPULACAO DE PASTAS OU ARQUIVOS":
# C.6.1) Crie/Insira a 'pasta/arquivo' dentro do 'ARCHETYPE', no local correto, EM:
#   - src\"MAIN"\resources\ "ARCHETYPE-RESOURCES" \ src
#
# C.7) Criar um 'FILE-SET':
# C.7.1) criar o 'file-set' num 'xml'
#
# C.7.2) Coloque o 'XML' criado, em 'src/main/resources/META-INF/maven/archetype_descriptor_filesets'
#
# C.7.3) EXEMPLO:
#        - ex: fileset-cpanel.xml, contendo:
#              <fileSet filtered="true" encoding="UTF-8">
#                  <directory>ELK</directory>
#                  <includes>
#                      <include>**/*.*</include>
#                  </includes>
#              </fileSet>
#
# C.7.4) RECRIAR o 'archetype-metadata.xml' c/ a constante 'RECREATE_ARCHETYPE=true' deste script
#        - Setar com 'RECREATE_ARCHETYPE=false', logo apos a 'recriacao'

# C.8) Crie uma 'FUNCAO PARA MANIPULACAO' (se necessario)
# C.8.1) Dentro de 'src\"MAIN"\resources\ "META-INF" \ "archetype-post-generate".groovy'

# C.8.2) A VARIAVEL 'DEF' criada:
#            * ex.: def keep_docker_folder = "${properties?.enable_docker}"
#        Nomeie de form "SEMELHANTE" que as VARIAVEL:
#            * 'coletada' no 'SCRIPT DE INPUT' +
#            * 'inserida' no 'SWITCH -D' criado no 'MVN-CLI SCRIPT' (mvn_command_line.sh)
#-------------------------------------------------------------------------------

execute_maven_command_line $SHOW_MAVEN_BASIC_LOG $SHOW_MAVEN_DEBUG_LOG "$maven_command_line"

open_ide_manager $USE_IDE_ACTIVATION_MANAGER $OPEN_IDE_DIRECTLY "$IDE_EXEC_FILE_FOLDER" "$IDE_EXEC_FILE" $SHOW_INJECTION_LOG