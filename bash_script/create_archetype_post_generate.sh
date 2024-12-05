#!/bin/bash

create_archetype_post_generate_groovy() {

  local proceed_with_function_param=$1
  local archetype_filepath_param=$2
  local archetype_filename_param=$3
  local show_debug_log_param=$4

  local divider="\n--------------------------------------------------------------------->\n"

  if [ "$proceed_with_function_param" = "false" ]; then
    return 1
  fi

#  add_enters_around_content() {
#    local var_name=$1
#    local num_enters=$2
#    local position=$3
#    local content=$(cat "$var_name")
#
#    if [ "$position" = "before" ]; then
#      printf "%*s%s\n" $num_enters "" "$content"
#    elif [ "$position" = "after" ]; then
#      printf "%s%*s\n" "$content" $num_enters ""
#    else
#      echo "Erro: posição inválida. Use 'before' ou 'after'."
#      exit 1
#    fi
#  }

  local final_file_content_merged="${archetype_filepath_param}\\${archetype_filename_param}"
  local initial_lines_to_be_deleted_from_merged_groovy_files='6'

  local body_folder="${archetype_filepath_param}\\archetype_post_generate_body"
  local variables_folder="${archetype_filepath_param}\\archetype_post_generate_variables"
  local snippets_folder="${archetype_filepath_param}\\archetype_post_generate_snippets"

  local imports_file="${body_folder}\\archetype-imports.groovy"
  local header_file="${body_folder}\\archetype-header.groovy"
  local global_vars_file="${body_folder}\\archetype-global-variables.groovy"
  local footer_file="${body_folder}\\archetype-footer.groovy"
  local function_file="${body_folder}\\archetype-functions.groovy"

  mapfile -t variable_filename_list_from_folder < <(ls -1 "${variables_folder}")
  mapfile -t snippets_filename_list_from_folder < <(ls -1 "${snippets_folder}")

  local ordered_final_file_list=()

  if [[ ${#variable_filename_list_from_folder[@]} -ge 1 ]]  &&
     [[ ${#snippets_filename_list_from_folder[@]} -ge 1 ]]; then

    local file_type="groovy"
    local prefix_variable="variables-"
    local prefix_snippet="snippet-"

    # 1. Add "VARIABLES-FILES"
    for file in "${variable_filename_list_from_folder[@]}"; do
        if [[ "$file" =~ \."${file_type}"$ ]] && [[ "$file" =~ ${prefix_variable} ]]; then
           ordered_final_file_list+=("${variables_folder}/${file}")
        fi
    done

    # 2. Add the "SNIPPET-FILES"
    for file in "${snippets_filename_list_from_folder[@]}"; do
        if [[ "$file" =~ \."${file_type}"$ ]] && [[ "$file" =~ ${prefix_snippet} ]]; then
           ordered_final_file_list+=("${snippets_folder}/${file}")
        fi
    done
  else
    echo "ERROR!!!"
    echo "No elements found in either Folders:"
    echo "a) 'archetype_post_generate_variables' or"
    echo "b) 'archetype_post_generate_snippets'"
    echo "Press any key to continue..."
    read -n 1 -s
    exit 1
  fi

  # Checks if necessary files exist
  if [[ ! -f "$header_file" ]] ||
     [[ ! -f "$imports_file" ]] ||
     [[ ! -f "$global_vars_file" ]] ||
     [[ ! -f "$function_file" ]] ||
     [[ ! -f "$footer_file" ]]; then
    echo "ERROR_BODY_FILES in ARCHETYPE_POST_GENERATE_GROOVY - NOT FOUND!"
    echo -e "\nType any 'key' to exit."
    read -n 1 -s
    exit 1
  fi


  # 1) Create/Overwrite the output file and add the 'IMPORT_FILE'
  cat "$imports_file" | tr -d '/' > "$final_file_content_merged"

  # 2) Add the 'HEADER_FILE' to the final file
#  printf '%*s' 10 $'\n' >> "$final_file_content_merged"
  sed '1,'${initial_lines_to_be_deleted_from_merged_groovy_files}'d' "$header_file" >> "$final_file_content_merged"

  # 3) Add the 'GENERAL_VARIABLES' to the final file
  sed '1,'${initial_lines_to_be_deleted_from_merged_groovy_files}'d' "$global_vars_file" >> "$final_file_content_merged"

  # 4) Merging the "content of each file from ORDERED_FINAL_FILE_LIST" (without extra tags) to the final file
  for file in "${ordered_final_file_list[@]}"; do
      sed '1,'${initial_lines_to_be_deleted_from_merged_groovy_files}'d' "$file" >> "$final_file_content_merged"
  done

  # 5) Adds the 'FOOTER_FILE' to the final file
  sed '1,'${initial_lines_to_be_deleted_from_merged_groovy_files}'d' "$footer_file" >> "$final_file_content_merged"

  # 6) Adds the 'FUNCTION_FILE' to the final file
  sed '1,'${initial_lines_to_be_deleted_from_merged_groovy_files}'d' "$function_file" >> "$final_file_content_merged"

  if [ "$show_debug_log_param" = "true" ]; then

    echo -e "$divider"
    echo -e "\n\n-----> EXECUTING: 'CREATE_ARCHETYPE_POST_GENERATE_GROOVY.SH function' - START \n\n"

    echo -e "\n\n-----> Files MERGED for 'ARCHETYPE_POST-GENERATE.GROOVY' creation ------------ \n"
    for file in "${ordered_final_file_list[@]}"; do
      echo -e "${file}"
    done

    echo -e "\n\n-----> "ARCHETYPE_POST-GENERATE.GROOVY" created ------------ \n"
    cat "$final_file_content_merged"

    echo -e "\n\n-----> EXECUTING: 'CREATE_ARCHETYPE_POST_GENERATE_GROOVY.SH function' - END \n\n"
    echo -e "$divider"

  fi

}