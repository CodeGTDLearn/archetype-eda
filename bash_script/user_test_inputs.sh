#!/bin/bash

# PROMPT
ENABLE_FOLDER_SRC_TEST_JAVA_PROMPT="Enable 'Tests' (src/test/java)"

# VARIAVEIS DE SWITCHES DO MVN CLI + INPUTS
enable_folder_src_test_java=$(user_input_or_default_value "boleana" "true" "$ENABLE_FOLDER_SRC_TEST_JAVA_PROMPT")