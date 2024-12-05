#!/bin/bash

# Função principal para analisar um arquivo JSON
get_json_properties_and_types() {
    local file_path="$1"

    # Verificar se o arquivo existe
    if [ ! -f "$file_path" ]; then
        echo "Erro: Arquivo '$file_path' não encontrado."
        return 1
    fi

    # Verificar se o jq está instalado
    if ! command -v jq &> /dev/null; then
        echo "Erro: jq não está instalado. Por favor, instale-o primeiro."
        return 1
    fi

    # Verificar se o arquivo é um JSON válido
    if ! jq empty "$file_path" 2>/dev/null; then
        echo "Erro: O arquivo não contém um JSON válido."
        return 1
    fi

    # Analisar o JSON usando jq para obter as propriedades
    local result=""
    local first=true

    # Processar as propriedades do primeiro nível
    while IFS= read -r property; do
        # Garantir que a chave seja extraída corretamente
        local key=$(echo "$property" | cut -d ':' -f1 | tr -d '"')

        # Ignorar chaves vazias
        if [ -z "$key" ]; then
            continue
        fi

        # Obter o tipo do valor com tratamento de erro
        local value_type
        value_type=$(jq -r ".$key | type" "$file_path" 2>/dev/null)

        # Verificar se o tipo foi obtido corretamente
        if [ -z "$value_type" ] || [ "$value_type" = "null" ]; then
            value_type="null"
        fi

        case "$value_type" in
            "array")
                # Verificar se o array está vazio
                local array_length=$(jq -r ".$key | length" "$file_path")

                if [ "$array_length" -eq 0 ]; then
                    # Array vazio - assumir List<String> como padrão
                    if [ "$first" = true ]; then
                        result="List<String>:$key"
                        first=false
                    else
                        result="$result, List<String>:$key"
                    fi
                else
                    # Verificar se contém objetos
                    local has_objects=$(jq -r ".$key | map(type) | map(. == \"object\") | any" "$file_path" 2>/dev/null)

                    if [ "$has_objects" = "true" ]; then
                        # Array de objetos - obter nome singular da chave
                        local singular_name
                        if [[ "$key" == *s ]]; then
                            singular_name=$(echo "$key" | sed 's/s$//')
                        else
                            singular_name="$key"
                        fi
                        # Capitalizar a primeira letra
                        local capitalized=$(echo "$singular_name" | sed 's/^./\U&/g')

                        if [ "$first" = true ]; then
                            result="List<$capitalized>:$key"
                            first=false
                        else
                            result="$result, List<$capitalized>:$key"
                        fi
                    else
                        # Array de tipos primitivos - verificar primeiro elemento com tratamento de erro
                        local first_elem_type
                        first_elem_type=$(jq -r ".$key | .[0] | type" "$file_path" 2>/dev/null)

                        # Se não conseguir determinar o tipo, assumir String
                        if [ -z "$first_elem_type" ] || [ "$first_elem_type" = "null" ]; then
                            first_elem_type="string"
                        fi

                        # Converter tipo jq para tipo Java
                        local java_type
                        case "$first_elem_type" in
                            "number")
                                # Verificar se é decimal com tratamento de erro
                                if jq -e ".$key | .[0] | tostring | test(\"\\\\.\") " "$file_path" > /dev/null 2>&1; then
                                    java_type="Double"
                                else
                                    java_type="Integer"
                                fi
                                ;;
                            "boolean")
                                java_type="Boolean"
                                ;;
                            *)
                                java_type="String"
                                ;;
                        esac

                        if [ "$first" = true ]; then
                            result="List<$java_type>:$key"
                            first=false
                        else
                            result="$result, List<$java_type>:$key"
                        fi
                    fi
                fi
                ;;

            "object")
                # Objeto aninhado - capitalizar nome da propriedade para o tipo com verificação de segurança
                local obj_name
                obj_name=$(echo "$key" | sed 's/^./\U&/g')

                # Garantir que o nome do objeto não esteja vazio
                if [ -z "$obj_name" ]; then
                    obj_name="Object"
                fi

                if [ "$first" = true ]; then
                    result="$obj_name:$key"
                    first=false
                else
                    result="$result, $obj_name:$key"
                fi
                ;;

            "boolean")
                if [ "$first" = true ]; then
                    result="Boolean:$key"
                    first=false
                else
                    result="$result, Boolean:$key"
                fi
                ;;

            "number")
                # Determinar se é inteiro ou decimal com tratamento de erro
                local is_decimal
                is_decimal=$(jq -e ".$key | tostring | test(\"\\\\.\") " "$file_path" 2>/dev/null)

                if [ "$?" -eq 0 ] && [ "$is_decimal" = "true" ]; then
                    if [ "$first" = true ]; then
                        result="Double:$key"
                        first=false
                    else
                        result="$result, Double:$key"
                    fi
                else
                    if [ "$first" = true ]; then
                        result="Integer:$key"
                        first=false
                    else
                        result="$result, Integer:$key"
                    fi
                fi
                ;;

            "string")
                # Verificar se é um formato especial (data, hora, etc.) com tratamento de erro
                local value
                value=$(jq -r ".$key" "$file_path" 2>/dev/null)

                # Garantir que value não esteja vazio
                if [ -z "$value" ]; then
                    if [ "$first" = true ]; then
                        result="String:$key"
                        first=false
                    else
                        result="$result, String:$key"
                    fi
                    continue
                fi

                # Verificar padrões de data/hora/UUID com cuidado extra para evitar falsos positivos
                if [[ "$value" =~ ^[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2} ]]; then
                    if [ "$first" = true ]; then
                        result="LocalDateTime:$key"
                        first=false
                    else
                        result="$result, LocalDateTime:$key"
                    fi
                elif [[ "$value" =~ ^[0-9]{4}-[0-9]{2}-[0-9]{2}$ ]]; then
                    if [ "$first" = true ]; then
                        result="LocalDate:$key"
                        first=false
                    else
                        result="$result, LocalDate:$key"
                    fi
                elif [[ "$value" =~ ^[0-9]{2}:[0-9]{2}:[0-9]{2}$ ]]; then
                    if [ "$first" = true ]; then
                        result="LocalTime:$key"
                        first=false
                    else
                        result="$result, LocalTime:$key"
                    fi
                elif [[ "$value" =~ ^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$ ]]; then
                    if [ "$first" = true ]; then
                        result="UUID:$key"
                        first=false
                    else
                        result="$result, UUID:$key"
                    fi
                else
                    if [ "$first" = true ]; then
                        result="String:$key"
                        first=false
                    else
                        result="$result, String:$key"
                    fi
                fi
                ;;

            *)
                # Null ou outros tipos não reconhecidos - usar String como padrão
                if [ "$first" = true ]; then
                    result="String:$key"
                    first=false
                else
                    result="$result, String:$key"
                fi
                ;;
        esac
    done < <(jq -r 'keys[] | . + ":"' "$file_path" 2>/dev/null)

    # Verificar se algum resultado foi gerado
    if [ -z "$result" ]; then
        echo "Erro: Não foi possível extrair propriedades do JSON."
        return 1
    fi

    echo "$result"
}