// -----------------------------------------------------------------------------








// BODY-04-GLOBAL-VARIABLES.GROOVY -------------------------------------------
// DOMAIN CONSTANTS ------------------------------------------------------------
def project_package = "${properties?.package}".replace('.', '/')

// Path alias of the flag entity_package_name (bound by
// variables-14-entity_package_name.groovy).
def entity_folder = "${properties?.entity_package_name}"

def config_folder = "config"
def properties_folder = "properties"
def map_properties_folder = "map_properties"
def list_properties_folder = "list_properties"
def authentication_folder = "authentication"
