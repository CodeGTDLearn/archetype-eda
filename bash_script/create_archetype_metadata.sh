#!/bin/bash

create_archetype_metadata_xml() {

  local proceed_with_function_param=$1
  local archetype_filepath_param=$2
  local archetype_filename_param=$3
  local show_debug_log_param=$4

  local divider="\n--------------------------------------------------------------------->\n"

  if [ "$proceed_with_function_param" = "false" ]; then
    return 1
  fi

  local body_folder="${archetype_filepath_param}\\archetype_descriptor_body"
  local transition_properties_filesets_file="${body_folder}\\archetype-transition.xml"
  local header_file="${body_folder}\\archetype-header.xml"
  local footer_file="${body_folder}\\archetype-footer.xml"

  local filesets_folder="${archetype_filepath_param}\\archetype_descriptor_filesets"
  local properties_folder="${archetype_filepath_param}\\archetype_descriptor_properties"

  mapfile -t filesets_filename_list_from_folder < <(ls -1 "${filesets_folder}")
  mapfile -t properties_filename_list_from_folder < <(ls -1 "${properties_folder}")

  local ordered_intermediate_files=(
  "${properties_filename_list_from_folder[@]}"
  "${transition_properties_filesets_file}"
  "${filesets_filename_list_from_folder[@]}")

  # Creating the combined array
  local ordered_intermediate_files=()

  if [[ ${#properties_filename_list_from_folder[@]} -ge 1 ]]  &&
     [[ ${#filesets_filename_list_from_folder[@]}   -ge 1 ]]; then

    local file_type="xml"
    local prefix_property="property-"
    local prefix_fileset="fileset-"
    local prefix_transition="transition"

    # 1. Add property files
    for file in "${properties_filename_list_from_folder[@]}"; do
        if [[ "$file" =~ \."${file_type}"$ ]] &&
          ([[ "$file" =~ ${prefix_property} ]] || [[ "$file" =~ ${prefix_transition} ]]); then
           ordered_intermediate_files+=("${properties_folder}/${file}")
        fi
    done

    # 2. Add the transition file
    ordered_intermediate_files+=("${transition_properties_filesets_file}")

    # 3. Add the fileset files
    for file in "${filesets_filename_list_from_folder[@]}"; do
      if [[ "$file" =~ \."${file_type}"$ ]] &&
        ([[ "$file" =~ ${prefix_fileset} ]] || [[ "$file" =~ ${prefix_transition} ]]); then
         ordered_intermediate_files+=("${filesets_folder}/${file}")
      fi
    done
  else
    echo "ERROR!!!"
    echo "No elements found in either Folders:"
    echo "a) 'archetype_descriptor_filesets' or"
    echo "b) 'archetype_descriptor_properties'"
    echo "Press any key to continue..."
    read -n 1 -s
    exit 1
  fi

  # Checks if necessary files exist
  if [[ ! -f "$header_file" ]] ||
     [[ ! -f "$transition_properties_filesets_file" ]] ||
     [[ ! -f "$footer_file" ]]; then
    echo "ERROR_BODY_FILES in ARCHETYPE_METADATA_XML - NOT FOUND!"
    echo -e "\nType any 'key' to exit."
    read -n 1 -s
    exit 1
  fi

  # Debug to check the content of each array before merging
  if [ "$show_debug_log_param" = "true" ]; then
    echo "Req_properties_files content:"
    printf '%s\n' "${properties_filename_list_from_folder[@]}"

    echo -e "\nTransition File:"
    echo "$transition_properties_filesets_file"

    echo -e "\nFile_sets_files content:"
    printf '%s\n' "${filesets_filename_list_from_folder[@]}"

    echo -e "\nOrdered Final Array:"
    printf '%s\n' "${ordered_intermediate_files[@]}"
  fi

  local final_file_content_merged="${archetype_filepath_param}\\${archetype_filename_param}"

  # Create/Overwrite the output file and add the header
  cat "$header_file" > "$final_file_content_merged"

  # Merging the content of each intermediate file (without extra tags)
  for file in "${ordered_intermediate_files[@]}"; do
      sed '1d; /^<.*>$/d' "$file" >> "$final_file_content_merged"
  done

  # Adds the footer to the final file
  cat "$footer_file" >> "$final_file_content_merged"

  if [ "$show_debug_log_param" = "true" ]; then
    echo -e "$divider"
    echo -e "\n\n-----> EXECUTING: 'create_archetype_metadata_xml function' - START \n\n"

    echo -e "\n\n-----> FINAL FILES MERGED FOR "ARCHETYPE_METADATA.XML" CREATION ------------ \n"
    for file in "${ordered_intermediate_files[@]}"; do
      echo -e "${file} \n"
    done

    echo -e "\n\n-----> ARCHETYPE_METADATA.XML CREATED ------------ \n"
    cat "$final_file_content_merged"

    echo -e "\n\n-----> EXECUTING: 'create_archetype_metadata_xml function' - END \n\n"
    echo -e "$divider"
  fi

}