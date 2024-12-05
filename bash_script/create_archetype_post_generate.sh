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

  local final_file_content_merged="${archetype_filepath_param}\\${archetype_filename_param}"
  local initial_lines_qtde_to_trim_in_merging_groovy_files='6'

  local body_folder="${archetype_filepath_param}\\archetype_post_generate_body"
  local variables_folder="${archetype_filepath_param}\\archetype_post_generate_variables"
  local snippets_folder="${archetype_filepath_param}\\archetype_post_generate_snippets"

  local imports_file="${body_folder}\\archetype-imports.groovy"
  local header_file="${body_folder}\\archetype-header.groovy"
  local global_vars_file="${body_folder}\\archetype-global-variables.groovy"
  local footer_file="${body_folder}\\archetype-footer.groovy"
  local functions_file="${body_folder}\\archetype-functions.groovy"

  mapfile -t variable_filename_list_from_folder < <(ls -1 "${variables_folder}")
  mapfile -t snippets_filename_list_from_folder < <(ls -1 "${snippets_folder}")

  local ordered_final_file_list=()

  if [[ ${#variable_filename_list_from_folder[@]} -ge 1 ]]  &&
     [[ ${#snippets_filename_list_from_folder[@]} -ge 1 ]]; then

    local file_type="groovy"
    local prefix_variable="variables-"
    local prefix_snippet="snippet-"

    # 1. Add "VARIABLES-FILES" em ordered_final_file_list(lista final de tds arquivos)
    for file in "${variable_filename_list_from_folder[@]}"; do
        if [[ "$file" =~ \."${file_type}"$ ]] && [[ "$file" =~ ${prefix_variable} ]]; then
           ordered_final_file_list+=("${variables_folder}/${file}")
        fi
    done

    # 2. Add the "SNIPPET-FILES" em ordered_final_file_list(lista final de tds arquivos)
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

  # Checks if, the necessary files exist
  if [[ ! -f "$header_file" ]] ||
     [[ ! -f "$imports_file" ]] ||
     [[ ! -f "$global_vars_file" ]] ||
     [[ ! -f "$functions_file" ]] ||
     [[ ! -f "$footer_file" ]]; then
    echo "ERROR_BODY_FILES in ARCHETYPE_POST_GENERATE_GROOVY - NOT FOUND!"
    echo -e "\nType any 'key' to exit."
    read -n 1 -s
    exit 1
  fi

  # 1) Create/Overriding '$final_file_content_merged file', adding the 'HEADER_FILE'
  cat "$header_file" > "$final_file_content_merged"

  # 2) Add the 'GENERAL_VARIABLES' to the 'final file'
  sed -i -e '$r '"$global_vars_file" "$final_file_content_merged"

  # 3) Add the "content of each file from ORDERED_FINAL_FILE_LIST"
  #    to the 'final file' APPENDING
  for file in "${ordered_final_file_list[@]}"; do
      sed -i -e '$r '"$file" "$final_file_content_merged"
  done

  # 4) Adds the 'FOOTER_FILE' to the 'final file' (APPENDING)
  sed -i -e '$r '"$footer_file" "$final_file_content_merged"

  # 5) Adds the 'FUNCTION_FILE' to the 'final file' (APPENDING)
  sed -i -e '$r '"$functions_file" "$final_file_content_merged"

  #NAO DELETAR ESTE LOG COMENTADO - ELE PODE SER UTILIZADO
#  if [ "$show_debug_log_param" = "true" ]; then
#     echo "BEGIN ---- FINAL-CONTENT 'BEFORE' 'REMOVING IMPORTS KEYWORD':"
#     cat "$final_file_content_merged"
#     echo "END   ---- FINAL-CONTENT 'BEFORE' 'REMOVING IMPORTS KEYWORD':"
#  fi

  # 6) Removing 'IMPORT KEYWORD' lines from the 'final file'
  #    and storing the result in 'free_imports_content'
  free_imports_content=$(remove_lines_with_import_keyword "$final_file_content_merged" "$show_debug_log_param")

  #NAO DELETAR ESTE LOG COMENTADO - ELE PODE SER UTILIZADO
#  if [ "$show_debug_log_param" = "true" ]; then
#     echo "BEGIN ---- FINAL-CONTENT 'AFTER' 'REMOVING IMPORTS KEYWORD':"
#     echo "$free_imports_content"
#     echo "END   ---- FINAL-CONTENT 'AFTER' 'REMOVING IMPORTS KEYWORD':"
#  fi

  # 7) Adding 'IMPORT KEYWORD FILE' in 'final file' "OVERRIDING THE FINAL FILE"
  cat "$imports_file" | tr -d '/' > "$final_file_content_merged"

  # 8) Adding 'FREE IMPORTS CONTENT' APPENDING in the 'final file' above overrided
  echo "$free_imports_content" >> "$final_file_content_merged"

  if [ "$show_debug_log_param" = "true" ]; then
    echo -e "$divider"
    echo -e "\n\n-----> EXECUTING: 'CREATE_ARCHETYPE_POST_GENERATE_GROOVY.SH function' - START \n\n"

    echo -e "\n\n-----> \"ARCHETYPE_POST-GENERATE.GROOVY\" created ------------ \n"
    cat "$final_file_content_merged"

    echo -e "\n\n-----> Files MERGED for 'ARCHETYPE_POST-GENERATE.GROOVY' creation ------------ \n"
    for file in "${ordered_final_file_list[@]}"; do
      echo -e "${file}"
    done

    echo -e "\n\n-----> EXECUTING: 'CREATE_ARCHETYPE_POST_GENERATE_GROOVY.SH function' - END \n\n"
    echo -e "$divider"
  fi
}

remove_lines_with_import_keyword() {
  local file_path=$1
  local show_debug_log_param=$2

  local divider="\n-------------------------------------------------------------------------------->\n"

  if [ "$show_debug_log_param" = "true" ]; then
    echo -e >&2 "
$divider
-----> EXECUTING: 'REMOVE_LINES_WITH_IMPORT_KEYWORD function' - START
--> TARGET-FILE:
$file_path"
  fi

  local temp_content=""

  # Verificar se o arquivo existe
  if [[ ! -f "$file_path" ]]; then
    echo "Target-File not found: $file_path" >&2
    return 1
  fi

  local found_import_lines=false
  local line_number=0
  local total_import_lines_found=0

  # Garantir que o arquivo($file_path) tenha uma quebra de linha no final
  # Isso é importante para garantir que a última linha seja lida,
  # e nao ignorada pelo comando 'read'(proximo 'while'), pois
  # o read ignora a ultima linha, se nao houver uma quebra de linha nela
  if [[ $(tail -c 1 "$file_path" | wc -l) -eq 0 ]]; then
    echo >> "$file_path"  # Adiciona uma quebra de linha ao final, se necessário
  fi

  # Processar o arquivo linha por linha
  while IFS= read -r line; do
    line_number=$((line_number + 1))

    # Verificar se a linha começa com 'import', '//import' ou '// import'
    # (ignorando maiúsculas e minúsculas e espaços)
    if [[ "$line" =~ ^[[:space:]]*(import|//[[:space:]]*import) ]]; then
      total_import_lines_found=$((total_import_lines_found + 1))
      found_import_lines=true
      echo -e "\nLine $line_number: 'Import' detected - Type: $line" >&2
      continue  # Ignorar esta linha, não adiciona-la ao '$temp_file'
    fi

    temp_content+="$line"$'\n'
  done < "$file_path"

  # Se nenhuma linha de import for encontrada, avisar
if [ "$show_debug_log_param" = "true" ] && [ "$found_import_lines" = false ]; then
  echo -e "\n\nNo 'import lines' were detected." >&2
  echo -e "\n------------------------------------------------------------------------ END" >&2
  echo "$temp_content"
  return 0
fi

  if [ "$show_debug_log_param" = "true" ]; then
    echo -e >&2 "
-----> Total 'Import Lines' found: $((total_import_lines_found))
-----> 'Import Lines' SUCCESSFULLY REMOVED.
------------------------------------------------------------------------ END
$divider"
  fi
  echo "$temp_content"
}