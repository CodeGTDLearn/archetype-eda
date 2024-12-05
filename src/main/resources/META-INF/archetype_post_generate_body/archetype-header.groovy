








// ARCHETYPE-HEADER.GROOVY -----------------------------------------------------


Properties properties = request.properties

def entity_folder = "${properties?.entity_name}"
def entity_name = "${properties?.entity_name}"

// 2.0) Variaveis Operacionais exclusivas deste Script
def config_folder = "config"

def project_package = "${properties?.package}".replace('.', '/')