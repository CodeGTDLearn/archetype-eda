#!/bin/bash

#!/bin/bash

progression_bar() {

  local pid=$1
  local symbol=$2
  local size_bar=$3
  local active=$4


  echo -e "\n$PROCESS_ON_GOING_MESSAGE\n"

  printf "\n"

  if $active == true; then
    percentual_mult_layer_progression_bar "$symbol" "$size_bar"
  fi
}

percentual_mult_layer_progression_bar() {

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