package eda.postgenerate.pre_processamento_maven_flags_via_llm.presentation

/**
 * Console boundary of the CLI.
 *
 * Responsibility: stdout/stderr formatting and flushing. The application
 * decides *what* to print; this class decides *where* it goes.
 */
class CliOutput {

    private final PrintStream out
    private final PrintStream err

    CliOutput() {
        this(System.out, System.err)
    }

    CliOutput(PrintStream out, PrintStream err) {
        this.out = out
        this.err = err
    }

    void commandLine(String line) {
        out.println(line)
        out.flush()
    }

    void reportHeader(boolean hit) {
        err.println("## Relatorio de Proveniencia - cache: " + (hit ? "HIT" : "MISS"))
        err.println("")
    }

    void reportBody(String body) {
        err.println(body)
    }

    void warning(String message) {
        err.println(message)
    }

    void error(String message) {
        err.println(message)
    }

    void info(String message) {
        out.println(message)
    }
}
