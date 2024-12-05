#!/bin/bash

# Keywords RELATIVAS/RASTREADAS do Maven
ERROR_KEYWORD="BUILD FAILURE"
SUCESS_KEYWORD="BUILD SUCCESS"


# Configurar "CONFORME" o Archetype "INSTALADO" no 'REPO-LOCAL DO MAVEN'
# 'REPO-LOCAL DO MAVEN' localizacao: ~\.m2\repository\archetype-catalog.xml
ARCHETYPE_VERSION="0.0.1-SNAPSHOT"
ARCHETYPE_GROUP_ID="com.eda-archetype"
ARCHETYPE_ARTIFACT_ID="eda-archetype"

# Sinalizacao de 'Item', 'Topico' ou 'Opcao'
ITEM_MARK="♦"
TOPIC_MARK=" •"
OPTION_MARK="  ▪"