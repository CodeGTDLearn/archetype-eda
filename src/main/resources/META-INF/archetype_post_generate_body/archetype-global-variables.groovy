// -----------------------------------------------------------------------------







// ARCHETYPE-GLOBAL-VARIABLES.GROOVY -------------------------------------------

def project_package = "${properties?.package}".replace('.', '/')

def entity_name = "${properties?.entity_name}"
def enable_denormalization = "${properties?.enable_denormalization}"

def entity_folder = "${properties?.entity_name}"
def config_folder = "config"
def properties_folder = "properties"
def map_properties_folder = "map_properties"
def list_properties_folder = "list_properties"

def show_pos_processor_log = "${properties?.show_archetype_pos_processor_log}"

def log_text_header =
        "\n\n\n-------------------------------------------------------------------------------\n" +
                "\n---------- POS-PROCESSOR  -  ARCHETYPE-POST-GENERATE LOG:  STARTING -----------\n" +
                "\n-------------------------------------------------------------------------------\n" +
                "\n\n"

def log_text_footer =
        "\n\n\n-------------------------------------------------------------------------------\n" +
                "\n----------- POS-PROCESSOR  -  ARCHETYPE-POST-GENERATE LOG:  ENDING ------------\n" +
                "\n-------------------------------------------------------------------------------\n" +
                "\n\n\n\n\n"

def log_text = log_text_header