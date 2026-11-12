// -----------------------------------------------------------------------------







// BODY-03-LOGGING-VARIABLES.GROOVY ------------------------------------------
// LOG INFRASTRUCTURE ----------------------------------------------------------
// Owns the report buffer of the post-processor. The flag that enables the
// report is bound by variables-22-show_archetype_post_generate_log.groovy.

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

/**
 * The trace lines of the helpers are gated by the
 * show_archetype_post_generate_log flag. The flag is read from the BINDING and
 * not from the script local: a `def` at script level lives inside run() and is
 * not visible to the helper methods.
 */
def boolean postGenerateLogEnabled() {
    def binding = getBinding()
    if (binding == null || !binding.hasVariable('show_archetype_post_generate_log')) return false
    return binding.getVariable('show_archetype_post_generate_log')?.toString() == 'true'
}
