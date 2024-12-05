import java.nio.file.Path












// -----------------------------------------------------------------------------

// ARCHETYPE-GLOBAL-VARIABLES.GROOVY -------------------------------------------

def entity_folder = "${properties?.entity_name}"

def entity_name = "${properties?.entity_name}"

def config_folder = "config"

def project_package = "${properties?.package}".replace('.', '/')

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

Path srcMainJava =
        Paths.get(
                request.outputDirectory,
                request.artifactId,
                'src', 'main', 'java')

Path srcMainResources =
        Paths.get(
                request.outputDirectory,
                request.artifactId,
                'src', 'main', 'resources')

Path propertiesResourcesFolder = srcMainResources.resolve("properties")

Path srcMainJava_EntityFolder_path =
        srcMainJava
                .resolve(project_package)
                .resolve(entity_folder)

Path srcMainJava_entityConfigFolder_path =
        srcMainJava_EntityFolder_path
                .resolve(config_folder)

Path propertiesProjectFolder =
        srcMainJava_entityConfigFolder_path
                .resolve("properties")