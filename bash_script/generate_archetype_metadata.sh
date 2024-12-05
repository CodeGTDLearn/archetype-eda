#!/bin/bash

create_archetype_metadata() {

  local proceed_with_function_param=$1
  local archetype_metadata_file_path_param=$2
  local archetype_metadata_file_name_param=$3
  local show_debug_log_param=$4

  local divider="\n--------------------------------------------------------------------->\n"

  if [ "$proceed_with_function_param" = "false" ]; then
    return 1
  fi

  local archetype_body_folder="${archetype_metadata_file_path_param}\\archetype_descriptor_body"
  local transition_properties_filesets_file="${archetype_body_folder}\\archetype-transition.xml"
  local header_file="${archetype_body_folder}\\archetype-header.xml"
  local footer_file="${archetype_body_folder}\\archetype-footer.xml"

  local file_sets_folder="${archetype_metadata_file_path_param}\\archetype_descriptor_filesets"
  local req_properties_folder="${archetype_metadata_file_path_param}\\archetype_descriptor_properties"

  mapfile -t file_sets_files < <(ls -1 "${file_sets_folder}")
  mapfile -t req_properties_files < <(ls -1 "${req_properties_folder}")
  local ordered_intermediate_files=("${req_properties_files[@]}" "${transition_properties_filesets_file}" "${file_sets_files[@]}")

  # Creating the combined array
  local ordered_intermediate_files=()

  # 1. Add property files
  for file in "${req_properties_files[@]}"; do
      if [[ "$file" =~ \.xml$ ]] &&
         ( [[ "$file" =~ property- ]] ||
           [[ "$file" =~ fileset- ]] ||
           [[ "$file" =~ transition ]] ||
           [[ "$file" =~ header ]] ||
           [[ "$file" =~ footer ]]
         ); then
         ordered_intermediate_files+=("${req_properties_folder}/${file}")
      fi
  done

  # 2. Add the transition file
  ordered_intermediate_files+=("${transition_properties_filesets_file}")

  # 3. Add the fileset files
  for file in "${file_sets_files[@]}"; do
      if [[ "$file" =~ \.xml$ ]] &&
         ( [[ "$file" =~ property- ]] ||
           [[ "$file" =~ fileset- ]] ||
           [[ "$file" =~ transition ]] ||
           [[ "$file" =~ header ]] ||
           [[ "$file" =~ footer ]]
         ); then
         ordered_intermediate_files+=("${file_sets_folder}/${file}")
      fi
  done

  # Debug to check the content of each array before merging
  if [ "$show_debug_log_param" = "true" ]; then
    echo "Req_properties_files content:"
    printf '%s\n' "${req_properties_files[@]}"

    echo -e "\nTransition File:"
    echo "$transition_properties_filesets_file"

    echo -e "\nFile_sets_files content:"
    printf '%s\n' "${file_sets_files[@]}"

    echo -e "\nOrdered Final Array:"
    printf '%s\n' "${ordered_intermediate_files[@]}"
  fi

  local output_file="${archetype_metadata_file_path_param}\\${archetype_metadata_file_name_param}"

  # Checks if necessary files exist
  if [[ ! -f "$header_file" ]] ||
     [[ ! -f "$transition_properties_filesets_file" ]] ||
     [[ ! -f "$footer_file" ]]; then
    echo "ERROR_BODY_FILES - NOT FOUND - ${ARCHETYPE_BODY_ITEMS_NOT_FOUND_MESSAGE}${archetype_metadata_file_path_param}!"
    echo -e "\nType any 'key' to exit."
    read -n 1 -s
    exit 1
  fi

  # Create/Overwrite the output file and add the header
  cat "$header_file" > "$output_file"

  # Merging the content of each intermediate file (without extra tags)
  for file in "${ordered_intermediate_files[@]}"; do
      sed '1d; /^<.*>$/d' "$file" >> "$output_file"
  done

  # Adds the footer to the final file
  cat "$footer_file" >> "$output_file"

  if [ "$show_debug_log_param" = "true" ]; then
    echo -e "$divider"
    echo -e "\n\n-----> EXECUTING: 'create_archetype_metadata function' - START \n\n"

    echo -e "\n\n-----> FINAL FILES MERGED FOR ARCHETYPE_METADATA.XML CREATION ------------ \n"
    for file in "${ordered_intermediate_files[@]}"; do
      echo -e "${file} \n"
    done

    echo -e "\n\n-----> ARCHETYPE_METADATA.XML CREATED ------------ \n"
    cat "$output_file"

    echo -e "\n\n-----> EXECUTING: 'create_archetype_metadata function' - END \n\n"
    echo -e "$divider"
  fi

  current_dir=$(pwd)
  cd /c/WORKBASE/DEV/SCRIPTS/archetype-v2-eda || return 1
  mvn clean install
  cd $current_dir || return 1

}