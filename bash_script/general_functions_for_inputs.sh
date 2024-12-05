#!/bin/bash

is_compound_input_valid() {
  local input_value=$1
  local separator=$2

  local terms=(${input_value//$separator/ })

  for term in "${terms[@]}"; do
    if [ "$(is_java_reserved_word "$term")" = "true" ]; then
      echo "false"
      return
    fi
  done

  echo "true"
}