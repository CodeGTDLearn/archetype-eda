#!/bin/bash

# 1) PROMPT
ENABLE_FOLDER_SRC_TEST_JAVA_PROMPT="${ITEM_MARK}Enable 'Api Tests' (src/test/)"

# 2) VARIAVEIS DE SWITCHES DO MVN CLI + INPUTS
enable_folder_src_test_java=$(get_user_input_or_default_value "boleana" "true" "$ENABLE_FOLDER_SRC_TEST_JAVA_PROMPT")

# 3) ADD PROMPT IN PROGRESS_BAR
add_apostrophe_item_in_progress_bar "${ENABLE_FOLDER_SRC_TEST_JAVA_PROMPT}" "${enable_folder_src_test_java}"