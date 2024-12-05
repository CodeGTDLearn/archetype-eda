#!/bin/bash

validar_json() {
    local arquivo="$1"

    # Verificar se o arquivo existe
    if [ ! -f "$arquivo" ]; then
        echo -e "\n"
        echo "      • JSON VALIDATION ERROR: Arquivo $arquivo não encontrado."
        echo -e "\n"
        read -p "$FINISH_PROMPT_MESSAGE" -n 1 -s
        exit 1
    fi

    # Verificar se o jq está instalado
    if ! command -v jq &> /dev/null; then
        echo -e "\n"
        echo "      • JSON VALIDATION ERROR: 'jq lib' não instalada."
        echo "      • Instale usando:"
        echo "        • Linux (Ubuntu): sudo apt-get install jq"
        echo "        • Linux (CentOS): sudo yum install jq"
        echo "        • macOS: brew install jq"
        echo "        • Windows (Chocolatey): choco install jq"
        echo "        • Windows (Manual): Baixe de https://stedolan.github.io/jq/download/ e adicione ao PATH"
        echo -e "\n"
        read -p "$FINISH_PROMPT_MESSAGE" -n 1 -s
        exit 1
    fi

    # Validar JSON usando jq
    if jq empty "$arquivo" 2>/dev/null; then
        echo "  •Validating '$arquivo'... $VALID_JSON_FILE_FORMAT_MESSAGE"
        #echo -e "\n"
        return 0
    else
        echo "$INVALID_JSON_FILE_FORMAT_MESSAGE"

        # Usar jq para obter detalhes do erro
        jq -e . "$arquivo" 2>&1 | sed 's/^/  /'  # Identar as mensagens de erro

        read -p "$FINISH_PROMPT_MESSAGE" -n 1 -s
        exit 1
    fi
}