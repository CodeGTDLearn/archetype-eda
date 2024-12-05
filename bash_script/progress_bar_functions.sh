#!/bin/bash

add_item_in_progress_bar() {
  local input_text_prompt="$1"
  local proceed_with_function="$2"

  if [ "$proceed_with_function" = "false" ]; then
    return 1
  fi

  # **************************************************************************
  # ** O APOSTROFE É ESSENCIAL PARA ADICIONAR ITEM NA BARRA DE PROGRESSO **
  # **************************************************************************
  # This loop is responsible for extracting all occurrences of text between
  # apostrophes (single quotes).
  # The regular expression used in the while loop
  # will capture the text between the single quotes,
  # so we can add, each item beetween those single quotes,
  # it to the array PROGRESS_BAR_ITEMS.
  # The Array PROGRESS_BAR_ITEMS is used to generate each item of the progression bar.
  # Hence, eachitem in the array PROGRESS_BAR_ITEMS is a component of the progression bar.
  while [[ $input_text_prompt =~ \'([^\']+)\' ]]; do
    PROGRESS_BAR_ITEMS+=("${BASH_REMATCH[1]}")
    input_text_prompt="${input_text_prompt#*${BASH_REMATCH[0]}}"
  done
}

show_progression_bar() {

  local pid=$1
  local symbol=$2
  local size_bar=$3
  local active=$4


  echo -e "\n$PROCESS_ON_GOING_MESSAGE\n"

  printf "\n"

  if $active == true; then
    generate_percentual_mult_layer_progression_bar "$symbol" "$size_bar"
  fi
}

generate_percentual_mult_layer_progression_bar() {

  local symbol=$1
  local size_bar=$2

  # Lista de componentes definida dentro da função
  local components=("${PROGRESS_BAR_ITEMS[@]}")

  # Largura do maior componente
  local max_length_component=0
  for item in "${components[@]}"; do
    max_length_component=$(( ${#item} > max_length_component ? ${#item} : max_length_component ))
  done

  for component in "${components[@]}"; do

    local progressBarPanelMargin=2
    local proportional_size=$((max_length_component - ${#component}))
    local marginLabel=$((proportional_size + progressBarPanelMargin))
    local label=$(printf "%${marginLabel}s" "")
    label+="${component}:"

    for ((i=1; i<=size_bar; i++)); do
      printf "\r${label} %-${size_bar}s%4d%%" "$(printf "%*s" $i | tr ' ' "$symbol")" $((i * 100 / size_bar));
    done

    printf "\n"
  done
}