#!/bin/bash

execute_maven_command_line() {

  local show_basic_log=$1
  local show_debug_log=$2
  local maven_command_line_param=$3

  local show_progress_bar=$ACTIVE_PROGRESS_BAR
  local maven_output
  local debug_command_line

  local divider="\n--------------------------------------------------------------------->\n"

  print_maven_command_line() {
    local mvn_cli=$1
    echo -e "\n\n\n\n-----> MVN-COMMAND GENERATED: maven_command_line\n"
    echo -e "$mvn_cli" | sed 's/ -D/\n-D/g'
    echo -e "\n----------------------------------->\n\n"
  }

  if [ "$show_basic_log" = "true" ]; then
    echo -e "$divider"
    echo -e "\n-----> EXECUTING: 'execute_maven_command_line function' - START \n"
  fi

  local output_header="\n\n\n\n"
       output_header+="\n-------------------------------------------------------------------------------\n"
       output_header+="\n------------------------- LOG/DEBUG PROCESS - STARTING ------------------------\n"
       output_header+="\n-------------------------------------------------------------------------------\n"

  local output_footer="\n\n-------------------------------------------------------------------------------\n"
         output_footer+="\n--------------------------- LOG/DEBUG PROCESS - ENDING ------------------------\n"
         output_footer+="\n-------------------------------------------------------------------------------\n"
         output_footer+="\n\n\n\n\n"

  local output=""

# debug_log: implicitamente "POSSUI" o "BASIC_LOG"
  if [ "$show_debug_log" = "true" ]; then

    echo -e "$output_header"

    output+="$SHOW_MAVEN_DEBUG_LOG_MESSAGE"
    echo -e "\n$output\n"

    mvn_cli_switches="--batch-mode --debug"
    before_batch_switch=${maven_command_line_param%%--batch-mode*}
    after_batch_switch=${maven_command_line_param#*--batch-mode}
    debug_command_line="$before_batch_switch $mvn_cli_switches $after_batch_switch"

    maven_output=$(eval "$debug_command_line" | tee /dev/tty)

    print_maven_command_line "$maven_command_line_param"

    echo -e "$output"
    echo -e "$output_footer"
  fi

# basic_log: "NAO POSSUI" o "DEBUG_LOG" implicitamente
  if [ "$show_basic_log" = "true" ] && [ "$show_debug_log" = "false" ]; then
    #output+="-------------------------------------------------------------------------------\n\n"
    output+="\n\n$SHOW_MAVEN_BASIC_LOG_MESSAGE"
    echo -e "\n\n$output\n\n"

    maven_output=$(eval "$maven_command_line_param" | tee /dev/tty)

    print_maven_command_line "$maven_command_line_param"

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

  if [ "$show_basic_log" = "true" ]; then
    echo -e "\n-----> EXECUTING: 'execute_maven_command_line function' - END \n"
    echo -e "$divider"
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

execute_mvn_clean_install() {

  local archetype_project_path_in_linux=$1
  local proceed_with_function_param=$2
  local show_maven_log_param=$3

  local divider="<------------------------------------------------------------------------->"
  local spacer="\n\n\n\n\n\n\n\n\n\n\n\n\n\n"

  if [ "$proceed_with_function_param" = "false" ]; then
    return 1
  fi

  current_dir=$(pwd)
  cd "$archetype_project_path_in_linux" || return 1

  if [ "$show_maven_log_param" = "true" ]; then

    echo -e "${spacer}Maven DEBUG - STARTING ${divider}\n"

    echo -e "${spacer}Maven DEBUG - CLEAN ${divider}\n"
    mvn clean

    echo -e "${spacer}Maven DEBUG - VERIFY ${divider}\n"
    mvn verify

    echo -e "${spacer}Maven DEBUG - CLEAN INSTALL ${divider}\n"
    mvn clean install -X

    echo -e "${spacer}Maven DEBUG - ENDING ${divider}\n"

  else
    mvn clean
    mvn verify
    mvn clean install
  fi

  cd $current_dir || return 1
}

extra_pom_dependencies_addition_input() {

  local input_example="org.aa:aa-api:1.7:test, org.bb:bb-api:1.8:runtime"

  if [[ "${enable_extra_pom_dependencies_addition,,}" =~ ^(true|t|T|TRUE|True)$ ]]; then
     extra_POM_dependencies=$(user_input_or_default_value "dependency" "$input_example" "$EXTRA_POM_DEPENDENCIES_PROMPT")
  else
     extra_POM_dependencies="false"
  fi
}