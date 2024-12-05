#!/bin/bash

obter_tipagem_do_java() {
  local valor="$1"
  local chave="$2"

  # Remover espaços
  valor=$(echo "$valor" | tr -d ' ')

  # Verificar o tipo do valor
  if [[ "$valor" == "true" || "$valor" == "false" ]]; then
    echo "Boolean"
  elif [[ "$valor" =~ ^-?[0-9]+$ ]]; then
    echo "Integer"
  elif [[ "$valor" =~ ^-?[0-9]+\.[0-9]+$ ]]; then
    echo "Double"
  elif [[ "$valor" == "null" ]]; then
    echo "String"
  elif [[ "$valor" =~ ^\".*\"$ ]]; then
    echo "String"
  elif [[ "$valor" =~ ^\[.*\]$ ]]; then
    echo "List"
  elif [[ "$valor" =~ ^\{.*\}$ ]]; then
    # Capitalizar a primeira letra do nome da propriedade para objetos
    local primeira_letra="${chave:0:1}"
    local resto="${chave:1}"
    echo "${primeira_letra^}${resto}"
  else
    echo "String"
  fi
}

get_json_properties_and_types() {
  local arquivo="$1"
  local show_log="$2"

  # Validar o JSON
  if ! validar_json "$arquivo"; then
    if [ "$show_log" = "true" ]; then
      echo "Erro: Formato JSON inválido" >&2
    fi
    return 1
  fi

  if [ "$show_log" = "true" ]; then
    echo -e "  ${OPTION_MARK}Json-File validated.\n" >&2
  fi

  # Ler o JSON
  local json=$(cat "$arquivo")

  # Remover espaços em branco, quebras de linha e comentários
  json=$(echo "$json" | tr -d '\n\r\t')

  # Remover as chaves externas
  json="${json#"{"}"
  json="${json%"}"}"

  # Resultado
  local resultado=""

  # Variáveis para o processamento
  local estado="chave"  # estados: chave, valor
  local chave=""
  local valor=""
  local dentro_string=0
  local nivel_chaves=0
  local nivel_colchetes=0
  local buffer=""

  for (( i=0; i<${#json}; i++ )); do
    local c="${json:$i:1}"

    # Gerenciar strings
    if [[ "$c" == '"' ]]; then
      # Verificar se é uma aspas escapada
      if [[ $i -gt 0 && "${json:$i-1:1}" == '\' ]]; then
        buffer+="$c"
        continue
      fi

      if [[ $dentro_string -eq 0 ]]; then
        dentro_string=1
      else
        dentro_string=0

        # Se estamos procurando uma chave, a string completa é a chave
        if [[ "$estado" == "chave" ]]; then
          chave="$buffer"
          buffer=""

          # Procurar o próximo ':'
          while (( i < ${#json} )); do
            i=$((i+1))
            c="${json:$i:1}"
            if [[ "$c" == ":" ]]; then
              estado="valor"
              break
            fi
          done

          continue
        fi
      fi

      buffer+="$c"
      continue
    fi

    # Se estamos dentro de uma string, adicione o caractere ao buffer
    if [[ $dentro_string -eq 1 ]]; then
      buffer+="$c"
      continue
    fi

    # Se estamos procurando um valor
    if [[ "$estado" == "valor" ]]; then
      # Início de um objeto
      if [[ "$c" == "{" ]]; then
        nivel_chaves=1
        buffer="{"

        # Capturar o objeto inteiro
        while (( i < ${#json} && nivel_chaves > 0 )); do
          i=$((i+1))
          c="${json:$i:1}"

          if [[ "$c" == "{" ]]; then
            ((nivel_chaves++))
          elif [[ "$c" == "}" ]]; then
            ((nivel_chaves--))
          fi

          buffer+="$c"
        done

        valor="$buffer"
        buffer=""

      # Início de um array
      elif [[ "$c" == "[" ]]; then
        nivel_colchetes=1
        buffer="["

        # Capturar o array inteiro
        while (( i < ${#json} && nivel_colchetes > 0 )); do
          i=$((i+1))
          c="${json:$i:1}"

          if [[ "$c" == "[" ]]; then
            ((nivel_colchetes++))
          elif [[ "$c" == "]" ]]; then
            ((nivel_colchetes--))
          fi

          buffer+="$c"
        done

        valor="$buffer"
        buffer=""

      # Outros valores (número, boolean, null)
      else
        buffer="$c"

        # Capturar até o próximo separador (vírgula ou fim do JSON)
        while (( i < ${#json} - 1 )); do
          i=$((i+1))
          c="${json:$i:1}"

          if [[ "$c" == "," ]]; then
            break
          fi

          buffer+="$c"
        done

        valor=$(echo "$buffer" | tr -d ' ')
        buffer=""
      fi

      # Processar o par chave-valor encontrado
      chave=$(echo "$chave" | tr -d '"')
      local tipo=$(obter_tipagem_do_java "$valor" "$chave")
      resultado+="$tipo:$chave, "

      # Resetar para procurar uma nova chave
      chave=""
      valor=""
      estado="chave"
      continue
    fi

    # Se não estamos dentro de uma string e não estamos processando um valor
    # simplesmente adicione o caractere ao buffer
    buffer+="$c"
  done

  # Remover a vírgula e espaço finais
  resultado="${resultado%, }"

  echo "$resultado"
}