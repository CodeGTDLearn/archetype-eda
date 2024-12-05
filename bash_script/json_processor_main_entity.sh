#!/bin/bash

validar_json() {

  local full_absolute_path_json_file="$1"

  # 1) Checar chaves de abertura e fechamento
  local primeiro_caractere=$(head -c 1 "$full_absolute_path_json_file")
  local ultimo_caractere=$(tail -c 1 "$full_absolute_path_json_file")
  if [[ "$primeiro_caractere" != "{" || "$ultimo_caractere" != "}" ]]; then
    echo "false"
    return
  fi

  # 2) Varredura de propriedades e verificação individual
  local conteudo=$(cat "$full_absolute_path_json_file")

  # Remover chaves de abertura e fechamento para facilitar a análise
  conteudo="${conteudo#"{"}"
  conteudo="${conteudo%"}"}"

  # Remover espaços e quebras de linha extras
  conteudo=$(echo "$conteudo" | tr -d ' \t\n\r')

  # Separar propriedades
  local propriedades=($(echo "$conteudo" | tr ',' '\n'))

  for propriedade in "${propriedades[@]}"; do
    # 3) Verificar formato da propriedade (chave: valor)
    if [[ ! "$propriedade" =~ ^\"[^\"]+\"\:.*$ ]]; then
      echo "false"
      return
    fi

    local chave=$(echo "$propriedade" | cut -d':' -f1)
    local valor=$(echo "$propriedade" | cut -d':' -f2-)

    # 3.1) Verificar se a chave está formatada corretamente
    if [[ ! "$chave" =~ ^\"[^\"]+\"$ ]]; then
      echo "false"
      return
    fi

    # 4) Verificar tipo do valor
    if [[ "$valor" =~ ^\"[^\"]*\"$ ]]; then
      # String
      continue

    elif [[ "$valor" =~ ^-?[0-9]+(\.[0-9]+)?$ ]]; then
      # Número
      continue

    elif [[ "$valor" == "true" || "$valor" == "false" ]]; then
      # Booleano
      continue

    elif [[ "$valor" == "null" ]]; then
      # Null
      continue

    elif [[ "$valor" =~ ^\[.*\]$ ]]; then
      # Array
      continue

    elif [[ "$valor" =~ ^\{.*\}$ ]]; then
      # Objeto
      continue

    else
      echo "false"
      return
    fi
  done

  echo "true"
}

obter_tipagem_do_java() {

  local valor="$1"
  local chave="$2" # Adicionamos a chave como parâmetro

  if [[ "$valor" == "true" || "$valor" == "false" ]]; then
    echo "Boolean"

  elif [[ "$valor" =~ ^-?[0-9]+$ ]]; then
    echo "Integer"

  elif [[ "$valor" =~ ^-?[0-9]+(\.[0-9]+)?$ ]]; then
    echo "Double"

  elif [[ "$valor" == "null" ]]; then
    echo "String"

  elif [[ "$valor" =~ ^\"(.*)\"$ ]]; then
    echo "String"

  elif [[ "$valor" =~ ^\[.*\]$ ]]; then
    # Array: nome da propriedade no singular e capitalizado
    local chave_singular="${chave%s}" # Remove o 's' final
    local primeira_letra="${chave_singular:0:1}"
    local resto="${chave_singular:1}"
    echo "${primeira_letra^^}${resto}s"

  elif [[ "$valor" =~ ^\{.*\}$ ]]; then
    # Objeto: nome da propriedade capitalizado
    local primeira_letra="${chave:0:1}"
    local resto="${chave:1}"
    echo "${primeira_letra^^}${resto}"

  else
    echo "String"
  fi
}

obter_propriedades_e_tipos() {
  
  local full_absolute_path_json_file="$1"
  local show_log="$2"

#  -----------------------------------------------------------------------------
#                            JSON VALIDACAO
#  -----------------------------------------------------------------------------
  if ! validar_json "$full_absolute_path_json_file"; then
    if [ "$show_log" = "true" ]; then
      echo "Erro: Formato JSON inválido - Funcao: obter_propriedades_e_tipos."
    fi
    return 1
  fi

  echo -e "  ${OPTION_MARK}Json-File validated.\n" >&2
#  -----------------------------------------------------------------------------
#  -----------------------------------------------------------------------------

  local conteudo=$(cat "$full_absolute_path_json_file")

  # Remover chaves de abertura e fechamento para facilitar a análise
  conteudo="${conteudo#"{"}"
  conteudo="${conteudo%"}"}"

  # Remover espaços e quebras de linha extras
  conteudo=$(echo "$conteudo" | tr -d ' \t\n\r')

  # Separar propriedades
  local propriedades=($(echo "$conteudo" | tr ',' '\n'))
  local resultado=""

  for propriedade in "${propriedades[@]}"; do
    local chave=$(echo "$propriedade" | cut -d':' -f1 | tr -d '"') # Remove as aspas da chave
    local valor=$(echo "$propriedade" | cut -d':' -f2-)
    local tipo=$(obter_tipagem_do_java "$valor" "$chave") # Passamos a chave para obter_tipagem_do_java
    resultado+="$tipo:$chave, "
  done

  echo "${resultado%, }"
}

get_json_atributes() {

  local full_absolute_path_json_file="$1"
  local show_log="$2"

  local divider="\n--------------------------------------------------------->\n"

echo -e "entrei na funcao get_json_atributes"

#  -----------------------------------------------------------------------------
#                CAPTURANDO PROPRIEDADES E TIPOS DO ARQUIVO JSON
#  -----------------------------------------------------------------------------
  local resultado=$(obter_propriedades_e_tipos "$full_absolute_path_json_file" "$show_log")
#  -----------------------------------------------------------------------------
#  -----------------------------------------------------------------------------
    echo -e "$resultado"

  if [ "$show_log" = "true" ]; then
    echo -e "$divider"
    echo -e "\n-----> Processing GET_JSON_ATRIBUTES <------ START\n"
    echo -e "$resultado"
    echo -e "\n-----> Processing GET_JSON_ATRIBUTES <------ END\n"
    echo -e "$divider"
  fi

  echo "$resultado"
}