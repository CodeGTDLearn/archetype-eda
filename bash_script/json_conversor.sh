# Função para verificar a existência do arquivo
verificar_arquivo() {
    local path="$1"
    local mensagem_erro="$2"
    if [[ ! -f "$path" ]]; then
        echo "$mensagem_erro"
        return 1
    fi
}

# Função para ler e interpretar o arquivo JSON
ler_json() {
    local path="$1"
    local mensagem_erro="$2"
    verificar_arquivo "$path" "$mensagem_erro" || return 1

    local json_content
    json_content=$(cat "$path")

    local propriedades
    propriedades=$(echo "$json_content" | jq 'keys | .[]')

    local resultado=""
    for propriedade in $propriedades; do
        local tipo
        tipo=$(determinar_tipo "$(echo "$json_content" | jq ".$propriedade")")
        resultado+="$tipo:$propriedade,"
    done

    echo "${resultado%,}"
}

# Função para determinar o tipo da propriedade
determinar_tipo() {
    local valor="$1"
    if [[ "$valor" == "true" || "$valor" == "false" ]]; then
        echo "Boolean"
    elif [[ "$valor" =~ ^-?[0-9]+$ ]]; then
        if (( valor >= -128 && valor <= 127 )); then
            echo "Byte"
        elif (( valor >= -32768 && valor <= 32767 )); then
            echo "Short"
        elif (( valor >= -2147483648 && valor <= 2147483647 )); then
            echo "Integer"
        else
            echo "Long"
        fi
    elif [[ "$valor" =~ ^-?[0-9]+\.[0-9]+$ ]]; then
        echo "Double"
    elif [[ "$valor" == *"{"* || "$valor" == *"["* ]]; then
        if [[ "$valor" == *"{"* ]]; then
            echo "Map"
        else
            echo "List"
        fi
    else
        echo "String"
    fi
}

# Função para obter o nome do arquivo JSON sem a extensão
nome_arquivo_json() {
    local path="$1"
    local mensagem_erro="$2"
    verificar_arquivo "$path" "$mensagem_erro" || return 1

    local nome_arquivo
    nome_arquivo=$(basename -- "$path")
    echo "${nome_arquivo%.*}"
}

# Exemplo de uso
# ler_json "caminho/para/arquivo.json" "Erro: Arquivo não encontrado."
# nome_arquivo_json "caminho/para/arquivo.json" "Erro: Arquivo não encontrado."