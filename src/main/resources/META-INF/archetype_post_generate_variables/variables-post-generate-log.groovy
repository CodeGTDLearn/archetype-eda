








//  VARIABLE-POST-GENERATE-LOG.GROOVY ------------------------------------------


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