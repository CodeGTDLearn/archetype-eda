#!/bin/bash

create_archetype_metadata() {

  local proceed_with_function=$1
  local archetype_metadata_path=$2
  local archetype_metadata_file=$3

  local body_items="${archetype_metadata_path}/archetype_descriptor_body"
  local file_sets="${archetype_metadata_path}/archetype_descriptor_filesets"
  local req_properties="${archetype_metadata_path}/archetype_descriptor_properties"

  local header_file="${archetype_metadata_path}/${body_items}/archetype-header.xml"
  local footer_file="${archetype_metadata_path}/${body_items}/archetype-footer.xml"
  local ordered_intermediate_files=(
    "${archetype_metadata_path}/${req_properties}/property-entity.xml"
    "${archetype_metadata_path}/${req_properties}/property-general.xml"
    "${archetype_metadata_path}/${req_properties}/property-dependencies.xml"
    "${archetype_metadata_path}/${req_properties}/property—logback.xml"
    "${archetype_metadata_path}/${req_properties}/property—cpanel.xml"
    "${archetype_metadata_path}/${req_properties}/property-authentication.xml"
    "${archetype_metadata_path}/${req_properties}/property-test.xml"
    "${archetype_metadata_path}/${body_items}/archetype-transition.xml"
    "${archetype_metadata_path}/${file_sets}/fileset-cpanel.xml"
    "${archetype_metadata_path}/${file_sets}/fileset-dependencies.xml"
    "${archetype_metadata_path}/${file_sets}/fileset-general.xml"
    "${archetype_metadata_path}/${file_sets}/fileset-logback.xml"
    "${archetype_metadata_path}/${file_sets}/fileset-src.xml"
    "${archetype_metadata_path}/${file_sets}/fileset-src-test.xml"
  )

  local output_file="${archetype_metadata_path}/${archetype_metadata_file}"

  if [ "$proceed_with_function" = "false" ]; then
    #read -p "$(echo -e "\n$FINISH_PROMPT_MESSAGE")"
    return 1
  fi

  # Verifica se os arquivos necessários existem
  if [[ ! -f "$header_file" ]] || [[ ! -f "$footer_file" ]]; then
    echo "Erro: O Header ou Footer está ausente em: ${archetype_metadata_path}!"
    return 1
  fi

  for file in "${ordered_intermediate_files[@]}"; do
    if [[ ! -f "$file" ]]; then
      echo "Erro: O arquivo $file não foi encontrado!"
      return 1
    fi
  done

  # Cria/Sobreescreve o arquivo de saída e adiciona o cabeçalho
  cat "$header_file" > "$output_file"

  # Adiciona o conteúdo de cada arquivo intermediário (sem tags extras)
  for file in "${ordered_intermediate_files[@]}"; do
    sed '1d; /^<.*>$/d' "$file" >> "$output_file"
  done

  # Adiciona o rodapé ao arquivo final
  cat "$footer_file" >> "$output_file"

  #ls "$archetype_metadata_path"

  echo "File Processor ($output_file) successful merged/created in: ${archetype_metadata_path}"
}

# Exemplo de uso:
#archetype_metadata_path="../src/main/resources/META-INF/maven"
#archetype_metadata_file="archetype-metadata.xml"
#
#generate_archetype_metadata true "$archetype_metadata_path" "$archetype_metadata_file"
##!/bin/bash
#
#proceed_with_function=true
#
#if [ "$proceed_with_function" = "false" ]; then
#  read -p "$(echo -e "\n$FINISH_PROMPT_MESSAGE")"
#  exit 1
#fi
#
## Diretório onde os arquivos XML estão armazenados
#archetype_metadata_path="../src/main/resources/META-INF/maven"
#archetype_metadata_file="archetype-metadata.xml"
#
## Arquivo de saída final
#output_file="${archetype_metadata_path}/${archetype_metadata_file}"
#
## Arquivos a serem combinados
#header_file="${archetype_metadata_path}/archetype-metadata-header.xml"
#footer_file="${archetype_metadata_path}/archetype-metadata-footer.xml"
#ordered_intermediate_files=(
#"archetype-property-entity.xml"
#"archetype-property-general.xml"
#"archetype-property-dependencies.xml"
#"archetype-property—logback.xml"
#"archetype-property—cpanel.xml"
#"archetype-property-authentication.xml"
#"archetype-property-test.xml"
#"archetype-transition.xml"
#"archetype-fileset-cpanel.xml"
#"archetype-fileset-dependencies.xml"
#"archetype-fileset-general.xml"
#"archetype-fileset-logback.xml"
#"archetype-fileset-src.xml"
#"archetype-fileset-src-test.xml"
#)
#
## Verifica se os arquivos necessários existem
#if [[ ! -f "$header_file" ]] || [[ ! -f "$footer_file" ]]; then
#    echo "Erro: O Header ou Footer está ausente me: ${archetype_metadata_path}!"
#    exit 1
#fi
#
#for file in "${ordered_intermediate_files[@]}"; do
#    if [[ ! -f "$file" ]]; then
#        echo "Erro: O arquivo $file não foi encontrado em: ${archetype_metadata_path}!"
#        exit 1
#    fi
#done
#
## Cria/Sobreescreve o arquivo de saída e adiciona o cabeçalho
#cat "$header_file" > "$output_file"
#
## Adiciona o conteúdo de cada arquivo intermediário (sem tags extras)
#for file in "${ordered_intermediate_files[@]}"; do
#    sed '1d; /^<.*>$/d' "$file" >> "$output_file"
#done
#
## Adiciona o rodapé ao arquivo final
#cat "$footer_file" >> "$output_file"
#
#ls $archetype_metadata_path
#
#echo "File Processor ($output_file) successful merged/created in: ${archetype_metadata_path}"