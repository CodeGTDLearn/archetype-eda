











// VARIABLE-AUTHENTICATION.GROOVY ----------------------------------------------


def keep_jwt_authentication = "${properties?.enable_jwt_authentication}"

def authentication_folder = "authentication"
//def project_security_folder = "${properties?.entity_name}".concat(config_folder)