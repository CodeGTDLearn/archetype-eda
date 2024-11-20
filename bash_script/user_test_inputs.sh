#!/bin/bash

# PROMPT
ENABLE_FOLDER_SRC_TEST_JAVA_PROMPT="Habilitar pasta src/test/java"
ENABLE_FOLDER_SRC_TEST_RESOURCES_PROMPT="Habilitar pasta src/test/resources"

# INPUTS
enable_folder_src_test_java=$(user_input_or_default_value "boleana" "false" "$ENABLE_FOLDER_SRC_TEST_JAVA_PROMPT")
enable_folder_src_test_resources=$(user_input_or_default_value "boleana" "false" "$ENABLE_FOLDER_SRC_TEST_RESOURCES_PROMPT")