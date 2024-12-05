#!/bin/bash

create_archetype_metadata() {

  local proceed_with_function_param=$1
  local archetype_metadata_file_path_param=$2
  local archetype_metadata_file_name_param=$3
  local show_debug_log_param=$4

  local divider="\n--------------------------------------------------------------------->\n"
  local log_file_text=""

  if [ "$proceed_with_function_param" = "false" ]; then
    return 1
  fi

  local archetype_body_folder="${archetype_metadata_file_path_param}\\archetype_descriptor_body"
  local division_properties_filesets_file="${archetype_body_folder}\\archetype-transition.xml"
  local header_file="${archetype_body_folder}\\archetype-header.xml"
  local footer_file="${archetype_body_folder}\\archetype-footer.xml"

  local file_sets_folder="${archetype_metadata_file_path_param}\\archetype_descriptor_filesets"
  local req_properties_folder="${archetype_metadata_file_path_param}\\archetype_descriptor_properties"

  mapfile -t file_sets_files < <(ls -1 "${file_sets_folder}")
  mapfile -t req_properties_files < <(ls -1 "${req_properties_folder}")
  local ordered_intermediate_files=("${req_properties_files[@]}" "${division_properties_filesets_file}" "${file_sets_files[@]}")

  # Criando o array combinado
  local ordered_intermediate_files=()

  # 1. Adiciona os arquivos de propriedades
  for file in "${req_properties_files[@]}"; do
      ordered_intermediate_files+=("${req_properties_folder}/${file}")
  done

  # 2. Adiciona o arquivo de transição
  ordered_intermediate_files+=("${division_properties_filesets_file}")

  # 3. Adiciona os arquivos de filesets
  for file in "${file_sets_files[@]}"; do
      ordered_intermediate_files+=("${file_sets_folder}/${file}")
  done

  # Debug para verificar o conteúdo de cada array antes do merge
  if [ "$show_debug_log_param" = "true" ]; then
    echo "Conteúdo de req_properties_files:"
    printf '%s\n' "${req_properties_files[@]}"

    echo -e "\nArquivo de transição:"
    echo "$division_properties_filesets_file"

    echo -e "\nConteúdo de file_sets_files:"
    printf '%s\n' "${file_sets_files[@]}"

    echo -e "\nArray final ordenado:"
    printf '%s\n' "${ordered_intermediate_files[@]}"
  fi

  local output_file="${archetype_metadata_file_path_param}\\${archetype_metadata_file_name_param}"

  # Verifica existencia dos arquivos necessários existem
  if [[ ! -f "$header_file" ]] || [[ ! -f "$footer_file" ]]; then
    echo "ERROR_BODY_FILES - NOT FOUND - ${ARCHETYPE_HEADER_OR_FOOTER_NOT_FOUND_MESSAGE}${archetype_metadata_file_path_param}!"
    echo -e "\nType any 'key' to exit."
    read -n 1 -s
    exit 1
  fi

  for file in "${ordered_intermediate_files[@]}"; do
    if [[ ! -f "$file" ]]; then
      echo "ERROR_INTERMEDIATE_FILES - NOT FOUND - ${ARCHETYPE_PROPERTY_OR_FILESET_NOT_FOUND_MESSAGE}$file"
      echo -e "\nType any 'key' to exit."
      read -n 1 -s
      exit 1
    fi
  done

  # Cria/Sobreescreve o arquivo de saída e adiciona o cabeçalho
  cat "$header_file" > "$output_file"
  log_file_text+="BODY ITEM - HEADER:\n ${header_file} \n \n"

  # Mergeando o conteúdo de cada arquivo intermediário (sem tags extras)
  for file in "${ordered_intermediate_files[@]}"; do
    log_file_text+="INTERMEDIATE ITEMS - MERGED: ${file} \n"
    sed '1d; /^<.*>$/d' "$file" >> "$output_file"
  done

  # Adiciona o rodapé ao arquivo final
  cat "$footer_file" >> "$output_file"
  log_file_text+="\n\nBODY ITEM - FOOTER: ${footer_file} \n\n\n"
  log_file_text+="FINAL FILE MERGED - "Archetype-Metadata.xml" FINISHED:\n $output_file\n"

  if [ "$show_debug_log_param" = "true" ]; then
    echo -e "$divider"
    echo -e "\n\n-----> EXECUTING: 'create_archetype_metadata function' - START \n\n"
    log_file_text=$(sed 's/\\\(\\[^n]\)/\1/g' <<< "${log_file_text}")
    echo -e "${log_file_text}"
    echo -e "\n\n-----> FINAL FILE CREATED: ARCHETYPE_METADATA.XML ------------ \n"
    cat "$output_file"
    echo -e "\n\n-----> EXECUTING: 'create_archetype_metadata function' - END \n\n"
    echo -e "$divider"
  fi

  current_dir=$(pwd)
  cd /c/WORKBASE/DEV/SCRIPTS/archetype-v2-eda || return 1
  mvn clean install
  cd $current_dir || return 1
}